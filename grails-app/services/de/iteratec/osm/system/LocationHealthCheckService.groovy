package de.iteratec.osm.system

import de.iteratec.osm.ConfigService
import de.iteratec.osm.batch.Activity
import de.iteratec.osm.batch.BatchActivityService
import de.iteratec.osm.batch.BatchActivityUpdater
import de.iteratec.osm.measurement.environment.Location
import de.iteratec.osm.measurement.environment.LocationWithXmlNode
import de.iteratec.osm.measurement.environment.QueueAndJobStatusService
import de.iteratec.osm.measurement.environment.WebPageTestServer
import de.iteratec.osm.report.external.GraphitePathName
import de.iteratec.osm.report.external.GraphiteServer
import de.iteratec.osm.report.external.GraphiteSocket
import de.iteratec.osm.report.external.provider.GraphiteSocketProvider
import de.iteratec.osm.result.JobResult
import grails.gorm.DetachedCriteria
import grails.transaction.Transactional
import groovy.util.slurpersupport.GPathResult
import org.joda.time.DateTime

/**
 * Functionality for getting and managing webpagetest locations health check data.
 */
@Transactional
class LocationHealthCheckService {

    public static final List<String> HEALTHCHECK_ATTRIBUTES_TO_SEND = [
            "numberOfAgents", "numberOfPendingJobsInWpt", "numberOfJobResultsLastHour", "numberOfEventResultsLastHour",
            "numberOfErrorsLastHour", "numberOfJobResultsNextHour", "numberOfEventResultsNextHour", "numberOfCurrentlyPendingJobs",
            "numberOfCurrentlyRunningJobs"
    ]
    QueueAndJobStatusService queueAndJobStatusService
    ConfigService configService
    BatchActivityService batchActivityService
    GraphiteSocketProvider graphiteSocketProvider

    /**
     * Run a health check for every active {@link Location} of every active {@link WebPageTestServer} and persist it.
     * @see LocationHealthCheckJob
     */
    public void runHealthChecksForAllActiveLocations(){

        List<LocationHealthCheck> writtenHealthChecks = []

        WebPageTestServer.findAllByActive(true).each { WebPageTestServer wptServer ->

            GPathResult getTestersResponse = queueAndJobStatusService.getAgentsHttpResponse(wptServer)
            List<LocationWithXmlNode> activeLocations = queueAndJobStatusService.getActiveLocations(wptServer)

            activeLocations.each {LocationWithXmlNode locationWithXmlNode ->
                writtenHealthChecks.add(
                    runHealthCheckForLocation(locationWithXmlNode, getTestersResponse)
                )
            }
        }

        reportToGraphiteServers(writtenHealthChecks)

    }

    public reportToGraphiteServers(List<LocationHealthCheck> writtenHealthChecks) {

        if (writtenHealthChecks.size() < 1){
            return
        }

        GraphiteServer.findByReportHealthMetrics(true).each { GraphiteServer graphiteServerToReportTo ->

            if (graphiteServerToReportTo.healthMetricsReportPrefix) {

                GraphiteSocket socket = graphiteSocketProvider.getSocket(graphiteServerToReportTo)
                writtenHealthChecks.each { LocationHealthCheck locationHealthCheck ->

                    sendMetricsOfThisCheck(socket, graphiteServerToReportTo, locationHealthCheck)

                }

            }else {
                log.warn("Couldn' send LocationHealthCheck to GraphiteServer '${graphiteServerToReportTo.toString()}' because" +
                        " no healthMetricsReportPrefix is set.")
            }
        }
    }

    private sendMetricsOfThisCheck(GraphiteSocket socket, GraphiteServer graphiteServerToReportTo, LocationHealthCheck locationHealthCheck) {
        if (locationHealthCheck?.location?.wptServer?.label && locationHealthCheck.location?.uniqueIdentifierForServer) {
            GraphitePathName prefixForThisCheck = GraphitePathName.valueOf(
                "${graphiteServerToReportTo.healthMetricsReportPrefix}" +
                ".${locationHealthCheck.location.wptServer.label}" +
                ".${locationHealthCheck.location.uniqueIdentifierForServer}"
            )
            HEALTHCHECK_ATTRIBUTES_TO_SEND.each { metric ->
                socket.sendDate(
                        GraphitePathName.valueOf("${prefixForThisCheck.toString()}.${metric}"),
                        Double.valueOf(locationHealthCheck."${metric}"),
                        locationHealthCheck.date
                )
            }
        }
    }

    public LocationHealthCheck runHealthCheckForLocation(LocationWithXmlNode locationWithXmlNode, GPathResult getTestersResponse) {

        Location location = locationWithXmlNode.location
        Object locationTagInXml = locationWithXmlNode.locationXmlNode

        List<JobResult> executingJobResults = queueAndJobStatusService.getExecutingJobResults(location)

        DateTime now = new DateTime()
        Date oneHourAgo = now.minusHours(1).toDate()
        Date oneHourFromNow = now.plusHours(1).toDate()
        Map nextHour = queueAndJobStatusService.getNumberOfJobsAndEventsDueToRunFromNowUntil(location, oneHourFromNow)

        Integer numberOfAgentsInLocation = queueAndJobStatusService.getNumberOfAgents(locationTagInXml, getTestersResponse)
        def numberOfPendingJobsFromWptServer = queueAndJobStatusService.getNumberOfPendingJobsFromWptServer(locationTagInXml)
        def numberOfJobResultsLastHour = queueAndJobStatusService.getFinishedJobResultCountSince(location, oneHourAgo)
        def numberOfEventResultsLastHour = queueAndJobStatusService.getEventResultCountBetween(location, oneHourAgo, now.toDate())
        def numberOfErrorsLastHour = queueAndJobStatusService.getErroneousJobResultCountSince(location, oneHourAgo)
        def numberOfCurrentlyPendingJobs = executingJobResults.findAll { it.httpStatusCode == 100 }.size()
        def numberOfCurrentlyRunningJobs = executingJobResults.findAll { it.httpStatusCode == 101 }.size()

        new LocationHealthCheck(
                date: now.toDate(),
                location: location,
                numberOfAgents: numberOfAgentsInLocation,
                numberOfPendingJobsInWpt: numberOfPendingJobsFromWptServer,
                numberOfJobResultsLastHour: numberOfJobResultsLastHour,
                numberOfEventResultsLastHour: numberOfEventResultsLastHour,
                numberOfErrorsLastHour: numberOfErrorsLastHour,
                numberOfJobResultsNextHour: nextHour.jobs,
                numberOfEventResultsNextHour: nextHour.events,
                numberOfCurrentlyPendingJobs: numberOfCurrentlyPendingJobs,
                numberOfCurrentlyRunningJobs: numberOfCurrentlyRunningJobs,
        ).save(failOnError: true)

    }

    /**
     * Deletes all {@link LocationHealthCheck}s older config setting internalMonitoringStorageTimeInDays.
     */
    public void cleanupHealthChecks(){

        Date toDeleteBefore = getDateToDeleteBefore()
        def dc = new DetachedCriteria(LocationHealthCheck).build {
            lt 'date', toDeleteBefore
        }
        int count = dc.count()

        String batchActivityName = "Nightly cleanup of LocationHealthChecks"
        if(count > 0 && !batchActivityService.runningBatch(LocationHealthCheck.class, batchActivityName, Activity.DELETE)) {
            BatchActivityUpdater batchActivityUpdater = batchActivityService.getActiveBatchActivity(LocationHealthCheck.class, Activity.DELETE, batchActivityName, 1, true)
            batchActivityUpdater.beginNewStage("Delete LocationHealthChecks", count)
            //batch size -> hibernate doc recommends 10..50
            int batchSize = 50
            0.step(count, batchSize) { int offset ->
                LocationHealthCheck.withNewTransaction {
                    def list = dc.list(max: batchSize)
                    list.each { LocationHealthCheck locationHealthCheck->
                        try {
                            locationHealthCheck.delete()
                            batchActivityUpdater.addProgressToStage()
                        } catch (Exception e) {
                            batchActivityUpdater.addFailures("Couldn't delete LocationHealthCheck ${locationHealthCheck.id} (${e.getMessage()})")
                        }
                    }
                }
            }
            batchActivityUpdater.done()
        }
    }

    /**
     * Gets internalMonitoringStorageTimeInDays from osm configuration.
     * @return
     */
    private Date getDateToDeleteBefore() {
        Integer internalMonitoringStorageTimeInDays = configService.getInternalMonitoringStorageTimeInDays()
        if (internalMonitoringStorageTimeInDays == null || internalMonitoringStorageTimeInDays < 1){
            throw new IllegalArgumentException("Setting 'internalMonitoringStorageTimeInDays' must be set to a positive integer!")
        }
        Date toDeleteBefore = new DateTime().minusDays(internalMonitoringStorageTimeInDays).toDate()
        return toDeleteBefore
    }
}

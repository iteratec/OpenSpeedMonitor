package de.iteratec.osm.system

import de.iteratec.osm.ConfigService
import de.iteratec.osm.batch.Activity
import de.iteratec.osm.batch.BatchActivityService
import de.iteratec.osm.batch.BatchActivityUpdater
import de.iteratec.osm.measurement.environment.Location
import de.iteratec.osm.measurement.environment.LocationWithXmlNode
import de.iteratec.osm.measurement.environment.QueueAndJobStatusService
import de.iteratec.osm.measurement.environment.WebPageTestServer
import de.iteratec.osm.result.JobResult
import grails.gorm.DetachedCriteria
import grails.transaction.Transactional
import groovy.util.slurpersupport.GPathResult
import org.joda.time.DateTime

@Transactional
class LocationHealthCheckService {

    QueueAndJobStatusService queueAndJobStatusService
    ConfigService configService
    BatchActivityService batchActivityService

    public void runHealthChecksForAllActiveLocations(){

        WebPageTestServer.findAllByActive(true).each { WebPageTestServer wptServer ->

            GPathResult getTestersResponse = queueAndJobStatusService.getAgentsHttpResponse(wptServer)
            List<LocationWithXmlNode> activeLocations = queueAndJobStatusService.getActiveLocations(wptServer)

            activeLocations.each {LocationWithXmlNode locationWithXmlNode ->
                runHealthCheckForLocation(locationWithXmlNode, getTestersResponse)
            }
        }
    }

    public void runHealthCheckForLocation(LocationWithXmlNode locationWithXmlNode, GPathResult getTestersResponse) {

        Location location = locationWithXmlNode.location
        Object locationTagInXml = locationWithXmlNode.locationXmlNode

        List<JobResult> executingJobResults = queueAndJobStatusService.getExecutingJobResults(location)

        DateTime now = new DateTime()
        Date oneHourAgo = now.minusHours(1).toDate()
        Date oneHourFromNow = now.plusHours(1).toDate()
        Map nextHour = queueAndJobStatusService.getNumberOfJobsAndEventsDueToRunFromNowUntil(location, oneHourFromNow)

        new LocationHealthCheck(
                date: now.toDate(),
                location: location,
                numberOfAgents: queueAndJobStatusService.getNumberOfAgents(locationTagInXml, getTestersResponse),
                numberOfPendingJobsInWpt: queueAndJobStatusService.getNumberOfPendingJobsFromWptServer(locationTagInXml),
                numberOfJobResultsLastHour: queueAndJobStatusService.getFinishedJobResultCountSince(location, oneHourAgo),
                numberOfEventResultsLastHour: queueAndJobStatusService.getEventResultCountBetween(location, oneHourAgo, now.toDate()),
                numberOfErrorsLastHour: queueAndJobStatusService.getErroneousJobResultCountSince(location, oneHourAgo),
                numberOfJobResultsNextHour: nextHour.jobs,
                numberOfEventResultsNextHour: nextHour.events,
                numberOfCurrentlyPendingJobs: executingJobResults.findAll { it.httpStatusCode == 100 }.size(),
                numberOfCurrentlyRunningJobs: executingJobResults.findAll { it.httpStatusCode == 101 }.size(),
        ).save(failOnError: true)

    }

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
                            batchActivityUpdater.addFailures("Couldn't delete LocationHealthCheck ${locationHealthCheck.id}")
                        }
                    }
                }
            }
            batchActivityUpdater.done()
        }
    }

    private Date getDateToDeleteBefore() {
        Integer internalMonitoringStorageTimeInDays = configService.getInternalMonitoringStorageTimeInDays()
        if (internalMonitoringStorageTimeInDays == null || internalMonitoringStorageTimeInDays < 1){
            throw new IllegalArgumentException("Setting 'internalMonitoringStorageTimeInDays' must be set to a positive integer!")
        }
        Date toDeleteBefore = new DateTime().minusDays(internalMonitoringStorageTimeInDays).toDate()
        return toDeleteBefore
    }
}

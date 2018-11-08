package de.iteratec.osm.measurement.environment
import de.iteratec.osm.annotations.RestAction
import de.iteratec.osm.measurement.schedule.Job
import de.iteratec.osm.result.JobResult
import de.iteratec.osm.result.WptStatus
import de.iteratec.osm.system.LocationHealthCheck
import de.iteratec.osm.system.LocationHealthCheckDaoService
import de.iteratec.osm.util.ControllerUtils
import grails.validation.Validateable

class QueueDashboardController {

    LocationHealthCheckDaoService locationHealthCheckDaoService
    QueueAndJobStatusService queueAndJobStatusService

    def index() { }

    def getActiveWptServer()
    {
        List<WebPageTestServer> list = WebPageTestServer.findAllByActive(true)
        return ControllerUtils.sendObjectAsJSON(response, list )
    }

    @RestAction
    def getWptServerInformation(Long id)
    {
        if(id == null) {
            return emptyResponse()
        }
        WebPageTestServer wptserver = WebPageTestServer.findById(id)

        if(wptserver != null){
            List<Location> listLocation = Location.findAllByActiveAndWptServer(true, wptserver)
            List<LocationHealthCheck> healthChecks = locationHealthCheckDaoService.getLatestHealthChecksFor(listLocation)
            List<Map> listLocationInfo = new ArrayList<Map>()

            listLocation.forEach( {Location location ->

                List<JobResult> executingJobResults
                executingJobResults = queueAndJobStatusService.getExecutingJobResults(location)

                Map<Job, List<JobResult>> executingJobs
                executingJobs = queueAndJobStatusService.aggregateJobs(executingJobResults)

                LocationHealthCheck healthCheck = healthChecks.findAll{ it.location == location }[0]

                Map map = buildMap(location, healthCheck, executingJobResults, executingJobs)
                listLocationInfo.add(map)
            } )
            return ControllerUtils.sendObjectAsJSON(response, listLocationInfo)
        }
        return emptyResponse()
    }

    private Map buildMap(Location location, LocationHealthCheck healthCheck, List<JobResult> executingJobResults, Map<Job, List<JobResult>> executingJobs){
        return [
                id                  : location?.uniqueIdentifierForServer,
                lastHealthCheckDate : healthCheck?.date?.toString(),
                label               : location?.location,
                agents              : healthCheck?.numberOfAgents,
                jobs                : healthCheck?.numberOfPendingJobsInWpt,
                eventResultsLastHour: healthCheck?.numberOfEventResultsLastHour,
                jobResultsLastHour  : healthCheck?.numberOfJobResultsLastHour,
                errorsLastHour      : healthCheck?.numberOfErrorsLastHour,
                jobsNextHour        : healthCheck?.numberOfJobResultsNextHour,
                eventsNextHour      : healthCheck?.numberOfEventResultsNextHour,
                executingJobs       : executingJobs?.values(),
                pendingJobs         : executingJobResults?.findAll {
                it.httpStatusCode == WptStatus.PENDING.getWptStatusCode() }?.size(),
                runningJobs         : executingJobResults?.findAll {
                it.httpStatusCode == WptStatus.RUNNING.getWptStatusCode() }?.size()
        ]
    }

    def emptyResponse()
    {
        response.status = 404
    }
}

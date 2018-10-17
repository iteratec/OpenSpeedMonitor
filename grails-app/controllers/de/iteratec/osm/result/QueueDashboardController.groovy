package de.iteratec.osm.result

import de.iteratec.osm.measurement.environment.Location
import de.iteratec.osm.measurement.environment.QueueAndJobStatusService
import de.iteratec.osm.measurement.environment.WebPageTestServer
import de.iteratec.osm.measurement.schedule.Job
import de.iteratec.osm.system.LocationHealthCheck
import de.iteratec.osm.system.LocationHealthCheckDaoService
import de.iteratec.osm.util.ControllerUtils

class QueueDashboardController {

    LocationHealthCheckDaoService locationHealthCheckDaoService
    QueueAndJobStatusService queueAndJobStatusService

    def index() { }

    def getActiveWptServer()
    {
        List<WebPageTestServer> list = WebPageTestServer.findAllByActive(true)
        return ControllerUtils.sendObjectAsJSON(response, list )
    }

    def getWptServerInformation()
    {
        if(!params.containsKey("id")){
            return emptyResponse()
        }

        long wptid
        try{
            wptid = Long.parseLong( params["id"] )
        }
        catch (Exception e) {
            return emptyResponse()
        }

        WebPageTestServer wptserver = WebPageTestServer.findById(wptid)

        if(wptserver != null){
            try {
                List<Location> listLocation = Location.findAllByActiveAndWptServer(true, wptserver)
                List<LocationHealthCheck> healthChecks = locationHealthCheckDaoService.getLatestHealthChecksFor(listLocation)
                List<Map> listLocationInfo = new ArrayList<Map>()

                listLocation.forEach( {Location location ->

                    List<JobResult> executingJobResults
                    executingJobResults = queueAndJobStatusService.getExecutingJobResults(location)
                    println( executingJobResults )

                    Map<Job, List<JobResult>> executingJobs
                    executingJobs = queueAndJobStatusService.aggregateJobs(executingJobResults)
                    println( executingJobs )

                    LocationHealthCheck healthCheck = healthChecks.findAll{ it.location == location }[0]

                    Map map = buildMap(location, healthCheck, executingJobResults, executingJobs)
                    listLocationInfo.add(map)
                } )
                return ControllerUtils.sendObjectAsJSON(response, listLocationInfo)
            }
            catch (Exception e){
                e.printStackTrace()
                return emptyResponse()
            }
        }
        return emptyResponse()
    }

    Map buildMap(Location location,
                         LocationHealthCheck healthCheck,
                         List<JobResult> executingJobResults,
                         Map<Job, List<JobResult>> executingJobs){
        return [
            id                  : location ? location.uniqueIdentifierForServer : "",
            lastHealthCheckDate : healthCheck ? healthCheck.date.toString() : "",
            label               : location ? location.location : "",
            agents              : healthCheck ? healthCheck.numberOfAgents : -1,
            jobs                : healthCheck ? healthCheck.numberOfPendingJobsInWpt : -1,

            eventResultsLastHour: healthCheck ? healthCheck.numberOfEventResultsLastHour : -1,
            jobResultsLastHour  : healthCheck ? healthCheck.numberOfJobResultsLastHour : -1,
            errorsLastHour      : healthCheck ? healthCheck.numberOfErrorsLastHour : -1,

            jobsNextHour        : healthCheck ? healthCheck.numberOfJobResultsNextHour : -1,
            eventsNextHour      : healthCheck ? healthCheck.numberOfEventResultsNextHour : -1,

            executingJobs       : executingJobs ? executingJobs.values() : [],

            pendingJobs         : executingJobResults ? executingJobResults.findAll {
                it.httpStatusCode == WptStatus.PENDING.getWptStatusCode() }.size() : 0,

            runningJobs         : executingJobResults ? executingJobResults.findAll {
                it.httpStatusCode == WptStatus.RUNNING.getWptStatusCode() }.size() : 0
        ]
    }

    def emptyResponse()
    {
        return ControllerUtils.sendObjectAsJSON(response, [])
    }
}

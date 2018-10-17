package de.iteratec.osm.measurement.environment

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

                    Map<Job, List<JobResult>> executingJobs
                    executingJobs = queueAndJobStatusService.aggregateJobs(executingJobResults)

                    LocationHealthCheck healthCheck = healthChecks.findAll{ it.location == location }[0]

                    DefaultQueueDashboardCommand command = new DefaultQueueDashboardCommand()
                    command.location = location
                    command.healthCheck = healthCheck
                    command.executingJobResults = executingJobResults
                    command.executingJobs = executingJobs
                    Map map = buildMap(command)
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

    Map buildMap(DefaultQueueDashboardCommand command){
        return [
                id                  : command.location ? command.location.uniqueIdentifierForServer : "",
                lastHealthCheckDate : command.healthCheck ? command.healthCheck.date.toString() : "",
                label               : command.location ? command.location.location : "",
                agents              : command.healthCheck ? command.healthCheck.numberOfAgents : -1,
                jobs                : command.healthCheck ? command.healthCheck.numberOfPendingJobsInWpt : -1,
                eventResultsLastHour: command.healthCheck ? command.healthCheck.numberOfEventResultsLastHour : -1,
                jobResultsLastHour  : command.healthCheck ? command.healthCheck.numberOfJobResultsLastHour : -1,
            errorsLastHour      : command.healthCheck ? command.healthCheck.numberOfErrorsLastHour : -1,
            jobsNextHour        : command.healthCheck ? command.healthCheck.numberOfJobResultsNextHour : -1,
            eventsNextHour      : command.healthCheck ? command.healthCheck.numberOfEventResultsNextHour : -1,
            executingJobs       : command.executingJobs ? command.executingJobs.values() : [],
            pendingJobs         : command.executingJobResults ? command.executingJobResults.findAll {
                it.httpStatusCode == WptStatus.PENDING.getWptStatusCode() }.size() : 0,
            runningJobs         : command.executingJobResults ? command.executingJobResults.findAll {
                it.httpStatusCode == WptStatus.RUNNING.getWptStatusCode() }.size() : 0
        ]
    }

    def emptyResponse()
    {
        return ControllerUtils.sendObjectAsJSON(response, [])
    }
}

class DefaultQueueDashboardCommand implements Validateable {
    Location location
    LocationHealthCheck healthCheck
    List<JobResult> executingJobResults
    Map<Job, List<JobResult>> executingJobs

    static constrains = {
        location(nullable: true)
        healthCheck(nullable: true)
        executingJobResults(nullable: true)
        executingJobs(nullable: true)
    }
}
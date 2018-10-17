package de.iteratec.osm.result

import de.iteratec.osm.measurement.environment.Location
import de.iteratec.osm.measurement.schedule.Job
import de.iteratec.osm.system.LocationHealthCheck
import grails.buildtestdata.mixin.Build
import grails.testing.web.controllers.ControllerUnitTest
import spock.lang.Specification
//import grails.test.mixin.TestFor


@Build([QueueDashboardController])
class QueueDashboardControllerSpec extends Specification implements ControllerUnitTest<QueueDashboardController>{

    Location location = new Location();
    LocationHealthCheck healthCheck = new LocationHealthCheck()

    def setup() {
        location.uniqueIdentifierForServer = "serverId1"
        location.label = "label1"

        healthCheck.date = Calendar.getInstance().getTime()
        healthCheck.numberOfAgents = 5
        healthCheck.numberOfPendingJobsInWpt = 4
        healthCheck.numberOfEventResultsLastHour = 8
        healthCheck.numberOfJobResultsLastHour = 9
        healthCheck.numberOfErrorsLastHour = 10
        healthCheck.numberOfEventResultsNextHour = 7
        healthCheck.numberOfJobResultsNextHour = 3
    }

    void "emptyResponse is empty"() {

        when:
        controller.emptyResponse()

        then:
        response.getJson() == []
    }

    void "Get an empty response if no id is given"() {

        when:
        controller.getWptServerInformation()

        then:
        response.getJson() == []
    }

    void "Get an empty response if invalid id is given"() {
        given:
        params.id = "no_number"

        when:
        controller.getWptServerInformation()

        then:
        response.getJson() == []
    }

    void "Return Map with defaults if parameter are null"() {
        given:
        Location locationNull = null
        LocationHealthCheck healthCheckNull = null
        List<JobResult> executingJobResultsNull = null
        Map<Job, List<JobResult>> executingJobsNull = null

        when:
        def map = controller.buildMap(locationNull, healthCheckNull, executingJobResultsNull, executingJobsNull)

        then:
        map == [
                agents: -1,
                errorsLastHour: -1,
                eventResultsLastHour: -1,
                eventsNextHour: -1,
                executingJobs: [],
                id: "",
                jobResultsLastHour: -1,
                jobs: -1,
                jobsNextHour: -1,
                label: "",
                lastHealthCheckDate: "",
                pendingJobs: 0,
                runningJobs: 0
        ]
    }

    void "Return Map with only location and healthCheck"() {
        given:
        List<JobResult> executingJobResultsNull = null
        Map<Job, List<JobResult>> executingJobsNull = null

        when:
        def map = controller.buildMap(location, healthCheck, executingJobResultsNull, executingJobsNull)

        then:
        map == [
                agents: healthCheck.numberOfAgents,
                errorsLastHour: healthCheck.numberOfErrorsLastHour,
                eventResultsLastHour: healthCheck.numberOfEventResultsLastHour,
                eventsNextHour: healthCheck.numberOfEventResultsNextHour,
                executingJobs: [],
                id: location.uniqueIdentifierForServer,
                jobResultsLastHour: healthCheck.numberOfJobResultsLastHour,
                jobs: healthCheck.numberOfPendingJobsInWpt,
                jobsNextHour: healthCheck.numberOfJobResultsNextHour,
                label: location.location,
                lastHealthCheckDate: healthCheck.date.toString(),
                pendingJobs: 0,
                runningJobs: 0
        ]
    }
}

package de.iteratec.osm.result

import de.iteratec.osm.measurement.environment.DefaultQueueDashboardCommand
import de.iteratec.osm.measurement.environment.Location
import de.iteratec.osm.measurement.environment.QueueDashboardController
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

    void "emptyResponse is 404"() {

        when:
        controller.emptyResponse()

        then:
        response.status == 404
    }

    void "Get an empty response if no id is given"() {

        when:
        controller.getWptServerInformation()

        then:
        response.status == 404
    }

    void "Get an empty response if invalid id is given"() {
        given:
        params.id = "no_number"

        when:
        controller.getWptServerInformation()

        then:
        response.status == 404
    }

    void "Return Map with defaults if parameter are null"() {
        given:
        DefaultQueueDashboardCommand command = new DefaultQueueDashboardCommand()
        command.location = null
        command.healthCheck = null
        command.executingJobResults = null
        command.executingJobs = null

        when:
        def map = controller.buildMap(command)

        then:
        map == [
                agents: null,
                errorsLastHour: null,
                eventResultsLastHour: null,
                eventsNextHour: null,
                executingJobs: null,
                id: null,
                jobResultsLastHour: null,
                jobs: null,
                jobsNextHour: null,
                label: null,
                lastHealthCheckDate: null,
                pendingJobs: null,
                runningJobs: null
        ]
    }

    void "Return Map with only location and healthCheck"() {
        given:
        DefaultQueueDashboardCommand command = new DefaultQueueDashboardCommand()
        command.location = location
        command.healthCheck = healthCheck
        command.executingJobResults = null
        command.executingJobs = null

        when:
        def map = controller.buildMap(command)

        then:
        map == [
                agents: healthCheck.numberOfAgents,
                errorsLastHour: healthCheck.numberOfErrorsLastHour,
                eventResultsLastHour: healthCheck.numberOfEventResultsLastHour,
                eventsNextHour: healthCheck.numberOfEventResultsNextHour,
                executingJobs: null,
                id: location.uniqueIdentifierForServer,
                jobResultsLastHour: healthCheck.numberOfJobResultsLastHour,
                jobs: healthCheck.numberOfPendingJobsInWpt,
                jobsNextHour: healthCheck.numberOfJobResultsNextHour,
                label: location.location,
                lastHealthCheckDate: healthCheck.date.toString(),
                pendingJobs: null,
                runningJobs: null
        ]
    }
}

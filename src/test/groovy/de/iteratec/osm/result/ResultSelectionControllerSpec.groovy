package de.iteratec.osm.result

import grails.buildtestdata.BuildDomainTest
import grails.testing.web.controllers.ControllerUnitTest
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import grails.buildtestdata.mixin.Build
import spock.lang.Specification
import de.iteratec.osm.measurement.schedule.JobGroup

@Build([JobGroup])
class ResultSelectionControllerSpec extends Specification implements BuildDomainTest<JobGroup>,
        ControllerUnitTest<ResultSelectionController> {
    JobGroup jobGroup1
    JobGroup jobGroup2
    JobGroup jobGroup3

    def setup() {
        jobGroup1 = JobGroup.build(name: "jobGroup1")
        jobGroup2 = JobGroup.build(name: "jobGroup2")
        jobGroup3 = JobGroup.build(name: "jobGroup3")
    }

    void "get an error if the timeframe is invalid"(def from, def to) {
        given:
        def command = new ResultSelectionCommand()

        when:
        params.caller = "EventResult"
        params.from = from
        params.to = to
        controller.bindData(command, params)

        then:
        !command.validate()
        command.hasErrors()

        where:
        from                   | to
        "2016-11-15"           | "2016-11-14"
        "2016-11-15T11:00:00Z" | "2016-11-15T11:00:00Z"
    }

    void "narrow timeframe is valid"() {
        given:
        def command = new ResultSelectionCommand()

        when:
        params.from = "2016-11-15T08:30:00Z"
        params.to = "2016-11-15T08:30:02Z"
        params.caller = "EventResult"
        controller.bindData(command, params)

        then:
        command.validate()
        !command.getErrors().hasErrors()
    }

    void "iso timestamp can be bound to joda DateTime object"() {
        given:
        params.from = "2016-11-14T08:30:00.23Z"
        params.to = "2016-11-15" // local time zone
        params.caller = "EventResult"
        def command = new ResultSelectionCommand()

        when:
        controller.bindData(command, params)

        then:
        command.validate()
        command.from == new DateTime(2016, 11, 14, 8, 30, 0, 230, DateTimeZone.UTC)
        command.to == new DateTime(2016, 11, 15, 0, 0) // local time zone
    }
}

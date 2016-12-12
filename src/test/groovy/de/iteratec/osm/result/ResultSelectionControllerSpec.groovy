package de.iteratec.osm.result

import de.iteratec.osm.csi.TestDataUtil
import de.iteratec.osm.measurement.environment.Browser
import de.iteratec.osm.measurement.environment.Location
import de.iteratec.osm.measurement.environment.WebPageTestServer
import de.iteratec.osm.measurement.schedule.Job
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.measurement.script.Script
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import spock.lang.Specification
/**
 * See the API for {@link grails.test.mixin.web.ControllerUnitTestMixin} for usage instructions
 */
@TestFor(ResultSelectionController)
@Mock([Location, Script, JobGroup, Job, JobResult, WebPageTestServer, Browser])
class ResultSelectionControllerSpec extends Specification {
    JobGroup jobGroup1
    JobGroup jobGroup2
    JobGroup jobGroup3

    def setup() {
        jobGroup1 = TestDataUtil.createJobGroup("jobGroup1")
        jobGroup2 = TestDataUtil.createJobGroup("jobGroup2")
        jobGroup3 = TestDataUtil.createJobGroup("jobGroup3")
    }

    def cleanup() {
    }

    void "get an error if the timeframe is invalid"(def from, def to) {
        given:
        def command = new ResultSelectionCommand()

        when:
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
        controller.bindData(command, params)

        then:
        command.validate()
        !command.getErrors().hasErrors()
    }

    void "iso timestamp can be bound to joda DateTime object"() {
        given:
        params.from = "2016-11-14T08:30:00.23Z"
        params.to = "2016-11-15" // local time zone
        def command = new ResultSelectionCommand()

        when:
        controller.bindData(command, params)

        then:
        command.validate()
        command.from == new DateTime(2016, 11, 14, 8, 30, 0, 230, DateTimeZone.UTC)
        command.to== new DateTime(2016, 11, 15, 0, 0) // local time zone
    }
}

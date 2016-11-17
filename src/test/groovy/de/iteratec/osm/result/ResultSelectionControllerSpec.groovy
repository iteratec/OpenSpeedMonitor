package de.iteratec.osm.result

import de.iteratec.osm.csi.TestDataUtil
import de.iteratec.osm.measurement.environment.Browser
import de.iteratec.osm.measurement.environment.Location
import de.iteratec.osm.measurement.environment.WebPageTestServer
import de.iteratec.osm.measurement.schedule.Job
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.measurement.schedule.dao.JobGroupDaoService
import de.iteratec.osm.measurement.script.Script
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import org.grails.web.json.JSONArray
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
        controller.jobGroupDaoService = Stub(JobGroupDaoService)

        when:
        params.from = from
        params.to = to
        controller.getJobGroupsInTimeFrame()

        then:
        response.status == 400
        response.text.contains("Invalid time frame")

        where:
        from                   | to
        "2016-11-15"           | "2016-11-14"
        "2016-11-15T11:00:00Z" | "2016-11-15T11:00:00Z"
    }

    void "get empty array in time frame without results"() {
        given:
        controller.jobGroupDaoService = Stub(JobGroupDaoService) {
            findByJobResultsInTimeFrame(_, _) >> []
        }

        when:
        params.from = "2015-11-15"
        params.to = "2015-11-20"
        controller.getJobGroupsInTimeFrame()

        then:
        response.status == 200
        response.text == "[]"
    }

    void "get JobGroups as DTOs for correct time frame"() {
        // the service mock used in the controller simply returns all job groups for a time frame in 2016
        given:
        def allJobGroups = JobGroup.findAll()
        controller.jobGroupDaoService = Stub(JobGroupDaoService) {
            findByJobResultsInTimeFrame(_, _) >> allJobGroups
        }

        when:
        params.from = "2016-11-15"
        params.to = "2016-11-20"
        controller.getJobGroupsInTimeFrame()

        then:
        response.status == 200
        JSONArray resultArray = response.json
        resultArray.length() == allJobGroups.size()
        (resultArray.collect { [it.id, it.name] } as Set) == (allJobGroups.collect { [it.id, it.name] } as Set)
    }
}

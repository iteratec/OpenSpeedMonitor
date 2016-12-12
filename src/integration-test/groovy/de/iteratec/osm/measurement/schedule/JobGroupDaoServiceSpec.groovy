package de.iteratec.osm.measurement.schedule

import de.iteratec.osm.csi.NonTransactionalIntegrationSpec
import de.iteratec.osm.csi.TestDataUtil
import de.iteratec.osm.measurement.schedule.dao.JobGroupDaoService
import grails.test.mixin.integration.Integration
import grails.transaction.*
import spock.lang.*

import java.time.Instant

@Integration
@Rollback
public class JobGroupDaoServiceSpec extends NonTransactionalIntegrationSpec {
    JobGroup jobGroup1 // result on 2016-11-13T15:00:00Z
    JobGroup jobGroup2 // result on 2016-11-14T06:00:00Z and 2016-11-20T16:00:00Z
    JobGroup jobGroup3 // result on 2016-11-15T08:00:00Z and failed on 2016-11-19T08:00:00Z

    JobGroupDaoService jobGroupDaoService


    def setup() {
        createTestDataCommonForAllTests()
    }


    void "get an empty list if no JobResults are in a specified time frame "() {
        when:
        def jobGroups = jobGroupDaoService.findByJobResultsInTimeFrame(isoDate("2016-10-01"), isoDate("2016-11-01"))

        then:
        jobGroups == []
    }

    void "get job groups in specified time frame"() {
        when:
        def jobGroups = jobGroupDaoService.findByJobResultsInTimeFrame(isoDate("2016-11-14"), isoDate("2016-11-16"))

        then:
        jobGroups.collect{[it.id, it.name]}.sort() == [jobGroup2, jobGroup3].collect{[it.id, it.name]}.sort()
    }

    void "get job groups in specified time frame ignoring invalid job results"() {
        when:
        def jobGroups = jobGroupDaoService.findByJobResultsInTimeFrame(isoDate("2016-11-17"), isoDate("2016-11-21"))

        then:
        jobGroups.collect{[it.id, it.name]} == [jobGroup2].collect{[it.id, it.name]}
    }

    private Date isoDate(String isoDateString) {
        return Date.parse("yyyy-MM-dd", isoDateString)
    }

    private void createTestDataCommonForAllTests() {
        jobGroup1 = TestDataUtil.createJobGroup("jobGroup1")
        jobGroup2 = TestDataUtil.createJobGroup("jobGroup2")
        jobGroup3 = TestDataUtil.createJobGroup("jobGroup3")

        def script = TestDataUtil.createScript()
        def location = TestDataUtil.createLocation()

        def job1 = TestDataUtil.createJob("Job 1", script, location, jobGroup1)
        def job2_1 = TestDataUtil.createJob("Job 2_1", script, location, jobGroup2)
        def job2_2 = TestDataUtil.createJob("Job 2_2", script, location, jobGroup2)
        def job3 = TestDataUtil.createJob("Job 3", script, location, jobGroup3)

        TestDataUtil.createJobResult("JobResult 1", Date.from(Instant.parse("2016-11-13T15:00:00Z")), job1, location)
        TestDataUtil.createJobResult("JobResult 2_1", Date.from(Instant.parse("2016-11-14T06:00:00Z")), job2_1, location)
        TestDataUtil.createJobResult("JobResult 2_2", Date.from(Instant.parse("2016-11-20T16:00:00Z")), job2_2, location)
        TestDataUtil.createJobResult("JobResult 3", Date.from(Instant.parse("2016-11-15T08:00:00Z")), job3, location)
        TestDataUtil.createJobResult("JobResult 3", Date.from(Instant.parse("2016-11-19T08:00:00Z")), job3, location, 400)
    }
}

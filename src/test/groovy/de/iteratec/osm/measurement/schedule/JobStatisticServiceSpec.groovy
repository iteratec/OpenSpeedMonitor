package de.iteratec.osm.measurement.schedule

import de.iteratec.osm.csi.TestDataUtil
import de.iteratec.osm.measurement.environment.Browser
import de.iteratec.osm.measurement.environment.Location
import de.iteratec.osm.measurement.environment.WebPageTestServer
import de.iteratec.osm.measurement.script.Script
import de.iteratec.osm.result.JobResult
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import org.joda.time.DateTime
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.services.ServiceUnitTestMixin} for usage instructions
 */
@TestFor(JobStatisticService)
@Mock([Job, Script, WebPageTestServer, Browser, JobGroup, JobStatistic, Location, JobResult])
class JobStatisticServiceSpec extends Specification {

    public static final DateTime now = new DateTime()
    Job job
    Location location

    def setup() {
        createTestDataCommonToAllTests()
    }

    def cleanup() {
    }

    void "job without tests"() {
        setup: "job gets prepared without any results at all"
        JobResultSequenceWithSameStatus noTestResultsAtAll = new JobResultSequenceWithSameStatus(count: 0, status: null)
        prepareJob([noTestResultsAtAll])
        when: "stats get calculated"
        service.updateStatsFor(job)
        then: "they show no calculated percentages"
        job.jobStatistic.percentageSuccessfulTestsOfLast5 == null
        job.jobStatistic.percentageSuccessfulTestsOfLast25 == null
        job.jobStatistic.percentageSuccessfulTestsOfLast150 == null
    }

    void "job with lot of tests and all successful"() {
        setup:
        JobResultSequenceWithSameStatus allSuccessful = new JobResultSequenceWithSameStatus(count: 160, status: 200)
        prepareJob([allSuccessful])
        when: "stats get calculated"
        service.updateStatsFor(job)
        then: "they show 100% successful tests"
        job.jobStatistic.percentageSuccessfulTestsOfLast5 == 100
        job.jobStatistic.percentageSuccessfulTestsOfLast25 == 100
        job.jobStatistic.percentageSuccessfulTestsOfLast150 == 100
    }

    void "job with less than 150 tests and all successful"() {
        setup: "job gets prepared with 80 successful results"
        JobResultSequenceWithSameStatus allSuccessful = new JobResultSequenceWithSameStatus(count: 80, status: 200)
        prepareJob([allSuccessful])
        when: "stats get calculated"
        service.updateStatsFor(job)
        then: "they show 100% successful results for the last 5 and 25 results and no percentage for the last 150"
        job.jobStatistic.percentageSuccessfulTestsOfLast5 == 100
        job.jobStatistic.percentageSuccessfulTestsOfLast25 == 100
        job.jobStatistic.percentageSuccessfulTestsOfLast150 == null
    }

    void "job with less than 25 tests and all successful"() {
        setup: "job gets prepared with 19 successful results"
        JobResultSequenceWithSameStatus allSuccessful = new JobResultSequenceWithSameStatus(count: 19, status: 200)
        prepareJob([allSuccessful])
        when: "stats get calculated"
        service.updateStatsFor(job)
        then: "they show 100% successful results for the last 5 results and no percentages for the last 25 and 150"
        job.jobStatistic.percentageSuccessfulTestsOfLast5 == 100
        job.jobStatistic.percentageSuccessfulTestsOfLast25 == null
        job.jobStatistic.percentageSuccessfulTestsOfLast150 == null
    }

    void "job with less than 5 tests and all successful"() {
        setup: "job gets prepared with 4 successful results"
        JobResultSequenceWithSameStatus allSuccessful = new JobResultSequenceWithSameStatus(count: 4, status: 200)
        prepareJob([allSuccessful])
        when: "stats get calculated"
        service.updateStatsFor(job)
        then: "they show no percentages at all"
        job.jobStatistic.percentageSuccessfulTestsOfLast5 == null
        job.jobStatistic.percentageSuccessfulTestsOfLast25 == null
        job.jobStatistic.percentageSuccessfulTestsOfLast150 == null
    }

    void "job with lot of tests and status 5,25,150: RED,YELLOW,GREEN"() {
        setup: "job gets prepared with results sequence"
        List<JobResultSequenceWithSameStatus> resultSequences = [
                new JobResultSequenceWithSameStatus(count: 3, status: 400),
                new JobResultSequenceWithSameStatus(count: 2, status: 200),
                new JobResultSequenceWithSameStatus(count: 150, status: 200)
        ]
        prepareJob(resultSequences)
        when: "stats get calculated"
        service.updateStatsFor(job)
        then: "they show (2/5)*100 % successful results for the last 5, (22/25)*100 % for the last 25 and (147/150)*100 % for the last 150 results"
        job.jobStatistic.percentageSuccessfulTestsOfLast5 == (2/5)*100
        job.jobStatistic.percentageSuccessfulTestsOfLast25 == (22/25)*100
        job.jobStatistic.percentageSuccessfulTestsOfLast150 == (147/150)*100
    }

    void "job with lot of tests and status 5,25,150: GREEN,YELLOW,GREEN"() {
        setup: "job gets prepared with results sequence"
        List<JobResultSequenceWithSameStatus> resultSequences = [
                new JobResultSequenceWithSameStatus(count: 5, status: 200),
                new JobResultSequenceWithSameStatus(count: 16, status: 200),
                new JobResultSequenceWithSameStatus(count: 4, status: 504),
                new JobResultSequenceWithSameStatus(count: 150, status: 200)
        ]
        prepareJob(resultSequences)
        when: "stats get calculated"
        service.updateStatsFor(job)
        then: "they show 100% successful results for the last 5, (21/25)*100 % for the last 25 and (146/150)*100 % for the last 150 results"
        job.jobStatistic.percentageSuccessfulTestsOfLast5 == 100
        job.jobStatistic.percentageSuccessfulTestsOfLast25 == (21/25)*100
        job.jobStatistic.percentageSuccessfulTestsOfLast150 == (146/150)*100
    }

    void "job with lot of tests and status 5,25,150: RED,RED,YELLOW"() {
        setup: "job gets prepared with results sequence"
        List<JobResultSequenceWithSameStatus> resultSequences = [
                new JobResultSequenceWithSameStatus(count: 25, status: 400),
                new JobResultSequenceWithSameStatus(count: 125, status: 200)
        ]
        prepareJob(resultSequences)
        when: "stats get calculated"
        service.updateStatsFor(job)
        then: "they show 0% successful results for the last 5 and 25 and (125/150)*100 % for the last 150 results"
        job.jobStatistic.percentageSuccessfulTestsOfLast5 == 0
        job.jobStatistic.percentageSuccessfulTestsOfLast25 == 0
        job.jobStatistic.percentageSuccessfulTestsOfLast150 == (125/150)*100
    }

    void "job with lot of tests and no one successful (RED,RED,RED)"() {
        setup: "job gets prepared with results sequence"
        List<JobResultSequenceWithSameStatus> resultSequences = [
                new JobResultSequenceWithSameStatus(count: 150, status: 400)
        ]
        prepareJob(resultSequences)
        when: "stats get calculated"
        service.updateStatsFor(job)
        then: "they show 0% successful results for the last 5, 25 and 150 results"
        job.jobStatistic.percentageSuccessfulTestsOfLast5 == 0
        job.jobStatistic.percentageSuccessfulTestsOfLast25 == 0
        job.jobStatistic.percentageSuccessfulTestsOfLast150 == 0
    }

    void "job with less than 150 tests and not all successful (YELLOW,RED,null)"() {
        setup: "job gets prepared with 80 results mixed successful and not successful"
        List<JobResultSequenceWithSameStatus> resultSequences = [
                new JobResultSequenceWithSameStatus(count: 4, status: 200),
                new JobResultSequenceWithSameStatus(count: 12, status: 400),
                new JobResultSequenceWithSameStatus(count: 64, status: 200)
        ]
        prepareJob(resultSequences)
        when: "stats get calculated"
        service.updateStatsFor(job)
        then: "they show 100% successful results for the last 5 and 25 results and no percentage for the last 150"
        job.jobStatistic.percentageSuccessfulTestsOfLast5 == (4/5)*100
        job.jobStatistic.percentageSuccessfulTestsOfLast25 == (13/25)*100
        job.jobStatistic.percentageSuccessfulTestsOfLast150 == null
    }

    private void createTestDataCommonToAllTests() {
        Script script = TestDataUtil.createScript()
        WebPageTestServer server = TestDataUtil.createWebPageTestServer("server", "proxyIdentifier", true, "http://baseurl.com")
        Browser browser = TestDataUtil.createBrowser("browser")
        location = TestDataUtil.createLocation(server, "uniqueIdentifier", browser, true)
        JobGroup jobGroup = TestDataUtil.createJobGroup("job group")
        job = TestDataUtil.createJob("job", script, location, jobGroup, "my job", 1, false, 60)
    }

    private void prepareJob(List<JobResultSequenceWithSameStatus> jrSequences) {
        int minuteCounter = 0
        jrSequences.each { jrSequence ->
            jrSequence.count.times {
                TestDataUtil.createJobResult("testid", now.minusMinutes(++minuteCounter).toDate(), job, location, jrSequence.status)
            }
        }
    }
    class JobResultSequenceWithSameStatus{
        Integer count
        Integer status
    }
}

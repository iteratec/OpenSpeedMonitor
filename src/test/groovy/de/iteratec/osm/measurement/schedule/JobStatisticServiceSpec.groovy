package de.iteratec.osm.measurement.schedule

import de.iteratec.osm.result.JobResult
import grails.buildtestdata.mixin.Build
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import org.joda.time.DateTime
import spock.lang.Specification

@TestFor(JobStatisticService)
@Build([Job, JobResult])
@Mock([Job, JobResult, JobStatistic])
class JobStatisticServiceSpec extends Specification {

    void "job without tests"() {
        setup: "job gets prepared without any results at all"
        Job job = Job.build()

        when: "stats get calculated"
        service.updateStatsFor(job)

        then: "they show no calculated percentages"
        job.jobStatistic.percentageSuccessfulTestsOfLast5 == null
        job.jobStatistic.percentageSuccessfulTestsOfLast25 == null
        job.jobStatistic.percentageSuccessfulTestsOfLast150 == null
    }

    void "job with lot of tests and all successful"() {
        setup:
        Job job = Job.build()
        160.times { JobResult.build(job: job, date: minutesAgo(it), httpStatusCode: 200) }

        when: "stats get calculated"
        service.updateStatsFor(job)

        then: "they show 100% successful tests"
        job.jobStatistic.percentageSuccessfulTestsOfLast5 == 100
        job.jobStatistic.percentageSuccessfulTestsOfLast25 == 100
        job.jobStatistic.percentageSuccessfulTestsOfLast150 == 100
    }

    void "job with less than 150 tests and all successful"() {
        setup: "job gets prepared with 80 successful results"
        Job job = Job.build()
        80.times { JobResult.build(job: job, date: minutesAgo(it), httpStatusCode: 200) }

        when: "stats get calculated"
        service.updateStatsFor(job)

        then: "they show 100% successful results for the last 5 and 25 results and no percentage for the last 150"
        job.jobStatistic.percentageSuccessfulTestsOfLast5 == 100
        job.jobStatistic.percentageSuccessfulTestsOfLast25 == 100
        job.jobStatistic.percentageSuccessfulTestsOfLast150 == null
    }

    void "job with less than 25 tests and all successful"() {
        setup: "job gets prepared with 19 successful results"
        Job job = Job.build()
        19.times { JobResult.build(job: job, date: minutesAgo(it), httpStatusCode: 200) }

        when: "stats get calculated"
        service.updateStatsFor(job)

        then: "they show 100% successful results for the last 5 results and no percentages for the last 25 and 150"
        job.jobStatistic.percentageSuccessfulTestsOfLast5 == 100
        job.jobStatistic.percentageSuccessfulTestsOfLast25 == null
        job.jobStatistic.percentageSuccessfulTestsOfLast150 == null
    }

    void "job with less than 5 tests and all successful"() {
        setup: "job gets prepared with 4 successful results"
        Job job = Job.build()
        4.times { JobResult.build(job: job, date: minutesAgo(it), httpStatusCode: 200) }

        when: "stats get calculated"
        service.updateStatsFor(job)

        then: "they show no percentages at all"
        job.jobStatistic.percentageSuccessfulTestsOfLast5 == null
        job.jobStatistic.percentageSuccessfulTestsOfLast25 == null
        job.jobStatistic.percentageSuccessfulTestsOfLast150 == null
    }

    void "job with lot of tests and status 5,25,150: RED,YELLOW,GREEN"() {
        setup: "job gets prepared with results sequence"
        Job job = Job.build()
        3.times { JobResult.build(job: job, date: minutesAgo(it), httpStatusCode: 400) }
        2.times { JobResult.build(job: job, date: minutesAgo(3 + it), httpStatusCode: 200) }
        150.times { JobResult.build(job: job, date: minutesAgo(5 + it), httpStatusCode: 200) }

        when: "stats get calculated"
        service.updateStatsFor(job)

        then: "they show (2/5)*100 % successful results for the last 5, (22/25)*100 % for the last 25 and (147/150)*100 % for the last 150 results"
        job.jobStatistic.percentageSuccessfulTestsOfLast5 == (2/5)*100
        job.jobStatistic.percentageSuccessfulTestsOfLast25 == (22/25)*100
        job.jobStatistic.percentageSuccessfulTestsOfLast150 == (147/150)*100
    }

    void "job with lot of tests and status 5,25,150: GREEN,YELLOW,GREEN"() {
        setup: "job gets prepared with results sequence"
        Job job = Job.build()
        5.times { JobResult.build(job: job, date: minutesAgo(it), httpStatusCode: 200) }
        16.times { JobResult.build(job: job, date: minutesAgo(5 + it), httpStatusCode: 200) }
        4.times { JobResult.build(job: job, date: minutesAgo(31 + it), httpStatusCode: 504) }
        150.times { JobResult.build(job: job, date: minutesAgo(35 + it), httpStatusCode: 200) }

        when: "stats get calculated"
        service.updateStatsFor(job)

        then: "they show 100% successful results for the last 5, (21/25)*100 % for the last 25 and (146/150)*100 % for the last 150 results"
        job.jobStatistic.percentageSuccessfulTestsOfLast5 == 100
        job.jobStatistic.percentageSuccessfulTestsOfLast25 == (21/25)*100
        job.jobStatistic.percentageSuccessfulTestsOfLast150 == (146/150)*100
    }

    void "job with lot of tests and status 5,25,150: RED,RED,YELLOW"() {
        setup: "job gets prepared with results sequence"
        Job job = Job.build()
        25.times  { JobResult.build(job: job, date: minutesAgo(it), httpStatusCode: 400) }
        125.times { JobResult.build(job: job, date: minutesAgo(25 + it), httpStatusCode: 200) }

        when: "stats get calculated"
        service.updateStatsFor(job)

        then: "they show 0% successful results for the last 5 and 25 and (125/150)*100 % for the last 150 results"
        job.jobStatistic.percentageSuccessfulTestsOfLast5 == 0
        job.jobStatistic.percentageSuccessfulTestsOfLast25 == 0
        job.jobStatistic.percentageSuccessfulTestsOfLast150 == (125/150)*100
    }

    void "job with lot of tests and no one successful (RED,RED,RED)"() {
        setup: "job gets prepared with results sequence"
        Job job = Job.build()
        150.times { JobResult.build(job: job, date: minutesAgo(it), httpStatusCode: 400) }

        when: "stats get calculated"
        service.updateStatsFor(job)

        then: "they show 0% successful results for the last 5, 25 and 150 results"
        job.jobStatistic.percentageSuccessfulTestsOfLast5 == 0
        job.jobStatistic.percentageSuccessfulTestsOfLast25 == 0
        job.jobStatistic.percentageSuccessfulTestsOfLast150 == 0
    }

    void "job with less than 150 tests and not all successful (YELLOW,RED,null)"() {
        setup: "job gets prepared with 80 results mixed successful and not successful"
        Job job = Job.build()
        4.times  { JobResult.build(job: job, date: minutesAgo(it), httpStatusCode: 200) }
        12.times { JobResult.build(job: job, date: minutesAgo(4 + it), httpStatusCode: 400) }
        64.times { JobResult.build(job: job, date: minutesAgo(16 + it), httpStatusCode: 200) }

        when: "stats get calculated"
        service.updateStatsFor(job)

        then: "they show 100% successful results for the last 5 and 25 results and no percentage for the last 150"
        job.jobStatistic.percentageSuccessfulTestsOfLast5 == (4/5)*100
        job.jobStatistic.percentageSuccessfulTestsOfLast25 == (13/25)*100
        job.jobStatistic.percentageSuccessfulTestsOfLast150 == null
    }

    private static Date minutesAgo(int numMinutes) {
        return DateTime.now().minusMinutes(numMinutes).toDate();
    }
}

package de.iteratec.osm.report.external

import de.iteratec.osm.measurement.schedule.Job
import de.iteratec.osm.measurement.schedule.JobGroup
import grails.testing.services.ServiceUnitTest
import spock.lang.Specification

class JobHealthReportServiceSpec extends Specification implements ServiceUnitTest<JobHealthReportService> {
    JobHealthReportService serviceUnderTest

    def setup() {
        serviceUnderTest = service
        serviceUnderTest.metaClass.getJobsToReport = { ->
            List<Job> jobs = new ArrayList<Job>()
            Job job = new Job()
            job.jobGroup = new JobGroup()
            jobs.add(job)
            return jobs
        }
    }

    void "test something"() {
        when:
        serviceUnderTest.reportJobHealthStatusToGraphite(new Date())

        then:
        false // fix me
    }
}

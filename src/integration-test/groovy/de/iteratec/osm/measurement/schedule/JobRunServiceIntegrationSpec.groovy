package de.iteratec.osm.measurement.schedule

import de.iteratec.osm.csi.NonTransactionalIntegrationSpec
import de.iteratec.osm.result.JobResult
import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import org.joda.time.DateTime
import org.joda.time.DateTimeUtils

import static de.iteratec.osm.result.JobResultStatus.*

@Integration(applicationClass = openspeedmonitor.Application.class)
@Rollback
class JobRunServiceIntegrationSpec extends NonTransactionalIntegrationSpec {
    JobRunService jobRunService

    void "closeRunningAndPengingJobs test"() {
        given: "a set of persisted jobResults"
        // fix current date for test purposes
        DateTimeUtils.setCurrentMillisFixed(1482395596904)
        DateTime currentDate = new DateTime()
        Job job = Job.build(maxDownloadTimeInMinutes: 60)

        JobResult.build(job: job, date: currentDate.toDate(), testId: "running test", jobResultStatus: RUNNING)
        JobResult.build(job: job, date: currentDate.toDate(), testId: "pending test", jobResultStatus: WAITING)
        JobResult.build(job: job, date: currentDate.minusMinutes(120).toDate(), testId: "barely running test", jobResultStatus: RUNNING)
        JobResult.build(job: job, date: currentDate.minusMinutes(121).toDate(), testId: "outdated running test", jobResultStatus: RUNNING)
        JobResult.build(job: job, date: currentDate.minusDays(5).toDate(), testId: "outdated pending test", jobResultStatus: WAITING)
        JobResult.build(job: job, date: currentDate.minusDays(5).toDate(), testId: "finished test", jobResultStatus: SUCCESS)
        JobResult.build(job: job, date: currentDate.minusDays(5).toDate(), testId: "failed test", jobResultStatus: TIMEOUT)

        when: "closing running and pending job results"
        jobRunService.closeRunningAndWaitingJobRuns()

        then: "find jobResults and check their jobResultStatus"
        JobResult.findByTestId("running test").jobResultStatus == RUNNING
        JobResult.findByTestId("pending test").jobResultStatus == WAITING
        JobResult.findByTestId("barely running test").jobResultStatus == RUNNING
        JobResult.findByTestId("outdated running test").jobResultStatus == ORPHANED
        JobResult.findByTestId("outdated pending test").jobResultStatus == ORPHANED
        JobResult.findByTestId("finished test").jobResultStatus == SUCCESS
        JobResult.findByTestId("failed test").jobResultStatus == TIMEOUT
    }
}

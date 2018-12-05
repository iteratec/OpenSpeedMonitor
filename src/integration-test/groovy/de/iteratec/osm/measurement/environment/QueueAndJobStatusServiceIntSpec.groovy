package de.iteratec.osm.measurement.environment

import de.iteratec.osm.csi.NonTransactionalIntegrationSpec
import de.iteratec.osm.measurement.environment.wptserver.JobResultPersisterService
import de.iteratec.osm.measurement.schedule.Job
import de.iteratec.osm.result.JobResult
import de.iteratec.osm.result.JobResultStatus
import de.iteratec.osm.result.WptStatus
import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration

import static de.iteratec.osm.result.JobResultStatus.*

@Integration(applicationClass = openspeedmonitor.Application.class)
@Rollback
class QueueAndJobStatusServiceIntSpec extends NonTransactionalIntegrationSpec {

    JobResultPersisterService jobResultPersisterService
    QueueAndJobStatusService queueAndJobStatusService

    void "getRunningAndRecentlyFinishedJobs test"() {
        given: "an inactive job and a date"
        Job job = Job.build(runs: 1, active: false, maxDownloadTimeInMinutes: 60)
        Date now = new Date()
        Date oldestDate = now - 5

        when: "creating and persisting jobResults with statusCodes and a job"
        inputStatusCodes.reverse().eachWithIndex { JobResultStatus jobResultStatus, int i ->
            JobResult result = jobResultPersisterService.persistUnfinishedJobResult(job.id, null, jobResultStatus, WptStatus.UNKNOWN)
            result.date = now - i
            result.save(flush: true, failOnError: true)
        }
        // test execution
        List recentRuns = queueAndJobStatusService.getRunningAndRecentlyFinishedJobs(oldestDate, oldestDate, oldestDate)[job.id]

        then: "check if the number of jobResults matches the number of statusCodes"
        inputStatusCodes.size() == JobResult.count()
        expectedStatusCodes.size() == recentRuns.size()
        expectedStatusCodes.eachWithIndex { JobResultStatus statusCode, int i ->
            statusCode == recentRuns[i]['status']
        }

        where: "the input status codes are the expected status codes"
        inputStatusCodes               || expectedStatusCodes
        [WAITING]                      || [WAITING]
        [SUCCESS]                      || [SUCCESS]
        [FAILED]                       || [FAILED]
        [FAILED, WAITING]              || [WAITING]
        [WAITING, FAILED]              || [WAITING, FAILED]
        [SUCCESS, FAILED]              || [SUCCESS, FAILED]
        [INCOMPLETE, SUCCESS]          || [SUCCESS]
        [WAITING, SUCCESS]             || [WAITING, SUCCESS]
        [SUCCESS, WAITING]             || [SUCCESS, WAITING]
        [WAITING, SUCCESS, FAILED]     || [WAITING, SUCCESS, FAILED]
        [WAITING, FAILED, SUCCESS]     || [WAITING, SUCCESS]
        [SUCCESS, WAITING, INCOMPLETE] || [SUCCESS, WAITING, INCOMPLETE]
        [SUCCESS, INCOMPLETE, WAITING] || [SUCCESS, WAITING]
        [INCOMPLETE, WAITING, SUCCESS] || [WAITING, SUCCESS]
        [FAILED, SUCCESS, WAITING]     || [SUCCESS, WAITING]
    }
}

package de.iteratec.osm.measurement.schedule

import de.iteratec.osm.ConfigService
import de.iteratec.osm.InMemoryConfigService
import de.iteratec.osm.measurement.environment.wptserver.JobExecutionException
import de.iteratec.osm.measurement.environment.wptserver.JobResultPersisterService
import de.iteratec.osm.measurement.environment.wptserver.WptInstructionService
import de.iteratec.osm.measurement.environment.wptserver.WptResultXml
import de.iteratec.osm.result.JobResult
import de.iteratec.osm.result.JobResultStatus
import de.iteratec.osm.result.WptStatus
import de.iteratec.osm.util.PerformanceLoggingService
import grails.gorm.transactions.Transactional
import org.apache.commons.lang.exception.ExceptionUtils
import org.joda.time.DateTime

import static de.iteratec.osm.util.PerformanceLoggingService.LogLevel.DEBUG

@Transactional
class JobRunService {
    WptInstructionService wptInstructionService
    JobSchedulingService jobSchedulingService
    PerformanceLoggingService performanceLoggingService
    InMemoryConfigService inMemoryConfigService
    ConfigService configService
    JobResultPersisterService jobResultPersisterService

    /**
     * Cancel a running Job. If the job is pending and has not been executed yet, the WPT server will
     * also terminate it. Otherwise it will be left running on the server but polling is stopped.
     */
    void cancelJobRun(Job job, String testId) {
        log.info("unschedule quartz triggers for job run: job=${job.label},test id=${testId}")
        jobSchedulingService.unscheduleTest(job, testId)
        log.info("unschedule quartz triggers for job run: job=${job.label},test id=${testId} ... DONE")
        JobResult result = JobResult.findByJobAndTestIdAndJobResultStatus(job, testId, JobResultStatus.WAITING)
        if (!result) {
            result = JobResult.findByJobAndTestIdAndJobResultStatus(job, testId, JobResultStatus.RUNNING)
        }
        if (result) {
            log.info("Set status of job Result to canceled: ${result}.")
            jobResultPersisterService.persistUnfinishedJobResult(job, testId, JobResultStatus.CANCELED, "Canceled by user.")
            log.info("Canceling respective test on wptserver.")
            wptInstructionService.cancelTest(job.location.wptServer, [test: testId])
        } else {
            log.info("Can't cancel test ${testId} of job ${job.id} since it's status is ${result.jobResultStatus}")
        }
    }


    /**
     * Setting the job result status of running and pending jobResults older than maxDate
     */
    void closeRunningAndWaitingJobRuns() {
        DateTime currentDate = new DateTime()
        List<JobResult> jobResults = JobResult.findAllByJobResultStatusInList([JobResultStatus.WAITING, JobResultStatus.RUNNING])
        jobResults = jobResults.findAll {
            currentDate > new DateTime(it.date).plusMinutes(it.job.maxDownloadTimeInMinutes * 2)
        }
        if (jobResults) {
            jobResults.each {
                it.jobResultStatus = JobResultStatus.ORPHANED
                it.description = "closed due to nightly cleanup of job results"
                it.save(failOnError: true)
            }
            log.info("Clenup of running and pending jobResults: Changed status of ${jobResults.size()} jobResults to ORPHANED.")
        }
    }

    /**
     * Checks if this job instance is still unfinished. If so, the corresponding WPT Server is polled one last time
     * and if job is still reported as running, it's temporary JobResult is set to jobResultStatus 504 (timeout)
     * in the database. In addition, a request to cancelTest.php is made.
     */
    void handleJobRunTimeout(Job job, String testId) {
        JobResult result = JobResult.findByJobAndTestId(job, testId)
        if (!result || result.wptStatus.isFinished()) {
            return
        }
        JobResultStatus jobResultStatus = pollJobRun(job, testId)
        if (jobResultStatus.isTerminated()) {
            return
        }
        jobSchedulingService.unscheduleTest(job, testId)
        jobResultPersisterService.persistUnfinishedJobResult(job, testId, JobResultStatus.TIMEOUT, "Test exceeded maximum polling time.")
        wptInstructionService.cancelTest(job.location.wptServer, [test: testId])
    }

    /**
     * Updates the status of a currently running job.
     * If the Job terminated (successfully or unsuccessfully) the Quartz trigger calling pollJobRun()
     * every pollDelaySeconds seconds is removed
     */
    JobResultStatus pollJobRun(Job job, String testId) {
        JobResultStatus jobResultStatus = JobResultStatus.PERSISTANCE_ERROR
        try {
            WptResultXml resultXml
            performanceLoggingService.logExecutionTime(DEBUG, "Polling jobrun ${testId} of job ${job.id}: fetching results from wptrserver.", 1) {
                resultXml = wptInstructionService.fetchResult(job.location.wptServer, testId)
            }
            performanceLoggingService.logExecutionTime(DEBUG, "Handling WPT result of jobrun ${testId} of job ${job.id}", 1) {
                jobResultStatus = jobResultPersisterService.handleWptResult(resultXml, testId, job)
            }
        } catch (Exception e) {
            log.error("Polling jobrun ${testId} of job ${job.label}: An unexpected exception occured. Error gets persisted as unfinished JobResult now", e)
            jobResultPersisterService.persistUnfinishedJobResult(job, testId, JobResultStatus.PERSISTANCE_ERROR, WptStatus.UNKNOWN, e.getMessage())
        } finally {
            if (jobResultStatus.isTerminated()) {
                jobSchedulingService.unscheduleTest(job, testId)
            }
        }
        return jobResultStatus
    }

    /**
     * If measurements are generally enabled: Launches the given Job and creates a Quartz trigger to poll for new results every x seconds.
     * If they are not enabled nothing happens.
     * @return the testId of the running job.
     */
    String launchJobRun(Job job, int priority = 5) {
        if (!inMemoryConfigService.areMeasurementsGenerallyEnabled()) {
            throw new IllegalStateException("Job run of Job ${job} is skipped cause measurements are generally disabled.")
        }
        if (inMemoryConfigService.pauseJobProcessingForOverloadedLocations) {
            throw new IllegalStateException("Job run of Job ${job} is skipped cause overloaded locations.")
        }
        try {
            String testId = wptInstructionService.runtest(job, priority)
            jobResultPersisterService.persistUnfinishedJobResult(job, testId, JobResultStatus.WAITING, WptStatus.PENDING, "Launched.")
            jobSchedulingService.scheduleJobRunPolling(job, testId, new Date())
            return testId
        } catch (Exception e) {
            WptStatus wptStatus = e instanceof JobExecutionException ? e.wptStatus : WptStatus.TEST_DID_NOT_START
            String testId = e instanceof JobExecutionException ? e.testId : null
            jobResultPersisterService.persistUnfinishedJobResult(job, testId, JobResultStatus.LAUNCH_ERROR, wptStatus, e.getMessage())
            throw new RuntimeException("An error occurred while launching job ${job.label}. Unfinished JobResult with error code will get persisted now: ${ExceptionUtils.getFullStackTrace(e)}")
        }
    }



    Map<String, Integer> handleOldJobResults() {
        log.info("handleOldJobResults() OSM starts")
        Date twoHoursAgo = new DateTime().minusHours(2).toDate()
        int jobResultsToDeleteCount = cleanJobRunsOlderThan(twoHoursAgo)
        int jobResultsToRescheduleCount = rescheduleJobRunsYoungerThan(twoHoursAgo)
        log.info("handleOldJobResults() OSM ends")
        return ["JobResultsToDeleteCount": jobResultsToDeleteCount, "JobResultsToRescheduleCount": jobResultsToRescheduleCount]
    }

    private int cleanJobRunsOlderThan(Date date) {
        def jobResultsToDelete = JobResult.findAllByJobResultStatusAndDateLessThan(JobResultStatus.WAITING, date)
        jobResultsToDelete.addAll(JobResult.findAllByJobResultStatusAndDateLessThan(JobResultStatus.RUNNING, date))
        if (jobResultsToDelete.isEmpty()) {
            return 0
        }
        log.debug("Found ${jobResultsToDelete.size()} pending/running JobResults with JobResultStatus < 200 that are to old. Start deleting...")
        jobResultsToDelete.each { JobResult jobResult ->
            try {
                jobResult.delete()
            } catch (Exception e) {
                log.error("Wasn't able to delete old JobResult JobId = ${jobResult.jobId} TestId = ${jobResult.testId}" + e)
            }
        }
        log.debug("Cleanup done.")
    }

    private int rescheduleJobRunsYoungerThan(Date date) {
        def jobResultsToReschedule = JobResult.findAllByJobResultStatusAndDateGreaterThan(JobResultStatus.WAITING, date)
        jobResultsToReschedule.addAll(JobResult.findAllByJobResultStatusAndDateGreaterThan(JobResultStatus.RUNNING, date))
        if (jobResultsToReschedule.isEmpty()) {
            return 0
        }
        log.debug("Found ${jobResultsToReschedule.size()} pending/running JobResults with JobResultStatus < 200 fresh enough for rescheduling. Start rescheduling ...")
        jobResultsToReschedule.each { JobResult jobResult ->
            jobSchedulingService.rescheduleJobRunPolling(jobResult)
        }
        log.debug("Rescheduling done.")
        return jobResultsToReschedule.size()
    }


}

package de.iteratec.osm.measurement.schedule

import de.iteratec.osm.ConfigService
import de.iteratec.osm.InMemoryConfigService
import de.iteratec.osm.measurement.environment.wptserver.JobExecutionException
import de.iteratec.osm.measurement.environment.wptserver.WptInstructionService
import de.iteratec.osm.measurement.environment.wptserver.WptResultXml
import de.iteratec.osm.result.JobResult
import de.iteratec.osm.result.JobResultStatus
import de.iteratec.osm.result.WptStatus
import de.iteratec.osm.util.PerformanceLoggingService
import grails.gorm.transactions.Transactional
import org.apache.commons.lang.exception.ExceptionUtils
import org.hibernate.StaleObjectStateException
import org.joda.time.DateTime

import static de.iteratec.osm.util.PerformanceLoggingService.LogLevel.DEBUG

@Transactional
class JobRunService {
    WptInstructionService wptInstructionService
    JobSchedulingService jobSchedulingService
    PerformanceLoggingService performanceLoggingService
    InMemoryConfigService inMemoryConfigService
    ConfigService configService

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
            log.info("Deleting the following JobResult as requested: ${result}.")
            result.delete(failOnError: true, flush: true)
            log.info("Deleting the following JobResult as requested: ${result}... DONE")
            log.info("Canceling respective test on wptserver.")
            wptInstructionService.cancelTest(job.location.wptServer, [test: testId])
            log.info("Canceling respective test on wptserver... DONE")
        }
    }

    /**
     * Saves a JobResult with the given parameters and no date to indicate that the
     * specified Job/test is running and that this is not the result of a finished
     * test execution.
     */
    JobResult persistUnfinishedJobResult(long jobId, String testId, JobResultStatus jobResultStatus, WptStatus wptStatus, String description = '') {
        // If no testId was provided some error occurred and needs to be logged
        Job job = Job.get(jobId)
        JobResult result = null
        if (testId) {
            result = JobResult.findByJobAndTestId(job, testId)
        }

        if (!result) {
            return persistNewUnfinishedJobResult(job, testId, jobResultStatus, wptStatus, description)
        } else {
            updateStatusAndPersist(result, job, testId, jobResultStatus, wptStatus, description)
            return result
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
        WptResultXml wptResult = pollJobRun(job, testId)
        if (wptResult.isFinishedWithResults()) {
            return
        }
        jobSchedulingService.unscheduleTest(job, testId)
        String description = "Test exceeded maximum polling time. Test had result code ${wptResult.wptStatus}."
        if (!wptResult.hasRuns()) {
            description += " XML result contains no runs."
        }
        persistUnfinishedJobResult(job.id, testId, JobResultStatus.TIMEOUT, wptResult.wptStatus, description)
        wptInstructionService.cancelTest(job.location.wptServer, [test: testId])
    }

    /**
     * Updates the status of a currently running job.
     * If the Job terminated (successfully or unsuccessfully) the Quartz trigger calling pollJobRun()
     * every pollDelaySeconds seconds is removed
     */
    WptResultXml pollJobRun(Job job, String testId) {
        WptResultXml resultXml
        try {
            performanceLoggingService.logExecutionTime(DEBUG, "Polling jobrun ${testId} of job ${job.id}: fetching results from wptrserver.", 1) {
                resultXml = wptInstructionService.fetchResult(job.location.wptServer, testId, job)
            }
            WptStatus wptStatus = resultXml.wptStatus
            if (wptStatus == WptStatus.PENDING || wptStatus == WptStatus.IN_PROGRESS) {
                JobResultStatus jobResultStatus = (wptStatus == WptStatus.PENDING) ? JobResultStatus.WAITING : JobResultStatus.RUNNING
                performanceLoggingService.logExecutionTime(DEBUG, "Polling jobrun ${testId} of job ${job.id}: updating jobresult.", 1) {
                    persistUnfinishedJobResult(job.id, testId, jobResultStatus, wptStatus, "polling jobrun")
                }
            }
        } catch (Exception e) {
            log.error("Polling jobrun ${testId} of job ${job.label}: An unexpected exception occured. Error gets persisted as unfinished JobResult now", e)
            persistUnfinishedJobResult(job.id, testId, JobResultStatus.PERSISTANCE_ERROR, WptStatus.UNKNOWN, e.getMessage())
        } finally {
            if (resultXml && resultXml.isFinishedWithResults()) {
                jobSchedulingService.unscheduleTest(job, testId)
            }
        }
        return resultXml
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
            jobSchedulingService.scheduleJobRunPolling(job, testId)
            // TODO(sbr): determine WptStatus and JobResultStatus
            persistUnfinishedJobResult(job.id, testId, JobResultStatus.WAITING, WptStatus.UNKNOWN)
            return testId
        } catch (Exception e) {
            WptStatus wptStatus = e instanceof JobExecutionException ? e.wptStatus : WptStatus.TEST_DID_NOT_START
            String testId = e instanceof JobExecutionException ? e.testId : null
            persistUnfinishedJobResult(job.id, testId, JobResultStatus.LAUNCH_ERROR, wptStatus, e.getMessage())
            throw new RuntimeException("An error occurred while launching job ${job.label}. Unfinished JobResult with error code will get persisted now: ${ExceptionUtils.getFullStackTrace(e)}")
        }
    }

    private void updateStatusAndPersist(JobResult result, Job job, String testId, JobResultStatus jobResultStatus, WptStatus wptStatus, String description) {
        log.debug("Updating status of existing JobResult: Job ${job.label}, test-id=${testId}")
        if (result.jobResultStatus != jobResultStatus || (wptStatus != null && result.wptStatus != wptStatus)) {
            updateStatus(result, jobResultStatus, wptStatus, description)
            try {
                result.save(failOnError: true, flush: true)
            } catch (StaleObjectStateException staleObjectStateException) {
                String logMessage = "Updating status of existing JobResult: Job ${job.label}, test-id=${testId}" +
                        "\n\t-> jobResultStatus of result couldn't get updated from ${result.jobResultStatus}->${jobResultStatus}"
                if (wptStatus != null) logMessage += "\n\twptStatus of result couldn't get updated from ${result.wptStatus}->${wptStatus}"
                log.error(logMessage, staleObjectStateException)
            }

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

    private void updateStatus(JobResult result, JobResultStatus jobResultStatus, WptStatus wptStatus, String description) {
        log.debug("Updating status of existing JobResult: jobResultStatus: ${result.jobResultStatus}->${jobResultStatus}")
        result.jobResultStatus = jobResultStatus
        result.description = description
        if (wptStatus != null) {
            log.debug("Updating status of existing JobResult: wptStatus: ${result.wptStatus}->${wptStatus}")
            result.wptStatus = wptStatus
        }
    }

    private JobResult persistNewUnfinishedJobResult(Job job, String testId, JobResultStatus jobResultStatus, WptStatus wptStatus, String description) {
        JobResult result = new JobResult(
                job: job,
                date: new Date(),
                testId: testId ?: UUID.randomUUID() as String,
                har: null,
                description: description,
                jobConfigLabel: job.label,
                jobConfigRuns: job.runs,
                wptServerLabel: job.location.wptServer.label,
                wptServerBaseurl: job.location.wptServer.baseUrl,
                locationLabel: job.location.label,
                locationLocation: job.location.location,
                locationBrowser: job.location.browser.name,
                locationUniqueIdentifierForServer: job.location.uniqueIdentifierForServer,
                jobGroupName: job.jobGroup.name,
                jobResultStatus: jobResultStatus)
        if (wptStatus != null) result.wptStatus = wptStatus
        log.debug("Persisting of unfinished result: Job ${job.label}, test-id=${testId} -> persisting new JobResult=${result}")
        return result.save(failOnError: true, flush: true)
    }

}

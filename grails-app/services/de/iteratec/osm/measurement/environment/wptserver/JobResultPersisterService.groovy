package de.iteratec.osm.measurement.environment.wptserver

import de.iteratec.osm.measurement.environment.WebPageTestServer
import de.iteratec.osm.measurement.schedule.Job
import de.iteratec.osm.measurement.schedule.JobDaoService
import de.iteratec.osm.result.JobResult
import de.iteratec.osm.result.JobResultStatus
import de.iteratec.osm.result.WptStatus
import de.iteratec.osm.util.PerformanceLoggingService
import grails.async.Promise
import grails.gorm.transactions.Transactional
import org.hibernate.StaleObjectStateException
import org.springframework.transaction.annotation.Propagation

import java.util.concurrent.locks.ReentrantLock

import static de.iteratec.osm.util.PerformanceLoggingService.LogLevel.DEBUG
import static grails.async.Promises.task

interface iResultListener {
    String getListenerName()

    void listenToResult(WptResultXml resultXml, WebPageTestServer wptserver, long jobId)

    boolean callListenerAsync()
}


@Transactional
class JobResultPersisterService {
    JobDaoService jobDaoService
    PerformanceLoggingService performanceLoggingService
    private final ReentrantLock lock = new ReentrantLock()
    protected List<iResultListener> resultListeners = new ArrayList<iResultListener>()


    void addResultListener(iResultListener listener) {
        this.resultListeners.add(listener)
    }

    /**
     * Saves a JobResult with the given parameters and no date to indicate that the
     * specified Job/test is running and that this is not the result of a finished
     * test execution.
     */
    JobResult persistUnfinishedJobResult(Job job, String testId, JobResultStatus jobResultStatus, String description = '') {
        JobResult result = JobResult.findByJobAndTestId(job, testId)
        WptStatus wptStatus = result ? result.wptStatus : WptStatus.UNKNOWN
        return persistUnfinishedJobResult(job, testId, jobResultStatus, wptStatus, description)
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    JobResult persistUnfinishedJobResult(Job job, String testId, JobResultStatus jobResultStatus, WptStatus wptStatus, String description = '') {
        JobResult result = testId ? JobResult.findByJobAndTestId(job, testId) : null
        if (!result) {
            result = persistNewUnfinishedJobResult(job, testId, jobResultStatus, wptStatus, description, new Date())
        } else if (wptStatus != result.wptStatus || jobResultStatus != result.jobResultStatus) {
            updateAndPersistJobResult(result, testId, jobResultStatus, wptStatus, description)
        }
        return result
    }

    def persistMissingJobResult(Job job, Date executionDate) {
        persistNewUnfinishedJobResult(job, "", JobResultStatus.DID_NOT_START, WptStatus.TEST_DID_NOT_START, "Missing JobResult", executionDate)
    }

    JobResultStatus handleWptResult(WptResultXml resultXml, String testId, Job job) {
        JobResultStatus jobResultStatus = determineJobResultStatusFromWptResult(resultXml, testId, job)
        log.info("Jobrun ${testId} has status ${jobResultStatus}")
        if (jobResultStatus.isTerminated()) {
            performanceLoggingService.logExecutionTime(DEBUG, "Persisting finished jobrun ${testId} of job ${job.id}.", 1) {
                processFinishedJobResult(resultXml, jobResultStatus, job)
            }
        } else {
            performanceLoggingService.logExecutionTime(DEBUG, "Polling jobrun ${testId} of job ${job.id}: updating jobresult.", 1) {
                persistUnfinishedJobResult(job, testId, jobResultStatus, resultXml.wptStatus, "Polling job run.")
            }
        }
        return jobResultStatus
    }


    private JobResultStatus determineJobResultStatusFromWptResult(WptResultXml resultXml, String testId, Job job) {
        WptStatus wptStatus = resultXml.wptStatus
        log.debug("WptStatus of ${testId}: ${wptStatus.toString()} (${wptStatus.wptStatusCode})")
        log.debug("resultXml.hasRuns()=${resultXml.hasRuns()}")
        log.debug("resultXml.runCount=${resultXml.hasRuns() ? resultXml.runCount : null}")
        if (wptStatus.isFailed()) {
            return JobResultStatus.FAILED
        } else if (wptStatus.isSuccess() && resultXml.hasRuns()) {
            return determineJobResultStatusFromCompletedTest(resultXml, job, testId)
        }
        if (wptStatus == WptStatus.IN_PROGRESS) {
            return JobResultStatus.RUNNING
        }
        return JobResultStatus.WAITING // keep polling
    }

    private JobResultStatus determineJobResultStatusFromCompletedTest(WptResultXml resultXml, Job job, String testId) {
        JobResultStatus fallbackStatus = resultXml.getTestStepCount() > 0 ? JobResultStatus.INCOMPLETE : JobResultStatus.FAILED
        JobResult jobResult = JobResult.findByJobAndTestId(job, testId)
        if (!jobResult) {
            log.error("There is no job result for finished job id ${job.id} and test id ${testId}!")
            return fallbackStatus
        }
        int numExpectedResults = jobResult.jobConfigRuns * jobResult.expectedSteps * (jobResult.firstViewOnly ? 1 : 2)
        if (numExpectedResults < 1) {
            log.warn("Number of expected results for job id ${job.id} and test id ${testId} is 0!")
            return fallbackStatus
        }
        int numValidResults = resultXml.countValidResults(jobResult.jobConfigRuns, jobResult.expectedSteps, jobResult.firstViewOnly)
        if (numValidResults < numExpectedResults) {
            log.info("Test ${testId} from job ${job.id} has only ${numValidResults} valid results, expected ${numExpectedResults}")
            return numValidResults < 1 ? JobResultStatus.FAILED : JobResultStatus.INCOMPLETE
        }
        return JobResultStatus.SUCCESS
    }

    private processFinishedJobResult(WptResultXml resultXml, JobResultStatus jobResultStatus, Job job) {
        try {
            lock.lockInterruptibly()
            persistFinishedJobResult(resultXml, jobResultStatus, job.id)
            if (jobResultStatus.hasResults()) {
                invokeResultPersisters(resultXml, job.location.wptServer, job.id)
            }
        } finally {
            lock.unlock()
        }
    }

    private invokeResultPersisters(WptResultXml resultXml, WebPageTestServer wptServer, long jobId) {
        this.resultListeners.each { listener ->
            log.info("calling listener ${listener.listenerName} for job id ${jobId}")
            if (listener.callListenerAsync()) {
                Promise p = task {
                    JobResult.withNewSession {
                        listener.listenToResult(resultXml, wptServer, jobId)
                    }
                }
                p.onError { Throwable err -> log.error("${listener.getListenerName()} failed persisting results", err) }
                p.onComplete { log.info("${listener.getListenerName()} successfully returned from async task") }
            } else {
                listener.listenToResult(resultXml, wptServer, jobId)
            }
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private void persistFinishedJobResult(WptResultXml resultXml, JobResultStatus jobResultStatus, long jobId) throws OsmResultPersistanceException {
        performanceLoggingService.logExecutionTime(DEBUG, "persist JobResult for job ${resultXml.getLabel()}, test ${resultXml.getTestId()}...", 4) {
            String testId = resultXml.getTestId()
            log.debug("test-ID for which results should get persisted now=${testId}, jobResultStatus=${jobResultStatus}")

            if (testId == null) {
                throw new OsmResultPersistanceException("No test id in result xml file from wpt server!")
            }

            log.debug("Deleting pending JobResults and create finished ...")
            Job job = jobDaoService.getJob(jobId)
            Date executionDate = getExecutionDateFromUnfinished(job, testId)
            deleteUnfinishedJobResults(job, testId)
            JobResult jobResult = JobResult.findByJobAndTestId(job, testId)
            if (!jobResult) {
                persistNewFinishedJobResult(job, testId, jobResultStatus, resultXml, executionDate)
            } else {
                updateJobResult(jobResult, jobResultStatus, resultXml)
            }
        }
    }


    private void updateJobResult(JobResult jobResult, JobResultStatus jobResultStatus, WptResultXml resultXml) {
        jobResult.testAgent = resultXml.getTestAgent()
        jobResult.wptVersion = resultXml.version.toString()
        updateAndPersistJobResult(jobResult, resultXml.testId, jobResultStatus, resultXml.wptStatus, resultXml.getStatusText())
    }

    private void updateAndPersistJobResult(JobResult result, String testId, JobResultStatus jobResultStatus, WptStatus wptStatus, String description = '') {
        log.debug("Updating status of existing JobResult: Job ${result.job.id}, test-id=${testId}")
        result.jobResultStatus = jobResultStatus
        result.description = description
        result.wptStatus = wptStatus
        try {
            result.save(failOnError: true, flush: true)
        } catch (StaleObjectStateException staleObjectStateException) {
            String logMessage = "Updating status of existing JobResult: Job ${result.job.id}, test-id=${testId}" +
                    "\n\t-> jobResultStatus of result couldn't get updated from ${result.jobResultStatus}->${jobResultStatus}" +
                    "\n\t-> wptStatus of result couldn't get updated from ${result.wptStatus}->${wptStatus}"
            log.error(logMessage, staleObjectStateException)
        }
    }

    private JobResult persistNewUnfinishedJobResult(
            Job job,
            String testId,
            JobResultStatus jobResultStatus,
            WptStatus wptStatus,
            String description,
            Date execDate) {
        JobResult result = new JobResult(
                job: job,
                date: new Date(),
                executionDate: execDate,
                testId: testId ?: UUID.randomUUID() as String,
                description: description,
                jobConfigLabel: job.label,
                jobConfigRuns: job.runs,
                expectedSteps: job.script.measuredEventsCount,
                firstViewOnly: job.firstViewOnly,
                wptServerLabel: job.location.wptServer.label,
                wptServerBaseurl: job.location.wptServer.baseUrl,
                locationLabel: job.location.label,
                locationLocation: job.location.location,
                locationBrowser: job.location.browser.name,
                locationUniqueIdentifierForServer: job.location.uniqueIdentifierForServer,
                jobGroupName: job.jobGroup.name,
                wptStatus: wptStatus,
                jobResultStatus: jobResultStatus)
        log.debug("Persisting of unfinished result: Job ${job.label}, test-id=${testId} -> persisting new JobResult=${result}")
        return result.save(failOnError: true, flush: true)
    }

    private void deleteUnfinishedJobResults(Job job, String testId) {
        JobResult.findAllByJobAndTestIdAndJobResultStatusInList(job, testId, [JobResultStatus.WAITING, JobResultStatus.RUNNING]).each {
            it.delete(failOnError: true, flush: true)
        }
    }

    private Date getExecutionDateFromUnfinished(Job job, String testId) {
        return JobResult.findByJobAndTestIdAndJobResultStatusInList(job, testId, [JobResultStatus.WAITING, JobResultStatus.RUNNING])?.executionDate
    }

    private void persistNewFinishedJobResult(Job job, String testId, JobResultStatus jobResultStatus, WptResultXml resultXml, Date execDate) {
        log.debug("persisting new JobResult ${testId}")
        Date testCompletion = resultXml.getCompletionDate() ?: new Date()
        JobResult result = new JobResult(
                job: job,
                executionDate: execDate,
                date: testCompletion,
                testId: testId,
                wptStatus: resultXml.getWptStatus(),
                jobResultStatus: jobResultStatus,
                jobConfigLabel: job.label,
                jobConfigRuns: job.runs,
                expectedSteps: job.script.measuredEventsCount,
                firstViewOnly: job.firstViewOnly,
                wptServerLabel: job.location.wptServer.label,
                wptServerBaseurl: job.location.wptServer.baseUrl,
                locationLabel: job.location.label,
                locationLocation: job.location.location,
                locationUniqueIdentifierForServer: job.location.uniqueIdentifierForServer,
                locationBrowser: job.location.browser.name,
                jobGroupName: job.jobGroup.name,
                testAgent: resultXml.getTestAgent(),
                wptVersion: resultXml.version.toString(),
        )
        //new 'feature' of grails 2.3: empty strings get converted to null in map-constructors
        result.setDescription(resultXml.getStatusText())
        result.save(failOnError: true, flush: true)
        job.lastRun = testCompletion
        job.merge(failOnError: true)
    }
}

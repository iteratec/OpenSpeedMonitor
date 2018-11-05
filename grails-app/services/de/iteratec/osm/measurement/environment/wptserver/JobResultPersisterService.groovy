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
    JobResult persistUnfinishedJobResult(long jobId, String testId, JobResultStatus jobResultStatus, String description = '') {
        JobResult result = JobResult.findByJobAndTestId(Job.get(jobId), testId)
        WptStatus wptStatus = result ? result.wptStatus : WptStatus.UNKNOWN
        return persistUnfinishedJobResult(jobId, testId, jobResultStatus, wptStatus, description)
    }

    JobResult persistUnfinishedJobResult(long jobId, String testId, JobResultStatus jobResultStatus, WptStatus wptStatus, String description = '') {
        Job job = Job.get(jobId)
        JobResult result = testId ? JobResult.findByJobAndTestId(job, testId) : null
        if (!result) {
            result = persistNewUnfinishedJobResult(job, testId, jobResultStatus, wptStatus, description)
        } else {
            updateJobResultStatus(result, job, testId, jobResultStatus, wptStatus, description)
        }
        return result
    }

    JobResultStatus handleWptResult(WptResultXml resultXml, String testId, Job job) {
        JobResultStatus jobResultStatus = determineJobResultStatusFromWptResult(resultXml, testId, job)
        if (jobResultStatus.isTerminated()) {
            performanceLoggingService.logExecutionTime(DEBUG, "Persisting finished jobrun ${testId} of job ${job.id}.", 1) {
                processFinishedJobResult(resultXml, jobResultStatus, job)
            }
        } else {
            performanceLoggingService.logExecutionTime(DEBUG, "Polling jobrun ${testId} of job ${job.id}: updating jobresult.", 1) {
                persistUnfinishedJobResult(job.id, testId, jobResultStatus, resultXml.wptStatus, "polling jobrun")
            }
        }
        return jobResultStatus
    }


    private JobResultStatus determineJobResultStatusFromWptResult(WptResultXml resultXml, String testId, Job job) {
        WptStatus wptStatus = resultXml.wptStatus
        log.info("WptStatus of ${testId}: ${wptStatus.toString()} (${wptStatus.wptStatusCode})")
        log.info("resultXml.hasRuns()=${resultXml.hasRuns()}|")
        log.info("resultXml.runCount=${resultXml.hasRuns() ? resultXml.runCount : null}")
        if (wptStatus.isFailed()) {
            return JobResultStatus.FAILED
        } else if (wptStatus.isSuccess() && resultXml.hasRuns()) {
            return JobResultStatus.SUCCESS
        }
        // TODO(sbr): Check for timeout
        if (wptStatus == WptStatus.IN_PROGRESS) {
            return JobResultStatus.RUNNING
        }
        return JobResultStatus.WAITING // keep polling
    }

    private processFinishedJobResult(WptResultXml resultXml, JobResultStatus jobResultStatus, Job job) {
        try {
            persistFinishedJobResult(resultXml, jobResultStatus, job.id)
            WebPageTestServer wptServer = job.location.wptServer
            lock.lockInterruptibly()
            this.resultListeners.each { listener ->
                log.info("calling listener ${listener.listenerName} for job id ${job.id}")
                if (listener.callListenerAsync()) {
                    Promise p = task {
                        JobResult.withNewSession {
                            listener.listenToResult(resultXml, wptServer, job.id)
                        }
                    }
                    p.onError { Throwable err -> log.error("${listener.getListenerName()} failed persisting results", err) }
                    p.onComplete { log.info("${listener.getListenerName()} successfully returned from async task") }
                } else {
                    listener.listenToResult(resultXml, wptServer, job.id)
                }
            }
        } finally {
            lock.unlock()
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
            deleteUnfinishedJobResults(job, testId)
            JobResult jobResult = JobResult.findByJobAndTestId(job, testId)
            if (!jobResult) {
                persistNewFinishedJobResult(job, testId, jobResultStatus, resultXml)
            } else {
                updateJobResult(jobResult, jobResultStatus, resultXml)
            }
        }
    }

    private void updateJobResultStatus(JobResult result, Job job, String testId, JobResultStatus jobResultStatus, WptStatus wptStatus, String description = '') {
        log.debug("Updating status of existing JobResult: Job ${job.label}, test-id=${testId}")
        if (result.jobResultStatus == jobResultStatus && result.wptStatus == wptStatus) {
            return
        }
        result.jobResultStatus = jobResultStatus
        result.description = description
        result.wptStatus = wptStatus
        try {
            result.save(failOnError: true, flush: true)
        } catch (StaleObjectStateException staleObjectStateException) {
            String logMessage = "Updating status of existing JobResult: Job ${job.label}, test-id=${testId}" +
                    "\n\t-> jobResultStatus of result couldn't get updated from ${result.jobResultStatus}->${jobResultStatus}" +
                    "\n\t-> wptStatus of result couldn't get updated from ${result.wptStatus}->${wptStatus}"
            log.error(logMessage, staleObjectStateException)
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
                wptStaus: wptStatus,
                jobResultStatus: jobResultStatus)
        log.debug("Persisting of unfinished result: Job ${job.label}, test-id=${testId} -> persisting new JobResult=${result}")
        return result.save(failOnError: true, flush: true)
    }

    private void deleteUnfinishedJobResults(Job job, String testId) {
        JobResult.findByJobAndTestIdAndWptStatus(job, testId, WptStatus.IN_PROGRESS)?.delete(failOnError: true, flush: true)
        JobResult.findByJobAndTestIdAndWptStatus(job, testId, WptStatus.PENDING)?.delete(failOnError: true, flush: true)
    }

    private void updateJobResult(JobResult jobResult, JobResultStatus jobResultStatus, WptResultXml resultXml) {
        jobResult.testAgent = resultXml.getTestAgent()
        jobResult.wptVersion = resultXml.version.toString()
        jobResult.wptStatus = resultXml.wptStatus
        jobResult.jobResultStatus = jobResultStatus
        jobResult.save(failOnError: true, flush: true)
    }

    private void persistNewFinishedJobResult(Job job, String testId, JobResultStatus jobResultStatus, WptResultXml resultXml) {
        log.debug("persisting new JobResult ${testId}")
        Date testCompletion = resultXml.getCompletionDate()
        JobResult result = new JobResult(
                job: job,
                date: testCompletion,
                testId: testId,
                wptStatus: resultXml.getWptStatus(),
                jobResultStatus: jobResultStatus,
                jobConfigLabel: job.label,
                jobConfigRuns: job.runs,
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
        result.setDescription('')
        result.save(failOnError: true, flush: true)
        job.lastRun = testCompletion
        job.merge(failOnError: true)
    }
}

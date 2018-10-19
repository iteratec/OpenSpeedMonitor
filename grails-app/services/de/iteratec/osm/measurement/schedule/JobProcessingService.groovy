/*
* OpenSpeedMonitor (OSM)
* Copyright 2014 iteratec GmbH
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* 	http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package de.iteratec.osm.measurement.schedule

import de.iteratec.osm.ConfigService
import de.iteratec.osm.InMemoryConfigService
import de.iteratec.osm.measurement.environment.WebPageTestServer
import de.iteratec.osm.measurement.environment.wptserver.WptInstructionService
import de.iteratec.osm.measurement.environment.wptserver.WptResultXml
import de.iteratec.osm.measurement.schedule.Job
import de.iteratec.osm.measurement.schedule.JobExecutionException
import de.iteratec.osm.measurement.schedule.quartzjobs.JobProcessingQuartzHandlerJob
import de.iteratec.osm.result.JobResult
import de.iteratec.osm.result.JobResultStatus
import de.iteratec.osm.result.WptStatus
import de.iteratec.osm.result.WptStatusFactory
import de.iteratec.osm.util.PerformanceLoggingService
import grails.gorm.transactions.NotTransactional
import grails.gorm.transactions.Transactional
import groovy.time.TimeCategory
import groovy.util.slurpersupport.GPathResult
import groovy.util.slurpersupport.NodeChild
import org.apache.commons.lang.exception.ExceptionUtils
import org.hibernate.StaleObjectStateException
import org.joda.time.DateTime
import org.quartz.*

import static de.iteratec.osm.util.PerformanceLoggingService.LogLevel.DEBUG

class JobExecutionException extends Exception {
    public String htmlResult

    public JobExecutionException(String message) {
        super(message)
    }

    public JobExecutionException(String message, String htmlResult) {
        super(message)
        this.htmlResult = htmlResult
    }
}

enum TriggerGroup {
    JOB_TRIGGER_LAUNCH('OSMJobCycles'),
    JOB_TRIGGER_POLL('OSMJobCyclePolls'),
    JOB_TRIGGER_TIMEOUT('OSMTimeoutTriggers')

    private final String value

    private TriggerGroup(String value) {
        this.value = value
    }

    String value() {
        value
    }
}

/**
 * This service provides functionality for launching Jobs and for scheduling management.
 *
 * @author dri
 */
@Transactional
class JobProcessingService {

    WptInstructionService wptInstructionService
    def quartzScheduler
    ConfigService configService
    InMemoryConfigService inMemoryConfigService
    PerformanceLoggingService performanceLoggingService
    JobDaoService jobDaoService
    WptStatusFactory wptStatusFactory = new WptStatusFactory()

    /**
     * Time to wait during Job processing before each check if results are available
     */
    private int pollDelaySeconds = 30
    /**
     * Time to wait after maxDownloadTime of a job has been exceeded before declaring
     * a timeout error
     */
    private final int declareTimeoutAfterMaxDownloadTimePlusSeconds = 60

    public void setPollDelaySeconds(int pollDelaySeconds) {
        this.pollDelaySeconds = pollDelaySeconds
    }

    /**
     * Maps the properties of a Job to the parameters expected by
     * the REST API available at https://sites.google.com/a/webpagetest.org/docs/advanced-features/webpagetest-restful-apis
     *
     * @return A map of parameters suitable for POSTing a valid request to runtest.php
     */
    private Map fillRequestParameters(Job job) {
        Map parameters = [
                url            : '',
                label          : job.label,
                location       : job.location.uniqueIdentifierForServer,
                runs           : job.runs,
                fvonly         : job.firstViewOnly,
                video          : job.captureVideo,
                web10          : job.web10,
                noscript       : job.noscript,
                clearcerts     : job.clearcerts,
                ignoreSSL      : job.ignoreSSL,
                standards      : job.standards,
                tcpdump        : job.tcpdump,
                continuousVideo: job.continuousVideo,
                private        : job.isPrivate,
                block          : job.urlsToBlock,
                mobile         : job.emulateMobile,
                dpr            : job.devicePixelRation,
                cmdline        : job.cmdlineOptions,
                custom         : job.customMetrics,
                tester         : job.tester,
                timeline       : job.captureTimeline,
                timelineStack  : job.javascriptCallstack,
                mobileDevice   : job.mobileDevice,
                lighthouse     : job.performLighthouseTest,
                type           : job.optionalTestTypes,
                customHeaders  : job.customHeaders,
                trace          : job.trace,
                traceCategories: job.traceCategories,
                spof           : job.spof
        ]
        if (job.takeScreenshots == Job.TakeScreenshots.NONE) {
            parameters.noimages = true
        }
        else if (job.takeScreenshots == Job.TakeScreenshots.FULL) {
            parameters.pngss = true
        }
        else {
            parameters.iq = job.imageQuality
        }

        if (job.saveBodies == Job.SaveBodies.HTML) {
            parameters.htmlbody = true
        }
        else if (job.saveBodies == Job.SaveBodies.ALL) {
            parameters.bodies = true
        }

        if (configService.globalUserAgentSuffix && job.useGlobalUASuffix) {
            parameters.appendua = configService.globalUserAgentSuffix
        }
        else {
            if(job.userAgent == Job.UserAgent.ORIGINAL) {
                parameters.keepua = true
            }
            else if(job.userAgent == Job.UserAgent.APPEND) {
                parameters.appendua = job.appendUserAgent
            }
            else if(job.userAgent == Job.UserAgent.OVERWRITE) {
                parameters.uastring = job.userAgentString
            }
        }


        if (job.noTrafficShapingAtAll) {
            parameters.location += ".Native"
        } else {
            parameters.location += ".custom"
            if (job.customConnectivityProfile) {
                parameters.bwDown = job.bandwidthDown
                parameters.bwUp = job.bandwidthUp
                parameters.latency = job.latency
                parameters.plr = job.packetLoss
            } else {
                parameters.bwDown = job.connectivityProfile.bandwidthDown
                parameters.bwUp = job.connectivityProfile.bandwidthUp
                parameters.latency = job.connectivityProfile.latency
                parameters.plr = job.connectivityProfile.packetLoss
            }
        }

        if (job.script) {
            parameters.script = job.script.getParsedNavigationScript(job)
            if (job.provideAuthenticateInformation) {
                parameters.login = job.authUsername
                parameters.password = job.authPassword
            }
        }

        String apiKey = job.location.wptServer.apiKey
        if (apiKey) {
            parameters.k = apiKey
        }

        parameters.f='xml'

        // convert all boolean parameters to 0 or 1
        parameters = parameters.each {
            if (it.value instanceof Boolean && it.value != null)
                it.value = it.value ? 1 : 0
        }
        return parameters
    }

    /**
     * Builds an ID string from the ID of the given Job and the supplied testId.
     * This ID is used to identify those Quartz triggers that are created when a
     * Job is started and fire every pollDelaySeconds seconds to poll for a result.
     */
    private String getSubtriggerId(Job job, String testId) {
        return String.format('%d:%s', job.id, testId)
    }

    /**
     * Saves a JobResult with the given parameters and no date to indicate that the
     * specified Job/test is running and that this is not the result of a finished
     * test execution.
     */
    @Transactional
    private JobResult persistUnfinishedJobResult(long jobId, String testId, JobResultStatus jobResultStatus, WptStatus wptStatus, String description = '') {
        // If no testId was provided some error occurred and needs to be logged
        Job job = Job.get(jobId)
        JobResult result
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

    @Transactional
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

    private NodeChild parseResponse(def runtestResponse, WebPageTestServer wptserver){
        NodeChild runtestResponseXml
        if(runtestResponse instanceof NodeChild || runtestResponse instanceof GPathResult){
            runtestResponseXml = runtestResponse
            return runtestResponseXml
        } else {
            throw new JobExecutionException("Response is not XML from wptserver ${wptserver}")
        }
    }

    /**
     * Build the trigger designed to poll WPTServer for new results every pollDelaySeconds (dubbed subtrigger)
     * until endDate.
     * @param endDate Poll until endDate or indefinitely, if endDate is null
     */
    private Trigger buildSubTrigger(Job job, String testId, Date endDate) {
        TriggerBuilder subTrigger = TriggerBuilder.newTrigger()
                .withIdentity(getSubtriggerId(job, testId), TriggerGroup.JOB_TRIGGER_POLL.value())
                .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                .withIntervalInSeconds(pollDelaySeconds)
                .repeatForever()
        // If application / scheduler is down for some time and becomes available again
        // polling will be continued until maxDownloadTimeInMinutes has been reached:
                .withMisfireHandlingInstructionNextWithRemainingCount())
        if (endDate)
            subTrigger = subTrigger.endAt(endDate);
        return subTrigger.build()
    }

    /**
     * Build the trigger to handle a job run timeout. Runs once at timeoutDate.
     */
    private Trigger buildTimeoutTrigger(Job job, String testId, Date timeoutDate) {
        TriggerBuilder timeoutTrigger = TriggerBuilder.newTrigger()
                .withIdentity(getSubtriggerId(job, testId), TriggerGroup.JOB_TRIGGER_TIMEOUT.value())
                .withSchedule(SimpleScheduleBuilder.simpleSchedule()
        // timeout handling is executed immediately after the scheduler discovers a misfire situation
                .withMisfireHandlingInstructionFireNow())
                .startAt(timeoutDate)
        return timeoutTrigger.build()
    }

    /**
     * If measurements are generally enabled: Launches the given Job and creates a Quartz trigger to poll for new results every x seconds.
     * If they are not enabled nothing happens.
     * @return the testId of the running job.
     */
    String launchJobRun(Job job, priority = 5) {

        if (!inMemoryConfigService.areMeasurementsGenerallyEnabled()) {
            throw new IllegalStateException("Job run of Job ${job} is skipped cause measurements are generally disabled.")
        }
        if (inMemoryConfigService.pauseJobProcessingForOverloadedLocations){
            //TODO: Implement logic for IT-1334 if we have example LocationHealthChecks for a real location under load.
            throw new IllegalStateException("Job run of Job ${job} is skipped cause overloaded locations.")
        }

        WptStatus wptStatus
        String testId
        try {
            def parameters = fillRequestParameters(job)
            parameters['priority'] = priority


            WebPageTestServer wptserver = job.location.wptServer
            if (!wptserver) {
                throw new JobExecutionException("Missing wptServer in job ${job.label}");
            }

            def result
            performanceLoggingService.logExecutionTime(DEBUG, "Launching job ${job.label}: Calling initial runtest on wptserver.", 1) {
                result = wptInstructionService.runtest(wptserver, parameters);
            }

            NodeChild runtestResponseXml = parseResponse(result, wptserver)
            wptStatus = wptStatusFactory.buildWptStatus(runtestResponseXml?.statusCode?.toInteger())
            testId = runtestResponseXml?.data?.testId
            if (wptStatus != WptStatus.COMPLETED) {
                throw new JobExecutionException("Got status code ${wptStatus} from wptserver ${wptserver}")
            }
            if(!testId){
                throw new JobExecutionException("Jobrun failed for: wptserver=${wptserver}, sent params=${parameters} => got no testId in response");
            } else {
                log.info("Jobrun successfully launched: wptserver=${wptserver}, sent params=${parameters}, got testID: ${testId}")
            }

            use(TimeCategory) {
                Map jobDataMap = [jobId: job.id, testId: testId]
                Date endDate = new Date() + job.maxDownloadTimeInMinutes.minutes
                // build and schedule subtrigger for polling every pollDelaySeconds seconds.
                // Polling is stopped by pollJobRun() when the running test is finished or on endDate at the latest
                log.debug("Building subtrigger for polling for job ${job.label} and test-id ${testId} (enddate ${endDate})")
                JobProcessingQuartzHandlerJob.schedule(buildSubTrigger(job, testId, endDate), jobDataMap)
                // schedule a timeout trigger which will fire once after endDate + a delay of
                // declareTimeoutAfterMaxDownloadTimePlusSeconds seconds and perform cleaning operations.
                // This will also set this job run's status to 504 Timeout.
                Date timeoutDate = endDate + declareTimeoutAfterMaxDownloadTimePlusSeconds.seconds
                log.debug("Building timeout trigger for job ${job.label} and test-id ${testId} (timeout date=${timeoutDate}, jobDataMap=${jobDataMap})")
                JobProcessingQuartzHandlerJob.schedule(buildTimeoutTrigger(job, testId, timeoutDate), jobDataMap)
            }

            return testId
        } catch (Exception e) {
            wptStatus = wptStatus ? wptStatus : WptStatus.TEST_DID_NOT_START
            persistUnfinishedJobResult(job.id, testId, wptStatus, JobResultStatus.LAUNCH_ERROR, e.getMessage())
            throw new RuntimeException("An error occurred while launching job ${job.label}. Unfinished JobResult with error code will get persisted now: ${ExceptionUtils.getFullStackTrace(e)}")
        }
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
            performanceLoggingService.logExecutionTime(DEBUG, "Polling jobrun ${testId} of job ${job.id}: updating jobresult.", 1) {
                if (resultXml.statusCodeOfWholeTest < 200) {
                    persistUnfinishedJobResult(job.id, testId, JobResultStatus.RUNNING, wptStatusFactory.buildWptStatus(resultXml.statusCodeOfWholeTest), "polling jobrun")
                }
            }
        } catch (Exception e) {
            log.error("Polling jobrun ${testId} of job ${job.label}: An unexpected exception occured. Error gets persisted as unfinished JobResult now", e)
            persistUnfinishedJobResult(job.id, testId, JobResultStatus.PERSISTANCE_ERROR, WptStatus.UNKNOWN, e.getMessage())
        } finally {
            if (resultXml && resultXml.statusCodeOfWholeTest >= 200 && resultXml.hasRuns()) {
                unscheduleTest(job, testId)
            }
        }
        return resultXml
    }

    private void unscheduleTest(Job job, String testId) {
        JobProcessingQuartzHandlerJob.removePollingLock(job, testId)
        JobProcessingQuartzHandlerJob.unschedule(getSubtriggerId(job, testId), TriggerGroup.JOB_TRIGGER_POLL.value())
        JobProcessingQuartzHandlerJob.unschedule(getSubtriggerId(job, testId), TriggerGroup.JOB_TRIGGER_TIMEOUT.value())
    }

    /**
     * Checks if this job instance is still unfinished. If so, the corresponding WPT Server is polled one last time
     * and if job is still reported as running, it's temporary JobResult is set to jobResultStatus 504 (timeout)
     * in the database. In addition, a request to cancelTest.php is made.
     */
    public void handleJobRunTimeout(Job job, String testId) {
        // Is there a non-running result? Then done
        JobResult result = JobResult.findByJobAndTestId(job, testId)
        if (!result)
            return
        if (result.wptStatus < WptStatus.COMPLETED) {
            // poll a last time
            WptResultXml lastResult = pollJobRun(job, testId)
            if (wptStatusFactory.buildWptStatus(lastResult.statusCodeOfWholeTest) < WptStatus.COMPLETED || (wptStatusFactory.buildWptStatus(lastResult.statusCodeOfWholeTest) >= WptStatus.COMPLETED && !lastResult.hasRuns())) {
                unscheduleTest(job, testId)
                String description = wptStatusFactory.buildWptStatus(lastResult.statusCodeOfWholeTest) < WptStatus.COMPLETED ? "Timeout of test" : "Test had result code ${lastResult.statusCodeOfWholeTest}. XML result contains no runs. Test exceeded maximum polling time"
                persistUnfinishedJobResult(job.id, testId, JobResultStatus.TIMEOUT, result.wptStatus, description)
                wptInstructionService.cancelTest(job.location.wptServer, [test: testId])
            }
        }
    }

    /**
     * Cancel a running Job. If the job is pending and has not been executed yet, the WPT server will
     * also terminate it. Otherwise it will be left running on the server but polling is stopped.
     */
    public void cancelJobRun(Job job, String testId) {
//		if (quartzScheduler.getTrigger(new TriggerKey(getSubtriggerId(job, testId)), TriggerGroup.JOB_TRIGGER_POLL.value()))
        log.info("unschedule quartz triggers for job run: job=${job.label},test id=${testId}")
        JobProcessingQuartzHandlerJob.removePollingLock(job, testId)
        JobProcessingQuartzHandlerJob.unschedule(getSubtriggerId(job, testId), TriggerGroup.JOB_TRIGGER_POLL.value())
        JobProcessingQuartzHandlerJob.unschedule(getSubtriggerId(job, testId), TriggerGroup.JOB_TRIGGER_TIMEOUT.value())
        log.info("unschedule quartz triggers for job run: job=${job.label},test id=${testId} ... DONE")
        JobResult result = JobResult.findByJobAndTestIdAndJobResultStatusLessThan(job, testId, JobResultStatus.SUCCESS)
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
     * Schedules a Quartz trigger to launch the given Job at the time(s) determined by its execution schedule Cron expression.
     */
    @NotTransactional
    public void scheduleJob(Job job, boolean rescheduleIfAlreadyScheduled = true) {
        if (!job.executionSchedule) {
            return
        }
        TriggerBuilder builder = TriggerBuilder.newTrigger()
                .withIdentity(job.id.toString(), TriggerGroup.JOB_TRIGGER_LAUNCH.value())
                .withSchedule(
                CronScheduleBuilder.cronSchedule(job.executionSchedule)
                // Immediately executes first misfired execution and discards other (i.e. all misfired executions are merged together).
                // Then back to schedule. No matter how many trigger executions were missed, only single immediate execution is performed.
                        .withMisfireHandlingInstructionFireAndProceed()
        )
        Trigger cronTrigger = builder.build()
        // job is already scheduled:
        if (quartzScheduler.getTrigger(new TriggerKey(job.id.toString(), TriggerGroup.JOB_TRIGGER_LAUNCH.value()))) {
            if (rescheduleIfAlreadyScheduled) {
                if (log.infoEnabled) log.info("Rescheduling Job ${job.label} (${job.executionSchedule})")
                JobProcessingQuartzHandlerJob.reschedule(cronTrigger, [jobId: job.id])
            } else {
                log.info("Ignoring Job ${job.label} as it is already scheduled.")
            }
        } else {
            try {
                log.info("Scheduling Job ${job.label} (${job.executionSchedule})")
                JobProcessingQuartzHandlerJob.schedule(cronTrigger, [jobId: job.id])
            } catch (SchedulerException se) {
                log.info("Job ${job.label} with schedule ${job.executionSchedule} can't be scheduled: ${se.message}", se)
            }
        }
    }

    @NotTransactional
    public void scheduleAllActiveJobs() {
        if (log.infoEnabled) log.info("Launching all active jobs")
        jobDaoService.getJobs(true).each {
            scheduleJob(it, false)
        }
    }

    /**
     * Removes the Quartz trigger for the specified Job
     */
    @NotTransactional
    public void unscheduleJob(Job job) {
        if (log.infoEnabled) log.info("Unscheduling Job ${job.label}")
        JobProcessingQuartzHandlerJob.unschedule(job.id.toString(), TriggerGroup.JOB_TRIGGER_LAUNCH.value())
    }

    public Map<String, Integer> handleOldJobResults() {
        log.info("handleOldJobResults() OSM starts")

        def jobResultsToDelete = JobResult.findAllByJobResultStatusLessThanAndDateLessThan(200, new DateTime().minusHours(2))
        def jobResultsToDeleteCount = jobResultsToDelete.size()
        if (!jobResultsToDelete.isEmpty()) {
            log.debug("Found ${jobResultsToDelete.size()} pending/running JobResults with JobResultStatus < 200 that are to old. Start deleting...")
            jobResultsToDelete.each { JobResult jobResult ->
                try {
                    jobResult.delete()
                } catch (Exception e) {
                    log.error("Wasn't able to delete old JobResult JobId = ${jobResult.jobId} TestId = ${jobResult.testId}" + e)
                }
            }
            log.debug("Deleting done.")
        }

        def jobResultsToReschedule = JobResult.findAllByJobResultStatusLessThanAndDateGreaterThan(200, new DateTime().minusHours(2))
        def jobResultsToRescheduleCount = jobResultsToReschedule.size()
        if (!jobResultsToReschedule.isEmpty()) {
            log.debug("Found ${jobResultsToReschedule.size()} pending/running JobResults with JobResultStatus < 200 fresh enough for rescheduling. Start rescheduling ...")
            jobResultsToReschedule.each { JobResult jobResult ->
                Map jobDataMap = [jobId: jobResult.jobId, testId: jobResult.testId]
                Date endDate = new DateTime(jobResult.date).plusMinutes(jobResult.job.maxDownloadTimeInMinutes).toDate()
                Date timeoutDate = new DateTime(endDate).plusSeconds(declareTimeoutAfterMaxDownloadTimePlusSeconds).toDate()
                if (new DateTime(jobResult.date).plusMinutes(jobResult.job.maxDownloadTimeInMinutes).plusSeconds(declareTimeoutAfterMaxDownloadTimePlusSeconds).toDate() > new Date()) {
                    try {
                        JobProcessingQuartzHandlerJob.schedule(buildSubTrigger(jobResult.job, jobResult.testId, endDate), jobDataMap)
                        JobProcessingQuartzHandlerJob.schedule(buildTimeoutTrigger(jobResult.job, jobResult.testId, timeoutDate), jobDataMap)
                    } catch (Exception e) {
                        log.error("Wasn't able to reschedule old JobResult JobId = ${jobResult.jobId} TestId = ${jobResult.testId}" + e)
                    }
                } else {

                    try {
                        JobProcessingQuartzHandlerJob.schedule(buildTimeoutTrigger(jobResult.job, jobResult.testId, new DateTime().plusMinutes(1).toDate()), jobDataMap)
                    } catch (Exception e) {
                        log.error("Wasn't able to reschedule old JobResult JobId = ${jobResult.jobId} TestId = ${jobResult.testId}" + e)
                    }
                }
            }
            log.debug("Rescheduling done.")
        }
        log.info("handleOldJobResults() OSM ends")
        return ["JobResultsToDeleteCount": jobResultsToDeleteCount, "JobResultsToRescheduleCount": jobResultsToRescheduleCount]
    }

    /**
     * Setting the http status code of running and pending jobResults older than maxDate
     */
    void closeRunningAndPengingJobResults() {
        DateTime currentDate = new DateTime()
        List<JobResult> jobResults = JobResult.findAllByJobResultStatusLessThan(WptStatus.SUCCESSFUL)
        jobResults = jobResults.findAll {
            // Close the jobResult, if its job timeout is exceeded by twice the amount
            currentDate > new DateTime(it.date).plusMinutes(it.job.maxDownloadTimeInMinutes * 2)
        }
        if (jobResults) {
            jobResults.each {
                it.jobResultStatus = JobResultStatus.ORPHANED
                it.description = "closed due to nightly cleanup of job results"
                it.save(failOnError: true)
            }
            log.info("Quartz controlled cleanup of running and pending jobResults: Chanaged status of ${jobResults.size()} jobResults.")
        }
    }
}

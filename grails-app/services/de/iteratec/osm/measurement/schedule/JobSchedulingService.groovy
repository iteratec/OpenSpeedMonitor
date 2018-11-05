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

import de.iteratec.osm.InMemoryConfigService
import de.iteratec.osm.measurement.schedule.Job
import de.iteratec.osm.measurement.schedule.quartzjobs.JobProcessingQuartzHandlerJob
import de.iteratec.osm.result.JobResult
import grails.gorm.transactions.NotTransactional
import grails.gorm.transactions.Transactional
import groovy.time.TimeCategory
import org.joda.time.DateTime
import org.quartz.*

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
class JobSchedulingService {

    def quartzScheduler
    InMemoryConfigService inMemoryConfigService
    JobDaoService jobDaoService

    /**
     * Time to wait during Job processing before each check if results are available
     */
    private int pollDelaySeconds = 30
    /**
     * Time to wait after maxDownloadTime of a job has been exceeded before declaring
     * a timeout error
     */
    private final int declareTimeoutAfterMaxDownloadTimePlusSeconds = 60



    /**
     * Builds an ID string from the ID of the given Job and the supplied testId.
     * This ID is used to identify those Quartz triggers that are created when a
     * Job is started and fire every pollDelaySeconds seconds to poll for a result.
     */
    private String getSubtriggerId(Job job, String testId) {
        return String.format('%d:%s', job.id, testId)
    }


    void scheduleJobRunPolling(Job job, String testId) {
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

    void unscheduleTest(Job job, String testId) {
        JobProcessingQuartzHandlerJob.removePollingLock(job, testId)
        JobProcessingQuartzHandlerJob.unschedule(getSubtriggerId(job, testId), TriggerGroup.JOB_TRIGGER_POLL.value())
        JobProcessingQuartzHandlerJob.unschedule(getSubtriggerId(job, testId), TriggerGroup.JOB_TRIGGER_TIMEOUT.value())
    }

    /**
     * Schedules a Quartz trigger to launch the given Job at the time(s) determined by its execution schedule Cron expression.
     */
    @NotTransactional
    void scheduleJob(Job job, boolean rescheduleIfAlreadyScheduled = true) {
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

    void rescheduleJobRunPolling(JobResult jobResult) {
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
}

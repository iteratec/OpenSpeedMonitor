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

package de.iteratec.osm.measurement.schedule.quartzjobs

import de.iteratec.osm.measurement.schedule.Job
import de.iteratec.osm.measurement.schedule.JobProcessingService
import de.iteratec.osm.measurement.schedule.TriggerGroup
import de.iteratec.osm.util.PerformanceLoggingService
import org.quartz.JobExecutionContext

import java.util.concurrent.locks.ReentrantLock

import static de.iteratec.osm.util.PerformanceLoggingService.LogLevel.DEBUG
import static de.iteratec.osm.util.Constants.*

/**
 * This class doesn't represent one static quartz job like the other job classes under grails-app/jobs.
 * It provides the entrypoint for all the dynamically scheduled and unscheduled quartz triggers (see {@link JobProcessingService}).
 */
class JobProcessingQuartzHandlerJob {

	JobProcessingService jobProcessingService
    PerformanceLoggingService performanceLoggingService

    static triggers = {}
    /**
     * This map contains a {@link ReentrantLock} for each measurement job run to poll results for.
     * As key a combination of {@link Job} and {@link de.iteratec.osm.result.JobResult#testId} is used.
     */
    static private pollingLocks = [:]

    /**
     * Entrypoint for all the dynamically scheduled and unscheduled quartz triggers
     * @param context
     *          Injected by the plugin.
     */
    def execute(JobExecutionContext context) {

        Job.withNewSession {

            Long jobId = context.mergedJobDataMap.getLong("jobId")
            Job job = Job.get(jobId)

            if (!job) {
                throw new IllegalStateException(
                        "JobProcessingQuartzHandler: No job found in execution of quartz job with id ${context.getTrigger().getKey().getName()}")
            } else {

                handleQuartzExecution(context, job)

            }
        }

    }

    private void handleQuartzExecution(JobExecutionContext context, Job job){

        String triggerGroup = context.getTrigger().getKey().getGroup();
        String testId = context.mergedJobDataMap.getString("testId")

        switch (triggerGroup){
            case (TriggerGroup.JOB_TRIGGER_LAUNCH.value()):
                handleLaunching(job)
                break
            case (TriggerGroup.JOB_TRIGGER_POLL.value()):
                handlePolling(job, testId)
                break
            case (TriggerGroup.JOB_TRIGGER_TIMEOUT.value()):
                handleTimeout(job, testId)
                break
        }

    }

    private void handleLaunching(Job job) {
        performanceLoggingService.logExecutionTime(DEBUG, "JobProcessingQuartzHandler: Launching job ${job.label}", PerformanceLoggingService.IndentationDepth.ONE) {
            jobProcessingService.launchJobRun(job)
        }
    }

    private void handlePolling(Job job, String testId){
        String lockKey = getLockKeyFor(job, testId)
        if (!pollingLocks[lockKey]){
            pollingLocks[lockKey] = new ReentrantLock();
        }
        if (pollingLocks[lockKey].tryLock()) {
            try{
                performanceLoggingService.logExecutionTime(DEBUG, "JobProcessingQuartzHandler: Polling of job ${job.label}", PerformanceLoggingService.IndentationDepth.ONE) {
                    jobProcessingService.pollJobRun(job, testId)
                }
            }finally{
                pollingLocks[lockKey].unlock()
            }
        }
    }

    private void handleTimeout(Job job, String testId) {
        performanceLoggingService.logExecutionTime(DEBUG, "JobProcessingQuartzHandler: Handle Job run timeout for job ${job.label}", PerformanceLoggingService.IndentationDepth.ONE) {
            removePollingLock(job, testId)
            jobProcessingService.handleJobRunTimeout(job, testId)
        }
    }

    /**
     * Removes the {@link ReentrantLock} of the polling for the results of one measurement job run.
     * @param job
     *          The measurement {@link Job} of the job run to remove the polling lock from.
     * @param testId
     *          The {@link de.iteratec.osm.result.JobResult#testId} of the job run to remove the polling lock from.
     */
    static void removePollingLock(Job job, String testId){
        String lockKey = getLockKeyFor(job, testId)
        pollingLocks.remove(lockKey)
    }

    private static String getLockKeyFor(Job job, String testId) {
        return "${job.ident()}${UNIQUE_STRING_DELIMITTER}${testId}"
    }
}
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

import static de.iteratec.osm.util.PerformanceLoggingService.LogLevel.DEBUG

import de.iteratec.osm.util.PerformanceLoggingService
import org.quartz.JobExecutionContext;
import de.iteratec.osm.measurement.schedule.Job
import de.iteratec.osm.measurement.schedule.TriggerGroup
import de.iteratec.osm.measurement.schedule.JobProcessingService

class CronDispatcherQuartzJob {
	JobProcessingService jobProcessingService
    PerformanceLoggingService performanceLoggingService

    static triggers = {}

    def execute(JobExecutionContext context) {
		String triggerGroup = context.getTrigger().getKey().getGroup();
		Long jobId = context.mergedJobDataMap.getLong("jobId")
		String testId = context.mergedJobDataMap.getString("testId")
		String id = context.getTrigger().getKey().getName()
		Job job = Job.get(jobId)
		if (!job) {
			println "CronDispatcherJob: No job found with id $id"
		} else {
			if (triggerGroup == TriggerGroup.QUARTZ_TRIGGER_GROUP.value()) {
                performanceLoggingService.logExecutionTime(DEBUG, "CronDispatcherJob: Launching job ${job.label}", PerformanceLoggingService.IndentationDepth.ONE){
                    jobProcessingService.launchJobRun(job)
                }
			} else if (triggerGroup == TriggerGroup.QUARTZ_SUBTRIGGER_GROUP.value()) {
                performanceLoggingService.logExecutionTime(DEBUG, "CronDispatcherJob: Polling of job ${job.label}", PerformanceLoggingService.IndentationDepth.ONE){
                    jobProcessingService.pollJobRun(job, testId)
                }
			} else if (triggerGroup == TriggerGroup.QUARTZ_TIMEOUTTRIGGER_GROUP.value()) {
                performanceLoggingService.logExecutionTime(DEBUG, "CronDispatcherJob: Handle Job run timeout for job ${job.label}", PerformanceLoggingService.IndentationDepth.ONE){
                    jobProcessingService.handleJobRunTimeout(job, testId)
                }
			}
		} 
    }
}
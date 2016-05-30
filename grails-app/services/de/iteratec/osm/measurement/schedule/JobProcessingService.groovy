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
import de.iteratec.osm.measurement.environment.wptserverproxy.ProxyService
import de.iteratec.osm.measurement.schedule.quartzjobs.CronDispatcherQuartzJob
import de.iteratec.osm.result.JobResult
import de.iteratec.osm.util.PerformanceLoggingService
import grails.transaction.NotTransactional
import grails.transaction.Transactional
import groovy.time.TimeCategory
import groovy.util.slurpersupport.GPathResult
import groovyx.net.http.HttpResponseDecorator
import org.apache.commons.lang.exception.ExceptionUtils
import org.quartz.*
import org.hibernate.StaleObjectStateException
import org.springframework.transaction.annotation.Propagation

import static de.iteratec.osm.util.PerformanceLoggingService.LogLevel.DEBUG

class JobExecutionException extends Exception{
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
	QUARTZ_TRIGGER_GROUP('OSMJobCycles'), QUARTZ_SUBTRIGGER_GROUP('OSMJobCyclePolls'), QUARTZ_TIMEOUTTRIGGER_GROUP('OSMTimeoutTriggers')

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

	ProxyService proxyService
	def quartzScheduler
	ConfigService configService
	InMemoryConfigService inMemoryConfigService
    PerformanceLoggingService performanceLoggingService

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
	private def fillRequestParameters(Job job) {
		def parameters = [
			url: '',
			label: job.label,
			location: job.location.uniqueIdentifierForServer,
			runs: job.runs,
			fvonly: job.firstViewOnly,
			video: job.captureVideo,
			web10: job.web10,
			noscript: job.noscript,
			clearcerts: job.clearcerts,
			ignoreSSL: job.ignoreSSL,
			standards: job.standards,
			tcpdump: job.tcpdump,
			bodies: job.bodies,
			continuousVideo: job.continuousVideo,
			keepua: job.keepua
			]

		// specify connectivity
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
	private JobResult persistUnfinishedJobResult(Job job, String testId, int statusCode, String wptStatus = null) {
		// If no testId was provided some error occurred and needs to be logged
		JobResult result
		if (testId) {
			result = JobResult.findByJobConfigLabelAndTestId(job.label, testId)
		}

		if (!result) {

            return persistNewUnfinishedJobResult(job, testId, statusCode, wptStatus)

		}else{

            updateStatusAndPersist(result, job, testId, statusCode, wptStatus)
            return result
        }

	}
    private void updateStatusAndPersist(JobResult result, Job job, String testId, int statusCode, String wptStatus){
        log.debug("Updating status of existing JobResult: Job ${job.label}, test-id=${testId}")
        if (result.httpStatusCode != statusCode || (wptStatus != null && result.wptStatus != wptStatus)){

            updateStatus(result, statusCode, wptStatus)

            try{
                result.save(failOnError: true, flush: true)

            }catch(StaleObjectStateException staleObjectStateException){
                String logMessage = "Updating status of existing JobResult: Job ${job.label}, test-id=${testId}" +
                        "\n\t-> httpStatusCode of result couldn't get updated from ${result.httpStatusCode}->${statusCode}"
                if (wptStatus != null) logMessage += "\n\twptStatus of result couldn't get updated from ${result.wptStatus}->${wptStatus}"
                log.error(logMessage, staleObjectStateException)
            }

        }
    }

    private void updateStatus(JobResult result, int statusCode, String wptStatus) {
        log.debug("Updating status of existing JobResult: httpStatusCode: ${result.httpStatusCode}->${statusCode}")
        result.httpStatusCode = statusCode
        if (wptStatus != null) {
            log.debug("Updating status of existing JobResult: wptStatus: ${result.wptStatus}->${wptStatus}")
            result.wptStatus = wptStatus
        }
    }

    private JobResult persistNewUnfinishedJobResult(Job job, String testId, int statusCode, String wptStatus) {
        JobResult result = new JobResult(
                job: job,
                date: new Date(),
                testId: testId ?: UUID.randomUUID() as String,
                har: null,
                description: '',
                jobConfigLabel: job.label,
                jobConfigRuns: job.runs,
                wptServerLabel: job.location.wptServer.label,
                wptServerBaseurl: job.location.wptServer.baseUrl,
                locationLabel: job.location.label,
                locationLocation: job.location.location,
                locationBrowser: job.location.browser.name,
                locationUniqueIdentifierForServer: job.location.uniqueIdentifierForServer,
                jobGroupName: job.jobGroup.name,
                httpStatusCode: statusCode)
        if (wptStatus != null) result.wptStatus = wptStatus
        log.debug("Persisting of unfinished result: Job ${job.label}, test-id=${testId} -> persisting new JobResult=${result}")
        return result.save(failOnError: true, flush: true)
    }

    private Map parseXmlResponse(String xml, WebPageTestServer wptserver) {
		GPathResult response = new XmlSlurper().parseText(xml)
		Map params = [:]
		params.statusCode = response.statusCode.toInteger()
		if (params.statusCode == 400) {
			throw new JobExecutionException(response.statusText.toString() + " (reported by ${wptserver})");
		}
		params.testId = response.data.testId?.text()
		if (!params.testId) {
			throw new JobExecutionException("No testId was provided")
		}
		params.userUrl = response.data.userUrl.text()

		return params
	}

	/**
	 * Build the trigger designed to poll WPTServer for new results every pollDelaySeconds (dubbed subtrigger)
	 * until endDate.
	 * @param endDate Poll until endDate or indefinitely, if endDate is null
	 */
	private Trigger buildSubTrigger(Job job, String testId, Date endDate) {
		TriggerBuilder subTrigger = TriggerBuilder.newTrigger()
			.withIdentity(getSubtriggerId(job, testId), TriggerGroup.QUARTZ_SUBTRIGGER_GROUP.value())
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
			.withIdentity(getSubtriggerId(job, testId), TriggerGroup.QUARTZ_TIMEOUTTRIGGER_GROUP.value())
			.withSchedule(SimpleScheduleBuilder.simpleSchedule()
				// timeout handling is executed immediately after the scheduler discovers a misfire situation
				.withMisfireHandlingInstructionFireNow())
			.startAt(timeoutDate)
		return timeoutTrigger.build()
	}

	/**
	 * If measurements are generally enabled: Launches the given Job and creates a Quartz trigger to poll for new results every x seconds.
	 * If they are not enabled nothing happens.
	 * @return whether the job was launched successfully
	 */
	public boolean launchJobRun(Job job) {

		if ( ! inMemoryConfigService.areMeasurementsGenerallyEnabled() ) {
			log.info("Job run of Job ${job} is skipped cause measurements are generally disabled.")
			return false
		}

		Map params = [ testId: '' ]
		int statusCode
		try {
			def parameters = fillRequestParameters(job);
			parameters.f = 'xml';

			WebPageTestServer wptserver = job.location.wptServer
			if (!wptserver) {
				throw new JobExecutionException("Missing wptServer in job ${job.label}");
			}

            HttpResponseDecorator result
            performanceLoggingService.logExecutionTime(DEBUG, "Launching job ${job.label}: Calling initial runtest on wptserver.", PerformanceLoggingService.IndentationDepth.TWO){
                result = proxyService.runtest(wptserver, parameters);
            }
			statusCode = result.getStatus()
			if (statusCode != 200) {
				throw new JobExecutionException("ProxyService.runtest() returned status code ${statusCode}");
			}
			log.info("Jobrun successfully launched: wptserver=${wptserver}, sent params=${parameters}")

			params = parseXmlResponse(result.data.text, wptserver)

			use (TimeCategory) {
				Map jobDataMap = [jobId: job.id, testId: params.testId]
				Date endDate = new Date() + job.maxDownloadTimeInMinutes.minutes
				// build and schedule subtrigger for polling every pollDelaySeconds seconds.
				// Polling is stopped by pollJobRun() when the running test is finished or on endDate at the latest
                log.debug("Building subtrigger for polling for job ${job.label} and test-id ${params.testId} (enddate ${endDate})")
				CronDispatcherQuartzJob.schedule(buildSubTrigger(job, params.testId, endDate), jobDataMap)
				// schedule a timeout trigger which will fire once after endDate + a delay of
				// declareTimeoutAfterMaxDownloadTimePlusSeconds seconds and perform cleaning operations.
				// This will also set this job run's status to 504 Timeout.
				Date timeoutDate = endDate + declareTimeoutAfterMaxDownloadTimePlusSeconds.seconds
                log.debug("Building timeout trigger for job ${job.label} and test-id ${params.testId} (timeout date=${timeoutDate}, jobDataMap=${jobDataMap})")
				CronDispatcherQuartzJob.schedule(buildTimeoutTrigger(job, params.testId, timeoutDate), jobDataMap)
			}

			return true
		} catch (Exception e) {
            log.error("An error occurred while launching job ${job.label}. Unfinished JobResult with error code will get persisted now: ${ExceptionUtils.getFullStackTrace(e)}")
			persistUnfinishedJobResult(job, params.testId, statusCode < 400 ? 400 : statusCode, e.getMessage())
			return false
		}
	}

	/**
	 * Launch Job interactively
	 *
	 * @return If successful, a URL to the WPT Server Results Page is returned
	 * @throws JobExecutionException If the job could not be submitted successfully, a JobExecutionException is thrown
	 * 	 and its htmlResult contains the HTML response from the WPT Server indicating why test execution failed.
	 */
	public String launchJobRunInteractive(Job job) throws JobExecutionException {
		def parameters = fillRequestParameters(job);
		WebPageTestServer wptserver = job.location.wptServer
		HttpResponseDecorator result = proxyService.runtest(wptserver, parameters);

		if (result.getStatus() == 302) {
			return result.getHeaders().getAt('Location').getValue()
		} else {
			throw new JobExecutionException("ProxyService.runtest() returned statusCode ${result.getStatus()}", result.data.text)
		}
	}

	/**
	 * Updates the status of a currently running job.
	 * If the Job terminated (successfully or unsuccessfully) the Quartz trigger calling pollJobRun()
	 * every pollDelaySeconds seconds is removed
	 */
	public int pollJobRun(Job job, String testId, String wptStatus = null) {
		int statusCode
		try
		{
            performanceLoggingService.logExecutionTime(DEBUG, "Polling jobrun ${testId} of job ${job.label}: fetching results from wptrserver.", PerformanceLoggingService.IndentationDepth.TWO){
                statusCode = proxyService.fetchResult(job.location.wptServer, [resultId: testId])
            }
            performanceLoggingService.logExecutionTime(DEBUG, "Polling jobrun ${testId} of job ${job.label}: updating jobresult.", PerformanceLoggingService.IndentationDepth.TWO){
                if (statusCode < 200){
                    persistUnfinishedJobResult(job, testId, statusCode, wptStatus)
                }
            }
		} catch (Exception e) {
			statusCode = 400
            log.error("Polling jobrun ${testId} of job ${job.label}: An unexpected exception occured. Error gets persisted as unfinished JobResult now", e)
			persistUnfinishedJobResult(job, testId, statusCode, e.getMessage())
		} finally {
			if (statusCode >= 200) {
				CronDispatcherQuartzJob.unschedule(getSubtriggerId(job, testId), TriggerGroup.QUARTZ_SUBTRIGGER_GROUP.value())
				CronDispatcherQuartzJob.unschedule(getSubtriggerId(job, testId), TriggerGroup.QUARTZ_TIMEOUTTRIGGER_GROUP.value())
			}
		}
		return statusCode
	}

	/**
	 * Checks if this job instance is still unfinished. If so, the corresponding WPT Server is polled one last time
	 * and if job is still reported as running, it's temporary JobResult is set to httpStatusCode 504 (timeout)
	 * in the database. In addition, a request to cancelTest.php is made.
	 */
	public void handleJobRunTimeout(Job job, String testId) {
		// Is there a non-running result? Then done
		JobResult result = JobResult.findByJobConfigLabelAndTestId(job.label, testId)
		if (!result)
			return
		if (result.httpStatusCode < 200 && pollJobRun(job, testId) < 200) {
			persistUnfinishedJobResult(job, testId, 504, '')
			proxyService.cancelTest(job.location.wptServer, [test: testId])
		}
	}

	/**
	 * Cancel a running Job. If the job is pending and has not been executed yet, the WPT server will
	 * also terminate it. Otherwise it will be left running on the server but polling is stopped.
	 */
	public void cancelJobRun(Job job, String testId) {
//		if (quartzScheduler.getTrigger(new TriggerKey(getSubtriggerId(job, testId)), TriggerGroup.QUARTZ_SUBTRIGGER_GROUP.value()))
		log.info("unschedule quartz triggers for job run: job=${job.label},test id=${testId}")
		CronDispatcherQuartzJob.unschedule(getSubtriggerId(job, testId), TriggerGroup.QUARTZ_SUBTRIGGER_GROUP.value())
		CronDispatcherQuartzJob.unschedule(getSubtriggerId(job, testId), TriggerGroup.QUARTZ_TIMEOUTTRIGGER_GROUP.value())
		log.info("unschedule quartz triggers for job run: job=${job.label},test id=${testId} ... DONE")
		JobResult result = JobResult.findByJobConfigLabelAndTestIdAndHttpStatusCodeLessThan(job.label, testId, 200)
		if (result) {
			log.info("Deleting the following JobResult as requested: ${result}.")
			result.delete(failOnError: true)
			log.info("Deleting the following JobResult as requested: ${result}... DONE")
			log.info("Canceling respective test on wptserver.")
			proxyService.cancelTest(job.location.wptServer, [test: testId])
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
				.withIdentity(job.id.toString(), TriggerGroup.QUARTZ_TRIGGER_GROUP.value())
				.withSchedule(
					CronScheduleBuilder.cronSchedule(job.executionSchedule)
					// Immediately executes first misfired execution and discards other (i.e. all misfired executions are merged together).
					// Then back to schedule. No matter how many trigger executions were missed, only single immediate execution is performed.
					.withMisfireHandlingInstructionFireAndProceed()
				)
		Trigger cronTrigger = builder.build()
		// job is already scheduled:
		if (quartzScheduler.getTrigger(new TriggerKey(job.id.toString(), TriggerGroup.QUARTZ_TRIGGER_GROUP.value()))) {
			if (rescheduleIfAlreadyScheduled) {
				if (log.infoEnabled) log.info("Rescheduling Job ${job.label} (${job.executionSchedule})")
				CronDispatcherQuartzJob.reschedule(cronTrigger, [jobId: job.id])
			} else {
				log.info("Ignoring Job ${job.label} as it is already scheduled.")
			}
		} else {
			try {
				log.info("Scheduling Job ${job.label} (${job.executionSchedule})")
				CronDispatcherQuartzJob.schedule(cronTrigger, [jobId: job.id])
			} catch (SchedulerException se) {
				log.info("Job ${job.label} with schedule ${job.executionSchedule} can't be scheduled: ${se.message}", se)
			}
		}
	}
	@NotTransactional
	public void scheduleAllActiveJobs() {
		if (log.infoEnabled) log.info("Launching all active jobs")
		Job.findAll { active == true }.each { scheduleJob(it, false) }
	}

	/**
	 * Removes the Quartz trigger for the specified Job
	 */
	@NotTransactional
	public void unscheduleJob(Job job) {
		if (log.infoEnabled) log.info("Unscheduling Job ${job.label}")
		CronDispatcherQuartzJob.unschedule(job.id.toString(), TriggerGroup.QUARTZ_TRIGGER_GROUP.value())
	}
}
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

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertNotNull
import static org.junit.Assert.assertNull

import grails.test.mixin.TestMixin
import grails.test.mixin.integration.IntegrationTestMixin
import groovyx.net.http.HttpResponseDecorator

import org.apache.http.HttpVersion
import org.apache.http.message.BasicHttpResponse
import org.junit.After
import org.junit.Before
import org.quartz.Trigger
import org.quartz.TriggerKey
import org.quartz.impl.triggers.CronTriggerImpl
import de.iteratec.osm.csi.IntTestWithDBCleanup
import de.iteratec.osm.csi.TestDataUtil;
import de.iteratec.osm.measurement.environment.wptserverproxy.ProxyService
import de.iteratec.osm.result.JobResult
import de.iteratec.osm.measurement.script.Script
import de.iteratec.osm.measurement.environment.Browser
import de.iteratec.osm.measurement.environment.Location
import de.iteratec.osm.measurement.environment.QueueAndJobStatusService
import de.iteratec.osm.measurement.environment.WebPageTestServer

/**
 * Integration test for JobProcessingService
 * 
 * @author dri
 */
@TestMixin(IntegrationTestMixin)
class JobProcessingServiceSpec extends IntTestWithDBCleanup{
	JobProcessingService jobProcessingService
	QueueAndJobStatusService queueAndJobStatusService

	final static String UNNAMED_JOB_LABEL = 'Unnamed Job'
	/** 
	 * Cron strings designed for Quartz jobs to never be executed before integration test ends
	 */
	private final static String CRON_STRING_1 = '* * */12 * * ?'
	private final static String CRON_STRING_2 = '* * */13 * * ?'
	private final static String EVERY_15_SECONDS = '*/15 * * * * ? *'

	Script script
	Location location
	JobGroup jobGroup

	@Before
	void setUp() {

		// mocks common for all tests
		
		jobProcessingService.proxyService = [ runtest: { WebPageTestServer wptserver, Map params ->
			if (params.f == 'xml') {
				String xml = """\
<?xml version="1.0" encoding="UTF-8"?>
<response>
	<statusCode>200</statusCode>
	<statusText>Ok</statusText>
	<data>
		<testId>${HttpRequestServiceMock.testId}</testId>
		<ownerKey>f942624563a31387d20025513d50a350b37a17f1</ownerKey>
		<xmlUrl>http://dev.server01.wpt.iteratec.de/xmlResult/${HttpRequestServiceMock.testId}/</xmlUrl>
		<userUrl>${HttpRequestServiceMock.redirectUserUrl}</userUrl>
		<summaryCSV>http://dev.server01.wpt.iteratec.de/result/${HttpRequestServiceMock.testId}/page_data.csv</summaryCSV>
		<detailCSV>http://dev.server01.wpt.iteratec.de/result/${HttpRequestServiceMock.testId}/requests.csv</detailCSV>
		<jsonUrl>http://dev.server01.wpt.iteratec.de/jsonResult.php?test=${HttpRequestServiceMock.testId}/</jsonUrl>
	</data>
</response>
			"""
				BasicHttpResponse httpResponse = new BasicHttpResponse(HttpVersion.HTTP_1_1, 200, null)
				return new HttpResponseDecorator(httpResponse, [text: xml] as Object)
			} else {
				BasicHttpResponse httpResponse = new BasicHttpResponse(HttpVersion.HTTP_1_1, 302, null)
				HttpResponseDecorator d = new HttpResponseDecorator(httpResponse, null)
				d.addHeader('Location', HttpRequestServiceMock.redirectUserUrl)
				return d
			}
		} ] as ProxyService
		jobProcessingService.proxyService.httpRequestService = new HttpRequestServiceMock()
		
		//test data common for all tests


		jobProcessingService.inMemoryConfigService = new InMemoryConfigService()
		jobProcessingService.inMemoryConfigService.activateMeasurementsGenerally()
		TestDataUtil.createOsmConfig()

		WebPageTestServer wptServer = new WebPageTestServer(
				label: 'Unnamed server',
				proxyIdentifier: 'proxy_identifier',
				dateCreated: new Date(),
				lastUpdated: new Date(),
				active: true,
				baseUrl: 'http://example.com').save(failOnError: true)
		Browser browser = new Browser(
				name: 'browser',
				weight: 1.0).save(failOnError: true)
		jobGroup = new JobGroup(
				name: 'Unnamed group',
				groupType: JobGroupType.CSI_AGGREGATION,
				graphiteServers: []).save(failOnError: true)

		script = Script.createDefaultScript('Unnamed job').save(failOnError: true)
		location = new Location(
				label: 'Unnamed location',
				dateCreated: new Date(),
				active: true,
				valid: 1,
				wptServer: wptServer,
				location: 'location',
				browser: browser
				).save(failOnError: true)
	}

	@After
	void tearDown() {
		JobResult.list()*.delete(flush: true)
	}

	void testScheduleJob() {
		// test execution
		Job wptJobToSchedule = createAndScheduleJob(CRON_STRING_1)
		TriggerKey triggerKey = getTriggerKeyOf(wptJobToSchedule)

		//assertions

		// check if Job was scheduled with correct Trigger identifier and group
		Trigger insertedTrigger = jobProcessingService.quartzScheduler.getTrigger(triggerKey)
		assertNotNull(insertedTrigger)
		assertEquals(triggerKey, insertedTrigger.getKey())
		// check if schedule of inserted Trigger matches Cron expression of wptJobToSchedule
		assertEquals(wptJobToSchedule.executionSchedule, getCronExpressionByTriggerKey(triggerKey, wptJobToSchedule))
	}

	void testUnscheduleJob() {
		Job wptJobToSchedule = createAndScheduleJob(CRON_STRING_1)

		// test execution
		jobProcessingService.unscheduleJob(wptJobToSchedule)

		// assertions
		assertNull(jobProcessingService.quartzScheduler.getTrigger(getTriggerKeyOf(wptJobToSchedule)))
	}

	void testRescheduleJob() {
		Job wptJobToSchedule = createAndScheduleJob(CRON_STRING_1)
		wptJobToSchedule.executionSchedule = CRON_STRING_2

		// test execution
		// reschedules Job if already scheduled
		jobProcessingService.scheduleJob(wptJobToSchedule)

		// assertions
		// check if schedule matches updated Cron expression of wptJobToSchedule
		assertEquals(wptJobToSchedule.executionSchedule, getCronExpressionByTriggerKey(getTriggerKeyOf(wptJobToSchedule), wptJobToSchedule))
	}

	void testScheduleAllJobs() {
		// test-specific data
		Job inactiveJob = createJob(false)
		Job activeJob1 = createJob(true, CRON_STRING_1)
		Job activeJob2 = createJob(true, CRON_STRING_2)

		// test execution
		jobProcessingService.scheduleAllActiveJobs();

		// assertions
		assertNull(jobProcessingService.quartzScheduler.getTrigger(getTriggerKeyOf(inactiveJob)))
		assertNotNull(jobProcessingService.quartzScheduler.getTrigger(getTriggerKeyOf(activeJob1)))
		assertNotNull(jobProcessingService.quartzScheduler.getTrigger(getTriggerKeyOf(activeJob2)))

		assertEquals(activeJob1.executionSchedule, getCronExpressionByTriggerKey(getTriggerKeyOf(activeJob1), activeJob1))
		assertEquals(activeJob2.executionSchedule, getCronExpressionByTriggerKey(getTriggerKeyOf(activeJob2), activeJob2))
	}

	/**
	 * Tests the following methods of JobProcessingService:
	 * 	getCurrentlyRunningJobs
	 * 	pollJobRun
	 * 	launchJobRun
	 */
	void testLaunchJobAndPoll() {
		//test specific data /////////////////////////////////////////////////////////////
		Job job = createJob(true, EVERY_15_SECONDS)
		
		//test execution /////////////////////////////////////////////////////////////
		//launchJobRun returns false, because it fails and catch the exception
		jobProcessingService.launchJobRun(job)
		// manual first execution cause quartz scheduling doesn't seem to work trustable in tests
		jobProcessingService.pollJobRun(job, HttpRequestServiceMock.testId)
		
		//assertions /////////////////////////////////////////////////////////////
		
		// ensure launchJobRun created a Quartz trigger (called subtrigger) to repeatedly execute JobProcessingService.pollJubRun()
		TriggerKey subtriggerKey = new TriggerKey(jobProcessingService.getSubtriggerId(job, HttpRequestServiceMock.testId), TriggerGroup.QUARTZ_SUBTRIGGER_GROUP.value())

		// no Job in  JobStore, because no Triger was created --> returns null
		Trigger subtrigger = jobProcessingService.quartzScheduler.getTrigger(subtriggerKey)
		assertNotNull(subtrigger)

		for (int i = 0; i < HttpRequestServiceMock.statusCodes.length; i++) {
			// assert that an unfinished JobResult with correct status code has been persisted
			if (HttpRequestServiceMock.statusCodes[i] < 200) {
				List<JobResult> jobResults = JobResult.list()
				JobResult unfinishedResult = JobResult.findByJobConfigLabelAndTestId(job.label, HttpRequestServiceMock.testId)
				assertNotNull(unfinishedResult)
				assertEquals(HttpRequestServiceMock.statusCodes[i], unfinishedResult.httpStatusCode)
			}
			jobProcessingService.pollJobRun(job, HttpRequestServiceMock.testId)
		}

		// ensure that upon successful completion of the test (statusCode 200) pollJobRun() removed the subtrigger
		assertNull(jobProcessingService.quartzScheduler.getTrigger(subtriggerKey))
	}

	void testLaunchJobInteractive() {
		// test execution
		Job job = createJob(false)
		String redirectUrl = jobProcessingService.launchJobRunInteractive(job)
		
		// assertions
		assertNotNull(redirectUrl)
		assertEquals(HttpRequestServiceMock.redirectUserUrl, redirectUrl)
	}
	
	/**
	 * This test creates several JobResults with different status codes and checks whether
	 * JobProcessingService.getRunningAndRecentlyFinishedJobs() filters these results correctly.
	 * Only results that are no errors or the most recent one should be retained.
	 */
	
	void testStatusOfRepeatedJobExecution() {
		Map testData = [
			[100]: [100],
			[200]: [200],
			[400]: [400],
			[400, 100]: [100],
			[100, 400]: [100, 400],
			[200, 400]: [200, 400],
			[400, 200]: [200],
			[100, 200]: [100, 200],
			[200, 100]: [200, 100],
			[100, 200, 400]: [100, 200, 400],
			[100, 400, 200]: [100, 200],
			[200, 100, 400]: [200, 100, 400],
			[200, 400, 100]: [200, 100],
			[400, 100, 200]: [100, 200],
			[400, 200, 100]: [200, 100]]
		
		// test setup
		Job job = createJob(false)
		Date now = new Date()
		Date oldestDate = now - 5
		
		testData.each { List inputStatusCodes, List expectedStatusCodes ->
			// test setup
			inputStatusCodes.reverse().eachWithIndex { int httpStatusCode, int i ->
				JobResult result = jobProcessingService.persistUnfinishedJobResult(job, null, httpStatusCode)
				result.date = now - i
				result.save(flush: true, failOnError: true)
			}
			// test execution
			List recentRuns = queueAndJobStatusService.getRunningAndRecentlyFinishedJobs(oldestDate, oldestDate, oldestDate)[job.id]

			// assertions
			assertEquals(inputStatusCodes.size(), JobResult.count())
			assertEquals(expectedStatusCodes.size(), recentRuns.size())
			expectedStatusCodes.eachWithIndex { int statusCode, int i ->
				assertEquals(statusCode, recentRuns[i]['status'])
			}
			
			// cleanup
			JobResult.list()*.delete(flush: true, failOnError: true)
		}
	}


	private Job createJob(boolean active, String executionSchedule = null){
		Job wptJobToSchedule = new Job(
				label: UNNAMED_JOB_LABEL + ' ' + UUID.randomUUID() as String,
				description: '',
				executionSchedule: executionSchedule,
				runs: 1,
				active: active,
				script: script,
				location: location,
				jobGroup: jobGroup,
				maxDownloadTimeInMinutes: 60
				).save(failOnError: true)

		return wptJobToSchedule
	}

	private Job createAndScheduleJob(String executionSchedule) {
		Job wptJobToSchedule = createJob(true, executionSchedule)
		jobProcessingService.scheduleJob(wptJobToSchedule)
		return wptJobToSchedule
	}

	private String getCronExpressionByTriggerKey(TriggerKey triggerKey, Job wptJobToSchedule) {
		Trigger insertedTrigger = jobProcessingService.quartzScheduler.getTrigger(triggerKey)
		CronTriggerImpl cronScheduleBuilder = insertedTrigger.getScheduleBuilder().build()
		return cronScheduleBuilder.cronExpression
	}

	private TriggerKey getTriggerKeyOf(Job job) {
		return new TriggerKey(job.id.toString(), TriggerGroup.QUARTZ_TRIGGER_GROUP.value())
	}
}
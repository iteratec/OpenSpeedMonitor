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

package de.iteratec.osm.result

import grails.test.mixin.*
import groovy.util.slurpersupport.GPathResult

import org.apache.commons.lang.time.DateUtils
import org.junit.*

import de.iteratec.osm.measurement.schedule.Job
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.measurement.schedule.JobGroupType
import de.iteratec.osm.csi.Page
import de.iteratec.osm.measurement.script.Script
import de.iteratec.osm.measurement.environment.Browser
import de.iteratec.osm.measurement.environment.Location
import de.iteratec.osm.measurement.environment.WebPageTestServer

/**
 * Test-suite of {@link de.iteratec.osm.result.EventResultService}.
 */
@TestFor(EventResultService)
@Mock([Browser, Page, Job, Location, WebPageTestServer, JobResult, EventResult, MeasuredEvent, JobGroup, Script])
class EventResultServiceTests {

	EventResultService serviceUnderTest

	WebPageTestServer server1
	Browser ffBrowser, ieBrowser, i8eBrowser
	Location ffAgent1, ieAgent1, ieHetznerAgent1, ffHetznerAgent1, i8eHetznerAgent1
	Job job1, job2, job3, job4, job5, job6, job7, job8, job9
	JobResult runOfJob1_3HoursAgo, runOfJob1_2HoursAgo, runOfJob1_1HoursAgo, runOfJob1_Now, runOfJob2_Now
	EventResult resultOfJobRun1_3HoursAgo, resultOfJobRun1_2HoursAgo, resultOfJobRun1_1HoursAgo, resultOfJobRun1_Now, resultOfJobRun2_Now

	EventResult resultOfcurRun_Now

	List<Job> jobs;

	Date now
	Date threeHoursAgo
	Date twoHoursAgo
	Date oneHoursAgo
	
	JobGroup jobGroupUndefined

	@Before
	void setUp() {
		serviceUnderTest = service

		now = new Date()
		threeHoursAgo = DateUtils.addHours(now, -3)
		twoHoursAgo = DateUtils.addHours(now, -2)
		oneHoursAgo = DateUtils.addHours(now, -1)

		server1 = new WebPageTestServer(
				baseUrl : 'http://server1.wpt.server.de',
				active : true,
				label : 'server 1 - wpt server',
				proxyIdentifier : 'server 1 - wpt server'
				).save(failOnError: true)

		ffBrowser = new Browser(
				name:'FF',
				weight: 0.55).save(failOnError:true)

		ieBrowser = new Browser(
				name:'IE',
				weight: 0.25).save(failOnError:true)

		i8eBrowser = new Browser(
				name:'I8E',
				weight: 0.20).save(failOnError:true)

		ffAgent1 = new Location(
				active: true,
				valid: 1,
				location: 'physNetLabAgent01-FF',
				label: 'physNetLabAgent01 - FF up to date',
				browser: ffBrowser,
				wptServer: server1
				).save(failOnError: true)

		ieAgent1 = new Location(
				active: true,
				valid: 1,
				location: 'physNetLabAgent01-IE8',
				label: 'physNetLabAgent01 - IE 8',
				browser: ieBrowser,
				wptServer: server1
				).save(failOnError: true)

		ieHetznerAgent1 = new Location(
				active: true,
				valid: 1,
				location: 'hetznerAgent01-IE8',
				label: 'hetznerAgent01 - IE 8',
				browser: ieBrowser,
				wptServer: server1
				).save(failOnError: true)

		i8eHetznerAgent1 = new Location(
				active: true,
				valid: 1,
				location: 'hetznerAgent01-IE8',
				label: 'hetznerAgent01 - IE 8',
				browser: i8eBrowser,
				wptServer: server1
				).save(failOnError: true)

		ffHetznerAgent1 = new Location(
				active: true,
				valid: 1,
				location: 'hetznerAgent01-FF',
				label: 'hetznerAgent01 - FF up to date',
				browser: ffBrowser,
				wptServer: server1
				).save(failOnError: true)

		jobGroupUndefined = new JobGroup(name: 'undefined', groupType: JobGroupType.RAW_DATA_SELECTION).save(failOnError:true)
				
		Script script = Script.createDefaultScript('Unnamed').save(failOnError: true)
		
		job1 = new Job(
				active: false,
				label: 'BV1 - Step 01',
				description: 'This is job 01...',
				location: ffAgent1,
				frequencyInMin: 5,
				runs: 1,
				jobGroup: jobGroupUndefined,
				script: script,
				maxDownloadTimeInMinutes: 60,
                customConnectivityProfile: true,
                customConnectivityName: 'Custom (6.000/512 Kbps, 50ms)',
                bandwidthDown: 6000,
                bandwidthUp: 512,
                latency: 50,
                packetLoss: 0
				).save(failOnError: true)

		job2 = new Job(
				active: false,
				label: 'BV1 - Step 02',
				description: 'This is job 02...',
				location: ffAgent1,
				frequencyInMin: 5,
				runs: 1,
				script: script	,
				jobGroup: jobGroupUndefined,
				maxDownloadTimeInMinutes: 60,
                customConnectivityProfile: true,
                customConnectivityName: 'Custom (6.000/512 Kbps, 50ms)',
                bandwidthDown: 6000,
                bandwidthUp: 512,
                latency: 50,
                packetLoss: 0
				).save(failOnError: true)

		job3 = new Job(
				active: false,
				label: 'BV1 - Step 03',
				description: 'This is job 03...',
				location: ieAgent1,
				frequencyInMin: 5,
				runs: 1,
				jobGroup: jobGroupUndefined,
				script: script	,
				maxDownloadTimeInMinutes: 60,
                customConnectivityProfile: true,
                customConnectivityName: 'Custom (6.000/512 Kbps, 50ms)',
                bandwidthDown: 6000,
                bandwidthUp: 512,
                latency: 50,
                packetLoss: 0
				).save(failOnError: true)

		job4 = new Job(
				active: false,
				label: 'BV1 - Step 04',
				description: 'This is job 04...',
				location: ieAgent1,
				frequencyInMin: 5,
				runs: 1,
				jobGroup: jobGroupUndefined,
				script: script	,
				maxDownloadTimeInMinutes: 60,
                customConnectivityProfile: true,
                customConnectivityName: 'Custom (6.000/512 Kbps, 50ms)',
                bandwidthDown: 6000,
                bandwidthUp: 512,
                latency: 50,
                packetLoss: 0
				).save(failOnError: true)

		job5 = new Job(
				active: false,
				label: 'BV1 - Step 05',
				description: 'This is job 05...',
				location: ieAgent1,
				frequencyInMin: 5,
				runs: 1,
				jobGroup: jobGroupUndefined,
				script: script,
				maxDownloadTimeInMinutes: 60,
                customConnectivityProfile: true,
                customConnectivityName: 'Custom (6.000/512 Kbps, 50ms)',
                bandwidthDown: 6000,
                bandwidthUp: 512,
                latency: 50,
                packetLoss: 0
				).save(failOnError: true)

		job6 = new Job(
				active: false,
				label: 'BV1 - Step 06',
				description: 'This is job 06...',
				location: ffHetznerAgent1,
				frequencyInMin: 5,
				runs: 1,
				jobGroup: jobGroupUndefined,
				script: script	,
				maxDownloadTimeInMinutes: 60,
                customConnectivityProfile: true,
                customConnectivityName: 'Custom (6.000/512 Kbps, 50ms)',
                bandwidthDown: 6000,
                bandwidthUp: 512,
                latency: 50,
                packetLoss: 0
				).save(failOnError: true)

		job7 = new Job(
				active: false,
				label: 'BV1 - Step 07',
				description: 'This is job 07...',
				location: ieHetznerAgent1,
				frequencyInMin: 5,
				runs: 1,
				jobGroup: jobGroupUndefined,
				script: script	,
				maxDownloadTimeInMinutes: 60,
                customConnectivityProfile: true,
                customConnectivityName: 'Custom (6.000/512 Kbps, 50ms)',
                bandwidthDown: 6000,
                bandwidthUp: 512,
                latency: 50,
                packetLoss: 0
				).save(failOnError: true)

		job8 = new Job(
				active: false,
				label: 'BV1 - Step 08',
				description: 'This is job 08...',
				location: ieHetznerAgent1,
				frequencyInMin: 5,
				runs: 1,
				jobGroup: jobGroupUndefined,
				script: script	,
				maxDownloadTimeInMinutes: 60,
                customConnectivityProfile: true,
                customConnectivityName: 'Custom (6.000/512 Kbps, 50ms)',
                bandwidthDown: 6000,
                bandwidthUp: 512,
                latency: 50,
                packetLoss: 0
				).save(failOnError: true)

		job9 = new Job(
				active: false,
				label: 'BV1 - Step 09',
				description: 'This is job 09...',
				location: i8eHetznerAgent1,
				frequencyInMin: 5,
				runs: 1,
				jobGroup: jobGroupUndefined,
				script: script	,
				maxDownloadTimeInMinutes: 60,
                customConnectivityProfile: true,
                customConnectivityName: 'Custom (6.000/512 Kbps, 50ms)',
                bandwidthDown: 6000,
                bandwidthUp: 512,
                latency: 50,
                packetLoss: 0
				).save(failOnError: true)

		jobs = [
			job1,
			job2,
			job3,
			job4,
			job5,
			job6,
			job7,
			job8,
			job9
		]

		for (int i = 0; i < jobs.size(); i++) {
			int j = i + 1
			Job curJob = jobs[i]

			JobResult runOfCurJob_now = new JobResult(
					job: curJob,
					date: now,
					testId: '1',
					description: '',
					jobConfigLabel: curJob.label,
					jobConfigRuns: curJob.runs,
					frequencyInMin: 5,
					locationLocation: curJob.location.location,
					locationBrowser: curJob.location.browser.name,
					httpStatusCode : 200,
					jobGroupName: jobGroupUndefined.getName()
					).save(failOnError: true)

			JobResult runOfCurJob_1HoursAgo = new JobResult(
					job: curJob,
					date: oneHoursAgo,
					testId: '1',
					description: '',
					jobConfigLabel: curJob.label,
					jobConfigRuns: curJob.runs,
					frequencyInMin: 5,
					locationLocation: curJob.location.location,
					locationBrowser: curJob.location.browser.name,
					httpStatusCode : 200,
					jobGroupName: jobGroupUndefined.getName()
					).save(failOnError: true)

			JobResult runOfCurJob_2HoursAgo = new JobResult(
					job: curJob,
					date: twoHoursAgo,
					testId: '1',
					description: '',
					jobConfigLabel: curJob.label,
					jobConfigRuns: curJob.runs,
					frequencyInMin: 5,
					locationLocation: curJob.location.location,
					locationBrowser: curJob.location.browser.name,
					httpStatusCode : 200,
					jobGroupName: jobGroupUndefined.getName()
					).save(failOnError: true)

			JobResult runOfCurJob_3HoursAgo = new JobResult(
					job: curJob,
					date: threeHoursAgo,
					testId: '1',
					description: '',
					jobConfigLabel: curJob.label,
					jobConfigRuns: curJob.runs,
					frequencyInMin: 5,
					locationLocation: curJob.location.location,
					locationBrowser: curJob.location.browser.name,
					httpStatusCode : 200,
					jobGroupName: jobGroupUndefined.getName()
					).save(failOnError: true)

			resultOfcurRun_Now = new EventResult(
					numberOfWptRun: 1,
					cachedView: CachedView.UNCACHED,
					medianValue: true,
					docCompleteBytesIn: 1,
					docCompleteRequests: 1,
					docCompleteTimeInMillisecs: 1000,
					domTimeInMillisecs: 1,
					firstByteInMillisecs: 1,
					fullyLoadedBytesIn: 1,
					fullyLoadedRequests: 1,
					fullyLoadedTimeInMillisecs: 1,
					loadTimeInMillisecs: 1,
					startRenderInMillisecs: 1,
					downloadAttempts: 1,
					firstStatusUpdate: now,
					lastStatusUpdate: now,
					wptStatus: 0,
					validationState : 'validationState',
					harData: 'harData',
					customerSatisfactionInPercent: (100 - j * 1),
					jobResult: runOfCurJob_now,
					jobResultDate: runOfCurJob_now.date,
					jobResultJobConfigId: runOfCurJob_now.job.ident(),
					speedIndex: EventResult.SPEED_INDEX_DEFAULT_VALUE
					).save(failOnError: true)
			
			runOfCurJob_now.save(failOnError: true)

			EventResult resultOfcurRun_1HoursAgo = new EventResult(
					numberOfWptRun: 1,
					cachedView: CachedView.UNCACHED,
					medianValue: true,
					docCompleteBytesIn: 1,
					docCompleteRequests: 1,
					docCompleteTimeInMillisecs: 1000,
					domTimeInMillisecs: 1,
					firstByteInMillisecs: 1,
					fullyLoadedBytesIn: 1,
					fullyLoadedRequests: 1,
					fullyLoadedTimeInMillisecs: 1,
					loadTimeInMillisecs: 1,
					startRenderInMillisecs: 1,
					downloadAttempts: 1,
					firstStatusUpdate: now,
					lastStatusUpdate: now,
					wptStatus: 0,
					validationState : 'validationState',
					harData: 'harData',
					customerSatisfactionInPercent: (100 - j * 2),
					jobResult: runOfCurJob_1HoursAgo,
					jobResultDate: runOfCurJob_1HoursAgo.date,
					jobResultJobConfigId: runOfCurJob_1HoursAgo.job.ident(),
					speedIndex: EventResult.SPEED_INDEX_DEFAULT_VALUE
					).save(failOnError: true)
			
			runOfCurJob_1HoursAgo.save(failOnError: true)


			EventResult resultOfcurRun_2HoursAgo = new EventResult(
					numberOfWptRun: 1,
					cachedView: CachedView.UNCACHED,
					medianValue: true,
					docCompleteBytesIn: 1,
					docCompleteRequests: 1,
					docCompleteTimeInMillisecs: 1000,
					domTimeInMillisecs: 1,
					firstByteInMillisecs: 1,
					fullyLoadedBytesIn: 1,
					fullyLoadedRequests: 1,
					fullyLoadedTimeInMillisecs: 1,
					loadTimeInMillisecs: 1,
					startRenderInMillisecs: 1,
					downloadAttempts: 1,
					firstStatusUpdate: now,
					lastStatusUpdate: now,
					wptStatus: 0,
					validationState : 'validationState',
					harData: 'harData',
					customerSatisfactionInPercent: (100 - j * 3),
					jobResult: runOfCurJob_2HoursAgo,
					jobResultDate: runOfCurJob_2HoursAgo.date,
					jobResultJobConfigId: runOfCurJob_2HoursAgo.job.ident(),
					speedIndex: EventResult.SPEED_INDEX_DEFAULT_VALUE
					).save(failOnError: true)
			
			runOfCurJob_2HoursAgo.save(failOnError: true)

			EventResult resultOfcurRun_3HoursAgo = new EventResult(
					numberOfWptRun: 1,
					cachedView: CachedView.UNCACHED,
					medianValue: true,
					docCompleteBytesIn: 1,
					docCompleteRequests: 1,
					docCompleteTimeInMillisecs: 1000,
					domTimeInMillisecs: 1,
					firstByteInMillisecs: 1,
					fullyLoadedBytesIn: 1,
					fullyLoadedRequests: 1,
					fullyLoadedTimeInMillisecs: 1,
					loadTimeInMillisecs: 1,
					startRenderInMillisecs: 1,
					downloadAttempts: 1,
					firstStatusUpdate: now,
					lastStatusUpdate: now,
					wptStatus: 0,
					validationState : 'validationState',
					harData: 'harData',
					customerSatisfactionInPercent: (100 - j * 4),
					jobResult: runOfCurJob_3HoursAgo,
					jobResultDate: runOfCurJob_3HoursAgo.date,
					jobResultJobConfigId: runOfCurJob_3HoursAgo.job.ident(),
					speedIndex: EventResult.SPEED_INDEX_DEFAULT_VALUE
					).save(failOnError: true)
			
			runOfCurJob_3HoursAgo.save(failOnError: true)
		}
	}

	@Test
	void testFindByJobId() {
		List<EventResult> result = serviceUnderTest.findByJobId(job1.id)

		assertNotNull(result)
		assertEquals(4, result.size())
	}

	@Test
	void testFindSinceDate() {
		List<EventResult> result = serviceUnderTest.findSinceDate(now)
		assertEquals(0,  result.size())

		result = serviceUnderTest.findSinceDate(oneHoursAgo)
		assertEquals(jobs.size(), result.size())

		result = serviceUnderTest.findSinceDate(twoHoursAgo)
		int doubleJobsSize = 2 * jobs.size() // TODO DOC Why a the doubled size?
		assertEquals(doubleJobsSize, result.size())

		result = serviceUnderTest.findSinceDate(threeHoursAgo)
		int threeTimesJobsSize = 3 * jobs.size() // TODO DOC Why a the three times the job size?
		assertEquals(threeTimesJobsSize, result.size())
	}

	@Test
	void testFindBetweenDate() {
		List<EventResult> result = serviceUnderTest.findBetweenDate(oneHoursAgo, now)
		int doubleJobsSize = 2 * jobs.size() // TODO DOC Why a the doubled size?
		assertEquals(doubleJobsSize, result.size())

		result = serviceUnderTest.findBetweenDate(twoHoursAgo, now)
		int threeTimesJobsSize = 3 * jobs.size() // TODO DOC Why a the three times the job size?
		assertEquals(threeTimesJobsSize, result.size())

		result = serviceUnderTest.findBetweenDate(threeHoursAgo, now)
		int fourTimesJobsSize = 4 * jobs.size() // TODO DOC Why a the three times the job size?
		assertEquals(fourTimesJobsSize, result.size())
	}

	/**
	 * @deprecated Redefine test. This Test contains to much logic!
	 */
	@Deprecated
	@Test
	void testFindByAgentAndByBrowserSinceDate() {

		List<Date> tempList = [oneHoursAgo, twoHoursAgo]

		tempList.each { Date curTimestamp ->

			int factor = curTimestamp == oneHoursAgo ? 1 : 2 // Why logic here??? This is to complicated for a test!

			List<EventResult> result = serviceUnderTest.findByAgentAndByBrowserSinceDate(null, "FF", curTimestamp)
			assert result.size() == 3 * factor

			result = serviceUnderTest.findByAgentAndByBrowserSinceDate(null, "IE", curTimestamp)
			assert result.size() == 5 * factor

			result = serviceUnderTest.findByAgentAndByBrowserSinceDate("phy%", null, curTimestamp)
			assert result.size() == 5 * factor

			result = serviceUnderTest.findByAgentAndByBrowserSinceDate("het%", null, curTimestamp)
			assert result.size() == 4 * factor

			result = serviceUnderTest.findByAgentAndByBrowserSinceDate("%FF", null, curTimestamp)
			assert result.size() == 3 * factor

			result = serviceUnderTest.findByAgentAndByBrowserSinceDate("%IE", null, curTimestamp)
			assert result.size() == 0 * factor

			result = serviceUnderTest.findByAgentAndByBrowserSinceDate("het%", "FF", curTimestamp)
			assert result.size() == 1 * factor

			result = serviceUnderTest.findByAgentAndByBrowserSinceDate("het%", "IE", curTimestamp)
			assert result.size() == 2 * factor
		}
	}

	/**
	 * @deprecated Redefine test. This Test contains to much logic!
	 */
	@Deprecated
	@Test
	void testFindByAgentAndByBrowserBetweenDate () {

		List<Date> tempList = [oneHoursAgo, twoHoursAgo]

		tempList.each { curTimestamp ->

			def factor = curTimestamp == oneHoursAgo? 2 : 3

			List<EventResult> result = serviceUnderTest.findByAgentAndByBrowserBetweenDate(null, "FF", curTimestamp, now)
			assert result.size() == 3 * factor

			result = serviceUnderTest.findByAgentAndByBrowserBetweenDate(null, "IE", curTimestamp, now)
			assert result.size() == 5 * factor

			result = serviceUnderTest.findByAgentAndByBrowserBetweenDate("phy%", null, curTimestamp, now)
			assert result.size() == 5 * factor

			result = serviceUnderTest.findByAgentAndByBrowserBetweenDate("het%", null, curTimestamp, now)
			assert result.size() == 4 * factor

			result = serviceUnderTest.findByAgentAndByBrowserBetweenDate("%FF", null, curTimestamp, now)
			assert result.size() == 3 * factor

			result = serviceUnderTest.findByAgentAndByBrowserBetweenDate("%IE", null, curTimestamp, now)
			assert result.size() == 0 * factor

			result = serviceUnderTest.findByAgentAndByBrowserBetweenDate("het%", "FF", curTimestamp, now)
			assert result.size() == 1 * factor

			result = serviceUnderTest.findByAgentAndByBrowserBetweenDate("het%", "IE", curTimestamp, now)
			assert result.size() == 2 * factor
		}
	}

	@Test
	void testIsCsiRelevant(){
		EventResult resultWithThousandMillisecsDocTime = resultOfcurRun_Now

		assertFalse(serviceUnderTest.isCsiRelevant(resultWithThousandMillisecsDocTime, 1200,180000))
		assertFalse(serviceUnderTest.isCsiRelevant(resultWithThousandMillisecsDocTime, 250,900))
		assertTrue(serviceUnderTest.isCsiRelevant(resultWithThousandMillisecsDocTime, 250,180000))
	}

	@Test
	void testFindByJobSinceDate() {
		List<EventResult> result = serviceUnderTest.findByJobSinceDate(job1, oneHoursAgo)

		assertNotNull(result)
		assertEquals(1, result.size())

		result = serviceUnderTest.findByJobSinceDate(job1, twoHoursAgo)

		assertNotNull(result)
		assertEquals(2, result.size())
	}

	@Test
	void testFindByJobBetweenDate() {
		List<EventResult> result = serviceUnderTest.findByJobBetweenDate(job1, oneHoursAgo, now, true, CachedView.UNCACHED)

		assertNotNull(result)
		assertEquals(2, result.size())

		result = serviceUnderTest.findByJobBetweenDate(job1, twoHoursAgo, now, true, CachedView.UNCACHED)

		assertNotNull(result)
		assertEquals(3, result.size())

		result = serviceUnderTest.findByJobBetweenDate(job1, threeHoursAgo, now, true, CachedView.UNCACHED)

		assertNotNull(result)
		assertEquals(4, result.size())

		result = serviceUnderTest.findByJobBetweenDate(job1, threeHoursAgo, twoHoursAgo, true, CachedView.UNCACHED)

		assertNotNull(result)
		assertEquals(2, result.size())
	}

	@Ignore("Currently tested with integration test cause GORM does not support EQL in unit tests. After method is changed to use the criteria API this need to be tested in an integration test.")
	@Test
	void _testFindByAgentAndByBrowserBetweenDateAsMap() {
		def result = serviceUnderTest.findByAgentAndByBrowserBetweenDateAsMap(null, null, twoHoursAgo, now)

		assertNotNull(result)
		assertEquals(9, result.size())
	}

	/**
	 * <p>
	 * Tests the query in a real world scenario, so all data is generated as 
	 * valid domains.
	 * </p>
	 * 
	 * <p>
	 * This test is quite a bit complicated. It is a full-stack test which 
	 * tests values within, before and after the queried time range. This is 
	 * done to not duplicate data creation -> three conditions tested at once. 
	 * </p>
	 */
	@Test
	public void testFindByMeasuredEventBetweenDate() {
		// Define a date range, an hour to work within:
		// - from is: 08.08.2013 - 14:00:00
		Date fromDate = new Date(1375963200000L);
		// - to is: 08.08.2013 - 14:59:59.999
		Date toDate = new Date(1375966799999L);

		// Define a MeasuredEvent:
		Page pageOfEvent = new Page(name: 'Testpage', /*Not of interest but required by constraint: */weight: 0).save(failOnError:true);
		MeasuredEvent theEventToLookFor = new MeasuredEvent(name: 'Testevent', testedPage: pageOfEvent).save(failOnError:true);
		MeasuredEvent anotherEventNotOfInterest = new MeasuredEvent(name: 'Testevent-2', testedPage: pageOfEvent).save(failOnError:true);

		// Define a group:
		JobGroup jobGroupCsiLhotse = new JobGroup(name: 'CSI Lhotse', groupType: JobGroupType.CSI_AGGREGATION).save(failOnError:true);
		
		// Define a Browser and a Location (not relevant, because query works on copied data, but required on constraints):
		Browser browser = new Browser(name: 'FF-w0', weight:0).save(failOnError:true);
		WebPageTestServer wptServer = new WebPageTestServer(label:'Testserver', proxyIdentifier:'testproxy', baseUrl:'http://example.com/', active:true).save(failOnError:true);
		Location location = new Location(active: true, location: 'agent1.example.com', browser: browser, label: 'Test-location', wptServer: wptServer).save(failOnError:true);
		Script script = Script.createDefaultScript('Unnamed').save(failOnError: true)
		
		// Define a job:
		Job theJobWichIsNotOfInterestHere = new Job(
                label:'testjob',
                active: false,
                location: location,
                description:'This is a test job only required during constraints',
                runs:1,
                jobGroup: jobGroupUndefined,
                script: script,
                maxDownloadTimeInMinutes: 60,
                customConnectivityProfile: true,
                customConnectivityName: 'Custom (6.000/512 Kbps, 50ms)',
                bandwidthDown: 6000,
                bandwidthUp: 512,
                latency: 50,
                packetLoss: 0
        ).save(failOnError:true)

		// Define some job results (we will only query on copied fields, so we skip references except the ones to event results):
		// - the first within range, measured at 08.08.2013 - 14:02:14:
		Date measuringDateOfWithinDateRange1 = new Date(1375963334000L)
		
		JobResult jobResultWithinDateRange1 = createJobResult(measuringDateOfWithinDateRange1,theJobWichIsNotOfInterestHere)
		EventResult eventResultWithinDateRange1_1_relevant =createEventResult(theEventToLookFor,CachedView.UNCACHED,true, jobResultWithinDateRange1, measuringDateOfWithinDateRange1);
		//Create not relevant EventResults
		EventResult eventResultWithinDateRange1_2_notRelevant_notMedian = createEventResult(theEventToLookFor, CachedView.UNCACHED,false,jobResultWithinDateRange1, measuringDateOfWithinDateRange1);
		EventResult eventResultWithinDateRange1_3_notRelevant_notUnchached = createEventResult(theEventToLookFor,CachedView.CACHED,true,jobResultWithinDateRange1,measuringDateOfWithinDateRange1);
		EventResult eventResultWithinDateRange1_3_notRelevant_differentEvent =createEventResult(anotherEventNotOfInterest,CachedView.UNCACHED,true, jobResultWithinDateRange1,measuringDateOfWithinDateRange1);


		// - the second within range, measured at 08.08.2013 - 14:14:44:
		Date measuringDateOfWithinDateRange2 = new Date(1375964084000L)
		JobResult jobResultWithinDateRange2 = createJobResult(measuringDateOfWithinDateRange2,theJobWichIsNotOfInterestHere)
		EventResult eventResultWithinDateRange2_relevant = createEventResult(theEventToLookFor,CachedView.UNCACHED,true,jobResultWithinDateRange2, measuringDateOfWithinDateRange2);


		// - before the range, measured at 08.08.2013 - 13:59:59.999:
		Date measuringDateOfBeforeDateRange = new Date(1375963199999L)
		JobResult jobResultBeforeDateRange = createJobResult(measuringDateOfBeforeDateRange,theJobWichIsNotOfInterestHere)
		EventResult eventResultBeforeDateRange_wouldBeRelevantIfWasWithin = createEventResult(theEventToLookFor,CachedView.UNCACHED,true,jobResultBeforeDateRange, measuringDateOfBeforeDateRange);


		// - after the range, measured at 08.08.2013 - 15:00:00:
		Date measuringDateOfAfterDateRange = new Date(1375966800000L)
		JobResult jobResultAfterDateRange = createJobResult(measuringDateOfAfterDateRange,theJobWichIsNotOfInterestHere)
		EventResult eventResultAfterDateRange_wouldBeRelevantIfWasWithin = createEventResult(theEventToLookFor,CachedView.UNCACHED,true,jobResultAfterDateRange, measuringDateOfAfterDateRange);
		
		// Run the test:
		Collection<EventResult> result = serviceUnderTest.findByMeasuredEventBetweenDate(jobGroupCsiLhotse, theEventToLookFor, location, fromDate, toDate);

		// Verify result:
		assertNotNull(result);
		assertEquals(2, result.size());
		assertEquals(1, result.count({ it.ident() == eventResultWithinDateRange1_1_relevant.ident() }))
		assertEquals(1, result.count({ it.ident() == eventResultWithinDateRange2_relevant.ident() }))
	}
	
	/**
	 * Creates an EventResult
	 * @param theEventToLookFor
	 * @param cachedView
	 * @param medianValue
	 * @param jobResultWithinDateRange1
	 * @param measuringDateOfWithinDateRange1
	 * @return
	 */
	private EventResult createEventResult(MeasuredEvent theEventToLookFor,CachedView cachedView,Boolean medianValue, JobResult jobResultWithinDateRange1, Date measuringDateOfWithinDateRange1){
		new EventResult(measuredEvent : theEventToLookFor, cachedView : cachedView, medianValue: medianValue, jobResult: jobResultWithinDateRange1, jobResultDate: measuringDateOfWithinDateRange1, /*Not of interest but required by constraint: */jobResultJobConfigId: 1, wptStatus:200, numberOfWptRun:1, speedIndex: EventResult.SPEED_INDEX_DEFAULT_VALUE).save(failOnError:true);
	}

	/**
	 * Creates a JobResult
	 * @param date
	 * @param job
	 * @return
	 */
	private JobResult createJobResult(Date date, Job job){
		new JobResult(
				jobGroupName: 'CSI Lhotse',
				locationLocation: 'agent1.example.com',
				locationBrowser: 'FF-w0',
				date: date,
				/*Not of interest but required by constraint: */
				testId: 1,
				httpStatusCode: 200,
				description: 'test',
				jobConfigLabel: 'testjob',
				jobConfigRuns: 1,
				job: job
		).save(failOnError: true)
	}
}

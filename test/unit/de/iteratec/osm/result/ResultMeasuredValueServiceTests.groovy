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

import de.iteratec.osm.csi.TestDataUtil
import de.iteratec.osm.measurement.schedule.ConnectivityProfile
import grails.test.mixin.*

import org.apache.commons.lang.time.DateUtils
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito

import de.iteratec.osm.report.chart.MeasuredValueDaoService
import de.iteratec.osm.report.chart.MeasuredValueUtilService
import de.iteratec.osm.measurement.schedule.Job
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.measurement.schedule.JobGroupType
import de.iteratec.osm.csi.Page
import de.iteratec.osm.report.chart.AggregatorType
import de.iteratec.osm.report.chart.MeasurandGroup
import de.iteratec.osm.report.chart.MeasuredValue
import de.iteratec.osm.report.chart.MeasuredValueInterval
import de.iteratec.osm.result.dao.EventResultDaoService
import de.iteratec.osm.measurement.script.Script
import de.iteratec.osm.measurement.environment.Browser
import de.iteratec.osm.measurement.environment.dao.BrowserDaoService
import de.iteratec.osm.measurement.environment.Location
import de.iteratec.osm.measurement.environment.WebPageTestServer
/**
 * Test-suite of {@link de.iteratec.osm.result.ResultMeasuredValueService}
 */
@TestFor(ResultMeasuredValueService)
@Mock([EventResult, Job, JobResult, JobGroup, MeasuredValue, MeasuredEvent, WebPageTestServer,
	Browser, Page, Location, AggregatorType, MeasuredValueInterval, Script, ConnectivityProfile])
class ResultMeasuredValueServiceTests {

	ResultMeasuredValueService serviceUnderTest
	BrowserDaoService browserDaoServiceMock
	MeasuredValueUtilService measuredValueUtilServiceMock
	MeasuredValueTagService measuredValueTagServiceMock

	List<EventResult> results = []


	MeasuredEvent measuredEvent
	Job job1, job2
	Date runDate

	MeasuredValueInterval hourly, daily, weekly
	AggregatorType docCompleteTime, domTime, firstByte, fullyLoadedRequestCount, fullyLoadedTime, loadTime, startRender, docCompleteIncommingBytes, docCompleteRequests, fullyLoadedIncommingBytes

	JobResult runInHour_One, runInHour_Two, runBeforeHour, runAfterHour
	EventResult resultRunInHour_One, resultRunInHour_Two, resultRunBeforeHour, resultRunAfterHour

	ConnectivityProfile connectivityProfile

	@Before
	void setUp() {
		serviceUnderTest = service;


		/** DAO Services **/
		browserDaoServiceMock = Mockito.mock(BrowserDaoService.class);
		serviceUnderTest.browserDaoService = browserDaoServiceMock;
		
		//DB Call Find By should explicit be tested
		serviceUnderTest.jobResultDaoService = new JobResultDaoService();
		serviceUnderTest.eventResultDaoService = new EventResultDaoService();

		/** Functional Services **/
		measuredValueTagServiceMock = new MeasuredValueTagService();
		serviceUnderTest.measuredValueTagService = measuredValueTagServiceMock
		measuredValueUtilServiceMock=new MeasuredValueUtilService();
		serviceUnderTest.measuredValueUtilService = measuredValueUtilServiceMock

		// Init some data:
		initIteratecTestData();
	}

	private void initIteratecTestData () {

		/*  2013-05-29T10:13:02.564+02:00   1369815182564 */
		runDate = DateUtils.setMinutes(DateUtils.setSeconds(new Date(1369815182564), 1), 1);
		Date tenMinutesAfter = DateUtils.addMinutes(runDate, +10)
		Date twentyMinutesAfterNow = DateUtils.addMinutes(runDate, +20)

		Date oneHourBefore = DateUtils.addHours(runDate, -1)
		Date oneHourAfter = DateUtils.addHours(runDate, +1)

		WebPageTestServer server= new WebPageTestServer(
				baseUrl : 'http://server1.wpt.server.de',
				active : true,
				label : 'server 1 - wpt server',
				proxyIdentifier : 'server 1 - wpt server'
				).save(failOnError: true);

		JobGroup jobGroup = new JobGroup(
				name: "TestGroup",
				groupType: JobGroupType.CSI_AGGREGATION
				).save(failOnError: true)

		Browser fireFoxBrowser = new Browser(
				name:'FF',
				weight: 0.55).save(failOnError:true)
		Browser ieBrowser = new Browser(
				name:'IE',
				weight: 0.25).save(failOnError:true)
		Browser i8eBrowser = new Browser(
				name:'I8E',
				weight: 0.20).save(failOnError:true)

		Location ffAgent1 = new Location(
				active: true,
				valid: 1,
				location: 'physNetLabAgent01-FF',
				label: 'physNetLabAgent01 - FF up to date',
				browser: fireFoxBrowser,
				wptServer: server
				).save(failOnError: true)


		Page homepage = new Page(
				name: 'homepage',
				weight: 0.5
				).save(failOnError: true)

		connectivityProfile = TestDataUtil.createConnectivityProfile("Test")

		daily = new MeasuredValueInterval(name: "daily", intervalInMinutes: MeasuredValueInterval.DAILY).save(failOnError: true)
		weekly = new MeasuredValueInterval(name: "weekly", intervalInMinutes: MeasuredValueInterval.WEEKLY).save(failOnError: true)
		hourly = new MeasuredValueInterval(name: "hourly", intervalInMinutes: MeasuredValueInterval.HOURLY).save(failOnError: true)

		docCompleteTime=new AggregatorType(name: AggregatorType.RESULT_UNCACHED_DOC_COMPLETE_TIME, measurandGroup: MeasurandGroup.LOAD_TIMES).save(failOnError: true)
		docCompleteIncommingBytes=new AggregatorType(name: AggregatorType.RESULT_UNCACHED_DOC_COMPLETE_INCOMING_BYTES, measurandGroup: MeasurandGroup.REQUEST_SIZES).save(failOnError: true)
		docCompleteRequests=new AggregatorType(name: AggregatorType.RESULT_UNCACHED_DOC_COMPLETE_REQUESTS, measurandGroup: MeasurandGroup.REQUEST_COUNTS).save(failOnError: true)
		fullyLoadedIncommingBytes=new AggregatorType(name: AggregatorType.RESULT_UNCACHED_FULLY_LOADED_INCOMING_BYTES, measurandGroup: MeasurandGroup.REQUEST_SIZES).save(failOnError: true)
		domTime=new AggregatorType(name: AggregatorType.RESULT_UNCACHED_DOM_TIME, measurandGroup: MeasurandGroup.LOAD_TIMES).save(failOnError: true)
		firstByte=new AggregatorType(name: AggregatorType.RESULT_UNCACHED_FIRST_BYTE, measurandGroup: MeasurandGroup.REQUEST_SIZES).save(failOnError: true)
		fullyLoadedRequestCount=new AggregatorType(name: AggregatorType.RESULT_UNCACHED_FULLY_LOADED_REQUEST_COUNT, measurandGroup: MeasurandGroup.REQUEST_COUNTS).save(failOnError: true)
		fullyLoadedTime=new AggregatorType(name: AggregatorType.RESULT_UNCACHED_FULLY_LOADED_TIME, measurandGroup: MeasurandGroup.LOAD_TIMES).save(failOnError: true)
		loadTime=new AggregatorType(name: AggregatorType.RESULT_UNCACHED_LOAD_TIME, measurandGroup: MeasurandGroup.LOAD_TIMES).save(failOnError: true)
		startRender=new AggregatorType(name: AggregatorType.RESULT_UNCACHED_START_RENDER, measurandGroup: MeasurandGroup.LOAD_TIMES).save(failOnError: true)

		new AggregatorType(name: AggregatorType.RESULT_UNCACHED_CUSTOMER_SATISFACTION_IN_PERCENT, measurandGroup: MeasurandGroup.PERCENTAGES).save(failOnError: true)
		new AggregatorType(name: AggregatorType.RESULT_UNCACHED_SPEED_INDEX, measurandGroup: MeasurandGroup.LOAD_TIMES).save(failOnError: true)
		new AggregatorType(name: AggregatorType.RESULT_UNCACHED_VISUALLY_COMPLETE, measurandGroup: MeasurandGroup.LOAD_TIMES).save(failOnError: true)
		
		new AggregatorType(name: AggregatorType.RESULT_CACHED_DOC_COMPLETE_TIME, measurandGroup: MeasurandGroup.LOAD_TIMES).save(failOnError: true)
		new AggregatorType(name: AggregatorType.RESULT_CACHED_DOC_COMPLETE_INCOMING_BYTES, measurandGroup: MeasurandGroup.REQUEST_SIZES).save(failOnError: true)
		new AggregatorType(name: AggregatorType.RESULT_CACHED_DOC_COMPLETE_REQUESTS, measurandGroup: MeasurandGroup.REQUEST_COUNTS).save(failOnError: true)
		new AggregatorType(name: AggregatorType.RESULT_CACHED_FULLY_LOADED_INCOMING_BYTES, measurandGroup: MeasurandGroup.REQUEST_SIZES).save(failOnError: true)
		new AggregatorType(name: AggregatorType.RESULT_CACHED_DOM_TIME, measurandGroup: MeasurandGroup.LOAD_TIMES).save(failOnError: true)
		new AggregatorType(name: AggregatorType.RESULT_CACHED_FIRST_BYTE, measurandGroup: MeasurandGroup.LOAD_TIMES).save(failOnError: true)
		new AggregatorType(name: AggregatorType.RESULT_CACHED_FULLY_LOADED_REQUEST_COUNT, measurandGroup: MeasurandGroup.REQUEST_COUNTS).save(failOnError: true)
		new AggregatorType(name: AggregatorType.RESULT_CACHED_FULLY_LOADED_TIME, measurandGroup: MeasurandGroup.LOAD_TIMES).save(failOnError: true)
		new AggregatorType(name: AggregatorType.RESULT_CACHED_LOAD_TIME, measurandGroup: MeasurandGroup.LOAD_TIMES).save(failOnError: true)
		new AggregatorType(name: AggregatorType.RESULT_CACHED_START_RENDER, measurandGroup: MeasurandGroup.LOAD_TIMES).save(failOnError: true)
		new AggregatorType(name: AggregatorType.RESULT_CACHED_CUSTOMER_SATISFACTION_IN_PERCENT, measurandGroup: MeasurandGroup.PERCENTAGES).save(failOnError: true)
		new AggregatorType(name: AggregatorType.RESULT_CACHED_SPEED_INDEX, measurandGroup: MeasurandGroup.LOAD_TIMES).save(failOnError: true)
		new AggregatorType(name: AggregatorType.RESULT_CACHED_VISUALLY_COMPLETE, measurandGroup: MeasurandGroup.LOAD_TIMES).save(failOnError: true)

		Script script = Script.createDefaultScript('Unnamed').save(failOnError: true)
		
		job1 = new Job(
            id: 1,
            active: false,
            label: 'BV1 - Step 01',
            description: 'This is job 01...',
            location: ffAgent1,
            frequencyInMin: 5,
            runs: 1,
            jobGroup: jobGroup,
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
            jobGroup: jobGroup,
            script: script,
            maxDownloadTimeInMinutes: 60,
            customConnectivityProfile: true,
            customConnectivityName: 'Custom (6.000/512 Kbps, 50ms)',
            bandwidthDown: 6000,
            bandwidthUp: 512,
            latency: 50,
            packetLoss: 0
        ).save(failOnError: true)

		measuredEvent = new MeasuredEvent()
		measuredEvent.setName('Test event')
		measuredEvent.setTestedPage(homepage)
		measuredEvent.save(failOnError:true)

		/* Create TestData */

		/* First Run in Hour */
		runInHour_One = new JobResult(
				job: job1,
				date: runDate,
				testId: '1',
				description: '',
				jobConfigLabel: job1.label,
				jobConfigRuns: job1.runs,
				jobGroupName: job1.jobGroup.name,
				frequencyInMin: 5,
				locationLocation: job1.location.location,
				locationBrowser: job1.location.browser.name,
				httpStatusCode : 200,
				).save(failOnError: true)

		resultRunInHour_One = new EventResult(
				numberOfWptRun: 1,
				cachedView: CachedView.UNCACHED,
				medianValue: true,
				docCompleteIncomingBytes: 1,
				docCompleteRequests: 2,
				docCompleteTimeInMillisecs: 3,
				domTimeInMillisecs: 4,
				firstByteInMillisecs: 5,
				fullyLoadedIncomingBytes: 6,
				fullyLoadedRequestCount: 7,
				fullyLoadedTimeInMillisecs: 8,
				loadTimeInMillisecs: 9,
				startRenderInMillisecs: 10,
				downloadAttempts: 1,
				firstStatusUpdate: runDate,
				lastStatusUpdate: runDate,
				wptStatus: 0,
				validationState : 'validationState',
				harData: 'harData',
				customerSatisfactionInPercent:  1,
				jobResult: runInHour_One,
				jobResultDate: runInHour_One.date,
				jobResultJobConfigId: runInHour_One.job.ident(),
				measuredEvent: measuredEvent,
				speedIndex: EventResult.SPEED_INDEX_DEFAULT_VALUE,
				connectivityProfile: connectivityProfile
				).save(failOnError: true)
		
		runInHour_One.save(failOnError: true)

		/* Secound Run in Hour */
		runInHour_Two = new JobResult(
				job: job1,
				date: tenMinutesAfter,
				testId: '1',
				description: '',
				jobConfigLabel: job1.label,
				jobConfigRuns: job1.runs,
				jobGroupName: job1.jobGroup.name,
				frequencyInMin: 5,
				locationLocation: job1.location.location,
				locationBrowser: job1.location.browser.name,
				httpStatusCode : 200,
				).save(failOnError: true)

		resultRunInHour_Two = new EventResult(
				numberOfWptRun: 1,
				cachedView: CachedView.UNCACHED,
				medianValue: true,
				docCompleteIncomingBytes: 10,
				docCompleteRequests: 20,
				docCompleteTimeInMillisecs: 30,
				domTimeInMillisecs: 40,
				firstByteInMillisecs: 50,
				fullyLoadedIncomingBytes: 60,
				fullyLoadedRequestCount: 70,
				fullyLoadedTimeInMillisecs: 80,
				loadTimeInMillisecs: 90,
				startRenderInMillisecs: 100,
				downloadAttempts: 1,
				firstStatusUpdate: runDate,
				lastStatusUpdate: runDate,
				wptStatus: 0,
				validationState : 'validationState',
				harData: 'harData',
				customerSatisfactionInPercent:  1,
				jobResult: runInHour_Two,
				jobResultDate: runInHour_Two.date,
				jobResultJobConfigId: runInHour_Two.job.ident(),
				measuredEvent: measuredEvent,
				speedIndex: EventResult.SPEED_INDEX_DEFAULT_VALUE,
				connectivityProfile: connectivityProfile
				).save(failOnError: true)
		
		runInHour_Two.save(failOnError: true)

		/* Run in before Hour */
		runBeforeHour = new JobResult(
				job: job1,
				date: oneHourBefore,
				testId: '1',
				description: '',
				jobConfigLabel: job1.label,
				jobConfigRuns: job1.runs,
				jobGroupName: job1.jobGroup.name,
				frequencyInMin: 5,
				locationLocation: job1.location.location,
				locationBrowser: job1.location.browser.name,
				httpStatusCode : 200,
				).save(failOnError: true)

		resultRunBeforeHour = new EventResult(
				numberOfWptRun: 1,
				cachedView: CachedView.UNCACHED,
				medianValue: true,
				docCompleteIncomingBytes: 1000,
				docCompleteRequests: 1000,
				docCompleteTimeInMillisecs: 1000,
				domTimeInMillisecs: 1000,
				firstByteInMillisecs: 1000,
				fullyLoadedIncomingBytes: 1000,
				fullyLoadedRequestCount: 1000,
				fullyLoadedTimeInMillisecs: 1000,
				loadTimeInMillisecs: 1000,
				startRenderInMillisecs: 1000,
				downloadAttempts: 1,
				firstStatusUpdate: runDate,
				lastStatusUpdate: runDate,
				wptStatus: 0,
				validationState : 'validationState',
				harData: 'harData',
				customerSatisfactionInPercent:  1,
				jobResult: runBeforeHour,
				jobResultDate: runBeforeHour.date,
				jobResultJobConfigId: runBeforeHour.job.ident(),
				measuredEvent: measuredEvent,
				speedIndex: EventResult.SPEED_INDEX_DEFAULT_VALUE,
				connectivityProfile: connectivityProfile
				).save(failOnError: true)
		
		runBeforeHour.save(failOnError: true)

		/* Run in after Hour */
		runAfterHour = new JobResult(
				job: job1,
				date: oneHourAfter,
				testId: '1',
				description: '',
				jobConfigLabel: job1.label,
				jobConfigRuns: job1.runs,
				jobGroupName: job1.jobGroup.name,
				frequencyInMin: 5,
				locationLocation: job1.location.location,
				locationBrowser: job1.location.browser.name,
				httpStatusCode: 200,
				).save(failOnError: true)

		resultRunAfterHour = new EventResult(
				numberOfWptRun: 1,
				cachedView: CachedView.UNCACHED,
				medianValue: true,
				docCompleteIncomingBytes: 1000,
				docCompleteRequests: 1000,
				docCompleteTimeInMillisecs: 1000,
				domTimeInMillisecs: 1000,
				firstByteInMillisecs: 1000,
				fullyLoadedIncomingBytes: 1000,
				fullyLoadedRequestCount: 1000,
				fullyLoadedTimeInMillisecs: 1000,
				loadTimeInMillisecs: 1000,
				startRenderInMillisecs: 1000,
				downloadAttempts: 1,
				firstStatusUpdate: runDate,
				lastStatusUpdate: runDate,
				wptStatus: 0,
				validationState : 'validationState',
				harData: 'harData',
				customerSatisfactionInPercent:  1,
				jobResult: runAfterHour,
				jobResultDate: runAfterHour.date,
				jobResultJobConfigId: runAfterHour.job.ident(),
				measuredEvent: measuredEvent,
				speedIndex: EventResult.SPEED_INDEX_DEFAULT_VALUE,
				connectivityProfile: connectivityProfile
				).save(failOnError: true)
		
		runAfterHour.save(failOnError: true)
	}



	private void assertIsCalculatedNotConstrain(List<MeasuredValue> values) {
		values.each {MeasuredValue value ->
			assertFalse( value.isCalculated() )
		}
	}

	private void assertIsCalculatedYesNoData(List<MeasuredValue> values) {
		values.each {MeasuredValue value ->
			assertTrue( value.isCalculated() )
			assertNull( value.value )
		}
	}

	@Test
	void testGetAggregatorTypeCachedViewType_CACHED() {
		/* Set up types to check */
		List<String> cached=[
			AggregatorType.RESULT_CACHED_DOC_COMPLETE_INCOMING_BYTES,
			AggregatorType.RESULT_CACHED_DOC_COMPLETE_REQUESTS,
			AggregatorType.RESULT_CACHED_FULLY_LOADED_INCOMING_BYTES,
			AggregatorType.RESULT_CACHED_DOC_COMPLETE_TIME,
			AggregatorType.RESULT_CACHED_DOM_TIME,
			AggregatorType.RESULT_CACHED_FIRST_BYTE,
			AggregatorType.RESULT_CACHED_FULLY_LOADED_REQUEST_COUNT,
			AggregatorType.RESULT_CACHED_FULLY_LOADED_TIME,
			AggregatorType.RESULT_CACHED_LOAD_TIME,
			AggregatorType.RESULT_CACHED_START_RENDER,
			AggregatorType.RESULT_CACHED_CUSTOMER_SATISFACTION_IN_PERCENT,
			AggregatorType.RESULT_CACHED_SPEED_INDEX,
				AggregatorType.RESULT_CACHED_VISUALLY_COMPLETE
		];

		/* test */

		assertEquals(cached.size(), ResultMeasuredValueService.getAggregatorMap().get(CachedView.CACHED).size())

		cached.each {String aggregatorName ->
			AggregatorType aggregator=AggregatorType.findByName(aggregatorName);
			assertNotNull(serviceUnderTest)
			assertNotNull(aggregator)
			assertNotNull(aggregator.name)
			CachedView c = serviceUnderTest.getAggregatorTypeCachedViewType(aggregator);
			assertNotNull(c)
			assertEquals("failed for: "+aggregator.name, CachedView.CACHED, serviceUnderTest.getAggregatorTypeCachedViewType(aggregator));
		}
	}

	@Test
	void testGetAggregatorTypeCachedViewType_UNCACHED() {
		/* Set up types to check */
		List<AggregatorType> uncached=[
			AggregatorType.RESULT_UNCACHED_DOC_COMPLETE_INCOMING_BYTES,
			AggregatorType.RESULT_UNCACHED_DOC_COMPLETE_REQUESTS,
			AggregatorType.RESULT_UNCACHED_FULLY_LOADED_INCOMING_BYTES,
			AggregatorType.RESULT_UNCACHED_DOC_COMPLETE_TIME,
			AggregatorType.RESULT_UNCACHED_DOM_TIME,
			AggregatorType.RESULT_UNCACHED_FIRST_BYTE,
			AggregatorType.RESULT_UNCACHED_FULLY_LOADED_REQUEST_COUNT,
			AggregatorType.RESULT_UNCACHED_FULLY_LOADED_TIME,
			AggregatorType.RESULT_UNCACHED_LOAD_TIME,
			AggregatorType.RESULT_UNCACHED_START_RENDER,
			AggregatorType.RESULT_UNCACHED_CUSTOMER_SATISFACTION_IN_PERCENT,
			AggregatorType.RESULT_UNCACHED_SPEED_INDEX,
				AggregatorType.RESULT_UNCACHED_VISUALLY_COMPLETE
		];

		/* test */

		assertEquals(uncached.size(), ResultMeasuredValueService.getAggregatorMap().get(CachedView.UNCACHED).size())

		uncached.each {String aggregatorName ->
			AggregatorType aggregator=AggregatorType.findByName(aggregatorName);
			assertEquals("failed for: "+aggregator.name, CachedView.UNCACHED, serviceUnderTest.getAggregatorTypeCachedViewType(aggregator));
		}


	}
	
	// mocks /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * Mocks {@link MeasuredValueDaoService}.
	 * @param toReturn
	 * 				{@link MeasuredValue}s to return from mocked method getMvs().
	 */
	private void mockMeasuredValueDaoService(List<MeasuredValue> toReturn){
		def measuredValueDaoServiceMock = mockFor(MeasuredValueDaoService, true)
		measuredValueDaoServiceMock.demand.getMvs(1..10000) {
			Date fromDate,
			Date toDate,
			String rlikePattern,
			MeasuredValueInterval interval,
			AggregatorType aggregator ->
			return toReturn
		}
		serviceUnderTest.measuredValueDaoService = measuredValueDaoServiceMock.createMock()
	}
	
}

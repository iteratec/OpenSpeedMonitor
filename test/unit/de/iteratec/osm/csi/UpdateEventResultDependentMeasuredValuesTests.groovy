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



package de.iteratec.osm.csi

import de.iteratec.osm.OsmConfiguration
import de.iteratec.osm.measurement.environment.Browser
import de.iteratec.osm.measurement.environment.BrowserAlias
import de.iteratec.osm.measurement.environment.Location
import de.iteratec.osm.measurement.environment.WebPageTestServer
import de.iteratec.osm.measurement.schedule.ConnectivityProfile
import de.iteratec.osm.measurement.schedule.Job
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.measurement.schedule.JobGroupType
import de.iteratec.osm.measurement.script.Script
import de.iteratec.osm.report.chart.*
import de.iteratec.osm.result.CachedView
import de.iteratec.osm.result.EventResult
import de.iteratec.osm.result.JobResult
import de.iteratec.osm.result.MeasuredEvent
import de.iteratec.osm.util.PerformanceLoggingService
import de.iteratec.osm.util.ServiceMocker
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.junit.Test

import static org.junit.Assert.assertEquals

/**
 * Tests the updating of hourly event-{@link MeasuredValue}s when a new {@link EventResult} is coming in.
 */
@TestMixin(GrailsUnitTestMixin)
@TestFor(EventMeasuredValueService)
@Mock([Browser, BrowserAlias, JobGroup, Location, MeasuredEvent, Page, WebPageTestServer, MeasuredValue, MeasuredValueInterval,
	AggregatorType, Location, EventResult, JobResult, Job, OsmConfiguration, CsiDay, Script, MeasuredValueUpdateEvent, ConnectivityProfile])
class UpdateEventResultDependentMeasuredValuesTests {
	
	static final double DELTA = 1e-15
	static final DateTime resultsExecutionTime = new DateTime(2012,1,1,0,0,0, DateTimeZone.UTC)
	static final String measuredEventName = 'HP:::BV1 - Step 01'
	static final String pageNameHp = 'HP'
	static final String browserName = 'IE'
	static final String locationLocation = 'myLocation'
	static final String serverLabel = 'server 1 - wpt server'
	static final String labelJobOfCsiGroup1 = 'HP-Job of csiGroup1'
	static final String labelJobOfCsiGroup2 = 'HP-Job of csiGroup2'
	static final String testIdOfJobRunCsiGroup1 = 'testId1'
	static final String testIdOfJobRunCsiGroup2 = 'testId2'
	static final String group1Name = 'group1'
	static final String group2Name = 'group2'
	
	MeasuredValueInterval hourly
	AggregatorType measuredEvent
	EventResult result1OfCsiGroup1
	EventResult result2OfCsiGroup1
	EventResult result1OfCsiGroup2
	EventResult result2OfCsiGroup2
	
	EventMeasuredValueService serviceUnderTest
	ServiceMocker mockGenerator

    void setUp() {
		
		serviceUnderTest = service	
		
		//mocks common for all tests
		serviceUnderTest.performanceLoggingService = new PerformanceLoggingService() 
		mockGenerator = ServiceMocker.create()
		mockGenerator.mockOsmConfigCacheService(serviceUnderTest)
		mockGenerator.mockEventResultService(serviceUnderTest)
		mockGenerator.mockJobResultDaoService(serviceUnderTest)
		mockGenerator.mockBrowserService(serviceUnderTest)
		mockGenerator.mockMeasuredValueUpdateEventDaoService(serviceUnderTest)
		Map idAsStringToJobGroupMap_irrelevantCauseNotUsedInTheseTests = [:]
		Map idAsStringToMeasuredEventMap_irrelevantCauseNotUsedInTheseTests = [:]
		Map idAsStringToPageMap_irrelevantCauseNotUsedInTheseTests = [:]
		Map idAsStringToBrowserMap_irrelevantCauseNotUsedInTheseTests = [:]
		Map idAsStringToLocationMap_irrelevantCauseNotUsedInTheseTests = [:]
		mockGenerator.mockMeasuredValueTagService(
			serviceUnderTest,
			idAsStringToJobGroupMap_irrelevantCauseNotUsedInTheseTests,
			idAsStringToMeasuredEventMap_irrelevantCauseNotUsedInTheseTests,
			idAsStringToPageMap_irrelevantCauseNotUsedInTheseTests,
			idAsStringToBrowserMap_irrelevantCauseNotUsedInTheseTests,
			idAsStringToLocationMap_irrelevantCauseNotUsedInTheseTests
        )
		createTestDataForAllTests()
		initializeFields()

    }

    void tearDown() {
		deleteTestData()
    }
	
	//tests////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * Tests the update of dependent hourly event-{@link MeasuredValue}s.
	 */
	@Test
	void testUpdateDependentMeasuredValues() {
		
		//create test-specific data
		
		JobResult run = JobResult.findByTestId(testIdOfJobRunCsiGroup1)
		EventResult result1 = createNewResult(run, 50, '1;1;1;1;1')
		EventResult result2 = createNewResult(run, 60, '1;1;1;1;1')
		EventResult result3 = createNewResult(run, 70, '1;1;1;1;1')
		EventResult result4 = createNewResult(run, 80, '1;1;1;1;1')
		EventResult result5 = createNewResult(run, 90, '1;1;1;1;1')
		
		//execute test
		
		serviceUnderTest.createOrUpdateHourlyValue(resultsExecutionTime, result1)
		
		//assertions (and following executions of tested method)
		
		List<MeasuredValue> hourlyMvs = serviceUnderTest.findAll(resultsExecutionTime.toDate(), resultsExecutionTime.toDate(), hourly)
		Integer countEvents = 1
		assertEquals(countEvents, hourlyMvs.size())
		
		MeasuredValue calculated = hourlyMvs[0]
		Double expectedValue = 50
		proofHemv(calculated, true, 1, expectedValue)
		
		serviceUnderTest.createOrUpdateHourlyValue(resultsExecutionTime, result2)
		expectedValue = (50 + 60) / 2
		proofHemv(calculated, true, 2, expectedValue)
		
		serviceUnderTest.createOrUpdateHourlyValue(resultsExecutionTime, result3)
		serviceUnderTest.createOrUpdateHourlyValue(resultsExecutionTime, result4)
		serviceUnderTest.createOrUpdateHourlyValue(resultsExecutionTime, result5)
		expectedValue = (50 + 60 + 70 + 80 + 90) / 5
		proofHemv(calculated, true, 5, expectedValue)
		
	}
	
	/**
	 * Tests the update of dependent hourly event-{@link MeasuredValue}s for different CSI-{@link JobGroup}s.
	 */
	@Test
	void testUpdateDependentMeasuredValuesForMultipleCsiGroups() {
		//create test-specific data
		
		JobResult runOfJobOfCsiGroup1 = JobResult.findByTestId(testIdOfJobRunCsiGroup1)
		JobResult runOfJobOfCsiGroup2 = JobResult.findByTestId(testIdOfJobRunCsiGroup2)
		JobGroup group1 = JobGroup.findByName(group1Name)
		JobGroup group2 = JobGroup.findByName(group2Name)
		Long jobGroup1Id = group1.ident()
		Long jobGroup2Id = group2.ident()
		EventResult result1OfCsiGroup1 = createNewResult(runOfJobOfCsiGroup1, 80, "${jobGroup1Id};1;1;1;1")
		EventResult result2OfCsiGroup1 = createNewResult(runOfJobOfCsiGroup1, 90, "${jobGroup1Id};1;1;1;1")
		EventResult result1OfCsiGroup2 = createNewResult(runOfJobOfCsiGroup2, 10, "${jobGroup2Id};1;1;1;1")
		EventResult result2OfCsiGroup2 = createNewResult(runOfJobOfCsiGroup2, 20, "${jobGroup2Id};1;1;1;1")
		EventResult result3OfCsiGroup2 = createNewResult(runOfJobOfCsiGroup2, 30, "${jobGroup2Id};1;1;1;1")
		
		//execute test
		serviceUnderTest.createOrUpdateHourlyValue(resultsExecutionTime, result1OfCsiGroup1)
		serviceUnderTest.createOrUpdateHourlyValue(resultsExecutionTime, result2OfCsiGroup1)
		serviceUnderTest.createOrUpdateHourlyValue(resultsExecutionTime, result1OfCsiGroup2)
		serviceUnderTest.createOrUpdateHourlyValue(resultsExecutionTime, result2OfCsiGroup2)
		serviceUnderTest.createOrUpdateHourlyValue(resultsExecutionTime, result3OfCsiGroup2)
		
		//assertions
		
		List<MeasuredValue> hourlyMvs = serviceUnderTest.findAll(resultsExecutionTime.toDate(), resultsExecutionTime.toDate(), hourly)
		Integer countEvents = 2
		assertEquals(countEvents, hourlyMvs.size())
		
		List<MeasuredValue> hourlyMvsOfGroup1 = hourlyMvs.findAll{it.tag ==~ /${jobGroup1Id};\d+;\d+;\d+;\d+/}
		assertEquals(1, hourlyMvsOfGroup1.size())
		proofHemv(hourlyMvsOfGroup1[0], true, 2, 85)
		
		List<MeasuredValue> hourlyMvsOfGroup2 = hourlyMvs.findAll{it.tag ==~ /${jobGroup2Id};\d+;\d+;\d+;\d+/}
		assertEquals(1, hourlyMvsOfGroup2.size())
		proofHemv(hourlyMvsOfGroup2[0], true, 3, 20)
		
	}
	
	/**
	 * Executes assertions to proof calculated {@link MeasuredValue}.
	 * @param mvHourlyEvent
	 * 			{@link MeasuredValue} to proof
	 * @param expectedCalculatedState
	 * 			Calculated-state of calculated {@link MeasuredValue} to expect. 
	 * @param expectedResultCount
	 * 			Count of {@link EventResult}s of calculated {@link MeasuredValue} to expect.
	 * @param expectedValue
	 * 			Double-value of calculated {@link MeasuredValue} to expect.
	 */
	private void proofHemv(
		MeasuredValue mvHourlyEvent,
		boolean expectedCalculatedState,
		Integer expectedResultCount,
		expectedValue){
		
		assertEquals(resultsExecutionTime.toDate(), mvHourlyEvent.started)
		assertEquals(hourly.intervalInMinutes, mvHourlyEvent.interval.intervalInMinutes)
		assertEquals(measuredEvent.name, mvHourlyEvent.aggregator.name)
		assertEquals(expectedCalculatedState, mvHourlyEvent.isCalculated())
		assertEquals(expectedResultCount, mvHourlyEvent.countResultIds())
		assertEquals(expectedValue, mvHourlyEvent.value, DELTA)
		
	}
		
	/**
	 * Creates a new {@link EventResult} and persists it.
	 * @param jobResult
	 * @param cs
	 * @return
	 */
	private EventResult createNewResult(JobResult jobResult, Integer cs, String resultTag){
		MeasuredEvent event = MeasuredEvent.findByName(measuredEventName) 
		EventResult returnValue=new EventResult(
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
			firstStatusUpdate: resultsExecutionTime.toDate(),
			lastStatusUpdate: resultsExecutionTime.toDate(),
			wptStatus: 0,
			validationState : 'validationState',
			customerSatisfactionInPercent: cs,
			jobResult: jobResult,
			jobResultDate: jobResult.date,
			jobResultJobConfigId: jobResult.job.ident(),
			measuredEvent: event,
			speedIndex: EventResult.SPEED_INDEX_DEFAULT_VALUE,
			connectivityProfile: null,
            customConnectivityName: null,
            noTrafficShapingAtAll: true,
			tag: resultTag).save(failOnError: true)
			
			jobResult.save(failOnError: true)
			
			return returnValue
	}
	
	//testdata////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	private void createTestDataForAllTests(){
		
		createOsmConfiguration()
		createMeasuredValueIntervals()
		createAggregatorTypes()
		createPages()
		createBrowsers()
		createServer()
		createLocations()
		createJobGroups()
		createJobConfigRunAndResult()
		createMeasuredEvents()
		
	}
	private void initializeFields(){
		hourly = MeasuredValueInterval.findByIntervalInMinutes(MeasuredValueInterval.HOURLY)
		measuredEvent = AggregatorType.findByName(AggregatorType.MEASURED_EVENT)
	}
	
	private void createOsmConfiguration(){
		OsmConfiguration.findAll()?:new OsmConfiguration(
			detailDataStorageTimeInWeeks: 2,
			defaultMaxDownloadTimeInMinutes: 60,
			minDocCompleteTimeInMillisecs: 250,
			maxDocCompleteTimeInMillisecs: 180000
		).save(failOnError: true)
	}
	private void createJobGroups(){
		JobGroup.findByName(group1Name)?:new JobGroup(
				name:group1Name,
				groupType: JobGroupType.CSI_AGGREGATION).save(failOnError: true)
		JobGroup.findByName(group2Name)?:new JobGroup(
			name:group2Name,
			groupType: JobGroupType.CSI_AGGREGATION).save(failOnError: true)
	}
	private void createAggregatorTypes(){
		new AggregatorType(name: AggregatorType.MEASURED_EVENT, measurandGroup: MeasurandGroup.NO_MEASURAND).save(failOnError: true)
	}
	private void createMeasuredValueIntervals(){
		new MeasuredValueInterval(
			name: "hourly",
			intervalInMinutes: MeasuredValueInterval.HOURLY
			).save(failOnError: true)
	}
	private void createPages(){
		new Page(
			name: pageNameHp,
			weight: 6).save(failOnError: true)
	}
	private void createBrowsers(){
		new Browser(
			name: browserName,
			weight: 45)
				.addToBrowserAliases(alias: "IE")
				.addToBrowserAliases(alias: "IE8")
				.addToBrowserAliases(alias: "Internet Explorer")
				.addToBrowserAliases(alias: "Internet Explorer 8")
				.save(failOnError: true)
	}
	private void createServer(){
		WebPageTestServer server1
		server1 = new WebPageTestServer(
			baseUrl : 'http://server1.wpt.server.de',
			active : true,
			label : serverLabel,
			proxyIdentifier : 'server 1 - wpt server'
			).save(failOnError: true)
	}
	private void createLocations(){
		WebPageTestServer server1 = WebPageTestServer.findByLabel(serverLabel)
		Browser browser = Browser.findByName(browserName)
		new Location(
			active: true,
			valid: 1,
			location: locationLocation,
			label: 'physNetLabAgent01 - FF up to date',
			browser: browser,
			wptServer: server1
			).save(failOnError: true)
	}
	private void createJobConfigRunAndResult(){
		//job
		Job job1, job2
		Page homepage = Page.findByName(pageNameHp)
		Location agent = Location.findByLocation(locationLocation)
		JobGroup csiGroup1 = JobGroup.findByName(group1Name)
		JobGroup csiGroup2 = JobGroup.findByName(group2Name)
		Script script = Script.createDefaultScript('Unnamed').save(failOnError: true)
		job1 = new Job(
			active: false,
			label: labelJobOfCsiGroup1,
			description: 'job1',
			location: agent,
			frequencyInMin: 5,
			runs: 1,
			jobGroup: csiGroup1,
			script: script,
			maxDownloadTimeInMinutes: 60,
            customConnectivityProfile: false,
			connectivityProfile: null,
			noTrafficShapingAtAll: true
        ).save(failOnError: true)

		job2 = new Job(
			active: false,
			label: labelJobOfCsiGroup2,
			description: 'job2',
			location: agent,
			frequencyInMin: 5,
			runs: 1,
			jobGroup: csiGroup2,
			script: script,
			maxDownloadTimeInMinutes: 60,
			customConnectivityProfile: false,
			connectivityProfile: null,
			noTrafficShapingAtAll: true
        ).save(failOnError: true)

		//wptjobrun
		new JobResult(
			job: job1,
			date: resultsExecutionTime.toDate(),
			testId: testIdOfJobRunCsiGroup1,
			description: '',
			jobConfigLabel: job1.label,
			jobConfigRuns: job1.runs,
			frequencyInMin: 5,
			locationLocation: locationLocation,
			locationBrowser: browserName,
			httpStatusCode: 200,
			testedPage: homepage,
			jobGroupName: group1Name
			).save(failOnError: true)
		new JobResult(
			job: job2,
			date: resultsExecutionTime.toDate(),
			testId: testIdOfJobRunCsiGroup2,
			description: '',
			jobConfigLabel: job2.label,
			jobConfigRuns: job2.runs,
			frequencyInMin: 5,
			locationLocation: locationLocation,
			locationBrowser: browserName,
			httpStatusCode: 200,
			testedPage: homepage,
			jobGroupName: group2Name
			).save(failOnError: true)
	}
	private void createMeasuredEvents(){
		Page homepage = Page.findByName(pageNameHp)
		new MeasuredEvent(name: measuredEventName, testedPage: homepage).save(failOnError: true)
	}
	/**
	 * Deleting testdata.
	 */
	private void deleteTestData() {
		OsmConfiguration.list()*.delete(flush: true)
		MeasuredValue.list()*.delete(flush: true)
		CsiDay.list()*.delete(flush: true)
		EventResult.list()*.delete(flush: true)
		JobResult.list()*.delete(flush: true)
		Job.list()*.delete(flush: true)
		Location.list()*.delete(flush: true)
		Browser.list()*.delete(flush: true)
		WebPageTestServer.list()*.delete(flush: true)
		Page.list()*.delete(flush: true)
		JobGroup.list()*.delete(flush: true)
	}
}

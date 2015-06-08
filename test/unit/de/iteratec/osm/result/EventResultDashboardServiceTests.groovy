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
import grails.test.mixin.support.*

import org.codehaus.groovy.grails.web.mapping.LinkGenerator
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.junit.*
import org.mockito.ArgumentCaptor
import org.mockito.Mockito

import de.iteratec.osm.report.chart.dao.AggregatorTypeDaoService
import de.iteratec.osm.report.chart.OsmChartGraph
import de.iteratec.osm.report.chart.MeasuredValueUtilService
import de.iteratec.osm.measurement.schedule.DefaultJobGroupDaoService
import de.iteratec.osm.measurement.schedule.DefaultPageDaoService
import de.iteratec.osm.measurement.schedule.Job
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.measurement.schedule.JobGroupType
import de.iteratec.osm.csi.Page
import de.iteratec.osm.report.chart.AggregatorType
import de.iteratec.osm.report.chart.MeasurandGroup
import de.iteratec.osm.report.chart.MeasuredValue
import de.iteratec.osm.report.chart.MeasuredValueInterval
import de.iteratec.osm.result.dao.DefaultMeasuredEventDaoService
import de.iteratec.osm.result.dao.EventResultDaoService
import de.iteratec.osm.util.PerformanceLoggingService
import de.iteratec.osm.util.PerformanceLoggingService.IndentationDepth
import de.iteratec.osm.util.PerformanceLoggingService.LogLevel
import de.iteratec.osm.measurement.script.Script
import de.iteratec.osm.measurement.environment.Browser
import de.iteratec.osm.measurement.environment.BrowserAlias
import de.iteratec.osm.measurement.environment.DefaultBrowserDaoService
import de.iteratec.osm.measurement.environment.DefaultLocationDaoService
import de.iteratec.osm.measurement.environment.Location
import de.iteratec.osm.measurement.environment.WebPageTestServer
import de.iteratec.osm.util.I18nService
/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
@TestMixin(GrailsUnitTestMixin)
@TestFor(EventResultDashboardService)
@Mock([Job, JobResult, MeasuredEvent, MeasuredValue, MeasuredValueInterval, Location, Browser, BrowserAlias, Page, JobGroup, AggregatorType, WebPageTestServer, EventResult, Script])
class EventResultDashboardServiceTests {

	EventResultDashboardService serviceUnderTest
	EventResultDaoService eventResultDaoService
	ResultMeasuredValueService resultMeasuredValueService

	/* Mocked Data */
	Browser browser
	WebPageTestServer server
	Location location
	JobGroup jobGroup
	MeasuredEvent measuredEvent
	Page page
	JobResult jobResult, jobResult2
	EventResult eventResultCached, eventResultUncached

	DateTime runDate
	DateTime runDateHourlyStart
	final static String group1Name = "CSI1"
	final static String group2Name = "CSI2"
	final static String page1Name = "page1"
	final static String page2Name = "page2"
	final static String event1Name = "event1"
	final static String event2Name = "event2"
	final static String browser1Name = "browser1"
	final static String browser2Name = "browser2"
	final static String location1Label = "ffLocationLabel"
	final static String location2Label = "ieLocationLabel"
	final static String location1Location = "ffLocationLocation"
	final static String location2Location = "ieLocationLocation"
	final static String i18nNameOfAggregatorTypeUsedInTests = 'i18nNameOfAggregatorTypeUsedInTests'

	@Before
	void setUp() {
		serviceUnderTest=service;
		
		serviceUnderTest.resultMeasuredValueService=new ResultMeasuredValueService()
		serviceUnderTest.resultMeasuredValueService.eventResultDaoService = new EventResultDaoService()
		serviceUnderTest.grailsLinkGenerator = Mockito.mock(LinkGenerator.class);
		serviceUnderTest.jobResultService = Mockito.mock(JobResultService.class);
		mockI18nService()

		runDate = new DateTime(2013,5,29,10,13,2,564, DateTimeZone.UTC)
		runDateHourlyStart = new DateTime(2013,5,29,10,0,0,0, DateTimeZone.UTC)

		createMeasuredValueInterval();
		createBrowser();
		createLocations();
		createCSIGroups();
		createPages();
		createMeasuredEvents();
		createJobResults();
		createEventResult();
	}

	void createMeasuredValueInterval() {
		new MeasuredValueInterval(
				name: "RAW",
				intervalInMinutes: MeasuredValueInterval.RAW
				).save(failOnError: true)
		new MeasuredValueInterval(
			name: "HOURLY",
			intervalInMinutes: MeasuredValueInterval.HOURLY
			).save(failOnError: true)
		new AggregatorType(
				name: AggregatorType.RESULT_CACHED_DOM_TIME,
				measurandGroup: MeasurandGroup.LOAD_TIMES
				).save(failOnError: true)
		new AggregatorType(
				name: AggregatorType.RESULT_UNCACHED_DOM_TIME,
				measurandGroup: MeasurandGroup.LOAD_TIMES
				).save(failOnError: true)
	}

	void createBrowser() {
		browser=new Browser(
				name: "Test",
				weight: 1)
				.addToBrowserAliases(new BrowserAlias(alias: "Test"))
				.save(failOnError: true)
		new Browser(
				name: "Test2",
				weight: 1)
				.addToBrowserAliases(new BrowserAlias(alias: "Test2"))
				.save(failOnError: true)
	}

	void createLocations() {
		server = new WebPageTestServer(
				baseUrl : 'http://server1.wpt.server.de',
				active : true,
				label : 'server 1 - wpt server',
				proxyIdentifier : 'server 1 - wpt server'
				).save(failOnError: true)

		location = new Location(
				active: true,
				valid: 1,
				location: location1Location,
				label: location1Label,
				browser: browser,
				wptServer: server
				).save(failOnError: true)

		Browser browser2 = Browser.findByName("Test2")
		assertNotNull(browser2)

		Location  ieAgent1 = new Location(
				active: true,
				valid: 1,
				location: location2Location,
				label: location2Label,
				browser: browser2,
				wptServer: server
				).save(failOnError: true)
	}

	void createCSIGroups() {
		jobGroup=new JobGroup(
				name: group1Name,
				groupType: JobGroupType.CSI_AGGREGATION).save(failOnError: true);
		new JobGroup(
				name: group2Name,
				groupType: JobGroupType.CSI_AGGREGATION).save(failOnError: true)
	}

	void createMeasuredEvents() {
		measuredEvent=new MeasuredEvent(
				name: event1Name,
				testedPage: page).save(failOnError: true);
		new MeasuredEvent(
				name: event2Name,
				testedPage: page).save(failOnError: true)
	}

	void createPages() {
		page=new Page(
				name: page1Name,
				weight: 0).save(failOnError: true)
		new Page(
				name: page2Name,
				weight: 0).save(failOnError: true)
	}

	void createJobResults() {
		Script script = Script.createDefaultScript('Unnamed').save(failOnError: true)
		
		Job parentJob=new Job(
				label: "TestJob",
				location: location,
				page: page,
				active: false,
				description: '',
				runs: 1,
				jobGroup: jobGroup,
				maxDownloadTimeInMinutes: 60,
				script: script,
                customConnectivityProfile: true,
                customConnectivityName: 'Custom (6.000/512 Kbps, 50ms)',
                bandwidthDown: 6000,
                bandwidthUp: 512,
                latency: 50,
                packetLoss: 0
        ).save(failOnError: true);

		jobResult=new JobResult(
				date: runDate.toDate(),
				testId: "1337",
				httpStatusCode: 200,
				jobConfigLabel: "TestJob",
				jobConfigRuns: 1,
				description: '',
				locationBrowser: location.browser.name,
				locationLocation: location.location,
				jobGroupName: parentJob.jobGroup.name,
				job: parentJob
				).save(failOnError: true)

		jobResult2=new JobResult(
				date: runDate.toDate(),
				testId: "1337",
				httpStatusCode: 200,
				jobConfigLabel: "TestJob",
				jobConfigRuns: 1,
				description: '',
				locationBrowser: location.browser.name,
				locationLocation: location.location,
				jobGroupName: parentJob.jobGroup.name,
				job: parentJob
				).save(failOnError: true)
	}


	private void createEventResult() {
		eventResultCached = new EventResult(
				numberOfWptRun: 1,
				cachedView: CachedView.CACHED,
				medianValue: true,
				wptStatus:200,
				docCompleteTimeInMillisecs: 1,
				domTimeInMillisecs: 2,
				customerSatisfactionInPercent: 0,
				jobResult: jobResult,
				jobResultDate: jobResult.date,
				jobResultJobConfigId: jobResult.job.ident(),
				measuredEvent: measuredEvent,
				tag:'1;1;1;1;1',
				speedIndex: EventResult.SPEED_INDEX_DEFAULT_VALUE
				).save(failOnError: true)
		
		jobResult.save(failOnError: true)

		eventResultUncached = new EventResult(
				numberOfWptRun: 1,
				cachedView: CachedView.UNCACHED,
				medianValue: true,
				wptStatus:200,
				docCompleteTimeInMillisecs: 10,
				domTimeInMillisecs: 20,
				customerSatisfactionInPercent: 0,
				jobResult: jobResult2,
				jobResultDate: jobResult2.date,
				jobResultJobConfigId: jobResult2.job.ident(),
				measuredEvent: measuredEvent,
				tag:'2;1;1;1;1',
				speedIndex: EventResult.SPEED_INDEX_DEFAULT_VALUE
				).save(failOnError: true)
		
		jobResult2.save(failOnError: true)
	}

	@Test
	void testGetEventResultDashboardChartMap_RAW_DATA_CACHED() {
		
		mockEventResultDaoService()
		mockPerformanceLoggingService()
		mockJobGroupDaoService()
		mockPageDaoService()
		mockMeasuredEventDaoService()
		mockBrowserDaoService()
		mockLocationDaoService()
		mockMeasuredValueTagService()
		
		ErQueryParams queryParams=new ErQueryParams();
		queryParams.browserIds.add(browser.id)
		queryParams.jobGroupIds.add(jobGroup.id)
		queryParams.locationIds.add(location.id)
		queryParams.measuredEventIds.add(measuredEvent.id)
		queryParams.pageIds.add(page.id)

		Date startTime=runDate.withMinuteOfHour(0).withSecondOfMinute(0).toDate()
		Date endTime=runDate.withMinuteOfHour(15).withSecondOfMinute(35).toDate()

		Collection<AggregatorType> aggregatorTypes=AggregatorType.findAllByName(AggregatorType.RESULT_CACHED_DOM_TIME) as List
		List<OsmChartGraph> resultGraphs = serviceUnderTest.getEventResultDashboardHighchartGraphs(startTime, endTime, MeasuredValueInterval.RAW, aggregatorTypes, queryParams);
		
		assertEquals(1, resultGraphs.size())
		List<OsmChartGraph> resultGraphsWithCorrectLabel = resultGraphs.findAll{
			it.label == "${AggregatorType.RESULT_CACHED_DOM_TIME} | ${group1Name} | ${event1Name} | ${location1Location}"}
		assertEquals(1, resultGraphsWithCorrectLabel.size())
		assertEquals(1, resultGraphsWithCorrectLabel.findAll{it.measurandGroup == MeasurandGroup.LOAD_TIMES}.size())
		assertTrue(resultGraphsWithCorrectLabel[0].points.findAll({ it.measuredValue == 2.0d }).size() == 1);
	}

	@Test
	void testGetEventResultDashboardChartMap_RAW_DATA_CACHED_AND_UNCACHED() {
		
		mockEventResultDaoService()
		mockPerformanceLoggingService()
		mockJobGroupDaoService()
		mockPageDaoService()
		mockMeasuredEventDaoService()
		mockBrowserDaoService()
		mockLocationDaoService()
		mockMeasuredValueTagService()
		
		ErQueryParams queryParams=new ErQueryParams();
		queryParams.browserIds.add(browser.id)
		queryParams.jobGroupIds.add(jobGroup.id)
		queryParams.locationIds.add(location.id)
		queryParams.measuredEventIds.add(measuredEvent.id)
		queryParams.pageIds.add(page.id)

		Date startTime=runDate.withMinuteOfHour(0).withSecondOfMinute(0).toDate()
		Date endTime=runDate.withMinuteOfHour(15).withSecondOfMinute(35).toDate()

		Collection<AggregatorType> aggregatorTypes=[]
		aggregatorTypes.add(AggregatorType.findAllByName(AggregatorType.RESULT_CACHED_DOM_TIME))
		aggregatorTypes.add(AggregatorType.findAllByName(AggregatorType.RESULT_UNCACHED_DOM_TIME))

		assertEquals(2, aggregatorTypes.size());

		List<OsmChartGraph> resultGraphs = serviceUnderTest.getEventResultDashboardHighchartGraphs(startTime, endTime, MeasuredValueInterval.RAW, aggregatorTypes, queryParams);
		
		assertEquals(2, resultGraphs.size())
		
		List<OsmChartGraph> resultsCsi1 = resultGraphs.findAll{
			it.label == "${AggregatorType.RESULT_CACHED_DOM_TIME} | ${group1Name} | ${event1Name} | ${location1Location}"}
		assertEquals(1, resultsCsi1.size())
		assertEquals(1, resultsCsi1.findAll{it.measurandGroup == MeasurandGroup.LOAD_TIMES}.size())
		assertTrue(resultsCsi1[0].points.findAll({ it.measuredValue == 2.0d }).size() == 1);
		
		List<OsmChartGraph> resultsCsi2 = resultGraphs.findAll{
			it.label == "${AggregatorType.RESULT_UNCACHED_DOM_TIME} | ${group2Name} | ${event1Name} | ${location1Location}"}
		assertEquals(1, resultsCsi2.size())
		assertEquals(1, resultsCsi2.findAll{it.measurandGroup == MeasurandGroup.LOAD_TIMES}.size())
		assertTrue(resultsCsi2[0].points.findAll({ it.measuredValue == 20.0d }).size() == 1);
	}
	
	@Test
	void testGetEventResultDashboardChartMap_RAW_DATA_CACHED_AND_UNCACHED_LIMITED_MIN() {
		
		mockEventResultDaoService()
		mockPerformanceLoggingService()
		mockJobGroupDaoService()
		mockPageDaoService()
		mockMeasuredEventDaoService()
		mockBrowserDaoService()
		mockLocationDaoService()
		mockMeasuredValueTagService()
		
		ErQueryParams queryParams=new ErQueryParams();
		queryParams.browserIds.add(browser.id)
		queryParams.jobGroupIds.add(jobGroup.id)
		queryParams.locationIds.add(location.id)
		queryParams.measuredEventIds.add(measuredEvent.id)
		queryParams.pageIds.add(page.id)
		queryParams.minLoadTimeInMillisecs = 5.0d

		Date startTime=runDate.withMinuteOfHour(0).withSecondOfMinute(0).toDate()
		Date endTime=runDate.withMinuteOfHour(15).withSecondOfMinute(35).toDate()

		Collection<AggregatorType> aggregatorTypes=[]
//		aggregatorTypes.add(AggregatorType.findAllByName(AggregatorType.RESULT_CACHED_DOM_TIME))
		aggregatorTypes.add(AggregatorType.findAllByName(AggregatorType.RESULT_UNCACHED_DOM_TIME))

		assertEquals(1, aggregatorTypes.size());

		List<OsmChartGraph> resultGraphs = serviceUnderTest.getEventResultDashboardHighchartGraphs(startTime, endTime, MeasuredValueInterval.RAW, aggregatorTypes, queryParams);

		assertEquals(1, resultGraphs.size())
		
		List<OsmChartGraph> resultsCsi1 = resultGraphs.findAll{
			it.label == "${AggregatorType.RESULT_UNCACHED_DOM_TIME} | ${group1Name} | ${event1Name} | ${location1Location}"}
		assertEquals(0, resultsCsi1.size())
		
		List<OsmChartGraph> resultsCsi2 = resultGraphs.findAll{
			it.label == "${AggregatorType.RESULT_UNCACHED_DOM_TIME} | ${group2Name} | ${event1Name} | ${location1Location}"}
		assertEquals(1, resultsCsi2.size())
		assertEquals(1, resultsCsi2.findAll{it.measurandGroup == MeasurandGroup.LOAD_TIMES}.size())
		assertTrue(resultsCsi2[0].points.findAll({ it.measuredValue == 20.0d }).size() == 1);
	}
	
	@Test
	void testGetEventResultDashboardChartMap_RAW_DATA_CACHED_AND_UNCACHED_LIMITED_MAX() {
		
		mockEventResultDaoService()
		mockPerformanceLoggingService()
		mockJobGroupDaoService()
		mockPageDaoService()
		mockMeasuredEventDaoService()
		mockBrowserDaoService()
		mockLocationDaoService()
		mockMeasuredValueTagService()
		
		ErQueryParams queryParams=new ErQueryParams();
		queryParams.browserIds.add(browser.id)
		queryParams.jobGroupIds.add(jobGroup.id)
		queryParams.locationIds.add(location.id)
		queryParams.measuredEventIds.add(measuredEvent.id)
		queryParams.pageIds.add(page.id)
		queryParams.maxLoadTimeInMillisecs = 15.0d

		Date startTime=runDate.withMinuteOfHour(0).withSecondOfMinute(0).toDate()
		Date endTime=runDate.withMinuteOfHour(15).withSecondOfMinute(35).toDate()

		Collection<AggregatorType> aggregatorTypes=[]
		aggregatorTypes.add(AggregatorType.findAllByName(AggregatorType.RESULT_CACHED_DOM_TIME))

		assertEquals(1, aggregatorTypes.size());

		List<OsmChartGraph> resultGraphs = serviceUnderTest.getEventResultDashboardHighchartGraphs(startTime, endTime, MeasuredValueInterval.RAW, aggregatorTypes, queryParams);
		
		assertEquals(1, resultGraphs.size())
		
		List<OsmChartGraph> resultsCsi1 = resultGraphs.findAll{
			it.label == "${AggregatorType.RESULT_CACHED_DOM_TIME} | ${group1Name} | ${event1Name} | ${location1Location}"}
		assertEquals(1, resultsCsi1.size())
		assertEquals(1, resultsCsi1.findAll{it.measurandGroup == MeasurandGroup.LOAD_TIMES}.size())
		assertTrue(resultsCsi1[0].points.findAll({ it.measuredValue == 2.0d }).size() == 1);
		
		List<OsmChartGraph> resultsCsi2 = resultGraphs.findAll{
			it.label == "${AggregatorType.RESULT_CACHED_DOM_TIME} | ${group2Name} | ${event1Name} | ${location1Location}"}
		assertEquals(0, resultsCsi2.size())
	}
	
	@Test
	void testGetEventResultDashboardChartMap_AGGREGATED_DATA_CACHED() {
		
		//mocks
		mockEventResultDaoService()
		mockPerformanceLoggingService()
		mockMeasuredValueUtilService()
		mockJobGroupDaoService()
		mockPageDaoService()
		mockMeasuredEventDaoService()
		mockBrowserDaoService()
		mockLocationDaoService()
		mockMeasuredValueTagService()
		mockAggregatorTypeDaoService()
		
		//test-specific data
		ErQueryParams queryParams=new ErQueryParams();
		queryParams.browserIds.add(browser.id)
		queryParams.jobGroupIds.add(jobGroup.id)
		queryParams.locationIds.add(location.id)
		queryParams.measuredEventIds.add(measuredEvent.id)
		queryParams.pageIds.add(page.id)

		Date startTime=runDate.minusDays(5).toDate()
		Date endTime=runDate.plusDays(5).toDate()

		Collection<AggregatorType> aggregatorTypes=AggregatorType.findAllByName(AggregatorType.RESULT_CACHED_DOM_TIME) as List
		
		//test-execution
		List<OsmChartGraph> resultGraphs = serviceUnderTest.getEventResultDashboardHighchartGraphs(startTime, endTime, MeasuredValueInterval.HOURLY, aggregatorTypes, queryParams);
		
		//assertions
		assertEquals(1, resultGraphs.size())
		List<OsmChartGraph> resultGraphsWithCorrectLabel = resultGraphs.findAll{
			it.label == "${AggregatorType.RESULT_CACHED_DOM_TIME} | ${group1Name} | ${event1Name} | ${location1Location}"}
		assertEquals(1, resultGraphsWithCorrectLabel.size())
		assertEquals(1, resultGraphsWithCorrectLabel.findAll{it.measurandGroup == MeasurandGroup.LOAD_TIMES}.size())
		assertTrue(resultGraphsWithCorrectLabel[0].points.findAll({ it.measuredValue == 2.0d }).size() == 1);
	}
	
	@Test
	void testGetEventResultDashboardChartMap_AGGREGATED_DATA_CACHED_AND_UNCACHED() {
		
		//mocks
		mockEventResultDaoService()
		mockPerformanceLoggingService()
		mockMeasuredValueUtilService()
		mockJobGroupDaoService()
		mockPageDaoService()
		mockMeasuredEventDaoService()
		mockBrowserDaoService()
		mockLocationDaoService()
		mockMeasuredValueTagService()
		mockAggregatorTypeDaoService()
		
		//test-specific data
		ErQueryParams queryParams=new ErQueryParams();
		queryParams.browserIds.add(browser.id)
		queryParams.jobGroupIds.add(jobGroup.id)
		queryParams.locationIds.add(location.id)
		queryParams.measuredEventIds.add(measuredEvent.id)
		queryParams.pageIds.add(page.id)

		Date startTime=runDate.minusDays(5).toDate()
		Date endTime=runDate.plusDays(5).toDate()

		Collection<AggregatorType> aggregatorTypes=[]
		aggregatorTypes.add(AggregatorType.findAllByName(AggregatorType.RESULT_CACHED_DOM_TIME))
		aggregatorTypes.add(AggregatorType.findAllByName(AggregatorType.RESULT_UNCACHED_DOM_TIME))
		assertEquals(2, aggregatorTypes.size());
		
		//test-execution
		List<OsmChartGraph> resultGraphs = serviceUnderTest.getEventResultDashboardHighchartGraphs(startTime, endTime, MeasuredValueInterval.HOURLY, aggregatorTypes, queryParams);
		
		//assertions
		assertEquals(2, resultGraphs.size())
		
		List<OsmChartGraph> resultsCsi1 = resultGraphs.findAll{
			it.label == "${AggregatorType.RESULT_CACHED_DOM_TIME} | ${group1Name} | ${event1Name} | ${location1Location}"}
		assertEquals(1, resultsCsi1.size())
		assertEquals(1, resultsCsi1.findAll{it.measurandGroup == MeasurandGroup.LOAD_TIMES}.size())
		assertTrue(resultsCsi1[0].points.findAll({ it.measuredValue == 2.0d }).size() == 1);
		
		List<OsmChartGraph> resultsCsi2 = resultGraphs.findAll{
			it.label == "${AggregatorType.RESULT_UNCACHED_DOM_TIME} | ${group2Name} | ${event1Name} | ${location1Location}"}
		assertEquals(1, resultsCsi2.size())
		assertEquals(1, resultsCsi2.findAll{it.measurandGroup == MeasurandGroup.LOAD_TIMES}.size())
		assertTrue(resultsCsi2[0].points.findAll({ it.measuredValue == 20.0d }).size() == 1);
	}

	public void testTryToBuildTestsDetailsURL_OneSingleResult() {
		tryToBuildTestsDetailsURL('1', 1, new URL('http://wptserver.example.com/result/testTryToBuildTestsDetailsURL_OneSingleResult'));
	}

	public void testTryToBuildTestsDetailsURL_TwoResults()
	{
		tryToBuildTestsDetailsURL('1,2', 2, new URL('http://wptserver.example.com/testTryToBuildTestsDetailsURL_TwoResults'));
	}
	
	private void tryToBuildTestsDetailsURL(String resultIDs, Integer resultIDsCount, final URL expectedURL) {
		// Create some data:
		final MeasuredValue measuredValue = new MeasuredValue(
				resultIds: resultIDs
				) {
					public Long getId() {
						return 4;
					}
				};
		assertEquals(resultIDsCount, measuredValue.countResultIds());

		// Simulate GrailsLinkGenerator
		LinkGenerator grailsLinkGeneratorMock = Mockito.mock(LinkGenerator.class);
		Mockito.when(grailsLinkGeneratorMock.link(Mockito.any(Map.class))).thenReturn(expectedURL.toString());

		// Inject relevant services
		serviceUnderTest.grailsLinkGenerator = grailsLinkGeneratorMock;

		// Run the (whitebox-)test:
		URL result = serviceUnderTest.tryToBuildTestsDetailsURL(measuredValue);

		// Verify result:
		// - is the URL the expected one?
		assertEquals(expectedURL, result)

		// - was the link requested properly?
		ArgumentCaptor<Map> argument = ArgumentCaptor.forClass(Map.class);
		Mockito.verify(grailsLinkGeneratorMock).link(argument.capture());
		Map linkRequest = argument.getValue();

		assertEquals('highchartPointDetails', linkRequest.get('controller'));
		assertEquals('listAggregatedResults', linkRequest.get('action'));
		assertEquals(true, linkRequest.get('absolute'));

		Object paramsEntry = linkRequest.get('params')
		assertNotNull(paramsEntry);
		assertTrue(paramsEntry instanceof Map);

		Map paramsMap = (Map)paramsEntry;
		assertEquals('4', paramsMap.get('measuredValueId'));
		assertEquals(String.valueOf(resultIDsCount), paramsMap.get('lastKnownCountOfAggregatedResultsOrNull'));
	}
	
	
	//mocking inner services////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * Mocks {@linkplain EventMeasuredValueService#eventResultDaoService}.
	 */
	private void mockEventResultDaoService(){
		def eventResultDaoService = mockFor(EventResultDaoService, true)
		eventResultDaoService.demand.getLimitedMedianEventResultsBy(1..10000) {
			Date fromDate, Date toDate, Set<CachedView> cachedViews, ErQueryParams erQueryParams, Map<String, Number> gtConstraints, Map<String, Number> ltConstraints ->
				List<EventResult> results = []
				if(cachedViews.contains(CachedView.CACHED)){ 
					if (!erQueryParams.minLoadTimeInMillisecs && !erQueryParams.maxLoadTimeInMillisecs) {
						results.add(eventResultCached)
					}else{
						if (erQueryParams.minLoadTimeInMillisecs && eventResultCached.domTimeInMillisecs > erQueryParams.minLoadTimeInMillisecs) {
							results.add(eventResultCached)
						}
						if (erQueryParams.maxLoadTimeInMillisecs && eventResultCached.domTimeInMillisecs < erQueryParams.maxLoadTimeInMillisecs) {
							results.add(eventResultCached)
						}
					}
				}
				if(cachedViews.contains(CachedView.UNCACHED)){ 
					if (!erQueryParams.minLoadTimeInMillisecs && !erQueryParams.maxLoadTimeInMillisecs) {
						results.add(eventResultUncached)
					}else{
						if (erQueryParams.minLoadTimeInMillisecs && eventResultUncached.domTimeInMillisecs > erQueryParams.minLoadTimeInMillisecs) {
							results.add(eventResultUncached)
						}
						if (erQueryParams.maxLoadTimeInMillisecs && eventResultUncached.domTimeInMillisecs < erQueryParams.maxLoadTimeInMillisecs) {
							results.add(eventResultUncached)
						}
					}
				}
				return results
		}
		eventResultDaoService.demand.tryToFindById(1..10000) {
			long databaseId ->
				return databaseId == 1?
					eventResultCached:
					eventResultUncached
		}
		serviceUnderTest.eventResultDaoService = eventResultDaoService.createMock()
	}
	/**
	 * Mocks {@linkplain EventMeasuredValueService#performanceLoggingService}.
	 */
	private void mockPerformanceLoggingService(){
		def performanceLoggingService = mockFor(PerformanceLoggingService, true)
		performanceLoggingService.demand.logExecutionTime(1..10000) {
			LogLevel level, String description, IndentationDepth indentation, Closure toMeasure ->
				toMeasure.call()
		}
		serviceUnderTest.performanceLoggingService = performanceLoggingService.createMock()
	}
	/**
	 * Mocks {@linkplain EventMeasuredValueService#performanceLoggingService}.
	 */
	private void mockMeasuredValueUtilService(){
		def measuredValueUtilService = mockFor(MeasuredValueUtilService, true)
		measuredValueUtilService.demand.resetToStartOfActualInterval(1..10000) {
			DateTime dateWithinInterval, Integer intervalInMinutes ->
			return runDateHourlyStart
		}
		serviceUnderTest.measuredValueUtilService = measuredValueUtilService.createMock()
	}
	/**
	 * Mocks {@linkplain EventMeasuredValueService#jobGroupDaoService}.
	 */
	private void mockJobGroupDaoService(){
		def jobGroupDaoService = mockFor(DefaultJobGroupDaoService, true)
		jobGroupDaoService.demand.getIdToObjectMap(1..10000) {->
			return [1:JobGroup.get(1), 2:JobGroup.get(2)]
		}
		serviceUnderTest.jobGroupDaoService = jobGroupDaoService.createMock()
	}
	/**
	 * Mocks {@linkplain EventMeasuredValueService#pageDaoService}.
	 */
	private void mockPageDaoService(){
		def pageDaoService = mockFor(DefaultPageDaoService, true)
		pageDaoService.demand.getIdToObjectMap(1..10000) {->
			return [1:Page.get(1), 2:Page.get(2)]
		}
		serviceUnderTest.pageDaoService = pageDaoService.createMock()
	}
	/**
	 * Mocks {@linkplain EventMeasuredValueService#measuredEventDaoService}.
	 */
	private void mockMeasuredEventDaoService(){
		def measuredEventDaoService = mockFor(DefaultMeasuredEventDaoService, true)
		measuredEventDaoService.demand.getIdToObjectMap(1..10000) {->
			return [1:MeasuredEvent.get(1), 2:MeasuredEvent.get(2)]
		}
		serviceUnderTest.measuredEventDaoService = measuredEventDaoService.createMock()
	}
	/**
	 * Mocks {@linkplain EventMeasuredValueService#browserDaoService}.
	 */
	private void mockBrowserDaoService(){
		def browserDaoService = mockFor(DefaultBrowserDaoService, true)
		browserDaoService.demand.getIdToObjectMap(1..10000) {->
			return [1:Browser.get(1), 2:Browser.get(2)]
		}
		serviceUnderTest.browserDaoService = browserDaoService.createMock()
	}
	/**
	 * Mocks {@linkplain EventMeasuredValueService#locationDaoService}.
	 */
	private void mockLocationDaoService(){
		def locationDaoService = mockFor(DefaultLocationDaoService, true)
		locationDaoService.demand.getIdToObjectMap(1..10000) {->
			return [1:Location.get(1), 2:Location.get(2)]
		}
		serviceUnderTest.locationDaoService = locationDaoService.createMock()
	}
	/**
	 * Mocks {@linkplain EventMeasuredValueService#locationDaoService}.
	 */
	private void mockMeasuredValueTagService(){
		def measuredValueTagService = mockFor(MeasuredValueTagService, true)
		measuredValueTagService.demand.findJobGroupIdOfHourlyEventTag(1..10000) {
			String hourlyEventMvTag ->
			return Long.valueOf(hourlyEventMvTag.tokenize(';')[0]) as Serializable
		}
		measuredValueTagService.demand.findPageIdOfHourlyEventTag(1..10000) {
			String hourlyEventMvTag ->
			return Long.valueOf(hourlyEventMvTag.tokenize(';')[1]) as Serializable
		}
		measuredValueTagService.demand.findMeasuredEventIdOfHourlyEventTag(1..10000) {
			String hourlyEventMvTag ->
			return Long.valueOf(hourlyEventMvTag.tokenize(';')[2]) as Serializable
		}
		measuredValueTagService.demand.findBrowserIdOfHourlyEventTag(1..10000) {
			String hourlyEventMvTag ->
			return Long.valueOf(hourlyEventMvTag.tokenize(';')[3]) as Serializable
		}
		measuredValueTagService.demand.findLocationIdOfHourlyEventTag(1..10000) {
			String hourlyEventMvTag ->
			return Long.valueOf(hourlyEventMvTag.tokenize(';')[4]) as Serializable
		}
		serviceUnderTest.measuredValueTagService = measuredValueTagService.createMock()
	}
	private void mockI18nService(){
		def i18nService = mockFor(I18nService, true)
		i18nService.demand.msg(1..10000) {
			String msgKey, String defaultMessage, List objs ->
			return 	defaultMessage
		}
		serviceUnderTest.i18nService = i18nService.createMock()
	}
	
	private mockAggregatorTypeDaoService(){
		def aggregatorTypeDaoService = mockFor(AggregatorTypeDaoService, true)
		aggregatorTypeDaoService.demand.getNameToObjectMap(1..10000) { ->
			Map<String, AggregatorType> map = [
				(AggregatorType.RESULT_CACHED_DOM_TIME): AggregatorType.findByName(AggregatorType.RESULT_CACHED_DOM_TIME),
				(AggregatorType.RESULT_UNCACHED_DOM_TIME): AggregatorType.findByName(AggregatorType.RESULT_UNCACHED_DOM_TIME)
				]
			return map
		}
		serviceUnderTest.aggregatorTypeDaoService = aggregatorTypeDaoService.createMock()
	}
}
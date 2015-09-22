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

import static de.iteratec.osm.util.Constants.*
import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertNotNull
import static org.junit.Assert.assertTrue
import grails.test.mixin.*

import org.apache.commons.lang.time.DateUtils
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.Interval
import org.junit.*

import de.iteratec.osm.report.chart.OsmChartGraph
import de.iteratec.osm.report.chart.OsmChartPoint
import de.iteratec.osm.report.chart.MeasuredValueUtilService
import de.iteratec.osm.measurement.schedule.Job
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.report.chart.AggregatorType
import de.iteratec.osm.report.chart.MeasuredValue
import de.iteratec.osm.report.chart.MeasuredValueInterval
import de.iteratec.osm.result.EventResultDashboardService
import de.iteratec.osm.result.MeasuredEvent
import de.iteratec.osm.result.MvQueryParams
import de.iteratec.osm.measurement.script.Script
import de.iteratec.osm.measurement.environment.Browser
import de.iteratec.osm.measurement.environment.Location
import de.iteratec.osm.measurement.environment.WebPageTestServer
import de.iteratec.osm.util.ServiceMocker

/**
 * Test-suite of {@link CustomerSatisfactionHighChartService}.
 */
@TestFor(CustomerSatisfactionHighChartService)
@Mock([AggregatorType, MeasuredValue, MeasuredValueInterval, Page, Job, CsTargetValue, CsTargetGraph, JobGroup, MeasuredEvent, Browser, Location,
	Script, WebPageTestServer])
class CustomerSatisfactionHighChartServiceTests {

	MeasuredValueInterval hourly
	MeasuredValueInterval weekly

	AggregatorType measured_event
	AggregatorType page
	AggregatorType shop

	Date now
	Date tomorrow
	Date fourMonthsAgo

	/**
	 * Contains one {@link MeasuredValue} for each existing combination of the following domain-objects
	 * (2 objects of each got created as test data before test-execution):
	 * <ul>
	 * <li>{@link JobGroup}</li>
	 * <li>{@link Page}</li>
	 * <li>{@link MeasuredEvent}</li>
	 * <li>{@link Browser}</li>
	 * <li>{@link Location}</li>
	 * </ul>
	 * So it contains 2EXP5=32 {@link MeasuredValue}s with respective tags.<br>
	 * <em>Note:</em>
	 * The id's of these domains are concatenated to the tag of hourly event-{@link MeasuredValue}s.
	 * @see #createMeasuredValues()
	 * @see MeasuredValue#tag
	 */
	List<MeasuredValue> measuredValueForEventHourlyList = []
	List<MeasuredValue> measuredValueForPageWeeklyList = []
	List<MeasuredValue> measuredValueForShopWeeklyList = []
	List<MeasuredValue> measuredValueForShopWeeklyWithNullList = []
	List<MeasuredValue> measuredValueListWithValuesLowerThanOne = []
	List<MeasuredValue> measuredValueListWithValuesLowerThanOneAndWithMoreThanTwoDecimalPlaces = []

	List<String> expectedJobLabels = ['job1', 'job2']

	List<String>  expectedJobGroupNames = ['group1', 'group2']
	List<String>  expectedPageNames = ['page1', 'page2']
	List<String>  expectedMeasuredEventNames = ['event1', 'event2']
	List<String>  expectedBrowserNames = ['browser1', 'browser2']
	List<String>  expectedLocationNames = ['location1', 'location2']

	String graphLabel
	Double tolerableDeviationDueToRounding

	CustomerSatisfactionHighChartService serviceUnderTest
	ServiceMocker mockGenerator

	@Before
	void setUp() {
		serviceUnderTest=service

		// We simply a modified version of the original service here, because
		// we expect only URL generation to be called and expect that
		// the service under test don't care about the URL itself.
		serviceUnderTest.eventResultDashboardService = new EventResultDashboardService() {
					@Override
					public URL tryToBuildTestsDetailsURL(MeasuredValue mv) {
						return new URL('http://measuredvalue.example.com/'+mv.id);
					}
				}

		createDataCommonForAllTests()
		
		//mocks common for all tests
		serviceUnderTest.measuredValueUtilService = new MeasuredValueUtilService()
		mockGenerator = ServiceMocker.create()
		mockGenerator.mockCsTargetGraphDaoService(serviceUnderTest, graphLabel)
		mockGenerator.mockLinkGenerator(serviceUnderTest, 'http://www.iteratec.de')
		mockGenerator.mockMeasuredValueTagService(
			serviceUnderTest,
			['1' : new JobGroup(id: 1, name: expectedJobGroupNames[0]), '2' : new JobGroup(id: 2, name: expectedJobGroupNames[1])],
			['1' : new MeasuredEvent(id: 1, name: expectedMeasuredEventNames[0]), '2' : new MeasuredEvent(id: 2, name: expectedMeasuredEventNames[1])],
			['1' : new Page(id: 1, name: expectedPageNames[0]), '2' : new Page(id: 2, name: expectedPageNames[1])],
			['1' : new Browser(id: 1, name: expectedBrowserNames[0]), '2' : new Browser(id: 2, name: expectedBrowserNames[1])],
			['1' : new Location(id: 1, location: expectedLocationNames[0]), '2' : new Location(location: expectedLocationNames[1])]
		)

	}

	@Test
	void testGetGraphLabelForHourlyEventMvGraph(){

		//create test-specific data

		MeasuredValue hemv12121 = new MeasuredValue(
				aggregator: new AggregatorType(name: AggregatorType.MEASURED_EVENT),
				tag: "1;2;1;2;1")
		MeasuredValue hemv11111 = new MeasuredValue(
				aggregator: new AggregatorType(name: AggregatorType.MEASURED_EVENT),
				tag: "1;1;1;1;1")
		MeasuredValue hemv22222 = new MeasuredValue(
				aggregator: new AggregatorType(name: AggregatorType.MEASURED_EVENT),
				tag: "2;2;2;2;2")

		//run tests and assertions

		String calculatedLabel12121 = serviceUnderTest.getMapLabel(hemv12121)
		String expectedLabel12121 =
				"${expectedJobGroupNames[0]}${HIGHCHART_LEGEND_DELIMITTER}"+
//				"${expectedPageNames[0]}${UNIQUE_STRING_DELIMITTER}"+
				"${expectedMeasuredEventNames[1]}${HIGHCHART_LEGEND_DELIMITTER}"+
//				"${expectedBrowserNames[1]}${UNIQUE_STRING_DELIMITTER}"+
				"${expectedLocationNames[0]}"
		assertEquals(expectedLabel12121, calculatedLabel12121)

		String calculatedLabel11111 = serviceUnderTest.getMapLabel(hemv11111)
		String expectedLabel11111 =
				"${expectedJobGroupNames[0]}${HIGHCHART_LEGEND_DELIMITTER}"+
//				"${expectedPageNames[0]}${UNIQUE_STRING_DELIMITTER}"+
				"${expectedMeasuredEventNames[0]}${HIGHCHART_LEGEND_DELIMITTER}"+
//				"${expectedBrowserNames[0]}${UNIQUE_STRING_DELIMITTER}"+
				"${expectedLocationNames[0]}"
		assertEquals(expectedLabel11111, calculatedLabel11111)

		String calculatedLabel22222 = serviceUnderTest.getMapLabel(hemv22222)
		String expectedLabel22222 =
				"${expectedJobGroupNames[1]}${HIGHCHART_LEGEND_DELIMITTER}"+
//				"${expectedPageNames[1]}${UNIQUE_STRING_DELIMITTER}"+
				"${expectedMeasuredEventNames[1]}${HIGHCHART_LEGEND_DELIMITTER}"+
//				"${expectedBrowserNames[1]}${UNIQUE_STRING_DELIMITTER}"+
				"${expectedLocationNames[1]}"
		assertEquals(expectedLabel22222, calculatedLabel22222)
	}

	@Test
	void testGetGraphLabelForWeeklyPageMvGraph(){

		//create test-specific data

		MeasuredValue wpmv11 = new MeasuredValue(
				aggregator: new AggregatorType(name: AggregatorType.PAGE),
				interval: weekly,
				tag: "1;1")
		MeasuredValue wpmv12 = new MeasuredValue(
				aggregator: new AggregatorType(name: AggregatorType.PAGE),
				interval: weekly,
				tag: "1;2")
		MeasuredValue wpmv22 = new MeasuredValue(
				aggregator: new AggregatorType(name: AggregatorType.PAGE),
				interval: weekly,
				tag: "2;2")

		//run tests and assertions

		String calculatedLabel11 = serviceUnderTest.getMapLabel(wpmv11)
		String expectedLabel11 =
				"${expectedJobGroupNames[0]}${HIGHCHART_LEGEND_DELIMITTER}${expectedPageNames[0]}"
		assertEquals(expectedLabel11, calculatedLabel11)

		String calculatedLabel12 = serviceUnderTest.getMapLabel(wpmv12)
		String expectedLabel12 =
				"${expectedJobGroupNames[0]}${HIGHCHART_LEGEND_DELIMITTER}${expectedPageNames[1]}"
		assertEquals(expectedLabel12, calculatedLabel12)

		String calculatedLabel22 = serviceUnderTest.getMapLabel(wpmv22)
		String expectedLabel22 =
				"${expectedJobGroupNames[1]}${HIGHCHART_LEGEND_DELIMITTER}${expectedPageNames[1]}"
		assertEquals(expectedLabel22, calculatedLabel22)
	}

	@Test
	void testGetGraphLabelForWeeklyShopMvGraph(){

		//create test-specific data

		MeasuredValue wsmv1 = new MeasuredValue(
				aggregator: new AggregatorType(name: AggregatorType.SHOP),
				interval: weekly,
				tag: "1")
		MeasuredValue wsmv2 = new MeasuredValue(
				aggregator: new AggregatorType(name: AggregatorType.SHOP),
				interval: weekly,
				tag: "2")

		//run tests and assertions

		String calculatedLabel1 = serviceUnderTest.getMapLabel(wsmv1)
		String expectedLabel1 = "${expectedJobGroupNames[0]}"
		assertEquals(expectedLabel1, calculatedLabel1)

		String calculatedLabel2 = serviceUnderTest.getMapLabel(wsmv2)
		String expectedLabel2 = "${expectedJobGroupNames[1]}"
		assertEquals(expectedLabel2, calculatedLabel2)

	}

	@Test
	void testGetOrCalculateCustomerSatisfactionMeasuredValuesAsHighChartMapForEventAggregator() {

		//create test-specific data

		MvQueryParams irrelevantQueryParamsCauseUsingFunctionalityIsMocked = new MvQueryParams()

		//mock inner service

		mockGenerator.mockEventMeasuredValueService(serviceUnderTest, measuredValueForEventHourlyList)

		//run test

		List<OsmChartGraph> result = serviceUnderTest.getCalculatedHourlyEventMeasuredValuesAsHighChartMap(
				now, tomorrow, irrelevantQueryParamsCauseUsingFunctionalityIsMocked)

		//assertions

		Integer numberOfExistingCombinations_JobgroupPageMeasuredeventBrowserLocation = 32
		assertNotNull(result)
		assertEquals(numberOfExistingCombinations_JobgroupPageMeasuredeventBrowserLocation, result.size())

	}

	@Test
	void testGetOrCalculateCustomerSatisfactionMeasuredValuesAsHighChartMapForPageAggregator() {

		//create test-specific data

		Integer numberOfGroupPageCombinations = 2
		Integer numberOfValuesInGraphOfGroupPageCombination_11 = 3
		Integer numberOfValuesInGraphOfGroupPageCombination_12 = 2
		String expectedGraphLabelOfGroupPageCombination_11 = "${expectedPageNames[0]}"
		String expectedGraphLabelOfGroupPageCombination_12 = "${expectedPageNames[1]}"

		//mock inner service

		mockGenerator.mockPageMeasuredValueService(serviceUnderTest, measuredValueForPageWeeklyList)

		
		
		//run test
		MeasuredValueInterval mvInterval=MeasuredValueInterval.findByIntervalInMinutes(MeasuredValueInterval.WEEKLY);
		List<OsmChartGraph> result = serviceUnderTest.getCalculatedPageMeasuredValuesAsHighChartMap(new Interval(now.getTime(), tomorrow.getTime()), new MvQueryParams(), mvInterval)

		//assertions

		assertNotNull(result)
		assertEquals(numberOfGroupPageCombinations, result.size())

		OsmChartGraph graphUnderTest=findGraphByLabel(result, expectedGraphLabelOfGroupPageCombination_11);
		
		assertEquals(numberOfValuesInGraphOfGroupPageCombination_11, graphUnderTest.getPoints().size())
		long lastTime = 0;

		List<OsmChartPoint> points = graphUnderTest.getPoints();
		for (OsmChartPoint eachPoint : points) {
			assertTrue (lastTime < eachPoint.time)
			lastTime = eachPoint.time
		}

		Long timestampKeyEarlierEveryMeasuredValue = -1;
		lastTime = timestampKeyEarlierEveryMeasuredValue;
		
		
		graphUnderTest=findGraphByLabel(result, expectedGraphLabelOfGroupPageCombination_12);
		assertEquals(numberOfValuesInGraphOfGroupPageCombination_12, graphUnderTest.getPoints().size())

		points = graphUnderTest.getPoints();
		for (OsmChartPoint eachPoint : points) {
			assertTrue (lastTime < eachPoint.time)
			lastTime = eachPoint.time
		}
	}

	@Test
	void testGetOrCalculateCustomerSatisfactionMeasuredValuesAsHighChartMapForShopAggregator() {

		//create test-specific data

		String expectedLabel = expectedJobGroupNames[0]

		//mock inner service

		mockGenerator.mockShopMeasuredValueService(serviceUnderTest, measuredValueForShopWeeklyList)

		//run test
		MeasuredValueInterval mvInterval=MeasuredValueInterval.findByIntervalInMinutes(MeasuredValueInterval.WEEKLY);
		List<OsmChartGraph> result = serviceUnderTest.getCalculatedShopMeasuredValuesAsHighChartMap(new Interval(now.getTime(), tomorrow.getTime()), mvInterval, new MvQueryParams())

		//assertions

		assertNotNull(result)
		assertEquals(1, result.size())
		
		OsmChartGraph graphUnderTest=findGraphByLabel(result, expectedLabel);
		
		assertEquals(measuredValueForShopWeeklyList.size(), graphUnderTest.getPoints().size())

		long lastTime = 0
		List<OsmChartPoint> points = graphUnderTest.getPoints();
		for (OsmChartPoint eachPoint : points) {
			assertTrue(eachPoint.measuredValue >= 0)
			assertTrue (lastTime < eachPoint.time)
			lastTime = eachPoint.time
		}
	}

	@Test
	void testGetOrCalculateCustomerSatisfactionMeasuredValuesAsHighChartMapForShopAggregatorWithNullInDataSource() {

		//create test-specific data

		String expectedLabel = expectedJobGroupNames[0]

		//mock inner service

		mockGenerator.mockShopMeasuredValueService(serviceUnderTest, measuredValueForShopWeeklyWithNullList)

		//run test
		
		MeasuredValueInterval mvInterval=MeasuredValueInterval.findByIntervalInMinutes(MeasuredValueInterval.WEEKLY);
		List<OsmChartGraph> result = serviceUnderTest.getCalculatedShopMeasuredValuesAsHighChartMap(new Interval(now.getTime(), tomorrow.getTime()), mvInterval, new MvQueryParams())

		//assertions

		assertNotNull(result)
		assertEquals(1, result.size())

		//measuredValueForShopHourlyWithNullList.size() - 1 because value of one element is null
		
		OsmChartGraph graphUnderTest=findGraphByLabel(result, expectedLabel);
		assertEquals(measuredValueForShopWeeklyWithNullList.size() - 1, graphUnderTest.getPoints().size())

		long lastTime = 0
		List<OsmChartPoint> points = graphUnderTest.getPoints()
		for (OsmChartPoint eachPoint : points) {
			assertTrue(eachPoint.measuredValue >= 0)
			assertTrue (lastTime < eachPoint.time)
			lastTime = eachPoint.time
		}
	}

	@Test
	void testGetOrCalculateCustomerSatisfactionMeasuredValuesAsHighChartMapForShopAggregatorWithValuesLowerThanOneInDataSource() {

		//create test-specific data

		String expectedLabel = expectedJobGroupNames[0]

		//mock inner service

		mockGenerator.mockShopMeasuredValueService(serviceUnderTest, measuredValueForShopWeeklyList)

		//run test
		
		MeasuredValueInterval mvInterval=MeasuredValueInterval.findByIntervalInMinutes(MeasuredValueInterval.WEEKLY);
		List<OsmChartGraph> result = serviceUnderTest.getCalculatedShopMeasuredValuesAsHighChartMap(new Interval(now.getTime(), tomorrow.getTime()), mvInterval, new MvQueryParams())

		//assertions
		assertNotNull(result)
		assertEquals(1, result.size())
		
		OsmChartGraph graphUnderTest=findGraphByLabel(result, expectedLabel);
		assertEquals(measuredValueListWithValuesLowerThanOne.size(), graphUnderTest.getPoints().size())

		long lastTime = 0
		List<OsmChartPoint> points = graphUnderTest.getPoints()
		for (OsmChartPoint eachPoint : points) {
			assertTrue(eachPoint.measuredValue > 1)
			assertTrue (lastTime < eachPoint.time)
			lastTime = eachPoint.time
		}
	}

	@Test
	void testGetOrCalculateCustomerSatisfactionMeasuredValuesAsHighChartMapForShopAggregatorWithValuesLowerThanOneAndMoreThanTwoDecimalPlacesInDataSource() {

		//create test-specific data

		String expectedLabel = expectedJobGroupNames[0]

		//mock inner service

		mockGenerator.mockShopMeasuredValueService(serviceUnderTest, measuredValueForShopWeeklyList)

		//run test
		
		MeasuredValueInterval mvInterval=MeasuredValueInterval.findByIntervalInMinutes(MeasuredValueInterval.WEEKLY);

		List<OsmChartGraph> result = serviceUnderTest.getCalculatedShopMeasuredValuesAsHighChartMap(new Interval(now.getTime(), tomorrow.getTime()), mvInterval, new MvQueryParams())

		//assertions

		assertNotNull(result)
		assertEquals(1,  result.size())
		OsmChartGraph graphUnderTest=findGraphByLabel(result, expectedLabel);
		assertEquals(measuredValueListWithValuesLowerThanOneAndWithMoreThanTwoDecimalPlaces.size(), graphUnderTest.getPoints().size())

		long lastTime = 0
		List<OsmChartPoint> points = graphUnderTest.getPoints()
		for (OsmChartPoint eachPoint : points) {
			assertTrue(eachPoint.measuredValue > 1)

			String[] splittedAtFractionPoint = String.valueOf(eachPoint.measuredValue).split("\\.");
			//			splits[0].length()   // Before Decimal Count
			//			splits[1].length()   // After  Deci
			assertTrue(splittedAtFractionPoint[1].length() <= 2)

			assertTrue (lastTime < eachPoint.time)
			lastTime = eachPoint.time
		}
	}

	@Test
	void testGetCsRelevantStaticGraphsAsResultMapForChart(){

		//create test-specific data

		DateTime fromDate = new DateTime(now).minusMonths(3)
		DateTime toDate = new DateTime(now).minusMonths(1)

		//run test

		List<OsmChartGraph> highcharts = serviceUnderTest.getCsRelevantStaticGraphsAsResultMapForChart(fromDate, toDate)

		//assertions

		assertEquals(1, highcharts.size())
		List<OsmChartPoint> points = findGraphByLabel(highcharts, graphLabel).getPoints();
		assertNotNull(points)
		assertEquals(2, points.size())

		OsmChartPoint threeMonthsAgo = points[0];
		assertEquals(
			new DateTime(fromDate, DateTimeZone.forID('MET')).getMillis(), 
			threeMonthsAgo.time);
		assertEquals(82.5, threeMonthsAgo.measuredValue, tolerableDeviationDueToRounding);

		OsmChartPoint oneMonthAgo = points[1];
		assertEquals(
			new DateTime(toDate, DateTimeZone.forID('MET')).getMillis(),
			oneMonthAgo.time);
		assertEquals(87.5, oneMonthAgo.measuredValue, tolerableDeviationDueToRounding);
	}

	//creating data common for all tests/////////////////////////////////////////////////////////////////////////////////////////////////////

	private void createDataCommonForAllTests(){
		// now = 17.07.2013 - 16:28:35
		now = new Date(1374071315000L)

		now = DateUtils.setMilliseconds(now, 0)
		now = DateUtils.setSeconds(now, 0)
		now = DateUtils.setMinutes(now, 0)
		now = DateUtils.setHours(now, 0)
		tomorrow = DateUtils.addDays(now, 1)
		fourMonthsAgo = DateUtils.addMonths(now, -4)
		tolerableDeviationDueToRounding = 0.2

		createAggregatorTypesAndIntervals()
		createJobs()
		createPages()
		createMeasuredValues()
		createCsTargetGraphs()

	}
	private void createAggregatorTypesAndIntervals(){
		hourly = new MeasuredValueInterval(
				intervalInMinutes: MeasuredValueInterval.HOURLY
				).save(failOnError: true, validate: false)
		weekly = new MeasuredValueInterval(
				intervalInMinutes: MeasuredValueInterval.WEEKLY
				).save(failOnError: true, validate: false)
		measured_event = new AggregatorType(
				name: AggregatorType.MEASURED_EVENT
				).save(failOnError: true, validate: false)
		page = new AggregatorType(
				name: AggregatorType.PAGE
				).save(failOnError: true, validate: false)
		shop = new AggregatorType(
				name: AggregatorType.SHOP
				).save(failOnError: true, validate: false)
	}
	private void createJobs(){
		new Job(
				label: expectedJobLabels[0]
				).save(failOnError: true, validate: false)
		new Job(
				label: expectedJobLabels[1]
				).save(failOnError: true, validate: false)
	}
	private void createPages(){
		new Page(
				name: expectedPageNames[0],
				weight: 1.0
				).save(failOnError: true)

		new Page(
				name: expectedPageNames[1],
				weight: 1.0
				).save(failOnError: true)
		assertNotNull( Page.findByName(expectedPageNames[0]))
		assertNotNull( Page.findByName(expectedPageNames[1]))
	}
	private void createMeasuredValues(){
		measuredValueForEventHourlyList.clear()
		2.times{zeroBasedIndexJobGroup ->
			2.times {zeroBasedIndexPage ->
				2.times {zeroBasedIndexMeasuredEvent ->
					2.times {zeroBasedIndexBrowser ->
						2.times {zeroBasedIndexLocation ->
							measuredValueForEventHourlyList.add(
									new MeasuredValue(
									started: now,
									interval: hourly,
									aggregator: measured_event,
									tag: "${zeroBasedIndexJobGroup+1};${zeroBasedIndexPage+1};${zeroBasedIndexMeasuredEvent+1};${zeroBasedIndexBrowser+1};${zeroBasedIndexLocation+1}",
									value: 85,
									resultIds: '1,2'
									))
						}}}}}

		measuredValueForPageWeeklyList = [
			new MeasuredValue(
			started: now,
			interval: hourly,
			aggregator: page,
			tag: "1;1",
			value: 85,
			resultIds: '1,2'
			),
			new MeasuredValue(
				started: DateUtils.addHours(now, 3),
				interval: hourly,
				aggregator: page,
				tag: "1;1",
				value: 83,
				resultIds: '5,6'
			),
			new MeasuredValue(
				started: DateUtils.addHours(now, 2),
				interval: hourly,
				aggregator: page,
				tag: "1;1",
				value: 79,
				resultIds: '3,4'
			),
			new MeasuredValue(
				started: DateUtils.addHours(now, 2),
				interval: hourly,
				aggregator: page,
				tag: "1;2",
				value: 65,
				resultIds: '7,8'
			),
			new MeasuredValue(
				started: now,
				interval: hourly,
				aggregator: page,
				tag: "1;2",
				value: 67,
				resultIds: '9,10'
			)
		]

		measuredValueForShopWeeklyList = [
			new MeasuredValue(
				started: now,
				interval: hourly,
				aggregator: shop,
				tag: '1',
				value: 85,
				resultIds: '1,2'
			),
			new MeasuredValue(
				started: DateUtils.addHours(now, 3),
				interval: hourly,
				aggregator: shop,
				tag: '1',
				value: 83,
				resultIds: '5,6'
			),
			new MeasuredValue(
				started: DateUtils.addHours(now, 2),
				interval: hourly,
				aggregator: shop,
				tag: '1',
				value: 79,
				resultIds: '3,4'
			)
		]

		measuredValueForShopWeeklyWithNullList = [
			new MeasuredValue(
				started: now,
				interval: weekly,
				aggregator: shop,
				tag: '1',
				value: null,
				resultIds: '1,2'
			),
			new MeasuredValue(
				started: DateUtils.addHours(now, 3),
				interval: weekly,
				aggregator: shop,
				tag: '1',
				value: 83,
				resultIds: '5,6'
			),
			new MeasuredValue(
				started: DateUtils.addHours(now, 2),
				interval: weekly,
				aggregator: shop,
				tag: '1',
				value: 79,
				resultIds: '3,4'
			)
		]

		measuredValueListWithValuesLowerThanOne = [
			new MeasuredValue(
				started: now,
				interval: weekly,
				aggregator: shop,
				tag: '1',
				value: 32,
				resultIds: '1,2'
			),
			new MeasuredValue(
			started: DateUtils.addHours(now, 3),
			interval: weekly,
			aggregator: shop,
			tag: '1',
			value: 83,
			resultIds: '5,6'
			),
			new MeasuredValue(
				started: DateUtils.addHours(now, 2),
				interval: weekly,
				aggregator: shop,
				tag: '1',
				value: 79,
				resultIds: '3,4'
			)
		]

		measuredValueListWithValuesLowerThanOneAndWithMoreThanTwoDecimalPlaces = [
			new MeasuredValue(
				started: now,
				interval: weekly,
				aggregator: shop,
				tag: '1',
				value: 32.4562,
				resultIds: '1,2'
			),
			new MeasuredValue(
				started: DateUtils.addHours(now, 3),
				interval: weekly,
				aggregator: shop,
				tag: '1',
				value: 83.12367,
				resultIds: '5,6'
			),
			new MeasuredValue(
				started: DateUtils.addHours(now, 2),
				interval: weekly,
				aggregator: shop,
				tag: '1',
				value: 79.0968,
				resultIds: '3,4'
			)
		]
	}
	private void createCsTargetGraphs(){
		CsTargetValue csTargetNow = new CsTargetValue(
				date: now,
				csInPercent: 90).save(failOnError: true)
		CsTargetValue csTargetTwoMonthsAgo = new CsTargetValue(
				date: fourMonthsAgo,
				csInPercent: 80).save(failOnError: true)
		graphLabel = 'myStaticGraph'
		new CsTargetGraph(
				label: graphLabel,
				defaultVisibility: true,
				pointOne: csTargetTwoMonthsAgo,
				pointTwo: csTargetNow).save(failOnError: true)
	}
	
	/**
	 * Finds a {@link de.iteratec.osm.report.chart.OsmChartGraph} by its label in a list.
	 *
	 * @param graphs List of {@link OsmChartGraph}s, not <code>null</code>
	 * @param label name of the OsmChartGraph
	 * @return Returns the last match or <code>null</code>!
	 */
	public static OsmChartGraph findGraphByLabel(List<OsmChartGraph> graphs, String label) {
		
		OsmChartGraph graph;
		
		graphs.each {
			if(it.getLabel()==label) {
				graph=it;
			}
		}
		
		return graph;
	}
}

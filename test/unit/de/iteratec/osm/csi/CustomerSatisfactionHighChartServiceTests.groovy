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

import de.iteratec.osm.measurement.environment.Browser
import de.iteratec.osm.measurement.environment.Location
import de.iteratec.osm.measurement.environment.WebPageTestServer
import de.iteratec.osm.measurement.schedule.Job
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.measurement.script.Script
import de.iteratec.osm.report.chart.*
import de.iteratec.osm.result.EventResultDashboardService
import de.iteratec.osm.result.MeasuredEvent
import de.iteratec.osm.result.MvQueryParams
import de.iteratec.osm.util.I18nService
import de.iteratec.osm.util.ServiceMocker
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import org.apache.commons.lang.time.DateUtils
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.Interval
import spock.lang.Shared
import spock.lang.Specification

import static de.iteratec.osm.util.Constants.HIGHCHART_LEGEND_DELIMITTER
import static org.junit.Assert.assertNotNull

/**
 * Test-suite of {@link CustomerSatisfactionHighChartService}.
 */
@TestFor(CustomerSatisfactionHighChartService)
@Mock([AggregatorType, CsiAggregation, MeasuredValueInterval, Page, Job, CsTargetValue, CsTargetGraph, JobGroup, MeasuredEvent, Browser, Location,
	Script, WebPageTestServer])
class CustomerSatisfactionHighChartServiceTests extends Specification{

    @Shared MeasuredValueInterval hourly
    @Shared MeasuredValueInterval weekly

    @Shared AggregatorType measured_event
    @Shared AggregatorType page
    @Shared AggregatorType shop

    @Shared Date now
    @Shared Date tomorrow
    @Shared Date fourMonthsAgo

	/**
	 * Contains one {@link CsiAggregation} for each existing combination of the following domain-objects
	 * (2 objects of each got created as test data before test-execution):
	 * <ul>
	 * <li>{@link JobGroup}</li>
	 * <li>{@link Page}</li>
	 * <li>{@link MeasuredEvent}</li>
	 * <li>{@link Browser}</li>
	 * <li>{@link Location}</li>
	 * </ul>
	 * So it contains 2EXP5=32 {@link CsiAggregation}s with respective tags.<br>
	 * <em>Note:</em>
	 * The id's of these domains are concatenated to the tag of hourly event-{@link CsiAggregation}s.
	 * @see #createMeasuredValues()
	 * @see CsiAggregation#tag
	 */
    @Shared List<CsiAggregation> measuredValueForEventHourlyList = []
    @Shared List<CsiAggregation> measuredValueForPageWeeklyList = []
    @Shared List<CsiAggregation> measuredValueForShopWeeklyList = []
    @Shared List<CsiAggregation> measuredValueForShopWeeklyWithNullList = []
    @Shared List<CsiAggregation> measuredValueListWithValuesLowerThanOne = []
    @Shared List<CsiAggregation> measuredValueListWithValuesLowerThanOneAndWithMoreThanTwoDecimalPlaces = []

    @Shared List<String> expectedJobLabels = ['job1', 'job2']

	@Shared List<String>  expectedJobGroupNames = ['group1', 'group2']
    @Shared List<String>  expectedPageNames = ['page1', 'page2']
    @Shared List<String>  expectedMeasuredEventNames = ['event1', 'event2']
    @Shared List<String>  expectedBrowserNames = ['browser1', 'browser2']
    @Shared List<String>  expectedLocationNames = ['location1', 'location2']

    @Shared String graphLabel
    @Shared Double tolerableDeviationDueToRounding

	CustomerSatisfactionHighChartService serviceUnderTest
	ServiceMocker mockGenerator

    public static final String I18N_LABEL_JOB_GROUP = 'Job Group'
    public static final String I18N_LABEL_MEASURED_EVENT = 'Measured step'
    public static final String I18N_LABEL_LOCATION = 'Location'
    public static final String I18N_LABEL_MEASURAND = 'Measurand'
    public static final String I18N_LABEL_CONNECTIVITY = 'Connectivity'


    void setup() {

		serviceUnderTest=service

		createDataCommonForAllTests()
		
		mockServicesCommonForAllTests()

	}

    void "correct graph labels get created for hourly event measured values"(){

        expect:
        serviceUnderTest.getMapLabel(mv) == expectedLabel

        where:
        mv << [
            new CsiAggregation(
                    aggregator: new AggregatorType(name: AggregatorType.MEASURED_EVENT),
                    tag: "1;2;1;2;1"),
            new CsiAggregation(
                    aggregator: new AggregatorType(name: AggregatorType.MEASURED_EVENT),
                    tag: "1;1;1;1;1"),
            new CsiAggregation(
                    aggregator: new AggregatorType(name: AggregatorType.MEASURED_EVENT),
                    tag: "2;2;2;2;2")
        ]
        expectedLabel << [
            "${expectedJobGroupNames[0]}${HIGHCHART_LEGEND_DELIMITTER}"+
                "${expectedMeasuredEventNames[1]}${HIGHCHART_LEGEND_DELIMITTER}"+
                "${expectedLocationNames[0]}",
            "${expectedJobGroupNames[0]}${HIGHCHART_LEGEND_DELIMITTER}"+
                    "${expectedMeasuredEventNames[0]}${HIGHCHART_LEGEND_DELIMITTER}"+
                    "${expectedLocationNames[0]}",
            "${expectedJobGroupNames[1]}${HIGHCHART_LEGEND_DELIMITTER}"+
                    "${expectedMeasuredEventNames[1]}${HIGHCHART_LEGEND_DELIMITTER}"+
                    "${expectedLocationNames[1]}"
        ]

	}

	void "correct graph labels get created for weekly page measured values"(){

        expect:
        serviceUnderTest.getMapLabel(mv) == expectedLabel

        where:
        mv << [
                new CsiAggregation(
                        aggregator: new AggregatorType(name: AggregatorType.PAGE),
                        interval: weekly,
                        tag: "1;1"),
                new CsiAggregation(
                        aggregator: new AggregatorType(name: AggregatorType.PAGE),
                        interval: weekly,
                        tag: "1;2"),
                new CsiAggregation(
                        aggregator: new AggregatorType(name: AggregatorType.PAGE),
                        interval: weekly,
                        tag: "2;2")
        ]
        expectedLabel << [
                "${expectedJobGroupNames[0]}${HIGHCHART_LEGEND_DELIMITTER}${expectedPageNames[0]}",
                "${expectedJobGroupNames[0]}${HIGHCHART_LEGEND_DELIMITTER}${expectedPageNames[1]}",
                "${expectedJobGroupNames[1]}${HIGHCHART_LEGEND_DELIMITTER}${expectedPageNames[1]}"
        ]

	}

	void "correct graph labels get created for weekly shop measured values"(){

        expect:
        serviceUnderTest.getMapLabel(mv) == expectedLabel

        where:
        mv << [
                new CsiAggregation(
                        aggregator: new AggregatorType(name: AggregatorType.SHOP),
                        interval: weekly,
                        tag: "1"),
                new CsiAggregation(
                        aggregator: new AggregatorType(name: AggregatorType.SHOP),
                        interval: weekly,
                        tag: "2")
        ]
        expectedLabel << [
                "${expectedJobGroupNames[0]}",
                "${expectedJobGroupNames[1]}"
        ]

	}

	void "build osm chart graphs correctly from hourly event measured values"() {

        setup:
		//create test-specific data
		MvQueryParams irrelevantQueryParamsCauseUsingFunctionalityIsMocked = new MvQueryParams()
		//mock inner service
		mockGenerator.mockEventMeasuredValueService(serviceUnderTest, measuredValueForEventHourlyList)
        Integer numberOfExistingCombinations_JobgroupPageMeasuredeventBrowserLocation = 32

        when:
		OsmRickshawChart chart = serviceUnderTest.getCalculatedHourlyEventMeasuredValuesAsHighChartMap(
				now, tomorrow, irrelevantQueryParamsCauseUsingFunctionalityIsMocked, CsiType.doc_complete
        )
        List<OsmChartGraph> graphs = chart.osmChartGraphs

        then:
		graphs != null
        graphs.size() == numberOfExistingCombinations_JobgroupPageMeasuredeventBrowserLocation

	}

	void "build osm chart graphs correctly from page measured values"() {

        setup:
		//create test-specific data
		Integer numberOfGroupPageCombinations = 2
		Integer numberOfValuesInGraphOfGroupPageCombination_11 = 3
		Integer numberOfValuesInGraphOfGroupPageCombination_12 = 2
		String expectedGraphLabelOfGroupPageCombination_11 = "${expectedPageNames[0]}"
		String expectedGraphLabelOfGroupPageCombination_12 = "${expectedPageNames[1]}"
        MeasuredValueInterval weekly = MeasuredValueInterval.findByIntervalInMinutes(MeasuredValueInterval.WEEKLY);
		//mock inner service
		mockGenerator.mockPageMeasuredValueService(serviceUnderTest, measuredValueForPageWeeklyList)

		when:
		OsmRickshawChart chart = serviceUnderTest.getCalculatedPageMeasuredValuesAsHighChartMap(
                new Interval(now.getTime(), tomorrow.getTime()),
                new MvQueryParams(),
                weekly,
				CsiType.doc_complete
        )
        List<OsmChartGraph> graphs = chart.osmChartGraphs
        List<OsmChartPoint> pointsGroupPageCombination_11 = findGraphByLabel(graphs, expectedGraphLabelOfGroupPageCombination_11).getPoints();
        List<Long> pointsTimesGroupPageCombination_11 = pointsGroupPageCombination_11*.time
        List<OsmChartPoint> pointsGroupPageCombination_12 = findGraphByLabel(graphs, expectedGraphLabelOfGroupPageCombination_12).getPoints();
        List<Long> pointsTimesGroupPageCombination_12 = pointsGroupPageCombination_12*.time


		then:
        graphs != null
        graphs.size() == numberOfGroupPageCombinations

        pointsGroupPageCombination_11.size() == numberOfValuesInGraphOfGroupPageCombination_11
        pointsTimesGroupPageCombination_11 == pointsTimesGroupPageCombination_11.sort()

        pointsGroupPageCombination_12.size() == numberOfValuesInGraphOfGroupPageCombination_12
        pointsTimesGroupPageCombination_12 == pointsTimesGroupPageCombination_12.sort()

	}

	void "build osm chart graphs correctly from shop measured values"() {

        setup:
		//create test-specific data
		String expectedLabel = expectedJobGroupNames[0]
        MeasuredValueInterval weekly = MeasuredValueInterval.findByIntervalInMinutes(MeasuredValueInterval.WEEKLY);
		//mock inner service
		mockGenerator.mockShopMeasuredValueService(serviceUnderTest, measuredValueForShopWeeklyList)

        when:
		OsmRickshawChart chart = serviceUnderTest.getCalculatedShopMeasuredValuesAsHighChartMap(
                new Interval(now.getTime(), tomorrow.getTime()),
                weekly,
                new MvQueryParams(),
				CsiType.doc_complete
        )
        List<OsmChartGraph> graphs = chart.osmChartGraphs
        List<OsmChartPoint> points = findGraphByLabel(graphs, expectedLabel).getPoints()
        List<Long> pointsTimes = points*.time

        then:
		graphs != null
		graphs.size() == 1

        points.size() == measuredValueForShopWeeklyList.size()
        points.every{point -> point.measuredValue > 0}
        pointsTimes == pointsTimes.sort()

	}

	void "build osm chart graphs correctly from shop measured values with null in data source"() {

        setup:
		//create test-specific data
		String expectedLabel = expectedJobGroupNames[0]
        MeasuredValueInterval weekly = MeasuredValueInterval.findByIntervalInMinutes(MeasuredValueInterval.WEEKLY);
		//mock inner service
		mockGenerator.mockShopMeasuredValueService(serviceUnderTest, measuredValueForShopWeeklyWithNullList)

		when:
		OsmRickshawChart chart = serviceUnderTest.getCalculatedShopMeasuredValuesAsHighChartMap(
                new Interval(now.getTime(), tomorrow.getTime()),
                weekly,
                new MvQueryParams(),
				CsiType.doc_complete
        )
        List<OsmChartGraph> graphs = chart.osmChartGraphs
        List<OsmChartPoint> points = findGraphByLabel(graphs, expectedLabel).getPoints()
        List<Long> pointsTimes = points*.time

		then:
		graphs != null
		graphs.size() == 1

        points.size() == measuredValueForShopWeeklyWithNullList.size() - 1
        points.every{point -> point.measuredValue > 0}
        pointsTimes == pointsTimes.sort()

	}

	void "build osm chart graphs correctly from shop measured values with values lower than one in data source"() {

        setup:
		//create test-specific data
		String expectedLabel = expectedJobGroupNames[0]
        MeasuredValueInterval weekly = MeasuredValueInterval.findByIntervalInMinutes(MeasuredValueInterval.WEEKLY);
		//mock inner service
		mockGenerator.mockShopMeasuredValueService(serviceUnderTest, measuredValueForShopWeeklyList)

		when:
		OsmRickshawChart chart = serviceUnderTest.getCalculatedShopMeasuredValuesAsHighChartMap(
                new Interval(now.getTime(), tomorrow.getTime()),
                weekly,
                new MvQueryParams(),
				CsiType.doc_complete
        )
        List<OsmChartGraph> graphs = chart.osmChartGraphs
        List<OsmChartPoint> points = findGraphByLabel(graphs, expectedLabel).getPoints()
        List<Long> pointsTimes = points*.time

		then:
        graphs != null
        graphs.size() == 1

        points.size() == measuredValueListWithValuesLowerThanOne.size()
        points.every{point -> point.measuredValue > 1}
        pointsTimes == pointsTimes.sort()

	}

	void "build osm chart graphs correctly from shop measured values with values lower than one and with more than two decimal places in data source"() {

        setup:
		//create test-specific data
		String expectedLabel = expectedJobGroupNames[0]
        MeasuredValueInterval weekly = MeasuredValueInterval.findByIntervalInMinutes(MeasuredValueInterval.WEEKLY);
		//mock inner service
		mockGenerator.mockShopMeasuredValueService(serviceUnderTest, measuredValueForShopWeeklyList)

		when:
		OsmRickshawChart chart = serviceUnderTest.getCalculatedShopMeasuredValuesAsHighChartMap(
                new Interval(now.getTime(), tomorrow.getTime()),
                weekly,
                new MvQueryParams(),
				CsiType.doc_complete
        )
        List<OsmChartGraph> graphs = chart.osmChartGraphs
        List<OsmChartPoint> points = findGraphByLabel(graphs, expectedLabel).getPoints()
        List<Long> pointsTimes = points*.time

		then:
        graphs != null
        graphs.size() == 1

        points.size() == measuredValueListWithValuesLowerThanOneAndWithMoreThanTwoDecimalPlaces.size()
        points.every{point ->
            point.measuredValue > 1 &&
                    String.valueOf(point.measuredValue).split("\\.")[1].length() <= 2
        }

        pointsTimes == pointsTimes.sort()

	}

	void "cs relevant static graphs get provided correctly"(){

        setup:
		//create test-specific data
		DateTime fromDate = new DateTime(now).minusMonths(3)
		DateTime toDate = new DateTime(now).minusMonths(1)

		when:
		List<OsmChartGraph> highcharts = serviceUnderTest.getCsRelevantStaticGraphsAsResultMapForChart(fromDate, toDate)
        List<OsmChartPoint> points = findGraphByLabel(highcharts, graphLabel).getPoints();
        OsmChartPoint threeMonthsAgo = points[0]
        double threeMonthsAgoDeviationDueToRounding = Math.abs(threeMonthsAgo.measuredValue - 82.5)
        OsmChartPoint oneMonthAgo = points[1]
        double oneMonthAgoDeviationDueToRounding = Math.abs(oneMonthAgo.measuredValue - 87.5)

		then:
		highcharts.size() == 1

		points != null
		points.size() == 2

        threeMonthsAgo.time == new DateTime(fromDate, DateTimeZone.forID('MET')).getMillis()
        threeMonthsAgoDeviationDueToRounding < tolerableDeviationDueToRounding

        oneMonthAgo.time == new DateTime(toDate, DateTimeZone.forID('MET')).getMillis()
        oneMonthAgoDeviationDueToRounding < tolerableDeviationDueToRounding
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
									new CsiAggregation(
									started: now,
									interval: hourly,
									aggregator: measured_event,
									tag: "${zeroBasedIndexJobGroup+1};${zeroBasedIndexPage+1};${zeroBasedIndexMeasuredEvent+1};${zeroBasedIndexBrowser+1};${zeroBasedIndexLocation+1}",
									csByWptDocCompleteInPercent: 85,
									underlyingEventResultsByWptDocComplete: '1,2'
									))
						}}}}}

		measuredValueForPageWeeklyList = [
			new CsiAggregation(
			started: now,
			interval: hourly,
			aggregator: page,
			tag: "1;1",
			csByWptDocCompleteInPercent: 85,
			underlyingEventResultsByWptDocComplete: '1,2'
			),
			new CsiAggregation(
				started: DateUtils.addHours(now, 3),
				interval: hourly,
				aggregator: page,
				tag: "1;1",
				csByWptDocCompleteInPercent: 83,
				underlyingEventResultsByWptDocComplete: '5,6'
			),
			new CsiAggregation(
				started: DateUtils.addHours(now, 2),
				interval: hourly,
				aggregator: page,
				tag: "1;1",
				csByWptDocCompleteInPercent: 79,
				underlyingEventResultsByWptDocComplete: '3,4'
			),
			new CsiAggregation(
				started: DateUtils.addHours(now, 2),
				interval: hourly,
				aggregator: page,
				tag: "1;2",
				csByWptDocCompleteInPercent: 65,
				underlyingEventResultsByWptDocComplete: '7,8'
			),
			new CsiAggregation(
				started: now,
				interval: hourly,
				aggregator: page,
				tag: "1;2",
				csByWptDocCompleteInPercent: 67,
				underlyingEventResultsByWptDocComplete: '9,10'
			)
		]

		measuredValueForShopWeeklyList = [
			new CsiAggregation(
				started: now,
				interval: hourly,
				aggregator: shop,
				tag: '1',
				csByWptDocCompleteInPercent: 85,
				underlyingEventResultsByWptDocComplete: '1,2'
			),
			new CsiAggregation(
				started: DateUtils.addHours(now, 3),
				interval: hourly,
				aggregator: shop,
				tag: '1',
				csByWptDocCompleteInPercent: 83,
				underlyingEventResultsByWptDocComplete: '5,6'
			),
			new CsiAggregation(
				started: DateUtils.addHours(now, 2),
				interval: hourly,
				aggregator: shop,
				tag: '1',
				csByWptDocCompleteInPercent: 79,
				underlyingEventResultsByWptDocComplete: '3,4'
			)
		]

		measuredValueForShopWeeklyWithNullList = [
			new CsiAggregation(
				started: now,
				interval: weekly,
				aggregator: shop,
				tag: '1',
				csByWptDocCompleteInPercent: null,
				underlyingEventResultsByWptDocComplete: '1,2'
			),
			new CsiAggregation(
				started: DateUtils.addHours(now, 3),
				interval: weekly,
				aggregator: shop,
				tag: '1',
				csByWptDocCompleteInPercent: 83,
				underlyingEventResultsByWptDocComplete: '5,6'
			),
			new CsiAggregation(
				started: DateUtils.addHours(now, 2),
				interval: weekly,
				aggregator: shop,
				tag: '1',
				csByWptDocCompleteInPercent: 79,
				underlyingEventResultsByWptDocComplete: '3,4'
			)
		]

		measuredValueListWithValuesLowerThanOne = [
			new CsiAggregation(
				started: now,
				interval: weekly,
				aggregator: shop,
				tag: '1',
				csByWptDocCompleteInPercent: 32,
				underlyingEventResultsByWptDocComplete: '1,2'
			),
			new CsiAggregation(
			started: DateUtils.addHours(now, 3),
			interval: weekly,
			aggregator: shop,
			tag: '1',
			csByWptDocCompleteInPercent: 83,
			underlyingEventResultsByWptDocComplete: '5,6'
			),
			new CsiAggregation(
				started: DateUtils.addHours(now, 2),
				interval: weekly,
				aggregator: shop,
				tag: '1',
				csByWptDocCompleteInPercent: 79,
				underlyingEventResultsByWptDocComplete: '3,4'
			)
		]

		measuredValueListWithValuesLowerThanOneAndWithMoreThanTwoDecimalPlaces = [
			new CsiAggregation(
				started: now,
				interval: weekly,
				aggregator: shop,
				tag: '1',
				csByWptDocCompleteInPercent: 32.4562,
				underlyingEventResultsByWptDocComplete: '1,2'
			),
			new CsiAggregation(
				started: DateUtils.addHours(now, 3),
				interval: weekly,
				aggregator: shop,
				tag: '1',
				csByWptDocCompleteInPercent: 83.12367,
				underlyingEventResultsByWptDocComplete: '5,6'
			),
			new CsiAggregation(
				started: DateUtils.addHours(now, 2),
				interval: weekly,
				aggregator: shop,
				tag: '1',
				csByWptDocCompleteInPercent: 79.0968,
				underlyingEventResultsByWptDocComplete: '3,4'
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

    void mockServicesCommonForAllTests() {
        // We simply a modified version of the original service here, because
        // we expect only URL generation to be called and expect that
        // the service under test don't care about the URL itself.
        serviceUnderTest.eventResultDashboardService = new EventResultDashboardService() {
            @Override
            public URL tryToBuildTestsDetailsURL(CsiAggregation mv) {
                return new URL('http://measuredvalue.example.com/'+mv.id);
            }
        }
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
        serviceUnderTest.osmChartProcessingService = new OsmChartProcessingService()
        serviceUnderTest.osmChartProcessingService.i18nService = [
                msg: {String msgKey, String defaultMessage = null, List objs = null ->
                    Map i18nKeysToValues = [
                            'job.jobGroup.label':I18N_LABEL_JOB_GROUP,
                            'de.iteratec.osm.result.measured-event.label':I18N_LABEL_MEASURED_EVENT,
                            'job.location.label':I18N_LABEL_LOCATION,
                            'de.iteratec.result.measurand.label': I18N_LABEL_MEASURAND,
                            'de.iteratec.osm.result.connectivity.label': I18N_LABEL_CONNECTIVITY
                    ]
                    return i18nKeysToValues[msgKey]
                }
        ] as I18nService

    }
}

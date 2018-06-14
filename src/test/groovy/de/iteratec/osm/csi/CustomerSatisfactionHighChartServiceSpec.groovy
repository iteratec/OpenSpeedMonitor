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

import asset.pipeline.grails.LinkGenerator
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
import grails.buildtestdata.mixin.Build
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import org.apache.commons.lang.time.DateUtils
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.Interval
import spock.lang.Specification

import static de.iteratec.osm.util.Constants.TIMESERIES_CHART_LEGEND_DELIMITTER

/**
 * Test-suite of {@link CustomerSatisfactionHighChartService}.
 */
@TestFor(CustomerSatisfactionHighChartService)
@Mock([CsiAggregation, CsiAggregationInterval, Page, Job, CsTargetValue, CsTargetGraph, JobGroup, MeasuredEvent, Browser, Location,
        Script, WebPageTestServer])
@Build([Browser, Location, MeasuredEvent, Page, Job, JobGroup, CsiAggregationInterval])
class CustomerSatisfactionHighChartServiceSpec extends Specification {

    CsiAggregationInterval hourly
    CsiAggregationInterval weekly

    Date now
    Date tomorrow
    Date fourMonthsAgo

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
     * So it contains 2EXP5=32 {@link CsiAggregation}s.<br>
     * <em>Note:</em>
     * @see #createCsiAggregations()
     */
    List<CsiAggregation> csiAggregationForEventHourlyList = []
    List<CsiAggregation> csiAggregationForPageWeeklyList = []
    List<CsiAggregation> csiAggregationForShopWeeklyList = []
    List<CsiAggregation> csiAggregationForShopWeeklyWithNullList = []
    List<CsiAggregation> csiAggregationListWithValuesLowerThanOne = []
    List<CsiAggregation> csiAggregationListWithValuesLowerThanOneAndWithMoreThanTwoDecimalPlaces = []

    List<String> expectedJobLabels = ['job1', 'job2']

    List<String> expectedJobGroupNames = ['group1', 'group2']
    List<String> expectedPageNames = ['page1', 'page2']
    List<String> expectedMeasuredEventNames = ['event1', 'event2']
    List<String> expectedBrowserNames = ['browser1', 'browser2']
    List<String> expectedLocationNames = ['location1', 'location2']
    List<String> expectedCsiTypes = [CsiType.DOC_COMPLETE.toString(), CsiType.VISUALLY_COMPLETE.toString()]

    String graphLabel
    Double tolerableDeviationDueToRounding

    JobGroup jobGroup1
    JobGroup jobGroup2
    MeasuredEvent measuredEvent1
    MeasuredEvent measuredEvent2
    Page page1
    Page page2
    Browser browser1
    Browser browser2
    Location location1
    Location location2

    CustomerSatisfactionHighChartService serviceUnderTest
    ServiceMocker mockGenerator

    public static final String I18N_LABEL_JOB_GROUP = 'Job Group'
    public static final String I18N_LABEL_MEASURED_EVENT = 'Measured step'
    public static final String I18N_LABEL_LOCATION = 'Location'
    public static final String I18N_LABEL_MEASURAND = 'Measurand'
    public static final String I18N_LABEL_CONNECTIVITY = 'Connectivity'

    def doWithSpring = {
        csiAggregationUtilService(CsiAggregationUtilService)
        osmChartProcessingService(OsmChartProcessingService)
        eventResultDashboardService(EventResultDashboardService)
    }

    void setup() {

        serviceUnderTest = service

        createDataCommonForAllTests()

        mockServicesCommonForAllTests()

    }

    void "correct graph labels get created for hourly event csiAggregations"() {
        given:
        CsiAggregation mv1 = new CsiAggregation(
                aggregationType: AggregationType.MEASURED_EVENT,
                jobGroup: jobGroup1,
                measuredEvent: measuredEvent2,
                page: page1,
                browser: browser2,
                location: location1)
        CsiAggregation mv2 = new CsiAggregation(
                aggregationType: AggregationType.MEASURED_EVENT,
                jobGroup: jobGroup1,
                measuredEvent: measuredEvent1,
                page: page1,
                browser: browser1,
                location: location1)
        CsiAggregation mv3 = new CsiAggregation(
                aggregationType: AggregationType.MEASURED_EVENT,
                jobGroup: jobGroup2,
                measuredEvent: measuredEvent2,
                page: page2,
                browser: browser2,
                location: location2)

        String expectedLabel1 = "${expectedCsiTypes[0]}${TIMESERIES_CHART_LEGEND_DELIMITTER}" +
                "${expectedJobGroupNames[0]}${TIMESERIES_CHART_LEGEND_DELIMITTER}" +
                "${expectedMeasuredEventNames[1]}${TIMESERIES_CHART_LEGEND_DELIMITTER}" +
                "${expectedLocationNames[0]}"
        String expectedLabel2 = "${expectedCsiTypes[0]}${TIMESERIES_CHART_LEGEND_DELIMITTER}" +
                "${expectedJobGroupNames[0]}${TIMESERIES_CHART_LEGEND_DELIMITTER}" +
                "${expectedMeasuredEventNames[0]}${TIMESERIES_CHART_LEGEND_DELIMITTER}" +
                "${expectedLocationNames[0]}"
        String expectedLabel3 = "${expectedCsiTypes[0]}${TIMESERIES_CHART_LEGEND_DELIMITTER}" +
                "${expectedJobGroupNames[1]}${TIMESERIES_CHART_LEGEND_DELIMITTER}" +
                "${expectedMeasuredEventNames[1]}${TIMESERIES_CHART_LEGEND_DELIMITTER}" +
                "${expectedLocationNames[1]}"

        when:
        String mapLabel1 = serviceUnderTest.getMapLabel(mv1, CsiType.DOC_COMPLETE)
        String mapLabel2 = serviceUnderTest.getMapLabel(mv2, CsiType.DOC_COMPLETE)
        String mapLabel3 = serviceUnderTest.getMapLabel(mv3, CsiType.DOC_COMPLETE)

        then:
        mapLabel1 == expectedLabel1
        mapLabel2 == expectedLabel2
        mapLabel3 == expectedLabel3
    }

    void "correct graph labels get created for weekly page csiAggregations"() {

        given:
        CsiAggregation mv1 = new CsiAggregation(
                aggregationType: AggregationType.PAGE,
                interval: weekly,
                jobGroup: jobGroup1,
                page: page1)
        CsiAggregation mv2 = new CsiAggregation(
                aggregationType: AggregationType.PAGE,
                interval: weekly,
                jobGroup: jobGroup1,
                page: page2)
        CsiAggregation mv3 = new CsiAggregation(
                aggregationType: AggregationType.PAGE,
                interval: weekly,
                jobGroup: jobGroup2,
                page: page2)

        String expectedLabel1 = "${expectedCsiTypes[1]}${TIMESERIES_CHART_LEGEND_DELIMITTER}" +
                "${expectedJobGroupNames[0]}${TIMESERIES_CHART_LEGEND_DELIMITTER}${expectedPageNames[0]}"
        String expectedLabel2 = "${expectedCsiTypes[1]}${TIMESERIES_CHART_LEGEND_DELIMITTER}" +
                "${expectedJobGroupNames[0]}${TIMESERIES_CHART_LEGEND_DELIMITTER}${expectedPageNames[1]}"
        String expectedLabel3 = "${expectedCsiTypes[1]}${TIMESERIES_CHART_LEGEND_DELIMITTER}" +
                "${expectedJobGroupNames[1]}${TIMESERIES_CHART_LEGEND_DELIMITTER}${expectedPageNames[1]}"

        when:
        String mapLabel1 = serviceUnderTest.getMapLabel(mv1, CsiType.VISUALLY_COMPLETE)
        String mapLabel2 = serviceUnderTest.getMapLabel(mv2, CsiType.VISUALLY_COMPLETE)
        String mapLabel3 = serviceUnderTest.getMapLabel(mv3, CsiType.VISUALLY_COMPLETE)

        then:
        mapLabel1 == expectedLabel1
        mapLabel2 == expectedLabel2
        mapLabel3 == expectedLabel3
    }

    void "correct graph labels get created for weekly shop csiAggregations"() {
        given:
        CsiAggregation mv1 = new CsiAggregation(
                aggregationType: AggregationType.JOB_GROUP,
                interval: weekly,
                jobGroup: jobGroup1)
        CsiAggregation mv2 = new CsiAggregation(
                aggregationType: AggregationType.JOB_GROUP,
                interval: weekly,
                jobGroup: jobGroup2)

        String expectedLabel1 = "${expectedCsiTypes[0]}${TIMESERIES_CHART_LEGEND_DELIMITTER}" +
                "${expectedJobGroupNames[0]}"
        String expectedLabel2 = "${expectedCsiTypes[0]}${TIMESERIES_CHART_LEGEND_DELIMITTER}" +
                "${expectedJobGroupNames[1]}"

        when:
        String mapLabel1 = serviceUnderTest.getMapLabel(mv1, CsiType.DOC_COMPLETE)
        String mapLabel2 = serviceUnderTest.getMapLabel(mv2, CsiType.DOC_COMPLETE)

        then:
        mapLabel1 == expectedLabel1
        mapLabel2 == expectedLabel2

    }

    void "build osm chart graphs correctly from hourly event csiAggregations"() {

        setup:
        //create test-specific data
        MvQueryParams irrelevantQueryParamsCauseUsingFunctionalityIsMocked = new MvQueryParams()
        //mock inner service
        mockGenerator.mockEventCsiAggregationService(serviceUnderTest, csiAggregationForEventHourlyList)
        Integer numberOfExistingCombinations_JobgroupPageMeasuredeventBrowserLocation = 32

        when:
        OsmRickshawChart chart = serviceUnderTest.getCalculatedHourlyEventCsiAggregationsAsHighChartMap(
                now, tomorrow, irrelevantQueryParamsCauseUsingFunctionalityIsMocked, [CsiType.DOC_COMPLETE]
        )
        List<OsmChartGraph> graphs = chart.osmChartGraphs

        then:
        graphs != null
        graphs.size() == numberOfExistingCombinations_JobgroupPageMeasuredeventBrowserLocation

    }

    void "build osm chart graphs correctly from page csiAggregations"() {

        setup:
        //create test-specific data
        Integer numberOfGroupPageCombinations = 2
        Integer numberOfValuesInGraphOfGroupPageCombination_11 = 3
        Integer numberOfValuesInGraphOfGroupPageCombination_12 = 2
        String expectedGraphLabelOfGroupPageCombination_11 = "${expectedPageNames[0]}"
        String expectedGraphLabelOfGroupPageCombination_12 = "${expectedPageNames[1]}"
        CsiAggregationInterval weekly = CsiAggregationInterval.findByIntervalInMinutes(CsiAggregationInterval.WEEKLY);
        //mock inner service
        mockGenerator.mockPageCsiAggregationService(serviceUnderTest, csiAggregationForPageWeeklyList)

        when:
        OsmRickshawChart chart = serviceUnderTest.getCalculatedPageCsiAggregationsAsHighChartMap(
                new Interval(now.getTime(), tomorrow.getTime()),
                new MvQueryParams(),
                weekly,
                [CsiType.DOC_COMPLETE]
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

    void "build osm chart graphs correctly from shop csiAggregations"() {

        setup:
        //create test-specific data
        String expectedLabel = expectedCsiTypes[0] + TIMESERIES_CHART_LEGEND_DELIMITTER + expectedJobGroupNames[0]
        CsiAggregationInterval weekly = CsiAggregationInterval.findByIntervalInMinutes(CsiAggregationInterval.WEEKLY);
        //mock inner service
        mockGenerator.mockJobGroupCsiAggregationService(serviceUnderTest, csiAggregationForShopWeeklyList)

        when:
        OsmRickshawChart chart = serviceUnderTest.getCalculatedShopCsiAggregationsAsHighChartMap(
                new Interval(now.getTime(), tomorrow.getTime()),
                weekly,
                new MvQueryParams(),
                [CsiType.DOC_COMPLETE]
        )
        List<OsmChartGraph> graphs = chart.osmChartGraphs
        List<OsmChartPoint> points = findGraphByLabel(graphs, expectedLabel).getPoints()
        List<Long> pointsTimes = points*.time

        then:
        graphs != null
        graphs.size() == 1

        points.size() == csiAggregationForShopWeeklyList.size()
        points.every { point -> point.csiAggregation > 0 }
        pointsTimes == pointsTimes.sort()

    }

    void "build osm chart graphs correctly from shop csiAggregations with null in data source"() {

        setup:
        //create test-specific data
        String expectedLabel = expectedCsiTypes[0] + TIMESERIES_CHART_LEGEND_DELIMITTER + expectedJobGroupNames[0]
        CsiAggregationInterval weekly = CsiAggregationInterval.findByIntervalInMinutes(CsiAggregationInterval.WEEKLY);
        //mock inner service
        mockGenerator.mockJobGroupCsiAggregationService(serviceUnderTest, csiAggregationForShopWeeklyWithNullList)

        when:
        OsmRickshawChart chart = serviceUnderTest.getCalculatedShopCsiAggregationsAsHighChartMap(
                new Interval(now.getTime(), tomorrow.getTime()),
                weekly,
                new MvQueryParams(),
                [CsiType.DOC_COMPLETE]
        )
        List<OsmChartGraph> graphs = chart.osmChartGraphs
        List<OsmChartPoint> points = findGraphByLabel(graphs, expectedLabel).getPoints()
        List<Long> pointsTimes = points*.time

        then:
        graphs != null
        graphs.size() == 1

        points.size() == csiAggregationForShopWeeklyWithNullList.size() - 1
        points.every { point -> point.csiAggregation > 0 }
        pointsTimes == pointsTimes.sort()

    }

    void "build osm chart graphs correctly from shop csiAggregations with values lower than one in data source"() {

        setup:
        //create test-specific data
        String expectedLabel = expectedCsiTypes[0] + TIMESERIES_CHART_LEGEND_DELIMITTER + expectedJobGroupNames[0]
        CsiAggregationInterval weekly = CsiAggregationInterval.findByIntervalInMinutes(CsiAggregationInterval.WEEKLY);
        //mock inner service
        mockGenerator.mockJobGroupCsiAggregationService(serviceUnderTest, csiAggregationForShopWeeklyList)

        when:
        OsmRickshawChart chart = serviceUnderTest.getCalculatedShopCsiAggregationsAsHighChartMap(
                new Interval(now.getTime(), tomorrow.getTime()),
                weekly,
                new MvQueryParams(),
                [CsiType.DOC_COMPLETE]
        )
        List<OsmChartGraph> graphs = chart.osmChartGraphs
        List<OsmChartPoint> points = findGraphByLabel(graphs, expectedLabel).getPoints()
        List<Long> pointsTimes = points*.time

        then:
        graphs != null
        graphs.size() == 1

        points.size() == csiAggregationListWithValuesLowerThanOne.size()
        points.every { point -> point.csiAggregation > 1 }
        pointsTimes == pointsTimes.sort()

    }

    void "build osm chart graphs correctly from shop csiAggregations with values lower than one and with more than two decimal places in data source"() {

        setup:
        //create test-specific data
        String expectedLabel = expectedCsiTypes[0] + TIMESERIES_CHART_LEGEND_DELIMITTER + expectedJobGroupNames[0]
        CsiAggregationInterval weekly = CsiAggregationInterval.findByIntervalInMinutes(CsiAggregationInterval.WEEKLY);
        //mock inner service
        mockGenerator.mockJobGroupCsiAggregationService(serviceUnderTest, csiAggregationForShopWeeklyList)

        when:
        OsmRickshawChart chart = serviceUnderTest.getCalculatedShopCsiAggregationsAsHighChartMap(
                new Interval(now.getTime(), tomorrow.getTime()),
                weekly,
                new MvQueryParams(),
                [CsiType.DOC_COMPLETE]
        )
        List<OsmChartGraph> graphs = chart.osmChartGraphs
        List<OsmChartPoint> points = findGraphByLabel(graphs, expectedLabel).getPoints()
        List<Long> pointsTimes = points*.time

        then:
        graphs != null
        graphs.size() == 1

        points.size() == csiAggregationListWithValuesLowerThanOneAndWithMoreThanTwoDecimalPlaces.size()
        points.every { point ->
            point.csiAggregation > 1 &&
                    String.valueOf(point.csiAggregation).split("\\.")[1].length() <= 2
        }

        pointsTimes == pointsTimes.sort()

    }

    void "cs relevant static graphs get provided correctly"() {

        setup:
        //create test-specific data
        DateTime fromDate = new DateTime(now).minusMonths(3)
        DateTime toDate = new DateTime(now).minusMonths(1)

        when:
        List<OsmChartGraph> highcharts = serviceUnderTest.getCsRelevantStaticGraphsAsResultMapForChart(fromDate, toDate)
        List<OsmChartPoint> points = findGraphByLabel(highcharts, graphLabel).getPoints();
        OsmChartPoint threeMonthsAgo = points[0]
        double threeMonthsAgoDeviationDueToRounding = Math.abs(threeMonthsAgo.csiAggregation - 82.5)
        OsmChartPoint oneMonthAgo = points[1]
        double oneMonthAgoDeviationDueToRounding = Math.abs(oneMonthAgo.csiAggregation - 87.5)

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

    private void createDataCommonForAllTests() {
        // now = 17.07.2013 - 16:28:35
        now = new Date(1374071315000L)

        now = DateUtils.setMilliseconds(now, 0)
        now = DateUtils.setSeconds(now, 0)
        now = DateUtils.setMinutes(now, 0)
        now = DateUtils.setHours(now, 0)
        tomorrow = DateUtils.addDays(now, 1)
        fourMonthsAgo = DateUtils.addMonths(now, -4)
        tolerableDeviationDueToRounding = 0.2

        createAggregatorIntervals()
        createJobs()
        createPages()
        createCsTargetGraphs()
        createJobGroups()
        createMeasuredEvents()
        createBrowsers()
        createLocations()
        createCsiAggregations()
    }

    private void createAggregatorIntervals() {
        hourly = CsiAggregationInterval.build(intervalInMinutes: CsiAggregationInterval.HOURLY)
        weekly = CsiAggregationInterval.build(intervalInMinutes: CsiAggregationInterval.WEEKLY)
    }

    private void createJobs() {
        Job.build(label: expectedJobLabels[0])
        Job.build(label: expectedJobLabels[1])
    }

    private void createPages() {
        page1 = Page.build(name: expectedPageNames[0])

        page2 = Page.build(name: expectedPageNames[1])
    }

    private void createJobGroups() {
        jobGroup1 = JobGroup.build(name: expectedJobGroupNames[0])
        jobGroup2 = JobGroup.build(name: expectedJobGroupNames[1])
    }

    private void createMeasuredEvents() {
        measuredEvent1 = MeasuredEvent.build(name: expectedMeasuredEventNames[0])
        measuredEvent2 = MeasuredEvent.build(name: expectedMeasuredEventNames[1])
    }

    private void createBrowsers() {
        browser1 = Browser.build(name: expectedBrowserNames[0])
        browser2 = Browser.build(name: expectedBrowserNames[1])
    }

    private void createLocations() {
        location1 = Location.build(location: expectedLocationNames[0])
        location2 = Location.build(location: expectedLocationNames[1])
    }

    private void createCsiAggregations() {
        csiAggregationForEventHourlyList.clear()
        2.times { zeroBasedIndexJobGroup ->
            2.times { zeroBasedIndexPage ->
                2.times { zeroBasedIndexMeasuredEvent ->
                    2.times { zeroBasedIndexBrowser ->
                        2.times { zeroBasedIndexLocation ->
                            csiAggregationForEventHourlyList.add(
                                    new CsiAggregation(
                                            started: now,
                                            interval: hourly,
                                            aggregationType: AggregationType.MEASURED_EVENT,
                                            jobGroup: JobGroup.get(zeroBasedIndexJobGroup + 1),
                                            measuredEvent: MeasuredEvent.get(zeroBasedIndexMeasuredEvent + 1),
                                            page: Page.get(zeroBasedIndexPage + 1),
                                            browser: Browser.get(zeroBasedIndexBrowser + 1),
                                            location: Location.get(zeroBasedIndexLocation + 1),
                                            csByWptDocCompleteInPercent: 85,
                                            underlyingEventResultsByWptDocComplete: '1,2'
                                    ))
                        }
                    }
                }
            }
        }

        csiAggregationForPageWeeklyList = [
                new CsiAggregation(
                        started: now,
                        interval: hourly,
                        aggregationType: AggregationType.PAGE,
                        jobGroup: jobGroup1,
                        page: page1,
                        csByWptDocCompleteInPercent: 85,
                        underlyingEventResultsByWptDocComplete: '1,2'
                ),
                new CsiAggregation(
                        started: DateUtils.addHours(now, 3),
                        interval: hourly,
                        aggregationType: AggregationType.PAGE,
                        jobGroup: jobGroup1,
                        page: page1,
                        csByWptDocCompleteInPercent: 83,
                        underlyingEventResultsByWptDocComplete: '5,6'
                ),
                new CsiAggregation(
                        started: DateUtils.addHours(now, 2),
                        interval: hourly,
                        aggregationType: AggregationType.PAGE,
                        jobGroup: jobGroup1,
                        page: page1,
                        csByWptDocCompleteInPercent: 79,
                        underlyingEventResultsByWptDocComplete: '3,4'
                ),
                new CsiAggregation(
                        started: DateUtils.addHours(now, 2),
                        interval: hourly,
                        aggregationType: AggregationType.PAGE,
                        jobGroup: jobGroup1,
                        page: page2,
                        csByWptDocCompleteInPercent: 65,
                        underlyingEventResultsByWptDocComplete: '7,8'
                ),
                new CsiAggregation(
                        started: now,
                        interval: hourly,
                        aggregationType: AggregationType.PAGE,
                        jobGroup: jobGroup1,
                        page: page2,
                        csByWptDocCompleteInPercent: 67,
                        underlyingEventResultsByWptDocComplete: '9,10'
                )
        ]

        csiAggregationForShopWeeklyList = [
                new CsiAggregation(
                        started: now,
                        interval: hourly,
                        aggregationType: AggregationType.JOB_GROUP,
                        jobGroup: jobGroup1,
                        csByWptDocCompleteInPercent: 85,
                        underlyingEventResultsByWptDocComplete: '1,2'
                ),
                new CsiAggregation(
                        started: DateUtils.addHours(now, 3),
                        interval: hourly,
                        aggregationType: AggregationType.JOB_GROUP,
                        jobGroup: jobGroup1,
                        csByWptDocCompleteInPercent: 83,
                        underlyingEventResultsByWptDocComplete: '5,6'
                ),
                new CsiAggregation(
                        started: DateUtils.addHours(now, 2),
                        interval: hourly,
                        aggregationType: AggregationType.JOB_GROUP,
                        jobGroup: jobGroup1,
                        csByWptDocCompleteInPercent: 79,
                        underlyingEventResultsByWptDocComplete: '3,4'
                )
        ]

        csiAggregationForShopWeeklyWithNullList = [
                new CsiAggregation(
                        started: now,
                        interval: weekly,
                        aggregationType: AggregationType.JOB_GROUP,
                        jobGroup: jobGroup1,
                        csByWptDocCompleteInPercent: null,
                        underlyingEventResultsByWptDocComplete: '1,2'
                ),
                new CsiAggregation(
                        started: DateUtils.addHours(now, 3),
                        interval: weekly,
                        aggregationType: AggregationType.JOB_GROUP,
                        jobGroup: jobGroup1,
                        csByWptDocCompleteInPercent: 83,
                        underlyingEventResultsByWptDocComplete: '5,6'
                ),
                new CsiAggregation(
                        started: DateUtils.addHours(now, 2),
                        interval: weekly,
                        aggregationType: AggregationType.JOB_GROUP,
                        jobGroup: jobGroup1,
                        csByWptDocCompleteInPercent: 79,
                        underlyingEventResultsByWptDocComplete: '3,4'
                )
        ]

        csiAggregationListWithValuesLowerThanOne = [
                new CsiAggregation(
                        started: now,
                        interval: weekly,
                        aggregationType: AggregationType.JOB_GROUP,
                        jobGroup: jobGroup1,
                        csByWptDocCompleteInPercent: 32,
                        underlyingEventResultsByWptDocComplete: '1,2'
                ),
                new CsiAggregation(
                        started: DateUtils.addHours(now, 3),
                        interval: weekly,
                        aggregationType: AggregationType.JOB_GROUP,
                        jobGroup: jobGroup1,
                        csByWptDocCompleteInPercent: 83,
                        underlyingEventResultsByWptDocComplete: '5,6'
                ),
                new CsiAggregation(
                        started: DateUtils.addHours(now, 2),
                        interval: weekly,
                        aggregationType: AggregationType.JOB_GROUP,
                        jobGroup: jobGroup1,
                        csByWptDocCompleteInPercent: 79,
                        underlyingEventResultsByWptDocComplete: '3,4'
                )
        ]

        csiAggregationListWithValuesLowerThanOneAndWithMoreThanTwoDecimalPlaces = [
                new CsiAggregation(
                        started: now,
                        interval: weekly,
                        aggregationType: AggregationType.JOB_GROUP,
                        jobGroup: jobGroup1,
                        csByWptDocCompleteInPercent: 32.4562,
                        underlyingEventResultsByWptDocComplete: '1,2'
                ),
                new CsiAggregation(
                        started: DateUtils.addHours(now, 3),
                        interval: weekly,
                        aggregationType: AggregationType.JOB_GROUP,
                        jobGroup: jobGroup1,
                        csByWptDocCompleteInPercent: 83.12367,
                        underlyingEventResultsByWptDocComplete: '5,6'
                ),
                new CsiAggregation(
                        started: DateUtils.addHours(now, 2),
                        interval: weekly,
                        aggregationType: AggregationType.JOB_GROUP,
                        jobGroup: jobGroup1,
                        csByWptDocCompleteInPercent: 79.0968,
                        underlyingEventResultsByWptDocComplete: '3,4'
                )
        ]
    }

    private void createCsTargetGraphs() {
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
            if (it.getLabel() == label) {
                graph = it;
            }
        }

        return graph;
    }


    void mockServicesCommonForAllTests() {
        // We simply a modified version of the original service here, because
        // we expect only URL generation to be called and expect that
        // the service under test don't care about the URL itself.
        serviceUnderTest.eventResultDashboardService = Stub(EventResultDashboardService) {
            tryToBuildTestsDetailsURL(_ as CsiAggregation) >> { CsiAggregation csiAggregation ->
                return new URL('http://measuredvalue.example.com/' + csiAggregation.id)
            }
        }
        serviceUnderTest.csiAggregationUtilService = Spy(CsiAggregationUtilService)
        mockGenerator = ServiceMocker.create()
        serviceUnderTest.csTargetGraphDaoService = Stub(CsTargetGraphDaoService) {
            getActualCsTargetGraph() >> CsTargetGraph.findByLabel(graphLabel)

        }
        serviceUnderTest.grailsLinkGenerator = Mock(LinkGenerator) {
            link(_ as Map) >> "http://www.iteratec.de"
        }
        serviceUnderTest.osmChartProcessingService = Spy(OsmChartProcessingService)
        serviceUnderTest.osmChartProcessingService.i18nService = Stub(I18nService) {
            msg(_ as String, _ as String, _ as List) >> { String msgKey, String defaultMessage = null, List objs = null ->
                Map i18nKeysToValues = [
                        'job.jobGroup.label'                         : I18N_LABEL_JOB_GROUP,
                        'de.iteratec.osm.result.measured-event.label': I18N_LABEL_MEASURED_EVENT,
                        'job.location.label'                         : I18N_LABEL_LOCATION,
                        'de.iteratec.result.measurand.label'         : I18N_LABEL_MEASURAND,
                        'de.iteratec.osm.result.connectivity.label'  : I18N_LABEL_CONNECTIVITY
                ]
                return i18nKeysToValues[msgKey]
            }
        }
    }
}

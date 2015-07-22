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

import de.iteratec.osm.report.chart.DefaultAggregatorTypeDaoService
import de.iteratec.osm.report.chart.MeasuredValueUtilService

import static de.iteratec.osm.util.Constants.*

import de.iteratec.osm.csi.Page
import de.iteratec.osm.csi.TestDataUtil
import de.iteratec.osm.csi.HourOfDay
import de.iteratec.osm.measurement.environment.Browser
import de.iteratec.osm.measurement.environment.BrowserAlias
import de.iteratec.osm.measurement.environment.Location
import de.iteratec.osm.measurement.environment.WebPageTestServer
import de.iteratec.osm.measurement.schedule.ConnectivityProfile
import de.iteratec.osm.measurement.schedule.Job
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.measurement.schedule.JobGroupType
import de.iteratec.osm.measurement.script.Script
import de.iteratec.osm.report.chart.AggregatorType
import de.iteratec.osm.report.chart.MeasurandGroup
import de.iteratec.osm.report.chart.MeasuredValue
import de.iteratec.osm.report.chart.MeasuredValueInterval
import de.iteratec.osm.report.chart.OsmChartGraph
import de.iteratec.osm.result.dao.EventResultDaoService
import de.iteratec.osm.util.ServiceMocker
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
@TestMixin(GrailsUnitTestMixin)
@TestFor(EventResultDashboardService)
@Mock([Job, JobResult, MeasuredEvent, MeasuredValue, MeasuredValueInterval, Location, Browser, BrowserAlias, Page, JobGroup, AggregatorType, WebPageTestServer, EventResult, Script, ConnectivityProfile, HourOfDay])
class SummarizedChartLegendEntriesSpec extends Specification{

    EventResultDashboardService serviceUnderTest

    public static final ServiceMocker MOCKER = new ServiceMocker()

    public static final String JOB_GROUP_1_NAME = 'group 1'
    public static final String JOB_GROUP_2_NAME = 'group 2'
    public static final String JOB_GROUP_3_NAME = 'group 3'
    public static final String JOB_GROUP_4_NAME = 'group 4'
    public static final String LOCATION_1_UNIQUE_IDENTIFIER = 'unique-identifier-location1'
    public static final String LOCATION_2_UNIQUE_IDENTIFIER = 'unique-identifier-location2'
    public static final String LOCATION_3_UNIQUE_IDENTIFIER = 'unique-identifier-location3'
    public static final String LOCATION_4_UNIQUE_IDENTIFIER = 'unique-identifier-location4'
    public static final String JOB_LABEL = 'jobName'
    public static final String TEST_ID = 'test-id'
    public static final String EVENT_1_NAME = 'event 1 name'
    public static final String EVENT_2_NAME = 'event 2 name'
    public static final String EVENT_3_NAME = 'event 3 name'
    public static final String EVENT_4_NAME = 'event 4 name'
    public static final DateTime RUN_DATE = new DateTime(2013, 5, 29, 10, 13, 2, 564, DateTimeZone.UTC)
    public static final Integer DOC_COMPLETE_TIME = 2000i
    public static final Integer DOC_COMPLETE_REQUESTS = 24
    public static final Integer CUSTOMER_SATISFACTION = 0.86d
    public static final ErQueryParams QUERY_PARAMS = new ErQueryParams()
    public static final String TAGPART_IRRELEVANT_FOR_LEGEND_ENTRIES = ';1;1'
    public static final String PROFILE_1_NAME = 'conn-profile 1'
    public static final String PROFILE_2_NAME = 'conn-profile 2'
    public static final String PROFILE_3_NAME = 'conn-profile 3'

    void setup() {
        serviceUnderTest = service
        prepareMocksCommonForAllTests()
        createTestDataCommonForAllTests()
    }

    void teardown(){
        EventResult.list()*.delete()
    }

    // RAW ////////////////////////////////////////////////////////////////////////////////////////////////////////

    void "aggregation RAW - no summarization possible"() {
        setup:
        MOCKER.mockEventResultDaoService(serviceUnderTest,
            [
                createEventResult("1;1${TAGPART_IRRELEVANT_FOR_LEGEND_ENTRIES};1", PROFILE_1_NAME),
                createEventResult("2;2${TAGPART_IRRELEVANT_FOR_LEGEND_ENTRIES};2", PROFILE_2_NAME)
            ]
        )

        when:
        List<OsmChartGraph> resultGraphs = serviceUnderTest.getEventResultDashboardHighchartGraphs(
                RUN_DATE.toDate(),
                RUN_DATE.plusHours(1).toDate(),
                MeasuredValueInterval.RAW,
                [AggregatorType.findByName(AggregatorType.RESULT_UNCACHED_DOC_COMPLETE_TIME),
                 AggregatorType.findByName(AggregatorType.RESULT_UNCACHED_DOC_COMPLETE_REQUESTS)],
                QUERY_PARAMS);

        then:
        resultGraphs.size() == 4
        List<String> graphLables = resultGraphs*.label
        graphLables.containsAll([
                [AggregatorType.RESULT_UNCACHED_DOC_COMPLETE_TIME, JOB_GROUP_1_NAME, EVENT_1_NAME, LOCATION_1_UNIQUE_IDENTIFIER, PROFILE_1_NAME].join(HIGHCHART_LEGEND_DELIMITTER),
                [AggregatorType.RESULT_UNCACHED_DOC_COMPLETE_REQUESTS, JOB_GROUP_1_NAME, EVENT_1_NAME, LOCATION_1_UNIQUE_IDENTIFIER, PROFILE_1_NAME].join(HIGHCHART_LEGEND_DELIMITTER),
                [AggregatorType.RESULT_UNCACHED_DOC_COMPLETE_TIME, JOB_GROUP_2_NAME, EVENT_2_NAME, LOCATION_2_UNIQUE_IDENTIFIER, PROFILE_2_NAME].join(HIGHCHART_LEGEND_DELIMITTER),
                [AggregatorType.RESULT_UNCACHED_DOC_COMPLETE_REQUESTS, JOB_GROUP_2_NAME, EVENT_2_NAME, LOCATION_2_UNIQUE_IDENTIFIER, PROFILE_2_NAME].join(HIGHCHART_LEGEND_DELIMITTER)
        ])
    }
    void "aggregation RAW - every legend part in every event result the same"() {
        setup:
        MOCKER.mockEventResultDaoService(serviceUnderTest,
                [
                        createEventResult("1;1${TAGPART_IRRELEVANT_FOR_LEGEND_ENTRIES};1", PROFILE_1_NAME),
                        createEventResult("2;1${TAGPART_IRRELEVANT_FOR_LEGEND_ENTRIES};1", PROFILE_1_NAME)
                ]
        )

        when:
        List<OsmChartGraph> resultGraphs = serviceUnderTest.getEventResultDashboardHighchartGraphs(
                RUN_DATE.toDate(),
                RUN_DATE.plusHours(1).toDate(),
                MeasuredValueInterval.RAW,
                [AggregatorType.findByName(AggregatorType.RESULT_UNCACHED_DOC_COMPLETE_TIME)],
                QUERY_PARAMS);

        then:
        resultGraphs.size() == 2
        List<String> graphLables = resultGraphs*.label
        graphLables.containsAll([
                [JOB_GROUP_1_NAME].join(HIGHCHART_LEGEND_DELIMITTER),
                [JOB_GROUP_2_NAME].join(HIGHCHART_LEGEND_DELIMITTER),
        ])
    }
    void "aggregation RAW - some legend parts in every event result the same, some different"() {
        setup:
        MOCKER.mockEventResultDaoService(serviceUnderTest,
                [
                        createEventResult("1;1${TAGPART_IRRELEVANT_FOR_LEGEND_ENTRIES};1", PROFILE_1_NAME),
                        createEventResult("2;1${TAGPART_IRRELEVANT_FOR_LEGEND_ENTRIES};1", PROFILE_1_NAME),
                        createEventResult("3;2${TAGPART_IRRELEVANT_FOR_LEGEND_ENTRIES};2", PROFILE_2_NAME),
                        createEventResult("4;3${TAGPART_IRRELEVANT_FOR_LEGEND_ENTRIES};3", PROFILE_3_NAME)
                ]
        )

        when:
        List<OsmChartGraph> resultGraphs = serviceUnderTest.getEventResultDashboardHighchartGraphs(
                RUN_DATE.toDate(),
                RUN_DATE.plusHours(1).toDate(),
                MeasuredValueInterval.RAW,
                [AggregatorType.findByName(AggregatorType.RESULT_UNCACHED_DOC_COMPLETE_TIME),
                AggregatorType.findByName(AggregatorType.RESULT_UNCACHED_DOC_COMPLETE_REQUESTS)],
                QUERY_PARAMS);

        then:
        resultGraphs.size() == 8
        List<String> graphLables = resultGraphs*.label
        graphLables.containsAll([
                [AggregatorType.RESULT_UNCACHED_DOC_COMPLETE_TIME, JOB_GROUP_1_NAME, EVENT_1_NAME, LOCATION_1_UNIQUE_IDENTIFIER, PROFILE_1_NAME].join(HIGHCHART_LEGEND_DELIMITTER),
                [AggregatorType.RESULT_UNCACHED_DOC_COMPLETE_TIME, JOB_GROUP_2_NAME, EVENT_1_NAME, LOCATION_1_UNIQUE_IDENTIFIER, PROFILE_1_NAME].join(HIGHCHART_LEGEND_DELIMITTER),
                [AggregatorType.RESULT_UNCACHED_DOC_COMPLETE_TIME, JOB_GROUP_3_NAME, EVENT_2_NAME, LOCATION_2_UNIQUE_IDENTIFIER, PROFILE_2_NAME].join(HIGHCHART_LEGEND_DELIMITTER),
                [AggregatorType.RESULT_UNCACHED_DOC_COMPLETE_TIME, JOB_GROUP_4_NAME, EVENT_3_NAME, LOCATION_3_UNIQUE_IDENTIFIER, PROFILE_3_NAME].join(HIGHCHART_LEGEND_DELIMITTER),
                [AggregatorType.RESULT_UNCACHED_DOC_COMPLETE_REQUESTS, JOB_GROUP_1_NAME, EVENT_1_NAME, LOCATION_1_UNIQUE_IDENTIFIER, PROFILE_1_NAME].join(HIGHCHART_LEGEND_DELIMITTER),
                [AggregatorType.RESULT_UNCACHED_DOC_COMPLETE_REQUESTS, JOB_GROUP_2_NAME, EVENT_1_NAME, LOCATION_1_UNIQUE_IDENTIFIER, PROFILE_1_NAME].join(HIGHCHART_LEGEND_DELIMITTER),
                [AggregatorType.RESULT_UNCACHED_DOC_COMPLETE_REQUESTS, JOB_GROUP_3_NAME, EVENT_2_NAME, LOCATION_2_UNIQUE_IDENTIFIER, PROFILE_2_NAME].join(HIGHCHART_LEGEND_DELIMITTER),
                [AggregatorType.RESULT_UNCACHED_DOC_COMPLETE_REQUESTS, JOB_GROUP_4_NAME, EVENT_3_NAME, LOCATION_3_UNIQUE_IDENTIFIER, PROFILE_3_NAME].join(HIGHCHART_LEGEND_DELIMITTER),
        ])
    }

    // HOURLY ////////////////////////////////////////////////////////////////////////////////////////////////////////

    void "aggregation HOURLY - no summarization possible"() {
        setup:
        MOCKER.mockEventResultDaoService(serviceUnderTest,
                [
                        createEventResult("1;1${TAGPART_IRRELEVANT_FOR_LEGEND_ENTRIES};1", PROFILE_1_NAME),
                        createEventResult("2;2${TAGPART_IRRELEVANT_FOR_LEGEND_ENTRIES};2", PROFILE_2_NAME)
                ]
        )

        when:
        List<OsmChartGraph> resultGraphs = serviceUnderTest.getEventResultDashboardHighchartGraphs(
                RUN_DATE.minusDays(7).toDate(),
                RUN_DATE.plusDays(7).toDate(),
                MeasuredValueInterval.HOURLY,
                [AggregatorType.findByName(AggregatorType.RESULT_UNCACHED_DOC_COMPLETE_TIME),
                 AggregatorType.findByName(AggregatorType.RESULT_UNCACHED_DOC_COMPLETE_REQUESTS)],
                QUERY_PARAMS);
        TestDataUtil.createHoursOfDay()

        then:
        resultGraphs.size() == 4
        List<String> graphLables = resultGraphs*.label
        graphLables.containsAll([
                [AggregatorType.RESULT_UNCACHED_DOC_COMPLETE_TIME, JOB_GROUP_1_NAME, EVENT_1_NAME, LOCATION_1_UNIQUE_IDENTIFIER, PROFILE_1_NAME].join(HIGHCHART_LEGEND_DELIMITTER),
                [AggregatorType.RESULT_UNCACHED_DOC_COMPLETE_REQUESTS, JOB_GROUP_1_NAME, EVENT_1_NAME, LOCATION_1_UNIQUE_IDENTIFIER, PROFILE_1_NAME].join(HIGHCHART_LEGEND_DELIMITTER),
                [AggregatorType.RESULT_UNCACHED_DOC_COMPLETE_TIME, JOB_GROUP_2_NAME, EVENT_2_NAME, LOCATION_2_UNIQUE_IDENTIFIER, PROFILE_2_NAME].join(HIGHCHART_LEGEND_DELIMITTER),
                [AggregatorType.RESULT_UNCACHED_DOC_COMPLETE_REQUESTS, JOB_GROUP_2_NAME, EVENT_2_NAME, LOCATION_2_UNIQUE_IDENTIFIER, PROFILE_2_NAME].join(HIGHCHART_LEGEND_DELIMITTER)
        ])
    }
    void "aggregation HOURLY - every legend part in every event result the same"() {
        setup:
        MOCKER.mockEventResultDaoService(serviceUnderTest,
                [
                        createEventResult("1;1${TAGPART_IRRELEVANT_FOR_LEGEND_ENTRIES};1", PROFILE_1_NAME),
                        createEventResult("2;1${TAGPART_IRRELEVANT_FOR_LEGEND_ENTRIES};1", PROFILE_1_NAME)
                ]
        )

        when:
        List<OsmChartGraph> resultGraphs = serviceUnderTest.getEventResultDashboardHighchartGraphs(
                RUN_DATE.minusDays(7).toDate(),
                RUN_DATE.plusDays(7).toDate(),
                MeasuredValueInterval.HOURLY,
                [AggregatorType.findByName(AggregatorType.RESULT_UNCACHED_DOC_COMPLETE_TIME)],
                QUERY_PARAMS);

        then:
        resultGraphs.size() == 2
        List<String> graphLables = resultGraphs*.label
        graphLables.containsAll([
                [JOB_GROUP_1_NAME].join(HIGHCHART_LEGEND_DELIMITTER),
                [JOB_GROUP_2_NAME].join(HIGHCHART_LEGEND_DELIMITTER),
        ])
    }
    void "aggregation HOURLY - some legend parts in every event result the same, some different"() {
        setup:
        MOCKER.mockEventResultDaoService(serviceUnderTest,
                [
                        createEventResult("1;1${TAGPART_IRRELEVANT_FOR_LEGEND_ENTRIES};1", PROFILE_1_NAME),
                        createEventResult("2;1${TAGPART_IRRELEVANT_FOR_LEGEND_ENTRIES};1", PROFILE_1_NAME),
                        createEventResult("3;2${TAGPART_IRRELEVANT_FOR_LEGEND_ENTRIES};2", PROFILE_2_NAME),
                        createEventResult("4;3${TAGPART_IRRELEVANT_FOR_LEGEND_ENTRIES};3", PROFILE_3_NAME)
                ]
        )

        when:
        List<OsmChartGraph> resultGraphs = serviceUnderTest.getEventResultDashboardHighchartGraphs(
                RUN_DATE.minusDays(7).toDate(),
                RUN_DATE.plusDays(7).toDate(),
                MeasuredValueInterval.HOURLY,
                [AggregatorType.findByName(AggregatorType.RESULT_UNCACHED_DOC_COMPLETE_TIME),
                 AggregatorType.findByName(AggregatorType.RESULT_UNCACHED_DOC_COMPLETE_REQUESTS)],
                QUERY_PARAMS);

        then:
        resultGraphs.size() == 8
        List<String> graphLables = resultGraphs*.label
        graphLables.containsAll([
                [AggregatorType.RESULT_UNCACHED_DOC_COMPLETE_TIME, JOB_GROUP_1_NAME, EVENT_1_NAME, LOCATION_1_UNIQUE_IDENTIFIER, PROFILE_1_NAME].join(HIGHCHART_LEGEND_DELIMITTER),
                [AggregatorType.RESULT_UNCACHED_DOC_COMPLETE_TIME, JOB_GROUP_2_NAME, EVENT_1_NAME, LOCATION_1_UNIQUE_IDENTIFIER, PROFILE_1_NAME].join(HIGHCHART_LEGEND_DELIMITTER),
                [AggregatorType.RESULT_UNCACHED_DOC_COMPLETE_TIME, JOB_GROUP_3_NAME, EVENT_2_NAME, LOCATION_2_UNIQUE_IDENTIFIER, PROFILE_2_NAME].join(HIGHCHART_LEGEND_DELIMITTER),
                [AggregatorType.RESULT_UNCACHED_DOC_COMPLETE_TIME, JOB_GROUP_4_NAME, EVENT_3_NAME, LOCATION_3_UNIQUE_IDENTIFIER, PROFILE_3_NAME].join(HIGHCHART_LEGEND_DELIMITTER),
                [AggregatorType.RESULT_UNCACHED_DOC_COMPLETE_REQUESTS, JOB_GROUP_1_NAME, EVENT_1_NAME, LOCATION_1_UNIQUE_IDENTIFIER, PROFILE_1_NAME].join(HIGHCHART_LEGEND_DELIMITTER),
                [AggregatorType.RESULT_UNCACHED_DOC_COMPLETE_REQUESTS, JOB_GROUP_2_NAME, EVENT_1_NAME, LOCATION_1_UNIQUE_IDENTIFIER, PROFILE_1_NAME].join(HIGHCHART_LEGEND_DELIMITTER),
                [AggregatorType.RESULT_UNCACHED_DOC_COMPLETE_REQUESTS, JOB_GROUP_3_NAME, EVENT_2_NAME, LOCATION_2_UNIQUE_IDENTIFIER, PROFILE_2_NAME].join(HIGHCHART_LEGEND_DELIMITTER),
                [AggregatorType.RESULT_UNCACHED_DOC_COMPLETE_REQUESTS, JOB_GROUP_4_NAME, EVENT_3_NAME, LOCATION_3_UNIQUE_IDENTIFIER, PROFILE_3_NAME].join(HIGHCHART_LEGEND_DELIMITTER),
        ])
    }

    // DAILY ////////////////////////////////////////////////////////////////////////////////////////////////////////

    void "aggregation DAILY - no summarization possible"() {
        setup:
        MOCKER.mockEventResultDaoService(serviceUnderTest,
                [
                        createEventResult("1;1${TAGPART_IRRELEVANT_FOR_LEGEND_ENTRIES};1", PROFILE_1_NAME),
                        createEventResult("2;2${TAGPART_IRRELEVANT_FOR_LEGEND_ENTRIES};2", PROFILE_2_NAME)
                ]
        )

        when:
        List<OsmChartGraph> resultGraphs = serviceUnderTest.getEventResultDashboardHighchartGraphs(
                RUN_DATE.minusDays(7).toDate(),
                RUN_DATE.plusDays(7).toDate(),
                MeasuredValueInterval.DAILY,
                [AggregatorType.findByName(AggregatorType.RESULT_UNCACHED_DOC_COMPLETE_TIME),
                 AggregatorType.findByName(AggregatorType.RESULT_UNCACHED_DOC_COMPLETE_REQUESTS)],
                QUERY_PARAMS);
        TestDataUtil.createHoursOfDay()

        then:
        resultGraphs.size() == 4
        List<String> graphLables = resultGraphs*.label
        graphLables.containsAll([
                [AggregatorType.RESULT_UNCACHED_DOC_COMPLETE_TIME, JOB_GROUP_1_NAME, EVENT_1_NAME, LOCATION_1_UNIQUE_IDENTIFIER, PROFILE_1_NAME].join(HIGHCHART_LEGEND_DELIMITTER),
                [AggregatorType.RESULT_UNCACHED_DOC_COMPLETE_REQUESTS, JOB_GROUP_1_NAME, EVENT_1_NAME, LOCATION_1_UNIQUE_IDENTIFIER, PROFILE_1_NAME].join(HIGHCHART_LEGEND_DELIMITTER),
                [AggregatorType.RESULT_UNCACHED_DOC_COMPLETE_TIME, JOB_GROUP_2_NAME, EVENT_2_NAME, LOCATION_2_UNIQUE_IDENTIFIER, PROFILE_2_NAME].join(HIGHCHART_LEGEND_DELIMITTER),
                [AggregatorType.RESULT_UNCACHED_DOC_COMPLETE_REQUESTS, JOB_GROUP_2_NAME, EVENT_2_NAME, LOCATION_2_UNIQUE_IDENTIFIER, PROFILE_2_NAME].join(HIGHCHART_LEGEND_DELIMITTER)
        ])
    }
    void "aggregation DAILY - every legend part in every event result the same"() {
        setup:
        MOCKER.mockEventResultDaoService(serviceUnderTest,
                [
                        createEventResult("1;1${TAGPART_IRRELEVANT_FOR_LEGEND_ENTRIES};1", PROFILE_1_NAME),
                        createEventResult("2;1${TAGPART_IRRELEVANT_FOR_LEGEND_ENTRIES};1", PROFILE_1_NAME)
                ]
        )

        when:
        List<OsmChartGraph> resultGraphs = serviceUnderTest.getEventResultDashboardHighchartGraphs(
                RUN_DATE.minusDays(7).toDate(),
                RUN_DATE.plusDays(7).toDate(),
                MeasuredValueInterval.DAILY,
                [AggregatorType.findByName(AggregatorType.RESULT_UNCACHED_DOC_COMPLETE_TIME)],
                QUERY_PARAMS);

        then:
        resultGraphs.size() == 2
        List<String> graphLables = resultGraphs*.label
        graphLables.containsAll([
                [JOB_GROUP_1_NAME].join(HIGHCHART_LEGEND_DELIMITTER),
                [JOB_GROUP_2_NAME].join(HIGHCHART_LEGEND_DELIMITTER),
        ])
    }
    void "aggregation DAILY - some legend parts in every event result the same, some different"() {
        setup:
        MOCKER.mockEventResultDaoService(serviceUnderTest,
                [
                        createEventResult("1;1${TAGPART_IRRELEVANT_FOR_LEGEND_ENTRIES};1", PROFILE_1_NAME),
                        createEventResult("2;1${TAGPART_IRRELEVANT_FOR_LEGEND_ENTRIES};1", PROFILE_1_NAME),
                        createEventResult("3;2${TAGPART_IRRELEVANT_FOR_LEGEND_ENTRIES};2", PROFILE_2_NAME),
                        createEventResult("4;3${TAGPART_IRRELEVANT_FOR_LEGEND_ENTRIES};3", PROFILE_3_NAME)
                ]
        )

        when:
        List<OsmChartGraph> resultGraphs = serviceUnderTest.getEventResultDashboardHighchartGraphs(
                RUN_DATE.minusDays(7).toDate(),
                RUN_DATE.plusDays(7).toDate(),
                MeasuredValueInterval.DAILY,
                [AggregatorType.findByName(AggregatorType.RESULT_UNCACHED_DOC_COMPLETE_TIME),
                 AggregatorType.findByName(AggregatorType.RESULT_UNCACHED_DOC_COMPLETE_REQUESTS)],
                QUERY_PARAMS);

        then:
        resultGraphs.size() == 8
        List<String> graphLables = resultGraphs*.label
        graphLables.containsAll([
                [AggregatorType.RESULT_UNCACHED_DOC_COMPLETE_TIME, JOB_GROUP_1_NAME, EVENT_1_NAME, LOCATION_1_UNIQUE_IDENTIFIER, PROFILE_1_NAME].join(HIGHCHART_LEGEND_DELIMITTER),
                [AggregatorType.RESULT_UNCACHED_DOC_COMPLETE_TIME, JOB_GROUP_2_NAME, EVENT_1_NAME, LOCATION_1_UNIQUE_IDENTIFIER, PROFILE_1_NAME].join(HIGHCHART_LEGEND_DELIMITTER),
                [AggregatorType.RESULT_UNCACHED_DOC_COMPLETE_TIME, JOB_GROUP_3_NAME, EVENT_2_NAME, LOCATION_2_UNIQUE_IDENTIFIER, PROFILE_2_NAME].join(HIGHCHART_LEGEND_DELIMITTER),
                [AggregatorType.RESULT_UNCACHED_DOC_COMPLETE_TIME, JOB_GROUP_4_NAME, EVENT_3_NAME, LOCATION_3_UNIQUE_IDENTIFIER, PROFILE_3_NAME].join(HIGHCHART_LEGEND_DELIMITTER),
                [AggregatorType.RESULT_UNCACHED_DOC_COMPLETE_REQUESTS, JOB_GROUP_1_NAME, EVENT_1_NAME, LOCATION_1_UNIQUE_IDENTIFIER, PROFILE_1_NAME].join(HIGHCHART_LEGEND_DELIMITTER),
                [AggregatorType.RESULT_UNCACHED_DOC_COMPLETE_REQUESTS, JOB_GROUP_2_NAME, EVENT_1_NAME, LOCATION_1_UNIQUE_IDENTIFIER, PROFILE_1_NAME].join(HIGHCHART_LEGEND_DELIMITTER),
                [AggregatorType.RESULT_UNCACHED_DOC_COMPLETE_REQUESTS, JOB_GROUP_3_NAME, EVENT_2_NAME, LOCATION_2_UNIQUE_IDENTIFIER, PROFILE_2_NAME].join(HIGHCHART_LEGEND_DELIMITTER),
                [AggregatorType.RESULT_UNCACHED_DOC_COMPLETE_REQUESTS, JOB_GROUP_4_NAME, EVENT_3_NAME, LOCATION_3_UNIQUE_IDENTIFIER, PROFILE_3_NAME].join(HIGHCHART_LEGEND_DELIMITTER),
        ])
    }

    // WEEKLY ////////////////////////////////////////////////////////////////////////////////////////////////////////

    void "aggregation WEEKLY - no summarization possible"() {
        setup:
        MOCKER.mockEventResultDaoService(serviceUnderTest,
                [
                        createEventResult("1;1${TAGPART_IRRELEVANT_FOR_LEGEND_ENTRIES};1", PROFILE_1_NAME),
                        createEventResult("2;2${TAGPART_IRRELEVANT_FOR_LEGEND_ENTRIES};2", PROFILE_2_NAME)
                ]
        )

        when:
        List<OsmChartGraph> resultGraphs = serviceUnderTest.getEventResultDashboardHighchartGraphs(
                RUN_DATE.minusDays(7).toDate(),
                RUN_DATE.plusDays(7).toDate(),
                MeasuredValueInterval.WEEKLY,
                [AggregatorType.findByName(AggregatorType.RESULT_UNCACHED_DOC_COMPLETE_TIME),
                 AggregatorType.findByName(AggregatorType.RESULT_UNCACHED_DOC_COMPLETE_REQUESTS)],
                QUERY_PARAMS);
        TestDataUtil.createHoursOfDay()

        then:
        resultGraphs.size() == 4
        List<String> graphLables = resultGraphs*.label
        graphLables.containsAll([
                [AggregatorType.RESULT_UNCACHED_DOC_COMPLETE_TIME, JOB_GROUP_1_NAME, EVENT_1_NAME, LOCATION_1_UNIQUE_IDENTIFIER, PROFILE_1_NAME].join(HIGHCHART_LEGEND_DELIMITTER),
                [AggregatorType.RESULT_UNCACHED_DOC_COMPLETE_REQUESTS, JOB_GROUP_1_NAME, EVENT_1_NAME, LOCATION_1_UNIQUE_IDENTIFIER, PROFILE_1_NAME].join(HIGHCHART_LEGEND_DELIMITTER),
                [AggregatorType.RESULT_UNCACHED_DOC_COMPLETE_TIME, JOB_GROUP_2_NAME, EVENT_2_NAME, LOCATION_2_UNIQUE_IDENTIFIER, PROFILE_2_NAME].join(HIGHCHART_LEGEND_DELIMITTER),
                [AggregatorType.RESULT_UNCACHED_DOC_COMPLETE_REQUESTS, JOB_GROUP_2_NAME, EVENT_2_NAME, LOCATION_2_UNIQUE_IDENTIFIER, PROFILE_2_NAME].join(HIGHCHART_LEGEND_DELIMITTER)
        ])
    }
    void "aggregation WEEKLY - every legend part in every event result the same"() {
        setup:
        MOCKER.mockEventResultDaoService(serviceUnderTest,
                [
                        createEventResult("1;1${TAGPART_IRRELEVANT_FOR_LEGEND_ENTRIES};1", PROFILE_1_NAME),
                        createEventResult("2;1${TAGPART_IRRELEVANT_FOR_LEGEND_ENTRIES};1", PROFILE_1_NAME)
                ]
        )

        when:
        List<OsmChartGraph> resultGraphs = serviceUnderTest.getEventResultDashboardHighchartGraphs(
                RUN_DATE.minusDays(7).toDate(),
                RUN_DATE.plusDays(7).toDate(),
                MeasuredValueInterval.WEEKLY,
                [AggregatorType.findByName(AggregatorType.RESULT_UNCACHED_DOC_COMPLETE_TIME)],
                QUERY_PARAMS);

        then:
        resultGraphs.size() == 2
        List<String> graphLables = resultGraphs*.label
        graphLables.containsAll([
                [JOB_GROUP_1_NAME].join(HIGHCHART_LEGEND_DELIMITTER),
                [JOB_GROUP_2_NAME].join(HIGHCHART_LEGEND_DELIMITTER),
        ])
    }
    void "aggregation WEEKLY - some legend parts in every event result the same, some different"() {
        setup:
        MOCKER.mockEventResultDaoService(serviceUnderTest,
                [
                        createEventResult("1;1${TAGPART_IRRELEVANT_FOR_LEGEND_ENTRIES};1", PROFILE_1_NAME),
                        createEventResult("2;1${TAGPART_IRRELEVANT_FOR_LEGEND_ENTRIES};1", PROFILE_1_NAME),
                        createEventResult("3;2${TAGPART_IRRELEVANT_FOR_LEGEND_ENTRIES};2", PROFILE_2_NAME),
                        createEventResult("4;3${TAGPART_IRRELEVANT_FOR_LEGEND_ENTRIES};3", PROFILE_3_NAME)
                ]
        )

        when:
        List<OsmChartGraph> resultGraphs = serviceUnderTest.getEventResultDashboardHighchartGraphs(
                RUN_DATE.minusDays(7).toDate(),
                RUN_DATE.plusDays(7).toDate(),
                MeasuredValueInterval.WEEKLY,
                [AggregatorType.findByName(AggregatorType.RESULT_UNCACHED_DOC_COMPLETE_TIME),
                 AggregatorType.findByName(AggregatorType.RESULT_UNCACHED_DOC_COMPLETE_REQUESTS)],
                QUERY_PARAMS);

        then:
        resultGraphs.size() == 8
        List<String> graphLables = resultGraphs*.label
        graphLables.containsAll([
                [AggregatorType.RESULT_UNCACHED_DOC_COMPLETE_TIME, JOB_GROUP_1_NAME, EVENT_1_NAME, LOCATION_1_UNIQUE_IDENTIFIER, PROFILE_1_NAME].join(HIGHCHART_LEGEND_DELIMITTER),
                [AggregatorType.RESULT_UNCACHED_DOC_COMPLETE_TIME, JOB_GROUP_2_NAME, EVENT_1_NAME, LOCATION_1_UNIQUE_IDENTIFIER, PROFILE_1_NAME].join(HIGHCHART_LEGEND_DELIMITTER),
                [AggregatorType.RESULT_UNCACHED_DOC_COMPLETE_TIME, JOB_GROUP_3_NAME, EVENT_2_NAME, LOCATION_2_UNIQUE_IDENTIFIER, PROFILE_2_NAME].join(HIGHCHART_LEGEND_DELIMITTER),
                [AggregatorType.RESULT_UNCACHED_DOC_COMPLETE_TIME, JOB_GROUP_4_NAME, EVENT_3_NAME, LOCATION_3_UNIQUE_IDENTIFIER, PROFILE_3_NAME].join(HIGHCHART_LEGEND_DELIMITTER),
                [AggregatorType.RESULT_UNCACHED_DOC_COMPLETE_REQUESTS, JOB_GROUP_1_NAME, EVENT_1_NAME, LOCATION_1_UNIQUE_IDENTIFIER, PROFILE_1_NAME].join(HIGHCHART_LEGEND_DELIMITTER),
                [AggregatorType.RESULT_UNCACHED_DOC_COMPLETE_REQUESTS, JOB_GROUP_2_NAME, EVENT_1_NAME, LOCATION_1_UNIQUE_IDENTIFIER, PROFILE_1_NAME].join(HIGHCHART_LEGEND_DELIMITTER),
                [AggregatorType.RESULT_UNCACHED_DOC_COMPLETE_REQUESTS, JOB_GROUP_3_NAME, EVENT_2_NAME, LOCATION_2_UNIQUE_IDENTIFIER, PROFILE_2_NAME].join(HIGHCHART_LEGEND_DELIMITTER),
                [AggregatorType.RESULT_UNCACHED_DOC_COMPLETE_REQUESTS, JOB_GROUP_4_NAME, EVENT_3_NAME, LOCATION_3_UNIQUE_IDENTIFIER, PROFILE_3_NAME].join(HIGHCHART_LEGEND_DELIMITTER),
        ])
    }

    EventResult createEventResult(String tag, String connectivityProfileName){
        ConnectivityProfile connProfile = ConnectivityProfile.findByName(connectivityProfileName) ?: TestDataUtil.createConnectivityProfile(connectivityProfileName)
        EventResult result = TestDataUtil.createEventResult(
                Job.findByLabel(JOB_LABEL),
                JobResult.findByTestId(TEST_ID),
                DOC_COMPLETE_TIME,
                CUSTOMER_SATISFACTION,
                MeasuredEvent.findByName(EVENT_1_NAME),
                connProfile
        )
        result.tag = tag
        result.docCompleteRequests = DOC_COMPLETE_REQUESTS
        return result.save(failOnError: true)
    }

    void prepareMocksCommonForAllTests() {
        serviceUnderTest.resultMeasuredValueService = new ResultMeasuredValueService()
        serviceUnderTest.resultMeasuredValueService.eventResultDaoService = new EventResultDaoService()
        MOCKER.mockLinkGenerator(serviceUnderTest, 'http://not-the-concern-of-this-test.org')
        serviceUnderTest.jobResultDaoService = new JobResultDaoService()
        MOCKER.mockI18nService(serviceUnderTest)
        MOCKER.mockPerformanceLoggingService(serviceUnderTest)
        serviceUnderTest.measuredValueTagService = new MeasuredValueTagService()
        serviceUnderTest.measuredValueUtilService = new MeasuredValueUtilService()
        serviceUnderTest.aggregatorTypeDaoService = new DefaultAggregatorTypeDaoService()
    }

    void createTestDataCommonForAllTests() {

        TestDataUtil.createAggregatorType(AggregatorType.RESULT_UNCACHED_DOC_COMPLETE_TIME, MeasurandGroup.LOAD_TIMES)
        TestDataUtil.createAggregatorType(AggregatorType.RESULT_UNCACHED_DOC_COMPLETE_REQUESTS, MeasurandGroup.REQUEST_COUNTS)
        TestDataUtil.createAggregatorType(AggregatorType.RESULT_UNCACHED_CUSTOMER_SATISFACTION_IN_PERCENT, MeasurandGroup.PERCENTAGES)

        TestDataUtil.createMeasuredValueIntervals()
        List<Browser> browsers = TestDataUtil.createBrowsersAndAliases()

        WebPageTestServer server = TestDataUtil.createWebPageTestServer('server', 'proxyIdentifier', true, 'http://baseurl.org')

        Location location1 = TestDataUtil.createLocation(server, LOCATION_1_UNIQUE_IDENTIFIER, browsers[0], true)
        TestDataUtil.createLocation(server, LOCATION_2_UNIQUE_IDENTIFIER, browsers[0], true)
        TestDataUtil.createLocation(server, LOCATION_3_UNIQUE_IDENTIFIER, browsers[0], true)
        TestDataUtil.createLocation(server, LOCATION_4_UNIQUE_IDENTIFIER, browsers[0], true)

        JobGroup jobGroup1 = TestDataUtil.createJobGroup(JOB_GROUP_1_NAME, JobGroupType.RAW_DATA_SELECTION)
        TestDataUtil.createJobGroup(JOB_GROUP_2_NAME, JobGroupType.RAW_DATA_SELECTION)
        TestDataUtil.createJobGroup(JOB_GROUP_3_NAME, JobGroupType.RAW_DATA_SELECTION)
        TestDataUtil.createJobGroup(JOB_GROUP_4_NAME, JobGroupType.RAW_DATA_SELECTION)

        Page page = TestDataUtil.createPage('pageName', 12d)

        TestDataUtil.createMeasuredEvent(EVENT_1_NAME, page)
        TestDataUtil.createMeasuredEvent(EVENT_2_NAME, page)
        TestDataUtil.createMeasuredEvent(EVENT_3_NAME, page)
        TestDataUtil.createMeasuredEvent(EVENT_4_NAME, page)

        Script script = Script.createDefaultScript('Unnamed').save(failOnError: true)

        Job job = TestDataUtil.createJob(JOB_LABEL, script, location1, jobGroup1, 'job description', 1, false, 60)

        TestDataUtil.createJobResult(TEST_ID, RUN_DATE.toDate(), job, location1)
    }

}

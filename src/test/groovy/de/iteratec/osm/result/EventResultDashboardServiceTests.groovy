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

import de.iteratec.osm.csi.Page
import de.iteratec.osm.dao.CriteriaSorting
import de.iteratec.osm.measurement.environment.*
import de.iteratec.osm.measurement.schedule.*
import de.iteratec.osm.measurement.script.Script
import de.iteratec.osm.report.chart.*
import de.iteratec.osm.result.dao.EventResultDaoService
import de.iteratec.osm.util.I18nService
import de.iteratec.osm.util.PerformanceLoggingService
import de.iteratec.osm.util.PerformanceLoggingService.LogLevel
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import grails.web.mapping.LinkGenerator
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import spock.lang.Specification

import static org.junit.Assert.*

/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
@TestMixin(GrailsUnitTestMixin)
@TestFor(EventResultDashboardService)
@Mock([Job, JobResult, MeasuredEvent, CsiAggregation, CsiAggregationInterval, Location, Browser, BrowserAlias, Page, JobGroup, AggregatorType, WebPageTestServer, EventResult, Script])
class EventResultDashboardServiceTests extends Specification {

    EventResultDashboardService serviceUnderTest

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
    final static String location1Label = "ffLocationLabel"
    final static String location2Label = "ieLocationLabel"
    final static String location1Location = "ffLocationLocation"
    final static String location2Location = "ieLocationLocation"
    final static String predefinedConnectivityName = "DSL 6.000"
    public static final String I18N_LABEL_JOB_GROUP = 'Job Group'
    public static final String I18N_LABEL_MEASURED_EVENT = 'Measured step'
    public static final String I18N_LABEL_LOCATION = 'Location'
    public static final String I18N_LABEL_MEASURAND = 'Measurand'
    public static final String I18N_LABEL_CONNECTIVITY = 'Connectivity'

    def doWithSpring = {
        resultCsiAggregationService(ResultCsiAggregationService)
        eventResultDaoService(EventResultDaoService)
        osmChartProcessingService(OsmChartProcessingService)
        eventResultDaoService(EventResultDaoService)
        performanceLoggingService(PerformanceLoggingService)
        csiAggregationUtilService(CsiAggregationUtilService)
        defaultJobGroupDaoService(DefaultJobGroupDaoService)
        defaultPageDaoService(DefaultPageDaoService)
        defaultBrowserDaoService(DefaultBrowserDaoService)
        defaultLocationDaoService(DefaultLocationDaoService)
        i18nService(I18nService)
        defaultAggregatorTypeDaoService(DefaultAggregatorTypeDaoService)
    }

    void setup() {

        serviceUnderTest = service;
        runDate = new DateTime(2013, 5, 29, 10, 13, 2, 564, DateTimeZone.UTC)
        runDateHourlyStart = new DateTime(2013, 5, 29, 10, 0, 0, 0, DateTimeZone.UTC)

        mocksCommonToAllTests()
        createTestdataCommonForAllTests()

    }

    void testGetEventResultDashboardChartMap_RAW_DATA_CACHED() {
        given:
        mockEventResultDaoService()
        mockPerformanceLoggingService()
        mockJobGroupDaoService()
        mockPageDaoService()
        mockBrowserDaoService()
        mockLocationDaoService()

        ErQueryParams queryParams = new ErQueryParams();
        queryParams.browserIds.add(browser.id)
        queryParams.jobGroupIds.add(jobGroup.id)
        queryParams.locationIds.add(location.id)
        queryParams.measuredEventIds.add(measuredEvent.id)
        queryParams.pageIds.add(page.id)

        Date startTime = runDate.withMinuteOfHour(0).withSecondOfMinute(0).toDate()
        Date endTime = runDate.withMinuteOfHour(15).withSecondOfMinute(35).toDate()
        when:
        Collection<AggregatorType> aggregatorTypes = AggregatorType.findAllByName(AggregatorType.RESULT_CACHED_DOM_TIME) as List
        OsmRickshawChart chart = serviceUnderTest.getEventResultDashboardHighchartGraphs(startTime, endTime, CsiAggregationInterval.RAW, aggregatorTypes, queryParams);
        List<OsmChartGraph> resultGraphs = chart.osmChartGraphs
        List<OsmChartGraph> resultGraphsWithCorrectLabel = resultGraphs.findAll {
            it.label == "${AggregatorType.RESULT_CACHED_DOM_TIME} | ${group1Name} | ${event1Name} | ${location1Location} | ${predefinedConnectivityName}"
        }
        then:
        assertEquals(1, resultGraphs.size())
        assertEquals(1, resultGraphsWithCorrectLabel.size())
        assertEquals(1, resultGraphsWithCorrectLabel.findAll { it.measurandGroup == MeasurandGroup.LOAD_TIMES }.size())
        assertTrue(resultGraphsWithCorrectLabel[0].points.findAll({ it.csiAggregation == 2.0d }).size() == 1);
    }

    void testGetEventResultDashboardChartMap_RAW_DATA_CACHED_AND_UNCACHED() {
        given:
        mockEventResultDaoService()
        mockPerformanceLoggingService()
        mockJobGroupDaoService()
        mockPageDaoService()
        mockBrowserDaoService()
        mockLocationDaoService()

        ErQueryParams queryParams = new ErQueryParams();
        queryParams.browserIds.add(browser.id)
        queryParams.jobGroupIds.add(jobGroup.id)
        queryParams.locationIds.add(location.id)
        queryParams.measuredEventIds.add(measuredEvent.id)
        queryParams.pageIds.add(page.id)

        Date startTime = runDate.withMinuteOfHour(0).withSecondOfMinute(0).toDate()
        Date endTime = runDate.withMinuteOfHour(15).withSecondOfMinute(35).toDate()

        Collection<AggregatorType> aggregatorTypes = []
        aggregatorTypes.add(AggregatorType.findAllByName(AggregatorType.RESULT_CACHED_DOM_TIME))
        aggregatorTypes.add(AggregatorType.findAllByName(AggregatorType.RESULT_UNCACHED_DOM_TIME))

        assertEquals(2, aggregatorTypes.size());
        when:
        OsmRickshawChart chart = serviceUnderTest.getEventResultDashboardHighchartGraphs(startTime, endTime, CsiAggregationInterval.RAW, aggregatorTypes, queryParams);
        List<OsmChartGraph> resultGraphs = chart.osmChartGraphs
        /**
         * IT-643
         *
         * Removed page name and location to match the result of summarizeLabel @Link
         */
        List<OsmChartGraph> resultsCsi1 = resultGraphs.findAll {
            it.label == "${AggregatorType.RESULT_CACHED_DOM_TIME} | ${group1Name}"
        }
        List<OsmChartGraph> resultsCsi2 = resultGraphs.findAll {
            it.label == "${AggregatorType.RESULT_UNCACHED_DOM_TIME} | ${group2Name}"
        }
        then:
        assertEquals(2, resultGraphs.size())

        assertEquals(1, resultsCsi1.size())
        assertEquals(1, resultsCsi1.findAll { it.measurandGroup == MeasurandGroup.LOAD_TIMES }.size())
        assertTrue(resultsCsi1[0].points.findAll({ it.csiAggregation == 2.0d }).size() == 1);

        assertEquals(1, resultsCsi2.size())
        assertEquals(1, resultsCsi2.findAll { it.measurandGroup == MeasurandGroup.LOAD_TIMES }.size())
        assertTrue(resultsCsi2[0].points.findAll({ it.csiAggregation == 20.0d }).size() == 1);
    }

    void testGetEventResultDashboardChartMap_RAW_DATA_CACHED_AND_UNCACHED_LIMITED_MIN() {
        given:
        mockEventResultDaoService()
        mockPerformanceLoggingService()
        mockJobGroupDaoService()
        mockPageDaoService()
        mockBrowserDaoService()
        mockLocationDaoService()

        ErQueryParams queryParams = new ErQueryParams();
        queryParams.browserIds.add(browser.id)
        queryParams.jobGroupIds.add(jobGroup.id)
        queryParams.locationIds.add(location.id)
        queryParams.measuredEventIds.add(measuredEvent.id)
        queryParams.pageIds.add(page.id)
        queryParams.minLoadTimeInMillisecs = 5.0d

        Date startTime = runDate.withMinuteOfHour(0).withSecondOfMinute(0).toDate()
        Date endTime = runDate.withMinuteOfHour(15).withSecondOfMinute(35).toDate()

        Collection<AggregatorType> aggregatorTypes = []
//		aggregatorTypes.add(AggregatorType.findAllByName(AggregatorType.RESULT_CACHED_DOM_TIME))
        aggregatorTypes.add(AggregatorType.findAllByName(AggregatorType.RESULT_UNCACHED_DOM_TIME))

        assertEquals(1, aggregatorTypes.size());
        when:
        OsmRickshawChart chart = serviceUnderTest.getEventResultDashboardHighchartGraphs(startTime, endTime, CsiAggregationInterval.RAW, aggregatorTypes, queryParams);
        List<OsmChartGraph> resultGraphs = chart.osmChartGraphs

        List<OsmChartGraph> resultsCsi1 = resultGraphs.findAll {
            it.label == "${AggregatorType.RESULT_UNCACHED_DOM_TIME} | ${group1Name} | ${event1Name} | ${location1Location} | ${predefinedConnectivityName}"
        }

        List<OsmChartGraph> resultsCsi2 = resultGraphs.findAll {
            it.label == "${AggregatorType.RESULT_UNCACHED_DOM_TIME} | ${group2Name} | ${event1Name} | ${location1Location} | ${predefinedConnectivityName}"
        }
        then:
        assertEquals(1, resultGraphs.size())
        assertEquals(0, resultsCsi1.size())
        assertEquals(1, resultsCsi2.size())
        assertEquals(1, resultsCsi2.findAll { it.measurandGroup == MeasurandGroup.LOAD_TIMES }.size())
        assertTrue(resultsCsi2[0].points.findAll({ it.csiAggregation == 20.0d }).size() == 1);
    }

    void testGetEventResultDashboardChartMap_RAW_DATA_CACHED_AND_UNCACHED_LIMITED_MAX() {
        given:
        mockEventResultDaoService()
        mockPerformanceLoggingService()
        mockJobGroupDaoService()
        mockPageDaoService()
        mockBrowserDaoService()
        mockLocationDaoService()

        ErQueryParams queryParams = new ErQueryParams();
        queryParams.browserIds.add(browser.id)
        queryParams.jobGroupIds.add(jobGroup.id)
        queryParams.locationIds.add(location.id)
        queryParams.measuredEventIds.add(measuredEvent.id)
        queryParams.pageIds.add(page.id)
        queryParams.maxLoadTimeInMillisecs = 15.0d

        Date startTime = runDate.withMinuteOfHour(0).withSecondOfMinute(0).toDate()
        Date endTime = runDate.withMinuteOfHour(15).withSecondOfMinute(35).toDate()

        Collection<AggregatorType> aggregatorTypes = []
        aggregatorTypes.add(AggregatorType.findAllByName(AggregatorType.RESULT_CACHED_DOM_TIME))

        assertEquals(1, aggregatorTypes.size());
        when:
        OsmRickshawChart chart = serviceUnderTest.getEventResultDashboardHighchartGraphs(startTime, endTime, CsiAggregationInterval.RAW, aggregatorTypes, queryParams);
        List<OsmChartGraph> resultGraphs = chart.osmChartGraphs

        List<OsmChartGraph> resultsCsi1 = resultGraphs.findAll {
            it.label == "${AggregatorType.RESULT_CACHED_DOM_TIME} | ${group1Name} | ${event1Name} | ${location1Location} | ${predefinedConnectivityName}"
        }

        List<OsmChartGraph> resultsCsi2 = resultGraphs.findAll {
            it.label == "${AggregatorType.RESULT_CACHED_DOM_TIME} | ${group2Name} | ${event1Name} | ${location1Location} | ${predefinedConnectivityName}"
        }
        then:
        assertEquals(1, resultGraphs.size())
        assertEquals(1, resultsCsi1.size())
        assertEquals(1, resultsCsi1.findAll { it.measurandGroup == MeasurandGroup.LOAD_TIMES }.size())
        assertTrue(resultsCsi1[0].points.findAll({ it.csiAggregation == 2.0d }).size() == 1);
        assertEquals(0, resultsCsi2.size())
    }

    void testGetEventResultDashboardChartMap_AGGREGATED_DATA_CACHED() {
        given:
        //mocks
        mockEventResultDaoService()
        mockPerformanceLoggingService()
        mockCsiAggregationUtilService()
        mockJobGroupDaoService()
        mockPageDaoService()
        mockBrowserDaoService()
        mockLocationDaoService()
        mockAggregatorTypeDaoService()

        //test-specific data
        ErQueryParams queryParams = new ErQueryParams();
        queryParams.browserIds.add(browser.id)
        queryParams.jobGroupIds.add(jobGroup.id)
        queryParams.locationIds.add(location.id)
        queryParams.measuredEventIds.add(measuredEvent.id)
        queryParams.pageIds.add(page.id)

        Date startTime = runDate.minusDays(5).toDate()
        Date endTime = runDate.plusDays(5).toDate()

        Collection<AggregatorType> aggregatorTypes = AggregatorType.findAllByName(AggregatorType.RESULT_CACHED_DOM_TIME) as List

        when:
        OsmRickshawChart chart = serviceUnderTest.getEventResultDashboardHighchartGraphs(startTime, endTime, CsiAggregationInterval.HOURLY, aggregatorTypes, queryParams);
        List<OsmChartGraph> resultGraphs = chart.osmChartGraphs
        List<OsmChartGraph> resultGraphsWithCorrectLabel = resultGraphs.findAll {
            it.label == "${AggregatorType.RESULT_CACHED_DOM_TIME} | ${group1Name} | ${event1Name} | ${location1Location} | ${predefinedConnectivityName}"
        }

        then:
        assertEquals(1, resultGraphs.size())
        assertEquals(1, resultGraphsWithCorrectLabel.size())
        assertEquals(1, resultGraphsWithCorrectLabel.findAll { it.measurandGroup == MeasurandGroup.LOAD_TIMES }.size())
        assertTrue(resultGraphsWithCorrectLabel[0].points.findAll({ it.csiAggregation == 2.0d }).size() == 1);
    }

    void testGetEventResultDashboardChartMap_AGGREGATED_DATA_CACHED_AND_UNCACHED() {
        given:
        //mocks
        mockEventResultDaoService()
        mockPerformanceLoggingService()
        mockCsiAggregationUtilService()
        mockJobGroupDaoService()
        mockPageDaoService()
        mockBrowserDaoService()
        mockLocationDaoService()
        mockAggregatorTypeDaoService()

        //test-specific data
        ErQueryParams queryParams = new ErQueryParams();
        queryParams.browserIds.add(browser.id)
        queryParams.jobGroupIds.add(jobGroup.id)
        queryParams.locationIds.add(location.id)
        queryParams.measuredEventIds.add(measuredEvent.id)
        queryParams.pageIds.add(page.id)

        Date startTime = runDate.minusDays(5).toDate()
        Date endTime = runDate.plusDays(5).toDate()

        Collection<AggregatorType> aggregatorTypes = []
        aggregatorTypes.add(AggregatorType.findAllByName(AggregatorType.RESULT_CACHED_DOM_TIME))
        aggregatorTypes.add(AggregatorType.findAllByName(AggregatorType.RESULT_UNCACHED_DOM_TIME))
        assertEquals(2, aggregatorTypes.size());

        when:
        OsmRickshawChart chart = serviceUnderTest.getEventResultDashboardHighchartGraphs(startTime, endTime, CsiAggregationInterval.HOURLY, aggregatorTypes, queryParams);
        List<OsmChartGraph> resultGraphs = chart.osmChartGraphs
        List<OsmChartGraph> resultsCsi1 = resultGraphs.findAll {
            it.label == "${AggregatorType.RESULT_CACHED_DOM_TIME} | ${group1Name}"
        }
        List<OsmChartGraph> resultsCsi2 = resultGraphs.findAll {
            it.label == "${AggregatorType.RESULT_UNCACHED_DOM_TIME} | ${group2Name}"
        }

        then:
        assertEquals(2, resultGraphs.size())

        assertEquals(1, resultsCsi1.size())
        assertEquals(1, resultsCsi1.findAll { it.measurandGroup == MeasurandGroup.LOAD_TIMES }.size())
        assertTrue(resultsCsi1[0].points.findAll({ it.csiAggregation == 2.0d }).size() == 1);

        assertEquals(1, resultsCsi2.size())
        assertEquals(1, resultsCsi2.findAll { it.measurandGroup == MeasurandGroup.LOAD_TIMES }.size())
        assertTrue(resultsCsi2[0].points.findAll({ it.csiAggregation == 20.0d }).size() == 1);
    }

    void tryToBuildTestsDetailsURL() {
        given:
        // Create some data:
        final CsiAggregation csiAggregation = new CsiAggregation(
                underlyingEventResultsByWptDocComplete: resultIDs
        ) {
            public Long getId() {
                return 4;
            }
        };
        assertEquals(resultIDsCount, csiAggregation.countUnderlyingEventResultsByWptDocComplete());

        def linkGeneratorMock = Mock(LinkGenerator)
        serviceUnderTest.grailsLinkGenerator = linkGeneratorMock

        when:
        // Run the (whitebox-)test:
        URL result = serviceUnderTest.tryToBuildTestsDetailsURL(csiAggregation);

        then:
        // Verify result:
        // - is the URL the expected one?
        1 * linkGeneratorMock.link({
            it['controller'] == 'highchartPointDetails' &&
                    it['action'] == 'listAggregatedResults' &&
                    it['absolute'] == true &&
                    (it['params'] as Map)['csiAggregationId'] == '4' &&
                    (it['params'] as Map)['lastKnownCountOfAggregatedResultsOrNull'] == String.valueOf(resultIDsCount)
        }) >> expectedURL
        assertEquals(expectedURL, result)

        where:
        resultIDs | expectedURL                                                                                  || resultIDsCount
        '1,2'     | new URL('http://wptserver.example.com/testTryToBuildTestsDetailsURL_TwoResults')             || 2
        '1'       | new URL('http://wptserver.example.com/result/testTryToBuildTestsDetailsURL_OneSingleResult') || 1
    }


    private void mocksCommonToAllTests() {
        serviceUnderTest.resultCsiAggregationService = grailsApplication.mainContext.getBean('resultCsiAggregationService')
        serviceUnderTest.resultCsiAggregationService.eventResultDaoService = grailsApplication.mainContext.getBean('eventResultDaoService')
        serviceUnderTest.osmChartProcessingService = grailsApplication.mainContext.getBean('osmChartProcessingService')
        serviceUnderTest.grailsLinkGenerator = Stub(LinkGenerator)
        serviceUnderTest.osmChartProcessingService.i18nService = Stub(I18nService) {
            msg(_, _, _) >> { String msgKey, String defaultMessage = null, List objs = null ->
                return [
                        'job.jobGroup.label'                         : I18N_LABEL_JOB_GROUP,
                        'de.iteratec.osm.result.measured-event.label': I18N_LABEL_MEASURED_EVENT,
                        'job.location.label'                         : I18N_LABEL_LOCATION,
                        'de.iteratec.result.measurand.label'         : I18N_LABEL_MEASURAND,
                        'de.iteratec.osm.result.connectivity.label'  : I18N_LABEL_CONNECTIVITY
                ].get(msgKey)
            }
        }
        serviceUnderTest.i18nService = Stub(I18nService) {
            msg(_, _, _) >> { String msgKey, String defaultMessage = null, List objs = null ->
                return defaultMessage
            }
        }
    }

    private void createTestdataCommonForAllTests() {
        createCsiAggregationInterval();
        createBrowser();
        createLocations();
        createCSIGroups();
        createPages();
        createMeasuredEvents();
        createJobResults();
        createEventResults();
    }

    void createCsiAggregationInterval() {
        new CsiAggregationInterval(
                name: "RAW",
                intervalInMinutes: CsiAggregationInterval.RAW
        ).save(failOnError: true)
        new CsiAggregationInterval(
                name: "HOURLY",
                intervalInMinutes: CsiAggregationInterval.HOURLY
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
        browser = new Browser(name: "Test")
                .addToBrowserAliases(new BrowserAlias(alias: "Test"))
                .save(failOnError: true)
        new Browser(name: "Test2")
                .addToBrowserAliases(new BrowserAlias(alias: "Test2"))
                .save(failOnError: true)
    }

    void createLocations() {
        server = new WebPageTestServer(
                baseUrl: 'http://server1.wpt.server.de',
                active: true,
                label: 'server 1 - wpt server',
                proxyIdentifier: 'server 1 - wpt server',
                dateCreated: new Date(),
                lastUpdated: new Date()
        ).save(failOnError: true)

        location = new Location(
                active: true,
                location: location1Location,
                label: location1Label,
                browser: browser,
                wptServer: server,
                dateCreated: new Date(),
                lastUpdated: new Date()
        ).save(failOnError: true)

        Browser browser2 = Browser.findByName("Test2")
        assertNotNull(browser2)

        Location ieAgent1 = new Location(
                active: true,
                location: location2Location,
                label: location2Label,
                browser: browser2,
                wptServer: server,
                dateCreated: new Date(),
                lastUpdated: new Date()
        ).save(failOnError: true)
    }

    void createCSIGroups() {
        jobGroup = new JobGroup(
                name: group1Name).save(failOnError: true);
        new JobGroup(
                name: group2Name).save(failOnError: true)
    }

    void createMeasuredEvents() {
        measuredEvent = new MeasuredEvent(
                name: event1Name,
                testedPage: page).save(failOnError: true);
        new MeasuredEvent(
                name: event2Name,
                testedPage: page).save(failOnError: true)
    }

    void createPages() {
        page = new Page(name: page1Name).save(failOnError: true)
        new Page(name: page2Name).save(failOnError: true)
    }

    void createJobResults() {
        Script script = Script.createDefaultScript('Unnamed').save(failOnError: true)

        Job parentJob = new Job(
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

        jobResult = new JobResult(
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

        jobResult2 = new JobResult(
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

    ConnectivityProfile mockConnectivity = new ConnectivityProfile(
            name: predefinedConnectivityName,
            bandwidthDown: 0,
            bandwidthUp: 0,
            latency: 0,
            packetLoss: 0)

    private void createEventResults() {
        eventResultCached = new EventResult(
                numberOfWptRun: 1,
                cachedView: CachedView.CACHED,
                medianValue: true,
                wptStatus: 200,
                docCompleteTimeInMillisecs: 1,
                domTimeInMillisecs: 2,
                csByWptDocCompleteInPercent: 0,
                jobResult: jobResult,
                jobResultDate: jobResult.date,
                jobResultJobConfigId: jobResult.job.ident(),
                jobGroup: jobGroup,
                measuredEvent: measuredEvent,
                page: page,
                browser: browser,
                location: location,
                speedIndex: EventResult.SPEED_INDEX_DEFAULT_VALUE,
                connectivityProfile: mockConnectivity,
                customConnectivityName: null,
                noTrafficShapingAtAll: false
        ).save(failOnError: true)

        jobResult.save(failOnError: true)

        eventResultUncached = new EventResult(
                numberOfWptRun: 1,
                cachedView: CachedView.UNCACHED,
                medianValue: true,
                wptStatus: 200,
                docCompleteTimeInMillisecs: 10,
                domTimeInMillisecs: 20,
                csByWptDocCompleteInPercent: 0,
                jobResult: jobResult2,
                jobResultDate: jobResult2.date,
                jobResultJobConfigId: jobResult2.job.ident(),
                jobGroup: JobGroup.findByName(group2Name),
                measuredEvent: measuredEvent,
                page: page,
                browser: browser,
                location: location,
                speedIndex: EventResult.SPEED_INDEX_DEFAULT_VALUE,
                connectivityProfile: mockConnectivity,
                customConnectivityName: null,
                noTrafficShapingAtAll: false
        ).save(failOnError: true)

        jobResult2.save(failOnError: true)
    }

    //mocking inner services////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Mocks {@linkplain EventCsiAggregationService#eventResultDaoService}.
     */
    private void mockEventResultDaoService() {
        def eventResultDaoService = grailsApplication.mainContext.getBean('eventResultDaoService')
        eventResultDaoService.metaClass.getLimitedMedianEventResultsBy = {
            Date fromDate,
            Date toDate,
            Set<CachedView> cachedViews,
            ErQueryParams queryParams,
            Map listCriteriaRestrictionMap,
            CriteriaSorting sorting ->

                List<EventResult> results = []
                if (cachedViews.contains(CachedView.CACHED)) {
                    if (!queryParams.minLoadTimeInMillisecs && !queryParams.maxLoadTimeInMillisecs) {
                        results.add(eventResultCached)
                    } else {
                        if (queryParams.minLoadTimeInMillisecs && eventResultCached.domTimeInMillisecs > queryParams.minLoadTimeInMillisecs) {
                            results.add(eventResultCached)
                        }
                        if (queryParams.maxLoadTimeInMillisecs && eventResultCached.domTimeInMillisecs < queryParams.maxLoadTimeInMillisecs) {
                            results.add(eventResultCached)
                        }
                    }
                }
                if (cachedViews.contains(CachedView.UNCACHED)) {
                    if (!queryParams.minLoadTimeInMillisecs && !queryParams.maxLoadTimeInMillisecs) {
                        results.add(eventResultUncached)
                    } else {
                        if (queryParams.minLoadTimeInMillisecs && eventResultUncached.domTimeInMillisecs > queryParams.minLoadTimeInMillisecs) {
                            results.add(eventResultUncached)
                        }
                        if (queryParams.maxLoadTimeInMillisecs && eventResultUncached.domTimeInMillisecs < queryParams.maxLoadTimeInMillisecs) {
                            results.add(eventResultUncached)
                        }
                    }
                }
                return results
        }
        eventResultDaoService.metaClass.tryToFindById = {
            long databaseId ->
                return databaseId == 1 ?
                        eventResultCached :
                        eventResultUncached
        }
        serviceUnderTest.eventResultDaoService = eventResultDaoService
    }
    /**
     * Mocks {@linkplain EventCsiAggregationService#performanceLoggingService}.
     */
    private void mockPerformanceLoggingService() {
        def performanceLoggingService = grailsApplication.mainContext.getBean('performanceLoggingService')
        performanceLoggingService.metaClass.logExecutionTime = {
            LogLevel level, String description, Integer indentationDepth, Closure toMeasure ->
                toMeasure.call()
        }
        serviceUnderTest.performanceLoggingService = performanceLoggingService
    }

    /**
     * Mocks {@linkplain EventCsiAggregationService#performanceLoggingService}.
     */
    private void mockCsiAggregationUtilService() {
        def csiAggregationUtilService = grailsApplication.mainContext.getBean('csiAggregationUtilService')
        csiAggregationUtilService.metaClass.resetToStartOfActualInterval = {
            DateTime dateWithinInterval, Integer intervalInMinutes ->
                return runDateHourlyStart
        }
        serviceUnderTest.csiAggregationUtilService = csiAggregationUtilService
    }
    /**
     * Mocks {@linkplain EventCsiAggregationService#jobGroupDaoService}.
     */
    private void mockJobGroupDaoService() {
        def jobGroupDaoService = grailsApplication.mainContext.getBean('defaultJobGroupDaoService')
        jobGroupDaoService.metaClass.getIdToObjectMap = { ->
            return [1: JobGroup.get(1), 2: JobGroup.get(2)]
        }
        serviceUnderTest.jobGroupDaoService = jobGroupDaoService
    }
    /**
     * Mocks {@linkplain EventCsiAggregationService#pageDaoService}.
     */
    private void mockPageDaoService() {
        def pageDaoService = grailsApplication.mainContext.getBean('defaultPageDaoService')
        pageDaoService.metaClass.getIdToObjectMap = { ->
            return [1: Page.get(1), 2: Page.get(2)]
        }
        serviceUnderTest.pageDaoService = pageDaoService
    }
    /**
     * Mocks {@linkplain EventCsiAggregationService#browserDaoService}.
     */
    private void mockBrowserDaoService() {
        def browserDaoService = grailsApplication.mainContext.getBean('defaultBrowserDaoService')
        browserDaoService.metaClass.getIdToObjectMap = { ->
            return [1: Browser.get(1), 2: Browser.get(2)]
        }
        serviceUnderTest.browserDaoService = browserDaoService
    }
    /**
     * Mocks {@linkplain EventCsiAggregationService#locationDaoService}.
     */
    private void mockLocationDaoService() {
        def locationDaoService = grailsApplication.mainContext.getBean('defaultLocationDaoService')
        locationDaoService.metaClass.getIdToObjectMap = { ->
            return [1: Location.get(1), 2: Location.get(2)]
        }
        serviceUnderTest.locationDaoService = locationDaoService
    }

    private mockAggregatorTypeDaoService() {
        def aggregatorTypeDaoService = grailsApplication.mainContext.getBean('defaultAggregatorTypeDaoService')
        aggregatorTypeDaoService.metaClass.getNameToObjectMap = { ->
            Map<String, AggregatorType> map = [
                    (AggregatorType.RESULT_CACHED_DOM_TIME)  : AggregatorType.findByName(AggregatorType.RESULT_CACHED_DOM_TIME),
                    (AggregatorType.RESULT_UNCACHED_DOM_TIME): AggregatorType.findByName(AggregatorType.RESULT_UNCACHED_DOM_TIME)
            ]
            return map
        }
        serviceUnderTest.aggregatorTypeDaoService = aggregatorTypeDaoService
    }
}
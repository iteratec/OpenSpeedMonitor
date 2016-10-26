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

package de.iteratec.osm.measurement.environment.wptserverproxy

import de.iteratec.osm.csi.*
import de.iteratec.osm.measurement.environment.*
import de.iteratec.osm.measurement.schedule.Job
import de.iteratec.osm.measurement.schedule.JobDaoService
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.measurement.script.Script
import de.iteratec.osm.report.external.MetricReportingService
import de.iteratec.osm.result.*
import de.iteratec.osm.util.PerformanceLoggingService
import de.iteratec.osm.util.ServiceMocker
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import org.junit.Before
import org.junit.Test

import static de.iteratec.osm.result.CachedView.CACHED
import static de.iteratec.osm.result.CachedView.UNCACHED
import static org.junit.Assert.*

/**
 * Tests the saving of EventResults.
 * Testing the mapping of load-times to customer satisfactions or the persisting of dependent {@link CsiAggregation}s is not the concern of the tests in this class.
 * @author nkuhn
 * @see {@link ProxyService}
 *
 */
@TestFor(ResultPersisterService)
@Mock([WebPageTestServer, Browser, Location, Job, JobResult, EventResult, BrowserAlias, Page, MeasuredEvent, JobGroup, Script, CsiConfiguration, TimeToCsMapping, CsiDay])
class PersistingNewEventResultsTests {

    private static final ServiceMocker SERVICE_MOCKER = ServiceMocker.create()

    WebPageTestServer server1, server2
    JobGroup undefinedJobGroup;
    Browser undefinedBrowser;

    ResultPersisterService serviceUnderTest

    def doWithSpring = {
        metricReportingService(MetricReportingService)
        performanceLoggingService(PerformanceLoggingService)
        proxyService(ProxyService)
        browserService(BrowserService)
        csiAggregationUpdateService(CsiAggregationUpdateService)
        pageService(PageService)
        csiValueService(CsiValueService)
        jobDaoService(JobDaoService)
    }

    @Before
    void setUp() {
        serviceUnderTest = service
        createTestDataCommonForAllTests()

        //mocks common for all tests
        SERVICE_MOCKER.mockTTCsMappingService(serviceUnderTest)
        mockMetricReportingService()
        serviceUnderTest.performanceLoggingService = grailsApplication.mainContext.getBean('performanceLoggingService')
    }

    @Test
    void testPersistingNewResult_BEFORE_MULTISTEP() {

        //create test-specific data
        String nameOfResultXmlFile = 'BEFORE_MULTISTEP_3Runs_CsiRelevant.xml'
        File file = new File("test/resources/WptResultXmls/${nameOfResultXmlFile}")
        WptResultXml xmlResult = new WptResultXml (new XmlSlurper().parse(file))
        createLocationIfNotExistent(xmlResult.responseNode.data.location.toString(), undefinedBrowser, server1);

        //mocking of inner services

        mockCsiAggregationUpdateService()
        ServiceMocker.create().mockTTCsMappingService(serviceUnderTest)
        mockPageService()

        deleteAllRelevantDomains()

        //test execution
        serviceUnderTest.listenToResult(xmlResult, server1)

        //assertions

        List<MeasuredEvent> events = MeasuredEvent.list()
        assertEquals('Count of events', 1, events.size())

        MeasuredEvent event1 = MeasuredEvent.findByName('FF_BV1_Step01_Homepage - netlab')
        assertNotNull(event1)

        //exemplarily testing some results
        List<EventResult> medianUncachedResultsOfsingleEvent = EventResult.findAllByMeasuredEvent(event1).findAll {
            it.medianValue == true && it.cachedView == CachedView.UNCACHED
        }
        assertEquals('Count of median uncached-EventResults for single event - ', 1, medianUncachedResultsOfsingleEvent.size())
        assertEquals('loadTimeInMillisecs of median uncached-EventResults for single event - ', 5873, medianUncachedResultsOfsingleEvent[0].docCompleteTimeInMillisecs)
        assertEquals('docCompleteRequests of median uncached-EventResults for single event - ', 157, medianUncachedResultsOfsingleEvent[0].docCompleteRequests)
        assertEquals('wptStatus of median uncached-EventResults for single event - ', 0, medianUncachedResultsOfsingleEvent[0].wptStatus)

        List<EventResult> medianCachedResultsOfsingleEventRun3 = EventResult.findAllByMeasuredEvent(event1).findAll {
            it.cachedView == CachedView.CACHED && it.numberOfWptRun == 3
        }
        assertEquals('Count of median cached-EventResults for single event, third run - ', 1, medianCachedResultsOfsingleEventRun3.size())
        assertEquals('loadTimeInMillisecs of median cached-EventResults for single event, third run - ', 3977, medianCachedResultsOfsingleEventRun3[0].docCompleteTimeInMillisecs)
        assertEquals('docCompleteRequests of median cached-EventResults for single event, third run - ', 36, medianCachedResultsOfsingleEventRun3[0].docCompleteRequests)
        assertEquals('wptStatus of median cached-EventResults for single event, third run - ', 0, medianCachedResultsOfsingleEventRun3[0].wptStatus)

        //TestDetailsUrl uncached and without pageAggregator
        EventResult eventResultUncachedTest = medianUncachedResultsOfsingleEvent[0]
        String detailsUrl = eventResultUncachedTest.getTestDetailsWaterfallURL().toString()
        assertEquals(
                'http://wptUnitTest.dev.hh.iteratec.local/details.php?test=121212_NH_6a2777a9c09ac89e108d1f2b94e74b83&run=2&cached=0#waterfall_viewFF_BV1_Step01_Homepage - netlab',
                detailsUrl
        )

        //TestDetailsUrl cached and without pageAggregator
        EventResult eventResultCachedTest = medianCachedResultsOfsingleEventRun3[0]
        detailsUrl = eventResultCachedTest.getTestDetailsWaterfallURL().toString()
        assertEquals(
                'http://wptUnitTest.dev.hh.iteratec.local/details.php?test=121212_NH_6a2777a9c09ac89e108d1f2b94e74b83&run=3&cached=1#waterfall_viewFF_BV1_Step01_Homepage - netlab',
                detailsUrl
        )
    }

    @Test
    void testPersistingNewResultWithPageName_BEFORE_MULTISTEP() {

        //create test-specific data
        String nameOfResultXmlFile = 'BEFORE_MULTISTEP_3Runs_WithPageName_CsiRelevant.xml'
        File file = new File("test/resources/WptResultXmls/${nameOfResultXmlFile}")
        WptResultXml xmlResult = new WptResultXml (new XmlSlurper().parse(file))
        createLocationIfNotExistent(xmlResult.responseNode.data.location.toString(), undefinedBrowser, server1);

        //mocking of inner services

        mockCsiAggregationUpdateService()
        ServiceMocker.create().mockTTCsMappingService(serviceUnderTest)
        mockPageService()

        deleteAllRelevantDomains()

        //test execution
        serviceUnderTest.listenToResult(xmlResult, server1)

        //assertions

        List<MeasuredEvent> events = MeasuredEvent.list()
        assertEquals('Count of events', 1, events.size())

        MeasuredEvent event1 = MeasuredEvent.findByName('FF_BV1_Step01_Homepage - netlab')
        assertNotNull(event1)

        //exemplarily testing some results
        List<EventResult> medianUncachedResultsOfsingleEvent = EventResult.findAllByMeasuredEvent(event1).findAll {
            it.medianValue == true && it.cachedView == CachedView.UNCACHED
        }
        assertEquals('Count of median uncached-EventResults for single event - ', 1, medianUncachedResultsOfsingleEvent.size())
        assertEquals('loadTimeInMillisecs of median uncached-EventResults for single event - ', 5873, medianUncachedResultsOfsingleEvent[0].docCompleteTimeInMillisecs)
        assertEquals('docCompleteRequests of median uncached-EventResults for single event - ', 157, medianUncachedResultsOfsingleEvent[0].docCompleteRequests)
        assertEquals('wptStatus of median uncached-EventResults for single event - ', 0, medianUncachedResultsOfsingleEvent[0].wptStatus)

        List<EventResult> medianCachedResultsOfsingleEventRun3 = EventResult.findAllByMeasuredEvent(event1).findAll {
            it.cachedView == CachedView.CACHED && it.numberOfWptRun == 3
        }
        assertEquals('Count of median cached-EventResults for single event, third run - ', 1, medianCachedResultsOfsingleEventRun3.size())
        assertEquals('loadTimeInMillisecs of median cached-EventResults for single event, third run - ', 3977, medianCachedResultsOfsingleEventRun3[0].docCompleteTimeInMillisecs)
        assertEquals('docCompleteRequests of median cached-EventResults for single event, third run - ', 36, medianCachedResultsOfsingleEventRun3[0].docCompleteRequests)
        assertEquals('wptStatus of median cached-EventResults for single event, third run - ', 0, medianCachedResultsOfsingleEventRun3[0].wptStatus)

        //TestDetailsUrl uncached and without pageAggregator
        EventResult eventResultUncachedTest = medianUncachedResultsOfsingleEvent[0]
        String detailsUrl = eventResultUncachedTest.getTestDetailsWaterfallURL().toString()
        assertEquals(
                "http://wptUnitTest.dev.hh.iteratec.local/details.php?test=121212_NH_6a2777a9c09ac89e108d1f2b94e74b83&run=2&cached=0#waterfall_viewFF_BV1_Step01_Homepage - netlab",
                detailsUrl
        )

        //TestDetailsUrl cached and without pageAggregator
        EventResult eventResultCachedTest = medianCachedResultsOfsingleEventRun3[0]
        detailsUrl = eventResultCachedTest.getTestDetailsWaterfallURL().toString()
        assertEquals(
                "http://wptUnitTest.dev.hh.iteratec.local/details.php?test=121212_NH_6a2777a9c09ac89e108d1f2b94e74b83&run=3&cached=1#waterfall_viewFF_BV1_Step01_Homepage - netlab",
                detailsUrl
        )
    }

    @Test
    void testPersistingNewResult_MULTISTEP_FORK_ITERATEC() {

        //create test-specific data
        String nameOfResultXmlFile = 'MULTISTEP_FORK_ITERATEC_3Runs_6EventNames.xml'
        File file = new File("test/resources/WptResultXmls/${nameOfResultXmlFile}")
        WptResultXml xmlResult = new WptResultXml (new XmlSlurper().parse(file))
        createLocationIfNotExistent(xmlResult.responseNode.data.location.toString(), undefinedBrowser, server1);

        //mocking of inner services

        mockCsiAggregationUpdateService()
        ServiceMocker.create().mockTTCsMappingService(serviceUnderTest)
        mockPageService()

        deleteAllRelevantDomains()

        //test execution

        serviceUnderTest.listenToResult(xmlResult, server1)

        //assertions

        List<MeasuredEvent> events = MeasuredEvent.list()
        assertEquals('Count of events', 6, events.size())

        MeasuredEvent event1 = MeasuredEvent.findByName('Seite laden')
        assertNotNull(event1)
        MeasuredEvent event2 = MeasuredEvent.findByName('Artikel suchen')
        assertNotNull(event2)
        MeasuredEvent event3 = MeasuredEvent.findByName('Artikel-Detailseite laden')
        assertNotNull(event3)
        MeasuredEvent event4 = MeasuredEvent.findByName('Produkt auswaehlen')
        assertNotNull(event4)
        MeasuredEvent event5 = MeasuredEvent.findByName('Produkt in Warenkorb legen')
        assertNotNull(event5)
        MeasuredEvent event6 = MeasuredEvent.findByName('Warenkorb oeffnen')
        assertNotNull(event6)

        //exemplarily testing results of some events
        List<EventResult> medianUncachedResultsOfEvent4 = EventResult.findAllByMeasuredEvent(event4).findAll {
            it.medianValue == true && it.cachedView == CachedView.UNCACHED
        }
        assertEquals('Count of median uncached-EventResults for event 4 - ', 1, medianUncachedResultsOfEvent4.size())
        assertEquals('loadTimeInMillisecs of median uncached-EventResults for event 4 - ', 2218, medianUncachedResultsOfEvent4[0].docCompleteTimeInMillisecs)
        assertEquals('docCompleteRequests of median uncached-EventResults for event 4 - ', 29, medianUncachedResultsOfEvent4[0].docCompleteRequests)
        assertEquals('wptStatus of median uncached-EventResults for event 4 - ', 99999, medianUncachedResultsOfEvent4[0].wptStatus)

        List<EventResult> medianCachedResultsOfEvent2Run1 = EventResult.findAllByMeasuredEvent(event2).findAll {
            it.medianValue == true && it.cachedView == CachedView.CACHED && it.numberOfWptRun == 1
        }
        assertEquals('Count of median cached-EventResults for event 2, first run - ', 1, medianCachedResultsOfEvent2Run1.size())
        assertEquals('loadTimeInMillisecs of median cached-EventResults for event 2, first run  - ', 931, medianCachedResultsOfEvent2Run1[0].docCompleteTimeInMillisecs)
        assertEquals('docCompleteRequests of median cached-EventResults for event 2, first run  - ', 6, medianCachedResultsOfEvent2Run1[0].docCompleteRequests)
        assertEquals('wptStatus of median cached-EventResults for event 2, first run  - ', 99999, medianCachedResultsOfEvent2Run1[0].wptStatus)

        //TestDetailsUrl uncached and without pageAggregator
        EventResult eventResultUncachedTest = medianUncachedResultsOfEvent4[0]
        String detailsUrl = eventResultUncachedTest.getTestDetailsWaterfallURL().toString()
        assertEquals(
            "http://wptUnitTest.dev.hh.iteratec.local/details.php?test=130425_W1_f606bebc977a3b22c1a9205f70d07a00&run=1&cached=0#waterfall_viewProdukt auswaehlen",
            detailsUrl
        )

        //TestDetailsUrl cached and without pageAggregator
        EventResult eventResultCachedTest = medianCachedResultsOfEvent2Run1[0]
        detailsUrl = eventResultCachedTest.getTestDetailsWaterfallURL().toString()
        assertEquals(
            "http://wptUnitTest.dev.hh.iteratec.local/details.php?test=130425_W1_f606bebc977a3b22c1a9205f70d07a00&run=1&cached=1#waterfall_viewArtikel suchen",
            detailsUrl
        )
    }

    @Test
    void testPersistingNewResultWithPageName_MULTISTEP_FORK_ITERATEC() {

        //create test-specific data
        String nameOfResultXmlFile = 'MULTISTEP_FORK_ITERATEC_3Runs_6EventNames_WithPageName.xml'
        File file = new File("test/resources/WptResultXmls/${nameOfResultXmlFile}")
        WptResultXml xmlResult = new WptResultXml (new XmlSlurper().parse(file))
        createLocationIfNotExistent(xmlResult.responseNode.data.location.toString(), undefinedBrowser, server1);

        //mocking of inner services

        mockCsiAggregationUpdateService()
        ServiceMocker.create().mockTTCsMappingService(serviceUnderTest)
        mockPageService()

        deleteAllRelevantDomains()

        //test execution

        serviceUnderTest.listenToResult(xmlResult, server1)

        //assertions

        List<MeasuredEvent> events = MeasuredEvent.list()
        assertEquals('Count of events', 6, events.size())

        MeasuredEvent event1 = MeasuredEvent.findByName('Seite laden')
        assertNotNull(event1)
        MeasuredEvent event2 = MeasuredEvent.findByName('Artikel suchen')
        assertNotNull(event2)
        MeasuredEvent event3 = MeasuredEvent.findByName('Artikel-Detailseite laden')
        assertNotNull(event3)
        MeasuredEvent event4 = MeasuredEvent.findByName('Produkt ausw.aehlen')
        assertNotNull(event4)
        MeasuredEvent event5 = MeasuredEvent.findByName('Produkt in Warenkorb legen')
        assertNotNull(event5)
        MeasuredEvent event6 = MeasuredEvent.findByName('Warenkorb oeffnen')
        assertNotNull(event6)

        //exemplarily testing results of some events
        List<EventResult> medianUncachedResultsOfEvent4 = EventResult.findAllByMeasuredEvent(event4).findAll {
            it.medianValue == true && it.cachedView == CachedView.UNCACHED
        }
        assertEquals('Count of median uncached-EventResults for event 4 - ', 1, medianUncachedResultsOfEvent4.size())
        assertEquals('loadTimeInMillisecs of median uncached-EventResults for event 4 - ', 2218, medianUncachedResultsOfEvent4[0].docCompleteTimeInMillisecs)
        assertEquals('docCompleteRequests of median uncached-EventResults for event 4 - ', 29, medianUncachedResultsOfEvent4[0].docCompleteRequests)
        assertEquals('wptStatus of median uncached-EventResults for event 4 - ', 99999, medianUncachedResultsOfEvent4[0].wptStatus)

        List<EventResult> medianCachedResultsOfEvent2Run1 = EventResult.findAllByMeasuredEvent(event2).findAll {
            it.medianValue == true && it.cachedView == CachedView.CACHED && it.numberOfWptRun == 1
        }
        assertEquals('Count of median cached-EventResults for event 2, first run - ', 1, medianCachedResultsOfEvent2Run1.size())
        assertEquals('loadTimeInMillisecs of median cached-EventResults for event 2, first run - ', 931, medianCachedResultsOfEvent2Run1[0].docCompleteTimeInMillisecs)
        assertEquals('docCompleteRequests of median cached-EventResults for event 2, first run - ', 6, medianCachedResultsOfEvent2Run1[0].docCompleteRequests)
        assertEquals('wptStatus of median cached-EventResults for event 2, first run - ', 99999, medianCachedResultsOfEvent2Run1[0].wptStatus)

        //TestDetailsUrl uncached and without pageAggregator
        EventResult eventResultUncachedTest = medianUncachedResultsOfEvent4[0]
        String detailsUrl = eventResultUncachedTest.getTestDetailsWaterfallURL().toString()
        //replacing check: in database with a dot 'Produkt ausw.aehlen'
        assertEquals(
            'http://wptUnitTest.dev.hh.iteratec.local/details.php?test=130425_W1_f606bebc977a3b22c1a9205f70d07a00&run=1&cached=0#waterfall_viewProdukt auswaehlen',
            detailsUrl
        )

        //TestDetailsUrl cached and without pageAggregator
        EventResult eventResultCachedTest = medianCachedResultsOfEvent2Run1[0]
        detailsUrl = eventResultCachedTest.getTestDetailsWaterfallURL().toString()
        assertEquals(
            'http://wptUnitTest.dev.hh.iteratec.local/details.php?test=130425_W1_f606bebc977a3b22c1a9205f70d07a00&run=1&cached=1#waterfall_viewArtikel suchen',
            detailsUrl
        )
    }

    @Test
    void testPersistingNewResultHasCsByVisuallyComplete_MULTISTEP_FORK_ITERATEC() {

        //create test-specific data
        String nameOfResultXmlFile = 'MULTISTEP_FORK_ITERATEC_5Runs_3Events_JustFirstView_WithVideo.xml'
        File file = new File("test/resources/WptResultXmls/${nameOfResultXmlFile}")
        WptResultXml xmlResult = new WptResultXml (new XmlSlurper().parse(file))
        createLocationIfNotExistent(xmlResult.responseNode.data.location.toString(), undefinedBrowser, server1);

        //mocking of inner services

        mockCsiAggregationUpdateService()
        ServiceMocker.create().mockTTCsMappingService(serviceUnderTest)
        mockPageService()

        deleteAllRelevantDomains()

        //test execution

        serviceUnderTest.listenToResult(xmlResult, server1)

        //assertions

        List<EventResult> events = EventResult.list()
        assertEquals('Count of events', 15, events.size())

        //check all EventResults have visualComplete
        boolean allHaveVisualComplete = !events.any { it.visuallyCompleteInMillisecs == null }
        assertTrue(allHaveVisualComplete)

        //check all EventResults have set csByVisualComplete
        boolean allHaveCsByVisualComplete = !events.any { it.csByWptVisuallyCompleteInPercent == null }
        assertTrue(allHaveCsByVisualComplete)
    }

    @Test
    void testCreationOfDetailUrls_MULTISTEP() {

        //create test-specific data
        String nameOfResultXmlFile = 'MULTISTEP_2Run.xml'
        File file = new File("test/resources/WptResultXmls/${nameOfResultXmlFile}")
        WptResultXml xmlResult = new WptResultXml (new XmlSlurper().parse(file))
        createLocationIfNotExistent(xmlResult.responseNode.data.location.toString(), undefinedBrowser, server1);

        //mocking of inner services

        mockCsiAggregationUpdateService()
        ServiceMocker.create().mockTTCsMappingService(serviceUnderTest)
        mockPageService()

        deleteAllRelevantDomains()

        //test execution

        serviceUnderTest.listenToResult(xmlResult, server1)

        //assertions

        List<EventResult> eventResults = EventResult.list()
        assertEquals(8, eventResults.size())

        // UNCACHED

        EventResult run1Step1Uncached = eventResults.find {
            it.numberOfWptRun == 1 && it.measuredEvent.name.equals('beforeTest') && it.cachedView == UNCACHED
        }
        String detailsUrl = run1Step1Uncached.getTestDetailsWaterfallURL().toString()
        assertEquals(
                'http://wptUnitTest.dev.hh.iteratec.local/details.php?test=160727_EV_4&run=1&cached=0#waterfall_view_step1',
                detailsUrl
        )

        EventResult run1Step2Uncached = eventResults.find {
            it.numberOfWptRun == 1 && it.measuredEvent.name.equals('testExecution') && it.cachedView == UNCACHED
        }
        detailsUrl = run1Step2Uncached.getTestDetailsWaterfallURL().toString()
        assertEquals(
                'http://wptUnitTest.dev.hh.iteratec.local/details.php?test=160727_EV_4&run=1&cached=0#waterfall_view_step2',
                detailsUrl
        )

        EventResult run2Step1Uncached = eventResults.find {
            it.numberOfWptRun == 2 && it.measuredEvent.name.equals('beforeTest') && it.cachedView == UNCACHED
        }
        detailsUrl = run2Step1Uncached.getTestDetailsWaterfallURL().toString()
        assertEquals(
                'http://wptUnitTest.dev.hh.iteratec.local/details.php?test=160727_EV_4&run=2&cached=0#waterfall_view_step1',
                detailsUrl
        )

        EventResult run2Step2Uncached = eventResults.find {
            it.numberOfWptRun == 2 && it.measuredEvent.name.equals('testExecution') && it.cachedView == UNCACHED
        }
        detailsUrl = run2Step2Uncached.getTestDetailsWaterfallURL().toString()
        assertEquals(
                'http://wptUnitTest.dev.hh.iteratec.local/details.php?test=160727_EV_4&run=2&cached=0#waterfall_view_step2',
                detailsUrl
        )

        //CACHED

        EventResult run1Step1Cached = eventResults.find {
            it.numberOfWptRun == 1 && it.measuredEvent.name.equals('beforeTest') && it.cachedView == CACHED
        }
        detailsUrl = run1Step1Cached.getTestDetailsWaterfallURL().toString()
        assertEquals(
                'http://wptUnitTest.dev.hh.iteratec.local/details.php?test=160727_EV_4&run=1&cached=1#waterfall_view_step1',
                detailsUrl
        )

        EventResult run1Step2Cached = eventResults.find {
            it.numberOfWptRun == 1 && it.measuredEvent.name.equals('testExecution') && it.cachedView == CACHED
        }
        detailsUrl = run1Step2Cached.getTestDetailsWaterfallURL().toString()
        assertEquals(
                'http://wptUnitTest.dev.hh.iteratec.local/details.php?test=160727_EV_4&run=1&cached=1#waterfall_view_step2',
                detailsUrl
        )

        EventResult run2Step1Cached = eventResults.find {
            it.numberOfWptRun == 2 && it.measuredEvent.name.equals('beforeTest') && it.cachedView == CACHED
        }
        detailsUrl = run2Step1Cached.getTestDetailsWaterfallURL().toString()
        assertEquals(
                'http://wptUnitTest.dev.hh.iteratec.local/details.php?test=160727_EV_4&run=2&cached=1#waterfall_view_step1',
                detailsUrl
        )

        EventResult run2Step2Cached = eventResults.find {
            it.numberOfWptRun == 2 && it.measuredEvent.name.equals('testExecution') && it.cachedView == CACHED
        }
        detailsUrl = run2Step2Cached.getTestDetailsWaterfallURL().toString()
        assertEquals(
                'http://wptUnitTest.dev.hh.iteratec.local/details.php?test=160727_EV_4&run=2&cached=1#waterfall_view_step2',
                detailsUrl
        )

    }

    @Test
    void testPageAssignementWhilePersistingNewResult_MULTISTEP_FORK_ITERATEC() {

        //create test-specific data
        String nameOfResultXmlFile = 'MULTISTEP_FORK_ITERATEC_1Run_2EventNames_PagePrefix.xml'
        File file = new File("test/resources/WptResultXmls/${nameOfResultXmlFile}")
        WptResultXml xmlResult = new WptResultXml (new XmlSlurper().parse(file))
        createLocationIfNotExistent(xmlResult.responseNode.data.location.toString(), undefinedBrowser, server1);

        //mocking of inner services

        mockCsiAggregationUpdateService()
        ServiceMocker.create().mockTTCsMappingService(serviceUnderTest)
        mockPageService()

        deleteAllRelevantDomains()

        //test execution
        serviceUnderTest.listenToResult(xmlResult, server1)

        //assertions
        List<MeasuredEvent> events = MeasuredEvent.list()
        assertEquals('Count of events', 2, events.size())

        MeasuredEvent event1 = MeasuredEvent.findByName('LH_Homepage_2')
        assertNotNull(event1)
        assertEquals('', '')
        MeasuredEvent event2 = MeasuredEvent.findByName('LH_Moduleinstieg_2')
        assertNotNull(event2)

    }

    private void deleteAllRelevantDomains() {
        JobResult.list()*.delete(flush: true)
        MeasuredEvent.list()*.delete(flush: true)
        EventResult.list()*.delete(flush: true)
    }

    // mocks ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    private void createLocationIfNotExistent(String locationIdentifier, Browser browser, WebPageTestServer server) {
        Location alreadyExistent = Location.findByWptServerAndUniqueIdentifierForServer(server, locationIdentifier)
        if (!alreadyExistent) {
            new Location(
                    active: true,
                    valid: 1,
                    uniqueIdentifierForServer: locationIdentifier, // z.B. Agent1-wptdriver:Chrome
                    location: "UNIT_TEST_LOCATION",//z.B. Agent1-wptdriver
                    label: "Unit Test Location: Browser?",//z.B. Agent 1: Windows 7 (S008178178)
                    browser: browser,//z.B. Firefox
                    wptServer: server,
                    dateCreated: new Date(),
                    lastUpdated: new Date()
            ).save(failOnError: true);
        }
    }

    private void mockCsiAggregationUpdateService() {
        def csiAggregationUpdateServiceMocked = grailsApplication.mainContext.getBean('csiAggregationUpdateService')
        csiAggregationUpdateServiceMocked.metaClass.createOrUpdateDependentMvs = { EventResult newResult ->
            //not the concern of this test
            return 34d
        }
        serviceUnderTest.csiAggregationUpdateService = csiAggregationUpdateServiceMocked
    }

    private void mockPageService() {
        def pageServiceMocked = grailsApplication.mainContext.getBean('pageService')
        pageServiceMocked.metaClass.getPageByStepName = { String pageName ->
            def tokenized = pageName.tokenize(PageService.STEPNAME_DELIMITTER)
            return tokenized.size() == 2 ? Page.findByName(tokenized[0]) : Page.findByName(Page.UNDEFINED)
        }
        pageServiceMocked.metaClass.getDefaultStepNameForPage = { Page page ->
            //not the concern of this test
            return Page.findByName('HP').name + PageService.STEPNAME_DELIMITTER + PageService.STEPNAME_DEFAULT_STEPNUMBER
        }
        pageServiceMocked.metaClass.excludePagenamePart = { String stepName ->
            return stepName.contains(PageService.STEPNAME_DELIMITTER) ?
                    stepName.substring(stepName.indexOf(PageService.STEPNAME_DELIMITTER) + PageService.STEPNAME_DELIMITTER.length(), stepName.length()) :
                    stepName
        }
        serviceUnderTest.pageService = pageServiceMocked
    }

    private void mockMetricReportingService() {
        def metricReportingService = grailsApplication.mainContext.getBean('metricReportingService')
        metricReportingService.metaClass.reportEventResultToGraphite = {
            EventResult result ->
                // not the concern of this test
        }
        serviceUnderTest.metricReportingService = metricReportingService
    }

    // create testdata common to all tests /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private createBrowsers() {
        String browserName = Browser.UNDEFINED
        undefinedBrowser = new Browser(
                name: browserName,
                weight: 0)
                .addToBrowserAliases(alias: Browser.UNDEFINED)
                .save(failOnError: true)

        browserName = "IE"
        new Browser(
                name: browserName,
                weight: 45)
                .addToBrowserAliases(alias: "IE")
                .addToBrowserAliases(alias: "IE8")
                .addToBrowserAliases(alias: "Internet Explorer")
                .addToBrowserAliases(alias: "Internet Explorer 8")
                .save(failOnError: true)
        browserName = "FF"
        new Browser(
                name: browserName,
                weight: 55)
                .addToBrowserAliases(alias: "FF")
                .addToBrowserAliases(alias: "FF7")
                .addToBrowserAliases(alias: "Firefox")
                .addToBrowserAliases(alias: "Firefox7")
                .save(failOnError: true)

        browserName = "Chrome"
        new Browser(
                name: browserName,
                weight: 55)
                .addToBrowserAliases(alias: "Chrome")
                .save(failOnError: true)
    }

    private void createPages() {
        ['HP', 'MES', Page.UNDEFINED].each { pageName ->
            Double weight = 0
            switch (pageName) {
                case 'HP': weight = 6; break
                case 'MES': weight = 9; break
                case 'SE': weight = 36; break
                case 'ADS': weight = 43; break
                case 'WKBS': weight = 3; break
                case 'WK': weight = 3; break
            }
            new Page(
                    name: pageName,
                    weight: weight).save(failOnError: true)
        }
    }

    void createTestDataCommonForAllTests() {
        server1 = new WebPageTestServer(
                label: "TestServer 1",
                proxyIdentifier: "TestServer1",
                baseUrl: "http://wptUnitTest.dev.hh.iteratec.local",
                active: true,
                dateCreated: new Date(),
                lastUpdated: new Date()
        ).save(failOnError: true, validate: false)

        server2 = new WebPageTestServer(
                label: "TestServer 2",
                proxyIdentifier: "TestServer2",
                baseUrl: "http://wptUnitTest2.dev.hh.iteratec.local",
                active: 1,
                dateCreated: new Date(),
                lastUpdated: new Date()
        ).save(failOnError: true, validate: false)

        undefinedJobGroup = new JobGroup(
                name: JobGroup.UNDEFINED_CSI
        );
        undefinedJobGroup.save(failOnError: true);

        //creating test-data common to all tests
        createPages()
        createBrowsers()
        Location testLocation = TestDataUtil.createLocation(server1, 'test-location', Browser.findByName('IE'), true)
        Script testScript = TestDataUtil.createScript('test-script', 'description', 'navigate   http://my-url.de')
        TestDataUtil.createJob('ie_step_testjob', testScript, testLocation, undefinedJobGroup, '', 1, false, 60)
        TestDataUtil.createJob('http://www.example.de.de - Multiple steps with event names + dom elements', testScript, testLocation, undefinedJobGroup, '', 1, false, 60)
        TestDataUtil.createJob('FF_BV1_Step01_Homepage - netlab', testScript, testLocation, undefinedJobGroup, '', 1, false, 60)
        TestDataUtil.createJob('testjob', testScript, testLocation, undefinedJobGroup, '', 1, false, 60)
        TestDataUtil.createJob('FF_BV1_Multistep_2', testScript, testLocation, undefinedJobGroup, '', 1, false, 60)
        TestDataUtil.createJob('IE_otto_hp_singlestep', testScript, testLocation, undefinedJobGroup, '', 1, false, 60)
        TestDataUtil.createJob('HP:::FF_BV1_Step01_Homepage - netlab', testScript, testLocation, undefinedJobGroup, '', 1, false, 60)
        TestDataUtil.createJob('example.de - Multiple steps with event names + dom elements', testScript, testLocation, undefinedJobGroup, '', 1, false, 60)
        TestDataUtil.createJob('FF_Otto_multistep', testScript, testLocation, undefinedJobGroup, '', 1, false, 60)

        CsiConfiguration csiConfiguration = TestDataUtil.createCsiConfiguration()
        csiConfiguration.timeToCsMappings = TestDataUtil.createTimeToCsMappingForAllPages(Page.list())
        undefinedJobGroup.csiConfiguration = csiConfiguration
        ServiceMocker mockGenerator = ServiceMocker.create()
        serviceUnderTest.csiValueService = grailsApplication.mainContext.getBean('csiValueService')
        mockGenerator.mockOsmConfigCacheService(serviceUnderTest.csiValueService)
    }
}

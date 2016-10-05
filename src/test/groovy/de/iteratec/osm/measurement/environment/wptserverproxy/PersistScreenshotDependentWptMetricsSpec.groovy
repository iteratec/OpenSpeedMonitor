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

import de.iteratec.osm.csi.Page
import de.iteratec.osm.csi.TestDataUtil
import de.iteratec.osm.measurement.environment.Browser
import de.iteratec.osm.measurement.environment.BrowserAlias
import de.iteratec.osm.measurement.environment.Location
import de.iteratec.osm.measurement.environment.WebPageTestServer
import de.iteratec.osm.measurement.schedule.Job
import de.iteratec.osm.measurement.schedule.JobDaoService
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.measurement.script.Script
import de.iteratec.osm.result.*
import de.iteratec.osm.util.PerformanceLoggingService
import de.iteratec.osm.util.ServiceMocker
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin

import static org.hamcrest.Matchers.*
import static org.junit.Assert.assertThat
/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
@TestMixin(GrailsUnitTestMixin)
@TestFor(ResultPersisterService)
@Mock([WebPageTestServer, Browser, Location, Job, JobResult, EventResult, BrowserAlias, Page, MeasuredEvent, JobGroup, Script])
class PersistScreenshotDependentWptMetricsSpec {

    public static final String PROXY_IDENTIFIER_WPT_SERVER = 'dev.server02.wpt.iteratec.de'

    public static final String RESULT_XML_MULTISTEP_1RUN_3EVENTS_FVONLY_WITHVIDEO = 'MULTISTEP_FORK_ITERATEC_1Run_3Events_JustFirstView_WithVideo.xml'
    public static final String RESULT_XML_MULTISTEP_1RUN_3EVENTS_FVONLY_WITHVIDEO_EVENTNAME_1 = 'otto_homepage'
    public static final String RESULT_XML_MULTISTEP_1RUN_3EVENTS_FVONLY_WITHVIDEO_EVENTNAME_2 = 'otto_search_shoes'
    public static final String RESULT_XML_MULTISTEP_1RUN_3EVENTS_FVONLY_WITHVIDEO_EVENTNAME_3 = 'otto_product_boots'

    public static final String RESULT_XML_MULTISTEP_1RUN_3EVENTS_FVONLY_WITHOUTVIDEO = 'MULTISTEP_FORK_ITERATEC_1Run_3Events_JustFirstView_WithoutVideo.xml'
    public static final String RESULT_XML_MULTISTEP_1RUN_3EVENTS_FVONLY_WITHOUTVIDEO_EVENTNAME_1 = 'otto_homepage'
    public static final String RESULT_XML_MULTISTEP_1RUN_3EVENTS_FVONLY_WITHOUTVIDEO_EVENTNAME_2 = 'otto_search_shoes'
    public static final String RESULT_XML_MULTISTEP_1RUN_3EVENTS_FVONLY_WITHOUTVIDEO_EVENTNAME_3 = 'otto_product_boots'

    public static final String RESULT_XML_MULTISTEP_5RUNS_3EVENTS_FVONLY_WITHVIDEO = 'MULTISTEP_FORK_ITERATEC_5Runs_3Events_JustFirstView_WithVideo.xml'
    public static final String RESULT_XML_MULTISTEP_5RUNS_3EVENTS_FVONLY_WITHVIDEO_EVENTNAME_1 = 'otto_homepage'
    public static final String RESULT_XML_MULTISTEP_5RUNS_3EVENTS_FVONLY_WITHVIDEO_EVENTNAME_2 = 'otto_search_shoes'
    public static final String RESULT_XML_MULTISTEP_5RUNS_3EVENTS_FVONLY_WITHVIDEO_EVENTNAME_3 = 'otto_product_boot'

    public static final String RESULT_XML_SINGLESTEP_1RUN_WITHVIDEO = 'BEFORE_MULTISTEP_1Run_WithVideo.xml'
    public static final String RESULT_XML_SINGLESTEP_1RUN_WITHVIDEO_EVENTNAME = 'IE_otto_hp_singlestep'

    public static final String RESULT_XML_SINGLESTEP_1RUN_WITHOUTVIDEO = 'BEFORE_MULTISTEP_1Run_WithoutVideo.xml'
    public static final String RESULT_XML_SINGLESTEP_1RUN_WITHOUTVIDEO_EVENTNAME = 'IE_otto_hp_singlestep'

    public static final String RESULT_XML_SINGLESTEP_5RUNS_FVONLY_WITHVIDEO = 'BEFORE_MULTISTEP_5Runs_WithVideo.xml'
    public static final String RESULT_XML_SINGLESTEP_5RUNS_FVONLY_WITHVIDEO_EVENTNAME = 'IE_otto_hp_singlestep'

    ResultPersisterService serviceUnderTest
    ServiceMocker mocker

    def doWithSpring = {
        pageService(PageService)
        performanceLoggingService(PerformanceLoggingService)
        jobDaoService(JobDaoService)
    }
    void setUp() {

        serviceUnderTest = service

        createTestDataCommonForAllTests()
        createMocksCommonForAllTests()

    }

    void createTestDataCommonForAllTests() {
        JobGroup jobGroup = TestDataUtil.createJobGroup(JobGroup.UNDEFINED_CSI)
        WebPageTestServer wptServer = TestDataUtil.createWebPageTestServer(PROXY_IDENTIFIER_WPT_SERVER, PROXY_IDENTIFIER_WPT_SERVER, true, "http://${PROXY_IDENTIFIER_WPT_SERVER}/")
        Browser ff = TestDataUtil.createBrowser('Firefox', 1d)
        Browser ie = TestDataUtil.createBrowser('IE', 1d)
        Location locationFirefox = TestDataUtil.createLocation(wptServer, 'iteratec-dev-hetzner-win7:Firefox', ff, true)
        Location locationFirefox_old = TestDataUtil.createLocation(wptServer, 'iteratec-dev-hetzner-64bit-ssd:Firefox', ff, true)
        Location locationIe = TestDataUtil.createLocation(wptServer, 'NewYork:IE 11', ie, true)
        Location locationIe_old = TestDataUtil.createLocation(wptServer, 'iteratec-dev-netlab-win7:IE', ie, true)
        List<Page> pages = TestDataUtil.createPages(['HP', 'SE'])

        Script testScript = TestDataUtil.createScript('test-script', 'description', 'navigate   http://my-url.de')
        TestDataUtil.createJob('FF_Otto_multistep', testScript, locationFirefox, jobGroup, '', 1 , false, 60)
        TestDataUtil.createJob('IE_otto_hp_singlestep', testScript, locationIe, jobGroup, '', 1 , false, 60)
    }

    void createMocksCommonForAllTests() {
        mocker = ServiceMocker.create()
        mocker.mockProxyService(serviceUnderTest)
        mocker.mockMetricReportingService(serviceUnderTest)
        serviceUnderTest.pageService = grailsApplication.mainContext.getBean('pageService')
        mocker.mockCsiAggregationTagService(serviceUnderTest, [:], [:], [:], [:], [:])
        serviceUnderTest.metaClass.informDependents = { List<EventResult> results ->
            // not the concern of this test
        }
        serviceUnderTest.metaClass.informDependents = { EventResult results ->
            // not the concern of this test
        }
        serviceUnderTest.metaClass.informDependentCsiAggregations = { EventResult results ->
            // not the concern of this test
        }
        mocker.mockTTCsMappingService(serviceUnderTest)
        serviceUnderTest.performanceLoggingService = grailsApplication.mainContext.getBean('performanceLoggingService')
    }

    void tearDown() {
        // Tear down logic here
    }

    // multistep ///////////////////////////////////////////////////////////////////////////////////////

    void testPersistingScreenshotDependentMetricsForMultistep1Run3EventsFvOnlyWithVideo() {
        //test data specific for this test
        File xmlResultFile = new File("test/resources/WptResultXmls/${RESULT_XML_MULTISTEP_1RUN_3EVENTS_FVONLY_WITHVIDEO}")
        WptResultXml xmlResult = new WptResultXml (new XmlSlurper().parse(xmlResultFile))
        TestDataUtil.createMeasuredEvent(RESULT_XML_MULTISTEP_1RUN_3EVENTS_FVONLY_WITHVIDEO_EVENTNAME_1, Page.findByName('HP'))
        TestDataUtil.createMeasuredEvent(RESULT_XML_MULTISTEP_1RUN_3EVENTS_FVONLY_WITHVIDEO_EVENTNAME_2, Page.findByName('SE'))
        TestDataUtil.createMeasuredEvent(RESULT_XML_MULTISTEP_1RUN_3EVENTS_FVONLY_WITHVIDEO_EVENTNAME_3, Page.findByName('SE'))
        //test execution
        serviceUnderTest.listenToResult(xmlResult, WebPageTestServer.findByProxyIdentifier(PROXY_IDENTIFIER_WPT_SERVER))
        //assertions
        List<EventResult> allResults = EventResult.getAll()
        assertThat(allResults.size(), is(3))
        EventResult resultStep_1 = allResults.find {it.measuredEvent.name.equals(RESULT_XML_MULTISTEP_1RUN_3EVENTS_FVONLY_WITHVIDEO_EVENTNAME_1)}
        assertThat(resultStep_1.visuallyCompleteInMillisecs, is(4500))
        assertThat(resultStep_1.speedIndex, is(1647))
        EventResult resultStep_2 = allResults.find {it.measuredEvent.name.equals(RESULT_XML_MULTISTEP_1RUN_3EVENTS_FVONLY_WITHVIDEO_EVENTNAME_2)}
        assertThat(resultStep_2.visuallyCompleteInMillisecs, is(1300))
        assertThat(resultStep_2.speedIndex, is(838))
        EventResult resultStep_3 = allResults.find {it.measuredEvent.name.equals(RESULT_XML_MULTISTEP_1RUN_3EVENTS_FVONLY_WITHVIDEO_EVENTNAME_3)}
        assertThat(resultStep_3.visuallyCompleteInMillisecs, is(1100))
        assertThat(resultStep_3.speedIndex, is(847))

    }
    void testPersistingScreenshotDependentMetricsForMultistep1Run3EventsFvOnlyWithoutVideo() {
        //test data specific for this test
        File xmlResultFile = new File("test/resources/WptResultXmls/${RESULT_XML_MULTISTEP_1RUN_3EVENTS_FVONLY_WITHOUTVIDEO}")
        WptResultXml xmlResult = new WptResultXml (new XmlSlurper().parse(xmlResultFile))
        TestDataUtil.createMeasuredEvent(RESULT_XML_MULTISTEP_1RUN_3EVENTS_FVONLY_WITHOUTVIDEO_EVENTNAME_1, Page.findByName('HP'))
        TestDataUtil.createMeasuredEvent(RESULT_XML_MULTISTEP_1RUN_3EVENTS_FVONLY_WITHOUTVIDEO_EVENTNAME_2, Page.findByName('SE'))
        TestDataUtil.createMeasuredEvent(RESULT_XML_MULTISTEP_1RUN_3EVENTS_FVONLY_WITHOUTVIDEO_EVENTNAME_3, Page.findByName('SE'))
        //test execution
        serviceUnderTest.listenToResult(xmlResult, WebPageTestServer.findByProxyIdentifier(PROXY_IDENTIFIER_WPT_SERVER))
        //assertions
        List<EventResult> allResults = EventResult.getAll()
        assertThat(allResults.size(), is(3))

        assertThat(allResults*.visuallyCompleteInMillisecs, everyItem(
                nullValue()
        ))
        assertThat(allResults*.speedIndex, everyItem(
                is(EventResult.SPEED_INDEX_DEFAULT_VALUE)
        ))

    }
    void testPersistingScreenshotDependentMetricsForMultistep5Run3EventsFvOnlyWithVideo() {

        //test data specific for this test
        File xmlResultFile = new File("test/resources/WptResultXmls/${RESULT_XML_MULTISTEP_5RUNS_3EVENTS_FVONLY_WITHVIDEO}")
        WptResultXml xmlResult = new WptResultXml (new XmlSlurper().parse(xmlResultFile))
        TestDataUtil.createMeasuredEvent(RESULT_XML_MULTISTEP_5RUNS_3EVENTS_FVONLY_WITHVIDEO_EVENTNAME_1, Page.findByName('HP'))
        TestDataUtil.createMeasuredEvent(RESULT_XML_MULTISTEP_5RUNS_3EVENTS_FVONLY_WITHVIDEO_EVENTNAME_2, Page.findByName('SE'))
        TestDataUtil.createMeasuredEvent(RESULT_XML_MULTISTEP_5RUNS_3EVENTS_FVONLY_WITHVIDEO_EVENTNAME_3, Page.findByName('SE'))

        //test execution
        serviceUnderTest.listenToResult(xmlResult, WebPageTestServer.findByProxyIdentifier(PROXY_IDENTIFIER_WPT_SERVER))

        //prepare expected results data for this test
        Map expectedScreenshotDependentMetrics = [
            run1_step1: [visualComplete: 3000, SpeedIndex: 2241],
            run1_step2: [visualComplete: 2000, SpeedIndex: 574],
            run1_step3: [visualComplete: 3400, SpeedIndex: 745],
            run2_step1: [visualComplete: 2900, SpeedIndex: 2318],
            run2_step2: [visualComplete: 2000, SpeedIndex: 620],
            run2_step3: [visualComplete: 1900, SpeedIndex: 770],
            run3_step1: [visualComplete: 3000, SpeedIndex: 2155],
            run3_step2: [visualComplete: 2000, SpeedIndex: 621],
            run3_step3: [visualComplete: 1800, SpeedIndex: 691],
            run4_step1: [visualComplete: 2900, SpeedIndex: 2177],
            run4_step2: [visualComplete: 2000, SpeedIndex: 542],
            run4_step3: [visualComplete: 1900, SpeedIndex: 765],
            run5_step1: [visualComplete: 2800, SpeedIndex: 2253],
            run5_step2: [visualComplete: 2300, SpeedIndex: 644],
            run5_step3: [visualComplete: 2300, SpeedIndex: 874],
        ]
        Map<Integer, String> stepNumberToEventNameMap = [
                1: RESULT_XML_MULTISTEP_5RUNS_3EVENTS_FVONLY_WITHVIDEO_EVENTNAME_1,
                2: RESULT_XML_MULTISTEP_5RUNS_3EVENTS_FVONLY_WITHVIDEO_EVENTNAME_2,
                3: RESULT_XML_MULTISTEP_5RUNS_3EVENTS_FVONLY_WITHVIDEO_EVENTNAME_3
        ]

        //assertions
        List<EventResult> allResults = EventResult.getAll()
        assertThat(allResults.size(), is(15))

        int numberOfRuns = 5
        int numberOfSteps = 3

        numberOfRuns.times {int runIndex ->
            numberOfSteps.times {int stepIndex ->

                int stepNumber = stepIndex + 1
                int runNumber = runIndex + 1
                EventResult resultOfCurrentRunAndStep = allResults.find {
                    it.numberOfWptRun == runNumber && it.measuredEvent.name.equals( stepNumberToEventNameMap[stepNumber] )
                }
                Map<String, Integer> expectedMetricsOfCurrentResult = expectedScreenshotDependentMetrics["run${runNumber}_step${stepNumber}"]

                assertThat(resultOfCurrentRunAndStep.visuallyCompleteInMillisecs, is(
                        expectedMetricsOfCurrentResult['visualComplete']
                ))
                assertThat(resultOfCurrentRunAndStep.speedIndex, is(
                        expectedMetricsOfCurrentResult['SpeedIndex']
                ))

            }
        }

    }

    // singlestep ///////////////////////////////////////////////////////////////////////////////////////

    void testPersistingScreenshotDependentMetricsForSinglestep1RunWithVideo() {
        //test data specific for this test
        File xmlResultFile = new File("test/resources/WptResultXmls/${RESULT_XML_SINGLESTEP_1RUN_WITHVIDEO}")
        WptResultXml xmlResult = new WptResultXml (new XmlSlurper().parse(xmlResultFile))
        TestDataUtil.createMeasuredEvent(RESULT_XML_SINGLESTEP_1RUN_WITHVIDEO_EVENTNAME, Page.findByName('HP'))
        //test execution
        serviceUnderTest.listenToResult(xmlResult, WebPageTestServer.findByProxyIdentifier(PROXY_IDENTIFIER_WPT_SERVER))
        //assertions
        List<EventResult> allResults = EventResult.getAll()
        assertThat(allResults.size(), is(2))
        EventResult fvResult = allResults.find {it.cachedView == CachedView.UNCACHED}
        assertThat(fvResult.visuallyCompleteInMillisecs, is(4500))
        assertThat(fvResult.speedIndex, is(2430))
        EventResult rvResult = allResults.find {it.cachedView == CachedView.CACHED}
        assertThat(rvResult.visuallyCompleteInMillisecs, is(2200))
        assertThat(rvResult.speedIndex, is(1693))
    }
    void testPersistingScreenshotDependentMetricsForSinglestep1RunWithoutVideo() {
        //test data specific for this test
        File xmlResultFile = new File("test/resources/WptResultXmls/${RESULT_XML_SINGLESTEP_1RUN_WITHOUTVIDEO}")
        WptResultXml xmlResult = new WptResultXml (new XmlSlurper().parse(xmlResultFile))
        TestDataUtil.createMeasuredEvent(RESULT_XML_SINGLESTEP_1RUN_WITHOUTVIDEO_EVENTNAME, Page.findByName('HP'))
        //test execution
        serviceUnderTest.listenToResult(xmlResult, WebPageTestServer.findByProxyIdentifier(PROXY_IDENTIFIER_WPT_SERVER))
        //assertions
        List<EventResult> allResults = EventResult.getAll()
        assertThat(allResults.size(), is(2))
        assertThat(allResults.get(0).visuallyCompleteInMillisecs, is(4888))
        assertThat(allResults.get(1).visuallyCompleteInMillisecs, is(2088))
        assertThat(allResults.get(0).speedIndex, is(2556))
        assertThat(allResults.get(1).speedIndex, is(1590))


    }
    void testPersistingScreenshotDependentMetricsForSinglestep5RunsWithVideo() {

        //test data specific for this test
        File xmlResultFile = new File("test/resources/WptResultXmls/${RESULT_XML_SINGLESTEP_5RUNS_FVONLY_WITHVIDEO}")
        WptResultXml xmlResult = new WptResultXml (new XmlSlurper().parse(xmlResultFile))
        TestDataUtil.createMeasuredEvent(RESULT_XML_SINGLESTEP_5RUNS_FVONLY_WITHVIDEO_EVENTNAME, Page.findByName('HP'))

        //test execution
        serviceUnderTest.listenToResult(xmlResult, WebPageTestServer.findByProxyIdentifier(PROXY_IDENTIFIER_WPT_SERVER))

        //assertions
        List<EventResult> allResults = EventResult.getAll()
            assertThat(allResults.size(), is(10))

        // fv

        EventResult fvResultOfRun_1 = allResults.find {it.numberOfWptRun == 1 && it.cachedView == CachedView.UNCACHED}
        assertThat(fvResultOfRun_1.visuallyCompleteInMillisecs, is(11700))
        assertThat(fvResultOfRun_1.speedIndex, is(5001))

        EventResult fvResultOfRun_2 = allResults.find {it.numberOfWptRun == 2 && it.cachedView == CachedView.UNCACHED}
        assertThat(fvResultOfRun_2.visuallyCompleteInMillisecs, is(12000))
        assertThat(fvResultOfRun_2.speedIndex, is(7000))

        EventResult fvResultOfRun_3 = allResults.find {it.numberOfWptRun == 3 && it.cachedView == CachedView.UNCACHED}
        assertThat(fvResultOfRun_3.visuallyCompleteInMillisecs, is(12000))
        assertThat(fvResultOfRun_3.speedIndex, is(6882))

        EventResult fvResultOfRun_4 = allResults.find {it.numberOfWptRun == 4 && it.cachedView == CachedView.UNCACHED}
        assertThat(fvResultOfRun_4.visuallyCompleteInMillisecs, is(12000))
        assertThat(fvResultOfRun_4.speedIndex, is(7025))

        EventResult fvResultOfRun_5 = allResults.find {it.numberOfWptRun == 5 && it.cachedView == CachedView.UNCACHED}
        assertThat(fvResultOfRun_5.visuallyCompleteInMillisecs, is(12000))
        assertThat(fvResultOfRun_5.speedIndex, is(7017))

        // rv

        EventResult rvResultOfRun_1 = allResults.find {it.numberOfWptRun == 1 && it.cachedView == CachedView.CACHED}
        assertThat(rvResultOfRun_1.visuallyCompleteInMillisecs, is(3800))
        assertThat(rvResultOfRun_1.speedIndex, is(1970))

        EventResult rvResultOfRun_2 = allResults.find {it.numberOfWptRun == 2 && it.cachedView == CachedView.CACHED}
        assertThat(rvResultOfRun_2.visuallyCompleteInMillisecs, is(10500))
        assertThat(rvResultOfRun_2.speedIndex, is(5510))

        EventResult rvResultOfRun_3 = allResults.find {it.numberOfWptRun == 3 && it.cachedView == CachedView.CACHED}
        assertThat(rvResultOfRun_3.visuallyCompleteInMillisecs, is(10000))
        assertThat(rvResultOfRun_3.speedIndex, is(2780))

        EventResult rvResultOfRun_4 = allResults.find {it.numberOfWptRun == 4 && it.cachedView == CachedView.CACHED}
        assertThat(rvResultOfRun_4.visuallyCompleteInMillisecs, is(10700))
        assertThat(rvResultOfRun_4.speedIndex, is(5665))

        EventResult rvResultOfRun_5 = allResults.find {it.numberOfWptRun == 5 && it.cachedView == CachedView.CACHED}
        assertThat(rvResultOfRun_5.visuallyCompleteInMillisecs, is(9700))
        assertThat(rvResultOfRun_5.speedIndex, is(1732))


    }

}

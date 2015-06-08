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

import de.iteratec.osm.result.CachedView
import de.iteratec.osm.result.PageService
import de.iteratec.osm.util.PerformanceLoggingService

import static org.hamcrest.Matchers.*
import static org.junit.Assert.*

import de.iteratec.osm.csi.Page
import de.iteratec.osm.csi.TestDataUtil
import de.iteratec.osm.measurement.environment.Browser
import de.iteratec.osm.measurement.environment.BrowserAlias
import de.iteratec.osm.measurement.environment.Location
import de.iteratec.osm.measurement.environment.WebPageTestServer
import de.iteratec.osm.measurement.schedule.Job
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.measurement.schedule.JobGroupType
import de.iteratec.osm.measurement.script.Script
import de.iteratec.osm.result.EventResult
import de.iteratec.osm.result.JobResult
import de.iteratec.osm.result.MeasuredEvent
import de.iteratec.osm.result.detail.WaterfallEntry
import de.iteratec.osm.result.detail.WebPerformanceWaterfall
import de.iteratec.osm.util.ServiceMocker
import groovy.util.slurpersupport.GPathResult
import grails.test.mixin.*
import grails.test.mixin.support.*

/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
@TestMixin(GrailsUnitTestMixin)
@TestFor(LocationAndResultPersisterService)
@Mock([WebPageTestServer, Browser, Location, Job, JobResult, EventResult, BrowserAlias, Page, MeasuredEvent, JobGroup, Script, WebPerformanceWaterfall, WaterfallEntry])
class PersistScreenshotDependentWptMetricsSpec {

    public static final String PROXY_IDENTIFIER_WPT_SERVER = 'dev.server02.wpt.iteratec.de'

    public static final String RESULT_XML_MULTISTEP_1RUN_3EVENTS_FVONLY_WITHVIDEO = 'Result_wptserver2.13-multistep7_1Run_3Events_JustFirstView_WithVideo.xml'
    public static final String RESULT_XML_MULTISTEP_1RUN_3EVENTS_FVONLY_WITHVIDEO_EVENTNAME_1 = 'otto_homepage'
    public static final String RESULT_XML_MULTISTEP_1RUN_3EVENTS_FVONLY_WITHVIDEO_EVENTNAME_2 = 'otto_search_shoes'
    public static final String RESULT_XML_MULTISTEP_1RUN_3EVENTS_FVONLY_WITHVIDEO_EVENTNAME_3 = 'otto_product_boot'

    public static final String RESULT_XML_MULTISTEP_1RUN_3EVENTS_FVONLY_WITHOUTVIDEO = 'Result_wptserver2.13-multistep7_1Run_3Events_JustFirstView_WithoutVideo.xml'
    public static final String RESULT_XML_MULTISTEP_1RUN_3EVENTS_FVONLY_WITHOUTVIDEO_EVENTNAME_1 = 'otto_homepage'
    public static final String RESULT_XML_MULTISTEP_1RUN_3EVENTS_FVONLY_WITHOUTVIDEO_EVENTNAME_2 = 'otto_search_shoes'
    public static final String RESULT_XML_MULTISTEP_1RUN_3EVENTS_FVONLY_WITHOUTVIDEO_EVENTNAME_3 = 'otto_product_boot'

    public static final String RESULT_XML_MULTISTEP_5RUNS_3EVENTS_FVONLY_WITHVIDEO = 'Result_wptserver2.13-multistep7_5Runs_3Events_JustFirstView_WithVideo.xml'
    public static final String RESULT_XML_MULTISTEP_5RUNS_3EVENTS_FVONLY_WITHVIDEO_EVENTNAME_1 = 'otto_homepage'
    public static final String RESULT_XML_MULTISTEP_5RUNS_3EVENTS_FVONLY_WITHVIDEO_EVENTNAME_2 = 'otto_search_shoes'
    public static final String RESULT_XML_MULTISTEP_5RUNS_3EVENTS_FVONLY_WITHVIDEO_EVENTNAME_3 = 'otto_product_boot'

    public static final String RESULT_XML_SINGLESTEP_1RUN_WITHVIDEO = 'Result_wptserver2.15_singlestep_1Run_WithVideo.xml'
    public static final String RESULT_XML_SINGLESTEP_1RUN_WITHVIDEO_EVENTNAME = 'IE_otto_hp_singlestep'

    public static final String RESULT_XML_SINGLESTEP_1RUN_WITHOUTVIDEO = 'Result_wptserver2.15-singlestep_1Run_WithoutVideo.xml'
    public static final String RESULT_XML_SINGLESTEP_1RUN_WITHOUTVIDEO_EVENTNAME = 'IE_otto_hp_singlestep'

    public static final String RESULT_XML_SINGLESTEP_5RUNS_FVONLY_WITHVIDEO = 'Result_wptserver2.15_singlestep_5Runs_WithVideo.xml'
    public static final String RESULT_XML_SINGLESTEP_5RUNS_FVONLY_WITHVIDEO_EVENTNAME = 'IE_otto_hp_singlestep'

    LocationAndResultPersisterService serviceUnderTest
    ServiceMocker mocker

    void setUp() {

        serviceUnderTest = service

        createTestDataCommonForAllTests()
        createMocksCommonForAllTests()

    }

    void createTestDataCommonForAllTests() {
        JobGroup jobGroup = TestDataUtil.createJobGroup(JobGroup.UNDEFINED_CSI, JobGroupType.CSI_AGGREGATION)
        WebPageTestServer wptServer = TestDataUtil.createWebPageTestServer(PROXY_IDENTIFIER_WPT_SERVER, PROXY_IDENTIFIER_WPT_SERVER, true, "http://${PROXY_IDENTIFIER_WPT_SERVER}/")
        Browser ff = TestDataUtil.createBrowser('Firefox', 1d)
        Browser ie = TestDataUtil.createBrowser('IE', 1d)
        Location locationFirefox = TestDataUtil.createLocation(wptServer, 'iteratec-dev-hetzner-64bit-ssd:Firefox', ff, true)
        Location locationIe = TestDataUtil.createLocation(wptServer, 'iteratec-dev-netlab-win7:IE', ie, true)
        List<Page> pages = TestDataUtil.createPages(['HP', 'SE'])

        Script testScript = TestDataUtil.createScript('test-script', 'description', 'navigate   http://my-url.de', false)
        TestDataUtil.createJob('FF_Otto_multistep', testScript, locationFirefox, jobGroup, '', 1 , false, 60)
        TestDataUtil.createJob('IE_otto_hp_singlestep', testScript, locationIe, jobGroup, '', 1 , false, 60)
    }

    void createMocksCommonForAllTests() {
        mocker = ServiceMocker.create()
        mocker.mockProxyService(serviceUnderTest)
        mocker.mockConfigService(serviceUnderTest, 'this.jdbc.driver.wont.support.rlike', 60)
        serviceUnderTest.pageService = new PageService()
        mocker.mockMeasuredValueTagService(serviceUnderTest, [:], [:], [:], [:], [:])
        serviceUnderTest.metaClass.informDependents = { List<EventResult> results ->
            // not the concern of this test
        }
        mocker.mockTTCsMappingService(serviceUnderTest)
        serviceUnderTest.performanceLoggingService = new PerformanceLoggingService()
    }

    void tearDown() {
        // Tear down logic here
    }

    // multistep ///////////////////////////////////////////////////////////////////////////////////////

    void testPersistingScreenshotDependentMetricsForMultistep1Run3EventsFvOnlyWithVideo() {
        //test data specific for this test
        File xmlResultFile = new File("test/resources/WptResultXmls/${RESULT_XML_MULTISTEP_1RUN_3EVENTS_FVONLY_WITHVIDEO}")
        GPathResult xmlResult = new XmlSlurper().parse(xmlResultFile)
        String har = 'notTheConcernOfThisTest'
        TestDataUtil.createMeasuredEvent(RESULT_XML_MULTISTEP_1RUN_3EVENTS_FVONLY_WITHVIDEO_EVENTNAME_1, Page.findByName('HP'))
        TestDataUtil.createMeasuredEvent(RESULT_XML_MULTISTEP_1RUN_3EVENTS_FVONLY_WITHVIDEO_EVENTNAME_2, Page.findByName('SE'))
        TestDataUtil.createMeasuredEvent(RESULT_XML_MULTISTEP_1RUN_3EVENTS_FVONLY_WITHVIDEO_EVENTNAME_3, Page.findByName('SE'))
        //test execution
        serviceUnderTest.listenToResult(xmlResult, har, WebPageTestServer.findByProxyIdentifier(PROXY_IDENTIFIER_WPT_SERVER))
        //assertions
        List<EventResult> allResults = EventResult.getAll()
        assertThat(allResults.size(), is(3))
        EventResult resultStep_1 = allResults.find {it.measuredEvent.name.equals(RESULT_XML_MULTISTEP_1RUN_3EVENTS_FVONLY_WITHVIDEO_EVENTNAME_1)}
        assertThat(resultStep_1.visuallyCompleteInMillisecs, is(2900))
        assertThat(resultStep_1.speedIndex, is(2182))
        EventResult resultStep_2 = allResults.find {it.measuredEvent.name.equals(RESULT_XML_MULTISTEP_1RUN_3EVENTS_FVONLY_WITHVIDEO_EVENTNAME_2)}
        assertThat(resultStep_2.visuallyCompleteInMillisecs, is(2500))
        assertThat(resultStep_2.speedIndex, is(791))
        EventResult resultStep_3 = allResults.find {it.measuredEvent.name.equals(RESULT_XML_MULTISTEP_1RUN_3EVENTS_FVONLY_WITHVIDEO_EVENTNAME_3)}
        assertThat(resultStep_3.visuallyCompleteInMillisecs, is(2100))
        assertThat(resultStep_3.speedIndex, is(858))

    }
    void testPersistingScreenshotDependentMetricsForMultistep1Run3EventsFvOnlyWithoutVideo() {
        //test data specific for this test
        File xmlResultFile = new File("test/resources/WptResultXmls/${RESULT_XML_MULTISTEP_1RUN_3EVENTS_FVONLY_WITHOUTVIDEO}")
        GPathResult xmlResult = new XmlSlurper().parse(xmlResultFile)
        String har = 'notTheConcernOfThisTest'
        TestDataUtil.createMeasuredEvent(RESULT_XML_MULTISTEP_1RUN_3EVENTS_FVONLY_WITHOUTVIDEO_EVENTNAME_1, Page.findByName('HP'))
        TestDataUtil.createMeasuredEvent(RESULT_XML_MULTISTEP_1RUN_3EVENTS_FVONLY_WITHOUTVIDEO_EVENTNAME_2, Page.findByName('SE'))
        TestDataUtil.createMeasuredEvent(RESULT_XML_MULTISTEP_1RUN_3EVENTS_FVONLY_WITHOUTVIDEO_EVENTNAME_3, Page.findByName('SE'))
        //test execution
        serviceUnderTest.listenToResult(xmlResult, har, WebPageTestServer.findByProxyIdentifier(PROXY_IDENTIFIER_WPT_SERVER))
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
        GPathResult xmlResult = new XmlSlurper().parse(xmlResultFile)
        String har = 'notTheConcernOfThisTest'
        TestDataUtil.createMeasuredEvent(RESULT_XML_MULTISTEP_5RUNS_3EVENTS_FVONLY_WITHVIDEO_EVENTNAME_1, Page.findByName('HP'))
        TestDataUtil.createMeasuredEvent(RESULT_XML_MULTISTEP_5RUNS_3EVENTS_FVONLY_WITHVIDEO_EVENTNAME_2, Page.findByName('SE'))
        TestDataUtil.createMeasuredEvent(RESULT_XML_MULTISTEP_5RUNS_3EVENTS_FVONLY_WITHVIDEO_EVENTNAME_3, Page.findByName('SE'))

        //test execution
        serviceUnderTest.listenToResult(xmlResult, har, WebPageTestServer.findByProxyIdentifier(PROXY_IDENTIFIER_WPT_SERVER))

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
        GPathResult xmlResult = new XmlSlurper().parse(xmlResultFile)
        String har = 'notTheConcernOfThisTest'
        TestDataUtil.createMeasuredEvent(RESULT_XML_SINGLESTEP_1RUN_WITHVIDEO_EVENTNAME, Page.findByName('HP'))
        //test execution
        serviceUnderTest.listenToResult(xmlResult, har, WebPageTestServer.findByProxyIdentifier(PROXY_IDENTIFIER_WPT_SERVER))
        //assertions
        List<EventResult> allResults = EventResult.getAll()
        assertThat(allResults.size(), is(2))
        EventResult fvResult = allResults.find {it.cachedView == CachedView.UNCACHED}
        assertThat(fvResult.visuallyCompleteInMillisecs, is(12000))
        assertThat(fvResult.speedIndex, is(7026))
        EventResult rvResult = allResults.find {it.cachedView == CachedView.CACHED}
        assertThat(rvResult.visuallyCompleteInMillisecs, is(3700))
        assertThat(rvResult.speedIndex, is(1852))
    }
    void testPersistingScreenshotDependentMetricsForSinglestep1RunWithoutVideo() {
        //test data specific for this test
        File xmlResultFile = new File("test/resources/WptResultXmls/${RESULT_XML_SINGLESTEP_1RUN_WITHOUTVIDEO}")
        GPathResult xmlResult = new XmlSlurper().parse(xmlResultFile)
        String har = 'notTheConcernOfThisTest'
        TestDataUtil.createMeasuredEvent(RESULT_XML_SINGLESTEP_1RUN_WITHOUTVIDEO_EVENTNAME, Page.findByName('HP'))
        //test execution
        serviceUnderTest.listenToResult(xmlResult, har, WebPageTestServer.findByProxyIdentifier(PROXY_IDENTIFIER_WPT_SERVER))
        //assertions
        List<EventResult> allResults = EventResult.getAll()
        assertThat(allResults.size(), is(2))

        assertThat(allResults*.visuallyCompleteInMillisecs, everyItem(
                nullValue()
        ))
        assertThat(allResults*.speedIndex, everyItem(
                is(EventResult.SPEED_INDEX_DEFAULT_VALUE)
        ))
    }
    void testPersistingScreenshotDependentMetricsForSinglestep5RunsWithVideo() {

        //test data specific for this test
        File xmlResultFile = new File("test/resources/WptResultXmls/${RESULT_XML_SINGLESTEP_5RUNS_FVONLY_WITHVIDEO}")
        GPathResult xmlResult = new XmlSlurper().parse(xmlResultFile)
        String har = 'notTheConcernOfThisTest'
        TestDataUtil.createMeasuredEvent(RESULT_XML_SINGLESTEP_5RUNS_FVONLY_WITHVIDEO_EVENTNAME, Page.findByName('HP'))

        //test execution
        serviceUnderTest.listenToResult(xmlResult, har, WebPageTestServer.findByProxyIdentifier(PROXY_IDENTIFIER_WPT_SERVER))

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

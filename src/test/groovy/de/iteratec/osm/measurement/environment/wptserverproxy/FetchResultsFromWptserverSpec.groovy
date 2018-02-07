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

import de.iteratec.osm.OsmConfiguration
import de.iteratec.osm.csi.CsiConfiguration
import de.iteratec.osm.csi.CsiDay
import de.iteratec.osm.measurement.environment.Browser
import de.iteratec.osm.measurement.environment.BrowserAlias
import de.iteratec.osm.measurement.environment.Location
import de.iteratec.osm.measurement.environment.WebPageTestServer
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.report.chart.CsiAggregationInterval
import de.iteratec.osm.result.EventResult
import de.iteratec.osm.result.JobResult
import de.iteratec.osm.util.PerformanceLoggingService
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import groovy.util.slurpersupport.GPathResult
import org.junit.Rule
import software.betamax.Configuration
import software.betamax.ProxyConfiguration
import software.betamax.junit.Betamax
import software.betamax.junit.RecorderRule
import spock.lang.Ignore
import spock.lang.Specification

import static org.hamcrest.Matchers.is
import static org.junit.Assert.assertThat
/**
 * Unit tests in this class test fetching of results from wptservers. In that they use wptservers getLocations.php function to
 * get xml result and proof the data registered {@link iResultListener}s get in their called fetchResults() method.
 */
@TestFor(ProxyService)
@Mock([WebPageTestServer, EventResult, JobResult, OsmConfiguration, CsiAggregationInterval, Browser, BrowserAlias, Location, JobGroup, CsiDay, CsiConfiguration])
@Ignore('[IT-1703] Re-Write these tests without mocking http requests (e.g. without betamax or similar library')
class FetchResultsFromWptserverTests extends Specification {

    public static final String WPTSERVER_MULTISTEP_URL = 'dev.server02.wpt.iteratec.de'
    public static final String WPTSERVER_SINGLESTEP_URL = 'www.webpagetest.org'
    private static final String LOCATION_IDENTIFIER_MULTISTEP = 'iteratec-dev-iteraHH-win7:IE'
    private static final String LOCATION_IDENTIFIER_SINGLESTEP = 'iteratec-dev-netlab-win7:IE'
    ProxyService serviceUnderTest

    Configuration configuration = ProxyConfiguration.builder().tapeRoot(new File("src/test/resources/betamax_tapes")).ignoreLocalhost(false).build();
    @Rule
    public RecorderRule recorder = new RecorderRule(configuration)

    def doWithSpring = {
        performanceLoggingService(PerformanceLoggingService)
        httpRequestService(HttpRequestService)
    }

    void setup() {

        serviceUnderTest = service
        mockHttpBuilderToUseBetamax()
        serviceUnderTest.performanceLoggingService = grailsApplication.mainContext.getBean('performanceLoggingService')

        createTestDataCommonToAllTests()
    }

    private void mockHttpBuilderToUseBetamax() {
        Properties properties = new Properties()
        new File('grails-app/conf/betamax.properties').withInputStream {
            properties.load(it)
        }
        String host = properties.'betamax.proxyHost'
        int port = properties.'betamax.proxyPort' as int
    }

    void createTestDataCommonToAllTests() {
        //Commented, so we can remove TestDataUtil. IT-1703, see TestDataUtil in history, to reproduce test data, if needed.
//        TestDataUtil.createOsmConfig()
//        TestDataUtil.createCsiAggregationIntervals()
//        WebPageTestServer wptserverMultistep = TestDataUtil.createWebPageTestServer(WPTSERVER_MULTISTEP_URL, WPTSERVER_MULTISTEP_URL, true, "http://${WPTSERVER_MULTISTEP_URL}/")
//        WebPageTestServer wptserverSinglestep = TestDataUtil.createWebPageTestServer(WPTSERVER_SINGLESTEP_URL, WPTSERVER_SINGLESTEP_URL, true, "http://${WPTSERVER_SINGLESTEP_URL}/")
//        List<Browser> browsers = TestDataUtil.createBrowsersAndAliases()
//        TestDataUtil.createJobGroups()
//        TestDataUtil.createLocation(wptserverMultistep, LOCATION_IDENTIFIER_MULTISTEP, browsers.find {
//            it.name.equals(Browser.UNDEFINED)
//        }, true)
//        TestDataUtil.createLocation(wptserverSinglestep, LOCATION_IDENTIFIER_SINGLESTEP, browsers.find {
//            it.name.equals(Browser.UNDEFINED)
//        }, true)
    }
    // TestSkript:
    //	logData	0
    ////um die hostnames vieler gemeinsamer adserver schon aufzulösen (sonst haben wir im IE10 zu viele pre-resolves)
    ////da esprit nicht per https aufgerufen wird bekommen wir nie gemeinsame third-party ressourcen in den cache
    ////setEventName	esprit_infrontofotto
    //	navigate	http://www.esprit.de
    ////für ein tls warmup im browser
    ////setEventName	google_infrontofotto
    //	navigate	https://google.de
    //	logData	1
    //	setEventName	HP_entry:::OTTO_Homepage_entry
    //	navigate	https://www.otto.de/
    //	setEventName	MES:::OTTO_Moduleinstieg_navigate
    //	navigate    https://www.otto.de/damenmode/
    //	setEventName	PL:::OTTO_Produktliste
    //	navigate    https://www.otto.de/damenmode/kategorien/schuhe/
    //	setEventName	SE:::OTTO_Suchergebnis_1_hosen_navigate
    //	navigate    https://www.otto.de/suche/hosen/
    //	setEventName	ADS:::OTTO_ADS_1_flashlisghts-hose_navigate
    //	navigate	https://www.otto.de/p/flashlights-cargohose-set-2-tlg-mit-guertel-100773864/#variationId=440054322
    ////Artikel	1 in WK
    //	logData	0
    //	exec	document.querySelector('#addToBasket').click();
    ////Artikel	2 in WK
    //	logData	1
    //	setEventName	SE:::OTTO_Suchergebnis_schuhe_navigate
    //	navigate https://www.otto.de/suche/schuhe/
    //	logData	0
    //	execAndWait	document.querySelector('#san_searchResult section .product a').click();
    //	exec	document.querySelector('#addToBasket').click();
    ////Artikel	3 in WK
    //	logData	1
    //	setEventName	SE:::OTTO_Suchergebnis_3_fernseher_navigate
    //	navigate https://www.otto.de/suche/fernseher/
    //	logData	0
    //	execAndWait	document.querySelector('#san_searchResult section .product a').click();
    //	exec	document.querySelector('#addToBasket').click();
    ////Artikel	4 in WK
    //	logData	1
    //	setEventName	SE:::OTTO_Suchergebnis_4_fahrrad_navigate
    //	navigate https://www.otto.de/suche/fahrrad/
    //	logData	0
    //	execAndWait	document.querySelector('#san_searchResult section .product a').click();
    //	exec	document.querySelector('#addToBasket').click();
    ////Artikel	5 in WK
    //	logData	1
    ////WK-Aufruf
    //	setEventName	WK:::OTTO_Warenkorb
    //	navigate	https://www.otto.de/order-system/basket
    // Expecting Only First View && Lable = FF_Otto_multistep && EventNames = otto_homepage & otto_search_shoes & otto_product_boot
    @Betamax(tape = 'FetchResultsFromWptserverTests_Multistep_1Run_11Events_JustFirstView')
    def testFetchResult_Multistep_1Run_11Events_JustFirstView() {
        given:
        //create test specific data
        def listener = new TestResultResultListener()
        serviceUnderTest.resultListeners[0] = listener
        File resultXmlFile = new File('src/test/resources/WptResultXmls/MULTISTEP_FORK_ITERATEC_1Run_11Events_JustFirstView.xml')
        WptResultXml expectedResult = new WptResultXml(new XmlSlurper().parseText(resultXmlFile.text))

        when:
        // Run the test:
        WebPageTestServer wptserver = WebPageTestServer.findByBaseUrl("http://${WPTSERVER_MULTISTEP_URL}/")
        WptResultXml resultXml = serviceUnderTest.fetchResult(wptserver, ['resultId': '160421_Q1_AR'])

        then:
        // Verify results
        assertThat(resultXml.statusCodeOfWholeTest, is(200))
        assertThat(listener.resultListeningCounter, is(1))
        assertThat(listener.wptserverOfLastListening, is(wptserver))
        listener.resultOfLastListening.responseNode.data == expectedResult.responseNode.data
    }
    //	TestSkript:
    //	setEventName	otto_homepage
    //	navigate	https://www.otto.de/
    //	setEventName	otto_search_shoes
    //	navigate    https://www.otto.de/suche/schuhe/
    //	setEventName	otto_product_boots
    //	navigate    https://www.otto.de/stiefel/
    // Expecting Only First View && Lable = FF_Otto_multistep && EventNames = otto_homepage & otto_search_shoes & otto_product_boot
    @Betamax(tape = 'FetchResultsFromWptserverTests_Result_wptserver2.13-multistep7_1Run_3Events_JustFirstView_WithoutVideo')
    def testFetchResult_Result_wptserver2_13_multistep7_1Run_3Events_JustFirstView_WithoutVideo() {
        given:
        //create test specific data
        def listener = new TestResultResultListener()
        serviceUnderTest.resultListeners[0] = listener
        File resultXmlFile = new File('src/test/resources/WptResultXmls/MULTISTEP_FORK_ITERATEC_1Run_3Events_JustFirstView_WithoutVideo.xml')
        WptResultXml expectedResult = new WptResultXml(new XmlSlurper().parseText(resultXmlFile.text))

        when:
        // Run the test:
        WebPageTestServer wptserver = WebPageTestServer.findByBaseUrl("http://${WPTSERVER_MULTISTEP_URL}/")
        WptResultXml resultXml = serviceUnderTest.fetchResult(wptserver, ['resultId': '160421_TN_D8'])

        then:
        // Verify results
        assertThat(resultXml.statusCodeOfWholeTest, is(200))
        assertThat(listener.resultListeningCounter, is(1))
        assertThat(listener.wptserverOfLastListening, is(wptserver))
        listener.resultOfLastListening.responseNode.data == expectedResult.responseNode.data
    }

    //	TestSkript:
    //	setEventName	esprit_infrontofotto
    //	navigate	http://www.esprit.de
    //	setEventName	HP_entry:::OTTO_Homepage_entry
    //	navigate	https://www.otto.de/
    //	setEventName	MES:::OTTO_Moduleinstieg_navigate
    //	navigate    https://www.otto.de/damenmode/
    // Expecting Only First View && Lable = FF_Otto_multistep && EventNames = otto_homepage & otto_search_shoes & otto_product_boot
    @Betamax(tape = 'FetchResultsFromWptserverTests_Result_wptserver2.13-multistep7_1Run_3Events_JustFirstView_WithVideo')
    def testFetchResult_Result_wptserver2_13_multistep7_1Run_3Events_JustFirstView_WithVideo() {
        given:
        //create test specific data
        def listener = new TestResultResultListener()
        serviceUnderTest.resultListeners[0] = listener
        File resultXmlFile = new File('src/test/resources/WptResultXmls/MULTISTEP_FORK_ITERATEC_1Run_3Events_JustFirstView_WithVideo.xml')
        WptResultXml expectedResult = new WptResultXml(new XmlSlurper().parseText(resultXmlFile.text))

        when:
        // Run the test:
        WebPageTestServer wptserver = WebPageTestServer.findByBaseUrl("http://${WPTSERVER_MULTISTEP_URL}/")
        WptResultXml resultXml = serviceUnderTest.fetchResult(wptserver, ['resultId': '160421_A9_EF'])

        then:
        // Verify results
        assertThat(resultXml.statusCodeOfWholeTest, is(200))
        assertThat(listener.resultListeningCounter, is(1))
        assertThat(listener.wptserverOfLastListening, is(wptserver))
        listener.resultOfLastListening.responseNode.data == expectedResult.responseNode.data
    }
    //	TestSkript:
    //	setEventName	IE_otto_hp_singlestep
    //	navigate	https://www.otto.de/
    // Expecting Repeated View && Lable = IE_otto_hp_singlestep && EventName = IE_otto_hp_singlestep
    @Betamax(tape = 'FetchResultsFromWptserverTests_Result_wptserver2.15-singlestep_1Run_WithoutVideo')
    void testFetchResult_Result_wptserver2_15_singlestep_1Run_WithoutVideo() {
        given:
        //create test specific data
        def listener = new TestResultResultListener()
        serviceUnderTest.resultListeners[0] = listener
        File resultXmlFile = new File('src/test/resources/WptResultXmls/BEFORE_MULTISTEP_1Run_WithoutVideo.xml')
        WptResultXml expectedResult = new WptResultXml(new XmlSlurper().parseText(resultXmlFile.text))

        when:
        // Run the test:
        WebPageTestServer wptserver = WebPageTestServer.findByBaseUrl("http://${WPTSERVER_SINGLESTEP_URL}/")
        WptResultXml resultXml = serviceUnderTest.fetchResult(wptserver, ['resultId': '160421_VP_ZMG'])

        then:
        // Verify results
        assertThat(resultXml.statusCodeOfWholeTest, is(200))
        assertThat(listener.resultListeningCounter, is(1))
        assertThat(listener.wptserverOfLastListening, is(wptserver))
        listener.resultOfLastListening.responseNode.data == expectedResult.responseNode.data
    }
    //	TestSkript:
    //	setEventName	IE_otto_hp_singlestep
    //	navigate	https://www.otto.de/
    // Expecting Repeated View && Lable = IE_otto_hp_singlestep && EventName = IE_otto_hp_singlestep
    @Betamax(tape = 'FetchResultsFromWptserverTests_Result_wptserver2.15_singlestep_1Run_WithVideo')
    void testFetchResult_Result_wptserver2_15_singlestep_1Run_WithVideo() {
        given:
        //create test specific data
        def listener = new TestResultResultListener()
        serviceUnderTest.resultListeners[0] = listener
        File resultXmlFile = new File('src/test/resources/WptResultXmls/BEFORE_MULTISTEP_1Run_WithVideo.xml')
        WptResultXml expectedResult = new WptResultXml(new XmlSlurper().parseText(resultXmlFile.text))

        when:
        // Run the test:
        WebPageTestServer wptserver = WebPageTestServer.findByBaseUrl("http://${WPTSERVER_SINGLESTEP_URL}/")
        WptResultXml resultXml = serviceUnderTest.fetchResult(wptserver, ['resultId': '160421_5E_ZFW'])

        then:
        // Verify results
        assertThat(resultXml.statusCodeOfWholeTest, is(200))
        assertThat(listener.resultListeningCounter, is(1))
        assertThat(listener.wptserverOfLastListening, is(wptserver))
        listener.resultOfLastListening.responseNode.data == expectedResult.responseNode.data
    }

    class TestResultResultListener implements iResultListener {

        int resultListeningCounter = 0
        WptResultXml resultOfLastListening
        WebPageTestServer wptserverOfLastListening

        public String getListenerName() { return 'test-resultListeners' }

        public List<Location> listenToLocations(GPathResult result, WebPageTestServer wptserver) { return [] }

        public void listenToResult(
                WptResultXml xmlResult,
                WebPageTestServer wptserver
        ) {
            resultOfLastListening = xmlResult
            wptserverOfLastListening = wptserver
            resultListeningCounter++
        }

        @Override
        boolean callListenerAsync() {
            return false
        }
    }

}

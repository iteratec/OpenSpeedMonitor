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

package de.iteratec.osm.measurement.environment.wptserver

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
import de.iteratec.osm.result.WptStatus
import de.iteratec.osm.util.PerformanceLoggingService
import grails.buildtestdata.BuildDataTest
import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import groovy.util.slurpersupport.GPathResult
import spock.lang.Specification

import static org.hamcrest.Matchers.is
import static org.junit.Assert.assertThat

/**
 * Unit tests in this class test fetching of results from wptservers. In that they use wptservers getLocations.php function to
 * get xml result and proof the data registered {@link iResultListener}s get in their called fetchResults() method.
 */
class FetchResultsFromWptserverTests extends Specification implements ServiceUnitTest<WptInstructionService>, DataTest, BuildDataTest {

    public static final String WPTSERVER_MULTISTEP_URL = 'dev.server02.wpt.iteratec.de'
    public static final String WPTSERVER_SINGLESTEP_URL = 'www.webpagetest.org'
    WptInstructionService serviceUnderTest

    Closure doWithSpring() {
        return {
            performanceLoggingService(PerformanceLoggingService)
            httpRequestService(HttpRequestService)
        }
    }

    void setup() {
        serviceUnderTest = service
        serviceUnderTest.performanceLoggingService = grailsApplication.mainContext.getBean('performanceLoggingService')
        serviceUnderTest.httpRequestService = Stub(HttpRequestService)

        WebPageTestServer.build(id: 0, baseUrl: "http://${WPTSERVER_MULTISTEP_URL}/")
        WebPageTestServer.build(id: 1, baseUrl: "http://${WPTSERVER_SINGLESTEP_URL}/")
    }

    void setupSpec() {
        mockDomains(WebPageTestServer, EventResult, JobResult, OsmConfiguration, CsiAggregationInterval, Browser,
                BrowserAlias, Location, JobGroup, CsiDay, CsiConfiguration)
    }

    //	TestSkript:
    //	setEventName	otto_homepage
    //	navigate	https://www.otto.de/
    //	setEventName	otto_search_shoes
    //	navigate    https://www.otto.de/suche/schuhe/
    //	setEventName	otto_product_boots
    //	navigate    https://www.otto.de/stiefel/
    // Expecting Only First View && Lable = FF_Otto_multistep && EventNames = otto_homepage & otto_search_shoes & otto_product_boot
    def testFetchResult_Result_wptserver2_13_multistep7_1Run_3Events_JustFirstView_WithoutVideo() {
        given:
        //create test specific data
        File resultXmlFile = new File('src/test/resources/WptResultXmls/MULTISTEP_FORK_ITERATEC_1Run_3Events_JustFirstView_WithoutVideo.xml')
        GPathResult content = new XmlSlurper().parseText(resultXmlFile.text)
        WptResultXml expectedResult = new WptResultXml(content)
        serviceUnderTest.httpRequestService = Stub(HttpRequestService) {
            getWptServerHttpGetResponse(_ as WebPageTestServer, _ as String, _ as Map, _ as String, _ as Map) >> content
        }

        when:
        // Run the test:
        WebPageTestServer wptserver = WebPageTestServer.findByBaseUrl("http://${WPTSERVER_MULTISTEP_URL}/")
        WptResultXml resultXml = serviceUnderTest.fetchResult(wptserver, '160421_TN_D8')

        then:
        // Verify results
        assertThat(resultXml.wptStatus, is(WptStatus.COMPLETED))
        assertThat(resultXml.responseNode, is(content))
        assertThat(resultXml.runCount, is(1))
        assertThat(resultXml.testStepCount, is(3))
        assertThat(resultXml.runNodes, is(expectedResult.runNodes))
    }


    //	TestSkript:
    //	setEventName	IE_otto_hp_singlestep
    //	navigate	https://www.otto.de/
    // Expecting Repeated View && Lable = IE_otto_hp_singlestep && EventName = IE_otto_hp_singlestep
    void testFetchResult_Result_wptserver2_15_singlestep_1Run_WithoutVideo() {
        given:
        //create test specific data
        File resultXmlFile = new File('src/test/resources/WptResultXmls/BEFORE_MULTISTEP_1Run_WithoutVideo.xml')
        GPathResult content = new XmlSlurper().parseText(resultXmlFile.text)
        WptResultXml expectedResult = new WptResultXml(content)
        serviceUnderTest.httpRequestService = Stub(HttpRequestService) {
            getWptServerHttpGetResponse(_ as WebPageTestServer, _ as String, _ as Map, _ as String, _ as Map) >> content
        }

        when:
        // Run the test:
        WebPageTestServer wptserver = WebPageTestServer.findByBaseUrl("http://${WPTSERVER_SINGLESTEP_URL}/")
        WptResultXml resultXml = serviceUnderTest.fetchResult(wptserver, '160421_VP_ZMG')

        then:
        // Verify results
        assertThat(resultXml.wptStatus, is(WptStatus.COMPLETED))
        assertThat(resultXml.responseNode, is(content))
        assertThat(resultXml.runCount, is(1))
        assertThat(resultXml.runNodes, is(expectedResult.runNodes))
    }

    void testFetchResult_Result_wptserver2_15_singlestep_1Run_5Steps() {
        given:
        //create test specific data
        File resultXmlFile = new File('src/test/resources/WptResultXmls/MULTISTEP_1Run_5Steps.xml')
        GPathResult content = new XmlSlurper().parseText(resultXmlFile.text)
        WptResultXml expectedResult = new WptResultXml(content)
        serviceUnderTest.httpRequestService = Stub(HttpRequestService) {
            getWptServerHttpGetResponse(_ as WebPageTestServer, _ as String, _ as Map, _ as String, _ as Map) >> content
        }

        when:
        // Run the test:
        WebPageTestServer wptserver = WebPageTestServer.findByBaseUrl("http://${WPTSERVER_SINGLESTEP_URL}/")
        WptResultXml resultXml = serviceUnderTest.fetchResult(wptserver, '160421_VP_ZMG')

        then:
        // Verify results
        assertThat(resultXml.wptStatus, is(WptStatus.COMPLETED))
        assertThat(resultXml.responseNode, is(content))
        assertThat(resultXml.runCount, is(1))
        assertThat(resultXml.testStepCount, is(5))
        assertThat(resultXml.runNodes, is(expectedResult.runNodes))
    }
}

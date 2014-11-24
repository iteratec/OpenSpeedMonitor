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
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.measurement.script.Script
import de.iteratec.osm.result.EventResult
import de.iteratec.osm.result.JobResult
import de.iteratec.osm.result.MeasuredEvent
import de.iteratec.osm.result.detail.WaterfallEntry
import de.iteratec.osm.result.detail.WebPerformanceWaterfall
import de.iteratec.osm.util.ServiceMocker
import groovy.util.slurpersupport.GPathResult

import static org.junit.Assert.*

import grails.test.mixin.*
import grails.test.mixin.support.*

/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
@TestMixin(GrailsUnitTestMixin)
@TestFor(LocationAndResultPersisterService)
@Mock([WebPageTestServer, Browser, Location, Job, JobResult, EventResult, BrowserAlias, Page, MeasuredEvent, JobGroup, Script, WebPerformanceWaterfall, WaterfallEntry])
class PersistVisuallyCompleteForEventResultsSpec {

    public static final String PROXY_IDENTIFIER_WPT_SERVER = 'dev.server02.wpt.iteratec.de'

    LocationAndResultPersisterService serviceUnderTest
    ServiceMocker mocker

    void setUp() {
        serviceUnderTest = service
        // test data common to all tests
        TestDataUtil.createWebPageTestServer(PROXY_IDENTIFIER_WPT_SERVER, PROXY_IDENTIFIER_WPT_SERVER, true, "http://${PROXY_IDENTIFIER_WPT_SERVER}/")
        //mocks common for all tests
        mocker = ServiceMocker.create()
        mocker.mockProxyService(serviceUnderTest)

    }

    void tearDown() {
        // Tear down logic here
    }

    void testSomething() {
        File xmlResultFile = new File("test/resources/WptResultXmls/Result_wptserver2.13-multistep7_1Run_3Events_WithVideo.xml")
        GPathResult xmlResult = new XmlSlurper().parse(xmlResultFile)
        String har = 'notTheConcernOfThisTest'
        serviceUnderTest.listenToResult(xmlResult, har, WebPageTestServer.findByProxyIdentifier(PROXY_IDENTIFIER_WPT_SERVER))
    }
}

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

import static org.apache.http.conn.params.ConnRoutePNames.DEFAULT_PROXY
import static org.hamcrest.Matchers.*
import static org.junit.Assert.*
import co.freeside.betamax.Betamax
import co.freeside.betamax.Recorder
import de.iteratec.osm.OsmConfiguration
import de.iteratec.osm.csi.TestDataUtil
import de.iteratec.osm.measurement.environment.Browser
import de.iteratec.osm.measurement.environment.BrowserAlias
import de.iteratec.osm.measurement.environment.Location
import de.iteratec.osm.measurement.environment.WebPageTestServer
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.report.chart.AggregatorType
import de.iteratec.osm.report.chart.MeasuredValueInterval
import de.iteratec.osm.result.EventResult
import de.iteratec.osm.result.JobResult
import de.iteratec.osm.util.PerformanceLoggingService
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import groovy.util.slurpersupport.GPathResult
import groovyx.net.http.RESTClient

import org.apache.http.HttpHost
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Unit tests in this class test fetching of results from wptservers. In that they use wptservers getLocations.php function to
 * get xml result and proof the data registered {@link iListener}s get in their called fetchResults() method.
 */
@TestFor(ProxyService)
@Mock([WebPageTestServer, EventResult, JobResult, OsmConfiguration, AggregatorType, MeasuredValueInterval, Browser, BrowserAlias, Location, JobGroup])
class FetchResultsFromWptserverTests {

	public static final String WPTSERVER_MULTISTEP_URL = 'dev.server02.wpt.iteratec.de'
	public static final String WPTSERVER_SINGLESTEP_URL = 'dev.server01.wpt.iteratec.de'
	private static final String LOCATION_IDENTIFIER_MULTISTEP = 'iteratec-dev-iteraHH-win7:IE'
	private static final String LOCATION_IDENTIFIER_SINGLESTEP = 'iteratec-dev-netlab-win7:IE'
	ProxyService serviceUnderTest

	@Rule public Recorder recorder = new Recorder(new ConfigSlurper().parse(new File('grails-app/conf/BetamaxConfig.groovy').toURL()).toProperties())

	@Before
	void setUp() {

		serviceUnderTest=service

		//mock HttpBuilder in HttpRequestService to use betamax-proxy
		Map betamaxProps = new ConfigSlurper().parse(new File('grails-app/conf/BetamaxConfig.groovy').toURL()).flatten()
		HttpRequestService httpRequestService = new HttpRequestService()
		httpRequestService.metaClass.getClient = {WebPageTestServer wptserver ->
			RESTClient restClient = new RESTClient(wptserver.baseUrl)
			restClient.client.params.setParameter(DEFAULT_PROXY, new HttpHost(betamaxProps['betamax.proxyHost'], betamaxProps['betamax.proxyPort'], 'http'))
			return restClient
		}
		serviceUnderTest.httpRequestService = httpRequestService
        serviceUnderTest.performanceLoggingService = new PerformanceLoggingService()

		createTestDataCommonToAllTests()
	}

	void createTestDataCommonToAllTests() {
		TestDataUtil.createOsmConfig()
		TestDataUtil.createAggregatorTypes()
		TestDataUtil.createMeasuredValueIntervals()
		WebPageTestServer wptserverMultistep = TestDataUtil.createWebPageTestServer(WPTSERVER_MULTISTEP_URL, WPTSERVER_MULTISTEP_URL, true, "http://${WPTSERVER_MULTISTEP_URL}/")
		WebPageTestServer wptserverSinglestep = TestDataUtil.createWebPageTestServer(WPTSERVER_SINGLESTEP_URL, WPTSERVER_SINGLESTEP_URL, true, "http://${WPTSERVER_SINGLESTEP_URL}/")
		List<Browser> browsers = TestDataUtil.createBrowsersAndAliases()
		TestDataUtil.createJobGroups()
		TestDataUtil.createLocation(wptserverMultistep, LOCATION_IDENTIFIER_MULTISTEP, browsers.find {it.name.equals(Browser.UNDEFINED)}, true)
		TestDataUtil.createLocation(wptserverSinglestep, LOCATION_IDENTIFIER_SINGLESTEP, browsers.find {it.name.equals(Browser.UNDEFINED)}, true)
	}

	@Test
	@Betamax(tape = 'FetchResultsFromWptserverTests_Multistep_1Run_11Events_JustFirstView')
	void testFetchResult_Multistep_1Run_11Events_JustFirstView() {

		//create test specific data
		def listener = new TestResultListener()
		serviceUnderTest.listener[0] = listener
		File resultXmlFile = new File('test/resources/WptResultXmls/Multistep_1Run_11Events_JustFirstView.xml')
		GPathResult expectedResult = new XmlSlurper().parseText(resultXmlFile.text)

		// Run the test:
		WebPageTestServer wptserver = WebPageTestServer.findByBaseUrl("http://${WPTSERVER_MULTISTEP_URL}/")
		Integer statusCode = serviceUnderTest.fetchResult(wptserver, ['resultId':'141013_EC_2026c719a0818c3f9b8d39c72ac3cd06'])

		// Verify results
		assertThat(statusCode, is(200))
		assertThat(listener.resultListeningCounter, is(1))
		assertThat(listener.wptserverOfLastListening, is(wptserver))
		assertThat(listener.resultOfLastListening.data, is(expectedResult.data))
	}

	@Test
	@Betamax(tape = 'FetchResultsFromWptserverTests_Result_wptserver2.13-multistep7_1Run_3Events_JustFirstView_WithoutVideo')
	void testFetchResult_Result_wptserver2_13_multistep7_1Run_3Events_JustFirstView_WithoutVideo() {

		//create test specific data
		def listener = new TestResultListener()
		serviceUnderTest.listener[0] = listener
		File resultXmlFile = new File('test/resources/WptResultXmls/Result_wptserver2.13-multistep7_1Run_3Events_JustFirstView_WithoutVideo.xml')
		GPathResult expectedResult = new XmlSlurper().parseText(resultXmlFile.text)

		// Run the test:
		WebPageTestServer wptserver = WebPageTestServer.findByBaseUrl("http://${WPTSERVER_MULTISTEP_URL}/")
		Integer statusCode = serviceUnderTest.fetchResult(wptserver, ['resultId':'141124_RF_NY'])

		// Verify results
		assertThat(statusCode, is(200))
		assertThat(listener.resultListeningCounter, is(1))
		assertThat(listener.wptserverOfLastListening, is(wptserver))
		assertThat(listener.resultOfLastListening.data, is(expectedResult.data))
	}

	@Test
	@Betamax(tape = 'FetchResultsFromWptserverTests_Result_wptserver2.13-multistep7_1Run_3Events_JustFirstView_WithVideo')
	void testFetchResult_Result_wptserver2_13_multistep7_1Run_3Events_JustFirstView_WithVideo() {

		//create test specific data
		def listener = new TestResultListener()
		serviceUnderTest.listener[0] = listener
		File resultXmlFile = new File('test/resources/WptResultXmls/Result_wptserver2.13-multistep7_1Run_3Events_JustFirstView_WithVideo.xml')
		GPathResult expectedResult = new XmlSlurper().parseText(resultXmlFile.text)

		// Run the test:
		WebPageTestServer wptserver = WebPageTestServer.findByBaseUrl("http://${WPTSERVER_MULTISTEP_URL}/")
		Integer statusCode = serviceUnderTest.fetchResult(wptserver, ['resultId':'141125_XC_13E'])

		// Verify results
		assertThat(statusCode, is(200))
		assertThat(listener.resultListeningCounter, is(1))
		assertThat(listener.wptserverOfLastListening, is(wptserver))
		assertThat(listener.resultOfLastListening.data, is(expectedResult.data))
	}

	@Test
	@Betamax(tape = 'FetchResultsFromWptserverTests_Result_wptserver2.15-singlestep_1Run_WithoutVideo')
	void testFetchResult_Result_wptserver2_15_singlestep_1Run_WithoutVideo() {

		//create test specific data
		def listener = new TestResultListener()
		serviceUnderTest.listener[0] = listener
		File resultXmlFile = new File('test/resources/WptResultXmls/Result_wptserver2.15-singlestep_1Run_WithoutVideo.xml')
		GPathResult expectedResult = new XmlSlurper().parseText(resultXmlFile.text)

		// Run the test:
		WebPageTestServer wptserver = WebPageTestServer.findByBaseUrl("http://${WPTSERVER_SINGLESTEP_URL}/")
		Integer statusCode = serviceUnderTest.fetchResult(wptserver, ['resultId':'141125_3Q_23'])

		// Verify results
		assertThat(statusCode, is(200))
		assertThat(listener.resultListeningCounter, is(1))
		assertThat(listener.wptserverOfLastListening, is(wptserver))
		assertThat(listener.resultOfLastListening.data, is(expectedResult.data))
	}

	@Test
	@Betamax(tape = 'FetchResultsFromWptserverTests_Result_wptserver2.15_singlestep_1Run_WithVideo')
	void testFetchResult_Result_wptserver2_15_singlestep_1Run_WithVideo() {

		//create test specific data
		def listener = new TestResultListener()
		serviceUnderTest.listener[0] = listener
		File resultXmlFile = new File('test/resources/WptResultXmls/Result_wptserver2.15_singlestep_1Run_WithVideo.xml')
		GPathResult expectedResult = new XmlSlurper().parseText(resultXmlFile.text)

		// Run the test:
		WebPageTestServer wptserver = WebPageTestServer.findByBaseUrl("http://${WPTSERVER_SINGLESTEP_URL}/")
		Integer statusCode = serviceUnderTest.fetchResult(wptserver, ['resultId':'141125_B2_25'])

		// Verify results
		assertThat(statusCode, is(200))
		assertThat(listener.resultListeningCounter, is(1))
		assertThat(listener.wptserverOfLastListening, is(wptserver))
		assertThat(listener.resultOfLastListening.data, is(expectedResult.data))
	}

	class TestResultListener implements iListener {

		int resultListeningCounter = 0
		GPathResult resultOfLastListening
		WebPageTestServer wptserverOfLastListening

		public String getName(){return 'test-listener'}
		public void listenToLocations(GPathResult result, WebPageTestServer wptserver){}
		public void listenToResult(
				GPathResult result,
				String har,
				WebPageTestServer wptserver
		) {
			resultOfLastListening = result
			wptserverOfLastListening = wptserver
			resultListeningCounter++
		}
	}

}

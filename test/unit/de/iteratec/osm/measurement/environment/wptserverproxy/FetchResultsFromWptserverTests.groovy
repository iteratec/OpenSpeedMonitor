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

import co.freeside.betamax.Betamax
import co.freeside.betamax.Recorder
import de.iteratec.osm.measurement.environment.WebPageTestServer
import de.iteratec.osm.result.EventResult
import de.iteratec.osm.result.JobResult
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import groovyx.net.http.RESTClient
import org.apache.http.HttpHost
import org.junit.Before
import org.junit.Rule
import org.junit.Test

import static org.apache.http.conn.params.ConnRoutePNames.DEFAULT_PROXY

/**
 * Unit tests in this class test fetching of results from wptservers. In that they use wptservers /getLocations.php function to
 * get xml result and proof the data registered {@link iListener}s get in their called fetchResults() method.
 */
@TestFor(ProxyService)
@Mock([WebPageTestServer, EventResult, JobResult])
class FetchResultsFromWptserverTests {

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
	}

	@Test
	@Betamax(tape = 'ProxyServiceTests_NotSuccessful_Result_NoMultistep_Error_testCompletedButThereWereNoSuccessfulResults')
	void testListenToCompletedTestWithoutSuccessfulResults() {
		// Just a non-existing server:
		WebPageTestServer aServer = new WebPageTestServer(
				baseUrl: 'http://example.com').save(failOnError: true, validate: false)

		// Mock load of data:
		File file = new File('test/resources/WptResultXmls/Result_NoMultistep_Error_testCompletedButThereWereNoSuccessfulResults.xml')
//		def httpRequestService = mockFor(HttpRequestService)
//		httpRequestService.demand.getWptServerHttpGetResponseAsGPathResult(1..10000) {
//			WebPageTestServer wptserver, String path, Map query, ContentType contentType, Map headers ->
//			GPathResult xmlResult = new XmlSlurper().parse(file)
//			return xmlResult
//		}
//		serviceUnderTest.httpRequestService = httpRequestService.createMock()


		// Run the test:
		Integer statusCode = serviceUnderTest.fetchResult(aServer, ['resultId':'121212_NH_6a2777a9c09ac89e108d1f2b94e74b83'])

		// Verify results
		assertEquals(200, statusCode)
		assertEquals(0, EventResult.list().size())
		assertEquals(0, JobResult.list().size())
	}
}

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

import grails.test.mixin.*
import groovy.util.slurpersupport.GPathResult
import groovyx.net.http.ContentType

import org.junit.*

import de.iteratec.osm.result.EventResult
import de.iteratec.osm.result.JobResult
import de.iteratec.osm.measurement.environment.WebPageTestServer

/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
@TestFor(ProxyService)
@Mock([WebPageTestServer, EventResult, JobResult])
class ProxyServiceTests {

	ProxyService serviceUnderTest

	@Before
	void setUp() {
		serviceUnderTest=service
	}

	@Test
	void testListenToCompletedTestWithoutSuccessfulResults() {
		// Just a non-existing server:
		WebPageTestServer aServer = new WebPageTestServer(
				baseUrl: 'http://example.com').save(failOnError: true, validate: false)

		// Mock load of data:
		File file = new File('test/resources/WptResultXmls/Result_NoMultistep_Error_testCompletedButThereWereNoSuccessfulResults.xml')
		def httpRequestService = mockFor(HttpRequestService)
		httpRequestService.demand.getWptServerHttpGetResponseAsGPathResult(1..10000) { 
			WebPageTestServer wptserver, String path, Map query, ContentType contentType, Map headers ->
			GPathResult xmlResult = new XmlSlurper().parse(file)
			return xmlResult
		}

		// Run the test:
		serviceUnderTest.httpRequestService = httpRequestService.createMock()
		Integer statusCode = serviceUnderTest.fetchResult(aServer, ['resultId':'121212_NH_6a2777a9c09ac89e108d1f2b94e74b83'])

		// Verify results
		assertEquals(200, statusCode)
		assertEquals(0, EventResult.list().size())
		assertEquals(0, JobResult.list().size())
	}
}

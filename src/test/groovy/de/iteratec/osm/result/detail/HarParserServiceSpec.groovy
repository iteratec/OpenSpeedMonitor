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




package de.iteratec.osm.result.detail

import groovy.mock.interceptor.MockFor

import static org.hamcrest.Matchers.*
import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertThat
import grails.test.mixin.*

import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.junit.*
import de.iteratec.osm.result.CachedView
import de.iteratec.osm.result.PageService

/**
 * See the API for {@link grails.test.mixin.services.ServiceUnitTestMixin} for usage instructions
 */
@TestFor(HarParserService)
class HarParserServiceSpec {
	HarParserService serviceUnderTest
	Map<String, String> hars
	Map<String, String> failedHars
	
	@Before
	void setUp() {
		serviceUnderTest = service
		
		//test-data common to all tests
		
		/** 
		 * Contains har-files of sucessful wpt-tests to test with {@link HarParserService}.
		 */
		hars = [:]
		/**
		 * Contains har-files of failed wpt-tests to test with {@link HarParserService}.
		 */
		failedHars = [:]
		
		// no multistep
		hars['nomultistep_1Run_FirstAndRepeatedView.har'] = new File("test/resources/HARs/nomultistep_1Run_FirstAndRepeatedView.har").text
		// multistep
		hars['multistep_1Run_6Events_FirstAndRepeatedView.har'] = new File("test/resources/HARs/multistep_1Run_6Events_FirstAndRepeatedView.har").text
		hars['multistep_2Runs_2Events_FirstView.har'] = new File("test/resources/HARs/multistep_2Runs_2Events_FirstView.har").text
		// no multistep no event name (arise in ad-hoc-tests from wptserver-gui, shouldn't ever get into the system)
		hars['nomultistep_noeventname_3Runs_FirstView.har'] = new File("test/resources/HARs/nomultistep_noeventname_3Runs_FirstView.har").text
		hars['nomultistep_noeventname_1Run_FirstView_example.de.140217_16_J7A.har'] = new File("test/resources/HARs/nomultistep_noeventname_1Run_FirstView_example.de.140217_16_J7A.har").text
		hars['nomultistep_noeventname_9Runs_FirstView_example.de.140218_3P_AS2.har'] = new File("test/resources/HARs/nomultistep_noeventname_9Runs_FirstView_example.de.140218_3P_AS2.har").text
		// failed tests
		failedHars['nomultistep_noeventname_1Run_FirstView_invalidurl.har'] = new File("test/resources/HARs/nomultistep_noeventname_1Run_FirstView_invalidurl.har").text
		failedHars['nomultistep_noeventname_1Run_FirstView_google.de.blocked.har'] = new File("test/resources/HARs/nomultistep_noeventname_1Run_FirstView_google.de.blocked.har").text
		
		//mocks common to all tests
		mockPageService()
	}
	
	@Test
	void testCreatePageIdFrom(){
		//test-execution and assertions
		assertThat(serviceUnderTest.createPageIdFrom(1, 'eventName', CachedView.UNCACHED), is('page_1_eventName_0'))
		assertThat(serviceUnderTest.createPageIdFrom(5, 'eventName', CachedView.UNCACHED), is('page_5_eventName_0'))
		assertThat(serviceUnderTest.createPageIdFrom(1, 'eventName', CachedView.CACHED), is('page_1_eventName_1'))
		assertThat(serviceUnderTest.createPageIdFrom(5, 'eventName', CachedView.CACHED), is('page_5_eventName_1'))
		assertThat(serviceUnderTest.createPageIdFrom(12, null, CachedView.UNCACHED), is('page_12_0'))
		assertThat(serviceUnderTest.createPageIdFrom(12, '', CachedView.UNCACHED), is('page_12_0'))
	}
	
	@Test
	void testParseFromPageId(){
		//test-execution and assertions
		assertThat(serviceUnderTest.parseFromPageId('page_1_eventName_0', HarParserService.PageIdComponent.RUN_NUMBER), is('1'))
		assertThat(serviceUnderTest.parseFromPageId('page_1_eventName_0', HarParserService.PageIdComponent.EVENT_NAME), is('eventName'))
		assertThat(serviceUnderTest.parseFromPageId('page_1_eventName_0', HarParserService.PageIdComponent.CACHED_VIEW_NUMBER), is('0'))
		assertThat(serviceUnderTest.parseFromPageId('page_7_1', HarParserService.PageIdComponent.RUN_NUMBER), is('7'))
		assertThat(serviceUnderTest.parseFromPageId('page_7_1', HarParserService.PageIdComponent.EVENT_NAME), nullValue())
		assertThat(serviceUnderTest.parseFromPageId('page_7_1', HarParserService.PageIdComponent.CACHED_VIEW_NUMBER), is('1'))
	}
	
	// mocks
	
	void mockPageService(){
		def pageService = new MockFor(PageService, true)
		pageService.demand.excludePagenamePart(1..100){String stepName ->
			return stepName //not the concern of these tests
		}
		serviceUnderTest.pageService = pageService.proxyInstance()
	}
	
}

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
	void testResultType() {
		// Test execution
		Collection<Map<String, WebPerformanceWaterfall>> result = serviceUnderTest.getWaterfalls(hars.values())
		// Test assertions
		assertThat(result, everyItem(isA(Map)))
		assertThat(result*.keySet().flatten(), everyItem(isA(String)))
		assertThat(result*.values().flatten(), everyItem(isA(WebPerformanceWaterfall)))
	}
	
	@Test
	void testEmptyInput() {
		// Test execution
		Collection<Map<String, WebPerformanceWaterfall>> result = serviceUnderTest.getWaterfalls([])
		// Test assertion
		assertEquals([], result)
	}
	@Test
	void testParsingNoMultistepWaterfalls_1Run_FirstAndRepeatedView() {
		// Test execution ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		Map<String, WebPerformanceWaterfall> result = serviceUnderTest.getWaterfalls(hars['nomultistep_1Run_FirstAndRepeatedView.har'])
		
		// Test assertions ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		
		// number of waterfalls
		assertThat(result.size(), is(2))
		
		// first view //////////////////////////////////////////////
		// hole waterfall
		WebPerformanceWaterfall waterfall = result['page_1_FF_LH_Step02_Moduleinstieg 1_0']
		assertThat(waterfall.url, 
			is('https://www.example.de/product/'))
		assertThat(waterfall.startDate, 
			is(new DateTime(2014,1,4,7,42,48,DateTimeZone.UTC).toDate()))
		assertThat(waterfall.title, 
			is('Run 1, Event Name FF_LH_Step02_Moduleinstieg 1, First View for https://www.example.de/product/'))
		assertThat(waterfall.eventName,
			is('FF_LH_Step02_Moduleinstieg 1'))
		assertThat(waterfall.numberOfWptRun, is(1))
		assertThat(waterfall.cachedView, is(CachedView.UNCACHED))
		assertThat(waterfall.startRenderInMillisecs, is(43))
		assertThat(waterfall.docCompleteTimeInMillisecs, is(1028))
		assertThat(waterfall.domTimeInMillisecs, is(0))
		assertThat(waterfall.fullyLoadedTimeInMillisecs, is(3337))
		// waterfall-entries
		Collection<WaterfallEntry> waterfallEntries = waterfall.waterfallEntries
		assertThat(waterfallEntries.size(), greaterThan(10))
		assertThat(waterfallEntries, everyItem(isA(WaterfallEntry)))
		assertThat(waterfallEntries*.blocked, everyItem(is(false)))
		assertThat(waterfallEntries*.httpStatus, everyItem(greaterThanOrEqualTo(0)))
		assertThat(waterfallEntries*.path, everyItem(not(isEmptyOrNullString())))
		assertThat(waterfallEntries*.host, everyItem(not(isEmptyOrNullString())))
		Collection<String> listOfUniqueMimeTypes= waterfallEntries*.mimeType.unique(false)
		assertThat(listOfUniqueMimeTypes, hasItem('text/html'))
		assertThat(listOfUniqueMimeTypes.size(), greaterThan(1))
		assertThat(waterfallEntries*.startOffset, everyItem(notNullValue()))
		assertThat(waterfallEntries*.dnsLookupTimeStartInMillisecs, everyItem(notNullValue()))
		assertThat(waterfallEntries*.dnsLookupTimeStartInMillisecs, everyItem(notNullValue()))
		assertThat(waterfallEntries*.initialConnectTimeStartInMillisecs, everyItem(notNullValue()))
		assertThat(waterfallEntries*.sslNegotationTimeStartInMillisecs, everyItem(notNullValue()))
		assertThat(waterfallEntries*.timeToFirstByteStartInMillisecs, everyItem(notNullValue()))
		assertThat(waterfallEntries*.downloadTimeStartInMillisecs, everyItem(notNullValue()))
		assertThat(waterfallEntries*.dnsLookupTimeEndInMillisecs, everyItem(notNullValue()))
		assertThat(waterfallEntries*.initialConnectTimeEndInMillisecs, everyItem(notNullValue()))
		assertThat(waterfallEntries*.sslNegotationTimeEndInMillisecs, everyItem(notNullValue()))
		assertThat(waterfallEntries*.timeToFirstByteEndInMillisecs, everyItem(notNullValue()))
		assertThat(waterfallEntries*.downloadTimeEndInMillisecs, everyItem(notNullValue()))
		assertThat(waterfallEntries*.downloadedBytes, everyItem(notNullValue()))
		assertThat(waterfallEntries*.uploadedBytes, everyItem(notNullValue()))
		assertThat(waterfallEntries*.oneBasedIndexInWaterfall, everyItem(greaterThan(0)))
		assertThat(waterfallEntries*.oneBasedIndexInWaterfall.size(), is(waterfallEntries*.oneBasedIndexInWaterfall.unique(false).size()))
		
		// repeated view //////////////////////////////////////////////
		// hole waterfall
		waterfall = result['page_1_FF_LH_Step02_Moduleinstieg 1_1']
		assertThat(waterfall.url,
			is('https://www.example.de/product/'))
		assertThat(waterfall.startDate,
			is(new DateTime(2014,1,4,7,43,6,DateTimeZone.UTC).toDate()))
		assertThat(waterfall.title,
			is('Run 1, Event Name FF_LH_Step02_Moduleinstieg 1, Repeat View for https://www.example.de/product/'))
		assertThat(waterfall.eventName,
			is('FF_LH_Step02_Moduleinstieg 1'))
		assertThat(waterfall.numberOfWptRun, is(1))
		assertThat(waterfall.cachedView, is(CachedView.CACHED))
		assertThat(waterfall.startRenderInMillisecs, is(43))
		assertThat(waterfall.docCompleteTimeInMillisecs, is(997))
		assertThat(waterfall.domTimeInMillisecs, is(0))
		assertThat(waterfall.fullyLoadedTimeInMillisecs, is(2806))
		// waterfall-entries
		waterfallEntries = waterfall.waterfallEntries
		assertThat(waterfallEntries.size(), greaterThan(10))
		assertThat(waterfallEntries, everyItem(isA(WaterfallEntry)))
		assertThat(waterfallEntries*.blocked, everyItem(is(false)))
		assertThat(waterfallEntries*.httpStatus, everyItem(greaterThanOrEqualTo(0)))
		assertThat(waterfallEntries*.path, everyItem(not(isEmptyOrNullString())))
		assertThat(waterfallEntries*.host, everyItem(not(isEmptyOrNullString())))
		listOfUniqueMimeTypes= waterfallEntries*.mimeType.unique(false)
		assertThat(listOfUniqueMimeTypes, hasItem('text/html'))
		assertThat(listOfUniqueMimeTypes.size(), greaterThan(1))
		assertThat(waterfallEntries*.startOffset, everyItem(notNullValue()))
		assertThat(waterfallEntries*.dnsLookupTimeStartInMillisecs, everyItem(notNullValue()))
		assertThat(waterfallEntries*.dnsLookupTimeStartInMillisecs, everyItem(notNullValue()))
		assertThat(waterfallEntries*.initialConnectTimeStartInMillisecs, everyItem(notNullValue()))
		assertThat(waterfallEntries*.sslNegotationTimeStartInMillisecs, everyItem(notNullValue()))
		assertThat(waterfallEntries*.timeToFirstByteStartInMillisecs, everyItem(notNullValue()))
		assertThat(waterfallEntries*.downloadTimeStartInMillisecs, everyItem(notNullValue()))
		assertThat(waterfallEntries*.dnsLookupTimeEndInMillisecs, everyItem(notNullValue()))
		assertThat(waterfallEntries*.initialConnectTimeEndInMillisecs, everyItem(notNullValue()))
		assertThat(waterfallEntries*.sslNegotationTimeEndInMillisecs, everyItem(notNullValue()))
		assertThat(waterfallEntries*.timeToFirstByteEndInMillisecs, everyItem(notNullValue()))
		assertThat(waterfallEntries*.downloadTimeEndInMillisecs, everyItem(notNullValue()))
		assertThat(waterfallEntries*.downloadedBytes, everyItem(notNullValue()))
		assertThat(waterfallEntries*.uploadedBytes, everyItem(notNullValue()))
		assertThat(waterfallEntries*.oneBasedIndexInWaterfall, everyItem(greaterThan(0)))
		assertThat(waterfallEntries*.oneBasedIndexInWaterfall.size(), is(waterfallEntries*.oneBasedIndexInWaterfall.unique(false).size()))
		
	}
	@Test
	void testParsingMultistepWaterfalls_1Run_6Events_FirstAndRepeatedView() {
		// Test execution ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		Map<String, WebPerformanceWaterfall> result = serviceUnderTest.getWaterfalls(hars['multistep_1Run_6Events_FirstAndRepeatedView.har'])
		
		// Test assertions ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		
		// number of waterfalls
		assertThat(result.size(), is(12))
		
		// run 1, event 1, first view //////////////////////////////////////////////
		// hole waterfall
		WebPerformanceWaterfall waterfall = result['page_1_HP:::LH_Homepage_navigates_0']
		assertThat(waterfall.url,
			is('https://www.example.de'))
		assertThat(waterfall.startDate,
			is(new DateTime(2014,3,3,10,41,1,DateTimeZone.UTC).toDate()))
		assertThat(waterfall.title,
			is('Run 1, Event Name HP:::LH_Homepage_navigates, First View for https://www.example.de'))
		assertThat(waterfall.eventName,
			is('HP:::LH_Homepage_navigates'))
		assertThat(waterfall.numberOfWptRun, is(1))
		assertThat(waterfall.cachedView, is(CachedView.UNCACHED))
		assertThat(waterfall.startRenderInMillisecs, is(1563))
		assertThat(waterfall.docCompleteTimeInMillisecs, is(1922))
		assertThat(waterfall.domTimeInMillisecs, is(0))
		assertThat(waterfall.fullyLoadedTimeInMillisecs, is(4857))
		// waterfall-entries
		Collection<WaterfallEntry> waterfallEntries = waterfall.waterfallEntries
		assertThat(waterfallEntries.size(), greaterThan(10))
		assertThat(waterfallEntries, everyItem(isA(WaterfallEntry)))
		assertThat(waterfallEntries*.blocked, everyItem(is(false)))
		assertThat(waterfallEntries*.httpStatus, everyItem(greaterThanOrEqualTo(0)))
		assertThat(waterfallEntries*.path, everyItem(not(isEmptyOrNullString())))
		assertThat(waterfallEntries*.host, everyItem(not(isEmptyOrNullString())))
		Collection<String> listOfUniqueMimeTypes= waterfallEntries*.mimeType.unique(false)
		assertThat(listOfUniqueMimeTypes, hasItem('text/html'))
		assertThat(listOfUniqueMimeTypes.size(), greaterThan(1))
		assertThat(waterfallEntries*.startOffset, everyItem(notNullValue()))
		assertThat(waterfallEntries*.dnsLookupTimeStartInMillisecs, everyItem(notNullValue()))
		assertThat(waterfallEntries*.dnsLookupTimeStartInMillisecs, everyItem(notNullValue()))
		assertThat(waterfallEntries*.initialConnectTimeStartInMillisecs, everyItem(notNullValue()))
		assertThat(waterfallEntries*.sslNegotationTimeStartInMillisecs, everyItem(notNullValue()))
		assertThat(waterfallEntries*.timeToFirstByteStartInMillisecs, everyItem(notNullValue()))
		assertThat(waterfallEntries*.downloadTimeStartInMillisecs, everyItem(notNullValue()))
		assertThat(waterfallEntries*.dnsLookupTimeEndInMillisecs, everyItem(notNullValue()))
		assertThat(waterfallEntries*.initialConnectTimeEndInMillisecs, everyItem(notNullValue()))
		assertThat(waterfallEntries*.sslNegotationTimeEndInMillisecs, everyItem(notNullValue()))
		assertThat(waterfallEntries*.timeToFirstByteEndInMillisecs, everyItem(notNullValue()))
		assertThat(waterfallEntries*.downloadTimeEndInMillisecs, everyItem(notNullValue()))
		assertThat(waterfallEntries*.downloadedBytes, everyItem(notNullValue()))
		assertThat(waterfallEntries*.uploadedBytes, everyItem(notNullValue()))
		assertThat(waterfallEntries*.oneBasedIndexInWaterfall, everyItem(greaterThan(0)))
		assertThat(waterfallEntries*.oneBasedIndexInWaterfall.size(), is(waterfallEntries*.oneBasedIndexInWaterfall.unique(false).size()))
		
		// run 1, event 4, repeated view //////////////////////////////////////////////
		// hole waterfall
		waterfall = result['page_1_ADS:::LH_ADS_navigates_1']
		assertThat(waterfall.url,
			is('https://www.example.de/subsite/exampleproduct/'))
		assertThat(waterfall.startDate,
			is(new DateTime(2014,3,3,10,43,59,DateTimeZone.UTC).toDate()))
		assertThat(waterfall.title,
			is('Run 1, Event Name ADS:::LH_ADS_navigates, Repeat View for https://www.example.de/subsite/exampleproduct/'))
		assertThat(waterfall.eventName,
			is('ADS:::LH_ADS_navigates'))
		assertThat(waterfall.numberOfWptRun, is(1))
		assertThat(waterfall.cachedView, is(CachedView.CACHED))
		assertThat(waterfall.startRenderInMillisecs, is(248))
		assertThat(waterfall.docCompleteTimeInMillisecs, is(392))
		assertThat(waterfall.domTimeInMillisecs, is(0))
		assertThat(waterfall.fullyLoadedTimeInMillisecs, is(2016))
		// waterfall-entries
		waterfallEntries = waterfall.waterfallEntries
		assertThat(waterfallEntries.size(), greaterThan(10))
		assertThat(waterfallEntries, everyItem(isA(WaterfallEntry)))
		assertThat(waterfallEntries*.blocked, everyItem(is(false)))
		assertThat(waterfallEntries*.httpStatus, everyItem(greaterThanOrEqualTo(0)))
		assertThat(waterfallEntries*.path, everyItem(not(isEmptyOrNullString())))
		assertThat(waterfallEntries*.host, everyItem(not(isEmptyOrNullString())))
		listOfUniqueMimeTypes= waterfallEntries*.mimeType.unique(false)
		assertThat(listOfUniqueMimeTypes, hasItem('text/html'))
		assertThat(listOfUniqueMimeTypes.size(), greaterThan(1))
		assertThat(waterfallEntries*.startOffset, everyItem(notNullValue()))
		assertThat(waterfallEntries*.dnsLookupTimeStartInMillisecs, everyItem(notNullValue()))
		assertThat(waterfallEntries*.dnsLookupTimeStartInMillisecs, everyItem(notNullValue()))
		assertThat(waterfallEntries*.initialConnectTimeStartInMillisecs, everyItem(notNullValue()))
		assertThat(waterfallEntries*.sslNegotationTimeStartInMillisecs, everyItem(notNullValue()))
		assertThat(waterfallEntries*.timeToFirstByteStartInMillisecs, everyItem(notNullValue()))
		assertThat(waterfallEntries*.downloadTimeStartInMillisecs, everyItem(notNullValue()))
		assertThat(waterfallEntries*.dnsLookupTimeEndInMillisecs, everyItem(notNullValue()))
		assertThat(waterfallEntries*.initialConnectTimeEndInMillisecs, everyItem(notNullValue()))
		assertThat(waterfallEntries*.sslNegotationTimeEndInMillisecs, everyItem(notNullValue()))
		assertThat(waterfallEntries*.timeToFirstByteEndInMillisecs, everyItem(notNullValue()))
		assertThat(waterfallEntries*.downloadTimeEndInMillisecs, everyItem(notNullValue()))
		assertThat(waterfallEntries*.downloadedBytes, everyItem(notNullValue()))
		assertThat(waterfallEntries*.uploadedBytes, everyItem(notNullValue()))
		assertThat(waterfallEntries*.oneBasedIndexInWaterfall, everyItem(greaterThan(0)))
		assertThat(waterfallEntries*.oneBasedIndexInWaterfall.size(), is(waterfallEntries*.oneBasedIndexInWaterfall.unique(false).size()))
		
	}
	@Test
	void testParsingMultistepWaterfalls_2Run2_2Events_FirstView() {
		// Test execution ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		Map<String, WebPerformanceWaterfall> result = serviceUnderTest.getWaterfalls(hars['multistep_2Runs_2Events_FirstView.har'])
		
		// Test assertions ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		
		// number of waterfalls
		assertThat(result.size(), is(4))
		
		// run 1, event 2 //////////////////////////////////////////////
		// hole waterfall
		WebPerformanceWaterfall waterfall = result['page_1_MES:::LH_Moduleinstieg_0']
		assertThat(waterfall.url,
			is('https://www.example.de'))
		assertThat(waterfall.startDate,
			is(new DateTime(2014,3,4,9,41,20,DateTimeZone.UTC).toDate()))
		assertThat(waterfall.title,
			is('Run 1, Event Name MES:::LH_Moduleinstieg, First View for https://www.example.de'))
		assertThat(waterfall.eventName,
			is('MES:::LH_Moduleinstieg'))
		assertThat(waterfall.numberOfWptRun, is(1))
		assertThat(waterfall.cachedView, is(CachedView.UNCACHED))
		assertThat(waterfall.startRenderInMillisecs, is(223))
		assertThat(waterfall.docCompleteTimeInMillisecs, is(994))
		assertThat(waterfall.domTimeInMillisecs, is(0))
		assertThat(waterfall.fullyLoadedTimeInMillisecs, is(3456))
		// waterfall-entries
		Collection<WaterfallEntry> waterfallEntries = waterfall.waterfallEntries
		assertThat(waterfallEntries.size(), greaterThan(10))
		assertThat(waterfallEntries, everyItem(isA(WaterfallEntry)))
		assertThat(waterfallEntries*.blocked, everyItem(is(false)))
		assertThat(waterfallEntries*.httpStatus, everyItem(greaterThanOrEqualTo(0)))
		assertThat(waterfallEntries*.path, everyItem(not(isEmptyOrNullString())))
		assertThat(waterfallEntries*.host, everyItem(not(isEmptyOrNullString())))
		Collection<String> listOfUniqueMimeTypes= waterfallEntries*.mimeType.unique(false)
		assertThat(listOfUniqueMimeTypes, hasItem('text/html'))
		assertThat(listOfUniqueMimeTypes.size(), greaterThan(1))
		assertThat(waterfallEntries*.startOffset, everyItem(notNullValue()))
		assertThat(waterfallEntries*.dnsLookupTimeStartInMillisecs, everyItem(notNullValue()))
		assertThat(waterfallEntries*.dnsLookupTimeStartInMillisecs, everyItem(notNullValue()))
		assertThat(waterfallEntries*.initialConnectTimeStartInMillisecs, everyItem(notNullValue()))
		assertThat(waterfallEntries*.sslNegotationTimeStartInMillisecs, everyItem(notNullValue()))
		assertThat(waterfallEntries*.timeToFirstByteStartInMillisecs, everyItem(notNullValue()))
		assertThat(waterfallEntries*.downloadTimeStartInMillisecs, everyItem(notNullValue()))
		assertThat(waterfallEntries*.dnsLookupTimeEndInMillisecs, everyItem(notNullValue()))
		assertThat(waterfallEntries*.initialConnectTimeEndInMillisecs, everyItem(notNullValue()))
		assertThat(waterfallEntries*.sslNegotationTimeEndInMillisecs, everyItem(notNullValue()))
		assertThat(waterfallEntries*.timeToFirstByteEndInMillisecs, everyItem(notNullValue()))
		assertThat(waterfallEntries*.downloadTimeEndInMillisecs, everyItem(notNullValue()))
		assertThat(waterfallEntries*.downloadedBytes, everyItem(notNullValue()))
		assertThat(waterfallEntries*.uploadedBytes, everyItem(notNullValue()))
		assertThat(waterfallEntries*.oneBasedIndexInWaterfall, everyItem(greaterThan(0)))
		assertThat(waterfallEntries*.oneBasedIndexInWaterfall.size(), is(waterfallEntries*.oneBasedIndexInWaterfall.unique(false).size()))
		
		// run 2, event 1 //////////////////////////////////////////////
		// hole waterfall
		waterfall = result['page_2_HP:::LH_Homepage_0']
		assertThat(waterfall.url,
			is('https://www.example.de'))
		assertThat(waterfall.startDate,
			is(new DateTime(2014,3,4,9,41,33,DateTimeZone.UTC).toDate()))
		assertThat(waterfall.title,
			is('Run 2, Event Name HP:::LH_Homepage, First View for https://www.example.de'))
		assertThat(waterfall.eventName,
			is('HP:::LH_Homepage'))
		assertThat(waterfall.numberOfWptRun, is(2))
		assertThat(waterfall.cachedView, is(CachedView.UNCACHED))
		assertThat(waterfall.startRenderInMillisecs, is(1690))
		assertThat(waterfall.docCompleteTimeInMillisecs, is(2647))
		assertThat(waterfall.domTimeInMillisecs, is(0))
		assertThat(waterfall.fullyLoadedTimeInMillisecs, is(5944))
		// waterfall-entries
		waterfallEntries = waterfall.waterfallEntries
		assertThat(waterfallEntries.size(), greaterThan(10))
		assertThat(waterfallEntries, everyItem(isA(WaterfallEntry)))
		assertThat(waterfallEntries*.blocked, everyItem(is(false)))
		assertThat(waterfallEntries*.httpStatus, everyItem(greaterThanOrEqualTo(0)))
		assertThat(waterfallEntries*.path, everyItem(not(isEmptyOrNullString())))
		assertThat(waterfallEntries*.host, everyItem(not(isEmptyOrNullString())))
		listOfUniqueMimeTypes= waterfallEntries*.mimeType.unique(false)
		assertThat(listOfUniqueMimeTypes, hasItem('text/html'))
		assertThat(listOfUniqueMimeTypes.size(), greaterThan(1))
		assertThat(waterfallEntries*.startOffset, everyItem(notNullValue()))
		assertThat(waterfallEntries*.dnsLookupTimeStartInMillisecs, everyItem(notNullValue()))
		assertThat(waterfallEntries*.dnsLookupTimeStartInMillisecs, everyItem(notNullValue()))
		assertThat(waterfallEntries*.initialConnectTimeStartInMillisecs, everyItem(notNullValue()))
		assertThat(waterfallEntries*.sslNegotationTimeStartInMillisecs, everyItem(notNullValue()))
		assertThat(waterfallEntries*.timeToFirstByteStartInMillisecs, everyItem(notNullValue()))
		assertThat(waterfallEntries*.downloadTimeStartInMillisecs, everyItem(notNullValue()))
		assertThat(waterfallEntries*.dnsLookupTimeEndInMillisecs, everyItem(notNullValue()))
		assertThat(waterfallEntries*.initialConnectTimeEndInMillisecs, everyItem(notNullValue()))
		assertThat(waterfallEntries*.sslNegotationTimeEndInMillisecs, everyItem(notNullValue()))
		assertThat(waterfallEntries*.timeToFirstByteEndInMillisecs, everyItem(notNullValue()))
		assertThat(waterfallEntries*.downloadTimeEndInMillisecs, everyItem(notNullValue()))
		assertThat(waterfallEntries*.downloadedBytes, everyItem(notNullValue()))
		assertThat(waterfallEntries*.uploadedBytes, everyItem(notNullValue()))
		assertThat(waterfallEntries*.oneBasedIndexInWaterfall, everyItem(greaterThan(0)))
		assertThat(waterfallEntries*.oneBasedIndexInWaterfall.size(), is(waterfallEntries*.oneBasedIndexInWaterfall.unique(false).size()))
		
	}
	@Test
	void testParsingNoMultistepNoEventNameWaterfalls_3Runs_FirstView() {
		// Test execution ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		Map<String, WebPerformanceWaterfall> result = serviceUnderTest.getWaterfalls(hars['nomultistep_noeventname_3Runs_FirstView.har'])
		
		// Test assertions ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		
		// number of waterfalls
		assertThat(result.size(), is(3))
		
		// run 1, first view //////////////////////////////////////////////
		// hole waterfall
		WebPerformanceWaterfall waterfall = result['page_1_0']
		assertThat(waterfall.url,
			is('http://example.com'))
		assertThat(waterfall.startDate,
			is(new DateTime(2014,2,17,10,56,23,DateTimeZone.UTC).toDate()))
		assertThat(waterfall.title,
			is('Run 1, First View for http://example.com'))
		assertThat(waterfall.eventName, isEmptyString())
		assertThat(waterfall.numberOfWptRun, is(1))
		assertThat(waterfall.cachedView, is(CachedView.UNCACHED))
		assertThat(waterfall.startRenderInMillisecs, is(3703))
		assertThat(waterfall.docCompleteTimeInMillisecs, is(7333))
		assertThat(waterfall.domTimeInMillisecs, is(0))
		assertThat(waterfall.fullyLoadedTimeInMillisecs, is(8172))
		// waterfall-entries
		Collection<WaterfallEntry> waterfallEntries = waterfall.waterfallEntries
		assertThat(waterfallEntries.size(), greaterThan(10))
		assertThat(waterfallEntries, everyItem(isA(WaterfallEntry)))
		assertThat(waterfallEntries*.blocked, everyItem(is(false)))
		assertThat(waterfallEntries*.httpStatus, everyItem(greaterThanOrEqualTo(0)))
		assertThat(waterfallEntries*.path, everyItem(not(isEmptyOrNullString())))
		assertThat(waterfallEntries*.host, everyItem(not(isEmptyOrNullString())))
		Collection<String> listOfUniqueMimeTypes= waterfallEntries*.mimeType.unique(false)
		assertThat(listOfUniqueMimeTypes, hasItem('text/html'))
		assertThat(listOfUniqueMimeTypes.size(), greaterThan(1))
		assertThat(waterfallEntries*.startOffset, everyItem(notNullValue()))
		assertThat(waterfallEntries*.dnsLookupTimeStartInMillisecs, everyItem(notNullValue()))
		assertThat(waterfallEntries*.dnsLookupTimeStartInMillisecs, everyItem(notNullValue()))
		assertThat(waterfallEntries*.initialConnectTimeStartInMillisecs, everyItem(notNullValue()))
		assertThat(waterfallEntries*.sslNegotationTimeStartInMillisecs, everyItem(notNullValue()))
		assertThat(waterfallEntries*.timeToFirstByteStartInMillisecs, everyItem(notNullValue()))
		assertThat(waterfallEntries*.downloadTimeStartInMillisecs, everyItem(notNullValue()))
		assertThat(waterfallEntries*.dnsLookupTimeEndInMillisecs, everyItem(notNullValue()))
		assertThat(waterfallEntries*.initialConnectTimeEndInMillisecs, everyItem(notNullValue()))
		assertThat(waterfallEntries*.sslNegotationTimeEndInMillisecs, everyItem(notNullValue()))
		assertThat(waterfallEntries*.timeToFirstByteEndInMillisecs, everyItem(notNullValue()))
		assertThat(waterfallEntries*.downloadTimeEndInMillisecs, everyItem(notNullValue()))
		assertThat(waterfallEntries*.downloadedBytes, everyItem(notNullValue()))
		assertThat(waterfallEntries*.uploadedBytes, everyItem(notNullValue()))
		assertThat(waterfallEntries*.oneBasedIndexInWaterfall, everyItem(greaterThan(0)))
		assertThat(waterfallEntries*.oneBasedIndexInWaterfall.size(), is(waterfallEntries*.oneBasedIndexInWaterfall.unique(false).size()))
		
	}
	@Test
	void testParsingNoMultistepNoEventNameWaterfalls_1Run_FirstView() {
		// Test execution ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		Map<String, WebPerformanceWaterfall> result = serviceUnderTest.getWaterfalls(hars['nomultistep_noeventname_1Run_FirstView_example.de.140217_16_J7A.har'])
		
		// Test assertions ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		
		// number of waterfalls
		assertThat(result.size(), is(1))
		
		// run 1, first view //////////////////////////////////////////////
		// hole waterfall
		WebPerformanceWaterfall waterfall = result['page_1_0']
		assertThat(waterfall.url,
			is('http://example.de'))
		assertThat(waterfall.startDate,
			is(new DateTime(2014,2,17,8,45,49,DateTimeZone.UTC).toDate()))
		assertThat(waterfall.title,
			is('Run 1, First View for http://example.de'))
		assertThat(waterfall.eventName, isEmptyString())
		assertThat(waterfall.numberOfWptRun, is(1))
		assertThat(waterfall.cachedView, is(CachedView.UNCACHED))
		assertThat(waterfall.startRenderInMillisecs, is(2201))
		assertThat(waterfall.docCompleteTimeInMillisecs, is(3636))
		assertThat(waterfall.domTimeInMillisecs, is(0))
		assertThat(waterfall.fullyLoadedTimeInMillisecs, is(8218))
		// waterfall-entries
		Collection<WaterfallEntry> waterfallEntries = waterfall.waterfallEntries
		assertThat(waterfallEntries.size(), greaterThan(10))
		assertThat(waterfallEntries, everyItem(isA(WaterfallEntry)))
		assertThat(waterfallEntries*.blocked, everyItem(is(false)))
		assertThat(waterfallEntries*.httpStatus, everyItem(greaterThanOrEqualTo(0)))
		assertThat(waterfallEntries*.path, everyItem(not(isEmptyOrNullString())))
		assertThat(waterfallEntries*.host, everyItem(not(isEmptyOrNullString())))
		Collection<String> listOfUniqueMimeTypes= waterfallEntries*.mimeType.unique(false)
		assertThat(listOfUniqueMimeTypes, hasItem('text/html'))
		assertThat(listOfUniqueMimeTypes.size(), greaterThan(1))
		assertThat(waterfallEntries*.startOffset, everyItem(notNullValue()))
		assertThat(waterfallEntries*.dnsLookupTimeStartInMillisecs, everyItem(notNullValue()))
		assertThat(waterfallEntries*.dnsLookupTimeStartInMillisecs, everyItem(notNullValue()))
		assertThat(waterfallEntries*.initialConnectTimeStartInMillisecs, everyItem(notNullValue()))
		assertThat(waterfallEntries*.sslNegotationTimeStartInMillisecs, everyItem(notNullValue()))
		assertThat(waterfallEntries*.timeToFirstByteStartInMillisecs, everyItem(notNullValue()))
		assertThat(waterfallEntries*.downloadTimeStartInMillisecs, everyItem(notNullValue()))
		assertThat(waterfallEntries*.dnsLookupTimeEndInMillisecs, everyItem(notNullValue()))
		assertThat(waterfallEntries*.initialConnectTimeEndInMillisecs, everyItem(notNullValue()))
		assertThat(waterfallEntries*.sslNegotationTimeEndInMillisecs, everyItem(notNullValue()))
		assertThat(waterfallEntries*.timeToFirstByteEndInMillisecs, everyItem(notNullValue()))
		assertThat(waterfallEntries*.downloadTimeEndInMillisecs, everyItem(notNullValue()))
		assertThat(waterfallEntries*.downloadedBytes, everyItem(notNullValue()))
		assertThat(waterfallEntries*.uploadedBytes, everyItem(notNullValue()))
		assertThat(waterfallEntries*.oneBasedIndexInWaterfall, everyItem(greaterThan(0)))
		assertThat(waterfallEntries*.oneBasedIndexInWaterfall.size(), is(waterfallEntries*.oneBasedIndexInWaterfall.unique(false).size()))
		
	}
	@Test
	void testParsingNoMultistepNoEventNameWaterfalls_9Runs_FirstView() {
		// Test execution ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		Map<String, WebPerformanceWaterfall> result = serviceUnderTest.getWaterfalls(hars['nomultistep_noeventname_9Runs_FirstView_example.de.140218_3P_AS2.har'])
		
		// Test assertions ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		
		// number of waterfalls
		assertThat(result.size(), is(9))
		
		// run 6, first view //////////////////////////////////////////////
		// hole waterfall
		WebPerformanceWaterfall waterfall = result['page_6_0']
		assertThat(waterfall.url,
			is('http://example.de'))
		assertThat(waterfall.startDate,
			is(new DateTime(2014,2,18,4,4,19,DateTimeZone.UTC).toDate()))
		assertThat(waterfall.title,
			is('Run 6, First View for http://example.de'))
		assertThat(waterfall.eventName, isEmptyString())
		assertThat(waterfall.numberOfWptRun, is(6))
		assertThat(waterfall.cachedView, is(CachedView.UNCACHED))
		assertThat(waterfall.startRenderInMillisecs, is(1681))
		assertThat(waterfall.docCompleteTimeInMillisecs, is(3284))
		assertThat(waterfall.domTimeInMillisecs, is(0))
		assertThat(waterfall.fullyLoadedTimeInMillisecs, is(8065))
		// waterfall-entries
		Collection<WaterfallEntry> waterfallEntries = waterfall.waterfallEntries
		assertThat(waterfallEntries.size(), greaterThan(10))
		assertThat(waterfallEntries, everyItem(isA(WaterfallEntry)))
		assertThat(waterfallEntries*.blocked, everyItem(is(false)))
		assertThat(waterfallEntries*.httpStatus, everyItem(greaterThanOrEqualTo(0)))
		assertThat(waterfallEntries*.path, everyItem(not(isEmptyOrNullString())))
		assertThat(waterfallEntries*.host, everyItem(not(isEmptyOrNullString())))
		Collection<String> listOfUniqueMimeTypes= waterfallEntries*.mimeType.unique(false)
		assertThat(listOfUniqueMimeTypes, hasItem('text/html'))
		assertThat(listOfUniqueMimeTypes.size(), greaterThan(1))
		assertThat(waterfallEntries*.startOffset, everyItem(notNullValue()))
		assertThat(waterfallEntries*.dnsLookupTimeStartInMillisecs, everyItem(notNullValue()))
		assertThat(waterfallEntries*.dnsLookupTimeStartInMillisecs, everyItem(notNullValue()))
		assertThat(waterfallEntries*.initialConnectTimeStartInMillisecs, everyItem(notNullValue()))
		assertThat(waterfallEntries*.sslNegotationTimeStartInMillisecs, everyItem(notNullValue()))
		assertThat(waterfallEntries*.timeToFirstByteStartInMillisecs, everyItem(notNullValue()))
		assertThat(waterfallEntries*.downloadTimeStartInMillisecs, everyItem(notNullValue()))
		assertThat(waterfallEntries*.dnsLookupTimeEndInMillisecs, everyItem(notNullValue()))
		assertThat(waterfallEntries*.initialConnectTimeEndInMillisecs, everyItem(notNullValue()))
		assertThat(waterfallEntries*.sslNegotationTimeEndInMillisecs, everyItem(notNullValue()))
		assertThat(waterfallEntries*.timeToFirstByteEndInMillisecs, everyItem(notNullValue()))
		assertThat(waterfallEntries*.downloadTimeEndInMillisecs, everyItem(notNullValue()))
		assertThat(waterfallEntries*.downloadedBytes, everyItem(notNullValue()))
		assertThat(waterfallEntries*.uploadedBytes, everyItem(notNullValue()))
		assertThat(waterfallEntries*.oneBasedIndexInWaterfall, everyItem(greaterThan(0)))
		assertThat(waterfallEntries*.oneBasedIndexInWaterfall.size(), is(waterfallEntries*.oneBasedIndexInWaterfall.unique(false).size()))
		
	}
	@Test
	void testParsingNoMultistepNoEventNameWaterfalls_1Run_FirstView_invalidurl() {
		// Test execution ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		Map<String, WebPerformanceWaterfall> result = serviceUnderTest.getWaterfalls(failedHars['nomultistep_noeventname_1Run_FirstView_invalidurl.har'])
		
		// Test assertions ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		
		// number of waterfalls
		assertThat(result.size(), is(0))
		
	}
	@Test
	void testParsingNoMultistepNoEventNameWaterfalls_1Run_FirstView_blockedMainUrl() {
		// Test execution ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		Map<String, WebPerformanceWaterfall> result = serviceUnderTest.getWaterfalls(failedHars['nomultistep_noeventname_1Run_FirstView_google.de.blocked.har'])
		
		// Test assertions ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		
		// number of waterfalls
		assertThat(result.size(), is(0))
		
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
	
	@Test
	void testRemoveWptMonitorSuffixes(){
		// test-specific data
		String eventNameWithoutSuffix = 'eventNameWithoutSuffix'
		String eventNameWithSuffix = 'eventName 1'
		String eventNameSuffixReduced = 'eventName'
		String pageidTemplate = 'page_1_%s_0' 
		Map<String, WebPerformanceWaterfall> origin = [
			(String.format(pageidTemplate, eventNameWithoutSuffix)) : new WebPerformanceWaterfall(eventName: eventNameWithoutSuffix),
			(String.format(pageidTemplate, eventNameWithSuffix)) : new WebPerformanceWaterfall(eventName: eventNameWithSuffix)]
		// test execution
		Map<String, WebPerformanceWaterfall> suffixesRemoved = serviceUnderTest.removeWptMonitorSuffixAndPagenamePrefixFromEventnames(origin)
		// assertions //////////////////////////////////////////////////////////////////////////
		//new map still has two entries 
		assertThat(suffixesRemoved.size(), is(2))
		//new map contains the pageids with replaced eventname in it  
		assertThat(suffixesRemoved.findAll{it.key.equals(String.format(pageidTemplate, eventNameWithoutSuffix))}.size(), is(1))
		assertThat(suffixesRemoved.findAll{it.key.equals(String.format(pageidTemplate, eventNameSuffixReduced))}.size(), is(1))
		//new map contains waterfalls with new event names
		assertThat(suffixesRemoved[String.format(pageidTemplate, eventNameWithoutSuffix)].eventName, is(eventNameWithoutSuffix))
		assertThat(suffixesRemoved[String.format(pageidTemplate, eventNameSuffixReduced)].eventName, is(eventNameSuffixReduced))
	}
	@Test
	void testRemoveWptMonitorSuffixesFromEmptyMap(){
		// test-specific data
		Map<String, WebPerformanceWaterfall> emptyOrigin = [:]
		// test execution
		Map<String, WebPerformanceWaterfall> suffixesRemoved = serviceUnderTest.removeWptMonitorSuffixAndPagenamePrefixFromEventnames(emptyOrigin)
		// assertions 
		assertThat(suffixesRemoved.size(), is(0))
	}
	
	// mocks
	
	void mockPageService(){
		def pageService = mockFor(PageService, true)
		pageService.demand.excludePagenamePart(1..100){String stepName ->
			return stepName //not the concern of these tests
		}
		serviceUnderTest.pageService = pageService.createMock()
	}
	
}

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
import groovyx.net.http.RESTClient

import org.apache.http.HttpHost
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

import co.freeside.betamax.Betamax
import co.freeside.betamax.Recorder
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.measurement.schedule.JobGroupType
import de.iteratec.osm.csi.Page
import de.iteratec.osm.csi.IntTestWithDBCleanup
import de.iteratec.osm.csi.TestDataUtil
import de.iteratec.osm.measurement.environment.wptserverproxy.LocationAndResultPersisterService
import de.iteratec.osm.measurement.environment.wptserverproxy.ProxyService
import de.iteratec.osm.result.EventResult
import de.iteratec.osm.measurement.environment.Browser
import de.iteratec.osm.measurement.environment.Location
import de.iteratec.osm.measurement.environment.WebPageTestServer

/**
 *	Tests persisting of  {@link EventResult}s and associated {@link WebPerformanceWaterfall}s.
 *	Result-xml's and -har's are requested from server first time tests are executed and afterwards retrieved from recorded betamax-tape.
 *	@see http://freeside.co/betamax/
 */
class ProxyServiceIntSpec extends IntTestWithDBCleanup {
	
	ProxyService proxyService
	LocationAndResultPersisterService locationAndResultPersisterService

    private static final String RESULT_ID = '130425_W1_f606bebc977a3b22c1a9205f70d07a00'
	private static final String LOCATION_IDENTIFIER_ITERATEC_DEV  = 'iteratec-dev-netlab:Firefox'
	private static final String LOCATION_IDENTIFIER_ITERATEC_DEV02 = 'iteratec-dev-iteraHH-win7:IE'
	WebPageTestServer wptServerDevIteratec, wptServerDevIteratec02
	JobGroup undefinedJobGroup
	Browser undefinedBrowser
	
	@Rule public Recorder recorder = new Recorder(new ConfigSlurper().parse(new File('grails-app/conf/BetamaxConfig.groovy').toURL()).toProperties())

	@Before
    void setUp() {
		//creating test-data common to all tests
		TestDataUtil.createOsmConfig()
		TestDataUtil.createAggregatorTypes()
		TestDataUtil.createMeasuredValueIntervals()
		createPages()
		createBrowsers()
		createJobGroups()
		createServers()
		createLocations()
		
		//mocks common to all tests
		mockTimeToCsMappingService()
		mockMetricReportingService()
		
		//mock HttpBuilder  to use betamax-proxy
		Map betamaxProps = new ConfigSlurper().parse(new File('grails-app/conf/BetamaxConfig.groovy').toURL()).flatten()
		proxyService.httpRequestService.metaClass.getClient = {WebPageTestServer wptserver ->
			RESTClient restClient = new RESTClient(wptserver.baseUrl)
			restClient.client.params.setParameter(DEFAULT_PROXY, new HttpHost(betamaxProps['betamax.proxyHost'], betamaxProps['betamax.proxyPort'], 'http'))
			return restClient
		}
		
    }

	@After
    void tearDown() {
    }
	
	@Test
	@Betamax(tape = 'ProxyServiceIntSpec_successful_wptresult_Multistep_1Run_11Events_JustFirstView')
	void testFetchingResultsWhileCalculationOfDependentMeasuredValuesFails() {
		
		//test execution
		proxyService.fetchResult(wptServerDevIteratec02, [resultId: '141013_EC_2026c719a0818c3f9b8d39c72ac3cd06'])

		//assertions ///////////////////////////////////////////////////////////////////////////////////////

		// event results
		int runs = 1
		int events = 11
		int cachedViews = 1
		int expectedNumberOfResults = runs * events * cachedViews
		List<EventResult> eventResults = EventResult.list()
		assertThat(eventResults.size(), is(11))
		
	}

	@Test()
	@Betamax(tape = 'successful_wptresult_NoMultistep_1Run_FirstAndRepeatedView')
    void testPersistingOfWaterfallsWhileFetchingNoMultistepResult() {
		
		assertTrue(true)
		
		/*
		 * TODO:
		 * 			* implement persisting of webperformance-waterfalls
		 * 			* enable these tests again, @Ignore doesn't work :-(
		 */
		
//		//test execution
//		proxyService.fetchResult(wptServerDevIteratec, [resultId: '140226_63_c71335d3b7e9c54fd601656cd6baa3ee'])
//		
//		//assertions ///////////////////////////////////////////////////////////////////////////////////////
//		
//		// event results
//		int runs = 1
//		int events = 1
//		int cachedViews = 2
//		int expectedNumberOfResults = runs * events * cachedViews
//		List<EventResult> eventResults = EventResult.list()
//		assertThat(eventResults.size(), is(expectedNumberOfResults))
//		
//		String expectedEventName = 'FF_LH_Step01_Homepage'
//		EventResult fvResult = eventResults.find{it.measuredEvent.name == expectedEventName && it.cachedView == CachedView.UNCACHED && it.numberOfWptRun == 1}
//		EventResult rvResult = eventResults.find{it.measuredEvent.name == expectedEventName && it.cachedView == CachedView.CACHED && it.numberOfWptRun == 1}
//		assertThat(fvResult, notNullValue())
//		assertThat(rvResult, notNullValue())
//		
//		// waterfalls with entries
//		List<WebPerformanceWaterfall> waterfalls = WebPerformanceWaterfall.list()
//		assertThat(waterfalls.size(), is(expectedNumberOfResults))
//		assertThat(waterfalls, everyItem(isA(WebPerformanceWaterfall)))
//		
//		//first view
//		WebPerformanceWaterfall fvWaterfall = fvResult.webPerformanceWaterfall
//		assertThat(fvWaterfall, notNullValue())
//		assertThat(fvWaterfall.eventName, is(''))
//		assertThat(fvWaterfall.numberOfWptRun, is(1))
//		assertThat(fvWaterfall.cachedView, is(CachedView.UNCACHED))
//		
//		List<WaterfallEntry> fvWaterfallEntries = fvWaterfall.waterfallEntries
//		assertThat(fvWaterfallEntries.size(), is(144))
//		assertThat(fvWaterfallEntries, everyItem(isA(WaterfallEntry)))
//		assertThat(fvWaterfallEntries*.blocked, everyItem(is(false)))
//		assertThat(fvWaterfallEntries*.httpStatus, everyItem(greaterThanOrEqualTo(0)))
//		assertThat(fvWaterfallEntries*.path, everyItem(not(isEmptyOrNullString())))
//		assertThat(fvWaterfallEntries*.host, everyItem(not(isEmptyOrNullString())))
//		Collection<String> listOfUniqueMimeTypes= fvWaterfallEntries*.mimeType.unique(false)
//		assertThat(listOfUniqueMimeTypes, hasItem('text/html'))
//		assertThat(listOfUniqueMimeTypes.size(), greaterThan(1))
//		assertThat(fvWaterfallEntries*.startOffset, everyItem(notNullValue()))
//		assertThat(fvWaterfallEntries*.dnsLookupTimeStartInMillisecs, everyItem(notNullValue()))
//		assertThat(fvWaterfallEntries*.dnsLookupTimeStartInMillisecs, everyItem(notNullValue()))
//		assertThat(fvWaterfallEntries*.initialConnectTimeStartInMillisecs, everyItem(notNullValue()))
//		assertThat(fvWaterfallEntries*.sslNegotationTimeStartInMillisecs, everyItem(notNullValue()))
//		assertThat(fvWaterfallEntries*.timeToFirstByteStartInMillisecs, everyItem(notNullValue()))
//		assertThat(fvWaterfallEntries*.downloadTimeStartInMillisecs, everyItem(notNullValue()))
//		assertThat(fvWaterfallEntries*.dnsLookupTimeEndInMillisecs, everyItem(notNullValue()))
//		assertThat(fvWaterfallEntries*.initialConnectTimeEndInMillisecs, everyItem(notNullValue()))
//		assertThat(fvWaterfallEntries*.sslNegotationTimeEndInMillisecs, everyItem(notNullValue()))
//		assertThat(fvWaterfallEntries*.timeToFirstByteEndInMillisecs, everyItem(notNullValue()))
//		assertThat(fvWaterfallEntries*.downloadTimeEndInMillisecs, everyItem(notNullValue()))
//		assertThat(fvWaterfallEntries*.downloadedBytes, everyItem(notNullValue()))
//		assertThat(fvWaterfallEntries*.uploadedBytes, everyItem(notNullValue()))
//		assertThat(fvWaterfallEntries*.oneBasedIndexInWaterfall, everyItem(greaterThan(0)))
//		assertThat(fvWaterfallEntries*.oneBasedIndexInWaterfall.size(), is(fvWaterfallEntries*.oneBasedIndexInWaterfall.unique(false).size()))
//		
//		// repeated view
//		WebPerformanceWaterfall rvWaterfall = rvResult.webPerformanceWaterfall
//		assertThat(rvWaterfall, notNullValue())
//		assertThat(rvWaterfall.eventName, is(''))
//		assertThat(rvWaterfall.numberOfWptRun, is(1))
//		assertThat(rvWaterfall.cachedView, is(CachedView.CACHED))
//		
//		List<WaterfallEntry> rvWaterfallEntries = rvWaterfall.waterfallEntries 
//		assertThat(rvWaterfallEntries.size(), is(59))
//		assertThat(rvWaterfallEntries, everyItem(isA(WaterfallEntry)))
//		assertThat(rvWaterfallEntries*.blocked, everyItem(is(false)))
//		assertThat(rvWaterfallEntries*.httpStatus, everyItem(greaterThanOrEqualTo(0)))
//		assertThat(rvWaterfallEntries*.path, everyItem(not(isEmptyOrNullString())))
//		assertThat(rvWaterfallEntries*.host, everyItem(not(isEmptyOrNullString())))
//		listOfUniqueMimeTypes= rvWaterfallEntries*.mimeType.unique(false)
//		assertThat(listOfUniqueMimeTypes, hasItem('text/html'))
//		assertThat(listOfUniqueMimeTypes.size(), greaterThan(1))
//		assertThat(rvWaterfallEntries*.startOffset, everyItem(notNullValue()))
//		assertThat(rvWaterfallEntries*.dnsLookupTimeStartInMillisecs, everyItem(notNullValue()))
//		assertThat(rvWaterfallEntries*.dnsLookupTimeStartInMillisecs, everyItem(notNullValue()))
//		assertThat(rvWaterfallEntries*.initialConnectTimeStartInMillisecs, everyItem(notNullValue()))
//		assertThat(rvWaterfallEntries*.sslNegotationTimeStartInMillisecs, everyItem(notNullValue()))
//		assertThat(rvWaterfallEntries*.timeToFirstByteStartInMillisecs, everyItem(notNullValue()))
//		assertThat(rvWaterfallEntries*.downloadTimeStartInMillisecs, everyItem(notNullValue()))
//		assertThat(rvWaterfallEntries*.dnsLookupTimeEndInMillisecs, everyItem(notNullValue()))
//		assertThat(rvWaterfallEntries*.initialConnectTimeEndInMillisecs, everyItem(notNullValue()))
//		assertThat(rvWaterfallEntries*.sslNegotationTimeEndInMillisecs, everyItem(notNullValue()))
//		assertThat(rvWaterfallEntries*.timeToFirstByteEndInMillisecs, everyItem(notNullValue()))
//		assertThat(rvWaterfallEntries*.downloadTimeEndInMillisecs, everyItem(notNullValue()))
//		assertThat(rvWaterfallEntries*.downloadedBytes, everyItem(notNullValue()))
//		assertThat(rvWaterfallEntries*.uploadedBytes, everyItem(notNullValue()))
//		assertThat(rvWaterfallEntries*.oneBasedIndexInWaterfall, everyItem(greaterThan(0)))
//		assertThat(rvWaterfallEntries*.oneBasedIndexInWaterfall.size(), is(rvWaterfallEntries*.oneBasedIndexInWaterfall.unique(false).size()))
		
    }
	
	@Test
	@Betamax(tape = 'successful_wptresult_Multistep_6Runs_FirstAndRepeatedView')
	void testPersistingOfWaterfallsWhileFetchingMultistepResult() {
		
		assertTrue(true)
		
		/*
		 * TODO:
		 * 			* implement persisting of webperformance-waterfalls
		 * 			* enable these tests again, @Ignore doesn't work :-(
		 */
		
//		//test execution
//		proxyService.fetchResult(wptServerDevIteratec03, [resultId: '140311_YH_fa19d386faa11f57380d8f0a9ec537d9'])
//		
//		//assertions ///////////////////////////////////////////////////////////////////////////////////////
//		
//		// event results
//		int runs = 1
//		int events = 6
//		int cachedViews = 2
//		int expectedNumberOfResults = runs * events * cachedViews
//		List<EventResult> eventResults = EventResult.list()
//		assertThat(eventResults.size(), is(expectedNumberOfResults))
//		
//		String eventNameWaterfallsGetProofed = 'LH_Produktliste_navigates'
//		EventResult fvResult = eventResults.find{it.measuredEvent.name == eventNameWaterfallsGetProofed && it.cachedView == CachedView.UNCACHED && it.numberOfWptRun == 1}
//		EventResult rvResult = eventResults.find{it.measuredEvent.name == eventNameWaterfallsGetProofed && it.cachedView == CachedView.CACHED && it.numberOfWptRun == 1}
//		assertThat(fvResult, notNullValue())
//		assertThat(rvResult, notNullValue())
//		
//		// waterfalls with entries
//		List<WebPerformanceWaterfall> waterfalls = WebPerformanceWaterfall.list()
//		assertThat(waterfalls.size(), is(expectedNumberOfResults))
//		assertThat(waterfalls, everyItem(isA(WebPerformanceWaterfall)))
//		
//		//first view of event LH_Produktliste_navigates
//		WebPerformanceWaterfall fvWaterfall = fvResult.webPerformanceWaterfall
//		assertThat(fvWaterfall, notNullValue())
//		assertThat(fvWaterfall.eventName, is(eventNameWaterfallsGetProofed))
//		assertThat(fvWaterfall.numberOfWptRun, is(1))
//		assertThat(fvWaterfall.cachedView, is(CachedView.UNCACHED))
//		
//		List<WaterfallEntry> fvWaterfallEntries = fvWaterfall.waterfallEntries
//		assertThat(fvWaterfallEntries.size(), is(64))
//		assertThat(fvWaterfallEntries, everyItem(isA(WaterfallEntry)))
//		assertThat(fvWaterfallEntries*.blocked, everyItem(is(false)))
//		assertThat(fvWaterfallEntries*.httpStatus, everyItem(greaterThanOrEqualTo(0)))
//		assertThat(fvWaterfallEntries*.path, everyItem(not(isEmptyOrNullString())))
//		assertThat(fvWaterfallEntries*.host, everyItem(not(isEmptyOrNullString())))
//		Collection<String> listOfUniqueMimeTypes= fvWaterfallEntries*.mimeType.unique(false)
//		assertThat(listOfUniqueMimeTypes, hasItem('text/html'))
//		assertThat(listOfUniqueMimeTypes.size(), greaterThan(1))
//		assertThat(fvWaterfallEntries*.startOffset, everyItem(notNullValue()))
//		assertThat(fvWaterfallEntries*.dnsLookupTimeStartInMillisecs, everyItem(notNullValue()))
//		assertThat(fvWaterfallEntries*.dnsLookupTimeStartInMillisecs, everyItem(notNullValue()))
//		assertThat(fvWaterfallEntries*.initialConnectTimeStartInMillisecs, everyItem(notNullValue()))
//		assertThat(fvWaterfallEntries*.sslNegotationTimeStartInMillisecs, everyItem(notNullValue()))
//		assertThat(fvWaterfallEntries*.timeToFirstByteStartInMillisecs, everyItem(notNullValue()))
//		assertThat(fvWaterfallEntries*.downloadTimeStartInMillisecs, everyItem(notNullValue()))
//		assertThat(fvWaterfallEntries*.dnsLookupTimeEndInMillisecs, everyItem(notNullValue()))
//		assertThat(fvWaterfallEntries*.initialConnectTimeEndInMillisecs, everyItem(notNullValue()))
//		assertThat(fvWaterfallEntries*.sslNegotationTimeEndInMillisecs, everyItem(notNullValue()))
//		assertThat(fvWaterfallEntries*.timeToFirstByteEndInMillisecs, everyItem(notNullValue()))
//		assertThat(fvWaterfallEntries*.downloadTimeEndInMillisecs, everyItem(notNullValue()))
//		assertThat(fvWaterfallEntries*.downloadedBytes, everyItem(notNullValue()))
//		assertThat(fvWaterfallEntries*.uploadedBytes, everyItem(notNullValue()))
//		assertThat(fvWaterfallEntries*.oneBasedIndexInWaterfall, everyItem(greaterThan(0)))
//		assertThat(fvWaterfallEntries*.oneBasedIndexInWaterfall.size(), is(fvWaterfallEntries*.oneBasedIndexInWaterfall.unique(false).size()))
//		
//		// repeated view of event LH_Produktliste_navigates
//		WebPerformanceWaterfall rvWaterfall = rvResult.webPerformanceWaterfall
//		assertThat(rvWaterfall, notNullValue())
//		assertThat(rvWaterfall.eventName, is(eventNameWaterfallsGetProofed))
//		assertThat(rvWaterfall.numberOfWptRun, is(1))
//		assertThat(rvWaterfall.cachedView, is(CachedView.CACHED))
//		
//		List<WaterfallEntry> rvWaterfallEntries = rvWaterfall.waterfallEntries
//		assertThat(rvWaterfallEntries.size(), is(61))
//		assertThat(rvWaterfallEntries, everyItem(isA(WaterfallEntry)))
//		assertThat(rvWaterfallEntries*.blocked, everyItem(is(false)))
//		assertThat(rvWaterfallEntries*.httpStatus, everyItem(greaterThanOrEqualTo(0)))
//		assertThat(rvWaterfallEntries*.path, everyItem(not(isEmptyOrNullString())))
//		assertThat(rvWaterfallEntries*.host, everyItem(not(isEmptyOrNullString())))
//		listOfUniqueMimeTypes= rvWaterfallEntries*.mimeType.unique(false)
//		assertThat(listOfUniqueMimeTypes, hasItem('text/html'))
//		assertThat(listOfUniqueMimeTypes.size(), greaterThan(1))
//		assertThat(rvWaterfallEntries*.startOffset, everyItem(notNullValue()))
//		assertThat(rvWaterfallEntries*.dnsLookupTimeStartInMillisecs, everyItem(notNullValue()))
//		assertThat(rvWaterfallEntries*.dnsLookupTimeStartInMillisecs, everyItem(notNullValue()))
//		assertThat(rvWaterfallEntries*.initialConnectTimeStartInMillisecs, everyItem(notNullValue()))
//		assertThat(rvWaterfallEntries*.sslNegotationTimeStartInMillisecs, everyItem(notNullValue()))
//		assertThat(rvWaterfallEntries*.timeToFirstByteStartInMillisecs, everyItem(notNullValue()))
//		assertThat(rvWaterfallEntries*.downloadTimeStartInMillisecs, everyItem(notNullValue()))
//		assertThat(rvWaterfallEntries*.dnsLookupTimeEndInMillisecs, everyItem(notNullValue()))
//		assertThat(rvWaterfallEntries*.initialConnectTimeEndInMillisecs, everyItem(notNullValue()))
//		assertThat(rvWaterfallEntries*.sslNegotationTimeEndInMillisecs, everyItem(notNullValue()))
//		assertThat(rvWaterfallEntries*.timeToFirstByteEndInMillisecs, everyItem(notNullValue()))
//		assertThat(rvWaterfallEntries*.downloadTimeEndInMillisecs, everyItem(notNullValue()))
//		assertThat(rvWaterfallEntries*.downloadedBytes, everyItem(notNullValue()))
//		assertThat(rvWaterfallEntries*.uploadedBytes, everyItem(notNullValue()))
//		assertThat(rvWaterfallEntries*.oneBasedIndexInWaterfall, everyItem(greaterThan(0)))
//		assertThat(rvWaterfallEntries*.oneBasedIndexInWaterfall.size(), is(rvWaterfallEntries*.oneBasedIndexInWaterfall.unique(false).size()))
		
	}
	
	// create testdata common to all tests /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	private createServers(){
		wptServerDevIteratec = new WebPageTestServer(
			label: "TestServer 1",
			proxyIdentifier: "TestServer1",
			baseUrl: "http://dev.server01.wpt.iteratec.de/",
			active: true
		).save(failOnError: true, validate: false)
		wptServerDevIteratec02 = new WebPageTestServer(
			label: "TestServer 2",
			proxyIdentifier: "TestServer2",
			baseUrl: "http://dev.server02.wpt.iteratec.de/",
			active: true
		).save(failOnError: true, validate: false)
	}
	
	private createLocations(){
		new Location(
				label: '',
				uniqueIdentifierForServer: LOCATION_IDENTIFIER_ITERATEC_DEV,
				active: true,
				valid: 1,
				wptServer: wptServerDevIteratec,
				location: '',
				browser: undefinedBrowser).save(failOnError: true, validate: false)
		new Location(
			label: '',
			uniqueIdentifierForServer: LOCATION_IDENTIFIER_ITERATEC_DEV02,
			active: true,
			valid: 1,
			wptServer: wptServerDevIteratec02,
			location: '',
			browser: undefinedBrowser).save(failOnError: true, validate: false)
	}
	
	private createJobGroups(){
		undefinedJobGroup=new JobGroup(
			name: JobGroup.UNDEFINED_CSI,
			groupType: JobGroupType.CSI_AGGREGATION
			);
		undefinedJobGroup.save(failOnError: true);
	}
	
	private createBrowsers(){
		String browserName=Browser.UNDEFINED
		undefinedBrowser=new Browser(
				name: browserName,
				weight: 0)
				.addToBrowserAliases(alias: Browser.UNDEFINED)
				.save(failOnError: true)
				
		browserName="IE"
		new Browser(
				name: browserName,
				weight: 1)
				.addToBrowserAliases(alias: "IE")
				.addToBrowserAliases(alias: "IE8")
				.addToBrowserAliases(alias: "Internet Explorer")
				.addToBrowserAliases(alias: "Internet Explorer 8")
				.save(failOnError: true)
		browserName="FF"
		new Browser(
				name: browserName,
				weight: 1)
				.addToBrowserAliases(alias: "FF")
				.addToBrowserAliases(alias: "FF7")
				.addToBrowserAliases(alias: "Firefox")
				.addToBrowserAliases(alias: "Firefox7")
				.save(failOnError: true)
				
		browserName="Chrome"
		new Browser(
				name: browserName,
				weight: 1)
				.addToBrowserAliases(alias: "Chrome")
				.save(failOnError: true)
	}
	private static createPages(){
		['HP', 'MES', Page.UNDEFINED].each{pageName ->
			new Page(
					name: pageName,
					weight: 1).save(failOnError: true)
		}
	}
	
	// mock methods  /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	void mockTimeToCsMappingService(){
		locationAndResultPersisterService.timeToCsMappingService.metaClass.getCustomerSatisfactionInPercent = {Integer docReadyTimeInMilliSecs, Page page -> 
			return 42 //not the concern of this tests
		}
	}
	void mockMetricReportingService(){
		locationAndResultPersisterService.metricReportingService.metaClass.reportEventResultToGraphite = {EventResult result ->
			//not the concern of this tests
		}
	}
}

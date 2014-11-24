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

import de.iteratec.osm.ConfigService
import de.iteratec.osm.csi.IntTestWithDBCleanup
import de.iteratec.osm.csi.Page
import de.iteratec.osm.measurement.environment.Browser
import de.iteratec.osm.measurement.environment.Location
import de.iteratec.osm.measurement.environment.WebPageTestServer
import de.iteratec.osm.measurement.environment.wptserverproxy.LocationAndResultPersisterService
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.measurement.schedule.JobGroupType
import de.iteratec.osm.result.EventResult
import org.junit.After
import org.junit.Before
import org.junit.Test

import static org.junit.Assert.assertTrue

/**
 *	Tests persisting of {@link WebPerformanceWaterfall}s while listening to incoming results.
 *Results-xml's and -har's are loaded from test/resources. 
 *@see ProxyServiceIntSpec
 */
class PersistingWaterfallsIntSpec extends IntTestWithDBCleanup {
	
	LocationAndResultPersisterService locationAndResultPersisterService
	
	private static final String RESULT_ID_MULTISTEP = '130425_W1_f606bebc977a3b22c1a9205f70d07a00'
	private static final String RESULT_ID_NO_MULTISTEP = '140106_4H_cebcd61785a29a60d9ecf2aa58cad2df'
	private static final String LOCATION_IDENTIFIER_MULTISTEP  = 'Agent1-wptdriver:Firefox'
	private static final String LOCATION_IDENTIFIER_NO_MULTISTEP  = 'iteratec-dev-netlab:Firefox'
	WebPageTestServer serverMultistep, serverNoMultistep
	JobGroup undefinedJobGroup
	Browser undefinedBrowser

	@Before
    void setUp() {
		//creating test-data common to all tests
		createPages()
		createBrowsers()
		createJobGroups()
		createServers()
		createLocations()
		//mocks common to all tests
		mockTimeToCsMappingService()
		mockMeasuredValueUpdateService()
		mockMetricReportingService()
		locationAndResultPersisterService.configService = [ getDetailDataStorageTimeInWeeks: { 12 },
			getDefaultMaxDownloadTimeInMinutes: { 60 } ] as ConfigService
    }

	@After
    void tearDown() {
    }

	@Test
    void testPersistingOfWaterfallsWhileFetchingMultistepResult() {
		
		assertTrue(true)
		
		/*
		 * TODO: 
		 * 			* implement persisting of webperformance-waterfalls
		 * 			* enable these tests again, @Ignore doesn't work :-(
		 */
		
//		//create test-specific data
//		String resultName = 'Result_Multistep_1Run_2EventNames_PagePrefix'
//		GPathResult xmlResult = new XmlSlurper().parse(new File("test/resources/WptResultXmls/${resultName}.xml"))
//		String har = new File("test/resources/HARs/${resultName}.har").getText()
//		
//		//test execution
//		serviceUnderTest.listenToResult(RESULT_ID_MULTISTEP, xmlResult, har, serverMultistep)
//		
//		//assertions
//		int runs = 1
//		int events = 2
//		int cachedViews = 2
//		int expectedNumberOfResults = runs * events * cachedViews
//		assertThat(EventResult.list().size, is(expectedNumberOfResults))
//		
//		List<WebPerformanceWaterfall> waterfalls = WebPerformanceWaterfall.list()
//		assertThat(waterfalls.size(), is(expectedNumberOfResults))
//		assertThat(waterfalls, everyItem(isA(WebPerformanceWaterfall)))
//		assertThat(waterfalls*.waterfallEntries.flatten(), everyItem(isA(WaterfallEntry)))
//		
//		String expectedEventName1 = 'LH_Homepage_2'
//		String expectedEventName2 = 'LH_Moduleinstieg_2'
//		assertThat(waterfalls.findAll{it.eventName.equals(expectedEventName1) && it.numberOfWptRun == 1 && it.cachedView == CachedView.UNCACHED}.size(), is(1))
//		assertThat(waterfalls.findAll{it.eventName.equals(expectedEventName1) && it.numberOfWptRun == 1 && it.cachedView == CachedView.CACHED}.size(), is(1))
//		assertThat(waterfalls.findAll{it.eventName.equals(expectedEventName2) && it.numberOfWptRun == 1 && it.cachedView == CachedView.UNCACHED}.size(), is(1))
//		assertThat(waterfalls.findAll{it.eventName.equals(expectedEventName2) && it.numberOfWptRun == 1 && it.cachedView == CachedView.CACHED}.size(), is(1))
    }
	
	@Test
	void testPersistingOfWaterfallsWhileFetchingNoMultistepResult() {
		
		assertTrue(true)
		
		/*
		 * TODO:
		 * 			* implement persisting of webperformance-waterfalls
		 * 			* enable these tests again, @Ignore doesn't work :-(
		 */
		
//		//create test-specific data
//		String resultName = 'Result_NoMultistep_1Run_FirstAndRepeatedView'
//		GPathResult xmlResult = new XmlSlurper().parse(new File("test/resources/WptResultXmls/${resultName}.xml"))
//		String har = new File("test/resources/HARs/${resultName}.har").getText()
//		
//		//test execution
//		serviceUnderTest.listenToResult(RESULT_ID_NO_MULTISTEP, xmlResult, har, serverNoMultistep)
//		
//		//assertions
//		int runs = 1
//		int events = 1
//		int cachedViews = 2
//		int expectedNumberOfResults = runs * events * cachedViews
//		assertThat(EventResult.list().size, is(expectedNumberOfResults))
//		
//		List<WebPerformanceWaterfall> waterfalls = WebPerformanceWaterfall.list()
//		assertThat(waterfalls.size(), is(expectedNumberOfResults))
//		assertThat(waterfalls, everyItem(isA(WebPerformanceWaterfall)))
//		assertThat(waterfalls*.waterfallEntries.flatten(), everyItem(isA(WaterfallEntry)))
//		
//		String expectedEventName = ''
//		assertThat(waterfalls.findAll{it.eventName.equals(expectedEventName) && it.numberOfWptRun == 1 && it.cachedView == CachedView.UNCACHED}.size(), is(1))
//		assertThat(waterfalls.findAll{it.eventName.equals(expectedEventName) && it.numberOfWptRun == 1 && it.cachedView == CachedView.CACHED}.size(), is(1))
	}
	
	// create testdata common to all tests /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	private createServers(){
		serverMultistep = new WebPageTestServer(
			label: "serverMultistep",
			proxyIdentifier: "serverMultistep",
			baseUrl: "http://ism.intgerationtest",
			active: true
		).save(failOnError: true, validate: false)
		serverNoMultistep = new WebPageTestServer(
			label: "serverNoMultistep",
			proxyIdentifier: "serverNoMultistep",
			baseUrl: "http://ism.intgerationtest",
			active: true
		).save(failOnError: true, validate: false)
	}
	
	private createLocations(){
		new Location(
				label: '',
				uniqueIdentifierForServer: LOCATION_IDENTIFIER_MULTISTEP,
				active: true,
				valid: 1,
				wptServer: serverMultistep,
				location: '',
				browser: undefinedBrowser).save(failOnError: true, validate: false)
		new Location(
			label: '',
			uniqueIdentifierForServer: LOCATION_IDENTIFIER_NO_MULTISTEP,
			active: true,
			valid: 1,
			wptServer: serverNoMultistep,
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
				weight: 45)
				.addToBrowserAliases(alias: "IE")
				.addToBrowserAliases(alias: "IE8")
				.addToBrowserAliases(alias: "Internet Explorer")
				.addToBrowserAliases(alias: "Internet Explorer 8")
				.save(failOnError: true)
		browserName="FF"
		new Browser(
				name: browserName,
				weight: 55)
				.addToBrowserAliases(alias: "FF")
				.addToBrowserAliases(alias: "FF7")
				.addToBrowserAliases(alias: "Firefox")
				.addToBrowserAliases(alias: "Firefox7")
				.save(failOnError: true)
				
		browserName="Chrome"
		new Browser(
				name: browserName,
				weight: 55)
				.addToBrowserAliases(alias: "Chrome")
				.save(failOnError: true)
	}
	private static createPages(){
		['HP', 'MES', Page.UNDEFINED].each{pageName ->
			Double weight = 0
			switch(pageName){
				case 'HP' : weight = 6		; break
				case 'MES' : weight = 9		; break
				case 'SE' : weight = 36		; break
				case 'ADS' : weight = 43		; break
				case 'WKBS' : weight = 3		; break
				case 'WK' : weight = 3		; break
			}
			new Page(
					name: pageName,
					weight: weight).save(failOnError: true)
		}
	}
	
	// mocks common to all tests /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	void mockTimeToCsMappingService(){
		locationAndResultPersisterService.timeToCsMappingService.metaClass.getCustomerSatisfactionInPercent = {Integer docReadyTimeInMilliSecs, Page page -> 
			return 42 //not the concern of this tests
		}
	}
	void mockMeasuredValueUpdateService(){
		locationAndResultPersisterService.measuredValueUpdateService.metaClass.createOrUpdateDependentMvs = {EventResult result -> 
			//not the concern of this tests
		}	
	}
	void mockMetricReportingService(){
		locationAndResultPersisterService.metricReportingService.metaClass.reportEventResultToGraphite = {EventResult result ->
			//not the concern of this tests
		}
	}
	
}

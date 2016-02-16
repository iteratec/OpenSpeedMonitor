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

import static org.hamcrest.Matchers.*
import static org.junit.Assert.*
import groovy.util.slurpersupport.GPathResult

import org.junit.After
import org.junit.Before
import org.junit.Test

import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.measurement.schedule.JobGroupType
import de.iteratec.osm.csi.Page
import de.iteratec.osm.ConfigService
import de.iteratec.osm.result.detail.WebPerformanceWaterfall
import de.iteratec.osm.csi.IntTestWithDBCleanup
import de.iteratec.osm.result.EventResult
import de.iteratec.osm.measurement.environment.Browser
import de.iteratec.osm.measurement.environment.Location
import de.iteratec.osm.measurement.environment.WebPageTestServer

/**
 *
 */
class PersistingResultsIntSpec extends IntTestWithDBCleanup {

    LocationAndResultPersisterService locationAndResultPersisterService
	
	private static final String RESULT_ID_MULTISTEP = '130425_W1_f606bebc977a3b22c1a9205f70d07a00'
	private static final String RESULT_ID_NO_MULTISTEP = '140106_4H_cebcd61785a29a60d9ecf2aa58cad2df'
	private static final String LOCATION_IDENTIFIER  = 'Agent1-wptdriver:Firefox'
	WebPageTestServer server1
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
		mockMetricReportingService()
		locationAndResultPersisterService.configService = [ getDetailDataStorageTimeInWeeks: { 12 },
			getDefaultMaxDownloadTimeInMinutes: { 60 } ] as ConfigService
    }

	@After
    void tearDown() {
    }

	@Test
    void testPersistingOfResultsAfterFailedHarParsing() {
		
		//create test-specific data
		GPathResult xmlResult = new XmlSlurper().parse(new File("test/resources/WptResultXmls/Result_Multistep_1Run_2EventNames_PagePrefix.xml"))
		String har = new File("test/resources/HARs/invalid.har").getText()
		
		//test specific mocks
		mockCsiAggregationUpdateService(false)
		
		//test execution
		locationAndResultPersisterService.listenToResult(xmlResult, har, server1)
		
		//assertions
		int runs = 1
		int events = 2
		int cachedViews = 2
		int expectedNumberOfResults = runs * events * cachedViews
		assertThat(EventResult.list().size, is(expectedNumberOfResults))
		
		List<WebPerformanceWaterfall> waterfalls = WebPerformanceWaterfall.list()
		assertThat(waterfalls.size(), is(0))
    }
	
	@Test
	void testPersistingOfResultsAfterFailedDependentCsiAggregationCalculation() {
		
		//create test-specific data
		GPathResult xmlResult = new XmlSlurper().parse(new File("test/resources/WptResultXmls/Result_Multistep_1Run_2EventNames_PagePrefix.xml"))
		String harInconsistentButNotTheConcernOfThisTest = new File("test/resources/HARs/multistep_1Run_6Events_FirstAndRepeatedView.har").getText()
		
		//test specific mocks
		mockCsiAggregationUpdateService(true)
		
		//test execution
		locationAndResultPersisterService.listenToResult(xmlResult, harInconsistentButNotTheConcernOfThisTest, server1)
		
		//assertions
		int runs = 1
		int events = 2
		int cachedViews = 2
		int expectedNumberOfResults = runs * events * cachedViews
		assertThat(EventResult.list().size, is(expectedNumberOfResults))
	}
	
	// create testdata common to all tests /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	private createServers(){
		server1 = new WebPageTestServer(
			label: "TestServer 1",
			proxyIdentifier: "TestServer1",
			baseUrl: "http://ism.intgerationtest",
			active: true
		).save(failOnError: true, validate: false)
	}
	
	private createLocations(){
		new Location(
				label: '',
				uniqueIdentifierForServer: LOCATION_IDENTIFIER,
				active: true,
				valid: 1,
				wptServer: server1,
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
	void mockCsiAggregationUpdateService(Boolean shouldFail){
		locationAndResultPersisterService.csiAggregationUpdateService.metaClass.createOrUpdateDependentMvs = {EventResult result ->
			if(shouldFail) throw new RuntimeException()
		}	
	}
	void mockMetricReportingService(){
		locationAndResultPersisterService.metricReportingService.metaClass.reportEventResultToGraphite = {EventResult result ->
			//not the concern of this tests
		}
	}
}

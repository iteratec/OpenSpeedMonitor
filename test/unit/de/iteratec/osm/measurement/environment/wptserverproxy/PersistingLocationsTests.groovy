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
import de.iteratec.osm.measurement.environment.Browser
import de.iteratec.osm.measurement.environment.BrowserAlias
import de.iteratec.osm.measurement.environment.BrowserService
import de.iteratec.osm.measurement.environment.Location
import de.iteratec.osm.measurement.environment.WebPageTestServer
import de.iteratec.osm.measurement.script.Script
import de.iteratec.issc.*
import de.iteratec.osm.ConfigService
import de.iteratec.osm.measurement.schedule.*
import de.iteratec.osm.result.*
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import groovy.util.slurpersupport.GPathResult
import org.junit.After
import org.junit.Before
import org.junit.Test

import static org.junit.Assert.*

/**
 * Tests the saving of locations and results. These functions are used in proxy-mechanism.
 * Testing the mapping of load-times to customer satisfactions or the persisting of dependent {@link CsiAggregation}s is not the concern of the tests in this class.
 * @author nkuhn
 * @see {@link ProxyService}
 * 
 */
@TestFor(LocationAndResultPersisterService)
@Mock([WebPageTestServer, Browser, Location, Job, JobResult, EventResult, BrowserAlias, Page, MeasuredEvent, JobGroup, Script])
class PersistingLocationsTests {

	WebPageTestServer server1, server2
	
	JobGroup undefinedJobGroup;
	
	Browser undefinedBrowser;
	
	LocationAndResultPersisterService serviceUnderTest

	@Before 
	void setUp() {
		
		serviceUnderTest = service
		
		server1 = new WebPageTestServer(
				label: "TestServer 1",
				proxyIdentifier: "TestServer1",
				baseUrl: "http://wptUnitTest.dev.hh.iteratec.local",
				active: true
		).save(failOnError: true, validate: false)
		
		server2 = new WebPageTestServer(
				label: "TestServer 2",
				proxyIdentifier: "TestServer2",
				baseUrl: "http://wptUnitTest2.dev.hh.iteratec.local",
				active: 1
		).save(failOnError: true, validate: false)
		
		undefinedJobGroup=new JobGroup(
			name: JobGroup.UNDEFINED_CSI
			);
		undefinedJobGroup.save(failOnError: true);
		
		//creating test-data common to all tests
		createPages()
		createBrowsers()
		
		serviceUnderTest.configService = [ getDetailDataStorageTimeInWeeks: { 12 },
										   getDefaultMaxDownloadTimeInMinutes: { 60 } ] as ConfigService
	}
	
	@After
	void tearDown(){
	}

	// tests///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * Tests the persisting of {@link Location}s while listening to incoming {@link EventResult}s.
	 */
	@Test
	void testSavingOfLocations() {
		
		//mocking of inner services

		mockBrowserService()
		
		//create test-specific data

		Integer locationCount = Location.findAll().size()
		def file = new File('test/resources/WptLocationXmls/locationResponse.xml')
		GPathResult result = new XmlSlurper().parse(file)
		
		//test execution and assertions

		serviceUnderTest.listenToLocations(result, server1)
		assertTrue(Location.findAll().size() > locationCount)

		locationCount = Location.findAll().size()
		serviceUnderTest.listenToLocations(result, server1)
		assertEquals(locationCount, Location.findAll().size())

		serviceUnderTest.listenToLocations(result, server2)
		assertTrue(Location.findAll().size() > locationCount)
	}
	
	/**
	 * Tests the update of {@link Location}s identifier while listening to incoming locationResponeses.
	 */
	@Test
	void testUpdateOfLocationIdentifier() {
		
		//mocking of inner services

		mockBrowserService()
		
		//create test-specific data

		Integer locationCount = Location.findAll().size()
		def file = new File('test/resources/WptLocationXmls/locationResponse.xml')
		GPathResult result = new XmlSlurper().parse(file)
		
		
		new Location(
				active: true,
				valid: 1,
				location: "UNIT_TEST_LOCATION",//z.B. Agent1-wptdriver
				label: "Unit Test Location: Browser?",//z.B. Agent 1: Windows 7 (S008178178)
				browser: undefinedBrowser,//z.B. Firefox
				wptServer: server1
				).save(failOnError: true);
		
		//test execution and assertions

		serviceUnderTest.listenToLocations(result, server1)
		assertTrue(Location.findAll().size() > locationCount)

		locationCount = Location.findAll().size()
		serviceUnderTest.listenToLocations(result, server1)
		assertEquals(locationCount, Location.findAll().size())

		serviceUnderTest.listenToLocations(result, server2)
		assertTrue(Location.findAll().size() > locationCount)
	}
	
	/**
	 * Tests the persisting of {@link Location}s while listening to incoming {@link EventResult}s.
	 * Should create a new Location with the right Browser
	 */
	@Test
	void testUpdatingOfLocationsBrowser() {
		
		//mocking of inner services

		mockBrowserService()
		
		//create test-specific data

		Integer locationCount = Location.findAll().size()
		assertEquals(0, locationCount);
		
		def file = new File('test/resources/WptLocationXmls/locationResponse.xml')
		GPathResult result = new XmlSlurper().parse(file)
		
		Integer newLocationCount=result.data.location.size();
		//test execution and assertions

		
		serviceUnderTest.listenToLocations(result, server1)
		assertEquals(newLocationCount, Location.findAll().size());
		
		//Modifing Result
		result.data.location.each { xmlResult ->
			
			if(xmlResult.Browser=="Chrome") {
				xmlResult.Browser=Browser.UNDEFINED;
			}
			
		}
		
		serviceUnderTest.listenToLocations(result, server1)
		assertEquals(newLocationCount+1, Location.findAll().size());
		
		assertEquals(1, Location.findAllByBrowser(Browser.findByName(Browser.UNDEFINED)).size())
		
	}
	

	// mocks ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	private void mockBrowserService(){
		def browserService = mockFor(BrowserService, true)
		browserService.demand.findByNameOrAlias(0..100) { String nameOrAlias ->
			//not the concern of this test
			if(nameOrAlias.startsWith("IE"))
				return Browser.findByName('IE');
			else if(nameOrAlias.startsWith("FF") || nameOrAlias.startsWith("Firefox"))
				return Browser.findByName('FF');
			else if(nameOrAlias.startsWith("Chrome"))
				return Browser.findByName('Chrome');
			else  {
				return Browser.findByName(Browser.UNDEFINED);
			}
		}
		serviceUnderTest.browserService = browserService.createMock()
	}

	// create testdata common to all tests /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

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
}

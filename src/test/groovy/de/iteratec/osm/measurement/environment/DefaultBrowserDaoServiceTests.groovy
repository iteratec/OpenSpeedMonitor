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

package de.iteratec.osm.measurement.environment

import de.iteratec.osm.measurement.environment.dao.BrowserDaoService
import org.junit.Test;

import grails.test.mixin.*

import org.junit.*
import static org.junit.Assert.*

/**
 * Test-suite for {@link Browser}.
 */
@TestFor(DefaultBrowserDaoService)
@Mock([Browser])
class DefaultBrowserDaoServiceTests {

    public static final String nameBrowser1 = 'browser1'
	public static final String nameBrowser2 = 'browser2'
	public static final String nameBrowser3 = 'browser3'
	public static final String nameBrowser4 = 'browser4'
	
	BrowserDaoService serviceUnderTest
	
    @Before
	void setUp(){
		serviceUnderTest = service
		createDataCommonForAllTests()
	}
	
	@Test
	void testFindAll() {
		new Browser(name: 'FindAllBrowser1', weight: 0.1d).save(validate: false)
		new Browser(name: 'FindAllBrowser2', weight: 0.54d).save(validate: false)
		
		Set<Browser> result = serviceUnderTest.findAll()
		
		assertNotNull(result);
		assertEquals(2, result.size());
		assertEquals(1, result.count( { it.name == 'FindAllBrowser1' } ));
		assertEquals(1, result.count( { it.name == 'FindAllBrowser2' } ));
		
		new Browser(name: 'FindAllBrowser3', weight: 0.54d).save(validate: false)
		
		Set<Browser> resultAfterAdding = serviceUnderTest.findAll()
		
		assertNotNull(resultAfterAdding);
		assertEquals(3, resultAfterAdding.size());
		assertEquals(1, resultAfterAdding.count( { it.name == 'FindAllBrowser1' } ));
		assertEquals(1, resultAfterAdding.count( { it.name == 'FindAllBrowser2' } ));
		assertEquals(1, resultAfterAdding.count( { it.name == 'FindAllBrowser3' } ));
	}
	
	@Test
    void testGetIdToObjectMap() {
		
		//create test-specific data
		
		Browser browser1 = new Browser(name: nameBrowser1, weight: 0).save(failOnError: true)
		Browser browser2 = new Browser(name: nameBrowser2, weight: 0).save(failOnError: true)
		Browser browser3 = new Browser(name: nameBrowser3, weight: 0).save(failOnError: true)
		Browser browser4 = new Browser(name: nameBrowser4, weight: 0).save(failOnError: true)
		
		//execute test
		
		Map<Long, Browser> idToObjectMap = serviceUnderTest.getIdToObjectMap()
		
		//assertions
		
		assertEquals(
			[
				(browser1.ident()) : browser1,
				(browser2.ident()) : browser2,
				(browser3.ident()) : browser3,
				(browser4.ident()) : browser4
				], 
			idToObjectMap)
    }
	private void createDataCommonForAllTests(){
		//nothing to do yet
	}
}

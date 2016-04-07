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

package de.iteratec.osm.measurement.schedule

import de.iteratec.osm.csi.Page
import de.iteratec.osm.measurement.schedule.dao.PageDaoService
import org.junit.Test;




import grails.test.mixin.*

import org.junit.*
import static org.junit.Assert.* 

/**
 * Test-suite for {@link de.iteratec.osm.measurement.schedule.dao.PageDaoService}.
 */
@TestFor(DefaultPageDaoService)
@Mock([Page])
class DefaultPageDaoServiceTests {

    public static final String namePage1 = 'page1'
	public static final String namePage2 = 'page2'
	public static final String namePage3 = 'page3'
	public static final String namePage4 = 'page4'
	
	PageDaoService serviceUnderTest
	
    @Before
	void setUp(){
		serviceUnderTest = service
		createDataCommonForAllTests()
	}
	
	@Test
	void testFindAll() {
		new Page(name: 'Page1', weight: 0).save(failOnError: true)
		new Page(name: 'Page2', weight: 0).save(failOnError: true)
		
		Set<Page> result = serviceUnderTest.findAll();
		
		assertNotNull(result);
		assertEquals(2, result.size());
		assertEquals(1, result.count( { it.name == 'Page1' } ));
		assertEquals(1, result.count( { it.name == 'Page2' } ));
		
		new Page(name: 'Page3', weight: 0).save(failOnError: true)
		
		Set<Page> resultAfterAdding = serviceUnderTest.findAll();
		
		assertNotNull(resultAfterAdding);
		assertEquals(3, resultAfterAdding.size());
		assertEquals(1, resultAfterAdding.count( { it.name == 'Page1' } ));
		assertEquals(1, resultAfterAdding.count( { it.name == 'Page2' } ));
		assertEquals(1, resultAfterAdding.count( { it.name == 'Page3' } ));
	}
	
	@Test
    void testGetIdToObjectMap() {
		
		//create test-specific data
		
		Page page1 = new Page(name: namePage1, weight: 0).save(failOnError: true)
		Page page2 = new Page(name: namePage2, weight: 0).save(failOnError: true)
		Page page3 = new Page(name: namePage3, weight: 0).save(failOnError: true)
		Page page4 = new Page(name: namePage4, weight: 0).save(failOnError: true)
		
		//execute test
		
		Map<Long, Page> idToObjectMap = serviceUnderTest.getIdToObjectMap()
		
		//assertions
		
		assertEquals(
			[
				(page1.ident()) : page1,
				(page2.ident()) : page2,
				(page3.ident()) : page3,
				(page4.ident()) : page4
				], 
			idToObjectMap)
    }
	private void createDataCommonForAllTests(){
		//nothing to do yet
	}
}

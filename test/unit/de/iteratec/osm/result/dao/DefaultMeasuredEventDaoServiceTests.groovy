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

package de.iteratec.osm.result.dao

import grails.test.mixin.*

import org.junit.*

import de.iteratec.osm.csi.Page
import de.iteratec.osm.result.MeasuredEvent

/**
 * Test-suite for {@link de.iteratec.osm.result.dao.DefaultMeasuredEventDaoService}.
 */
@TestFor(DefaultMeasuredEventDaoService)
@Mock([Page, MeasuredEvent])
class DefaultMeasuredEventDaoServiceTests {
	
	MeasuredEventDaoService serviceUnderTest
	
	public static final String nameHp = 'HP'
	public static final String nameMes = 'MES'
	public static final String nameSe = 'SE'
	public static final String nameAds = 'ADS'
	public static final String nameWkbs = 'WKBS'
	public static final String nameWk = 'WK'
	
	@Before
	void setUp(){
		serviceUnderTest = service
		createTestDataCommonForAllTests()
	}
	
	@After
	public void tearDown()
	{
		// Clean-up:
		MeasuredEvent.list()*.delete(flush:true, failOnError: true)
		Page.list()*.delete(flush:true, failOnError: true)
	}
	
	@Test
	void testFindAll() {
		Page page1 = new Page(name: 'FindAllPage1').save(validate: false)
		new MeasuredEvent(name: 'FindAllEvent1', testedPage: page1).save(failOnError: true, validate: false)
		
		Page page2 = new Page(name: 'FindAllPage2').save(validate: false)
		new MeasuredEvent(name: 'FindAllEvent2', testedPage: page2).save(failOnError: true, validate: false)
		
		Set<MeasuredEvent> result = serviceUnderTest.findAll()
		
		assertNotNull(result);
		assertEquals(2, result.size());
		assertEquals(1, result.count( { it.name == 'FindAllEvent1' } ));
		assertEquals(1, result.count( { it.name == 'FindAllEvent2' } ));
		
		Page page3 = new Page(name: 'FindAllPage3').save(validate: false)
		new MeasuredEvent(name: 'FindAllEvent3', testedPage: page3).save(failOnError: true, validate: false)
		
		Set<MeasuredEvent> resultAfterAdding = serviceUnderTest.findAll()
		
		assertNotNull(resultAfterAdding);
		assertEquals(3, resultAfterAdding.size());
		assertEquals(1, resultAfterAdding.count( { it.name == 'FindAllEvent1' } ));
		assertEquals(1, resultAfterAdding.count( { it.name == 'FindAllEvent2' } ));
		assertEquals(1, resultAfterAdding.count( { it.name == 'FindAllEvent3' } ));
	}

	@Test
    void testGettingEventsForPages() {
		
		//getting/creating test-specific data
		
		Page hp = Page.findByName(nameHp)
		Page mes = Page.findByName(nameMes)
		Page se = Page.findByName(nameSe)
		Page ads = Page.findByName(nameAds)
		Page wkbs = Page.findByName(nameWkbs)
		Page wk = Page.findByName(nameWk)
        createMeasuredEvents("${nameHp}1", hp)
		createMeasuredEvents("${nameHp}2", hp)
		createMeasuredEvents("${nameHp}3", hp)
		createMeasuredEvents(nameMes, mes)
		createMeasuredEvents(nameSe, se)
		createMeasuredEvents(nameAds, ads)
		createMeasuredEvents(nameWk, wk)
		
		//execute test and assertions
		
		assertEquals(3, serviceUnderTest.getEventsFor([hp,wkbs]).size())
		assertEquals(0, serviceUnderTest.getEventsFor([wkbs]).size())
		assertEquals(7, serviceUnderTest.getEventsFor([hp, mes, se, ads, wkbs, wk]).size())
    }
	
	@Test
	void testGetIdToObjectMap() {
		
		//create test-specific data
		
		MeasuredEvent event1 = createMeasuredEvents('event1', null)
		MeasuredEvent event2 = createMeasuredEvents('event2', null)
		MeasuredEvent event3 = createMeasuredEvents('event3', null)
		MeasuredEvent event4 = createMeasuredEvents('event4', null)
		
		//execute test
		
		Map<Long, MeasuredEvent> idToObjectMap = serviceUnderTest.getIdToObjectMap()
		
		//assertions
		
		assertEquals(
			[
				(event1.ident()) : event1,
				(event2.ident()) : event2,
				(event3.ident()) : event3,
				(event4.ident()) : event4
				],
			idToObjectMap)
	}
	
	private MeasuredEvent createMeasuredEvents(String eventName, Page eventPage){
		return new MeasuredEvent(name: eventName, testedPage: eventPage).save(failOnError: true, validate: false)
	}
	
	//creation of testdata common for all tests///////////////////////////////////////////////////////////
	
	private void createTestDataCommonForAllTests(){
		createPages()
	}
	
	private void createPages(){
		new Page(name: nameHp).save(validate: false)
		new Page(name: nameMes).save(validate: false)
		new Page(name: nameSe).save(validate: false)
		new Page(name: nameAds).save(validate: false)
		new Page(name: nameWkbs).save(validate: false)
		new Page(name: nameWk).save(validate: false)
	}
}

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

import de.iteratec.osm.csi.Page
import de.iteratec.osm.result.MeasuredEvent
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import spock.lang.Specification

/**
 * Test-suite for {@link DefaultMeasuredEventDaoService}.
 */
@TestFor(DefaultMeasuredEventDaoService)
@Mock([Page, MeasuredEvent])
class DefaultMeasuredEventDaoServiceTests extends Specification {

    DefaultMeasuredEventDaoService serviceUnderTest
	
	public static final String nameHp = 'HP'
	public static final String nameMes = 'MES'
	public static final String nameSe = 'SE'
	public static final String nameAds = 'ADS'
	public static final String nameWkbs = 'WKBS'
	public static final String nameWk = 'WK'

	void setup(){
		serviceUnderTest = service
		createTestDataCommonForAllTests()
	}
	
	void cleanup(){
		MeasuredEvent.list()*.delete(flush:true, failOnError: true)
		Page.list()*.delete(flush:true, failOnError: true)
	}
	
	void "Find all method will find all MeasuredEvents from db"() {

        setup: "Create initial MeasuredEvents for this feature method."
        Page hp = Page.findByName(nameHp)
        Page mes = Page.findByName(nameMes)
        Page ads = Page.findByName(nameAds)
		new MeasuredEvent(name: 'FindAllEvent1', testedPage: hp).save(failOnError: true)
		new MeasuredEvent(name: 'FindAllEvent2', testedPage: mes).save(failOnError: true)

        when: "findAll() is called first time"
		Set<MeasuredEvent> result = serviceUnderTest.findAll()

        then: "initial MeasuredEvents should be found."
        result != null
        result.size() == 2
        result.count{ it.name == 'FindAllEvent1' } == 1
        result.count{ it.name == 'FindAllEvent2' } == 1

        when: "A third MeasuredEvent is created and findAll() is called again."
        new MeasuredEvent(name: 'FindAllEvent3', testedPage: ads).save(failOnError: true)
        result = serviceUnderTest.findAll()

        then: "New MeasuredEvent should be found additionally."
        result != null
        result.size() == 3
        result.count{ it.name == 'FindAllEvent1' } == 1
        result.count{ it.name == 'FindAllEvent2' } == 1
        result.count{ it.name == 'FindAllEvent3' } == 1

	}

    //TODO: The following test fails with grails versions 2.5.0 and 2.5.1 cause of a grails bug
    // Should be enabled if this is fixed with a newer grails version we can use
    // see: https://github.com/grails/grails-core/issues/9279

//    void "MeasuredEvents can be queried by Pages"() {
//
//        when: "Creating test-specific MeasuredEvents with  different Pages."
//		Page hp = Page.findByName(nameHp)
//		Page mes = Page.findByName(nameMes)
//		Page se = Page.findByName(nameSe)
//		Page ads = Page.findByName(nameAds)
//		Page wkbs = Page.findByName(nameWkbs)
//		Page wk = Page.findByName(nameWk)
//        createMeasuredEvents("${nameHp}1", hp)
//		createMeasuredEvents("${nameHp}2", hp)
//		createMeasuredEvents("${nameHp}3", hp)
//		createMeasuredEvents(nameMes, mes)
//		createMeasuredEvents(nameSe, se)
//		createMeasuredEvents(nameAds, ads)
//		createMeasuredEvents(nameWk, wk)
//
//        then: "These events can be queried by associated Pages."
//        serviceUnderTest.getEventsFor([hp,wkbs]).size() == 3
//        serviceUnderTest.getEventsFor([wkbs]).size() == 0
//        serviceUnderTest.getEventsFor([hp, mes, se, ads, wkbs, wk]).size() == 7
//
//    }

	void "Service method getIdToObjectMap() provides an id to object map of all persisted MeasuredEvents"() {

		setup: "Create test-specific MeasuredEvents."
        Page hp = Page.findByName(nameHp)
		MeasuredEvent event1 = createMeasuredEvents('event1', hp)
		MeasuredEvent event2 = createMeasuredEvents('event2', hp)
		MeasuredEvent event3 = createMeasuredEvents('event3', hp)
		MeasuredEvent event4 = createMeasuredEvents('event4', hp)

        when: "Service method getIdToObjectMap() is called."
        Map<Long, MeasuredEvent> idToObjectMap = serviceUnderTest.getIdToObjectMap()

	    then: "The response is an id to object map representing all persisted MeasuredEvents."
        idToObjectMap == [
                (event1.ident()) : event1,
                (event2.ident()) : event2,
                (event3.ident()) : event3,
                (event4.ident()) : event4
            ]
	}

    //helper methods///////////////////////////////////////////////////////////
	
	private MeasuredEvent createMeasuredEvents(String eventName, Page eventPage){
		return new MeasuredEvent(name: eventName, testedPage: eventPage).save(failOnError: true)
	}
	
	//creation of testdata common for all tests///////////////////////////////////////////////////////////
	
	private void createTestDataCommonForAllTests(){
		createPages()
	}
	
	private void createPages(){
		new Page(name: nameHp).save(failOnError: true)
		new Page(name: nameMes).save(failOnError: true)
		new Page(name: nameSe).save(failOnError: true)
		new Page(name: nameAds).save(failOnError: true)
		new Page(name: nameWkbs).save(failOnError: true)
		new Page(name: nameWk).save(failOnError: true)
	}
}

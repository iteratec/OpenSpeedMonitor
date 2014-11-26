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

package de.iteratec.osm.result

import de.iteratec.osm.result.EventResultXmlService
import grails.test.mixin.*
import groovy.util.slurpersupport.GPathResult
import org.junit.Before
import org.junit.Test

/**
 * Test-suite of {@link de.iteratec.osm.result.EventResultXmlService}.
 */
@TestFor(EventResultXmlService)
class EventResultXmlServiceTests {
	
	EventResultXmlService serviceUnderTest
	
	GPathResult resultXml_6steps_3runs_UncachedAndCached
	GPathResult resultXml_6steps_3runs_JustUncached
	GPathResult resultXml_2steps_3runs_UncachedAndCached
	GPathResult resultXml_1step_3runs_UncachedAndCached
	
	GPathResult resultXml_old_beforeMultistep
	
	@Before
	void setUp(){
		serviceUnderTest=service
		
		resultXml_6steps_3runs_UncachedAndCached = new XmlSlurper().parse(new File("test/resources/WptResultXmls/6steps_3runs_UncachedAndCached.xml"))
		resultXml_6steps_3runs_JustUncached = new XmlSlurper().parse(new File("test/resources/WptResultXmls/6steps_3runs_JustUncached.xml"))
		resultXml_2steps_3runs_UncachedAndCached = new XmlSlurper().parse(new File("test/resources/WptResultXmls/2steps_3runs_UncachedAndCached.xml"))
		resultXml_1step_3runs_UncachedAndCached = new XmlSlurper().parse(new File("test/resources/WptResultXmls/1step_3runs_UncachedAndCached.xml"))

		resultXml_old_beforeMultistep  = new XmlSlurper().parse(new File("test/resources/WptResultXmls/Result_version1_BeforeMultistep.xml"))
	}

	@Test
    void testGetTeststepCount() {
		assertEquals(6, serviceUnderTest.getTeststepCount(resultXml_6steps_3runs_UncachedAndCached))
		assertEquals(6, serviceUnderTest.getTeststepCount(resultXml_6steps_3runs_JustUncached))
		assertEquals(2, serviceUnderTest.getTeststepCount(resultXml_2steps_3runs_UncachedAndCached))
		assertEquals(1, serviceUnderTest.getTeststepCount(resultXml_1step_3runs_UncachedAndCached))
		
		assertEquals(1, serviceUnderTest.getTeststepCount(resultXml_old_beforeMultistep))
    }
}

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

import de.iteratec.osm.result.CachedView

import static org.hamcrest.Matchers.*
import static org.junit.Assert.*
import grails.test.mixin.*
import grails.test.mixin.support.*
import groovy.util.slurpersupport.GPathResult

/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
@TestMixin(GrailsUnitTestMixin)
@TestFor(ResultPersisterService)
class ResultXmlProcessingSpec {
	ResultPersisterService serviceUnderTest
    void setUp() {
		serviceUnderTest = service
    }

    void tearDown() {
    }
	
	//TODO: write tests for single- and multistep-tests each with more than one run
	//TODO: write tests for older result xml's?

    void testIsMedianMultistep1Run() {
		//test specific data
		GPathResult xmlResult = new XmlSlurper().parse(new File("test/resources/WptResultXmls/MULTISTEP_FORK_ITERATEC_1Run_3Events_JustFirstView_WithVideo.xml"))
		WptResultXml resultXml = new WptResultXml(xmlResult)
		//test execution and assertions
		assertThat(resultXml.isMedian(0, CachedView.UNCACHED, 0), is(true))
		assertThat(resultXml.isMedian(0, CachedView.UNCACHED, 1), is(true))
		assertThat(resultXml.isMedian(0, CachedView.UNCACHED, 2), is(true))
    }
	void testIsMedianMultistep5Runs() {
		//test specific data
		GPathResult xmlResult = new XmlSlurper().parse(new File("test/resources/WptResultXmls/MULTISTEP_FORK_ITERATEC_5Runs_3Events_JustFirstView_WithVideo.xml"))
		WptResultXml resultXml = new WptResultXml(xmlResult)

		//test execution and assertions
		assertThat(resultXml.isMedian(0, CachedView.UNCACHED, 0), is(false))
		assertThat(resultXml.isMedian(0, CachedView.UNCACHED, 1), is(false))
		assertThat(resultXml.isMedian(0, CachedView.UNCACHED, 2), is(false))
		assertThat(resultXml.isMedian(1, CachedView.UNCACHED, 0), is(false))
		assertThat(resultXml.isMedian(1, CachedView.UNCACHED, 1), is(false))
		assertThat(resultXml.isMedian(1, CachedView.UNCACHED, 2), is(true))
		assertThat(resultXml.isMedian(2, CachedView.UNCACHED, 0), is(false))
		assertThat(resultXml.isMedian(2, CachedView.UNCACHED, 1), is(true))
		assertThat(resultXml.isMedian(2, CachedView.UNCACHED, 2), is(false))
		assertThat(resultXml.isMedian(3, CachedView.UNCACHED, 0), is(false))
		assertThat(resultXml.isMedian(3, CachedView.UNCACHED, 1), is(false))
		assertThat(resultXml.isMedian(3, CachedView.UNCACHED, 2), is(false))
		assertThat(resultXml.isMedian(4, CachedView.UNCACHED, 0), is(true))
		assertThat(resultXml.isMedian(4, CachedView.UNCACHED, 1), is(false))
		assertThat(resultXml.isMedian(4, CachedView.UNCACHED, 2), is(false))

	}
	void testIsMedianSinglestep1Run() {
		//test specific data
		GPathResult xmlResult = new XmlSlurper().parse(new File("test/resources/WptResultXmls/BEFORE_MULTISTEP_1Run_WithVideo.xml"))
		WptResultXml resultXml = new WptResultXml(xmlResult)
		//test execution and assertions
		
		assertThat(resultXml.isMedian(0,CachedView.UNCACHED, 0), is(true))
		assertThat(resultXml.isMedian(0,CachedView.CACHED, 0), is(true))
	}
	void testIsMedianSinglestep5Runs() {
		//test specific data
		GPathResult xmlResult = new XmlSlurper().parse(new File("test/resources/WptResultXmls/BEFORE_MULTISTEP_5Runs_WithVideo.xml"))
		WptResultXml resultXml = new WptResultXml(xmlResult)

		//test execution and assertions
		assertThat(resultXml.isMedian(0, CachedView.UNCACHED, 0), is(false))
		assertThat(resultXml.isMedian(1, CachedView.UNCACHED, 0), is(false))
		assertThat(resultXml.isMedian(2, CachedView.UNCACHED, 0), is(false))
		assertThat(resultXml.isMedian(3, CachedView.UNCACHED, 0), is(false))
		assertThat(resultXml.isMedian(4, CachedView.UNCACHED, 0), is(true))

		assertThat(resultXml.isMedian(0, CachedView.CACHED, 0), is(false))
		assertThat(resultXml.isMedian(1, CachedView.CACHED, 0), is(true))
		assertThat(resultXml.isMedian(2, CachedView.CACHED, 0), is(false))
		assertThat(resultXml.isMedian(3, CachedView.CACHED, 0), is(false))
		assertThat(resultXml.isMedian(4, CachedView.CACHED, 0), is(false))
	}
    void testGetBwDown(){
        //test specific data
        GPathResult xmlResultSinglestep = new XmlSlurper().parse(new File("test/resources/WptResultXmls/BEFORE_MULTISTEP_5Runs_WithVideo.xml"))
        WptResultXml resultXmlSinglestep = new WptResultXml(xmlResultSinglestep)
        GPathResult xmlResultMultistep = new XmlSlurper().parse(new File("test/resources/WptResultXmls/MULTISTEP_FORK_ITERATEC_1Run_WithVideo.xml"))
        WptResultXml resultXmlMultistep = new WptResultXml(xmlResultMultistep)

        //test execution and assertions
        assertThat(resultXmlSinglestep.getBwDown(), is(6000))
        assertThat(resultXmlMultistep.getBwDown(), is(6000))
    }
    void testGetBwUp(){
        //test specific data
        GPathResult xmlResultSinglestep = new XmlSlurper().parse(new File("test/resources/WptResultXmls/BEFORE_MULTISTEP_5Runs_WithVideo.xml"))
        WptResultXml resultXmlSinglestep = new WptResultXml(xmlResultSinglestep)
        GPathResult xmlResultMultistep = new XmlSlurper().parse(new File("test/resources/WptResultXmls/MULTISTEP_FORK_ITERATEC_1Run_WithVideo.xml"))
        WptResultXml resultXmlMultistep = new WptResultXml(xmlResultMultistep)

        //test execution and assertions
        assertThat(resultXmlSinglestep.getBwUp(), is(512))
        assertThat(resultXmlMultistep.getBwUp(), is(512))
    }
    void testGetLatency(){
        //test specific data
        GPathResult xmlResultSinglestep = new XmlSlurper().parse(new File("test/resources/WptResultXmls/BEFORE_MULTISTEP_5Runs_WithVideo.xml"))
        WptResultXml resultXmlSinglestep = new WptResultXml(xmlResultSinglestep)
        GPathResult xmlResultMultistep = new XmlSlurper().parse(new File("test/resources/WptResultXmls/MULTISTEP_FORK_ITERATEC_1Run_WithVideo.xml"))
        WptResultXml resultXmlMultistep = new WptResultXml(xmlResultMultistep)

        //test execution and assertions
        assertThat(resultXmlSinglestep.getLatency(), is(50))
        assertThat(resultXmlMultistep.getLatency(), is(50))
    }
    void testGetPlr(){
        //test specific data
        GPathResult xmlResultSinglestep = new XmlSlurper().parse(new File("test/resources/WptResultXmls/BEFORE_MULTISTEP_5Runs_WithVideo.xml"))
        WptResultXml resultXmlSinglestep = new WptResultXml(xmlResultSinglestep)
        GPathResult xmlResultMultistep = new XmlSlurper().parse(new File("test/resources/WptResultXmls/MULTISTEP_FORK_ITERATEC_1Run_WithVideo.xml"))
        WptResultXml resultXmlMultistep = new WptResultXml(xmlResultMultistep)

        //test execution and assertions
        assertThat(resultXmlSinglestep.getPacketLossRate(), is(0))
        assertThat(resultXmlMultistep.getPacketLossRate(), is(0))
    }
}

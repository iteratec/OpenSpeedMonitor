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
import de.iteratec.osm.result.WptXmlResultVersion
import groovy.util.slurpersupport.GPathResult
import spock.lang.Specification
import static WptXmlResultVersion.*

class ResultXmlProcessingSpec extends Specification{

    void "WptResultXmls isMedian determines whether a step is the median step correctly - MULTISTEP_FORK_ITERATEC | 1 run"() {
		when: "the xml is parsed"
		GPathResult xmlResult = new XmlSlurper().parse(new File("src/test/resources/WptResultXmls/MULTISTEP_FORK_ITERATEC_1Run_3Events_JustFirstView_WithVideo.xml"))
		WptResultXml resultXml = new WptResultXml(xmlResult)
		then: "isMedian works as expected"
		resultXml.isMedian(0, CachedView.UNCACHED, 0)
		resultXml.isMedian(0, CachedView.UNCACHED, 1)
		resultXml.isMedian(0, CachedView.UNCACHED, 2)
    }
	void "WptResultXmls isMedian determines whether a step is the median step correctly - MULTISTEP_FORK_ITERATEC | 5 runs"() {
        when: "the xml is parsed"
		GPathResult xmlResult = new XmlSlurper().parse(new File("src/test/resources/WptResultXmls/MULTISTEP_FORK_ITERATEC_5Runs_3Events_JustFirstView_WithVideo.xml"))
		WptResultXml resultXml = new WptResultXml(xmlResult)
        then: "isMedian works as expected"
		resultXml.isMedian(0, CachedView.UNCACHED, 0) == false
		resultXml.isMedian(0, CachedView.UNCACHED, 1) == false
		resultXml.isMedian(0, CachedView.UNCACHED, 2) == false
		resultXml.isMedian(1, CachedView.UNCACHED, 0) == false
		resultXml.isMedian(1, CachedView.UNCACHED, 1) == false
		resultXml.isMedian(1, CachedView.UNCACHED, 2) == true
		resultXml.isMedian(2, CachedView.UNCACHED, 0) == false
		resultXml.isMedian(2, CachedView.UNCACHED, 1) == true
		resultXml.isMedian(2, CachedView.UNCACHED, 2) == false
		resultXml.isMedian(3, CachedView.UNCACHED, 0) == false
		resultXml.isMedian(3, CachedView.UNCACHED, 1) == false
		resultXml.isMedian(3, CachedView.UNCACHED, 2) == false
		resultXml.isMedian(4, CachedView.UNCACHED, 0) == true
		resultXml.isMedian(4, CachedView.UNCACHED, 1) == false
		resultXml.isMedian(4, CachedView.UNCACHED, 2) == false

	}
	void "WptResultXmls isMedian determines whether a step is the median step correctly - BEFORE_MULTISTEP | 1 run"() {
        when: "the xml is parsed"
		GPathResult xmlResult = new XmlSlurper().parse(new File("src/test/resources/WptResultXmls/BEFORE_MULTISTEP_1Run_WithVideo.xml"))
		WptResultXml resultXml = new WptResultXml(xmlResult)
        then: "isMedian works as expected"
		resultXml.isMedian(0,CachedView.UNCACHED, 0)
		resultXml.isMedian(0,CachedView.CACHED, 0)
	}
	void "WptResultXmls isMedian determines whether a step is the median step correctly - BEFORE_MULTISTEP | 5 runs"() {
        when: "the xml is parsed"
		GPathResult xmlResult = new XmlSlurper().parse(new File("src/test/resources/WptResultXmls/BEFORE_MULTISTEP_5Runs_WithVideo.xml"))
		WptResultXml resultXml = new WptResultXml(xmlResult)
        then: "isMedian works as expected"
		resultXml.isMedian(0, CachedView.UNCACHED, 0) == false
		resultXml.isMedian(1, CachedView.UNCACHED, 0) == false
		resultXml.isMedian(2, CachedView.UNCACHED, 0) == false
		resultXml.isMedian(3, CachedView.UNCACHED, 0) == false
		resultXml.isMedian(4, CachedView.UNCACHED, 0) == true

		resultXml.isMedian(0, CachedView.CACHED, 0) == false
		resultXml.isMedian(1, CachedView.CACHED, 0) == true
		resultXml.isMedian(2, CachedView.CACHED, 0) == false
		resultXml.isMedian(3, CachedView.CACHED, 0) == false
		resultXml.isMedian(4, CachedView.CACHED, 0) == false
	}
    void "WptResultXmls getBwDown works as expected - BEFORE_MULTISTEP and MULTISTEP_FORK_ITERATEC"(){
        when: "the xml results are parsed"
        GPathResult xmlResultSinglestep = new XmlSlurper().parse(new File("src/test/resources/WptResultXmls/BEFORE_MULTISTEP_5Runs_WithVideo.xml"))
        WptResultXml resultXmlSinglestep = new WptResultXml(xmlResultSinglestep)
        GPathResult xmlResultMultistep = new XmlSlurper().parse(new File("src/test/resources/WptResultXmls/MULTISTEP_FORK_ITERATEC_1Run_WithVideo.xml"))
        WptResultXml resultXmlMultistep = new WptResultXml(xmlResultMultistep)
        then: "getBwDown works as expected"
        resultXmlSinglestep.getBwDown() == 6000
        resultXmlMultistep.getBwDown() == 6000
    }
    void "WptResultXmls getBwUp works as expected - BEFORE_MULTISTEP and MULTISTEP_FORK_ITERATEC"(){
        when: "the xml results are parsed"
        GPathResult xmlResultSinglestep = new XmlSlurper().parse(new File("src/test/resources/WptResultXmls/BEFORE_MULTISTEP_5Runs_WithVideo.xml"))
        WptResultXml resultXmlSinglestep = new WptResultXml(xmlResultSinglestep)
        GPathResult xmlResultMultistep = new XmlSlurper().parse(new File("src/test/resources/WptResultXmls/MULTISTEP_FORK_ITERATEC_1Run_WithVideo.xml"))
        WptResultXml resultXmlMultistep = new WptResultXml(xmlResultMultistep)
        then: "getBwUp works as expected"
        resultXmlSinglestep.getBwUp() == 512
        resultXmlMultistep.getBwUp() == 512
    }
    void "WptResultXmls getLatency works as expected - BEFORE_MULTISTEP and MULTISTEP_FORK_ITERATEC"(){
        when: "the xml results are parsed"
        GPathResult xmlResultSinglestep = new XmlSlurper().parse(new File("src/test/resources/WptResultXmls/BEFORE_MULTISTEP_5Runs_WithVideo.xml"))
        WptResultXml resultXmlSinglestep = new WptResultXml(xmlResultSinglestep)
        GPathResult xmlResultMultistep = new XmlSlurper().parse(new File("src/test/resources/WptResultXmls/MULTISTEP_FORK_ITERATEC_1Run_WithVideo.xml"))
        WptResultXml resultXmlMultistep = new WptResultXml(xmlResultMultistep)
        then: "getLatency works as expected"
        resultXmlSinglestep.getLatency() == 50
        resultXmlMultistep.getLatency() == 50
    }
    void "WptResultXmls getPlr works as expected - BEFORE_MULTISTEP and MULTISTEP_FORK_ITERATEC"(){
        when: "the xml results are parsed"
        GPathResult result = new XmlSlurper().parse(new File("src/test/resources/WptResultXmls/${resultXml}"))
        WptResultXml parsedResultXml = new WptResultXml(result)
        then: "getPlr works as expected"
        parsedResultXml.getPacketLossRate() == expectedPlr
        where:
        resultXml || expectedPlr
        'BEFORE_MULTISTEP_5Runs_WithVideo.xml' || 0
        'MULTISTEP_FORK_ITERATEC_1Run_WithVideo.xml' || 0
    }
    void "WptResultXmls getTeststepCount works as expected"() {
        when: "the xml results are parsed"
        GPathResult result = new XmlSlurper().parse(new File("src/test/resources/WptResultXmls/${resultXml}"))
        WptResultXml parsedResultXml = new WptResultXml(result)
        then: "getTeststepCount works as expected"
        parsedResultXml.getTestStepCount() == expectedNumberOfSteps
        where:
        resultXml || expectedNumberOfSteps
        'BEFORE_MULTISTEP_3Runs.xml' || 1
        'MULTISTEP_FORK_ITERATEC_3Runs_6EventNames.xml' || 6
        'BEFORE_MULTISTEP_1Run_NotCsiRelevantCauseDocTimeTooHighResponse.xml' || 1
        'BEFORE_MULTISTEP_1Run_JustFirstView.xml' || 1
        'MULTISTEP_FORK_ITERATEC_1Run_2EventNamesWithPagePrefix_JustFirstView.xml' || 2
        'MULTISTEP_FORK_ITERATEC_1Run_2EventNames_PagePrefix.xml' || 2
        'BEFORE_MULTISTEP_1Run_WithoutVideo.xml' || 1
        'BEFORE_MULTISTEP_1Run_WithVideo.xml' || 1
    }
    void "WptResultXmls get created with correct wptserver version from xml files"() {
        when: "the xml results are parsed"
        GPathResult result = new XmlSlurper().parse(new File("src/test/resources/WptResultXmls/${resultXml}"))
        WptResultXml parsedResultXml = new WptResultXml(result)
        then: "getTeststepCount works as expected"
        parsedResultXml.version == expectedWptResultVersion
        where:
        resultXml || expectedWptResultVersion
        'BEFORE_MULTISTEP_3Runs.xml' || BEFORE_MULTISTEP
        'MULTISTEP_FORK_ITERATEC_3Runs_6EventNames.xml' || MULTISTEP_FORK_ITERATEC
        'BEFORE_MULTISTEP_1Run_NotCsiRelevantCauseDocTimeTooHighResponse.xml' || BEFORE_MULTISTEP
        'BEFORE_MULTISTEP_1Run_JustFirstView.xml' || BEFORE_MULTISTEP
        'MULTISTEP_FORK_ITERATEC_1Run_2EventNamesWithPagePrefix_JustFirstView.xml' || MULTISTEP_FORK_ITERATEC
        'MULTISTEP_FORK_ITERATEC_1Run_2EventNames_PagePrefix.xml' || MULTISTEP_FORK_ITERATEC
        'BEFORE_MULTISTEP_1Run_WithoutVideo.xml' || BEFORE_MULTISTEP
        'BEFORE_MULTISTEP_1Run_WithVideo.xml' || BEFORE_MULTISTEP
        'MULTISTEP_2Run.xml' || MULTISTEP
    }

}

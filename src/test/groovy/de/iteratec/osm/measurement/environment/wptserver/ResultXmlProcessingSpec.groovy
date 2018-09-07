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

package de.iteratec.osm.measurement.environment.wptserver

import de.iteratec.osm.result.CachedView
import groovy.util.slurpersupport.GPathResult
import spock.lang.Specification

import static de.iteratec.osm.result.WptXmlResultVersion.*

class ResultXmlProcessingSpec extends Specification {

    void "WptResultXmls isMedian determines whether a step is the median step correctly - MULTISTEP_FORK_ITERATEC | 1 run"() {
        when: "the multistep XML with only 1 run is parsed"
        GPathResult xmlResult = new XmlSlurper().parse(new File("src/test/resources/WptResultXmls/MULTISTEP_FORK_ITERATEC_1Run_3Events_JustFirstView_WithVideo.xml"))
        WptResultXml resultXml = new WptResultXml(xmlResult)
        then: "isMedian is true for the first run"
        resultXml.isMedian(0, CachedView.UNCACHED, 0)
        resultXml.isMedian(0, CachedView.UNCACHED, 1)
        resultXml.isMedian(0, CachedView.UNCACHED, 2)
    }

    void "WptResultXmls isMedian determines whether a step is the median step correctly - MULTISTEP_FORK_ITERATEC | 5 runs"() {
        when: "the multistep xml with 5 runs is parsed"
        GPathResult xmlResult = new XmlSlurper().parse(new File("src/test/resources/WptResultXmls/MULTISTEP_FORK_ITERATEC_5Runs_3Events_JustFirstView_WithVideo.xml"))
        WptResultXml resultXml = new WptResultXml(xmlResult)

        then: "isMedian determines is set based on the information in the XML"
        !resultXml.isMedian(0, CachedView.UNCACHED, 0)
        !resultXml.isMedian(0, CachedView.UNCACHED, 1)
        !resultXml.isMedian(0, CachedView.UNCACHED, 2)
        !resultXml.isMedian(1, CachedView.UNCACHED, 0)
        !resultXml.isMedian(1, CachedView.UNCACHED, 1)
        resultXml.isMedian(1, CachedView.UNCACHED, 2)
        !resultXml.isMedian(2, CachedView.UNCACHED, 0)
        resultXml.isMedian(2, CachedView.UNCACHED, 1)
        !resultXml.isMedian(2, CachedView.UNCACHED, 2)
        !resultXml.isMedian(3, CachedView.UNCACHED, 0)
        !resultXml.isMedian(3, CachedView.UNCACHED, 1)
        !resultXml.isMedian(3, CachedView.UNCACHED, 2)
        resultXml.isMedian(4, CachedView.UNCACHED, 0)
        !resultXml.isMedian(4, CachedView.UNCACHED, 1)
        !resultXml.isMedian(4, CachedView.UNCACHED, 2)

    }

    void "WptResultXmls isMedian determines whether a step is the median step correctly - BEFORE_MULTISTEP | 1 run"() {
        when: "the singlestep xml with only 1 run is parsed"
        GPathResult xmlResult = new XmlSlurper().parse(new File("src/test/resources/WptResultXmls/BEFORE_MULTISTEP_1Run_WithVideo.xml"))
        WptResultXml resultXml = new WptResultXml(xmlResult)

        then: "isMedian is true for the first run"
        resultXml.isMedian(0, CachedView.UNCACHED, 0)
        resultXml.isMedian(0, CachedView.CACHED, 0)
    }

    void "WptResultXmls isMedian determines whether a step is the median step correctly - BEFORE_MULTISTEP | 5 runs"() {
        when: "the singlestep xml is parsed"
        GPathResult xmlResult = new XmlSlurper().parse(new File("src/test/resources/WptResultXmls/BEFORE_MULTISTEP_5Runs_WithVideo.xml"))
        WptResultXml resultXml = new WptResultXml(xmlResult)

        then: "isMedian determines is set based on the information in the XML"
        !resultXml.isMedian(0, CachedView.UNCACHED, 0)
        !resultXml.isMedian(1, CachedView.UNCACHED, 0)
        !resultXml.isMedian(2, CachedView.UNCACHED, 0)
        !resultXml.isMedian(3, CachedView.UNCACHED, 0)
        resultXml.isMedian(4, CachedView.UNCACHED, 0)

        !resultXml.isMedian(0, CachedView.CACHED, 0)
        resultXml.isMedian(1, CachedView.CACHED, 0)
        !resultXml.isMedian(2, CachedView.CACHED, 0)
        !resultXml.isMedian(3, CachedView.CACHED, 0)
        !resultXml.isMedian(4, CachedView.CACHED, 0)
    }

    void "WptResultXmls extracts the correct bwDown"(resultXml) {
        when: "the xml results are parsed"
        GPathResult xml = new XmlSlurper().parse(new File("src/test/resources/WptResultXmls/${resultXml}"))
        WptResultXml result = new WptResultXml(xml)

        then: "getBwDown extracts information from the XML"
        result.getBwDown() == 6000

        where:
        resultXml                                    | _
        "BEFORE_MULTISTEP_5Runs_WithVideo.xml"       | _
        "MULTISTEP_FORK_ITERATEC_1Run_WithVideo.xml" | _
    }

    void "WptResultXmls extracts the correct bwUp"(resultXml) {
        when: "the xml results are parsed"
        GPathResult xml = new XmlSlurper().parse(new File("src/test/resources/WptResultXmls/${resultXml}"))
        WptResultXml result = new WptResultXml(xml)

        then: "getBwUp extracts information from the XML"
        result.getBwUp() == 512

        where:
        resultXml                                    | _
        "BEFORE_MULTISTEP_5Runs_WithVideo.xml"       | _
        "MULTISTEP_FORK_ITERATEC_1Run_WithVideo.xml" | _
    }

    void "WptResultXmls extracts the correct latency"(resultXml) {
        when: "the xml results are parsed"
        GPathResult xml = new XmlSlurper().parse(new File("src/test/resources/WptResultXmls/${resultXml}"))
        WptResultXml result = new WptResultXml(xml)

        then: "the correct latency is detected"
        result.getLatency() == 50

        where:
        resultXml                                    | _
        "BEFORE_MULTISTEP_5Runs_WithVideo.xml"       | _
        "MULTISTEP_FORK_ITERATEC_1Run_WithVideo.xml" | _
    }

    void "WptResultXml detects the correct correct PLR"(resultXml, expectedPlr) {
        when: "the xml results are parsed"
        GPathResult result = new XmlSlurper().parse(new File("src/test/resources/WptResultXmls/${resultXml}"))
        WptResultXml parsedResultXml = new WptResultXml(result)

        then: "getPlr works as expected"
        parsedResultXml.getPacketLossRate() == expectedPlr

        where:
        resultXml                                    | expectedPlr
        'BEFORE_MULTISTEP_5Runs_WithVideo.xml'       | 0
        'MULTISTEP_FORK_ITERATEC_1Run_WithVideo.xml' | 0
    }

    void "WptResultXml can detected the right number of steps"(resultXml, expectedNumberOfSteps) {
        when: "the xml results are parsed"
        GPathResult result = new XmlSlurper().parse(new File("src/test/resources/WptResultXmls/${resultXml}"))
        WptResultXml parsedResultXml = new WptResultXml(result)

        then: "the correct number of steps is detected"
        parsedResultXml.getTestStepCount() == expectedNumberOfSteps

        where:
        resultXml                                                                  | expectedNumberOfSteps
        'BEFORE_MULTISTEP_3Runs.xml'                                               | 1
        'MULTISTEP_FORK_ITERATEC_3Runs_6EventNames.xml'                            | 6
        'BEFORE_MULTISTEP_1Run_NotCsiRelevantCauseDocTimeTooHighResponse.xml'      | 1
        'BEFORE_MULTISTEP_1Run_JustFirstView.xml'                                  | 1
        'MULTISTEP_FORK_ITERATEC_1Run_2EventNamesWithPagePrefix_JustFirstView.xml' | 2
        'MULTISTEP_FORK_ITERATEC_1Run_2EventNames_PagePrefix.xml'                  | 2
        'BEFORE_MULTISTEP_1Run_WithoutVideo.xml'                                   | 1
        'BEFORE_MULTISTEP_1Run_WithVideo.xml'                                      | 1
    }

    void "WptResultXmls get created with correct wptserver version from xml files"(resultXml, expectedWptResultVersion) {
        when: "the xml results are parsed"
        GPathResult result = new XmlSlurper().parse(new File("src/test/resources/WptResultXmls/${resultXml}"))
        WptResultXml parsedResultXml = new WptResultXml(result)

        then: "the correct WPT version is detected"
        parsedResultXml.version == expectedWptResultVersion

        where:
        resultXml                                                                  | expectedWptResultVersion
        'BEFORE_MULTISTEP_3Runs.xml'                                               | BEFORE_MULTISTEP
        'MULTISTEP_FORK_ITERATEC_3Runs_6EventNames.xml'                            | MULTISTEP_FORK_ITERATEC
        'BEFORE_MULTISTEP_1Run_NotCsiRelevantCauseDocTimeTooHighResponse.xml'      | BEFORE_MULTISTEP
        'BEFORE_MULTISTEP_1Run_JustFirstView.xml'                                  | BEFORE_MULTISTEP
        'MULTISTEP_FORK_ITERATEC_1Run_2EventNamesWithPagePrefix_JustFirstView.xml' | MULTISTEP_FORK_ITERATEC
        'MULTISTEP_FORK_ITERATEC_1Run_2EventNames_PagePrefix.xml'                  | MULTISTEP_FORK_ITERATEC
        'BEFORE_MULTISTEP_1Run_WithoutVideo.xml'                                   | BEFORE_MULTISTEP
        'BEFORE_MULTISTEP_1Run_WithVideo.xml'                                      | BEFORE_MULTISTEP
        'MULTISTEP_2Run.xml'                                                       | MULTISTEP
    }

}

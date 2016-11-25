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

package de.iteratec.osm.d3

import de.iteratec.osm.util.I18nService
import grails.test.mixin.*
import groovy.util.slurpersupport.NodeChild
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.web.GroovyPageUnitTestMixin} for usage instructions
 */
@TestFor(D3ChartTagLib)
class D3ChartTagLibSpec extends Specification {

    static def HTML_FRAGMENT_PARSER

    void setup(){
        HTML_FRAGMENT_PARSER = new org.cyberneko.html.parsers.SAXParser()
        HTML_FRAGMENT_PARSER.setFeature("http://cyberneko.org/html/features/balance-tags/document-fragment", true)
        def d3ChartTagLib = mockTagLib(D3ChartTagLib)
        d3ChartTagLib.i18nService = Mock(I18nService)
    }

    def "HTML provided by taglib iteratec:multiLineChart returns container with correct identifier"() {
        given:
        String identifier = "ChartIdentifier"

        when:
        String testHTML = applyTemplate("""<iteratec:multiLineChart chartIdentifier ="${identifier}"/>""")

        then:
        NodeChild testHtmlAsNode = new XmlSlurper(HTML_FRAGMENT_PARSER).parseText(testHTML)
        testHtmlAsNode.size() == 1
        def chartContainerNode = testHtmlAsNode.getAt(0)
        chartContainerNode.attributes['id'] == identifier
        chartContainerNode.attributes['class'] == "col-md-8"
    }

    def "HTML provided by taglib iteratec:multiLineChart creates modal filling container for modal dialog"() {
        given:
        String identifier = "ChartIdentifier"

        when:
        String testHTML = applyTemplate("""<iteratec:multiLineChart chartIdentifier ="${identifier}"
                                            modal="true" />""")

        then:
        NodeChild testHtmlAsNode = new XmlSlurper(HTML_FRAGMENT_PARSER).parseText(testHTML)
        def chartContainerNode = testHtmlAsNode.getAt(0)
        chartContainerNode.attributes['class'] == "col-md-12"
    }

    def "HTML provided by taglib iteratec:barChart returns svg container with correct identifier"() {
        given:
        String identifier = "ChartIdentifier"

        when:
        String testHTML = applyTemplate("""<iteratec:barChart chartIdentifier ="${identifier}"/>""")

        then:
        NodeChild testHtmlAsNode = new XmlSlurper(HTML_FRAGMENT_PARSER).parseText(testHTML)
        testHtmlAsNode.size() == 1
        def chartSVGContainerNode = testHtmlAsNode.childNodes().getAt(0).childNodes().getAt(0)
        chartSVGContainerNode.attributes['id'] == identifier
    }

    def "HTML provided by taglib iteratec:treemap returns three containers with correct identifiers"() {
        given:
        String identifier = "ChartIdentifier"

        when:
        String testHTML = applyTemplate("""<iteratec:treemap chartIdentifier ="${identifier}"/>""")

        then:
        NodeChild testHtmlAsNode = new XmlSlurper(HTML_FRAGMENT_PARSER).parseText(testHTML)
        testHtmlAsNode.childNodes().size() == 1
        def chartContainerNode = testHtmlAsNode.childNodes().getAt(0)
        chartContainerNode.childNodes().size() == 3
        def treeMap = chartContainerNode.childNodes().getAt(0)
        def tooltipTreemap = chartContainerNode.childNodes().getAt(1)
        def zeroWeigthSpan = chartContainerNode.childNodes().getAt(2)
        treeMap.attributes['id'] == identifier
        tooltipTreemap.attributes['id'] == "tooltipTreemap"
        zeroWeigthSpan.attributes['id'] == "zeroWeightSpan"
    }

    def "HTML provided by taglib iteratec:scheduleChart returns container with correct identifier"() {
        given:
        String identifier = "ChartIdentifier"

        when:
        String testHTML = applyTemplate("""<iteratec:scheduleChart chartIdentifier ="${identifier}"/>""")

        then:
        NodeChild testHtmlAsNode = new XmlSlurper(HTML_FRAGMENT_PARSER).parseText(testHTML)
        def formContainer = testHtmlAsNode.childNodes().getAt(0)
        def chartSVGContainerNode = testHtmlAsNode.childNodes().getAt(2)

        testHtmlAsNode.childNodes().size() == 3
        formContainer.attributes['id'] == "ScheduleChart" + identifier
        chartSVGContainerNode.attributes['id'] == "show-overused-queues" + identifier
    }
}

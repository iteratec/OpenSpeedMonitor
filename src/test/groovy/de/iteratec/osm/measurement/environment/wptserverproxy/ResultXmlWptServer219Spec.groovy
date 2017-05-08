package de.iteratec.osm.measurement.environment.wptserverproxy

import de.iteratec.osm.result.CachedView
import de.iteratec.osm.result.WptXmlResultVersion
import groovy.util.slurpersupport.GPathResult
import spock.lang.Specification

class ResultXmlWptServer219Spec extends Specification {
    private static WptResultXml resultXml

    void "setupSpec"() {
        GPathResult xmlResult = new XmlSlurper().parse(new File("src/test/resources/WptResultXmls/MULTISTEP_2Run.xml"))
        resultXml = new WptResultXml(xmlResult)
    }

    void "test correct version"() {
        expect:
        resultXml.version == WptXmlResultVersion.MULTISTEP
    }

    void "test getStepCount"() {
        expect:
        resultXml.getTestStepCount() == 2
    }

    void "test getEventName"() {
        expect:
        resultXml.getEventName(null, 0) == "beforeTest"
        resultXml.getEventName(null, 1) == "testExecution"
    }


    void "test getRunNumberOfMedianViewNode"() {
        expect:
        resultXml.getRunNumberOfMedianViewNode(CachedView.UNCACHED, 0) == 1
        resultXml.getRunNumberOfMedianViewNode(CachedView.CACHED, 0) == 2
    }
}

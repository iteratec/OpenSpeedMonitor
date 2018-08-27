package de.iteratec.osm.measurement.environment.wptserver

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

    void "correct version is recognized"() {
        expect:
        resultXml.version == WptXmlResultVersion.MULTISTEP
    }

    void "number of steps is correct"() {
        expect:
        resultXml.getTestStepCount() == 2
    }

    void "event name can get extracted"() {
        expect:
        resultXml.getEventName(null, 0) == "beforeTest"
        resultXml.getEventName(null, 1) == "testExecution"
    }


    void "run number of the median view is correct"() {
        expect:
        resultXml.getRunNumberOfMedianViewNode(CachedView.UNCACHED, 0) == 1
        resultXml.getRunNumberOfMedianViewNode(CachedView.CACHED, 0) == 2
    }
}

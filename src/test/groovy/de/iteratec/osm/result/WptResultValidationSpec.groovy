package de.iteratec.osm.result

import de.iteratec.osm.measurement.environment.wptserver.WptResultXml
import groovy.util.slurpersupport.GPathResult
import spock.lang.Specification

/**
 * @author nkuhn
 */
class WptResultValidationSpec extends Specification {

    void "WPT Result validation"() {
        setup: "given a result node from wpt"
        GPathResult xmlResult = new XmlSlurper().parseText("""
<firstView>
    <result>${wptStatus}</result>
    <TTFB>${ttfb}</TTFB>
    <render>${startRender}</render>
    <visualComplete>${visComplete}</visualComplete>
    <SpeedIndex>${speedIndex}</SpeedIndex>
    <docTime>${docComplete}</docTime>
    <fullyLoaded>${fullyLoadedTime}</fullyLoaded>
</firstView>
""")
        WptResultXml resultXml = new WptResultXml(xmlResult, 10, 60000)

        expect: "it gets validated to #valid"
        resultXml.isValidTestStep(xmlResult) == valid

        where:
        wptStatus   | ttfb  | startRender   | visComplete   | speedIndex    | docComplete   | fullyLoadedTime || valid
        0           | 0     | 0             | 0             | 0             | 0             | 0               || false
        0           | 100   | 0             | 0             | 0             | 0             | 0               || true
        0           | 0     | 300           | 0             | 0             | 0             | 0               || true
        0           | 0     | 0             | 2000          | 0             | 0             | 0               || true
        0           | 0     | 0             | 0             | 1500          | 0             | 0               || true
        0           | 0     | 0             | 0             | 0             | 2100          | 0               || true
        0           | 0     | 0             | 0             | 0             | 0             | 3400            || true
        0           | 100   | 300           | 2300          | 1500          | 2100          | 3400            || true
        200         | 100   | 300           | 2300          | 1500          | 2100          | 3400            || true
        99999       | 100   | 300           | 2300          | 1500          | 2100          | 3400            || true
        12999       | 100   | 300           | 2300          | 1500          | 2100          | 3400            || true
        400         | 100   | 300           | 2300          | 1500          | 2100          | 3400            || false
        500         | 100   | 300           | 2300          | 1500          | 2100          | 3400            || false
        701         | 100   | 300           | 2300          | 1500          | 2100          | 3400            || false
        99997       | 100   | 300           | 2300          | 1500          | 2100          | 3400            || false
        99996       | 100   | 300           | 2300          | 1500          | 2100          | 3400            || false
        99998       | 100   | 300           | 2300          | 1500          | 2100          | 3400            || false
    }

}

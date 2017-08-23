package de.iteratec.osm.util

import de.iteratec.osm.result.EventResult
import de.iteratec.osm.result.Measurand
import grails.buildtestdata.mixin.Build
import grails.test.mixin.Mock
import spock.lang.Specification
/**
 * See the API for {@link grails.test.mixin.services.ServiceUnitTestMixin} for usage instructions
 */
@Mock([EventResult])
@Build([EventResult])
class MeasurandSpec extends Specification {

    void "test normalizeValue for Measurand"() {
        setup: "determine measurand"
        Measurand measurand = Measurand.valueOf(measurandString)

        expect: "value is normalized for measurand"
        measurand.normalizeValue(inputValue) == expectedValue

        where:
        measurandString               || inputValue || expectedValue
        'LOAD_TIME'                   || 1000       || 1000
        'FIRST_BYTE'                  || 1000       || 1000
        'START_RENDER'                || 1000       || 1000
        'DOC_COMPLETE_TIME'           || 1000       || 1000
        'VISUALLY_COMPLETE'           || 1000       || 1000
        'DOM_TIME'                    || 1000       || 1000
        'FULLY_LOADED_TIME'           || 1000       || 1000
        'DOC_COMPLETE_REQUESTS'       || 1000       || 1000
        'FULLY_LOADED_REQUEST_COUNT'  || 1000       || 1000
        'DOC_COMPLETE_INCOMING_BYTES' || 1000       || 0.001
        'FULLY_LOADED_INCOMING_BYTES' || 1000       || 0.001
        'CS_BY_WPT_DOC_COMPLETE'      || 1000       || 1000
        'CS_BY_WPT_VISUALLY_COMPLETE' || 1000       || 1000
        'SPEED_INDEX'                 || 1000       || 1000
        'LOAD_TIME'                   || null       || null
        'FIRST_BYTE'                  || null       || null
        'START_RENDER'                || null       || null
        'DOC_COMPLETE_TIME'           || null       || null
        'VISUALLY_COMPLETE'           || null       || null
        'DOM_TIME'                    || null       || null
        'FULLY_LOADED_TIME'           || null       || null
        'DOC_COMPLETE_REQUESTS'       || null       || null
        'FULLY_LOADED_REQUEST_COUNT'  || null       || null
        'DOC_COMPLETE_INCOMING_BYTES' || null       || null
        'FULLY_LOADED_INCOMING_BYTES' || null       || null
        'CS_BY_WPT_DOC_COMPLETE'      || null       || null
        'CS_BY_WPT_VISUALLY_COMPLETE' || null       || null
        'SPEED_INDEX'                 || null       || null
        'LOAD_TIME'                   || 0          || 0
        'FIRST_BYTE'                  || 0          || 0
        'START_RENDER'                || 0          || 0
        'DOC_COMPLETE_TIME'           || 0          || 0
        'VISUALLY_COMPLETE'           || 0          || 0
        'DOM_TIME'                    || 0          || 0
        'FULLY_LOADED_TIME'           || 0          || 0
        'DOC_COMPLETE_REQUESTS'       || 0          || 0
        'FULLY_LOADED_REQUEST_COUNT'  || 0          || 0
        'DOC_COMPLETE_INCOMING_BYTES' || 0          || 0
        'FULLY_LOADED_INCOMING_BYTES' || 0          || 0
        'CS_BY_WPT_DOC_COMPLETE'      || 0          || 0
        'CS_BY_WPT_VISUALLY_COMPLETE' || 0          || 0
        'SPEED_INDEX'                 || 0          || 0
    }
}

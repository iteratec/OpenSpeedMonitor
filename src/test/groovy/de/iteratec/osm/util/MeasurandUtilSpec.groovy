package de.iteratec.osm.util


import de.iteratec.osm.result.EventResult
import de.iteratec.osm.result.Measurand
import de.iteratec.osm.result.MeasurandGroup
import grails.buildtestdata.mixin.Build
import grails.test.mixin.Mock
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.services.ServiceUnitTestMixin} for usage instructions
 */
@Mock([EventResult])
@Build([EventResult])
class MeasurandUtilSpec extends Specification {

    void "test getEventResultPropertyForCalculation for different measurands"(String eventResultFieldName, Integer expectedValue) {
        setup: "EventResult is initiated"
        Map<String, Integer> props = [:]
        props.put(eventResultFieldName, expectedValue)
        EventResult eventResult =  EventResult.build(props)
        Measurand measurand = Measurand.values().find{it.getEventResultField() == eventResultFieldName}

        expect: "correct attribute is returned"
        MeasurandUtil.getEventResultPropertyForCalculation(measurand,eventResult).getClass() == Double.class
        MeasurandUtil.getEventResultPropertyForCalculation(measurand,eventResult) == expectedValue

        where:
        eventResultFieldName               || expectedValue
        'loadTimeInMillisecs'              || 1000
        'firstByteInMillisecs'             || 1000
        'startRenderInMillisecs'           || 1000
        'docCompleteTimeInMillisecs'       || 1000
        'visuallyCompleteInMillisecs'      || 1000
        'domTimeInMillisecs'               || 1000
        'fullyLoadedTimeInMillisecs'       || 1000
        'docCompleteRequests'              || 1000
        'fullyLoadedRequestCount'          || 1000
        'docCompleteIncomingBytes'         || 1000
        'fullyLoadedIncomingBytes'         || 1000
        'csByWptVisuallyCompleteInPercent' || 1000
        'csByWptDocCompleteInPercent'      || 1000
        'speedIndex'                       || 1000
    }

    void "test normalizeValue for String"() {
        expect: "value is normalized for measurand string"
        MeasurandUtil.normalizeValue(inputValue, measurandString) == expectedValue

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
        null                          || 1000       || 1000
        null                          || null       || null
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
    }

    void "test normalizeValue for Measurand"() {
        setup: "determine measurand"
        Measurand measurand = Measurand.valueOf(measurandString)

        expect: "value is normalized for measurand"
        MeasurandUtil.normalizeValue(inputValue, measurand) == expectedValue

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
    }

    void "test all measurand are found"() {
        setup: " determine group"
        MeasurandGroup measurandGroup = MeasurandGroup.valueOf(measurandGroupString)

        expect: "all measurands are found for set group"
        MeasurandUtil.getAllMeasurandsByMeasurandGroup()[measurandGroup] == Measurand.values().findAll {it.getMeasurandGroup() == measurandGroup}

        where:
        measurandGroupString | _
        'LOAD_TIMES'         | _
        'REQUEST_COUNTS'     | _
        'REQUEST_SIZES'      | _
        'PERCENTAGES'        | _
        'UNDEFINED'          | _
    }
}

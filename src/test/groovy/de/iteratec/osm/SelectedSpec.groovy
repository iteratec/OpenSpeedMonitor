package de.iteratec.osm

import de.iteratec.osm.result.CachedView
import de.iteratec.osm.result.EventResult
import de.iteratec.osm.result.Measurand
import de.iteratec.osm.result.Selected
import de.iteratec.osm.result.SelectedType
import de.iteratec.osm.result.UserTiming
import de.iteratec.osm.result.UserTimingType
import grails.buildtestdata.mixin.Build
import grails.test.mixin.Mock
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import spock.lang.*

/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
@TestMixin(GrailsUnitTestMixin)
@Mock([EventResult, UserTiming])
@Build([EventResult, UserTiming])
class SelectedSpec extends Specification {
    CachedView  cachedView = CachedView.UNCACHED

    void "test if constructor works as intended for edge cases"(){
        when: "selected is constructed for optionValue"
        Selected selected = new Selected(optionValue, cachedView)

        then: "selectedType is as expected"
        selected.selectedType == expectedSelectedType

        where:
        optionValue                                                                  | expectedSelectedType
        null                                                                         | SelectedType.USERTIMING_MEASURE
        ""                                                                           | SelectedType.USERTIMING_MEASURE
        "duck"                                                                       | SelectedType.USERTIMING_MEASURE
        "Donald Duck"                                                                | SelectedType.USERTIMING_MEASURE
        SelectedType.USERTIMING_MARK.optionPrefix                                    | SelectedType.USERTIMING_MEASURE
        SelectedType.USERTIMING_MEASURE.optionPrefix                                 | SelectedType.USERTIMING_MEASURE
        SelectedType.USERTIMING_MEASURE.optionPrefix + "name"                        | SelectedType.USERTIMING_MEASURE
        SelectedType.USERTIMING_MEASURE.optionPrefix + Measurand.DOM_TIME.toString() | SelectedType.USERTIMING_MEASURE
        SelectedType.USERTIMING_MARK.optionPrefix + "name"                           | SelectedType.USERTIMING_MARK
        SelectedType.USERTIMING_MARK.optionPrefix + Measurand.DOM_TIME.toString()    | SelectedType.USERTIMING_MARK
        Measurand.DOM_TIME.toString()                                                | SelectedType.MEASURAND
    }

    void "test if constructor works as intended for all measurands"(){
        when: " all measurands are converted"
        List<Selected> toTest = Measurand.values().collect {new Selected(it.toString(), cachedView)}

        then: " each selected has measurand as its type"
        toTest.each { it.selectedType == SelectedType.MEASURAND}
    }

    void "test normalizeValues"() {
        setup: "create selected"
        Selected selected = new Selected(optionValue, cachedView)

        expect: "value is normalized"
        selected.normalizeValue(inputValue) == expectedValue

        where:
        optionValue                   | inputValue | expectedValue
        'LOAD_TIME'                   | 1000       | 1000
        'FIRST_BYTE'                  | 1000       | 1000
        'START_RENDER'                | 1000       | 1000
        'DOC_COMPLETE_TIME'           | 1000       | 1000
        'VISUALLY_COMPLETE'           | 1000       | 1000
        'DOM_TIME'                    | 1000       | 1000
        'FULLY_LOADED_TIME'           | 1000       | 1000
        'DOC_COMPLETE_REQUESTS'       | 1000       | 1000
        'FULLY_LOADED_REQUEST_COUNT'  | 1000       | 1000
        'DOC_COMPLETE_INCOMING_BYTES' | 1000       | 0.001
        'FULLY_LOADED_INCOMING_BYTES' | 1000       | 0.001
        'CS_BY_WPT_DOC_COMPLETE'      | 1000       | 1000
        'CS_BY_WPT_VISUALLY_COMPLETE' | 1000       | 1000
        'SPEED_INDEX'                 | 1000       | 1000
        'LOAD_TIME'                   | null       | null
        'FIRST_BYTE'                  | null       | null
        'START_RENDER'                | null       | null
        'DOC_COMPLETE_TIME'           | null       | null
        'VISUALLY_COMPLETE'           | null       | null
        'DOM_TIME'                    | null       | null
        'FULLY_LOADED_TIME'           | null       | null
        'DOC_COMPLETE_REQUESTS'       | null       | null
        'FULLY_LOADED_REQUEST_COUNT'  | null       | null
        'DOC_COMPLETE_INCOMING_BYTES' | null       | null
        'FULLY_LOADED_INCOMING_BYTES' | null       | null
        'CS_BY_WPT_DOC_COMPLETE'      | null       | null
        'CS_BY_WPT_VISUALLY_COMPLETE' | null       | null
        'SPEED_INDEX'                 | null       | null
        'LOAD_TIME'                   | 0          | 0
        'FIRST_BYTE'                  | 0          | 0
        'START_RENDER'                | 0          | 0
        'DOC_COMPLETE_TIME'           | 0          | 0
        'VISUALLY_COMPLETE'           | 0          | 0
        'DOM_TIME'                    | 0          | 0
        'FULLY_LOADED_TIME'           | 0          | 0
        'DOC_COMPLETE_REQUESTS'       | 0          | 0
        'FULLY_LOADED_REQUEST_COUNT'  | 0          | 0
        'DOC_COMPLETE_INCOMING_BYTES' | 0          | 0
        'FULLY_LOADED_INCOMING_BYTES' | 0          | 0
        'CS_BY_WPT_DOC_COMPLETE'      | 0          | 0
        'CS_BY_WPT_VISUALLY_COMPLETE' | 0          | 0
        'SPEED_INDEX'                 | 0          | 0
        '_UTME_name'                  | 1000       | 1000
        '_UTMK_name'                  | 1000       | 1000
        ''                            | 1000       | 1000
        null                          | 1000       | 1000
    }

    void "test get value from EventResult"(){
        given: "testee is intiated"
        EventResult testee = createTestee(1000, 2000, 3000)

        when: "selected is build"
        Selected selected = new Selected(name, cachedView)

        then: "normalized value is as expected"
        selected.getNormalizedValueFrom(testee) == expectedResult

        where:
        name                                                           | expectedResult
        null                                                           | null
        ""                                                             | null
        "duck"                                                         | null
        "donald duck"                                                  | null
        SelectedType.USERTIMING_MARK.optionPrefix.toLowerCase()+"mark" | null
        SelectedType.USERTIMING_MARK.optionPrefix + "mark"             | 1000
        SelectedType.USERTIMING_MARK.optionPrefix + "measure"          | null
        SelectedType.USERTIMING_MEASURE.optionPrefix + "mark"          | null
        SelectedType.USERTIMING_MEASURE.optionPrefix + "measure"       | 2000
        Measurand.DOC_COMPLETE_TIME.toString()                         | 3000
    }

    private EventResult createTestee(Double valueMark, Double valueMeasure, Integer docCompleteTime){
        List<UserTiming> userTimings = []
        userTimings.add(
                UserTiming.build(
                        name: "mark",
                        startTime: valueMark,
                        type: UserTimingType.MARK,
                )
        )
        userTimings.add(
                UserTiming.build(
                        name: "measure",
                        duration: valueMeasure,
                        type: UserTimingType.MEASURE,
                )
        )

        return EventResult.build(
                cachedView: cachedView,
                docCompleteTimeInMillisecs: docCompleteTime,
                userTimings: userTimings
        )
    }
}
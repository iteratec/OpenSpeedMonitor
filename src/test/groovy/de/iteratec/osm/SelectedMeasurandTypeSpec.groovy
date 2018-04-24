package de.iteratec.osm

import de.iteratec.osm.result.EventResult
import de.iteratec.osm.result.Measurand
import de.iteratec.osm.result.SelectedMeasurandType
import de.iteratec.osm.result.UserTiming
import de.iteratec.osm.result.UserTimingType
import grails.buildtestdata.BuildDataTest
import grails.buildtestdata.mixin.Build
import spock.lang.*

@Build([EventResult, UserTiming])
class SelectedMeasurandTypeSpec extends Specification implements BuildDataTest {
    void setupSpec() {
        mockDomains(EventResult, UserTiming)
    }

    void "test getValue for Measurand"() {
        setup: "EventResult is initiated"
        EventResult eventResult = EventResult.build((eventResultFieldName): expectedValue)
        String measurand = Measurand.values().find { it.getEventResultField() == eventResultFieldName }.toString()

        expect: "correct attribute is returned"
        if (expectedValue) {
            SelectedMeasurandType.MEASURAND.getValue(eventResult, measurand).getClass() == Double.class
        }
        SelectedMeasurandType.MEASURAND.getValue(eventResult, measurand) == expectedValue

        where:
        eventResultFieldName               | expectedValue
        'firstByteInMillisecs'             | 1000
        'startRenderInMillisecs'           | 1000
        'docCompleteTimeInMillisecs'       | 1000
        'visuallyCompleteInMillisecs'      | 1000
        'domTimeInMillisecs'               | 1000
        'fullyLoadedTimeInMillisecs'       | 1000
        'docCompleteRequests'              | 1000
        'fullyLoadedRequestCount'          | 1000
        'docCompleteIncomingBytes'         | 1000
        'fullyLoadedIncomingBytes'         | 1000
        'csByWptVisuallyCompleteInPercent' | 1000
        'csByWptDocCompleteInPercent'      | 1000
        'speedIndex'                       | 1000
        'firstByteInMillisecs'             | 0
        'startRenderInMillisecs'           | 0
        'docCompleteTimeInMillisecs'       | 0
        'visuallyCompleteInMillisecs'      | 0
        'domTimeInMillisecs'               | 0
        'fullyLoadedTimeInMillisecs'       | 0
        'docCompleteRequests'              | 0
        'fullyLoadedRequestCount'          | 0
        'docCompleteIncomingBytes'         | 0
        'fullyLoadedIncomingBytes'         | 0
        'csByWptVisuallyCompleteInPercent' | 0
        'csByWptDocCompleteInPercent'      | 0
        'speedIndex'                       | 0
        'speedIndex'                       | null
    }

    void "test getValue for UserTiming"() {
        setup: "EventResult is initiated"
        Double startTime = type == UserTimingType.MARK ? expectedValue : 10
        Double duraction = type == UserTimingType.MEASURE ? expectedValue : null
        List<UserTiming> userTimings = [UserTiming.build(name: name, type: type, startTime: startTime, duration: duraction),
                                        UserTiming.build(name: 'someMark', type: UserTimingType.MARK, startTime: 2000, duration: null),
                                        UserTiming.build(name: 'someMeasure', type: UserTimingType.MEASURE, startTime: 20, duration: 4000)]
        EventResult eventResult = EventResult.build(userTimings: userTimings)

        expect: "correct attribute is returned"
        if (expectedValue) {
            type.selectedMeasurandType.getValue(eventResult, name).getClass() == Double.class
        }
        type.selectedMeasurandType.getValue(eventResult, name) == expectedValue

        where:
        name                | expectedValue | type
        'userTimingMark'    | 1000          | UserTimingType.MARK
        'userTimingMeasure' | 1000          | UserTimingType.MEASURE
    }
}
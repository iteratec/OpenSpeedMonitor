package de.iteratec.osm.result

import grails.buildtestdata.BuildDataTest
import grails.buildtestdata.mixin.Build
import grails.test.mixin.Mock
import spock.lang.Specification

@Mock([UserTiming])
@Build([UserTiming])
class UserTimingSpec extends Specification implements BuildDataTest {


    void "test constraints for duration"(duration, userTimingType, expectedResult) {
        when:"userTiming is created"
        UserTiming testee = UserTiming.buildWithoutSave(duration: duration, type: userTimingType)

        then:"validation is as expected"
        testee.validate() == expectedResult

        where:
        duration | userTimingType         | expectedResult
        0.0      | UserTimingType.MARK    | false
        3.14     | UserTimingType.MARK    | false
        null     | UserTimingType.MARK    | true
        0.0      | UserTimingType.MEASURE | true
        3.14     | UserTimingType.MEASURE | true
        null     | UserTimingType.MEASURE | false
    }
}

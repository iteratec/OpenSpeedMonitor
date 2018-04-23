package de.iteratec.osm.csi

import grails.buildtestdata.BuildDataTest
import grails.buildtestdata.mixin.Build
import grails.test.mixin.Mock
import spock.lang.Specification

@Mock([CsiDay])
@Build([CsiDay])
class CsiDaySpec extends Specification implements BuildDataTest {

    void setup() {
    }

    void "test set a new weight for hour"() {
        given: "a new weight"
        Double newWeight = 12.0
        Double originalWeight = 5
        CsiDay dayToTest = CsiDay.build(hour3Weight: originalWeight)

        when: "the new weight is set"
        dayToTest.setHourWeight(3, newWeight)

        then: "CsiDay has the correct weight"
        dayToTest.hour3Weight == newWeight
    }

    void "test get weight for hour"() {
        given: "a CsiDay with a fixed weight"
        Double weight = 5
        CsiDay dayToTest = CsiDay.build(hour1Weight: weight)

        when: "getting the weight"
        Double weightForHour1 = dayToTest.getHourWeight(1)

        then: "the hour has the correct weight"
        weightForHour1 == weight
    }

    void "test copyDay creates a valid copy"() {
        given: "a CsiDay"
        CsiDay dayToTest = CsiDay.build()

        when: "copying the CsiDay"
        CsiDay copy = CsiDay.copyDay(dayToTest)

        then: "the copy has the same properties and is valid"
        copy.properties == dayToTest.properties
        copy.validate()
    }

    void "changing weight in copy does not effect source day"() {
        given: "a CsiDay and a copy of it"
        Double newWeight = 12.0
        Double originalWeight = 5
        CsiDay dayToTest = CsiDay.build(hour3Weight: originalWeight)
        CsiDay copy = CsiDay.copyDay(dayToTest)

        when: "changing the copy"
        copy.setHourWeight(3, newWeight)

        then: "the original CsiDay is unchanged"
        copy.getHourWeight(3) == newWeight
        dayToTest.getHourWeight(3) == originalWeight
    }
}

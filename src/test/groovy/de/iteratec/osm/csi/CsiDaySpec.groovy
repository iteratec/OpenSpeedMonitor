package de.iteratec.osm.csi

import grails.test.mixin.Mock
import spock.lang.Specification

@Mock([CsiDay])
class CsiDaySpec extends Specification {

    CsiDay dayToTest

    void "setup"() {
        dayToTest = new CsiDay()
        dayToTest.with {
            hour0Weight = 0
            hour1Weight = 0.1
            hour2Weight = 0.2
            hour3Weight = 0.3
            hour4Weight = 0.4
            hour5Weight = 0.5
            hour6Weight = 0.6
            hour7Weight = 0.7
            hour8Weight = 0.8
            hour9Weight = 0.9
            hour10Weight = 1.0
            hour11Weight = 1.1
            hour12Weight = 1.2
            hour13Weight = 1.3
            hour14Weight = 1.4
            hour15Weight = 1.5
            hour16Weight = 1.6
            hour17Weight = 1.7
            hour18Weight = 1.8
            hour19Weight = 1.9
            hour20Weight = 2.0
            hour21Weight = 2.1
            hour22Weight = 2.2
            hour23Weight = 2.3
        }
        dayToTest.save()
    }

    void "test set a new wight for hour"() {
        given:
        Double newWeight = 12.0
        assert dayToTest.hour3Weight != newWeight

        when:
        dayToTest.setHourWeight(3, newWeight)

        then:
        assert dayToTest.hour3Weight == newWeight
    }

    void "test get weight for hour"() {
        given:
        Double expectedWeight = 0.1

        when:
        Double weightForHour1 = dayToTest.getHourWeight(1)

        then:
        assert expectedWeight == weightForHour1
    }

    void "test copyDay creates a valid copy" () {
        when:
        CsiDay copy = CsiDay.copyDay(dayToTest)

        then:
        assert copy.properties == dayToTest.properties
        assert copy.validate()
    }

    void "test change weight in copy does not effect source day"() {
        given:
        CsiDay copy = CsiDay.copyDay(dayToTest)
        Double newWeight = 12.0

        when:
        copy.setHourWeight(3, newWeight)

        then:
        assert copy.getHourWeight(3) == newWeight
        assert dayToTest.getHourWeight(3) != newWeight
    }
}

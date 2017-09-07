package de.iteratec.osm.result

import grails.buildtestdata.mixin.Build
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.services.ServiceUnitTestMixin} for usage instructions
 */
@TestFor(ThresholdService)
@Mock([EventResult])
@Build([EventResult])
class ThresholdServiceSpec extends Specification {

    ThresholdService serviceUnderTest

    def setup() {
        serviceUnderTest = service
    }

    def cleanup() {
    }

    void "test something"() {
        expect:"fix me"
            true == false
    }
}

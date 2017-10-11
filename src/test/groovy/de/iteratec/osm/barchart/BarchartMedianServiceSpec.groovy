package de.iteratec.osm.barchart

import de.iteratec.osm.result.EventResult
import de.iteratec.osm.result.UserTiming
import de.iteratec.osm.result.UserTimingType
import grails.buildtestdata.mixin.Build
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.services.ServiceUnitTestMixin} for usage instructions
 */
@TestFor(BarchartMedianService)
@Build([EventResult, UserTimingType])
@Mock([EventResult, UserTimingType, UserTiming])
class BarchartMedianServiceSpec extends Specification {
    void "test something"() {
        expect:"fix me"
            true == false
    }
}

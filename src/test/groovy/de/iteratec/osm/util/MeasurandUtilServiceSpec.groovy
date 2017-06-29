package de.iteratec.osm.util

import grails.test.mixin.TestFor
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.services.ServiceUnitTestMixin} for usage instructions
 */
@TestFor(MeasurandUtil)
class MeasurandUtilServiceSpec extends Specification {

    def setup() {
        mockI18nService()
    }

    private mockI18nService() {
        I18nService i18nServiceStub = Stub() {
            msg(_, _) >> { String messageCode, String defaultMessage ->
                return defaultMessage
            }
        }
        service.i18nService = i18nServiceStub
    }

    def cleanup() {
    }

    void "test getDimensionalUnit for different measurands"() {
        expect: "dimensional unit correct for given measurand"
        service.getDimensionalUnit(measurand) == expectedUnit

        where:
        measurand                          || expectedUnit
        'loadTimeInMillisecs'              || 'ms'
        'firstByteInMillisecs'             || 'ms'
        'startRenderInMillisecs'           || 'ms'
        'docCompleteTimeInMillisecs'       || 'ms'
        'visuallyCompleteInMillisecs'      || 'ms'
        'domTimeInMillisecs'               || 'ms'
        'fullyLoadedTimeInMillisecs'       || 'ms'
        'docCompleteRequests'              || '#'
        'fullyLoadedRequestCount'          || '#'
        'docCompleteIncomingBytes'         || 'MB'
        'fullyLoadedIncomingBytes'         || 'MB'
        'csByWptVisuallyCompleteInPercent' || '%'
        'csByWptDocCompleteInPercent'      || '%'
        'speedIndex'                       || 'ms'
    }

    void "test getDimensionalUnit does not diffentiate between cached and uncached measurands"() {
        expect: "dimensial unit is equal for cached and uncached measurands"
        service.getDimensionalUnit(measurand) == expectedUnit

        where:
        measurand                     || expectedUnit
        'loadTimeInMillisecs'         || 'ms'
        'loadTimeInMillisecsCached'   || 'ms'
        'loadTimeInMillisecsUncached' || 'ms'
    }

    void "test getAxisLabel for different measurands"() {
        expect: "axisLabel correct for given measurand"
        service.getAxisLabel(measurand) == expectedUnit

        where:
        measurand                          || expectedUnit
        'loadTimeInMillisecs'              || 'Loading Time [ms]'
        'firstByteInMillisecs'             || 'Loading Time [ms]'
        'startRenderInMillisecs'           || 'Loading Time [ms]'
        'docCompleteTimeInMillisecs'       || 'Loading Time [ms]'
        'visuallyCompleteInMillisecs'      || 'Loading Time [ms]'
        'domTimeInMillisecs'               || 'Loading Time [ms]'
        'fullyLoadedTimeInMillisecs'       || 'Loading Time [ms]'
        'docCompleteRequests'              || 'Amount'
        'fullyLoadedRequestCount'          || 'Amount'
        'docCompleteIncomingBytes'         || 'Size [MB]'
        'fullyLoadedIncomingBytes'         || 'Size [MB]'
        'csByWptVisuallyCompleteInPercent' || 'Percent [%]'
        'csByWptDocCompleteInPercent'      || 'Percent [%]'
        'speedIndex'                       || 'Loading Time [ms]'
    }

    void "test getAxisLabel does not diffentiate between cached and uncached measurands"() {
        expect: "yAxisLabel is equal for cached and uncached measurands"
        service.getAxisLabel(measurand) == expectedUnit

        where:
        measurand                     || expectedUnit
        'loadTimeInMillisecs'         || 'Loading Time [ms]'
        'loadTimeInMillisecsCached'   || 'Loading Time [ms]'
        'loadTimeInMillisecsUncached' || 'Loading Time [ms]'
    }
}

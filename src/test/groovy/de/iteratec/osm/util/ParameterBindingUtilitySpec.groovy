package de.iteratec.osm.util

import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import spock.lang.Specification

class ParameterBindingUtilitySpec extends Specification {

    void "is null if value is unset"() {
        when:
        DateTime result = ParameterBindingUtility.parseDateTimeParameter(null, false)

        then:
        result == null
    }

    void "value simply returns joda datetime"(DateTime input, boolean fallbackToEndOfDay) {
        when:
        DateTime result = ParameterBindingUtility.parseDateTimeParameter(input, fallbackToEndOfDay)

        then:
        result == input

        where:
        input          | fallbackToEndOfDay
        DateTime.now() | true
        DateTime.now() | false
    }

    void "parsing ISO strings works"(String input, DateTime expected) {
        when:
        DateTime result = ParameterBindingUtility.parseDateTimeParameter(input, false)

        then:
        result == expected

        where:
        input                           | expected
        "2017-03-01T12:33:44.567Z"      | new DateTime(2017, 3, 1, 12, 33, 44, 567, DateTimeZone.UTC)
        "2017-03-01T12:33:44.567+02:00" | new DateTime(2017, 3, 1, 12, 33, 44, 567, DateTimeZone.forOffsetHours(2))
    }

    void "parsed ISO strings are not affected by fallbackToEndOfDay parameter"() {
        when:
        DateTime result = ParameterBindingUtility.parseDateTimeParameter("2017-03-01T12:33:44.567Z", true)

        then:
        result == new DateTime(2017, 3, 1, 12, 33, 44, 567, DateTimeZone.UTC)
    }

    void "german date format can be parsed as fallback with start of day time"() {
        when:
        DateTime result = ParameterBindingUtility.parseDateTimeParameter("03.11.2016", false)

        then:
        result == new DateTime(2016, 11, 3, 0, 0, 0, 0)
    }

    void "german date format can be parsed as fallback with end of day time"() {
        when:
        DateTime result = ParameterBindingUtility.parseDateTimeParameter("03.11.2016", true)

        then:
        result == new DateTime(2016, 11, 3, 23, 59, 59, 999)
    }

    void "wrong format throws exception"(input) {
        when:
        ParameterBindingUtility.parseDateTimeParameter(input, false)

        then:
        IllegalArgumentException e = thrown()

        where:
        input              | _
        "foobar"           | _
        "2017-01-02"       | _ // incomplete
        "12.30.2016"       | _ // american
        "02.06.1992 11:00" | _ // with time
    }
}

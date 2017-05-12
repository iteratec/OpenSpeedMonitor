package de.iteratec.osm.util

import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import spock.lang.Specification

class ParameterBindingUtilitySpec extends Specification {

    void "is null if value is unset"() {
        when: "datetime to parse is null"
        DateTime result = ParameterBindingUtility.parseDateTimeParameter(null, false)

        then: "null is returned"
        result == null
    }

    void "value simply returns joda datetime"(DateTime input, boolean fallbackToEndOfDay) {
        when: "datetime to parse already is a DateTime"
        DateTime result = ParameterBindingUtility.parseDateTimeParameter(input, fallbackToEndOfDay)

        then: "this DateTime is simply returned"
        result == input

        where:
        input          | fallbackToEndOfDay
        DateTime.now() | true
        DateTime.now() | false
    }

    void "parsing ISO strings works"(String input, DateTime expected) {
        when: "datetime String to parse is correctly ISO formatted"
        DateTime result = ParameterBindingUtility.parseDateTimeParameter(input, false)

        then: "a correctly parsed DateTime is returned"
        result == expected

        where:
        input                           | expected
        "2017-03-01T12:33:44.567Z"      | new DateTime(2017, 3, 1, 12, 33, 44, 567, DateTimeZone.UTC)
        "2017-03-01T12:33:44.567+02:00" | new DateTime(2017, 3, 1, 12, 33, 44, 567, DateTimeZone.forOffsetHours(2))
    }

    void "parsed ISO strings are not affected by fallbackToEndOfDay parameter"() {
        when: "parseDateTimeParameter is called with a correctly ISO formatted String and fallbackToEndOfDay=true"
        DateTime result = ParameterBindingUtility.parseDateTimeParameter("2017-03-01T12:33:44.567Z", true)

        then: "returned DateTimes time isn't set to end of day (23:59:59.999)"
        result == new DateTime(2017, 3, 1, 12, 33, 44, 567, DateTimeZone.UTC)
    }

    void "german date format can be parsed as fallback with start of day time"() {
        when: "datetime String to parse is 'german date time' formatted and fallbackToEndOfDay=false"
        DateTime result = ParameterBindingUtility.parseDateTimeParameter("03.11.2016", false)

        then: "returned DateTimes time is set to start of day (00:00:00.000)"
        result == new DateTime(2016, 11, 3, 0, 0, 0, 0)
    }

    void "german date format can be parsed as fallback with end of day time"() {
        when: "datetime String to parse is 'german date time' formatted and fallbackToEndOfDay=true"
        DateTime result = ParameterBindingUtility.parseDateTimeParameter("03.11.2016", true)

        then: "returned DateTimes time is set to end of day (23:59:59.999)"
        result == new DateTime(2016, 11, 3, 23, 59, 59, 999)
    }

    void "wrong format throws exception"(input) {
        when: "datetime String to parse is of an invalid format"
        ParameterBindingUtility.parseDateTimeParameter(input, false)

        then: "an IllegalArgumentException is thrown"
        IllegalArgumentException e = thrown()

        where:
        input              | _
        "foobar"           | _
        "2017-01-02"       | _ // incomplete
        "12.30.2016"       | _ // american
        "02.06.1992 11:00" | _ // with time
    }
}

package de.iteratec.osm.util

import grails.test.mixin.TestFor
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.format.ISODateTimeFormat
import spock.lang.Specification

@TestFor(CronExpressionController)
class CronExpressionControllerSpec extends Specification {

    def setup() {
    }

    def cleanup() {
    }

    void "Null expression fails"() {
        when: "nextExecutionTime is called with 'null'"
        controller.nextExecutionTime(null)

        then: "response status is 400"
        response.status == 400
    }

    void "Empty expression fails"() {
        when: "nextExecutionTime is called with empty String"
        controller.nextExecutionTime("")

        then: "response status is 400"
        response.status == 400
    }

    void "Short expression fails"() {
        when: "nextExecutionTime is called with invalid quartz expression (too short)"
        controller.nextExecutionTime("0 *")

        then: "response status is 400"
        response.status == 400
    }

    void "Random text fails"() {
        when: "nextExecutionTime is called with invalid quartz expression (random text)"
        controller.nextExecutionTime("sadfo3 d8342 * %")

        then: "response status is 400"
        response.status == 400
    }

    void "Seconds-qualified expression fails"() {
        when: "nextExecutionTime is called with invalid quartz expression (seconds are specified as first part)"
        controller.nextExecutionTime("0 0 */2 * * ? *")

        then: "response status is 400"
        response.status == 400
    }

    void "Good expression gives new time in ISO format"() {
        when: "nextExecutionTime is called with a valid quartz expression"
        def now = new DateTime(DateTimeZone.UTC).plusHours(1).withMinuteOfHour(0).withSecondOfMinute(0)
        controller.nextExecutionTime("0 * * * ? *")

        then: "response status is 200 and response text is the time in ISO format"
        response.status == 200
        response.text == ISODateTimeFormat.dateTimeNoMillis().print(now)
    }
}

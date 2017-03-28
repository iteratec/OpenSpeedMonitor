package de.iteratec.osm.util

import grails.test.mixin.TestFor
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.format.ISODateTimeFormat
import spock.lang.Specification
/**
 * See the API for {@link grails.test.mixin.web.ControllerUnitTestMixin} for usage instructions
 */
@TestFor(CronExpressionController)
class CronExpressionControllerSpec extends Specification {

    def setup() {
    }

    def cleanup() {
    }

    void "Null expression fails"() {
        when:
        controller.nextExecutionTime(null)

        then:
        response.status == 400
    }

    void "Empty expression fails"() {
        when:
        controller.nextExecutionTime("")

        then:
        response.status == 400
    }

    void "Short expression fails"() {
        when:
        controller.nextExecutionTime("0 *")

        then:
        response.status == 400
    }

    void "Random text fails"() {
        when:
        controller.nextExecutionTime("sadfo3 d8342 * %")

        then:
        response.status == 400
    }

    void "Seconds-qualified expression fails"() {
        when:
        controller.nextExecutionTime("0 0 */2 * * ? *")

        then:
        response.status == 400
    }

    void "Good expression gives new time in ISO format"() {
        when:
        def now = new DateTime(DateTimeZone.UTC).plusHours(1).withMinuteOfHour(0).withSecondOfMinute(0)
        controller.nextExecutionTime("0 * * * ? *")

        then:

        response.text == ISODateTimeFormat.dateTimeNoMillis().print(now)
    }
}

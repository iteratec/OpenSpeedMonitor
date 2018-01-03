package de.iteratec.osm.result

import de.iteratec.osm.measurement.schedule.Job
import grails.buildtestdata.mixin.Build
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import spock.lang.Specification
import spock.lang.Unroll

/**
 * See the API for {@link grails.test.mixin.domain.DomainClassUnitTestMixin} for usage instructions
 */
@TestFor(Threshold)
@Mock([Threshold, Job, MeasuredEvent])
@Build([Job, MeasuredEvent, Threshold])
@Unroll
class ThresholdSpec extends Specification {

    def setup() {
    }

    def cleanup() {
    }

    void "test duplications for measurands in measured events of a job"() {
        when:
        Job job1 = Job.build()
        MeasuredEvent measuredEvent = MeasuredEvent.build()
        Threshold threshold1 = new Threshold(job: job1, measuredEvent: measuredEvent, measurand: measurand1, upperBoundary: 100, lowerBoundary: 10)
        threshold1.save(flush: true)
        Threshold threshold2 = new Threshold(job: job1, measuredEvent: measuredEvent, measurand: measurand2, upperBoundary: 100, lowerBoundary: 10)

        then:
        threshold1.validate()
        threshold2.validate() == shouldBeValid

        where:
        shouldBeValid | measurand1                            | measurand2
        false         | Measurand.DOC_COMPLETE_INCOMING_BYTES | Measurand.DOC_COMPLETE_INCOMING_BYTES
        true          | Measurand.DOC_COMPLETE_INCOMING_BYTES | Measurand.DOC_COMPLETE_TIME
    }

    void "test constraints for upperBoundary and lowerBoundary"() {
        when:
        Threshold threshold = Threshold.build()
        threshold.upperBoundary = upperBoundary
        threshold.lowerBoundary = lowerBoundary

        then:
        (threshold.upperBoundary > threshold.lowerBoundary) == shouldBeValid

        where:
        shouldBeValid | upperBoundary   | lowerBoundary
        false         | 10              | 100
        true          | 100             | 10
    }
}

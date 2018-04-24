package de.iteratec.osm.result

import de.iteratec.osm.measurement.schedule.Job
import grails.buildtestdata.BuildDataTest
import grails.buildtestdata.mixin.Build
import spock.lang.Specification
import spock.lang.Unroll

@Build([Job, MeasuredEvent, Threshold])
@Unroll
class ThresholdSpec extends Specification implements BuildDataTest {

    def setup() {
    }

    void setupSpec() {
        mockDomains(Threshold, Job, MeasuredEvent)
    }

    def cleanup() {
    }

    void "test duplications for measurands in measured events of a job"() {
        given: "a threshold with measurand #measurand1"
        Job job1 = Job.build()
        MeasuredEvent measuredEvent = MeasuredEvent.build()

        Threshold threshold1 = Threshold.build(
                job: job1,
                measurand: measurand1,
                measuredEvent: measuredEvent,
                upperBoundary: 100,
                lowerBoundary: 10)
        threshold1.save(flush: true)

        when: "another threshold gets created with measurand #measurand2"
        Threshold threshold2 = new Threshold(
                job: job1,
                measurand: measurand2,
                measuredEvent: measuredEvent,
                upperBoundary: 100,
                lowerBoundary: 10)

        then: "threshold creation succeeds: #shouldBeValid"
        threshold2.validate() == shouldBeValid

        where: "the thresholds have the same/different measurands"
        shouldBeValid | measurand1                            | measurand2
        false         | Measurand.DOC_COMPLETE_INCOMING_BYTES | Measurand.DOC_COMPLETE_INCOMING_BYTES
        true          | Measurand.DOC_COMPLETE_INCOMING_BYTES | Measurand.DOC_COMPLETE_TIME
    }

    void "test constraints for upperBoundary and lowerBoundary"() {
        when: "a threshold gets created with upperBoundary: #upperBoundary and lowerBoundary: #lowerBoundary"
        Threshold threshold = new Threshold(
                job: Job.build(),
                measuredEvent: MeasuredEvent.build(),
                measurand: Measurand.DOC_COMPLETE_TIME,
                upperBoundary: upperBoundary,
                lowerBoundary: lowerBoundary
        )

        then: "threshold creation succeeds: #shouldBeValid"
        threshold.validate() == shouldBeValid

        where: "the upper boundary is higher/lower/equal than the lower boundary "
        shouldBeValid | upperBoundary   | lowerBoundary
        false         | 10              | 100
        false         | 100             | 100
        true          | 100             | 10
    }
}

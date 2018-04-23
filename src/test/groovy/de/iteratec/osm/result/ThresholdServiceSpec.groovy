package de.iteratec.osm.result

import de.iteratec.osm.api.dto.MeasurementResultDto
import de.iteratec.osm.measurement.schedule.Job
import grails.buildtestdata.BuildDataTest
import grails.buildtestdata.mixin.Build
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import spock.lang.Specification
import spock.lang.Unroll

/**
 * See the API for {@link grails.test.mixin.domain.DomainClassUnitTestMixin} for usage instructions
 */
@TestFor(ThresholdService)
@Mock([EventResult, Threshold])
@Build([EventResult, Threshold])
class ThresholdServiceSpec extends Specification implements BuildDataTest {

    void "test measured event result"() {
        given: "a event result and a threshold"
        EventResult eventResult = EventResult.build(docCompleteTimeInMillisecs: docCompleteTimeInMillisecs)
        Threshold.build(
                measuredEvent: eventResult.measuredEvent,
                job: eventResult.jobResult.job,
                measurand: Measurand.DOC_COMPLETE_TIME,
                lowerBoundary: 10,
                upperBoundary: 100
        )

        when: "the event result gets checked whether it matches his thresholds"
        MeasurementResultDto measurementResultDto = service.checkEventResult(eventResult).first()

        then: "the measured value of the event result is good/ok/bad"
        measurementResultDto.evaluatedResult == expectedResult

        where: "the measured value has values that are good/ok/bad"
        expectedResult                   | docCompleteTimeInMillisecs
        ThresholdResult.GOOD.getResult() | 0
        ThresholdResult.GOOD.getResult() | 9
        ThresholdResult.OK.getResult()   | 10
        ThresholdResult.OK.getResult()   | 59
        ThresholdResult.OK.getResult()   | 100
        ThresholdResult.BAD.getResult()  | 101
        ThresholdResult.BAD.getResult()  | 3213
    }

}

package de.iteratec.osm.result

import de.iteratec.osm.api.dto.MeasurementResultDto
import grails.transaction.Transactional

@Transactional
class ThresholdService {

    /**
     *
     * @param eventResults
     * @return
     */
    List<MeasurementResultDto> checkResults(List<EventResult> eventResults) {
        List<MeasurementResultDto> results = []
        eventResults.each {
            results.add(checkEventResult(it))
        }
        return results
    }

    /**
     *
     * @param eventResult
     * @return
     */
    List<MeasurementResultDto> checkEventResult(EventResult eventResult) {
        List<Threshold> thresholds = Threshold.findAllByMeasuredEventAndJob(eventResult.measuredEvent, eventResult.jobResult.job)
        return thresholds.collect {
            String evaluatedResult
            if (eventResult."$it.measurand.eventResultField" < it.lowerBoundary) {
                evaluatedResult = ThresholdResult.GOOD.getResult()
            } else if (eventResult."$it.measurand.eventResultField" > it.upperBoundary) {
                evaluatedResult = ThresholdResult.BAD.getResult()
            } else {
                evaluatedResult = ThresholdResult.OK.getResult()
            }
            new MeasurementResultDto(
                    evaluatedResult: evaluatedResult,
                    measuredEvent: eventResult.measuredEvent.name,
                    measuredValue: eventResult."$it.measurand.eventResultField",
                    measurand: it.measurand)
        }
    }
}
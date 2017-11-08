package de.iteratec.osm.result

import de.iteratec.osm.api.dto.MeasurementResultDto
import de.iteratec.osm.measurement.schedule.Job
import grails.transaction.Transactional

@Transactional
class ThresholdService {

    /**
     * Checks all even results.
     *
     * @param eventResults The Event results
     * @return A list with the threshold results of the event results.
     */
    List checkResults(List<EventResult> eventResults) {
        return eventResults.collect() {
            checkEventResult(it)
        }
    }

    /**
     * Checks whether an event results passes his threshold tests.
     *
     * @param eventResult
     * @return A list with the threshold results of an event result.
     */
    List<MeasurementResultDto> checkEventResult(EventResult eventResult) {
        List<Threshold> thresholds = Threshold.findAllByMeasuredEventAndJob(eventResult.measuredEvent, eventResult.jobResult.job)

        return thresholds.collect {

            Integer missedValue

            String evaluatedResult
            if (eventResult."$it.measurand.eventResultField" < it.lowerBoundary) {
                evaluatedResult = ThresholdResult.GOOD.getResult()
                missedValue = null
            } else if (eventResult."$it.measurand.eventResultField" > it.upperBoundary) {
                evaluatedResult = ThresholdResult.BAD.getResult()
                missedValue = it.upperBoundary
            } else {
                evaluatedResult = ThresholdResult.OK.getResult()
                missedValue = it.lowerBoundary
            }

            new MeasurementResultDto(
                    evaluatedResult: evaluatedResult,
                    measuredEvent: eventResult.measuredEvent.name,
                    measuredValue: eventResult."$it.measurand.eventResultField",
                    missedValue: missedValue,
                    measurand: it.measurand)
        }
    }

    /**
     *
     * @param job
     * @return
     */
    List<Threshold> getThresholdsForJob(Job job){
        return Threshold.findAllByJob(job)
    }
}
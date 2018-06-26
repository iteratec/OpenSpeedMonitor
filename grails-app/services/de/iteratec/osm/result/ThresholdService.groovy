package de.iteratec.osm.result

import de.iteratec.osm.api.dto.MeasurementResultDto
import de.iteratec.osm.measurement.schedule.Job
import grails.gorm.transactions.Transactional

@Transactional
class ThresholdService {

    /**
     * Checks all event results.
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
                    lowerBoundary: it.lowerBoundary,
                    upperBoundary: it.upperBoundary,
                    measurand: it.measurand,
                    unit: it.measurand.measurandGroup.unit.label
            )
        }
    }

    /**
     * Collects all thresholds for a job.
     *
     * @param job The selected job.
     * @return All thresholds for the given job.
     */
    List<Threshold> getThresholdsForJob(Job job){
        return Threshold.findAllByJob(job)
    }
}
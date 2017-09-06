package de.iteratec.osm.result

import de.iteratec.osm.api.dto.MeasurementResultDto
import de.iteratec.osm.report.chart.Event
import grails.transaction.Transactional

@Transactional
class ThresholdService {

    /**
     *
     * @param eventResults
     * @return
     */
    List<MeasurementResultDto> checkResults(List<EventResult> eventResults){
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
    List<MeasurementResultDto> checkEventResult(EventResult eventResult){
        List<MeasurementResultDto> results = []
        List<Threshold> thresholds = Threshold.findAllByMeasuredEventAndJob(eventResult.measuredEvent, eventResult.jobResult.job)
        thresholds.each {
            if (eventResult."$it.measurand.eventResultField" < it.lowerBoundary) {
                results.add(new MeasurementResultDto(eventResult.measuredEvent, it.measurand, (Integer)eventResult."$it.measurand.eventResultField", "good"))
            } else if (eventResult."$it.measurand.eventResultField" > it.upperBoundary) {
                results.add(new MeasurementResultDto(eventResult.measuredEvent, it.measurand, (Integer)eventResult."$it.measurand.eventResultField", "bad"))
            } else {
                results.add(new MeasurementResultDto(eventResult.measuredEvent, it.measurand, (Integer)eventResult."$it.measurand.eventResultField", "ok"))
            }
        }

        return results
    }
}
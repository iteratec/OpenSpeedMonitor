package de.iteratec.osm.result

import de.iteratec.osm.report.chart.Event
import grails.transaction.Transactional

@Transactional
class ThresholdService {

    /**
     *
     * @param eventResults
     * @return
     */
    String checkMeasurement(List<EventResult> eventResults){
        eventResults.each {
            Threshold threshold = Threshold.findByMeasuredEvent(it.measuredEvent)
            if(threshold) {
                if (it.docCompleteTimeInMillisecs < threshold.lowerBoundary) {
                    return "good"
                } else if (it.docCompleteTimeInMillisecs > threshold.upperBoundary) {
                    return "bad"
                } else {
                    return "ok"
                }
            }
        }
    }
}

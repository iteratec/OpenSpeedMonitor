package de.iteratec.osm.api.dto

import de.iteratec.osm.result.Measurand
import de.iteratec.osm.result.MeasuredEvent

class MeasurementResultDto {
    String measuredEvent

    String measurand

    Integer measuredValue

    String evaluatedResult

    public MeasurementResultDto(MeasuredEvent measuredEvent, Measurand measurand, Integer measuredValue, String evaluatedResult){
        this.measuredEvent = measuredEvent.getName()
        this.measurand = measurand.name()
        this.measuredValue = measuredValue
        this.evaluatedResult = evaluatedResult
    }
}
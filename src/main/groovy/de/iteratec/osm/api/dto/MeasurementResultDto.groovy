package de.iteratec.osm.api.dto

import de.iteratec.osm.result.Measurand

class MeasurementResultDto {
    String measurand

    Integer measuredValue

    String evaluatedResult

    public MeasurementResultDto(Measurand measurand, Integer measuredValue, String evaluatedResult){
        this.measurand = measurand.name()
        this.measuredValue = measuredValue
        this.evaluatedResult = evaluatedResult
    }
}
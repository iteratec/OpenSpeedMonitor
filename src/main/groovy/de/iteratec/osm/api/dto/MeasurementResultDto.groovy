package de.iteratec.osm.api.dto

import de.iteratec.osm.result.Measurand
import de.iteratec.osm.result.MeasuredEvent

class MeasurementResultDto {
    String measuredEvent

    String measurand

    Integer measuredValue

    String evaluatedResult
}

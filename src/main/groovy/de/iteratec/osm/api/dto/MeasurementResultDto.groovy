package de.iteratec.osm.api.dto

/**
 * Transport class for the threshold results.
 */
class MeasurementResultDto {
    String measuredEvent
    String measurand
    String unit
    Integer measuredValue
    Integer lowerBoundary
    Integer upperBoundary
    String evaluatedResult
}
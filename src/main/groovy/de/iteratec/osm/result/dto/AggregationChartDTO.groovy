package de.iteratec.osm.result.dto

class AggregationChartDTO {
    List<AggregationChartSeriesDTO> series = []
    Map<String, List<String>> filterRules = [:].withDefault {[]}
    Map<String, String> i18nMap = [:]
    boolean hasComparativeData
}

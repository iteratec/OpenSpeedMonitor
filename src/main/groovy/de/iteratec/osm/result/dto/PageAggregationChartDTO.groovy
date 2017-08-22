package de.iteratec.osm.result.dto

class PageAggregationChartDTO {
    List<PageAggregationChartSeriesDTO> series = []
    Map<String, List<String>> filterRules = [:].withDefault {[]}
    Map<String, String> i18nMap = [:]
    boolean hasComparativeData
}

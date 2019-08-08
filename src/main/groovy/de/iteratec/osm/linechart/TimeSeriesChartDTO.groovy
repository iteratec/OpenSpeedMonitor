package de.iteratec.osm.linechart

class TimeSeriesChartDTO {
    Map<String, TimeSeries> series = [:]
    Map<String, List<String>> filterRules = [:].withDefault {[]}
    Map<String, String> i18nMap = [:]
}

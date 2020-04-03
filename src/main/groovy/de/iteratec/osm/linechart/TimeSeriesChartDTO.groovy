package de.iteratec.osm.linechart

class TimeSeriesChartDTO {
    List<TimeSeries> series = []
    Map<String, String> measurandGroups = [:]
    List<SummaryLabel> summaryLabels = []
}

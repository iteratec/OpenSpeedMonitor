package de.iteratec.osm.linechart

class TimeSeriesChartDTO {
    Map<String, List<TimeSeries>> series = [:]
    Map<String, String> measurandGroups = [:]
    List<SummaryLabel> summaryLabels = []
    int numberOfTimeSeries = 0
}

package de.iteratec.osm.distributionData

class DistributionChartDTO {
    Map<String, DistributionTrace> series = new HashMap<String, DistributionTrace>()
    Map<String, List<String>> filterRules = [:].withDefault {[]}
    String dimensionalUnit = ""
    Map<String, String> i18nMap = [:]
}

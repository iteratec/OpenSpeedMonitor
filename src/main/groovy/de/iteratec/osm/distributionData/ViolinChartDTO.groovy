package de.iteratec.osm.distributionData

class ViolinChartDTO {
    List<Violin> series = []
    Map<String, List<String>> filterRules = [:].withDefault {[]}
    String measurandGroup = ""
    String dimensionalUnit = ""
}

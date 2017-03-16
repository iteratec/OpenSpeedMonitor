package de.iteratec.osm.dimple

class BarchartDTO {
    List<BarchartSeries> series = []
    String groupingLabel = "Grouping"
    Map<String, List<String>> filterRules = [:].withDefault {[]}
    Map<String, String> i18nMap = [:]
}

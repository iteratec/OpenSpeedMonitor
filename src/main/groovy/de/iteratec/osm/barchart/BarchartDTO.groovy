package de.iteratec.osm.barchart

class BarchartDTO {
    List<BarchartSeries> series = []
    Map<String, List<String>> filterRules = [:].withDefault {[]}
    Map<String, String> i18nMap = [:]
}

package de.iteratec.osm.report.external

import de.iteratec.osm.report.chart.AggregationType

class GraphitePathCsiData {
    String prefix
    AggregationType aggregationType

    static belongsTo = [graphiteServer: GraphiteServer]

    static constraints = {
        prefix(matches: /([a-zA-Z0-9]+\.)+/, nullable: false, blank: false, maxSize: 255)
    }
}

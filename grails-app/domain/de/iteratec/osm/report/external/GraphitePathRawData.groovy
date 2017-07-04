package de.iteratec.osm.report.external

import de.iteratec.osm.report.chart.Measurand
import de.iteratec.osm.result.CachedView

class GraphitePathRawData {
    String prefix
    Measurand measurand
    CachedView cachedView

    static belongsTo = [graphiteServer: GraphiteServer]

    static constraints = {
        prefix(matches: /([a-zA-Z0-9]+\.)+/, nullable: false, blank: false, maxSize: 255)
    }
}

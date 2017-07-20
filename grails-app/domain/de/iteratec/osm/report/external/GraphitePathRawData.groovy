package de.iteratec.osm.report.external

import de.iteratec.osm.result.Measurand
import de.iteratec.osm.result.CachedView
import de.iteratec.osm.util.Constants

class GraphitePathRawData {
    String prefix
    Measurand measurand
    CachedView cachedView

    static belongsTo = [graphiteServer: GraphiteServer]

    static constraints = {
        importFrom(GraphitePathCsiData)
    }

    @Override
    String toString(){
        return "${prefix}[STATIC_PATH].${measurand}"+Constants.UNIQUE_STRING_DELIMITTER+"${cachedView}"
    }
}

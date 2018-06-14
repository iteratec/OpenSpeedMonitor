package de.iteratec.osm.result.dao

import de.iteratec.osm.report.chart.RepresentableWptResult
import groovy.transform.EqualsAndHashCode
/**
 * Created by mwg on 20.09.2017.
 */
@EqualsAndHashCode(excludes = ["projectedProperties"])
class EventResultProjection implements RepresentableWptResult{

    Long id

    Map projectedProperties = [:]

    def get(String property) {
        return projectedProperties.get(property)
    }
    String toString(){
        StringBuilder sb = new StringBuilder()
            .append("[${this.id}] ")
            .append(this.projectedProperties)
        return sb.toString()
    }
}

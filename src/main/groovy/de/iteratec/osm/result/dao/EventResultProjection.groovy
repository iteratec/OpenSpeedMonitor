package de.iteratec.osm.result.dao

import de.iteratec.osm.csi.Page
import de.iteratec.osm.measurement.schedule.JobGroup
import groovy.transform.EqualsAndHashCode

/**
 * Created by mwg on 20.09.2017.
 */
@EqualsAndHashCode(excludes = ["projectedProperties"])
class EventResultProjection {
    boolean isAggregation
    JobGroup jobGroup
    Page page
    Map projectedProperties = [:]

    def get(String property) {
        return projectedProperties.get(property)
    }
}

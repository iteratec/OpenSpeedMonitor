package de.iteratec.osm.result.dao

import de.iteratec.osm.result.SelectedMeasurand
import de.iteratec.osm.result.SelectedMeasurandType
import org.grails.datastore.mapping.query.Projections
import org.grails.datastore.mapping.query.Query

/**
 * Created by mwg on 20.09.2017.
 */
abstract class AbstractProjectionBuilder {
    List<String> projectedFields = []
    List<Query.Projection> projections = []

    void addGroupByProjection(String propertyName, String projectionName = null) {
        addProjection(Projections.groupProperty(propertyName), propertyName, projectionName)
    }

    void addPropertyProjection(String propertyName, String projectionName = null) {
        addProjection(Projections.property(propertyName), propertyName, projectionName)
    }

    void addAvgProjection(String propertyName, String projectionName = null) {
        addProjection(Projections.avg(propertyName), propertyName, projectionName)
    }


    private void addProjection(Query.Projection projection, String propertyName, String projectionName) {
        String projectedField = projectionName ?: propertyName
        if (!projectedFields.contains(projectedField)) {
            projections.add(projection)
            projectedFields.add(projectedField)
        }
    }


    List<Map> transformAggregations(def aggregations) {
        List<Map> result = []
        aggregations.each { aggregation ->
            Map transformed = [:]
            projectedFields.each {
                transformed.put(it, aggregation[projectedFields.indexOf(it)])
            }
            result.add(transformed)
        }
        return result
    }
}

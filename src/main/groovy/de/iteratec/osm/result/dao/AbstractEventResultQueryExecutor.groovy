package de.iteratec.osm.result.dao

import de.iteratec.osm.result.EventResult
import grails.gorm.DetachedCriteria
import org.grails.datastore.mapping.query.Query

/**
 * Created by mwg on 20.09.2017.
 */
abstract class AbstractEventResultQueryExecutor extends AbstractProjectionBuilder {
    DetachedCriteria query

    List<EventResultProjection> getResults(List<Query.Criterion> criterionList, List<Query.Projection> projectionList, List<String> projectionNames) {
        configureQuery(criterionList, projectionList, projectionNames)
        query.projections.addAll(projections)
        return createEventResultProjections(transformAggregations(query.list()))
    }

    void configureQuery(List<Query.Criterion> criterionList, List<Query.Projection> projectionList, List<String> projectionNames) {
        criterionList.each {
            query.add(it)
        }
        projections += projectionList
        projectedFields += projectionNames
    }

    void checkIfQueryIsThere() {
        if (!query) {
            query = new DetachedCriteria(EventResult)
        }
    }

    abstract List<EventResultProjection> createEventResultProjections(List<Map> aggregations)
}

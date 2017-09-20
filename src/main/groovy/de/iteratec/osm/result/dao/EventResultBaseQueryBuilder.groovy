package de.iteratec.osm.result.dao

import de.iteratec.osm.result.EventResult
import grails.gorm.DetachedCriteria
import org.grails.datastore.mapping.query.Query

/**
 * Created by mwg on 20.09.2017.
 */
class EventResultBaseQueryBuilder extends AbstractProjectionBuilder {
    DetachedCriteria criterionBuilder

    EventResultBaseQueryBuilder(int minValidLoadtime, int maxValidLoadtime) {
        criterionBuilder = new DetachedCriteria(EventResult)
        criterionBuilder.between('fullyLoadedTimeInMillisecs', minValidLoadtime, maxValidLoadtime)
    }

    void filterDateBetween(String propertyName, Date from, Date to) {
        if (from && to) {
            criterionBuilder.between(propertyName, from, to)
        }
    }

    void withFilterIn(String propertyName, List range, boolean groupBy) {
        if (range) {
            criterionBuilder.in(propertyName, range)
            if (groupBy) {
                addGroupByProjection(propertyName)
            }
        }
    }

    List<Query.Criterion> getFilters() {
        return criterionBuilder.criteria
    }
}

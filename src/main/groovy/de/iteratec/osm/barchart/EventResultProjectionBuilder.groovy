package de.iteratec.osm.barchart

import de.iteratec.osm.OsmConfigCacheService
import de.iteratec.osm.csi.Page
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.result.EventResult
import de.iteratec.osm.result.SelectedMeasurand
import de.iteratec.osm.result.SelectedMeasurandType
import grails.gorm.DetachedCriteria
import org.grails.datastore.mapping.query.Projections
import org.grails.datastore.mapping.query.Query
import org.springframework.beans.factory.annotation.Autowired

/**
 * Created by mwg on 31.08.2017.
 */
class EventResultProjectionBuilder {
    private static final String USER_TIMINGS_NAME = 'userTimings'

    DetachedCriteria query
    List<Query.Projection> projections
    List<String> projectedFields

    EventResultProjectionBuilder(Integer minValidLoadtime, Integer maxValidLoadtime) {
        query = new DetachedCriteria(EventResult)
        projections = []
        projectedFields = []
        query.between('fullyLoadedTimeInMillisecs', minValidLoadtime, maxValidLoadtime)
    }

    EventResultProjectionBuilder withJobResultDateBetween(Date from, Date to) {
        query.between('jobResultDate', from, to)
        return this
    }

    EventResultProjectionBuilder withJobGroupIn(List<JobGroup> jobGroups, boolean withProjection) {
        return withIn('jobGroup', jobGroups, withProjection)
    }

    EventResultProjectionBuilder withPageIn(List<Page> pages, boolean withProjection) {
        return withIn('page', pages, withProjection)
    }

    EventResultProjectionBuilder withIn(String propertyName, List range, boolean withProjection) {
        if (range) {
            query.in(propertyName, range)
            return withProjection ? addGroupByProjection(propertyName, null) : this
        }
        return this
    }

    EventResultProjectionBuilder withUserTiming(List<SelectedMeasurand> userTimings) {
        List<String> userTimingNames = userTimings.findAll {
            it.selectedType != SelectedMeasurandType.MEASURAND
        }.collect { it.getDatabaseRelevantName() }
        query.createAlias(USER_TIMINGS_NAME, USER_TIMINGS_NAME)
        query.in(USER_TIMINGS_NAME + '.name', userTimingNames)
        addGroupByProjection(USER_TIMINGS_NAME + '.name', 'name')
        addGroupByProjection(USER_TIMINGS_NAME + '.type', 'type')
        addAvgProjection(USER_TIMINGS_NAME + '.startTime', 'startTime')
        addAvgProjection(USER_TIMINGS_NAME + '.duration', 'duration')
        return this
    }

    EventResultProjectionBuilder withSelectedMeasurandProjection(List<SelectedMeasurand> selectedMeasurands) {
        if (selectedMeasurands.any { it.selectedType == SelectedMeasurandType.MEASURAND }) {
            selectedMeasurands.each {
                if (it.selectedType == SelectedMeasurandType.MEASURAND) {
                    addAvgProjection(it.getDatabaseRelevantName(), null)
                }
            }
        } else {
            withUserTiming(selectedMeasurands.findAll { it.selectedType != SelectedMeasurandType.MEASURAND })
        }
        return this
    }


    EventResultProjectionBuilder addGroupByProjection(String propertyName, String projectionName) {
        projections.add(Projections.groupProperty(propertyName))
        projectedFields.add(projectionName ?: propertyName)
        return this
    }

    EventResultProjectionBuilder addAvgProjection(String propertyName, String projectionName) {
        projections.add(Projections.avg(propertyName))
        projectedFields.add(projectionName ?: propertyName)
        return this
    }

    List<Map> getResults() {
        query.projections.addAll(projections)
        def aggregations = query.list()
        return transformAggregations(aggregations)
    }

    private List<Map> transformAggregations(def aggregations) {
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

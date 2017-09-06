package de.iteratec.osm.barchart

import de.iteratec.osm.csi.Page
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.result.EventResult
import de.iteratec.osm.result.SelectedMeasurand
import de.iteratec.osm.result.SelectedMeasurandType
import grails.gorm.DetachedCriteria
import org.grails.datastore.mapping.query.Projections
import org.grails.datastore.mapping.query.Query

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

    EventResultProjectionBuilder withJobGroupIn(List<JobGroup> jobGroups, boolean groupBy) {
        return withIn('jobGroup', jobGroups, groupBy)
    }

    EventResultProjectionBuilder withPageIn(List<Page> pages, boolean groupBy) {
        return withIn('page', pages, groupBy)
    }

    EventResultProjectionBuilder withIn(String propertyName, List range, boolean groupBy) {
        if (range) {
            query.in(propertyName, range)
            return groupBy ? addGroupByProjection(propertyName, null) : this
        }
        return this
    }

    EventResultProjectionBuilder withUserTiming(List<SelectedMeasurand> userTimings) {
        List<String> userTimingNames = userTimings.findAll {
            it.selectedType != SelectedMeasurandType.MEASURAND
        }.collect { it.getDatabaseRelevantName() }
        this.joinUserTimings()
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

    EventResultProjectionBuilder withSelectedMeasurandPropertyProjection(SelectedMeasurand selectedMeasurand, String projectionName) {
        String propertyName
        if (selectedMeasurand.selectedType == SelectedMeasurandType.MEASURAND) {
            propertyName = selectedMeasurand.getDatabaseRelevantName()
        } else {
            this.joinUserTimings()
            query.eq(USER_TIMINGS_NAME + '.name', selectedMeasurand.getDatabaseRelevantName())
            String relevantFieldName = selectedMeasurand.selectedType == SelectedMeasurandType.USERTIMING_MEASURE ? 'duration' : 'startTime'
            propertyName = USER_TIMINGS_NAME + '.' + relevantFieldName
            if(!projectionName){
                projectionName = relevantFieldName
            }
        }
        return addPropertyProjection(propertyName, projectionName)
    }

    private void joinUserTimings() {
        query.createAlias(USER_TIMINGS_NAME, USER_TIMINGS_NAME)
    }

    EventResultProjectionBuilder addGroupByProjection(String propertyName, String projectionName) {
        return addProjection(Projections.groupProperty(propertyName), propertyName, projectionName)
    }

    EventResultProjectionBuilder addAvgProjection(String propertyName, String projectionName) {
        return addProjection(Projections.avg(propertyName), propertyName, projectionName)
    }

    EventResultProjectionBuilder addPropertyProjection(String propertyName, String projectionName) {
       return addProjection(Projections.property(propertyName), propertyName, projectionName)
    }

    EventResultProjectionBuilder addProjection(Query.Projection projection, String propertyName, String projectionName) {
        projections.add(projection)
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

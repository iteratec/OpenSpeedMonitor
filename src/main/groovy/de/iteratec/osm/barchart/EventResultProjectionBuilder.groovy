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
        if (from && to) {
            query.between('jobResultDate', from, to)
        }
        return this
    }

    EventResultProjectionBuilder withJobGroupIn(List<JobGroup> jobGroups, boolean groupBy = false) {
        return withIn('jobGroup', jobGroups, groupBy)
    }

    EventResultProjectionBuilder withPageIn(List<Page> pages, boolean groupBy = false) {
        return withIn('page', pages, groupBy)
    }

    EventResultProjectionBuilder withIn(String propertyName, List range, boolean groupBy) {
        if (range) {
            query.in(propertyName, range)
            return groupBy ? addGroupByProjection(propertyName) : this
        }
        return this
    }

    EventResultProjectionBuilder withSelectedMeasurandsAveragesProjection(List<SelectedMeasurand> selectedMeasurands) {
        if (selectedMeasurands.any { it.selectedType == SelectedMeasurandType.MEASURAND }) {
            selectedMeasurands.each {
                if (it.selectedType == SelectedMeasurandType.MEASURAND) {
                    addAvgProjection(it.getDatabaseRelevantName())
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
            query.eq(addAliasForUserTimingField('name'), selectedMeasurand.getDatabaseRelevantName())
            String relevantFieldName = selectedMeasurand.selectedType == SelectedMeasurandType.USERTIMING_MEASURE ? 'duration' : 'startTime'
            propertyName = addAliasForUserTimingField(relevantFieldName)
            if (!projectionName) {
                projectionName = relevantFieldName
            }
        }
        return addPropertyProjection(propertyName, projectionName)
    }

    private void withUserTiming(List<SelectedMeasurand> userTimings) {
        List<String> userTimingNames = userTimings.findAll {
            it.selectedType != SelectedMeasurandType.MEASURAND
        }.collect { it.getDatabaseRelevantName() }
        this.joinUserTimings()
        query.in(addAliasForUserTimingField('name'), userTimingNames)
        addGroupByProjection(addAliasForUserTimingField('name'), 'name')
        addGroupByProjection(addAliasForUserTimingField('type'), 'type')
        addAvgProjection(addAliasForUserTimingField('startTime'), 'startTime')
        addAvgProjection(addAliasForUserTimingField('duration'), 'duration')
    }

    private void joinUserTimings() {
        query.createAlias(USER_TIMINGS_NAME, USER_TIMINGS_NAME)
    }

    private String addAliasForUserTimingField(String fieldNameInUserTiming) {
        return USER_TIMINGS_NAME + '.' + fieldNameInUserTiming
    }

    EventResultProjectionBuilder addGroupByProjection(String propertyName, String projectionName = null) {
        return addProjection(Projections.groupProperty(propertyName), propertyName, projectionName)
    }

    EventResultProjectionBuilder addAvgProjection(String propertyName, String projectionName = null) {
        return addProjection(Projections.avg(propertyName), propertyName, projectionName)
    }

    EventResultProjectionBuilder addPropertyProjection(String propertyName, String projectionName = null) {
        return addProjection(Projections.property(propertyName), propertyName, projectionName)
    }

    private EventResultProjectionBuilder addProjection(Query.Projection projection, String propertyName, String projectionName) {
        String projectedField = projectionName ?: propertyName
        if (!projectedFields.contains(projectedField)) {
            projections.add(projection)
            projectedFields.add(projectedField)
        }
        return this
    }

    List<Map> getResults() {
        query.projections.addAll(projections)
        return transformAggregations(query.list())
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

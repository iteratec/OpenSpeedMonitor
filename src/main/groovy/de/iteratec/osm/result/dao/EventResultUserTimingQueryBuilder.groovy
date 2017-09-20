package de.iteratec.osm.result.dao

import de.iteratec.osm.result.SelectedMeasurand
import de.iteratec.osm.result.SelectedMeasurandType
import de.iteratec.osm.result.UserTimingType

/**
 * Created by mwg on 20.09.2017.
 */
class EventResultUserTimingQueryBuilder{
    private static final String USER_TIMINGS_NAME = 'userTimings'
    private String singleMeasurandProjectionName = null
    private EventResultCriteriaBuilder builder = new EventResultCriteriaBuilder()

    EventResultUserTimingQueryBuilder withUserTimingsAveragesProjection(List<SelectedMeasurand> userTimings) {
        if (userTimings.any { !it.selectedType.isUserTiming() }) {
            throw new IllegalArgumentException("selectedMeasurands must all be user timings")
        }
        List<String> userTimingNames = userTimings*.getDatabaseRelevantName()
        this.joinUserTimings()
        builder.query.in(addAliasForUserTimingField('name'), userTimingNames)
        builder.addGroupByProjection(addAliasForUserTimingField('name'), 'name')
        builder.addGroupByProjection(addAliasForUserTimingField('type'), 'type')
        builder.addAvgProjection(addAliasForUserTimingField('startTime'), 'startTime')
        builder.addAvgProjection(addAliasForUserTimingField('duration'), 'duration')

        return this
    }

    EventResultUserTimingQueryBuilder withSelectedMeasurandPropertyProjection(SelectedMeasurand selectedMeasurand, String projectionName) {
        if (selectedMeasurand.selectedType.isUserTiming()) {
            this.joinUserTimings()
            builder.query.eq(addAliasForUserTimingField('name'), selectedMeasurand.getDatabaseRelevantName())
            String relevantFieldName = selectedMeasurand.selectedType == SelectedMeasurandType.USERTIMING_MEASURE ? 'duration' : 'startTime'
            String propertyName = addAliasForUserTimingField(relevantFieldName)
            if (!projectionName) {
                projectionName = relevantFieldName
            }
            singleMeasurandProjectionName = projectionName
            builder.addPropertyProjection(propertyName, projectionName)
        }
        return this
    }

    private String addAliasForUserTimingField(String fieldNameInUserTiming) {
        return USER_TIMINGS_NAME + '.' + fieldNameInUserTiming
    }

    private void joinUserTimings() {
        builder.query.createAlias(USER_TIMINGS_NAME, USER_TIMINGS_NAME)
    }

    List<EventResultProjection> getResultsForFilter(EventResultCriteriaBuilder baseFilters){
        this.builder.mergeWith(baseFilters)
        return createEventResultProjections(builder.getResults())
    }

    List<EventResultProjection> createEventResultProjections(List<Map> transformedAggregations) {
        List<EventResultProjection> result = []
        transformedAggregations.each { transformedAggregation ->
            if (singleMeasurandProjectionName && transformedAggregation."$singleMeasurandProjectionName") {
                EventResultProjection relevantProjection = new EventResultProjection(jobGroup: transformedAggregation.jobGroup, page: transformedAggregation.page)
                relevantProjection.projectedProperties.put(singleMeasurandProjectionName, transformedAggregation."$singleMeasurandProjectionName")
                result += relevantProjection
            } else {
                def relevantValue = transformedAggregation.type == UserTimingType.MEASURE ? transformedAggregation.duration : transformedAggregation.startTime
                EventResultProjection relevantProjection = result.find {
                    it.page == transformedAggregation.page && it.jobGroup == transformedAggregation.jobGroup
                }
                if (relevantProjection) {
                    relevantProjection.projectedProperties.put(transformedAggregation.name, relevantValue)
                } else {
                    relevantProjection = new EventResultProjection(jobGroup: transformedAggregation.jobGroup, page: transformedAggregation.page)
                    relevantProjection.projectedProperties.put(transformedAggregation.name, relevantValue)
                    result += relevantProjection
                }
            }

        }
        return result
    }
}

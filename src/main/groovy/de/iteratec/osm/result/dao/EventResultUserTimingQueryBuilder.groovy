package de.iteratec.osm.result.dao

import de.iteratec.osm.result.SelectedMeasurand
import de.iteratec.osm.result.SelectedMeasurandType
import de.iteratec.osm.result.UserTimingType

/**
 * Created by mwg on 20.09.2017.
 */
class EventResultUserTimingQueryBuilder {
    private static final String USER_TIMINGS_NAME = 'userTimings'
    private EventResultCriteriaBuilder builder = new EventResultCriteriaBuilder()
    private boolean isAggregated

    EventResultUserTimingQueryBuilder withUserTimingsAveragesProjection(List<SelectedMeasurand> userTimings) {
        isAggregated = true
        if (userTimings.any { !it.selectedType.isUserTiming() }) {
            throw new IllegalArgumentException("selectedMeasurands must all be user timings")
        }
        this.configureForQueryForUserTimings(userTimings)
        builder.addGroupByProjection(addAliasForUserTimingField('name'), 'name')
        builder.addGroupByProjection(addAliasForUserTimingField('type'), 'type')
        builder.addAvgProjection(addAliasForUserTimingField('startTime'), 'startTime')
        builder.addAvgProjection(addAliasForUserTimingField('duration'), 'duration')
        return this
    }

    EventResultUserTimingQueryBuilder withUserTimingsPropertyProjection(List<SelectedMeasurand> userTimings) {
        if (userTimings.any { SelectedMeasurand userTiming -> !userTiming.selectedType.isUserTiming() }) {
            throw new IllegalArgumentException("selectedMeasurands must all be user timings")
        }
        this.configureForQueryForUserTimings(userTimings)
        builder.addPropertyProjection(addAliasForUserTimingField('name'), 'name')
        builder.addPropertyProjection(addAliasForUserTimingField('type'), 'type')
        builder.addPropertyProjection(addAliasForUserTimingField('startTime'), 'startTime')
        builder.addPropertyProjection(addAliasForUserTimingField('duration'), 'duration')
        return this
    }

    private void configureForQueryForUserTimings(List<SelectedMeasurand> userTimings) {
        builder.query.createAlias(USER_TIMINGS_NAME, USER_TIMINGS_NAME)
        if (userTimings) {
            List<String> userTimingNames = userTimings*.getDatabaseRelevantName()
            builder.query.in(addAliasForUserTimingField('name'), userTimingNames)
        }
    }

    private String addAliasForUserTimingField(String fieldNameInUserTiming) {
        return USER_TIMINGS_NAME + '.' + fieldNameInUserTiming
    }


    List<EventResultProjection> getResultsForFilter(EventResultCriteriaBuilder baseFilters) {
        this.builder.mergeWith(baseFilters)
        return createEventResultProjections(builder.getResults())
    }

    private List<EventResultProjection> createEventResultProjections(List<Map> transformedAggregations) {
        List<EventResultProjection> result = []
        transformedAggregations.each { transformedAggregation ->
            def relevantValue = transformedAggregation.type == UserTimingType.MEASURE ? transformedAggregation.duration : transformedAggregation.startTime
            getRelevantProjection(transformedAggregation, result).projectedProperties.put(transformedAggregation.name, relevantValue)
        }
        return result
    }

    private EventResultProjection getRelevantProjection(Map transformedAggregation, List<EventResultProjection> result){
        EventResultProjection relevantProjection = result.find {
            it.page == transformedAggregation.page && it.jobGroup == transformedAggregation.jobGroup && it."$transformedAggregation.name" == null
        }
        if(!relevantProjection){
            relevantProjection = new EventResultProjection(jobGroup: transformedAggregation.jobGroup, page: transformedAggregation.page, isAggregation: isAggregated)
            result.add(relevantProjection)
        }
        return relevantProjection
    }
}

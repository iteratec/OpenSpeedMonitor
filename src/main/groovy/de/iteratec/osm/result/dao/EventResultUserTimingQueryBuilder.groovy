package de.iteratec.osm.result.dao

import de.iteratec.osm.result.SelectedMeasurand
import de.iteratec.osm.result.UserTimingType
/**
 * Created by mwg on 20.09.2017.
 */
class EventResultUserTimingAveragesQueryBuilder implements BaseUserTimingQueryBuilder {

    EventResultUserTimingAveragesQueryBuilder(){
        builder = new EventResultAveragesCriteriaBuilder()
    }

    void configureForSelectedMeasurands(List<SelectedMeasurand> userTimings) {
        isAggregated = true
        if (userTimings.any { !it.selectedType.isUserTiming() }) {
            throw new IllegalArgumentException("selectedMeasurands must all be user timings")
        }
        this.configureForQueryForUserTimings(userTimings)
        builder.addGroupByProjection(addAliasForUserTimingField('name'), 'name')
        builder.addGroupByProjection(addAliasForUserTimingField('type'), 'type')
        builder.addAvgProjection(addAliasForUserTimingField('startTime'), 'startTime')
        builder.addAvgProjection(addAliasForUserTimingField('duration'), 'duration')
    }

}

class EventResultUserTimingRawDataQueryBuilder implements BaseUserTimingQueryBuilder{

    EventResultUserTimingRawDataQueryBuilder(){
        builder = new EventResultRawDataCriteriaBuilder()
    }

    void configureForSelectedMeasurands(List<SelectedMeasurand> userTimings) {
        if (userTimings.any { SelectedMeasurand userTiming -> !userTiming.selectedType.isUserTiming() }) {
            throw new IllegalArgumentException("selectedMeasurands must all be user timings")
        }
        this.configureForQueryForUserTimings(userTimings)
        builder.addPropertyProjection(addAliasForUserTimingField('name'), 'name')
        builder.addPropertyProjection(addAliasForUserTimingField('type'), 'type')
        builder.addPropertyProjection(addAliasForUserTimingField('startTime'), 'startTime')
        builder.addPropertyProjection(addAliasForUserTimingField('duration'), 'duration')
    }
}

trait BaseUserTimingQueryBuilder extends  SelectedMeasurandQueryBuilder {
    static final String USER_TIMINGS_NAME = 'userTimings'

    void configureForQueryForUserTimings(List<SelectedMeasurand> userTimings) {
        builder.query.createAlias(USER_TIMINGS_NAME, USER_TIMINGS_NAME)
        if (userTimings) {
            List<String> userTimingNames = userTimings*.getDatabaseRelevantName()
            builder.query.in(addAliasForUserTimingField('name'), userTimingNames)
        }
    }

    String addAliasForUserTimingField(String fieldNameInUserTiming) {
        return USER_TIMINGS_NAME + '.' + fieldNameInUserTiming
    }

    List<EventResultProjection> getResultsForFilter(EventResultCriteriaBuilder baseFilters) {
        this.builder.mergeWith(baseFilters)
        return createEventResultProjections(builder.getResults())
    }

    List<EventResultProjection> createEventResultProjections(List<Map> transformedAggregations) {
        List<EventResultProjection> result = []
        transformedAggregations.each { transformedAggregation ->
            def relevantValue = transformedAggregation.type == UserTimingType.MEASURE ? transformedAggregation.duration : transformedAggregation.startTime
            getRelevantProjection(transformedAggregation, result).projectedProperties.put(transformedAggregation.name, relevantValue)
        }
        return result
    }

    EventResultProjection getRelevantProjection(Map transformedAggregation, List<EventResultProjection> result){
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
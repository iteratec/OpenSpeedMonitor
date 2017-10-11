package de.iteratec.osm.result.dao

import de.iteratec.osm.result.EventResult
import de.iteratec.osm.result.SelectedMeasurand
import de.iteratec.osm.result.UserTimingType

/**
 * Created by mwg on 11.10.2017.
 */
class UserTimingRawDataQueryBuilder implements SelectedMeasurandQueryBuilder {
    List<SelectedMeasurand> selectedMeasurands
    boolean isAggregated = false

    @Override
    Closure buildProjection(List<String> additionalProjections) {
        List<String> userTimingList = selectedMeasurands.findAll { it.selectedType.isUserTiming() }.collect {
            it.databaseRelevantName
        }

        if (!userTimingList) {
            return null
        }

        return {
            userTimings {
                'in' 'name', userTimingList
            }
            projections {
                userTimings {
                    property 'name', 'name'
                    property 'type', 'type'
                    property 'startTime', 'startTime'
                    property 'duration', 'duration'
                }
                additionalProjections.each {
                    property it, it
                }
            }
        }
    }

    @Override
    void configureForSelectedMeasurands(List<SelectedMeasurand> selectedMeasurands){
        this.selectedMeasurands = selectedMeasurands
    }

    @Override
    List<EventResultProjection> getResultsForFilter(List<Closure> baseFilters, List<String> additionalProjections) {
        List<Closure> filters = []
        filters.addAll(baseFilters)
        return createEventResultProjections(getTransformedData(filters))
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

    List<Map> getTransformedData(List<Closure> queryParts) {
        return EventResult.createCriteria().list {
            queryParts.each {
                applyClosure(it, delegate)
            }
        }
    }

    void applyClosure(Closure closure, def criteriabuilder) {
        closure.delegate = criteriabuilder
        closure()
    }
}

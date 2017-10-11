package de.iteratec.osm.result.dao

import de.iteratec.osm.result.EventResult
import de.iteratec.osm.result.SelectedMeasurand

/**
 * Created by mwg on 11.10.2017.
 */
class MeasurandRawDataQueryBuilder implements SelectedMeasurandQueryBuilder {
    List<SelectedMeasurand> selectedMeasurands
    boolean isAggregated = false

    @Override
    Closure buildProjection(List<String> additionalProjections) {
        isAggregated = false
        List<SelectedMeasurand> measurands = selectedMeasurands.findAll { !it.selectedType.isUserTiming() }
        if (!measurands) {
            return null
        }
        return {
            projections {
                measurands.each {
                    property it.databaseRelevantName, it.databaseRelevantName
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
        filters.add(buildProjection(additionalProjections))
        return createEventResultProjections(getTransformedData(filters))
    }

    List<EventResultProjection> createEventResultProjections(List<Map> transformedAggregations) {
        List<EventResultProjection> eventResultProjections = []
        transformedAggregations.each {
            EventResultProjection eventResultProjection = new EventResultProjection(
                    jobGroup: it.jobGroup,
                    page: it.page,
                    isAggregation: isAggregated,
            )
            it.remove("jobGroup")
            it.remove("page")
            eventResultProjection.projectedProperties = it
            eventResultProjections += eventResultProjection
        }
        return eventResultProjections
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

package de.iteratec.osm.result.dao

import de.iteratec.osm.result.SelectedMeasurand

/**
 * Created by mwg on 20.09.2017.
 */
class EventResultMeasurandQueryBuilder{
    private EventResultCriteriaBuilder builder = new EventResultCriteriaBuilder()
    boolean  isAggregated

    EventResultMeasurandQueryBuilder withMeasurandsAveragesProjection(List<SelectedMeasurand> measurands) {
        isAggregated = true
        if (measurands.any { SelectedMeasurand measurand -> measurand.selectedType.isUserTiming() }) {
            throw new IllegalArgumentException("selectedMeasurands must not be user timings")
        }
        measurands.each {
            builder.addAvgProjection(it.getDatabaseRelevantName())
        }
        return this
    }

    EventResultMeasurandQueryBuilder withMeasurandProjection(List<SelectedMeasurand> measurands) {
        if (measurands.any { SelectedMeasurand measurand -> measurand.selectedType.isUserTiming() }) {
            throw new IllegalArgumentException("selectedMeasurands must not be user timings")
        }
        measurands.each {
            builder.addPropertyProjection(it.getDatabaseRelevantName())
        }
        return this
    }

    List<EventResultProjection> getResultsForFilter(EventResultCriteriaBuilder baseFilters){
        this.builder.mergeWith(baseFilters)
        return createEventResultProjections(builder.getResults())
    }

    private List<EventResultProjection> createEventResultProjections(List<Map> normalized) {
        List<EventResultProjection> eventResultProjections = []
        normalized.each {
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
}

package de.iteratec.osm.result.dao

import de.iteratec.osm.result.SelectedMeasurand

/**
 * Created by mwg on 20.09.2017.
 */
class EventResultMeasurandQueryBuilder{
    private EventResultCriteriaBuilder builder = new EventResultCriteriaBuilder()

    EventResultMeasurandQueryBuilder withMeasurandsAveragesProjection(List<SelectedMeasurand> measurands) {
        if (measurands.any { it.selectedType.isUserTiming() }) {
            throw new IllegalArgumentException("selectedMeasurands must not be user timings")
        }
        measurands.each {
            builder.addAvgProjection(it.getDatabaseRelevantName())
        }
        return this
    }

    EventResultMeasurandQueryBuilder withSelectedMeasurandPropertyProjection(SelectedMeasurand selectedMeasurand, String projectionName) {
        if (!selectedMeasurand.selectedType.isUserTiming()) {
            String propertyName = selectedMeasurand.getDatabaseRelevantName()
            builder.addPropertyProjection(propertyName, projectionName)
        }
        return this
    }

    List<EventResultProjection> getResultsForFilter(EventResultCriteriaBuilder baseFilters){
        this.builder.mergeWith(baseFilters)
        return createEventResultProjections(builder.getResults())
    }

    List<EventResultProjection> createEventResultProjections(List<Map> normalized) {
        List<EventResultProjection> eventResultProjections = []
        normalized.each {
            EventResultProjection eventResultProjection = new EventResultProjection(
                    jobGroup: it.jobGroup,
                    page: it.page,
            )
            it.remove("jobGroup")
            it.remove("page")
            eventResultProjection.projectedProperties = it
            eventResultProjections += eventResultProjection
        }
        return eventResultProjections
    }
}

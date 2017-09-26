package de.iteratec.osm.result.dao

import de.iteratec.osm.result.SelectedMeasurand
import de.iteratec.osm.result.UserTimingType

/**
 * Created by mwg on 26.09.2017.
 */
trait SelectedMeasurandQueryBuilder {
    EventResultCriteriaBuilder builder
    boolean isAggregated

    abstract void configureForSelectedMeasurands(List<SelectedMeasurand> selectedMeasurands)

    List<EventResultProjection> getResultsForFilter(EventResultCriteriaBuilder baseFilters) {
        this.builder.mergeWith(baseFilters)
        return createEventResultProjections(builder.getResults())
    }

    abstract List<EventResultProjection> createEventResultProjections(List<Map> transformedAggregations)
}


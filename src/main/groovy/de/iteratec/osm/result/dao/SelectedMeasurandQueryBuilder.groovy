package de.iteratec.osm.result.dao

import de.iteratec.osm.result.EventResult
import de.iteratec.osm.result.SelectedMeasurand
import de.iteratec.osm.result.UserTimingType

/**
 * Created by mwg on 26.09.2017.
 */
interface SelectedMeasurandQueryBuilder {

    Closure buildProjection(List<String> additionalProjections)

    void configureForSelectedMeasurands(List<SelectedMeasurand> selectedMeasurands)

    List<EventResultProjection> getResultsForFilter(List<Closure> filters, List<String> additionalProjections)
}


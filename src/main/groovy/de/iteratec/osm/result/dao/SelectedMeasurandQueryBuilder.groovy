package de.iteratec.osm.result.dao

import de.iteratec.osm.result.SelectedMeasurand
import de.iteratec.osm.util.PerformanceLoggingService

/**
 * Created by mwg on 26.09.2017.
 */
interface SelectedMeasurandQueryBuilder {

    Closure buildProjection(Set<ProjectionProperty> additionalProjections)

    void configureForSelectedMeasurands(List<SelectedMeasurand> selectedMeasurands)

    List<EventResultProjection> getResultsForFilter(List<Closure> filters, Set<ProjectionProperty> baseProjections, List<MeasurandTrim> trims, PerformanceLoggingService performanceLoggingService)
}


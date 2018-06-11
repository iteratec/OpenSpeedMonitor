package de.iteratec.osm.result.dao

import de.iteratec.osm.dao.ProjectionProperty
import de.iteratec.osm.result.SelectedMeasurand
import de.iteratec.osm.util.PerformanceLoggingService

/**
 * Created by mwg on 26.09.2017.
 */
interface SelectedMeasurandQueryBuilder {

    Closure buildProjection(List<ProjectionProperty> additionalProjections)

    void configureForSelectedMeasurands(List<SelectedMeasurand> selectedMeasurands)

    List<EventResultProjection> getResultsForFilter(List<EventResultFilter> filters, List<ProjectionProperty> baseProjections, List<MeasurandTrim> trims, PerformanceLoggingService performanceLoggingService)
}


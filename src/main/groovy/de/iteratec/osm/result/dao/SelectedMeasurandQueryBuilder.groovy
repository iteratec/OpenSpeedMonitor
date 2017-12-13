package de.iteratec.osm.result.dao

import de.iteratec.osm.dao.ProjectionProperty
import de.iteratec.osm.result.SelectedMeasurand
/**
 * Created by mwg on 26.09.2017.
 */
interface SelectedMeasurandQueryBuilder {

    Closure buildProjection(List<ProjectionProperty> additionalProjections)

    void configureForSelectedMeasurands(List<SelectedMeasurand> selectedMeasurands)

    List<EventResultProjection> getResultsForFilter(List<Closure> filters, List<ProjectionProperty> baseProjections, List<MeasurandTrim> trims)
}


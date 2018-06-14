package de.iteratec.osm.result.dao.query.projector

import de.iteratec.osm.result.SelectedMeasurand
import de.iteratec.osm.result.dao.MeasurandTrim
import de.iteratec.osm.result.dao.ProjectionProperty

interface EventResultProjector {

    Closure generateSelectedMeasurandProjectionFor(List<SelectedMeasurand> measurands, Set<ProjectionProperty> baseProjections)

    List<Closure> buildTrims(List<SelectedMeasurand> measurands, List<MeasurandTrim> trims)
}

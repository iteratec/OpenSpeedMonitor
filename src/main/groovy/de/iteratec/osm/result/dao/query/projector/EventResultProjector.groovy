package de.iteratec.osm.result.dao.query.projector

import de.iteratec.osm.result.SelectedMeasurand

interface EventResultProjector {

    Closure generateSelectedMeasurandProjectionFor(List<SelectedMeasurand> measurands, Set<ProjectionProperty> baseProjections)
}

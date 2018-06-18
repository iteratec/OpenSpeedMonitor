package de.iteratec.osm.result.dao.query.projector

import de.iteratec.osm.result.SelectedMeasurand

class MeasurandRawDataProjector implements EventResultProjector {
    @Override
    Closure generateSelectedMeasurandProjectionFor(List<SelectedMeasurand> measurands, Set<ProjectionProperty> baseProjections) {
        return {
            projections {
                measurands.each {
                    property it.databaseRelevantName, it.databaseRelevantName
                }
                baseProjections.each {ProjectionProperty pp ->
                    property pp.dbName, pp.alias
                }
            }
        }
    }
}

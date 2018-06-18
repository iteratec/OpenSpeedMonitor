package de.iteratec.osm.result.dao.query.projector

import de.iteratec.osm.result.SelectedMeasurand

class MeasurandAverageDataProjector implements EventResultProjector {
    @Override
    Closure generateSelectedMeasurandProjectionFor(List<SelectedMeasurand> measurands, Set<ProjectionProperty> baseProjections) {
        return {
            projections {
                measurands.each {
                    avg it.databaseRelevantName, it.databaseRelevantName
                }
                baseProjections.each { ProjectionProperty pp ->
                    groupProperty pp.dbName, pp.alias
                }
            }
        }
    }
}

package de.iteratec.osm.result.dao.query.projector

import de.iteratec.osm.result.SelectedMeasurand

class UserTimingAverageDataProjector implements EventResultProjector {
    @Override
    Closure generateSelectedMeasurandProjectionFor(List<SelectedMeasurand> measurands, Set<ProjectionProperty> baseProjections) {
        return {
            'userTimings' {
                'in' 'name', measurands.collect { it.databaseRelevantName }
            }
            'projections' {
                'userTimings' {
                    'groupProperty' 'name', 'name'
                    'groupProperty' 'type', 'type'
                    'avg' 'startTime', 'startTime'
                    'avg' 'duration', 'duration'
                }
                baseProjections.each { ProjectionProperty pp ->
                    'groupProperty' pp.dbName, pp.alias
                }
            }
        }
    }
}

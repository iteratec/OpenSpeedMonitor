package de.iteratec.osm.result.dao.query.projector

import de.iteratec.osm.result.SelectedMeasurand
import de.iteratec.osm.result.dao.query.ProjectionProperty

class UserTimingRawDataProjector implements EventResultProjector {
    @Override
    Closure generateSelectedMeasurandProjectionFor(List<SelectedMeasurand> measurands, Set<ProjectionProperty> baseProjections) {
        return {
            'userTimings' {
                'in' 'name', measurands.collect { it.databaseRelevantName }
            }
            'projections' {
                'userTimings' {
                    'property' 'name', 'name'
                    'property' 'type', 'type'
                    'property' 'startTime', 'startTime'
                    'property' 'duration', 'duration'
                }
                baseProjections.each { ProjectionProperty pp ->
                    'property' pp.dbName, pp.alias
                }
            }
        }
    }
}

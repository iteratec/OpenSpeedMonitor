package de.iteratec.osm.result.dao.query.projector

import de.iteratec.osm.result.MeasurandGroup
import de.iteratec.osm.result.SelectedMeasurand
import de.iteratec.osm.result.dao.MeasurandTrim
import de.iteratec.osm.result.dao.ProjectionProperty

class UserTimingRawDataProjector implements EventResultProjector {
    @Override
    Closure generateSelectedMeasurandProjectionFor(List<SelectedMeasurand> measurands, Set<ProjectionProperty> baseProjections) {
        return {
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

    @Override
    List<Closure> buildTrims(List<SelectedMeasurand> measurands, List<MeasurandTrim> trims) {
        List<String> userTimingList = measurands.collect { it.databaseRelevantName }
        trims = trims ?: []
        List<MeasurandTrim> loadTimeTrims = trims.findAll { it.measurandGroup == MeasurandGroup.LOAD_TIMES }
        Closure trimClosure = {
            userTimings {
                'in' 'name', userTimingList
                loadTimeTrims.each { MeasurandTrim trim ->
                    or {
                        and {
                            "${trim.qualifier.getGormSyntax()}" "startTime", Double.valueOf(trim.value)
                            isNull("duration")
                        }
                        "${trim.qualifier.getGormSyntax()}" "duration", Double.valueOf(trim.value)
                    }
                }
            }
        }
        return [trimClosure]
    }
}

package de.iteratec.osm.result.dao.query.trimmer

import de.iteratec.osm.result.MeasurandGroup
import de.iteratec.osm.result.SelectedMeasurand
import de.iteratec.osm.result.dao.query.AggregationUtil

class UserTimingDataTrimmer implements EventResultTrimmer {
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
        List<Closure> trimClosures = [trimClosure]
        trimClosures.addAll(AggregationUtil.findAllTrimsStillToBeSet(measurands, trims))
        return trimClosures
    }
}

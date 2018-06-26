package de.iteratec.osm.result.dao.query.trimmer

import de.iteratec.osm.result.SelectedMeasurand
import de.iteratec.osm.result.dao.query.AggregationUtil
import de.iteratec.osm.result.dao.query.MeasurandTrim

class MeasurandRawDataTrimmer implements EventResultTrimmer {
    @Override
    List<Closure> buildTrims(List<SelectedMeasurand> measurands, List<MeasurandTrim> trims) {
        List<Closure> trimClosures = []
        trims = trims ?: []
        trims.each { MeasurandTrim trim ->
            measurands.each { SelectedMeasurand selectedMeasurand ->
                if (AggregationUtil.checkIfTrimIsRelevantFor(selectedMeasurand, trim)) {
                    trimClosures.add({
                        "${trim.qualifier.getGormSyntax()}" "${selectedMeasurand.databaseRelevantName}", trim.value
                    })
                }
            }
        }
        trimClosures.addAll(AggregationUtil.findAllTrimsStillToBeSet(measurands, trims))
        return trimClosures
    }
}

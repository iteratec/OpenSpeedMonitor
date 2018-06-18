package de.iteratec.osm.result.dao.query.trimmer

import de.iteratec.osm.result.SelectedMeasurand
import de.iteratec.osm.result.dao.query.AggregationUtil

class MeasurandAverageDataTrimmer implements EventResultTrimmer {
    @Override
    List<Closure> buildTrims(List<SelectedMeasurand> measurands, List<MeasurandTrim> trims) {
        List<Closure> trimClosures = []
        trims = trims ?: []
        trims.each { MeasurandTrim trim ->
            measurands.each { SelectedMeasurand selectedMeasurand ->
                if (AggregationUtil.checkIfTrimIsRelevantFor(selectedMeasurand, trim)) {
                    trimClosures.add({
                        "${trim.qualifier.getGormSyntax()}" "${selectedMeasurand.databaseRelevantName}", Double.valueOf(trim.value)
                    })
                }
            }
        }
        trimClosures.addAll(AggregationUtil.findAllTrimsStillToBeSet(measurands, trims))
        return trimClosures
    }
}

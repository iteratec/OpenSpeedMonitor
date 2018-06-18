package de.iteratec.osm.result.dao.query.trimmer

import de.iteratec.osm.result.SelectedMeasurand
import de.iteratec.osm.result.dao.query.MeasurandTrim

interface EventResultTrimmer {

    List<Closure> buildTrims(List<SelectedMeasurand> measurands, List<MeasurandTrim> trims)
}

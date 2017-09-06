package de.iteratec.osm.barchart

import de.iteratec.osm.csi.Page
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.result.SelectedMeasurand
import groovy.transform.EqualsAndHashCode

/**
 * Created by mwg on 30.08.2017.
 */
@EqualsAndHashCode(excludes = ["value", "valueComparative"])
class BarchartAggregation {
    Double value
    Double valueComparative
    SelectedMeasurand selectedMeasurand
    Page page
    JobGroup jobGroup
}

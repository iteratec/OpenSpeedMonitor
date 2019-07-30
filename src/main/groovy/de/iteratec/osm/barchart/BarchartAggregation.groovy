package de.iteratec.osm.barchart

import de.iteratec.osm.csi.Page
import de.iteratec.osm.measurement.environment.Browser
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.result.PerformanceAspectType
import de.iteratec.osm.result.SelectedMeasurand
import groovy.transform.EqualsAndHashCode

/**
 * Created by mwg on 30.08.2017.
 */
@EqualsAndHashCode(excludes = ["value", "valueComparative", "median", "medianComparative"])
class BarchartAggregation {
    Double value
    Double valueComparative
    SelectedMeasurand selectedMeasurand
    PerformanceAspectType performanceAspectType
    Page page
    JobGroup jobGroup
    Browser browser
    String aggregationValue
    String deviceType
    String operatingSystem
}

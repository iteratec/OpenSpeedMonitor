package de.iteratec.osm.barchart

import de.iteratec.osm.csi.Page
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.result.Selected

/**
 * Created by mwg on 09.08.2017.
 */
class BarChartAggregation {
    Selected selected
    JobGroup jobGroup
    Page page
    Double value
    Double comparativeValue
}

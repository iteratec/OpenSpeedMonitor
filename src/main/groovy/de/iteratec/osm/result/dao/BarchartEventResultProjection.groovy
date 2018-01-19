package de.iteratec.osm.result.dao

import de.iteratec.osm.csi.Page
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.report.chart.RepresentableWptResult
import groovy.transform.EqualsAndHashCode
import de.iteratec.osm.barchart.BarchartQueryAndCalculationService
/**
 * EventResultProjection extended for usage in {@link BarchartQueryAndCalculationService}.
 *
 */
@EqualsAndHashCode(excludes = ["projectedProperties"])
class BarchartEventResultProjection extends EventResultProjection implements RepresentableWptResult{

    JobGroup jobGroup
    Page page
    boolean isAggregation

}

package de.iteratec.osm.result

import de.iteratec.osm.csi.Page
import de.iteratec.osm.measurement.schedule.JobGroup

class PerformanceAspect {
    JobGroup jobGroup
    Page page
    PerformanceAspectType performanceAspectType

    String metricIdentifier
    CachedView cachedView

    static transients = ['metric']

    static constraints = {
        performanceAspectType(unique: ['jobGroup', 'page'])
    }



    void setMetric(SelectedMeasurand metric){
        this.metricIdentifier = metric.optionValue
        this.cachedView = metric.cachedView
    }

    SelectedMeasurand getMetric(){
        return new SelectedMeasurand(this.metricIdentifier, this.cachedView)
    }
}

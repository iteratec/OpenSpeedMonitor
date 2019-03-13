package de.iteratec.osm.result

import de.iteratec.osm.csi.Page
import de.iteratec.osm.measurement.schedule.JobGroup

class PerformanceAspect {
    JobGroup jobGroup
    Page page
    PerformanceAspectType performanceAspectType

    SelectedMeasurand metric
    String metricIdentifier
    CachedView cachedView

    static transients = ['metric']

    static constraints = {
        performanceAspectType(unique: ['jobGroup', 'page'])
    }

    def onLoad(){
        this.metric = new SelectedMeasurand(this.metricIdentifier, this.cachedView)
    }

    void setMetric(SelectedMeasurand metric){
        this.metric = metric
        this.metricIdentifier = metric.optionValue
        this.cachedView = metric.cachedView
    }
}

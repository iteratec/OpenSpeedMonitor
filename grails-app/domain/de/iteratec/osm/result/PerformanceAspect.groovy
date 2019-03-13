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

    def beforeInsert(){
        this.metricIdentifier = metric.optionValue
        this.cachedView = metric.cachedView
    }

    def onLoad(){
        this.metric = new SelectedMeasurand(this.metricIdentifier, this.cachedView)
    }
}

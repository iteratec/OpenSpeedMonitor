package de.iteratec.osm.result

import de.iteratec.osm.csi.Page
import de.iteratec.osm.measurement.environment.Browser
import de.iteratec.osm.measurement.schedule.JobGroup

class PerformanceAspect {
    JobGroup jobGroup
    Page page
    Browser browser
    PerformanceAspectType performanceAspectType

    String metricIdentifier
    CachedView cachedView

    static transients = ['metric']

    static constraints = {
        performanceAspectType(unique: ['jobGroup', 'page', 'browser'])
    }



    void setMetric(SelectedMeasurand metric){
        this.metricIdentifier = metric.optionValue
        this.cachedView = metric.cachedView
    }

    SelectedMeasurand getMetric(){
        return new SelectedMeasurand(this.metricIdentifier, this.cachedView)
    }
}

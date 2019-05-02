package de.iteratec.osm.result.dao.query


import de.iteratec.osm.result.PerformanceAspect
import de.iteratec.osm.result.PerformanceAspectType
import de.iteratec.osm.result.SelectedMeasurand
import de.iteratec.osm.result.dao.EventResultProjection
import groovy.transform.EqualsAndHashCode
/**
 * @author nkuhn
 */
class AspectUtil {

    List<Long> jobGroupIds = []
    List<Long> pageIds = []
    List<PerformanceAspectType> aspectTypes = []

    void addMissingAspectMeasurands(List<SelectedMeasurand> measurands, Closure<Boolean> validFn) {
        getAspects().each {PerformanceAspect aspect ->
            SelectedMeasurand aspectMetric = aspect.getMetric()
            if (!measurands.contains(aspectMetric) && validFn(aspectMetric)) {
                measurands.add(aspectMetric)
            }
        }
    }

    List<PerformanceAspect> getAspects() {
        if (jobGroupIds.size() == 0 || pageIds.size() == 0 || this.aspectTypes.size() == 0) return []
        return PerformanceAspect.createCriteria().list {
            'in'('jobGroup', this.jobGroupIds)
            'in'('page', this.pageIds)
            'in'('performanceAspectType', this.aspectTypes)
        }
    }

    void appendAspectMetrics(List<EventResultProjection> metricsFromBuilder){

    }

}

package de.iteratec.osm.result.dao

import de.iteratec.osm.result.CachedView
import de.iteratec.osm.result.SelectedMeasurand

class PerformanceAspectDto {
    Long id
    Long applicationId
    Long pageId
    Long browserId
    MeasurandDto measurand
    PerformanceAspectTypeDto performanceAspectType
    Boolean persistent

    public PerformanceAspectDto(Map aspectAsMap) {
        this.id = aspectAsMap['id']
        this.applicationId = aspectAsMap['jobGroupId']
        this.pageId = aspectAsMap['pageId']
        this.browserId = aspectAsMap['browserId']
        SelectedMeasurand selectedMetric = new SelectedMeasurand(aspectAsMap['metricIdentifier'], CachedView.UNCACHED)
        this.measurand = new MeasurandDto(id: selectedMetric.optionValue, name: selectedMetric.name)
        this.performanceAspectType = new PerformanceAspectTypeDto(
                name: aspectAsMap['performanceAspectType']?.toString(),
                icon: aspectAsMap['performanceAspectType']?.icon
        )
        this.persistent = aspectAsMap['persistent']
    }
}

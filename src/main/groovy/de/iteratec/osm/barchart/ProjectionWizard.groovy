package de.iteratec.osm.barchart

import de.iteratec.osm.csi.Page
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.result.EventResult
import de.iteratec.osm.result.MeasurandGroup
import de.iteratec.osm.result.Selected
import de.iteratec.osm.result.SelectedType

/**
 * Created by mwg on 10.08.2017.
 */
class ProjectionWizard {
    List<String> projectedFields

    Closure createProjection(List<String> groupedProperties, List<Selected> selectedList) {
        projectedFields = []

        List<String> averagedProperties = selectedList.findAll { it.selectedType == SelectedType.MEASURAND }.collect {
            it.getDatabaseRelevantName()
        }

        boolean withUserTiming = selectedList.any { it.selectedType != SelectedType.MEASURAND }

        if (!groupedProperties) {
            groupedProperties = []
        }
        projectedFields.addAll(groupedProperties)
        projectedFields.addAll(averagedProperties)

        if (withUserTiming) {
            projectedFields.add('name')
            projectedFields.add('type')
            projectedFields.add('startTime')
            projectedFields.add('duration')
        }

        return {
            projections {
                groupedProperties.each { property ->
                    groupProperty(property)
                }
                if (!withUserTiming) {
                    averagedProperties.each { m ->
                        avg(m)
                    }
                } else {
                    userTimings {
                        groupProperty('name')
                        groupProperty('type')
                        avg('startTime')
                        avg('duration')
                    }
                }
            }
        }
    }


    List<Map> transformAggregations(def aggregations) {
        List<Map> result = []
        aggregations.each { aggregation ->
            Map transformed = [:]
            projectedFields.each {
                transformed.put(it, aggregation[projectedFields.indexOf(it)])
            }
            result.add(transformed)
        }
        return result
    }
}

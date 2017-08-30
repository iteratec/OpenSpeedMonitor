package de.iteratec.osm.barchart

import de.iteratec.osm.csi.Page
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.result.EventResult
import de.iteratec.osm.result.SelectedMeasurand
import de.iteratec.osm.result.SelectedMeasurandType

/**
 * Created by mwg on 10.08.2017.
 */
class BarchartEventResultAggregationBuilder {
    List<String> projectedFields

    List<Map> aggregateFor(Date from, Date to, List<JobGroup> allJobGroups, List<Page> allPages, List<SelectedMeasurand> selectedMeasurands, Integer minValidLoadtime, Integer maxValidLoadtime) {
        List<String> groupedProperties = []
        if(allJobGroups){
            groupedProperties.add('jobGroup')
        }
        if(allPages){
            groupedProperties.add('page')
        }

        Closure projection = createProjection(groupedProperties, selectedMeasurands)
        List<SelectedMeasurand> userTimings = []
        if (selectedMeasurands.any { it.selectedType != SelectedMeasurandType.MEASURAND }) {
            userTimings = selectedMeasurands
        }
        return aggregateEventResults(projection, from, to, allJobGroups, allPages, userTimings, minValidLoadtime, maxValidLoadtime)
    }

    private Closure createProjection(List<String> groupedProperties, List<SelectedMeasurand> selectedList) {
        boolean withUserTiming = selectedList.any { it.selectedType != SelectedMeasurandType.MEASURAND }
        projectedFields = []

        if (!groupedProperties) {
            groupedProperties = []
        }
        projectedFields.addAll(groupedProperties)

        List<String> averagedProperties = []
        if (withUserTiming) {
            projectedFields.add('name')
            projectedFields.add('type')
            projectedFields.add('startTime')
            projectedFields.add('duration')
        } else {
            averagedProperties = selectedList.findAll { it.selectedType == SelectedMeasurandType.MEASURAND }.collect {
                it.getDatabaseRelevantName()
            }
            projectedFields.addAll(averagedProperties)
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

    private List<Map> aggregateEventResults(Closure projection, Date from, Date to, List<JobGroup> allJobGroups, List<Page> allPages, List<SelectedMeasurand> userTimings, Integer minValidLoadtime, Integer maxValidLoadtime) {
        return transformAggregations(EventResult.createCriteria().list {
            new BarchartEventResultFilterBuilder(delegate, minValidLoadtime, maxValidLoadtime)
                    .withJobResultDate(from, to)
                    .withJobGroup(allJobGroups)
                    .withPage(allPages)
                    .withUserTimings(userTimings)
                    .getFilter()
            projection.delegate = delegate
            projection()
        })
    }


    private List<Map> transformAggregations(def aggregations) {
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

package de.iteratec.osm.barchart

import de.iteratec.osm.csi.Page
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.result.EventResult
import de.iteratec.osm.result.Selected
import de.iteratec.osm.result.SelectedType

/**
 * Created by mwg on 10.08.2017.
 */
class BarchartEventResultAggregationBuilder {
    List<String> projectedFields

    List<Map> aggregateFor(List<String> groupedProperties, Date from, Date to, List<JobGroup> allJobGroups, List<Page> allPages, List<Selected> selectedList, Integer minValidLoadtime, Integer maxValidLoadtime) {
        Closure projection = createProjection(groupedProperties, selectedList);
        List<Selected> userTimings = []
        if (selectedList.any { it.selectedType != SelectedType.MEASURAND }) {
            userTimings = selectedList
        }
        return aggregateEventResults(projection, from, to, allJobGroups, allPages, userTimings, minValidLoadtime, maxValidLoadtime)
    }

    private Closure createProjection(List<String> groupedProperties, List<Selected> selectedList) {
        boolean withUserTiming = selectedList.any { it.selectedType != SelectedType.MEASURAND }
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
            averagedProperties = selectedList.findAll { it.selectedType == SelectedType.MEASURAND }.collect {
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

    private List<Map> aggregateEventResults(Closure projection, Date from, Date to, List<JobGroup> allJobGroups, List<Page> allPages, List<Selected> userTimings, Integer minValidLoadtime, Integer maxValidLoadtime) {
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

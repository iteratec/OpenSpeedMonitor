package de.iteratec.osm.barchart

import de.iteratec.osm.OsmConfigCacheService
import de.iteratec.osm.csi.Page
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.result.CachedView
import de.iteratec.osm.result.EventResult
import de.iteratec.osm.result.Selected
import de.iteratec.osm.result.SelectedType
import de.iteratec.osm.result.UserTimingType
import grails.transaction.Transactional

@Transactional
class BarChartAggregationService {

    OsmConfigCacheService osmConfigCacheService

    List<BarChartAggregation> getAggregationForPageAggreagation(GetBarchartCommand cmd) {
        List<Selected> allSelected = cmd.selectedSeries*.measurands.flatten().collect { new Selected(it, CachedView.UNCACHED)}
        Date from =  cmd.from.toDate()
        Date to = cmd.to.toDate()
        List<JobGroup> allJobGroups = JobGroup.findAllByNameInList(cmd.selectedJobGroups)
        List<Page> allPages = Page.findAllByNameInList(cmd.selectedPages)

        def aggregationsForMeasurands = aggregateMeasurandForPageAggregation(allSelected, from, to, allJobGroups, allPages)
        def comparativesForMeasurands = []
        def aggregationsForUserTimings = aggregateUserTimingsForPageAggregation(allSelected, from, to, allJobGroups, allPages)
        def comparativesForUserTimings = []
        if (cmd.fromComparative && cmd.toComparative) {
            Date comparativeFrom =  cmd.fromComparative.toDate()
            Date comparativeTo =  cmd.toComparative.toDate()
            comparativesForMeasurands = aggregateMeasurandForPageAggregation(allSelected, comparativeFrom, comparativeTo, allJobGroups, allPages)
            comparativesForUserTimings = aggregateUserTimingsForPageAggregation(allSelected, comparativeFrom, comparativeTo, allJobGroups, allPages)
        }

        List<BarChartAggregation> resultForMeasurands = collectBarChartAggregationsForMeasurand(allSelected, aggregationsForMeasurands, comparativesForMeasurands)
        List<BarChartAggregation> resultForUserTimings = collectBarChartAggregationsForUserTimings(allSelected, aggregationsForUserTimings, comparativesForUserTimings)

        List<BarChartAggregation> result = []
        result.addAll(resultForMeasurands)
        result.addAll(resultForUserTimings)

        return result
    }

    private def aggregateMeasurandForPageAggregation(List<Selected> allSelected, Date from, Date to, List<JobGroup> allJobGroups, List<Page> allPages){
        List<Selected> selectedMeasurands = allSelected.findAll {it.selectedType == SelectedType.MEASURAND}
        Closure projection = {
            projections {
                groupProperty('page')
                groupProperty('jobGroup')
                selectedMeasurands.collect {it.getDatabaseRelevantName()}.each { m ->
                    avg(m)
                }
            }
        }
        return aggregateFor(projection, from, to, allJobGroups, allPages, null)
    }

    private def aggregateUserTimingsForPageAggregation(List<Selected> allSelected, Date from, Date to, List<JobGroup> allJobGroups, List<Page> allPages){
        List<Selected> selectedUserTimings = allSelected.findAll {it.selectedType != SelectedType.MEASURAND}
        if(selectedUserTimings.size() < 1){
            return []
        }
        Closure projection = {
            projections {
                groupProperty('page')
                groupProperty('jobGroup')
                userTimings{
                    groupProperty('name')
                    groupProperty('type')
                    avg('startTime')
                    avg('duration')
                }
            }
        }
        return  aggregateFor(projection, from, to, allJobGroups, allPages, selectedUserTimings)

    }

    private def aggregateFor(Closure projection, Date from, Date to, List<JobGroup> allJobGroups, List<Page> allPages, List<Selected> userTimings){
        return EventResult.createCriteria().list {
            new BarChartAggregationFilterBuilder(delegate, osmConfigCacheService.getMinValidLoadtime(), osmConfigCacheService.getMaxValidLoadtime())
                    .withJobResultDate(from, to)
                    .withJobGroup(allJobGroups)
                    .withPage(allPages)
                    .withUserTimings(userTimings)
                    .build()
            projection.delegate = delegate
            projection()
        }
    }

    private List<BarChartAggregation> collectBarChartAggregationsForMeasurand(List<Selected> allSelected, def values, def comparativeValues){
        List<BarChartAggregation> result = []
        if(values && values.size() >= 1){
            result = createListForMeasurandAggregation(allSelected,values)
            if(comparativeValues && comparativeValues.size() >= 1){
                List<BarChartAggregation> comparatives = createListForMeasurandAggregation(allSelected, comparativeValues)
                comparatives.each{ comparative ->
                    BarChartAggregation matches = result.find {it.page == comparative.page && it.jobGroup == comparative.jobGroup && it.selected == comparative.selected}
                    matches.comparativeValue = comparative.value
                }
            }
        }
        return result
    }

    private List<BarChartAggregation> createListForMeasurandAggregation(List<Selected> allSelected, def measurandAggregations){
        List<BarChartAggregation> result = []
        List<Selected> selectedMeasurands = allSelected.findAll {it.selectedType == SelectedType.MEASURAND}
        if(measurandAggregations && measurandAggregations.size() >= 1){
            measurandAggregations.each{ aggregation ->
                result.addAll(
                        selectedMeasurands.collect{ Selected selected ->
                            int relevantIndex = selectedMeasurands.indexOf(selected) + 2
                            new BarChartAggregation(page: aggregation[0], jobGroup: aggregation[1], value: aggregation[relevantIndex], selected: selected)
                        }
                )
            }
        }
        return result
    }

    private List<BarChartAggregation> collectBarChartAggregationsForUserTimings(List<Selected> allSelected, def values, def comparativeValues){
        List<BarChartAggregation> result = []
        if(values && values.size() >= 1){
            result = createListForUserTimingAggregation(allSelected,values)
            if(comparativeValues && comparativeValues.size() >= 1){
                List<BarChartAggregation> comparatives = createListForUserTimingAggregation(allSelected, comparativeValues)
                comparatives.each{ comparative ->
                    BarChartAggregation matches = result.find {it.page == comparative.page && it.jobGroup == comparative.jobGroup && it.selected == comparative.selected}
                    matches.comparativeValue = comparative.value
                }
            }
        }
        return result
    }

    private List<BarChartAggregation> createListForUserTimingAggregation(List<Selected> allSelected, def userTimingAggregations){
        List<BarChartAggregation> result = []
        List<Selected> selectedMeasurands = allSelected.findAll {it.selectedType != SelectedType.MEASURAND}
        if(userTimingAggregations && userTimingAggregations.size() >= 1){
            result = userTimingAggregations.collect{ aggregation ->
                Selected selected = selectedMeasurands.find{it.name = aggregation[2]}
                Double value
                if(aggregation[3] == UserTimingType.MEASURE){
                    value = aggregation[5]
                }else{
                    value = aggregation[4]
                }
                new BarChartAggregation(page: aggregation[0], jobGroup: aggregation[1], value: value, selected: selected)
            }
        }
        return result
    }
}

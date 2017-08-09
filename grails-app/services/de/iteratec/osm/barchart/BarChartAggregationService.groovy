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
        List<Selected> allUserTimings = cmd.selectedSeries*.measurands.flatten().collect { new Selected(it, CachedView.UNCACHED)}.findAll {it.selectedType != SelectedType.MEASURAND}
        List<Selected> allMeasurands = cmd.selectedSeries*.measurands.flatten().collect { new Selected(it, CachedView.UNCACHED)}.findAll {it.selectedType == SelectedType.MEASURAND}
        Date from =  cmd.from.toDate()
        Date to = cmd.to.toDate()
        List<JobGroup> allJobGroups = JobGroup.findAllByNameInList(cmd.selectedJobGroups)
        List<Page> allPages = Page.findAllByNameInList(cmd.selectedPages)

        List<BarChartAggregation> aggregationsForMeasurands = aggregateMeasurandForPageAggregation(allMeasurands, from, to, allJobGroups, allPages)
        List<BarChartAggregation> aggregationsForUserTimings = aggregateUserTimingsForPageAggregation(allUserTimings, from, to, allJobGroups, allPages)

        List<BarChartAggregation> comparatives = []
        if (cmd.fromComparative && cmd.toComparative) {
            Date comparativeFrom =  cmd.fromComparative.toDate()
            Date comparativeTo =  cmd.toComparative.toDate()
            List<BarChartAggregation> comparativesForMeasurands = aggregateMeasurandForPageAggregation(allMeasurands, comparativeFrom, comparativeTo, allJobGroups, allPages)
            List<BarChartAggregation> comparativesForUserTimings = aggregateUserTimingsForPageAggregation(allUserTimings, comparativeFrom, comparativeTo, allJobGroups, allPages)
            comparatives.addAll(comparativesForMeasurands)
            comparatives.addAll(comparativesForUserTimings)
        }

        List<BarChartAggregation> result = []
        result.addAll(aggregationsForMeasurands)
        result.addAll(aggregationsForUserTimings)

        return mergeAggregationsWithComparatives(result, comparatives)
    }

    private def aggregateMeasurandForPageAggregation(List<Selected> allSelected, Date from, Date to, List<JobGroup> allJobGroups, List<Page> allPages){
        if(allSelected.size() < 1){
            return []
        }
        Closure projection = {
            projections {
                groupProperty('page')
                groupProperty('jobGroup')
                allSelected.collect {it.getDatabaseRelevantName()}.each { m ->
                    avg(m)
                }
            }
        }
        def aggregation =  aggregateFor(projection, from, to, allJobGroups, allPages, null)
        return  createListForMeasurandAggregation(allSelected, aggregation)
    }

    private def aggregateUserTimingsForPageAggregation(List<Selected> allSelected, Date from, Date to, List<JobGroup> allJobGroups, List<Page> allPages){
        if(allSelected.size() < 1){
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
        def aggregation =  aggregateFor(projection, from, to, allJobGroups, allPages, allSelected)
        return  createListForUserTimingAggregation(allSelected, aggregation)
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

    private List<BarChartAggregation> mergeAggregationsWithComparatives(def values, def comparativeValues){
        List<BarChartAggregation> result = values
            if(comparativeValues && comparativeValues.size() >= 1){
                comparativeValues.each{ comparative ->
                    BarChartAggregation matches = result.find {it.page == comparative.page && it.jobGroup == comparative.jobGroup && it.selected == comparative.selected}
                    matches.comparativeValue = comparative.value
                }
            }
        return result
    }

    private List<BarChartAggregation> createListForMeasurandAggregation(List<Selected> allSelected, def measurandAggregations){
        List<BarChartAggregation> result = []
        if(measurandAggregations && measurandAggregations.size() >= 1){
            measurandAggregations.each{ aggregation ->
                result.addAll(
                        allSelected.collect{ Selected selected ->
                            int relevantIndex = allSelected.indexOf(selected) + 2
                            Double value = selected.normalizeValue(aggregation[relevantIndex])
                            new BarChartAggregation(page: aggregation[0], jobGroup: aggregation[1], value: value, selected: selected)
                        }
                )
            }
        }
        return result
    }


    private List<BarChartAggregation> createListForUserTimingAggregation(List<Selected> allSelected, def userTimingAggregations){
        List<BarChartAggregation> result = []
        if(userTimingAggregations && userTimingAggregations.size() >= 1){
            result = userTimingAggregations.collect{ aggregation ->
                Selected selected = allSelected.find{it.name == aggregation[2] && it.selectedType == aggregation[3].selectedType}
                Double valueRaw
                if(aggregation[3] == UserTimingType.MEASURE){
                    valueRaw = aggregation[5]
                }else{
                    valueRaw = aggregation[4]
                }
                Double value = selected.normalizeValue(valueRaw)
                new BarChartAggregation(page: aggregation[0], jobGroup: aggregation[1], value: value, selected: selected)
            }
        }
        return result
    }
}

package de.iteratec.osm.barchart

import de.iteratec.osm.OsmConfigCacheService
import de.iteratec.osm.csi.Page
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.result.CachedView
import de.iteratec.osm.result.EventResult
import de.iteratec.osm.result.Selected
import de.iteratec.osm.result.SelectedType
import de.iteratec.osm.result.UserTimingType
import de.iteratec.osm.util.I18nService
import grails.transaction.Transactional

@Transactional
class BarchartAggregationService {

    OsmConfigCacheService osmConfigCacheService
    I18nService i18nService

    List<BarchartSeries> getBarchartSeriesFor(GetBarchartCommand cmd) {
        List<String> groupProperties = []
        List<JobGroup> allJobGroups = null
        if(cmd.selectedJobGroups && cmd.selectedJobGroups.size() >= 1){
            allJobGroups = JobGroup.findAllByNameInList(cmd.selectedJobGroups)
            groupProperties.add('jobGroup')
        }
        List<Page> allPages = null
        if(cmd.selectedPages && cmd.selectedPages.size() >= 1){
            allPages = Page.findAllByNameInList(cmd.selectedPages)
            groupProperties.add('page')
        }

        List<Selected> allUserTimings = cmd.selectedSeries*.measurands.flatten().collect { new Selected(it, CachedView.UNCACHED)}.findAll {it.selectedType != SelectedType.MEASURAND}
        List<Selected> allMeasurands = cmd.selectedSeries*.measurands.flatten().collect { new Selected(it, CachedView.UNCACHED)}.findAll {it.selectedType == SelectedType.MEASURAND}

        List<BarchartSeries> result = []
        result.addAll(aggregateForMeasurandOrUserTiming(groupProperties, allMeasurands, cmd, allJobGroups, allPages, true))
        result.addAll(aggregateForMeasurandOrUserTiming(groupProperties, allUserTimings, cmd, allJobGroups, allPages, false))

        return result
    }

    List<BarchartSeries> aggregateForMeasurandOrUserTiming(List<String> groupProperties,List<Selected> allSelected, GetBarchartCommand cmd, List<JobGroup> allJobGroups, List<Page> allPages, boolean isMeasurand){
        Date from =  cmd.from.toDate()
        Date to = cmd.to.toDate()
        List<BarchartSeries> aggregations = aggregateFor(groupProperties, allSelected, from, to, allJobGroups, allPages, isMeasurand)

        List<BarchartSeries> comparatives = []
        if (cmd.fromComparative && cmd.toComparative) {
            Date comparativeFrom = cmd.fromComparative.toDate()
            Date comparativeTo = cmd.toComparative.toDate()
            comparatives = aggregateFor(groupProperties, allSelected, comparativeFrom, comparativeTo, allJobGroups, allPages, isMeasurand)
        }

        return mergeAggregationsWithComparatives(aggregations, comparatives)
    }

    private def aggregateFor(List<String> groupProperties,List<Selected> allSelected, Date from, Date to, List<JobGroup> allJobGroups, List<Page> allPages, boolean isMeasurand){
        if(allSelected.size() < 1){
            return []
        }
        Closure projection = {
            projections{
                groupProperties.each{ property ->
                    groupProperty(property)
                }
                if(isMeasurand){
                    allSelected.collect {it.getDatabaseRelevantName()}.each { m ->
                        avg(m)
                    }
                }else{
                    userTimings{
                        groupProperty('name')
                        groupProperty('type')
                        avg('startTime')
                        avg('duration')
                    }
                }
            }
        }
        def aggregation =  aggregateEventResults(projection, from, to, allJobGroups, allPages, allSelected)
        return isMeasurand? createListForMeasurandAggregation(groupProperties, allSelected, aggregation) : createListForUserTimingAggregation(groupProperties, allSelected, aggregation)
    }

    private def aggregateEventResults(Closure projection, Date from, Date to, List<JobGroup> allJobGroups, List<Page> allPages, List<Selected> userTimings){
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

    private List<BarchartSeries> mergeAggregationsWithComparatives(List<BarchartSeries> values, List<BarchartSeries> comparativeValues){
        if(comparativeValues){
            comparativeValues.each{ comparative ->
                BarchartSeries matches = values.find {it == comparative}
                matches.valueComparative = comparative.value
            }
        }
        return values
    }

    private List<BarchartSeries> createListForMeasurandAggregation(List<String> groupProperties,List<Selected> allSelected, def measurandAggregations){
        List<BarchartSeries> result = []
        if(measurandAggregations && measurandAggregations.size() >= 1){
            measurandAggregations.each{ aggregation ->
                result.addAll(
                        allSelected.collect{ Selected selected ->
                            int relevantIndex = allSelected.indexOf(selected) + groupProperties.size()
                            Double value = selected.normalizeValue(aggregation[relevantIndex])
                            createBarchartSeries(groupProperties, aggregation, value, selected)
                        }
                )
            }
        }
        return result
    }

    private List<BarchartSeries> createListForUserTimingAggregation(List<String> groupProperties, List<Selected> allSelected, def userTimingAggregations){
        List<BarchartSeries> result = []
        if(userTimingAggregations && userTimingAggregations.size() >= 1){
            result = userTimingAggregations.collect{ aggregation ->
                Selected selected = allSelected.find{it.name == aggregation[groupProperties.size()] && it.selectedType == aggregation[groupProperties.size()+1].selectedType}
                Double valueRaw
                if(aggregation[groupProperties.size()+1] == UserTimingType.MEASURE){
                    valueRaw = aggregation[groupProperties.size()+3]
                }else{
                    valueRaw = aggregation[groupProperties.size()+2]
                }
                Double value = selected.normalizeValue(valueRaw)
                createBarchartSeries(groupProperties, aggregation, value, selected)
            }
        }
        return result
    }

    private BarchartSeries createBarchartSeries(List<String> groupProperties, def aggregation, Double value, Selected selected){
        BarchartSeries result = new BarchartSeries(
                unit: selected.getMeasurandGroup().unit.label,
                measurandLabel: i18nService.msg("de.iteratec.isr.measurand.${selected.name}", selected.name),
                measurand: selected.name,
                measurandGroup: selected.getMeasurandGroup(),
                value: value,
        )
        groupProperties.each {
            int index = groupProperties.indexOf(it)
            result."$it" = aggregation[index]
        }
        return result
    }
}

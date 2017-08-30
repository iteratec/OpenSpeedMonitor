package de.iteratec.osm.barchart

import de.iteratec.osm.OsmConfigCacheService
import de.iteratec.osm.csi.Page
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.result.CachedView
import de.iteratec.osm.result.SelectedMeasurand
import de.iteratec.osm.result.UserTimingType
import de.iteratec.osm.util.I18nService
import grails.transaction.Transactional

@Transactional
class BarchartAggregationService {

    OsmConfigCacheService osmConfigCacheService
    I18nService i18nService

    List<BarChartAggregation> getBarchartSeriesFor(GetBarchartCommand cmd) {
        List<JobGroup> allJobGroups = null
        if (cmd.selectedJobGroups) {
            allJobGroups = JobGroup.findAllByNameInList(cmd.selectedJobGroups)
        }
        List<Page> allPages = null
        if (cmd.selectedPages) {
            allPages = Page.findAllByNameInList(cmd.selectedPages)
        }
        List<SelectedMeasurand> allSelected = cmd.selectedSeries*.measurands.flatten().collect {
            new SelectedMeasurand(it, CachedView.UNCACHED)
        }
        Date from = cmd.from.toDate()
        Date to = cmd.to.toDate()
        Date fromComparative = null
        Date toComparative = null
        if (cmd.fromComparative && cmd.toComparative) {
            fromComparative = cmd.fromComparative.toDate()
            toComparative = cmd.toComparative.toDate()
        }

        List<BarChartAggregation> result = []
        result.addAll(aggregateWithComparativesForMeasurandOrUserTiming(allSelected.findAll {
            it.selectedType == SelectedType.MEASURAND
        }, from, to, fromComparative, toComparative, allJobGroups, allPages))
        result.addAll(aggregateWithComparativesForMeasurandOrUserTiming(allSelected.findAll {
            it.selectedType != SelectedType.MEASURAND
        }, from, to, fromComparative, toComparative, allJobGroups, allPages))

        return result
    }

    List<BarChartAggregation> aggregateWithComparativesForMeasurandOrUserTiming(List<SelectedMeasurand> allSelected, Date from, Date to, Date fromComparative, Date toComparative, List<JobGroup> allJobGroups, List<Page> allPages) {
        List<BarChartAggregation> aggregations = aggregateFor(allSelected, from, to, allJobGroups, allPages)
        List<BarChartAggregation> comparatives = []
        if (fromComparative && toComparative) {
            comparatives = aggregateFor(allSelected, fromComparative, toComparative, allJobGroups, allPages)
        }
        return mergeAggregationsWithComparatives(aggregations, comparatives)
    }

    private
    List<BarChartAggregation> aggregateFor(List<SelectedMeasurand> allSelected, Date from, Date to, List<JobGroup> allJobGroups, List<Page> allPages) {
        if (!allSelected) {
            return []
        }
        boolean hasUserTiming = allSelected.any { it.selectedType != SelectedType.MEASURAND }
        boolean hasMeasurand = allSelected.any { it.selectedType == SelectedType.MEASURAND }
        if (hasMeasurand & hasUserTiming) {
            return []
        }
        BarchartEventResultAggregationBuilder eventResultAggregationBuilder = new BarchartEventResultAggregationBuilder()
        List<Map> transformedAggregations = eventResultAggregationBuilder.aggregateFor(from, to, allJobGroups, allPages, allSelected, osmConfigCacheService.getMinValidLoadtime(), osmConfigCacheService.getMaxValidLoadtime())
        return hasMeasurand ? createListForMeasurandAggregation(allSelected, transformedAggregations) : createListForUserTimingAggregation(allSelected, transformedAggregations)
    }

    private List<BarChartAggregation> mergeAggregationsWithComparatives(List<BarChartAggregation> values, List<BarChartAggregation> comparativeValues) {
        if (comparativeValues) {
            comparativeValues.each { comparative ->
                BarChartAggregation matches = values.find { it == comparative }
                matches.valueComparative = comparative.value
            }
        }
        return values
    }

    private List<BarChartAggregation> createListForMeasurandAggregation(List<SelectedMeasurand> allSelected, List<Map> measurandAggregations) {
        List<BarChartAggregation> result = []
        measurandAggregations.each { aggregation ->
            result.addAll(
                    allSelected.collect { SelectedMeasurand selected ->
                        new BarChartAggregation(
                                value: selected.normalizeValue(aggregation."${selected.getDatabaseRelevantName()}"),
                                selectedMeasurand: selected,
                                jobGroup: aggregation.jobGroup,
                                page: aggregation.page
                        )
                    }
            )
        }
        return result
    }

    private List<BarChartAggregation> createListForUserTimingAggregation(List<SelectedMeasurand> allSelected, List<Map> userTimingAggregations) {
        return userTimingAggregations.collect { aggregation ->
            SelectedMeasurand selected = allSelected.find {
                it.name == aggregation.name && it.selectedType == aggregation.type.selectedType
            }
            Double valueRaw = aggregation.type == UserTimingType.MEASURE? aggregation.duration : aggregation.startTime
            new BarChartAggregation(
                    value: selected.normalizeValue(valueRaw),
                    selectedMeasurand:  selected,
                    jobGroup: aggregation.jobGroup,
                    page: aggregation.page
            )
        }
    }

}

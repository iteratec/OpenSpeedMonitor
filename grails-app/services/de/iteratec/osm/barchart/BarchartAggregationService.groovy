package de.iteratec.osm.barchart

import de.iteratec.osm.OsmConfigCacheService
import de.iteratec.osm.csi.Page
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.result.CachedView
import de.iteratec.osm.result.dao.EventResultProjection
import de.iteratec.osm.result.dao.EventResultQueryBuilder
import de.iteratec.osm.result.SelectedMeasurand
import de.iteratec.osm.util.I18nService
import de.iteratec.osm.util.PerformanceLoggingService
import grails.transaction.Transactional

@Transactional
class BarchartAggregationService {

    OsmConfigCacheService osmConfigCacheService
    I18nService i18nService
    PerformanceLoggingService performanceLoggingService


    List<BarchartAggregation> getBarchartAggregationsFor(GetBarchartCommand cmd) {
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

        return aggregateWithComparativesForMeasurandOrUserTiming(allSelected, from, to, fromComparative, toComparative, allJobGroups, allPages)
    }

    List<BarchartAggregation> aggregateWithComparativesForMeasurandOrUserTiming(List<SelectedMeasurand> selectedMeasurands, Date from, Date to, Date fromComparative, Date toComparative, List<JobGroup> allJobGroups, List<Page> allPages) {
        List<BarchartAggregation> aggregations = aggregateFor(selectedMeasurands, from, to, allJobGroups, allPages)
        List<BarchartAggregation> comparatives = []
        if (fromComparative && toComparative) {
            comparatives = aggregateFor(selectedMeasurands, fromComparative, toComparative, allJobGroups, allPages)
        }
        return mergeAggregationsWithComparatives(aggregations, comparatives)
    }

    List<BarchartAggregation> aggregateFor(List<SelectedMeasurand> selectedMeasurands, Date from, Date to, List<JobGroup> jobGroups, List<Page> pages) {
        if (!selectedMeasurands) {
            return []
        }
        EventResultQueryBuilder builder = new EventResultQueryBuilder(osmConfigCacheService.getMinValidLoadtime(), osmConfigCacheService.getMaxValidLoadtime())
                .withJobResultDateBetween(from, to)
                .withPageIn(pages)
                .withJobGroupIn(jobGroups)
                .withSelectedMeasurands(selectedMeasurands)

        List<BarchartAggregation> averages = createListForEventResultProjection(selectedMeasurands, builder.getAverages())
        List<BarchartAggregation> medians = createListForEventResultProjection(selectedMeasurands, builder.getMedians())

        return mergeWithMedians(averages, medians)
    }

    private List<BarchartAggregation> mergeWithMedians(List<BarchartAggregation> avgs, List<BarchartAggregation> medians) {
        avgs.each { avg ->
            BarchartAggregation median = medians.find { it == avg }
            avg.median = median.value
        }
        return avgs
    }

    private List<BarchartAggregation> mergeAggregationsWithComparatives(List<BarchartAggregation> values, List<BarchartAggregation> comparativeValues) {
        if (comparativeValues) {
            comparativeValues.each { comparative ->
                BarchartAggregation matches = values.find { it == comparative }
                matches.valueComparative = comparative.value
                matches.medianComparative = comparative.median
            }
        }
        return values
    }

    private List<BarchartAggregation> createListForEventResultProjection(List<SelectedMeasurand> selectedMeasurands, List<EventResultProjection> measurandAggregations) {
        List<BarchartAggregation> result = []
        measurandAggregations.each { aggregation ->
            result += selectedMeasurands.collect { SelectedMeasurand selected ->
                new BarchartAggregation(
                        value: selected.normalizeValue(aggregation."${selected.getDatabaseRelevantName()}"),
                        selectedMeasurand: selected,
                        jobGroup: aggregation.jobGroup,
                        page: aggregation.page,
                )
            }
        }
        return result
    }
}

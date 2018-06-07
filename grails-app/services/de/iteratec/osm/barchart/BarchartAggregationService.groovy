package de.iteratec.osm.barchart

import de.iteratec.osm.OsmConfigCacheService
import de.iteratec.osm.csi.Page
import de.iteratec.osm.d3Data.GetPageComparisonDataCommand
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.result.CachedView
import de.iteratec.osm.result.dao.EventResultProjection
import de.iteratec.osm.result.dao.EventResultQueryBuilder
import de.iteratec.osm.result.SelectedMeasurand
import de.iteratec.osm.util.I18nService
import grails.transaction.Transactional

@Transactional
class BarchartAggregationService {

    OsmConfigCacheService osmConfigCacheService
    I18nService i18nService

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
        selectedMeasurands.unique({a, b -> a.name <=> b.name})
        List<EventResultProjection> projections = new EventResultQueryBuilder(osmConfigCacheService.getMinValidLoadtime(), osmConfigCacheService.getMaxValidLoadtime())
                .withJobResultDateBetween(from, to)
                .withPageIn(pages, true)
                .withJobGroupIn(jobGroups, true)
                .withSelectedMeasurandsAverageProjection(selectedMeasurands)
                .getResults()

        return createListForEventResultProjection(selectedMeasurands, projections)
    }


    List<PageComparisonAggregation> getBarChartAggregationsFor(GetPageComparisonDataCommand cmd){
        List<PageComparisonAggregation> comparisons = []
        List<Page> pages = []
        List<JobGroup> jobGroups = []
        cmd.selectedPageComparisons.each {
            pages << Page.get(it.firstPageId)
            pages << Page.get(it.secondPageId)
            jobGroups << JobGroup.get(it.firstJobGroupId)
            jobGroups << JobGroup.get(it.secondJobGroupId)
        }
        SelectedMeasurand measurand = new SelectedMeasurand(cmd.measurand, CachedView.UNCACHED)
        List<BarchartAggregation> aggregations = aggregateFor([measurand], cmd.from.toDate(), cmd.to.toDate(), jobGroups, pages)
        cmd.selectedPageComparisons.each { comparison ->
            PageComparisonAggregation pageComparisonAggregation = new PageComparisonAggregation()
            pageComparisonAggregation.baseAggregation = aggregations.find { aggr -> aggr.jobGroup.id == (comparison.firstJobGroupId as long) && aggr.page.id == (comparison.firstPageId as long) }
            pageComparisonAggregation.comperativeAggregation = aggregations.find { aggr -> aggr.jobGroup.id == (comparison.secondJobGroupId as long) && aggr.page.id == (comparison.secondPageId as long) }
            comparisons << pageComparisonAggregation
        }
        return comparisons
    }


    private List<BarchartAggregation> mergeAggregationsWithComparatives(List<BarchartAggregation> values, List<BarchartAggregation> comparativeValues) {
        if (comparativeValues) {
            comparativeValues.each { comparative ->
                BarchartAggregation matches = values.find { it == comparative }
                matches.valueComparative = comparative.value
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
                        page: aggregation.page
                )
            }
        }
        return result
    }
}

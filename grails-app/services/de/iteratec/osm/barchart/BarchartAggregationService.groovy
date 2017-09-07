package de.iteratec.osm.barchart

import de.iteratec.osm.OsmConfigCacheService
import de.iteratec.osm.csi.Page
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.result.CachedView
import de.iteratec.osm.result.SelectedMeasurand
import de.iteratec.osm.result.SelectedMeasurandType
import de.iteratec.osm.result.UserTimingType
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

        List<BarchartAggregation> result = []
        result.addAll(aggregateWithComparativesForMeasurandOrUserTiming(allSelected.findAll {
            it.selectedType == SelectedMeasurandType.MEASURAND
        }, from, to, fromComparative, toComparative, allJobGroups, allPages))
        result.addAll(aggregateWithComparativesForMeasurandOrUserTiming(allSelected.findAll {
            it.selectedType != SelectedMeasurandType.MEASURAND
        }, from, to, fromComparative, toComparative, allJobGroups, allPages))

        return result
    }

    List<BarchartAggregation> aggregateWithComparativesForMeasurandOrUserTiming(List<SelectedMeasurand> selectedMeasurands, Date from, Date to, Date fromComparative, Date toComparative, List<JobGroup> allJobGroups, List<Page> allPages) {
        List<BarchartAggregation> aggregations = aggregateFor(selectedMeasurands, from, to, allJobGroups, allPages)
        List<BarchartAggregation> comparatives = []
        if (fromComparative && toComparative) {
            comparatives = aggregateFor(selectedMeasurands, fromComparative, toComparative, allJobGroups, allPages)
        }
        return mergeAggregationsWithComparatives(aggregations, comparatives)
    }

    List<BarchartAggregation> aggregateFor(List<SelectedMeasurand> selectedMeasurands, Date from, Date to, List<JobGroup> allJobGroups, List<Page> allPages) {
        if (!selectedMeasurands) {
            return []
        }
        boolean hasUserTiming = selectedMeasurands.any { it.selectedType != SelectedMeasurandType.MEASURAND }
        boolean hasMeasurand = selectedMeasurands.any { it.selectedType == SelectedMeasurandType.MEASURAND }
        if (hasMeasurand & hasUserTiming) {
            return []
        }

        List<Map> transformedAggregations = new EventResultProjectionBuilder(osmConfigCacheService.getMinValidLoadtime(), osmConfigCacheService.getMaxValidLoadtime())
                .withJobResultDateBetween(from, to)
                .withPageIn(allPages, true)
                .withJobGroupIn(allJobGroups, true)
                .withSelectedMeasurandsAveragesProjection(selectedMeasurands)
                .getResults()

        return hasMeasurand ? createListForMeasurandAggregation(selectedMeasurands, transformedAggregations) : createListForUserTimingAggregation(selectedMeasurands, transformedAggregations)
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

    private List<BarchartAggregation> createListForMeasurandAggregation(List<SelectedMeasurand> selectedMeasurands, List<Map> measurandAggregations) {
        List<BarchartAggregation> result = []
        measurandAggregations.each { aggregation ->
            result.addAll(
                    selectedMeasurands.collect { SelectedMeasurand selected ->
                        new BarchartAggregation(
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

    private List<BarchartAggregation> createListForUserTimingAggregation(List<SelectedMeasurand> selectedMeasurands, List<Map> userTimingAggregations) {
        return userTimingAggregations.collect { aggregation ->
            SelectedMeasurand selected = selectedMeasurands.find {
                it.name == aggregation.name && it.selectedType == aggregation.type.selectedMeasurandType
            }
            Double valueRaw = aggregation.type == UserTimingType.MEASURE ? aggregation.duration : aggregation.startTime
            new BarchartAggregation(
                    value: selected.normalizeValue(valueRaw),
                    selectedMeasurand: selected,
                    jobGroup: aggregation.jobGroup,
                    page: aggregation.page
            )
        }
    }

}

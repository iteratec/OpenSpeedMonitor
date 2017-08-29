package de.iteratec.osm.barchart

import de.iteratec.osm.OsmConfigCacheService
import de.iteratec.osm.csi.Page
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.result.CachedView
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
        if (cmd.selectedJobGroups && cmd.selectedJobGroups.size() >= 1) {
            allJobGroups = JobGroup.findAllByNameInList(cmd.selectedJobGroups)
            groupProperties.add('jobGroup')
        }
        List<Page> allPages = null
        if (cmd.selectedPages && cmd.selectedPages.size() >= 1) {
            allPages = Page.findAllByNameInList(cmd.selectedPages)
            groupProperties.add('page')
        }

        List<Selected> allSelected = cmd.selectedSeries*.measurands.flatten().collect {
            new Selected(it, CachedView.UNCACHED)
        }

        List<BarchartSeries> result = []
        result.addAll(aggregateForMeasurandOrUserTiming(groupProperties, allSelected.findAll {
            it.selectedType == SelectedType.MEASURAND
        }, cmd, allJobGroups, allPages))
        result.addAll(aggregateForMeasurandOrUserTiming(groupProperties, allSelected.findAll {
            it.selectedType != SelectedType.MEASURAND
        }, cmd, allJobGroups, allPages))

        return result
    }

    List<BarchartSeries> aggregateForMeasurandOrUserTiming(List<String> groupProperties, List<Selected> allSelected, GetBarchartCommand cmd, List<JobGroup> allJobGroups, List<Page> allPages) {
        Date from = cmd.from.toDate()
        Date to = cmd.to.toDate()
        List<BarchartSeries> aggregations = aggregateFor(groupProperties, allSelected, from, to, allJobGroups, allPages)

        List<BarchartSeries> comparatives = []
        if (cmd.fromComparative && cmd.toComparative) {
            Date fromComparative = cmd.fromComparative.toDate()
            Date toComparative = cmd.toComparative.toDate()
            comparatives = aggregateFor(groupProperties, allSelected, fromComparative, toComparative, allJobGroups, allPages)
        }

        return mergeAggregationsWithComparatives(aggregations, comparatives)
    }

    private
    List<BarchartSeries> aggregateFor(List<String> groupProperties, List<Selected> allSelected, Date from, Date to, List<JobGroup> allJobGroups, List<Page> allPages) {
        if (allSelected.size() < 1) {
            return []
        }
        boolean isUserTiming = allSelected.any { it.selectedType != SelectedType.MEASURAND }
        boolean isMeasurand = allSelected.any { it.selectedType == SelectedType.MEASURAND }
        if (isMeasurand & isUserTiming) {
            return []
        }
        BarchartEventResultAggregationBuilder eventResultAggregationBuilder = new BarchartEventResultAggregationBuilder()

        List<Map> transformedAggregations = eventResultAggregationBuilder.aggregateFor(groupProperties, from, to, allJobGroups, allPages, allSelected, osmConfigCacheService.getMinValidLoadtime(), osmConfigCacheService.getMaxValidLoadtime())
        return isMeasurand ? createListForMeasurandAggregation(allSelected, transformedAggregations) : createListForUserTimingAggregation(allSelected, transformedAggregations)
    }

    private List<BarchartSeries> mergeAggregationsWithComparatives(List<BarchartSeries> values, List<BarchartSeries> comparativeValues) {
        if (comparativeValues) {
            comparativeValues.each { comparative ->
                BarchartSeries matches = values.find { it == comparative }
                matches.valueComparative = comparative.value
            }
        }
        return values
    }

    private List<BarchartSeries> createListForMeasurandAggregation(List<Selected> allSelected, List<Map> measurandAggregations) {
        List<BarchartSeries> result = []

        measurandAggregations.each { aggregation ->
            result.addAll(
                    allSelected.collect { Selected selected ->
                        String key = selected.getDatabaseRelevantName()
                        new BarchartSeries(
                                value: selected.normalizeValue(aggregation."$key"),
                                unit: selected.getMeasurandGroup().unit.label,
                                measurandLabel: i18nService.msg("de.iteratec.isr.measurand.${selected.name}", selected.name),
                                measurand: selected.name,
                                measurandGroup: selected.getMeasurandGroup(),
                                jobGroup: aggregation.jobGroup,
                                page: aggregation.page
                        )
                    }
            )
        }
        return result
    }

    private List<BarchartSeries> createListForUserTimingAggregation(List<Selected> allSelected, List<Map> userTimingAggregations) {
        return userTimingAggregations.collect { aggregation ->
            Selected selected = allSelected.find {
                it.name == aggregation.name && it.selectedType == aggregation.type.selectedType
            }
            Double valueRaw
            if (aggregation.type == UserTimingType.MEASURE) {
                valueRaw = aggregation.duration
            } else {
                valueRaw = aggregation.startTime
            }
            new BarchartSeries(
                    value: selected.normalizeValue(valueRaw),
                    unit: selected.getMeasurandGroup().unit.label,
                    measurandLabel: i18nService.msg("de.iteratec.isr.measurand.${selected.name}", selected.name),
                    measurand: selected.name,
                    measurandGroup: selected.getMeasurandGroup(),
                    jobGroup: aggregation.jobGroup,
                    page: aggregation.page
            )
        }
    }

}

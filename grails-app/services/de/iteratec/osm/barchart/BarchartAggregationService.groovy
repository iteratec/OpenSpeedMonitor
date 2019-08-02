package de.iteratec.osm.barchart

import de.iteratec.osm.OsmConfigCacheService
import de.iteratec.osm.csi.Page
import de.iteratec.osm.d3Data.GetPageComparisonDataCommand
import de.iteratec.osm.measurement.environment.Browser
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.result.CachedView
import de.iteratec.osm.result.DeviceType
import de.iteratec.osm.result.OperatingSystem
import de.iteratec.osm.result.PerformanceAspectType
import de.iteratec.osm.result.SelectedMeasurand
import de.iteratec.osm.result.dao.EventResultProjection
import de.iteratec.osm.result.dao.EventResultQueryBuilder
import de.iteratec.osm.util.I18nService
import grails.gorm.transactions.Transactional

@Transactional
class BarchartAggregationService {

    OsmConfigCacheService osmConfigCacheService
    I18nService i18nService

    List<BarchartAggregation> getBarchartAggregationsFor(GetBarchartCommand cmd) {
        List<JobGroup> allJobGroups = null
        if (cmd.jobGroups) {
            allJobGroups = JobGroup.findAllByIdInList(cmd.jobGroups)
        }
        List<Page> allPages = null
        if (cmd.pages) {
            allPages = Page.findAllByIdInList(cmd.pages)
        }
        List<SelectedMeasurand> measurands = cmd.measurands.collect {
            new SelectedMeasurand(it, CachedView.UNCACHED)
        }
        Date from = cmd.from.toDate()
        Date to = cmd.to.toDate()
        Date fromComparative = null
        Date toComparative = null

        List<DeviceType> deviceTypes = []
        if (cmd.deviceTypes) {
            deviceTypes = cmd.deviceTypes.collect{it.toString().toUpperCase() as DeviceType}
        }

        List<OperatingSystem> operatingSystems = []
        if (cmd.operatingSystems) {
            operatingSystems = cmd.operatingSystems.collect{it.toString().toUpperCase() as OperatingSystem}
        }

        if (cmd.fromComparative && cmd.toComparative) {
            fromComparative = cmd.fromComparative.toDate()
            toComparative = cmd.toComparative.toDate()
        }

        List<PerformanceAspectType> performanceAspectTypes = []
        if (cmd.performanceAspectTypes) {
            performanceAspectTypes = cmd.performanceAspectTypes.collect{it.toString().toUpperCase() as PerformanceAspectType}
        }

        return aggregateWithComparativesForMeasurandOrUserTiming(measurands, from, to, fromComparative, toComparative, allJobGroups, allPages, cmd.aggregationValue, cmd.browsers, deviceTypes, operatingSystems, performanceAspectTypes)
    }

    List<BarchartAggregation> aggregateWithComparativesForMeasurandOrUserTiming(List<SelectedMeasurand> selectedMeasurands, Date from, Date to, Date fromComparative, Date toComparative,
                                                                                List<JobGroup> allJobGroups, List<Page> allPages, String selectedAggregationValue, List<Long> browserIds,
                                                                                List<DeviceType> deviceTypes, List<OperatingSystem> operatingSystems, List<PerformanceAspectType> performanceAspectTypes) {
        List<BarchartAggregation> aggregations = aggregateFor(selectedMeasurands, from, to, allJobGroups, allPages, selectedAggregationValue, browserIds, deviceTypes, operatingSystems, performanceAspectTypes)
        List<BarchartAggregation> comparatives = []
        if (fromComparative && toComparative) {
            comparatives = aggregateFor(selectedMeasurands, fromComparative, toComparative, allJobGroups, allPages, selectedAggregationValue, browserIds, deviceTypes, operatingSystems, performanceAspectTypes)
        }
        return mergeAggregationsWithComparatives(aggregations, comparatives)
    }

    List<BarchartAggregation> aggregateFor(List<SelectedMeasurand> selectedMeasurands, Date from, Date to, List<JobGroup> jobGroups, List<Page> pages,
                                           String selectedAggregationValue, List<Long> browserIds, List<DeviceType> deviceTypes, List<OperatingSystem> operatingSystems, List<PerformanceAspectType> performanceAspectTypes) {
        if (!selectedMeasurands && !performanceAspectTypes) {
            return []
        }
        if (selectedMeasurands) {
            selectedMeasurands.unique({ a, b -> a.name <=> b.name })
        }

        EventResultQueryBuilder queryBuilder = new EventResultQueryBuilder()
                .withJobResultDateBetween(from, to)
                .withSelectedMeasurands(selectedMeasurands)
                .withJobGroupIn(jobGroups)

        if (pages) {
            queryBuilder = queryBuilder.withPageIn(pages)
        } else {
            queryBuilder = queryBuilder.withoutPagesIn([Page.findByName(Page.UNDEFINED)])
        }

        if (browserIds) {
            queryBuilder = queryBuilder.withBrowserIdsIn(browserIds)
        }

        if (deviceTypes) {
            queryBuilder = queryBuilder.withDeviceTypes(deviceTypes)
        }

        if (operatingSystems) {
            queryBuilder = queryBuilder.withOperatingSystems(operatingSystems)
        }

        if (performanceAspectTypes) {
            performanceAspectTypes.unique()
            queryBuilder = queryBuilder.withPerformanceAspects(performanceAspectTypes)
        }

        List<EventResultProjection> eventResultProjections = []
        switch (selectedAggregationValue) {
            case 'avg':
                eventResultProjections = queryBuilder.getAverageData()
                break
            default:
                try {
                    int percentage = Integer.parseInt(selectedAggregationValue)
                    eventResultProjections = queryBuilder.getPercentile(percentage)
                }
                catch (Exception e) {
                    e.printStackTrace()
                }
        }
        return createListForEventResultProjection(selectedAggregationValue, selectedMeasurands, eventResultProjections, jobGroups, pages, performanceAspectTypes)
    }

    List<PageComparisonAggregation> getBarChartAggregationsFor(GetPageComparisonDataCommand cmd) {
        List<PageComparisonAggregation> comparisons = []
        List<Page> pages = []
        List<JobGroup> jobGroups = []
        List<Long> browsers = []
        cmd.selectedPageComparisons.each {
            pages << Page.get(it.firstPageId)
            pages << Page.get(it.secondPageId)
            jobGroups << JobGroup.get(it.firstJobGroupId)
            jobGroups << JobGroup.get(it.secondJobGroupId)
        }

        List<DeviceType> deviceTypes = []
        if (cmd.selectedDeviceTypes) {
            deviceTypes = cmd.selectedDeviceTypes.collect{it.toString().toUpperCase() as DeviceType}
        }

        List<OperatingSystem> operatingSystems = []
        if (cmd.selectedOperatingSystems) {
            operatingSystems = cmd.selectedOperatingSystems.collect{it.toString().toUpperCase() as OperatingSystem}
        }

        SelectedMeasurand measurand = new SelectedMeasurand(cmd.measurand, CachedView.UNCACHED)
        List<BarchartAggregation> aggregations = aggregateFor([measurand], cmd.from.toDate(), cmd.to.toDate(), jobGroups, pages, cmd.selectedAggregationValue, browsers, deviceTypes, operatingSystems, [])
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
                if (matches) matches.valueComparative = comparative.value
            }
        }
        return values
    }

    private List<BarchartAggregation> createListForEventResultProjection(String selectedAggregationValue, List<SelectedMeasurand> selectedMeasurands, List<EventResultProjection> measurandAggregations, List<JobGroup> jobGroups, List<Page> pages, List<PerformanceAspectType> performanceAspectTypes) {
        List<BarchartAggregation> result = []
        measurandAggregations.each { aggregation ->
            JobGroup jobGroup = jobGroups.find { it.id == aggregation.jobGroupId }
            Page page = pages.find { it.id == aggregation.pageId }
            Browser browser = Browser.findById(aggregation.browserId)
            DeviceType deviceType = aggregation?.deviceType
            OperatingSystem operatingSystem = aggregation?.operatingSystem
            result += performanceAspectTypes.collect { PerformanceAspectType aspectType ->
                new BarchartAggregation(
                        value: aggregation."${aspectType.name()}",
                        performanceAspectType: aspectType,
                        jobGroup: jobGroup,
                        page: page,
                        browser: browser ?: null,
                        aggregationValue: selectedAggregationValue,
                        deviceType: deviceType?.deviceTypeLabel,
                        operatingSystem: operatingSystem?.OSLabel
                )
            }
            result += selectedMeasurands.collect { SelectedMeasurand selected ->
                new BarchartAggregation(
                        value: selected.normalizeValue(aggregation."${selected.getDatabaseRelevantName()}"),
                        selectedMeasurand: selected,
                        jobGroup: jobGroup,
                        page: page,
                        browser: browser ?: null,
                        aggregationValue: selectedAggregationValue,
                        deviceType: deviceType?.deviceTypeLabel,
                        operatingSystem: operatingSystem?.OSLabel
                )
            }
        }
        return result
    }
}

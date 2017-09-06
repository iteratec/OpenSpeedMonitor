package de.iteratec.osm.result

import de.iteratec.osm.OsmConfigCacheService
import de.iteratec.osm.annotations.RestAction
import de.iteratec.osm.barchart.BarchartAggregation
import de.iteratec.osm.barchart.BarchartAggregationService
import de.iteratec.osm.csi.Page
import de.iteratec.osm.d3Data.GetPageComparisonDataCommand
import de.iteratec.osm.barchart.BarchartDTO
import de.iteratec.osm.barchart.BarchartDatum
import de.iteratec.osm.barchart.BarchartSeries
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.util.ControllerUtils
import de.iteratec.osm.util.ExceptionHandlerController
import de.iteratec.osm.util.I18nService

class PageComparisonController extends ExceptionHandlerController {

    public final static String DATE_FORMAT_STRING_FOR_HIGH_CHART = 'dd.mm.yyyy';
    public final static int MONDAY_WEEKSTART = 1

    I18nService i18nService
    OsmConfigCacheService osmConfigCacheService
    BarchartAggregationService barchartAggregationService

    def index() { redirect(action: 'show') }

    def show() {
        Map<String, Object> modelToRender = [:]

        modelToRender.put("pages", Page.list().collectEntries { [it.id, it.name] })
        modelToRender.put("jobGroups", JobGroup.list().collectEntries { [it.id, it.name] })

        modelToRender.put("aggrGroupValuesUnCached", SelectedMeasurand.createDataMapForOptGroupSelect())
        modelToRender.put("selectedAggrGroupValuesUnCached", [])

        // JavaScript-Utility-Stuff:
        modelToRender.put("dateFormat", DATE_FORMAT_STRING_FOR_HIGH_CHART)
        modelToRender.put("weekStart", MONDAY_WEEKSTART)

        return modelToRender
    }

    @RestAction
    def getBarchartData(GetPageComparisonDataCommand cmd) {
        JobGroup baseJobGroup = JobGroup.get((cmd.selectedPageComparisons[0].jobGroupId1) as long)
        JobGroup comparativeJobGroup = JobGroup.get((cmd.selectedPageComparisons[0].jobGroupId2) as long)

        Page basePage = Page.get((cmd.selectedPageComparisons[0].pageId1) as long)
        Page comparativePage = Page.get((cmd.selectedPageComparisons[0].pageId1) as long)

        SelectedMeasurand selectedMeasurand = new SelectedMeasurand(cmd.measurand, CachedView.UNCACHED)
        List<BarchartAggregation> aggregations = barchartAggregationService.aggregateFor([selectedMeasurand], cmd.from.toDate(), cmd.to.toDate(), [baseJobGroup, comparativeJobGroup], [basePage, comparativePage])

        if (!aggregations || aggregations.every { it.value == null }) {
            ControllerUtils.sendObjectAsJSON(response, [:])
        }

        BarchartDTO dto = new BarchartDTO()
        dto.i18nMap.put("measurand", i18nService.msg("de.iteratec.result.measurand.label", "Measurand"))
        dto.i18nMap.put("jobGroup", i18nService.msg("de.iteratec.isr.wptrd.labels.filterFolder", "JobGroup"))
        dto.i18nMap.put("page", i18nService.msg("de.iteratec.isr.wptrd.labels.filterPage", "Page"))
        dto.series = [createSeriesFor(aggregations, baseJobGroup, comparativeJobGroup, basePage, comparativePage)]

        ControllerUtils.sendObjectAsJSON(response, dto)
    }

    private BarchartSeries createSeriesFor(List<BarchartAggregation> aggregations, JobGroup baseJobGroup, JobGroup comparativeJobGroup, Page basePage, Page comparativePage) {
        BarchartDatum baseSeries = mapToSeriesFor(aggregations, basePage, baseJobGroup)
        BarchartDatum comparativeSeries = mapToSeriesFor(aggregations, comparativePage, comparativeJobGroup)
        return new BarchartSeries(
                stacked: false,
                dimensionalUnit: aggregations[0].selectedMeasurand.getMeasurandGroup().unit.label,
                data: [baseSeries, comparativeSeries]
        )
    }

    private BarchartDatum mapToSeriesFor(List<BarchartAggregation> aggregations, Page page, JobGroup jobGroup) {
        BarchartAggregation aggregation = aggregations.find { it.page == page && it.jobGroup == jobGroup }
        return new BarchartDatum(
                measurand: i18nService.msg("de.iteratec.isr.measurand.${aggregation.selectedMeasurand.name}", aggregation.selectedMeasurand.name),
                value: aggregation.value,
                grouping: "${aggregation.jobGroup} | ${aggregation.page}")
    }
}

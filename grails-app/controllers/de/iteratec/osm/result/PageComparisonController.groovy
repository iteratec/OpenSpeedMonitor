package de.iteratec.osm.result

import de.iteratec.osm.annotations.RestAction
import de.iteratec.osm.csi.Page
import de.iteratec.osm.d3Data.GetPageComparisonDataCommand
import de.iteratec.osm.dimple.BarchartDTO
import de.iteratec.osm.dimple.BarchartDatum
import de.iteratec.osm.dimple.BarchartSeries
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.util.ControllerUtils
import de.iteratec.osm.util.ExceptionHandlerController
import de.iteratec.osm.util.MeasurandUtilService

class PageComparisonController extends ExceptionHandlerController {

    public final static String DATE_FORMAT_STRING_FOR_HIGH_CHART = 'dd.mm.yyyy';
    public final static int MONDAY_WEEKSTART = 1

    MeasurandUtilService measurandUtilService

    def index() { redirect(action: 'show') }

    def show() {
        Map<String, Object> modelToRender = [:]

        modelToRender.put("pages", Page.list().collectEntries { [it.id, it.name] })
        modelToRender.put("jobGroups", JobGroup.list().collectEntries { [it.id, it.name] })

        modelToRender.put("aggrGroupValuesUnCached", ResultCsiAggregationService.getAggregatorMapForOptGroupSelect().get(CachedView.UNCACHED))
        modelToRender.put("selectedAggrGroupValuesUnCached", [])

        // JavaScript-Utility-Stuff:
        modelToRender.put("dateFormat", DATE_FORMAT_STRING_FOR_HIGH_CHART)
        modelToRender.put("weekStart", MONDAY_WEEKSTART)

        return modelToRender
    }

    @RestAction
    def getBarchartData(GetPageComparisonDataCommand cmd) {
        List<Page> allPages = Page.getAll(cmd.selectedPageComparisons.collect {
            [it['pageId1'] as long, it['pageId2'] as long]
        }.flatten().unique())

        List<JobGroup> allJobGroups = JobGroup.getAll(cmd.selectedPageComparisons.collect {
            [it['jobGroupId1'] as long, it['jobGroupId2'] as long]
        }.flatten().unique())

        List allEventResults = EventResult.createCriteria().list {
            'in'('page', allPages)
            'in'('jobGroup', allJobGroups)
            'between'('jobResultDate', cmd.from.toDate(), cmd.to.toDate())
            projections {
                groupProperty('jobGroup.id')
                groupProperty('page.id')
                avg(cmd.measurand)
            }
        }

        if (!allEventResults || allEventResults.every { it[2] == null }) {
            ControllerUtils.sendObjectAsJSON(response, [:])
        }

        BarchartDTO dto = new BarchartDTO()
        barchartDTO.i18nMap.put("measurand", i18nService.msg("de.iteratec.result.measurand.label", "Measurand"))
        barchartDTO.i18nMap.put("jobGroup", i18nService.msg("de.iteratec.isr.wptrd.labels.filterFolder", "JobGroup"))
        barchartDTO.i18nMap.put("page", i18nService.msg("de.iteratec.isr.wptrd.labels.filterPage", "Page"))

        cmd.selectedPageComparisons.each { row ->
            BarchartSeries series = new BarchartSeries(stacked: false, dimensionalUnit: measurandUtilService.getDimensionalUnit(cmd.measurand))
            BarchartDatum datum1 = new BarchartDatum(measurand: cmd.measurand)
            BarchartDatum datum2 = new BarchartDatum(measurand: cmd.measurand)
            def result1 = allEventResults.find {
                it[0].toString() == row['jobGroupId1'] &&
                        it[1].toString() == row['pageId1']
            }
            def result2 = allEventResults.find {
                it[0].toString() == row['jobGroupId2'] &&
                        it[1].toString() == row['pageId2']
            }
            if (result1) {
                datum1.value = result1 ? result1[2] : null
                datum1.grouping = allPages.find {
                    it.id.toString() == row['pageId1']
                }.name + " | " + allJobGroups.find { it.id.toString() == row['jobGroupId1'] }.name
                series.data << datum1
            }
            if (result2) {
                datum2.value = result2 ? result2[2] : null
                datum2.grouping = allPages.find {
                    it.id.toString() == row['pageId2']
                }.name + " | " + allJobGroups.find { it.id.toString() == row['jobGroupId2'] }.name
                series.data << datum2
            }
            if (series.data) {
                dto.series << series
            }
        }

        ControllerUtils.sendObjectAsJSON(response, dto)
    }
}

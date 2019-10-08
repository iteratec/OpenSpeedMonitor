package de.iteratec.osm.result

import de.iteratec.osm.OsmConfigCacheService
import de.iteratec.osm.annotations.RestAction
import de.iteratec.osm.barchart.BarchartAggregation
import de.iteratec.osm.barchart.BarchartAggregationService
import de.iteratec.osm.barchart.GetBarchartCommand
import de.iteratec.osm.csi.Page
import de.iteratec.osm.measurement.schedule.Job
import de.iteratec.osm.measurement.schedule.JobDaoService
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.measurement.schedule.JobGroupService
import de.iteratec.osm.measurement.script.PlaceholdersUtility
import de.iteratec.osm.measurement.script.Script
import de.iteratec.osm.measurement.script.ScriptParser
import de.iteratec.osm.result.dto.AggregationChartDTO
import de.iteratec.osm.result.dto.AggregationChartSeriesDTO
import de.iteratec.osm.util.ControllerUtils
import de.iteratec.osm.util.ExceptionHandlerController
import de.iteratec.osm.util.I18nService
import de.iteratec.osm.util.PerformanceLoggingService
import org.springframework.http.HttpStatus

class AggregationLegacyController extends ExceptionHandlerController {

    public final static String DATE_FORMAT_STRING_FOR_HIGH_CHART = 'dd.mm.yyyy';
    public final static int MONDAY_WEEKSTART = 1

    JobGroupService jobGroupService
    JobDaoService jobDaoService
    EventResultDashboardService eventResultDashboardService
    I18nService i18nService
    PageService pageService
    OsmConfigCacheService osmConfigCacheService
    BarchartAggregationService barchartAggregationService
    PerformanceLoggingService performanceLoggingService

    def index() {
        redirect(action: 'show')
    }

    def show() {
        Map<String, Object> modelToRender = [:]

        // AggregatorTypes
        modelToRender.put('aggrGroupValuesUnCached', SelectedMeasurand.createDataMapForOptGroupSelect())

        // JobGroups
        List<JobGroup> jobGroups = eventResultDashboardService.getAllJobGroups()
        modelToRender.put('folders', jobGroups)
        modelToRender.put('selectedFolder', params.selectedFolder)

        // Pages
        List<Page> pages = eventResultDashboardService.getAllPages()
        modelToRender.put('pages', pages)
        modelToRender.put('selectedPages', params.selectedPages)

        // JavaScript-Utility-Stuff:
        modelToRender.put("dateFormat", DATE_FORMAT_STRING_FOR_HIGH_CHART)
        modelToRender.put("weekStart", MONDAY_WEEKSTART)

        modelToRender.put('selectedAggrGroupValuesUnCached', [])

        modelToRender.put("tagToJobGroupNameMap", jobGroupService.getTagToJobGroupNameMap())

        // Done! :)
        return modelToRender
    }

    def entryAndFollow() {
        return [:]
    }

}

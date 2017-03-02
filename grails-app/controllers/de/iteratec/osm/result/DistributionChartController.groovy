package de.iteratec.osm.result

import de.iteratec.osm.annotations.RestAction
import de.iteratec.osm.csi.Page
import de.iteratec.osm.distributionData.DistributionChartDTO
import de.iteratec.osm.distributionData.DistributionTrace
import de.iteratec.osm.distributionData.GetDistributionCommand
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.measurement.schedule.dao.JobGroupDaoService
import de.iteratec.osm.util.ControllerUtils
import de.iteratec.osm.util.ExceptionHandlerController
import org.springframework.http.HttpStatus

class DistributionChartController extends ExceptionHandlerController {
    public final static Map<CachedView, Map<String, List<String>>> AGGREGATOR_GROUP_VALUES = ResultCsiAggregationService.getAggregatorMapForOptGroupSelect()

    public final static String DATE_FORMAT_STRING_FOR_HIGH_CHART = 'dd.mm.yyyy'
    public final static int MONDAY_WEEKSTART = 1

    JobGroupDaoService jobGroupDaoService
    EventResultDashboardService eventResultDashboardService

    def index() {
        redirect(action: 'show')
    }

    def show() {
        Map<String, Object> modelToRender = [:]

        // JobGroups
        List<JobGroup> jobGroups = eventResultDashboardService.getAllJobGroups()
        modelToRender.put('folders', jobGroups)

        // Pages
        List<Page> pages = eventResultDashboardService.getAllPages()
        modelToRender.put('pages', pages)

        // Measurands
        modelToRender.put('measurandsUncached', AGGREGATOR_GROUP_VALUES.get(CachedView.UNCACHED))

        // JavaScript-Utility-Stuff:
        modelToRender.put("dateFormat", DATE_FORMAT_STRING_FOR_HIGH_CHART)
        modelToRender.put("weekStart", MONDAY_WEEKSTART)

        modelToRender.put("tagToJobGroupNameMap", jobGroupDaoService.getTagToJobGroupNameMap())

        return modelToRender
    }

    /**
     * Rest Method for ajax call.
     * @param cmd The requested data.
     * @return DistributionChartDTO as JSON or string message if an error occurred
     */
    @RestAction
    def getDistributionChartData(GetDistributionCommand cmd) {
        String errorMessages = getErrorMessages(cmd)
        if (errorMessages) {
            ControllerUtils.sendSimpleResponseAsStream(response, HttpStatus.BAD_REQUEST, errorMessages)
            return
        }

        List<JobGroup> allJobGroups = JobGroup.findAllByNameInList(cmd.selectedJobGroups)
        List<Page> allPages = Page.findAllByNameInList(cmd.selectedPages)
        def selectedMeasurand = cmd.selectedMeasurand.replace("Uncached", "")

        List allEventResults = EventResult.createCriteria().list {
            'in'('page', allPages)
            'in'('jobGroup', allJobGroups)
            'between'('jobResultDate', cmd.from.toDate(), cmd.to.toDate())
        }

        // return if no data is available
        if (!allEventResults) {
            ControllerUtils.sendObjectAsJSON(response, [:])
            return
        }

        DistributionChartDTO distributionChartDTO = new DistributionChartDTO()

        allEventResults.each { result ->
            def jobGroup = result.jobGroup.toString()
            def page = result.page.toString()

            def identifier = jobGroup + "-" + page

            if (distributionChartDTO.series.get(identifier) == null) {
                distributionChartDTO.series.put(identifier, new DistributionTrace())
            }

            def newTrace = distributionChartDTO.series.get(identifier)
            newTrace.jobGroup = jobGroup
            newTrace.page = page
            newTrace.data.add(result[selectedMeasurand])
        }

        ControllerUtils.sendObjectAsJSON(response, distributionChartDTO)
    }

    /**
     * Validates the command and creates an error message string if necessary.
     * @param cmd
     * @return a string containing the error messages in html format or an empty string if the command is valid
     */
    private String getErrorMessages(GetDistributionCommand cmd) {
        String result = ""
        if (!cmd.selectedPages) {
            result += i18nService.msg("de.iteratec.osm.gui.selectedPage.error.validator.error.selectedPage", "Please select at least one page")
            result += "<br />"
        }
        if (!cmd.selectedJobGroups) {
            result += i18nService.msg("de.iteratec.osm.gui.selectedFolder.error.validator.error.selectedFolder", "Please select at least one jobGroup")
            result += "<br />"
        }
        return result
    }
}

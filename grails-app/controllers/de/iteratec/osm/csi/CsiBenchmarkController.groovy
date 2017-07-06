package de.iteratec.osm.csi

import de.iteratec.osm.annotations.RestAction
import de.iteratec.osm.barchart.GetBarchartCommand
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.measurement.schedule.dao.JobGroupDaoService
import de.iteratec.osm.report.chart.AggregationType
import de.iteratec.osm.report.chart.CsiAggregation
import de.iteratec.osm.report.chart.CsiAggregationInterval
import de.iteratec.osm.report.chart.MeasurandGroup
import de.iteratec.osm.result.CachedView
import de.iteratec.osm.result.EventResultDashboardService
import de.iteratec.osm.util.ControllerUtils
import de.iteratec.osm.util.ExceptionHandlerController
import de.iteratec.osm.util.I18nService
import de.iteratec.osm.util.MeasurandUtil
import org.joda.time.DateTime
import org.springframework.http.HttpStatus

class CsiBenchmarkController extends ExceptionHandlerController {

    public final static String DATE_FORMAT_STRING_FOR_HIGH_CHART = 'dd.mm.yyyy'
    public final static int MONDAY_WEEKSTART = 1

    JobGroupDaoService jobGroupDaoService
    EventResultDashboardService eventResultDashboardService
    I18nService i18nService

    def index() { redirect(action: 'show') }

    def show() {
        Map<String, Object> modelToRender = [:]

        // JobGroups
        List<JobGroup> jobGroups = eventResultDashboardService.getAllJobGroups()
        modelToRender.put('folders', jobGroups)

        // JavaScript-Utility-Stuff:
        modelToRender.put("dateFormat", DATE_FORMAT_STRING_FOR_HIGH_CHART)
        modelToRender.put("weekStart", MONDAY_WEEKSTART)

        modelToRender.put('selectedAggrGroupValuesUnCached', [])

        modelToRender.put("tagToJobGroupNameMap", jobGroupDaoService.getTagToJobGroupNameMap())

        // Done! :)
        return modelToRender
    }

    @RestAction
    def getBarChartData(GetBarchartCommand cmd) {
        String errorMessages = getErrorMessages(cmd)
        if (errorMessages) {
            ControllerUtils.sendSimpleResponseAsStream(response, HttpStatus.BAD_REQUEST, errorMessages)
            return
        }

        String selectedCsiType = params.csiType == "visuallyComplete" ? 'csByWptVisuallyCompleteInPercent' : 'csByWptDocCompleteInPercent'
        List<JobGroup> allJobGroups = JobGroup.findAllByNameInList(cmd.selectedJobGroups)

        CsiAggregationInterval interval = CsiAggregationInterval.findByIntervalInMinutes(CsiAggregationInterval.DAILY)

        Date from = cmd.from.toDate()
        // exclude current date
        DateTime today = new DateTime()
        Date to
        if (cmd.to.year() == today.year() && cmd.to.monthOfYear() == today.monthOfYear() && cmd.to.dayOfMonth() == today.dayOfMonth()) {
            to = cmd.to.minusDays(1).toDate()
        } else {
            to = cmd.to.toDate()
        }

        List allCsiAggregations = CsiAggregation.createCriteria().list {
            'in'('jobGroup', allJobGroups)
            eq('aggregationType', AggregationType.JOB_GROUP)
            eq('interval', interval)
            'between'('started', from, to)
            projections {
                jobGroup {
                    groupProperty('name')
                }
                avg(selectedCsiType)
            }
        }

        // return if no data is available
        if (!allCsiAggregations) {
            ControllerUtils.sendObjectAsJSON(response, [:])
            return
        }

        def result = []
        allCsiAggregations.each {
            if (it[1])
                result.add(['name': it[0], 'value': it[1].round(2)])
        }

        ControllerUtils.sendObjectAsJSON(response, result)
    }

    /**
     * Validates the command and creates an error message string if necessary.
     * @param cmd
     * @return a string containing the error messages in html format or an empty string if the command is valid
     */
    private String getErrorMessages(GetBarchartCommand cmd) {
        String result = ""
        if (!cmd.selectedJobGroups) {
            result += i18nService.msg("de.iteratec.osm.gui.selectedFolder.error.validator.error.selectedFolder", "Please select at least one jobGroup")
            result += "<br />"
        }
        return result
    }
}

package de.iteratec.osm.result

import de.iteratec.osm.OsmConfigCacheService
import de.iteratec.osm.annotations.RestAction
import de.iteratec.osm.barchart.GetBarchartCommand
import de.iteratec.osm.measurement.schedule.JobDaoService
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.measurement.schedule.dao.JobGroupDaoService
import de.iteratec.osm.result.dto.JobGroupAggregationChartDTO
import de.iteratec.osm.result.dto.JobGroupAggregationChartSeriesDTO
import de.iteratec.osm.result.dto.JobGroupDTO
import de.iteratec.osm.util.ControllerUtils
import de.iteratec.osm.util.ExceptionHandlerController
import de.iteratec.osm.util.I18nService
import org.springframework.http.HttpStatus

class JobGroupAggregationController extends ExceptionHandlerController {
    public final static String DATE_FORMAT_STRING_FOR_HIGH_CHART = 'dd.mm.yyyy';
    public final static int MONDAY_WEEKSTART = 1

    JobGroupDaoService jobGroupDaoService
    JobDaoService jobDaoService
    EventResultDashboardService eventResultDashboardService
    I18nService i18nService
    PageService pageService
    OsmConfigCacheService osmConfigCacheService

    def index() {
        redirect(action: 'show')
    }

    def show() {
        Map<String, Object> modelToRender = [:]

        // AggregatorTypes
        modelToRender.put('aggrGroupValuesUnCached', Measurand.values().groupBy { it.measurandGroup })

        // JobGroups
        List<JobGroup> jobGroups = eventResultDashboardService.getAllJobGroups()
        modelToRender.put('folders', jobGroups)
        modelToRender.put('selectedFolder', params.selectedFolder)

        // JavaScript-Utility-Stuff:
        modelToRender.put("dateFormat", DATE_FORMAT_STRING_FOR_HIGH_CHART)
        modelToRender.put("weekStart", MONDAY_WEEKSTART)

        modelToRender.put('selectedAggrGroupValuesUnCached', [])

        modelToRender.put("tagToJobGroupNameMap", jobGroupDaoService.getTagToJobGroupNameMap())

        // Done! :)
        return modelToRender
    }

    /**
     * Rest Method for ajax call.
     * @param cmd The requested data.
     * @return BarchartDTO as JSON or string message if an error occurred
     */
    @RestAction
    def getBarchartData(GetBarchartCommand cmd) {
        String errorMessages = getErrorMessages(cmd)
        if (errorMessages) {
            ControllerUtils.sendSimpleResponseAsStream(response, HttpStatus.BAD_REQUEST, errorMessages)
            return
        }

        List<JobGroup> allJobGroups = JobGroup.findAllByNameInList(cmd.selectedJobGroups)
        List<String> allMeasurands = cmd.selectedSeries*.measurands.flatten()
        List<String> measurandFieldName= allMeasurands.collect { (it as Measurand).getEventResultField() }

        List allEventResults = EventResult.createCriteria().list {
            'in'('jobGroup', allJobGroups)
            'between'('jobResultDate', cmd.from.toDate(), cmd.to.toDate())
            'between'(
                    'fullyLoadedTimeInMillisecs',
                    osmConfigCacheService.getMinValidLoadtime(),
                    osmConfigCacheService.getMaxValidLoadtime()
            )
            projections {
                groupProperty('jobGroup')
                measurandFieldName.each { m ->
                    avg(m)
                }
            }
        }

        // return if no data is available
        if (!allEventResults) {
            ControllerUtils.sendObjectAsJSON(response, [:])
            return
        }

        JobGroupAggregationChartDTO jobGroupAggregationChartDTO = new JobGroupAggregationChartDTO()

        //Label translations
        jobGroupAggregationChartDTO.i18nMap.put("measurand", i18nService.msg("de.iteratec.result.measurand.label", "Measurand"))
        jobGroupAggregationChartDTO.i18nMap.put("jobGroup", i18nService.msg("de.iteratec.isr.wptrd.labels.filterFolder", "JobGroup"))

        //Jobgroup measurand and unit
        JobGroupAggregationChartSeriesDTO jobGroupAggregationChartSeriesDTO = new JobGroupAggregationChartSeriesDTO()
        jobGroupAggregationChartSeriesDTO.measurand = i18nService.msg("de.iteratec.isr.measurand.${allMeasurands[0]}", allMeasurands[0])
        jobGroupAggregationChartSeriesDTO.unit = (allMeasurands[0] as Measurand).measurandGroup.unit.label
        jobGroupAggregationChartSeriesDTO.measurandGroup = Measurand.valueOf(allMeasurands[0]).measurandGroup

        //Jobgroup groups and their values
        allEventResults.each { series ->
            JobGroupDTO jobGroupDTO = new JobGroupDTO()
            jobGroupDTO.group = series[0].name
            jobGroupDTO.value = series[1]
            jobGroupAggregationChartSeriesDTO.groupData.add(jobGroupDTO);
        }
        jobGroupAggregationChartDTO.series = jobGroupAggregationChartSeriesDTO

        ControllerUtils.sendObjectAsJSON(response, jobGroupAggregationChartDTO)
    }

    /**
     * Validates the command and creates an error message string if necessary.
     * @param cmd
     * @return a string containing the error messages in html format or an empty string if the command is valid
     */
    private String getErrorMessages(GetBarchartCommand cmd) {
        String result = ""
        if (!cmd.selectedSeries) {
            result += i18nService.msg("de.iteratec.osm.gui.selectedMeasurandSeries.error.validator.error.selectedMeasurandSeries", "Please select at least one measurand series")
            result += "<br />"
        }
        if (!cmd.selectedJobGroups) {
            result += i18nService.msg("de.iteratec.osm.gui.selectedFolder.error.validator.error.selectedFolder", "Please select at least one jobGroup")
            result += "<br />"
        }
        return result
    }
}

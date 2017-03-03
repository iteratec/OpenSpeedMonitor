package de.iteratec.osm.result

import de.iteratec.osm.annotations.RestAction
import de.iteratec.osm.chartUtilities.FilteringAndSortingDataService
import de.iteratec.osm.csi.Page
import de.iteratec.osm.dimple.BarchartDTO
import de.iteratec.osm.dimple.BarchartDatum
import de.iteratec.osm.dimple.BarchartSeries
import de.iteratec.osm.dimple.GetBarchartCommand
import de.iteratec.osm.measurement.schedule.Job
import de.iteratec.osm.measurement.schedule.JobDaoService
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.measurement.schedule.dao.JobGroupDaoService
import de.iteratec.osm.measurement.script.PlaceholdersUtility
import de.iteratec.osm.measurement.script.Script
import de.iteratec.osm.measurement.script.ScriptParser
import de.iteratec.osm.report.chart.MeasurandGroup
import de.iteratec.osm.util.ControllerUtils
import de.iteratec.osm.util.ExceptionHandlerController
import de.iteratec.osm.util.I18nService
import org.springframework.http.HttpStatus

class PageAggregationController extends ExceptionHandlerController {
    public final
    static Map<CachedView, Map<String, List<String>>> AGGREGATOR_GROUP_VALUES = ResultCsiAggregationService.getAggregatorMapForOptGroupSelect()

    public final static String DATE_FORMAT_STRING_FOR_HIGH_CHART = 'dd.mm.yyyy';
    public final static int MONDAY_WEEKSTART = 1

    JobGroupDaoService jobGroupDaoService
    JobDaoService jobDaoService
    EventResultDashboardService eventResultDashboardService
    I18nService i18nService
    PageService pageService
    FilteringAndSortingDataService filteringAndSortingDataService

    def index() {
        redirect(action: 'show')
    }

    def show() {
        Map<String, Object> modelToRender = [:]

        // AggregatorTypes
        modelToRender.put('aggrGroupValuesUnCached', AGGREGATOR_GROUP_VALUES.get(CachedView.UNCACHED))

        // JobGroups
        List<JobGroup> jobGroups = eventResultDashboardService.getAllJobGroups()
        modelToRender.put('folders', jobGroups)

        // Pages
        List<Page> pages = eventResultDashboardService.getAllPages()
        modelToRender.put('pages', pages)

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
        List<Page> allPages = Page.findAllByNameInList(cmd.selectedPages)
        List<String> allMeasurands = cmd.selectedSeries*.measurands.flatten()*.replace("Uncached", "")

        List allEventResults = EventResult.createCriteria().list {
            'in'('page', allPages)
            'in'('jobGroup', allJobGroups)
            'between'('jobResultDate', cmd.from.toDate(), cmd.to.toDate())
            projections {
                groupProperty('page')
                groupProperty('jobGroup')
                allMeasurands.each { m ->
                    avg(m)
                }
            }
        }

        // return if no data is available
        if (!allEventResults) {
            ControllerUtils.sendObjectAsJSON(response, [:])
            return
        }

        List allSeries = cmd.selectedSeries
        BarchartDTO barchartDTO = new BarchartDTO(groupingLabel: "Page / JobGroup")

        allSeries.each { series ->
            BarchartSeries barchartSeries = new BarchartSeries(dimensionalUnit: getDimensionalUnit(series.measurands[0]), yAxisLabel: getYAxisLabel(series.measurands[0]), stacked: series.stacked)
            series.measurands.each { currentMeasurand ->
                allEventResults.each { datum ->
                    barchartSeries.data.add(
                            new BarchartDatum(index: currentMeasurand.replace("Uncached", ""), indexValue: datum[allMeasurands.indexOf(currentMeasurand.replace("Uncached", "")) + 2], grouping: "${datum[0]} / ${datum[1]?.name}"))
                }
            }

            barchartDTO.series.add(barchartSeries)
        }

//      JOHANNES2DO: check how to extract the filteringAndSortingDataService
        barchartDTO.filterRules = createFilterRules(allPages, allJobGroups)
//        barchartDTO.filterRules = filteringAndSortingDataService.createFilterRules(allPages, allJobGroups)

        ControllerUtils.sendObjectAsJSON(response, barchartDTO)
    }

    /**
     * Creates Filter Rules for given Pages and JobGroups.
     * The filterRules can filter the data by testedPages in {@link Script}
     * @return a map which maps the {@link Script}-name to a list of page-jobGroup-name-combinations (e.g. ["script1" : ["page1 / jobGroup1"]])
     */
    private Map<String, List<String>> createFilterRules(List<Page> pages, List<JobGroup> jobGroups) {
        Map<String, List<String>> result = [:].withDefault { [] }

        // Get all scripts
        List<Job> jobList = jobDaoService.getJobs(jobGroups)
        List<Script> allScripts = jobList*.script.unique()

        allScripts.each { currentScript ->
            List<String> filterRule = []
            List<Page> testedPages = []
            List<List<Page>> testedPagesPerJob = [].withDefault { [] }

            jobList.findAll { it.script == currentScript }.each { j ->
                testedPagesPerJob << new ScriptParser(pageService, PlaceholdersUtility.getParsedNavigationScript(currentScript.navigationScript, j.variables)).getTestedPages().unique()
            }

            // if all lists are equal take any
            // else merge lists
            if (testedPagesPerJob.every { it.equals(testedPagesPerJob[0]) }) {
                testedPages = testedPagesPerJob[0]
            } else {
                testedPages = mergeLists(testedPagesPerJob)
            }

            testedPages.each { p ->
                if (pages.contains(p)) {
                    jobGroups.each {
                        filterRule << "${p.name} / ${it.name}"
                    }
                }
            }

            if (filterRule)
                result.put(currentScript.label, filterRule)
        }

        return result
    }

    /**
     * Merges multiple lists of pages by collecting all pages of each list and keeping the order
     * E.g.:
     * List1 = ["a", "b",       "c"]
     * List2 = ["a", "c",       "c"]
     * List2 = ["a", "b",       "d"]
     * Result = ["a", "b", "c", "c", "d"]
     * @param listOfPages
     * @return
     */
    private List<Page> mergeLists(List<List<Page>> listOfPages) {
        List<Page> result = []

        for (int i = 0; i < listOfPages*.size().max(); i++) {
            List<Page> l = listOfPages*.getAt(i).unique()
            l.removeAll([null])
            result.addAll(l)
        }

        return result
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

    /**
     * Returns the dimensional unit for the given measurand
     * @param measurand the {@link MeasurandGroup}
     * @return the dimensional unit as string
     */
    private String getDimensionalUnit(String measurand) {
        def aggregatorGroup = AGGREGATOR_GROUP_VALUES.get(CachedView.UNCACHED)
        if (aggregatorGroup.get(MeasurandGroup.LOAD_TIMES).contains(measurand)) {
            return "ms"
        } else if (aggregatorGroup.get(MeasurandGroup.PERCENTAGES).contains(measurand)) {
            return "%"
        } else if (aggregatorGroup.get(MeasurandGroup.REQUEST_COUNTS).contains(measurand)) {
            return "#"
        } else if (aggregatorGroup.get(MeasurandGroup.REQUEST_SIZES).contains(measurand)) {
            return "kb"
        } else {
            return ""
        }

    }

    /**
     * Returns the y-axis label for the given measurand
     * @param measurand the {@link MeasurandGroup}
     * @return the y-axis label as string
     */
    private String getYAxisLabel(String measurand) {
        def aggregatorGroup = AGGREGATOR_GROUP_VALUES.get(CachedView.UNCACHED)
        if (aggregatorGroup.get(MeasurandGroup.LOAD_TIMES).contains(measurand)) {
            return i18nService.msg("de.iteratec.osm.measurandGroup.loadTimes.yAxisLabel", "Loading Time [ms]")
        } else if (aggregatorGroup.get(MeasurandGroup.PERCENTAGES).contains(measurand)) {
            return i18nService.msg("de.iteratec.osm.measurandGroup.percentages.yAxisLabel", "Percent [%]")
        } else if (aggregatorGroup.get(MeasurandGroup.REQUEST_COUNTS).contains(measurand)) {
            return i18nService.msg("de.iteratec.osm.measurandGroup.requestCounts.yAxisLabel", "Amount")
        } else if (aggregatorGroup.get(MeasurandGroup.REQUEST_SIZES).contains(measurand)) {
            return i18nService.msg("de.iteratec.osm.measurandGroup.requestSize.yAxisLabel", "Size [kb]")
        } else {
            return ""
        }

    }
}

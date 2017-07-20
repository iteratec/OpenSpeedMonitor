package de.iteratec.osm.result

import de.iteratec.osm.OsmConfigCacheService
import de.iteratec.osm.annotations.RestAction
import de.iteratec.osm.barchart.BarchartDTO
import de.iteratec.osm.barchart.BarchartDatum
import de.iteratec.osm.barchart.BarchartSeries
import de.iteratec.osm.barchart.GetBarchartCommand
import de.iteratec.osm.csi.Page
import de.iteratec.osm.measurement.schedule.Job
import de.iteratec.osm.measurement.schedule.JobDaoService
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.measurement.schedule.dao.JobGroupDaoService
import de.iteratec.osm.measurement.script.PlaceholdersUtility
import de.iteratec.osm.measurement.script.Script
import de.iteratec.osm.measurement.script.ScriptParser
import de.iteratec.osm.util.ControllerUtils
import de.iteratec.osm.util.ExceptionHandlerController
import de.iteratec.osm.util.I18nService

import org.springframework.http.HttpStatus

class PageAggregationController extends ExceptionHandlerController {

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

        // Pages
        List<Page> pages = eventResultDashboardService.getAllPages()
        modelToRender.put('pages', pages)
        modelToRender.put('selectedPages', params.selectedPages)

        // JavaScript-Utility-Stuff:
        modelToRender.put("dateFormat", DATE_FORMAT_STRING_FOR_HIGH_CHART)
        modelToRender.put("weekStart", MONDAY_WEEKSTART)

        modelToRender.put('selectedAggrGroupValuesUnCached', [])

        modelToRender.put("tagToJobGroupNameMap", jobGroupDaoService.getTagToJobGroupNameMap())

        // Done! :)
        return modelToRender
    }

    def entryAndFollow(){
        return [:]
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
        List<String> allMeasurands = cmd.selectedSeries*.measurands.flatten()
        List<String> measurandFieldName = allMeasurands.collect {(it as Measurand).eventResultField}

        List eventResultAverages = EventResult.createCriteria().list {
            'in'('page', allPages)
            'in'('jobGroup', allJobGroups)
            'between'('jobResultDate', cmd.from.toDate(), cmd.to.toDate())
            'between'(
                'fullyLoadedTimeInMillisecs',
                osmConfigCacheService.getMinValidLoadtime(),
                osmConfigCacheService.getMaxValidLoadtime()
            )
            projections {
                groupProperty('page')
                groupProperty('jobGroup')
                measurandFieldName.each { m ->
                    avg(m)
                }
            }
        }

        Map comparativeEventResultAverages = null
        if (cmd.fromComparative && cmd.toComparative) {
            comparativeEventResultAverages = EventResult.createCriteria().list {
                'in'('page', allPages)
                'in'('jobGroup', allJobGroups)
                'between'('jobResultDate', cmd.fromComparative.toDate(), cmd.toComparative.toDate())
                projections {
                    groupProperty('page')
                    groupProperty('jobGroup')
                    measurandFieldName.each { m ->
                        avg(m)
                    }
                }
            }?.collectEntries({ it ->
                [("${it[0].name} | ${it[1].name}".toString()): it.takeRight(it.size()-2)]
            })
        }

        // return if no data is available
        if (!eventResultAverages && !comparativeEventResultAverages) {
            ControllerUtils.sendObjectAsJSON(response, [:])
            return
        }

        List allSeries = cmd.selectedSeries
        BarchartDTO barchartDTO = new BarchartDTO(groupingLabel: "Page / JobGroup")
        barchartDTO.i18nMap.put("measurand", i18nService.msg("de.iteratec.result.measurand.label", "Measurand"))
        barchartDTO.i18nMap.put("jobGroup", i18nService.msg("de.iteratec.isr.wptrd.labels.filterFolder", "JobGroup"))
        barchartDTO.i18nMap.put("page", i18nService.msg("de.iteratec.isr.wptrd.labels.filterPage", "Page"))
        barchartDTO.i18nMap.put("comparativeImprovement", i18nService.msg("de.iteratec.osm.chart.comparative.improvement", "Improvement"))
        barchartDTO.i18nMap.put("comparativeDeterioration", i18nService.msg("de.iteratec.osm.chart.comparative.deterioration", "Deterioration"))

        allSeries.each { series ->
            BarchartSeries barchartSeries = new BarchartSeries(
                    dimensionalUnit: (series.measurands[0] as Measurand).measurandGroup.unit.label,
                    yAxisLabel:  (series.measurands[0] as Measurand).measurandGroup.unit.label,
                    stacked: series.stacked)
            series.measurands.each { currentMeasurand ->
                eventResultAverages.each { datum ->
                    def measurandIndex = allMeasurands.indexOf(currentMeasurand)
                    def key = "${datum[0]} | ${datum[1]?.name}".toString()
                    def value = Measurand.valueOf(currentMeasurand).normalizeValue(datum[measurandIndex + 2])
                    if (value) {
                        barchartSeries.data.add(
                                new BarchartDatum(
                                        measurand: i18nService.msg("de.iteratec.isr.measurand.${currentMeasurand}", currentMeasurand),
                                        originalMeasurandName: currentMeasurand,
                                        value: value,
                                        valueComparative: Measurand.valueOf(currentMeasurand).normalizeValue(comparativeEventResultAverages?.get(key)?.getAt(measurandIndex)),
                                        grouping: key
                                )
                        )
                    }
                }
            }
            barchartDTO.series.add(barchartSeries)
        }

//      TODO: see ticket [IT-1614]
        barchartDTO.filterRules = createFilterRules(allPages, allJobGroups)
//        barchartDTO.filterRules = filteringAndSortingDataService.createFilterRules(allPages, allJobGroups)

        boolean hasData = false;
        barchartDTO.series.each {series ->
            series.data.each {datum ->
                if (datum.value) {
                    hasData = true;
                }
            }
        }
        if (!hasData) {
            ControllerUtils.sendSimpleResponseAsStream(
                response,
                HttpStatus.OK,
                i18nService.msg("de.iteratec.ism.no.data.on.current.selection.heading", "No data")
            )
        }
        else {
            ControllerUtils.sendObjectAsJSON(response, barchartDTO)
        }
    }

    @RestAction
    def getEntryAndFollowBarchartData(){

        JobGroup jobGroup = JobGroup.findByName('develop_Desktop')
        List<Job> jobs = jobDaoService.getJobs(jobGroup)
        Set<Page> uniqueTestedPages = [] as Set

        jobs*.script*.navigationScript.each {String navigationScript ->
            List<Page> pagesOfThisScript = new ScriptParser(pageService, navigationScript).getTestedPages()
            uniqueTestedPages.addAll(pagesOfThisScript)
        }
        uniqueTestedPages.each {

        }
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
                testedPages = getOrderedPagesOfAllScripts(testedPagesPerJob)
            }

            testedPages.each { p ->
                if (pages.contains(p)) {
                    jobGroups.each {
                        filterRule << "${p.name} | ${it.name}"
                    }
                }
            }

            if (filterRule)
                result.put(currentScript.label, filterRule)
        }

        return result
    }

    /**
     * Merges multiple lists of pages by collecting all pages of each list, filtering out duplicates
     * and keeping the order
     *
     * E.g.:
     * List1 = ["a", "b",       "c"]
     * List2 = ["a", "c",       "c"]
     * List2 = ["a", "b",       "d"]
     *
     * Result = ["a", "b", "c", "c", "d"]
     *
     * @param listOfPages A list of scripts where each item is a list of pages which are measured in that particular script
     * @return
     */
    private List<Page> getOrderedPagesOfAllScripts(List<List<Page>> listOfPages) {
        List<Page> orderedPagesOfAllScripts = []
        int sizeOfLongestPageList = listOfPages*.size().max()

        sizeOfLongestPageList.times { pageStep ->
            List<Page> pagesOfCurrentStep = listOfPages*.getAt(pageStep).flatten()
            pagesOfCurrentStep.unique(true).removeAll([null])

            orderedPagesOfAllScripts.addAll(pagesOfCurrentStep)
        }

        return orderedPagesOfAllScripts
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
}

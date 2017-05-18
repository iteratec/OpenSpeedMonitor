/*
 * OpenSpeedMonitor (OSM)
 * Copyright 2014 iteratec GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.iteratec.osm.result

import de.iteratec.osm.ConfigService
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.measurement.schedule.dao.JobGroupDaoService
import de.iteratec.osm.p13n.CustomDashboardService
import de.iteratec.osm.report.UserspecificDashboardBase
import de.iteratec.osm.report.UserspecificDashboardService
import de.iteratec.osm.report.UserspecificEventResultDashboard
import de.iteratec.osm.report.chart.*
import de.iteratec.osm.util.AnnotationUtil
import de.iteratec.osm.util.ControllerUtils
import de.iteratec.osm.util.I18nService
import de.iteratec.osm.util.ParameterBindingUtility
import de.iteratec.osm.util.TreeMapOfTreeMaps
import grails.converters.JSON
import grails.web.mapping.LinkGenerator
import org.grails.web.json.JSONObject
import org.joda.time.DateTime
import org.joda.time.Interval
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import org.springframework.dao.DataIntegrityViolationException
import org.supercsv.encoder.DefaultCsvEncoder
import org.supercsv.io.CsvListWriter
import org.supercsv.prefs.CsvPreference

class EventResultDashboardController {

    JobGroupDaoService jobGroupDaoService
    EventResultDashboardService eventResultDashboardService
    I18nService i18nService
    EventService eventService
    def springSecurityService
    ConfigService configService
    CustomDashboardService customDashboardService
    UserspecificDashboardService userspecificDashboardService

    /**
     * The Grails engine to generate links.
     *
     * @see http://mrhaki.blogspot.ca/2012/01/grails-goodness-generate-links-outside.html
     */
    LinkGenerator grailsLinkGenerator

    public static final Map<CachedView, Map<String, List<String>>> AGGREGATOR_GROUP_VALUES = ResultCsiAggregationService.getAggregatorMapForOptGroupSelect()

    public static final String DATE_FORMAT_STRING_FOR_HIGH_CHART = 'dd.mm.yyyy'

    public static final List<Long> AGGREGATION_INTERVALS = [CsiAggregationInterval.RAW, CsiAggregationInterval.HOURLY, CsiAggregationInterval.DAILY, CsiAggregationInterval.WEEKLY]


    def intervals = ['not', 'hourly', 'daily', 'weekly']

    def index() {
        redirect(action: 'showAll')
    }

    /**
     * deletes custom Dashboard
     *
     * @return Nothing , redirects immediately.
     */
    Map<String, Object> delete() {

        def userspecificDashboardInstance = UserspecificEventResultDashboard.get(params.id)
        if (!userspecificDashboardInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'custom.dashboard.label', default: 'Custom dashboard'), params.id])
            redirect(action: "showAll")
            return
        }

        try {
            userspecificDashboardInstance.delete(flush: true)
            flash.message = message(code: 'default.deleted.message', args: [message(code: 'custom.dashboard.label', default: 'Custom dashboard'), params.id])
            redirect(action: "showAll")
        } catch (DataIntegrityViolationException e) {
            flash.message = message(code: 'default.not.deleted.message', args: [message(code: 'custom.dashboard.label', default: 'Custom dashboard'), params.id])
            redirect(action: "showAll")
        }
    }

    /**
     * Thats the view used to show results with previous selection of date
     * range, groups and more filter criteria.
     *
     * @param cmd The request / command send to this action,
     *            not <code>null</code>.
     * @return A model map with event result data to be used by the
     *         corresponding GSP, not <code>null</code> and never
     * {@linkplain Map#isEmpty() empty}.
     */
    Map<String, Object> showAll(EventResultDashboardShowAllCommand cmd) {


        Map<String, Object> modelToRender = constructStaticViewDataOfShowAll();

        boolean requestedAllowedDashboard = true;

        if (params.dashboardID) {
            if (!isUserAllowedToViewDashboard(params.dashboardID)) {
                flash.message = i18nService.msg("de.iteratec.osm.userspecificDashboard.notAllowed", "not allowed", [params.dashboardID])
                requestedAllowedDashboard = false
            } else {
                fillWithUserspecificDashboardValues(cmd, params.dashboardID)
            }
        }

        cmd.loadTimeMaximum = cmd.loadTimeMaximum ?: "auto"
        cmd.chartHeight = cmd.chartHeight > 0 ? cmd.chartHeight : configService.getInitialChartHeightInPixels()
        cmd.chartWidth = cmd.chartWidth > 0 ? cmd.chartWidth : -1;

        cmd.copyRequestDataToViewModelMap(modelToRender);

        if (!ControllerUtils.isEmptyRequest(params) && requestedAllowedDashboard) {
            if (!cmd.validate()) {
                modelToRender.put('command', cmd)
            } else {
                fillWithEventResultData(modelToRender, cmd);
            }
        }
        modelToRender.put("availableDashboards", userspecificDashboardService.getListOfAvailableEventResultDashboards())

        log.info("from=${modelToRender['from']}")
        log.info("to=${modelToRender['to']}")

        fillWithI18N(modelToRender)

        return modelToRender
    }

    private boolean isUserAllowedToViewDashboard(String dashboardID) {
        UserspecificDashboardBase requestedDashboard = UserspecificDashboardBase.get(dashboardID)
        return requestedDashboard && (requestedDashboard.publiclyVisible || this.userspecificDashboardService.isCurrentUserDashboardOwner(dashboardID))
    }
/**
 * Gets data for the showAllCommand from a saved userspecificCsiDashboard
 * @param cmd the command where the attribute gets set
 * @param dashboardID the id of the saved userspecificCsiDashboard
 */
    private void fillWithUserspecificDashboardValues(EventResultDashboardShowAllCommand cmd, String dashboardID) {
        UserspecificEventResultDashboard dashboard = UserspecificEventResultDashboard.get(Long.parseLong(dashboardID))
        dashboard.fillCommand(cmd)
    }

    /**
     * <p>
     * Ajax service to validate and store custom dashboard settings.
     * Note: It will overwrite existing dashboards with same name!
     * </p>
     *
     * @param values
     *         The dashboard settings, JSON encoded;
     *         not <code>null</code>.
     */
    def validateAndSaveDashboardValues(String values) {

        JSONObject dashboardValues = JSON.parse(values)

        String dashboardName = dashboardValues.dashboardName
        String username = springSecurityService.authentication.principal.getUsername()
        Boolean publiclyVisible = dashboardValues.publiclyVisible as Boolean

        // Parse JSON Data for Command
        DateTime from = ParameterBindingUtility.parseDateTimeParameter(dashboardValues.from, false)
        DateTime to = ParameterBindingUtility.parseDateTimeParameter(dashboardValues.to, true)


        Collection<Long> selectedFolder = customDashboardService.getValuesFromJSON(dashboardValues, "selectedFolder")
        Collection<Long> selectedPages = customDashboardService.getValuesFromJSON(dashboardValues, "selectedPages")
        Collection<Long> selectedMeasuredEventIds = customDashboardService.getValuesFromJSON(dashboardValues, "selectedMeasuredEventIds")
        Collection<Long> selectedBrowsers = customDashboardService.getValuesFromJSON(dashboardValues, "selectedBrowser")
        Collection<Long> selectedLocations = customDashboardService.getValuesFromJSON(dashboardValues, "selectedLocations")

        Collection<String> selectedAggrGroupValuesCached = []
        // String or List<String>
        def valuesCached = dashboardValues.selectedAggrGroupValuesCached
        if (valuesCached) {
            if (valuesCached.class == String) {
                selectedAggrGroupValuesCached.add(valuesCached)
            } else {
                valuesCached.each { l -> selectedAggrGroupValuesCached.add(l) }
            }
        }

        Collection<String> selectedAggrGroupValuesUnCached = []
        // String or List<String>
        def valuesUnCached = dashboardValues.selectedAggrGroupValuesUnCached
        if (valuesUnCached) {
            if (valuesUnCached.class == String) {
                selectedAggrGroupValuesUnCached.add(valuesUnCached)
            } else {
                valuesUnCached.each { l -> selectedAggrGroupValuesUnCached.add(l) }
            }
        }

        Collection<String> selectedConnectivities = []
        // String or List<String>
        def valuesConnectivities = dashboardValues.selectedConnectivities
        if (valuesConnectivities) {
            if (valuesConnectivities.class == String) {
                selectedConnectivities.add(valuesConnectivities)
            } else {
                valuesConnectivities.each { l -> selectedConnectivities.add(l) }
            }
        }

        // Create cmd for validation
        EventResultDashboardShowAllCommand cmd = new EventResultDashboardShowAllCommand(
                from: from,
                to: to,
                selectedFolder: selectedFolder,
                selectedPages: selectedPages,
                selectedMeasuredEventIds: selectedMeasuredEventIds,
                selectedBrowsers: selectedBrowsers,
                selectedLocations: selectedLocations,
                selectedAggrGroupValuesCached: selectedAggrGroupValuesCached,
                selectedAggrGroupValuesUnCached: selectedAggrGroupValuesUnCached,
                selectedConnectivities: selectedConnectivities,
                chartTitle: dashboardValues.chartTitle ?: "",
                loadTimeMaximum: dashboardValues.loadTimeMaximum ?: "auto",
                showDataLabels: dashboardValues.showDataLabels,
                showDataMarkers: dashboardValues.showDataMarkers,
                graphNameAliases: dashboardValues.graphAliases,
                graphColors: dashboardValues.graphColors)

        // Parse IntegerValues if they exist
        if (dashboardValues.selectedInterval) cmd.selectedInterval = dashboardValues.selectedInterval.toInteger()
        if (dashboardValues.selectedTimeFrameInterval) cmd.selectedTimeFrameInterval = dashboardValues.selectedTimeFrameInterval.toInteger()
        if (dashboardValues.trimAboveRequestSizes) cmd.trimAboveRequestSizes = dashboardValues.trimAboveRequestSizes.toInteger()
        if (dashboardValues.trimBelowRequestSizes) cmd.trimBelowRequestSizes = dashboardValues.trimBelowRequestSizes.toInteger()
        if (dashboardValues.trimAboveRequestCounts) cmd.trimAboveRequestCounts = dashboardValues.trimAboveRequestCounts.toInteger()
        if (dashboardValues.trimBelowRequestCounts) cmd.trimBelowRequestCounts = dashboardValues.trimBelowRequestCounts.toInteger()
        if (dashboardValues.trimAboveLoadTimes) cmd.trimAboveLoadTimes = dashboardValues.trimAboveLoadTimes.toInteger()
        if (dashboardValues.trimBelowLoadTimes) cmd.trimBelowLoadTimes = dashboardValues.trimBelowLoadTimes.toInteger()
        if (dashboardValues.loadTimeMinimum) cmd.loadTimeMinimum = dashboardValues.loadTimeMinimum.toInteger()
        if (dashboardValues.chartHeight) {
            cmd.chartHeight = dashboardValues.chartHeight == "auto" ? -1 : dashboardValues.chartHeight.toInteger()
        }
        if (dashboardValues.chartWidth) {
            cmd.chartWidth = dashboardValues.chartWidth == "auto" ? -1 : dashboardValues.chartWidth.toInteger()
        }
        // Check validation and send errors if needed
        if (!cmd.validate()) {
            //send errors
            def errMsgList = cmd.errors.allErrors.collect { g.message([error: it]) }
            response.sendError(400, "beginErrorMessage" + errMsgList.toString() + "endErrorMessage")
            // Apache Tomcat will output the response as part of (HTML) error page
        } else {
            // Remove old if existing
            UserspecificEventResultDashboard existing = UserspecificEventResultDashboard.findByDashboardName(dashboardName)
            if (existing) {
                existing.delete(flush: true, failOnError: true)
            }

            UserspecificEventResultDashboard newCustomDashboard = new UserspecificEventResultDashboard(cmd, dashboardName, publiclyVisible, username)

            if (!newCustomDashboard.save(failOnError: true, flush: true)) {
                response.sendError(500, 'save error')
            }
            response.setStatus(200)
            ControllerUtils.sendObjectAsJSON(response, ["path": "/eventResultDashboard/", "dashboardId": newCustomDashboard.id], false)
        }
    }

    private void fillWithEventResultData(Map<String, Object> modelToRender, EventResultDashboardShowAllCommand cmd) {
        Interval timeFrame = cmd.createTimeFrameInterval();

        List<String> aggregatorNames = [];
        aggregatorNames.addAll(cmd.getSelectedAggrGroupValuesCached());
        aggregatorNames.addAll(cmd.getSelectedAggrGroupValuesUnCached());

        List<AggregatorType> aggregators = getAggregators(aggregatorNames);

        LinkedList<OsmChartAxis> labelToDataMap = new LinkedList<OsmChartAxis>();
        labelToDataMap.add(new OsmChartAxis(i18nService.msg("de.iteratec.isr.measurand.group.UNDEFINED",
                MeasurandGroup.UNDEFINED.toString()), MeasurandGroup.UNDEFINED, "", 1, OsmChartAxis.RIGHT_CHART_SIDE));
        labelToDataMap.add(new OsmChartAxis(i18nService.msg("de.iteratec.isr.measurand.group.REQUEST_COUNTS",
                MeasurandGroup.REQUEST_COUNTS.toString()), MeasurandGroup.REQUEST_COUNTS, "c", 1, OsmChartAxis.RIGHT_CHART_SIDE));
        labelToDataMap.add(new OsmChartAxis(i18nService.msg("de.iteratec.isr.measurand.group.LOAD_TIMES",
                MeasurandGroup.LOAD_TIMES.toString()), MeasurandGroup.LOAD_TIMES, "s", 1000, OsmChartAxis.LEFT_CHART_SIDE));
        labelToDataMap.add(new OsmChartAxis(i18nService.msg("de.iteratec.isr.measurand.group.REQUEST_SIZES",
                MeasurandGroup.REQUEST_SIZES.toString()), MeasurandGroup.REQUEST_SIZES, "KB", 1000, OsmChartAxis.RIGHT_CHART_SIDE));
        labelToDataMap.add(new OsmChartAxis(i18nService.msg("de.iteratec.isr.measurand.group.PERCENTAGES",
                MeasurandGroup.PERCENTAGES.toString()), MeasurandGroup.PERCENTAGES, "%", 0.01, OsmChartAxis.RIGHT_CHART_SIDE));

        ErQueryParams queryParams = cmd.createErQueryParams();

        OsmRickshawChart chart = eventResultDashboardService.getEventResultDashboardHighchartGraphs(
                timeFrame.getStart().toDate(), timeFrame.getEnd().toDate(), cmd.selectedInterval, aggregators, queryParams
        )
        modelToRender.put("eventResultValues", chart.osmChartGraphs);

        modelToRender.put("labelSummary", chart.osmChartGraphsCommonLabel);
        modelToRender.put("yAxisMax", cmd.loadTimeMaximum)
        modelToRender.put("yAxisMin", cmd.loadTimeMinimum)

        modelToRender.put("highChartLabels", labelToDataMap);
        modelToRender.put("markerShouldBeEnabled", false);
        modelToRender.put("labelShouldBeEnabled", false);

        fillWithAnnotations(modelToRender, timeFrame, cmd.selectedFolder)
    }

    /**
     * <p>
     * Fills the annotations with values.
     * </p>
     *
     * @param modelToRender
     *         The map to be filled. Previously added entries are overridden.
     *         This map should not be <code>null</code>.
     * @param timeFrame
     *         The time-frame for that data should be calculated,
     *         not <code>null</code>.
     */
    private void fillWithAnnotations(
            Map<String, Object> modelToRender,
            Interval timeFrame,
            Collection<Long> selectedFolder) {
        AnnotationUtil.fillWithAnnotations(modelToRender, timeFrame, selectedFolder, eventService)

    }

    // fill the context menu with i18n
    private void fillWithI18N(Map<String, Object> modelToRender) {
        Map<String, String> i18n = [:]

        i18n.put("summary", message(code: 'de.iteratec.chart.contextMenu.summary', default: 'Summary'))
        i18n.put("waterfall", message(code: 'de.iteratec.chart.contextMenu.waterfall', default: 'Waterfall'))
        i18n.put("performanceReview", message(code: 'de.iteratec.chart.contextMenu.performanceReview', default: 'Performance Review'))
        i18n.put("contentBreakdown", message(code: 'de.iteratec.chart.contextMenu.contentBreakdown', default: 'Content Breakdown'))
        i18n.put("domains", message(code: 'de.iteratec.chart.contextMenu.domains', default: 'Domains'))
        i18n.put("screenshot", message(code: 'de.iteratec.chart.contextMenu.screenshot', default: 'Screenshot'))
        i18n.put("filmstrip", message(code: 'de.iteratec.chart.contextMenu.filmstrip', default: 'Filmstrip'))
        i18n.put("compareFilmstrips", message(code: 'de.iteratec.chart.contextMenu.compareFilmstrips', default: 'Compare Filmstrips'))
        i18n.put("selectPoint", message(code: 'de.iteratec.chart.contextMenu.selectPoint', default: 'Select Point'))
        i18n.put("deselectPoint", message(code: 'de.iteratec.chart.contextMenu.deselectPoint', default: 'Deselect Point'))
        i18n.put("deselectAllPoints", message(code: 'de.iteratec.chart.contextMenu.deselectAllPoints', default: 'Deselect all Points'))

        modelToRender.put('i18n', i18n as JSON)
    }

    private Collection<AggregatorType> getAggregators(Collection<String> aggregatorNames) {

        Collection<AggregatorType> aggregators = []
        aggregatorNames.each { String name ->
            AggregatorType aggregator = AggregatorType.findByName(name);
            if (aggregator != null) {
                aggregators.add(aggregator);
            }
        }

        return aggregators;
    }

    /**
     * <p>
     * WARNING: This method is a duplicate of CsiDashboardController's
     * version.
     * </p>
     *
     * <p>
     * Performs a redirect with HTTP status code 303 (see other).
     * </p>
     *
     * <p>
     * Using this redirect enforces the client to perform the next request
     * with the HTTP method GET.
     * This method SHOULD be used in a redirect-after-post situation.
     * </p>
     *
     * <p>
     * After using this method, the response should be considered to be
     * committed and should not be written to.
     * </p>
     *
     * @param actionNameToRedirectTo
     *        The Name of the action to redirect to;
     *        not <code>null</code>.
     * @param urlParams
     *        The parameters to add as query string;
     *        not <code>null</code>.
     *
     * @see <a href="http://tools.ietf.org/html/rfc2616#section-10.3.4"
     *      >http://tools.ietf.org/html/rfc2616#section-10.3.4</a>
     *
     * @since copy since: IT-188
     */
    private void redirectWith303(String actionNameToRedirectTo, Map urlParams) {
        // There is a missing feature to do this:
        // http://jira.grails.org/browse/GRAILS-8829

        // Workaround based on:
        // http://fillinginthegaps.wordpress.com/2008/12/26/grails-301-moved-permanently-redirect/
        Map paramsWithoutGrailsActionNameOfOldAction = urlParams.findAll({ Map.Entry m -> !m.getKey().toString().startsWith('_action') });
        String uri = grailsLinkGenerator.link(action: actionNameToRedirectTo, params: paramsWithoutGrailsActionNameOfOldAction)
        response.setStatus(303)
        response.setHeader("Location", uri)
        render(status: 303)
    }

    /**
     * <p>
     * WARNING: This constant is a duplicate of CsiDashboardController's
     * version.
     * </p>
     *
     * The {@link DateTimeFormat} used for CSV export and table view.
     * @since copy since: IT-188
     */
    private static
    final DateTimeFormatter CSV_TABLE_DATE_TIME_FORMAT = DateTimeFormat.forPattern("dd.MM.yyyy HH:mm:ss");

    /**
     * <p>
     * WARNING: This method is a duplicate of CsiDashboardController's
     * version.
     * </p>
     *
     * <p>
     * Converts the specified result values in the source map to a CSV
     * corresponding to RFC 4180 written to specified {@link Writer}.
     * </p>
     *
     * @param source
     *         The result values a List of OsmChartGraph,
     *         not <code>null</code>.
     * @param target
     *         The {@link Writer} to write CSV to,
     *         not <code>null</code>.
     * @param localeForNumberFormat
     *         The locale used to format the numeric values,
     *         not <code>null</code>.
     *
     * @throws IOException if write on {@code target} failed.
     * @since copy since: IT-188
     */
    private
    static void writeCSV(List<OsmChartGraph> source, Writer target) throws IOException {
        // Sort graph points by time
        TreeMapOfTreeMaps<Long, String, OsmChartPoint> pointsByGraphByTime = new TreeMapOfTreeMaps<Long, String, OsmChartPoint>();
        for (OsmChartGraph eachCSIValueEntry : source) {
            for (OsmChartPoint eachPoint : eachCSIValueEntry.getPoints()) {
                pointsByGraphByTime.getOrCreate(eachPoint.time).put(eachCSIValueEntry.getLabel(), eachPoint);
            }
        }

        CsvListWriter csvWriter = new CsvListWriter(
                target,
                new CsvPreference.Builder(CsvPreference.EXCEL_NORTH_EUROPE_PREFERENCE).useEncoder(new DefaultCsvEncoder()).build()
        )

        // Create CSV header:
        List<String> csvHeader = new LinkedList<String>();
        csvHeader.add('Zeitpunkt'); // TODO i18n?

        List<String> graphLabelsInOrderOfHeader = new LinkedList<String>();

        for (OsmChartGraph eachGraph : source) {
            csvHeader.add(eachGraph.getLabel());
            graphLabelsInOrderOfHeader.add(eachGraph.getLabel());
        }

        csvWriter.writeHeader(csvHeader.toArray(new String[csvHeader.size()]));

        for (Map.Entry<Long, TreeMap<String, OsmChartPoint>> eachPointByGraphOfTime : pointsByGraphByTime) {
            List<String> row = new LinkedList<String>();

            DateTime time = new DateTime(eachPointByGraphOfTime.getKey());
            row.add(CSV_TABLE_DATE_TIME_FORMAT.print(time));

            for (String eachGraphLabel : graphLabelsInOrderOfHeader) {
                OsmChartPoint point = eachPointByGraphOfTime.getValue().get(eachGraphLabel);
                if (point != null) {
                    row.add(point.csiAggregation?.round(2));
                } else {
                    row.add("");
                }
            }

            csvWriter.writeRow(row);
        }

        csvWriter.flush();
    }

    /**
     * <p>
     * Creates a CSV based on the selection passed as {@link EventResultDashboardShowAllCommand}.
     * </p>
     *
     * @param cmd
     *         The command with the users selections;
     *         not <code>null</code>.
     * @return nothing , immediately renders a CSV to response' output stream.
     * @see <a href="http://tools.ietf.org/html/rfc4180">http://tools.ietf.org/html/rfc4180</a>
     */
    public Map<String, Object> downloadCsv(EventResultDashboardShowAllCommand cmd) {

        Map<String, Object> modelToRender = new HashMap<String, Object>();

        if (request.queryString && cmd.validate()) {
            fillWithEventResultData(modelToRender, cmd);
            cmd.copyRequestDataToViewModelMap(modelToRender)
        } else {
            redirectWith303('showAll', params)
            return
        }
        String filename = ""
        List<JobGroup> selectedJobGroups = JobGroup.findAllByIdInList(modelToRender['selectedFolder'])

        selectedJobGroups.each { jobGroup ->
            filename += jobGroup.name + '_'
        }
        if (modelToRender['selectedInterval'] != -1) {
            filename += modelToRender['selectedInterval'] + 'm_'
        }
        DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("yyyy-MM-dd_HH-mm-ss")
        filename += dateFormatter.print(modelToRender["from"]) + '_to_' + dateFormatter.print(modelToRender["to"]) + '.csv'

        response.setHeader('Content-disposition', 'attachment; filename=' + filename);
        response.setContentType("text/csv;header=present;charset=UTF-8");

        Writer responseWriter = new OutputStreamWriter(response.getOutputStream());

        List<OsmChartGraph> csiValues = modelToRender['eventResultValues'];
        writeCSV(csiValues, responseWriter);

        response.getOutputStream().flush()
        return null;
    }

    /**
     * <p>
     * Constructs the static view data of the {@link #showAll(EventResultDashboardShowAllCommand)}
     * view as {@link Map}.
     * </p>
     *
     * <p>
     * This map does always contain all available data for selections, previous
     * selections are not considered.
     * </p>
     *
     * @return A Map containing the static view data which are accessible
     *         through corresponding keys. The Map is modifiable to add
     *         further data. Subsequent calls will never return the same
     *         instance.
     */
    Map<String, Object> constructStaticViewDataOfShowAll() {
        def pages = eventResultDashboardService.getAllPages()
        def measuredEvents = eventResultDashboardService.getAllMeasuredEvents()
        def browsers = eventResultDashboardService.getAllBrowser()
        def locations = eventResultDashboardService.getAllLocations()
        def eventsOfPages = pages.collectEntries { page ->
            [(page.id): measuredEvents.findResults { page.id == it.testedPage.id ? it.id : null } as HashSet<Long>]
        }
        def locationsOfBrowsers = browsers.collectEntries { browser ->
            [(browser.id): locations.findResults { browser.id == it.browser.id ? it.id : null } as HashSet<Long>]
        }
        return [
                'aggrGroupValuesCached': AGGREGATOR_GROUP_VALUES.get(CachedView.CACHED),
                'aggrGroupValuesUnCached': AGGREGATOR_GROUP_VALUES.get(CachedView.UNCACHED),
                'aggregationIntervals': AGGREGATION_INTERVALS,
                'folders': eventResultDashboardService.getAllJobGroups(),
                'pages': pages,
                'measuredEvents': measuredEvents,
                'browsers': browsers,
                'locations': locations,
                'avaiableConnectivities': eventResultDashboardService.getAllConnectivities(true),
                'dateFormat': DATE_FORMAT_STRING_FOR_HIGH_CHART,
                'tagToJobGroupNameMap' : jobGroupDaoService.getTagToJobGroupNameMap(),
                'eventsOfPages': eventsOfPages,
                'locationsOfBrowsers': locationsOfBrowsers
        ]
    }

    public def checkDashboardNameUnique(String values) {
        JSONObject dashboardValues = JSON.parse(values)
        String dashboardName = dashboardValues.dashboardName

        def answer
        if (UserspecificEventResultDashboard.findByDashboardName(dashboardName)) {
            answer = [result: 'false']
        } else {
            answer = [result: 'true']
        }

        render answer as JSON
    }
}

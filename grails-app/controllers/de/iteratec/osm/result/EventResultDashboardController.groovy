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

import de.iteratec.osm.csi.Page
import de.iteratec.osm.measurement.environment.Browser
import de.iteratec.osm.measurement.environment.Location
import de.iteratec.osm.measurement.environment.dao.BrowserDaoService
import de.iteratec.osm.measurement.environment.dao.LocationDaoService
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.measurement.schedule.dao.JobGroupDaoService
import de.iteratec.osm.measurement.schedule.dao.PageDaoService
import de.iteratec.osm.p13n.CookieBasedSettingsService
import de.iteratec.osm.report.UserspecificEventResultDashboard
import de.iteratec.osm.report.chart.*
import de.iteratec.osm.report.chart.dao.AggregatorTypeDaoService
import de.iteratec.osm.util.AnnotationUtil
import de.iteratec.osm.util.ControllerUtils
import de.iteratec.osm.util.I18nService
import de.iteratec.osm.util.TreeMapOfTreeMaps
import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONObject
import org.codehaus.groovy.grails.web.mapping.LinkGenerator
import org.joda.time.DateTime
import org.joda.time.Duration
import org.joda.time.Interval
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.web.servlet.support.RequestContextUtils
import org.supercsv.encoder.DefaultCsvEncoder
import org.supercsv.io.CsvListWriter
import org.supercsv.prefs.CsvPreference

import java.text.NumberFormat
import java.text.SimpleDateFormat

class EventResultDashboardController {

    static final int RESULT_DASHBOARD_MAX_POINTS_PER_SERIES = 100000

    AggregatorTypeDaoService aggregatorTypeDaoService
    JobGroupDaoService jobGroupDaoService
    PageDaoService pageDaoService
    BrowserDaoService browserDaoService
    LocationDaoService locationDaoService
    EventResultService eventResulshowatService
    MeasuredValueUtilService measuredValueUtilService
    EventResultDashboardService eventResultDashboardService
    PageService pageService
    I18nService i18nService
    CookieBasedSettingsService cookieBasedSettingsService
    EventService eventService
    def springSecurityService

    /**
     * The Grails engine to generate links.
     *
     * @see http://mrhaki.blogspot.ca/2012/01/grails-goodness-generate-links-outside.html
     */
    LinkGenerator grailsLinkGenerator

    public final static Integer EXPECTED_RESULTS_PER_DAY = 50;
    public final
    static Map<CachedView, Map<String, List<String>>> AGGREGATOR_GROUP_VALUES = ResultMeasuredValueService.getAggregatorMapForOptGroupSelect()

    public final
    static List<String> AGGREGATOR_GROUP_LABELS = ['de.iteratec.isocsi.csi.per.job', 'de.iteratec.isocsi.csi.per.page', 'de.iteratec.isocsi.csi.per.csi.group']

    List<Long> measuredValueIntervals = [MeasuredValueInterval.RAW, MeasuredValueInterval.HOURLY, MeasuredValueInterval.DAILY, MeasuredValueInterval.WEEKLY]


    public final static String DATE_FORMAT_STRING = 'dd.mm.yyyy';
    public final static int MONDAY_WEEKSTART = 1
    private final static SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat(DATE_FORMAT_STRING)
    //def timeFrames = [0, 900, 1800, 3600, 10800, 21600, 43200, 86400, 604800, 1209600, 2419200]
    //	def intervals = ['auto', 'max', '5min', '15min', '30min', '1h', '3h', '6h', '12h', 'daily', 'weekly']

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
        cmd.loadTimeMaximum = cmd.loadTimeMaximum?:"auto"
        Map<String, Object> modelToRender = constructStaticViewDataOfShowAll();
        cmd.copyRequestDataToViewModelMap(modelToRender);

        if (!ControllerUtils.isEmptyRequest(params)) {
            if (!cmd.validate()) {
                modelToRender.put('command', cmd)
            } else {
                // For validation errors if there is a request and it is not valid:

                boolean warnAboutLongProcessingTimeInsteadOfShowingData = false;
                if (!cmd.overwriteWarningAboutLongProcessingTime) {

                    int countOfSelectedBrowser = cmd.selectedBrowsers.size();
                    if (countOfSelectedBrowser < 1) {
                        countOfSelectedBrowser = ((List) modelToRender.get('browsers')).size();
                    }

                    int countOfSelectedAggregators = cmd.selectedAggrGroupValuesCached.size() + cmd.selectedAggrGroupValuesUnCached.size();

                    warnAboutLongProcessingTimeInsteadOfShowingData = shouldWarnAboutLongProcessingTime(cmd.getSelectedTimeFrame(), cmd.getSelectedInterval(), countOfSelectedAggregators, cmd.selectedFolder.size(), countOfSelectedBrowser, cmd.selectedPages.size());

                }

                if (warnAboutLongProcessingTimeInsteadOfShowingData) {
                    modelToRender.put('warnAboutLongProcessingTime', true)
                } else {
                    fillWithMeasuredValueData(modelToRender, cmd);
                }
            }
        }

        log.info("from=${modelToRender['from']}")
        log.info("to=${modelToRender['to']}")
        log.info("fromHour=${modelToRender['fromHour']}")
        log.info("toHour=${modelToRender['toHour']}")
        return modelToRender
    }

    /**
     * <p>
     * Ajax service to validate and store custom dashboard settings.
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
        String publiclyVisible = dashboardValues.publiclyVisible
        String wideScreenDiagramMontage = dashboardValues.wideScreenDiagramMontage

        // Check if name is unique
        def dashboards = UserspecificEventResultDashboard.findAllByDashboardName(dashboardName)
        if (dashboards) {
            response.sendError(302, 'dashboard by that name exists already')
            return null
        }

        // Parse JSON Data for Command
        Date fromDate = SIMPLE_DATE_FORMAT.parse(dashboardValues.from)
        Date toDate = SIMPLE_DATE_FORMAT.parse(dashboardValues.to)
        Collection<Long> selectedFolder = []
        dashboardValues.selectedFolder.each { l -> selectedFolder.add(Long.parseLong(l)) }
        Collection<Long> selectedPages = []
        dashboardValues.selectedPages.each { l -> selectedPages.add(Long.parseLong(l)) }
        Collection<Long> selectedMeasuredEventIds = []
        dashboardValues.selectedMeasuredEventIds.each { l -> selectedMeasuredEventIds.add(Long.parseLong(l)) }
        Collection<Long> selectedBrowsers = []
        dashboardValues.selectedBrowsers.each { l -> selectedBrowsers.add(Long.parseLong(l)) }
        Collection<Long> selectedLocations = []
        dashboardValues.selectedLocations.each { l -> selectedLocations.add(Long.parseLong(l)) }

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

        Collection<Long> selectedConnectivityProfiles = []
        dashboardValues.selectedConnectivityProfiles.each { l -> selectedConnectivityProfiles.add(Long.parseLong(l)) }

        // Create cmd for validation
        EventResultDashboardShowAllCommand cmd = new EventResultDashboardShowAllCommand(
                from: fromDate, to: toDate, fromHour: dashboardValues.fromHour, toHour: dashboardValues.toHour, aggrGroup: dashboardValues.aggrGroup,
                selectedFolder: selectedFolder, selectedPages: selectedPages, selectedMeasuredEventIds: selectedMeasuredEventIds, selectedAllMeasuredEvents: dashboardValues.selectedAllMeasuredEvents,
                selectedBrowsers: selectedBrowsers, selectedAllBrowsers: dashboardValues.selectedAllBrowsers, selectedLocations: selectedLocations, selectedAllLocations: dashboardValues.selectedAllLocations,
                selectedAggrGroupValuesCached: selectedAggrGroupValuesCached, selectedAggrGroupValuesUnCached: selectedAggrGroupValuesUnCached,
                overwriteWarningAboutLongProcessingTime: true, debug: dashboardValues.debug, setFromHour: dashboardValues.setFromHour, setToHour: dashboardValues.setToHour,
                includeCustomConnectivity: dashboardValues.includeCustomConnectivity, includeNativeConnectivity: dashboardValues.includeNativeConnectivity,
                selectedConnectivityProfiles: selectedConnectivityProfiles, selectedAllConnectivityProfiles: dashboardValues.selectedAllConnectivityProfiles, chartTitle: dashboardValues.chartTitle?:"",
                loadTimeMaximum: dashboardValues.loadTimeMaximum?:"auto", showDataLabels: dashboardValues.showDataLabels, showDataMarkers: dashboardValues.showDataMarkers)

        // Parse IntegerValues if they exist
        if (dashboardValues.selectedInterval) cmd.selectedInterval = dashboardValues.selectedInterval.toInteger()
        if (dashboardValues.selectedTimeFrameInterval) cmd.selectedTimeFrameInterval = dashboardValues.selectedTimeFrameInterval.toInteger()
        if (dashboardValues.selectChartType) cmd.selectChartType = dashboardValues.selectChartType.toInteger()
        if (dashboardValues.trimAboveRequestSizes) cmd.trimAboveRequestSizes = dashboardValues.trimAboveRequestSizes.toInteger()
        if (dashboardValues.trimBelowRequestSizes) cmd.trimBelowRequestSizes = dashboardValues.trimBelowRequestSizes.toInteger()
        if (dashboardValues.trimAboveRequestCounts) cmd.trimAboveRequestCounts = dashboardValues.trimAboveRequestCounts.toInteger()
        if (dashboardValues.trimBelowRequestCounts) cmd.trimBelowRequestCounts = dashboardValues.trimBelowRequestCounts.toInteger()
        if (dashboardValues.trimAboveLoadTimes) cmd.trimAboveLoadTimes = dashboardValues.trimAboveLoadTimes.toInteger()
        if (dashboardValues.trimBelowLoadTimes) cmd.trimBelowLoadTimes = dashboardValues.trimBelowLoadTimes.toInteger()
        if (dashboardValues.loadTimeMinimum) cmd.loadTimeMinimum = dashboardValues.loadTimeMinimum.toInteger()
        if (dashboardValues.chartHeight) cmd.chartHeight = dashboardValues.chartHeight.toInteger()
        if (dashboardValues.chartHeight) cmd.chartHeight = dashboardValues.chartHeight.toInteger()
        if (dashboardValues.chartWidth) cmd.chartWidth = dashboardValues.chartWidth.toInteger()
        // Check validation and send errors if needed
        if (!cmd.validate()) {
            //send errors
            def errMsgList = cmd.errors.allErrors.collect { g.message([error: it]) }
            response.sendError(400, "beginErrorMessage" + errMsgList.toString() + "endErrorMessage")
            // Apache Tomcat will output the response as part of (HTML) error page
        } else {
            UserspecificEventResultDashboard newCustomDashboard = new UserspecificEventResultDashboard(cmd, dashboardName, publiclyVisible, wideScreenDiagramMontage, username)

            if (!newCustomDashboard.save(failOnError: true, flush: true)) {
                response.sendError(500, 'save error')
            } else {
                response.sendError(200, 'OK')
            }
        }
    }

    private void fillWithMeasuredValueData(Map<String, Object> modelToRender, EventResultDashboardShowAllCommand cmd) {
        Interval timeFrame = cmd.getSelectedTimeFrame();

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

        MvQueryParams queryParams = cmd.createMvQueryParams();

        OsmRickshawChart chart = eventResultDashboardService.getEventResultDashboardHighchartGraphs(
                timeFrame.getStart().toDate(), timeFrame.getEnd().toDate(), cmd.selectedInterval, aggregators, queryParams
        )
        modelToRender.put("eventResultValues", chart.osmChartGraphs);

        modelToRender.put("labelSummary", chart.osmChartGraphsCommonLabel);

        if (isHighchartGraphLimitReached(chart.osmChartGraphs)) {
            modelToRender.put("warnAboutExceededPointsPerGraphLimit", true);
        }
        modelToRender.put("highChartsTurboThreshold", RESULT_DASHBOARD_MAX_POINTS_PER_SERIES);

        modelToRender.put("highChartLabels", labelToDataMap);
        modelToRender.put("markerShouldBeEnabled", false);
        modelToRender.put("labelShouldBeEnabled", false);

        //add / remove 5 Minutes
        modelToRender.put('fromTimestampForHighChart', (timeFrame.getStart().toDate().getTime() - 300000))
        modelToRender.put('toTimestampForHighChart', (timeFrame.getEnd().toDate().getTime() + 300000))

        modelToRender.put("selectedCharttypeForHighchart", cmd.getSelectChartType());
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

    /**
     * <p>
     * Checks if the maximum count of points per graph is exceeded.
     * </p>
     * <p><strong>Important: </strong> The current limit is 1000 points per graph: {@link http://api.highcharts.com/highcharts#plotOptions.series.turboThreshold} </p>
     *
     * @param graphCollection List of Highchart graphs, not <code>null</code>!
     * @return <code>true</code> if the limit is exceeded,
     *         <code>false</code> else.
     */
    private boolean isHighchartGraphLimitReached(List<OsmChartGraph> graphCollection) {
        boolean returnValue = false;

        graphCollection.each { OsmChartGraph graph ->
            if (graph.getPoints().size() > RESULT_DASHBOARD_MAX_POINTS_PER_SERIES) {
                returnValue = true;
            }
        }
        return returnValue;
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
     * Rounds a double according to CSV and Table requirements.
     *
     * @param valueToRound
     *         The double value to round.
     * @return The rounded value.
     * @since IT-102 / copy since: IT-188
     */
    static private double roundDouble(double valueToRound) {
        return new BigDecimal(valueToRound).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
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
    static void writeCSV(List<OsmChartGraph> source, Writer target, Locale localeForNumberFormat) throws IOException {
        NumberFormat valueFormat = NumberFormat.getNumberInstance(localeForNumberFormat);

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
                    row.add(valueFormat.format(roundDouble(point.measuredValue)));
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
            fillWithMeasuredValueData(modelToRender, cmd);
            cmd.copyRequestDataToViewModelMap(modelToRender)
        } else {
            redirectWith303('showAll', params)
            return
        }

        String filename = modelToRender['aggrGroupAndInterval'] + '_' + modelToRender['fromFormatted'] + '_to_' + modelToRender['toFormatted'] + '.csv';

        response.setHeader('Content-disposition', 'attachment; filename=' + filename);
        response.setContentType("text/csv;header=present;charset=UTF-8");

        Writer responseWriter = new OutputStreamWriter(response.getOutputStream());

        List<OsmChartGraph> csiValues = modelToRender['eventResultValues'];
        writeCSV(csiValues, responseWriter, RequestContextUtils.getLocale(request));

        response.getOutputStream().flush()
        response.sendError(200, 'OK');
        return null;
    }

    /**
     * <p>
     * Tests weather the UI should warn the user about an expected long
     * execution time for calculations on a time frame.
     * </p>
     *
     * @param timeFrame
     *         The time frame to guess weather a user should be warned
     *         about potently very long calculation time;
     *         not <code>null</code>.
     * @param countOfSelectedAggregatorTypes
     *         The number of selected aggregatorTypes; >= 1.
     * @param countOfSelectedSystems
     *         The number of selected systems / {@link JobGroup}s; >= 1.
     * @param countOfSelectedPages
     *         The number of selected pages; >= 1.
     * @param countOfSelectedBrowser
     *         The number of selected browser; >= 1.
     *
     * @return <code>true</code> if the user should be warned,
     *         <code>false</code> else.
     * @since IT-157
     */
    public boolean shouldWarnAboutLongProcessingTime(
            Interval timeFrame,
            int interval,
            int countOfSelectedAggregatorTypes,
            int countOfSelectedSystems,
            int countOfSelectedBrowser,
            int countOfSelectedPages) {
        int minutesInTimeFrame = new Duration(timeFrame.getStart(), timeFrame.getEnd()).getStandardMinutes();

        long expectedPointsOfEachGraph;
        if (interval == MeasuredValueInterval.RAW || interval == 0 || interval == null) {
            //50 results per Day
            expectedPointsOfEachGraph = Math.round(minutesInTimeFrame / 60 / 24 * EXPECTED_RESULTS_PER_DAY);
        } else {
            expectedPointsOfEachGraph = Math.round(minutesInTimeFrame / interval);
        }

        if (expectedPointsOfEachGraph > 1000) {
            return true;
        } else {

            long expectedCountOfGraphs = countOfSelectedAggregatorTypes * countOfSelectedSystems * countOfSelectedPages * countOfSelectedBrowser;
            long expectedTotalNumberOfPoints = expectedCountOfGraphs * expectedPointsOfEachGraph;

            return expectedTotalNumberOfPoints > 10000;
        }
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
    public Map<String, Object> constructStaticViewDataOfShowAll() {
        Map<String, Object> result = [:]

        // AggregatorTypes
        result.put('aggrGroupLabels', AGGREGATOR_GROUP_LABELS)
        result.put('aggrGroupValuesCached', AGGREGATOR_GROUP_VALUES.get(CachedView.CACHED))
        result.put('aggrGroupValuesUnCached', AGGREGATOR_GROUP_VALUES.get(CachedView.UNCACHED))

        // Intervals
        result.put('measuredValueIntervals', measuredValueIntervals)

        // JobGroups
        List<JobGroup> jobGroups = eventResultDashboardService.getAllJobGroups()
        result.put('folders', jobGroups)

        // Pages
        List<Page> pages = eventResultDashboardService.getAllPages()
        result.put('pages', pages)

        // MeasuredEvents
        List<MeasuredEvent> measuredEvents = eventResultDashboardService.getAllMeasuredEvents()
        result.put('measuredEvents', measuredEvents)

        // Browsers
        List<Browser> browsers = eventResultDashboardService.getAllBrowser()
        result.put('browsers', browsers)

        // Locations
        List<Location> locations = eventResultDashboardService.getAllLocations()
        result.put('locations', locations)

        // ConnectivityProfiles
        result['connectivityProfiles'] = eventResultDashboardService.getAllConnectivityProfiles()

        // JavaScript-Utility-Stuff:
        result.put("dateFormat", DATE_FORMAT_STRING)
        result.put("weekStart", MONDAY_WEEKSTART)

        // --- Map<PageID, Set<MeasuredEventID>> for fast view filtering:
        Map<Long, Set<Long>> eventsOfPages = new HashMap<Long, Set<Long>>()
        for (Page eachPage : pages) {
            Set<Long> eventIds = new HashSet<Long>();

            Collection<Long> ids = measuredEvents.findResults {
                it.testedPage.getId() == eachPage.getId() ? it.getId() : null
            }
            if (!ids.isEmpty()) {
                eventIds.addAll(ids);
            }

            eventsOfPages.put(eachPage.getId(), eventIds);
        }
        result.put('eventsOfPages', eventsOfPages);

        // --- Map<BrowserID, Set<LocationID>> for fast view filtering:
        Map<Long, Set<Long>> locationsOfBrowsers = new HashMap<Long, Set<Long>>()
        for (Browser eachBrowser : browsers) {
            Set<Long> locationIds = new HashSet<Long>();

            Collection<Long> ids = locations.findResults {
                it.browser.getId() == eachBrowser.getId() ? it.getId() : null
            }
            if (!ids.isEmpty()) {
                locationIds.addAll(ids);
            }

            locationsOfBrowsers.put(eachBrowser.getId(), locationIds);
        }
        result.put('locationsOfBrowsers', locationsOfBrowsers);

        result.put("selectedChartType", 0);
        result.put("warnAboutExceededPointsPerGraphLimit", false);

        // Done! :)
        return result;
    }

}
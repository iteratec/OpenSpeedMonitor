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
import de.iteratec.osm.measurement.schedule.JobGroupService
import de.iteratec.osm.p13n.CustomDashboardService
import de.iteratec.osm.report.UserspecificDashboardBase
import de.iteratec.osm.report.UserspecificDashboardService
import de.iteratec.osm.report.UserspecificEventResultDashboard
import de.iteratec.osm.report.chart.CsiAggregationInterval
import de.iteratec.osm.report.chart.EventService
import de.iteratec.osm.report.chart.OsmChartAxis
import de.iteratec.osm.report.chart.OsmRickshawChart
import de.iteratec.osm.util.AnnotationUtil
import de.iteratec.osm.util.ControllerUtils
import de.iteratec.osm.util.I18nService
import de.iteratec.osm.util.ParameterBindingUtility
import grails.converters.JSON
import grails.web.mapping.LinkGenerator
import org.grails.web.json.JSONObject
import org.joda.time.DateTime
import org.joda.time.Interval
import org.springframework.dao.DataIntegrityViolationException

class EventResultDashboardController {

    JobGroupService jobGroupService
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

        Collection<Measurand> selectedAggrGroupValuesCached = []
        // String or List<String>
        def valuesCached = dashboardValues.selectedAggrGroupValuesCached
        if (valuesCached) {
            if (valuesCached.class == String) {
                selectedAggrGroupValuesCached.add(valuesCached)
            } else {
                valuesCached.each { l -> selectedAggrGroupValuesCached.add(l) }
            }
        }

        Collection<Measurand> selectedAggrGroupValuesUnCached = []
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
        if (dashboardValues.trimAboveRequestSizes) cmd.trimAboveRequestSizes = dashboardValues.trimAboveRequestSizes
        if (dashboardValues.trimBelowRequestSizes) cmd.trimBelowRequestSizes = dashboardValues.trimBelowRequestSizes
        if (dashboardValues.trimAboveRequestCounts) cmd.trimAboveRequestCounts = dashboardValues.trimAboveRequestCounts
        if (dashboardValues.trimBelowRequestCounts) cmd.trimBelowRequestCounts = dashboardValues.trimBelowRequestCounts
        if (dashboardValues.trimAboveLoadTimes) cmd.trimAboveLoadTimes = dashboardValues.trimAboveLoadTimes
        if (dashboardValues.trimBelowLoadTimes) cmd.trimBelowLoadTimes = dashboardValues.trimBelowLoadTimes
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

        List<SelectedMeasurand> allMeasurands = modelToRender.get('selectedAggrGroupValues')

        List<OsmChartAxis> labelToDataMap = allMeasurands.collect {
                new OsmChartAxis(
                    it.getMeasurandGroup(),
                    i18nService.msg("de.iteratec.isr.measurand.group.${it.getMeasurandGroup()}", it.getMeasurandGroup().toString())
                    + " [" + it.getMeasurandGroup().getUnit().getLabel() + "]",
                            getAxisSide(it.getMeasurandGroup()) )
        }

        ErQueryParams queryParams = cmd.createErQueryParams();

        OsmRickshawChart chart = eventResultDashboardService.getEventResultDashboardHighchartGraphs(
                timeFrame.getStart().toDate(), timeFrame.getEnd().toDate(), cmd.selectedInterval, allMeasurands, queryParams
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

    private int getAxisSide(MeasurandGroup measurandGroup){
        if(measurandGroup == MeasurandGroup.LOAD_TIMES){
            return OsmChartAxis.LEFT_CHART_SIDE
        }else{
            return OsmChartAxis.RIGHT_CHART_SIDE
        }
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
                'aggrGroupValuesCached'  : SelectedMeasurand.createDataMapForOptGroupSelect(),
                'aggrGroupValuesUnCached': SelectedMeasurand.createDataMapForOptGroupSelect(),
                'aggregationIntervals'   : AGGREGATION_INTERVALS,
                'folders'                : eventResultDashboardService.getAllJobGroups(),
                'pages'                  : pages,
                'measuredEvents'         : measuredEvents,
                'browsers'               : browsers,
                'locations'              : locations,
                'avaiableConnectivities' : eventResultDashboardService.getAllConnectivities(true),
                'dateFormat'             : DATE_FORMAT_STRING_FOR_HIGH_CHART,
                'tagToJobGroupNameMap'   : jobGroupService.getTagToJobGroupNameMap(),
                'eventsOfPages'          : eventsOfPages,
                'locationsOfBrowsers'    : locationsOfBrowsers,
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

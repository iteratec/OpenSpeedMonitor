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

package de.iteratec.osm.csi

import de.iteratec.osm.ConfigService
import de.iteratec.osm.csi.transformation.DefaultTimeToCsMappingService
import de.iteratec.osm.csi.transformation.TimeToCsMappingService
import de.iteratec.osm.measurement.environment.Browser
import de.iteratec.osm.measurement.environment.Location
import de.iteratec.osm.measurement.environment.dao.BrowserDaoService
import de.iteratec.osm.measurement.environment.dao.LocationDaoService
import de.iteratec.osm.measurement.schedule.ConnectivityProfileDaoService
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.measurement.schedule.dao.JobGroupDaoService
import de.iteratec.osm.measurement.schedule.dao.PageDaoService
import de.iteratec.osm.p13n.CookieBasedSettingsService
import de.iteratec.osm.p13n.CustomDashboardService
import de.iteratec.osm.report.UserspecificCsiDashboard
import de.iteratec.osm.report.UserspecificDashboardBase
import de.iteratec.osm.report.UserspecificDashboardService
import de.iteratec.osm.report.chart.*
import de.iteratec.osm.report.chart.dao.AggregatorTypeDaoService
import de.iteratec.osm.result.EventResultService
import de.iteratec.osm.result.MeasuredEvent
import de.iteratec.osm.result.MvQueryParams
import de.iteratec.osm.result.dao.MeasuredEventDaoService
import de.iteratec.osm.util.AnnotationUtil
import de.iteratec.osm.util.ControllerUtils
import de.iteratec.osm.util.I18nService
import de.iteratec.osm.util.TreeMapOfTreeMaps
import grails.converters.JSON
import grails.plugin.springsecurity.SpringSecurityService
import grails.web.mapping.LinkGenerator
import org.grails.web.json.JSONObject
import org.joda.time.DateTime
import org.joda.time.Days
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

import static de.iteratec.osm.csi.Contract.requiresArgumentNotNull

//TODO: implement some tests for this controller

/**
 * <p>
 * The controller for the customer satisfaction index (CSI) dashboard.
 * </p>
 */
class CsiDashboardController {

    AggregatorTypeDaoService aggregatorTypeDaoService
    JobGroupDaoService jobGroupDaoService
    PageDaoService pageDaoService
    MeasuredEventDaoService measuredEventDaoService
    BrowserDaoService browserDaoService
    LocationDaoService locationDaoService
    I18nService i18nService

    EventResultService eventResultService
    CustomerSatisfactionHighChartService customerSatisfactionHighChartService
    CsiHelperService csiHelperService
    CsiAggregationUtilService csiAggregationUtilService
    CookieBasedSettingsService cookieBasedSettingsService
    EventService eventService
    SpringSecurityService springSecurityService
    ConnectivityProfileDaoService connectivityProfileDaoService
    DefaultTimeToCsMappingService defaultTimeToCsMappingService
    TimeToCsMappingService timeToCsMappingService
    ConfigService configService
    CustomDashboardService customDashboardService
    UserspecificDashboardService userspecificDashboardService

    /**
     * The Grails engine to generate links.
     *
     * @see http://mrhaki.blogspot.ca/2012/01/grails-goodness-generate-links-outside.html
     */
    LinkGenerator grailsLinkGenerator

    public final static String DATE_FORMAT_STRING_FOR_HIGH_CHART = 'dd.mm.yyyy'
    public final static String DATE_FORMAT_STRING = 'dd.MM.yyyy'
    private final static SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat(DATE_FORMAT_STRING)

    public static final String DAILY_AGGR_GROUP_PAGE = 'daily_page'
    public static final String DAILY_AGGR_GROUP_SHOP = 'daily_shop'
    public static final String DAILY_AGGR_GROUP_SYSTEM = 'daily_system'
    public static final String WEEKLY_AGGR_GROUP_PAGE = 'weekly_page'
    public static final String WEEKLY_AGGR_GROUP_SHOP = 'weekly_shop'
    public static final String WEEKLY_AGGR_GROUP_SYSTEM = 'weekly_system'
    public static final String HOURLY_MEASURED_EVENT = "measured_event"

    String DATE_TIME_FORMAT_STRING = 'dd.MM.yyyy HH:mm:ss'
    public final static int MONDAY_WEEKSTART = 1
    public final static List<String> AGGREGATOR_GROUP_VALUES = [HOURLY_MEASURED_EVENT,
                                                                DAILY_AGGR_GROUP_PAGE, WEEKLY_AGGR_GROUP_PAGE,
                                                                DAILY_AGGR_GROUP_SHOP, WEEKLY_AGGR_GROUP_SHOP,
                                                                DAILY_AGGR_GROUP_SYSTEM, WEEKLY_AGGR_GROUP_SYSTEM]
    public final static List<String> AGGREGATOR_GROUP_LABELS = ['de.iteratec.isocsi.csi.per.measured_event',
                                                                'de.iteratec.isocsi.csi.per.page.daily', 'de.iteratec.isocsi.csi.per.page',
                                                                'de.iteratec.isocsi.csi.per.csi.group.daily', 'de.iteratec.isocsi.csi.per.csi.group',
                                                                'de.iteratec.isocsi.csi.per.csi.system.daily', 'de.iteratec.isocsi.csi.per.csi.system.weekly']

    /**
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
     * @param actionNameToRedirectTo The Name of the action to redirect to;
     *        not <code>null</code>.
     *
     * @see <a href="http://tools.ietf.org/html/rfc2616#section-10.3.4"
     *      >http://tools.ietf.org/html/rfc2616#section-10.3.4</a>
     */
    private void redirectWith303(String actionNameToRedirectTo) {
        // There is a missing feature to do this:
        // http://jira.grails.org/browse/GRAILS-8829

        // Workaround based on:
        // http://fillinginthegaps.wordpress.com/2008/12/26/grails-301-moved-permanently-redirect/
        String uri = grailsLinkGenerator.link(action: actionNameToRedirectTo)
        response.setStatus(303)
        response.setHeader("Location", uri)
        render(status: 303)
    }

    /**
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
     */
    private void redirectWith303(String actionNameToRedirectTo, Map urlParams) {
        // There is a missing feature to do this:
        // http://jira.grails.org/browse/GRAILS-8829

        // Workaround based on:
        // http://fillinginthegaps.wordpress.com/2008/12/26/grails-301-moved-permanently-redirect/
        Map paramsWithoutGrailsActionNameOfOldAction = urlParams.findAll({ Map.Entry m -> !m.getKey().toString().startsWith('_action') })
        String uri = grailsLinkGenerator.link(action: actionNameToRedirectTo, params: paramsWithoutGrailsActionNameOfOldAction)
        response.setStatus(303)
        response.setHeader("Location", uri)
        render(status: 303)
    }

    /**
     * Redirects to {@link #showAll()}.
     *
     * @return Nothing , redirects immediately.
     */
    Map<String, Object> index() {
        redirectWith303('showAll')
    }

    /**
     * deletes custom Dashboard
     *
     * @return Nothing , redirects immediately.
     */
    Map<String, Object> delete() {

        UserspecificCsiDashboard userspecificCSIDashboardInstance = UserspecificCsiDashboard.get(params.id)
        if (!userspecificCSIDashboardInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'custom.dashboard.label', default: 'Custom dashboard'), params.id])
            redirect(action: "showAll")
            return
        }

        try {
            userspecificCSIDashboardInstance.delete(flush: true)
            flash.message = message(code: 'default.deleted.message', args: [message(code: 'custom.dashboard.label', default: 'Custom dashboard'), params.id])
            redirect(action: "showAll")
        } catch (DataIntegrityViolationException e) {
            flash.message = message(code: 'default.not.deleted.message', args: [message(code: 'custom.dashboard.label', default: 'Custom dashboard'), params.id])
            redirect(action: "showAll", id: params.id)
        }

        redirectWith303('showAll')
    }

    /**
     * Thats the view used to show CSI graphs with previous selection of date
     * range, groups and more filter criteria. This page is intended to be
     * used by admins and developers.
     *
     * @param cmd The request / command send to this action,
     *            not <code>null</code>.
     * @return A CSI model map to be used by the corresponding GSP,
     * 	       not <code>null</code> and never
     * {@linkplain Map#isEmpty() empty}.
     */
    Map<String, Object> showAll(CsiDashboardShowAllCommand cmd) {
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
        cmd.chartWidth = cmd.chartWidth > 0 ? cmd.chartWidth : configService.getInitialChartWidthInPixels()
        Map<String, Object> modelToRender = constructStaticViewDataOfShowAll()

        cmd.copyRequestDataToViewModelMap(modelToRender)
        // Validate command for errors if there was a non-empty, non-"only-language-change" request:
        if (!ControllerUtils.isEmptyRequest(params) && requestedAllowedDashboard) {
            if (!cmd.validate()) {
                modelToRender.put('command', cmd)
            } else {

                boolean warnAboutLongProcessingTimeInsteadOfShowingData = false

                if (!cmd.overwriteWarningAboutLongProcessingTime) {
                    int countOfSelectedEvents = cmd.selectedMeasuredEventIds.size()
                    if (countOfSelectedEvents < 1) {
                        // User checked: "Select all"
                        countOfSelectedEvents = ((Collection) modelToRender.get('measuredEvents')).size()
                    }

                    int selectedAggregationIntervallInMintues = cmd.receiveSelectedMeasuredIntervalInMinutes()

                    int countOfSelectedBrowser = cmd.selectedBrowsers.size()
                    if (countOfSelectedBrowser < 1) {
                        countOfSelectedBrowser = ((List) modelToRender.get('browsers')).size()
                    }

                    warnAboutLongProcessingTimeInsteadOfShowingData = cmd.shouldWarnAboutLongProcessingTime(
                            fixTimeFrame(cmd.receiveSelectedTimeFrame(), selectedAggregationIntervallInMintues),
                            selectedAggregationIntervallInMintues,
                            countOfSelectedBrowser)
                }

                if (warnAboutLongProcessingTimeInsteadOfShowingData) {
                    modelToRender.put('warnAboutLongProcessingTime', true)
                } else {
                    fillWithAproximateCsiAggregationData(modelToRender, cmd, true, CsiType.getCsiTypes(cmd))
                }
            }
        }

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
    private void fillWithUserspecificDashboardValues(CsiDashboardShowAllCommand cmd, String dashboardID) {
        UserspecificCsiDashboard dashboard = UserspecificCsiDashboard.get(Long.parseLong(dashboardID))

        cmd.with {
            from = dashboard.fromDate
            to = dashboard.toDate
            fromHour = dashboard.fromHour
            toHour = dashboard.toHour
            aggrGroupAndInterval = dashboard.aggrGroup

            if (dashboard.selectedFolder) {
                for (item in dashboard.selectedFolder.tokenize(',')) {
                    selectedFolder.add(Long.parseLong(item))
                }
            }
            if (dashboard.selectedPages) {
                for (item in dashboard.selectedPages.tokenize(',')) {
                    selectedPages.add(Long.parseLong(item))
                }
            }
            if (dashboard.selectedMeasuredEventIds) {
                for (item in dashboard.selectedMeasuredEventIds.tokenize(',')) {
                    selectedMeasuredEventIds.add(Long.parseLong(item))
                }
            }
            if (dashboard.selectedBrowsers) {
                for (item in dashboard.selectedBrowsers.tokenize(',')) {
                    selectedBrowsers.add(Long.parseLong(item))
                }
            }
            if (dashboard.selectedLocations) {
                for (item in dashboard.selectedLocations.tokenize(',')) {
                    selectedLocations.add(Long.parseLong(item))
                }
            }
            if (dashboard.selectedCsiSystems) {
                for (item in dashboard.selectedCsiSystems.tokenize(',')) {
                    selectedCsiSystems.add(Long.parseLong(item))
                }
            }

            selectedAllBrowsers = dashboard.selectedAllBrowsers
            selectedAllLocations = dashboard.selectedAllLocations

            overwriteWarningAboutLongProcessingTime = dashboard.overwriteWarningAboutLongProcessingTime
            debug = dashboard.debug
            selectedTimeFrameInterval = dashboard.selectedTimeFrameInterval
            setFromHour = dashboard.setFromHour
            setToHour = dashboard.setToHour
            includeInterval = dashboard.includeInterval

            chartTitle = dashboard.chartTitle
            chartWidth = dashboard.chartWidth
            chartHeight = dashboard.chartHeight
            loadTimeMinimum = dashboard.loadTimeMinimum
            loadTimeMaximum = dashboard.loadTimeMaximum
            showDataMarkers = dashboard.showDataMarkers
            showDataLabels = dashboard.showDataLabels
            csiTypeDocComplete = dashboard.csiTypeDocComplete
            csiTypeVisuallyComplete = dashboard.csiTypeVisuallyComplete
            wideScreenDiagramMontage = dashboard.wideScreenDiagramMontage

            if (dashboard.graphNameAliases.size() > 0) {
                graphNameAliases = dashboard.graphNameAliases
            }
            if (dashboard.graphColors.size() > 0) {
                graphColors = dashboard.graphColors
            }

            dashboardName = dashboard.dashboardName
            publiclyVisible = dashboard.publiclyVisible
        }
    }
/**
 * <p>
 * Fills the specified map with approximate data based on {@linkplain
 * CsiAggregation measured values} correspond to the selection in
 * specified {@linkplain CsiDashboardShowAllCommand command object}.
 * </p>
 *
 * @param modelToRender
 *         The map to be filled. Previously added entries are overridden.
 *         This map should not be <code>null</code>.
 * @param cmd
 *         The command with users selections, not <code>null</code>.
 * @param withTargetGraph
 *         If <code>true</code> the CSI target graph will be added to
 *         the graphs in {@link modelToRender} else, if set to
 *         <code>false</code> not.
 */
    private void fillWithAproximateCsiAggregationData(Map<String, Object> modelToRender, CsiDashboardShowAllCommand cmd, boolean withTargetGraph, List<CsiType> csiType) {
        // TODO Test this: Structure and data...

        requiresArgumentNotNull('modelToRender', modelToRender)
        requiresArgumentNotNull('cmd', cmd)

        Interval timeFrame = cmd.receiveSelectedTimeFrame()
        log.info("Timeframe for CSI-Dashboard=$timeFrame")

        MvQueryParams csiAggregationsQueryParams = cmd.createMvQueryParams()
        modelToRender.put("yAxisMax", cmd.loadTimeMaximum)
        modelToRender.put("yAxisMin", cmd.loadTimeMinimum)

        switch (cmd.aggrGroupAndInterval) {
            case WEEKLY_AGGR_GROUP_PAGE:
                CsiAggregationInterval weeklyInterval = CsiAggregationInterval.findByIntervalInMinutes(CsiAggregationInterval.WEEKLY)
                fillWithPageValuesAsHighChartMap(modelToRender, timeFrame, weeklyInterval, csiAggregationsQueryParams, withTargetGraph, csiType)
                break
            case DAILY_AGGR_GROUP_PAGE:
                CsiAggregationInterval dailyInterval = CsiAggregationInterval.findByIntervalInMinutes(CsiAggregationInterval.DAILY)
                fillWithPageValuesAsHighChartMap(modelToRender, timeFrame, dailyInterval, csiAggregationsQueryParams, withTargetGraph, csiType)
                break
            case WEEKLY_AGGR_GROUP_SHOP:
                CsiAggregationInterval weeklyInterval = CsiAggregationInterval.findByIntervalInMinutes(CsiAggregationInterval.WEEKLY)
                fillWithShopValuesAsHighChartMap(modelToRender, timeFrame, weeklyInterval, csiAggregationsQueryParams, withTargetGraph, false, csiType)
                break
            case DAILY_AGGR_GROUP_SHOP:
                CsiAggregationInterval dailyInterval = CsiAggregationInterval.findByIntervalInMinutes(CsiAggregationInterval.DAILY)
                fillWithShopValuesAsHighChartMap(modelToRender, timeFrame, dailyInterval, csiAggregationsQueryParams, withTargetGraph, false, csiType)
                break
            case WEEKLY_AGGR_GROUP_SYSTEM:
                CsiAggregationInterval weeklyInterval = CsiAggregationInterval.findByIntervalInMinutes(CsiAggregationInterval.WEEKLY)
                fillWithCsiSystemValuesAsHighChartMap(modelToRender, timeFrame, weeklyInterval, cmd.selectedCsiSystems, withTargetGraph, false, csiType)
                break
            case DAILY_AGGR_GROUP_SYSTEM:
                CsiAggregationInterval dailyInterval = CsiAggregationInterval.findByIntervalInMinutes(CsiAggregationInterval.DAILY)
                fillWithCsiSystemValuesAsHighChartMap(modelToRender, timeFrame, dailyInterval, cmd.selectedCsiSystems, withTargetGraph, false, csiType)
                break
            default: // AggregatorType.MEASURED_EVENT
                fillWithHourlyValuesAsHighChartMap(modelToRender, timeFrame, csiAggregationsQueryParams, csiType)
                break
        }
        if (cmd.aggrGroupAndInterval == WEEKLY_AGGR_GROUP_SYSTEM || cmd.aggrGroupAndInterval == DAILY_AGGR_GROUP_SYSTEM) {
            List<JobGroup> jobGroups = CsiSystem.getAll(cmd.selectedCsiSystems)*.affectedJobGroups
            Collection<Long> jobGroupsIds = jobGroups*.id.flatten().unique(false)
            fillWithAnnotations(modelToRender, timeFrame, jobGroupsIds)
        } else {
            fillWithAnnotations(modelToRender, timeFrame, cmd.selectedFolder)
        }
    }

    /**
     * <p>
     * Fills the view-model-map with page values. Calling this method
     * should not be mixed with other operations than weekly page depended
     * ones.
     * </p>
     *
     * @param timeFrame
     *         The time-frame for that data should be calculated,
     *         not <code>null</code>.
     * @param modelToRender
     *         The map to be filled. Previously added entries are overridden.
     *         This map should not be <code>null</code>.
     * @param csiAggregationsQueryParams
     *         The {@linkplain MvQueryParams filter} to select relevant
     *         measured values, not <code>null</code>.
     */
    private void fillWithPageValuesAsHighChartMap(Map<String, Object> modelToRender, Interval timeFrame, CsiAggregationInterval interval, MvQueryParams csiAggregationsQueryParams, boolean withTargetGraph, List<CsiType> csiType) {
        // TODO Test this: Structure and data...

        Interval fixedTimeFrame = fixTimeFrame(timeFrame, interval.getIntervalInMinutes())


        OsmRickshawChart chart = customerSatisfactionHighChartService.getCalculatedPageCsiAggregationsAsHighChartMap(fixedTimeFrame, csiAggregationsQueryParams, interval, csiType)
        List<OsmChartGraph> graphs = chart.osmChartGraphs

        DateTime resetFromDate = fixedTimeFrame.getStart()
        DateTime resetToDate = fixedTimeFrame.getEnd()

        if (withTargetGraph) {
            graphs.addAll(customerSatisfactionHighChartService.getCsRelevantStaticGraphsAsResultMapForChart(
                    resetFromDate.minusDays(1), resetToDate.plusDays(1)))
        }

        boolean includeCsTargetGraphs = false

        //add / remove 5 Minutes
        modelToRender.put('fromTimestampForHighChart', (resetFromDate.toDate().getTime() - 300000))
        modelToRender.put('toTimestampForHighChart', (resetToDate.toDate().getTime() + 300000))

        modelToRender.put('wptCustomerSatisfactionValues', graphs)
        modelToRender.put('wptCustomerSatisfactionValuesForTable', formatForTable(graphs, includeCsTargetGraphs))

        modelToRender.put('labelSummary', chart.osmChartGraphsCommonLabel);

        modelToRender.put('markerShouldBeEnabled', true)
        modelToRender.put('labelShouldBeEnabled', false)
    }

    /**
     * <p>
     * Fills the view-model-map with hourly measured values. Calling this
     * method should not be mixed with other operations than hourly depended
     * ones.
     * </p>
     *
     * @param timeFrame
     *         The time-frame for that data should be calculated,
     *         not <code>null</code>.
     * @param timeFrame
     *         The time frame values to be delivered for,
     *         not <code>null</code>.
     * @param queryParams
     *         The query parameters to find corresponding {@linkplain
     * CsiAggregation measured vales}.
     * @param modelToRender
     *         The map to be filled. Previously added entries are overridden.
     *         This map should not be <code>null</code>.
     */
    private void fillWithHourlyValuesAsHighChartMap(Map<String, Object> modelToRender, Interval timeFrame, MvQueryParams queryParams, List<CsiType> csiType) {
        // TODO Test this: Structure and data...

        Interval fixedTimeFrame = fixTimeFrame(timeFrame, CsiAggregationInterval.HOURLY)

        OsmRickshawChart chart = customerSatisfactionHighChartService.getCalculatedHourlyEventCsiAggregationsAsHighChartMap(
                fixedTimeFrame.getStart().toDate(), fixedTimeFrame.getEnd().toDate(), queryParams, csiType
        )

        DateTime resetFromDate = fixedTimeFrame.getStart()
        DateTime resetToDate = fixedTimeFrame.getEnd()

        //		csiValueMap.putAll(customerSatisfactionHighChartService.getCsRelevantStaticGraphsAsResultMapForChart(
        //			resetFromDate.minusDays(1), resetToDate.plusDays(1)))

        boolean includeCsTargetGraphs = true
        modelToRender.put('fromTimestampForHighChart', resetFromDate.toDate().getTime())
        modelToRender.put('toTimestampForHighChart', resetToDate.toDate().getTime())
        modelToRender.put('wptCustomerSatisfactionValues', chart.osmChartGraphs)
        modelToRender.put('wptCustomerSatisfactionValuesForTable', formatForTable(chart.osmChartGraphs, includeCsTargetGraphs))

        modelToRender.put('labelSummary', chart.osmChartGraphsCommonLabel);

        modelToRender.put('markerShouldBeEnabled', false)
        modelToRender.put('labelShouldBeEnabled', false)
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
     * Fills the view-model-map with shop values. Calling this method
     * should not be mixed with other operations than weekly shop depended
     * ones.
     * </p>
     *
     * @param modelToRender
     *         The map to be filled. Previously added entries are overridden.
     *         This map should not be <code>null</code>.
     * @param timeFrame
     *         The time-frame for that data should be calculated,
     *         not <code>null</code>.
     * @param interval
     *         The interval of measured values to include in calculation and
     *         used for "fixing" the time-frame boundaries to find all measured
     *         values in this interval, not <code>null</code>.
     * @param csiAggregationsQueryParams
     *         The {@linkplain MvQueryParams filter} to select relevant
     *         measured values, not <code>null</code>.
     * @param withTargetGraph
     *  		Whether or not to include {@link CsTargetGraph}s.
     * @param moveGraphsByOneWeek
     *  		Whether or not to move all {@link OsmChartPoint}s to the end of their interval (in default-CSI-dashboard this is the default-behaviour).
     */
    private void fillWithShopValuesAsHighChartMap(
            Map<String, Object> modelToRender,
            Interval timeFrame,
            CsiAggregationInterval interval,
            MvQueryParams csiAggregationsQueryParams,
            boolean withTargetGraph,
            boolean moveGraphsByOneWeek,
            List<CsiType> csiType) {
        Interval fixedTimeFrame = fixTimeFrame(timeFrame, interval.getIntervalInMinutes())

        DateTime resetFromDate = fixedTimeFrame.getStart()
        DateTime resetToDate = fixedTimeFrame.getEnd()

        OsmRickshawChart chart = customerSatisfactionHighChartService.getCalculatedShopCsiAggregationsAsHighChartMap(
                fixedTimeFrame, interval, csiAggregationsQueryParams, csiType
        )
        List<OsmChartGraph> graphs = chart.osmChartGraphs

        if (moveGraphsByOneWeek == true) {
            moveDataPointsOneWeekForward(graphs)
            resetFromDate = resetFromDate.plusWeeks(1)
            resetToDate = resetToDate.plusWeeks(1)
        }

        Integer oneDayOffset = Math.round(CsiAggregationInterval.DAILY)
        DateTime resetFromDateWithOffsetChange = resetFromDate.minusMinutes(oneDayOffset)
        Integer rightOffset = oneDayOffset
        DateTime resetToDateWithOffsetChange = resetToDate.plusMinutes(rightOffset)

        if (withTargetGraph) {
            graphs.addAll(customerSatisfactionHighChartService.getCsRelevantStaticGraphsAsResultMapForChart(
                    resetFromDateWithOffsetChange.minusDays(1), resetToDateWithOffsetChange.plusDays(1))
            )
        }

        boolean includeCsTargetGraphs = true
        modelToRender.put('fromTimestampForHighChart', resetFromDateWithOffsetChange.toDate().getTime())
        modelToRender.put('toTimestampForHighChart', resetToDateWithOffsetChange.toDate().getTime())
        modelToRender.put('wptCustomerSatisfactionValues', graphs)
        modelToRender.put('wptCustomerSatisfactionValuesForTable', formatForTable(graphs, includeCsTargetGraphs))

        modelToRender.put('labelSummary', chart.osmChartGraphsCommonLabel);

        modelToRender.put('markerShouldBeEnabled', true)
        modelToRender.put('labelShouldBeEnabled', false)
    }

    /**
     * <p>
     * Fills the view-model-map with csiSystem values.
     * </p>
     *
     * @param modelToRender
     *         The map to be filled. Previously added entries are overridden.
     *         This map should not be <code>null</code>.
     * @param timeFrame
     *         The time-frame for that data should be calculated,
     *         not <code>null</code>.
     * @param interval
     *         The interval of measured values to include in calculation and
     *         used for "fixing" the time-frame boundaries to find all measured
     *         values in this interval, not <code>null</code>.
     * @param selectedCsiSystems
     *         The {@linkplain CsiSystem} to select relevant
     *         measured values, not <code>null</code>.
     * @param withTargetGraph
     *  		Whether or not to include {@link CsTargetGraph}s.
     * @param moveGraphsByOneWeek
     *  		Whether or not to move all {@link OsmChartPoint}s to the end of their interval (in default-CSI-dashboard this is the default-behaviour).
     */
    private void fillWithCsiSystemValuesAsHighChartMap(
            Map<String, Object> modelToRender,
            Interval timeFrame,
            CsiAggregationInterval interval,
            Set<Long> selectedCsiSystems,
            boolean withTargetGraph,
            boolean moveGraphsByOneWeek,
            List<CsiType> csiType) {

        Interval fixedTimeFrame = fixTimeFrame(timeFrame, interval.getIntervalInMinutes())

        DateTime resetFromDate = fixedTimeFrame.getStart()
        DateTime resetToDate = fixedTimeFrame.getEnd()

        OsmRickshawChart chart = customerSatisfactionHighChartService.getCalculatedCsiSystemCsiAggregationsAsHighChartMap(
                fixedTimeFrame, interval, selectedCsiSystems, csiType
        )
        List<OsmChartGraph> graphs = chart.osmChartGraphs

        if (moveGraphsByOneWeek == true) {
            moveDataPointsOneWeekForward(graphs)
            resetFromDate = resetFromDate.plusWeeks(1)
            resetToDate = resetToDate.plusWeeks(1)
        }

        Integer oneDayOffset = Math.round(CsiAggregationInterval.DAILY)
        DateTime resetFromDateWithOffsetChange = resetFromDate.minusMinutes(oneDayOffset)
        Integer rightOffset = oneDayOffset
        DateTime resetToDateWithOffsetChange = resetToDate.plusMinutes(rightOffset)

        if (withTargetGraph) {
            graphs.addAll(customerSatisfactionHighChartService.getCsRelevantStaticGraphsAsResultMapForChart(
                    resetFromDateWithOffsetChange.minusDays(1), resetToDateWithOffsetChange.plusDays(1))
            )
        }

        boolean includeCsTargetGraphs = true
        modelToRender.put('fromTimestampForHighChart', resetFromDateWithOffsetChange.toDate().getTime())
        modelToRender.put('toTimestampForHighChart', resetToDateWithOffsetChange.toDate().getTime())
        modelToRender.put('wptCustomerSatisfactionValues', graphs)
        modelToRender.put('wptCustomerSatisfactionValuesForTable', formatForTable(graphs, includeCsTargetGraphs))

        modelToRender.put('labelSummary', chart.osmChartGraphsCommonLabel);

        modelToRender.put('markerShouldBeEnabled', true)
        modelToRender.put('labelShouldBeEnabled', false)
    }

    /**
     * Fixes the specified time frame to fit interval range.
     *
     * @return The fixed time frame, never <code>null</code>.
     */
    private Interval fixTimeFrame(Interval timeFrameToFix, int intervalRangeInMinutes) {
        return csiAggregationUtilService.fixTimeFrameToMatchIntervalRange(timeFrameToFix, intervalRangeInMinutes)
    }

    /**
     * This method moves every Point in the Highchart Graph one week forward.
     * @param graphs
     * @return
     * @since IT-217
     */
    private List<OsmChartGraph> moveDataPointsOneWeekForward(List<OsmChartGraph> graphs) {

        List<OsmChartGraph> movedGraphs = []

        graphs.each { OsmChartGraph graph ->

            List<OsmChartPoint> oldList = graph.getPoints()
            graph.setPoints([])

            oldList.each { OsmChartPoint point ->

                DateTime time = new DateTime(point.time)
                time = time.plusWeeks(1)

                OsmChartPoint movedPoint = new OsmChartPoint(time: time.toDate().getTime(), csiAggregation: point.csiAggregation, countOfAggregatedResults: point.countOfAggregatedResults, sourceURL: point.sourceURL, testingAgent: point.testingAgent)
                if (movedPoint.isValid())
                    graph.getPoints().add(movedPoint)
            }
        }

        return graphs
    }

    /**
     * The record separator of a CSV as described in Informational RFC 4180.
     *
     * @see <a href="http://tools.ietf.org/html/rfc4180#section-2">http://tools.ietf.org/html/rfc4180#section-2</a> (2).
     */
    @Deprecated
    private static final String CRLF = String.valueOf((char) 13) + String.valueOf((char) 10)

    /**
     * The {@link DateTimeFormat} used for CSV export and table view.
     */
    private static final DateTimeFormatter CSV_TABLE_DATE_TIME_FORMAT = DateTimeFormat.forPattern("dd.MM.yyyy HH:mm:ss")

    //	/**
    //	 * The {@link NumberFormat} used to format CSI values for CSV export and table view.
    //	 */
    //	private static final NumberFormat CSV_TABLE_CSI_VALUE_FORMAT_GERMAN = NumberFormat.getNumberInstance(Locale.GERMAN);
    //
    //	/**
    //	 * The {@link NumberFormat} used to format CSI values for CSV export and table view.
    //	 */
    //	private static final NumberFormat CSV_TABLE_CSI_VALUE_FORMAT_DEFAULT = NumberFormat.getNumberInstance(Locale.US);

    /**
     * <p>
     * Creates a CSV based on the selection passed as {@link CsiDashboardShowAllCommand}.
     * </p>
     *
     * @param cmd
     *         The command with the users selections;
     *         not <code>null</code>.
     * @return nothing , immediately renders a CSV to response' output stream.
     * @see <a href="http://tools.ietf.org/html/rfc4180">http://tools.ietf.org/html/rfc4180</a>
     */
    public Map<String, Object> csiValuesCsv(CsiDashboardShowAllCommand cmd) {
        Map<String, Object> modelToRender = new HashMap<String, Object>()

        if (request.queryString && cmd.validate()) {
            fillWithAproximateCsiAggregationData(modelToRender, cmd, false, CsiType.getCsiTypes(cmd))
            cmd.copyRequestDataToViewModelMap(modelToRender)
        } else {
            redirectWith303('showAll', params)
            return
        }

        String filename = modelToRender['aggrGroupAndInterval'] + '_' + modelToRender['fromFormatted'] + '_to_' + modelToRender['toFormatted'] + '.csv'

        response.setHeader('Content-disposition', 'attachment; filename=' + filename)
        response.setContentType("text/csv;header=present;charset=UTF-8")

        Writer responseWriter = new OutputStreamWriter(response.getOutputStream())

        List<OsmChartGraph> csiValues = modelToRender['wptCustomerSatisfactionValues']
        writeCSV(csiValues, responseWriter, RequestContextUtils.getLocale(request), false)

        response.getOutputStream().flush()
        return null
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
     * @return nothing , immediately sends HTTP response codes to client.
     */
    def validateAndSaveDashboardValues(String values) {
        JSONObject dashboardValues = JSON.parse(values)

        String dashboardName = dashboardValues.dashboardName
        String username = springSecurityService.authentication.principal.getUsername()
        Boolean publiclyVisible = dashboardValues.publiclyVisible as Boolean
        String wideScreenDiagramMontage = dashboardValues.wideScreenDiagramMontage

        // Parse data for command
        Date fromDate = SIMPLE_DATE_FORMAT.parse(dashboardValues.from)
        Date toDate = SIMPLE_DATE_FORMAT.parse(dashboardValues.to)
        Collection<Long> selectedFolder = customDashboardService.getValuesFromJSON(dashboardValues, "selectedFolder")
        Collection<Long> selectedPages = customDashboardService.getValuesFromJSON(dashboardValues, "selectedPages")
        Collection<Long> selectedMeasuredEventIds = customDashboardService.getValuesFromJSON(dashboardValues, "selectedMeasuredEventIds")
        Collection<Long> selectedBrowsers = customDashboardService.getValuesFromJSON(dashboardValues, "selectedBrowsers")
        Collection<Long> selectedLocations = customDashboardService.getValuesFromJSON(dashboardValues, "selectedLocations")
        Collection<Long> selectedCsiSystems = customDashboardService.getValuesFromJSON(dashboardValues, "selectedCsiSystems")
        int timeFrameInterval = Integer.parseInt(dashboardValues.selectedTimeFrameInterval)
        if ( !dashboardValues.graphAliases.isEmpty()) {
            dashboardValues.graphAliases.each{
                if (it.key == i18nService.msgInLocale('de.iteratec.isocsi.targetcsi.label',Locale.GERMAN)){
                    dashboardValues.graphAliases[i18nService.msgInLocale('de.iteratec.isocsi.targetcsi.label',Locale.ENGLISH)] = it.value
                }
                if (it.key == i18nService.msgInLocale('de.iteratec.isocsi.targetcsi.label',Locale.ENGLISH)){
                    dashboardValues.graphAliases[i18nService.msgInLocale('de.iteratec.isocsi.targetcsi.label',Locale.GERMAN)] = it.value
                }
            }
        }
        // Create command for validation
        CsiDashboardShowAllCommand cmd = new CsiDashboardShowAllCommand(from: fromDate, to: toDate, fromHour: dashboardValues.fromHour,
                toHour: dashboardValues.toHour,  aggrGroupAndInterval: dashboardValues.aggrGroupAndInterval, selectedFolder: selectedFolder,
                selectedPages: selectedPages, selectedMeasuredEventIds: selectedMeasuredEventIds, selectedAllMeasuredEvents: dashboardValues.selectedAllMeasuredEvents,
                selectedBrowsers: selectedBrowsers, selectedAllBrowsers: dashboardValues.selectedAllBrowsers, selectedLocations: selectedLocations, selectedCsiSystems: selectedCsiSystems,
                selectedAllLocations: dashboardValues.selectedAllLocations, debug: dashboardValues.debug, selectedTimeFrameInterval: timeFrameInterval,
                includeInterval: dashboardValues.includeInterval, setFromHour: dashboardValues.setFromHour, setToHour: dashboardValues.setToHour,
                chartTitle: dashboardValues.chartTitle ?: "", loadTimeMaximum: dashboardValues.loadTimeMaximum ?: "auto",
                showDataLabels: dashboardValues.showDataLabels, showDataMarkers: dashboardValues.showDataMarkers,
                csiTypeDocComplete: dashboardValues.csiTypeDocComplete, csiTypeVisuallyComplete: dashboardValues.csiTypeVisuallyComplete,
                graphNameAliases: dashboardValues.graphAliases, graphColors: dashboardValues.graphColors)

        if (dashboardValues.loadTimeMinimum) cmd.loadTimeMinimum = dashboardValues.loadTimeMinimum.toInteger()
        if (dashboardValues.chartHeight) cmd.chartHeight = dashboardValues.chartHeight.toInteger()
        if (dashboardValues.chartHeight) cmd.chartHeight = dashboardValues.chartHeight.toInteger()
        if (dashboardValues.chartWidth) cmd.chartWidth = dashboardValues.chartWidth.toInteger()

        if (!cmd.validate()) {
            //send errors
            def errMsgList = cmd.errors.allErrors.collect { g.message([error: it]) }
            response.sendError(400, "beginErrorMessage" + errMsgList.toString() + "endErrorMessage")
            // Apache Tomcat will output the response as part of (HTML) error page
            return null
        } else {
            // Remove old if existing
            UserspecificCsiDashboard existing = UserspecificCsiDashboard.findByDashboardName(dashboardName)
            if (existing) {
                existing.delete(flush: true, failOnError: true)
            }

            UserspecificCsiDashboard newCustomDashboard = new UserspecificCsiDashboard(cmd, publiclyVisible, wideScreenDiagramMontage, dashboardName, username)

            if (!newCustomDashboard.save(failOnError: true, flush: true)) {
                response.sendError(500, 'save error')
                return null
            } else {
                response.sendError(200, 'OK')
                return null
            }
        }
    }

    /**
     * <p>
     * Converts the specified CSI values in the source map to a CSV
     * corresponding to RFC 4180 written to specified {@link Writer}.
     * </p>
     *
     * @param source
     *         The CSI values a List of OsmChartGraph, not <code>null</code>.
     * @param target
     *         The {@link Writer} to write CSV to,
     *         not <code>null</code>.
     * @param localeForNumberFormat
     *         The locale used to format the numeric values,
     *         not <code>null</code>.
     * @param repeatCSITargetValueColumns
     *         Should the target CSI values be repeated? For UI table this is
     *         required due readability, for a download CSV this is overhead;
     *         <code>true</code> if should be repeated, <code>false</code>
     *         else.
     *
     * @throws IOException if write on {@code target} failed.
     */
    private
    static void writeCSV(List<OsmChartGraph> source, Writer target, Locale localeForNumberFormat, boolean repeatCSITargetValueColumns) throws IOException {
        NumberFormat csvCSIValueFormat = NumberFormat.getNumberInstance(localeForNumberFormat)

        // Sort graph points by time
        TreeMapOfTreeMaps<Long, String, OsmChartPoint> pointsByGraphByTime = new TreeMapOfTreeMaps<Long, String, OsmChartPoint>()
        for (OsmChartGraph eachCSIValueEntry : source) {
            for (OsmChartPoint eachPoint : eachCSIValueEntry.getPoints()) {
                pointsByGraphByTime.getOrCreate(eachPoint.time).put(eachCSIValueEntry.getLabel(), eachPoint)
            }
        }

        CsvListWriter csvWriter = new CsvListWriter(
                target,
                new CsvPreference.Builder(CsvPreference.EXCEL_NORTH_EUROPE_PREFERENCE).useEncoder(new DefaultCsvEncoder()).build()
        )

        // Create CSV header:
        List<String> csvHeader = new LinkedList<String>()
        csvHeader.add('Zeitpunkt') // TODO i18n?
        csvHeader.add('Ziel-CSI') // TODO i18n?

        List<String> graphLabelsInOrderOfHeader = new LinkedList<String>()

        for (OsmChartGraph eachGraph : source) {
            csvHeader.add(eachGraph.getLabel())
            if (repeatCSITargetValueColumns) {
                csvHeader.add('Ziel-CSI') // TODO i18n?
            }
            csvHeader.add('Delta') // TODO i18n?

            graphLabelsInOrderOfHeader.add(eachGraph.getLabel())
        }

        csvWriter.writeHeader(csvHeader.toArray(new String[csvHeader.size()]))

        // The target graph:
        CsTargetGraph targetGraph = CsTargetGraph.list().get(0) // TODO Move DB access to DAO.

        for (Map.Entry<Long, TreeMap<String, OsmChartPoint>> eachPointByGraphOfTime : pointsByGraphByTime) {
            List<String> row = new LinkedList<String>()

            DateTime time = new DateTime(eachPointByGraphOfTime.getKey())
            double targetValue = targetGraph.getPercentOfDate(time)

            row.add(CSV_TABLE_DATE_TIME_FORMAT.print(time))

            row.add(csvCSIValueFormat.format(targetValue))

            for (String eachGraphLabel : graphLabelsInOrderOfHeader) {
                OsmChartPoint point = eachPointByGraphOfTime.getValue().get(eachGraphLabel)
                if (point != null) {
                    row.add(csvCSIValueFormat.format(roundDouble(point.csiAggregation)))
                    if (repeatCSITargetValueColumns) {
                        row.add(csvCSIValueFormat.format(roundDouble(targetValue)))
                    }
                    row.add(csvCSIValueFormat.format(roundDouble(point.csiAggregation - targetValue)))
                } else {
                    row.add("")
                    if (repeatCSITargetValueColumns) {
                        row.add("")
                    }
                    row.add("")
                }
            }

            csvWriter.writeRow(row)
        }

        csvWriter.flush()
    }

    /**
     * Rounds a double according to CSV and Table requirements.
     *
     * @param valueToRound
     *         The double value to round.
     * @return The rounded value.
     * @since IT-102
     */
    static private double roundDouble(double valueToRound) {
        return new BigDecimal(valueToRound).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue()
    }

    /**
     * <p>
     * Constructs the static view data of the {@link #showAll(CsiDashboardShowAllCommand)}
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
        //		List<AggregatorType> allAggregatorTypes = aggregatorTypeDaoService.findAll().sort(false, { it.name });
        //		result.put('aggrGroupLabels', allAggregatorTypes.collect( { it.name } ))
        //		result.put('aggrGroupValues', allAggregatorTypes.collect( { String.valueOf( it.id) } ))
        result.put('aggrGroupLabels', AGGREGATOR_GROUP_LABELS)
        result.put('aggrGroupValues', AGGREGATOR_GROUP_VALUES)

        // CSI-JobGroups
        result.put('folders', jobGroupDaoService.findCSIGroups().sort(false, { it.name }))

        // Pages
        List<Page> pages = pageDaoService.findAll().sort(false, { it.name })
        result.put('pages', pages)

        // MeasuredEvents
        List<MeasuredEvent> measuredEvents = measuredEventDaoService.findAll().sort(false, { it.name })
        result.put('measuredEvents', measuredEvents)

        // Browsers
        List<Browser> browsers = browserDaoService.findAll().sort(false, { it.name })
        result.put('browsers', browsers)

        // Locations
        List<Location> locations = locationDaoService.findAll().
                sort(false, { Location it -> it.getWptServer().label }).
                sort(false, { Location it -> it.getBrowser().name }).
                sort(false, { Location it -> it.location })
        result.put('locations', locations)

        // ConnectivityProfiles
        result['connectivityProfiles'] = connectivityProfileDaoService.findAll().sort(false, { it.name.toLowerCase() });

        // CsiSystems
        List<CsiSystem> csiSystems = CsiSystem.findAll().sort(false, { it.label })
        result.put('csiSystems', csiSystems)

        // JavaScript-Utility-Stuff:
        result.put("dateFormatString", DATE_FORMAT_STRING_FOR_HIGH_CHART)
        result.put("weekStart", MONDAY_WEEKSTART)

        // --- Map<PageID, Set<MeasuredEventID>> for fast view filtering:
        Map<Long, Set<Long>> eventsOfPages = new HashMap<Long, Set<Long>>()
        for (Page eachPage : pages) {
            Set<Long> eventIds = new HashSet<Long>()

            Collection<Long> ids = measuredEvents.findResults {
                it.testedPage.getId() == eachPage.getId() ? it.getId() : null
            }
            if (!ids.isEmpty()) {
                eventIds.addAll(ids)
            }

            eventsOfPages.put(eachPage.getId(), eventIds)
        }
        result.put('eventsOfPages', eventsOfPages)

        // --- Map<BrowserID, Set<LocationID>> for fast view filtering:
        Map<Long, Set<Long>> locationsOfBrowsers = new HashMap<Long, Set<Long>>()
        for (Browser eachBrowser : browsers) {
            Set<Long> locationIds = new HashSet<Long>()

            Collection<Long> ids = locations.findResults {
                it.browser.getId() == eachBrowser.getId() ? it.getId() : null
            }
            if (!ids.isEmpty()) {
                locationIds.addAll(ids)
            }

            locationsOfBrowsers.put(eachBrowser.getId(), locationIds)
        }
        result.put('locationsOfBrowsers', locationsOfBrowsers)
        result.put('defaultChartTitle', csiHelperService.getCsiChartDefaultTitle())
        result.put("tagToJobGroupNameMap", jobGroupDaoService.getTagToJobGroupNameMap())

        // Done! :)
        return result
    }

    /**
     * Checks if hours between given fromDate and toDate is greater than 4 months.
     *
     *
     * @param fromDate TODO Doc: Inclusive? Eclusive?
     * @param toDate TODO Doc: Inclusive? Eclusive?
     * @return TODO Doc
     * @deprecated TODO Currently unused -> Discuss if this range check is required or just should be done in UI.
     */
    @Deprecated
    private boolean exceedsTimeframeBoundary(Date fromDate, Date toDate, CsiAggregationInterval interval) {
        Days daysBetween = Days.daysBetween(new DateTime(fromDate), new DateTime(toDate))
        Integer maxDays
        switch (interval.intervalInMinutes) {
            case CsiAggregationInterval.WEEKLY:
                maxDays = 26 * 7
                break
            case CsiAggregationInterval.DAILY:
                maxDays = 6 * 7
                break
            default:
                maxDays = 2 * 7
                break
        }
        return daysBetween.isGreaterThan(new Days(maxDays))
    }

    /**
     * Creates the CSV as String to be converted to HTML table by view. Thrown Exceptions get catched and an error-message is returned instead of csv-representation.
     * @return String-representation of data or an error-message if any {@link Exception} is thrown.
     */
    private String formatForTable(List<OsmChartGraph> csiValueMap, boolean includeCsTargetGraphs) {
        String csvAsString
        try {
            csvAsString = tryToFormatForTable(csiValueMap, includeCsTargetGraphs)
        } catch (Exception e) {
            CsiDashboardController.log.error('An error occurred while creating csv-representation of measurement-data', e)
            flash.tableDataError = i18nService.msg('de.iteratec.ism.measurement.conversion.errormessage', 'Bei der Umwandlung der Messdaten f&uuml;r die tabellarischen Darstellung ist ein Fehler aufgetreten!')
        }
        return csvAsString
    }

    private String tryToFormatForTable(List<OsmChartGraph> csiValueMap, boolean includeCsTargetGraphs) {
        StringWriter writer = new StringWriter()
        writeCSV(csiValueMap, writer, RequestContextUtils.getLocale(request), true)
        String csvAsString = writer.toString()
        if (csvAsString.endsWith('\n')) {
            csvAsString = csvAsString.substring(0, csvAsString.length() - 1)
        }
        return csvAsString
    }

    public def checkDashboardNameUnique(String values) {
        JSONObject dashboardValues = JSON.parse(values)
        String dashboardName = dashboardValues.dashboardName

        def answer
        if (UserspecificCsiDashboard.findByDashboardName(dashboardName)) {
            answer = [result: 'false']
        } else {
            answer = [result: 'true']
        }

        render answer as JSON
    }
}

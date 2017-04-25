package de.iteratec.osm.result

import de.iteratec.osm.measurement.schedule.ConnectivityProfile
import de.iteratec.osm.report.chart.CsiAggregationInterval
import de.iteratec.osm.util.ParameterBindingUtility
import grails.converters.JSON
import grails.validation.Validateable
import org.grails.databinding.BindUsing
import org.joda.time.DateTime
import org.joda.time.Duration
import org.joda.time.Interval
/**
 * <p>
 * Command of {@link EventResultDashboardController#showAll(EventResultDashboardShowAllCommand)
 *}.
 * </p>
 *
 * <p>
 * None of the properties will be <code>null</code> for a valid instance.
 * Some collections might be empty depending on the {@link #aggrGroup}
 * used.
 * </p>
 *
 * @author mze , rhe
 * @since IT-6
 */
public class EventResultDashboardShowAllCommand implements Validateable {


    public final static Integer LINE_CHART_SELECTION = 0;
    public final static Integer POINT_CHART_SELECTION = 1;
    public final static Integer EXPECTED_RESULTS_PER_DAY = 50;
    /**
     * The selected start date.
     *
     * Please use {@link #createTimeFrameInterval()}.
     */
    @BindUsing({obj, source ->
        ParameterBindingUtility.parseDateTimeParameter(source["from"], false)
    })
    DateTime from

    /**
     * The selected end date.
     *
     * Please use {@link #createTimeFrameInterval()}.
     */
    @BindUsing({obj, source ->
        ParameterBindingUtility.parseDateTimeParameter(source["to"], true)
    })
    DateTime to

    /**
     * The name of the {@link de.iteratec.osm.report.chart.AggregatorType}.
     *
     * @deprecated Currently unused! TODO Check for usages, if not required: remove!
     */
    @Deprecated
    String aggrGroup

    /**
     * The time of the {@link CsiAggregationInterval}.
     */
    Integer selectedInterval

    /**
     * A predefined time frame.
     */
    int selectedTimeFrameInterval = 259200

    /**
     * The Selected chart type (line or point)
     */
    Integer selectChartType;

    /**
     * The database IDs of the selected {@linkplain de.iteratec.osm.measurement.schedule.JobGroup CSI groups}
     * which are the systems measured for a CSI value
     */
    Collection<Long> selectedFolder = []

    /**
     * The database IDs of the selected {@linkplain de.iteratec.osm.csi.Page pages}
     * which results to be shown.
     */
    Collection<Long> selectedPages = []

    /**
     * The database IDs of the selected {@linkplain de.iteratec.osm.result.MeasuredEvent
     * measured events} which results to be shown.
     *
     * These selections are only relevant if
     * {@link #selectedAllMeasuredEvents} is evaluated to
     * <code>false</code>.
     */
    Collection<Long> selectedMeasuredEventIds = []

    /**
     * User enforced the selection of all measured events.
     * This selection <em>is not</em> reflected in
     * {@link #selectedMeasuredEventIds} cause of URL length
     * restrictions. If this flag is evaluated to
     * <code>true</code>, the selections in
     * {@link #selectedMeasuredEventIds} should be ignored.
     */
    Boolean selectedAllMeasuredEvents = true

    /**
     * The database IDs of the selected {@linkplain de.iteratec.osm.measurement.environment.Browser
     * browsers} which results to be shown.
     *
     * These selections are only relevant if
     * {@link #selectedAllBrowsers} is evaluated to
     * <code>false</code>.
     */
    Collection<Long> selectedBrowsers = []

    /**
     * User enforced the selection of all browsers.
     * This selection <em>is not</em> reflected in
     * {@link #selectedBrowsers} cause of URL length
     * restrictions. If this flag is evaluated to
     * <code>true</code>, the selections in
     * {@link #selectedBrowsers} should be ignored.
     */
    Boolean selectedAllBrowsers = true

    /**
     * The database IDs of the selected {@linkplain de.iteratec.osm.measurement.environment.Location
     * locations} which results to be shown.
     *
     * These selections are only relevant if
     * {@link #selectedAllLocations} is evaluated to
     * <code>false</code>.
     */
    Collection<Long> selectedLocations = []

    /**
     * User enforced the selection of all locations.
     * This selection <em>is not</em> reflected in
     * {@link #selectedLocations} cause of URL length
     * restrictions. If this flag is evaluated to
     * <code>true</code>, the selections in
     * {@link #selectedLocations} should be ignored.
     */
    Boolean selectedAllLocations = true

    /**
     * The selected connectivities. Could include connectivityProfile ids, customConnectivityNames or 'native'
     */
    Collection<String> selectedConnectivities = []

    /**
     * User enforced the selection of all ConnectivityProfiles.
     * This selection <em>is not</em> reflected in
     * {@link #selectedConnectivities} cause of URL length
     * restrictions. If this flag is evaluated to
     * <code>true</code>, the selections in
     * {@link #selectedConnectivities} should be ignored.
     */
    Boolean selectedAllConnectivityProfiles = true

    /**
     * Database name of the selected {@link de.iteratec.osm.report.chart.AggregatorType}, selected by the user.
     * Determines wich {@link CachedView#CACHED} results should be shown.
     */
    Collection<String> selectedAggrGroupValuesCached = []

    /**
     * Database name of the selected {@link de.iteratec.osm.report.chart.AggregatorType}, selected by the user.
     * Determines wich {@link CachedView#UNCACHED} results should be shown.
     */
    Collection<String> selectedAggrGroupValuesUnCached = []

    /**
     * Lower bound for load-time-measurands. Values lower than this will be excluded from graphs.
     */
    Integer trimBelowLoadTimes

    /**
     * Upper bound for load-time-measurands. Values greater than this will be excluded from graphs.
     */
    Integer trimAboveLoadTimes

    /**
     * Lower bound for request-count-measurands. Values lower than this will be excluded from graphs.
     */
    Integer trimBelowRequestCounts

    /**
     * Upper bound for request-count-measurands. Values greater than this will be excluded from graphs.
     */
    Integer trimAboveRequestCounts

    /**
     * Lower bound for request-sizes-measurands. Values lower than this will be excluded from graphs.
     */
    Integer trimBelowRequestSizes

    /**
     * Upper bound for request-sizes-measurands. Values greater than this will be excluded from graphs.
     */
    Integer trimAboveRequestSizes

    /**
     * If the user has been warned about a potentially long processing
     * time, did he overwrite the waring and really want to perform
     * the request?
     *
     * A value of <code>true</code> indicates that overwrite, everything
     * should be done as requested, <code>false</code> indicates that
     * the user hasn't been warned before, so there is no overwrite.
     */
    Boolean overwriteWarningAboutLongProcessingTime;

    /**
     * Flag for manual debugging.
     * Used for debugging highcharts-export-server, e.g.
     */
    Boolean debug

    String chartTitle
    int chartWidth
    int chartHeight
    int loadTimeMinimum
    boolean wideScreenDiagramMontage = false;

    /**
     * The maximum load time could be set to 'auto', so we handle it as a string
     */
    String loadTimeMaximum = "auto"
    boolean showDataMarkers
    boolean showDataLabels

    // If map is not specified, it acts as a string/string mapping (gorm default)
    Map graphNameAliases = [:]
    Map graphColors = [:]

    /**
     * The name of a saved userspecificDashboard
     */
    String dashboardName = ""
    /**
     * Whether a saved userspecificDashboard is publicly visible or not
     */
    boolean publiclyVisible = false

    /**
     * Constraints needs to fit.
     */
    static constraints = {
        from(nullable: true, validator: { DateTime currentFrom, EventResultDashboardShowAllCommand cmd ->
            boolean manualTimeframe = cmd.selectedTimeFrameInterval == 0
            if (manualTimeframe && currentFrom == null) return ['de.iteratec.osm.gui.startAndEndDateSelection.error.from.nullWithManualSelection']
        })
        to(nullable: true, validator: { DateTime currentTo, EventResultDashboardShowAllCommand cmd ->
            boolean manualTimeframe = cmd.selectedTimeFrameInterval == 0
            if (manualTimeframe && currentTo == null) return ['de.iteratec.osm.gui.startAndEndDateSelection.error.to.nullWithManualSelection']
            else if (manualTimeframe && currentTo != null && cmd.from != null && !currentTo.isAfter(cmd.from)) return ['de.iteratec.osm.gui.startAndEndDateSelection.error.to.beforeFromDate']
        })
        selectedAggrGroupValuesCached(nullable: false, validator: { Collection<String> selectedCheckedAggregators, EventResultDashboardShowAllCommand cmd ->
            if (cmd.selectedAggrGroupValuesCached.size() < 1 && cmd.selectedAggrGroupValuesUnCached.size() < 1) return ['de.iteratec.osm.gui.selectedAggrGroupValuesCached.error.validator.error.selectedAggrGroupValuesCached']
        })
        selectedAllMeasuredEvents(nullable: true)
        selectedAllBrowsers(nullable: true)
        selectedAllLocations(nullable: true)

        selectedFolder(nullable: false, validator: { Collection currentCollection, EventResultDashboardShowAllCommand cmd ->
            if (currentCollection.isEmpty()) return ['de.iteratec.osm.gui.selectedFolder.error.validator.error.selectedFolder']
        })
        selectedPages(nullable: true)
        selectedBrowsers(nullable: false, validator: { Collection currentCollection, EventResultDashboardShowAllCommand cmd ->
            if (!cmd.selectedAllBrowsers && currentCollection.isEmpty()) return ['de.iteratec.osm.gui.selectedBrowsers.error.validator.error.selectedBrowsers']
        })
        selectedMeasuredEventIds(nullable: false, validator: { Collection currentCollection, EventResultDashboardShowAllCommand cmd ->
            if (!cmd.selectedAllMeasuredEvents && currentCollection.isEmpty()) return ['de.iteratec.osm.gui.selectMeasurings.error.selectedMeasuredEvents.validator.error.selectedMeasuredEvents']
        })
        selectedLocations(nullable: false, validator: { Collection currentCollection, EventResultDashboardShowAllCommand cmd ->
            if (!cmd.selectedAllLocations && currentCollection.isEmpty()) return ['de.iteratec.osm.gui.selectedConnectivities.error.validator.error.selectedConnectivites']
        })
        selectedAllConnectivityProfiles(nullable: true)

        overwriteWarningAboutLongProcessingTime(nullable: true)

        chartTitle(nullable: true)
        loadTimeMaximum(nullable: true)
        trimAboveLoadTimes(nullable: true)
        trimAboveRequestCounts(nullable: true)
        trimAboveRequestSizes(nullable: true)
        trimBelowLoadTimes(nullable: true)
        trimBelowRequestCounts(nullable: true)
        trimBelowRequestSizes(nullable: true)
        debug(nullable: true)
        selectChartType(nullable: true)
        aggrGroup(nullable: true)
        selectedConnectivities(validator: { Collection currentCollection, EventResultDashboardShowAllCommand cmd ->
            if (currentCollection.size() <= 0 && !cmd.selectedAllConnectivityProfiles) return ['de.iteratec.osm.gui.selectedConnectivities.error.validator.error.selectedConnectivites']
        })
    }

    /**
     * returns the selected connectivityProfiles by filtering all selected connectivities.
     */
    Collection<Long> getSelectedConnectivityProfiles() {
        return selectedConnectivities.findAll { it.isLong() && ConnectivityProfile.exists(it as Long) }.collect {
            Long.parseLong(it)
        }
    }

    /**
     * returns the selected customConnectivityNames by filtering all selected connectivities.
     */
    Collection<String> getSelectedCustomConnectivityNames() {
        return selectedConnectivities.findAll {
            (!it.isLong() && it != ResultSelectionController.MetaConnectivityProfileId.Native.value) || (it.isLong() && !ConnectivityProfile.exists(it as Long))
        }
    }

    /**
     * <p>
     * Returns the selected time frame as {@link org.joda.time.Interval}.
     * That is the interval from {@link #from} to {@link #to} if {@link #selectedTimeFrameInterval} is 0 (that means manual).
     * If {@link #selectedTimeFrameInterval} is greater 0 the returned time frame is now minus {@link #selectedTimeFrameInterval} seconds to now.
     * </p>
     *
     * @return not <code>null</code>.
     */
    Interval createTimeFrameInterval() {
        if (this.selectedTimeFrameInterval == 0) {
            return new Interval(this.from, this.to)
        } else {
            DateTime now = DateTime.now()
            return new Interval(now.minusSeconds(this.selectedTimeFrameInterval), now)
        }
    }

    /**
     * Whether or not EventResults measured with native connectivity should get included.
     */
    boolean getIncludeNativeConnectivity() {
        return selectedAllConnectivityProfiles || selectedConnectivities.contains(ResultSelectionController.MetaConnectivityProfileId.Native.value)
    }

    /**
     * <p>
     * Copies all request data to the specified map. This operation does
     * not care about the validation status of this instance.
     * For missing values the defaults are inserted.
     * </p>
     *
     * @param viewModelToCopyTo
     *         The {@link Map} the request data contained in this command
     *         object should be copied to. The map must be modifiable.
     *         Previously contained data will be overwritten.
     *         The argument might not be <code>null</code>.
     */
    public void copyRequestDataToViewModelMap(Map<String, Object> viewModelToCopyTo) {

        viewModelToCopyTo.put('selectedTimeFrameInterval', this.selectedTimeFrameInterval)
        viewModelToCopyTo.put('selectedInterval', this.selectedInterval ?: CsiAggregationInterval.RAW)

        viewModelToCopyTo.put('selectedFolder', this.selectedFolder)
        viewModelToCopyTo.put('selectedPages', this.selectedPages)

        viewModelToCopyTo.put('selectedAllMeasuredEvents', this.selectedAllMeasuredEvents)
        viewModelToCopyTo.put('selectedMeasuredEventIds', this.selectedMeasuredEventIds)

        viewModelToCopyTo.put('selectedAllBrowsers', this.selectedAllBrowsers)
        viewModelToCopyTo.put('selectedBrowsers', this.selectedBrowsers)

        viewModelToCopyTo.put('selectedAllLocations', this.selectedAllLocations)
        viewModelToCopyTo.put('selectedLocations', this.selectedLocations)

        viewModelToCopyTo.put('selectedAllConnectivityProfiles', this.selectedAllConnectivityProfiles)
        viewModelToCopyTo.put('selectedConnectivities', this.selectedConnectivities)

        viewModelToCopyTo.put('from', ISO_DATE_TIME_FORMATTER.print(this.from))
        viewModelToCopyTo.put('to', ISO_DATE_TIME_FORMATTER.print(this.to))

        viewModelToCopyTo.put("selectedChartType", this.selectChartType ? POINT_CHART_SELECTION : LINE_CHART_SELECTION);

        viewModelToCopyTo.put('selectedAggrGroupValuesCached', this.selectedAggrGroupValuesCached)
        viewModelToCopyTo.put('selectedAggrGroupValuesUnCached', this.selectedAggrGroupValuesUnCached)

        viewModelToCopyTo.put('trimBelowLoadTimes', this.trimBelowLoadTimes)
        viewModelToCopyTo.put('trimAboveLoadTimes', this.trimAboveLoadTimes)
        viewModelToCopyTo.put('trimBelowRequestCounts', this.trimBelowRequestCounts)
        viewModelToCopyTo.put('trimAboveRequestCounts', this.trimAboveRequestCounts)
        viewModelToCopyTo.put('trimBelowRequestSizes', this.trimBelowRequestSizes)
        viewModelToCopyTo.put('trimAboveRequestSizes', this.trimAboveRequestSizes)
        viewModelToCopyTo.put('debug', this.debug ?: false)
        viewModelToCopyTo.put('chartTitle', this.chartTitle)
        viewModelToCopyTo.put('chartWidth', this.chartWidth)
        viewModelToCopyTo.put('chartHeight', this.chartHeight)
        viewModelToCopyTo.put('showDataLabels', this.showDataLabels)
        viewModelToCopyTo.put('showDataMarkers', this.showDataMarkers)
        viewModelToCopyTo.put('loadTimeMaximum', this.loadTimeMaximum)
        viewModelToCopyTo.put('loadTimeMinimum', this.loadTimeMinimum)
        viewModelToCopyTo.put('graphNameAliases', this.graphNameAliases as JSON)
        viewModelToCopyTo.put('graphColors', this.graphColors as JSON)
        viewModelToCopyTo.put('publiclyVisible', this.publiclyVisible)
        viewModelToCopyTo.put('dashboardName', this.dashboardName)
        viewModelToCopyTo.put('wideScreenDiagramMontage', this.wideScreenDiagramMontage)
    }

    /**
     * <p>
     * Creates {@link MvQueryParams} based on this command. This command
     * need to be valid for this operation to be successful.
     * </p>
     *
     * @return not <code>null</code>.
     * @throws IllegalStateException
     *         if called on an invalid instance.
     */
    public MvQueryParams createMvQueryParams() throws IllegalStateException {
        if (!this.validate()) {
            throw new IllegalStateException('Query params are not available from an invalid command.')
        }

        ErQueryParams result = new ErQueryParams();

        result.jobGroupIds.addAll(this.selectedFolder);

        if (!this.selectedAllMeasuredEvents) {
            result.measuredEventIds.addAll(this.selectedMeasuredEventIds);
        }

        result.pageIds.addAll(this.selectedPages);

        if (!this.selectedAllBrowsers) {
            result.browserIds.addAll(this.selectedBrowsers);
        }

        if (!this.selectedAllLocations) {
            result.locationIds.addAll(this.selectedLocations);
        }
        if (this.trimBelowLoadTimes) {
            result.minLoadTimeInMillisecs = this.trimBelowLoadTimes
        }
        if (this.trimAboveLoadTimes) {
            result.maxLoadTimeInMillisecs = this.trimAboveLoadTimes
        }
        if (this.trimBelowRequestCounts) {
            result.minRequestCount = this.trimBelowRequestCounts
        }
        if (this.trimAboveRequestCounts) {
            result.maxRequestCount = this.trimAboveRequestCounts
        }
        if (this.trimBelowRequestSizes) {
            result.minRequestSizeInBytes = this.trimBelowRequestSizes * 1000
        }
        if (this.trimAboveRequestSizes) {
            result.maxRequestSizeInBytes = this.trimAboveRequestSizes * 1000
        }
        result.includeNativeConnectivity = this.getIncludeNativeConnectivity()
        result.customConnectivityNames.addAll(this.selectedCustomConnectivityNames)

        result.includeAllConnectivities = this.selectedAllConnectivityProfiles
        if (this.selectedConnectivityProfiles.size() > 0) {
            result.connectivityProfileIds.addAll(this.selectedConnectivityProfiles)
        }

        return result;
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
     *         The number of selected systems / {@link de.iteratec.osm.measurement.schedule.JobGroup}s; >= 1.
     * @param countOfSelectedPages
     *         The number of selected pages; >= 1.
     * @param countOfSelectedBrowser
     *         The number of selected browser; >= 1.
     *
     * @return <code>true</code> if the user should be warned,
     *         <code>false</code> else.
     * @since IT-157
     * TODO(sburnicki): Remove as its obsolete and unused
     */
    public boolean shouldWarnAboutLongProcessingTime(int countOfSelectedAggregatorTypes, int countOfSelectedBrowser) {

        int countOfSelectedSystems = selectedFolder.size()
        Interval timeFrame = createTimeFrameInterval()
        int interval = getSelectedInterval()
        int countOfSelectedPages = selectedPages.size()
        int minutesInTimeFrame = new Duration(timeFrame.getStart(), timeFrame.getEnd()).getStandardMinutes();

        long expectedPointsOfEachGraph;
        if (interval == CsiAggregationInterval.RAW || interval == 0 || interval == null) {
            //50 results per Day
            expectedPointsOfEachGraph = Math.round(minutesInTimeFrame / 60 / 24 * EXPECTED_RESULTS_PER_DAY);
        } else {
            expectedPointsOfEachGraph = Math.round(minutesInTimeFrame / interval);
        }

        if (expectedPointsOfEachGraph > 5000) {
            return true;
        } else {

            long expectedCountOfGraphs = countOfSelectedAggregatorTypes * countOfSelectedSystems * countOfSelectedPages * countOfSelectedBrowser;
            long expectedTotalNumberOfPoints = expectedCountOfGraphs * expectedPointsOfEachGraph;

            return expectedTotalNumberOfPoints > 50000;
        }
    }



}

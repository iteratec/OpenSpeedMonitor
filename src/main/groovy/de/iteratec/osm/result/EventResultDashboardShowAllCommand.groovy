package de.iteratec.osm.result

import de.iteratec.osm.report.chart.CsiAggregationInterval
import de.iteratec.osm.util.ParameterBindingUtility
import grails.converters.JSON
import grails.validation.Validateable
import org.grails.databinding.BindUsing
import org.joda.time.DateTime
import org.joda.time.Interval
/**
 * <p>
 * Command of {@link EventResultDashboardController#showAll(EventResultDashboardShowAllCommand)
 *}.
 * </p>
 *
 * @author mze , rhe
 * @since IT-6
 */
class EventResultDashboardShowAllCommand implements Validateable {

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
     * The time of the {@link CsiAggregationInterval}.
     */
    Integer selectedInterval

    /**
     * A predefined time frame.
     */
    int selectedTimeFrameInterval = 3 * 24 * 60 * 60

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
     * measured events} which results to be shown. If empty, all measured events are considered.
     */
    Collection<Long> selectedMeasuredEventIds = []

    /**
     * The database IDs of the selected {@linkplain de.iteratec.osm.measurement.environment.Browser
     * browsers} which results to be shown. If empty, all browsers are included
      */
    Collection<Long> selectedBrowsers = []

    /**
     * The database IDs of the selected {@linkplain de.iteratec.osm.measurement.environment.Location
     * locations} which results to be shown. If empty, all locations are considered
     */
    Collection<Long> selectedLocations = []

    /**
     * The selected connectivities. Could include connectivityProfile ids, customConnectivityNames or 'native'
     */
    Collection<String> selectedConnectivities = []

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

    String chartTitle
    int chartWidth
    int chartHeight
    int loadTimeMinimum

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

        selectedFolder(nullable: false, validator: { Collection currentCollection, EventResultDashboardShowAllCommand cmd ->
            if (currentCollection.isEmpty()) return ['de.iteratec.osm.gui.selectedFolder.error.validator.error.selectedFolder']
        })
        selectedPages(nullable: false, validator: { Collection currentCollection, EventResultDashboardShowAllCommand cmd ->
            if (currentCollection.isEmpty()) return ['de.iteratec.osm.gui.selectedFolder.error.validator.error.selectedPage']
        })
        selectedBrowsers(nullable: true)
        selectedMeasuredEventIds(nullable: true)
        selectedLocations(nullable: true)

        chartTitle(nullable: true)
        loadTimeMaximum(nullable: true)
        trimAboveLoadTimes(nullable: true)
        trimAboveRequestCounts(nullable: true)
        trimAboveRequestSizes(nullable: true)
        trimBelowLoadTimes(nullable: true)
        trimBelowRequestCounts(nullable: true)
        trimBelowRequestSizes(nullable: true)
        selectedConnectivities(nullable: true)
    }

    /**
     * returns the selected connectivityProfiles by filtering all selected connectivities.
     */
    Collection<Long> getSelectedConnectivityProfiles() {
        return selectedConnectivities.findAll { it.isLong() }.collect { Long.parseLong(it) }
    }

    /**
     * returns the selected customConnectivityNames by filtering all selected connectivities.
     */
    Collection<String> getSelectedCustomConnectivityNames() {
        return selectedConnectivities.findAll {
            !it.isLong() && it != ResultSelectionController.MetaConnectivityProfileId.Native.value
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
        return !selectedConnectivities || selectedConnectivities.contains(ResultSelectionController.MetaConnectivityProfileId.Native.value)
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

        viewModelToCopyTo.put('selectedMeasuredEventIds', this.selectedMeasuredEventIds)
        viewModelToCopyTo.put('selectedBrowsers', this.selectedBrowsers)
        viewModelToCopyTo.put('selectedLocations', this.selectedLocations)
        viewModelToCopyTo.put('selectedConnectivities', this.selectedConnectivities)

        viewModelToCopyTo.put('from', ParameterBindingUtility.ISO_DATE_TIME_FORMATTER.print(this.from))
        viewModelToCopyTo.put('to', ParameterBindingUtility.ISO_DATE_TIME_FORMATTER.print(this.to))

        viewModelToCopyTo.put('selectedAggrGroupValuesCached', this.selectedAggrGroupValuesCached)
        viewModelToCopyTo.put('selectedAggrGroupValuesUnCached', this.selectedAggrGroupValuesUnCached)

        viewModelToCopyTo.put('trimBelowLoadTimes', this.trimBelowLoadTimes)
        viewModelToCopyTo.put('trimAboveLoadTimes', this.trimAboveLoadTimes)
        viewModelToCopyTo.put('trimBelowRequestCounts', this.trimBelowRequestCounts)
        viewModelToCopyTo.put('trimAboveRequestCounts', this.trimAboveRequestCounts)
        viewModelToCopyTo.put('trimBelowRequestSizes', this.trimBelowRequestSizes)
        viewModelToCopyTo.put('trimAboveRequestSizes', this.trimAboveRequestSizes)
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
    }

    /**
     * <p>
     * Creates {@link ErQueryParams} based on this command. This command
     * need to be valid for this operation to be successful.
     * </p>
     *
     * @return not <code>null</code>.
     * @throws IllegalStateException
     *         if called on an invalid instance.
     */
    public ErQueryParams createErQueryParams() throws IllegalStateException {
        if (!this.validate()) {
            throw new IllegalStateException('Query params are not available from an invalid command.')
        }

        ErQueryParams result = new ErQueryParams();

        result.jobGroupIds.addAll(this.selectedFolder);

        if (this.selectedMeasuredEventIds) {
            result.measuredEventIds.addAll(this.selectedMeasuredEventIds)
        }

        result.pageIds.addAll(this.selectedPages)

        if (this.selectedBrowsers) {
            result.browserIds.addAll(this.selectedBrowsers);
        }

        if (this.selectedLocations) {
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
        result.includeAllConnectivities = !this.selectedConnectivities
        result.connectivityProfileIds.addAll(this.selectedConnectivityProfiles)

        return result
    }
}

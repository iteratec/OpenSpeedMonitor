package de.iteratec.osm.result

import de.iteratec.osm.report.chart.CsiAggregationInterval

/**
 * <p>
 * Command of {@link EventResultDashboardController#showAll(EventResultDashboardShowAllCommand)
 *}.
 * </p>
 *
 * @author mze , rhe
 * @since IT-6
 */
class EventResultDashboardShowAllCommand extends TimeSeriesShowCommandBase {
    /**
     * Aggregation interval
     */
    Integer selectedInterval

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
     * Constraints needs to fit.
     */
    static constraints = {
        importFrom(TimeSeriesShowCommandBase)
        selectedAggrGroupValuesCached(nullable: false, validator: { Collection<String> selectedCheckedAggregators, EventResultDashboardShowAllCommand cmd ->
            if (cmd.selectedAggrGroupValuesCached.size() < 1 && cmd.selectedAggrGroupValuesUnCached.size() < 1) return ['de.iteratec.osm.gui.selectedAggrGroupValuesCached.error.validator.error.selectedAggrGroupValuesCached']
        })
        trimAboveLoadTimes(nullable: true)
        trimAboveRequestCounts(nullable: true)
        trimAboveRequestSizes(nullable: true)
        trimBelowLoadTimes(nullable: true)
        trimBelowRequestCounts(nullable: true)
        trimBelowRequestSizes(nullable: true)
    }

    @Override
    void copyRequestDataToViewModelMap(Map<String, Object> viewModelToCopyTo) {
        super.copyRequestDataToViewModelMap(viewModelToCopyTo)
        viewModelToCopyTo.put('selectedInterval', this.selectedInterval ?: CsiAggregationInterval.RAW)
        viewModelToCopyTo.put('selectedAggrGroupValuesCached', this.selectedAggrGroupValuesCached)
        viewModelToCopyTo.put('selectedAggrGroupValuesUnCached', this.selectedAggrGroupValuesUnCached)

        viewModelToCopyTo.put('trimBelowLoadTimes', this.trimBelowLoadTimes)
        viewModelToCopyTo.put('trimAboveLoadTimes', this.trimAboveLoadTimes)
        viewModelToCopyTo.put('trimBelowRequestCounts', this.trimBelowRequestCounts)
        viewModelToCopyTo.put('trimAboveRequestCounts', this.trimAboveRequestCounts)
        viewModelToCopyTo.put('trimBelowRequestSizes', this.trimBelowRequestSizes)
        viewModelToCopyTo.put('trimAboveRequestSizes', this.trimAboveRequestSizes)
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
    ErQueryParams createErQueryParams() throws IllegalStateException {
        ErQueryParams queryParams = new ErQueryParams()
        fillMvQueryParams(queryParams)

        queryParams.includeNativeConnectivity = this.getIncludeNativeConnectivity()
        queryParams.customConnectivityNames.addAll(this.selectedCustomConnectivityNames)
        if (this.trimBelowLoadTimes) {
            queryParams.minLoadTimeInMillisecs = this.trimBelowLoadTimes
        }
        if (this.trimAboveLoadTimes) {
            queryParams.maxLoadTimeInMillisecs = this.trimAboveLoadTimes
        }
        if (this.trimBelowRequestCounts) {
            queryParams.minRequestCount = this.trimBelowRequestCounts
        }
        if (this.trimAboveRequestCounts) {
            queryParams.maxRequestCount = this.trimAboveRequestCounts
        }
        if (this.trimBelowRequestSizes) {
            queryParams.minRequestSizeInBytes = this.trimBelowRequestSizes * 1000
        }
        if (this.trimAboveRequestSizes) {
            queryParams.maxRequestSizeInBytes = this.trimAboveRequestSizes * 1000
        }

        return queryParams
    }

    @Override
    MvQueryParams createMvQueryParams() throws IllegalStateException {
        return createErQueryParams()
    }
}

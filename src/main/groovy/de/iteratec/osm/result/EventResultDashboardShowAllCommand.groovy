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
class EventResultDashboardShowAllCommand extends TimeSeriesDashboardCommandBase {
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
        importFrom(TimeSeriesDashboardCommandBase)
        trimAboveLoadTimes(nullable: true)
        trimAboveRequestCounts(nullable: true)
        trimAboveRequestSizes(nullable: true)
        trimBelowLoadTimes(nullable: true)
        trimBelowRequestCounts(nullable: true)
        trimBelowRequestSizes(nullable: true)
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
        ErQueryParams result = super.createErQueryParams()

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

        return result
    }
}

package de.iteratec.osm.result

import de.iteratec.osm.csi.Page
import de.iteratec.osm.util.ParameterBindingUtility
import grails.converters.JSON
import grails.validation.Validateable
import org.grails.databinding.BindUsing
import org.joda.time.DateTime
import org.joda.time.Interval

class TimeSeriesShowCommandBase implements Validateable {
    /**
     * The selected start date (inclusive).
     */
    @BindUsing({ obj, source ->
            ParameterBindingUtility.parseDateTimeParameter(source["from"], false)
    })
    DateTime from

    /**
     * The selected end date (inclusive).
     */
    @BindUsing({ obj, source ->
            ParameterBindingUtility.parseDateTimeParameter(source["to"], true)
    })
    DateTime to

    /**
     * The database IDs of the selected {@linkplain de.iteratec.osm.measurement.schedule.JobGroup CSI groups}
     * which are the systems measured for a CSI value
     *
     */
    Collection<Long> selectedFolder = []

    /**
     * The database IDs of the selected {@linkplain Page pages}
     * which results to be shown.
     */
    Collection<Long> selectedPages = []

    /**
     * The database IDs of the selected {@linkplain de.iteratec.osm.result.MeasuredEvent
     * measured events} which results to be shown.
     */
    Collection<Long> selectedMeasuredEventIds = []

    /**
     * The database IDs of the selected {@linkplain de.iteratec.osm.measurement.environment.Browser
     * browsers} which results to be shown.
     */
    Collection<Long> selectedBrowsers = []

    /**
     * The database IDs of the selected {@linkplain de.iteratec.osm.measurement.environment.Location
     * locations} which results to be shown.
     */
    Collection<Long> selectedLocations = []

    /**
     * The selected connectivities. Could include connectivityProfile ids, customConnectivityNames or 'native'
     */
    Collection<String> selectedConnectivities = []

    /**
     * A predefined time frame.
     */
    int selectedTimeFrameInterval = 3 * 24 * 60 * 60


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

    static constraints = {
        from(nullable: true, validator: { DateTime currentFrom, TimeSeriesShowCommandBase cmd ->
            boolean manualTimeframe = cmd.selectedTimeFrameInterval == 0
            if (manualTimeframe && currentFrom == null) return ['de.iteratec.osm.gui.startAndEndDateSelection.error.from.nullWithManualSelection']
        })
        to(nullable: true, validator: { DateTime currentTo, TimeSeriesShowCommandBase cmd ->
            boolean manualTimeframe = cmd.selectedTimeFrameInterval == 0
            if (manualTimeframe && currentTo == null) return ['de.iteratec.osm.gui.startAndEndDateSelection.error.to.nullWithManualSelection']
            else if (manualTimeframe && currentTo != null && cmd.from != null && !currentTo.isAfter(cmd.from)) return ['de.iteratec.osm.gui.startAndEndDateSelection.error.to.beforeFromDate']
        })

        selectedFolder(nullable: false, validator: { Collection currentCollection, TimeSeriesShowCommandBase cmd ->
            if (currentCollection.isEmpty()) return ['de.iteratec.osm.gui.selectedFolder.error.validator.error.selectedFolder']
        })
        selectedPages(nullable: true, validator: { Collection currentCollection, TimeSeriesShowCommandBase cmd ->
            if (!currentCollection && !cmd.selectedMeasuredEventIds) return ['de.iteratec.osm.gui.selectedFolder.error.validator.error.selectedPage']
        })
        selectedBrowsers(nullable: true)
        selectedMeasuredEventIds(nullable: true)
        selectedLocations(nullable: true)
        selectedConnectivities(nullable: true)
        chartTitle(nullable: true)
        loadTimeMaximum(nullable: true)
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

        viewModelToCopyTo.put('selectedTimeFrameInterval', this.selectedTimeFrameInterval)

        viewModelToCopyTo.put('selectedFolder', this.selectedFolder)
        viewModelToCopyTo.put('selectedPages', this.selectedPages)

        viewModelToCopyTo.put('selectedMeasuredEventIds', this.selectedMeasuredEventIds)
        viewModelToCopyTo.put('selectedBrowsers', this.selectedBrowsers)
        viewModelToCopyTo.put('selectedLocations', this.selectedLocations)
        viewModelToCopyTo.put('selectedConnectivities', this.selectedConnectivities)

        viewModelToCopyTo.put('from', ParameterBindingUtility.formatDateTimeParameter(this.from))
        viewModelToCopyTo.put('to', ParameterBindingUtility.formatDateTimeParameter(this.to))

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

    protected fillMvQueryParams(MvQueryParams queryParams) throws IllegalStateException {
        if (!this.validate()) {
            throw new IllegalStateException('Query params are not available from an invalid command.')
        }

        queryParams.jobGroupIds.addAll(this.selectedFolder)

        queryParams.pageIds.addAll(this.selectedPages)

        if (this.selectedMeasuredEventIds) {
            queryParams.measuredEventIds.addAll(this.selectedMeasuredEventIds)
        }

        if (this.selectedBrowsers) {
            queryParams.browserIds.addAll(this.selectedBrowsers)
        }

        if (this.selectedLocations) {
            queryParams.locationIds.addAll(this.selectedLocations)
        }

        queryParams.includeAllConnectivities = !this.selectedConnectivities
        queryParams.connectivityProfileIds.addAll(this.selectedConnectivityProfiles)

        return queryParams
    }

    /**
     * <p>
     * Create {@link MvQueryParams} based on this command. This command
     * need to be valid for this operation to be successful.
     * </p>
     *
     * @return not <code>null</code>.
     * @throws IllegalStateException
     *         if called on an invalid instance.
     */
    MvQueryParams createMvQueryParams() throws IllegalStateException {
        MvQueryParams queryParams = new MvQueryParams()
        fillMvQueryParams(queryParams)
        return queryParams
    }
}

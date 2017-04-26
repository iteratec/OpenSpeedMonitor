package de.iteratec.osm.csi

import de.iteratec.osm.measurement.schedule.ConnectivityProfile
import de.iteratec.osm.report.chart.CsiAggregationInterval
import de.iteratec.osm.report.chart.CsiAggregationUtilService
import de.iteratec.osm.result.MvQueryParams
import de.iteratec.osm.util.ParameterBindingUtility
import grails.converters.JSON
import grails.validation.Validateable
import org.grails.databinding.BindUsing
import org.joda.time.DateTime
import org.joda.time.Duration
import org.joda.time.Interval
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter

import java.text.SimpleDateFormat
import java.util.regex.Pattern

/**
 * <p>
 * Command of {@link CsiDashboardController#showAll(CsiDashboardShowAllCommand)} and
 * {@link CsiDashboardController#csiValuesCsv(CsiDashboardShowAllCommand)}.
 * </p>
 *
 * <p>
 * None of the properties will be <code>null</code> for a valid instance.
 * Some collections might be empty depending on the {@link #aggrGroup}
 * used.
 * </p>
 *
 * @author mze
 * @since IT-74
 */
class CsiDashboardShowAllCommand implements Validateable {
    CsiAggregationUtilService csiAggregationUtilService

    /**
     * The selected start date (inclusive).
     *
     * Please use {@link #receiveSelectedTimeFrame()}.
     */
    @BindUsing({ obj, source ->
        ParameterBindingUtility.parseDateTimeParameter(source["from"], false)
    })
    DateTime from

    /**
     * The selected end date (inclusive).
     *
     * Please use {@link #receiveSelectedTimeFrame()}.
     */
    @BindUsing({ obj, source ->
        ParameterBindingUtility.parseDateTimeParameter(source["to"], true)
    })
    DateTime to

    /**
     * The name of the {@link de.iteratec.osm.report.chart.AggregatorType}.
     *
     * @see de.iteratec.osm.csi.CsiDashBoardController#DAILY_AGGR_GROUP_PAGE
     * @see de.iteratec.osm.csi.CsiDashBoardController#DAILY_AGGR_GROUP_SHOP
     * @see de.iteratec.osm.csi.CsiDashBoardController#DAILY_AGGR_GROUP_SYSTEM
     * @see de.iteratec.osm.csi.CsiDashBoardController#WEEKLY_AGGR_GROUP_PAGE
     * @see de.iteratec.osm.csi.CsiDashBoardController#WEEKLY_AGGR_GROUP_SHOP
     * @see de.iteratec.osm.csi.CsiDashBoardController#WEEKLY_AGGR_GROUP_SYSTEM
     * @see de.iteratec.osm.csi.CsiDashBoardController#HOURLY_MEASURED_EVENT
     */
    String aggrGroupAndInterval

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
     *
     * These selections are only relevant if
     * {@link #selectedAllMeasuredEvents} is evaluated to
     * <code>false</code>.
     */
    Collection<Long> selectedMeasuredEventIds = []

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
     * The database IDs of the selected {@linkplain de.iteratec.osm.measurement.environment.Location
     * locations} which results to be shown.
     *
     * These selections are only relevant if
     * {@link #selectedAllLocations} is evaluated to
     * <code>false</code>.
     */
    Collection<Long> selectedLocations = []

    /**
     * The database IDs of the selected {@linkplain CsiSystem}
     * which results to be shown
     */
    Set<Long> selectedCsiSystems = []

    /**
     * If the user has been warned about a potentially long processing
     * time, did he overwrite the waring and really want to perform
     * the request?
     *
     * A value of <code>true</code> indicates that overwrite, everything
     * should be done as requested, <code>false</code> indicates that
     * the user hasn't been warned before, so there is no overwrite.
     */
    Boolean overwriteWarningAboutLongProcessingTime

    /**
     * A predefined time frame.
     */
    int selectedTimeFrameInterval = 259200

    /**
     * Whether or not current and not yet finished intervals should be loaded and displayed
     */
    Boolean includeInterval

    /**
     * The selected connectivities. Could include connectivityProfile ids, customConnectivityNames or 'native'
     */
    Collection<Long> selectedConnectivities = []

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

    boolean csiTypeVisuallyComplete
    boolean csiTypeDocComplete

    /**
     * Constraints needs to fit.
     */
    static constraints = {
        from(nullable: true, validator: { DateTime currentFrom, CsiDashboardShowAllCommand cmd ->
            boolean manualTimeframe = cmd.selectedTimeFrameInterval == 0
            if (manualTimeframe && currentFrom == null) return ['de.iteratec.osm.gui.startAndEndDateSelection.error.from.nullWithManualSelection']
        })
        to(nullable: true, validator: { DateTime currentTo, CsiDashboardShowAllCommand cmd ->
            boolean manualTimeframe = cmd.selectedTimeFrameInterval == 0
            if (manualTimeframe && currentTo == null) return ['de.iteratec.osm.gui.startAndEndDateSelection.error.to.nullWithManualSelection']
            else if (manualTimeframe && currentTo != null && cmd.from != null && !currentTo.isAfter(cmd.from)) return ['de.iteratec.osm.gui.startAndEndDateSelection.error.to.beforeFromDate']
        })

        aggrGroupAndInterval(nullable: false, inList: [CsiDashboardController.HOURLY_MEASURED_EVENT,
                                                       CsiDashboardController.WEEKLY_AGGR_GROUP_PAGE, CsiDashboardController.DAILY_AGGR_GROUP_PAGE,
                                                       CsiDashboardController.WEEKLY_AGGR_GROUP_SHOP, CsiDashboardController.DAILY_AGGR_GROUP_SHOP,
                                                       CsiDashboardController.WEEKLY_AGGR_GROUP_SYSTEM, CsiDashboardController.DAILY_AGGR_GROUP_SYSTEM])

        selectedFolder(nullable: false, validator: { Collection currentCollection, CsiDashboardShowAllCommand cmd ->
            if (currentCollection.isEmpty() &&
                    !(cmd.aggrGroupAndInterval.equals(CsiDashboardController.DAILY_AGGR_GROUP_SYSTEM) || cmd.aggrGroupAndInterval.equals(CsiDashboardController.WEEKLY_AGGR_GROUP_SYSTEM))) {
                return ['de.iteratec.isocsi.CsiDashboardController$ShowAllCommand.selectedFolder.validator.error.selectedFolder']
            }
        })

        selectedPages(nullable: false, validator: { Collection currentCollection, CsiDashboardShowAllCommand cmd ->

            boolean correctBecauseHourlyEventAndNotEmpty = cmd.aggrGroupAndInterval.equals(CsiDashboardController.HOURLY_MEASURED_EVENT) && (!currentCollection.isEmpty())
            boolean correctBecausePageAggregatorAndNotEmpty =
                    (cmd.aggrGroupAndInterval.equals(CsiDashboardController.DAILY_AGGR_GROUP_PAGE) || cmd.aggrGroupAndInterval.equals(CsiDashboardController.WEEKLY_AGGR_GROUP_PAGE)) &&
                            !currentCollection.isEmpty()
            boolean correctBecauseShop = cmd.aggrGroupAndInterval.equals(CsiDashboardController.DAILY_AGGR_GROUP_SHOP) || cmd.aggrGroupAndInterval.equals(CsiDashboardController.WEEKLY_AGGR_GROUP_SHOP)
            boolean correctBeacuseCsiSystem = cmd.aggrGroupAndInterval.equals(CsiDashboardController.DAILY_AGGR_GROUP_SYSTEM) || cmd.aggrGroupAndInterval.equals(CsiDashboardController.WEEKLY_AGGR_GROUP_SYSTEM)

            if (!(correctBecauseHourlyEventAndNotEmpty || correctBecausePageAggregatorAndNotEmpty || correctBecauseShop || correctBeacuseCsiSystem)) {
                return ['de.iteratec.isocsi.CsiDashboardController$ShowAllCommand.selectedPage.validator.error.selectedPage']
            }

        })

        selectedMeasuredEventIds(nullable: true, validator: { Collection currentCollection, CsiDashboardShowAllCommand cmd ->

            boolean correctBecauseHourlyEventAndNotEmptyOrAllEvents = cmd.aggrGroupAndInterval.equals(CsiDashboardController.HOURLY_MEASURED_EVENT) && (!currentCollection.isEmpty() || !currentCollection)
            boolean correctBecausePageAggregator = cmd.aggrGroupAndInterval.equals(CsiDashboardController.WEEKLY_AGGR_GROUP_PAGE) || cmd.aggrGroupAndInterval.equals(CsiDashboardController.DAILY_AGGR_GROUP_PAGE)
            boolean correctBecauseShopAggregator = cmd.aggrGroupAndInterval.equals(CsiDashboardController.WEEKLY_AGGR_GROUP_SHOP) || cmd.aggrGroupAndInterval.equals(CsiDashboardController.DAILY_AGGR_GROUP_SHOP)
            boolean correctBeacuseCsiSystemAggregator = cmd.aggrGroupAndInterval.equals(CsiDashboardController.WEEKLY_AGGR_GROUP_SYSTEM) || cmd.aggrGroupAndInterval.equals(CsiDashboardController.DAILY_AGGR_GROUP_SYSTEM)

            if (!(correctBecauseHourlyEventAndNotEmptyOrAllEvents || correctBecausePageAggregator || correctBecauseShopAggregator || correctBeacuseCsiSystemAggregator)) {
                return ['de.iteratec.isocsi.CsiDashboardController$ShowAllCommand.selectedMeasuredEvents.validator.error.selectedMeasuredEvents']
            }

        })

        selectedBrowsers(nullable: true, validator: { Collection currentCollection, CsiDashboardShowAllCommand cmd ->

            boolean correctBecauseHourlyEventAndNotEmptyOrAllBrowsers = cmd.aggrGroupAndInterval.equals(CsiDashboardController.HOURLY_MEASURED_EVENT) && (!currentCollection.isEmpty() || !currentCollection)
            boolean correctBecausePageAggregator = cmd.aggrGroupAndInterval.equals(CsiDashboardController.WEEKLY_AGGR_GROUP_PAGE) || cmd.aggrGroupAndInterval.equals(CsiDashboardController.DAILY_AGGR_GROUP_PAGE)
            boolean correctBecauseShopAggregator = cmd.aggrGroupAndInterval.equals(CsiDashboardController.WEEKLY_AGGR_GROUP_SHOP) || cmd.aggrGroupAndInterval.equals(CsiDashboardController.DAILY_AGGR_GROUP_SHOP)
            boolean correctBeacuseCsiSystemAggregator = cmd.aggrGroupAndInterval.equals(CsiDashboardController.WEEKLY_AGGR_GROUP_SYSTEM) || cmd.aggrGroupAndInterval.equals(CsiDashboardController.DAILY_AGGR_GROUP_SYSTEM)

            if (!(correctBecauseHourlyEventAndNotEmptyOrAllBrowsers || correctBecausePageAggregator || correctBecauseShopAggregator || correctBeacuseCsiSystemAggregator)) {
                return ['de.iteratec.isocsi.CsiDashboardController$ShowAllCommand.selectedBrowsers.validator.error.selectedBrowsers']
            }

        })

        selectedLocations(nullable: true, validator: { Collection currentCollection, CsiDashboardShowAllCommand cmd ->

            boolean correctBecauseHourlyEventAggregatorAndNotEmptyOrAllLocations = cmd.aggrGroupAndInterval.equals(CsiDashboardController.HOURLY_MEASURED_EVENT) && (!currentCollection.isEmpty() || !currentCollection)
            boolean correctBecausePageAggregator = cmd.aggrGroupAndInterval.equals(CsiDashboardController.WEEKLY_AGGR_GROUP_PAGE) || cmd.aggrGroupAndInterval.equals(CsiDashboardController.DAILY_AGGR_GROUP_PAGE)
            boolean correctBecauseShopAggregator = cmd.aggrGroupAndInterval.equals(CsiDashboardController.WEEKLY_AGGR_GROUP_SHOP) || cmd.aggrGroupAndInterval.equals(CsiDashboardController.DAILY_AGGR_GROUP_SHOP)
            boolean correctBecauseCsiSystemAggregator = cmd.aggrGroupAndInterval.equals(CsiDashboardController.WEEKLY_AGGR_GROUP_SYSTEM) || cmd.aggrGroupAndInterval.equals(CsiDashboardController.DAILY_AGGR_GROUP_SYSTEM)

            if (!(correctBecauseHourlyEventAggregatorAndNotEmptyOrAllLocations || correctBecausePageAggregator || correctBecauseShopAggregator || correctBecauseCsiSystemAggregator)) {
                return ['de.iteratec.isocsi.CsiDashboardController$ShowAllCommand.selectedLocations.validator.error.selectedLocations']
            }

        })

        selectedCsiSystems(nullable: false, validator: { Collection currentCollection, CsiDashboardShowAllCommand cmd ->
            boolean correctBecauseHourlyEventAggregator = cmd.aggrGroupAndInterval.equals(CsiDashboardController.HOURLY_MEASURED_EVENT)
            boolean correctBecausePageAggregator = cmd.aggrGroupAndInterval.equals(CsiDashboardController.WEEKLY_AGGR_GROUP_PAGE) || cmd.aggrGroupAndInterval.equals(CsiDashboardController.DAILY_AGGR_GROUP_PAGE)
            boolean correctBecauseShopAggregator = cmd.aggrGroupAndInterval.equals(CsiDashboardController.WEEKLY_AGGR_GROUP_SHOP) || cmd.aggrGroupAndInterval.equals(CsiDashboardController.DAILY_AGGR_GROUP_SHOP)
            boolean correctBecauseCsiSystemAggreagatorAndNotEmpty = (cmd.aggrGroupAndInterval.equals(CsiDashboardController.WEEKLY_AGGR_GROUP_SYSTEM) || cmd.aggrGroupAndInterval.equals(CsiDashboardController.DAILY_AGGR_GROUP_SYSTEM)) && !currentCollection.isEmpty()

            if (!(correctBecauseHourlyEventAggregator || correctBecausePageAggregator || correctBecauseShopAggregator || correctBecauseCsiSystemAggreagatorAndNotEmpty)) {
                return ['de.iteratec.isocsi.CsiDashboardController$ShowAllCommand.selectedLocations.validator.error.selectedCsiSystems']
            }
        })
        csiTypeVisuallyComplete(validator: { boolean b, CsiDashboardShowAllCommand cmd ->
            if (!(cmd.csiTypeDocComplete || cmd.csiTypeVisuallyComplete)) {
                return ['de.iteratec.isocsi.CsiDashboardController$ShowAllCommand.selectedLocations.validator.error.selectedCsiType']
            }
        })

        includeInterval(nullable: true)
        csiAggregationUtilService(nullable: true)
        overwriteWarningAboutLongProcessingTime(nullable: true)
        chartTitle(nullable: true)
        loadTimeMaximum(nullable: true)
        dashboardName(nullable: true)
    }

    static transients = ['selectedTimeFrame', 'firstDayWithTime', 'selectedInterval', 'selectedAggregatorType']

    /**
     * <p>
     * Returns the selected time frame as {@link org.joda.time.Interval}.
     * That is the interval from {@link #from} / {@link #fromHour} to {@link #to} / {@link #toHour} if {@link #selectedTimeFrameInterval} is 0 (that means manual).
     * If {@link #selectedTimeFrameInterval} is greater 0 the returned time frame is now minus {@link #selectedTimeFrameInterval} minutes to now.
     * </p>
     *
     * @return not <code>null</code>.
     * @throws IllegalStateException
     *         if called on an invalid instance.
     */
    public Interval receiveSelectedTimeFrame() throws IllegalStateException {

        DateTime start
        DateTime end

        if (this.selectedTimeFrameInterval == 0) {
            start = this.from
            end = this.to
        } else {
            end = DateTime.now()
            start = end.minusSeconds(this.selectedTimeFrameInterval)
        }
        if (!includeInterval) {
            Integer intervalInMinutes = this.receiveSelectedMeasuredIntervalInMinutes()
            if (csiAggregationUtilService.isInActualInterval(end, intervalInMinutes)) {
                end = csiAggregationUtilService.subtractOneInterval(end, intervalInMinutes)
            }
            if (csiAggregationUtilService.isInActualInterval(start, intervalInMinutes) || !start.isBefore(end)) {
                start = csiAggregationUtilService.subtractOneInterval(start, intervalInMinutes)
            }
        }
        return new Interval(start, end)
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

        viewModelToCopyTo.put('selectedFolder', this.selectedFolder)
        viewModelToCopyTo.put('selectedPages', this.selectedPages)

        viewModelToCopyTo.put('selectedMeasuredEventIds', this.selectedMeasuredEventIds)

        viewModelToCopyTo.put('selectedBrowsers', this.selectedBrowsers)

        viewModelToCopyTo.put('selectedLocations', this.selectedLocations)
        viewModelToCopyTo.put('selectedConnectivities', this.selectedConnectivities)
        viewModelToCopyTo.put('selectedCsiSystems', this.selectedCsiSystems)

        viewModelToCopyTo.put('from', this.from ? ParameterBindingUtility.ISO_DATE_TIME_FORMATTER.print(this.from) : null)

        viewModelToCopyTo.put('to', this.to ? ParameterBindingUtility.ISO_DATE_TIME_FORMATTER.print(this.to) : null)
        viewModelToCopyTo.put('aggrGroupAndInterval', this.aggrGroupAndInterval ?: CsiDashboardController.HOURLY_MEASURED_EVENT)
        viewModelToCopyTo.put('includeInterval', this.includeInterval)
        viewModelToCopyTo.put('chartTitle', this.chartTitle)
        viewModelToCopyTo.put('chartWidth', this.chartWidth)
        viewModelToCopyTo.put('chartHeight', this.chartHeight)
        viewModelToCopyTo.put('showDataLabels', this.showDataLabels)
        viewModelToCopyTo.put('showDataMarkers', this.showDataMarkers)
        viewModelToCopyTo.put('loadTimeMaximum', this.loadTimeMaximum)
        viewModelToCopyTo.put('loadTimeMinimum', this.loadTimeMinimum)
        viewModelToCopyTo.put('csiTypeVisuallyComplete', this.csiTypeVisuallyComplete)
        viewModelToCopyTo.put('csiTypeDocComplete', this.csiTypeDocComplete)
        viewModelToCopyTo.put('graphNameAliases', this.graphNameAliases as JSON)
        viewModelToCopyTo.put('graphColors', this.graphColors as JSON)
        viewModelToCopyTo.put('publiclyVisible', this.publiclyVisible)
        viewModelToCopyTo.put('dashboardName', this.dashboardName)
    }

    /**
     * <p>
     * Creates {@link de.iteratec.osm.result.MvQueryParams} based on this command. This command
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

        MvQueryParams result = new MvQueryParams()

        result.jobGroupIds.addAll(this.selectedFolder)

        if (this.selectedMeasuredEventIds) {
            result.measuredEventIds.addAll(this.selectedMeasuredEventIds)
        }

        result.pageIds.addAll(this.selectedPages)

        if (this.selectedBrowsers) {
            result.browserIds.addAll(this.selectedBrowsers)
        }

        if (this.selectedLocations) {
            result.locationIds.addAll(this.selectedLocations)
        }
        if (!this.selectedConnectivities) {
            result.connectivityProfileIds.addAll(ConnectivityProfile.list()*.ident())
        } else if (this.selectedConnectivities.size() > 0) {
            result.connectivityProfileIds.addAll(this.selectedConnectivities.collect {it as Long})
        }

        return result
    }

    public Integer receiveSelectedMeasuredIntervalInMinutes() {
        Integer interval
        switch (this.aggrGroupAndInterval) {
            case CsiDashboardController.HOURLY_MEASURED_EVENT:
                interval = CsiAggregationInterval.HOURLY; break
            case CsiDashboardController.DAILY_AGGR_GROUP_PAGE:
            case CsiDashboardController.DAILY_AGGR_GROUP_SHOP:
            case CsiDashboardController.DAILY_AGGR_GROUP_SYSTEM:
                interval = CsiAggregationInterval.DAILY; break
            case CsiDashboardController.WEEKLY_AGGR_GROUP_PAGE:
            case CsiDashboardController.WEEKLY_AGGR_GROUP_SHOP:
            case CsiDashboardController.WEEKLY_AGGR_GROUP_SYSTEM:
                interval = CsiAggregationInterval.WEEKLY; break
            default:
                throw new IllegalArgumentException("No valid CsiAggregationInterval could be determined from command attrribute aggrGroupAndInterval (actual value=${this.aggrGroupAndInterval})")
        }
        return interval
    }

    public static String receiveControlnameFor(CsiType csiType) {
        if (csiType == CsiType.DOC_COMPLETE) {
            return 'csiTypeDocComplete'
        } else if (csiType == CsiType.VISUALLY_COMPLETE) {
            return 'csiTypeVisuallyComplete'
        } else {
            throw new IllegalArgumentException("The following ")
        }
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
     * @param selectedAggregationIntervallInMintues
     *         The number of minutes in selected measuring interval; >= 1.
     * @param countOfSelectedSystems
     *         The number of selected systems / {@link de.iteratec.osm.measurement.schedule.JobGroup}s; >= 1.
     * @param countOfSelectedPages
     *         The number of selected pages; >= 1.
     * @param countOfSelectedBrowser
     *         The number of selected browser; >= 1.
     *
     * @return <code>true</code> if the user should be warned,
     *         <code>false</code> else.
     * @since IT-95, significantly changed for IT-152.
     */
    public boolean shouldWarnAboutLongProcessingTime(
            Interval timeFrame,
            int selectedAggregationIntervallInMintues,
            int countOfSelectedBrowser) {

        int countOfSelectedSystems = selectedFolder.size()
        int countOfSelectedPages = selectedPages.size()
        int minutesInTimeFrame = new Duration(timeFrame.getStart(), timeFrame.getEnd()).getStandardMinutes()

        long expectedCountOfGraphs = countOfSelectedSystems * countOfSelectedPages * countOfSelectedBrowser
        long expectedPointsOfEachGraph = Math.round(minutesInTimeFrame / selectedAggregationIntervallInMintues)
        long expectedTotalNumberOfPoints = expectedCountOfGraphs * expectedPointsOfEachGraph

        return expectedTotalNumberOfPoints > 50000
    }
}

package de.iteratec.osm.csi

import de.iteratec.osm.report.chart.CsiAggregationInterval
import de.iteratec.osm.report.chart.CsiAggregationUtilService
import de.iteratec.osm.result.TimeSeriesShowCommandBase
import org.joda.time.DateTime
import org.joda.time.Duration
import org.joda.time.Interval

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
class CsiDashboardShowAllCommand extends TimeSeriesShowCommandBase {
    CsiAggregationUtilService csiAggregationUtilService

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
     * Whether or not current and not yet finished intervals should be loaded and displayed
     */
    Boolean includeInterval

    boolean csiTypeVisuallyComplete
    boolean csiTypeDocComplete

    /**
     * Constraints needs to fit.
     */
    static constraints = {
        importFrom(TimeSeriesShowCommandBase, exclude:[/selected.*/])

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
    }

    static transients = ['selectedTimeFrame', 'firstDayWithTime', 'selectedInterval', 'selectedAggregatorType']

    @Override
    Interval createTimeFrameInterval() {
        Interval interval = super.createTimeFrameInterval()
        if (includeInterval) {
            return interval
        }
        DateTime end = interval.end
        DateTime start = interval.start
        Integer intervalInMinutes = this.receiveSelectedMeasuredIntervalInMinutes()
        if (csiAggregationUtilService.isInActualInterval(end, intervalInMinutes)) {
            end = csiAggregationUtilService.subtractOneInterval(end, intervalInMinutes)
        }
        if (csiAggregationUtilService.isInActualInterval(start, intervalInMinutes) || !start.isBefore(end)) {
            start = csiAggregationUtilService.subtractOneInterval(start, intervalInMinutes)
        }
        return new Interval(start, end)
    }

    @Override
    void copyRequestDataToViewModelMap(Map<String, Object> viewModelToCopyTo) {
        super.copyRequestDataToViewModelMap(viewModelToCopyTo)
        viewModelToCopyTo.put('selectedCsiSystems', this.selectedCsiSystems)
        viewModelToCopyTo.put('aggrGroupAndInterval', this.aggrGroupAndInterval ?: CsiDashboardController.HOURLY_MEASURED_EVENT)
        viewModelToCopyTo.put('includeInterval', this.includeInterval)
        viewModelToCopyTo.put('csiTypeVisuallyComplete', this.csiTypeVisuallyComplete)
        viewModelToCopyTo.put('csiTypeDocComplete', this.csiTypeDocComplete)
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

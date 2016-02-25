package de.iteratec.osm.csi

import de.iteratec.osm.measurement.schedule.ConnectivityProfile
import de.iteratec.osm.report.chart.CsiAggregationInterval
import de.iteratec.osm.report.chart.CsiAggregationUtilService
import de.iteratec.osm.result.MvQueryParams
import grails.validation.Validateable
import org.joda.time.DateTime
import org.joda.time.Interval
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter

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
@Validateable(nullable = true)
public class CsiDashboardShowAllCommand {

    CsiAggregationUtilService csiAggregationUtilService

    /**
     * The selected start date (inclusive).
     *
     * Please use {@link #getSelectedTimeFrame()}.
     */
    Date from

    /**
     * The selected end date (inclusive).
     *
     * Please use {@link #getSelectedTimeFrame()}.
     */
    Date to

    /**
     * The selected start hour of date.
     *
     * Please use {@link #getSelectedTimeFrame()}.
     */
    String fromHour
    String fromMinute

    /**
     * The selected end hour of date.
     *
     * Please use {@link #getSelectedTimeFrame()}.
     */
    String toHour
    String toMinute

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
     * Flag for manual debugging.
     * Used for debugging highcharts-export-server, e.g.
     */
    Boolean debug

    /**
     * A predefined time frame.
     */
    int selectedTimeFrameInterval = 259200

    /**
     * Whether or not the time of the start-date should be selected manually.
     */
    Boolean setFromHour
    /**
     * Whether or not the time of the start-date should be selected manually.
     */
    Boolean setToHour

    /**
     * Whether or not current and not yet finished intervals should be loaded and displayed
     */
    Boolean includeInterval

    /**
     * The database IDs of the selected {@linkplain de.iteratec.osm.measurement.schedule.ConnectivityProfile}s which results to be shown.
     *
     * These selections are only relevant if
     * {@link #selectedAllConnectivityProfiles} is evaluated to
     * <code>false</code>.
     */
    Collection<Long> selectedConnectivityProfiles = []

    /**
     * User enforced the selection of all ConnectivityProfiles.
     * This selection <em>is not</em> reflected in
     * {@link #selectedConnectivityProfiles} cause of URL length
     * restrictions. If this flag is evaluated to
     * <code>true</code>, the selections in
     * {@link #selectedConnectivityProfiles} should be ignored.
     */
    Boolean selectedAllConnectivityProfiles = true

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

    boolean csiTypeVisuallyComplete
    boolean csiTypeDocComplete

    /**
     * Constraints needs to fit.
     */
    static constraints = {

        from(nullable: true, validator: { Date currentFrom, CsiDashboardShowAllCommand cmd ->

            boolean manualTimeframe = cmd.selectedTimeFrameInterval == 0

            if (manualTimeframe && currentFrom == null) {
                return ['de.iteratec.isr.CsiDashboardController$ShowAllCommand.from.nullWithManualSelection']
            }

        })

        to(nullable: true, validator: { Date currentTo, CsiDashboardShowAllCommand cmd ->

            boolean manualTimeframe = cmd.selectedTimeFrameInterval == 0

            if (manualTimeframe && currentTo == null) {
                return ['de.iteratec.isr.CsiDashboardController$ShowAllCommand.to.nullWithManualSelection']
            } else if (manualTimeframe && currentTo != null && cmd.from != null && currentTo.before(cmd.from)) {
                return ['de.iteratec.isr.CsiDashboardController$ShowAllCommand.to.beforeFromDate']
            }
        })

        fromHour(nullable: true, validator: { String currentFromHour, CsiDashboardShowAllCommand cmd ->

            boolean manualTimeframe = cmd.selectedTimeFrameInterval == 0

            if (manualTimeframe && currentFromHour == null) {
                return ['de.iteratec.isr.CsiDashboardController$ShowAllCommand.fromHour.nullWithManualSelection']
            }

        })

        toHour(nullable: true, validator: { String currentToHour, CsiDashboardShowAllCommand cmd ->

            boolean manualTimeframe = cmd.selectedTimeFrameInterval == 0

            if (manualTimeframe && currentToHour == null) {
                return ['de.iteratec.isr.CsiDashboardController$ShowAllCommand.toHour.nullWithManualSelection']
            } else if (manualTimeframe && cmd.from != null && cmd.to != null && cmd.from.equals(cmd.to) && cmd.fromHour != null && currentToHour != null) {

                DateTime firstDayWithFromDaytime = getFirstDayWithTime(cmd.fromHour)
                DateTime firstDayWithToDaytime = getFirstDayWithTime(currentToHour)

                if (!firstDayWithToDaytime.isAfter(firstDayWithFromDaytime)) {
                    return ['de.iteratec.isr.CsiDashboardController$ShowAllCommand.toHour.inCombinationWithDateBeforeFrom']
                }

            }
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

        selectedMeasuredEventIds(nullable: false, validator: { Collection currentCollection, CsiDashboardShowAllCommand cmd ->

            boolean correctBecauseHourlyEventAndNotEmptyOrAllEvents = cmd.aggrGroupAndInterval.equals(CsiDashboardController.HOURLY_MEASURED_EVENT) && (!currentCollection.isEmpty() || cmd.selectedAllMeasuredEvents)
            boolean correctBecausePageAggregator = cmd.aggrGroupAndInterval.equals(CsiDashboardController.WEEKLY_AGGR_GROUP_PAGE) || cmd.aggrGroupAndInterval.equals(CsiDashboardController.DAILY_AGGR_GROUP_PAGE)
            boolean correctBecauseShopAggregator = cmd.aggrGroupAndInterval.equals(CsiDashboardController.WEEKLY_AGGR_GROUP_SHOP) || cmd.aggrGroupAndInterval.equals(CsiDashboardController.DAILY_AGGR_GROUP_SHOP)
            boolean correctBeacuseCsiSystemAggregator = cmd.aggrGroupAndInterval.equals(CsiDashboardController.WEEKLY_AGGR_GROUP_SYSTEM) || cmd.aggrGroupAndInterval.equals(CsiDashboardController.DAILY_AGGR_GROUP_SYSTEM)

            if (!(correctBecauseHourlyEventAndNotEmptyOrAllEvents || correctBecausePageAggregator || correctBecauseShopAggregator || correctBeacuseCsiSystemAggregator)) {
                return ['de.iteratec.isocsi.CsiDashboardController$ShowAllCommand.selectedMeasuredEvents.validator.error.selectedMeasuredEvents']
            }

        })

        selectedBrowsers(nullable: false, validator: { Collection currentCollection, CsiDashboardShowAllCommand cmd ->

            boolean correctBecauseHourlyEventAndNotEmptyOrAllBrowsers = cmd.aggrGroupAndInterval.equals(CsiDashboardController.HOURLY_MEASURED_EVENT) && (!currentCollection.isEmpty() || cmd.selectedAllBrowsers)
            boolean correctBecausePageAggregator = cmd.aggrGroupAndInterval.equals(CsiDashboardController.WEEKLY_AGGR_GROUP_PAGE) || cmd.aggrGroupAndInterval.equals(CsiDashboardController.DAILY_AGGR_GROUP_PAGE)
            boolean correctBecauseShopAggregator = cmd.aggrGroupAndInterval.equals(CsiDashboardController.WEEKLY_AGGR_GROUP_SHOP) || cmd.aggrGroupAndInterval.equals(CsiDashboardController.DAILY_AGGR_GROUP_SHOP)
            boolean correctBeacuseCsiSystemAggregator = cmd.aggrGroupAndInterval.equals(CsiDashboardController.WEEKLY_AGGR_GROUP_SYSTEM) || cmd.aggrGroupAndInterval.equals(CsiDashboardController.DAILY_AGGR_GROUP_SYSTEM)

            if (!(correctBecauseHourlyEventAndNotEmptyOrAllBrowsers || correctBecausePageAggregator || correctBecauseShopAggregator || correctBeacuseCsiSystemAggregator)) {
                return ['de.iteratec.isocsi.CsiDashboardController$ShowAllCommand.selectedBrowsers.validator.error.selectedBrowsers']
            }

        })

        selectedLocations(nullable: false, validator: { Collection currentCollection, CsiDashboardShowAllCommand cmd ->

            boolean correctBecauseHourlyEventAggregatorAndNotEmptyOrAllLocations = cmd.aggrGroupAndInterval.equals(CsiDashboardController.HOURLY_MEASURED_EVENT) && (!currentCollection.isEmpty() || cmd.selectedAllLocations)
            boolean correctBecausePageAggregator = cmd.aggrGroupAndInterval.equals(CsiDashboardController.WEEKLY_AGGR_GROUP_PAGE) || cmd.aggrGroupAndInterval.equals(CsiDashboardController.DAILY_AGGR_GROUP_PAGE)
            boolean correctBecauseShopAggregator = cmd.aggrGroupAndInterval.equals(CsiDashboardController.WEEKLY_AGGR_GROUP_SHOP) || cmd.aggrGroupAndInterval.equals(CsiDashboardController.DAILY_AGGR_GROUP_SHOP)
            boolean correctBecauseCsiSystemAggregator = cmd.aggrGroupAndInterval.equals(CsiDashboardController.WEEKLY_AGGR_GROUP_SYSTEM) || cmd.aggrGroupAndInterval.equals(CsiDashboardController.DAILY_AGGR_GROUP_SYSTEM)

            if (!(correctBecauseHourlyEventAggregatorAndNotEmptyOrAllLocations || correctBecausePageAggregator || correctBecauseShopAggregator || correctBecauseCsiSystemAggregator)) {
                return ['de.iteratec.isocsi.CsiDashboardController$ShowAllCommand.selectedLocations.validator.error.selectedLocations']
            }

        })

        selectedCsiSystems(nullable: false, validator: {Collection currentCollection, CsiDashboardShowAllCommand cmd ->
            boolean correctBecauseHourlyEventAggregator = cmd.aggrGroupAndInterval.equals(CsiDashboardController.HOURLY_MEASURED_EVENT)
            boolean correctBecausePageAggregator = cmd.aggrGroupAndInterval.equals(CsiDashboardController.WEEKLY_AGGR_GROUP_PAGE) || cmd.aggrGroupAndInterval.equals(CsiDashboardController.DAILY_AGGR_GROUP_PAGE)
            boolean correctBecauseShopAggregator = cmd.aggrGroupAndInterval.equals(CsiDashboardController.WEEKLY_AGGR_GROUP_SHOP) || cmd.aggrGroupAndInterval.equals(CsiDashboardController.DAILY_AGGR_GROUP_SHOP)
            boolean correctBecauseCsiSystemAggreagatorAndNotEmpty = (cmd.aggrGroupAndInterval.equals(CsiDashboardController.WEEKLY_AGGR_GROUP_SYSTEM) || cmd.aggrGroupAndInterval.equals(CsiDashboardController.DAILY_AGGR_GROUP_SYSTEM)) && !currentCollection.isEmpty()

            if (!(correctBecauseHourlyEventAggregator || correctBecausePageAggregator || correctBecauseShopAggregator || correctBecauseCsiSystemAggreagatorAndNotEmpty)) {
                return ['de.iteratec.isocsi.CsiDashboardController$ShowAllCommand.selectedLocations.validator.error.selectedCsiSystems']
            }
        })
        csiTypeVisuallyComplete(validator: {boolean b , CsiDashboardShowAllCommand cmd ->
            if(!(cmd.csiTypeDocComplete || cmd.csiTypeVisuallyComplete) ){
                return ['de.iteratec.isocsi.CsiDashboardController$ShowAllCommand.selectedLocations.validator.error.selectedCsiType']
            }
        })

        overwriteWarningAboutLongProcessingTime(nullable: true)
        chartTitle(nullable: true)
        loadTimeMaximum(nullable: true)

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
    public Interval getSelectedTimeFrame() throws IllegalStateException {
        if (!this.validate()) {
            throw new IllegalStateException('A time frame is not available from an invalid command.')
        }

        DateTime start
        DateTime end

        Boolean manualTimeframe = this.selectedTimeFrameInterval == 0
        if (manualTimeframe && fromHour && toHour) {

            DateTime firstDayWithFromHourAsDaytime
            DateTime firstDayWithToHourAsDaytime

            if (fromMinute != null) {
                firstDayWithFromHourAsDaytime = getFirstDayWithTime(fromHour + ":" + fromMinute.padRight(2, '0'))
            } else if (fromHour.indexOf(":") > -1) {
                firstDayWithFromHourAsDaytime = getFirstDayWithTime(fromHour)
            } else {
                firstDayWithFromHourAsDaytime = getFirstDayWithTime(fromHour + ":00")
            }

            if (toMinute != null) {
                firstDayWithToHourAsDaytime = getFirstDayWithTime(toHour + ":" + toMinute.padRight(2, '0'))
            } else if (toHour.indexOf(":") > -1) {
                firstDayWithToHourAsDaytime = getFirstDayWithTime(toHour)
            } else {
                firstDayWithToHourAsDaytime = getFirstDayWithTime(toHour + ":00")
            }

            start = new DateTime(this.from.getTime())
                    .withTime(
                    firstDayWithFromHourAsDaytime.getHourOfDay(),
                    firstDayWithFromHourAsDaytime.getMinuteOfHour(),
                    0, 0
            )
            end = new DateTime(this.to.getTime())
                    .withTime(
                    firstDayWithToHourAsDaytime.getHourOfDay(),
                    firstDayWithToHourAsDaytime.getMinuteOfHour(),
                    59, 999
            )

        } else {

            end = new DateTime()
            start = end.minusSeconds(this.selectedTimeFrameInterval)

        }
        if (!includeInterval) {
            Integer intervalInMinutes = this.getSelectedMeasuredIntervalInMinutes()
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
     * Returns a {@link DateTime} of the first csiDay in unix-epoch with daytime respective param timeWithOrWithoutMeridian.
     * @param timeWithOrWithoutMeridian
     * 		The format can be with or without meridian (e.g. "04:45", "16:12" without or "02:00 AM", "11:23 PM" with meridian)
     * @return A {@link DateTime} of the first csiDay in unix-epoch with daytime respective param timeWithOrWithoutMeridian.
     * @throws IllegalStateException If timeWithOrWithoutMeridian is in wrong format.
     */
    public static DateTime getFirstDayWithTime(String timeWithOrWithoutMeridian) throws IllegalStateException {

        Pattern regexWithMeridian = ~/\d{1,2}:\d\d [AP]M/
        Pattern regexWithoutMeridian = ~/\d{1,2}:\d\d/
        String dateFormatString

        if (timeWithOrWithoutMeridian ==~ regexWithMeridian) dateFormatString = "dd.MM.yyyy hh:mm"
        else if (timeWithOrWithoutMeridian ==~ regexWithoutMeridian) dateFormatString = "dd.MM.yyyy HH:mm"
        else throw new IllegalStateException("Wrong format of time: ${timeWithOrWithoutMeridian}")

        DateTimeFormatter fmt = DateTimeFormat.forPattern(dateFormatString)
        return fmt.parseDateTime("01.01.1970 ${timeWithOrWithoutMeridian}")

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

        viewModelToCopyTo.put('selectedAllMeasuredEvents', (this.selectedAllMeasuredEvents))
        viewModelToCopyTo.put('selectedMeasuredEventIds', this.selectedMeasuredEventIds)

        viewModelToCopyTo.put('selectedAllBrowsers', (this.selectedAllBrowsers))
        viewModelToCopyTo.put('selectedBrowsers', this.selectedBrowsers)

        viewModelToCopyTo.put('selectedAllLocations', (this.selectedAllLocations))
        viewModelToCopyTo.put('selectedLocations', this.selectedLocations)
        viewModelToCopyTo.put('selectedAllConnectivityProfiles', this.selectedAllConnectivityProfiles)
        viewModelToCopyTo.put('selectedConnectivityProfiles', this.selectedConnectivityProfiles)

        viewModelToCopyTo.put('from', this.from)
        if (!this.fromHour.is(null)) {
            viewModelToCopyTo.put('fromHour', this.fromHour)
        }

        viewModelToCopyTo.put('to', this.to)
        if (!this.toHour.is(null)) {
            viewModelToCopyTo.put('toHour', this.toHour)
        }
        viewModelToCopyTo.put('aggrGroupAndInterval', this.aggrGroupAndInterval ?: CsiDashboardController.HOURLY_MEASURED_EVENT)
        viewModelToCopyTo.put('debug', this.debug ?: false)
        viewModelToCopyTo.put('setFromHour', this.setFromHour)
        viewModelToCopyTo.put('setToHour', this.setToHour)
        viewModelToCopyTo.put('includeInterval', this.includeInterval)
        viewModelToCopyTo.put('setFromHour', this.setFromHour)
        viewModelToCopyTo.put('setToHour', this.setToHour)
        viewModelToCopyTo.put('chartTitle', this.chartTitle)
        viewModelToCopyTo.put('chartWidth', this.chartWidth)
        viewModelToCopyTo.put('chartHeight', this.chartHeight)
        viewModelToCopyTo.put('showDataLabels', this.showDataLabels)
        viewModelToCopyTo.put('showDataMarkers', this.showDataMarkers)
        viewModelToCopyTo.put('loadTimeMaximum', this.loadTimeMaximum)
        viewModelToCopyTo.put('loadTimeMinimum', this.loadTimeMinimum)
        viewModelToCopyTo.put('csiTypeVisuallyComplete',this.csiTypeVisuallyComplete)
        viewModelToCopyTo.put('csiTypeDocComplete',this.csiTypeDocComplete)
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

        if (!this.selectedAllMeasuredEvents) {
            result.measuredEventIds.addAll(this.selectedMeasuredEventIds)
        }

        result.pageIds.addAll(this.selectedPages)

        if (!this.selectedAllBrowsers) {
            result.browserIds.addAll(this.selectedBrowsers)
        }

        if (!this.selectedAllLocations) {
            result.locationIds.addAll(this.selectedLocations)
        }
        if (this.selectedAllConnectivityProfiles) {
            result.connectivityProfileIds.addAll(ConnectivityProfile.list()*.ident())
        } else if (this.selectedConnectivityProfiles.size() > 0) {
            result.connectivityProfileIds.addAll(this.selectedConnectivityProfiles)
        }

        return result
    }

    public Integer getSelectedMeasuredIntervalInMinutes() {
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
}
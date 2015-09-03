package de.iteratec.osm.csi

import de.iteratec.osm.report.chart.AggregatorType
import de.iteratec.osm.report.chart.MeasuredValueInterval
import de.iteratec.osm.report.chart.MeasuredValueUtilService
import de.iteratec.osm.result.MvQueryParams
import grails.validation.Validateable

import java.util.regex.Pattern

import org.joda.time.DateTime
import org.joda.time.Interval
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter

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
@Validateable
public class CsiDashboardShowAllCommand {

    MeasuredValueUtilService measuredValueUtilService

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
    String  fromHour
    String  fromMinute

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
     * @see de.iteratec.osm.report.chart.AggregatorType#getName()
     * @see de.iteratec.osm.report.chart.AggregatorType#MEASURED_STEP
     * @see de.iteratec.osm.report.chart.AggregatorType#PAGE
     * @see de.iteratec.osm.report.chart.AggregatorType#PAGE_AND_BROWSER
     * @see de.iteratec.osm.report.chart.AggregatorType#SHOP
     */
    String aggrGroup

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
    Boolean selectedAllMeasuredEvents

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
    Boolean selectedAllBrowsers

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
    Boolean selectedAllLocations

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
     * Constraints needs to fit.
     */
    static constraints = {
        from(nullable: true, validator: {Date currentFrom, CsiDashboardShowAllCommand cmd ->
            boolean manualTimeframe = cmd.selectedTimeFrameInterval == 0
            if(manualTimeframe && currentFrom == null) return ['de.iteratec.isr.CsiDashboardController$ShowAllCommand.from.nullWithManualSelection']
        })
        to(nullable:true, validator: { Date currentTo, CsiDashboardShowAllCommand cmd ->
            boolean manualTimeframe = cmd.selectedTimeFrameInterval == 0
            if(manualTimeframe && currentTo == null) return ['de.iteratec.isr.CsiDashboardController$ShowAllCommand.to.nullWithManualSelection']
            else if(manualTimeframe && currentTo != null && cmd.from != null && currentTo.before(cmd.from)) return ['de.iteratec.isr.CsiDashboardController$ShowAllCommand.to.beforeFromDate']
        })
        fromHour(nullable: true, validator: {String currentFromHour, CsiDashboardShowAllCommand cmd ->
            boolean manualTimeframe = cmd.selectedTimeFrameInterval == 0
            if(manualTimeframe && currentFromHour == null) return ['de.iteratec.isr.CsiDashboardController$ShowAllCommand.fromHour.nullWithManualSelection']
        })
        toHour(nullable: true, validator: {String currentToHour, CsiDashboardShowAllCommand cmd ->
            boolean manualTimeframe = cmd.selectedTimeFrameInterval == 0
            if(manualTimeframe && currentToHour == null) {
                return ['de.iteratec.isr.CsiDashboardController$ShowAllCommand.toHour.nullWithManualSelection']
            }
            else if(manualTimeframe && cmd.from != null && cmd.to != null && cmd.from.equals(cmd.to) && cmd.fromHour != null && currentToHour != null) {
                DateTime firstDayWithFromDaytime = getFirstDayWithTime(cmd.fromHour)
                DateTime firstDayWithToDaytime = getFirstDayWithTime(currentToHour)
                if(!firstDayWithToDaytime.isAfter(firstDayWithFromDaytime)) return ['de.iteratec.isr.CsiDashboardController$ShowAllCommand.toHour.inCombinationWithDateBeforeFrom']
            }
        })
        aggrGroup(nullable:false, inList: [AggregatorType.MEASURED_EVENT, AggregatorType.PAGE, AggregatorType.SHOP, CsiDashboardController.DAILY_AGGR_GROUP_PAGE, CsiDashboardController.DAILY_AGGR_GROUP_SHOP])
        selectedFolder(nullable: false, validator: { Collection currentCollection, CsiDashboardShowAllCommand cmd ->
            if (currentCollection.isEmpty()) return ['de.iteratec.isocsi.CsiDashboardController$ShowAllCommand.selectedFolder.validator.error.selectedFolder']
        })

        // selectedPages is only allowed to be empty if aggrGroup is AggregatorType.SHOP
        selectedPages(nullable: false, validator: { Collection currentCollection, CsiDashboardShowAllCommand cmd ->
            if (!((AggregatorType.MEASURED_EVENT.equals(cmd.aggrGroup) && (!currentCollection.isEmpty())) ||
                    ( (AggregatorType.PAGE.equals(cmd.aggrGroup) || CsiDashboardController.DAILY_AGGR_GROUP_PAGE.equals(cmd.aggrGroup)) && (!currentCollection.isEmpty())) ||
                    AggregatorType.SHOP.equals(cmd.aggrGroup) || CsiDashboardController.DAILY_AGGR_GROUP_SHOP.equals(cmd.aggrGroup))) return ['de.iteratec.isocsi.CsiDashboardController$ShowAllCommand.selectedPage.validator.error.selectedPage']
        })
        // selectedMeasuredEventIds is only allowed to be empty if aggrGroup is NOT AggregatorType.MEASURED_EVENT or selectedAllMeasuredEvents evaluates to true
        selectedMeasuredEventIds(nullable:false, validator: { Collection currentCollection, CsiDashboardShowAllCommand cmd ->
            if (!((AggregatorType.MEASURED_EVENT.equals(cmd.aggrGroup) && (!currentCollection.isEmpty() || cmd.selectedAllMeasuredEvents)) ||
                    AggregatorType.PAGE.equals(cmd.aggrGroup)  || CsiDashboardController.DAILY_AGGR_GROUP_PAGE.equals(cmd.aggrGroup) ||
                    AggregatorType.SHOP.equals(cmd.aggrGroup) || CsiDashboardController.DAILY_AGGR_GROUP_SHOP.equals(cmd.aggrGroup))) return ['de.iteratec.isocsi.CsiDashboardController$ShowAllCommand.selectedMeasuredEvents.validator.error.selectedMeasuredEvents']
        })

        // selectedBrowsers is only allowed to be empty if aggrGroup is NOT AggregatorType.MEASURED_EVENT or selectedAllBrowsers evaluates to true
        selectedBrowsers(nullable:false, validator: { Collection currentCollection, CsiDashboardShowAllCommand cmd ->
            if (!((AggregatorType.MEASURED_EVENT.equals(cmd.aggrGroup) && (!currentCollection.isEmpty() || cmd.selectedAllBrowsers)) ||
                    AggregatorType.PAGE.equals(cmd.aggrGroup)  || CsiDashboardController.DAILY_AGGR_GROUP_PAGE.equals(cmd.aggrGroup) ||
                    AggregatorType.SHOP.equals(cmd.aggrGroup) || CsiDashboardController.DAILY_AGGR_GROUP_SHOP.equals(cmd.aggrGroup))) return ['de.iteratec.isocsi.CsiDashboardController$ShowAllCommand.selectedBrowsers.validator.error.selectedBrowsers']
        })

        // selectedLocations is only allowed to be empty if aggrGroup is NOT AggregatorType.MEASURED_EVENT or selectedAllLocations evaluates to true
        selectedLocations(nullable:false, validator: { Collection currentCollection, CsiDashboardShowAllCommand cmd ->
            if (!((AggregatorType.MEASURED_EVENT.equals(cmd.aggrGroup) && (!currentCollection.isEmpty() || cmd.selectedAllLocations)) ||
                    AggregatorType.PAGE.equals(cmd.aggrGroup) || CsiDashboardController.DAILY_AGGR_GROUP_PAGE.equals(cmd.aggrGroup) ||
                    AggregatorType.SHOP.equals(cmd.aggrGroup) || CsiDashboardController.DAILY_AGGR_GROUP_SHOP.equals(cmd.aggrGroup))) return ['de.iteratec.isocsi.CsiDashboardController$ShowAllCommand.selectedLocations.validator.error.selectedLocations']
        })
        overwriteWarningAboutLongProcessingTime(nullable:true)
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
    public Interval getSelectedTimeFrame() throws IllegalStateException
    {
        if( !this.validate() )
        {
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

        }else{

            end = new DateTime()
            start = end.minusSeconds(this.selectedTimeFrameInterval)

        }
        if(!includeInterval) {
            Integer intervalInMinutes = this.getSelectedMeasuredIntervalInMinutes()
            if(measuredValueUtilService.isInActualInterval(end, intervalInMinutes)){
                end = measuredValueUtilService.subtractOneInterval(end, intervalInMinutes)
            }
            if(measuredValueUtilService.isInActualInterval(start, intervalInMinutes) || !start.isBefore(end)){
                start = measuredValueUtilService.subtractOneInterval(start, intervalInMinutes)
            }
        }
        return new Interval(start, end)
    }

    /**
     * Returns a {@link DateTime} of the first day in unix-epoch with daytime respective param timeWithOrWithoutMeridian.
     * @param timeWithOrWithoutMeridian
     * 		The format can be with or without meridian (e.g. "04:45", "16:12" without or "02:00 AM", "11:23 PM" with meridian)
     * @return A {@link DateTime} of the first day in unix-epoch with daytime respective param timeWithOrWithoutMeridian.
     * @throws IllegalStateException If timeWithOrWithoutMeridian is in wrong format.
     */
    public static DateTime getFirstDayWithTime(String timeWithOrWithoutMeridian) throws IllegalStateException{

        Pattern regexWithMeridian = ~/\d{1,2}:\d\d [AP]M/
        Pattern regexWithoutMeridian = ~/\d{1,2}:\d\d/
        String dateFormatString

        if(timeWithOrWithoutMeridian ==~ regexWithMeridian) dateFormatString = "dd.MM.yyyy hh:mm"
        else if(timeWithOrWithoutMeridian ==~ regexWithoutMeridian) dateFormatString = "dd.MM.yyyy HH:mm"
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
    public void copyRequestDataToViewModelMap(Map<String, Object> viewModelToCopyTo)
    {
        viewModelToCopyTo.put('selectedTimeFrameInterval', this.selectedTimeFrameInterval)

        viewModelToCopyTo.put('selectedFolder', this.selectedFolder)
        viewModelToCopyTo.put('selectedPages', this.selectedPages)

        viewModelToCopyTo.put('selectedAllMeasuredEvents', (this.selectedAllMeasuredEvents as boolean ? 'on' : ''))
        viewModelToCopyTo.put('selectedMeasuredEventIds', this.selectedMeasuredEventIds)

        viewModelToCopyTo.put('selectedAllBrowsers', (this.selectedAllBrowsers as boolean ? 'on' : ''))
        viewModelToCopyTo.put('selectedBrowsers', this.selectedBrowsers)

        viewModelToCopyTo.put('selectedAllLocations', (this.selectedAllLocations as boolean ? 'on' : ''))
        viewModelToCopyTo.put('selectedLocations', this.selectedLocations)

        viewModelToCopyTo.put('from', this.from)
        if(!this.fromHour.is(null)) {
            viewModelToCopyTo.put('fromHour',this.fromHour)
        }

        viewModelToCopyTo.put('to', this.to)
        if (!this.toHour.is(null)){
            viewModelToCopyTo.put('toHour', this.toHour)
        }
        viewModelToCopyTo.put('aggrGroup', this.aggrGroup ?: AggregatorType.MEASURED_EVENT)
        viewModelToCopyTo.put('debug', this.debug?:false)
        viewModelToCopyTo.put('setFromHour', this.setFromHour)
        viewModelToCopyTo.put('setToHour', this.setToHour)
        viewModelToCopyTo.put('includeInterval', this.includeInterval)
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
    public MvQueryParams createMvQueryParams() throws IllegalStateException
    {
        if( !this.validate() )
        {
            throw new IllegalStateException('Query params are not available from an invalid command.')
        }

        MvQueryParams result = new MvQueryParams()

        result.jobGroupIds.addAll(this.selectedFolder)

        if( !this.selectedAllMeasuredEvents )
        {
            result.measuredEventIds.addAll(this.selectedMeasuredEventIds)
        }

        result.pageIds.addAll(this.selectedPages)

        if( !this.selectedAllBrowsers )
        {
            result.browserIds.addAll(this.selectedBrowsers)
        }

        if( !this.selectedAllLocations )
        {
            result.locationIds.addAll(this.selectedLocations)
        }

        return result
    }
    public Integer getSelectedMeasuredIntervalInMinutes(){
        Integer interval
        switch (this.aggrGroup){
            case AggregatorType.MEASURED_EVENT:
                interval = MeasuredValueInterval.HOURLY;break
            case CsiDashboardController.DAILY_AGGR_GROUP_PAGE:
                interval = MeasuredValueInterval.DAILY;break
            case AggregatorType.PAGE:
                interval = MeasuredValueInterval.WEEKLY;break
            case CsiDashboardController.DAILY_AGGR_GROUP_SHOP:
                interval = MeasuredValueInterval.DAILY;break
            case AggregatorType.SHOP:
                interval = MeasuredValueInterval.WEEKLY;break
            default:
                throw new IllegalArgumentException("No valid MeasuredValueInterval could be determined from command attrribute aggrGroup (actual value=${this.aggrGroup})")
        }
        return interval
    }
}
package de.iteratec.osm.result

import grails.validation.Validateable
import org.grails.databinding.BindUsing
import org.joda.time.DateTime
import org.joda.time.Interval
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter

import java.text.SimpleDateFormat
import java.util.regex.Pattern

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
class DetailAnalysisDashboardShowCommand implements Validateable {
    private final static SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat(EventResultDashboardController.DATE_FORMAT_STRING);

    /**
     * The selected start date.
     *
     * Please use {@link #getSelectedTimeFrame()}.
     */
    @BindUsing({
        obj, source ->

            def dateObject = source['from']
            if (dateObject) {
                if (dateObject instanceof Date) {
                    return dateObject
                } else {
                    return SIMPLE_DATE_FORMAT.parse(dateObject)
                }
            }
    })
    Date from

    /**
     * The selected end date.
     *
     * Please use {@link #getSelectedTimeFrame()}.
     */
    @BindUsing({
        obj, source ->

            def dateObject = source['to']
            if (dateObject) {
                if (dateObject instanceof Date) {
                    return dateObject
                } else {
                    return SIMPLE_DATE_FORMAT.parse(dateObject)
                }
            }
    })
    Date to

    /**
     * The selected start hour of date.
     *
     * Please use {@link #getSelectedTimeFrame()}.
     */
    String fromHour

    /**
     * The selected end hour of date.
     *
     * Please use {@link #getSelectedTimeFrame()}.
     */
    String toHour

    /**
     * A predefined time frame.
     */
    int selectedTimeFrameInterval = 259200

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
     * Whether or not the time of the start-date should be selected manually.
     */
    Boolean setFromHour
    /**
     * Whether or not the time of the start-date should be selected manually.
     */
    Boolean setToHour

    /**
     * Constraints needs to fit.
     */
    static constraints = {
        from(nullable: true, validator: { Date currentFrom, DetailAnalysisDashboardShowCommand cmd ->
            boolean manualTimeframe = cmd.selectedTimeFrameInterval == 0
            if (manualTimeframe && currentFrom == null) return ['de.iteratec.osm.gui.startAndEndDateSelection.error.from.nullWithManualSelection']
        })
        to(nullable: true, validator: { Date currentTo, DetailAnalysisDashboardShowCommand cmd ->
            boolean manualTimeframe = cmd.selectedTimeFrameInterval == 0
            if (manualTimeframe && currentTo == null) return ['de.iteratec.osm.gui.startAndEndDateSelection.error.to.nullWithManualSelection']
            else if (manualTimeframe && currentTo != null && cmd.from != null && currentTo.before(cmd.from)) return ['de.iteratec.osm.gui.startAndEndDateSelection.error.to.beforeFromDate']
        })
        fromHour(nullable: true, validator: { String currentFromHour, DetailAnalysisDashboardShowCommand cmd ->
            boolean manualTimeframe = cmd.selectedTimeFrameInterval == 0
            if (manualTimeframe && currentFromHour == null) return ['de.iteratec.osm.gui.startAndEndDateSelection.error.fromHour.nullWithManualSelection']
        })
        toHour(nullable: true, validator: { String currentToHour, DetailAnalysisDashboardShowCommand cmd ->
            boolean manualTimeframe = cmd.selectedTimeFrameInterval == 0
            if (manualTimeframe && currentToHour == null) {
                return ['de.iteratec.osm.gui.startAndEndDateSelection.error.toHour.nullWithManualSelection']
            } else if (manualTimeframe && cmd.from != null && cmd.to != null && cmd.from.equals(cmd.to) && cmd.fromHour != null && currentToHour != null) {
                DateTime firstDayWithFromDaytime = getFirstDayWithTime(cmd.fromHour)
                DateTime firstDayWithToDaytime = getFirstDayWithTime(currentToHour)
                if (!firstDayWithToDaytime.isAfter(firstDayWithFromDaytime)) return ['de.iteratec.osm.gui.startAndEndDateSelection.error.toHour.inCombinationWithDateBeforeFrom']
            }
        })

        selectedFolder(nullable: false, validator: { Collection currentCollection, DetailAnalysisDashboardShowCommand cmd ->
            if (currentCollection.isEmpty()) return ['de.iteratec.osm.gui.selectedFolder.error.validator.error.selectedFolder']
        })
        selectedPages(nullable: true)

        setToHour(nullable: true)
        setFromHour(nullable: true)
    }

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
    Interval getSelectedTimeFrame() throws IllegalStateException {
        DateTime start
        DateTime end

        Boolean manualTimeframe = this.selectedTimeFrameInterval == 0
        if (manualTimeframe && fromHour && toHour) {

            DateTime firstDayWithFromHourAsDaytime = getFirstDayWithTime(fromHour)
            DateTime firstDayWithToHourAsDaytime = getFirstDayWithTime(toHour)

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

        return new Interval(start, end);
    }

    /**
     * Returns a {@link DateTime} of the first csiDay in unix-epoch with daytime respective param timeWithOrWithoutMeridian.
     * @param timeWithOrWithoutMeridian
     * 		The format can be with or without meridian (e.g. "04:45", "16:12" without or "02:00 AM", "11:23 PM" with meridian)
     * @return A {@link DateTime} of the first csiDay in unix-epoch with daytime respective param timeWithOrWithoutMeridian.
     * @throws IllegalStateException If timeWithOrWithoutMeridian is in wrong format.
     */
    static DateTime getFirstDayWithTime(String timeWithOrWithoutMeridian) throws IllegalStateException {

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
    void copyRequestDataToViewModelMap(Map<String, Object> viewModelToCopyTo) {

        viewModelToCopyTo.put('selectedTimeFrameInterval', this.selectedTimeFrameInterval)

        viewModelToCopyTo.put('selectedFolder', this.selectedFolder)
        viewModelToCopyTo.put('selectedPages', this.selectedPages)

        viewModelToCopyTo.put('from', this.from ? SIMPLE_DATE_FORMAT.format(this.from) : null)
        if (!this.fromHour.is(null)) {
            viewModelToCopyTo.put('fromHour', this.fromHour)
        }

        viewModelToCopyTo.put('to', this.to ? SIMPLE_DATE_FORMAT.format(this.to) : null)
        if (!this.toHour.is(null)) {
            viewModelToCopyTo.put('toHour', this.toHour)
        }

        viewModelToCopyTo.put('setFromHour', this.setFromHour)
        viewModelToCopyTo.put('setToHour', this.setToHour)
    }
}

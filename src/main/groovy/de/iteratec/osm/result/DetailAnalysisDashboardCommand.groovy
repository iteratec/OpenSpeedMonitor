package de.iteratec.osm.result

import grails.validation.Validateable
import org.joda.time.DateTime
import org.joda.time.Interval
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter

import java.util.regex.Pattern

class DetailAnalysisDashboardCommand implements Validateable{
    /**
     * The selected start date.
     *
     * Please use {@link #getSelectedTimeFrame()}.
     */
    Date from

    /**
     * The selected end date.
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


    /**
     * Whether or not the time of the start-date should be selected manually.
     */
    Boolean setFromHour
    /**
     * Whether or not the time of the start-date should be selected manually.
     */
    Boolean setToHour

    /**
     * Whether or not EventResults measured with native connectivity should get included.
     */
    Boolean includeNativeConnectivity

    /**
     * Whether or not EventResults measured with native connectivity should get included.
     */
    Boolean includeCustomConnectivity

    /**
     * If set, this is handled as a regular expression to select results measured with custom connectivity and whos custom
     * connectivity name matches this regex.
     */
    String customConnectivityName



    /**
     * Constraints needs to fit.
     */
    static constraints = {
        from(nullable: true, validator: { Date currentFrom, DetailAnalysisDashboardCommand cmd ->
            boolean manualTimeframe = cmd.selectedTimeFrameInterval == 0
            if (manualTimeframe && currentFrom == null) return ['de.iteratec.isr.EventResultDashboardController$ShowAllCommand.from.nullWithManualSelection']
        })
        to(nullable: true, validator: { Date currentTo, DetailAnalysisDashboardCommand cmd ->
            boolean manualTimeframe = cmd.selectedTimeFrameInterval == 0
            if (manualTimeframe && currentTo == null) return ['de.iteratec.isr.EventResultDashboardController$ShowAllCommand.to.nullWithManualSelection']
            else if (manualTimeframe && currentTo != null && cmd.from != null && currentTo.before(cmd.from)) return ['de.iteratec.isr.EventResultDashboardController$ShowAllCommand.to.beforeFromDate']
        })
        fromHour(nullable: true, validator: { String currentFromHour, DetailAnalysisDashboardCommand cmd ->
            boolean manualTimeframe = cmd.selectedTimeFrameInterval == 0
            if (manualTimeframe && currentFromHour == null) return ['de.iteratec.isr.EventResultDashboardController$ShowAllCommand.fromHour.nullWithManualSelection']
        })
        toHour(nullable: true, validator: { String currentToHour, DetailAnalysisDashboardCommand cmd ->
            boolean manualTimeframe = cmd.selectedTimeFrameInterval == 0
            if (manualTimeframe && currentToHour == null) {
                return ['de.iteratec.isr.EventResultDashboardController$ShowAllCommand.toHour.nullWithManualSelection']
            } else if (manualTimeframe && cmd.from != null && cmd.to != null && cmd.from.equals(cmd.to) && cmd.fromHour != null && currentToHour != null) {
                DateTime firstDayWithFromDaytime = getFirstDayWithTime(cmd.fromHour)
                DateTime firstDayWithToDaytime = getFirstDayWithTime(currentToHour)
                if (!firstDayWithToDaytime.isAfter(firstDayWithFromDaytime)) return ['de.iteratec.isr.EventResultDashboardController$ShowAllCommand.toHour.inCombinationWithDateBeforeFrom']
            }
        })
        selectedAllMeasuredEvents(nullable: true)
        selectedAllBrowsers(nullable: true)
        selectedAllLocations(nullable: true)

        selectedFolder(nullable: false, validator: { Collection currentCollection, DetailAnalysisDashboardCommand cmd ->
            if (currentCollection.isEmpty()) return ['de.iteratec.isr.EventResultDashboardController$ShowAllCommand.selectedFolder.validator.error.selectedFolder']
        })
        selectedPages(nullable: false, validator: { Collection currentCollection, DetailAnalysisDashboardCommand cmd ->
            if (currentCollection.isEmpty()) return ['de.iteratec.isr.EventResultDashboardController$ShowAllCommand.selectedPage.validator.error.selectedPage']
        })
        selectedBrowsers(nullable: false, validator: { Collection currentCollection, DetailAnalysisDashboardCommand cmd ->
            if (!cmd.selectedAllBrowsers && currentCollection.isEmpty()) return ['de.iteratec.isr.EventResultDashboardController$ShowAllCommand.selectedBrowsers.validator.error.selectedBrowsers']
        })
        selectedMeasuredEventIds(nullable: false, validator: { Collection currentCollection, DetailAnalysisDashboardCommand cmd ->
            if (!cmd.selectedAllMeasuredEvents && currentCollection.isEmpty()) return ['de.iteratec.isr.EventResultDashboardController$ShowAllCommand.selectedMeasuredEvents.validator.error.selectedMeasuredEvents']
        })
        selectedLocations(nullable: false, validator: { Collection currentCollection, DetailAnalysisDashboardCommand cmd ->
            if (!cmd.selectedAllLocations && currentCollection.isEmpty()) return ['de.iteratec.isr.EventResultDashboardController$ShowAllCommand.selectedLocations.validator.error.selectedLocations']
        })
        selectedAllConnectivityProfiles(nullable: true)

        includeNativeConnectivity(nullable: true)

        includeCustomConnectivity(nullable: true)

        customConnectivityName(nullable: true)
    }

    static transients = ['selectedTimeFrame']

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

        viewModelToCopyTo.put('selectedAllMeasuredEvents', this.selectedAllMeasuredEvents)
        viewModelToCopyTo.put('selectedMeasuredEventIds', this.selectedMeasuredEventIds)

        viewModelToCopyTo.put('selectedAllBrowsers', this.selectedAllBrowsers)
        viewModelToCopyTo.put('selectedBrowsers', this.selectedBrowsers)

        viewModelToCopyTo.put('selectedAllLocations', this.selectedAllLocations)
        viewModelToCopyTo.put('selectedLocations', this.selectedLocations)

        viewModelToCopyTo.put('selectedAllConnectivityProfiles', this.selectedAllConnectivityProfiles)
        viewModelToCopyTo.put('selectedConnectivityProfiles', this.selectedConnectivityProfiles)
        viewModelToCopyTo.put('includeNativeConnectivity', this.includeNativeConnectivity)
        viewModelToCopyTo.put('includeCustomConnectivity', this.includeCustomConnectivity)
        viewModelToCopyTo.put('customConnectivityName', this.customConnectivityName)

        viewModelToCopyTo.put('from', this.from)
        if (!this.fromHour.is(null)) {
            viewModelToCopyTo.put('fromHour', this.fromHour)
        }

        viewModelToCopyTo.put('to', this.to)
        if (!this.toHour.is(null)) {
            viewModelToCopyTo.put('toHour', this.toHour)
        }

        viewModelToCopyTo.put('setFromHour', this.setFromHour)
        viewModelToCopyTo.put('setToHour', this.setToHour)
    }
}

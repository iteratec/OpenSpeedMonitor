package de.iteratec.osm.result

import grails.validation.Validateable
import org.joda.time.DateTime
import org.joda.time.Interval
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter

import java.util.regex.Pattern

/**
 * <p>
 * Command of {@link TabularResultPresentationController#listResults(TabularResultListResultsCommand)
 *}.
 * </p>
 * Created by Marko Schnecke on 04.04.2016.
 */
@Validateable(nullable = true)
public class TabularResultEventResultsCommandBase {
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

    Integer max = 50

    Integer offset = 0

    /**
     * Constraints needs to fit.
     */
    static constraints = {
        from(nullable: true, validator: {Date currentFrom, TabularResultEventResultsCommandBase cmd ->
            boolean manualTimeframe = cmd.selectedTimeFrameInterval == 0
            if(manualTimeframe && currentFrom == null) return ['de.iteratec.osm.gui.startAndEndDateSelection.error.from.nullWithManualSelection']
        })
        to(nullable:true, validator: { Date currentTo, TabularResultEventResultsCommandBase cmd ->
            boolean manualTimeframe = cmd.selectedTimeFrameInterval == 0
            if(manualTimeframe && currentTo == null) return ['de.iteratec.osm.gui.startAndEndDateSelection.error.to.nullWithManualSelection']
            else if(manualTimeframe && currentTo != null && cmd.from != null && currentTo.before(cmd.from)) return ['de.iteratec.osm.gui.startAndEndDateSelection.error.to.beforeFromDate']
        })
        fromHour(nullable: true, validator: {String currentFromHour, TabularResultEventResultsCommandBase cmd ->
            boolean manualTimeframe = cmd.selectedTimeFrameInterval == 0
            if(manualTimeframe && currentFromHour == null) return ['de.iteratec.osm.gui.startAndEndDateSelection.error.fromHour.nullWithManualSelection']
        })
        toHour(nullable: true, validator: {String currentToHour, TabularResultEventResultsCommandBase cmd ->
            boolean manualTimeframe = cmd.selectedTimeFrameInterval == 0
            if(manualTimeframe && currentToHour == null) {
                return ['de.iteratec.osm.gui.startAndEndDateSelection.error.toHour.nullWithManualSelection']
            }
            else if(manualTimeframe && cmd.from != null && cmd.to != null && cmd.from.equals(cmd.to) && cmd.fromHour != null && currentToHour != null) {
                DateTime firstDayWithFromDaytime = getFirstDayWithTime(cmd.fromHour)
                DateTime firstDayWithToDaytime = getFirstDayWithTime(currentToHour)
                if(!firstDayWithToDaytime.isAfter(firstDayWithFromDaytime)) return ['de.iteratec.osm.gui.startAndEndDateSelection.error.toHour.inCombinationWithDateBeforeFrom']
            }
        })
        max(nullable:true)
        offset(nullable:true)
    }

    static transients = ['selectedTimeFrame']

    /**
     * <p>
     * Returns the selected time frame as {@link org.joda.time.Interval}, which is the
     * result of the aggregation of {@link #from}, {@link #fromHour},
     * to {@link #to}, {@link #toHour}.
     * </p>
     *
     * @return not <code>null</code>; end is intended to be inclusive
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

        }else{

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
        viewModelToCopyTo.put('from', this.from)
        if(!this.fromHour.is(null)) {
            viewModelToCopyTo.put('fromHour',this.fromHour)
        }

        viewModelToCopyTo.put('to', this.to)
        if (!this.toHour.is(null)){
            viewModelToCopyTo.put('toHour', this.toHour)
        }
        viewModelToCopyTo.put('max', this.max)
        viewModelToCopyTo.put('offset', this.offset)
    }
}

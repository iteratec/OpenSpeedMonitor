/* 
* OpenSpeedMonitor (OSM)
* Copyright 2014 iteratec GmbH
* 
* Licensed under the Apache License, Version 2.0 (the "License"); 
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* 
* 	http://www.apache.org/licenses/LICENSE-2.0
* 
* Unless required by applicable law or agreed to in writing, software 
* distributed under the License is distributed on an "AS IS" BASIS, 
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
* See the License for the specific language governing permissions and 
* limitations under the License.
*/

package de.iteratec.osm.report.chart


import static org.joda.time.DateTimeConstants.*

import org.joda.time.DateTime
import org.joda.time.DateTimeConstants
import org.joda.time.DateTimeZone
import org.joda.time.Interval

/**
 * <p>
 * An utility service for measured values.
 * </p>
 * 
 * <p>
 * Unless otherwise noted, passing a null argument to a constructor or method 
 * in this class will cause a NullPointerException to be thrown. 
 * </p>
 *
 * @TODO TODO mze-2013-07-12: Name possibly not matching. This service seems to
 *       handle intervals calculations and not measured values explicitly!?
 */
class CsiAggregationUtilService {

	/**
	 * <p>
	 * Calculates the interval (period) root/start for a {@linkplain DateTime 
	 * date}. This is the first date that would be within the selected 
	 * interval. This method uses {@link DateTimeConstants#FRIDAY} as reset
	 * csiDay-of-week for weekly intervals.
	 * </p>
	 * 
	 * <p>
	 * <em>Note:</em> This operation behaves functional and does neither relay
	 * on configuration nor database content.
	 * </p>
	 * 
	 * @param dateWithinInterval
	 *         The date within the interval which end is to be calculated.
	 * @param intervalInMinutes 
	 *         The interval length in minutes.
	 * @return The period start, not null.
	 * 
	 * @throws NullPointerException 
	 *         if at least on argument is <code>null</code>.
	 * 
	 * @see CsiAggregationInterval#HOURLY
	 * @see CsiAggregationInterval#DAILY
	 * @see CsiAggregationInterval#WEEKLY
	 */
	DateTime resetToStartOfActualInterval(DateTime dateWithinInterval, Integer intervalInMinutes) throws NullPointerException {
		return resetToStartOfActualInterval(dateWithinInterval, intervalInMinutes, FRIDAY);
	}

	/**
	 * <p>
	 * Calculates the interval (period) root/start for a {@linkplain DateTime
	 * date}. This is the first date that would be within the selected
	 * interval. This method uses the specified csiDay-of-week for weekly interval
	 * resets.
	 * </p>
	 *
	 * <p>
	 * <em>Note:</em> This operation behaves functional and does neither relay
	 * on configuration nor database content.
	 * </p>
	 *
	 * @param dateWithinInterval
	 *         The date within the interval which start is to be calculated.
	 * @param intervalInMinutes
	 *         The interval length in minutes.
	 * @param dayOfWeek
	 *         The csiDay of week to be used for weekly resets. Must be one of
	 *         {@link DateTimeConstants#MONDAY}, 
	 *         {@link DateTimeConstants#TUESDAY},
	 *         {@link DateTimeConstants#WEDNESDAY},
	 *         {@link DateTimeConstants#THURSDAY},
	 *         {@link DateTimeConstants#FRIDAY}.      
	 * @return The period start, not null.
	 *
	 * @throws NullPointerException
	 *         if at least on argument is <code>null</code>.
	 * @throws IllegalArgumentException
	 *         if {@code dayOfWeek} is not within the above specified range.
	 *
	 * @see CsiAggregationInterval#HOURLY
	 * @see CsiAggregationInterval#DAILY
	 * @see CsiAggregationInterval#WEEKLY
	 */
	DateTime resetToStartOfActualInterval(DateTime dateWithinInterval, Integer intervalInMinutes, int dayOfWeek) throws NullPointerException, IllegalArgumentException {
		DateTime result = dateWithinInterval
		switch (intervalInMinutes) {
			case CsiAggregationInterval.WEEKLY:
				result=result.withDayOfWeek(dayOfWeek)
				if( result.isAfter(dateWithinInterval) ) {
					result = result.minusWeeks(1);
				}
			case CsiAggregationInterval.DAILY:
			/* and CsiAggregationInterval.WEEKLY as well (as declared before) */
				result=result.withHourOfDay(0)
			default:
				result=result.withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0)
		}
		return result
	}

	/**
	 * <p>
	 * Calculates the interval (period) end for a {@linkplain DateTime date}. 
	 * This is the last date that would be within the selected interval. This 
	 * method uses {@link DateTimeConstants#FRIDAY} as reset csiDay-of-week for
	 * weekly intervals.
	 * </p>
	 * 
	 * <p>
	 * <em>Note:</em> This operation behaves functional and does neither relay 
	 * on configuration nor database content.
	 * </p>
	 * 
	 * @param dateWithinInterval
	 *         The date within the interval which end is to be calculated.
	 * @param intervalInMinutes 
	 * 		The interval length in minutes.
	 * @return 
	 * 		The period end, not null.
	 * 
	 * @throws NullPointerException
	 *         if at least on argument is <code>null</code>.
	 * 
	 * @see CsiAggregationInterval#HOURLY
	 * @see CsiAggregationInterval#DAILY
	 * @see CsiAggregationInterval#WEEKLY
	 */
	DateTime resetToEndOfActualInterval(DateTime dateWithinInterval, Integer intervalInMinutes) throws NullPointerException {
		return resetToStartOfActualInterval(dateWithinInterval, intervalInMinutes).plusMinutes(intervalInMinutes)
	}

	/**
	 * <p>
	 * Calculates the interval (period) end for a {@linkplain DateTime date}.
	 * This is the last date that would be within the selected interval. This 
	 * method uses the specified csiDay-of-week for weekly interval resets.
	 * </p>
	 *
	 * <p>
	 * <em>Note:</em> This operation behaves functional and does neither relay
	 * on configuration nor database content.
	 * </p>
	 *
	 * @param actualDateTime
	 * 		The date which period end is to be calculated.
	 * @param intervalInMinutes
	 * 		The interval length in minutes.
	 * @param dayOfWeek
	 *         The csiDay of week to be used for weekly resets. Must be one of
	 *         {@link DateTimeConstants#MONDAY}, 
	 *         {@link DateTimeConstants#TUESDAY},
	 *         {@link DateTimeConstants#WEDNESDAY},
	 *         {@link DateTimeConstants#THURSDAY},
	 *         {@link DateTimeConstants#FRIDAY}.   
	 * @return
	 * 		The period end, not null.
	 *
	 * @throws NullPointerException
	 *         if at least on argument is <code>null</code>.
	 * @throws IllegalArgumentException
	 *         if {@code dayOfWeek} is not within the above specified range.
	 *         
	 * @see CsiAggregationInterval#HOURLY
	 * @see CsiAggregationInterval#DAILY
	 * @see CsiAggregationInterval#WEEKLY
	 */
	DateTime resetToEndOfActualInterval(DateTime actualDateTime, Integer intervalInMinutes, int dayOfWeek) throws NullPointerException, IllegalArgumentException {
		DateTime endOfInterval = actualDateTime
		return resetToStartOfActualInterval(actualDateTime, intervalInMinutes).plusMinutes(intervalInMinutes)
	}

	/**
	 * <p>
	 * Fixes the start and end date within the specified interval to match the 
	 * the n-th of the interval-range where n is the maximum number of 
	 * interval-ranges which fits within the specified interval plus 1.
	 * The method uses {@link DateTimeConstants#FRIDAY} as reset csiDay-of-week
	 * for {@linkplain CsiAggregationInterval#WEEKLY weekly intervals}.
	 * </p>
	 * 
	 * @param timeFrameToFix 
	 *         The time frame to fix; not <code>null</code>.
	 * @param intervalRangeInMinutes
	 *         The range of one (and all other) {@linkplain 
	 *         CsiAggregationInterval interval}(s) within the time frame.
	 * @return The fixed time frame which start and ends is is set to fit the
	 *         interval range. The start could be earlier, the end later than 
	 *         the original time frame dates passes. The result is never 
	 *         <code>null</code>.
	 * @throws NullPointerException 
	 *         if at least one argument is <code>null</code>.
	 * @throws IllegalArgumentException 
	 *         if {@code intervalRangeInMinutes} < 1.
	 */
	public Interval fixTimeFrameToMatchIntervalRange(
				Interval timeFrameToFix, 
				int intervalRangeInMinutes) 
				throws NullPointerException, IllegalArgumentException {
					
		return new Interval(
				this.resetToStartOfActualInterval(
						timeFrameToFix.getStart(), 
						intervalRangeInMinutes),
				this.resetToStartOfActualInterval(
						timeFrameToFix.getEnd(), 
						intervalRangeInMinutes)
		);
	}

	/**
	 * <p>
	 * Adds the given {@link CsiAggregationInterval} to the given {@link DateTime}. For adding intervals "plus-methods" from JodaTime are used, so
	 * switches cause of daylight saving time are taken into consideration. 
	 * </p>
	 * @param toAddTo
	 * @param interval
	 * @return 
	 * @throws IllegalArgumentException if interval isn't one of {@link CsiAggregationInterval#HOURLY}, {@link CsiAggregationInterval#DAILY} or
	 * {@link CsiAggregationInterval#WEEKLY}.
	 */
	DateTime addOneInterval(DateTime toAddTo, Integer intervalInMinutes){
		if (intervalInMinutes == CsiAggregationInterval.HOURLY) {
			return toAddTo.plusHours(1)
		}else if (intervalInMinutes == CsiAggregationInterval.DAILY) {
			return toAddTo.plusDays(1)
		}else if (intervalInMinutes == CsiAggregationInterval.WEEKLY) {
			return toAddTo.plusWeeks(1)
		}else{
			throw new IllegalArgumentException("Unnknown interval: ${intervalInMinutes}")
		}
	}
	
	/**
	 * <p>
	 * Subtracts the given {@link CsiAggregationInterval} to the given {@link DateTime}. For subtracting intervals "minus-methods" from JodaTime are used, so
	 * switches cause of daylight saving time are taken into consideration.
	 * </p>
	 * @param toSubtractFrom {@link DateTime} from which interval should be subtracted.
	 * @param interval Interval in minutes to subtract from toSubtractFrom. 
	 * @return {@link DateTime} representing the timestamp intervalInMinutes before toSubtractFrom.   
	 * @throws IllegalArgumentException if interval isn't one of {@link CsiAggregationInterval#HOURLY}, {@link CsiAggregationInterval#DAILY} or
	 * {@link CsiAggregationInterval#WEEKLY}.
	 */
	DateTime subtractOneInterval(DateTime toSubtractFrom, Integer intervalInMinutes){
		if (intervalInMinutes == CsiAggregationInterval.HOURLY) {
			return toSubtractFrom.minusHours(1)
		}else if (intervalInMinutes == CsiAggregationInterval.DAILY) {
			return toSubtractFrom.minusDays(1)
		}else if (intervalInMinutes == CsiAggregationInterval.WEEKLY) {
			return toSubtractFrom.minusWeeks(1)
		}else{
			throw new IllegalArgumentException("Unknown interval: ${intervalInMinutes}")
		}
	}

	/**
	 * Returns an instance of {@link DateTime} with timezone UTC and value of now.
	 * @return Now as an instance of {@link DateTime} with timezone UTC.
	 */
	public DateTime getNowInUtc(){
		return new DateTime(DateTimeZone.UTC)
	}

    /**
     * Checks, whether given DateTime dateTime is within actual interval of size {@link #interval} in minutes.
     * @param dateTime
     *          Any DateTime which should get checked.
     * @param interval
     *          Interval in minutes.
     * @return Whether or not the given DateTime dateTime is within actual interval of size {@link #interval} in minutes.
     */
    Boolean isInActualInterval(DateTime dateTime, Integer interval) {
        DateTime startOfActualInterval = resetToStartOfActualInterval(new DateTime(), interval)
        DateTime startOfIntervalOfGivenDateTime = resetToStartOfActualInterval(dateTime, interval)
        return startOfActualInterval.isEqual(startOfIntervalOfGivenDateTime)
    }
}

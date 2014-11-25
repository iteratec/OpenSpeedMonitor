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

import de.iteratec.osm.report.chart.MeasuredValueUtilService

import static org.junit.Assert.*
import grails.test.mixin.TestFor

import org.joda.time.DateTime
import org.joda.time.DateTimeConstants
import org.joda.time.Interval
import org.junit.Before
import org.junit.Test

import de.iteratec.osm.report.chart.MeasuredValueInterval

/**
 * Test-suite of {@link de.iteratec.osm.report.chart.MeasuredValueUtilService}.
 */
@TestFor(MeasuredValueUtilService)
class MeasuredValueUtilServiceTests {

	MeasuredValueUtilService serviceUnderTest
	
	@Before
	void setUp() {
		serviceUnderTest=service;
	}
	
	@Test
    void testResetToStartOfActualInterval_HourlyReset() {
		DateTime dateToReset = new DateTime(2013, 1, 10, 13, 43, 12, 234);
		DateTime expectedResult = new DateTime(2013, 1, 10, 13, 0, 0, 0);
		
		DateTime currentResult = serviceUnderTest.resetToStartOfActualInterval(
				dateToReset, MeasuredValueInterval.HOURLY); 
		
		assertEquals(expectedResult, currentResult);
	}
	
	@Test
	void testResetToStartOfActualInterval_DailyReset() {
		DateTime dateToReset = new DateTime(2013, 1, 10, 13, 43, 12, 234);
		DateTime expectedResult = new DateTime(2013, 1, 10, 0, 0, 0, 0);
		
		DateTime currentResult = serviceUnderTest.resetToStartOfActualInterval(
				dateToReset, MeasuredValueInterval.DAILY);
		
		assertEquals(expectedResult, currentResult);
	}
	
	@Test
	void testResetToStartOfActualInterval_WeeklyReset() {
		DateTime dateToReset = new DateTime(2013, 1, 10, 13, 43, 12, 234);
		DateTime expectedResult = new DateTime(2013, 1, 4, 0, 0, 0, 0);
		
		DateTime currentResult = serviceUnderTest.resetToStartOfActualInterval(
				dateToReset, MeasuredValueInterval.WEEKLY);
		
		assertEquals(expectedResult, currentResult);
    }
	
	@Test
	void testResetToEndOfActualInterval_HourlyReset() {
		DateTime dateToReset = new DateTime(2013, 1, 10, 13, 43, 12);
		DateTime expectedResult = new DateTime(2013, 1, 10, 14, 0, 0, 0);
		
		DateTime currentResult = serviceUnderTest.resetToEndOfActualInterval(
			dateToReset, MeasuredValueInterval.HOURLY);
		
		assertEquals(expectedResult, currentResult);
	}
	
	@Test
	void testResetToEndOfActualInterval_DailyReset() {
		DateTime dateToReset = new DateTime(2013, 1, 10, 13, 43, 12);
		DateTime expectedResult = new DateTime(2013, 1, 11, 0, 0, 0, 0);
		
		DateTime currentResult = serviceUnderTest.resetToEndOfActualInterval(
			dateToReset, MeasuredValueInterval.DAILY);
		
		assertEquals(expectedResult, currentResult);
	}
	
	@Test
	void testResetToEndOfActualInterval_WeeklyReset() {
		DateTime dateToReset = new DateTime(2013, 1, 10, 13, 43, 12);
		DateTime expectedResult = new DateTime(2013, 1, 11, 0, 0, 0, 0);
		
		DateTime currentResult = serviceUnderTest.resetToEndOfActualInterval(
			dateToReset, MeasuredValueInterval.WEEKLY);
		
		assertEquals(expectedResult, currentResult);
	}
	
	
	@Test
	void testResetToStartOfActualInterval_WeeklyReset_Thursday() {
		DateTime dateToReset = new DateTime(2013, 9, 10, 13, 43, 12);
		DateTime expectedResult = new DateTime(2013, 9, 5, 0, 0, 0, 0);
		
		DateTime currentResult = serviceUnderTest.resetToStartOfActualInterval(
			dateToReset, MeasuredValueInterval.WEEKLY, 
			DateTimeConstants.THURSDAY);
		
		assertEquals(expectedResult, currentResult);
	}
	
	@Test
	void testResetToEndOfActualInterval_WeeklyReset_Thursday() {
		DateTime dateToReset = new DateTime(2013, 9, 10, 13, 43, 12);
		DateTime expectedResult = new DateTime(2013, 9, 13, 0, 0, 0, 0);
		
		DateTime currentResult = serviceUnderTest.resetToEndOfActualInterval(
			dateToReset, MeasuredValueInterval.WEEKLY, 
			DateTimeConstants.THURSDAY);
		
		assertEquals(expectedResult, currentResult);
	}
	
	@Test
	void testFixTimeFrameToMatchIntervalRange_WeeklyInterval() {
		DateTime startDate = new DateTime(2013, 6, 19, 15, 55, 00);
		DateTime endDate = new DateTime(2013, 9, 19, 15, 55, 00);
		
		DateTime expectedFixedStartDate = new DateTime(2013, 6, 14, 0, 0, 0, 0);
		DateTime expectedFixedEndDate = new DateTime(2013, 9, 20, 0, 0, 0, 0);
		
		Interval timeFrame = new Interval(startDate, endDate);
		
		Interval fixedTimeFrame = serviceUnderTest.fixTimeFrameToMatchIntervalRange(
			timeFrame, MeasuredValueInterval.WEEKLY);
		
		assertNotNull(fixedTimeFrame);
		assertEquals(expectedFixedStartDate, fixedTimeFrame.getStart());
		assertEquals(expectedFixedEndDate, fixedTimeFrame.getEnd());
	}
	
	@Test
	void testAddingIntervals(){
		//test-data before end of summertime
		DateTime tenDaysBeforeEndOfSummertime = new DateTime(2013, 10, 17, 0, 0, 0);
		DateTime threeDaysBeforeEndOfSummertime = new DateTime(2013, 10, 24, 0, 0, 0);
		DateTime twoDaysBeforeEndOfSummertime = new DateTime(2013, 10, 25, 0, 0, 0);
		DateTime midnightLastDayOfSummertime = new DateTime(2013, 10, 27, 0, 0, 0);
		DateTime midnightFirstDayAfterSummertime = new DateTime(2013, 10, 28, 0, 0, 0);
		DateTime midnightSecondDayAfterSummertime = new DateTime(2013, 10, 29, 0, 0, 0);
		DateTime twoHoursBeforeEndOfSummertime = new DateTime(2013, 10, 27, 1, 0, 0);
		DateTime oneHourBeforeEndOfSummertime = new DateTime(2013, 10, 27, 2, 0, 0);
		//test-data after end of summertime
		DateTime fourDaysAfterEndOfSummertime = new DateTime(2013, 10, 31, 0, 0, 0);
		DateTime elevenDaysAfterEndOfSummertime = new DateTime(2013, 11, 7, 0, 0, 0);
		DateTime noonLastDayOfSummertime = new DateTime(2013, 10, 27, 12, 0, 0);
		
		//adding a week
		assertEquals(threeDaysBeforeEndOfSummertime, serviceUnderTest.addOneInterval(tenDaysBeforeEndOfSummertime, MeasuredValueInterval.WEEKLY));
		assertEquals(fourDaysAfterEndOfSummertime, serviceUnderTest.addOneInterval(threeDaysBeforeEndOfSummertime, MeasuredValueInterval.WEEKLY));
		assertEquals(elevenDaysAfterEndOfSummertime, serviceUnderTest.addOneInterval(fourDaysAfterEndOfSummertime, MeasuredValueInterval.WEEKLY));
		//adding a day
		assertEquals(twoDaysBeforeEndOfSummertime, serviceUnderTest.addOneInterval(threeDaysBeforeEndOfSummertime, MeasuredValueInterval.DAILY));
		assertEquals(midnightFirstDayAfterSummertime, serviceUnderTest.addOneInterval(midnightLastDayOfSummertime, MeasuredValueInterval.DAILY));
		assertEquals(midnightSecondDayAfterSummertime, serviceUnderTest.addOneInterval(midnightFirstDayAfterSummertime, MeasuredValueInterval.DAILY));
		//adding an hour
		assertEquals(oneHourBeforeEndOfSummertime, serviceUnderTest.addOneInterval(twoHoursBeforeEndOfSummertime, MeasuredValueInterval.HOURLY));
		assertEquals(oneHourBeforeEndOfSummertime.getHourOfDay(), serviceUnderTest.addOneInterval(oneHourBeforeEndOfSummertime, MeasuredValueInterval.HOURLY).getHourOfDay());
		assertEquals(new DateTime(2013, 10, 27, 13, 0, 0), serviceUnderTest.addOneInterval(noonLastDayOfSummertime, MeasuredValueInterval.HOURLY));
		
	}
	
	@Test
	void testSubtractingIntervals(){
		//test-data before end of summertime
		DateTime tenDaysBeforeEndOfSummertime = new DateTime(2013, 10, 17, 0, 0, 0);
		DateTime threeDaysBeforeEndOfSummertime = new DateTime(2013, 10, 24, 0, 0, 0);
		DateTime twoDaysBeforeEndOfSummertime = new DateTime(2013, 10, 25, 0, 0, 0);
		DateTime midnightLastDayOfSummertime = new DateTime(2013, 10, 27, 0, 0, 0);
		DateTime midnightFirstDayAfterSummertime = new DateTime(2013, 10, 28, 0, 0, 0);
		DateTime midnightSecondDayAfterSummertime = new DateTime(2013, 10, 29, 0, 0, 0);
		DateTime twoHoursBeforeEndOfSummertime = new DateTime(2013, 10, 27, 1, 0, 0);
		DateTime oneHourBeforeEndOfSummertime = new DateTime(2013, 10, 27, 2, 0, 0);
		//test-data summertime
		DateTime exactlyEndOfSummertime = new DateTime(2013, 10, 27, 3, 0, 0);
		//test-data after end of summertime
		DateTime oneHourAfterEndOfSummertime = new DateTime(2013, 10, 27, 4, 0, 0);
		DateTime fourDaysAfterEndOfSummertime = new DateTime(2013, 10, 31, 0, 0, 0);
		DateTime elevenDaysAfterEndOfSummertime = new DateTime(2013, 11, 7, 0, 0, 0);
		DateTime noonLastDayOfSummertime = new DateTime(2013, 10, 27, 12, 0, 0);
		
		//subtracting a week
		assertEquals(tenDaysBeforeEndOfSummertime, serviceUnderTest.subtractOneInterval(threeDaysBeforeEndOfSummertime, MeasuredValueInterval.WEEKLY));
		assertEquals(threeDaysBeforeEndOfSummertime, serviceUnderTest.subtractOneInterval(fourDaysAfterEndOfSummertime, MeasuredValueInterval.WEEKLY));
		assertEquals(fourDaysAfterEndOfSummertime, serviceUnderTest.subtractOneInterval(elevenDaysAfterEndOfSummertime, MeasuredValueInterval.WEEKLY));
		//subtracting a day
		assertEquals(threeDaysBeforeEndOfSummertime, serviceUnderTest.subtractOneInterval(twoDaysBeforeEndOfSummertime, MeasuredValueInterval.DAILY));
		assertEquals(midnightLastDayOfSummertime, serviceUnderTest.subtractOneInterval(midnightFirstDayAfterSummertime, MeasuredValueInterval.DAILY));
		assertEquals(midnightFirstDayAfterSummertime, serviceUnderTest.subtractOneInterval(midnightSecondDayAfterSummertime, MeasuredValueInterval.DAILY));
		//subtracting an hour
		assertEquals(twoHoursBeforeEndOfSummertime, serviceUnderTest.subtractOneInterval(oneHourBeforeEndOfSummertime, MeasuredValueInterval.HOURLY));
		assertEquals(oneHourBeforeEndOfSummertime.getHourOfDay(), serviceUnderTest.subtractOneInterval(exactlyEndOfSummertime, MeasuredValueInterval.HOURLY).getHourOfDay());
		assertEquals(exactlyEndOfSummertime.getHourOfDay(), serviceUnderTest.subtractOneInterval(oneHourAfterEndOfSummertime, MeasuredValueInterval.HOURLY).getHourOfDay());
		
	}
	
	@Test
	void testCountIntervals(){
		DateTime tenDaysBeforeEndOfSummertime = new DateTime(2013, 10, 17, 0, 0, 0);
	}
}

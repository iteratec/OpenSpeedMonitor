/* 
* OpenSpeedMonitor (OSM)
* Copyright 2014 iteratec GmbH
* 
* Licensed under the Apache License, Version 2.0 (the "License")
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

import spock.lang.Specification

import grails.test.mixin.TestFor

import org.joda.time.DateTime
import org.joda.time.DateTimeConstants
import org.joda.time.Interval

/**
 * Test-suite of {@link CsiAggregationUtilService}.
 */
@TestFor(CsiAggregationUtilService)
class CsiAggregationUtilServiceTests extends Specification{

	CsiAggregationUtilService serviceUnderTest
	
	def setup() {
		serviceUnderTest=service
	}
	

    void "test resetToStartOfActualInterval with HourlyReset"() {
		given:
		DateTime dateToReset = new DateTime(2013, 1, 10, 13, 43, 12, 234)
		DateTime expectedResult = new DateTime(2013, 1, 10, 13, 0, 0, 0)

		when:
		DateTime currentResult = serviceUnderTest.resetToStartOfActualInterval(
				dateToReset, CsiAggregationInterval.HOURLY)

		then:
		expectedResult == currentResult
	}
	

	void "test resetToStartOfActualInterval with DailyReset"() {
		given:
		DateTime dateToReset = new DateTime(2013, 1, 10, 13, 43, 12, 234)
		DateTime expectedResult = new DateTime(2013, 1, 10, 0, 0, 0, 0)

		when:
		DateTime currentResult = serviceUnderTest.resetToStartOfActualInterval(
				dateToReset, CsiAggregationInterval.DAILY)

		then:
		expectedResult == currentResult
	}


	void "test resetToStartOfActualInterval with WeeklyReset"() {
		given:
		DateTime dateToReset = new DateTime(2013, 1, 10, 13, 43, 12, 234)
		DateTime expectedResult = new DateTime(2013, 1, 4, 0, 0, 0, 0)

		when:
		DateTime currentResult = serviceUnderTest.resetToStartOfActualInterval(
				dateToReset, CsiAggregationInterval.WEEKLY)
		then:
		expectedResult == currentResult
    }


	void testResetToEndOfActualInterval_HourlyReset() {
		given:
		DateTime dateToReset = new DateTime(2013, 1, 10, 13, 43, 12)
		DateTime expectedResult = new DateTime(2013, 1, 10, 14, 0, 0, 0)

		when:
		DateTime currentResult = serviceUnderTest.resetToEndOfActualInterval(
			dateToReset, CsiAggregationInterval.HOURLY)

		then:
		expectedResult == currentResult
	}


	void testResetToEndOfActualInterval_DailyReset() {
		given:
		DateTime dateToReset = new DateTime(2013, 1, 10, 13, 43, 12)
		DateTime expectedResult = new DateTime(2013, 1, 11, 0, 0, 0, 0)

		when:
		DateTime currentResult = serviceUnderTest.resetToEndOfActualInterval(
			dateToReset, CsiAggregationInterval.DAILY)
		then:
		expectedResult == currentResult
	}


	void testResetToEndOfActualInterval_WeeklyReset() {
		given:
		DateTime dateToReset = new DateTime(2013, 1, 10, 13, 43, 12)
		DateTime expectedResult = new DateTime(2013, 1, 11, 0, 0, 0, 0)

		when:
		DateTime currentResult = serviceUnderTest.resetToEndOfActualInterval(
			dateToReset, CsiAggregationInterval.WEEKLY)

		then:
		expectedResult == currentResult
	}



	void testResetToStartOfActualInterval_WeeklyReset_Thursday() {
        given:
		DateTime dateToReset = new DateTime(2013, 9, 10, 13, 43, 12)
		DateTime expectedResult = new DateTime(2013, 9, 5, 0, 0, 0, 0)

		when:
		DateTime currentResult = serviceUnderTest.resetToStartOfActualInterval(
			dateToReset, CsiAggregationInterval.WEEKLY,
			DateTimeConstants.THURSDAY)

		then:
		expectedResult == currentResult
	}


	void testResetToEndOfActualInterval_WeeklyReset_Thursday() {
		given:
		DateTime dateToReset = new DateTime(2013, 9, 10, 13, 43, 12)
		DateTime expectedResult = new DateTime(2013, 9, 13, 0, 0, 0, 0)

		when:
		DateTime currentResult = serviceUnderTest.resetToEndOfActualInterval(
			dateToReset, CsiAggregationInterval.WEEKLY,
			DateTimeConstants.THURSDAY)

		then:
		expectedResult == currentResult
	}


	void testFixTimeFrameToMatchIntervalRange_WeeklyInterval() {
		given:
		DateTime startDate = new DateTime(2013, 6, 19, 15, 55, 00)
		DateTime endDate = new DateTime(2013, 9, 19, 15, 55, 00)

		DateTime expectedFixedStartDate = new DateTime(2013, 6, 14, 0, 0, 0, 0)
		DateTime expectedFixedEndDate = new DateTime(2013, 9, 13, 0, 0, 0, 0)

		Interval timeFrame = new Interval(startDate, endDate)

		when:
		Interval fixedTimeFrame = serviceUnderTest.fixTimeFrameToMatchIntervalRange(
			timeFrame, CsiAggregationInterval.WEEKLY)

		then:
		fixedTimeFrame
		expectedFixedStartDate == fixedTimeFrame.getStart()
		expectedFixedEndDate == fixedTimeFrame.getEnd()
	}


	void "test adding hour intervals"(){
		given: "a couple dates around end of summertime"
		DateTime midnightLastDayOfSummertime = new DateTime(2013, 10, 27, 0, 0, 0)
		DateTime twoHoursBeforeEndOfSummertime = midnightLastDayOfSummertime.plusHours(1)
		DateTime oneHourBeforeEndOfSummertime = midnightLastDayOfSummertime.plusHours(2)
		DateTime noonLastDayOfSummertime = new DateTime(2013, 10, 27, 12, 0, 0)
		DateTime oneHourAfterNoonLastDayOfSummertime = noonLastDayOfSummertime.plusHours(1)
		expect:
		oneHourBeforeEndOfSummertime == serviceUnderTest.addOneInterval(twoHoursBeforeEndOfSummertime, CsiAggregationInterval.HOURLY)
		oneHourBeforeEndOfSummertime.getHourOfDay() == serviceUnderTest.addOneInterval(oneHourBeforeEndOfSummertime, CsiAggregationInterval.HOURLY).getHourOfDay()
		oneHourAfterNoonLastDayOfSummertime == serviceUnderTest.addOneInterval(noonLastDayOfSummertime, CsiAggregationInterval.HOURLY)
	}

	void "test adding week intervals"(){
		given: "a couple dates around summertime"
		DateTime midnightLastDayOfSummertime = new DateTime(2013, 10, 27, 0, 0, 0)
		DateTime threeDaysBeforeEndOfSummertime = midnightLastDayOfSummertime.minusDays(3)
		DateTime tenDaysBeforeEndOfSummertime = midnightLastDayOfSummertime.minusDays(10)
		DateTime fourDaysAfterEndOfSummertime = midnightLastDayOfSummertime.plusDays(4)
		DateTime elevenDaysAfterEndOfSummertime = midnightLastDayOfSummertime.plusDays(11)

		expect:
		threeDaysBeforeEndOfSummertime == serviceUnderTest.addOneInterval(tenDaysBeforeEndOfSummertime, CsiAggregationInterval.WEEKLY)
		fourDaysAfterEndOfSummertime == serviceUnderTest.addOneInterval(threeDaysBeforeEndOfSummertime, CsiAggregationInterval.WEEKLY)
		elevenDaysAfterEndOfSummertime == serviceUnderTest.addOneInterval(fourDaysAfterEndOfSummertime, CsiAggregationInterval.WEEKLY)
	}


	void "test adding day intervals"(){
		given: "a couple dates around summertime"
		DateTime midnightLastDayOfSummertime = new DateTime(2013, 10, 27, 0, 0, 0)
		DateTime threeDaysBeforeEndOfSummertime = midnightLastDayOfSummertime.minusDays(3)
		DateTime twoDaysBeforeEndOfSummertime = midnightLastDayOfSummertime.minusDays(2)
		DateTime midnightFirstDayAfterSummertime = midnightLastDayOfSummertime.plusDays(1)
		DateTime midnightSecondDayAfterSummertime = midnightLastDayOfSummertime.plusDays(2)

		expect:
		twoDaysBeforeEndOfSummertime == serviceUnderTest.addOneInterval(threeDaysBeforeEndOfSummertime, CsiAggregationInterval.DAILY)
		midnightFirstDayAfterSummertime == serviceUnderTest.addOneInterval(midnightLastDayOfSummertime, CsiAggregationInterval.DAILY)
		midnightSecondDayAfterSummertime == serviceUnderTest.addOneInterval(midnightFirstDayAfterSummertime, CsiAggregationInterval.DAILY)
	}

	void "test subtracting week intervals"(){
		given: "a couple days around end of summertime"
		DateTime midnightLastDayOfSummertime = new DateTime(2013, 10, 27, 0, 0, 0)
		DateTime threeDaysBeforeEndOfSummertime = midnightLastDayOfSummertime.minusDays(3)
		DateTime tenDaysBeforeEndOfSummertime = midnightLastDayOfSummertime.minusDays(10)
		DateTime fourDaysAfterEndOfSummertime = midnightLastDayOfSummertime.plusDays(4)
		DateTime elevenDaysAfterEndOfSummertime = midnightLastDayOfSummertime.plusDays(11)

		expect:
		tenDaysBeforeEndOfSummertime == serviceUnderTest.subtractOneInterval(threeDaysBeforeEndOfSummertime, CsiAggregationInterval.WEEKLY)
		threeDaysBeforeEndOfSummertime == serviceUnderTest.subtractOneInterval(fourDaysAfterEndOfSummertime, CsiAggregationInterval.WEEKLY)
		fourDaysAfterEndOfSummertime == serviceUnderTest.subtractOneInterval(elevenDaysAfterEndOfSummertime, CsiAggregationInterval.WEEKLY)
	}

	void "test subtracting day intervals"(){
		given: "a couple dates around end of summertime"
		DateTime midnightLastDayOfSummertime = new DateTime(2013, 10, 27, 0, 0, 0)
		DateTime threeDaysBeforeEndOfSummertime = midnightLastDayOfSummertime.minusDays(3)
		DateTime twoDaysBeforeEndOfSummertime = midnightLastDayOfSummertime.minusDays(2)
		DateTime midnightFirstDayAfterSummertime = midnightLastDayOfSummertime.plusDays(1)
		DateTime midnightSecondDayAfterSummertime = midnightLastDayOfSummertime.plusDays(2)

		expect:
		threeDaysBeforeEndOfSummertime == serviceUnderTest.subtractOneInterval(twoDaysBeforeEndOfSummertime, CsiAggregationInterval.DAILY)
		midnightLastDayOfSummertime == serviceUnderTest.subtractOneInterval(midnightFirstDayAfterSummertime, CsiAggregationInterval.DAILY)
		midnightFirstDayAfterSummertime == serviceUnderTest.subtractOneInterval(midnightSecondDayAfterSummertime, CsiAggregationInterval.DAILY)
	}

	void "test subtracting hours"(){
		given: "a couple dates around end of summertime"
		DateTime midnightLastDayOfSummertime = new DateTime(2013, 10, 27, 0, 0, 0)
		DateTime twoHoursBeforeEndOfSummertime = midnightLastDayOfSummertime.plusHours(1)
		DateTime oneHourBeforeEndOfSummertime = midnightLastDayOfSummertime.plusHours(2)
		DateTime exactlyEndOfSummertime = midnightLastDayOfSummertime.plusHours(3)
		DateTime oneHourAfterEndOfSummertime = midnightLastDayOfSummertime.plusHours(4)

		expect:
//		subtracting an hour
		twoHoursBeforeEndOfSummertime == serviceUnderTest.subtractOneInterval(oneHourBeforeEndOfSummertime, CsiAggregationInterval.HOURLY)
		oneHourBeforeEndOfSummertime.getHourOfDay() == serviceUnderTest.subtractOneInterval(exactlyEndOfSummertime, CsiAggregationInterval.HOURLY).getHourOfDay()
		exactlyEndOfSummertime.getHourOfDay() == serviceUnderTest.subtractOneInterval(oneHourAfterEndOfSummertime, CsiAggregationInterval.HOURLY).getHourOfDay()
	}



    void testIsInActualInterval() {
		given:
        Integer hourly = CsiAggregationInterval.HOURLY
        Integer daily = CsiAggregationInterval.DAILY
        Integer weekly = CsiAggregationInterval.WEEKLY
        DateTime now = new DateTime()

        expect:
        serviceUnderTest.isInActualInterval(now, hourly)
        !serviceUnderTest.isInActualInterval(now.minusMinutes(hourly + 1), hourly)
        !serviceUnderTest.isInActualInterval(now.minusMinutes(daily), hourly)
        !serviceUnderTest.isInActualInterval(now.minusMinutes(weekly), hourly)

        serviceUnderTest.isInActualInterval(now, daily)
        !serviceUnderTest.isInActualInterval(now.minusMinutes(daily + 1), daily)
        !serviceUnderTest.isInActualInterval(now.minusMinutes(weekly), daily)

        serviceUnderTest.isInActualInterval(now, weekly)
        !serviceUnderTest.isInActualInterval(now.minusMinutes(weekly + 1), weekly)

    }
}

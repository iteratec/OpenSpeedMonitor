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

import grails.test.mixin.TestFor
import org.joda.time.DateTime
import org.joda.time.DateTimeConstants
import org.joda.time.Interval
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import static de.iteratec.osm.report.chart.CsiAggregationInterval.*

/**
 * Test-suite of {@link CsiAggregationUtilService}.
 */
@TestFor(CsiAggregationUtilService)
class CsiAggregationUtilServiceTests extends Specification {

    @Shared
    DateTime midnightLastDayOfSummertime = new DateTime(2013, 10, 27, 0, 0, 0)
    @Shared
    DateTime twoHoursBeforeEndOfSummertime = midnightLastDayOfSummertime.plusHours(1)
    @Shared
    DateTime oneHourBeforeEndOfSummertime = midnightLastDayOfSummertime.plusHours(2)
    @Shared
    DateTime noonLastDayOfSummertime = new DateTime(2013, 10, 27, 12, 0, 0)
    @Shared
    DateTime oneHourAfterNoonLastDayOfSummertime = noonLastDayOfSummertime.plusHours(1)
    @Shared
    DateTime threeDaysBeforeEndOfSummertime = midnightLastDayOfSummertime.minusDays(3)
    @Shared
    DateTime tenDaysBeforeEndOfSummertime = midnightLastDayOfSummertime.minusDays(10)
    @Shared
    DateTime fourDaysAfterEndOfSummertime = midnightLastDayOfSummertime.plusDays(4)
    @Shared
    DateTime elevenDaysAfterEndOfSummertime = midnightLastDayOfSummertime.plusDays(11)
    @Shared
    DateTime twoDaysBeforeEndOfSummertime = midnightLastDayOfSummertime.minusDays(2)
    @Shared
    DateTime midnightFirstDayAfterSummertime = midnightLastDayOfSummertime.plusDays(1)
    @Shared
    DateTime midnightSecondDayAfterSummertime = midnightLastDayOfSummertime.plusDays(2)
    @Shared
    DateTime exactlyEndOfSummertime = midnightLastDayOfSummertime.plusHours(3)
    @Shared
    DateTime oneHourAfterEndOfSummertime = midnightLastDayOfSummertime.plusHours(4)
    CsiAggregationUtilService serviceUnderTest

    def setup() {
        serviceUnderTest = service
    }


    @Unroll
    void "test reset to start of actual interval with Interval: #interval"() {
        when:
        DateTime dateToReset = new DateTime(2013, 1, 10, 13, 43, 12, 234)

        then:
        serviceUnderTest.resetToStartOfActualInterval(dateToReset, interval) == expect

        where:
        interval || expect
        HOURLY   || new DateTime(2013, 1, 10, 13, 0, 0, 0)
        DAILY    || new DateTime(2013, 1, 10, 0, 0, 0, 0)
        WEEKLY   || new DateTime(2013, 1, 4, 0, 0, 0, 0)
    }

    @Unroll
    void "test reset to end of actual interval withInterval: #interval"() {
        when:
        DateTime dateToReset = new DateTime(2013, 1, 10, 13, 43, 12)

        then:
        serviceUnderTest.resetToEndOfActualInterval(dateToReset, interval) == expect

        where:
        interval || expect
        HOURLY   || new DateTime(2013, 1, 10, 14, 0, 0, 0)
        DAILY    || new DateTime(2013, 1, 11, 0, 0, 0)
        WEEKLY   || new DateTime(2013, 1, 11, 0, 0, 0, 0)
    }

    void "reset to start of actual interval with WeeklyReset on Thursday"() {
        given:
        DateTime dateToReset = new DateTime(2013, 9, 10, 13, 43, 12)
        DateTime expectedResult = new DateTime(2013, 9, 5, 0, 0, 0, 0)

        when:
        DateTime currentResult = serviceUnderTest.resetToStartOfActualInterval(dateToReset, WEEKLY, DateTimeConstants.THURSDAY)

        then:
        expectedResult == currentResult
    }


    void "reset to end of actual interval with WeeklyReset on Thursday"() {
        given:
        DateTime dateToReset = new DateTime(2013, 9, 10, 13, 43, 12)
        DateTime expectedResult = new DateTime(2013, 9, 13, 0, 0, 0, 0)

        when:
        DateTime currentResult = serviceUnderTest.resetToEndOfActualInterval(dateToReset, WEEKLY, DateTimeConstants.THURSDAY)

        then:
        expectedResult == currentResult
    }


    void "fixTimeFrameToMatchIntervalRange with WeeklyInterval"() {
        given:
        DateTime startDate = new DateTime(2013, 6, 19, 15, 55, 00)
        DateTime endDate = new DateTime(2013, 9, 19, 15, 55, 00)

        DateTime expectedFixedStartDate = new DateTime(2013, 6, 14, 0, 0, 0, 0)
        DateTime expectedFixedEndDate = new DateTime(2013, 9, 13, 0, 0, 0, 0)

        Interval timeFrame = new Interval(startDate, endDate)

        when:
        Interval fixedTimeFrame = serviceUnderTest.fixTimeFrameToMatchIntervalRange(timeFrame, WEEKLY)

        then:
        fixedTimeFrame
        expectedFixedStartDate == fixedTimeFrame.getStart()
        expectedFixedEndDate == fixedTimeFrame.getEnd()
    }

    @Unroll
    void "test adding intervals of Interval:#interval"() {
        expect:
        serviceUnderTest.addOneInterval(date, interval) == expectedResult

        where:
        date                            | interval || expectedResult
        twoHoursBeforeEndOfSummertime   | HOURLY   || oneHourBeforeEndOfSummertime
        tenDaysBeforeEndOfSummertime    | WEEKLY   || threeDaysBeforeEndOfSummertime
        fourDaysAfterEndOfSummertime    | WEEKLY   || elevenDaysAfterEndOfSummertime
        threeDaysBeforeEndOfSummertime  | DAILY    || twoDaysBeforeEndOfSummertime
        midnightFirstDayAfterSummertime | DAILY    || midnightSecondDayAfterSummertime
    }

    @Unroll
    void "test adding hour interval and enter summertime"() {
        expect:
        serviceUnderTest.addOneInterval(date, interval) == expectedResult

        where:
        date                           | interval || expectedResult
        noonLastDayOfSummertime        | HOURLY   || oneHourAfterNoonLastDayOfSummertime
        threeDaysBeforeEndOfSummertime | WEEKLY   || fourDaysAfterEndOfSummertime
        midnightLastDayOfSummertime    | DAILY    || midnightFirstDayAfterSummertime
    }

    @Unroll
    void "test subtracting intervals of Interval:#interval"() {
        expect:
        serviceUnderTest.subtractOneInterval(date, interval) == expectedResult

        where:
        date                             | interval || expectedResult
        threeDaysBeforeEndOfSummertime   | WEEKLY   || tenDaysBeforeEndOfSummertime
        elevenDaysAfterEndOfSummertime   | WEEKLY   || fourDaysAfterEndOfSummertime
        twoDaysBeforeEndOfSummertime     | DAILY    || threeDaysBeforeEndOfSummertime
        midnightSecondDayAfterSummertime | DAILY    || midnightFirstDayAfterSummertime
        oneHourBeforeEndOfSummertime     | HOURLY   || twoHoursBeforeEndOfSummertime
        oneHourAfterEndOfSummertime      | HOURLY   || exactlyEndOfSummertime
    }

    @Unroll
    void "test subtracting hour interval of Interval:#interval and leave summertime"() {
        expect:
        serviceUnderTest.subtractOneInterval(date, interval) == expectedResult

        where:
        date                            | interval || expectedResult
        fourDaysAfterEndOfSummertime    | WEEKLY   || threeDaysBeforeEndOfSummertime
        midnightFirstDayAfterSummertime | DAILY    || midnightLastDayOfSummertime
        exactlyEndOfSummertime          | HOURLY   || oneHourBeforeEndOfSummertime
    }

    void testIsInActualInterval() {
        given:
        DateTime now = new DateTime()

        expect:
        serviceUnderTest.isInActualInterval(now, HOURLY)
        !serviceUnderTest.isInActualInterval(now.minusMinutes(HOURLY + 1), HOURLY)
        !serviceUnderTest.isInActualInterval(now.minusMinutes(DAILY), HOURLY)
        !serviceUnderTest.isInActualInterval(now.minusMinutes(WEEKLY), HOURLY)

        serviceUnderTest.isInActualInterval(now, DAILY)
        !serviceUnderTest.isInActualInterval(now.minusMinutes(DAILY + 1), DAILY)
        !serviceUnderTest.isInActualInterval(now.minusMinutes(WEEKLY), DAILY)

        serviceUnderTest.isInActualInterval(now, WEEKLY)
        !serviceUnderTest.isInActualInterval(now.minusMinutes(WEEKLY + 1), WEEKLY)

    }
}

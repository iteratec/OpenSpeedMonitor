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

package de.iteratec.osm.csi

import de.iteratec.osm.report.chart.CsiAggregationInterval
import de.iteratec.osm.report.chart.CsiAggregationUtilService
import de.iteratec.osm.util.DateValueConverter
import de.iteratec.osm.util.DoubleValueConverter
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.Interval
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter

import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.*
import static org.junit.Assert.assertTrue

import grails.test.mixin.*
import grails.test.mixin.support.*
import org.junit.*

/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
@TestMixin(GrailsUnitTestMixin)
@TestFor(CsiDashboardController)
class CsiDashboardControllerActualIntervalSpec{

    public static final DateTimeFormatter FORMATTER_ddMMyyyy = DateTimeFormat.forPattern("dd.MM.yyyy")
    public static final DateTimeFormatter FORMATTER_ddMMyyyyhhmm = DateTimeFormat.forPattern("dd.MM.yyyy hh:mm")
    static CsiDashboardShowAllCommand command

    CsiDashboardController controllerUnderTest



    void setUp() {

        defineBeans {
            csiAggregationUtilService(CsiAggregationUtilService)
            doubleValueConverter(DoubleValueConverter)
            dateValueConverter(DateValueConverter)
        }
        // The controller under test:
        controllerUnderTest = controller;

        //Mocks
        command = new CsiDashboardShowAllCommand()
//        command.csiAggregationUtilService = new CsiAggregationUtilService()
        command.csiAggregationUtilService = grailsApplication.mainContext.getBean('csiAggregationUtilService')
    }

// daily page ////////////////////////////////////////////////////////////////////////////////////////////////////

    @Test
    public void testIncludingActualInterval_DailyPageAggregation() {
        // request args important for this test
        DateTime toDateExpected = new DateTime()
        DateTime fromDateExpected = toDateExpected.minusDays(14)

        params.from = FORMATTER_ddMMyyyy.print(fromDateExpected);
        params.fromHour = '12:00'
        params.to = FORMATTER_ddMMyyyy.print(toDateExpected);
        params.toHour = '12:00'

        params.aggrGroupAndInterval = CsiDashboardController.DAILY_AGGR_GROUP_PAGE
        params.includeInterval = true

        // request args necessary to validate the command object
        params.selectedTimeFrameInterval = 0
        params.selectedFolder = '1'
        params.selectedPages = ['1', '5']
        params.selectedMeasuredEventIds = ['7', '8', '9']
        params.selectedBrowsers = '2'
        params.selectedLocations = '17'
        params.csiTypeDocComplete = true
        params.debug = false
        params.setToHour = false
        params.setFromHour = false

        // Create and fill the command:
        controllerUnderTest.bindData(command, params)

        // Verification:

        assertTrue(command.validate())

        Interval interval = command.receiveSelectedTimeFrame()
        DateTime start = interval.getStart()
        DateTime end = interval.getEnd()

        assertThat(
                FORMATTER_ddMMyyyyhhmm.print(start),
                is(FORMATTER_ddMMyyyyhhmm.print(fromDateExpected.withHourOfDay(12).withMinuteOfHour(0)))
        )
        assertThat(
                FORMATTER_ddMMyyyyhhmm.print(end),
                is(FORMATTER_ddMMyyyyhhmm.print(toDateExpected.withHourOfDay(12).withMinuteOfHour(0)))
        )
    }

    @Test
    public void testExcludingActualInterval_DailyPageAggregation() {
        // request args important for this test
        DateTime toDate = new DateTime()
        DateTime toDateExpected = toDate.minusMinutes(CsiAggregationInterval.DAILY)
        params.to = FORMATTER_ddMMyyyy.print(toDate);
        DateTime fromDateExpected = toDate.minusDays(14)
        params.from = FORMATTER_ddMMyyyy.print(fromDateExpected);

        params.aggrGroupAndInterval = CsiDashboardController.DAILY_AGGR_GROUP_PAGE
        params.includeInterval = false
        params.fromHour = '12:00'
        params.toHour = '12:00'

        // request args necessary to validate the command object
        params.selectedTimeFrameInterval = 0
        params.selectedFolder = '1'
        params.selectedPages = ['1', '5']
        params.selectedMeasuredEventIds = ['7', '8', '9']
        params.selectedBrowsers = '2'
        params.selectedLocations = '17'
        params.csiTypeDocComplete = true
        params.debug = false
        params.setToHour = false
        params.setFromHour = false

        // Create and fill the command
        controllerUnderTest.bindData(command, params)

        // Verification

        assertTrue(command.validate())

        Interval interval = command.receiveSelectedTimeFrame()
        DateTime start = interval.getStart()
        DateTime end = interval.getEnd()

        assertThat(
                FORMATTER_ddMMyyyyhhmm.print(start),
                is(FORMATTER_ddMMyyyyhhmm.print(fromDateExpected.withHourOfDay(12).withMinuteOfHour(0)))
        )
        assertThat(
                FORMATTER_ddMMyyyyhhmm.print(end),
                is(FORMATTER_ddMMyyyyhhmm.print(toDateExpected.withHourOfDay(12).withMinuteOfHour(0)))
        )

    }

    @Test
    public void testExcludingActualIntervalForSmallChosenInterval_DailyPageAggregation() {
        // request args important for this test
        DateTime toDate = new DateTime(2015, 4, 20, 5, 0, 0, DateTimeZone.UTC)
        DateTime toDateExpected = toDate.minusMinutes(CsiAggregationInterval.DAILY)
        params.to = FORMATTER_ddMMyyyy.print(toDate);
        DateTime fromDate = toDate.minusHours(10)
        DateTime fromDateExpected = fromDate.minusMinutes(CsiAggregationInterval.DAILY)
        params.from = FORMATTER_ddMMyyyy.print(fromDate);

        params.aggrGroupAndInterval = CsiDashboardController.DAILY_AGGR_GROUP_PAGE
        params.includeInterval = false
        params.fromHour = '12:00'
        params.toHour = '12:00'

        // request args necessary to validate the command object
        params.selectedTimeFrameInterval = 0
        params.selectedFolder = '1'
        params.selectedPages = ['1', '5']
        params.selectedMeasuredEventIds = ['7', '8', '9']
        params.selectedBrowsers = '2'
        params.selectedLocations = '17'
        params.csiTypeDocComplete = true
        params.debug = false
        params.setToHour = false
        params.setFromHour = false

        // Create and fill the command
        command.csiAggregationUtilService.metaClass.isInActualInterval = { DateTime dateTime, Integer interval ->
            //should be true here to simulate fix interval as actual
            return true
        }
        controllerUnderTest.bindData(command, params)

        // Verification

        assertTrue(command.validate())

        Interval interval = command.receiveSelectedTimeFrame()
        DateTime start = interval.getStart()
        DateTime end = interval.getEnd()

        assertThat(
                FORMATTER_ddMMyyyyhhmm.print(start),
                is(FORMATTER_ddMMyyyyhhmm.print(fromDateExpected.withHourOfDay(12).withMinuteOfHour(0)))
        )
        assertThat(
                FORMATTER_ddMMyyyyhhmm.print(end),
                is(FORMATTER_ddMMyyyyhhmm.print(toDateExpected.withHourOfDay(12).withMinuteOfHour(0)))
        )

    }

    @Test
    public void testIncludingLastIntervalIfActualIntervalIsNotInChosenInterval_DailyPageAggregation() {
        // request args important for this test
        DateTime toDateExpected = new DateTime().minusDays(2)
        params.to = FORMATTER_ddMMyyyy.print(toDateExpected);
        DateTime fromDateExpected = toDateExpected.minusDays(14)
        params.from = FORMATTER_ddMMyyyy.print(fromDateExpected);

        params.aggrGroupAndInterval = CsiDashboardController.DAILY_AGGR_GROUP_PAGE
        params.includeInterval = false
        params.fromHour = '12:00'
        params.toHour = '12:00'

        // request args to validate the command object
        params.selectedTimeFrameInterval = 0
        params.selectedFolder = '1'
        params.selectedPages = ['1', '5']
        params.selectedMeasuredEventIds = ['7', '8', '9']
        params.selectedBrowsers = '2'
        params.selectedLocations = '17'
        params.csiTypeDocComplete = true
        params.debug = false
        params.setToHour = false
        params.setFromHour = false
        // Create and fill the command
        controllerUnderTest.bindData(command, params)

        // Verification
        assertTrue(command.validate())

        Interval interval = command.receiveSelectedTimeFrame()
        DateTime start = interval.getStart()
        DateTime end = interval.getEnd()

        assertThat(
                FORMATTER_ddMMyyyyhhmm.print(start),
                is(FORMATTER_ddMMyyyyhhmm.print(fromDateExpected.withHourOfDay(12).withMinuteOfHour(0)))
        )
        assertThat(
                FORMATTER_ddMMyyyyhhmm.print(end),
                is(FORMATTER_ddMMyyyyhhmm.print(toDateExpected.withHourOfDay(12).withMinuteOfHour(0)))
        )
    }

    // daily shop ////////////////////////////////////////////////////////////////////////////////////////////////////

    @Test
    public void testIncludingActualInterval_DailyShopAggregation() {
        // request args important for this test
        DateTime toDateExpected = new DateTime()
        DateTime fromDateExpected = toDateExpected.minusDays(14)

        params.from = FORMATTER_ddMMyyyy.print(fromDateExpected);
        params.fromHour = '12:00'
        params.to = FORMATTER_ddMMyyyy.print(toDateExpected);
        params.toHour = '12:00'

        params.aggrGroupAndInterval = CsiDashboardController.DAILY_AGGR_GROUP_SHOP
        params.includeInterval = true

        // request args necessary to validate the command object
        params.selectedTimeFrameInterval = 0
        params.selectedFolder = '1'
        params.selectedPages = ['1', '5']
        params.selectedMeasuredEventIds = ['7', '8', '9']
        params.selectedBrowsers = '2'
        params.selectedLocations = '17'
        params.csiTypeDocComplete = true
        params.debug = false
        params.setToHour = false
        params.setFromHour = false

        // Create and fill the command:
        controllerUnderTest.bindData(command, params)

        // Verification:

        assertTrue(command.validate())

        Interval interval = command.receiveSelectedTimeFrame()
        DateTime start = interval.getStart()
        DateTime end = interval.getEnd()

        assertThat(
                FORMATTER_ddMMyyyyhhmm.print(start),
                is(FORMATTER_ddMMyyyyhhmm.print(fromDateExpected.withHourOfDay(12).withMinuteOfHour(0)))
        )
        assertThat(
                FORMATTER_ddMMyyyyhhmm.print(end),
                is(FORMATTER_ddMMyyyyhhmm.print(toDateExpected.withHourOfDay(12).withMinuteOfHour(0)))
        )
    }

    @Test
    public void testExcludingActualInterval_DailyShopAggregation() {
        // request args important for this test
        DateTime toDate = new DateTime()
        DateTime toDateExpected = toDate.minusMinutes(CsiAggregationInterval.DAILY)
        params.to = FORMATTER_ddMMyyyy.print(toDate);
        DateTime fromDateExpected = toDate.minusDays(14)
        params.from = FORMATTER_ddMMyyyy.print(fromDateExpected);

        params.aggrGroupAndInterval = CsiDashboardController.DAILY_AGGR_GROUP_SHOP
        params.includeInterval = false
        params.fromHour = '12:00'
        params.toHour = '12:00'

        // request args necessary to validate the command object
        params.selectedTimeFrameInterval = 0
        params.selectedFolder = '1'
        params.selectedPages = ['1', '5']
        params.selectedMeasuredEventIds = ['7', '8', '9']
        params.selectedBrowsers = '2'
        params.selectedLocations = '17'
        params.csiTypeDocComplete = true
        params.debug = false
        params.setToHour = false
        params.setFromHour = false

        // Create and fill the command
        controllerUnderTest.bindData(command, params)

        // Verification

        assertTrue(command.validate())

        Interval interval = command.receiveSelectedTimeFrame()
        DateTime start = interval.getStart()
        DateTime end = interval.getEnd()

        assertThat(
                FORMATTER_ddMMyyyyhhmm.print(start),
                is(FORMATTER_ddMMyyyyhhmm.print(fromDateExpected.withHourOfDay(12).withMinuteOfHour(0)))
        )
        assertThat(
                FORMATTER_ddMMyyyyhhmm.print(end),
                is(FORMATTER_ddMMyyyyhhmm.print(toDateExpected.withHourOfDay(12).withMinuteOfHour(0)))
        )

    }

    @Test
    public void testExcludingActualIntervalForSmallChosenInterval_DailyShopAggregation() {
        // request args important for this test
        DateTime toDate = new DateTime(2015, 4, 20, 5, 0, 0, DateTimeZone.UTC)
        DateTime toDateExpected = toDate.minusMinutes(CsiAggregationInterval.DAILY)
        params.to = FORMATTER_ddMMyyyy.print(toDate);
        DateTime fromDate = toDate.minusHours(10)
        DateTime fromDateExpected = fromDate.minusMinutes(CsiAggregationInterval.DAILY)
        params.from = FORMATTER_ddMMyyyy.print(fromDate);

        params.aggrGroupAndInterval = CsiDashboardController.DAILY_AGGR_GROUP_SHOP
        params.includeInterval = false
        params.fromHour = '12:00'
        params.toHour = '12:00'

        // request args necessary to validate the command object
        params.selectedTimeFrameInterval = 0
        params.selectedFolder = '1'
        params.selectedPages = ['1', '5']
        params.selectedMeasuredEventIds = ['7', '8', '9']
        params.selectedBrowsers = '2'
        params.selectedLocations = '17'
        params.csiTypeDocComplete = true
        params.debug = false
        params.setToHour = false
        params.setFromHour = false

        // Create and fill the command
        command.csiAggregationUtilService.metaClass.isInActualInterval = { DateTime dateTime, Integer interval ->
            //should be true here to simulate fix interval as actual
            return true
        }
        controllerUnderTest.bindData(command, params)

        // Verification

        assertTrue(command.validate())

        Interval interval = command.receiveSelectedTimeFrame()
        DateTime start = interval.getStart()
        DateTime end = interval.getEnd()

        assertThat(
                FORMATTER_ddMMyyyyhhmm.print(start),
                is(FORMATTER_ddMMyyyyhhmm.print(fromDateExpected.withHourOfDay(12).withMinuteOfHour(0)))
        )
        assertThat(
                FORMATTER_ddMMyyyyhhmm.print(end),
                is(FORMATTER_ddMMyyyyhhmm.print(toDateExpected.withHourOfDay(12).withMinuteOfHour(0)))
        )

    }

    @Test
    public void testIncludingLastIntervalIfActualIntervalIsNotInChosenInterval_DailyShopAggregation() {
        // request args important for this test
        DateTime toDateExpected = new DateTime().minusDays(2)
        params.to = FORMATTER_ddMMyyyy.print(toDateExpected);
        DateTime fromDateExpected = toDateExpected.minusDays(14)
        params.from = FORMATTER_ddMMyyyy.print(fromDateExpected);

        params.aggrGroupAndInterval = CsiDashboardController.DAILY_AGGR_GROUP_SHOP
        params.includeInterval = false
        params.fromHour = '12:00'
        params.toHour = '12:00'

        // request args to validate the command object
        params.selectedTimeFrameInterval = 0
        params.selectedFolder = '1'
        params.selectedPages = ['1', '5']
        params.selectedMeasuredEventIds = ['7', '8', '9']
        params.selectedBrowsers = '2'
        params.selectedLocations = '17'
        params.csiTypeDocComplete = true
        params.debug = false
        params.setToHour = false
        params.setFromHour = false

        // Create and fill the command
        controllerUnderTest.bindData(command, params)

        // Verification
        assertTrue(command.validate())

        Interval interval = command.receiveSelectedTimeFrame()
        DateTime start = interval.getStart()
        DateTime end = interval.getEnd()

        assertThat(
                FORMATTER_ddMMyyyyhhmm.print(start),
                is(FORMATTER_ddMMyyyyhhmm.print(fromDateExpected.withHourOfDay(12).withMinuteOfHour(0)))
        )
        assertThat(
                FORMATTER_ddMMyyyyhhmm.print(end),
                is(FORMATTER_ddMMyyyyhhmm.print(toDateExpected.withHourOfDay(12).withMinuteOfHour(0)))
        )
    }

    // weekly page ////////////////////////////////////////////////////////////////////////////////////////////////////

    @Test
    public void testIncludingActualInterval_WeeklyPageAggregation() {
        // request args important for this test
        DateTime toDateExpected = new DateTime()
        DateTime fromDateExpected = toDateExpected.minusWeeks(12)

        params.from = FORMATTER_ddMMyyyy.print(fromDateExpected);
        params.fromHour = '12:00'
        params.to = FORMATTER_ddMMyyyy.print(toDateExpected);
        params.toHour = '12:00'

        params.aggrGroupAndInterval = CsiDashboardController.WEEKLY_AGGR_GROUP_PAGE
        params.includeInterval = true

        // request args necessary to validate the command object
        params.selectedTimeFrameInterval = 0
        params.selectedFolder = '1'
        params.selectedPages = ['1', '5']
        params.selectedMeasuredEventIds = ['7', '8', '9']
        params.selectedBrowsers = '2'
        params.selectedLocations = '17'
        params.csiTypeDocComplete = true
        params.debug = false
        params.setToHour = false
        params.setFromHour = false

        // Create and fill the command:
        controllerUnderTest.bindData(command, params)

        // Verification:

        assertTrue(command.validate())

        Interval interval = command.receiveSelectedTimeFrame()
        DateTime start = interval.getStart()
        DateTime end = interval.getEnd()

        assertThat(
                FORMATTER_ddMMyyyyhhmm.print(start),
                is(FORMATTER_ddMMyyyyhhmm.print(fromDateExpected.withHourOfDay(12).withMinuteOfHour(0)))
        )
        assertThat(
                FORMATTER_ddMMyyyyhhmm.print(end),
                is(FORMATTER_ddMMyyyyhhmm.print(toDateExpected.withHourOfDay(12).withMinuteOfHour(0)))
        )
    }

    @Test
    public void testExcludingActualInterval_WeeklyPageAggregation() {
        // request args important for this test
        DateTime toDate = new DateTime()
        DateTime toDateExpected = toDate.minusMinutes(CsiAggregationInterval.WEEKLY)
        params.to = FORMATTER_ddMMyyyy.print(toDate);
        DateTime fromDateExpected = toDate.minusWeeks(12)
        params.from = FORMATTER_ddMMyyyy.print(fromDateExpected);

        params.aggrGroupAndInterval = CsiDashboardController.WEEKLY_AGGR_GROUP_PAGE
        params.includeInterval = false
        params.fromHour = '12:00'
        params.toHour = '12:00'

        // request args necessary to validate the command object
        params.selectedTimeFrameInterval = 0
        params.selectedFolder = '1'
        params.selectedPages = ['1', '5']
        params.selectedMeasuredEventIds = ['7', '8', '9']
        params.selectedBrowsers = '2'
        params.selectedLocations = '17'
        params.csiTypeDocComplete = true
        params.debug = false
        params.setToHour = false
        params.setFromHour = false

        // Create and fill the command
        controllerUnderTest.bindData(command, params)

        // Verification

        assertTrue(command.validate())

        Interval interval = command.receiveSelectedTimeFrame()
        DateTime start = interval.getStart()
        DateTime end = interval.getEnd()

        assertThat(
                FORMATTER_ddMMyyyyhhmm.print(start),
                is(FORMATTER_ddMMyyyyhhmm.print(fromDateExpected.withHourOfDay(12).withMinuteOfHour(0)))
        )
        assertThat(
                FORMATTER_ddMMyyyyhhmm.print(end),
                is(FORMATTER_ddMMyyyyhhmm.print(toDateExpected.withHourOfDay(12).withMinuteOfHour(0)))
        )

    }

    @Test
    public void testExcludingActualIntervalForSmallChosenInterval_WeeklyPageAggregation() {
        // request args important for this test
        DateTime toDate = new DateTime(2015, 4, 20, 5, 0, 0, DateTimeZone.UTC)
        DateTime toDateExpected = toDate.minusMinutes(CsiAggregationInterval.WEEKLY)
        params.to = FORMATTER_ddMMyyyy.print(toDate);
        DateTime fromDate = toDate.minusDays(4)
        DateTime fromDateExpected = fromDate.minusMinutes(CsiAggregationInterval.WEEKLY)
        params.from = FORMATTER_ddMMyyyy.print(fromDate);

        params.aggrGroupAndInterval = CsiDashboardController.WEEKLY_AGGR_GROUP_PAGE
        params.includeInterval = false
        params.fromHour = '12:00'
        params.toHour = '12:00'

        // request args necessary to validate the command object
        params.selectedTimeFrameInterval = 0
        params.selectedFolder = '1'
        params.selectedPages = ['1', '5']
        params.selectedMeasuredEventIds = ['7', '8', '9']
        params.selectedBrowsers = '2'
        params.selectedLocations = '17'
        params.csiTypeDocComplete = true
        params.debug = false
        params.setToHour = false
        params.setFromHour = false

        // Create and fill the command
        command.csiAggregationUtilService.metaClass.isInActualInterval = { DateTime dateTime, Integer interval ->
            //should be true here to simulate fix interval as actual
            return true
        }
        controllerUnderTest.bindData(command, params)

        // Verification

        assertTrue(command.validate())

        Interval interval = command.receiveSelectedTimeFrame()
        DateTime start = interval.getStart()
        DateTime end = interval.getEnd()

        assertThat(
                FORMATTER_ddMMyyyyhhmm.print(start),
                is(FORMATTER_ddMMyyyyhhmm.print(fromDateExpected.withHourOfDay(12).withMinuteOfHour(0)))
        )
        assertThat(
                FORMATTER_ddMMyyyyhhmm.print(end),
                is(FORMATTER_ddMMyyyyhhmm.print(toDateExpected.withHourOfDay(12).withMinuteOfHour(0)))
        )

    }

    @Test
    public void testIncludingLastIntervalIfActualIntervalIsNotInChosenInterval_WeeklyPageAggregation() {
        // request args important for this test
        DateTime toDateExpected = new DateTime().minusWeeks(2)
        params.to = FORMATTER_ddMMyyyy.print(toDateExpected);
        DateTime fromDateExpected = toDateExpected.minusWeeks(12)
        params.from = FORMATTER_ddMMyyyy.print(fromDateExpected);

        params.aggrGroupAndInterval = CsiDashboardController.WEEKLY_AGGR_GROUP_PAGE
        params.includeInterval = false
        params.fromHour = '12:00'
        params.toHour = '12:00'

        // request args to validate the command object
        params.selectedTimeFrameInterval = 0
        params.selectedFolder = '1'
        params.selectedPages = ['1', '5']
        params.selectedMeasuredEventIds = ['7', '8', '9']
        params.selectedBrowsers = '2'
        params.selectedLocations = '17'
        params.csiTypeDocComplete = true
        params.debug = false
        params.setToHour = false
        params.setFromHour = false

        // Create and fill the command
        controllerUnderTest.bindData(command, params)

        // Verification
        assertTrue(command.validate())

        Interval interval = command.receiveSelectedTimeFrame()
        DateTime start = interval.getStart()
        DateTime end = interval.getEnd()

        assertThat(
                FORMATTER_ddMMyyyyhhmm.print(start),
                is(FORMATTER_ddMMyyyyhhmm.print(fromDateExpected.withHourOfDay(12).withMinuteOfHour(0)))
        )
        assertThat(
                FORMATTER_ddMMyyyyhhmm.print(end),
                is(FORMATTER_ddMMyyyyhhmm.print(toDateExpected.withHourOfDay(12).withMinuteOfHour(0)))
        )
    }

    // weekly shop ////////////////////////////////////////////////////////////////////////////////////////////////////

    @Test
    public void testIncludingActualInterval_WeeklyShopAggregation() {
        // request args important for this test
        DateTime toDateExpected = new DateTime()
        DateTime fromDateExpected = toDateExpected.minusWeeks(12)

        params.from = FORMATTER_ddMMyyyy.print(fromDateExpected);
        params.fromHour = '12:00'
        params.to = FORMATTER_ddMMyyyy.print(toDateExpected);
        params.toHour = '12:00'

        params.aggrGroupAndInterval = CsiDashboardController.WEEKLY_AGGR_GROUP_SHOP
        params.includeInterval = true

        // request args necessary to validate the command object
        params.selectedTimeFrameInterval = 0
        params.selectedFolder = '1'
        params.selectedPages = ['1', '5']
        params.selectedMeasuredEventIds = ['7', '8', '9']
        params.selectedBrowsers = '2'
        params.selectedLocations = '17'
        params.csiTypeDocComplete = true
        params.debug = false
        params.setToHour = false
        params.setFromHour = false

        // Create and fill the command:
        controllerUnderTest.bindData(command, params)

        // Verification:

        assertTrue(command.validate())

        Interval interval = command.receiveSelectedTimeFrame()
        DateTime start = interval.getStart()
        DateTime end = interval.getEnd()

        assertThat(
                FORMATTER_ddMMyyyyhhmm.print(start),
                is(FORMATTER_ddMMyyyyhhmm.print(fromDateExpected.withHourOfDay(12).withMinuteOfHour(0)))
        )
        assertThat(
                FORMATTER_ddMMyyyyhhmm.print(end),
                is(FORMATTER_ddMMyyyyhhmm.print(toDateExpected.withHourOfDay(12).withMinuteOfHour(0)))
        )
    }

    @Test
    public void testExcludingActualInterval_WeeklyShopAggregation() {
        // request args important for this test
        DateTime toDate = new DateTime()
        DateTime toDateExpected = toDate.minusMinutes(CsiAggregationInterval.WEEKLY)
        params.to = FORMATTER_ddMMyyyy.print(toDate);
        DateTime fromDateExpected = toDate.minusWeeks(12)
        params.from = FORMATTER_ddMMyyyy.print(fromDateExpected);

        params.aggrGroupAndInterval = CsiDashboardController.WEEKLY_AGGR_GROUP_SHOP
        params.includeInterval = false
        params.fromHour = '12:00'
        params.toHour = '12:00'

        // request args necessary to validate the command object
        params.selectedTimeFrameInterval = 0
        params.selectedFolder = '1'
        params.selectedPages = ['1', '5']
        params.selectedMeasuredEventIds = ['7', '8', '9']
        params.selectedBrowsers = '2'
        params.selectedLocations = '17'
        params.csiTypeDocComplete = true
        params.debug = false
        params.setToHour = false
        params.setFromHour = false

        // Create and fill the command
        controllerUnderTest.bindData(command, params)

        // Verification

        assertTrue(command.validate())

        Interval interval = command.receiveSelectedTimeFrame()
        DateTime start = interval.getStart()
        DateTime end = interval.getEnd()

        assertThat(
                FORMATTER_ddMMyyyyhhmm.print(start),
                is(FORMATTER_ddMMyyyyhhmm.print(fromDateExpected.withHourOfDay(12).withMinuteOfHour(0)))
        )
        assertThat(
                FORMATTER_ddMMyyyyhhmm.print(end),
                is(FORMATTER_ddMMyyyyhhmm.print(toDateExpected.withHourOfDay(12).withMinuteOfHour(0)))
        )

    }

    @Test
    public void testExcludingActualIntervalForSmallChosenInterval_WeeklyShopAggregation() {
        // request args important for this test
        DateTime toDate = new DateTime(2015, 4, 20, 5, 0, 0, DateTimeZone.UTC)
        DateTime toDateExpected = toDate.minusMinutes(CsiAggregationInterval.WEEKLY)
        params.to = FORMATTER_ddMMyyyy.print(toDate);
        DateTime fromDate = toDate.minusDays(4)
        DateTime fromDateExpected = fromDate.minusMinutes(CsiAggregationInterval.WEEKLY)
        params.from = FORMATTER_ddMMyyyy.print(fromDate);

        params.aggrGroupAndInterval = CsiDashboardController.WEEKLY_AGGR_GROUP_SHOP
        params.includeInterval = false
        params.fromHour = '12:00'
        params.toHour = '12:00'

        // request args necessary to validate the command object
        params.selectedTimeFrameInterval = 0
        params.selectedFolder = '1'
        params.selectedPages = ['1', '5']
        params.selectedMeasuredEventIds = ['7', '8', '9']
        params.selectedBrowsers = '2'
        params.selectedLocations = '17'
        params.csiTypeDocComplete = true
        params.debug = false
        params.setToHour = false
        params.setFromHour = false

        // Create and fill the command
        command.csiAggregationUtilService.metaClass.isInActualInterval = { DateTime dateTime, Integer interval ->
            //should be true here to simulate fix interval as actual
            return true
        }
        controllerUnderTest.bindData(command, params)

        // Verification

        assertTrue(command.validate())

        Interval interval = command.receiveSelectedTimeFrame()
        DateTime start = interval.getStart()
        DateTime end = interval.getEnd()

        assertThat(
                FORMATTER_ddMMyyyyhhmm.print(start),
                is(FORMATTER_ddMMyyyyhhmm.print(fromDateExpected.withHourOfDay(12).withMinuteOfHour(0)))
        )
        assertThat(
                FORMATTER_ddMMyyyyhhmm.print(end),
                is(FORMATTER_ddMMyyyyhhmm.print(toDateExpected.withHourOfDay(12).withMinuteOfHour(0)))
        )

    }

    @Test
    public void testIncludingLastIntervalIfActualIntervalIsNotInChosenInterval_WeeklyShopAggregation() {
        // request args important for this test
        DateTime toDateExpected = new DateTime().minusWeeks(2)
        params.to = FORMATTER_ddMMyyyy.print(toDateExpected);
        DateTime fromDateExpected = toDateExpected.minusWeeks(12)
        params.from = FORMATTER_ddMMyyyy.print(fromDateExpected);

        params.aggrGroupAndInterval = CsiDashboardController.WEEKLY_AGGR_GROUP_SHOP
        params.includeInterval = false
        params.fromHour = '12:00'
        params.toHour = '12:00'

        // request args to validate the command object
        params.selectedTimeFrameInterval = 0
        params.selectedFolder = '1'
        params.selectedPages = ['1', '5']
        params.selectedMeasuredEventIds = ['7', '8', '9']
        params.selectedBrowsers = '2'
        params.selectedLocations = '17'
        params.csiTypeDocComplete = true
        params.debug = false
        params.setToHour = false
        params.setFromHour = false

        // Create and fill the command
        controllerUnderTest.bindData(command, params)

        // Verification
        assertTrue(command.validate())

        Interval interval = command.receiveSelectedTimeFrame()
        DateTime start = interval.getStart()
        DateTime end = interval.getEnd()

        assertThat(
                FORMATTER_ddMMyyyyhhmm.print(start),
                is(FORMATTER_ddMMyyyyhhmm.print(fromDateExpected.withHourOfDay(12).withMinuteOfHour(0)))
        )
        assertThat(
                FORMATTER_ddMMyyyyhhmm.print(end),
                is(FORMATTER_ddMMyyyyhhmm.print(toDateExpected.withHourOfDay(12).withMinuteOfHour(0)))
        )
    }
}

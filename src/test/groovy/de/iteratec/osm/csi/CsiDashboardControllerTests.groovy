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

import de.iteratec.osm.ConfigService
import de.iteratec.osm.measurement.environment.Browser
import de.iteratec.osm.measurement.environment.Location
import de.iteratec.osm.measurement.environment.WebPageTestServer
import de.iteratec.osm.measurement.environment.dao.BrowserDaoService
import de.iteratec.osm.measurement.environment.dao.LocationDaoService
import de.iteratec.osm.measurement.schedule.ConnectivityProfile
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.measurement.schedule.dao.JobGroupDaoService
import de.iteratec.osm.measurement.schedule.dao.PageDaoService
import de.iteratec.osm.report.UserspecificDashboardService
import de.iteratec.osm.report.chart.CsiAggregationUtilService
import de.iteratec.osm.report.chart.dao.AggregatorTypeDaoService
import de.iteratec.osm.result.MeasuredEvent
import de.iteratec.osm.result.MvQueryParams
import de.iteratec.osm.result.dao.MeasuredEventDaoService
import de.iteratec.osm.util.DateValueConverter
import de.iteratec.osm.util.DoubleValueConverter
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import groovy.mock.interceptor.MockFor
import org.joda.time.DateTime
import org.joda.time.Interval
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito

import static de.iteratec.osm.csi.Contract.requiresArgumentNotNull
import static org.junit.Assert.*
import static org.mockito.Mockito.when

/**
 * <p>
 * Test-suite of {@link CsiDashboardController} and 
 * {@link CsiDashboardShowAllCommand}.
 * </p> 
 *
 * @author mze
 * @since IT-74
 */
@TestFor(CsiDashboardController)
@Mock([ConnectivityProfile, CsiSystem])
class CsiDashboardControllerTests {

    CsiDashboardController controllerUnderTest
    static CsiDashboardShowAllCommand command

    // Mocks:
    AggregatorTypeDaoService aggregatorTypeDaoServiceMock
    JobGroupDaoService jobGroupDaoServiceMock
    PageDaoService pageDaoServiceMock
    MeasuredEventDaoService measuredEventDaoServiceMock
    BrowserDaoService browserDaoServiceMock
    LocationDaoService locationDaoServiceMock
    ConfigService configServiceMock
    UserspecificDashboardService userspecificDashboardServiceMock

    def doWithSpring ={
        csiAggregationUtilService(CsiAggregationUtilService)
    }

    @Before
    public void setUp() {
        // Because spring resources not loaded in unit tests, declare them locally:
        defineBeans {
            doubleValueConverter(DoubleValueConverter)
            dateValueConverter(DateValueConverter)
        }

        // The controller under test:
        controllerUnderTest = controller

        // Mock relevant services:
        command = new CsiDashboardShowAllCommand()
        command.csiAggregationUtilService = grailsApplication.mainContext.getBean('csiAggregationUtilService')

        this.aggregatorTypeDaoServiceMock = Mockito.mock(AggregatorTypeDaoService.class);
        controllerUnderTest.aggregatorTypeDaoService = aggregatorTypeDaoServiceMock;

        this.jobGroupDaoServiceMock = Mockito.mock(JobGroupDaoService.class);
        controllerUnderTest.jobGroupDaoService = this.jobGroupDaoServiceMock;

        this.pageDaoServiceMock = Mockito.mock(PageDaoService.class);
        controllerUnderTest.pageDaoService = this.pageDaoServiceMock;

        this.measuredEventDaoServiceMock = Mockito.mock(MeasuredEventDaoService.class);
        controllerUnderTest.measuredEventDaoService = this.measuredEventDaoServiceMock;

        this.browserDaoServiceMock = Mockito.mock(BrowserDaoService.class);
        controllerUnderTest.browserDaoService = this.browserDaoServiceMock;

        this.locationDaoServiceMock = Mockito.mock(LocationDaoService.class);
        controllerUnderTest.locationDaoService = this.locationDaoServiceMock;

        def dashboardServiceMock = new MockFor(UserspecificDashboardService);
        dashboardServiceMock.demand.getListOfAvailableCsiDashboards() {-> return [] }
        this.userspecificDashboardServiceMock = dashboardServiceMock.proxyInstance()
        controllerUnderTest.userspecificDashboardService = this.userspecificDashboardServiceMock;

        def configMock = new MockFor(ConfigService, true)
        configMock.demand.getInitialChartHeightInPixels(0..100000) { -> return 400 }
        configServiceMock = configMock.proxyInstance()
        controllerUnderTest.configService = this.configServiceMock



        controllerUnderTest.csiHelperService = [getCsiChartDefaultTitle: { -> return 'not relevant for these tests'
        }] as CsiHelperService

    }

/**
 * Test for inner class {@link CsiDashboardShowAllCommand}.
 */
    @Test
    public void testShowAllCommand_EmptyCreationIsInvalid() {
        assertFalse(command.validate())
        assertNotNull("Collections are never null", command.selectedFolder)
        assertNotNull("Collections are never null", command.selectedPages)
        assertNotNull("Collections are never null", command.selectedMeasuredEventIds)
        assertNotNull("Collections are never null", command.selectedBrowsers)
        assertNotNull("Collections are never null", command.selectedLocations)
    }

    /**
     * Test for inner class {@link CsiDashboardShowAllCommand}.
     */
    @Test
    public void testShowAllCommand_BindFromEmptyRequestArgsIsInvalid() {
        controllerUnderTest.bindData(command, params)

        assertFalse(command.validate())
        assertNotNull("Collections are never null", command.selectedFolder)
        assertNotNull("Collections are never null", command.selectedPages)
        assertNotNull("Collections are never null", command.selectedMeasuredEventIds)
        assertNotNull("Collections are never null", command.selectedBrowsers)
        assertNotNull("Collections are never null", command.selectedLocations)
    }

    /**
     * Test for inner class {@link CsiDashboardShowAllCommand}.
     */
    @Test
    public void testShowAllCommand_BindFromValidRequestArgsIsValid_ValuesNearlyDefaults() {
        // Fill-in request args:
        params.from = '18.08.2013'
        Date expectedDateForFrom = new Date(1376776800000L)

        params.fromHour = '12:00'

        params.to = '19.08.2013'
        Date expectedDateForTo = new Date(1376863200000L)

        params.toHour = '13:00'
        params.aggrGroupAndInterval = CsiDashboardController.HOURLY_MEASURED_EVENT
        params.selectedFolder = '1'
        params.selectedPages = ['1', '5']
        params.selectedAllMeasuredEvents = false
        params.selectedMeasuredEventIds = ['7', '8', '9']
        params.selectedAllBrowsers = false
        params.selectedBrowsers = '2'
        params.selectedAllLocations = false
        params.selectedLocations = '17'
        params._action_showAll = 'Anzeigen'
        params.selectedTimeFrameInterval = 0
        params.csiTypeDocComplete = true
        params.setToHour = false
        params.setFromHour = false
        params.debug = false
        params.includeInterval = false
        // Create and fill the command:
        controllerUnderTest.bindData(command, params)

        // Verification:
        assertTrue(command.validate())
        assertNotNull("Collections are never null", command.selectedFolder)
        assertNotNull("Collections are never null", command.selectedPages)
        assertNotNull("Collections are never null", command.selectedMeasuredEventIds)
        assertNotNull("Collections are never null", command.selectedBrowsers)
        assertNotNull("Collections are never null", command.selectedLocations)

        assertEquals(expectedDateForFrom, command.from);
        assertEquals("12:00", command.fromHour);
        assertEquals("13:00", command.toHour);
        assertEquals(CsiDashboardController.HOURLY_MEASURED_EVENT, command.aggrGroupAndInterval);

        assertEquals(1, command.selectedFolder.size())
        assertTrue(command.selectedFolder.contains(1L))

        assertEquals(2, command.selectedPages.size())
        assertTrue(command.selectedPages.contains(1L))
        assertTrue(command.selectedPages.contains(5L))

        assertFalse(command.selectedAllMeasuredEvents as boolean)
        assertEquals(3, command.selectedMeasuredEventIds.size())
        assertTrue(command.selectedMeasuredEventIds.contains(7L))
        assertTrue(command.selectedMeasuredEventIds.contains(8L))
        assertTrue(command.selectedMeasuredEventIds.contains(9L))

        assertFalse(command.selectedAllBrowsers as boolean)
        assertEquals(1, command.selectedBrowsers.size())
        assertTrue(command.selectedBrowsers.contains(2L))

        assertFalse(command.selectedAllLocations as boolean)
        assertEquals(1, command.selectedLocations.size())
        assertTrue(command.selectedLocations.contains(17L))

        // Could we assume the time frame at once?
        Interval timeFrame = command.receiveSelectedTimeFrame();

        DateTime start = timeFrame.getStart();
        DateTime end = timeFrame.getEnd();

        assertEquals(2013, start.getYear())
        assertEquals(8, start.getMonthOfYear())
        assertEquals(18, start.getDayOfMonth())
        assertEquals(12, start.getHourOfDay())
        assertEquals(0, start.getMinuteOfHour())
        assertEquals(0, start.getSecondOfMinute())
        assertEquals(0, start.getMillisOfSecond())

        assertEquals(2013, end.getYear())
        assertEquals(8, end.getMonthOfYear())
        assertEquals(19, end.getDayOfMonth())
        assertEquals(13, end.getHourOfDay())
        assertEquals(0, end.getMinuteOfHour())
        assertEquals(59, end.getSecondOfMinute())
        assertEquals(999, end.getMillisOfSecond())
    }

    /**
     * Test for inner class {@link CsiDashboardShowAllCommand}.
     */
    @Test
    public void testShowAllCommand_BindFromValidRequestArgsIsValid_ToDateBeforeFromDate() {
        // Fill-in request args:
        params.from = '18.08.2013'
        params.fromHour = '12:00'
        params.to = '17.08.2013'
        params.toHour = '13:00'
        params.selectedTimeFrameInterval = 0
        params.aggrGroup = CsiDashboardController.HOURLY_MEASURED_EVENT
        params.selectedFolder = '1'
        params.selectedPages = ['1', '5']
        params.selectedMeasuredEventIds = ['7', '8', '9']
        params.selectedBrowsers = '2'
        params.selectedLocations = '17'
        params._action_showAll = 'Anzeigen'

        // Create and fill the command:
        controllerUnderTest.bindData(command, params)

        // Verification:
        assertFalse(command.validate())
    }

    /**
     * Test for inner class {@link CsiDashboardShowAllCommand}.
     */
    @Test
    public void testShowAllCommand_BindFromValidRequestArgsIsValid_EqualDateToHourBeforeFromHour() {
        // Fill-in request args:
        params.from = '18.08.2013'
        params.fromHour = '12:00'
        params.to = '18.08.2013'
        params.toHour = '11:00'
        params.selectedTimeFrameInterval = 0
        params.aggrGroup = CsiDashboardController.HOURLY_MEASURED_EVENT
        params.selectedFolder = '1'
        params.selectedPages = ['1', '5']
        params.selectedMeasuredEventIds = ['7', '8', '9']
        params.selectedBrowsers = '2'
        params.selectedLocations = '17'
        params._action_showAll = 'Anzeigen'

        // Create and fill the command:
        controllerUnderTest.bindData(command, params)

        // Verification:
        assertFalse(command.validate())
    }

    /**
     * Test for inner class {@link CsiDashboardShowAllCommand}.
     */
    @Test
    public void testShowAllCommand_BindFromValidRequestArgsIsValid_EqualDateEqualHourToMinuteBeforeFromMinute() {
        // Fill-in request args:
        params.from = '18.08.2013'
        params.fromHour = '12:00'

        params.to = '18.08.2013'
        params.toHour = '12:00'
        params.aggrGroup = CsiDashboardController.HOURLY_MEASURED_EVENT
        params.selectedTimeFrameInterval = 0
        params.selectedFolder = '1'
        params.selectedPages = ['1', '5']
        params.selectedMeasuredEventIds = ['7', '8', '9']
        params.selectedBrowsers = '2'
        params.selectedLocations = '17'
        params._action_showAll = 'Anzeigen'

        // Create and fill the command:
        controllerUnderTest.bindData(command, params)

        // Verification:
        assertFalse(command.validate())
    }

    /**
     * Test for inner class {@link CsiDashboardShowAllCommand}.
     */
    @Test
    public void testShowAllCommand_BindFromValidRequestArgsIsValid_ValuesDifferingFromDefaults() {
        // Fill-in request args:
        params.from = '18.08.2013'
        Date expectedDateForFrom = new Date(1376776800000L)

        params.fromHour = '16:00'
        params.to = '18.08.2013'
        Date expectedDateForTo = new Date(1376776800000L)

        params.toHour = '18:00'
        params.aggrGroupAndInterval = CsiDashboardController.WEEKLY_AGGR_GROUP_PAGE
        params.selectedFolder = '1'
        params.selectedPages = ['1', '5']
        params.selectedAllMeasuredEvents = false
        params.selectedMeasuredEventIds = ['7', '8', '9']
        params.selectedAllBrowsers = false
        params.selectedBrowsers = '2'
        params.selectedAllLocations = false
        params.selectedLocations = '17'
        params._action_showAll = 'Anzeigen'
        params.selectedTimeFrameInterval = 0
        params.includeInterval = true
        params.csiTypeDocComplete = true
        params.setToHour = false
        params.setFromHour = false
        params.debug = false
        params.includeInterval = false

        // Create and fill the command:
        controllerUnderTest.bindData(command, params)

        // Verification:
        assertTrue(command.validate())
        assertNotNull("Collections are never null", command.selectedFolder)
        assertNotNull("Collections are never null", command.selectedPages)
        assertNotNull("Collections are never null", command.selectedMeasuredEventIds)
        assertNotNull("Collections are never null", command.selectedBrowsers)
        assertNotNull("Collections are never null", command.selectedLocations)

        assertEquals(expectedDateForFrom, command.from);
        assertEquals("16:00", command.fromHour);
        assertEquals("18:00", command.toHour);
        assertEquals(CsiDashboardController.WEEKLY_AGGR_GROUP_PAGE, command.aggrGroupAndInterval);

        assertEquals(1, command.selectedFolder.size())
        assertTrue(command.selectedFolder.contains(1L))

        assertEquals(2, command.selectedPages.size())
        assertTrue(command.selectedPages.contains(1L))
        assertTrue(command.selectedPages.contains(5L))

        assertFalse(command.selectedAllMeasuredEvents as boolean)
        assertEquals(3, command.selectedMeasuredEventIds.size())
        assertTrue(command.selectedMeasuredEventIds.contains(7L))
        assertTrue(command.selectedMeasuredEventIds.contains(8L))
        assertTrue(command.selectedMeasuredEventIds.contains(9L))

        assertFalse(command.selectedAllBrowsers as boolean)
        assertEquals(1, command.selectedBrowsers.size())
        assertTrue(command.selectedBrowsers.contains(2L))

        assertFalse(command.selectedAllLocations as boolean)
        assertEquals(1, command.selectedLocations.size())
        assertTrue(command.selectedLocations.contains(17L))

        // Could we assume the time frame at once?
        Interval timeFrame = command.receiveSelectedTimeFrame();

        DateTime start = timeFrame.getStart();
        DateTime end = timeFrame.getEnd();

        assertEquals(2013, start.getYear())
        assertEquals(8, start.getMonthOfYear())
        assertEquals(18, start.getDayOfMonth())
        assertEquals(16, start.getHourOfDay())
        assertEquals(0, start.getMinuteOfHour())
        assertEquals(0, start.getSecondOfMinute())
        assertEquals(0, start.getMillisOfSecond())

        assertEquals(2013, end.getYear())
        assertEquals(8, end.getMonthOfYear())
        assertEquals(18, end.getDayOfMonth())
        assertEquals(18, end.getHourOfDay())
        assertEquals(0, end.getMinuteOfHour())
        assertEquals(59, end.getSecondOfMinute())
        assertEquals(999, end.getMillisOfSecond())
    }

    @Test
    public void testShowAllCommand_BindFromValidRequestArgsIsValid_ValuesDifferingFromDefaults_DAILY_PAGE() {
        // Fill-in request args:
        params.from = '18.08.2013'
        Date expectedDateForFrom = new Date(1376776800000L)

        params.fromHour = '16:00'
        params.to = '18.08.2013'
        Date expectedDateForTo = new Date(1376776800000L)

        params.toHour = '18:00'
        params.aggrGroupAndInterval = CsiDashboardController.DAILY_AGGR_GROUP_PAGE
        params.selectedFolder = '1'
        params.selectedPages = ['1', '5']
        params.selectedAllMeasuredEvents = false
        params.selectedMeasuredEventIds = ['7', '8', '9']
        params.selectedAllBrowsers = false
        params.selectedBrowsers = '2'
        params.selectedAllLocations = false
        params.selectedLocations = '17'
        params._action_showAll = 'Anzeigen'
        params.selectedTimeFrameInterval = 0
        params.csiTypeDocComplete = true
        params.setToHour = false
        params.setFromHour = false
        params.debug = false
        params.includeInterval = false
        // Create and fill the command:
        controllerUnderTest.bindData(command, params)

        // Verification:
        assertTrue(command.validate())
        assertNotNull("Collections are never null", command.selectedFolder)
        assertNotNull("Collections are never null", command.selectedPages)
        assertNotNull("Collections are never null", command.selectedMeasuredEventIds)
        assertNotNull("Collections are never null", command.selectedBrowsers)
        assertNotNull("Collections are never null", command.selectedLocations)

        assertEquals(expectedDateForFrom, command.from);
        assertEquals("16:00", command.fromHour);
        assertEquals("18:00", command.toHour);
        assertEquals(CsiDashboardController.DAILY_AGGR_GROUP_PAGE, command.aggrGroupAndInterval);

        assertEquals(1, command.selectedFolder.size())
        assertTrue(command.selectedFolder.contains(1L))

        assertEquals(2, command.selectedPages.size())
        assertTrue(command.selectedPages.contains(1L))
        assertTrue(command.selectedPages.contains(5L))

        assertFalse(command.selectedAllMeasuredEvents as boolean)
        assertEquals(3, command.selectedMeasuredEventIds.size())
        assertTrue(command.selectedMeasuredEventIds.contains(7L))
        assertTrue(command.selectedMeasuredEventIds.contains(8L))
        assertTrue(command.selectedMeasuredEventIds.contains(9L))

        assertFalse(command.selectedAllBrowsers as boolean)
        assertEquals(1, command.selectedBrowsers.size())
        assertTrue(command.selectedBrowsers.contains(2L))

        assertFalse(command.selectedAllLocations as boolean)
        assertEquals(1, command.selectedLocations.size())
        assertTrue(command.selectedLocations.contains(17L))

        // Could we assume the time frame at once?
        Interval timeFrame = command.receiveSelectedTimeFrame();

        DateTime start = timeFrame.getStart();
        DateTime end = timeFrame.getEnd();

        assertEquals(2013, start.getYear())
        assertEquals(8, start.getMonthOfYear())
        assertEquals(18, start.getDayOfMonth())
        assertEquals(16, start.getHourOfDay())
        assertEquals(0, start.getMinuteOfHour())
        assertEquals(0, start.getSecondOfMinute())
        assertEquals(0, start.getMillisOfSecond())

        assertEquals(2013, end.getYear())
        assertEquals(8, end.getMonthOfYear())
        assertEquals(18, end.getDayOfMonth())
        assertEquals(18, end.getHourOfDay())
        assertEquals(0, end.getMinuteOfHour())
        assertEquals(59, end.getSecondOfMinute())
        assertEquals(999, end.getMillisOfSecond())
    }

    @Test
    public void testShowAllCommand_BindFromValidRequestArgsIsValid_ValuesDifferingFromDefaults_DAILY_SHOP() {
        // Fill-in request args:
        params.from = '18.08.2013'
        Date expectedDateForFrom = new Date(1376776800000L)

        params.fromHour = '16:00'
        params.to = '18.08.2013'
        Date expectedDateForTo = new Date(1376776800000L)

        params.toHour = '18:00'
        params.aggrGroupAndInterval = CsiDashboardController.DAILY_AGGR_GROUP_SHOP
        params.selectedFolder = '1'
        params.selectedPages = []
        params.selectedAllMeasuredEvents = true
        params.selectedMeasuredEventIds = []
        params.selectedAllBrowsers = true
        params.selectedBrowsers = []
        params.selectedAllLocations = true
        params.selectedLocations = []
        params._action_showAll = 'Anzeigen'
        params.selectedTimeFrameInterval = 0
        params.csiTypeDocComplete = true
        params.setToHour = false
        params.setFromHour = false
        params.debug = false
        params.includeInterval = false
        // Create and fill the command:
        controllerUnderTest.bindData(command, params)

        // Verification:
        assertTrue(command.validate())
        assertNotNull("Collections are never null", command.selectedFolder)
        assertNotNull("Collections are never null", command.selectedPages)
        assertNotNull("Collections are never null", command.selectedMeasuredEventIds)
        assertNotNull("Collections are never null", command.selectedBrowsers)
        assertNotNull("Collections are never null", command.selectedLocations)

        assertEquals(expectedDateForFrom, command.from);
        assertEquals("16:00", command.fromHour);
        assertEquals("18:00", command.toHour);
        assertEquals(CsiDashboardController.DAILY_AGGR_GROUP_SHOP, command.aggrGroupAndInterval);

        assertEquals(1, command.selectedFolder.size())
        assertTrue(command.selectedFolder.contains(1L))

        assertEquals(0, command.selectedPages.size())

        assertTrue(command.selectedAllMeasuredEvents as boolean)
        assertEquals(0, command.selectedMeasuredEventIds.size())

        assertTrue(command.selectedAllBrowsers as boolean)
        assertEquals(0, command.selectedBrowsers.size())

        assertTrue(command.selectedAllLocations as boolean)
        assertEquals(0, command.selectedLocations.size())

        // Could we assume the time frame at once?
        Interval timeFrame = command.receiveSelectedTimeFrame();

        DateTime start = timeFrame.getStart();
        DateTime end = timeFrame.getEnd();

        assertEquals(2013, start.getYear())
        assertEquals(8, start.getMonthOfYear())
        assertEquals(18, start.getDayOfMonth())
        assertEquals(16, start.getHourOfDay())
        assertEquals(0, start.getMinuteOfHour())
        assertEquals(0, start.getSecondOfMinute())
        assertEquals(0, start.getMillisOfSecond())

        assertEquals(2013, end.getYear())
        assertEquals(8, end.getMonthOfYear())
        assertEquals(18, end.getDayOfMonth())
        assertEquals(18, end.getHourOfDay())
        assertEquals(0, end.getMinuteOfHour())
        assertEquals(59, end.getSecondOfMinute())
        assertEquals(999, end.getMillisOfSecond())
    }

    @Test
    public void testShowAllCommand_BindFromValidRequestArgsIsValid_ValuesDifferingFromDefaults_DAILY_SYSTEM() {
        // Fill-in request args:
        params.from = '18.08.2013'
        Date expectedDateForFrom = new Date(1376776800000L)

        params.fromHour = '16:00'
        params.to = '18.08.2013'
        Date expectedDateForTo = new Date(1376776800000L)

        params.toHour = '18:00'
        params.aggrGroupAndInterval = CsiDashboardController.DAILY_AGGR_GROUP_SYSTEM
        params.selectedCsiSystems = [1, 2]
        params._action_showAll = 'Anzeigen'
        params.selectedTimeFrameInterval = 0
        params.csiTypeDocComplete = true
        params.setToHour = false
        params.setFromHour = false
        params.debug = false
        params.includeInterval = false

        // Create and fill the command:
        controllerUnderTest.bindData(command, params)

        // Verification:
        assertTrue(command.validate())
        assertNotNull("Collections are never null", command.selectedFolder)
        assertNotNull("Collections are never null", command.selectedPages)
        assertNotNull("Collections are never null", command.selectedMeasuredEventIds)
        assertNotNull("Collections are never null", command.selectedBrowsers)
        assertNotNull("Collections are never null", command.selectedLocations)
        assertNotNull("Collections are never null", command.selectedCsiSystems)

        assertEquals(expectedDateForFrom, command.from);
        assertEquals("16:00", command.fromHour);
        assertEquals("18:00", command.toHour);
        assertEquals(CsiDashboardController.DAILY_AGGR_GROUP_SYSTEM, command.aggrGroupAndInterval);

        assertEquals(2, command.selectedCsiSystems.size())
        assertTrue(command.selectedCsiSystems.contains(1L))
        assertTrue(command.selectedCsiSystems.contains(2L))

        // Could we assume the time frame at once?
        Interval timeFrame = command.receiveSelectedTimeFrame();
        DateTime start = timeFrame.getStart();
        DateTime end = timeFrame.getEnd();

        assertEquals(2013, start.getYear())
        assertEquals(8, start.getMonthOfYear())
        assertEquals(18, start.getDayOfMonth())
        assertEquals(16, start.getHourOfDay())
        assertEquals(0, start.getMinuteOfHour())
        assertEquals(0, start.getSecondOfMinute())
        assertEquals(0, start.getMillisOfSecond())

        assertEquals(2013, end.getYear())
        assertEquals(8, end.getMonthOfYear())
        assertEquals(18, end.getDayOfMonth())
        assertEquals(18, end.getHourOfDay())
        assertEquals(0, end.getMinuteOfHour())
        assertEquals(59, end.getSecondOfMinute())
        assertEquals(999, end.getMillisOfSecond())
    }

    /**
     * Test for inner class {@link CsiDashboardShowAllCommand}.
     */
    @Test
    public void testShowAllCommand_BUG_IT_189() {
        // Fill-in request args:
        params.from = '29.10.2013'

        params.fromHour = '12:00'
        params.to = '29.10.2013'

        params.toHour = '13:00'
        params.aggrGroupAndInterval = CsiDashboardController.WEEKLY_AGGR_GROUP_PAGE
        params.selectedFolder = '1'
        params.selectedPages = ['1', '5']
        params.selectedMeasuredEventIds = ['7', '8', '9']
        params.selectedBrowsers = '2'
        params.selectedLocations = '17'
        params._action_showAll = 'Anzeigen'
        params.csiTypeDocComplete = true
        params.setToHour = false
        params.setFromHour = false
        params.debug = false
        params.includeInterval = false
        
        // Create and fill the command:
        controllerUnderTest.bindData(command, params)

        // Verification:
        assertTrue(command.validate())

        assertEquals("12:00", command.fromHour);
        assertEquals("13:00", command.toHour);
    }

    @Test
    public void testShowAllCommand_BindFromInvalidRequestArgsIsInvalid() {
        // Fill-in request args:
        params.from = '18.08.2013'
        params.fromHour = '12:00'
        params.to = '19.08.2013'
        params.toHour = '13:00'
        params.aggrGroupAndInterval = CsiDashboardController.HOURLY_MEASURED_EVENT
        params.selectedFolder = '1'
        params.selectedPages = ['NOT-A-NUMBER']
        params.selectedMeasuredEventIds = ['7', '8', '9']
        params.selectedBrowsers = '2'
        params.selectedLocations = 'UGLY'
        params._action_showAll = 'Anzeigen'

        // Create and fill the command:
        controllerUnderTest.bindData(command, params)

        // Verification:
        assertFalse(command.validate())
        assertNotNull("Collections are never null", command.selectedFolder)
        assertNotNull("Collections are never null", command.selectedPages)
        assertNotNull("Collections are never null", command.selectedMeasuredEventIds)
        assertNotNull("Collections are never null", command.selectedBrowsers)
        assertNotNull("Collections are never null", command.selectedLocations)

        assertTrue("Invalid data -> no elements in Collection", command.selectedPages.isEmpty())
        assertTrue("Invalid data -> no elements in Collection", command.selectedLocations.isEmpty())
    }

    /**
     * Test for inner class {@link CsiDashboardShowAllCommand}.
     */
    @Test
    public void testShowAllCommand_BindFromInvalidRequestArgsIsInvalid_selectedPage_isEmpty_for_MEASURED_EVENT() {
        // Fill-in request args:
        params.from = '18.08.2013'
        params.fromHour = '16:00'
        params.to = '18.08.2013'
        params.toHour = '18:00'
        params.aggrGroupAndInterval = CsiDashboardController.HOURLY_MEASURED_EVENT
        params.selectedFolder = '1'
        params.selectedPages = []
        params.selectedMeasuredEventIds = ['7', '8', '9']
        params.selectedBrowsers = '2'
        params.selectedLocations = '17'
        params._action_showAll = 'Anzeigen'

        // Create and fill the command:
        controllerUnderTest.bindData(command, params)

        // Verification:
        assertFalse(command.validate())
    }

    /**
     * Test for inner class {@link CsiDashboardShowAllCommand}.
     */
    @Test
    public void testShowAllCommand_BindFromInvalidRequestArgsIsInvalid_selectedPage_isEmpty_for_WEEKLY_PAGE() {
        // Fill-in request args:
        params.from = '18.08.2013'
        params.fromHour = '16:00'
        params.to = '18.08.2013'
        params.toHour = '18:00'
        params.aggrGroupAndInterval = CsiDashboardController.WEEKLY_AGGR_GROUP_PAGE
        params.selectedFolder = '1'
        params.selectedPages = []
        params.selectedMeasuredEventIds = ['7', '8', '9']
        params.selectedBrowsers = '2'
        params.selectedLocations = '17'
        params._action_showAll = 'Anzeigen'

        // Create and fill the command:
        controllerUnderTest.bindData(command, params)

        // Verification:
        assertFalse(command.validate())
    }

    /**
     * Test for inner class {@link CsiDashboardShowAllCommand}.
     */
    @Test
    public void testShowAllCommand_BindFromInvalidRequestArgsIsInvalid_selectedPage_isEmpty_for_DAILY_PAGE() {
        // Fill-in request args:
        params.from = '18.08.2013'
        params.fromHour = '16:00'
        params.to = '18.08.2013'
        params.toHour = '18:00'
        params.aggrGroupAndInterval = CsiDashboardController.DAILY_AGGR_GROUP_PAGE
        params.selectedFolder = '1'
        params.selectedPages = []
        params.selectedMeasuredEventIds = ['7', '8', '9']
        params.selectedBrowsers = '2'
        params.selectedLocations = '17'
        params._action_showAll = 'Anzeigen'

        // Create and fill the command:
        controllerUnderTest.bindData(command, params)

        // Verification:
        assertFalse(command.validate())
    }

    /**
     * Test for inner class {@link CsiDashboardShowAllCommand}.
     */
    @Test
    public void testShowAllCommand_BindFromInvalidRequestArgsIsInvalid_selectedCsiSystems_isEmpty_for_WEEKLY_SYTEM() {
        // Fill-in request args:
        params.from = '18.08.2013'
        params.fromHour = '16:00'
        params.to = '18.08.2013'
        params.toHour = '18:00'
        params.aggrGroupAndInterval = CsiDashboardController.WEEKLY_AGGR_GROUP_SYSTEM
        params.selectedCsiSystems = []
        params._action_showAll = 'Anzeigen'

        // Create and fill the command:
        controllerUnderTest.bindData(command, params)

        // Verification:
        assertFalse(command.validate())
    }

    /**
     * Test for inner class {@link CsiDashboardShowAllCommand}.
     */
    @Test
    public void testShowAllCommand_BindFromInvalidRequestArgsIsInvalid_selectedCsiSystems_isEmpty_for_DAILY_System() {
        // Fill-in request args:
        params.from = '18.08.2013'
        params.fromHour = '16:00'
        params.to = '18.08.2013'
        params.toHour = '18:00'
        params.aggrGroupAndInterval = CsiDashboardController.DAILY_AGGR_GROUP_SYSTEM
        params.selectedCsiSystems = []
        params._action_showAll = 'Anzeigen'

        // Create and fill the command:
        controllerUnderTest.bindData(command, params)

        // Verification:
        assertFalse(command.validate())
    }

    /**
     * Test for inner class {@link CsiDashboardShowAllCommand}.
     */
    @Test
    public void testShowAllCommand_BindFromInvalidRequestArgsIsInvalid_selectedMeasuredEventIds_isEmpty_for_MEASURED_EVENT() {
        // Fill-in request args:
        params.from = '18.08.2013'
        params.fromHour = '16:00'
        params.to = '18.08.2013'
        params.toHour = '18:00'
        params.aggrGroupAndInterval = CsiDashboardController.HOURLY_MEASURED_EVENT
        params.selectedFolder = '1'
        params.selectedPages = ['1', '5']
        params.selectedAllMeasuredEvents = false
        params.selectedMeasuredEventIds = []
        params.selectedAllBrowsers = false
        params.selectedBrowsers = '2'
        params.selectedLocations = '17'
        params._action_showAll = 'Anzeigen'

        // Create and fill the command:
        controllerUnderTest.bindData(command, params)

        // Verification:
        assertFalse(command.validate())
    }

    /**
     * Test for inner class {@link CsiDashboardShowAllCommand}.
     */
    @Test
    public void testShowAllCommand_BindFromInvalidRequestArgsIsInvalid_selectedBrowsers_isEmpty_for_MEASURED_EVENT() {
        // Fill-in request args:
        params.from = '18.08.2013'
        params.fromHour = '16:00'
        params.to = '18.08.2013'
        params.toHour = '18:00'
        params.aggrGroupAndInterval = CsiDashboardController.HOURLY_MEASURED_EVENT
        params.selectedFolder = '1'
        params.selectedPages = ['1', '5']
        params.selectedMeasuredEventIds = ['7', '8', '9']
        params.selectedBrowsers = []
        params.selectedAllBrowsers = false
        params.selectedLocations = '17'
        params._action_showAll = 'Anzeigen'

        // Create and fill the command:
        controllerUnderTest.bindData(command, params)

        // Verification:
        assertFalse(command.validate())
    }

    /**
     * Test for inner class {@link CsiDashboardShowAllCommand}.
     */
    @Test
    public void testShowAllCommand_BindFromInvalidRequestArgsIsInvalid_selectedLocations_isEmpty_for_MEASURED_EVENT() {
        // Fill-in request args:
        params.from = '18.08.2013'
        params.fromHour = '16:00'
        params.to = '18.08.2013'
        params.toHour = '18:00'
        params.aggrGroupAndInterval = CsiDashboardController.HOURLY_MEASURED_EVENT
        params.selectedFolder = '1'
        params.selectedPages = ['1', '5']
        params.selectedMeasuredEventIds = ['7', '8', '9']
        params.selectedBrowsers = '1'
        params.selectedAllLocations = false
        params.selectedLocations = []
        params._action_showAll = 'Anzeigen'

        // Create and fill the command:
        controllerUnderTest.bindData(command, params)

        // Verification:
        assertFalse(command.validate())
    }

    @Test
    public void testConstructViewDataMap() {
        // Mock data:
        //		when(aggregatorTypeDaoServiceMock.findAll()).thenReturn([
        //			new AggregatorType(name: 'AT-1') { public Long getId() { return 1; } },
        //			new AggregatorType(name: 'AT-2') { public Long getId() { return 2; } }
        //		] as Set);

        when(jobGroupDaoServiceMock.findCSIGroups()).thenReturn([
                new JobGroup(name: 'Group2'),
                new JobGroup(name: 'Group1')
        ] as Set);

        Page page1 = new Page(name: 'Page1', weight: 0) {
            public Long getId() { return 1L; }
        };
        Page page2 = new Page(name: 'Page3', weight: 0.25d) {
            public Long getId() { return 2L; }
        };
        Page page3 = new Page(name: 'Page2', weight: 0.5d) {
            public Long getId() { return 3L; }
        };
        when(pageDaoServiceMock.findAll()).thenReturn([
                page1,
                page2,
                page3
        ] as Set);

        MeasuredEvent measuredEvent1 = new MeasuredEvent(name: 'MeasuredEvent1', testedPage: page3) {
            public Long getId() { return 1001L; }
        };
        MeasuredEvent measuredEvent2 = new MeasuredEvent(name: 'MeasuredEvent2', testedPage: page2) {
            public Long getId() { return 1002L; }
        };
        MeasuredEvent measuredEvent3 = new MeasuredEvent(name: 'MeasuredEvent3', testedPage: page1) {
            public Long getId() { return 1003L; }
        };
        MeasuredEvent measuredEvent4 = new MeasuredEvent(name: 'MeasuredEvent4', testedPage: page2) {
            public Long getId() { return 1004L; }
        };
        when(measuredEventDaoServiceMock.findAll()).thenReturn([
                measuredEvent3,
                measuredEvent1,
                measuredEvent2,
                measuredEvent4
        ] as Set);

        Browser browser1 = new Browser(name: 'Browser1') {
            public Long getId() { return 11L; }
        };
        when(browserDaoServiceMock.findAll()).thenReturn([
                browser1
        ] as Set);

        WebPageTestServer server1 = new WebPageTestServer(label: 'server1');

        Location location1 = new Location(label: 'Location1', location: 'locationA', browser: browser1, wptServer: server1) {
            public Long getId() { return 101L; }
        };
        Location location2 = new Location(label: 'Location2', location: 'locationB', browser: browser1, wptServer: server1) {
            public Long getId() { return 102L; }
        };
        Location location3 = new Location(label: 'Location3', location: 'locationC', browser: browser1, wptServer: server1) {
            public Long getId() { return 103L; }
        };
        when(locationDaoServiceMock.findAll()).thenReturn([
                location2,
                location1,
                location3
        ] as Set);

        // Run the test:
        Map<String, Object> result = controllerUnderTest.constructStaticViewDataOfShowAll();

        // Verify result (lists should be sorted by UI visible name or label):
        assertNotNull(result);
        assertEquals(15, result.size());

        // AggregatorType
        assertTrue(result.containsKey('aggrGroupLabels'))
        List<String> aggrGroupLabels = result.get('aggrGroupLabels');
        assertEquals(CsiDashboardController.AGGREGATOR_GROUP_LABELS, aggrGroupLabels)
        //		assertEquals(2, aggrGroupLabels.size())
        //		assertEquals('AT-1', aggrGroupLabels.get(0))
        //		assertEquals('AT-2', aggrGroupLabels.get(1))

        assertTrue(result.containsKey('aggrGroupValues'))
        List<String> aggrGroupValues = result.get('aggrGroupValues');
        assertEquals(CsiDashboardController.AGGREGATOR_GROUP_VALUES, aggrGroupValues)
        //		assertEquals(2, aggrGroupValues.size())
        //		assertEquals('1', aggrGroupValues.get(0))
        //		assertEquals('2', aggrGroupValues.get(1))

        // Folders / CSI-groups
        assertTrue(result.containsKey('folders'))
        List<JobGroup> folders = result.get('folders');
        assertEquals(2, folders.size())
        assertEquals('Group1', folders.get(0).getName())
        assertEquals('Group2', folders.get(1).getName())

        // Pages
        assertTrue(result.containsKey('pages'))
        List<Page> pages = result.get('pages');
        assertEquals(3, pages.size())
        assertEquals('Page1', pages.get(0).getName())
        assertEquals('Page2', pages.get(1).getName())
        assertEquals('Page3', pages.get(2).getName())

        // MeasuredEvents
        assertTrue(result.containsKey('measuredEvents'))
        List<MeasuredEvent> measuredEvents = result.get('measuredEvents');
        assertEquals(4, measuredEvents.size())
        assertEquals('MeasuredEvent1', measuredEvents.get(0).getName())
        assertEquals('MeasuredEvent2', measuredEvents.get(1).getName())
        assertEquals('MeasuredEvent3', measuredEvents.get(2).getName())
        assertEquals('MeasuredEvent4', measuredEvents.get(3).getName())

        // Browsers
        assertTrue(result.containsKey('browsers'))
        List<Browser> browsers = result.get('browsers');
        assertEquals(1, browsers.size())
        assertEquals('Browser1', browsers.get(0).getName())

        // Locations
        assertTrue(result.containsKey('locations'))
        List<Location> locations = result.get('locations');
        assertEquals(3, locations.size())
        assertEquals('Location1', locations.get(0).getLabel())
        assertEquals('Location2', locations.get(1).getLabel())
        assertEquals('Location3', locations.get(2).getLabel())

        // Data for java-script utilities:
        assertTrue(result.containsKey('dateFormat'))
        assertEquals(CsiDashboardController.DATE_FORMAT_STRING_FOR_HIGH_CHART, result.get('dateFormat'))
        assertTrue(result.containsKey('weekStart'))
        assertEquals(CsiDashboardController.MONDAY_WEEKSTART, result.get('weekStart'))

        // --- Map<PageID, Set<MeasuredEventID>>
        Map<Long, Set<Long>> eventsOfPages = result.get('eventsOfPages')
        assertNotNull(eventsOfPages)

        Set<Long> eventsOfPage1 = eventsOfPages.get(1L)
        assertNotNull(eventsOfPage1)
        assertEquals(1, eventsOfPage1.size());
        assertTrue(eventsOfPage1.contains(1003L));

        Set<Long> eventsOfPage2 = eventsOfPages.get(2L)
        assertNotNull(eventsOfPage2)
        assertEquals(2, eventsOfPage2.size());
        assertTrue(eventsOfPage2.contains(1002L));
        assertTrue(eventsOfPage2.contains(1004L));

        Set<Long> eventsOfPage3 = eventsOfPages.get(3L)
        assertNotNull(eventsOfPage3)
        assertEquals(1, eventsOfPage3.size());
        assertTrue(eventsOfPage3.contains(1001L));

        // --- Map<BrowserID, Set<LocationID>>
        Map<Long, Set<Long>> locationsOfBrowsers = result.get('locationsOfBrowsers')
        assertNotNull(locationsOfBrowsers)

        Set<Long> locationsOfBrowser1 = locationsOfBrowsers.get(11L)
        assertNotNull(locationsOfBrowser1)
        assertEquals(3, locationsOfBrowser1.size());
        assertTrue(locationsOfBrowser1.contains(101L));
        assertTrue(locationsOfBrowser1.contains(102L));
        assertTrue(locationsOfBrowser1.contains(103L));
    }

    @Test
    public void testConstructViewDataMap_withDuplicatedLocationsStrings() {
        // Mock data:
        //		when(aggregatorTypeDaoServiceMock.findAll()).thenReturn([
        //			new AggregatorType(name: 'AT-1') { public Long getId() { return 1; } },
        //			new AggregatorType(name: 'AT-2') { public Long getId() { return 2; } }
        //		] as Set);

        when(jobGroupDaoServiceMock.findCSIGroups()).thenReturn([
                new JobGroup(name: 'Group2'),
                new JobGroup(name: 'Group1')
        ] as Set);

        Page page1 = new Page(name: 'Page1', weight: 0) {
            public Long getId() { return 1L; }
        };
        Page page2 = new Page(name: 'Page3', weight: 0.25d) {
            public Long getId() { return 2L; }
        };
        Page page3 = new Page(name: 'Page2', weight: 0.5d) {
            public Long getId() { return 3L; }
        };
        when(pageDaoServiceMock.findAll()).thenReturn([
                page1,
                page2,
                page3
        ] as Set);

        MeasuredEvent measuredEvent1 = new MeasuredEvent(name: 'MeasuredEvent1', testedPage: page3) {
            public Long getId() { return 1001L; }
        };
        MeasuredEvent measuredEvent2 = new MeasuredEvent(name: 'MeasuredEvent2', testedPage: page2) {
            public Long getId() { return 1002L; }
        };
        MeasuredEvent measuredEvent3 = new MeasuredEvent(name: 'MeasuredEvent3', testedPage: page1) {
            public Long getId() { return 1003L; }
        };
        MeasuredEvent measuredEvent4 = new MeasuredEvent(name: 'MeasuredEvent4', testedPage: page2) {
            public Long getId() { return 1004L; }
        };
        when(measuredEventDaoServiceMock.findAll()).thenReturn([
                measuredEvent3,
                measuredEvent1,
                measuredEvent2,
                measuredEvent4
        ] as Set);

        Browser browser1 = new Browser(name: 'Browser1') {
            public Long getId() { return 11L; }
        };
        Browser browser2 = new Browser(name: 'Browser2') {
            public Long getId() { return 12L; }
        };
        when(browserDaoServiceMock.findAll()).thenReturn([
                browser1,
                browser2
        ] as Set);

        WebPageTestServer server1 = new WebPageTestServer(label: 'server1');
        WebPageTestServer server2 = new WebPageTestServer(label: 'server2');

        Location location1 = new Location(label: 'Location1', location: 'duplicatedLocation', browser: browser1, wptServer: server1) {
            public Long getId() { return 101L; }
        };
        Location location2 = new Location(label: 'Location2', location: 'duplicatedLocation', browser: browser2, wptServer: server2) {
            public Long getId() { return 102L; }
        };
        Location location3 = new Location(label: 'Location3', location: 'duplicatedLocation', browser: browser2, wptServer: server1) {
            public Long getId() { return 103L; }
        };
        when(locationDaoServiceMock.findAll()).thenReturn([
                location2,
                location1,
                location3
        ] as Set);

        // Run the test:
        Map<String, Object> result = controllerUnderTest.constructStaticViewDataOfShowAll();

        // Verify result (lists should be sorted by UI visible name or label):
        assertNotNull(result);
        assertEquals(15, result.size());

        // AggregatorType
        assertTrue(result.containsKey('aggrGroupLabels'))
        List<String> aggrGroupLabels = result.get('aggrGroupLabels');
        assertEquals(CsiDashboardController.AGGREGATOR_GROUP_LABELS, aggrGroupLabels)
        //		assertEquals(2, aggrGroupLabels.size())
        //		assertEquals('AT-1', aggrGroupLabels.get(0))
        //		assertEquals('AT-2', aggrGroupLabels.get(1))

        assertTrue(result.containsKey('aggrGroupValues'))
        List<String> aggrGroupValues = result.get('aggrGroupValues');
        assertEquals(CsiDashboardController.AGGREGATOR_GROUP_VALUES, aggrGroupValues)
        //		assertEquals(2, aggrGroupValues.size())
        //		assertEquals('1', aggrGroupValues.get(0))
        //		assertEquals('2', aggrGroupValues.get(1))

        // Folders / CSI-groups
        assertTrue(result.containsKey('folders'))
        List<JobGroup> folders = result.get('folders');
        assertEquals(2, folders.size())
        assertEquals('Group1', folders.get(0).getName())
        assertEquals('Group2', folders.get(1).getName())

        // Pages
        assertTrue(result.containsKey('pages'))
        List<Page> pages = result.get('pages');
        assertEquals(3, pages.size())
        assertEquals('Page1', pages.get(0).getName())
        assertEquals('Page2', pages.get(1).getName())
        assertEquals('Page3', pages.get(2).getName())

        // MeasuredEvents
        assertTrue(result.containsKey('measuredEvents'))
        List<MeasuredEvent> measuredEvents = result.get('measuredEvents');
        assertEquals(4, measuredEvents.size())
        assertEquals('MeasuredEvent1', measuredEvents.get(0).getName())
        assertEquals('MeasuredEvent2', measuredEvents.get(1).getName())
        assertEquals('MeasuredEvent3', measuredEvents.get(2).getName())
        assertEquals('MeasuredEvent4', measuredEvents.get(3).getName())

        // Browsers
        assertTrue(result.containsKey('browsers'))
        List<Browser> browsers = result.get('browsers');
        assertEquals(2, browsers.size())
        assertEquals('Browser1', browsers.get(0).getName())
        assertEquals('Browser2', browsers.get(1).getName())

        // Locations
        assertTrue(result.containsKey('locations'))
        List<Location> locations = result.get('locations');
        assertEquals(3, locations.size())
        assertEquals(101L, locations.get(0).getId())
        assertEquals(103L, locations.get(1).getId())
        assertEquals(102L, locations.get(2).getId())

        // Data for java-script utilities:
        assertTrue(result.containsKey('dateFormat'))
        assertEquals(CsiDashboardController.DATE_FORMAT_STRING_FOR_HIGH_CHART, result.get('dateFormat'))
        assertTrue(result.containsKey('weekStart'))
        assertEquals(CsiDashboardController.MONDAY_WEEKSTART, result.get('weekStart'))

        // --- Map<PageID, Set<MeasuredEventID>>
        Map<Long, Set<Long>> eventsOfPages = result.get('eventsOfPages')
        assertNotNull(eventsOfPages)

        Set<Long> eventsOfPage1 = eventsOfPages.get(1L)
        assertNotNull(eventsOfPage1)
        assertEquals(1, eventsOfPage1.size());
        assertTrue(eventsOfPage1.contains(1003L));

        Set<Long> eventsOfPage2 = eventsOfPages.get(2L)
        assertNotNull(eventsOfPage2)
        assertEquals(2, eventsOfPage2.size());
        assertTrue(eventsOfPage2.contains(1002L));
        assertTrue(eventsOfPage2.contains(1004L));

        Set<Long> eventsOfPage3 = eventsOfPages.get(3L)
        assertNotNull(eventsOfPage3)
        assertEquals(1, eventsOfPage3.size());
        assertTrue(eventsOfPage3.contains(1001L));

        // --- Map<BrowserID, Set<LocationID>>
        Map<Long, Set<Long>> locationsOfBrowsers = result.get('locationsOfBrowsers')
        assertNotNull(locationsOfBrowsers)

        Set<Long> locationsOfBrowser1 = locationsOfBrowsers.get(11L)
        assertNotNull(locationsOfBrowser1)
        assertEquals(1, locationsOfBrowser1.size());
        assertTrue(locationsOfBrowser1.contains(101L));
    }

    /**
     * Test for inner class {@link CsiDashboardShowAllCommand}.
     */
    @Test
    public void testShowAllCommand_testCopyRequestDataToViewModelMap() {
        // form = '18.08.2013'
        Date expectedDate = new Date(1376776800000L)
        command.from = expectedDate
        command.fromHour = '12:00'
        // to = '19.08.2013'
        Date expectedToDate = new Date(1376863200000L)
        command.to = expectedToDate
        command.toHour = '13:00'
        command.aggrGroupAndInterval = CsiDashboardController.HOURLY_MEASURED_EVENT
        command.selectedFolder = [1L]
        command.selectedPages = [1L, 5L]
        command.selectedAllMeasuredEvents = false
        command.selectedMeasuredEventIds = [7L, 8L, 9L]
        command.selectedAllBrowsers = false
        command.selectedBrowsers = [2L]
        command.selectedAllLocations = false
        command.selectedLocations = [17L]
        command.debug = true
        command.selectedTimeFrameInterval = 0
        command.setFromHour = false
        command.setToHour = false
        command.csiTypeDocComplete = true
        command.setToHour = false
        command.setFromHour = false
        command.debug = true
        command.includeInterval = false

        // Create and fill a command:
        assertTrue(command.validate())

        // Run the test:
        Map<String, Object> dataUnderTest = new HashMap<String, Object>();
        command.copyRequestDataToViewModelMap(dataUnderTest);

        // Verification:
        assertEquals(35, dataUnderTest.size());

        assertContainedAndNotNullAndEquals(dataUnderTest, 'selectedFolder', [1L]);
        assertContainedAndNotNullAndEquals(dataUnderTest, 'selectedPages', [1L, 5L]);
        assertContainedAndNotNullAndEquals(dataUnderTest, 'selectedAllMeasuredEvents', false);
        assertContainedAndNotNullAndEquals(dataUnderTest, 'selectedMeasuredEventIds', [7L, 8L, 9L]);
        assertContainedAndNotNullAndEquals(dataUnderTest, 'selectedAllBrowsers', false);
        assertContainedAndNotNullAndEquals(dataUnderTest, 'selectedBrowsers', [2L]);
        assertContainedAndNotNullAndEquals(dataUnderTest, 'selectedAllLocations', false);
        assertContainedAndNotNullAndEquals(dataUnderTest, 'selectedLocations', [17L]);

        assertContainedAndNotNullAndEquals(dataUnderTest, 'from', expectedDate);
        assertContainedAndNotNullAndEquals(dataUnderTest, 'fromHour', '12:00');

        assertContainedAndNotNullAndEquals(dataUnderTest, 'to', expectedToDate);
        assertContainedAndNotNullAndEquals(dataUnderTest, 'toHour', '13:00');

        assertContainedAndNotNullAndEquals(dataUnderTest, 'aggrGroupAndInterval', CsiDashboardController.HOURLY_MEASURED_EVENT);
        assertContainedAndNotNullAndEquals(dataUnderTest, 'debug', true);
        assertContainedAndNotNullAndEquals(dataUnderTest, 'selectedTimeFrameInterval', 0)
        assertContainedAndNotNullAndEquals(dataUnderTest, 'setFromHour', false)
        assertContainedAndNotNullAndEquals(dataUnderTest, 'setToHour', false)
    }

    /**
     * Test for inner class {@link CsiDashboardShowAllCommand}.
     */
    @Test
    public void testShowAllCommand_testCopyRequestDataToViewModelMap_defaultsForMissingValues() {
        // form = '18.08.2013'
        Date expectedDate = new Date(1376776800000L)
        command.from = expectedDate
        command.fromHour = '12:00'
        // to = '19.08.2013'
        Date expectedToDate = new Date(1376863200000L)
        command.to = expectedToDate
        command.toHour = '18:00'
        command.aggrGroupAndInterval = null // Missing! -> Default should be set
        command.selectedFolder = [1L]
        command.selectedPages = [1L, 5L]
        command.selectedAllMeasuredEvents = false
        command.selectedMeasuredEventIds = [7L, 8L, 9L]
        command.selectedAllBrowsers = false
        command.selectedBrowsers = [2L]
        command.selectedAllLocations = false
        command.selectedLocations = [17L]
        command.debug = false
        command.selectedTimeFrameInterval = 0
        command.setFromHour = false
        command.setToHour = false

        // Run the test:
        Map<String, Object> dataUnderTest = new HashMap<String, Object>();
        command.copyRequestDataToViewModelMap(dataUnderTest);

        // Verification:
        assertEquals(35, dataUnderTest.size());

        assertContainedAndNotNullAndEquals(dataUnderTest, 'selectedFolder', [1L]);
        assertContainedAndNotNullAndEquals(dataUnderTest, 'selectedPages', [1L, 5L]);
        assertContainedAndNotNullAndEquals(dataUnderTest, 'selectedAllMeasuredEvents', false);
        assertContainedAndNotNullAndEquals(dataUnderTest, 'selectedMeasuredEventIds', [7L, 8L, 9L]);
        assertContainedAndNotNullAndEquals(dataUnderTest, 'selectedAllBrowsers', false);
        assertContainedAndNotNullAndEquals(dataUnderTest, 'selectedBrowsers', [2L]);
        assertContainedAndNotNullAndEquals(dataUnderTest, 'selectedAllLocations', false);
        assertContainedAndNotNullAndEquals(dataUnderTest, 'selectedLocations', [17L]);
        assertContainedAndNotNullAndEquals(dataUnderTest, 'from', expectedDate);
        assertContainedAndNotNullAndEquals(dataUnderTest, 'fromHour', '12:00');
        assertContainedAndNotNullAndEquals(dataUnderTest, 'to', expectedToDate);
        assertContainedAndNotNullAndEquals(dataUnderTest, 'toHour', '18:00');
        assertContainedAndNotNullAndEquals(dataUnderTest, 'aggrGroupAndInterval', CsiDashboardController.HOURLY_MEASURED_EVENT);
        assertContainedAndNotNullAndEquals(dataUnderTest, 'debug', false);
        assertContainedAndNotNullAndEquals(dataUnderTest, 'selectedTimeFrameInterval', 0)
        assertContainedAndNotNullAndEquals(dataUnderTest, 'setFromHour', false)
        assertContainedAndNotNullAndEquals(dataUnderTest, 'setToHour', false)
    }
//    /**
//     * Test for inner class {@link CsiDashboardShowAllCommand}.
//     */
    @Test
    public void testShowAllCommand_testNoCsiTypeSelectedLeadsToFail() {
        // form = '18.08.2013'
        Date expectedDate = new Date(1376776800000L)
        command.from = expectedDate
        command.fromHour = '12:00'
        // to = '19.08.2013'
        Date expectedToDate = new Date(1376863200000L)
        command.to = expectedToDate
        command.toHour = '13:00'
        command.aggrGroupAndInterval = CsiDashboardController.HOURLY_MEASURED_EVENT
        command.selectedFolder = [1L]
        command.selectedPages = [1L, 5L]
        command.selectedAllMeasuredEvents = false
        command.selectedMeasuredEventIds = [7L, 8L, 9L]
        command.selectedAllBrowsers = false
        command.selectedBrowsers = [2L]
        command.selectedAllLocations = false
        command.selectedLocations = [17L]
        command.debug = true
        command.selectedTimeFrameInterval = 0
        command.setFromHour = false
        command.setToHour = false

        assertFalse(command.validate())

    }

    @Test
    public void testShowAllCommand_testAtleastOneCsiTypeSelected() {
        // form = '18.08.2013'
        Date expectedDate = new Date(1376776800000L)
        command.from = expectedDate
        command.fromHour = '12:00'
        // to = '19.08.2013'
        Date expectedToDate = new Date(1376863200000L)
        command.to = expectedToDate
        command.toHour = '13:00'
        command.aggrGroupAndInterval = CsiDashboardController.HOURLY_MEASURED_EVENT
        command.selectedFolder = [1L]
        command.selectedPages = [1L, 5L]
        command.selectedAllMeasuredEvents = false
        command.selectedMeasuredEventIds = [7L, 8L, 9L]
        command.selectedAllBrowsers = false
        command.selectedBrowsers = [2L]
        command.selectedAllLocations = false
        command.selectedLocations = [17L]
        command.debug = true
        command.selectedTimeFrameInterval = 0
        command.setFromHour = false
        command.setToHour = false
        command.csiTypeDocComplete = true
        command.setToHour = false
        command.setFromHour = false
        command.debug = false
        command.includeInterval = false

        assertTrue(command.validate())
    }

    /**
     * Test for inner class {@link CsiDashboardShowAllCommand}.
     */
    @Test
    public void testShowAllCommand_testCopyRequestDataToViewModelMap_selectAllSelection() {
        // Create and fill a command:
        // form = '18.08.2013'
        Date expectedFromDate = new Date(1376776800000L)
        command.from = expectedFromDate
        command.fromHour = "12:00"
        // to = '19.08.2013'
        Date expectedToDate = new Date(1376863200000L)
        command.to = expectedToDate
        command.toHour = "13:00"
        command.aggrGroupAndInterval = CsiDashboardController.HOURLY_MEASURED_EVENT
        command.selectedFolder = [1L]
        command.selectedPages = [1L, 5L]
        command.selectedAllMeasuredEvents = true
        command.selectedMeasuredEventIds = []
        command.selectedAllBrowsers = false
        command.selectedBrowsers = [2L]
        command.selectedAllLocations = false
        command.selectedLocations = [17L]
        command.debug = false
        command.selectedTimeFrameInterval = 0
        command.setFromHour = false
        command.setToHour = false
        command.csiTypeDocComplete = true
        command.setToHour = false
        command.setFromHour = false
        command.debug = false
        command.includeInterval = false

        // Do we fill all fields?
        assertTrue(command.validate())

        // Run the test:
        Map<String, Object> dataUnderTest = new HashMap<String, Object>();
        command.copyRequestDataToViewModelMap(dataUnderTest);

        // Verification:
        assertEquals(35, dataUnderTest.size());

        assertContainedAndNotNullAndEquals(dataUnderTest, 'selectedFolder', [1L]);
        assertContainedAndNotNullAndEquals(dataUnderTest, 'selectedPages', [1L, 5L]);
        assertContainedAndNotNullAndEquals(dataUnderTest, 'selectedAllMeasuredEvents', true);
        assertContainedAndNotNullAndEquals(dataUnderTest, 'selectedMeasuredEventIds', []);
        assertContainedAndNotNullAndEquals(dataUnderTest, 'selectedAllBrowsers', false);
        assertContainedAndNotNullAndEquals(dataUnderTest, 'selectedBrowsers', [2L]);
        assertContainedAndNotNullAndEquals(dataUnderTest, 'selectedAllLocations', false);
        assertContainedAndNotNullAndEquals(dataUnderTest, 'selectedLocations', [17L]);

        assertContainedAndNotNullAndEquals(dataUnderTest, 'from', expectedFromDate);
        assertContainedAndNotNullAndEquals(dataUnderTest, 'fromHour', '12:00');

        assertContainedAndNotNullAndEquals(dataUnderTest, 'to', expectedToDate);
        assertContainedAndNotNullAndEquals(dataUnderTest, 'toHour', '13:00');

        assertContainedAndNotNullAndEquals(dataUnderTest, 'aggrGroupAndInterval', CsiDashboardController.HOURLY_MEASURED_EVENT);
        assertContainedAndNotNullAndEquals(dataUnderTest, 'debug', false);
        assertContainedAndNotNullAndEquals(dataUnderTest, 'selectedTimeFrameInterval', 0)
        assertContainedAndNotNullAndEquals(dataUnderTest, 'setFromHour', false)
        assertContainedAndNotNullAndEquals(dataUnderTest, 'setToHour', false)
    }

    /**
     * Test for inner class {@link CsiDashboardShowAllCommand}.
     */
    @Test
    public void testShowAllCommand_testCreateMvQueryParams() {
        // Create and fill a command:
        // form = '18.08.2013'
        command.from = new Date(1376776800000L)
        command.fromHour = '12:00'
        // to = '19.08.2013'
        command.to = new Date(1376863200000L)
        command.toHour = '13:00'
        command.aggrGroupAndInterval = CsiDashboardController.HOURLY_MEASURED_EVENT
        command.selectedFolder = [1L]
        command.selectedPages = [1L, 5L]
        command.selectedAllMeasuredEvents = false
        command.selectedMeasuredEventIds = [7L, 8L, 9L]
        command.selectedAllBrowsers = false
        command.selectedBrowsers = [2L]
        command.selectedAllLocations = false
        command.selectedLocations = [17L]
        command.csiTypeDocComplete = true
        command.setToHour = false
        command.setFromHour = false
        command.debug = false
        command.includeInterval = false

        // Do we fill all fields?
        assertTrue(command.validate())

        // Run the test:
        MvQueryParams mvQueryParams = command.createMvQueryParams();

        // Verification:
        assertNotNull(mvQueryParams);
        assertEquals([1L] as SortedSet, mvQueryParams.jobGroupIds);
        assertEquals([1L, 5L] as SortedSet, mvQueryParams.pageIds);
        assertEquals([7L, 8L, 9L] as SortedSet, mvQueryParams.measuredEventIds);
        assertEquals([2L] as SortedSet, mvQueryParams.browserIds);
        assertEquals([17L] as SortedSet, mvQueryParams.locationIds);
    }

    /**
     * Test for inner class {@link CsiDashboardShowAllCommand}.
     */
    @Test
    public void testShowAllCommand_testCreateMvQueryParams_SelectAllIgnoresRealSelection_MeasuredEvents() {
        // Create and fill a command:
        // form = '18.08.2013'
        command.from = new Date(1376776800000L)
        command.fromHour = '12:00'
        // to = '19.08.2013'
        command.to = new Date(1376863200000L)
        command.toHour = '13:00'
        command.aggrGroupAndInterval = CsiDashboardController.HOURLY_MEASURED_EVENT
        command.selectedFolder = [1L]
        command.selectedPages = [1L, 5L]
        command.selectedAllMeasuredEvents = true
        command.selectedMeasuredEventIds = [7L, 8L, 9L]
        command.selectedAllBrowsers = false
        command.selectedBrowsers = [2L]
        command.selectedAllLocations = false
        command.selectedLocations = [17L]
        command.csiTypeDocComplete = true
        command.setToHour = false
        command.setFromHour = false
        command.debug = false
        command.includeInterval = false

        // Do we fill all fields?
        assertTrue(command.validate())

        // Run the test:
        MvQueryParams mvQueryParams = command.createMvQueryParams();

        // Verification:
        assertNotNull(mvQueryParams);
        assertEquals([1L] as SortedSet, mvQueryParams.jobGroupIds);
        assertEquals([1L, 5L] as SortedSet, mvQueryParams.pageIds);
        assertEquals("This set is empty which means to fit all",
                [] as SortedSet, mvQueryParams.measuredEventIds);
        assertEquals([2L] as SortedSet, mvQueryParams.browserIds);
        assertEquals([17L] as SortedSet, mvQueryParams.locationIds);
    }

    /**
     * Test for inner class {@link CsiDashboardShowAllCommand}.
     */
    @Test
    public void testShowAllCommand_testCreateMvQueryParams_SelectAllIgnoresRealSelection_Browsers() {
        // Create and fill a command:
        // form = '18.08.2013'
        command.from = new Date(1376776800000L)
        command.fromHour = '12:00'
        // to = '19.08.2013'
        command.to = new Date(1376863200000L)
        command.toHour = '13:00'
        command.aggrGroupAndInterval = CsiDashboardController.HOURLY_MEASURED_EVENT
        command.selectedFolder = [1L]
        command.selectedPages = [1L, 5L]
        command.selectedAllMeasuredEvents = false
        command.selectedMeasuredEventIds = [7L, 8L, 9L]
        command.selectedAllBrowsers = true;
        command.selectedBrowsers = [2L]
        command.selectedAllLocations = false
        command.selectedLocations = [17L]
        command.csiTypeDocComplete = true
        command.setToHour = false
        command.setFromHour = false
        command.debug = false
        command.includeInterval = false

        // Do we fill all fields?
        assertTrue(command.validate())

        // Run the test:
        MvQueryParams mvQueryParams = command.createMvQueryParams();

        // Verification:
        assertNotNull(mvQueryParams);
        assertEquals([1L] as SortedSet, mvQueryParams.jobGroupIds);
        assertEquals([1L, 5L] as SortedSet, mvQueryParams.pageIds);
        assertEquals([7L, 8L, 9L] as SortedSet, mvQueryParams.measuredEventIds);
        assertEquals("This set is empty which means to fit all",
                [] as SortedSet, mvQueryParams.browserIds);
        assertEquals([17L] as SortedSet, mvQueryParams.locationIds);
    }

    /**
     * Test for inner class {@link CsiDashboardShowAllCommand}.
     */
    @Test
    public void testShowAllCommand_testCreateMvQueryParams_SelectAllIgnoresRealSelection_Locations() {
        // Create and fill a command:
        // form = '18.08.2013'
        command.from = new Date(1376776800000L)
        command.fromHour = '12:00'
        // to = '19.08.2013'
        command.to = new Date(1376863200000L)
        command.toHour = '13:00'
        command.aggrGroupAndInterval = CsiDashboardController.HOURLY_MEASURED_EVENT
        command.selectedFolder = [1L]
        command.selectedPages = [1L, 5L]
        command.selectedAllMeasuredEvents = false
        command.selectedMeasuredEventIds = [7L, 8L, 9L]
        command.selectedAllBrowsers = false
        command.selectedBrowsers = [2L]
        command.selectedAllLocations = true;
        command.selectedLocations = [17L]
        command.csiTypeDocComplete = true
        command.setToHour = false
        command.setFromHour = false
        command.debug = false
        command.includeInterval = false

        // Do we fill all fields?
        assertTrue(command.validate())

        // Run the test:
        MvQueryParams mvQueryParams = command.createMvQueryParams();

        // Verification:
        assertNotNull(mvQueryParams);
        assertEquals([1L] as SortedSet, mvQueryParams.jobGroupIds);
        assertEquals([1L, 5L] as SortedSet, mvQueryParams.pageIds);
        assertEquals([7L, 8L, 9L] as SortedSet, mvQueryParams.measuredEventIds);
        assertEquals([2L] as SortedSet, mvQueryParams.browserIds);
        assertEquals("This set is empty which means to fit all",
                [] as SortedSet, mvQueryParams.locationIds);
    }

    /**
     * Test for inner class {@link CsiDashboardShowAllCommand}.
     */
    @Test(expected = IllegalStateException.class)
    public void testShowAllCommand_testCreateMvQueryParams_invalidCommand() {
        // Should be invalid:
        assertFalse(command.validate())

        // Run the test:
        command.createMvQueryParams();
    }

    @Test
    public void testShowAll_InvalidRequestCausesErrorMessagesByPassingCommandBackToPage() {
        // Fill invalid data to request args:
        params.from = '18.08.2013'
        params.fromHour = null // Invalid!!!
        params.to = '18.08.2013'
        params.toHour = '18:00'
        params.aggrGroup = CsiDashboardController.WEEKLY_AGGR_GROUP_PAGE
        params.selectedTimeFrameInterval = 0
        params.selectedFolder = '1'
        params.selectedPages = ['1', '5']
        params.selectedMeasuredEventIds = ['7', '8', '9']
        params.selectedBrowsers = '2'
        params.selectedLocations = '17'
        params._action_showAll = 'Anzeigen'

        // Create and fill the command:
        controllerUnderTest.bindData(command, params)

        // Check tests precondition:
        assertFalse(command.validate())

        // Run the test:
        Map<String, Object> dataUnderTest = controllerUnderTest.showAll(command);

        // Verification:
        assertTrue(dataUnderTest.containsKey('command'));
        assertSame(command, dataUnderTest.get('command'));
    }

    @Test
    public void testShowAll_EmptyRequestDoesNotCausesErrorMessagesByItNotPassesCommandBackToPage() {
        // We leave 'params' 'empty' here (only grails additions are present! => first and empty request!
        params.action = 'showAll'
        params.controller = 'csiDashboard'

        // Create and fill the command:
        controllerUnderTest.bindData(command, params)

        // Check tests precondition:
        assertFalse(command.validate())

        // Run the test:
        Map<String, Object> dataUnderTest = controllerUnderTest.showAll(command);

        // Verification:
        assertFalse(dataUnderTest.containsKey('command'));
    }

    @Test
    public void testShowAll_EmptyRequestDoesNotCausesErrorMessagesAlsoIfItIsALanguageChangeByItNotPassesCommandBackToPage() {
        // We leave 'params' empty except of the lang attribute to change the language => first and empty request after language change
        params.lang = 'de'
        params.action = 'showAll'
        params.controller = 'csiDashboard'

        // Create and fill the command:
        controllerUnderTest.bindData(command, params)

        // Check tests precondition:
        assertFalse(command.validate())

        // Run the test:
        Map<String, Object> dataUnderTest = controllerUnderTest.showAll(command);

        // Verification:
        assertFalse(dataUnderTest.containsKey('command'));
    }

    @Test
    public void testGetControlnameForCsiType() {
        assertEquals(CsiDashboardShowAllCommand.receiveControlnameFor(CsiType.DOC_COMPLETE), 'csiTypeDocComplete')
        assertEquals(CsiDashboardShowAllCommand.receiveControlnameFor(CsiType.VISUALLY_COMPLETE), 'csiTypeVisuallyComplete')
        shouldFail(IllegalArgumentException) {
            CsiDashboardShowAllCommand.receiveControlnameFor(CsiType.valueOf('NOT_EXISTANT'))
        }
    }

    /**
     * <p>
     * Asserts that a value is contained in a {@link Map}, that the value is
     * not <code>null</code> and is equals to specified expected value.
     * </p>
     *
     * @param dataUnderTest
     *         The map which contents is to be checked, not <code>null</code>.
     * @param key
     *         The key to which the value to check is bound,
     *         not <code>null</code>.
     * @param expectedValue
     *         The expected value to be equals to according to
     * {@link Object#equals(Object)}; not <code>null</code>.
     * @throws AssertionError
     *         if at least one of the conditions are not satisfied.
     */
    private static void assertContainedAndNotNullAndEquals(Map<String, Object> dataUnderTest, String key, Object expectedValue) throws AssertionError {
        requiresArgumentNotNull('dataUnderTest', dataUnderTest)
        requiresArgumentNotNull('key', key)
        requiresArgumentNotNull('expectedValue', expectedValue)

        assertTrue('Map must contain key \"' + key + '\"', dataUnderTest.containsKey(key))
        assertNotNull('Map must contain a not-null value for key \"' + key + '\"', dataUnderTest.get(key))
        assertEquals(expectedValue, dataUnderTest.get(key))
    }
}

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

import de.iteratec.osm.measurement.environment.Browser
import de.iteratec.osm.measurement.environment.Location
import de.iteratec.osm.measurement.environment.WebPageTestServer
import de.iteratec.osm.measurement.environment.dao.BrowserDaoService
import de.iteratec.osm.measurement.environment.dao.LocationDaoService
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.measurement.schedule.JobGroupType
import de.iteratec.osm.measurement.schedule.dao.JobGroupDaoService
import de.iteratec.osm.measurement.schedule.dao.PageDaoService
import de.iteratec.osm.p13n.CookieBasedSettingsService
import de.iteratec.osm.report.chart.AggregatorType
import de.iteratec.osm.report.chart.ChartingLibrary
import de.iteratec.osm.report.chart.dao.AggregatorTypeDaoService
import de.iteratec.osm.result.MeasuredEvent
import de.iteratec.osm.result.MvQueryParams
import de.iteratec.osm.result.dao.MeasuredEventDaoService
import de.iteratec.osm.util.CustomDateEditorRegistrar
import grails.test.mixin.TestFor
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
 * {@link CsiDashboardController.ShowAllCommand}.
 * </p> 
 * 
 * @author mze
 * @since IT-74
 */
@TestFor(CsiDashboardController)
class CsiDashboardControllerTests {

	CsiDashboardController controllerUnderTest

	// Mocks:
	AggregatorTypeDaoService aggregatorTypeDaoServiceMock
	JobGroupDaoService jobGroupDaoServiceMock
	PageDaoService pageDaoServiceMock
	MeasuredEventDaoService measuredEventDaoServiceMock
	BrowserDaoService browserDaoServiceMock
	LocationDaoService locationDaoServiceMock

	@Before
	public void setUp()
	{
		// Because spring resources not loaded in unit tests, declare them locally:
		defineBeans { customPropertyEditorRegistrar(CustomDateEditorRegistrar) }

		// Enable constraint tests:
		mockForConstraintsTests(CsiDashboardController.ShowAllCommand.class);

		// The controller under test:
		controllerUnderTest = controller;

		// Mock relevant services:
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

		controllerUnderTest.cookieBasedSettingsService = [getChartingLibraryToUse: {-> return ChartingLibrary.RICKSHAW}] as CookieBasedSettingsService
		controllerUnderTest.csiHelperService = [getCsiChartDefaultTitle: {-> return 'not relevant for these tests'}] as CsiHelperService
	}

	/**
	 * Test for inner class {@link CsiDashboardController.ShowAllCommand}.
	 */
	@Test
	public void testShowAllCommand_EmptyCreationIsInvalid()
	{
		CsiDashboardController.ShowAllCommand out = new CsiDashboardController.ShowAllCommand()

		assertFalse(out.validate())
		assertNotNull("Collections are never null", out.selectedFolder)
		assertNotNull("Collections are never null", out.selectedPage)
		assertNotNull("Collections are never null", out.selectedMeasuredEventIds)
		assertNotNull("Collections are never null", out.selectedBrowsers)
		assertNotNull("Collections are never null", out.selectedLocations)
	}

	/**
	 * Test for inner class {@link CsiDashboardController.ShowAllCommand}.
	 */
	@Test
	public void testShowAllCommand_BindFromEmptyRequestArgsIsInvalid()
	{
		CsiDashboardController.ShowAllCommand out = new CsiDashboardController.ShowAllCommand()

		controllerUnderTest.bindData(out, params)

		assertFalse(out.validate())
		assertNotNull("Collections are never null", out.selectedFolder)
		assertNotNull("Collections are never null", out.selectedPage)
		assertNotNull("Collections are never null", out.selectedMeasuredEventIds)
		assertNotNull("Collections are never null", out.selectedBrowsers)
		assertNotNull("Collections are never null", out.selectedLocations)
	}

	/**
	 * Test for inner class {@link CsiDashboardController.ShowAllCommand}.
	 */
	@Test
	public void testShowAllCommand_BindFromValidRequestArgsIsValid_ValuesNearlyDefaults()
	{
		// Fill-in request args:
		params.from = '18.08.2013'
		Date expectedDateForFrom = new Date(1376776800000L)

		params.fromHour = '12:00'
		
		params.to = '19.08.2013'
		Date expectedDateForTo = new Date(1376863200000L)

		params.toHour = '13:00'
				params.aggrGroup = AggregatorType.MEASURED_EVENT.toString()
		params.selectedFolder = '1'
		params.selectedPage = ['1', '5']
		params.selectedMeasuredEventIds = ['7', '8', '9']
		params.selectedBrowsers = '2'
		params.selectedLocations = '17'
		params._action_showAll = 'Anzeigen'
		params.selectedTimeFrameInterval = 0

		// Create and fill the command:
		CsiDashboardController.ShowAllCommand out = new CsiDashboardController.ShowAllCommand()
		controllerUnderTest.bindData(out, params)

		// Verification:
		assertTrue(out.validate())
		assertNotNull("Collections are never null", out.selectedFolder)
		assertNotNull("Collections are never null", out.selectedPage)
		assertNotNull("Collections are never null", out.selectedMeasuredEventIds)
		assertNotNull("Collections are never null", out.selectedBrowsers)
		assertNotNull("Collections are never null", out.selectedLocations)

		assertEquals(expectedDateForFrom, out.from);
		assertEquals("12:00", out.fromHour);
		assertEquals("13:00", out.toHour);
		assertEquals(AggregatorType.MEASURED_EVENT, out.aggrGroup);

		assertEquals(1, out.selectedFolder.size())
		assertTrue(out.selectedFolder.contains(1L))

		assertEquals(2, out.selectedPage.size())
		assertTrue(out.selectedPage.contains(1L))
		assertTrue(out.selectedPage.contains(5L))

		assertFalse(out.selectedAllMeasuredEvents as boolean)
		assertEquals(3, out.selectedMeasuredEventIds.size())
		assertTrue(out.selectedMeasuredEventIds.contains(7L))
		assertTrue(out.selectedMeasuredEventIds.contains(8L))
		assertTrue(out.selectedMeasuredEventIds.contains(9L))

		assertFalse(out.selectedAllBrowsers as boolean)
		assertEquals(1, out.selectedBrowsers.size())
		assertTrue(out.selectedBrowsers.contains(2L))

		assertFalse(out.selectedAllLocations as boolean)
		assertEquals(1, out.selectedLocations.size())
		assertTrue(out.selectedLocations.contains(17L))

		// Could we assume the time frame at once?
		Interval timeFrame = out.selectedTimeFrame;

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
	 * Test for inner class {@link CsiDashboardController.ShowAllCommand}.
	 */
	@Test
	public void testShowAllCommand_BindFromValidRequestArgsIsValid_ToDateBeforeFromDate()
	{
		// Fill-in request args:
		params.from = '18.08.2013'
		params.fromHour = '12:00'
		params.to = '17.08.2013'
		params.toHour = '13:00'
		params.selectedTimeFrameInterval = 0
		params.aggrGroup = AggregatorType.MEASURED_EVENT.toString()
		params.selectedFolder = '1'
		params.selectedPage = ['1', '5']
		params.selectedMeasuredEventIds = ['7', '8', '9']
		params.selectedBrowsers = '2'
		params.selectedLocations = '17'
		params._action_showAll = 'Anzeigen'

		// Create and fill the command:
		CsiDashboardController.ShowAllCommand out = new CsiDashboardController.ShowAllCommand()
		controllerUnderTest.bindData(out, params)

		// Verification:
		assertFalse(out.validate())
	}

	/**
	 * Test for inner class {@link CsiDashboardController.ShowAllCommand}.
	 */
	@Test
	public void testShowAllCommand_BindFromValidRequestArgsIsValid_EqualDateToHourBeforeFromHour()
	{
		// Fill-in request args:
		params.from = '18.08.2013'
		params.fromHour = '12:00'
		params.to = '18.08.2013'
		params.toHour = '11:00'
		params.selectedTimeFrameInterval = 0
		params.aggrGroup = AggregatorType.MEASURED_EVENT.toString()
		params.selectedFolder = '1'
		params.selectedPage = ['1', '5']
		params.selectedMeasuredEventIds = ['7', '8', '9']
		params.selectedBrowsers = '2'
		params.selectedLocations = '17'
		params._action_showAll = 'Anzeigen'

		// Create and fill the command:
		CsiDashboardController.ShowAllCommand out = new CsiDashboardController.ShowAllCommand()
		controllerUnderTest.bindData(out, params)

		// Verification:
		assertFalse(out.validate())
	}

	/**
	 * Test for inner class {@link CsiDashboardController.ShowAllCommand}.
	 */
	@Test
	public void testShowAllCommand_BindFromValidRequestArgsIsValid_EqualDateEqualHourToMinuteBeforeFromMinute()
	{
		// Fill-in request args:
		params.from = '18.08.2013'
		params.fromHour = '12:00'
		
		params.to = '18.08.2013'
		params.toHour = '12:00'
		params.aggrGroup = AggregatorType.MEASURED_EVENT.toString()
		params.selectedTimeFrameInterval = 0
		params.selectedFolder = '1'
		params.selectedPage = ['1', '5']
		params.selectedMeasuredEventIds = ['7', '8', '9']
		params.selectedBrowsers = '2'
		params.selectedLocations = '17'
		params._action_showAll = 'Anzeigen'

		// Create and fill the command:
		CsiDashboardController.ShowAllCommand out = new CsiDashboardController.ShowAllCommand()
		controllerUnderTest.bindData(out, params)

		// Verification:
		assertFalse(out.validate())
	}

	/**
	 * Test for inner class {@link CsiDashboardController.ShowAllCommand}. 
	 */
	@Test
	public void testShowAllCommand_BindFromValidRequestArgsIsValid_ValuesDifferingFromDefaults()
	{
		// Fill-in request args:
		params.from = '18.08.2013'
		Date expectedDateForFrom = new Date(1376776800000L)

		params.fromHour = '16:00'
				params.to = '18.08.2013'
		Date expectedDateForTo = new Date(1376776800000L)

		params.toHour = '18:00'
				params.aggrGroup = AggregatorType.PAGE.toString()
		params.selectedFolder = '1'
		params.selectedPage = ['1', '5']
		params.selectedMeasuredEventIds = ['7', '8', '9']
		params.selectedBrowsers = '2'
		params.selectedLocations = '17'
		params._action_showAll = 'Anzeigen'
		params.selectedTimeFrameInterval = 0

		// Create and fill the command:
		CsiDashboardController.ShowAllCommand out = new CsiDashboardController.ShowAllCommand()
		controllerUnderTest.bindData(out, params)

		// Verification:
		assertTrue(out.validate())
		assertNotNull("Collections are never null", out.selectedFolder)
		assertNotNull("Collections are never null", out.selectedPage)
		assertNotNull("Collections are never null", out.selectedMeasuredEventIds)
		assertNotNull("Collections are never null", out.selectedBrowsers)
		assertNotNull("Collections are never null", out.selectedLocations)

		assertEquals(expectedDateForFrom, out.from);
		assertEquals("16:00", out.fromHour);
		assertEquals("18:00", out.toHour);
		assertEquals(AggregatorType.PAGE, out.aggrGroup);

		assertEquals(1, out.selectedFolder.size())
		assertTrue(out.selectedFolder.contains(1L))

		assertEquals(2, out.selectedPage.size())
		assertTrue(out.selectedPage.contains(1L))
		assertTrue(out.selectedPage.contains(5L))

		assertFalse(out.selectedAllMeasuredEvents as boolean)
		assertEquals(3, out.selectedMeasuredEventIds.size())
		assertTrue(out.selectedMeasuredEventIds.contains(7L))
		assertTrue(out.selectedMeasuredEventIds.contains(8L))
		assertTrue(out.selectedMeasuredEventIds.contains(9L))

		assertFalse(out.selectedAllBrowsers as boolean)
		assertEquals(1, out.selectedBrowsers.size())
		assertTrue(out.selectedBrowsers.contains(2L))

		assertFalse(out.selectedAllLocations as boolean)
		assertEquals(1, out.selectedLocations.size())
		assertTrue(out.selectedLocations.contains(17L))

		// Could we assume the time frame at once?
		Interval timeFrame = out.selectedTimeFrame;

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
	public void testShowAllCommand_BindFromValidRequestArgsIsValid_ValuesDifferingFromDefaults_DAILY_PAGE()
	{
		final DAILY_PAGE_AGGREGATOR="daily_page"
		
		// Fill-in request args:
		params.from = '18.08.2013'
		Date expectedDateForFrom = new Date(1376776800000L)

		params.fromHour = '16:00'
				params.to = '18.08.2013'
		Date expectedDateForTo = new Date(1376776800000L)

		params.toHour = '18:00'
				params.aggrGroup = DAILY_PAGE_AGGREGATOR
		params.selectedFolder = '1'
		params.selectedPage = ['1', '5']
		params.selectedMeasuredEventIds = ['7', '8', '9']
		params.selectedBrowsers = '2'
		params.selectedLocations = '17'
		params._action_showAll = 'Anzeigen'
		params.selectedTimeFrameInterval = 0

		// Create and fill the command:
		CsiDashboardController.ShowAllCommand out = new CsiDashboardController.ShowAllCommand()
		controllerUnderTest.bindData(out, params)

		// Verification:
		assertTrue(out.validate())
		assertNotNull("Collections are never null", out.selectedFolder)
		assertNotNull("Collections are never null", out.selectedPage)
		assertNotNull("Collections are never null", out.selectedMeasuredEventIds)
		assertNotNull("Collections are never null", out.selectedBrowsers)
		assertNotNull("Collections are never null", out.selectedLocations)

		assertEquals(expectedDateForFrom, out.from);
		assertEquals("16:00", out.fromHour);
		assertEquals("18:00", out.toHour);
		assertEquals(DAILY_PAGE_AGGREGATOR, out.aggrGroup);

		assertEquals(1, out.selectedFolder.size())
		assertTrue(out.selectedFolder.contains(1L))

		assertEquals(2, out.selectedPage.size())
		assertTrue(out.selectedPage.contains(1L))
		assertTrue(out.selectedPage.contains(5L))

		assertFalse(out.selectedAllMeasuredEvents as boolean)
		assertEquals(3, out.selectedMeasuredEventIds.size())
		assertTrue(out.selectedMeasuredEventIds.contains(7L))
		assertTrue(out.selectedMeasuredEventIds.contains(8L))
		assertTrue(out.selectedMeasuredEventIds.contains(9L))

		assertFalse(out.selectedAllBrowsers as boolean)
		assertEquals(1, out.selectedBrowsers.size())
		assertTrue(out.selectedBrowsers.contains(2L))

		assertFalse(out.selectedAllLocations as boolean)
		assertEquals(1, out.selectedLocations.size())
		assertTrue(out.selectedLocations.contains(17L))

		// Could we assume the time frame at once?
		Interval timeFrame = out.selectedTimeFrame;

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
	public void testShowAllCommand_BindFromValidRequestArgsIsValid_ValuesDifferingFromDefaults_DAILY_SHOP()
	{
		final DAILY_SHOP_AGGREGATOR=CsiDashboardController.DAILY_AGGR_GROUP_SHOP;
		
		// Fill-in request args:
		params.from = '18.08.2013'
		Date expectedDateForFrom = new Date(1376776800000L)

		params.fromHour = '16:00'
				params.to = '18.08.2013'
		Date expectedDateForTo = new Date(1376776800000L)

		params.toHour = '18:00'
				params.aggrGroup = DAILY_SHOP_AGGREGATOR
		params.selectedFolder = '1'
		params.selectedPage = []
		params.selectedMeasuredEventIds = []
		params.selectedBrowsers = []
		params.selectedLocations = []
		params._action_showAll = 'Anzeigen'
		params.selectedTimeFrameInterval = 0

		// Create and fill the command:
		CsiDashboardController.ShowAllCommand out = new CsiDashboardController.ShowAllCommand()
		controllerUnderTest.bindData(out, params)

		// Verification:
		assertTrue(out.validate())
		assertNotNull("Collections are never null", out.selectedFolder)
		assertNotNull("Collections are never null", out.selectedPage)
		assertNotNull("Collections are never null", out.selectedMeasuredEventIds)
		assertNotNull("Collections are never null", out.selectedBrowsers)
		assertNotNull("Collections are never null", out.selectedLocations)

		assertEquals(expectedDateForFrom, out.from);
		assertEquals("16:00", out.fromHour);
		assertEquals("18:00", out.toHour);
		assertEquals(DAILY_SHOP_AGGREGATOR, out.aggrGroup);

		assertEquals(1, out.selectedFolder.size())
		assertTrue(out.selectedFolder.contains(1L))

		assertEquals(0, out.selectedPage.size())

		assertFalse(out.selectedAllMeasuredEvents as boolean)
		assertEquals(0, out.selectedMeasuredEventIds.size())

		assertFalse(out.selectedAllBrowsers as boolean)
		assertEquals(0, out.selectedBrowsers.size())

		assertFalse(out.selectedAllLocations as boolean)
		assertEquals(0, out.selectedLocations.size())

		// Could we assume the time frame at once?
		Interval timeFrame = out.selectedTimeFrame;

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
	 * Test for inner class {@link CsiDashboardController.ShowAllCommand}.
	 */
	@Test
	public void testShowAllCommand_BUG_IT_189()
	{
		// Fill-in request args:
		params.from = '29.10.2013'

		params.fromHour = '12:00'
				params.to = '29.10.2013'

		params.toHour = '13:00'
				params.aggrGroup = AggregatorType.PAGE.toString()
		params.selectedFolder = '1'
		params.selectedPage = ['1', '5']
		params.selectedMeasuredEventIds = ['7', '8', '9']
		params.selectedBrowsers = '2'
		params.selectedLocations = '17'
		params._action_showAll = 'Anzeigen'

		// Create and fill the command:
		CsiDashboardController.ShowAllCommand out = new CsiDashboardController.ShowAllCommand()
		controllerUnderTest.bindData(out, params)

		// Verification:
		assertTrue(out.validate())
		
		assertEquals("12:00", out.fromHour);
		assertEquals("13:00", out.toHour);
	}

	@Test
	public void testShowAllCommand_BindFromInvalidRequestArgsIsInvalid()
	{
		// Fill-in request args:
		params.from = '18.08.2013'
		params.fromHour = '12:00'
				params.to = '19.08.2013'
		params.toHour = '13:00'
				params.aggrGroup = AggregatorType.MEASURED_EVENT.toString()
		params.selectedFolder = '1'
		params.selectedPage = ['1', 'NOT-A-NUMBER']
		params.selectedMeasuredEventIds = ['7', '8', '9']
		params.selectedBrowsers = '2'
		params.selectedLocations = 'UGLY'
		params._action_showAll = 'Anzeigen'

		// Create and fill the command:
		CsiDashboardController.ShowAllCommand out = new CsiDashboardController.ShowAllCommand()
		controllerUnderTest.bindData(out, params)

		// Verification:
		assertFalse(out.validate())
		assertNotNull("Collections are never null", out.selectedFolder)
		assertNotNull("Collections are never null", out.selectedPage)
		assertNotNull("Collections are never null", out.selectedMeasuredEventIds)
		assertNotNull("Collections are never null", out.selectedBrowsers)
		assertNotNull("Collections are never null", out.selectedLocations)

		assertTrue("Invalid data -> no elements in Collection", out.selectedPage.isEmpty())
		assertTrue("Invalid data -> no elements in Collection", out.selectedLocations.isEmpty())
	}

	/**
	 * Test for inner class {@link CsiDashboardController.ShowAllCommand}.
	 */
	@Test
	public void testShowAllCommand_BindFromInvalidRequestArgsIsInvalid_selectedPage_isEmpty_for_MEASURED_EVENT()
	{
		// Fill-in request args:
		params.from = '18.08.2013'
		params.fromHour = '16:00'
				params.to = '18.08.2013'
		params.toHour = '18:00'
				params.aggrGroup = AggregatorType.MEASURED_EVENT.toString()
		params.selectedFolder = '1'
		params.selectedPage = []
		params.selectedMeasuredEventIds = ['7', '8', '9']
		params.selectedBrowsers = '2'
		params.selectedLocations = '17'
		params._action_showAll = 'Anzeigen'

		// Create and fill the command:
		CsiDashboardController.ShowAllCommand out = new CsiDashboardController.ShowAllCommand()
		controllerUnderTest.bindData(out, params)

		// Verification:
		assertFalse(out.validate())
	}

	/**
	 * Test for inner class {@link CsiDashboardController.ShowAllCommand}.
	 */
	@Test
	public void testShowAllCommand_BindFromInvalidRequestArgsIsInvalid_selectedPage_isEmpty_for_WEEKLY_PAGE()
	{
		// Fill-in request args:
		params.from = '18.08.2013'
		params.fromHour = '16:00'
				params.to = '18.08.2013'
		params.toHour = '18:00'
				params.aggrGroup = AggregatorType.PAGE.toString()
		params.selectedFolder = '1'
		params.selectedPage = []
		params.selectedMeasuredEventIds = ['7', '8', '9']
		params.selectedBrowsers = '2'
		params.selectedLocations = '17'
		params._action_showAll = 'Anzeigen'

		// Create and fill the command:
		CsiDashboardController.ShowAllCommand out = new CsiDashboardController.ShowAllCommand()
		controllerUnderTest.bindData(out, params)

		// Verification:
		assertFalse(out.validate())
	}
	
	/**
	 * Test for inner class {@link CsiDashboardController.ShowAllCommand}.
	 */
	@Test
	public void testShowAllCommand_BindFromInvalidRequestArgsIsInvalid_selectedPage_isEmpty_for_DAILY_PAGE()
	{
		// Fill-in request args:
		params.from = '18.08.2013'
		params.fromHour = '16:00'
				params.to = '18.08.2013'
		params.toHour = '18:00'
				params.aggrGroup = CsiDashboardController.DAILY_AGGR_GROUP_PAGE
		params.selectedFolder = '1'
		params.selectedPage = []
		params.selectedMeasuredEventIds = ['7', '8', '9']
		params.selectedBrowsers = '2'
		params.selectedLocations = '17'
		params._action_showAll = 'Anzeigen'

		// Create and fill the command:
		CsiDashboardController.ShowAllCommand out = new CsiDashboardController.ShowAllCommand()
		controllerUnderTest.bindData(out, params)

		// Verification:
		assertFalse(out.validate())
	}

	/**
	 * Test for inner class {@link CsiDashboardController.ShowAllCommand}.
	 */
	@Test
	public void testShowAllCommand_BindFromInvalidRequestArgsIsInvalid_selectedMeasuredEventIds_isEmpty_for_MEASURED_EVENT()
	{
		// Fill-in request args:
		params.from = '18.08.2013'
		params.fromHour = '16:00'
				params.to = '18.08.2013'
		params.toHour = '18:00'
				params.aggrGroup = AggregatorType.MEASURED_EVENT.toString()
		params.selectedFolder = '1'
		params.selectedPage = ['1', '5']
		params.selectedMeasuredEventIds = []
		params.selectedBrowsers = '2'
		params.selectedLocations = '17'
		params._action_showAll = 'Anzeigen'

		// Create and fill the command:
		CsiDashboardController.ShowAllCommand out = new CsiDashboardController.ShowAllCommand()
		controllerUnderTest.bindData(out, params)

		// Verification:
		assertFalse(out.validate())
	}

	/**
	 * Test for inner class {@link CsiDashboardController.ShowAllCommand}.
	 */
	@Test
	public void testShowAllCommand_BindFromInvalidRequestArgsIsInvalid_selectedBrowsers_isEmpty_for_MEASURED_EVENT()
	{
		// Fill-in request args:
		params.from = '18.08.2013'
		params.fromHour = '16:00'
				params.to = '18.08.2013'
		params.toHour = '18:00'
				params.aggrGroup = AggregatorType.MEASURED_EVENT.toString()
		params.selectedFolder = '1'
		params.selectedPage = ['1', '5']
		params.selectedMeasuredEventIds = ['7', '8', '9']
		params.selectedBrowsers = []
		params.selectedLocations = '17'
		params._action_showAll = 'Anzeigen'

		// Create and fill the command:
		CsiDashboardController.ShowAllCommand out = new CsiDashboardController.ShowAllCommand()
		controllerUnderTest.bindData(out, params)

		// Verification:
		assertFalse(out.validate())
	}

	/**
	 * Test for inner class {@link CsiDashboardController.ShowAllCommand}.
	 */
	@Test
	public void testShowAllCommand_BindFromInvalidRequestArgsIsInvalid_selectedLocations_isEmpty_for_MEASURED_EVENT()
	{
		// Fill-in request args:
		params.from = '18.08.2013'
		params.fromHour = '16:00'
				params.to = '18.08.2013'
		params.toHour = '18:00'
				params.aggrGroup = AggregatorType.MEASURED_EVENT.toString()
		params.selectedFolder = '1'
		params.selectedPage = ['1', '5']
		params.selectedMeasuredEventIds = ['7', '8', '9']
		params.selectedBrowsers = '1'
		params.selectedLocations = []
		params._action_showAll = 'Anzeigen'

		// Create and fill the command:
		CsiDashboardController.ShowAllCommand out = new CsiDashboardController.ShowAllCommand()
		controllerUnderTest.bindData(out, params)

		// Verification:
		assertFalse(out.validate())
	}

	@Test
	public void testConstructViewDataMap()
	{
		// Mock data:
		//		when(aggregatorTypeDaoServiceMock.findAll()).thenReturn([
		//			new AggregatorType(name: 'AT-1') { public Long getId() { return 1; } },
		//			new AggregatorType(name: 'AT-2') { public Long getId() { return 2; } }
		//		] as Set);
	
		when(jobGroupDaoServiceMock.findCSIGroups()).thenReturn([
			new JobGroup(name: 'Group2', groupType: JobGroupType.CSI_AGGREGATION),
			new JobGroup(name: 'Group1', groupType: JobGroupType.CSI_AGGREGATION)
		] as Set);
	
		Page page1 = new Page(name: 'Page1', weight: 0)     { public Long getId() { return 1L; } };
		Page page2 = new Page(name: 'Page3', weight: 0.25d) { public Long getId() { return 2L; } };
		Page page3 = new Page(name: 'Page2', weight: 0.5d)  { public Long getId() { return 3L; } };
		when(pageDaoServiceMock.findAll()).thenReturn([
			page1,
			page2,
			page3
		] as Set);
	
		MeasuredEvent measuredEvent1 = new MeasuredEvent(name: 'MeasuredEvent1', testedPage: page3) { public Long getId() { return 1001L; } };
		MeasuredEvent measuredEvent2 = new MeasuredEvent(name: 'MeasuredEvent2', testedPage: page2) { public Long getId() { return 1002L; } };
		MeasuredEvent measuredEvent3 = new MeasuredEvent(name: 'MeasuredEvent3', testedPage: page1) { public Long getId() { return 1003L; } };
		MeasuredEvent measuredEvent4 = new MeasuredEvent(name: 'MeasuredEvent4', testedPage: page2) { public Long getId() { return 1004L; } };
		when(measuredEventDaoServiceMock.findAll()).thenReturn([
			measuredEvent3,
			measuredEvent1,
			measuredEvent2,
			measuredEvent4
		] as Set);
	
		Browser browser1 = new Browser(name: 'Browser1') { public Long getId() { return 11L; } };
		when(browserDaoServiceMock.findAll()).thenReturn([
			browser1
		] as Set);
	
		WebPageTestServer server1 = new WebPageTestServer(label: 'server1');
	
		Location location1 = new Location(label: 'Location1', location: 'locationA', browser: browser1, wptServer: server1) { public Long getId() { return 101L; } };
		Location location2 = new Location(label: 'Location2', location: 'locationB', browser: browser1, wptServer: server1) { public Long getId() { return 102L; } };
		Location location3 = new Location(label: 'Location3', location: 'locationC', browser: browser1, wptServer: server1) { public Long getId() { return 103L; } };
		when(locationDaoServiceMock.findAll()).thenReturn([
			location2,
			location1,
			location3
		] as Set);

		// Run the test:
		Map<String, Object> result = controllerUnderTest.constructStaticViewDataOfShowAll();

		// Verify result (lists should be sorted by UI visible name or label):
		assertNotNull(result);
		assertEquals(13, result.size());

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
		assertTrue(result.containsKey('dateFormatString'))
		assertEquals(CsiDashboardController.DATE_FORMAT_STRING_FOR_HIGH_CHART, result.get('dateFormatString'))
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
	public void testConstructViewDataMap_withDuplicatedLocationsStrings()
	{
		// Mock data:
		//		when(aggregatorTypeDaoServiceMock.findAll()).thenReturn([
		//			new AggregatorType(name: 'AT-1') { public Long getId() { return 1; } },
		//			new AggregatorType(name: 'AT-2') { public Long getId() { return 2; } }
		//		] as Set);
	
		when(jobGroupDaoServiceMock.findCSIGroups()).thenReturn([
			new JobGroup(name: 'Group2', groupType: JobGroupType.CSI_AGGREGATION),
			new JobGroup(name: 'Group1', groupType: JobGroupType.CSI_AGGREGATION)
		] as Set);
	
		Page page1 = new Page(name: 'Page1', weight: 0)     { public Long getId() { return 1L; } };
		Page page2 = new Page(name: 'Page3', weight: 0.25d) { public Long getId() { return 2L; } };
		Page page3 = new Page(name: 'Page2', weight: 0.5d)  { public Long getId() { return 3L; } };
		when(pageDaoServiceMock.findAll()).thenReturn([
			page1,
			page2,
			page3
		] as Set);
	
		MeasuredEvent measuredEvent1 = new MeasuredEvent(name: 'MeasuredEvent1', testedPage: page3) { public Long getId() { return 1001L; } };
		MeasuredEvent measuredEvent2 = new MeasuredEvent(name: 'MeasuredEvent2', testedPage: page2) { public Long getId() { return 1002L; } };
		MeasuredEvent measuredEvent3 = new MeasuredEvent(name: 'MeasuredEvent3', testedPage: page1) { public Long getId() { return 1003L; } };
		MeasuredEvent measuredEvent4 = new MeasuredEvent(name: 'MeasuredEvent4', testedPage: page2) { public Long getId() { return 1004L; } };
		when(measuredEventDaoServiceMock.findAll()).thenReturn([
			measuredEvent3,
			measuredEvent1,
			measuredEvent2,
			measuredEvent4
		] as Set);
	
		Browser browser1 = new Browser(name: 'Browser1') { public Long getId() { return 11L; } };
		Browser browser2 = new Browser(name: 'Browser2') { public Long getId() { return 12L; } };
		when(browserDaoServiceMock.findAll()).thenReturn([
			browser1,
			browser2
		] as Set);
	
		WebPageTestServer server1 = new WebPageTestServer(label: 'server1');
		WebPageTestServer server2 = new WebPageTestServer(label: 'server2');
	
		Location location1 = new Location(label: 'Location1', location: 'duplicatedLocation', browser: browser1, wptServer: server1) { public Long getId() { return 101L; } };
		Location location2 = new Location(label: 'Location2', location: 'duplicatedLocation', browser: browser2, wptServer: server2) { public Long getId() { return 102L; } };
		Location location3 = new Location(label: 'Location3', location: 'duplicatedLocation', browser: browser2, wptServer: server1) { public Long getId() { return 103L; } };
		when(locationDaoServiceMock.findAll()).thenReturn([
			location2,
			location1,
			location3
		] as Set);

		// Run the test:
		Map<String, Object> result = controllerUnderTest.constructStaticViewDataOfShowAll();

		// Verify result (lists should be sorted by UI visible name or label):
		assertNotNull(result);
		assertEquals(13, result.size());

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
		assertTrue(result.containsKey('dateFormatString'))
		assertEquals(CsiDashboardController.DATE_FORMAT_STRING_FOR_HIGH_CHART, result.get('dateFormatString'))
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
	 * Test for inner class {@link CsiDashboardController.ShowAllCommand}.
	 */
	@Test
	public void testShowAllCommand_testCopyRequestDataToViewModelMap()
	{
		// Create and fill a command:
		CsiDashboardController.ShowAllCommand out = new CsiDashboardController.ShowAllCommand()
		// form = '18.08.2013'
		out.from = new Date(1376776800000L)
		out.fromHour = '12:00'
		// to = '19.08.2013'
		out.to = new Date(1376863200000L)
		out.toHour = '13:00'
		out.aggrGroup = AggregatorType.MEASURED_EVENT.toString()
		out.selectedFolder = [1L]
		out.selectedPage = [1L, 5L]
		out.selectedMeasuredEventIds = [7L, 8L, 9L]
		out.selectedBrowsers = [2L]
		out.selectedLocations = [17L]
		out.debug = true
		out.selectedTimeFrameInterval = 0
		out.setFromHour = false
		out.setToHour = false

		// Do we fill all fields?
		assertTrue(out.validate())

		// Run the test:
		Map<String, Object> dataUnderTest = new HashMap<String, Object>();
		out.copyRequestDataToViewModelMap(dataUnderTest);

		// Verification:
		assertEquals(17, dataUnderTest.size());

		assertContainedAndNotNullAndEquals(dataUnderTest, 'selectedFolder', [1L]);
		assertContainedAndNotNullAndEquals(dataUnderTest, 'selectedPage', [1L, 5L]);
		assertContainedAndNotNullAndEquals(dataUnderTest, 'selectedAllMeasuredEvents', '');
		assertContainedAndNotNullAndEquals(dataUnderTest, 'selectedMeasuredEventIds', [7L, 8L, 9L]);
		assertContainedAndNotNullAndEquals(dataUnderTest, 'selectedAllBrowsers', '');
		assertContainedAndNotNullAndEquals(dataUnderTest, 'selectedBrowsers', [2L]);
		assertContainedAndNotNullAndEquals(dataUnderTest, 'selectedAllLocations', '');
		assertContainedAndNotNullAndEquals(dataUnderTest, 'selectedLocations', [17L]);

		assertContainedAndNotNullAndEquals(dataUnderTest, 'from', '18.08.2013');
		assertContainedAndNotNullAndEquals(dataUnderTest, 'fromHour', '12:00');

		assertContainedAndNotNullAndEquals(dataUnderTest, 'to', '19.08.2013');
		assertContainedAndNotNullAndEquals(dataUnderTest, 'toHour', '13:00');

		assertContainedAndNotNullAndEquals(dataUnderTest, 'aggrGroup', AggregatorType.MEASURED_EVENT.toString());
		assertContainedAndNotNullAndEquals(dataUnderTest, 'debug', true);
		assertContainedAndNotNullAndEquals(dataUnderTest, 'selectedTimeFrameInterval', 0)
		assertContainedAndNotNullAndEquals(dataUnderTest, 'setFromHour', false)
		assertContainedAndNotNullAndEquals(dataUnderTest, 'setToHour', false)
	}

	/**
	 * Test for inner class {@link CsiDashboardController.ShowAllCommand}.
	 */
	@Test
	public void testShowAllCommand_testCopyRequestDataToViewModelMap_defaultsForMissingValues()
	{
		// Create and fill a command:
		CsiDashboardController.ShowAllCommand out = new CsiDashboardController.ShowAllCommand()
		// form = '18.08.2013'
		out.from = new Date(1376776800000L)
		out.fromHour = '12:00'
		// to = '19.08.2013'
		out.to = new Date(1376863200000L)
		out.toHour = '18:00'
		out.aggrGroup = null // Missing! -> Default should be set
		out.selectedFolder = [1L]
		out.selectedPage = [1L, 5L]
		out.selectedMeasuredEventIds = [7L, 8L, 9L]
		out.selectedBrowsers = [2L]
		out.selectedLocations = [17L]
		out.debug = false
		out.selectedTimeFrameInterval = 0
		out.setFromHour = false
		out.setToHour = false

		// Run the test:
		Map<String, Object> dataUnderTest = new HashMap<String, Object>();
		out.copyRequestDataToViewModelMap(dataUnderTest);

		// Verification:
		assertEquals(17, dataUnderTest.size());

		assertContainedAndNotNullAndEquals(dataUnderTest, 'selectedFolder', [1L]);
		assertContainedAndNotNullAndEquals(dataUnderTest, 'selectedPage', [1L, 5L]);
		assertContainedAndNotNullAndEquals(dataUnderTest, 'selectedAllMeasuredEvents', '');
		assertContainedAndNotNullAndEquals(dataUnderTest, 'selectedMeasuredEventIds', [7L, 8L, 9L]);
		assertContainedAndNotNullAndEquals(dataUnderTest, 'selectedAllBrowsers', '');
		assertContainedAndNotNullAndEquals(dataUnderTest, 'selectedBrowsers', [2L]);
		assertContainedAndNotNullAndEquals(dataUnderTest, 'selectedAllLocations', '');
		assertContainedAndNotNullAndEquals(dataUnderTest, 'selectedLocations', [17L]);
		assertContainedAndNotNullAndEquals(dataUnderTest, 'from', '18.08.2013');
		assertContainedAndNotNullAndEquals(dataUnderTest, 'fromHour', '12:00');
		assertContainedAndNotNullAndEquals(dataUnderTest, 'to', '19.08.2013');
		assertContainedAndNotNullAndEquals(dataUnderTest, 'toHour', '18:00');
		assertContainedAndNotNullAndEquals(dataUnderTest, 'aggrGroup', AggregatorType.MEASURED_EVENT.toString());
		assertContainedAndNotNullAndEquals(dataUnderTest, 'debug', false);
		assertContainedAndNotNullAndEquals(dataUnderTest, 'selectedTimeFrameInterval', 0)
		assertContainedAndNotNullAndEquals(dataUnderTest, 'setFromHour', false)
		assertContainedAndNotNullAndEquals(dataUnderTest, 'setToHour', false)
	}

	/**
	 * Test for inner class {@link CsiDashboardController.ShowAllCommand}.
	 */
	@Test
	public void testShowAllCommand_testCopyRequestDataToViewModelMap_selectAllSelection()
	{
		// Create and fill a command:
		CsiDashboardController.ShowAllCommand out = new CsiDashboardController.ShowAllCommand()
		// form = '18.08.2013'
		out.from = new Date(1376776800000L)
		out.fromHour = "12:00"
		// to = '19.08.2013'
		out.to = new Date(1376863200000L)
		out.toHour = "13:00"
		out.aggrGroup = AggregatorType.MEASURED_EVENT.toString()
		out.selectedFolder = [1L]
		out.selectedPage = [1L, 5L]
		out.selectedAllMeasuredEvents = 'on'
		out.selectedMeasuredEventIds = []
		out.selectedBrowsers = [2L]
		out.selectedLocations = [17L]
		out.debug = false
		out.selectedTimeFrameInterval = 0
		out.setFromHour = false
		out.setToHour = false

		// Do we fill all fields?
		assertTrue(out.validate())

		// Run the test:
		Map<String, Object> dataUnderTest = new HashMap<String, Object>();
		out.copyRequestDataToViewModelMap(dataUnderTest);

		// Verification:
		assertEquals(17, dataUnderTest.size());

		assertContainedAndNotNullAndEquals(dataUnderTest, 'selectedFolder', [1L]);
		assertContainedAndNotNullAndEquals(dataUnderTest, 'selectedPage', [1L, 5L]);
		assertContainedAndNotNullAndEquals(dataUnderTest, 'selectedAllMeasuredEvents', 'on');
		assertContainedAndNotNullAndEquals(dataUnderTest, 'selectedMeasuredEventIds', []);
		assertContainedAndNotNullAndEquals(dataUnderTest, 'selectedAllBrowsers', '');
		assertContainedAndNotNullAndEquals(dataUnderTest, 'selectedBrowsers', [2L]);
		assertContainedAndNotNullAndEquals(dataUnderTest, 'selectedAllLocations', '');
		assertContainedAndNotNullAndEquals(dataUnderTest, 'selectedLocations', [17L]);

		assertContainedAndNotNullAndEquals(dataUnderTest, 'from', '18.08.2013');
		assertContainedAndNotNullAndEquals(dataUnderTest, 'fromHour', '12:00');

		assertContainedAndNotNullAndEquals(dataUnderTest, 'to', '19.08.2013');
		assertContainedAndNotNullAndEquals(dataUnderTest, 'toHour', '13:00');

		assertContainedAndNotNullAndEquals(dataUnderTest, 'aggrGroup', AggregatorType.MEASURED_EVENT.toString());
		assertContainedAndNotNullAndEquals(dataUnderTest, 'debug', false);
		assertContainedAndNotNullAndEquals(dataUnderTest, 'selectedTimeFrameInterval', 0)
		assertContainedAndNotNullAndEquals(dataUnderTest, 'setFromHour', false)
		assertContainedAndNotNullAndEquals(dataUnderTest, 'setToHour', false)
	}

	/**
	 * Test for inner class {@link CsiDashboardController.ShowAllCommand}.
	 */
	@Test
	public void testShowAllCommand_testCreateMvQueryParams()
	{
		// Create and fill a command:
		CsiDashboardController.ShowAllCommand out = new CsiDashboardController.ShowAllCommand()
		// form = '18.08.2013'
		out.from = new Date(1376776800000L)
		out.fromHour = '12:00'
		// to = '19.08.2013'
		out.to = new Date(1376863200000L)
		out.toHour = '13:00'
		out.aggrGroup = AggregatorType.MEASURED_EVENT.toString()
		out.selectedFolder = [1L]
		out.selectedPage = [1L, 5L]
		out.selectedMeasuredEventIds = [7L, 8L, 9L]
		out.selectedBrowsers = [2L]
		out.selectedLocations = [17L]

		// Do we fill all fields?
		assertTrue(out.validate())

		// Run the test:
		MvQueryParams mvQueryParams = out.createMvQueryParams();

		// Verification:
		assertNotNull(mvQueryParams);
		assertEquals([1L] as SortedSet, mvQueryParams.jobGroupIds);
		assertEquals([1L, 5L] as SortedSet, mvQueryParams.pageIds);
		assertEquals([7L, 8L, 9L] as SortedSet, mvQueryParams.measuredEventIds);
		assertEquals([2L] as SortedSet, mvQueryParams.browserIds);
		assertEquals([17L] as SortedSet, mvQueryParams.locationIds);
	}

	/**
	 * Test for inner class {@link CsiDashboardController.ShowAllCommand}.
	 */
	@Test
	public void testShowAllCommand_testCreateMvQueryParams_SelectAllIgnoresRealSelection_MeasuredEvents()
	{
		// Create and fill a command:
		CsiDashboardController.ShowAllCommand out = new CsiDashboardController.ShowAllCommand()
		// form = '18.08.2013'
		out.from = new Date(1376776800000L)
		out.fromHour = '12:00'
		// to = '19.08.2013'
		out.to = new Date(1376863200000L)
		out.toHour = '13:00'
		out.aggrGroup = AggregatorType.MEASURED_EVENT.toString()
		out.selectedFolder = [1L]
		out.selectedPage = [1L, 5L]
		out.selectedAllMeasuredEvents = 'on';
		out.selectedMeasuredEventIds = [7L, 8L, 9L]
		out.selectedBrowsers = [2L]
		out.selectedLocations = [17L]

		// Do we fill all fields?
		assertTrue(out.validate())

		// Run the test:
		MvQueryParams mvQueryParams = out.createMvQueryParams();

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
	 * Test for inner class {@link CsiDashboardController.ShowAllCommand}.
	 */
	@Test
	public void testShowAllCommand_testCreateMvQueryParams_SelectAllIgnoresRealSelection_Browsers()
	{
		// Create and fill a command:
		CsiDashboardController.ShowAllCommand out = new CsiDashboardController.ShowAllCommand()
		// form = '18.08.2013'
		out.from = new Date(1376776800000L)
		out.fromHour = '12:00'
		// to = '19.08.2013'
		out.to = new Date(1376863200000L)
		out.toHour = '13:00'
		out.aggrGroup = AggregatorType.MEASURED_EVENT.toString()
		out.selectedFolder = [1L]
		out.selectedPage = [1L, 5L]
		out.selectedMeasuredEventIds = [7L, 8L, 9L]
		out.selectedAllBrowsers = true;
		out.selectedBrowsers = [2L]
		out.selectedLocations = [17L]

		// Do we fill all fields?
		assertTrue(out.validate())

		// Run the test:
		MvQueryParams mvQueryParams = out.createMvQueryParams();

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
	 * Test for inner class {@link CsiDashboardController.ShowAllCommand}.
	 */
	@Test
	public void testShowAllCommand_testCreateMvQueryParams_SelectAllIgnoresRealSelection_Locations()
	{
		// Create and fill a command:
		CsiDashboardController.ShowAllCommand out = new CsiDashboardController.ShowAllCommand()
		// form = '18.08.2013'
		out.from = new Date(1376776800000L)
		out.fromHour = '12:00'
		// to = '19.08.2013'
		out.to = new Date(1376863200000L)
		out.toHour = '13:00'
		out.aggrGroup = AggregatorType.MEASURED_EVENT.toString()
		out.selectedFolder = [1L]
		out.selectedPage = [1L, 5L]
		out.selectedMeasuredEventIds = [7L, 8L, 9L]
		out.selectedBrowsers = [2L]
		out.selectedAllLocations = true;
		out.selectedLocations = [17L]

		// Do we fill all fields?
		assertTrue(out.validate())

		// Run the test:
		MvQueryParams mvQueryParams = out.createMvQueryParams();

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
	 * Test for inner class {@link CsiDashboardController.ShowAllCommand}.
	 */
	@Test(expected=IllegalStateException.class)
	public void testShowAllCommand_testCreateMvQueryParams_invalidCommand()
	{
		// Create and fill a command:
		CsiDashboardController.ShowAllCommand out = new CsiDashboardController.ShowAllCommand()

		// Should be invalid:
		assertFalse(out.validate())

		// Run the test:
		out.createMvQueryParams();
	}
	
	@Test
	public void testShowAll_InvalidRequestCausesErrorMessagesByPassingCommandBackToPage() {
		// Fill invalid data to request args:
		params.from = '18.08.2013'
		params.fromHour = null // Invalid!!!
		params.to = '18.08.2013'
		params.toHour = '18:00'
		params.aggrGroup = AggregatorType.PAGE.toString()
		params.selectedTimeFrameInterval = 0
		params.selectedFolder = '1'
		params.selectedPage = ['1', '5']
		params.selectedMeasuredEventIds = ['7', '8', '9']
		params.selectedBrowsers = '2'
		params.selectedLocations = '17'
		params._action_showAll = 'Anzeigen'

		// Create and fill the command:
		CsiDashboardController.ShowAllCommand command = new CsiDashboardController.ShowAllCommand()
		controllerUnderTest.bindData(command, params)

		// Check tests precondition:
		assertFalse(command.validate())
		
		// Run the test:
		Map<String,Object> dataUnderTest = controllerUnderTest.showAll(command);
		
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
		CsiDashboardController.ShowAllCommand command = new CsiDashboardController.ShowAllCommand()
		controllerUnderTest.bindData(command, params)

		// Check tests precondition:
		assertFalse(command.validate())
		
		// Run the test:
		Map<String,Object> dataUnderTest = controllerUnderTest.showAll(command);
		
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
		CsiDashboardController.ShowAllCommand command = new CsiDashboardController.ShowAllCommand()
		controllerUnderTest.bindData(command, params)

		// Check tests precondition:
		assertFalse(command.validate())
		
		// Run the test:
		Map<String,Object> dataUnderTest = controllerUnderTest.showAll(command);
		
		// Verification:
		assertFalse(dataUnderTest.containsKey('command'));
	}
	
	@Test
	public void testShouldWarnAboutLongProcessingTime_Design_Weekly_40_Points_Total()
	{
		int countOfSelectedSystems = 2;
		int countOfSelectedPages = 5;
		int countOfSelectedBrowser = 2;

		DateTime start = new DateTime(2013, 9, 30, 0, 0);
		DateTime end = new DateTime(2013, 10, 13, 23, 59);
		Interval timeFrameTwoWeeks = new Interval(start, end);
		
		int selectedAggregationIntervallInMintues = 7 * 24 * 60; // one week
		
		assertFalse(controllerUnderTest.shouldWarnAboutLongProcessingTime(
				timeFrameTwoWeeks,
				selectedAggregationIntervallInMintues,
				countOfSelectedSystems,
				countOfSelectedPages,
				countOfSelectedBrowser)
		);
	}
	
	@Test
	public void testShouldWarnAboutLongProcessingTime_Design_Hourly_2_weeks_6720_Points_Total_LessThan10000()
	{
		int countOfSelectedSystems = 2;
		int countOfSelectedPages = 5;
		int countOfSelectedBrowser = 2;

		DateTime start = new DateTime(2013, 9, 30, 0, 0);
		DateTime end = new DateTime(2013, 10, 13, 23, 59);
		Interval timeFrameTwoWeeks = new Interval(start, end);
		
		int selectedAggregationIntervallInMintues = 60; // one hour
		
		assertFalse(controllerUnderTest.shouldWarnAboutLongProcessingTime(
				timeFrameTwoWeeks,
				selectedAggregationIntervallInMintues,
				countOfSelectedSystems,
				countOfSelectedPages,
				countOfSelectedBrowser)
		);
	}
	
	@Test
	public void testShouldWarnAboutLongProcessingTime_Design_Hourly_3_weeks_10080_Points_Total_LessThan10000()
	{
		int countOfSelectedSystems = 2;
		int countOfSelectedPages = 5;
		int countOfSelectedBrowser = 2;

		DateTime start = new DateTime(2013, 9, 30, 0, 0);
		DateTime end = new DateTime(2013, 10, 20, 23, 59);
		Interval timeFrameTwoWeeks = new Interval(start, end);
		
		int selectedAggregationIntervallInMintues = 60; // one hour
		
		assertTrue(controllerUnderTest.shouldWarnAboutLongProcessingTime(
				timeFrameTwoWeeks,
				selectedAggregationIntervallInMintues,
				countOfSelectedSystems,
				countOfSelectedPages,
				countOfSelectedBrowser)
		);
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
	 *         {@link Object#equals(Object)}; not <code>null</code>.
	 * @throws AssertionError 
	 *         if at least one of the conditions are not satisfied.
	 */
	private static void assertContainedAndNotNullAndEquals(Map<String, Object> dataUnderTest, String key, Object expectedValue) throws AssertionError
	{
		requiresArgumentNotNull('dataUnderTest', dataUnderTest)
		requiresArgumentNotNull('key', key)
		requiresArgumentNotNull('expectedValue', expectedValue)

		assertTrue('Map must contain key \"' + key + '\"', dataUnderTest.containsKey(key))
		assertNotNull('Map must contain a not-null value for key \"' + key + '\"', dataUnderTest.get(key))
		assertEquals(expectedValue, dataUnderTest.get(key))
	}
}

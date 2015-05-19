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

package de.iteratec.osm.result

import de.iteratec.osm.csi.Page
import de.iteratec.osm.measurement.environment.Browser
import de.iteratec.osm.measurement.environment.Location
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
import de.iteratec.osm.result.EventResultDashboardController.ShowAllCommand
import de.iteratec.osm.result.dao.MeasuredEventDaoService
import de.iteratec.osm.util.CustomDateEditorRegistrar
import grails.test.mixin.TestFor
import org.joda.time.DateTime
import org.joda.time.Interval
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito

import static org.mockito.Mockito.when

/**
 * <p>
 * Test-suite of {@link EventResultDashboardController} and 
 * {@link EventResultDashboardController.ShowAllCommand}.
 * </p> 
 * 
 * @author rhe
 * @since IT-98
 */
@TestFor(EventResultDashboardController)
class EventResultDashboardControllerTests {
	
	EventResultDashboardController controllerUnderTest

	// Mocks:

	AggregatorTypeDaoService aggregatorTypeDaoServiceMock
	JobGroupDaoService jobGroupDaoServiceMock
	PageDaoService pageDaoServiceMock
	MeasuredEventDaoService measuredEventDaoServiceMock
	BrowserDaoService browserDaoServiceMock
	LocationDaoService locationDaoServiceMock
	EventResultDashboardService eventResultDashboardServiceMock
	
	@Before
	public void setUp()
	{
		// Because spring resources not loaded in unit tests, declare them locally:
		defineBeans { customPropertyEditorRegistrar(CustomDateEditorRegistrar) }

		// Enable constraint tests:
		mockForConstraintsTests(ShowAllCommand.class);

		// The controller under test:
		controllerUnderTest = controller;

		// Mock relevant services:
		this.aggregatorTypeDaoServiceMock = Mockito.mock(AggregatorTypeDaoService.class);
		controllerUnderTest.aggregatorTypeDaoService = aggregatorTypeDaoServiceMock;

		this.jobGroupDaoServiceMock = Mockito.mock(JobGroupDaoService.class);
		controllerUnderTest.jobGroupDaoService = this.jobGroupDaoServiceMock;

		this.pageDaoServiceMock = Mockito.mock(PageDaoService.class);
		controllerUnderTest.pageDaoService = this.pageDaoServiceMock;

		//this.measuredEventDaoServiceMock = Mockito.mock(MeasuredEventDaoService.class);
		//controllerUnderTest.measuredEventDaoService = this.measuredEventDaoServiceMock;

		// FIXME DRI,MZE 2014-01-03: Remove use of "real" implementation. Suggest to infer Java interface and to use Mockito again. 
		this.eventResultDashboardServiceMock = new EventResultDashboardService();
//		 Mockito.mock(EventResultDashboardService.class);
		controllerUnderTest.eventResultDashboardService = this.eventResultDashboardServiceMock;
		
		this.browserDaoServiceMock = Mockito.mock(BrowserDaoService.class);
		controllerUnderTest.browserDaoService = this.browserDaoServiceMock;

		this.locationDaoServiceMock = Mockito.mock(LocationDaoService.class);
		controllerUnderTest.locationDaoService = this.locationDaoServiceMock;

		controllerUnderTest.cookieBasedSettingsService = [getChartingLibraryToUse: {-> return ChartingLibrary.RICKSHAW}] as CookieBasedSettingsService
	}

	/**
	 * Test for inner class {@link CsiDashboardController.ShowAllCommand}.
	 */
	@Test
	public void testShowAllCommand_EmptyCreationIsInvalid()
	{
		ShowAllCommand out = new ShowAllCommand()

		assertFalse(out.validate())
		assertNotNull("Collections are never null", out.selectedFolder)
		assertNotNull("Collections are never null", out.selectedPages)
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
		ShowAllCommand out = new ShowAllCommand()

		controllerUnderTest.bindData(out, params)

		assertFalse(out.validate())
		assertNotNull("Collections are never null", out.selectedFolder)
		assertNotNull("Collections are never null", out.selectedPages)
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
		params.aggrGroup = AggregatorType.RESULT_CACHED_DOC_COMPLETE_INCOMING_BYTES.toString()
		params.selectedFolder = '1'
		params.selectedPages = ['1', '5']
		params.selectedMeasuredEventIds = ['7', '8', '9']
		params.selectedBrowsers = '2'
		params.selectedLocations = '17'
		params._action_showAll = 'Anzeigen'
		params.selectedAllBrowsers = false
		params.selectedAllLocations = false
		params.selectedAllMeasuredEvents = false
		params.selectedAggrGroupValuesCached = [ AggregatorType.RESULT_CACHED_LOAD_TIME ]
		params.selectedTimeFrameInterval = 0
		
		// Create and fill the command:
		ShowAllCommand out = new ShowAllCommand()
		controllerUnderTest.bindData(out, params)

		// Verification:
		assertTrue(out.validate())
		assertNotNull("Collections are never null", out.selectedFolder)
		assertNotNull("Collections are never null", out.selectedPages)
		assertNotNull("Collections are never null", out.selectedMeasuredEventIds)
		assertNotNull("Collections are never null", out.selectedBrowsers)
		assertNotNull("Collections are never null", out.selectedLocations)

		assertEquals(expectedDateForFrom, out.from);
		assertEquals("12:00", out.fromHour);
		assertEquals("13:00", out.toHour);
		assertEquals(AggregatorType.RESULT_CACHED_DOC_COMPLETE_INCOMING_BYTES, out.aggrGroup);

		assertEquals(1, out.selectedFolder.size())
		assertTrue(out.selectedFolder.contains(1L))

		assertEquals(2, out.selectedPages.size())
		assertTrue(out.selectedPages.contains(1L))
		assertTrue(out.selectedPages.contains(5L))

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
				params.aggrGroup = AggregatorType.RESULT_CACHED_DOC_COMPLETE_INCOMING_BYTES.toString()
		params.selectedFolder = '1'
		params.selectedPages = ['1', '5']
		params.selectedMeasuredEventIds = ['7', '8', '9']
		params.selectedBrowsers = '2'
		params.selectedLocations = '17'
		params._action_showAll = 'Anzeigen'

		// Create and fill the command:
		ShowAllCommand out = new ShowAllCommand()
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
				params.aggrGroup = AggregatorType.RESULT_CACHED_DOC_COMPLETE_INCOMING_BYTES.toString()
		params.selectedFolder = '1'
		params.selectedPages = ['1', '5']
		params.selectedMeasuredEventIds = ['7', '8', '9']
		params.selectedBrowsers = '2'
		params.selectedLocations = '17'
		params._action_showAll = 'Anzeigen'

		// Create and fill the command:
		ShowAllCommand out = new ShowAllCommand()
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
				params.aggrGroup = AggregatorType.RESULT_CACHED_DOC_COMPLETE_INCOMING_BYTES.toString()
		params.selectedFolder = '1'
		params.selectedPages = ['1', '5']
		params.selectedMeasuredEventIds = ['7', '8', '9']
		params.selectedBrowsers = '2'
		params.selectedLocations = '17'
		params._action_showAll = 'Anzeigen'

		// Create and fill the command:
		ShowAllCommand out = new ShowAllCommand()
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
		params.selectedPages = ['1', '5']
		params.selectedMeasuredEventIds = ['7', '8', '9']
		params.selectedBrowsers = '2'
		params.selectedLocations = '17'
		params._action_showAll = 'Anzeigen'
		params.selectedAllBrowsers = false
		params.selectedAllLocations = false
		params.selectedAllMeasuredEvents = false
		params.selectedAggrGroupValuesCached = [ AggregatorType.RESULT_CACHED_LOAD_TIME ]
		params.selectedTimeFrameInterval = 0

		// Create and fill the command:
		ShowAllCommand out = new ShowAllCommand()
		controllerUnderTest.bindData(out, params)

		// Verification:
		assertTrue(out.validate())
		assertNotNull("Collections are never null", out.selectedFolder)
		assertNotNull("Collections are never null", out.selectedPages)
		assertNotNull("Collections are never null", out.selectedMeasuredEventIds)
		assertNotNull("Collections are never null", out.selectedBrowsers)
		assertNotNull("Collections are never null", out.selectedLocations)

		assertEquals(expectedDateForFrom, out.from);
		assertEquals("16:00", out.fromHour);
		assertEquals("18:00", out.toHour);
		assertEquals(AggregatorType.PAGE, out.aggrGroup);

		assertEquals(1, out.selectedFolder.size())
		assertTrue(out.selectedFolder.contains(1L))

		assertEquals(2, out.selectedPages.size())
		assertTrue(out.selectedPages.contains(1L))
		assertTrue(out.selectedPages.contains(5L))

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
	public void testShowAllCommand_BindFromInvalidRequestArgsIsInvalid()
	{
		// Fill-in request args:
		params.from = '18.08.2013'
		params.fromHour = '12:00'
				params.to = '19.08.2013'
		params.toHour = '13:00'
				params.aggrGroup = AggregatorType.RESULT_CACHED_DOC_COMPLETE_INCOMING_BYTES.toString()
		params.selectedFolder = '1'
		params.selectedPages = ['NOT-A-NUMBER']
		params.selectedMeasuredEventIds = ['7', '8', '9']
		params.selectedBrowsers = '2'
		params.selectedLocations = 'UGLY'
		params._action_showAll = 'Anzeigen'

		// Create and fill the command:
		ShowAllCommand out = new ShowAllCommand()
		controllerUnderTest.bindData(out, params)

		// Verification:
		assertFalse(out.validate())
		assertNotNull("Collections are never null", out.selectedFolder)
		assertNotNull("Collections are never null", out.selectedPages)
		assertNotNull("Collections are never null", out.selectedMeasuredEventIds)
		assertNotNull("Collections are never null", out.selectedBrowsers)
		assertNotNull("Collections are never null", out.selectedLocations)

		assertTrue("Invalid data -> no elements in Collection", out.selectedPages.isEmpty())
		assertTrue("Invalid data -> no elements in Collection", out.selectedLocations.isEmpty())
	}

	/**
	 * Test for inner class {@link CsiDashboardController.ShowAllCommand}.
	 */
	@Test
	public void testShowAllCommand_BindFromInvalidRequestArgsIsInvalid_selectedPage_isEmpty_for_RESULT_CACHED_DOC_COMPLETE_INCOMING_BYTES()
	{
		// Fill-in request args:
		params.from = '18.08.2013'
		params.fromHour = '16:00'
				params.to = '18.08.2013'
		params.toHour = '18:00'
				params.aggrGroup = AggregatorType.RESULT_CACHED_DOC_COMPLETE_INCOMING_BYTES.toString()
		params.selectedFolder = '1'
		params.selectedPages = []
		params.selectedMeasuredEventIds = ['7', '8', '9']
		params.selectedBrowsers = '2'
		params.selectedLocations = '17'
		params._action_showAll = 'Anzeigen'

		// Create and fill the command:
		ShowAllCommand out = new ShowAllCommand()
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
		params.selectedPages = []
		params.selectedMeasuredEventIds = ['7', '8', '9']
		params.selectedBrowsers = '2'
		params.selectedLocations = '17'
		params._action_showAll = 'Anzeigen'

		// Create and fill the command:
		ShowAllCommand out = new ShowAllCommand()
		controllerUnderTest.bindData(out, params)

		// Verification:
		assertFalse(out.validate())
	}

	/**
	 * Test for inner class {@link CsiDashboardController.ShowAllCommand}.
	 */
	@Test
	public void testShowAllCommand_BindFromInvalidRequestArgsIsInvalid_selectedMeasuredEvents_isEmpty_for_RESULT_CACHED_DOC_COMPLETE_INCOMING_BYTES()
	{
		// Fill-in request args:
		params.from = '18.08.2013'
		params.fromHour = '16:00'
				params.to = '18.08.2013'
		params.toHour = '18:00'
				params.aggrGroup = AggregatorType.RESULT_CACHED_DOC_COMPLETE_INCOMING_BYTES.toString()
		params.selectedFolder = '1'
		params.selectedPages = ['1', '5']
		params.selectedMeasuredEventIds = []
		params.selectedBrowsers = '2'
		params.selectedLocations = '17'
		params._action_showAll = 'Anzeigen'

		// Create and fill the command:
		ShowAllCommand out = new ShowAllCommand()
		controllerUnderTest.bindData(out, params)

		// Verification:
		assertFalse(out.validate())
	}

	/**
	 * Test for inner class {@link CsiDashboardController.ShowAllCommand}.
	 */
	@Test
	public void testShowAllCommand_BindFromInvalidRequestArgsIsInvalid_selectedBrowsers_isEmpty_for_RESULT_CACHED_DOC_COMPLETE_INCOMING_BYTES()
	{
		// Fill-in request args:
		params.from = '18.08.2013'
		params.fromHour = '16:00'
				params.to = '18.08.2013'
		params.toHour = '18:00'
				params.aggrGroup = AggregatorType.RESULT_CACHED_DOC_COMPLETE_INCOMING_BYTES.toString()
		params.selectedFolder = '1'
		params.selectedPages = ['1', '5']
		params.selectedMeasuredEventIds = ['7', '8', '9']
		params.selectedBrowsers = []
		params.selectedLocations = '17'
		params._action_showAll = 'Anzeigen'

		// Create and fill the command:
		ShowAllCommand out = new ShowAllCommand()
		controllerUnderTest.bindData(out, params)

		// Verification:
		assertFalse(out.validate())
	}

	/**
	 * Test for inner class {@link CsiDashboardController.ShowAllCommand}.
	 */
	@Test
	public void testShowAllCommand_BindFromInvalidRequestArgsIsInvalid_selectedLocations_isEmpty_for_RESULT_CACHED_DOC_COMPLETE_INCOMING_BYTES()
	{
		// Fill-in request args:
		params.from = '18.08.2013'
		params.fromHour = '16:00'
				params.to = '18.08.2013'
		params.toHour = '18:00'
				params.aggrGroup = AggregatorType.RESULT_CACHED_DOC_COMPLETE_INCOMING_BYTES.toString()
		params.selectedFolder = '1'
		params.selectedPages = ['1', '5']
		params.selectedMeasuredEventIds = ['7', '8', '9']
		params.selectedBrowsers = '1'
		params.selectedLocations = []
		params._action_showAll = 'Anzeigen'

		// Create and fill the command:
		ShowAllCommand out = new ShowAllCommand()
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
	
		when(jobGroupDaoServiceMock.findAll()).thenReturn([
			new JobGroup(name: 'Group2', groupType: JobGroupType.RAW_DATA_SELECTION),
			new JobGroup(name: 'Group1', groupType: JobGroupType.CSI_AGGREGATION)
		] as Set);
	
		Page page1 = new Page(name: 'Page1', weight: 0)     { public Long getId() { return 1L; } };
		Page page2 = new Page(name: 'Page2', weight: 0.25d) { public Long getId() { return 2L; } };
		Page page3 = new Page(name: 'Page3', weight: 0.5d)  { public Long getId() { return 3L; } };
		when(pageDaoServiceMock.findAll()).thenReturn([
			page1,
			page2,
			page3
		] as Set);
	
		MeasuredEvent measuredEvent1 = new MeasuredEvent(name: 'MeasuredEvent1', testedPage: page3) { public Long getId() { return 1001L; } };
		MeasuredEvent measuredEvent2 = new MeasuredEvent(name: 'MeasuredEvent2', testedPage: page2) { public Long getId() { return 1002L; } };
		MeasuredEvent measuredEvent3 = new MeasuredEvent(name: 'MeasuredEvent3', testedPage: page1) { public Long getId() { return 1003L; } };
		MeasuredEvent measuredEvent4 = new MeasuredEvent(name: 'MeasuredEvent4', testedPage: page2) { public Long getId() { return 1004L; } };
		/*
		when(measuredEventDaoServiceMock.findAll()).thenReturn([
			measuredEvent3,
			measuredEvent1,
			measuredEvent2,
			measuredEvent4
		] as Set); */
	
		Browser browser1 = new Browser(name: 'Browser1') { public Long getId() { return 11L; } };
		when(browserDaoServiceMock.findAll()).thenReturn([
			browser1
		] as Set);
	
		Location location1 = new Location(label: 'Location1', browser: browser1) { public Long getId() { return 101L; } };
		Location location2 = new Location(label: 'Location2', browser: browser1) { public Long getId() { return 102L; } };
		Location location3 = new Location(label: 'Location3', browser: browser1) { public Long getId() { return 103L; } };
		when(locationDaoServiceMock.findAll()).thenReturn([
			location2,
			location1,
			location3
		] as Set);

		eventResultDashboardServiceMock.metaClass.getAllJobGroups = { -> return [
			new JobGroup(name: 'Group2', groupType: JobGroupType.RAW_DATA_SELECTION),
			new JobGroup(name: 'Group1', groupType: JobGroupType.CSI_AGGREGATION)
		] };
		eventResultDashboardServiceMock.metaClass.getAllPages = { -> return [
			page1,
			page2,
			page3
		] };
		eventResultDashboardServiceMock.metaClass.getAllMeasuredEvents = { -> return [
			measuredEvent3,
			measuredEvent1,
			measuredEvent2,
			measuredEvent4
		] };
		eventResultDashboardServiceMock.metaClass.getAllBrowser = { -> return [
			browser1
		] };
		eventResultDashboardServiceMock.metaClass.getAllLocations = { -> return [
			location1,
			location2,
			location3
		]};
		
		// Run the test:
		Map<String, Object> result = controllerUnderTest.constructStaticViewDataOfShowAll();

		// Verify result (lists should be sorted by UI visible name or label):
		assertNotNull(result);
		assertEquals(16, result.size());

		// AggregatorType
		assertTrue(result.containsKey('aggrGroupLabels'))
		List<String> aggrGroupLabels = result.get('aggrGroupLabels');
		assertEquals(EventResultDashboardController.AGGREGATOR_GROUP_LABELS, aggrGroupLabels)
		//		assertEquals(2, aggrGroupLabels.size())
		//		assertEquals('AT-1', aggrGroupLabels.get(0))
		//		assertEquals('AT-2', aggrGroupLabels.get(1))

		assertTrue(result.containsKey('aggrGroupValuesCached'))
		assertTrue(result.containsKey('aggrGroupValuesUnCached'))
		
		// CSI-groups
		assertTrue(result.containsKey('folders'))
		List<JobGroup> csiGroups = result.get('folders');
		assertEquals(2, csiGroups.size())
		assertEquals('Group2', csiGroups.get(0).getName())
		assertEquals('Group1', csiGroups.get(1).getName())

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
		assertEquals('MeasuredEvent3', measuredEvents.get(0).getName())
		assertEquals('MeasuredEvent1', measuredEvents.get(1).getName())
		assertEquals('MeasuredEvent2', measuredEvents.get(2).getName())
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
		assertEquals(EventResultDashboardController.DATE_FORMAT_STRING, result.get('dateFormat'))
		assertTrue(result.containsKey('weekStart'))
		assertEquals(EventResultDashboardController.MONDAY_WEEKSTART, result.get('weekStart'))
		
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

	/**
	 * Test for inner class {@link CsiDashboardController.ShowAllCommand}.
	 */
	@Test
	public void testShowAllCommand_testCopyRequestDataToViewModelMap()
	{
		// Create and fill a command:
		ShowAllCommand out = new ShowAllCommand()
		// form = '18.08.2013'
		out.from = new Date(1376776800000L)
		out.fromHour = "12:00"
				out.to = new Date(1376863200000L)
		out.toHour = "13:00"
				out.aggrGroup = AggregatorType.RESULT_CACHED_DOC_COMPLETE_INCOMING_BYTES.toString()
		out.selectedFolder = [1L]
		out.selectedPages = [1L, 5L]
		out.selectedMeasuredEventIds = [7L, 8L, 9L]
		out.selectedBrowsers = [2L]
		out.selectedLocations = [17L]
		out.selectedAllBrowsers = false
		out.selectedAllLocations = false
		out.selectedAllMeasuredEvents = false
		out.selectedAggrGroupValuesCached = [ AggregatorType.RESULT_CACHED_LOAD_TIME ]
		out.debug = false
		out.setFromHour = false
		out.setToHour = false
		
		// Do we fill all fields?
		assertTrue(out.validate())

		// Run the test:
		Map<String, Object> dataUnderTest = new HashMap<String, Object>();
		out.copyRequestDataToViewModelMap(dataUnderTest);

		// Verification:
		assertEquals(26, dataUnderTest.size());

		assertContainedAndNotNullAndEquals(dataUnderTest, 'selectedFolder', [1L]);
		assertContainedAndNotNullAndEquals(dataUnderTest, 'selectedPages', [1L, 5L]);
		assertContainedAndNotNullAndEquals(dataUnderTest, 'selectedAllMeasuredEvents', false);
		assertContainedAndNotNullAndEquals(dataUnderTest, 'selectedMeasuredEventIds', [7L, 8L, 9L]);
		assertContainedAndNotNullAndEquals(dataUnderTest, 'selectedAllBrowsers', false);
		assertContainedAndNotNullAndEquals(dataUnderTest, 'selectedBrowsers', [2L]);
		assertContainedAndNotNullAndEquals(dataUnderTest, 'selectedAllLocations', false);
		assertContainedAndNotNullAndEquals(dataUnderTest, 'selectedLocations', [17L]);

		assertContainedAndNotNullAndEquals(dataUnderTest, 'from', '18.08.2013');
		assertContainedAndNotNullAndEquals(dataUnderTest, 'fromHour', '12:00');

		assertContainedAndNotNullAndEquals(dataUnderTest, 'to', '19.08.2013');
		assertContainedAndNotNullAndEquals(dataUnderTest, 'toHour', '13:00');
		assertContainedAndNotNullAndEquals(dataUnderTest, 'debug', false);
	}

	/**
	 * Test for inner class {@link CsiDashboardController.ShowAllCommand}.
	 */
	@Test
	public void testShowAllCommand_testCopyRequestDataToViewModelMap_defaultsForMissingValues()
	{
		// Create and fill a command:
		ShowAllCommand out = new ShowAllCommand()
		// form = '18.08.2013'
		out.from = new Date(1376776800000L)
		out.fromHour = null // Missing!
				out.to = new Date(1376863200000L)
		out.toHour = null // Missing!
				out.aggrGroup = null // Missing!
		out.selectedFolder = [1L]
		out.selectedPages = [1L, 5L]
		out.selectedMeasuredEventIds = [7L, 8L, 9L]
		out.selectedBrowsers = [2L]
		out.selectedLocations = [17L]
		out.selectedAllBrowsers = false
		out.selectedAllLocations = false
		out.selectedAllMeasuredEvents = false
		out.selectedAggrGroupValuesCached = [ AggregatorType.RESULT_CACHED_LOAD_TIME ]
		out.debug = false
		out.setFromHour = false
		out.setToHour = false

		// Run the test:
		Map<String, Object> dataUnderTest = new HashMap<String, Object>();
		out.copyRequestDataToViewModelMap(dataUnderTest);

		// Verification:
		assertEquals(24, dataUnderTest.size());

		assertContainedAndNotNullAndEquals(dataUnderTest, 'selectedFolder', [1L]);
		assertContainedAndNotNullAndEquals(dataUnderTest, 'selectedPages', [1L, 5L]);
		assertContainedAndNotNullAndEquals(dataUnderTest, 'selectedAllMeasuredEvents', false);
		assertContainedAndNotNullAndEquals(dataUnderTest, 'selectedMeasuredEventIds', [7L, 8L, 9L]);
		assertContainedAndNotNullAndEquals(dataUnderTest, 'selectedAllBrowsers', false);
		assertContainedAndNotNullAndEquals(dataUnderTest, 'selectedBrowsers', [2L]);
		assertContainedAndNotNullAndEquals(dataUnderTest, 'selectedAllLocations', false);
		assertContainedAndNotNullAndEquals(dataUnderTest, 'selectedLocations', [17L]);

		assertContainedAndNotNullAndEquals(dataUnderTest, 'from', '18.08.2013');

		assertContainedAndNotNullAndEquals(dataUnderTest, 'to', '19.08.2013');
		assertContainedAndNotNullAndEquals(dataUnderTest, 'debug', false);
	}

	/**
	 * Test for inner class {@link CsiDashboardController.ShowAllCommand}.
	 */
	@Test
	public void testShowAllCommand_testCopyRequestDataToViewModelMap_selectAllSelection()
	{
		// Create and fill a command:
		ShowAllCommand out = new ShowAllCommand()
		// form = '18.08.2013'
		out.from = new Date(1376776800000L)
		out.fromHour = "12:00"
				// to = '19.08.2013'
		out.to = new Date(1376863200000L)
		out.toHour = "13:00"
				out.aggrGroup = AggregatorType.RESULT_CACHED_DOC_COMPLETE_INCOMING_BYTES.toString()
		out.selectedFolder = [1L]
		out.selectedPages = [1L, 5L]
		out.selectedAllMeasuredEvents = 'on'
		out.selectedMeasuredEventIds = []
		out.selectedBrowsers = [2L]
		out.selectedLocations = [17L]
		out.selectedAllBrowsers = false
		out.selectedAllLocations = false
		out.selectedAggrGroupValuesCached = [ AggregatorType.RESULT_CACHED_LOAD_TIME ]
		out.debug = true
		out.setFromHour = false
		out.setToHour = false

		// Do we fill all fields?
		assertTrue(out.validate())

		// Run the test:
		Map<String, Object> dataUnderTest = new HashMap<String, Object>();
		out.copyRequestDataToViewModelMap(dataUnderTest);

		// Verification:
		assertEquals(26, dataUnderTest.size());

		assertContainedAndNotNullAndEquals(dataUnderTest, 'selectedFolder', [1L]);
		assertContainedAndNotNullAndEquals(dataUnderTest, 'selectedPages', [1L, 5L]);
		assertContainedAndNotNullAndEquals(dataUnderTest, 'selectedAllMeasuredEvents', true);
		assertContainedAndNotNullAndEquals(dataUnderTest, 'selectedMeasuredEventIds', []);
		assertContainedAndNotNullAndEquals(dataUnderTest, 'selectedAllBrowsers', false);
		assertContainedAndNotNullAndEquals(dataUnderTest, 'selectedBrowsers', [2L]);
		assertContainedAndNotNullAndEquals(dataUnderTest, 'selectedAllLocations', false);
		assertContainedAndNotNullAndEquals(dataUnderTest, 'selectedLocations', [17L]);

		assertContainedAndNotNullAndEquals(dataUnderTest, 'from', '18.08.2013');
		assertContainedAndNotNullAndEquals(dataUnderTest, 'fromHour', "12:00");

		assertContainedAndNotNullAndEquals(dataUnderTest, 'to', '19.08.2013');
		assertContainedAndNotNullAndEquals(dataUnderTest, 'toHour', "13:00");
		assertContainedAndNotNullAndEquals(dataUnderTest, 'debug', true);
	}

	/**
	 * Test for inner class {@link CsiDashboardController.ShowAllCommand}.
	 */
	@Test
	public void testShowAllCommand_testCreateMvQueryParams()
	{
		// Create and fill a command:
		ShowAllCommand out = new ShowAllCommand()
		
		// form = '18.08.2013'
		out.from = new Date(1376776800000L)
		out.fromHour = "12:00"
		// to = '19.08.2013'
		out.to = new Date(1376863200000L)
		out.toHour = "13:00"
		out.aggrGroup = AggregatorType.RESULT_CACHED_DOC_COMPLETE_INCOMING_BYTES.toString()
		out.selectedFolder = [1L]
		out.selectedPages = [1L, 5L]
		out.selectedMeasuredEventIds = [7L, 8L, 9L]
		out.selectedBrowsers = [2L]
		out.selectedLocations = [17L]
		out.selectedAggrGroupValuesCached = [AggregatorType.RESULT_CACHED_DOC_COMPLETE_INCOMING_BYTES]
		out.selectedAllMeasuredEvents = false
		out.selectedAllBrowsers = false
		out.selectedAllLocations = false
        out.selectedTimeFrameInterval = 0

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
		ShowAllCommand out = new ShowAllCommand()
		// form = '18.08.2013'
		out.from = new Date(1376776800000L)
		out.fromHour = "12:00"
		// to = '19.08.2013'
		out.to = new Date(1376863200000L)
		out.toHour = "13:00"
		out.aggrGroup = AggregatorType.RESULT_CACHED_DOC_COMPLETE_INCOMING_BYTES.toString()
		out.selectedFolder = [1L]
		out.selectedPages = [1L, 5L]
		out.selectedAllMeasuredEvents = 'on';
		out.selectedMeasuredEventIds = [7L, 8L, 9L]
		out.selectedBrowsers = [2L]
		out.selectedLocations = [17L]
		out.selectedAllBrowsers = false
		out.selectedAllLocations = false
		out.selectedAggrGroupValuesCached = [ AggregatorType.RESULT_CACHED_LOAD_TIME ]

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
		ShowAllCommand out = new ShowAllCommand()
		// form = '18.08.2013'
		out.from = new Date(1376776800000L)
		out.fromHour = "12:00"
		// to = '19.08.2013'
		out.to = new Date(1376863200000L)
		out.toHour = "13:00"
		out.aggrGroup = AggregatorType.RESULT_CACHED_DOC_COMPLETE_INCOMING_BYTES.toString()
		out.selectedFolder = [1L]
		out.selectedPages = [1L, 5L]
		out.selectedMeasuredEventIds = [7L, 8L, 9L]
		out.selectedAllBrowsers = true;
		out.selectedBrowsers = [2L]
		out.selectedLocations = [17L]
		out.selectedAllLocations = false
		out.selectedAllMeasuredEvents = false
		out.selectedAggrGroupValuesCached = [ AggregatorType.RESULT_CACHED_LOAD_TIME ]

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
		ShowAllCommand out = new ShowAllCommand()
		// form = '18.08.2013'
		out.from = new Date(1376776800000L)
		out.fromHour = "12:00"
		// to = '19.08.2013'
		out.to = new Date(1376863200000L)
		out.toHour = "13:00"
		out.aggrGroup = AggregatorType.RESULT_CACHED_DOC_COMPLETE_INCOMING_BYTES.toString()
		out.selectedFolder = [1L]
		out.selectedPages = [1L, 5L]
		out.selectedMeasuredEventIds = [7L, 8L, 9L]
		out.selectedBrowsers = [2L]
		out.selectedAllLocations = true;
		out.selectedLocations = [17L]
		out.selectedAllBrowsers = false
		out.selectedAllMeasuredEvents = false
		out.selectedAggrGroupValuesCached = [ AggregatorType.RESULT_CACHED_LOAD_TIME ]
		
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
		ShowAllCommand out = new ShowAllCommand()

		// Should be invalid:
		assertFalse(out.validate())

		// Run the test:
		out.createMvQueryParams();
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
		assertNotNull('dataUnderTest', dataUnderTest)
		assertNotNull('key', key)
		assertNotNull('expectedValue', expectedValue)

		assertTrue('Map must contain key \"' + key + '\"', dataUnderTest.containsKey(key))
		assertNotNull('Map must contain a not-null value for key \"' + key + '\"', dataUnderTest.get(key))
		assertEquals(expectedValue, dataUnderTest.get(key))
	}
}

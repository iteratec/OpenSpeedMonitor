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

import de.iteratec.osm.csi.CsiConfiguration
import de.iteratec.osm.csi.TestDataUtil

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertFalse
import static org.junit.Assert.assertTrue
import grails.test.mixin.*

import java.util.regex.Pattern

import org.junit.Before
import org.junit.Test

import de.iteratec.osm.measurement.schedule.Job
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.measurement.schedule.JobService
import de.iteratec.osm.csi.Page
import de.iteratec.osm.measurement.environment.Browser
import de.iteratec.osm.measurement.environment.Location
import de.iteratec.osm.measurement.environment.WebPageTestServer

/**
 * Test-suite of {@link CsiAggregationTagService}.
 * 
 * @author nkuhn
 * @author mze
 */
@TestFor(CsiAggregationTagService)
@Mock([Job, JobResult, EventResult, Page, JobGroup, Browser, Location, MeasuredEvent, WebPageTestServer, CsiConfiguration])
class CsiAggregationTagServiceTests {

	static final Integer eventResultStatus = 200
	static final String jobGroupName1 = 'myJobGroup'
	static final String jobGroupName2 = 'myJobGroup2'
	static final String jobGroupName3 = 'myJobGroup3'
	static final String jobLabel = 'myJobLabel'
	static final String pageName1 = 'myPageName'
	static final String pageName2 = 'myPageName2'
	static final String pageName3 = 'myPageName3'
	static final String pageName4 = 'myPageName4'
	static final String testId = 'myTestId'
	static final String browserName1 = 'browserName1'
	static final String browserName2 = 'browserName2'

	CsiAggregationTagService serviceUnderTest

	@Before
	void setUp() {
		serviceUnderTest = service

		Page page  = new Page(name: pageName1).save(validate: false)
		new Page(name: pageName2).save(validate: false)
		new Page(name: pageName3).save(validate: false)
		new Page(name: pageName4).save(validate: false)
		Browser browser1 = new Browser(name: browserName1).save(validate: false)
		Browser browser2 = new Browser(name: browserName2).save(validate: false)
		Location location = new Location(browser: browser1).save(validate: false)

		JobGroup jobGroup = new JobGroup(name: jobGroupName1).save(validate: false)
		Job job = new Job(label: jobLabel, location: location, jobGroup: jobGroup).save(validate: false)
		JobResult jobResult = new JobResult(job: job, testId: testId).save(validate: false)
		MeasuredEvent measuredEvent = new MeasuredEvent(testedPage: page).save(validate: false)

		EventResult eventResult=new EventResult(jobResult: jobResult, wptStatus: eventResultStatus, measuredEvent: measuredEvent).save(validate: false)
		
		jobResult.save(failOnError: true, validate: false)

		new JobGroup(name: jobGroupName2).save(validate: false)
		new JobGroup(name: jobGroupName3).save(validate: false)
	}

	@Test
	public void testCreateHourlyAggregatorTag() {
		// Create some data:
		JobGroup jobGroup = new JobGroup(name: 'Lhoste CSI').save(validate: false)
		Page page = new Page(name: 'Homepage').save(validate: false)
		MeasuredEvent measuredEvent = new MeasuredEvent(testedPage: page).save(validate: false)
		Browser browser = new Browser(name: 'Firefox').save(validate: false)
		Location location = new Location(browser: browser).save(validate: false)

		// Define expectation
		Serializable jobGroupId = jobGroup.ident()
		Serializable measuredEventId = measuredEvent.ident()
		Serializable pageId = page.ident()
		Serializable browserId = browser.ident()
		Serializable locationId = location.ident()

		String expectedTag = jobGroupId.toString() +
				';' +
				measuredEventId.toString() +
				';' +
				pageId.toString() +
				';' +
				browserId.toString() +
				';' +
				locationId.toString()

		// Run the test
		String tagCreated = serviceUnderTest.createHourlyEventTag(jobGroup, measuredEvent, page, browser, location)

		// Verify result
		assertEquals(expectedTag, tagCreated)

		// Could we recover the data based to create the tag? (we just compare the ident()-results to verify)
		JobGroup foundJobGroup = serviceUnderTest.findJobGroupOfHourlyEventTag(tagCreated)
		assertEquals(jobGroup.ident(), foundJobGroup.ident())

		Page foundPage = serviceUnderTest.findPageOfHourlyEventTag(tagCreated)
		assertEquals(page.ident(), foundPage.ident())

		MeasuredEvent foundMeasuredEvent = serviceUnderTest.findMeasuredEventOfHourlyEventTag(tagCreated)
		assertEquals(measuredEvent.ident(), foundMeasuredEvent.ident())

		Browser foundBrowser = serviceUnderTest.findBrowserOfHourlyEventTag(tagCreated)
		assertEquals(browser.ident(), foundBrowser.ident())

		Location foundLocation = serviceUnderTest.findLocationOfHourlyEventTag(tagCreated)
		assertEquals(location.ident(), foundLocation.ident())
	}

	@Test(expected=IllegalArgumentException.class)
	public void testFindJobGroupOfHourlyAggregatorTag_InvalidTag_ToLessSegmentCount() {
		serviceUnderTest.findJobGroupOfHourlyEventTag('1;2;3;4')
	}

	@Test(expected=IllegalArgumentException.class)
	public void testFindJobGroupOfHourlyAggregatorTag_InvalidTag_ToHighSegmentCount() {
		serviceUnderTest.findJobGroupOfHourlyEventTag('1;2;3;4;5;6')
	}

	@Test(expected=NullPointerException.class)
	public void testCreateHourlyAggregatorTag_LocationIsNull() {
		// Create some data:
		JobGroup jobGroup = new JobGroup(name: 'Lhoste CSI').save(validate: false)
		Page page = new Page(name: 'Homepage').save(validate: false)
		MeasuredEvent measuredEvent = new MeasuredEvent(testedPage: page).save(validate: false)
		Browser browser = new Browser(name: 'Firefox').save(validate: false)
		Location location = null

		// Run the test
		String tagCreated = serviceUnderTest.createHourlyEventTag(jobGroup, measuredEvent, page, browser, location)
	}

	@Test(expected=IllegalArgumentException.class)
	public void testCreateHourlyAggregatorTag_LocationNotSavedBefore() {
		// Create some data:
		JobGroup jobGroup = new JobGroup(name: 'Lhoste CSI').save(validate: false)
		Page page = new Page(name: 'Homepage').save(validate: false)
		MeasuredEvent measuredEvent = new MeasuredEvent(testedPage: page).save(validate: false)
		Browser browser = new Browser(name: 'Firefox').save(validate: false)
		Location location = new Location(browser: browser) // Not saved => no id

		// Run the test
		String tagCreated = serviceUnderTest.createHourlyEventTag(jobGroup, measuredEvent, page, browser, location)
	}

	@Test
	public void testGetTagPatternForHourlyCsiAggregations() {
		// Create a query request:
		MvQueryParams queryParams = new MvQueryParams();
		queryParams.jobGroupIds.addAll([8, 9]);
		queryParams.measuredEventIds.addAll([38, 77]);
		queryParams.pageIds.addAll([1, 3, 8]);
		queryParams.browserIds.addAll([7]);
		queryParams.locationIds.addAll([99, 101]);

		// Define expectation
		String expectedPatternAsString = '^(8|9);(38|77);(1|3|8);(7);(99|101)$'

		// Run the test
		Pattern createdPattern = serviceUnderTest.getTagPatternForHourlyCsiAggregations(queryParams)

		// Verify results
		assertEquals(expectedPatternAsString, createdPattern.toString())
	}
	
	@Test
	public void testGetTagPatternForHourlyCsiAggregations_EmptyGroups_CreatesPatternToFindAllGroups() {
		// Create a query request:
		MvQueryParams queryParams = new MvQueryParams();
		assertTrue('Here is the empty one!', queryParams.jobGroupIds.isEmpty());
		queryParams.measuredEventIds.addAll([38, 77]);
		queryParams.pageIds.addAll([1, 3, 8]);
		queryParams.browserIds.addAll([7]);
		queryParams.locationIds.addAll([99, 101]);

		// Define expectation
		String expectedPatternAsString = '^[^;]*;(38|77);(1|3|8);(7);(99|101)$'

		// Run the test
		Pattern createdPattern = serviceUnderTest.getTagPatternForHourlyCsiAggregations(queryParams)

		// Verify results
		assertEquals(expectedPatternAsString, createdPattern.toString())
	}
	
	@Test
	public void testGetTagPatternForHourlyCsiAggregations_EmptyPages_CreatesPatternToFindAllPages() {
		// Create a query request:
		MvQueryParams queryParams = new MvQueryParams();
		queryParams.jobGroupIds.addAll([8, 9]);
		queryParams.measuredEventIds.addAll([38, 77]);
		assertTrue('Here is the empty one!', queryParams.pageIds.isEmpty());
		queryParams.browserIds.addAll([7]);
		queryParams.locationIds.addAll([99, 101]);

		// Define expectation
		String expectedPatternAsString = '^(8|9);(38|77);[^;]*;(7);(99|101)$'

		// Run the test
		Pattern createdPattern = serviceUnderTest.getTagPatternForHourlyCsiAggregations(queryParams)

		// Verify results
		assertEquals(expectedPatternAsString, createdPattern.toString())
	}

	@Test
	void testCreatePageAggregatorTagByEventResultSuccessfully() {
		// Select test data:
		JobGroup group = JobGroup.findByName(jobGroupName1)
		CsiConfiguration csiConfiguration = TestDataUtil.createCsiConfiguration()
		group.csiConfiguration = csiConfiguration
		EventResult result = EventResult.findByWptStatus(eventResultStatus)

		// Mock required services
		mockJobService(group)
		mockJobResultService(JobResult.findByTestId(testId))

		// Expectation:
		String expectedTag = group.ident().toString() + ';' + Page.findByName(pageName1).ident().toString()

		// Run the test:
		String tag = serviceUnderTest.createPageAggregatorTagByEventResult(result)

		// Verify result:
		assertNotNull tag
		assertEquals(expectedTag, tag)
	}

	@Test
	void testCreatePageAggregatorTagByEventResultWithNotCsiJobGroup() {
		// Select test data:
		JobGroup group = JobGroup.findByName(jobGroupName1)
		EventResult result = EventResult.findByWptStatus(eventResultStatus)

		// Mock required services
		mockJobService(new JobGroup(name: 'bla'))
		mockJobResultService(JobResult.findByTestId(testId))

		// Run the test:
		String tag = serviceUnderTest.createPageAggregatorTagByEventResult(result)

		// Verify result:
		assertNull tag
	}

	@Test
	void testCreatePageAggregatorTagByEventResultWithNullJobGroup() {
		// Select test data:
		EventResult result = EventResult.findByWptStatus(eventResultStatus)

		// Mock required services
		mockJobService(null)
		mockJobResultService(JobResult.findByTestId(testId))

		// Run the test:
		String tag = serviceUnderTest.createPageAggregatorTagByEventResult(result)

		// Verify result:
		assertNull tag
	}

	@Test
	void testCreateWeeklyPageAggregatorTag() {
		// Select test data:
		JobGroup group = JobGroup.findByName(jobGroupName1)
		Page page = Page.findByName(pageName1)

		// Expectation:
		String expectedTag = group.ident().toString() + ';' + page.ident().toString()

		// Run the test:
		String tag = serviceUnderTest.createPageAggregatorTag(group, page)

		// Verify result:
		assertEquals(expectedTag, tag)
	}

	@Test(expected=NullPointerException.class)
	void testCreateWeeklyPageAggregatorTag_JobGroupIsNull() {
		// Select test data:
		JobGroup group = JobGroup.findByName(jobGroupName1)

		// Run the test:
		String tag = serviceUnderTest.createPageAggregatorTag(group, null)
	}

	@Test(expected=NullPointerException.class)
	void testCreateWeeklyPageAggregatorTag_PageIsNull() {
		// Select test data:
		Page page = Page.findByName(pageName1)

		// Run the test:
		String tag = serviceUnderTest.createPageAggregatorTag(null, page)
	}

	@Test
	void testCreateWeeklyShopAggregatorTag() {
		// Select test data:
		JobGroup group = JobGroup.findByName(jobGroupName1)

		// Expectation:
		String expectedTag = group.ident().toString()

		// Run the test:
		String tag = serviceUnderTest.createShopAggregatorTag(group)

		// Verify result:
		assertEquals(expectedTag, tag)
	}

	@Test(expected=NullPointerException.class)
	void testCreateWeeklyShopAggregatorTag_NullArg() {
		String tag = serviceUnderTest.createShopAggregatorTag(null)
	}

	@Test
	void testGetTagPatternForWeeklyPageMvsWithJobGroupsAndPages_groups_1_2_3_pages_1_2() {
		// Select test data:
		JobGroup group1 = JobGroup.findByName(jobGroupName1)
		JobGroup group2 = JobGroup.findByName(jobGroupName2)
		JobGroup group3 = JobGroup.findByName(jobGroupName3)
		Page page1 = Page.findByName(pageName1)
		Page page2 = Page.findByName(pageName2)

		// Expectation:
		Pattern expectedPattern = ~/^(1|2|3);(1|2)$/

		// Run the test:
		Pattern tagPattern = serviceUnderTest.getTagPatternForWeeklyPageCasWithJobGroupsAndPages([group1, group2, group3], [page1, page2])

		// Verify result:
		assertEquals(expectedPattern.pattern(), tagPattern.pattern())
	}
	
	@Test
	void testGetTagPatternForWeeklyPageMvsWithJobGroupsAndPages_groups_1_2_3_pages_empty_findAllPages() {
		// Select test data:
		JobGroup group1 = JobGroup.findByName(jobGroupName1)
		JobGroup group2 = JobGroup.findByName(jobGroupName2)
		JobGroup group3 = JobGroup.findByName(jobGroupName3)

		// Expectation:
		Pattern expectedPattern = ~/^(1|2|3);[^;]*$/

		// Run the test:
		Pattern tagPattern = serviceUnderTest.getTagPatternForWeeklyPageCasWithJobGroupsAndPages([group1, group2, group3], [])

		// Verify result:
		assertEquals(expectedPattern.pattern(), tagPattern.pattern())
	}

	@Test
	void testGetTagPatternForWeeklyPageMvsWithJobGroupsAndPages_group_3_pages_1_3() {
		// Select test data:
		JobGroup group3 = JobGroup.findByName(jobGroupName3)
		Page page1 = Page.findByName(pageName1)
		Page page3 = Page.findByName(pageName3)

		// Expectation:
		Pattern expectedPattern = ~/^(3);(1|3)$/

		// Run the test:
		Pattern tagPattern = serviceUnderTest.getTagPatternForWeeklyPageCasWithJobGroupsAndPages([group3], [page1, page3])

		// Verify result:
		assertEquals(expectedPattern.pattern(), tagPattern.pattern())
	}
	
	@Test(expected=NullPointerException.class)
	void testGetTagPatternForWeeklyPageMvsWithJobGroupsAndPages_group_null_pages_1_3() {
		// Select test data:
		Page page1 = Page.findByName(pageName1)
		Page page3 = Page.findByName(pageName3)

		// Run the test should fail:
		Pattern tagPattern = serviceUnderTest.getTagPatternForWeeklyPageCasWithJobGroupsAndPages(null, [page1, page3])
	}
	
	@Test(expected=NullPointerException.class)
	void testGetTagPatternForWeeklyPageMvsWithJobGroupsAndPages_group_3_pages_null() {
		// Select test data:
		JobGroup group3 = JobGroup.findByName(jobGroupName3)

		// Run the test should fail:
		Pattern tagPattern = serviceUnderTest.getTagPatternForWeeklyPageCasWithJobGroupsAndPages([group3], null)
	}

	@Test
	void testGetTagPatternForWeeklyShopMvsWithJobGroups(){
		// Select test data:
		JobGroup group1 = JobGroup.findByName(jobGroupName1)
		JobGroup group2 = JobGroup.findByName(jobGroupName2)
		JobGroup group3 = JobGroup.findByName(jobGroupName3)

		// Expectation:
		Pattern expectedPattern = ~/^(1|2|3)$/

		// Run the test:
		Pattern tagPattern = serviceUnderTest.getTagPatternForWeeklyShopCasWithJobGroups([group1, group2, group3])

		// Verify result:
		assertEquals(expectedPattern.pattern(), tagPattern.pattern())
	}
	
	@Test
	void testGetTagPatternForWeeklyShopMvsWithJobGroups_EmptyGroups(){
		// Expectation:
		Pattern expectedPattern = ~/^[^;]*$/

		// Run the test:
		Pattern tagPattern = serviceUnderTest.getTagPatternForWeeklyShopCasWithJobGroups([])

		// Verify result:
		assertEquals(expectedPattern.pattern(), tagPattern.pattern())
	}
	
	@Test
	void testFindJobGroupOfShopTag(){
		// Select/create test data
		JobGroup group1 = JobGroup.findByName(jobGroupName1)
		String weeklyShopTag = serviceUnderTest.createShopAggregatorTag(group1)
		//test execution
		JobGroup resultingJobGroup = serviceUnderTest.findJobGroupOfWeeklyShopTag(weeklyShopTag)
		//assertions
		assertEquals(group1, resultingJobGroup)
	}
	
	@Test
	void testFindJobGroupOfWeeklyPageTag(){
		// Select/create test data
		JobGroup group = JobGroup.findByName(jobGroupName1)
		Page page = Page.findByName(pageName1)
		String weeklyPageTag = serviceUnderTest.createPageAggregatorTag(group, page)
		//test execution
		JobGroup resultingJobGroup = serviceUnderTest.findJobGroupOfWeeklyPageTag(weeklyPageTag)
		//assertions
		assertEquals(group, resultingJobGroup)
	}
	
	@Test
	void testFindPageByPageTag(){
		// Select/create test data
		JobGroup group = JobGroup.findByName(jobGroupName1)
		Page page = Page.findByName(pageName1)
		String weeklyPageTag = serviceUnderTest.createPageAggregatorTag(group, page)
		//test execution
		Page resultingPage = serviceUnderTest.findPageByPageTag(weeklyPageTag)
		//assertions
		assertEquals(page, resultingPage)
	}
	
	@Test
	void testProofHourlyEventTag(){
		// Select/create test data
		String valid = '1;1;1;1;1'
		String invalidTooLittleNumberOfSegments = '1;1;1;1'
		String invalidTooLargeNumberOfSegments = '1;1;1;1;1;1'
		String invalidCauseJobGroupDoesntExist = '5;1;1;1;1'
		String invalidCauseEventDoesntExist = '1;5;1;1;1'
		String invalidCausePageDoesntExist = '1;1;5;1;1'
		String invalidCauseBrowserDoesntExist = '1;1;1;5;1'
		String invalidCauseLocationDoesntExist = '1;1;1;1;5'
		//test execution and assertions
		assertTrue(serviceUnderTest.isValidHourlyEventTag(valid))
		assertFalse(serviceUnderTest.isValidHourlyEventTag(invalidTooLittleNumberOfSegments))
		assertFalse(serviceUnderTest.isValidHourlyEventTag(invalidTooLargeNumberOfSegments))
		assertFalse(serviceUnderTest.isValidHourlyEventTag(invalidCauseJobGroupDoesntExist))
		assertFalse(serviceUnderTest.isValidHourlyEventTag(invalidCauseEventDoesntExist))
		assertFalse(serviceUnderTest.isValidHourlyEventTag(invalidCausePageDoesntExist))
		assertFalse(serviceUnderTest.isValidHourlyEventTag(invalidCauseBrowserDoesntExist))
		assertFalse(serviceUnderTest.isValidHourlyEventTag(invalidCauseLocationDoesntExist))
	}

	//mock services

	private void mockJobService(toReturn){
		def jobServiceMocked = mockFor(JobService)
		jobServiceMocked.demand.getCsiJobGroupOf(Job.findByLabel(jobLabel)) { Job jobConfig -> return toReturn }
		serviceUnderTest.jobService = jobServiceMocked.createMock()
	}
	private mockJobResultService(JobResult toReturn){
		def jobResultDaoServiceMocked = mockFor(JobResultDaoService)
		jobResultDaoServiceMocked.demand.findJobResultByEventResult(1..10000) { EventResult eventResult -> return toReturn }
		serviceUnderTest.jobResultDaoService = jobResultDaoServiceMocked.createMock()
	}
}

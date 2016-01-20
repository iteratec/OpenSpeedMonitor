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

import de.iteratec.osm.util.ServiceMocker
import org.junit.Assert

import static org.junit.Assert.assertEquals
import grails.test.mixin.*

import java.util.regex.Pattern

import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.junit.After
import org.junit.Before
import org.junit.Test

import de.iteratec.osm.report.chart.MeasuredValueDaoService
import de.iteratec.osm.report.chart.MeasuredValueUtilService
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.measurement.schedule.JobGroupType
import de.iteratec.osm.report.chart.AggregatorType
import de.iteratec.osm.report.chart.MeasurandGroup
import de.iteratec.osm.report.chart.MeasuredValue
import de.iteratec.osm.report.chart.MeasuredValueInterval
import de.iteratec.osm.report.chart.MeasuredValueUpdateEvent
import de.iteratec.osm.report.chart.MeasuredValueUpdateEventDaoService
import de.iteratec.osm.csi.weighting.WeightFactor
import de.iteratec.osm.csi.weighting.WeightedCsiValue
import de.iteratec.osm.csi.weighting.WeightedValue
import de.iteratec.osm.csi.weighting.WeightingService
import de.iteratec.osm.result.EventResult
import de.iteratec.osm.result.MeasuredEvent
import de.iteratec.osm.result.MeasuredValueTagService
import de.iteratec.osm.result.MvQueryParams
import de.iteratec.osm.result.dao.DefaultMeasuredEventDaoService
import de.iteratec.osm.util.PerformanceLoggingService
import de.iteratec.osm.measurement.environment.Browser
import de.iteratec.osm.measurement.environment.Location

/**
 * Test-suite of {@link PageMeasuredValueService}. 
 */
@TestFor(PageMeasuredValueService)
@Mock([MeasuredValue, MeasuredValueInterval, AggregatorType, JobGroup, Page, MeasuredEvent, Browser, Location, 
	EventResult, MeasuredValueDaoService, DefaultMeasuredEventDaoService, EventMeasuredValueService,
	CustomerSatisfactionWeightService,Day, MeanCalcService, MeasuredValueUpdateEvent])
class PageMeasuredValueServiceTests {

	static final double DELTA = 1e-15
	
	MeasuredValueInterval weeklyInterval, dailyInterval, hourlyInterval
	JobGroup jobGroup1, jobGroup2, jobGroup3
	Map<String,JobGroup> allCsiGroups
	Page page1, page2, page3
	Map<String,Page> allPages
	AggregatorType pageAggregator;
	Browser browser;
	
	DateTime startDate = new DateTime(2013,5,16,0,0,0)
	String jobGroupName1 = 'myJobGroup1'
	String jobGroupName2 = 'myJobGroup2'
	String jobGroupName3 = 'myJobGroup3'
	String pageName1 = 'myPageName1'
	String pageName2 = 'myPageName2'
	String pageName3 = 'myPageName3'

	PageMeasuredValueService serviceUnderTest

	@Before
	void setUp() {
		serviceUnderTest = service
		
		//mocks common for all tests 
		serviceUnderTest.measuredEventDaoService = new DefaultMeasuredEventDaoService();
		serviceUnderTest.eventMeasuredValueService = new EventMeasuredValueService();
		serviceUnderTest.measuredValueUtilService = new MeasuredValueUtilService();
		serviceUnderTest.performanceLoggingService = new PerformanceLoggingService()
		mockMeasuredValueUpdateEventDaoService()
		
		//test-data common for all tests
		createTestDataForAllTests()
    }
	@After
	void tearDown() {
		deleteTestData()
	}
	
	//tests////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@Test
	void testFindAll() {
		Assert.assertEquals(10, serviceUnderTest.findAll(startDate.toDate(), startDate.toDate(), weeklyInterval).size())
	}

	@Test
	void testFindAllByJobGroupsAndPages(){
		mockMeasuredValueDaoService()
		
		//getting test-specific data
		
		JobGroup group1 = JobGroup.findByName(jobGroupName1)
		JobGroup group2 = JobGroup.findByName(jobGroupName2)
		JobGroup group3 = JobGroup.findByName(jobGroupName3)
		Page page1 = Page.findByName(pageName1)
		Page page2 = Page.findByName(pageName2)
		Page page3 = Page.findByName(pageName3)

		Integer with_group1to3_page1to3 = 5
		Integer with_group1_page1to3 = 2
		Integer with_group2_page1to3 = 0
		Integer with_group3_page1to3 = 3
		Integer with_group1to3_page1 = 2
		Integer with_group1to3_page2 = 2
		Integer with_group1to3_page3 = 1
		Integer with_group1to2_page3 = 0
		Integer with_group1to2_page1 = 1
		Integer with_group3_page1or3 = 2
		
		//test execution, mocking inner services and assertions
		
		Map<String,JobGroup> groups = ['1':group1, '2':group2, '3':group3]
		Map<String,Page> pages = ['1':page1, '2':page2, '3':page3]
		List<JobGroup> groupsAsList = groups.values().toList()
		List<Page> pagesAsList = pages.values().toList()
		mockMeasuredValueTagService(groups, pages)
		Assert.assertEquals(with_group1to3_page1to3, serviceUnderTest.findAll(startDate.toDate(), startDate.toDate(), weeklyInterval, groupsAsList, pagesAsList).size())
		groups = ['1':group1]
		pages = ['1':page1, '2':page2, '3':page3]
		mockMeasuredValueTagService(groups, pages)
		Assert.assertEquals(with_group1_page1to3, serviceUnderTest.findAll(startDate.toDate(), startDate.toDate(), weeklyInterval, groupsAsList, pagesAsList).size() )
		groups = ['1':group2]
		pages = ['1':page1, '2':page2, '3':page3]
		mockMeasuredValueTagService(groups, pages)
		Assert.assertEquals(with_group2_page1to3, serviceUnderTest.findAll(startDate.toDate(), startDate.toDate(), weeklyInterval, groupsAsList, pagesAsList).size())
		groups = ['1':group3]
		pages = ['1':page1, '2':page2, '3':page3]
		mockMeasuredValueTagService(groups, pages)
		Assert.assertEquals(with_group3_page1to3, serviceUnderTest.findAll(startDate.toDate(), startDate.toDate(), weeklyInterval, groupsAsList, pagesAsList).size())
		groups = ['1':group1, '2':group2, '3':group3]
		pages = ['1':page1]
		mockMeasuredValueTagService(groups, pages)
		Assert.assertEquals(with_group1to3_page1, serviceUnderTest.findAll(startDate.toDate(), startDate.toDate(), weeklyInterval, groupsAsList, pagesAsList).size())
		groups = ['1':group1, '2':group2, '3':group3]
		pages = ['1':page2]
		mockMeasuredValueTagService(groups, pages)
		Assert.assertEquals(with_group1to3_page2, serviceUnderTest.findAll(startDate.toDate(), startDate.toDate(), weeklyInterval, groupsAsList, pagesAsList).size())
		groups = ['1':group1, '2':group2, '3':group3]
		pages = ['1':page3]
		mockMeasuredValueTagService(groups, pages)
		Assert.assertEquals(with_group1to3_page3, serviceUnderTest.findAll(startDate.toDate(), startDate.toDate(), weeklyInterval, groupsAsList, pagesAsList).size())
		groups = ['1':group1, '2':group2]
		pages = ['1':page3]
		mockMeasuredValueTagService(groups, pages)
		Assert.assertEquals(with_group1to2_page3, serviceUnderTest.findAll(startDate.toDate(), startDate.toDate(), weeklyInterval, groupsAsList, pagesAsList).size())
		groups = ['1':group1, '2':group2]
		pages = ['1':page1]
		mockMeasuredValueTagService(groups, pages)
		Assert.assertEquals(with_group1to2_page1, serviceUnderTest.findAll(startDate.toDate(), startDate.toDate(), weeklyInterval, groupsAsList, pagesAsList).size())
		groups = ['1':group3]
		pages = ['1':page1, '2':page3]
		mockMeasuredValueTagService(groups, pages)
		Assert.assertEquals(with_group3_page1or3, serviceUnderTest.findAll(startDate.toDate(), startDate.toDate(), weeklyInterval, groupsAsList, pagesAsList).size())
	}
	
	/**
	 * Tests the marking of dependent (weekly-page-){@link MeasuredValue}s, which aren't calculated when new {@link EventResult}s get persisted.
	 * {@link MeasuredValueUpdateEvent}s with {@link MeasuredValueUpdateEvent#UpdateCause} OUTDATED should be written to db. 
	 */
	@Test
	void testMarkingWeeklyPageMvsAsOutdated(){
		
		//getting test-specific data
		
		EventResult eventResult = new EventResult().save(validate: false)
		DateTime startTimeOfWeek = new DateTime(2013,8,5,0,0,0, DateTimeZone.UTC)
		
		//mocking inner services
		
		mockMeasuredValueTagService(['1':jobGroup1], ['1':page1])
		
		//precondition
		
		List<MeasuredValue> wpmvs = serviceUnderTest.findAll(startTimeOfWeek.toDate(), startTimeOfWeek.toDate(), weeklyInterval) 
		assertEquals(0, wpmvs.size())
		
		//test execution
		serviceUnderTest.markMvAsOutdated(startTimeOfWeek, eventResult, weeklyInterval)
		
		//assertions
		
		wpmvs = serviceUnderTest.findAll(startTimeOfWeek.toDate(), startTimeOfWeek.toDate(), weeklyInterval)
		assertEquals(1, wpmvs.size())
		MeasuredValue wpmvMarked = wpmvs[0]
		assertFalse(wpmvMarked.isCalculated())
		assertTrue(wpmvMarked.hasToBeCalculated())
		
		//second mark of the same weekly-page-mv
		serviceUnderTest.markMvAsOutdated(startTimeOfWeek, eventResult, weeklyInterval)
		wpmvs = serviceUnderTest.findAll(startTimeOfWeek.toDate(), startTimeOfWeek.toDate(), weeklyInterval)
		assertEquals(1, wpmvs.size())
		wpmvMarked = wpmvs[0]
		assertFalse(wpmvMarked.isCalculated())
		assertTrue(wpmvMarked.hasToBeCalculated())
		
	}
	
	/**
	 * Tests the marking of dependent (weekly-page-){@link MeasuredValue}s, which aren't calculated when new {@link EventResult}s get persisted.
	 * {@link MeasuredValueUpdateEvent}s with {@link MeasuredValueUpdateEvent#UpdateCause} OUTDATED should be written to db.
	 */
	@Test
	void testMarkingDailyPageMvsAsOutdated(){
		
		//getting test-specific data
		
		EventResult eventResult = new EventResult().save(validate: false)
		DateTime startTime = new DateTime(2013,8,5,0,0,0, DateTimeZone.UTC)
		
		//mocking inner services
		
		mockMeasuredValueTagService(['1':jobGroup1], ['1':page1])
		
		//precondition
		
		List<MeasuredValue> mvs = serviceUnderTest.findAll(startTime.toDate(), startTime.toDate(), dailyInterval)
		assertEquals(0, mvs.size())
		
		//test execution
		serviceUnderTest.markMvAsOutdated(startTime, eventResult, dailyInterval)
		
		//assertions
		
		mvs = serviceUnderTest.findAll(startTime.toDate(), startTime.toDate(), dailyInterval)
		assertEquals(1, mvs.size())
		MeasuredValue wpmvMarked = mvs[0]
		assertFalse(wpmvMarked.isCalculated())
		assertTrue(wpmvMarked.hasToBeCalculated())
		
		//second mark of the same weekly-page-mv
		serviceUnderTest.markMvAsOutdated(startTime, eventResult, dailyInterval)
		mvs = serviceUnderTest.findAll(startTime.toDate(), startTime.toDate(), dailyInterval)
		assertEquals(1, mvs.size())
		wpmvMarked = mvs[0]
		assertFalse(wpmvMarked.isCalculated())
		assertTrue(wpmvMarked.hasToBeCalculated())
		
	}
	
	/**
	 * Tests calculation of daily-page-{@link MeasuredValue}s, which aren't calculated when new {@link EventResult}s get persisted. 
	 */
	@Test
	void testCalculation_DailyInterval_SingleHourlyMv(){
		DateTime startedTime = new DateTime(2013,5,16,12,12,11)
		
		MeasuredValue hpmv=new MeasuredValue(interval: hourlyInterval, aggregator: AggregatorType.findByName(AggregatorType.MEASURED_EVENT), tag: jobGroup1.ident()+';'+page1.ident()+';1;1;1', started: startedTime.plusMinutes(2).toDate(), resultIds: "1", value: 12d).save(failOnError: true);
		TestDataUtil.createUpdateEvent(hpmv.ident(), MeasuredValueUpdateEvent.UpdateCause.CALCULATED)

		Day testDay = new Day()
		(0..23).each {
			testDay.setHourWeight(it, 1)
		}
		testDay.save(failOnError: true)
		
		List<WeightedCsiValue> weightedCsiValuesToReturnInMock = [
			new WeightedCsiValue(weightedValue: new WeightedValue(value: 12d, weight: 1d), underlyingEventResultIds: [])]
		
		
		//mocking inner services
		
		mockMeasuredValueDaoService()
		mockMeasuredValueTagService(allCsiGroups, allPages)
		mockWeightingService(weightedCsiValuesToReturnInMock)
		
		//precondition
		
		List<MeasuredValue> mvs = serviceUnderTest.findAll(startedTime.toDate(), startedTime.toDate(), dailyInterval)
		assertEquals(0, mvs.size())
		
		//test execution
		List<MeasuredValue> calculatedMvs=serviceUnderTest.getOrCalculatePageMeasuredValues(startedTime.toDate(), startedTime.toDate(), dailyInterval, [jobGroup1], [page1])
		
		//assertions
		
		assertEquals(1, calculatedMvs.size())
		assertTrue(calculatedMvs[0].isCalculated())
		assertEquals(12d, calculatedMvs[0].value, DELTA)
		
		mvs = serviceUnderTest.findAll(startedTime.toDate(), startedTime.toDate(), dailyInterval)
		assertEquals(1, mvs.size())
	}
	
	/**
	 * Tests calculation of daily-page-{@link MeasuredValue}s, which aren't calculated when new {@link EventResult}s get persisted. 
	 */
	@Test
	void testCalculation_DailyInterval_MultipleHourlyMv(){
		DateTime startedTime = new DateTime(2013,5,16,12,12,11)
		
		MeasuredValue mv1 = new MeasuredValue(interval: hourlyInterval, aggregator: AggregatorType.findByName(AggregatorType.MEASURED_EVENT), tag: jobGroup1.ident()+';'+page1.ident()+';1;1;1', started: startedTime.plusMinutes(2).toDate(), resultIds: "1", value: 12d).save(failOnError: true);
		TestDataUtil.createUpdateEvent(mv1.ident(), MeasuredValueUpdateEvent.UpdateCause.CALCULATED)
		MeasuredValue mv2 = new MeasuredValue(interval: hourlyInterval, aggregator: AggregatorType.findByName(AggregatorType.MEASURED_EVENT), tag: jobGroup1.ident()+';'+page1.ident()+';1;1;1', started: startedTime.plusHours(2).toDate(), resultIds: "1", value: 10d).save(failOnError: true);
		TestDataUtil.createUpdateEvent(mv2.ident(), MeasuredValueUpdateEvent.UpdateCause.CALCULATED)
		MeasuredValue mv3 = new MeasuredValue(interval: hourlyInterval, aggregator: AggregatorType.findByName(AggregatorType.MEASURED_EVENT), tag: jobGroup1.ident()+';'+page1.ident()+';1;1;1', started: startedTime.plusHours(10).toDate(), resultIds: "1", value: 11d).save(failOnError: true);
		TestDataUtil.createUpdateEvent(mv3.ident(), MeasuredValueUpdateEvent.UpdateCause.CALCULATED)

		//MVs outside of Interval
		MeasuredValue mv4 = new MeasuredValue(interval: hourlyInterval, aggregator: AggregatorType.findByName(AggregatorType.MEASURED_EVENT), tag: jobGroup1.ident()+';'+page1.ident()+';1;1;1', started: startedTime.minusMinutes(1).toDate(), resultIds: "1", value: 1000d).save(failOnError: true);
		TestDataUtil.createUpdateEvent(mv4.ident(), MeasuredValueUpdateEvent.UpdateCause.CALCULATED)
		MeasuredValue mv5 = new MeasuredValue(interval: hourlyInterval, aggregator: AggregatorType.findByName(AggregatorType.MEASURED_EVENT), tag: jobGroup1.ident()+';'+page1.ident()+';1;1;1', started: startedTime.plusMinutes(MeasuredValueInterval.DAILY+1).toDate(), resultIds: "1", value: 1000d).save(failOnError: true);
		TestDataUtil.createUpdateEvent(mv5.ident(), MeasuredValueUpdateEvent.UpdateCause.CALCULATED)
		
		Day testDay = new Day()
		(0..23).each {
			testDay.setHourWeight(it, 1)
		}
		testDay.save(failOnError: true)
		
		List<WeightedCsiValue> weightedCsiValuesToReturnInMock = [
			new WeightedCsiValue(weightedValue: new WeightedValue(value: 12d, weight: 1d), underlyingEventResultIds: []),
			new WeightedCsiValue(weightedValue: new WeightedValue(value: 10d, weight: 1d), underlyingEventResultIds: []),
			new WeightedCsiValue(weightedValue: new WeightedValue(value: 11d, weight: 1d), underlyingEventResultIds: [])]
		
		//mocking inner services
		
		mockMeasuredValueDaoService()
		mockMeasuredValueTagService(allCsiGroups, allPages)
		mockWeightingService(weightedCsiValuesToReturnInMock)
		
		//precondition
		
		List<MeasuredValue> mvs = serviceUnderTest.findAll(startedTime.toDate(), startedTime.toDate(), dailyInterval)
		assertEquals(0, mvs.size())
		
		//test execution
		List<MeasuredValue> calculatedMvs=serviceUnderTest.getOrCalculatePageMeasuredValues(startedTime.toDate(), startedTime.toDate(), dailyInterval, [jobGroup1], [page1])
		
		//assertions
		
		assertEquals(1, calculatedMvs.size())
		assertTrue(calculatedMvs[0].isCalculated())
		assertEquals(11d, calculatedMvs[0].value, DELTA)
		
		mvs = serviceUnderTest.findAll(startedTime.toDate(), startedTime.toDate(), dailyInterval)
		assertEquals(1, mvs.size())
		
		MeasuredValue wpmvMarked = mvs[0]
		assertTrue(wpmvMarked.isCalculated())
		
	}
	
	/**
	 * Tests calculation of daily-page-{@link MeasuredValue}s, which aren't calculated when new {@link EventResult}s get persisted.
	 */
	@Test
	void testCalculation_DailyInterval_MultipleHourlyMv_differentWeights(){
		
		//test-specific data
		
		DateTime startedTime = new DateTime(2013,5,16,12,12,11)
		
		MeasuredValue mv1 = new MeasuredValue(interval: hourlyInterval, aggregator: AggregatorType.findByName(AggregatorType.MEASURED_EVENT), tag: jobGroup1.ident()+';'+page1.ident()+';1;1;1', started: startedTime.toDate(), resultIds: "1", value: 1d).save(failOnError: true)
		TestDataUtil.createUpdateEvent(mv1.ident(), MeasuredValueUpdateEvent.UpdateCause.CALCULATED)
		MeasuredValue mv2 = new MeasuredValue(interval: hourlyInterval, aggregator: AggregatorType.findByName(AggregatorType.MEASURED_EVENT), tag: jobGroup1.ident()+';'+page1.ident()+';1;1;1', started: startedTime.plusHours(2).toDate(), resultIds: "1", value: 10d).save(failOnError: true)
		TestDataUtil.createUpdateEvent(mv2.ident(), MeasuredValueUpdateEvent.UpdateCause.CALCULATED)
		MeasuredValue mv3 = new MeasuredValue(interval: hourlyInterval, aggregator: AggregatorType.findByName(AggregatorType.MEASURED_EVENT), tag: jobGroup1.ident()+';'+page1.ident()+';1;1;1', started: startedTime.plusHours(10).toDate(), resultIds: "1", value: 11d).save(failOnError: true)
		TestDataUtil.createUpdateEvent(mv3.ident(), MeasuredValueUpdateEvent.UpdateCause.CALCULATED)

		//MVs outside of Interval
		MeasuredValue mv4 = new MeasuredValue(interval: hourlyInterval, aggregator: AggregatorType.findByName(AggregatorType.MEASURED_EVENT), tag: jobGroup1.ident()+';'+page1.ident()+';1;1;1', started: startedTime.minusMinutes(1).toDate(), resultIds: "1", value: 1000d).save(failOnError: true)
		TestDataUtil.createUpdateEvent(mv4.ident(), MeasuredValueUpdateEvent.UpdateCause.CALCULATED)
		MeasuredValue mv5 =new MeasuredValue(interval: hourlyInterval, aggregator: AggregatorType.findByName(AggregatorType.MEASURED_EVENT), tag: jobGroup1.ident()+';'+page1.ident()+';1;1;1', started: startedTime.plusMinutes(MeasuredValueInterval.DAILY+1).toDate(), resultIds: "1", value: 1000d).save(failOnError: true)
		TestDataUtil.createUpdateEvent(mv5.ident(), MeasuredValueUpdateEvent.UpdateCause.CALCULATED)

		Day testDay = new Day()
		(0..23).each {
			testDay.setHourWeight(it, 1)
		}
		testDay.setHourWeight(12, 10)
		testDay.save(failOnError: true)
		
		List<WeightedCsiValue> weightedCsiValuesToReturnInMock = [
			new WeightedCsiValue(weightedValue: new WeightedValue(value: 1d, weight: 10d), underlyingEventResultIds: []),
			new WeightedCsiValue(weightedValue: new WeightedValue(value: 10d, weight: 1d), underlyingEventResultIds: []),
			new WeightedCsiValue(weightedValue: new WeightedValue(value: 11d, weight: 1d), underlyingEventResultIds: [])]
		
		//mocking inner services
		
		mockMeasuredValueDaoService()
		mockMeasuredValueTagService(allCsiGroups, allPages)
		mockWeightingService(weightedCsiValuesToReturnInMock)
		
		//precondition
		
		List<MeasuredValue> mvs = serviceUnderTest.findAll(startedTime.toDate(), startedTime.toDate(), dailyInterval)
		assertEquals(0, mvs.size())
		
		//test execution
		List<MeasuredValue> calculatedMvs=serviceUnderTest.getOrCalculatePageMeasuredValues(startedTime.toDate(), startedTime.toDate(), dailyInterval, [jobGroup1], [page1])
		
		//assertions
		
		assertEquals(1, calculatedMvs.size())
		assertTrue(calculatedMvs[0].isCalculated())
		double browserWeightAllMvs = 1d
		double valueFirstMv = 1d
		double hourofdayWeightFirstMv = 10d
		double valueSecondMv = 10d
		double hourofdayWeightSecondMv = 1d
		double valueThirdMv = 11d
		double hourofdayWeightThirdMv = 1d
		double sumOfAllWeights = 12d
		assertEquals(
			((valueFirstMv * hourofdayWeightFirstMv * browserWeightAllMvs) + (valueSecondMv * hourofdayWeightSecondMv * browserWeightAllMvs) + (valueThirdMv * hourofdayWeightThirdMv * browserWeightAllMvs)) / sumOfAllWeights,
			calculatedMvs[0].value, 
			DELTA
			);
		
		mvs = serviceUnderTest.findAll(startedTime.toDate(), startedTime.toDate(), dailyInterval)
		assertEquals(1, mvs.size())
		
		MeasuredValue wpmvMarked = mvs[0]
		assertTrue(wpmvMarked.isCalculated())
		
	}
	
	/**
	 * Tests calculation of daily-page-{@link MeasuredValue}s, which aren't calculated when new {@link EventResult}s get persisted.
	 */
	@Test
	void testCalculation_DailyInterval_MultipleHourlyMv_YesCalculatedNoData(){
		DateTime startedTime = new DateTime(2013,5,16,12,12,11)
		

		//MVs outside of Interval
		MeasuredValue mv1 = new MeasuredValue(interval: hourlyInterval, aggregator: AggregatorType.findByName(AggregatorType.MEASURED_EVENT), tag: jobGroup1.ident()+';'+page1.ident()+';1;1;1', started: startedTime.minusMinutes(1).toDate(), resultIds: "1", value: 1000d).save(failOnError: true);
		TestDataUtil.createUpdateEvent(mv1.ident(), MeasuredValueUpdateEvent.UpdateCause.CALCULATED)
		MeasuredValue mv2 = new MeasuredValue(interval: hourlyInterval, aggregator: AggregatorType.findByName(AggregatorType.MEASURED_EVENT), tag: jobGroup1.ident()+';'+page1.ident()+';1;1;1', started: startedTime.plusMinutes(MeasuredValueInterval.DAILY+1).toDate(), resultIds: "1", value: 1000d).save(failOnError: true);
		TestDataUtil.createUpdateEvent(mv2.ident(), MeasuredValueUpdateEvent.UpdateCause.CALCULATED)

		Day testDay = new Day()
		(0..23).each {
			testDay.setHourWeight(it, 1)
		}
		testDay.save(failOnError: true)
		
		
		//mocking inner services
		
		mockMeasuredValueDaoService()
		mockMeasuredValueTagService(allCsiGroups, allPages)
		
		//precondition
		
		List<MeasuredValue> mvs = serviceUnderTest.findAll(startedTime.toDate(), startedTime.toDate(), dailyInterval)
		assertEquals(0, mvs.size())
		
		//test execution
		List<MeasuredValue> calculatedMvs=serviceUnderTest.getOrCalculatePageMeasuredValues(startedTime.toDate(), startedTime.toDate(), dailyInterval, [jobGroup1], [page1])
		
		//assertions
		
		assertEquals(1, calculatedMvs.size())
		assertTrue(calculatedMvs[0].isCalculated())
		assertNull(calculatedMvs[0].value)
		
		mvs = serviceUnderTest.findAll(startedTime.toDate(), startedTime.toDate(), dailyInterval)
		assertEquals(1, mvs.size())
		
		
	}
	
	
	
	//mocking inner services////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
	/**
	 * Mocks used methods of {@link EventMeasuredValueService}. 
	 * @param csiGroups
	 * @param pages
	 */
	private void mockMeasuredValueTagService(Map<String,JobGroup> allCsiGroups, Map<String,Page> allPages){
//		def measuredValueTagServiceMocked = mockFor(MeasuredValueTagService, true)
//		measuredValueTagServiceMocked.demand.getTagPatternForWeeklyPageMvsWithJobGroupsAndPages(0..10000) {
//			List<JobGroup> theCsiGroups, List<Page> thePages ->
//			return patternToReturn
//		}

//
//		measuredValueTagServiceMocked.demand.findJobGroupOfHourlyEventTag(0..10000) { String tag ->
//			return jobGroup1;
//		}
//		measuredValueTagServiceMocked.demand.findPageOfHourlyEventTag(0..10000) { String tag ->
//			return page1;
//		}

		ServiceMocker serviceMocker = ServiceMocker.create()
		serviceMocker.mockMeasuredValueTagService(serviceUnderTest,allCsiGroups,null,allPages,null,null)
		serviceMocker.mockMeasuredValueTagService(serviceUnderTest.eventMeasuredValueService,allCsiGroups,null,allPages,null,null)
	}
	
	
	private void mockMeasuredValueDaoService(){
		MeasuredValueDaoService original = new MeasuredValueDaoService()
		serviceUnderTest.measuredValueDaoService = original
		serviceUnderTest.eventMeasuredValueService.measuredValueDaoService = original
	}
	
	/**
	 * Mocks methods of {@link WeightingService}.
	 */
	private void mockWeightingService(List<WeightedCsiValue> toReturnFromGetWeightedCsiValues){
		def weightingService = mockFor(WeightingService, true)
		weightingService.demand.getWeightedCsiValues(1..10000) {
			List<CsiValue> csiValues, Set<WeightFactor> weightFactors, CsiConfiguration csiConfiguration ->
			return toReturnFromGetWeightedCsiValues
		}
		serviceUnderTest.weightingService = weightingService.createMock()
	}
	
	/**
	 * Mocks methods of {@link MeasuredValueUpdateEventDaoService}.
	 */
	private void mockMeasuredValueUpdateEventDaoService(){
		def measuredValueUpdateEventDaoService = mockFor(MeasuredValueUpdateEventDaoService, true)
		measuredValueUpdateEventDaoService.demand.createUpdateEvent(1..10000) {
			Long measuredValueId, MeasuredValueUpdateEvent.UpdateCause cause ->
			
				new MeasuredValueUpdateEvent(
					dateOfUpdate: new Date(),
					measuredValueId: measuredValueId,
					updateCause: cause
				).save(failOnError: true)
				
		}
		serviceUnderTest.measuredValueUpdateEventDaoService = measuredValueUpdateEventDaoService.createMock()
	}
	
	//testdata////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	private void createTestDataForAllTests(){
		weeklyInterval = new MeasuredValueInterval(name: 'weekly', intervalInMinutes: MeasuredValueInterval.WEEKLY).save(failOnError: true)
		dailyInterval = new MeasuredValueInterval(name: 'daily', intervalInMinutes: MeasuredValueInterval.DAILY).save(failOnError: true)
		hourlyInterval = new MeasuredValueInterval(name: 'hourly', intervalInMinutes: MeasuredValueInterval.HOURLY).save(failOnError: true)
		
		pageAggregator = new AggregatorType(name: AggregatorType.PAGE, measurandGroup: MeasurandGroup.NO_MEASURAND).save(failOnError: true)
		new AggregatorType(name: AggregatorType.MEASURED_EVENT, measurandGroup: MeasurandGroup.NO_MEASURAND).save(failOnError: true)

		browser=new Browser(name: "Test", weight: 1).save(failOnError: true);
		
		jobGroup1=new JobGroup(name: jobGroupName1, groupType: JobGroupType.CSI_AGGREGATION).save(failOnError: true)
		jobGroup2=new JobGroup(name: jobGroupName2, groupType: JobGroupType.CSI_AGGREGATION).save(failOnError: true)
		jobGroup3=new JobGroup(name: jobGroupName3, groupType: JobGroupType.CSI_AGGREGATION).save(failOnError: true)
		page1=new Page(name: pageName1).save(validate: false)
		page2=new Page(name: pageName2).save(validate: false)
		page3=new Page(name: pageName3).save(validate: false)
		
		//with existing JobGroup and Page:
		new MeasuredValue(interval: weeklyInterval, aggregator: pageAggregator, tag: '1;1', started: startDate.toDate()).save(validate: false)
		new MeasuredValue(interval: weeklyInterval, aggregator: pageAggregator, tag: '1;2', started: startDate.toDate()).save(validate: false)
		new MeasuredValue(interval: weeklyInterval, aggregator: pageAggregator, tag: '3;1', started: startDate.toDate()).save(validate: false)
		new MeasuredValue(interval: weeklyInterval, aggregator: pageAggregator, tag: '3;2', started: startDate.toDate()).save(validate: false)
		new MeasuredValue(interval: weeklyInterval, aggregator: pageAggregator, tag: '3;3', started: startDate.toDate()).save(validate: false)
		
		new MeasuredValue(interval: hourlyInterval, aggregator: pageAggregator, tag: '1;1', started: startDate.toDate(), value: 12).save(validate: true)
		new MeasuredValue(interval: hourlyInterval, aggregator: pageAggregator, tag: '1;2', started: startDate.toDate(), value: 10).save(validate: false)
		new MeasuredValue(interval: hourlyInterval, aggregator: pageAggregator, tag: '3;1', started: startDate.toDate(), value: 12).save(validate: false)
		new MeasuredValue(interval: hourlyInterval, aggregator: pageAggregator, tag: '3;2', started: startDate.toDate(), value: 12).save(validate: false)
		new MeasuredValue(interval: hourlyInterval, aggregator: pageAggregator, tag: '3;3', started: startDate.toDate(), value: 10).save(validate: false)

		//not with existing page
		new MeasuredValue(interval: weeklyInterval, aggregator: pageAggregator, tag: '3;4', started: startDate.toDate()).save(validate: false)
		//not with existing JobGroup
		new MeasuredValue(interval: weeklyInterval, aggregator: pageAggregator, tag: '4;2', started: startDate.toDate()).save(validate: false)
		//not with existing JobGroup and Page:
		new MeasuredValue(interval: weeklyInterval, aggregator: pageAggregator, tag: '4;4', started: startDate.toDate()).save(validate: false)
		new MeasuredValue(interval: weeklyInterval, aggregator: pageAggregator, tag: '5;14', started: startDate.toDate()).save(validate: false)
		new MeasuredValue(interval: weeklyInterval, aggregator: pageAggregator, tag: '6;7', started: startDate.toDate()).save(validate: false)

		allCsiGroups = ['1':jobGroup1,'2':jobGroup2,'3':jobGroup3]
		allPages = ['1':page1,'2':page2,'3':page3]
	}
	
	private void deleteTestData(){
		Page.list()*.delete(flush: true)
		MeasuredValue.list()*.delete(flush: true)
		JobGroup.list()*.delete(flush: true)
	}
}

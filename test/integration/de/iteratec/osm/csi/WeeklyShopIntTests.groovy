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

import grails.test.mixin.TestMixin
import grails.test.mixin.integration.IntegrationTestMixin

import static org.junit.Assert.*

import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test

import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.measurement.schedule.JobService
import de.iteratec.osm.report.chart.AggregatorType
import de.iteratec.osm.report.chart.CsiAggregation
import de.iteratec.osm.report.chart.CsiAggregationInterval
import de.iteratec.osm.report.chart.CsiAggregationUpdateEventDaoService
import de.iteratec.osm.csi.weighting.WeightingService
import de.iteratec.osm.result.EventResult
import de.iteratec.osm.result.EventResultService
import de.iteratec.osm.result.CsiAggregationTagService

@TestMixin(IntegrationTestMixin)
class WeeklyShopIntTests extends IntTestWithDBCleanup {

	static transactional = false

	/** injected by grails */
	EventCsiAggregationService eventCsiAggregationService
	ShopCsiAggregationService shopCsiAggregationService
	EventResultService eventResultService
	JobService jobService
	CsiAggregationTagService csiAggregationTagService
	WeightingService weightingService
	MeanCalcService meanCalcService
	CsiAggregationUpdateEventDaoService csiAggregationUpdateEventDaoService

	CsiAggregationInterval hourly
	CsiAggregationInterval weekly

	AggregatorType pageAggregatorMeasuredEvent
	AggregatorType pageAggregatorType
	AggregatorType pageAggregatorShop

	static final List<String> pagesToTest = [
		'HP',
		'MES',
		'SE',
		'ADS',
		'WKBS',
		'WK'
	]
	static final DateTime startOfWeek = new DateTime(2012,11,12,0,0,0)
	static final String csiGroupName = "CSI"
	/** Testdata is persisted respective this csv */
	static final String csvFilename = 'weekly_page.csv'

	//tests//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@Test
	void testCalculatingWeeklyShopValueWithoutData(){

		//create test-specific data
		Date startDate = new DateTime(2012,01,12,0,0, DateTimeZone.UTC).toDate()
		JobGroup csiGroup = JobGroup.findByName(csiGroupName)

		CsiAggregation mvWeeklyShop = new CsiAggregation(
				started: startDate,
				interval: weekly,
				aggregator: pageAggregatorShop,
				tag: csiGroup.ident().toString(),
				csByWptDocCompleteInPercent: null,
				underlyingEventResultsByWptDocComplete: ''
				).save(failOnError:true)

		//execute test

		shopCsiAggregationService.calcCa(mvWeeklyShop)

		//assertions

		assertEquals(startDate, mvWeeklyShop.started)
		assertEquals(weekly.intervalInMinutes, mvWeeklyShop.interval.intervalInMinutes)
		assertEquals(pageAggregatorShop.name, mvWeeklyShop.aggregator.name)
		assertEquals(csiGroup.ident().toString(), mvWeeklyShop.tag)
		assertTrue(mvWeeklyShop.isCalculated())
		assertEquals(0, mvWeeklyShop.countUnderlyingEventResultsByWptDocComplete())
		assertNull(mvWeeklyShop.value)
	}

	/**
	 * Tests the calculation of one weekly-shop-{@link CsiAggregation}. Databasis for calculation are weekly page-{@link CsiAggregation}s. These get calculated
	 * on-the-fly while calculating the respective weekly-shop-{@link CsiAggregation}. The hourly-event-{@link CsiAggregation}s of the period have to exist (they
	 * won't get calculated on-the-fly. Therefore these get precalculated in test here. 
	 */
	@Test
	void testCalculatingWeeklyShopValue(){
		
		Date startDate = new DateTime(2012,11,12,0,0, DateTimeZone.UTC).toDate()
		Integer targetResultCount = 233+231+122+176+172+176
		
		List<EventResult> results = EventResult.findAllByJobResultDateBetween(startDate, new DateTime(startDate).plusWeeks(1).toDate())
		assertTrue(Math.abs(results.size() - targetResultCount) < 30)

		//create test-specific data
		JobGroup csiGroup = JobGroup.findByName(csiGroupName)
		List<CsiAggregation> createdHmvs = precalcHourlyJobMvs(csiGroup)
		Map<String, List<CsiAggregation>> hmvsByPagename = getHmvsByPagenameMap(createdHmvs)

		Double expectedValue = 61.30


		CsiAggregation mvWeeklyShop = new CsiAggregation(
				started: startDate,
				interval: weekly,
				aggregator: pageAggregatorShop,
				tag: csiGroup.ident().toString(),
				csByWptDocCompleteInPercent: null,
				underlyingEventResultsByWptDocComplete: ''
				).save(failOnError:true)

		//execute test

		shopCsiAggregationService.calcCa(mvWeeklyShop)

		//assertions

		assertEquals(startDate, mvWeeklyShop.started)
		assertEquals(weekly.intervalInMinutes, mvWeeklyShop.interval.intervalInMinutes)
		assertEquals(pageAggregatorShop.name, mvWeeklyShop.aggregator.name)
		assertEquals(csiGroup.ident().toString(), mvWeeklyShop.tag)
		assertTrue(mvWeeklyShop.isCalculated())
		assertNotNull mvWeeklyShop.value
		Double calculated = mvWeeklyShop.value * 100
		//TODO: diff should be smaller
		assertEquals(expectedValue, calculated, 5.0d)
	}
	
	/**
	 * <p>
	 * Pre-calculates some hourly measured values based on the CSV from which the weekly values should be calculated.
	 * </p>
	 * 
	 * @param jobGroup The group to use.
	 * 
	 * @return A collection of pre-calculated hourly values.
	 */
	private List<CsiAggregation> precalcHourlyJobMvs(JobGroup jobGroup){

		DateTime currentDateTime = startOfWeek
		DateTime endOfWeek = startOfWeek.plusWeeks(1)
		
		List<CsiAggregation> createdHmvs = []
		pagesToTest.each { String pageName ->
			createdHmvs.addAll(TestDataUtil.precalculateHourlyCsiAggregations(
				jobGroup, pageName, endOfWeek, currentDateTime, hourly, 
				eventCsiAggregationService,
				csiAggregationTagService,
				eventResultService,
				weightingService,
				meanCalcService,
				csiAggregationUpdateEventDaoService)
			)
		}
		return createdHmvs
	}
	
	private getHmvsByPagenameMap(List<CsiAggregation> createdHmvs){
		Map<String, List<CsiAggregation>> hmvsByPagename = [:]
		pagesToTest.each{
			hmvsByPagename[it] = []
		}
		Page page
		createdHmvs.each{ CsiAggregation hmv ->
			page = csiAggregationTagService.findPageOfHourlyEventTag(hmv.tag)
			if (page && hmvsByPagename.containsKey(page.name)) {
				hmvsByPagename[page.name].add(hmv)
			}
		}
		return hmvsByPagename
	}

	//testsdata common to all tests//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * Creating testdata.
	 * JobConfigs, jobRuns and results are generated from a csv-export of WPT-Monitor from november 2012. Customer satisfaction-values were calculated 
	 * with valid TimeToCsMappings from 2012 and added to csv.
	 */
	@BeforeClass
	static void createTestData() {
		System.out.println('Create some common test-data...');
		TestDataUtil.createOsmConfig()
		TestDataUtil.createCsiAggregationIntervals()
		TestDataUtil.createAggregatorTypes()
		TestDataUtil.createHoursOfDay()
		System.out.println('Create some common test-data... DONE');

		System.out.println('Loading CSV-data...');
		TestDataUtil.loadTestDataFromCustomerCSV(new File("test/resources/CsiData/${csvFilename}"), pagesToTest, pagesToTest);
		System.out.println('Loading CSV-data... DONE');
	}
	
	@Before
	public void setUpServiceMockRlikeAndData() {
		hourly= CsiAggregationInterval.findByIntervalInMinutes(CsiAggregationInterval.HOURLY)
		weekly= CsiAggregationInterval.findByIntervalInMinutes(CsiAggregationInterval.WEEKLY)
		pageAggregatorMeasuredEvent = AggregatorType.findByName(AggregatorType.MEASURED_EVENT)
		pageAggregatorShop = AggregatorType.findByName(AggregatorType.SHOP)
		pageAggregatorType = AggregatorType.findByName(AggregatorType.PAGE)

	}
}

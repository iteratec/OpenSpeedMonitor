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
class WeeklyShopMultipleCsiGroupsIntTests extends IntTestWithDBCleanup {

	static transactional = false

	/** injected by grails */
	EventCsiAggregationService eventCsiAggregationService
	ShopCsiAggregationService shopCsiAggregationService
	JobService jobService
	CsiAggregationTagService csiAggregationTagService
	EventResultService eventResultService
	WeightingService weightingService
	MeanCalcService meanCalcService
	CsiAggregationUpdateEventDaoService csiAggregationUpdateEventDaoService

	CsiAggregationInterval weeklyInterval

	AggregatorType shopAggregatorType

	Map<String, Double> targetValues
	List<JobGroup> csiGroups
	List<Page> pageObjectsToTest

	static final List<String> pagesToTest = [
		'HP',
		'MES',
		'SE',
		'ADS',
		'WKBS',
		'WK'
	]
	static final String csvName = 'weekly_shop_multiple_csi_groups.csv'
	static final DateTime startOfWeek = new DateTime(2012,11,12,0,0,0)
	static final String csiGroup1Name = 'csiGroup1'
	static final String csiGroup2Name = 'csiGroup2'

	static final Integer countResultsPerWeeklyShopMv = 12
	static final Integer countWeeklyShopMvsToBeCreated = 2
	static final Integer countResultsPerWeeklyPageMv = 4
	static final Integer countWeeklyPageMvsToBeCreated = 4

	@Before
	void setUp() {
		shopCsiAggregationService.meanCalcService = new MeanCalcService();
		
		weeklyInterval = CsiAggregationInterval.findByIntervalInMinutes(CsiAggregationInterval.WEEKLY)

		shopAggregatorType = AggregatorType.findByName(AggregatorType.SHOP)
		csiGroups = [
			JobGroup.findByName(csiGroup1Name),
			JobGroup.findByName(csiGroup2Name)
		]
		targetValues = [
			csiGroup1: 0.391,
			csiGroup2: 0.691
		]
		pageObjectsToTest = []
		pagesToTest.each{
			pageObjectsToTest.add(Page.findByName(it))
		}
	}

	/**
	 * After pre-calculation of hourly job-{@link CsiAggregation}s the creation and calculation of weekly shop-{@link CsiAggregation}s is tested.
	 */
	@Test
	void testCreationAndCalculationOfWeeklyShopValues() {
		
		Date startDate = startOfWeek.toDate()
		
		List<EventResult> results = EventResult.findAllByJobResultDateBetween(startDate, new DateTime(startDate).plusWeeks(1).toDate())
		assertEquals(24, results.size())

		precalcHourlyJobMvs()
		
		CsiAggregationInterval weeklyInterval = CsiAggregationInterval.findByIntervalInMinutes(CsiAggregationInterval.WEEKLY)
		List<CsiAggregation> wsmvs = shopCsiAggregationService.getOrCalculateShopCsiAggregations(startDate, startDate, weeklyInterval, csiGroups)
		assertNotNull(wsmvs)
		assertEquals(countWeeklyShopMvsToBeCreated, wsmvs.size()) 
		wsmvs.each{ CsiAggregation mvWeeklyShop ->
			assertEquals(startDate, mvWeeklyShop.started)
			assertEquals(weeklyInterval.intervalInMinutes, mvWeeklyShop.interval.intervalInMinutes)
			assertEquals(shopAggregatorType.name, mvWeeklyShop.aggregator.name)
			assertTrue(mvWeeklyShop.isCalculated())
		}

		csiGroups.each{JobGroup csiGroup ->
			List<CsiAggregation> wpmvsOfOneGroupPageCombination = shopCsiAggregationService.getOrCalculateShopCsiAggregations(startDate, startDate, weeklyInterval, [csiGroup])
			assertEquals(1, wpmvsOfOneGroupPageCombination.size())
			wpmvsOfOneGroupPageCombination.each{mvWeeklyPage ->
				assertEquals(csiGroup.ident().toString(), mvWeeklyPage.tag)
				assertEquals(targetValues["${csiGroup.name}"], mvWeeklyPage.value, 0.01d)
			}
		}
	}

	/**
	 * Pre-calculate hourly MVs for both groups.
	 */
	private List<CsiAggregation> precalcHourlyJobMvs(){

		CsiAggregationInterval hourlyInterval = CsiAggregationInterval.findByIntervalInMinutes(CsiAggregationInterval.HOURLY)

		JobGroup csiGroup1 = JobGroup.findByName(csiGroup1Name)
		JobGroup csiGroup2 = JobGroup.findByName(csiGroup2Name)

		DateTime currentDateTime = startOfWeek
		DateTime endOfWeek = startOfWeek.plusWeeks(1)

		List<CsiAggregation> createdHmvs = []
		pagesToTest.each { String pageName ->
			createdHmvs.addAll(
				TestDataUtil.precalculateHourlyCsiAggregations(
					csiGroup1, pageName, endOfWeek, currentDateTime, hourlyInterval, 
					eventCsiAggregationService,
					csiAggregationTagService,
					eventResultService,
					weightingService,
					meanCalcService,
					csiAggregationUpdateEventDaoService
				)
			)
			createdHmvs.addAll(
				TestDataUtil.precalculateHourlyCsiAggregations(
				csiGroup2, pageName, endOfWeek, currentDateTime, hourlyInterval, 
					eventCsiAggregationService,
					csiAggregationTagService,
					eventResultService,
					weightingService,
					meanCalcService,
					csiAggregationUpdateEventDaoService
				)
			)
		}

		return createdHmvs
	}

	/**
	 * Creating testdata.
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
		TestDataUtil.loadTestDataFromCustomerCSV(new File("test/resources/CsiData/${csvName}"), pagesToTest, pagesToTest);
		System.out.println('Loading CSV-data... DONE');
	}
}

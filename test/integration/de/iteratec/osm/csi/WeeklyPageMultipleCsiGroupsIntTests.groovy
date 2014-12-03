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
import java.lang.reflect.UndeclaredThrowableException
import static org.junit.Assert.*

import org.joda.time.DateTime
import org.junit.After
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test

import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.measurement.schedule.JobService
import de.iteratec.osm.report.chart.AggregatorType
import de.iteratec.osm.report.chart.MeasuredValue
import de.iteratec.osm.report.chart.MeasuredValueInterval
import de.iteratec.osm.report.chart.MeasuredValueUpdateEventDaoService
import de.iteratec.osm.csi.weighting.WeightingService
import de.iteratec.osm.result.EventResult
import de.iteratec.osm.result.EventResultService
import de.iteratec.osm.result.JobResult
import de.iteratec.osm.result.MeasuredValueTagService
import de.iteratec.osm.csi.EventMeasuredValueService
import de.iteratec.osm.result.JobResultService

@TestMixin(IntegrationTestMixin)
class WeeklyPageMultipleCsiGroupsIntTests extends de.iteratec.osm.csi.IntTestWithDBCleanup {

	static transactional = true

	/** injected by grails */
	EventMeasuredValueService eventMeasuredValueService
	PageMeasuredValueService pageMeasuredValueService
	JobService jobService
	MeasuredValueTagService measuredValueTagService
	EventResultService eventResultService
	WeightingService weightingService
	MeanCalcService meanCalcService
	MeasuredValueUpdateEventDaoService measuredValueUpdateEventDaoService
	MeasuredValueUpdateService measuredValueUpdateService

	MeasuredValueInterval hourly
	MeasuredValueInterval weekly
	Map<String, Double> targetValues
	List<JobGroup> csiGroups

	static final List<String> pagesToGenerateDataFor = ['HP', 'MES']
	static final List<String> allPages =[
		'HP',
		'MES',
		'SE',
		'ADS',
		'WKBS',
		'WK',
		Page.UNDEFINED
	];
	static AggregatorType job
	static AggregatorType page
	static AggregatorType shop

	static final String csvName = 'weekly_page_multiple_csi_groups.csv'
	static final DateTime startOfWeek = new DateTime(2012,11,12,0,0,0)
	static final String csiGroup1Name = 'csiGroup1'
	static final String csiGroup2Name = 'csiGroup2'

	/**
	 * Creating testdata.
	 * JobConfigs, jobRuns and results are generated from a csv-export of WPT-Monitor from november 2012. Customer satisfaction-values were calculated
	 * with valid TimeToCsMappings from 2012 and added to csv.
	 */
	@BeforeClass
	public static void setUpData() {
		TestDataUtil.cleanUpDatabase()
		System.out.println('Create some common test-data...');
		TestDataUtil.createOsmConfig()
		TestDataUtil.createMeasuredValueIntervals()
		TestDataUtil.createAggregatorTypes()
		TestDataUtil.createHoursOfDay()
		System.out.println('Create some common test-data... DONE');		
	}

	@Before
	void setUp() {
		JobResultService.metaClass.findJobResultByEventResult{EventResult eventResult ->
			List<JobResult> results = JobResult.getAll().findAll{eventResult in it.eventResults}
			return results.get(0)
		}
		
		System.out.println('Loading CSV-data...');
		TestDataUtil.loadTestDataFromCustomerCSV(new File("test/resources/CsiData/${csvName}"), pagesToGenerateDataFor, allPages, measuredValueTagService);
		System.out.println('Loading CSV-data... DONE');
		
		job = AggregatorType.findByName(AggregatorType.MEASURED_EVENT)
		page = AggregatorType.findByName(AggregatorType.PAGE)
		shop = AggregatorType.findByName(AggregatorType.SHOP)

		hourly = MeasuredValueInterval.findByIntervalInMinutes(MeasuredValueInterval.HOURLY)
		weekly = MeasuredValueInterval.findByIntervalInMinutes(MeasuredValueInterval.WEEKLY)
		csiGroups = [
			JobGroup.findByName(csiGroup1Name),
			JobGroup.findByName(csiGroup2Name)
		]
		targetValues = [
			'csiGroup1_HP':0.15,
			'csiGroup1_MES':0.35,
			'csiGroup2_HP':0.55,
			'csiGroup2_MES':0.75
		]
	}

	@After
	void tearDown() {
	}

	@Test
	public void testCreationAndCalculationOfWeeklyPageValuesFor_MES() {
		Integer countResultsPerWeeklyPageMv = 4
		Integer countWeeklyPageMvsToBeCreated = 2
		List<EventResult> results = EventResult.findAllByJobResultDateBetween(startOfWeek.toDate(), startOfWeek.plusWeeks(1).toDate())
		assertEquals(16, results.size())
		creationAndCalculationOfWeeklyPageValuesTest("MES", countResultsPerWeeklyPageMv, countWeeklyPageMvsToBeCreated, results);
	}

	@Test
	public void testCreationAndCalculationOfWeeklyPageValuesFor_HP() {
		Integer countResultsPerWeeklyPageMv = 4
		Integer countWeeklyPageMvsToBeCreated = 2
		List<EventResult> results = EventResult.findAllByJobResultDateBetween(startOfWeek.toDate(), startOfWeek.plusWeeks(1).toDate())
		assertEquals(16, results.size())
		creationAndCalculationOfWeeklyPageValuesTest("HP", countResultsPerWeeklyPageMv, countWeeklyPageMvsToBeCreated, results);
	}

	/**
	 * After pre-calculation of hourly job-{@link MeasuredValue}s the creation and calculation of weekly page-{@link MeasuredValue}s is tested.
	 */
	void creationAndCalculationOfWeeklyPageValuesTest(String pageName, final Integer countResultsPerWeeklyPageMv, final Integer countWeeklyPageMvsToBeCreated, List<EventResult> results) {

		Page testedPage=Page.findByName(pageName);

		// Skip Page if no data is generated (SpeedUp Test) see pagesToGenerateDataFor
		if(!pagesToGenerateDataFor.contains(pageName)) {
			fail("No data Was generated for the page "+pageName+" Test skipped.")
		}

		results.each { EventResult result ->
			measuredValueUpdateService.createOrUpdateDependentMvs(result)
		}

		Date startDate = startOfWeek.toDate()
		MeasuredValueInterval mvInterval=MeasuredValueInterval.findByIntervalInMinutes(MeasuredValueInterval.WEEKLY)
		List<MeasuredValue> wpmvs = pageMeasuredValueService.getOrCalculatePageMeasuredValues(startDate, startDate, mvInterval, csiGroups, [testedPage])
		assertNotNull(wpmvs)
		assertEquals(countWeeklyPageMvsToBeCreated, wpmvs.size())

		wpmvs.each{MeasuredValue mvWeeklyPage ->
			System.out.println(
				"WeeklyPageMultipleCsiGroupsIntTests.creationAndCalculationOfWeeklyPageValuesTest(): " + 
				mvWeeklyPage.ident()+" : "+mvWeeklyPage.getTag()+" : "+mvWeeklyPage.isCalculated());
		}


		wpmvs.each{MeasuredValue mvWeeklyPage ->
			assertEquals(startDate, mvWeeklyPage.started)
			assertEquals(weekly.intervalInMinutes, mvWeeklyPage.interval.intervalInMinutes)
			assertEquals(page.name, mvWeeklyPage.aggregator.name)
			assertTrue(mvWeeklyPage.isCalculated())
		}
		
		MeasuredValueInterval weeklyInterval = MeasuredValueInterval.findByIntervalInMinutes(MeasuredValueInterval.WEEKLY);
		csiGroups.each{JobGroup csiGroup ->
			List<MeasuredValue> wpmvsOfOneGroupPageCombination = pageMeasuredValueService.getOrCalculatePageMeasuredValues(startDate, startDate, weeklyInterval, [csiGroup], [testedPage])
			assertEquals(1, wpmvsOfOneGroupPageCombination.size())

			wpmvsOfOneGroupPageCombination.each{MeasuredValue mvWeeklyPage ->
				assertTrue(mvWeeklyPage.tag.equals(csiGroup.ident().toString() + ';' + testedPage.ident().toString()))
				assertEquals(targetValues["${csiGroup.name}_${testedPage.name}"], mvWeeklyPage.value, 0.01d)
			}
		}


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
	private List<MeasuredValue> precalcHourlyJobMvs(JobGroup jobGroup, String pageName){
		DateTime currentDateTime = startOfWeek
		DateTime endOfWeek = startOfWeek.plusWeeks(1)
		List<MeasuredValue> createdHmvs = TestDataUtil.precalculateHourlyMeasuredValues(
				jobGroup, pageName, endOfWeek, currentDateTime, hourly, 
				eventMeasuredValueService,
				measuredValueTagService,
				eventResultService, 
				weightingService, 
				meanCalcService,
				measuredValueUpdateEventDaoService)

		return createdHmvs
	}


	private Map<String, List<MeasuredValue>> getHmvsByPagenameMap(List<MeasuredValue> createdHmvs){
		Map<String, List<MeasuredValue>> hmvsByPagename = [:]
		JobGroup csi = JobGroup.findByName("CSI")

		allPages.each{
			Page p = Page.findByName(it)

			hmvsByPagename[csi.ident()+':::'+p.ident()] = []
		}

		Page page
		createdHmvs.each{MeasuredValue hmv ->
			page = measuredValueTagService.findPageOfHourlyEventTag(hmv.tag)
			if (page && hmvsByPagename.containsKey(csi.ident()+':::'+page.ident())) {
				hmvsByPagename[csi.ident()+':::'+page.ident()].add(hmv)
			}
		}
		return hmvsByPagename
	}
}

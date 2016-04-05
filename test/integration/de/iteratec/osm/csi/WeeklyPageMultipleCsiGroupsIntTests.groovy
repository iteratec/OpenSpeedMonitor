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

import de.iteratec.osm.csi.weighting.WeightingService
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.measurement.schedule.JobService
import de.iteratec.osm.report.chart.AggregatorType
import de.iteratec.osm.report.chart.CsiAggregation
import de.iteratec.osm.report.chart.CsiAggregationInterval
import de.iteratec.osm.report.chart.CsiAggregationUpdateEventDaoService
import de.iteratec.osm.result.*
import grails.test.mixin.TestMixin
import grails.test.mixin.integration.IntegrationTestMixin
import grails.test.spock.IntegrationSpec
import org.joda.time.DateTime
import spock.lang.Shared

import static org.junit.Assert.*

//@TestMixin(IntegrationTestMixin)
class WeeklyPageMultipleCsiGroupsIntTests extends NonTransactionalIntegrationSpec{

	/** injected by grails */
	EventCsiAggregationService eventCsiAggregationService
	PageCsiAggregationService pageCsiAggregationService
	JobService jobService
	@Shared CsiAggregationTagService csiAggregationTagService
	EventResultService eventResultService
	WeightingService weightingService
	MeanCalcService meanCalcService
	CsiAggregationUpdateEventDaoService csiAggregationUpdateEventDaoService
	CsiAggregationUpdateService csiAggregationUpdateService

	CsiAggregationInterval hourly
	CsiAggregationInterval weekly
	Map<String, Double> targetValues
	List<JobGroup> csiGroups
	Map<Long, JobResult> mapToFindJobResultByEventResult

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
	def setupSpec() {
		System.out.println('Create some common test-data...');
		TestDataUtil.createOsmConfig()
		TestDataUtil.createCsiAggregationIntervals()
		TestDataUtil.createAggregatorTypes()
        TestDataUtil.createCsiConfiguration()

		System.out.println('Loading CSV-data...');
		TestDataUtil.loadTestDataFromCustomerCSV(new File("test/resources/CsiData/${csvName}"), pagesToGenerateDataFor, allPages, csiAggregationTagService);
		System.out.println('Loading CSV-data... DONE');

		System.out.println('Create some common test-data... DONE');
	}

	def setup() {
		
		job = AggregatorType.findByName(AggregatorType.MEASURED_EVENT)
		page = AggregatorType.findByName(AggregatorType.PAGE)
		shop = AggregatorType.findByName(AggregatorType.SHOP)

		hourly = CsiAggregationInterval.findByIntervalInMinutes(CsiAggregationInterval.HOURLY)
		weekly = CsiAggregationInterval.findByIntervalInMinutes(CsiAggregationInterval.WEEKLY)
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


	void testCreationAndCalculationOfWeeklyPageValuesFor_MES() {
		given:
		Integer countResultsPerWeeklyPageMv = 4
		Integer countWeeklyPageMvsToBeCreated = 2
		when:
		List<EventResult> results = EventResult.findAllByJobResultDateBetween(startOfWeek.toDate(), startOfWeek.plusWeeks(1).toDate())
		then:
		results.size()==16
		creationAndCalculationOfWeeklyPageValuesTest("MES", countResultsPerWeeklyPageMv, countWeeklyPageMvsToBeCreated, results);
	}

	void testCreationAndCalculationOfWeeklyPageValuesFor_HP() {
        given:
		Integer countResultsPerWeeklyPageMv = 4
		Integer countWeeklyPageMvsToBeCreated = 2
        when:
		List<EventResult> results = EventResult.findAllByJobResultDateBetween(startOfWeek.toDate(), startOfWeek.plusWeeks(1).toDate())
		then:
        results.size() == 16
		creationAndCalculationOfWeeklyPageValuesTest("HP", countResultsPerWeeklyPageMv, countWeeklyPageMvsToBeCreated, results);
	}

	/**
	 * After pre-calculation of hourly job-{@link CsiAggregation}s the creation and calculation of weekly page-{@link CsiAggregation}s is tested.
	 */
	private void creationAndCalculationOfWeeklyPageValuesTest(String pageName, final Integer countResultsPerWeeklyPageMv, final Integer countWeeklyPageMvsToBeCreated, List<EventResult> results) {

		Page testedPage=Page.findByName(pageName);

		// Skip Page if no data is generated (SpeedUp Test) see pagesToGenerateDataFor
		if(!pagesToGenerateDataFor.contains(pageName)) {
			fail("No data Was generated for the page "+pageName+" Test skipped.")
		}

		results.each { EventResult result ->
			csiAggregationUpdateService.createOrUpdateDependentMvs(result)
		}

		Date startDate = startOfWeek.toDate()
		CsiAggregationInterval mvInterval=CsiAggregationInterval.findByIntervalInMinutes(CsiAggregationInterval.WEEKLY)
		List<CsiAggregation> wpmvs = pageCsiAggregationService.getOrCalculatePageCsiAggregations(startDate, startDate, mvInterval, csiGroups, [testedPage])
		assertNotNull(wpmvs)
		assertEquals(countWeeklyPageMvsToBeCreated, wpmvs.size())

		wpmvs.each{ CsiAggregation mvWeeklyPage ->
			System.out.println(
				"WeeklyPageMultipleCsiGroupsIntTests.creationAndCalculationOfWeeklyPageValuesTest(): " + 
				mvWeeklyPage.ident()+" : "+mvWeeklyPage.getTag()+" : "+mvWeeklyPage.isCalculated());
		}


		wpmvs.each{ CsiAggregation mvWeeklyPage ->
			assertEquals(startDate, mvWeeklyPage.started)
			assertEquals(weekly.intervalInMinutes, mvWeeklyPage.interval.intervalInMinutes)
			assertEquals(page.name, mvWeeklyPage.aggregator.name)
			assertTrue(mvWeeklyPage.isCalculated())
		}
		
		CsiAggregationInterval weeklyInterval = CsiAggregationInterval.findByIntervalInMinutes(CsiAggregationInterval.WEEKLY);
		csiGroups.each{JobGroup csiGroup ->
			List<CsiAggregation> wpmvsOfOneGroupPageCombination = pageCsiAggregationService.getOrCalculatePageCsiAggregations(startDate, startDate, weeklyInterval, [csiGroup], [testedPage])
			assertEquals(1, wpmvsOfOneGroupPageCombination.size())

			wpmvsOfOneGroupPageCombination.each{ CsiAggregation mvWeeklyPage ->
				assertTrue(mvWeeklyPage.tag.equals(csiGroup.ident().toString() + ';' + testedPage.ident().toString()))
				assertEquals(targetValues["${csiGroup.name}_${testedPage.name}"], mvWeeklyPage.csByWptDocCompleteInPercent, 0.01d)
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
	private List<CsiAggregation> precalcHourlyJobMvs(JobGroup jobGroup, String pageName){
		DateTime currentDateTime = startOfWeek
		DateTime endOfWeek = startOfWeek.plusWeeks(1)
		List<CsiAggregation> createdHmvs = TestDataUtil.precalculateHourlyCsiAggregations(
				jobGroup, pageName, endOfWeek, currentDateTime, hourly, 
				eventCsiAggregationService,
				csiAggregationTagService,
				eventResultService, 
				weightingService, 
				meanCalcService,
				csiAggregationUpdateEventDaoService)

		return createdHmvs
	}


	private Map<String, List<CsiAggregation>> getHmvsByPagenameMap(List<CsiAggregation> createdHmvs){
		Map<String, List<CsiAggregation>> hmvsByPagename = [:]
		JobGroup csi = JobGroup.findByName("CSI")

		allPages.each{
			Page p = Page.findByName(it)

			hmvsByPagename[csi.ident()+':::'+p.ident()] = []
		}

		Page page
		createdHmvs.each{ CsiAggregation hmv ->
			page = csiAggregationTagService.findPageOfHourlyEventTag(hmv.tag)
			if (page && hmvsByPagename.containsKey(csi.ident()+':::'+page.ident())) {
				hmvsByPagename[csi.ident()+':::'+page.ident()].add(hmv)
			}
		}
		return hmvsByPagename
	}
}

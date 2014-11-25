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

import static org.junit.Assert.*
import grails.test.mixin.TestMixin
import grails.test.mixin.integration.IntegrationTestMixin

import org.joda.time.DateTime
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
import de.iteratec.osm.result.EventResultService
import de.iteratec.osm.result.MeasuredValueTagService


@TestMixin(IntegrationTestMixin)
class WeeklyPageIntTests extends IntTestWithDBCleanup {

	static transactional = false

	/** injected by grails */
	EventMeasuredValueService eventMeasuredValueService
	MeasuredValueTagService measuredValueTagService
	EventResultService eventResultService
	WeightingService weightingService
	MeanCalcService meanCalcService
	MeasuredValueUpdateEventDaoService measuredValueUpdateEventDaoService
	PageMeasuredValueService pageMeasuredValueService
	JobService jobService
	MeasuredValueUpdateService measuredValueUpdateService

	MeasuredValueInterval hourly
	MeasuredValueInterval weekly
	Map<String, Double> targetValues
	Map<String, Integer> targetResultCounts

	static final List<String> pagesToGenerateDataFor = [
		'HP',
		'MES',
		'SE',
		'ADS',
		'WKBS',
		'WK'
	]
	static final String csvName = 'weekly_page_KW50_2012.csv'
	static final DateTime startOfWeek = new DateTime(2012,11,12,0,0,0)
	static final List<String> allPages =[
		'HP',
		'MES',
		'SE',
		'ADS',
		'WKBS',
		'WK',
		Page.UNDEFINED
	];
	static AggregatorType pageAggregatorType


	@BeforeClass
	public static void setUpData() {
		TestDataUtil.createOsmConfig()
		TestDataUtil.createMeasuredValueIntervals()
		TestDataUtil.createAggregatorTypes()

		pageAggregatorType = AggregatorType.findByName(AggregatorType.PAGE)

		TestDataUtil.createHoursOfDay()
		System.out.println('Create some common test-data... DONE');

		System.out.println('Loading CSV-data...');
		TestDataUtil.loadTestDataFromCustomerCSV(new File("test/resources/CsiData/${csvName}"), pagesToGenerateDataFor, allPages);
		System.out.println('Loading CSV-data... DONE');
	}

	/**
	 * Creating testdata.
	 * JobConfigs, jobRuns and results are generated from a csv-export of WPT-Monitor from november 2012. Customer satisfaction-values were calculated 
	 * with valid TimeToCsMappings from 2012 and added to csv.
	 */
	@Before
	void setUp() {
		System.out.println('Set-up...');
		targetValues = [
			'weekly_page.csv':
			[
				'HP':89.30,
				'MES':82.24,
				'SE':61.83,
				'ADS':54.95,
				'WKBS':49.35,
				'WK':37.93
			],
			'weekly_page_KW50_2012.csv':
			[
				'HP':95.45,
				'MES':93.88,
				'SE':92.97,
				'ADS':76.94,
				'WKBS':82.56,
				'WK':50.81]]
		targetResultCounts = [
			'weekly_page_KW50_2012.csv':
			[
				'HP':1426,
				'MES':1344,
				'SE':1346,
				'ADS':1553,
				'WKBS':1395,
				'WK':1437,
			],
			'weekly_page.csv':
			[
				'HP':233,
				'MES':231,
				'SE':122,
				'ADS':176,
				'WKBS':172,
				'WK':176]]


		hourly = MeasuredValueInterval.findByIntervalInMinutes(MeasuredValueInterval.HOURLY)
		weekly = MeasuredValueInterval.findByIntervalInMinutes(MeasuredValueInterval.WEEKLY)

		System.out.println('Set-up... DONE');
	}

	/**
	 * Test for Page SE
	 */
	@Test
	public void testCalculatingWeeklyPageValuesForPage_SE() {
		calculatingWeeklyPageValuesForPageTest("SE");
	}

	/**
	 * Test for Page SE
	 */
	@Test
	public void testCalculatingWeeklyPageValuesForPage_ADS() {
		calculatingWeeklyPageValuesForPageTest("ADS");
	}

	/**
	 * Test for Page WKBS
	 */
	@Test
	public void testCalculatingWeeklyPageValuesForPage_WKBS() {
		calculatingWeeklyPageValuesForPageTest("WKBS");
	}

	/**
	 * Test for Page WK
	 */
	@Test
	public void testCalculatingWeeklyPageValuesForPage_WK() {
		calculatingWeeklyPageValuesForPageTest("WK");
	}

	/**
	 * Test for Page HP
	 */
	@Test
	public void testCalculatingWeeklyPageValuesForPage_HP() {
		calculatingWeeklyPageValuesForPageTest("HP");
	}

	/**
	 * Test for Page MES
	 */
	@Test
	public void testCalculatingWeeklyPageValuesForPage_MES() {
		calculatingWeeklyPageValuesForPageTest("MES");
	}

	/**
	 * The target weekly page-values were taken from the old excel-evaluation: .\test\resources\SR_2.44.0_Kundenzufriedenheit.xlsx. 
	 * The CSV read is {@code weekly_page_KW50_2012.csv}.
	 * Calculating weekly page-values via {@link PageMeasuredValueService} should provide (nearly) the same results!
	 */
	private void calculatingWeeklyPageValuesForPageTest(String pageName) {

		// Skip Page if no data is generated (SpeedUp Test) see pagesToGenerateDataFor
		if(!pagesToGenerateDataFor.contains(pageName)) {
			fail("No data Was generated for the page "+pageName+" Test skipped.")
		}

		System.out.println('Test: testCalculatingWeeklyPageValues()');

		Date startDate = startOfWeek.toDate()

		Page pageToCalculateMvFor = Page.findByName(pageName)
		JobGroup jobGroup = JobGroup.findByName("CSI")

		System.out.println('createWeeklyPageAggregatorTag(jg, p)...');
		String tagOfWeeklyMVs = measuredValueTagService.createPageAggregatorTag(jobGroup, pageToCalculateMvFor)
		System.out.println('createWeeklyPageAggregatorTag(jg, p) results in ' + tagOfWeeklyMVs);
		System.out.println('createWeeklyPageAggregatorTag(jg, p)... DONE');

		System.out.println('precalcHourlyJobMvs()...');
		List<MeasuredValue> createdHmvs = []
		createdHmvs.addAll(precalcHourlyJobMvs(jobGroup, pageName))
		System.out.println('precalcHourlyJobMvs()... DONE');

		System.out.println('createHourlyMeasuredValueByGroupAndPageIdMap(createdHmvs, measuredValueTagService)...');
		Map<String, List<MeasuredValue>> hmvsByPagename = TestDataUtil.createHourlyMeasuredValueByGroupAndPageIdMap(createdHmvs, measuredValueTagService)
		System.out.println('createHourlyMeasuredValueByGroupAndPageIdMap(createdHmvs, measuredValueTagService)... DONE');

		MeasuredValue mvWeeklyPage = new MeasuredValue(
				started: startDate,
				interval: weekly,
				aggregator: pageAggregatorType,
				tag: tagOfWeeklyMVs,
				value: null,
				resultIds: ''
				).save(failOnError:true)

		assertNotNull(jobGroup)

		MvCachingContainer cachingContainer = new MvCachingContainer(
				pageToCalcMvFor:pageToCalculateMvFor,
				csiGroupToCalcMvFor: jobGroup,
				hmvsByCsiGroupPageCombination:hmvsByPagename)

		System.out.println('calcMv(mvWeeklyPage, cC)...');
		pageMeasuredValueService.calcMv(mvWeeklyPage, cachingContainer)
		System.out.println('calcMv(mvWeeklyPage, cC)... DONE');

		assertEquals(startDate, mvWeeklyPage.started)
		assertEquals(weekly.intervalInMinutes, mvWeeklyPage.interval.intervalInMinutes)
		assertEquals(pageAggregatorType.name, mvWeeklyPage.aggregator.name)
		assertTrue(mvWeeklyPage.isCalculated())

		// TODO mze-2013-08-15: Check this:
		// Disabled reason: Values differ may from data
		// int targetResultCount = targetResultCounts[csvName][pageName]?:-1
		// assertEquals(targetResultCount, mvWeeklyPage.countResultIds())

		assertNotNull(mvWeeklyPage.value)

		double expectedValue = targetValues[csvName][pageName]?:-1

		Double calculated = mvWeeklyPage.value * 100
		Double difference = Math.abs(calculated - expectedValue)
		println "page ${pageName} / ${pageAggregatorType.name}"
		println "calculated  = ${calculated}"
		println "targetValue = ${expectedValue}"
		println "difference  = ${difference}"

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
			measuredValueUpdateEventDaoService
		)
		return createdHmvs
	}



}

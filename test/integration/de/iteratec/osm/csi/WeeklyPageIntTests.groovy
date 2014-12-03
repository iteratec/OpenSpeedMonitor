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
import de.iteratec.osm.result.JobResult
import de.iteratec.osm.result.EventResult
import de.iteratec.osm.result.EventResultService
import de.iteratec.osm.result.MeasuredValueTagService
import de.iteratec.osm.util.PerformanceLoggingService;
import de.iteratec.osm.util.PerformanceLoggingService.IndentationDepth
import de.iteratec.osm.util.PerformanceLoggingService.LogLevel
import de.iteratec.osm.result.JobResultService


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
	Map<Long, JobResult> mapToFindJobResultByEventResult
	

	MeasuredValueInterval hourly
	MeasuredValueInterval weekly
	Map<String, Double> targetValues
	Map<String, Integer> targetResultCounts

	static final List<String> pagesToGenerateDataFor = ['SE']
	static final String csvName = 'weekly_page_KW50_2012.csv'
	static final DateTime startOfWeek = new DateTime(2012,11,12,0,0,0)
	static final List<String> allPages =[
		'SE',
		Page.UNDEFINED
	];
	static AggregatorType pageAggregatorType


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

	/**
	 * Creating testdata.
	 * JobConfigs, jobRuns and results are generated from a csv-export of WPT-Monitor from november 2012. Customer satisfaction-values were calculated 
	 * with valid TimeToCsMappings from 2012 and added to csv.
	 */
	@Before
	void setUp() {
		
		System.out.println('Loading CSV-data...');
		TestDataUtil.loadTestDataFromCustomerCSV(new File("test/resources/CsiData/${csvName}"), pagesToGenerateDataFor, allPages, measuredValueTagService);
		System.out.println('Loading CSV-data... DONE');
		
		mapToFindJobResultByEventResult = TestDataUtil.generateMapToFindJobResultByEventResultId(JobResult.list())
		JobResultService.metaClass.findJobResultByEventResult{EventResult eventResult ->
			return mapToFindJobResultByEventResult[eventResult.ident()]
		}
		
		pageAggregatorType = AggregatorType.findByName(AggregatorType.PAGE)
		
		System.out.println('Set-up...');
		targetValues = ['weekly_page.csv':['SE':61.83], 'weekly_page_KW50_2012.csv':['SE':92.97]]
		
		targetResultCounts = ['weekly_page_KW50_2012.csv':['SE':1346], 'weekly_page.csv':['SE':122]]


		hourly = MeasuredValueInterval.findByIntervalInMinutes(MeasuredValueInterval.HOURLY)
		weekly = MeasuredValueInterval.findByIntervalInMinutes(MeasuredValueInterval.WEEKLY)

		System.out.println('Set-up... DONE');
	}

	/**
	 * Test for Page SE
	 */
	@Test
	public void testCalculatingWeeklyPageValuesForPage_SE() {
		List<EventResult> results = EventResult.findAllByJobResultDateBetween(startOfWeek.toDate(), startOfWeek.plusWeeks(1).toDate())
		calculatingWeeklyPageValuesForPageTest("SE", results);
	}

	/**
	 * The target weekly page-values were taken from the old excel-evaluation: .\test\resources\SR_2.44.0_Kundenzufriedenheit.xlsx. 
	 * The CSV read is {@code weekly_page_KW50_2012.csv}.
	 * Calculating weekly page-values via {@link PageMeasuredValueService} should provide (nearly) the same results!
	 */
	private void calculatingWeeklyPageValuesForPageTest(String pageName, List<EventResult> results) {
		
		// Skip Page if no data is generated (SpeedUp Test) see pagesToGenerateDataFor
		if(!pagesToGenerateDataFor.contains(pageName)) {
			fail("No data Was generated for the page "+pageName+" Test skipped.")
		}

		System.out.println('Test: testCalculatingWeeklyPageValues()');

		Date startDate = startOfWeek.toDate()

		Page pageToCalculateMvFor = Page.findByName(pageName)
		JobGroup jobGroup = JobGroup.findByName("CSI")
		
		results.each { EventResult result ->
			measuredValueUpdateService.createOrUpdateDependentMvs(result)
		}
		
		List<MeasuredValue> wpmvsOfOneGroupPageCombination = pageMeasuredValueService.getOrCalculatePageMeasuredValues(startDate, startDate, weekly, [jobGroup], [pageToCalculateMvFor])
		MeasuredValue mvWeeklyPage = wpmvsOfOneGroupPageCombination.get(0)
		
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
}
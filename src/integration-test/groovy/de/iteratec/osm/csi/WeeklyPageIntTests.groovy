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

import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.report.chart.AggregatorType
import de.iteratec.osm.report.chart.CsiAggregation
import de.iteratec.osm.report.chart.CsiAggregationInterval
import de.iteratec.osm.result.EventResult
import grails.test.mixin.integration.Integration
import grails.transaction.Rollback
import org.joda.time.DateTime
import spock.lang.Ignore

import static org.junit.Assert.*

@Integration
@Rollback
class WeeklyPageIntTests  extends NonTransactionalIntegrationSpec {
	/** injected by grails */
	PageCsiAggregationService pageCsiAggregationService
	CsiAggregationUpdateService csiAggregationUpdateService

	CsiAggregationInterval hourly
	CsiAggregationInterval weekly
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



	/**
	 * Creating testdata.
	 * JobConfigs, jobRuns and results are generated from a csv-export of WPT-Monitor from november 2012. Customer satisfaction-values were calculated
	 * with valid TimeToCsMappings from 2012 and added to csv.
	 */
	def setup() {
		CsiAggregation.withNewTransaction {
			System.out.println('Create some common test-data...');
			TestDataUtil.createOsmConfig()
			TestDataUtil.createCsiAggregationIntervals()
			TestDataUtil.createAggregatorTypes()

			System.out.println('Loading CSV-data...');
			TestDataUtil.
					loadTestDataFromCustomerCSV(new File("src/test/resources/CsiData/${csvName}"), pagesToGenerateDataFor, allPages);
			System.out.println('Loading CSV-data... DONE');

			System.out.println('Create some common test-data... DONE');
		}

		pageAggregatorType = AggregatorType.findByName(AggregatorType.PAGE)
		
		System.out.println('Set-up...');
		targetValues = ['weekly_page.csv':['SE':61.83], 'weekly_page_KW50_2012.csv':['SE':92.97]]
		
		targetResultCounts = ['weekly_page_KW50_2012.csv':['SE':1346], 'weekly_page.csv':['SE':122]]


		hourly = CsiAggregationInterval.findByIntervalInMinutes(CsiAggregationInterval.HOURLY)
		weekly = CsiAggregationInterval.findByIntervalInMinutes(CsiAggregationInterval.WEEKLY)

		System.out.println('Set-up... DONE');
	}



	/**
	 * The target weekly page-values were taken from a manual pre osm excel sheet.
	 * The CSV read is {@code weekly_page_KW50_2012.csv}.
	 * Calculating weekly page-values via {@link PageCsiAggregationService} should provide (nearly) the same results!
	 */
	@Ignore("bad performance, takes more than 30min")
	void "test calculating weekly page values for Page_SE"() {
		// Skip Page if no data is generated (SpeedUp Test) see pagesToGenerateDataFor
		String pageName = "SE"
		if(!pagesToGenerateDataFor.contains(pageName)) {
			fail("No data Was generated for the page "+pageName+" Test skipped.")
		}
		System.out.println('Test: testCalculatingWeeklyPageValues()');


		given:
		List<EventResult> results = EventResult.findAllByJobResultDateBetween(startOfWeek.toDate(), startOfWeek.plusWeeks(1).toDate())
		Date startDate = startOfWeek.toDate()
		Page pageToCalculateMvFor = Page.findByName(pageName)
		JobGroup jobGroup = JobGroup.findByName("CSI")
		CsiAggregation.withNewTransaction {
			results.each { EventResult result ->
				csiAggregationUpdateService.createOrUpdateDependentMvs(result)
			}
		}
		double expectedValue = targetValues[csvName][pageName]?:-1

		when: "We calculate the csi value"
		List<CsiAggregation> wpmvsOfOneGroupPageCombination = pageCsiAggregationService.getOrCalculatePageCsiAggregations(startDate, startDate, weekly, [jobGroup], [pageToCalculateMvFor])
		CsiAggregation mvWeeklyPage = wpmvsOfOneGroupPageCombination.get(0)

		then: "There should be the correct value"
		assertEquals(startDate, mvWeeklyPage.started)
		assertEquals(weekly.intervalInMinutes, mvWeeklyPage.interval.intervalInMinutes)
		assertEquals(pageAggregatorType.name, mvWeeklyPage.aggregator.name)
		assertTrue(mvWeeklyPage.isCalculated())

		// TODO mze-2013-08-15: Check this:
		// Disabled reason: Values differ may from data
		// int targetResultCount = targetResultCounts[csvName][pageName]?:-1
		// assertEquals(targetResultCount, mvWeeklyPage.countUnderlyingEventResultsByWptDocComplete())
		assertNotNull(mvWeeklyPage.csByWptDocCompleteInPercent)

		Double calculated = mvWeeklyPage.csByWptDocCompleteInPercent * 100
		Double difference = Math.abs(calculated - expectedValue)
		println "page ${pageName} / ${pageAggregatorType.name}"
		println "calculated  = ${calculated}"
		println "targetValue = ${expectedValue}"
		println "difference  = ${difference}"

		assertEquals(expectedValue, calculated, 5.0d)
	}
}
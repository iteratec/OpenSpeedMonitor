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

import de.iteratec.osm.measurement.environment.Browser
import de.iteratec.osm.measurement.environment.wptserverproxy.LocationAndResultPersisterService
import de.iteratec.osm.measurement.schedule.ConnectivityProfile
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.measurement.schedule.JobService
import de.iteratec.osm.report.chart.AggregatorType
import de.iteratec.osm.report.chart.CsiAggregation
import de.iteratec.osm.report.chart.CsiAggregationInterval
import de.iteratec.osm.result.CsiAggregationTagService
import de.iteratec.osm.result.EventResult
import org.joda.time.DateTime

class WeeklyShopMultipleCsiGroupsIntTests extends NonTransactionalIntegrationSpec {

	/** injected by grails */
	EventCsiAggregationService eventCsiAggregationService
	JobService jobService
	CsiAggregationTagService csiAggregationTagService
	static ShopCsiAggregationService shopCsiAggregationService
	static LocationAndResultPersisterService locationAndResultPersisterService

	Map<String, Double> targetValues
	List<Page> pageObjectsToTest

	static CsiAggregationInterval weeklyInterval
	static AggregatorType shopAggregatorType
	static List<JobGroup> csiGroups
	static List<CsiAggregation> wsmvs
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

	def setup() {
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
	void testCreationAndCalculationOfWeeklyShopValues() {
		setup:
		Date startDate = startOfWeek.toDate()

		when:
		List<EventResult> results = EventResult.findAllByJobResultDateBetween(startDate, new DateTime(startDate).plusWeeks(1).toDate())
		
		CsiAggregationInterval weeklyInterval = CsiAggregationInterval.findByIntervalInMinutes(CsiAggregationInterval.WEEKLY)
		List<CsiAggregation> wpmvsOfOneGroupPageCombination = shopCsiAggregationService.getOrCalculateShopCsiAggregations(startDate, startDate, weeklyInterval, [csiGroup])

		then:
		24 == results.size()
		wsmvs != null
		countWeeklyShopMvsToBeCreated == wsmvs.size()
		startDate == mvWeeklyShop.started
		weeklyInterval.intervalInMinutes == mvWeeklyShop.interval.intervalInMinutes
		shopAggregatorType.name == mvWeeklyShop.aggregator.name
		mvWeeklyShop.isCalculated()

		1 == wpmvsOfOneGroupPageCombination.size()
		wpmvsOfOneGroupPageCombination.each{mvWeeklyPage ->
			assert csiGroup.ident().toString() == mvWeeklyPage.tag
			assert Double.compare(targetValues["${csiGroup.name}"], mvWeeklyPage.csByWptDocCompleteInPercent.round(2)) < 0.01
		}

		where:
		mvWeeklyShop << wsmvs
		csiGroup << csiGroups
	}

	/**
	 * Creating testdata.
	 */
	def setupSpec() {
		System.out.println('Create some common test-data...');
		TestDataUtil.createOsmConfig()
		TestDataUtil.createCsiAggregationIntervals()
		TestDataUtil.createAggregatorTypes()
		System.out.println('Create some common test-data... DONE');

		System.out.println('Loading CSV-data...');
		TestDataUtil.loadTestDataFromCustomerCSV(new File("test/resources/CsiData/${csvName}"), pagesToTest, pagesToTest);
		System.out.println('Loading CSV-data... DONE');

		csiGroups = [
				JobGroup.findByName(csiGroup1Name),
				JobGroup.findByName(csiGroup2Name)
		]

		EventResult.findAll().each {
			locationAndResultPersisterService.informDependentCsiAggregations(it)
		}
		CsiConfiguration.findAll().each { csiConfiguration ->
			ConnectivityProfile.findAll().each { connectivityProfile ->
				Browser.findAll().each { browser ->
					csiConfiguration.browserConnectivityWeights.add(new BrowserConnectivityWeight(browser: browser, connectivity: connectivityProfile, weight: 1))
				}
				Page.findAll().each { page ->
					csiConfiguration.pageWeights.add(new PageWeight(page: page, weight: 1))
				}
			}
		}

		shopAggregatorType = AggregatorType.findByName(AggregatorType.SHOP)
		weeklyInterval = CsiAggregationInterval.findByIntervalInMinutes(CsiAggregationInterval.WEEKLY)
		wsmvs = shopCsiAggregationService.getOrCalculateShopCsiAggregations(startOfWeek.toDate(), startOfWeek.toDate(), weeklyInterval, csiGroups)
	}
}

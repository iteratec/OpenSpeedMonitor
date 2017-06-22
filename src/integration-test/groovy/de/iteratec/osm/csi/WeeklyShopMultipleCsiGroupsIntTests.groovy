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
import de.iteratec.osm.measurement.environment.wptserverproxy.ResultPersisterService
import de.iteratec.osm.measurement.schedule.ConnectivityProfile
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.report.chart.AggregatorType
import de.iteratec.osm.report.chart.CsiAggregation
import de.iteratec.osm.report.chart.CsiAggregationInterval
import de.iteratec.osm.result.EventResult
import grails.test.mixin.integration.Integration
import org.joda.time.DateTime
import org.springframework.test.annotation.Rollback

@Integration
@Rollback
class WeeklyShopMultipleCsiGroupsIntTests extends NonTransactionalIntegrationSpec {

    /** injected by grails */
    JobGroupCsiAggregationService jobGroupCsiAggregationService
    ResultPersisterService resultPersisterService

    Map<String, Double> targetValues
    List<Page> pageObjectsToTest

    static CsiAggregationInterval weeklyInterval
    static AggregatorType shopAggregatorType
    static List<JobGroup> csiGroups
    static final List<String> pagesToTest = [
            'HP',
            'MES',
            'SE',
            'ADS',
            'WKBS',
            'WK'
    ]
    static final String csvName = 'weekly_shop_multiple_csi_groups.csv'
    static final DateTime startOfWeek = new DateTime(2012, 11, 12, 0, 0, 0)
    static final String csiGroup1Name = 'csiGroup1'
    static final String csiGroup2Name = 'csiGroup2'

    static final Integer countWeeklyShopMvsToBeCreated = 2

    def setupData() {
        System.out.println('Create some common test-data...');
        TestDataUtil.createOsmConfig()
        TestDataUtil.createCsiAggregationIntervals()
        TestDataUtil.createAggregatorTypes()
        System.out.println('Create some common test-data... DONE');

        System.out.println('Loading CSV-data...');
        TestDataUtil.loadTestDataFromCustomerCSV(new File("src/test/resources/CsiData/${csvName}"), pagesToTest, pagesToTest);
        System.out.println('Loading CSV-data... DONE');
        csiGroups = [
                JobGroup.findByName(csiGroup1Name),
                JobGroup.findByName(csiGroup2Name)
        ]

        EventResult.findAll().each {
            resultPersisterService.informDependentCsiAggregations(it)
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

        targetValues = [
                csiGroup1: 0.391,
                csiGroup2: 0.691
        ]
        pageObjectsToTest = []
        pagesToTest.each {
            pageObjectsToTest.add(Page.findByName(it))
        }
    }

    /**
     * After pre-calculation of hourly job-{@link CsiAggregation}s the creation and calculation of weekly shop-{@link CsiAggregation}s is tested.
     */
    void testCreationAndCalculationOfWeeklyShopValues() {
        setup:
        EventResult.withNewSession { session ->
            setupData()
            session.flush()
        }
        Date startDate = startOfWeek.toDate()
        List<CsiAggregation> weeklyShopCsiAggregations
        when:
        List<EventResult> results
        EventResult.withNewSession {
            results = EventResult.findAllByJobResultDateBetween(startDate, new DateTime(startDate).plusWeeks(1).toDate())
            CsiAggregationInterval weeklyInterval = CsiAggregationInterval.findByIntervalInMinutes(CsiAggregationInterval.WEEKLY)
            weeklyShopCsiAggregations = jobGroupCsiAggregationService.getOrCalculateShopCsiAggregations(startDate, startDate, weeklyInterval, csiGroups)
        }

        then:
        24 == results.size()
        countWeeklyShopMvsToBeCreated == weeklyShopCsiAggregations.size()
        CsiAggregation.withNewSession {
            weeklyShopCsiAggregations*.id.each { mvWeeklyShopId ->
                CsiAggregation mvWeeklyShop = CsiAggregation.get(mvWeeklyShopId)
                assert shopAggregatorType.name == mvWeeklyShop.aggregator.name
                assert startDate == mvWeeklyShop.started
                assert weeklyInterval.intervalInMinutes == mvWeeklyShop.interval.intervalInMinutes
                assert mvWeeklyShop.isCalculated()
                assert Double.compare(targetValues["${mvWeeklyShop.jobGroup.name}"], mvWeeklyShop.csByWptDocCompleteInPercent.round(2)) < 0.01
            }
        }
    }
}

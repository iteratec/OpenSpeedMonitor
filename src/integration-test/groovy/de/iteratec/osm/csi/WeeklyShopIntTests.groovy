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
import grails.transaction.Rollback
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import spock.lang.Shared

@Integration
@Rollback
class WeeklyShopIntTests extends NonTransactionalIntegrationSpec {

    /** injected by grails */
    ShopCsiAggregationService shopCsiAggregationService
    @Shared
    ResultPersisterService resultPersisterService

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
    static final String csiGroupName = "CSI"
    /** Testdata is persisted respective this csv */
    static final String csvFilename = 'weekly_page.csv'

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
                    loadTestDataFromCustomerCSV(new File("src/test/resources/CsiData/${csvFilename}"), pagesToTest, pagesToTest);
            System.out.println('Loading CSV-data... DONE');

        }

        EventResult.findAll().each {
            resultPersisterService.informDependentCsiAggregations(it)
        }

        CsiAggregation.withNewTransaction {
            CsiConfiguration.findAll().each { csiConfiguration ->
                ConnectivityProfile.findAll().each { connectivityProfile ->
                    Browser.findAll().each { browser ->
                        csiConfiguration.browserConnectivityWeights.
                                add(new BrowserConnectivityWeight(browser: browser, connectivity: connectivityProfile, weight: 1))
                    }
                    Page.findAll().each { page ->
                        csiConfiguration.pageWeights.add(new PageWeight(page: page, weight: 1))
                    }
                }
            }

            System.out.println('Create some common test-data... DONE');
        }
        hourly = CsiAggregationInterval.findByIntervalInMinutes(CsiAggregationInterval.HOURLY)
        weekly = CsiAggregationInterval.findByIntervalInMinutes(CsiAggregationInterval.WEEKLY)
        pageAggregatorMeasuredEvent = AggregatorType.findByName(AggregatorType.MEASURED_EVENT)
        pageAggregatorShop = AggregatorType.findByName(AggregatorType.SHOP)
        pageAggregatorType = AggregatorType.findByName(AggregatorType.PAGE)

    }

    //tests//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    void testCalculatingWeeklyShopValueWithoutData() {
        setup:
        Date startDate = new DateTime(2012, 01, 12, 0, 0, DateTimeZone.UTC).toDate()
        JobGroup csiGroup = JobGroup.findByName(csiGroupName)

        CsiAggregation mvWeeklyShop
        CsiAggregation.withNewTransaction {
            mvWeeklyShop = new CsiAggregation(
                    started: startDate,
                    interval: weekly,
                    aggregator: pageAggregatorShop,
                    jobGroup: csiGroup,
                    csByWptDocCompleteInPercent: null,
                    underlyingEventResultsByWptDocComplete: ''
            ).save(failOnError: true)
        }

        when:
        shopCsiAggregationService.calcCsiAggregations([mvWeeklyShop.id])

        then:
        startDate == mvWeeklyShop.started
        weekly.intervalInMinutes == mvWeeklyShop.interval.intervalInMinutes
        pageAggregatorShop.name == mvWeeklyShop.aggregator.name
        csiGroup.id == mvWeeklyShop.jobGroupId
        mvWeeklyShop.isCalculated()
        0 == mvWeeklyShop.countUnderlyingEventResultsByWptDocComplete()
        mvWeeklyShop.csByWptDocCompleteInPercent == null
    }

    /**
     * Tests the calculation of one weekly-shop-{@link CsiAggregation}. Databasis for calculation are weekly page-{@link CsiAggregation}s. These get calculated
     * on-the-fly while calculating the respective weekly-shop-{@link CsiAggregation}. The hourly-event-{@link CsiAggregation}s of the period have to exist (they
     * won't get calculated on-the-fly. Therefore these get precalculated in test here.
     */
    void testCalculatingWeeklyShopValue() {
        setup:
        Date startDate = new DateTime(2012, 11, 12, 0, 0, DateTimeZone.UTC).toDate()
        Integer targetResultCount = 233 + 231 + 122 + 176 + 172 + 176

        List<EventResult> results = EventResult.findAllByJobResultDateBetween(startDate, new DateTime(startDate).plusWeeks(1).toDate())

        //create test-specific data
        JobGroup csiGroup = JobGroup.findByName(csiGroupName)
        Double expectedValue = 61.30
        long csiAggregationId

        CsiAggregation.withNewTransaction {

            CsiAggregation aggregation = new CsiAggregation(
                    started: startDate,
                    interval: weekly,
                    aggregator: pageAggregatorShop,
                    jobGroup: csiGroup,
                    csByWptDocCompleteInPercent: null,
                    underlyingEventResultsByWptDocComplete: ''
            ).save(failOnError: true, flush: true)
            csiAggregationId = aggregation.id
        }

        CsiAggregationInterval.findAll()

        when:
        CsiAggregation.withNewSession { session ->
            shopCsiAggregationService.calcCsiAggregations([csiAggregationId])
            session.flush()
        }
        CsiAggregation mvWeeklyShop = CsiAggregation.get(csiAggregationId)

        then:
		Math.abs(results.size() - targetResultCount) < 30
        startDate == mvWeeklyShop.started
        weekly.intervalInMinutes == mvWeeklyShop.interval.intervalInMinutes
        pageAggregatorShop.name == mvWeeklyShop.aggregator.name
        csiGroup == mvWeeklyShop.jobGroup
        mvWeeklyShop.isCalculated()
        mvWeeklyShop.csByWptDocCompleteInPercent != null
        Double calculated = mvWeeklyShop.csByWptDocCompleteInPercent * 100
        //TODO: diff should be smaller
        Double.compare(expectedValue, calculated) < 5.0d
    }
}

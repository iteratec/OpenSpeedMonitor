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

import de.iteratec.osm.csi.weighting.WeightFactor
import de.iteratec.osm.csi.weighting.WeightedCsiValue
import de.iteratec.osm.csi.weighting.WeightedValue
import de.iteratec.osm.csi.weighting.WeightingService
import de.iteratec.osm.measurement.environment.Browser
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.report.chart.*
import de.iteratec.osm.result.EventResult
import grails.test.mixin.integration.Integration
import grails.transaction.Rollback
import org.joda.time.DateTime
import org.junit.Test

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertTrue

/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
@Integration
@Rollback
class ShopCsiAggregationServiceTests extends NonTransactionalIntegrationSpec {
    CsiAggregationInterval weeklyInterval, dailyInterval, hourlyInterval
    JobGroup jobGroup1, jobGroup2, jobGroup3
    Page page1

    AggregatorType shopAggregator, pageAggregator

    Browser browser;

    static final double DELTA = 1e-15
    DateTime startDate = new DateTime(2013, 5, 16, 0, 0, 0)
    String jobGroupName1 = 'myJobGroup1'
    String jobGroupName2 = 'myJobGroup2'
    String jobGroupName3 = 'myJobGroup3'

    ShopCsiAggregationService shopCsiAggregationService

    def setup() {
        weeklyInterval = new CsiAggregationInterval(name: 'weekly', intervalInMinutes: CsiAggregationInterval.WEEKLY).save(failOnError: true)
        dailyInterval = new CsiAggregationInterval(name: 'daily', intervalInMinutes: CsiAggregationInterval.DAILY).save(failOnError: true)
        hourlyInterval = new CsiAggregationInterval(name: 'hourly', intervalInMinutes: CsiAggregationInterval.HOURLY).save(failOnError: true)

        TestDataUtil.createAggregatorTypes()
        shopAggregator = AggregatorType.findByName(AggregatorType.SHOP)
        pageAggregator = AggregatorType.findByName(AggregatorType.PAGE)

        jobGroup1 = TestDataUtil.createJobGroup(jobGroupName1)
        jobGroup2 = TestDataUtil.createJobGroup(jobGroupName2)
        jobGroup3 = TestDataUtil.createJobGroup(jobGroupName3)

        page1 = new Page(name: "page1", weight: 1).save(failOnError: true)

        browser = new Browser(name: "Test", weight: 1).save(failOnError: true);

        //with existing JobGroup:
        TestDataUtil.createCsiAggregation(startDate.toDate(), weeklyInterval, shopAggregator, jobGroup1, null, null, "", false)
        TestDataUtil.createCsiAggregation(startDate.toDate(), weeklyInterval, shopAggregator, jobGroup2, null, null, "", false)
        TestDataUtil.createCsiAggregation(startDate.toDate(), weeklyInterval, shopAggregator, jobGroup3, null, null, "", false)

        //mocks common for all tests
//        shopCsiAggregationService.csiAggregationUtilService = grailsApplication.mainContext.getBean('csiAggregationUtilService')
//        shopCsiAggregationService.pageCsiAggregationService.csiAggregationUtilService = grailsApplication.mainContext.getBean('csiAggregationUtilService')
//        shopCsiAggregationService.pageCsiAggregationService.meanCalcService = grailsApplication.mainContext.getBean('meanCalcService')
//        shopCsiAggregationService.performanceLoggingService = grailsApplication.mainContext.getBean('performanceLoggingService')
//        mockCsiAggregationUpdateEventDaoService()
    }

    void "test findAll"() {
        Integer countMvs = 3

        expect:
        shopCsiAggregationService.findAll(startDate.toDate(), startDate.toDate(), weeklyInterval, JobGroup.list()).size() == countMvs
    }

    void "test findAllByJobGroups"() {
        when:
        Integer with_group1to3 = 3
        Integer with_group2to3 = 2
        Integer with_group1or3 = 2
        Integer with_group2 = 1

        then:
        shopCsiAggregationService.findAll(startDate.toDate(), startDate.toDate(), weeklyInterval, [jobGroup1, jobGroup2, jobGroup3]).size() == with_group1to3
        shopCsiAggregationService.findAll(startDate.toDate(), startDate.toDate(), weeklyInterval, [jobGroup2, jobGroup3]).size() == with_group2to3
        shopCsiAggregationService.findAll(startDate.toDate(), startDate.toDate(), weeklyInterval, [jobGroup1, jobGroup3]).size() == with_group1or3
        shopCsiAggregationService.findAll(startDate.toDate(), startDate.toDate(), weeklyInterval, [jobGroup2]).size() == with_group2
    }

    /**
     * Tests calculation of daily-shopAggregator{@link CsiAggregation}s, which aren't calculated when new {@link EventResult}s get persisted.
     * In this test one single pageAggregator-{@link CsiAggregation}s exists, which should be the database of the calculation of the daily-shopAggregator-{@link CsiAggregation}.
     */
    void "test calculation_DailyInterval_SingleDailyMv"() {
        given:
        DateTime startedTime = new DateTime(2013, 5, 16, 12, 12, 11)

        List<WeightedCsiValue> weightedCsiValuesToReturnInMock = [
                new WeightedCsiValue(weightedValue: new WeightedValue(value: 12d, weight: 1d), underlyingEventResultIds: [1, 2, 3])]

        //mocking inner services
        mockWeightingService(weightedCsiValuesToReturnInMock, [])

        //precondition
        List<CsiAggregation> mvs = shopCsiAggregationService.findAll(startedTime.toDate(), startedTime.toDate(), dailyInterval, JobGroup.list())
        assertEquals(0, mvs.size())

        when:
        List<CsiAggregation> calculatedMvs = shopCsiAggregationService.getOrCalculateShopCsiAggregations(startedTime.toDate(), startedTime.toDate(), dailyInterval, [jobGroup1])
        mvs = shopCsiAggregationService.findAll(startedTime.toDate(), startedTime.toDate(), dailyInterval, JobGroup.list())

        then:
        assertEquals(1, calculatedMvs.size())
        assertTrue(calculatedMvs[0].isCalculated())
        assertEquals(12d, calculatedMvs[0].csByWptDocCompleteInPercent, DELTA)
        assertEquals(1, mvs.size())
    }

    /**
     * Tests calculation of daily-shopAggregator-{@link CsiAggregation}s, which aren't calculated when new {@link EventResult}s get persisted.
     * In this test pageAggregator-{@link CsiAggregation}s with different weights exist.
     */
    void "test calculation_DailyInterval_MultipleDailyMv"() {
        given:
        DateTime startedTime = new DateTime(2013, 5, 16, 12, 12, 11)
        double valueFirstMv = 12d
        double pageWeightFirstMv = 1d
        double valueSecondMv = 10d
        double pageWeightSecondMv = 2d
        double valueThirdMv = 13d
        double pageWeightThirdMv = 1d
        double sumOfAllWeights = 4d

        //mocking inner services
        List<WeightedCsiValue> weightedCsiValuesToReturnInMock = [
                new WeightedCsiValue(weightedValue: new WeightedValue(value: 12d, weight: 1d), underlyingEventResultIds: [1, 2, 3]),
                new WeightedCsiValue(weightedValue: new WeightedValue(value: 10d, weight: 2d), underlyingEventResultIds: [4]),
                new WeightedCsiValue(weightedValue: new WeightedValue(value: 13d, weight: 1d), underlyingEventResultIds: [5, 6])]

        mockWeightingService(weightedCsiValuesToReturnInMock, [])

        //precondition
        List<CsiAggregation> mvs = shopCsiAggregationService.findAll(startedTime.toDate(), startedTime.toDate(), dailyInterval, JobGroup.list())
        assertEquals(0, mvs.size())

        when:
        List<CsiAggregation> calculatedMvs = shopCsiAggregationService.getOrCalculateShopCsiAggregations(startedTime.toDate(), startedTime.toDate(), dailyInterval, [jobGroup1])
        mvs = shopCsiAggregationService.findAll(startedTime.toDate(), startedTime.toDate(), dailyInterval, JobGroup.list())

        then:
        assertEquals(1, calculatedMvs.size())
        assertEquals(shopAggregator.name, calculatedMvs[0].aggregator.name)
        assertTrue(calculatedMvs[0].isCalculated())
        assertEquals(
                (((valueFirstMv * pageWeightFirstMv) + (valueSecondMv * pageWeightSecondMv) + (valueThirdMv * pageWeightThirdMv)) / sumOfAllWeights),
                calculatedMvs[0].csByWptDocCompleteInPercent,
                DELTA
        )
        assertEquals(1, mvs.size())
    }

    /**
     * Tests calculation of daily-shopAggregator-{@link CsiAggregation}s, which aren't calculated when new {@link EventResult}s get persisted.
     * In this test no pageAggregator-{@link CsiAggregation}s exist, which are database of the calculation of daily-shopAggregator-{@link CsiAggregation}s. So all calculated values should have state {@link Calculated#YesNoData}
     */
    void "test calculation_DailyInterval_MultipleHourlyMv_YesCalculatedNoData"() {
        given:
        DateTime startedTime = new DateTime(2013, 5, 16, 12, 12, 11)

        //mocking inner services
        mockWeightingService([], [])

        //precondition

        List<CsiAggregation> mvs = shopCsiAggregationService.findAll(startedTime.toDate(), startedTime.toDate(), dailyInterval, JobGroup.list())
        assertEquals(0, mvs.size())

        when:
        List<CsiAggregation> calculatedMvs = shopCsiAggregationService.getOrCalculateShopCsiAggregations(startedTime.toDate(), startedTime.toDate(), dailyInterval, [jobGroup1])
        mvs = shopCsiAggregationService.findAll(startedTime.toDate(), startedTime.toDate(), dailyInterval, JobGroup.list())

        then:
        assertEquals(1, calculatedMvs.size())
        assertTrue(calculatedMvs[0].isCalculated())
        assertEquals(null, calculatedMvs[0].csByWptDocCompleteInPercent)
        assertEquals(0, calculatedMvs[0].getUnderlyingEventResultsByWptDocComplete().size())
        assertEquals(1, mvs.size())
    }

    /**
     * Mocks methods of {@link WeightingService}.
     */

    private void mockWeightingService(List<WeightedCsiValue> toReturnFromGetWeightedCsiValues, List<WeightedCsiValue> toReturnFromGetWeightedCsiValuesByVisuallyComplete) {
        def weightingService = grailsApplication.mainContext.getBean('weightingService')
        weightingService.metaClass.getWeightedCsiValues = {
            List<CsiValue> csiValues, Set<WeightFactor> weightFactors, CsiConfiguration csiConfiguration ->
                return toReturnFromGetWeightedCsiValues
        }
        weightingService.metaClass.getWeightedCsiValuesByVisuallyComplete = {
            List<CsiValue> csiValues, Set<WeightFactor> weightFactors, CsiConfiguration csiConfiguration ->
                return toReturnFromGetWeightedCsiValuesByVisuallyComplete
        }
        shopCsiAggregationService.weightingService = weightingService
    }
}

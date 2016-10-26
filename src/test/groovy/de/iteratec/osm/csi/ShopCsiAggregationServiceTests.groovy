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
import de.iteratec.osm.measurement.environment.Location
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.report.chart.*
import de.iteratec.osm.result.EventResult
import de.iteratec.osm.result.dao.DefaultMeasuredEventDaoService
import de.iteratec.osm.util.PerformanceLoggingService
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import org.joda.time.DateTime
import org.junit.Test

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertTrue

/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
@TestFor(ShopCsiAggregationService)
@Mock([MeanCalcService, CsiAggregation, CsiAggregationInterval, AggregatorType, Browser, JobGroup, Location, PageCsiAggregationService,
        Page, DefaultMeasuredEventDaoService, EventCsiAggregationService, CsiAggregationDaoService, CustomerSatisfactionWeightService, CsiAggregationUpdateEvent, CsiAggregation])
class ShopCsiAggregationServiceTests {
    CsiAggregationInterval weeklyInterval, dailyInterval, hourlyInterval
    JobGroup jobGroup1, jobGroup2, jobGroup3
    Page page1

    AggregatorType shop, page

    Browser browser;

    static final double DELTA = 1e-15
    DateTime startDate = new DateTime(2013, 5, 16, 0, 0, 0)
    String jobGroupName1 = 'myJobGroup1'
    String jobGroupName2 = 'myJobGroup2'
    String jobGroupName3 = 'myJobGroup3'

    ShopCsiAggregationService serviceUnderTest

    def doWithSpring = {
        pageCsiAggregationService(PageCsiAggregationService)
        csiAggregationDaoService(CsiAggregationDaoService)
        csiAggregationUtilService(CsiAggregationUtilService)
        meanCalcService(MeanCalcService)
        performanceLoggingService(PerformanceLoggingService)
        csiAggregationUpdateEventDaoService(CsiAggregationUpdateEventDaoService)
        weightingService(WeightingService)
    }

    void setUp() {
        weeklyInterval = new CsiAggregationInterval(name: 'weekly', intervalInMinutes: CsiAggregationInterval.WEEKLY).save(failOnError: true)
        dailyInterval = new CsiAggregationInterval(name: 'daily', intervalInMinutes: CsiAggregationInterval.DAILY).save(failOnError: true)
        hourlyInterval = new CsiAggregationInterval(name: 'hourly', intervalInMinutes: CsiAggregationInterval.HOURLY).save(failOnError: true)

        shop = new AggregatorType(name: AggregatorType.SHOP).save(validate: false)
        page = new AggregatorType(name: AggregatorType.PAGE).save(validate: false)
        new AggregatorType(name: AggregatorType.MEASURED_EVENT, measurandGroup: MeasurandGroup.NO_MEASURAND).save(failOnError: true)

        jobGroup1 = new JobGroup(name: jobGroupName1).save(validate: false)
        jobGroup2 = new JobGroup(name: jobGroupName2).save(validate: false)
        jobGroup3 = new JobGroup(name: jobGroupName3).save(validate: false)

        page1 = new Page(name: "page1", weight: 1).save(failOnError: true)

        browser = new Browser(name: "Test", weight: 1).save(failOnError: true);

        //with existing JobGroup:
        new CsiAggregation(interval: weeklyInterval, aggregator: shop, jobGroup: jobGroup1, started: startDate.toDate()).save(validate: false)
        new CsiAggregation(interval: weeklyInterval, aggregator: shop, jobGroup: jobGroup2, started: startDate.toDate()).save(validate: false)
        new CsiAggregation(interval: weeklyInterval, aggregator: shop, jobGroup: jobGroup3, started: startDate.toDate()).save(validate: false)
        //not with existing JobGroup:
        new CsiAggregation(interval: weeklyInterval, aggregator: shop, jobGroup: null, started: startDate.toDate()).save(validate: false)

        serviceUnderTest = service
        //mocks common for all tests
        serviceUnderTest.csiAggregationUtilService = grailsApplication.mainContext.getBean('csiAggregationUtilService')
        serviceUnderTest.pageCsiAggregationService.csiAggregationUtilService = grailsApplication.mainContext.getBean('csiAggregationUtilService')
        serviceUnderTest.pageCsiAggregationService.meanCalcService = grailsApplication.mainContext.getBean('meanCalcService')
        serviceUnderTest.performanceLoggingService = grailsApplication.mainContext.getBean('performanceLoggingService')
        mockCsiAggregationUpdateEventDaoService()
    }

    void tearDown() {
        // Tear down logic here
    }
    
    @Test
    void testFindAll() {
        Integer countMvs = 4
        assert service.findAll(startDate.toDate(), startDate.toDate(), weeklyInterval).size() == countMvs
    }

    @Test
    void testFindAllByJobGroups() {
        mockCsiAggregationDaoService()

        JobGroup group1 = JobGroup.findByName(jobGroupName1)
        JobGroup group2 = JobGroup.findByName(jobGroupName2)
        JobGroup group3 = JobGroup.findByName(jobGroupName3)

        Integer with_group1to3 = 3
        Integer with_group2to3 = 2
        Integer with_group1or3 = 2
        Integer with_group2 = 1

        List<JobGroup> groups = [group1, group2, group3]
        assert service.findAll(startDate.toDate(), startDate.toDate(), weeklyInterval, groups).size() == with_group1to3
        groups = [group2, group3]
        assert service.findAll(startDate.toDate(), startDate.toDate(), weeklyInterval, groups).size() == with_group2to3
        groups = [group1, group3]
        assert service.findAll(startDate.toDate(), startDate.toDate(), weeklyInterval, groups).size() == with_group1or3
        groups = [group2]
        assert service.findAll(startDate.toDate(), startDate.toDate(), weeklyInterval, groups).size() == with_group2
    }

    /**
     * Tests calculation of daily-shopAggregator{@link CsiAggregation}s, which aren't calculated when new {@link EventResult}s get persisted.
     * In this test one single pageAggregator-{@link CsiAggregation}s exists, which should be the database of the calculation of the daily-shopAggregator-{@link CsiAggregation}.
     */
    @Test
    void testCalculation_DailyInterval_SingleDailyMv() {

        //test-specific data

        DateTime startedTime = new DateTime(2013, 5, 16, 12, 12, 11)

        List<WeightedCsiValue> weightedCsiValuesToReturnInMock = [
                new WeightedCsiValue(weightedValue: new WeightedValue(value: 12d, weight: 1d), underlyingEventResultIds: [1, 2, 3])]

        //mocking inner services
        mockWeightingService(weightedCsiValuesToReturnInMock, [])
        mockPageCsiAggregationService()

        //precondition

        List<CsiAggregation> mvs = serviceUnderTest.findAll(startedTime.toDate(), startedTime.toDate(), dailyInterval)
        assertEquals(0, mvs.size())

        //test execution
        List<CsiAggregation> calculatedMvs = serviceUnderTest.getOrCalculateShopCsiAggregations(startedTime.toDate(), startedTime.toDate(), dailyInterval, [jobGroup1])

        //assertions

        assertEquals(1, calculatedMvs.size())
        assertTrue(calculatedMvs[0].isCalculated())
        assertEquals(12d, calculatedMvs[0].csByWptDocCompleteInPercent, DELTA)

        mvs = serviceUnderTest.findAll(startedTime.toDate(), startedTime.toDate(), dailyInterval)
        assertEquals(1, mvs.size())
    }

    /**
     * Tests calculation of daily-shopAggregator-{@link CsiAggregation}s, which aren't calculated when new {@link EventResult}s get persisted.
     * In this test pageAggregator-{@link CsiAggregation}s with different weights exist.
     */
    @Test
    void testCalculation_DailyInterval_MultipleDailyMv() {
        DateTime startedTime = new DateTime(2013, 5, 16, 12, 12, 11)

        //test-specific data

        List<WeightedCsiValue> weightedCsiValuesToReturnInMock = [
                new WeightedCsiValue(weightedValue: new WeightedValue(value: 12d, weight: 1d), underlyingEventResultIds: [1, 2, 3]),
                new WeightedCsiValue(weightedValue: new WeightedValue(value: 10d, weight: 2d), underlyingEventResultIds: [4]),
                new WeightedCsiValue(weightedValue: new WeightedValue(value: 13d, weight: 1d), underlyingEventResultIds: [5, 6])]

        //mocking inner services
        mockWeightingService(weightedCsiValuesToReturnInMock, [])
        mockPageCsiAggregationService()

        //precondition

        List<CsiAggregation> mvs = serviceUnderTest.findAll(startedTime.toDate(), startedTime.toDate(), dailyInterval)
        assertEquals(0, mvs.size())

        //test execution
        List<CsiAggregation> calculatedMvs = serviceUnderTest.getOrCalculateShopCsiAggregations(startedTime.toDate(), startedTime.toDate(), dailyInterval, [jobGroup1])

        //assertions

        assertEquals(1, calculatedMvs.size())
        assertEquals(shop, calculatedMvs[0].aggregator)
        assertTrue(calculatedMvs[0].isCalculated())
        double valueFirstMv = 12d
        double pageWeightFirstMv = 1d
        double valueSecondMv = 10d
        double pageWeightSecondMv = 2d
        double valueThirdMv = 13d
        double pageWeightThirdMv = 1d
        double sumOfAllWeights = 4d
        assertEquals(
                (((valueFirstMv * pageWeightFirstMv) + (valueSecondMv * pageWeightSecondMv) + (valueThirdMv * pageWeightThirdMv)) / sumOfAllWeights),
                calculatedMvs[0].csByWptDocCompleteInPercent,
                DELTA
        )

        mvs = serviceUnderTest.findAll(startedTime.toDate(), startedTime.toDate(), dailyInterval)
        assertEquals(1, mvs.size())

    }

    /**
     * Tests calculation of daily-shopAggregator-{@link CsiAggregation}s, which aren't calculated when new {@link EventResult}s get persisted.
     * In this test no pageAggregator-{@link CsiAggregation}s exist, which are database of the calculation of daily-shopAggregator-{@link CsiAggregation}s. So all calculated values should have state {@link Calculated#YesNoData}
     */
    @Test
    void testCalculation_DailyInterval_MultipleHourlyMv_YesCalculatedNoData() {
        DateTime startedTime = new DateTime(2013, 5, 16, 12, 12, 11)

        //mocking inner services
        mockWeightingService([], [])
        mockPageCsiAggregationService()

        //precondition

        List<CsiAggregation> mvs = serviceUnderTest.findAll(startedTime.toDate(), startedTime.toDate(), dailyInterval)
        assertEquals(0, mvs.size())

        //test execution
        List<CsiAggregation> calculatedMvs = serviceUnderTest.getOrCalculateShopCsiAggregations(startedTime.toDate(), startedTime.toDate(), dailyInterval, [jobGroup1])

        //assertions

        assertEquals(1, calculatedMvs.size())
        assertTrue(calculatedMvs[0].isCalculated())
        assertEquals(null, calculatedMvs[0].csByWptDocCompleteInPercent)
        assertEquals(0, calculatedMvs[0].getUnderlyingEventResultsByWptDocComplete().size())

        mvs = serviceUnderTest.findAll(startedTime.toDate(), startedTime.toDate(), dailyInterval)
        assertEquals(1, mvs.size())


    }

    // mocks

    private void mockPageCsiAggregationService() {
        def pageCsiAggregationService = grailsApplication.mainContext.getBean('pageCsiAggregationService')
        pageCsiAggregationService.metaClass.getOrCalculatePageCsiAggregations = {
            Date fromDate, Date toDate, CsiAggregationInterval interval, List<JobGroup> csiGroups ->
                List<CsiAggregation> irrelevantCauseListOfWeightedValuesIsRetrievedByMock = [new CsiAggregation()]
                return irrelevantCauseListOfWeightedValuesIsRetrievedByMock
        }
        serviceUnderTest.pageCsiAggregationService = pageCsiAggregationService
    }

    private void mockCsiAggregationDaoService() {
        CsiAggregationDaoService original = grailsApplication.mainContext.getBean('csiAggregationDaoService')
        serviceUnderTest.csiAggregationDaoService = original
        serviceUnderTest.pageCsiAggregationService.csiAggregationDaoService = original
        serviceUnderTest.pageCsiAggregationService.eventCsiAggregationService.csiAggregationDaoService = original
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
        serviceUnderTest.weightingService = weightingService
    }

    /**
     * Mocks methods of {@link CsiAggregationUpdateEventDaoService}.
     */
    private void mockCsiAggregationUpdateEventDaoService() {
        def csiAggregationUpdateEventDaoService = grailsApplication.mainContext.getBean('csiAggregationUpdateEventDaoService')
        csiAggregationUpdateEventDaoService.metaClass.createUpdateEvent = {
            Long csiAggregationId, CsiAggregationUpdateEvent.UpdateCause cause ->

                new CsiAggregationUpdateEvent(
                        dateOfUpdate: new Date(),
                        csiAggregationId: csiAggregationId,
                        updateCause: cause
                ).save(failOnError: true)

        }
        serviceUnderTest.csiAggregationUpdateEventDaoService = csiAggregationUpdateEventDaoService
    }
}

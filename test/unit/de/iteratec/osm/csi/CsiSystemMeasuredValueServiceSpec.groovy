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

import de.iteratec.osm.csi.weighting.WeightedCsiValue
import de.iteratec.osm.csi.weighting.WeightedValue
import de.iteratec.osm.csi.weighting.WeightingService
import de.iteratec.osm.measurement.environment.Browser
import de.iteratec.osm.measurement.environment.Location
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.measurement.schedule.JobGroupType
import de.iteratec.osm.report.chart.*
import de.iteratec.osm.result.EventResult
import de.iteratec.osm.util.ServiceMocker
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import org.joda.time.DateTime
import org.junit.Test
import spock.lang.Shared
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
@TestMixin(GrailsUnitTestMixin)
@TestFor(CsiSystemMeasuredValueService)
@Mock([MeanCalcService, CsiAggregation, MeasuredValueInterval, AggregatorType, Browser, JobGroup, Location,
        Page, MeasuredValueUpdateEvent, CsiSystem])
class CsiSystemMeasuredValueServiceSpec extends Specification {

    @Shared
    static final ServiceMocker SERVICE_MOCKER = ServiceMocker.create()

    MeasuredValueInterval weeklyInterval, dailyInterval, hourlyInterval
    JobGroup jobGroup1, jobGroup2, jobGroup3
    CsiSystem csiSystem1, csiSystem2, csiSystemWith3

    AggregatorType shop, csiSystemAggregator

    Browser browser;

    static final double DELTA = 1e-15
    DateTime startDate = new DateTime(2013, 5, 16, 0, 0, 0)
    String jobGroupName1 = 'myJobGroup1'
    String jobGroupName2 = 'myJobGroup2'
    String jobGroupName3 = 'myJobGroup3'

    CsiSystemMeasuredValueService serviceUnderTest

    void setup() {

        serviceUnderTest = service

        createTestDataCommonForAllTests()

        mocksCommonForAllTests(serviceUnderTest)

    }

    void tearDown() {
        // Tear down logic here
    }
    
    @Test
    void "find no Mvs if no CsiSystem is given"() {
        given:
        List<CsiSystem> groups = []

        when:
        List<CsiAggregation> emptyList = serviceUnderTest.findAll(startDate.toDate(), startDate.toDate(), weeklyInterval, groups)

        then:
        emptyList.isEmpty()
    }

    @Test
    void "find no Mvs if no Mv is existing"() {
        given:
        List<CsiSystem> groups = CsiSystem.findAll()

        when:
        List<CsiAggregation> emptyList = serviceUnderTest.findAll(startDate.toDate(), startDate.toDate(), weeklyInterval, groups)

        then:
        emptyList.isEmpty()
    }

    @Test
    void "find all Mvs for given CsiSystem"() {
        given:
        List<CsiSystem> groups1 = [csiSystem1]
        List<CsiSystem> groups2 = [csiSystem2]
        List<CsiSystem> groupsOnly3 = [csiSystemWith3]

        when:
        List<CsiAggregation> mvs1 = serviceUnderTest.findAll(startDate.toDate(), startDate.toDate(), weeklyInterval, groups1)
        List<CsiAggregation> mvs2 = serviceUnderTest.findAll(startDate.toDate(), startDate.toDate(), weeklyInterval, groups2)
        List<CsiAggregation> mvsOnly3 = serviceUnderTest.findAll(startDate.toDate(), startDate.toDate(), weeklyInterval, groupsOnly3)

        then:
        mvs1.size() == 1
        mvs2.size() == 1
        mvsOnly3.size() == 1
    }

    /**
     * Tests calculation of daily-CsiSystem{@link CsiAggregation}s, which aren't calculated when new {@link EventResult}s get persisted.
     * In this test one single shop-{@link CsiAggregation}s exists, which should be the database of the calculation of the daily-csiSystem-{@link CsiAggregation}.
     */
    @Test
    void "don't calc daily-Mv if only findAll()"() {
        given:
        //test-specific data
        DateTime startedTime = new DateTime(2013, 5, 16, 12, 12, 11)
        List<WeightedCsiValue> weightedCsiValuesToReturnInMock = [
                new WeightedCsiValue(weightedValue: new WeightedValue(value: 12d, weight: 1d), underlyingEventResultIds: [1, 2, 3])]

        //mocking inner services
        mockWeightingService(weightedCsiValuesToReturnInMock)

        when:
        List<CsiAggregation> mvs = serviceUnderTest.findAll(startedTime.toDate(), startedTime.toDate(), dailyInterval)

        then:
        mvs.isEmpty()
    }

    @Test
    void "calc single daily-Mv"() {
        given:
        //test-specific data
        DateTime startedTime = new DateTime(2013, 5, 16, 12, 12, 11)
        List<WeightedCsiValue> weightedCsiValuesToReturnInMock = [
                new WeightedCsiValue(weightedValue: new WeightedValue(value: 12d, weight: 1d), underlyingEventResultIds: [1, 2, 3])]

        //mocking inner services
        mockWeightingService(weightedCsiValuesToReturnInMock)

        when:
        //test execution
        List<CsiAggregation> calculatedMvs = serviceUnderTest.getOrCalculateCsiSystemMeasuredValues(startedTime.toDate(), startedTime.toDate(), dailyInterval, [csiSystem1])
        List<CsiAggregation> mvs = serviceUnderTest.findAll(startedTime.toDate(), startedTime.toDate(), dailyInterval)

        then:
        calculatedMvs.size() == 1
        calculatedMvs[0].isCalculated()
        calculatedMvs[0].csByWptDocCompleteInPercent == 12d

        mvs.size() == 1
    }

    /**
     * Tests calculation of daily-csiSystem-{@link CsiAggregation}s, which aren't calculated when new {@link EventResult}s get persisted.
     * In this test shop-{@link CsiAggregation}s with different weights exist.
     */
    @Test
    void "calc multiple daily-Mv"() {
        given:
        //test-specific data
        double valueFirstMv = 12d
        double weightFirstMv = 1d
        double valueSecondMv = 10d
        double weightSecondMv = 2d
        double valueThirdMv = 13d
        double weightThirdMv = 3d
        double sumOfAllWeights = weightFirstMv + weightSecondMv + weightThirdMv
        DateTime startedTime = new DateTime(2013, 5, 16, 12, 12, 11)
        List<WeightedCsiValue> weightedCsiValuesToReturnInMock = [
                new WeightedCsiValue(weightedValue: new WeightedValue(value: valueFirstMv, weight: weightFirstMv), underlyingEventResultIds: [1, 2, 3]),
                new WeightedCsiValue(weightedValue: new WeightedValue(value: valueSecondMv, weight: weightSecondMv), underlyingEventResultIds: [4]),
                new WeightedCsiValue(weightedValue: new WeightedValue(value: valueThirdMv, weight: weightThirdMv), underlyingEventResultIds: [5, 6])]

        //mocking inner services
        mockWeightingService(weightedCsiValuesToReturnInMock)

        when:
        List<CsiAggregation> calculatedMvs = serviceUnderTest.getOrCalculateCsiSystemMeasuredValues(startedTime.toDate(), startedTime.toDate(), dailyInterval, [csiSystem2])
        List<CsiAggregation> mvs = serviceUnderTest.findAll(startedTime.toDate(), startedTime.toDate(), dailyInterval)

        then:
        calculatedMvs.size() == 1
        calculatedMvs[0].aggregator == csiSystemAggregator
        calculatedMvs[0].isCalculated()

        double expectedValue = ((valueFirstMv * weightFirstMv) + (valueSecondMv * weightSecondMv) + (valueThirdMv * weightThirdMv)) / sumOfAllWeights
        calculatedMvs[0].csByWptDocCompleteInPercent == expectedValue

        mvs.size() == 1
    }

    /**
     * Tests calculation of daily-csiSystem-{@link CsiAggregation}s, which aren't calculated when new {@link EventResult}s get persisted.
     * In this test no shop-{@link CsiAggregation}s exist, which are database of the calculation of daily-csiSystem-{@link CsiAggregation}s. So all calculated values should have state {@link Calculated#YesNoData}
     */
    @Test
    void "calc multiple daily-Mv, but no value calculated"() {
        given:
        //test-specific data
        DateTime startedTime = new DateTime(2013, 5, 16, 12, 12, 11)

        //mocking inner services
        mockWeightingService([])

        when:
        List<CsiAggregation> calculatedMvs = serviceUnderTest.getOrCalculateCsiSystemMeasuredValues(startedTime.toDate(), startedTime.toDate(), dailyInterval, [csiSystem2])
        List<CsiAggregation> mvs = serviceUnderTest.findAll(startedTime.toDate(), startedTime.toDate(), dailyInterval)

        then:
        calculatedMvs.size() == 1
        calculatedMvs[0].aggregator == csiSystemAggregator
        calculatedMvs[0].isCalculated()
        calculatedMvs[0].csByWptDocCompleteInPercent == null
        calculatedMvs[0].resultIdsAsList.isEmpty()

        mvs.size() == 1
    }

    // mocks

    /**
     * Mocks methods of {@link WeightingService}.
     */
    private void mockWeightingService(List<WeightedCsiValue> toReturnFromGetWeightedCsiValues) {
        def weightingService = mockFor(WeightingService, true)
        weightingService.demand.getWeightedCsiValues(1..10000) {
            List<CsiValue> csiValues, CsiSystem csiSystem ->
                return toReturnFromGetWeightedCsiValues
        }
        serviceUnderTest.weightingService = weightingService.createMock()
    }

    /**
     * Mocks methods of {@link MeasuredValueUpdateEventDaoService}.
     */
    private void mockMeasuredValueUpdateEventDaoService() {
        def measuredValueUpdateEventDaoService = mockFor(MeasuredValueUpdateEventDaoService, true)
        measuredValueUpdateEventDaoService.demand.createUpdateEvent(1..10000) {
            Long measuredValueId, MeasuredValueUpdateEvent.UpdateCause cause ->

                new MeasuredValueUpdateEvent(
                        dateOfUpdate: new Date(),
                        measuredValueId: measuredValueId,
                        updateCause: cause
                ).save(failOnError: true)

        }
        serviceUnderTest.measuredValueUpdateEventDaoService = measuredValueUpdateEventDaoService.createMock()
    }

    private mocksCommonForAllTests(CsiSystemMeasuredValueService serviceUnderTest) {
        serviceUnderTest.measuredValueUtilService = new MeasuredValueUtilService();
        serviceUnderTest.measuredValueDaoService = new MeasuredValueDaoService()

        Map<String, JobGroup> idAsStringToJobGroupMap = ['1': jobGroup1, '2': jobGroup2, '3': jobGroup3]
        SERVICE_MOCKER.mockMeasuredValueTagService(serviceUnderTest, idAsStringToJobGroupMap, [:], [:], [:], [:])

        SERVICE_MOCKER.mockShopMeasuredValueService(serviceUnderTest, [new CsiAggregation()])

        SERVICE_MOCKER.mockPerformanceLoggingService(serviceUnderTest)

        mockMeasuredValueUpdateEventDaoService()

    }

    private void createTestDataCommonForAllTests() {
        weeklyInterval = new MeasuredValueInterval(name: 'weekly', intervalInMinutes: MeasuredValueInterval.WEEKLY).save(failOnError: true)
        dailyInterval = new MeasuredValueInterval(name: 'daily', intervalInMinutes: MeasuredValueInterval.DAILY).save(failOnError: true)
        hourlyInterval = new MeasuredValueInterval(name: 'hourly', intervalInMinutes: MeasuredValueInterval.HOURLY).save(failOnError: true)

        shop = new AggregatorType(name: AggregatorType.SHOP).save(validate: false)
        csiSystemAggregator = new AggregatorType(name: AggregatorType.CSI_SYSTEM).save(validate: false)
        new AggregatorType(name: AggregatorType.MEASURED_EVENT, measurandGroup: MeasurandGroup.NO_MEASURAND).save(failOnError: true)

        jobGroup1 = new JobGroup(name: jobGroupName1, groupType: JobGroupType.CSI_AGGREGATION).save(validate: false)
        jobGroup2 = new JobGroup(name: jobGroupName2, groupType: JobGroupType.CSI_AGGREGATION).save(validate: false)
        jobGroup3 = new JobGroup(name: jobGroupName3, groupType: JobGroupType.CSI_AGGREGATION).save(validate: false)

        browser = new Browser(name: "Test", weight: 1).save(failOnError: true);

        csiSystem1 = new CsiSystem(jobGroupWeights: [
                new JobGroupWeight(jobGroup: jobGroup1, weight: 1.0),
                new JobGroupWeight(jobGroup: jobGroup2, weight: 1.0),
                new JobGroupWeight(jobGroup: jobGroup3, weight: 1.0)
        ])
        csiSystem2 = new CsiSystem(jobGroupWeights: [
                new JobGroupWeight(jobGroup: jobGroup1, weight: 1.0),
                new JobGroupWeight(jobGroup: jobGroup2, weight: 2.0),
                new JobGroupWeight(jobGroup: jobGroup3, weight: 3.0)
        ])
        csiSystemWith3 = new CsiSystem(jobGroupWeights: [
                new JobGroupWeight(jobGroup: jobGroup3, weight: 3.0)
        ])

        //with existing JobGroup:
        new CsiAggregation(interval: weeklyInterval, aggregator: csiSystemAggregator, tag: '1', csiSystem: csiSystem1, started: startDate.toDate()).save(validate: false)
        new CsiAggregation(interval: weeklyInterval, aggregator: csiSystemAggregator, tag: '2', csiSystem: csiSystem2, started: startDate.toDate()).save(validate: false)
        new CsiAggregation(interval: weeklyInterval, aggregator: csiSystemAggregator, tag: '3', csiSystem: csiSystemWith3, started: startDate.toDate()).save(validate: false)
        //not with existing CsiSystem:
        new CsiAggregation(interval: weeklyInterval, aggregator: csiSystemAggregator, tag: '4', csiSystem: null, started: startDate.toDate()).save(validate: false)
    }
}

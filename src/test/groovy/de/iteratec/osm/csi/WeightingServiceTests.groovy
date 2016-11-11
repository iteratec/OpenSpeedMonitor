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
import de.iteratec.osm.measurement.schedule.ConnectivityProfile
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.report.chart.CsiAggregation
import de.iteratec.osm.report.chart.CsiAggregationUpdateEvent
import de.iteratec.osm.result.CsiValueService
import de.iteratec.osm.result.EventResult
import de.iteratec.osm.util.PerformanceLoggingService
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import groovy.mock.interceptor.MockFor
import org.joda.time.DateTime
import org.junit.Before

import static org.junit.Assert.assertEquals

@TestFor(WeightingService)
@Mock([EventResult, CsiAggregation, CsiAggregationUpdateEvent, BrowserConnectivityWeight, Browser, ConnectivityProfile,
        JobGroup, CsiDay, CsiConfiguration, CsiSystem])
class WeightingServiceTests {

    WeightingService serviceUnderTest
    private static final double DELTA = 1e-15
    private static final DateTime SHOULD_BE_MAPPED_TO_TWO_A_CLOCK_AM = new DateTime(2014, 1, 1, 2, 30, 12)
    private static final DateTime SHOULD_BE_MAPPED_TO_FIVE_A_CLOCK_PM = new DateTime(2014, 1, 1, 17, 43, 56)
    private JobGroup jobGroup1, jobGroup2
    private Page page_50
    private Page page_70
    private Browser browserToReturn_50
    private Browser browserToReturn_70
    private BrowserConnectivityWeight browserConnectivityWeight_50
    private BrowserConnectivityWeight browserConnectivityWeight_70
    private ConnectivityProfile connectivityProfile_50
    private ConnectivityProfile connectivityProfile_70
    private CsiConfiguration csiConfiguration
    private CsiSystem csiSystem

    def doWithSpring = {
        performanceLoggingService(PerformanceLoggingService)
        csiValueService(CsiValueService)
    }
    @Before
    void setUp() {
        serviceUnderTest = service
        serviceUnderTest.csiValueService = grailsApplication.mainContext.getBean('csiValueService')
        mocksCommonToAllTests()
        createTestDataCommonToAllTests()
    }

    void tearDown() {
    }

    void testGetWeightWithoutWeightFactors() {

        // test specific data
        Set<WeightFactor> weightFactors = [] as Set
        CsiValue eventResult = new EventResult()
        CsiValue csiAggregation = new CsiAggregation()

        //test for EventResult
        Double deliveredWeight = serviceUnderTest.getWeight(eventResult, weightFactors, csiConfiguration)
        assertEquals(1d, deliveredWeight, DELTA)
        //test for CsiAggregation
        deliveredWeight = serviceUnderTest.getWeight(csiAggregation, weightFactors, csiConfiguration)
        assertEquals(1d, deliveredWeight, DELTA)

    }

    void testGetWeightWithHourofdayAsWeightFactor() {
        // test specific data
        Set<WeightFactor> weightFactors = [WeightFactor.HOUROFDAY] as Set
        CsiValue eventResultTwoAClockAm = new EventResult(jobResultDate: SHOULD_BE_MAPPED_TO_TWO_A_CLOCK_AM.toDate(), jobGroup: jobGroup1)
        CsiValue eventResultFiveAClockPm = new EventResult(jobResultDate: SHOULD_BE_MAPPED_TO_FIVE_A_CLOCK_PM.toDate(), jobGroup: jobGroup2)
        CsiValue csiAggregationTwoAClockAm = new CsiAggregation(started: SHOULD_BE_MAPPED_TO_TWO_A_CLOCK_AM.toDate(), jobGroup: jobGroup1)
        CsiValue csiAggregationFiveAClockPm = new CsiAggregation(started: SHOULD_BE_MAPPED_TO_FIVE_A_CLOCK_PM.toDate(), jobGroup: jobGroup2)

        //test for EventResults
        Double deliveredWeight = serviceUnderTest.getWeight(eventResultTwoAClockAm, weightFactors, csiConfiguration)
        double expectedWeight = 0.2d
        assertEquals(expectedWeight, deliveredWeight, DELTA)
        deliveredWeight = serviceUnderTest.getWeight(eventResultFiveAClockPm, weightFactors, csiConfiguration)
        expectedWeight = 7.3d
        assertEquals(expectedWeight, deliveredWeight, DELTA)

        //test for CsiAggregations
        deliveredWeight = serviceUnderTest.getWeight(csiAggregationTwoAClockAm, weightFactors, csiConfiguration)
        expectedWeight = 0.2d
        assertEquals(expectedWeight, deliveredWeight, DELTA)
        deliveredWeight = serviceUnderTest.getWeight(csiAggregationFiveAClockPm, weightFactors, csiConfiguration)
        expectedWeight = 7.3d
        assertEquals(expectedWeight, deliveredWeight, DELTA)
    }

    void testGetWeightWithBrowserConnectivityAsWeightFactor() {
        // test specific data
        Set<WeightFactor> weightFactors = [WeightFactor.BROWSER_CONNECTIVITY_COMBINATION] as Set

        ConnectivityProfile.metaClass.toString = { -> return delegate.name }

        CsiValue eventResultBrowserWeightOfFiftyPercent = new EventResult(browser: browserToReturn_50, connectivityProfile: connectivityProfile_50)
        CsiValue eventResultBrowserWeightOfSeventyPercent = new EventResult(browser: browserToReturn_70, connectivityProfile: connectivityProfile_70)
        CsiValue csiAggregationBrowserWeightOfFiftyPercent = new CsiAggregation(browser: browserToReturn_50, connectivityProfile: connectivityProfile_50)
        CsiValue csiAggregationBrowserWeightOfSeventyPercent = new CsiAggregation(browser: browserToReturn_70, connectivityProfile: connectivityProfile_70)


        //test for EventResults
        Double deliveredWeight = serviceUnderTest.getWeight(
                eventResultBrowserWeightOfFiftyPercent, weightFactors, csiConfiguration
        )
        //assertions
        double expectedWeight = 0.5d
        assertEquals(expectedWeight, deliveredWeight, DELTA)
        deliveredWeight = serviceUnderTest.getWeight(eventResultBrowserWeightOfSeventyPercent, weightFactors, csiConfiguration)
        expectedWeight = 0.7d
        assertEquals(expectedWeight, deliveredWeight, DELTA)

        //test for CsiAggregations
        deliveredWeight = serviceUnderTest.getWeight(
                csiAggregationBrowserWeightOfFiftyPercent, weightFactors, csiConfiguration
        )
        //assertions
        expectedWeight = 0.5d
        assertEquals(expectedWeight, deliveredWeight, DELTA)
        deliveredWeight = serviceUnderTest.getWeight(csiAggregationBrowserWeightOfSeventyPercent, weightFactors, csiConfiguration)
        expectedWeight = 0.7d
        assertEquals(expectedWeight, deliveredWeight, DELTA)
    }

    void testGetWeightWithPageAsWeightFactor() {
        // test specific data
        Set<WeightFactor> weightFactors = [WeightFactor.PAGE] as Set
        CsiValue eventResultBrowserWeightOfFiftyPercent = new EventResult(page: page_50)
        CsiValue eventResultBrowserWeightOfSeventyPercent = new EventResult(page: page_70)
        CsiValue csiAggregationBrowserWeightOfFiftyPercent = new CsiAggregation(page: page_50)
        CsiValue csiAggregationBrowserWeightOfSeventyPercent = new CsiAggregation(page: page_70)

        //test for EventResults
        Double deliveredWeight = serviceUnderTest.getWeight(eventResultBrowserWeightOfFiftyPercent, weightFactors, csiConfiguration)
        double expectedWeight = 0.5d
        assertEquals(expectedWeight, deliveredWeight, DELTA)
        deliveredWeight = serviceUnderTest.getWeight(eventResultBrowserWeightOfSeventyPercent, weightFactors, csiConfiguration)
        expectedWeight = 0.7d
        assertEquals(expectedWeight, deliveredWeight, DELTA)

        //test for CsiAggregations
        deliveredWeight = serviceUnderTest.getWeight(csiAggregationBrowserWeightOfFiftyPercent, weightFactors, csiConfiguration)
        expectedWeight = 0.5d
        assertEquals(expectedWeight, deliveredWeight, DELTA)
        deliveredWeight = serviceUnderTest.getWeight(csiAggregationBrowserWeightOfSeventyPercent, weightFactors, csiConfiguration)
        expectedWeight = 0.7d
        assertEquals(expectedWeight, deliveredWeight, DELTA)
    }

    void testGetWeightWithMultipleWeightFactorsForEventResults() {
        // test specific data
        TestDataUtil.createConnectivityProfile("conn")
        CsiValue eventResultWeightFiftyTwoAm =
                new EventResult(
                        browser: browserToReturn_50,
                        page: page_50,
                        jobResultDate: SHOULD_BE_MAPPED_TO_TWO_A_CLOCK_AM.toDate(),
                        connectivityProfile: connectivityProfile_50,
                        jobGroup:  jobGroup1
                )
        CsiValue eventResultWeightSeventyFivePm =
                new EventResult(
                        browser: browserToReturn_70,
                        page: page_70,
                        jobResultDate: SHOULD_BE_MAPPED_TO_FIVE_A_CLOCK_PM.toDate(),
                        connectivityProfile: connectivityProfile_70,
                        jobGroup: jobGroup2
                )


        //test with all three WeightFactors
        Double deliveredWeight = serviceUnderTest.getWeight(
                eventResultWeightFiftyTwoAm,
                [WeightFactor.HOUROFDAY, WeightFactor.PAGE, WeightFactor.BROWSER_CONNECTIVITY_COMBINATION] as Set,
                csiConfiguration
        )
        double expectedHourofdayWeight = 0.2d
        double expectedBrowserConnectivityWeight = 0.5d
        double expectedPageWeight = 0.5d
        double expectedWeight = expectedHourofdayWeight * expectedBrowserConnectivityWeight * expectedPageWeight
        assertEquals(expectedWeight, deliveredWeight, DELTA)

        deliveredWeight = serviceUnderTest.getWeight(
                eventResultWeightSeventyFivePm,
                [WeightFactor.HOUROFDAY, WeightFactor.PAGE, WeightFactor.BROWSER_CONNECTIVITY_COMBINATION] as Set,
                csiConfiguration
        )
        expectedHourofdayWeight = 7.3d
        expectedBrowserConnectivityWeight = 0.7d
        expectedPageWeight = 0.7d
        expectedWeight = expectedHourofdayWeight * expectedBrowserConnectivityWeight * expectedPageWeight
        assertEquals(expectedWeight, deliveredWeight, DELTA)

        //test with two weightFactors
        deliveredWeight = serviceUnderTest.getWeight(
                eventResultWeightSeventyFivePm,
                [WeightFactor.PAGE, WeightFactor.BROWSER_CONNECTIVITY_COMBINATION] as Set,
                csiConfiguration
        )
        expectedBrowserConnectivityWeight = 0.7d
        expectedPageWeight = 0.7d
        expectedWeight = expectedBrowserConnectivityWeight * expectedPageWeight
        assertEquals(expectedWeight, deliveredWeight, DELTA)

        deliveredWeight = serviceUnderTest.getWeight(
                eventResultWeightSeventyFivePm,
                [WeightFactor.HOUROFDAY, WeightFactor.BROWSER_CONNECTIVITY_COMBINATION] as Set,
                csiConfiguration
        )
        expectedHourofdayWeight = 7.3d
        expectedBrowserConnectivityWeight = 0.7d
        expectedWeight = expectedHourofdayWeight * expectedBrowserConnectivityWeight
        assertEquals(expectedWeight, deliveredWeight, DELTA)

    }

    void testGetWeightedCsiValuesFromEventResults() {
        // test specific data
        CsiValue eventResultWeightFiftyTwoAm = new EventResult(
                id: 1l,
                jobGroup: jobGroup1,
                csByWptDocCompleteInPercent: 10d,
                browser: browserToReturn_50,
                page: page_50,
                jobResultDate: SHOULD_BE_MAPPED_TO_TWO_A_CLOCK_AM.toDate(),
                docCompleteTimeInMillisecs: 1000,
                connectivityProfile: connectivityProfile_50).save(validate: false)
        CsiValue eventResultWeightSeventyFivePm = new EventResult(
                id: 2l,
                jobGroup: jobGroup2,
                csByWptDocCompleteInPercent: 20d,
                page: page_70,
                browser: browserToReturn_70,
                jobResultDate: SHOULD_BE_MAPPED_TO_FIVE_A_CLOCK_PM.toDate(),
                docCompleteTimeInMillisecs: 1000,
                connectivityProfile: connectivityProfile_70).save(validate: false)

        //tests with all three WeightFactors

        List<WeightedCsiValue> weightedValues = serviceUnderTest.getWeightedCsiValues(
                [eventResultWeightFiftyTwoAm, eventResultWeightSeventyFivePm],
                [WeightFactor.HOUROFDAY, WeightFactor.PAGE, WeightFactor.BROWSER_CONNECTIVITY_COMBINATION] as Set, csiConfiguration)

        assertEquals(2, weightedValues.size())

        double expectedHourofdayWeight = 0.2d
        double expectedBrowserConnectivityWeight = 0.5d
        double expectedPageWeight = 0.5d
        double expectedWeight = expectedHourofdayWeight * expectedBrowserConnectivityWeight * expectedPageWeight
        assertEquals(1, weightedValues.findAll {
            it.underlyingEventResultIds.equals([1]) && it.weightedValue.getValue().equals(10d) && it.weightedValue.getWeight().equals(expectedWeight)
        }.size())

        expectedHourofdayWeight = 7.3d
        expectedBrowserConnectivityWeight = 0.7d
        expectedPageWeight = 0.7d
        expectedWeight = expectedHourofdayWeight * expectedBrowserConnectivityWeight * expectedPageWeight
        assertEquals(1, weightedValues.findAll {
            it.underlyingEventResultIds.equals([2]) && it.weightedValue.getValue().equals(20d) && it.weightedValue.getWeight().equals(expectedWeight)
        }.size())

        //tests with hourofday and page as WeightFactors

        weightedValues = serviceUnderTest.getWeightedCsiValues(
                [eventResultWeightFiftyTwoAm, eventResultWeightSeventyFivePm],
                [WeightFactor.HOUROFDAY, WeightFactor.PAGE] as Set, csiConfiguration)

        assertEquals(2, weightedValues.size())

        expectedHourofdayWeight = 0.2d
        expectedPageWeight = 0.5d
        expectedWeight = expectedHourofdayWeight * expectedPageWeight
        assertEquals(1, weightedValues.findAll {
            it.underlyingEventResultIds.equals([1]) && it.weightedValue.getValue().equals(10d) && it.weightedValue.getWeight().equals(expectedWeight)
        }.size())

        expectedHourofdayWeight = 7.3d
        expectedPageWeight = 0.7d
        expectedWeight = expectedHourofdayWeight * expectedPageWeight
        assertEquals(1, weightedValues.findAll {
            it.underlyingEventResultIds.equals([2]) && it.weightedValue.getValue().equals(20d) && it.weightedValue.getWeight().equals(expectedWeight)
        }.size())

        //tests with browserConnectivityWeight and page as WeightFactors

        weightedValues = serviceUnderTest.getWeightedCsiValues(
                [eventResultWeightFiftyTwoAm, eventResultWeightSeventyFivePm],
                [WeightFactor.BROWSER_CONNECTIVITY_COMBINATION, WeightFactor.PAGE] as Set, csiConfiguration)

        assertEquals(2, weightedValues.size())

        expectedBrowserConnectivityWeight = 0.5d
        expectedPageWeight = 0.5d
        expectedWeight = expectedBrowserConnectivityWeight * expectedPageWeight
        assertEquals(1, weightedValues.findAll {
            it.underlyingEventResultIds.equals([1]) && it.weightedValue.getValue().equals(10d) && it.weightedValue.getWeight().equals(expectedWeight)
        }.size())

        expectedBrowserConnectivityWeight = 0.7d
        expectedPageWeight = 0.7d
        expectedWeight = expectedBrowserConnectivityWeight * expectedPageWeight
        assertEquals(1, weightedValues.findAll {
            it.underlyingEventResultIds.equals([2]) && it.weightedValue.getValue().equals(20d) && it.weightedValue.getWeight().equals(expectedWeight)
        }.size())

        //tests with just browserConnectivity as WeightFactor

        weightedValues = serviceUnderTest.getWeightedCsiValues(
                [eventResultWeightFiftyTwoAm, eventResultWeightSeventyFivePm],
                [WeightFactor.BROWSER_CONNECTIVITY_COMBINATION] as Set, csiConfiguration)

        assertEquals(2, weightedValues.size())

        expectedBrowserConnectivityWeight = 0.5d
        assertEquals(1, weightedValues.findAll {
            it.underlyingEventResultIds.equals([1]) && it.weightedValue.getValue().equals(10d) && it.weightedValue.getWeight().equals(expectedBrowserConnectivityWeight)
        }.size())

        expectedBrowserConnectivityWeight = 0.7d
        assertEquals(1, weightedValues.findAll {
            it.underlyingEventResultIds.equals([2]) && it.weightedValue.getValue().equals(20d) && it.weightedValue.getWeight().equals(expectedBrowserConnectivityWeight)
        }.size())

        //tests without WeightFactors

        weightedValues = serviceUnderTest.getWeightedCsiValues(
                [eventResultWeightFiftyTwoAm, eventResultWeightSeventyFivePm],
                [] as Set, csiConfiguration)

        assertEquals(1, weightedValues.size())

        WeightedCsiValue flattenedCauseOfMissingWeightFactors = weightedValues[0]
        expectedWeight = 1d
        List<Long> expectedUnderlyingResultIds = [1l, 2l]
        Double expectedValue = (10d + 20d) / 2
        assertEquals(expectedUnderlyingResultIds, flattenedCauseOfMissingWeightFactors.underlyingEventResultIds)
        assertEquals(expectedValue, flattenedCauseOfMissingWeightFactors.weightedValue.value, DELTA)
        assertEquals(expectedWeight, flattenedCauseOfMissingWeightFactors.weightedValue.weight, DELTA)

    }

    void testGetWeightedCsiValuesFromCsiAggregations() {
        CsiAggregation csiAggregationWeightFiftyTwoAm = new CsiAggregation(
                csByWptDocCompleteInPercent: 10d,
                browser: browserToReturn_50,
                page: page_50,
                started: SHOULD_BE_MAPPED_TO_TWO_A_CLOCK_AM.toDate(),
                connectivityProfile: connectivityProfile_50,
                jobGroup: jobGroup1
        )
        csiAggregationWeightFiftyTwoAm.addAllToUnderlyingEventResultsByWptDocComplete([1l, 2l, 3l])
        csiAggregationWeightFiftyTwoAm.save(validate: false)
        TestDataUtil.createUpdateEvent(csiAggregationWeightFiftyTwoAm.ident(), CsiAggregationUpdateEvent.UpdateCause.CALCULATED)
        CsiAggregation csiAggregationWeightSeventyFivePm = new CsiAggregation(
                csByWptDocCompleteInPercent: 20d,
                browser: browserToReturn_70,
                page: page_70,
                started: SHOULD_BE_MAPPED_TO_FIVE_A_CLOCK_PM.toDate(),
                connectivityProfile: connectivityProfile_70,
                jobGroup: jobGroup1
        )
        csiAggregationWeightSeventyFivePm.addAllToUnderlyingEventResultsByWptDocComplete([4l, 5l, 6l])
        csiAggregationWeightSeventyFivePm.save(validate: false)
        TestDataUtil.createUpdateEvent(csiAggregationWeightSeventyFivePm.ident(), CsiAggregationUpdateEvent.UpdateCause.CALCULATED)

        //tests with all three WeightFactors
        List<WeightedCsiValue> weightedValues = serviceUnderTest.getWeightedCsiValues(
                [csiAggregationWeightFiftyTwoAm, csiAggregationWeightSeventyFivePm],
                [WeightFactor.HOUROFDAY, WeightFactor.PAGE, WeightFactor.BROWSER_CONNECTIVITY_COMBINATION] as Set, csiConfiguration)

        assertEquals(2, weightedValues.size())

        double expectedHourofdayWeight = 0.2d
        double expectedBrowserConnectivityWeight = 0.5d
        double expectedPageWeight = 0.5d
        double expectedWeight = expectedHourofdayWeight * expectedBrowserConnectivityWeight * expectedPageWeight
        assertEquals(1, weightedValues.findAll {
            it.underlyingEventResultIds.equals([1l, 2l, 3l]) && it.weightedValue.getValue().equals(10d) && it.weightedValue.getWeight().equals(expectedWeight)
        }.size())

        expectedHourofdayWeight = 7.3d
        expectedBrowserConnectivityWeight = 0.7d
        expectedPageWeight = 0.7d
        expectedWeight = expectedHourofdayWeight * expectedBrowserConnectivityWeight * expectedPageWeight
        assertEquals(1, weightedValues.findAll {
            it.underlyingEventResultIds.equals([4l, 5l, 6l]) && it.weightedValue.getValue().equals(20d) && it.weightedValue.getWeight().equals(expectedWeight)
        }.size())

        //tests with hourofday and page as WeightFactors

        weightedValues = serviceUnderTest.getWeightedCsiValues(
                [csiAggregationWeightFiftyTwoAm, csiAggregationWeightSeventyFivePm],
                [WeightFactor.HOUROFDAY, WeightFactor.PAGE] as Set, csiConfiguration)

        assertEquals(2, weightedValues.size())

        expectedHourofdayWeight = 0.2d
        expectedPageWeight = 0.5d
        expectedWeight = expectedHourofdayWeight * expectedPageWeight
        assertEquals(1, weightedValues.findAll {
            it.underlyingEventResultIds.equals([1l, 2l, 3l]) && it.weightedValue.getValue().equals(10d) && it.weightedValue.getWeight().equals(expectedWeight)
        }.size())

        expectedHourofdayWeight = 7.3d
        expectedPageWeight = 0.7d
        expectedWeight = expectedHourofdayWeight * expectedPageWeight
        assertEquals(1, weightedValues.findAll {
            it.underlyingEventResultIds.equals([4l, 5l, 6l]) && it.weightedValue.getValue().equals(20d) && it.weightedValue.getWeight().equals(expectedWeight)
        }.size())

        //tests with browserConnectivity and page as WeightFactors

        weightedValues = serviceUnderTest.getWeightedCsiValues(
                [csiAggregationWeightFiftyTwoAm, csiAggregationWeightSeventyFivePm],
                [WeightFactor.BROWSER_CONNECTIVITY_COMBINATION, WeightFactor.PAGE] as Set, csiConfiguration)

        assertEquals(2, weightedValues.size())

        expectedBrowserConnectivityWeight = 0.5d
        expectedPageWeight = 0.5d
        expectedWeight = expectedBrowserConnectivityWeight * expectedPageWeight
        assertEquals(1, weightedValues.findAll {
            it.underlyingEventResultIds.equals([1l, 2l, 3l]) && it.weightedValue.getValue().equals(10d) && it.weightedValue.getWeight().equals(expectedWeight)
        }.size())

        expectedBrowserConnectivityWeight = 0.7d
        expectedPageWeight = 0.7d
        expectedWeight = expectedBrowserConnectivityWeight * expectedPageWeight
        assertEquals(1, weightedValues.findAll {
            it.underlyingEventResultIds.equals([4l, 5l, 6l]) && it.weightedValue.getValue().equals(20d) && it.weightedValue.getWeight().equals(expectedWeight)
        }.size())

        //tests without WeightFactors

        weightedValues = serviceUnderTest.getWeightedCsiValues(
                [csiAggregationWeightFiftyTwoAm, csiAggregationWeightSeventyFivePm],
                [] as Set, csiConfiguration)

        assertEquals(1, weightedValues.size())

        WeightedCsiValue flattenedCauseOfMissingWeightFactors = weightedValues[0]
        expectedWeight = 1d
        List<Long> expectedUnderlyingResultIds = [1l, 2l, 3l, 4l, 5l, 6l]
        Double expectedValue = (10d + 20d) / 2
        assertEquals(expectedUnderlyingResultIds, flattenedCauseOfMissingWeightFactors.underlyingEventResultIds)
        assertEquals(expectedValue, flattenedCauseOfMissingWeightFactors.weightedValue.value, DELTA)
        assertEquals(expectedWeight, flattenedCauseOfMissingWeightFactors.weightedValue.weight, DELTA)

    }

    void testGetWeightedCsiValuesFromCsiAggregationsByCsiSystem() {
        CsiAggregation csiAggregation1 = new CsiAggregation(
                csByWptDocCompleteInPercent: 10d,
                jobGroup: jobGroup1,
                started: SHOULD_BE_MAPPED_TO_TWO_A_CLOCK_AM.toDate(),
                connectivityProfile: connectivityProfile_50,
        )
        csiAggregation1.addAllToUnderlyingEventResultsByWptDocComplete([1l, 2l, 3l])
        csiAggregation1.save(validate: false)
        TestDataUtil.createUpdateEvent(csiAggregation1.ident(), CsiAggregationUpdateEvent.UpdateCause.CALCULATED)
        CsiAggregation csiAggregation2 = new CsiAggregation(
                csByWptDocCompleteInPercent: 20d,
                jobGroup: jobGroup2,
                started: SHOULD_BE_MAPPED_TO_FIVE_A_CLOCK_PM.toDate(),
                connectivityProfile: connectivityProfile_70
        )
        csiAggregation2.addAllToUnderlyingEventResultsByWptDocComplete([4l, 5l, 6l])
        csiAggregation2.save(validate: false)
        TestDataUtil.createUpdateEvent(csiAggregation2.ident(), CsiAggregationUpdateEvent.UpdateCause.CALCULATED)

        //tests with all three WeightFactors
        List<WeightedCsiValue> weightedValues = serviceUnderTest.getWeightedCsiValues(
                [csiAggregation1, csiAggregation2], csiSystem)

        assertEquals(2, weightedValues.size())

        double expectedJobGroupWeight1 = 0.5d
        double expectedJobGroupWeight2 = 2.0d
        WeightedCsiValue weightedCsiValue1 = weightedValues.find {
            it.weightedValue.weight == expectedJobGroupWeight1
        }
        assert (weightedCsiValue1)
        assertEquals(expectedJobGroupWeight1, weightedCsiValue1.weightedValue.weight, DELTA)
        assertEquals(10d, weightedCsiValue1.weightedValue.value, DELTA)
        weightedValues.removeElement(weightedCsiValue1)

        WeightedCsiValue weightedCsiValue2 = weightedValues[0]
        assert (weightedCsiValue1)
        assertEquals(expectedJobGroupWeight2, weightedCsiValue2.weightedValue.weight, DELTA)
        assertEquals(20d, weightedCsiValue2.weightedValue.value, DELTA)
    }

    void testGetWeightedCsiValuesFromCsiAggregationsByVisuallyCompleteForCsiSystem() {
        // data for this test
        CsiValue csiAggregationWithVisuallyComplete1 = new CsiAggregation(
                csByWptDocCompleteInPercent: 10d,
                csByWptVisuallyCompleteInPercent: 10d,
                started: SHOULD_BE_MAPPED_TO_TWO_A_CLOCK_AM.toDate(),
                connectivityProfile: connectivityProfile_50,
                underlyingEventResultsByVisuallyComplete: "1L,2L",
                jobGroup: jobGroup1
        )
        CsiValue csiAggregationWithVisuallyComplete2 = new CsiAggregation(
                csByWptDocCompleteInPercent: 10d,
                csByWptVisuallyCompleteInPercent: 20d,
                started: SHOULD_BE_MAPPED_TO_TWO_A_CLOCK_AM.toDate(),
                connectivityProfile: connectivityProfile_50,
                underlyingEventResultsByVisuallyComplete: "1L,2L",
                jobGroup: jobGroup1
        )
        CsiValue csiAggregationWithVisuallyComplete3 = new CsiAggregation(
                csByWptDocCompleteInPercent: 10d,
                csByWptVisuallyCompleteInPercent: 20d,
                started: SHOULD_BE_MAPPED_TO_TWO_A_CLOCK_AM.toDate(),
                connectivityProfile: connectivityProfile_50,
                underlyingEventResultsByVisuallyComplete: "1L,2L",
                jobGroup: jobGroup2
        )
        CsiValue csiAggregationWithoutVisuallyComplete1 = new CsiAggregation(
                csByWptDocCompleteInPercent: 10d,
                started: SHOULD_BE_MAPPED_TO_TWO_A_CLOCK_AM.toDate(),
                connectivityProfile: connectivityProfile_50,
                underlyingEventResultsByVisuallyComplete: "1L,2L",
                jobGroup: jobGroup1
        )
        CsiValue csiAggregationWithoutVisuallyComplete2 = new CsiAggregation(
                csByWptDocCompleteInPercent: 10d,
                started: SHOULD_BE_MAPPED_TO_TWO_A_CLOCK_AM.toDate(),
                connectivityProfile: connectivityProfile_50,
                underlyingEventResultsByVisuallyComplete: "1L,2L",
                jobGroup: jobGroup2
        )

        // execution
        // test all underlying csiAggregations have csByWptVisuallyCompleteInPercent
        List<WeightedCsiValue> weightedCsiValuesAllHaveVisuallyComplete = serviceUnderTest.getWeightedCsiValuesByVisuallyComplete([csiAggregationWithVisuallyComplete1, csiAggregationWithVisuallyComplete2, csiAggregationWithVisuallyComplete3], csiSystem)

        // test some underlying csiAggregations have csByWptVisuallyCompleteInPercent
        List<WeightedCsiValue> weightedCsiValuesSomeHaveVisuallyComplete = serviceUnderTest.getWeightedCsiValuesByVisuallyComplete([csiAggregationWithVisuallyComplete1, csiAggregationWithoutVisuallyComplete1], csiSystem)

        // test no underlying csiAggregations have csByWptVisuallyCompleteInPercent
        List<WeightedCsiValue> weightedCsiValuesNoHaveVisuallyComplete = serviceUnderTest.getWeightedCsiValuesByVisuallyComplete([csiAggregationWithoutVisuallyComplete1, csiAggregationWithoutVisuallyComplete2], csiSystem)

        // expectations
        assertEquals(2, weightedCsiValuesAllHaveVisuallyComplete.size())
        assertEquals(1, weightedCsiValuesSomeHaveVisuallyComplete.size())
        assertEquals(0, weightedCsiValuesNoHaveVisuallyComplete.size())

        //weights are defined in csiSystem
        Double meanOfTwoEqualWeightedValues = (10.0 + 20.0) / 2
        assertEquals(new WeightedValue(value: meanOfTwoEqualWeightedValues, weight: 0.5), weightedCsiValuesAllHaveVisuallyComplete[0].weightedValue)
        Double valueOfDifferentWeightedValue = 20.0
        assertEquals(new WeightedValue(value: valueOfDifferentWeightedValue, weight: 2.0), weightedCsiValuesAllHaveVisuallyComplete[1].weightedValue)


        assertEquals(new WeightedValue(value: 10.0, weight: 0.5), weightedCsiValuesSomeHaveVisuallyComplete[0].weightedValue)
    }

    void testGetWeightedCsiValuesFromCsiAggregationsByVisuallyComplete() {
        // data for this test
        CsiValue csiAggregationWithVisuallyComplete1 = new CsiAggregation(
                csByWptDocCompleteInPercent: 10d,
                csByWptVisuallyCompleteInPercent: 10d,
                browser: browserToReturn_50,
                started: SHOULD_BE_MAPPED_TO_TWO_A_CLOCK_AM.toDate(),
                connectivityProfile: connectivityProfile_50,
                underlyingEventResultsByVisuallyComplete: "1L,2L",
                jobGroup: jobGroup1,
                page: page_50
        )
        CsiValue csiAggregationWithVisuallyComplete2 = new CsiAggregation(
                csByWptDocCompleteInPercent: 10d,
                csByWptVisuallyCompleteInPercent: 20d,
                browser: browserToReturn_50,
                started: SHOULD_BE_MAPPED_TO_TWO_A_CLOCK_AM.toDate(),
                connectivityProfile: connectivityProfile_50,
                underlyingEventResultsByVisuallyComplete: "1L,2L",
                jobGroup: jobGroup1,
                page: page_50
        )
        CsiValue csiAggregationWithVisuallyComplete3 = new CsiAggregation(
                csByWptDocCompleteInPercent: 10d,
                csByWptVisuallyCompleteInPercent: 20d,
                browser: browserToReturn_70,
                started: SHOULD_BE_MAPPED_TO_TWO_A_CLOCK_AM.toDate(),
                connectivityProfile: connectivityProfile_70,
                underlyingEventResultsByVisuallyComplete: "1L,2L",
                jobGroup: jobGroup1,
                page: page_70
        )
        CsiValue csiAggregationWithoutVisuallyComplete1 = new CsiAggregation(
                csByWptDocCompleteInPercent: 10d,
                browser: browserToReturn_50,
                started: SHOULD_BE_MAPPED_TO_TWO_A_CLOCK_AM.toDate(),
                connectivityProfile: connectivityProfile_50,
                underlyingEventResultsByVisuallyComplete: "1L,2L",
                jobGroup: jobGroup1,
                page: page_50
        )
        CsiValue csiAggregationWithoutVisuallyComplete2 = new CsiAggregation(
                csByWptDocCompleteInPercent: 10d,
                browser: browserToReturn_70,
                started: SHOULD_BE_MAPPED_TO_TWO_A_CLOCK_AM.toDate(),
                connectivityProfile: connectivityProfile_70,
                underlyingEventResultsByVisuallyComplete: "1L,2L",
                jobGroup: jobGroup1,
                page: page_70
        )


        // execution
        // test all underlying csiAggregations have csByWptVisuallyCompleteInPercent
        List<WeightedCsiValue> weightedCsiValuesAllHaveVisuallyComplete = serviceUnderTest.getWeightedCsiValuesByVisuallyComplete([csiAggregationWithVisuallyComplete1, csiAggregationWithVisuallyComplete2, csiAggregationWithVisuallyComplete3], [WeightFactor.PAGE] as Set, csiConfiguration)

        // test some underlying csiAggregations have csByWptVisuallyCompleteInPercent
        List<WeightedCsiValue> weightedCsiValuesSomeHaveVisuallyComplete = serviceUnderTest.getWeightedCsiValuesByVisuallyComplete([csiAggregationWithVisuallyComplete1, csiAggregationWithoutVisuallyComplete1], [WeightFactor.PAGE] as Set, csiConfiguration)

        // test no underlying csiAggregations have csByWptVisuallyCompleteInPercent
        List<WeightedCsiValue> weightedCsiValuesNoHaveVisuallyComplete = serviceUnderTest.getWeightedCsiValuesByVisuallyComplete([csiAggregationWithoutVisuallyComplete1, csiAggregationWithoutVisuallyComplete2], [WeightFactor.PAGE] as Set, csiConfiguration)

        // expectations
        assertEquals(2, weightedCsiValuesAllHaveVisuallyComplete.size())
        assertEquals(1, weightedCsiValuesSomeHaveVisuallyComplete.size())
        assertEquals(0, weightedCsiValuesNoHaveVisuallyComplete.size())

        // weights are defined in csiConfiguration
        // (value 10 + value 20) / 2 --> both are weighted 0.5
        assertEquals(new WeightedValue(value: 15.0, weight: 0.5), weightedCsiValuesAllHaveVisuallyComplete[0].weightedValue)
        assertEquals(new WeightedValue(value: 20.0, weight: 0.7), weightedCsiValuesAllHaveVisuallyComplete[1].weightedValue)

        assertEquals(new WeightedValue(value: 10.0, weight: 0.5), weightedCsiValuesSomeHaveVisuallyComplete[0].weightedValue)
    }

    void testFlattenWeightedCsiValuesWithoutData() {
        //test data
        List<WeightedCsiValue> valuesToFlatten = []
        //test-execution
        List<WeightedCsiValue> flattened = serviceUnderTest.flattenWeightedCsiValues(valuesToFlatten)
        //assertions
        assertEquals(0, flattened.size())
    }

    void testFlattenWeightedCsiValuesCalledWithData() {
        //test data
        Double firstWeight = 0.1d
        Double secondWeight = 0.2d
        Double thirdWeight = 0.3d

        List<WeightedCsiValue> valuesToFlatten = []

        valuesToFlatten.add(new WeightedCsiValue(underlyingEventResultIds: [1, 2], weightedValue: new WeightedValue(value: 1, weight: firstWeight)))
        valuesToFlatten.add(new WeightedCsiValue(underlyingEventResultIds: [3], weightedValue: new WeightedValue(value: 2, weight: firstWeight)))
        valuesToFlatten.add(new WeightedCsiValue(underlyingEventResultIds: [4, 5], weightedValue: new WeightedValue(value: 3, weight: firstWeight)))

        valuesToFlatten.add(new WeightedCsiValue(underlyingEventResultIds: [10, 11], weightedValue: new WeightedValue(value: 10, weight: secondWeight)))
        valuesToFlatten.add(new WeightedCsiValue(underlyingEventResultIds: [12], weightedValue: new WeightedValue(value: 11, weight: secondWeight)))
        valuesToFlatten.add(new WeightedCsiValue(underlyingEventResultIds: [13], weightedValue: new WeightedValue(value: 12, weight: secondWeight)))
        valuesToFlatten.add(new WeightedCsiValue(underlyingEventResultIds: [14, 15, 16], weightedValue: new WeightedValue(value: 13, weight: secondWeight)))
        valuesToFlatten.add(new WeightedCsiValue(underlyingEventResultIds: [17], weightedValue: new WeightedValue(value: 14, weight: secondWeight)))

        valuesToFlatten.add(new WeightedCsiValue(underlyingEventResultIds: [100], weightedValue: new WeightedValue(value: 100, weight: thirdWeight)))
        valuesToFlatten.add(new WeightedCsiValue(underlyingEventResultIds: [101, 102, 103], weightedValue: new WeightedValue(value: 101, weight: thirdWeight)))

        //test-execution
        List<WeightedCsiValue> flattened = serviceUnderTest.flattenWeightedCsiValues(valuesToFlatten)

        //assertions
        assertEquals(3, flattened.size())

        List<WeightedCsiValue> ofFirstWeight = flattened.findAll {
            Math.abs(it.weightedValue.weight - firstWeight) < DELTA
        }
        assertEquals(1, ofFirstWeight.size())
        assertEquals([1, 2, 3, 4, 5], ofFirstWeight[0].underlyingEventResultIds)
        assertEquals((1 + 2 + 3) / 3, ofFirstWeight[0].weightedValue.value, DELTA)

        List<WeightedCsiValue> ofSecondWeight = flattened.findAll {
            Math.abs(it.weightedValue.weight - secondWeight) < DELTA
        }
        assertEquals(1, ofSecondWeight.size())
        assertEquals([10, 11, 12, 13, 14, 15, 16, 17], ofSecondWeight[0].underlyingEventResultIds)
        assertEquals((10 + 11 + 12 + 13 + 14) / 5, ofSecondWeight[0].weightedValue.value, DELTA)

        List<WeightedCsiValue> ofThirdWeight = flattened.findAll {
            Math.abs(it.weightedValue.weight - thirdWeight) < DELTA
        }
        assertEquals(1, ofThirdWeight.size())
        assertEquals([100, 101, 102, 103], ofThirdWeight[0].underlyingEventResultIds)
        assertEquals((100 + 101) / 2, ofThirdWeight[0].weightedValue.value, DELTA)
    }

    private void mocksCommonToAllTests() {
        mockCustomerSatisfactionWeightService()
        serviceUnderTest.performanceLoggingService = grailsApplication.mainContext.getBean('performanceLoggingService')
        // into the domain EventResult injected service csiConfigCacheService would be null so we have to use metaclass to implement isCsiRelevant()-method for tests
        serviceUnderTest.csiValueService.metaClass.isCsiRelevant = { CsiValue csiValue ->
            return true
        }
        serviceUnderTest.csiValueService.metaClass.isCsiRelevant = { CsiAggregation csiValue ->
            return true
        }
        serviceUnderTest.csiValueService.metaClass.isCsiRelevant = { EventResult csiValue ->
            return true
        }
    }

    private createTestDataCommonToAllTests() {
        page_50 = new Page(name: 'page_50')
        page_70 = new Page(name: 'page_70')
        browserToReturn_50 = new Browser(name: 'browser_50')
        browserToReturn_70 = new Browser(name: 'browser_70')
        CsiDay day = new CsiDay()
        day.with {
            hour0Weight = 2.9d
            hour1Weight = 0.4d
            hour2Weight = 0.2d
            hour3Weight = 0.1d
            hour4Weight = 0.1d
            hour5Weight = 0.2d
            hour6Weight = 0.7d
            hour7Weight = 1.7d
            hour8Weight = 3.2d
            hour9Weight = 4.8d
            hour10Weight = 5.6d
            hour11Weight = 5.7d
            hour12Weight = 5.5d
            hour13Weight = 5.8d
            hour14Weight = 5.9d
            hour15Weight = 6.0d
            hour16Weight = 6.7d
            hour17Weight = 7.3d
            hour18Weight = 7.6d
            hour19Weight = 8.8d
            hour20Weight = 9.3d
            hour21Weight = 7.0d
            hour22Weight = 3.6d
            hour23Weight = 0.9d
        }
        connectivityProfile_50 = new ConnectivityProfile(name: "conn", packetLoss: 0, active: true, latency: 0, bandwidthDown: 0, bandwidthUp: 0)
        connectivityProfile_70 = new ConnectivityProfile(name: "conn", packetLoss: 0, active: true, latency: 0, bandwidthDown: 0, bandwidthUp: 0)

        browserConnectivityWeight_50 =
                new BrowserConnectivityWeight(
                        connectivity: connectivityProfile_50, browser: browserToReturn_50, weight: 0.5d
                ).save()
        browserConnectivityWeight_70 =
                new BrowserConnectivityWeight(
                        connectivity: connectivityProfile_70, browser: browserToReturn_70, weight: 0.7d
                ).save()

        csiConfiguration = new CsiConfiguration(label: "Csi Config",
                description: "For testing",
                csiDay: day,
                browserConnectivityWeights: [browserConnectivityWeight_50, browserConnectivityWeight_70],
                pageWeights: [new PageWeight(page: page_50, weight: 0.5d), new PageWeight(page: page_70, weight: 0.7d)]
        )

        jobGroup1 = new JobGroup(name: "jobGroup1", csiConfiguration: csiConfiguration).save(failOnError: true)
        jobGroup2 = new JobGroup(name: "jobGroup2", csiConfiguration: csiConfiguration).save(failOnError: true)

        csiSystem = new CsiSystem(label: "csiSystem")
        csiSystem.jobGroupWeights.add(new JobGroupWeight(jobGroup: jobGroup1, weight: 0.5, csiSystem: csiSystem))
        csiSystem.jobGroupWeights.add(new JobGroupWeight(jobGroup: jobGroup2, weight: 2.0, csiSystem: csiSystem))
        csiSystem.save(failOnError: true)
    }

    /**
     * Mocks used methods of {@link CustomerSatisfactionWeightService}.
     */
    private void mockCustomerSatisfactionWeightService() {
        def customerSatisfactionWeightService = new MockFor(CustomerSatisfactionWeightService, true)
        customerSatisfactionWeightService.demand.getHoursOfDay(0..10000) { ->
            Map<Integer, Double> hoursofday = [
                    0 : 2.9d,
                    1 : 0.4d,
                    2 : 0.2d,
                    3 : 0.1d,
                    4 : 0.1d,
                    5 : 0.2d,
                    6 : 0.7d,
                    7 : 1.7d,
                    8 : 3.2d,
                    9 : 4.8d,
                    10: 5.6d,
                    11: 5.7d,
                    12: 5.5d,
                    13: 5.8d,
                    14: 5.9d,
                    15: 6.0d,
                    16: 6.7d,
                    17: 7.3d,
                    18: 7.6d,
                    19: 8.8d,
                    20: 9.3d,
                    21: 7.0d,
                    22: 3.6d,
                    23: 0.9d
            ]
            return hoursofday
        }
        serviceUnderTest.customerSatisfactionWeightService = customerSatisfactionWeightService.proxyInstance();
    }

}

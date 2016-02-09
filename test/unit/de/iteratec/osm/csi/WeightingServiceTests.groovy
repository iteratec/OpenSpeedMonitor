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
import de.iteratec.osm.measurement.schedule.JobGroupType
import de.iteratec.osm.report.chart.CsiAggregation
import de.iteratec.osm.report.chart.MeasuredValueUpdateEvent
import de.iteratec.osm.result.EventResult
import de.iteratec.osm.result.MeasuredValueTagService
import de.iteratec.osm.util.PerformanceLoggingService
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import org.joda.time.DateTime
import org.junit.Before

import static org.junit.Assert.assertEquals

@TestFor(WeightingService)
@Mock([EventResult, CsiAggregation, MeasuredValueUpdateEvent, BrowserConnectivityWeight, Browser, ConnectivityProfile,
        JobGroup, CsiDay, CsiConfiguration, CsiSystem])
class WeightingServiceTests {

    WeightingService serviceUnderTest
    private static final double DELTA = 1e-15
    private static final DateTime SHOULD_BE_MAPPED_TO_TWO_A_CLOCK_AM = new DateTime(2014, 1, 1, 2, 30, 12)
    private static final DateTime SHOULD_BE_MAPPED_TO_FIVE_A_CLOCK_PM = new DateTime(2014, 1, 1, 17, 43, 56)
    private static final String TAG_INDICATING_WEIGHT_OF_FIFTY_PERCENT = 'browserWeightedFiftyPercent'
    private static final String TAG_INDICATING_WEIGHT_OF_SEVENTY_PERCENT = 'browserWeightedSeventyPercent'
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

    @Before
    void setUp() {
        serviceUnderTest = service
        mocksCommonToAllTests()
        createTestdataCommonToAllTests()
    }

    void tearDown() {
    }

    void testGetWeightWithoutWeightFactors() {

        // test specific data
        Set<WeightFactor> weightFactors = [] as Set
        CsiValue eventResult = new EventResult()
        CsiValue measuredValue = new CsiAggregation()

        //test for EventResult
        Double deliveredWeight = serviceUnderTest.getWeight(eventResult, weightFactors, csiConfiguration)
        assertEquals(1d, deliveredWeight, DELTA)
        //test for CsiAggregation
        deliveredWeight = serviceUnderTest.getWeight(measuredValue, weightFactors, csiConfiguration)
        assertEquals(1d, deliveredWeight, DELTA)

    }

    void testGetWeightWithHourofdayAsWeightFactor() {
        // test specific data
        Set<WeightFactor> weightFactors = [WeightFactor.HOUROFDAY] as Set
        CsiValue eventResultTwoAClockAm = new EventResult(jobResultDate: SHOULD_BE_MAPPED_TO_TWO_A_CLOCK_AM.toDate())
        CsiValue eventResultFiveAClockPm = new EventResult(jobResultDate: SHOULD_BE_MAPPED_TO_FIVE_A_CLOCK_PM.toDate())
        CsiValue measuredValueTwoAClockAm = new CsiAggregation(started: SHOULD_BE_MAPPED_TO_TWO_A_CLOCK_AM.toDate())
        CsiValue measuredValueFiveAClockPm = new CsiAggregation(started: SHOULD_BE_MAPPED_TO_FIVE_A_CLOCK_PM.toDate())

        mockMeasuredValueTagService(this.browserToReturn_50, this.browserToReturn_70, this.page_50, this.page_70)

        //test for EventResults
        Double deliveredWeight = serviceUnderTest.getWeight(eventResultTwoAClockAm, weightFactors, csiConfiguration)
        double expectedWeight = 0.2d
        assertEquals(expectedWeight, deliveredWeight, DELTA)
        deliveredWeight = serviceUnderTest.getWeight(eventResultFiveAClockPm, weightFactors, csiConfiguration)
        expectedWeight = 7.3d
        assertEquals(expectedWeight, deliveredWeight, DELTA)

        //test for MeasuredValues
        deliveredWeight = serviceUnderTest.getWeight(measuredValueTwoAClockAm, weightFactors, csiConfiguration)
        expectedWeight = 0.2d
        assertEquals(expectedWeight, deliveredWeight, DELTA)
        deliveredWeight = serviceUnderTest.getWeight(measuredValueFiveAClockPm, weightFactors, csiConfiguration)
        expectedWeight = 7.3d
        assertEquals(expectedWeight, deliveredWeight, DELTA)
    }

    void testGetWeightWithBrowserConnectivityAsWeightFactor() {
        // test specific data
        Set<WeightFactor> weightFactors = [WeightFactor.BROWSER_CONNECTIVITY_COMBINATION] as Set

        ConnectivityProfile.metaClass.toString = { -> return delegate.name }

        CsiValue eventResultBrowserWeightOfFiftyPercent = new EventResult(tag: TAG_INDICATING_WEIGHT_OF_FIFTY_PERCENT, connectivityProfile: connectivityProfile_50)
        CsiValue eventResultBrowserWeightOfSeventyPercent = new EventResult(tag: TAG_INDICATING_WEIGHT_OF_SEVENTY_PERCENT, connectivityProfile: connectivityProfile_70)
        CsiValue measuredValueBrowserWeightOfFiftyPercent = new CsiAggregation(tag: TAG_INDICATING_WEIGHT_OF_FIFTY_PERCENT, connectivityProfile: connectivityProfile_50)
        CsiValue measuredValueBrowserWeightOfSeventyPercent = new CsiAggregation(tag: TAG_INDICATING_WEIGHT_OF_SEVENTY_PERCENT, connectivityProfile: connectivityProfile_70)

        //test specific mocks
        mockMeasuredValueTagService(this.browserToReturn_50, this.browserToReturn_70, this.page_50, this.page_70)

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

        //test for MeasuredValues
        deliveredWeight = serviceUnderTest.getWeight(
                measuredValueBrowserWeightOfFiftyPercent, weightFactors, csiConfiguration
        )
        //assertions
        expectedWeight = 0.5d
        assertEquals(expectedWeight, deliveredWeight, DELTA)
        deliveredWeight = serviceUnderTest.getWeight(measuredValueBrowserWeightOfSeventyPercent, weightFactors, csiConfiguration)
        expectedWeight = 0.7d
        assertEquals(expectedWeight, deliveredWeight, DELTA)
    }

    void testGetWeightWithPageAsWeightFactor() {
        // test specific data
        Set<WeightFactor> weightFactors = [WeightFactor.PAGE] as Set
        CsiValue eventResultBrowserWeightOfFiftyPercent = new EventResult(tag: TAG_INDICATING_WEIGHT_OF_FIFTY_PERCENT)
        CsiValue eventResultBrowserWeightOfSeventyPercent = new EventResult(tag: TAG_INDICATING_WEIGHT_OF_SEVENTY_PERCENT)
        CsiValue measuredValueBrowserWeightOfFiftyPercent = new CsiAggregation(tag: TAG_INDICATING_WEIGHT_OF_FIFTY_PERCENT)
        CsiValue measuredValueBrowserWeightOfSeventyPercent = new CsiAggregation(tag: TAG_INDICATING_WEIGHT_OF_SEVENTY_PERCENT)

        //test specific mocks
        mockMeasuredValueTagService(browserToReturn_50, browserToReturn_70, page_50, page_70)

        //test for EventResults
        Double deliveredWeight = serviceUnderTest.getWeight(eventResultBrowserWeightOfFiftyPercent, weightFactors, csiConfiguration)
        double expectedWeight = 0.5d
        assertEquals(expectedWeight, deliveredWeight, DELTA)
        deliveredWeight = serviceUnderTest.getWeight(eventResultBrowserWeightOfSeventyPercent, weightFactors, csiConfiguration)
        expectedWeight = 0.7d
        assertEquals(expectedWeight, deliveredWeight, DELTA)

        //test for MeasuredValues
        deliveredWeight = serviceUnderTest.getWeight(measuredValueBrowserWeightOfFiftyPercent, weightFactors, csiConfiguration)
        expectedWeight = 0.5d
        assertEquals(expectedWeight, deliveredWeight, DELTA)
        deliveredWeight = serviceUnderTest.getWeight(measuredValueBrowserWeightOfSeventyPercent, weightFactors, csiConfiguration)
        expectedWeight = 0.7d
        assertEquals(expectedWeight, deliveredWeight, DELTA)
    }

    void testGetWeightWithMultipleWeightFactorsForEventResults() {
        // test specific data
        TestDataUtil.createConnectivityProfile("conn")
        CsiValue eventResultWeightFiftyTwoAm =
                new EventResult(
                        tag: TAG_INDICATING_WEIGHT_OF_FIFTY_PERCENT,
                        jobResultDate: SHOULD_BE_MAPPED_TO_TWO_A_CLOCK_AM.toDate(),
                        connectivityProfile: connectivityProfile_50
                )
        CsiValue eventResultWeightSeventyFivePm =
                new EventResult(
                        tag: TAG_INDICATING_WEIGHT_OF_SEVENTY_PERCENT,
                        jobResultDate: SHOULD_BE_MAPPED_TO_FIVE_A_CLOCK_PM.toDate(),
                        connectivityProfile: connectivityProfile_70
                )

        //test specific mocks
        mockMeasuredValueTagService(browserToReturn_50, browserToReturn_70, page_50, page_70)

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
                csByWptDocCompleteInPercent: 10d,
                tag: TAG_INDICATING_WEIGHT_OF_FIFTY_PERCENT,
                jobResultDate: SHOULD_BE_MAPPED_TO_TWO_A_CLOCK_AM.toDate(),
                docCompleteTimeInMillisecs: 1000,
                connectivityProfile: connectivityProfile_50).save(validate: false)
        CsiValue eventResultWeightSeventyFivePm = new EventResult(
                id: 2l,
                csByWptDocCompleteInPercent: 20d,
                tag: TAG_INDICATING_WEIGHT_OF_SEVENTY_PERCENT,
                jobResultDate: SHOULD_BE_MAPPED_TO_FIVE_A_CLOCK_PM.toDate(),
                docCompleteTimeInMillisecs: 1000,
                connectivityProfile: connectivityProfile_70).save(validate: false)

        eventResultWeightFiftyTwoAm.metaClass.isCsiRelevant = { ->
            return true
        }
        eventResultWeightSeventyFivePm.metaClass.isCsiRelevant = { ->
            return true
        }

        //test specific mocks
        mockMeasuredValueTagService(browserToReturn_50, browserToReturn_70, this.page_50, this.page_70)

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

    void testGetWeightedCsiValuesFromMeasuredValues() {
        CsiAggregation measuredValueWeightFiftyTwoAm = new CsiAggregation(
                csByWptDocCompleteInPercent: 10d,
                tag: TAG_INDICATING_WEIGHT_OF_FIFTY_PERCENT,
                started: SHOULD_BE_MAPPED_TO_TWO_A_CLOCK_AM.toDate(),
                connectivityProfile: connectivityProfile_50
        )
        measuredValueWeightFiftyTwoAm.addAllToResultIds([1l, 2l, 3l])
        measuredValueWeightFiftyTwoAm.save(validate: false)
        TestDataUtil.createUpdateEvent(measuredValueWeightFiftyTwoAm.ident(), MeasuredValueUpdateEvent.UpdateCause.CALCULATED)
        CsiAggregation measuredValueWeightSeventyFivePm = new CsiAggregation(
                csByWptDocCompleteInPercent: 20d,
                tag: TAG_INDICATING_WEIGHT_OF_SEVENTY_PERCENT,
                started: SHOULD_BE_MAPPED_TO_FIVE_A_CLOCK_PM.toDate(),
                connectivityProfile: connectivityProfile_70
        )
        measuredValueWeightSeventyFivePm.addAllToResultIds([4l, 5l, 6l])
        measuredValueWeightSeventyFivePm.save(validate: false)
        TestDataUtil.createUpdateEvent(measuredValueWeightSeventyFivePm.ident(), MeasuredValueUpdateEvent.UpdateCause.CALCULATED)

        //test specific mocks
        mockMeasuredValueTagService(browserToReturn_50, browserToReturn_70, page_50, page_70)

        //tests with all three WeightFactors
        List<WeightedCsiValue> weightedValues = serviceUnderTest.getWeightedCsiValues(
                [measuredValueWeightFiftyTwoAm, measuredValueWeightSeventyFivePm],
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
                [measuredValueWeightFiftyTwoAm, measuredValueWeightSeventyFivePm],
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
                [measuredValueWeightFiftyTwoAm, measuredValueWeightSeventyFivePm],
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
                [measuredValueWeightFiftyTwoAm, measuredValueWeightSeventyFivePm],
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

    void testGetWeightedCsiValuesFromMeasuredValuesByCsiSystem() {
        CsiAggregation measuredValue1 = new CsiAggregation(
                csByWptDocCompleteInPercent: 10d,
                tag: jobGroup1.ident(),
                started: SHOULD_BE_MAPPED_TO_TWO_A_CLOCK_AM.toDate(),
                connectivityProfile: connectivityProfile_50,
        )
        measuredValue1.addAllToResultIds([1l, 2l, 3l])
        measuredValue1.save(validate: false)
        TestDataUtil.createUpdateEvent(measuredValue1.ident(), MeasuredValueUpdateEvent.UpdateCause.CALCULATED)
        CsiAggregation measuredValue2 = new CsiAggregation(
                csByWptDocCompleteInPercent: 20d,
                tag: jobGroup2.ident(),
                started: SHOULD_BE_MAPPED_TO_FIVE_A_CLOCK_PM.toDate(),
                connectivityProfile: connectivityProfile_70
        )
        measuredValue2.addAllToResultIds([4l, 5l, 6l])
        measuredValue2.save(validate: false)
        TestDataUtil.createUpdateEvent(measuredValue2.ident(), MeasuredValueUpdateEvent.UpdateCause.CALCULATED)

        //test specific mocks
        mockMeasuredValueTagService(browserToReturn_50, browserToReturn_70, page_50, page_70)

        //tests with all three WeightFactors
        List<WeightedCsiValue> weightedValues = serviceUnderTest.getWeightedCsiValues(
                [measuredValue1, measuredValue2], csiSystem)

        assertEquals(2, weightedValues.size())

        double expectedJobGroupWeight1 = 0.5d
        double expectedJobGroupWeight2 = 2.0d
        WeightedCsiValue weightedCsiValue1 = weightedValues.find {
            it.weightedValue.weight == expectedJobGroupWeight1
        }
        assert(weightedCsiValue1)
        assertEquals(expectedJobGroupWeight1,weightedCsiValue1.weightedValue.weight, DELTA)
        assertEquals(10d,weightedCsiValue1.weightedValue.value, DELTA)
        weightedValues.removeElement(weightedCsiValue1)

        WeightedCsiValue weightedCsiValue2 = weightedValues[0]
        assert(weightedCsiValue1)
        assertEquals(expectedJobGroupWeight2,weightedCsiValue2.weightedValue.weight, DELTA)
        assertEquals(20d,weightedCsiValue2.weightedValue.value, DELTA)
    }

    void testFlattenWeightedCsiValuesWithoutData() {
        //testdata
        List<WeightedCsiValue> valuesToFlatten = []
        //test-execution
        List<WeightedCsiValue> flattened = serviceUnderTest.flattenWeightedCsiValues(valuesToFlatten)
        //assertions
        assertEquals(0, flattened.size())
    }

    void testFlattenWeightedCsiValuesCalledWithData() {
        //testdata
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
        serviceUnderTest.performanceLoggingService = new PerformanceLoggingService()
        // into the domain EventResult injected service csiConfigCacheService would be null so we have to use metaclass to implement isCsiRelevant()-method for tests
        EventResult.metaClass.isCsiRelevant = { ->
            return true
        }
    }

    private createTestdataCommonToAllTests() {
        page_50 = new Page(name: 'page_50')
        page_70 = new Page(name: 'page_70')
        browserToReturn_50 = new Browser(name: 'browser_50', weight: 0.5d)
        browserToReturn_70 = new Browser(name: 'browser_70', weight: 0.7d)
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

        jobGroup1 = new JobGroup(name: "jobGroup1", csiConfiguration: csiConfiguration, groupType: JobGroupType.CSI_AGGREGATION).save(failOnError: true)
        jobGroup2 = new JobGroup(name: "jobGroup2", csiConfiguration: csiConfiguration, groupType: JobGroupType.CSI_AGGREGATION).save(failOnError: true)

        csiSystem = new CsiSystem(jobGroupWeights: [
                new JobGroupWeight(jobGroup: jobGroup1, weight: 0.5),
                new JobGroupWeight(jobGroup: jobGroup2, weight: 2)
        ])
    }

    /**
     * Mocks used methods of {@link CustomerSatisfactionWeightService}.
     */
    private void mockCustomerSatisfactionWeightService() {
        def customerSatisfactionWeightService = mockFor(CustomerSatisfactionWeightService, true)
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
        serviceUnderTest.customerSatisfactionWeightService = customerSatisfactionWeightService.createMock();
    }

    /**
     * Mocks used methods of {@link MeasuredValueTagService}.
     */
    private void mockMeasuredValueTagService(Browser browserToReturn_50, Browser browserToReturn_70, Page pageToReturn_50, Page pageToReturn_70) {
        def measuredValueTagService = mockFor(MeasuredValueTagService, true)
        measuredValueTagService.demand.findBrowserOfHourlyEventTag(0..10000) { String hourlyEventMvTag ->
            Browser browser
            if (hourlyEventMvTag.equals(TAG_INDICATING_WEIGHT_OF_FIFTY_PERCENT)) {
                browser = browserToReturn_50
            }
            if (hourlyEventMvTag.equals(TAG_INDICATING_WEIGHT_OF_SEVENTY_PERCENT)) {
                browser = browserToReturn_70
            }
            return browser
        }
        measuredValueTagService.demand.findPageByPageTag(0..10000) { String hourlyEventMvTag ->
            Page page
            if (hourlyEventMvTag.equals(TAG_INDICATING_WEIGHT_OF_FIFTY_PERCENT)) {
                page = pageToReturn_50
            }
            if (hourlyEventMvTag.equals(TAG_INDICATING_WEIGHT_OF_SEVENTY_PERCENT)) {
                page = pageToReturn_70
            }
            return page
        }
        measuredValueTagService.demand.getJobGroupIdFromWeeklyOrDailyPageTag(0..100000) { String tag ->
            try {
                return Long.valueOf(tag)
            } catch(NumberFormatException e) {
                return 1
            }
        }
        measuredValueTagService.demand.getJobGroupIdFromWeeklyOrDailyShopTag(0..100000) { String tag ->
            try {
                return Long.valueOf(tag)
            } catch(NumberFormatException e) {
                return 1
            }
        }
        measuredValueTagService.demand.findJobGroupIdOfHourlyEventTag(0..100000) { String tag ->
            try {
                return Long.valueOf(tag)
            } catch(NumberFormatException e) {
                return 1
            }
        }
        serviceUnderTest.measuredValueTagService = measuredValueTagService.createMock();
    }
}

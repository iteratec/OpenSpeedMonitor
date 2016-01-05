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
import de.iteratec.osm.report.chart.MeasuredValue
import de.iteratec.osm.report.chart.MeasuredValueUpdateEvent
import de.iteratec.osm.result.EventResult
import de.iteratec.osm.result.MeasuredValueTagService
import de.iteratec.osm.util.PerformanceLoggingService
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import org.joda.time.DateTime

import static org.junit.Assert.assertEquals

@TestFor(WeightingService)
@Mock([EventResult, MeasuredValue, MeasuredValueUpdateEvent, BrowserConnectivityWeight, Browser])
class WeightingServiceTests {

    WeightingService serviceUnderTest
    private static final double DELTA = 1e-15
    private static final DateTime SHOULD_BE_MAPPED_TO_TWO_A_CLOCK_AM = new DateTime(2014, 1, 1, 2, 30, 12)
    private static final DateTime SHOULD_BE_MAPPED_TO_FIVE_A_CLOCK_PM = new DateTime(2014, 1, 1, 17, 43, 56)
    private static final String TAG_INDICATING_WEIGHT_OF_FIFTY_PERCENT = 'browserWeightedFiftyPercent'
    private static final String TAG_INDICATING_WEIGHT_OF_SEVENTY_PERCENT = 'browserWeightedSeventyPercent'


    void setUp() {
        serviceUnderTest = service
        // into the domain EventResult injected service csiConfigCacheService would be null so we have to use metaclass to implement isCsiRelevant()-method for tests
        EventResult.metaClass.isCsiRelevant = { ->
            return true
        }
        //mocks
        mockCustomerSatisfactionWeightService()
        mockMeasuredValueTagService()
        serviceUnderTest.performanceLoggingService = new PerformanceLoggingService()
    }

    void tearDown() {
    }

    void testGetWeightWithoutWeightFactors() {

        // test specific data
        Set<WeightFactor> weightFactors = [] as Set
        CsiValue eventResult = new EventResult()
        CsiValue measuredValue = new MeasuredValue()

        //test for EventResult
        Double deliveredWeight = serviceUnderTest.getWeight(eventResult, weightFactors, BrowserConnectivityWeight.list())
        assertEquals(1d, deliveredWeight, DELTA)
        //test for MeasuredValue
        deliveredWeight = serviceUnderTest.getWeight(measuredValue, weightFactors, BrowserConnectivityWeight.list())
        assertEquals(1d, deliveredWeight, DELTA)

    }

    void testGetWeightWithHourofdayAsWeightFactor() {
        // test specific data
        Set<WeightFactor> weightFactors = [WeightFactor.HOUROFDAY] as Set
        CsiValue eventResultTwoAClockAm = new EventResult(jobResultDate: SHOULD_BE_MAPPED_TO_TWO_A_CLOCK_AM.toDate())
        CsiValue eventResultFiveAClockPm = new EventResult(jobResultDate: SHOULD_BE_MAPPED_TO_FIVE_A_CLOCK_PM.toDate())
        CsiValue measuredValueTwoAClockAm = new MeasuredValue(started: SHOULD_BE_MAPPED_TO_TWO_A_CLOCK_AM.toDate())
        CsiValue measuredValueFiveAClockPm = new MeasuredValue(started: SHOULD_BE_MAPPED_TO_FIVE_A_CLOCK_PM.toDate())

        //test for EventResults
        Double deliveredWeight = serviceUnderTest.getWeight(eventResultTwoAClockAm, weightFactors, BrowserConnectivityWeight.list())
        double expectedWeight = 0.2d
        assertEquals(expectedWeight, deliveredWeight, DELTA)
        deliveredWeight = serviceUnderTest.getWeight(eventResultFiveAClockPm, weightFactors, BrowserConnectivityWeight.list())
        expectedWeight = 7.3d
        assertEquals(expectedWeight, deliveredWeight, DELTA)

        //test for MeasuredValues
        deliveredWeight = serviceUnderTest.getWeight(measuredValueTwoAClockAm, weightFactors, BrowserConnectivityWeight.list())
        expectedWeight = 0.2d
        assertEquals(expectedWeight, deliveredWeight, DELTA)
        deliveredWeight = serviceUnderTest.getWeight(measuredValueFiveAClockPm, weightFactors, BrowserConnectivityWeight.list())
        expectedWeight = 7.3d
        assertEquals(expectedWeight, deliveredWeight, DELTA)

        //test for not getting correct hourofday
        mockCustomerSatisfactionWeightServiceToDeliverWrongHoursofDay()
        deliveredWeight = serviceUnderTest.getWeight(measuredValueTwoAClockAm, weightFactors, BrowserConnectivityWeight.list())
        expectedWeight = 0d
        assertEquals(expectedWeight, deliveredWeight, DELTA)
    }

    void testGetWeightWithBrowserConnectivityAsWeightFactor() {
        // test specific data
        Set<WeightFactor> weightFactors = [WeightFactor.BROWSER_CONNECTIVITY_COMBINATION] as Set

        ConnectivityProfile connectivityProfile = new ConnectivityProfile(name: "conn", packetLoss: 0, active: true, latency: 0, bandwidthDown: 0, bandwidthUp: 0)
        ConnectivityProfile connectivityProfile70 = new ConnectivityProfile(name: "conn", packetLoss: 0, active: true, latency: 0, bandwidthDown: 0, bandwidthUp: 0)
//        BrowserConnectivityWeight.metaClass.'static'.findByBrowserAndConnectivity = { browser, conn ->
//            if (conn == connectivityProfile70) return browserConnectivityWeight70 else return browserConnectivityWeight }
        CsiValue eventResultBrowserWeightOfFiftyPercent = new EventResult(tag: TAG_INDICATING_WEIGHT_OF_FIFTY_PERCENT, connectivityProfile: connectivityProfile)
        CsiValue eventResultBrowserWeightOfSeventyPercent = new EventResult(tag: TAG_INDICATING_WEIGHT_OF_SEVENTY_PERCENT, connectivityProfile: connectivityProfile70)
        CsiValue measuredValueBrowserWeightOfFiftyPercent = new MeasuredValue(tag: TAG_INDICATING_WEIGHT_OF_FIFTY_PERCENT, connectivityProfile: connectivityProfile)
        CsiValue measuredValueBrowserWeightOfSeventyPercent = new MeasuredValue(tag: TAG_INDICATING_WEIGHT_OF_SEVENTY_PERCENT, connectivityProfile: connectivityProfile70)
        BrowserConnectivityWeight browserConnectivityWeight = new BrowserConnectivityWeight(
                connectivityProfile: connectivityProfile,
                browser: serviceUnderTest.measuredValueTagService.findBrowserOfHourlyEventTag(TAG_INDICATING_WEIGHT_OF_FIFTY_PERCENT),
                weight: 0.5d)
        BrowserConnectivityWeight browserConnectivityWeight70 = new BrowserConnectivityWeight(
                connectivityProfile: connectivityProfile70,
                browser: serviceUnderTest.measuredValueTagService.findBrowserOfHourlyEventTag(TAG_INDICATING_WEIGHT_OF_SEVENTY_PERCENT),
                weight: 0.7d)

        //test for EventResults
        Double deliveredWeight = serviceUnderTest.getWeight(eventResultBrowserWeightOfFiftyPercent, weightFactors, [browserConnectivityWeight, browserConnectivityWeight70])
        double expectedWeight = 0.5d
        assertEquals(expectedWeight, deliveredWeight, DELTA)
        deliveredWeight = serviceUnderTest.getWeight(eventResultBrowserWeightOfSeventyPercent, weightFactors, [browserConnectivityWeight, browserConnectivityWeight70])
        expectedWeight = 0.7d
        assertEquals(expectedWeight, deliveredWeight, DELTA)

        //test for MeasuredValues
        deliveredWeight = serviceUnderTest.getWeight(measuredValueBrowserWeightOfFiftyPercent, weightFactors, [browserConnectivityWeight, browserConnectivityWeight70])
        expectedWeight = 0.5d
        assertEquals(expectedWeight, deliveredWeight, DELTA)
        deliveredWeight = serviceUnderTest.getWeight(measuredValueBrowserWeightOfSeventyPercent, weightFactors, [browserConnectivityWeight, browserConnectivityWeight70])
        expectedWeight = 0.7d
        assertEquals(expectedWeight, deliveredWeight, DELTA)
    }

    void testGetWeightWithPageAsWeightFactor() {
        // test specific data
        Set<WeightFactor> weightFactors = [WeightFactor.PAGE] as Set
        CsiValue eventResultBrowserWeightOfFiftyPercent = new EventResult(tag: TAG_INDICATING_WEIGHT_OF_FIFTY_PERCENT)
        CsiValue eventResultBrowserWeightOfSeventyPercent = new EventResult(tag: TAG_INDICATING_WEIGHT_OF_SEVENTY_PERCENT)
        CsiValue measuredValueBrowserWeightOfFiftyPercent = new MeasuredValue(tag: TAG_INDICATING_WEIGHT_OF_FIFTY_PERCENT)
        CsiValue measuredValueBrowserWeightOfSeventyPercent = new MeasuredValue(tag: TAG_INDICATING_WEIGHT_OF_SEVENTY_PERCENT)

        //test for EventResults
        Double deliveredWeight = serviceUnderTest.getWeight(eventResultBrowserWeightOfFiftyPercent, weightFactors, BrowserConnectivityWeight.list())
        double expectedWeight = 0.5d
        assertEquals(expectedWeight, deliveredWeight, DELTA)
        deliveredWeight = serviceUnderTest.getWeight(eventResultBrowserWeightOfSeventyPercent, weightFactors, BrowserConnectivityWeight.list())
        expectedWeight = 0.7d
        assertEquals(expectedWeight, deliveredWeight, DELTA)

        //test for MeasuredValues
        deliveredWeight = serviceUnderTest.getWeight(measuredValueBrowserWeightOfFiftyPercent, weightFactors, BrowserConnectivityWeight.list())
        expectedWeight = 0.5d
        assertEquals(expectedWeight, deliveredWeight, DELTA)
        deliveredWeight = serviceUnderTest.getWeight(measuredValueBrowserWeightOfSeventyPercent, weightFactors, BrowserConnectivityWeight.list())
        expectedWeight = 0.7d
        assertEquals(expectedWeight, deliveredWeight, DELTA)
    }

    void testGetWeightWithMultipleWeightFactorsForEventResults() {
        // test specific data
        ConnectivityProfile connectivityProfile = new ConnectivityProfile(name: "conn", packetLoss: 0, active: true, latency: 0, bandwidthDown: 0, bandwidthUp: 0)
        BrowserConnectivityWeight browserConnectivityWeight = new BrowserConnectivityWeight(connectivityProfile: connectivityProfile, browser: new Browser(name: "null"), weight: 0.5d)
        browserConnectivityWeight.save()
        BrowserConnectivityWeight.metaClass.'static'.findByBrowserAndConnectivity = { browser, conn -> browserConnectivityWeight }
        CsiValue eventResultWeightFiftyTwoAm = new EventResult(tag: TAG_INDICATING_WEIGHT_OF_FIFTY_PERCENT, jobResultDate: SHOULD_BE_MAPPED_TO_TWO_A_CLOCK_AM.toDate(), connectivityProfile: connectivityProfile)
        CsiValue eventResultWeightSeventyFivePm = new EventResult(tag: TAG_INDICATING_WEIGHT_OF_SEVENTY_PERCENT, jobResultDate: SHOULD_BE_MAPPED_TO_FIVE_A_CLOCK_PM.toDate(), connectivityProfile: connectivityProfile)

        //test with all three WeightFactors
        Double deliveredWeight = serviceUnderTest.getWeight(eventResultWeightFiftyTwoAm, [WeightFactor.HOUROFDAY, WeightFactor.PAGE, WeightFactor.BROWSER_CONNECTIVITY_COMBINATION] as Set, BrowserConnectivityWeight.list())
        double expectedHourofdayWeight = 0.2d
        double expectedBrowserConnectivityWeight = 0.5d
        double expectedPageWeight = 0.5d
        double expectedWeight = expectedHourofdayWeight * expectedBrowserConnectivityWeight * expectedPageWeight
        assertEquals(expectedWeight, deliveredWeight, DELTA)

        deliveredWeight = serviceUnderTest.getWeight(eventResultWeightSeventyFivePm, [WeightFactor.HOUROFDAY, WeightFactor.PAGE, WeightFactor.BROWSER_CONNECTIVITY_COMBINATION] as Set, BrowserConnectivityWeight.list())
        expectedHourofdayWeight = 7.3d
        expectedBrowserConnectivityWeight = 0.5d
        expectedPageWeight = 0.7d
        expectedWeight = expectedHourofdayWeight * expectedBrowserConnectivityWeight * expectedPageWeight
        assertEquals(expectedWeight, deliveredWeight, DELTA)

        //test with two weightFactors
        deliveredWeight = serviceUnderTest.getWeight(eventResultWeightSeventyFivePm, [WeightFactor.PAGE, WeightFactor.BROWSER_CONNECTIVITY_COMBINATION] as Set, BrowserConnectivityWeight.list())
        expectedBrowserConnectivityWeight = 0.5d
        expectedPageWeight = 0.7d
        expectedWeight = expectedBrowserConnectivityWeight * expectedPageWeight
        assertEquals(expectedWeight, deliveredWeight, DELTA)

        deliveredWeight = serviceUnderTest.getWeight(eventResultWeightSeventyFivePm, [WeightFactor.HOUROFDAY, WeightFactor.BROWSER_CONNECTIVITY_COMBINATION] as Set, BrowserConnectivityWeight.list())
        expectedHourofdayWeight = 7.3d
        expectedBrowserConnectivityWeight = 0.5d
        expectedWeight = expectedHourofdayWeight * expectedBrowserConnectivityWeight
        assertEquals(expectedWeight, deliveredWeight, DELTA)

    }

    void testGetWeightedCsiValuesFromEventResults() {
        // test specific data
        ConnectivityProfile connectivityProfile = new ConnectivityProfile(name: "conn", packetLoss: 0, active: true, latency: 0, bandwidthDown: 0, bandwidthUp: 0)
        ConnectivityProfile connectivityProfile70 = new ConnectivityProfile(name: "conn", packetLoss: 0, active: true, latency: 0, bandwidthDown: 0, bandwidthUp: 0)
        BrowserConnectivityWeight browserConnectivityWeight = new BrowserConnectivityWeight(connectivityProfile: connectivityProfile, browser: new Browser(name: "null"), weight: 0.5d)
        BrowserConnectivityWeight browserConnectivityWeight70 = new BrowserConnectivityWeight(connectivityProfile: connectivityProfile, browser: new Browser(name: "null"), weight: 0.7d)
        browserConnectivityWeight.save()
        BrowserConnectivityWeight.metaClass.'static'.findByBrowserAndConnectivity = { browser, conn ->
            if(conn == connectivityProfile70) return browserConnectivityWeight70 else return browserConnectivityWeight }
        CsiValue eventResultWeightFiftyTwoAm = new EventResult(
                id: 1l,
                customerSatisfactionInPercent: 10d,
                tag: TAG_INDICATING_WEIGHT_OF_FIFTY_PERCENT,
                jobResultDate: SHOULD_BE_MAPPED_TO_TWO_A_CLOCK_AM.toDate(),
                docCompleteTimeInMillisecs: 1000,
                connectivityProfile: connectivityProfile).save(validate: false)
        CsiValue eventResultWeightSeventyFivePm = new EventResult(
                id: 2l,
                customerSatisfactionInPercent: 20d,
                tag: TAG_INDICATING_WEIGHT_OF_SEVENTY_PERCENT,
                jobResultDate: SHOULD_BE_MAPPED_TO_FIVE_A_CLOCK_PM.toDate(),
                docCompleteTimeInMillisecs: 1000,
                connectivityProfile: connectivityProfile70).save(validate: false)

        eventResultWeightFiftyTwoAm.metaClass.isCsiRelevant = { ->
            return true
        }
        eventResultWeightSeventyFivePm.metaClass.isCsiRelevant = { ->
            return true
        }
        //tests with all three WeightFactors

        List<WeightedCsiValue> weightedValues = serviceUnderTest.getWeightedCsiValues(
                [eventResultWeightFiftyTwoAm, eventResultWeightSeventyFivePm],
                [WeightFactor.HOUROFDAY, WeightFactor.PAGE, WeightFactor.BROWSER_CONNECTIVITY_COMBINATION] as Set)

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
                [WeightFactor.HOUROFDAY, WeightFactor.PAGE] as Set)

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
                [WeightFactor.BROWSER_CONNECTIVITY_COMBINATION, WeightFactor.PAGE] as Set)

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
                [WeightFactor.BROWSER_CONNECTIVITY_COMBINATION] as Set)

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
                [] as Set)

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
        // test specific data
        ConnectivityProfile connectivityProfile = new ConnectivityProfile(name: "conn", packetLoss: 0, active: true, latency: 0, bandwidthDown: 0, bandwidthUp: 0)
        BrowserConnectivityWeight browserConnectivityWeight = new BrowserConnectivityWeight(connectivityProfile: connectivityProfile, browser: new Browser(name: "null"), weight: 0.5d)
        browserConnectivityWeight.save()
        BrowserConnectivityWeight.metaClass.'static'.findByBrowserAndConnectivity = { browser, conn -> browserConnectivityWeight }

        MeasuredValue measuredValueWeightFiftyTwoAm = new MeasuredValue(
                value: 10d,
                tag: TAG_INDICATING_WEIGHT_OF_FIFTY_PERCENT,
                started: SHOULD_BE_MAPPED_TO_TWO_A_CLOCK_AM.toDate(),
                connectivityProfile: connectivityProfile
        ).save(validate: false)
        measuredValueWeightFiftyTwoAm.addAllToResultIds([1l, 2l, 3l])
        TestDataUtil.createUpdateEvent(measuredValueWeightFiftyTwoAm.ident(), MeasuredValueUpdateEvent.UpdateCause.CALCULATED)
        MeasuredValue measuredValueWeightSeventyFivePm = new MeasuredValue(
                value: 20d,
                tag: TAG_INDICATING_WEIGHT_OF_SEVENTY_PERCENT,
                started: SHOULD_BE_MAPPED_TO_FIVE_A_CLOCK_PM.toDate(),
                connectivityProfile: connectivityProfile
        ).save(validate: false)
        measuredValueWeightSeventyFivePm.addAllToResultIds([4l, 5l, 6l])
        TestDataUtil.createUpdateEvent(measuredValueWeightSeventyFivePm.ident(), MeasuredValueUpdateEvent.UpdateCause.CALCULATED)

        //tests with all three WeightFactors

        List<WeightedCsiValue> weightedValues = serviceUnderTest.getWeightedCsiValues(
                [measuredValueWeightFiftyTwoAm, measuredValueWeightSeventyFivePm],
                [WeightFactor.HOUROFDAY, WeightFactor.PAGE, WeightFactor.BROWSER_CONNECTIVITY_COMBINATION] as Set)

        assertEquals(2, weightedValues.size())

        double expectedHourofdayWeight = 0.2d
        double expectedBrowserConnectivityWeight = 0.5d
        double expectedPageWeight = 0.5d
        double expectedWeight = expectedHourofdayWeight * expectedBrowserConnectivityWeight * expectedPageWeight
        assertEquals(1, weightedValues.findAll {
            it.underlyingEventResultIds.equals([1l, 2l, 3l]) && it.weightedValue.getValue().equals(10d) && it.weightedValue.getWeight().equals(expectedWeight)
        }.size())

        expectedHourofdayWeight = 7.3d
        expectedBrowserConnectivityWeight = 0.5d
        expectedPageWeight = 0.7d
        expectedWeight = expectedHourofdayWeight * expectedBrowserConnectivityWeight * expectedPageWeight
        assertEquals(1, weightedValues.findAll {
            it.underlyingEventResultIds.equals([4l, 5l, 6l]) && it.weightedValue.getValue().equals(20d) && it.weightedValue.getWeight().equals(expectedWeight)
        }.size())

        //tests with hourofday and page as WeightFactors

        weightedValues = serviceUnderTest.getWeightedCsiValues(
                [measuredValueWeightFiftyTwoAm, measuredValueWeightSeventyFivePm],
                [WeightFactor.HOUROFDAY, WeightFactor.PAGE] as Set)

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
                [WeightFactor.BROWSER_CONNECTIVITY_COMBINATION, WeightFactor.PAGE] as Set)

        assertEquals(2, weightedValues.size())

        expectedBrowserConnectivityWeight = 0.5d
        expectedPageWeight = 0.5d
        expectedWeight = expectedBrowserConnectivityWeight * expectedPageWeight
        assertEquals(1, weightedValues.findAll {
            it.underlyingEventResultIds.equals([1l, 2l, 3l]) && it.weightedValue.getValue().equals(10d) && it.weightedValue.getWeight().equals(expectedWeight)
        }.size())

        expectedBrowserConnectivityWeight = 0.5d
        expectedPageWeight = 0.7d
        expectedWeight = expectedBrowserConnectivityWeight * expectedPageWeight
        assertEquals(1, weightedValues.findAll {
            it.underlyingEventResultIds.equals([4l, 5l, 6l]) && it.weightedValue.getValue().equals(20d) && it.weightedValue.getWeight().equals(expectedWeight)
        }.size())

        //tests without WeightFactors

        weightedValues = serviceUnderTest.getWeightedCsiValues(
                [measuredValueWeightFiftyTwoAm, measuredValueWeightSeventyFivePm],
                [] as Set)

        assertEquals(1, weightedValues.size())

        WeightedCsiValue flattenedCauseOfMissingWeightFactors = weightedValues[0]
        expectedWeight = 1d
        List<Long> expectedUnderlyingResultIds = [1l, 2l, 3l, 4l, 5l, 6l]
        Double expectedValue = (10d + 20d) / 2
        assertEquals(expectedUnderlyingResultIds, flattenedCauseOfMissingWeightFactors.underlyingEventResultIds)
        assertEquals(expectedValue, flattenedCauseOfMissingWeightFactors.weightedValue.value, DELTA)
        assertEquals(expectedWeight, flattenedCauseOfMissingWeightFactors.weightedValue.weight, DELTA)

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

    //mocking inner services////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

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
     * Mocks used methods of {@link CustomerSatisfactionWeightService}.
     */
    private void mockCustomerSatisfactionWeightServiceToDeliverWrongHoursofDay() {
        def customerSatisfactionWeightService = mockFor(CustomerSatisfactionWeightService, true)
        customerSatisfactionWeightService.demand.getHoursOfDay(0..10000) { ->
            Map<Integer, Double> hoursofday = [
                    0 : null,
                    1 : null,
                    2 : null,
                    3 : null,
                    4 : null,
                    5 : null,
                    6 : null,
                    7 : null,
                    8 : null,
                    9 : null,
                    10: null,
                    11: null,
                    12: null,
                    13: null,
                    14: null,
                    15: null,
                    16: null,
                    17: null,
                    18: null,
                    19: null,
                    20: null,
                    21: null,
                    22: null,
                    23: null
            ]
            return hoursofday
        }
        serviceUnderTest.customerSatisfactionWeightService = customerSatisfactionWeightService.createMock();
    }

    /**
     * Mocks used methods of {@link MeasuredValueTagService}.
     */
    private void mockMeasuredValueTagService() {
        def measuredValueTagService = mockFor(MeasuredValueTagService, true)
        measuredValueTagService.demand.findBrowserOfHourlyEventTag(0..10000) { String hourlyEventMvTag ->
            Browser toReturn = new Browser(weight: 0d)
            if (hourlyEventMvTag.equals(TAG_INDICATING_WEIGHT_OF_FIFTY_PERCENT)) {
                toReturn.setWeight(0.5d)
            }
            if (hourlyEventMvTag.equals(TAG_INDICATING_WEIGHT_OF_SEVENTY_PERCENT)) {
                toReturn.setWeight(0.7d)
            }
            return toReturn
        }
        measuredValueTagService.demand.findPageOfWeeklyPageTag(0..10000) { String hourlyEventMvTag ->
            Page toReturn = new Page(weight: 0d)
            if (hourlyEventMvTag.equals(TAG_INDICATING_WEIGHT_OF_FIFTY_PERCENT)) {
                toReturn.setWeight(0.5d)
            }
            if (hourlyEventMvTag.equals(TAG_INDICATING_WEIGHT_OF_SEVENTY_PERCENT)) {
                toReturn.setWeight(0.7d)
            }
            return toReturn
        }
        serviceUnderTest.measuredValueTagService = measuredValueTagService.createMock();
    }
}

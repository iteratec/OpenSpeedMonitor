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
import de.iteratec.osm.measurement.schedule.ConnectivityProfileService
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.report.chart.CsiAggregation
import de.iteratec.osm.report.chart.CsiAggregationUpdateEvent
import de.iteratec.osm.result.CsiValueService
import de.iteratec.osm.result.EventResult
import de.iteratec.osm.util.PerformanceLoggingService
import grails.buildtestdata.mixin.Build
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import org.joda.time.DateTime
import spock.lang.Specification

import static org.junit.Assert.assertEquals
import static spock.util.matcher.HamcrestMatchers.closeTo
import static spock.util.matcher.HamcrestSupport.that

@TestFor(WeightingService)
@Mock([EventResult, CsiAggregation, CsiAggregationUpdateEvent, BrowserConnectivityWeight, Browser, ConnectivityProfile,
        JobGroup, CsiDay, CsiConfiguration, CsiSystem])
@Build([EventResult, CsiAggregation, CsiConfiguration, Browser, Page, BrowserConnectivityWeight, PageWeight, JobGroup,
        CsiSystem, CsiDay, ConnectivityProfile])
class WeightingServiceTests extends Specification {

    private static final double DELTA = 1e-15

    static final Date MAPPED_TO_TWO_A_CLOCK_AM = new DateTime(2014, 1, 1, 2, 30, 12).toDate()
    static final Date MAPPED_TO_FIVE_A_CLOCK_PM = new DateTime(2014, 1, 1, 17, 43, 56).toDate()

    private JobGroup jobGroup1, jobGroup2

    static Page PAGE_WEIGHTED_50
    static Page PAGE_WEIGHTED_70
    static Browser BROWSER_WEIGHTED_50
    static Browser BROWSER_WEIGHTED_70
    static ConnectivityProfile CONNECTIVITY_WEIGHTED_50
    static ConnectivityProfile CONNECTIVITY_WEIGHTED_70
    
    static CsiConfiguration CSI_CONFIGURATION

    static CsiSystem csiSystem

    def doWithSpring = {
        performanceLoggingService(PerformanceLoggingService)
        csiValueService(CsiValueService)
    }

    void setup() {
        mocksCommonToAllTests()
        createTestDataCommonToAllTests()
    }

    void "Without WeightFactors weight is always 1 when calculated for EventResults"() {

        when: "Weight gets calculated for an EventResult without WeightFactors."
        Double deliveredWeight = service.getWeight(EventResult.build(), [] as Set, CSI_CONFIGURATION)

        then: "That weiht is 1."
        that deliveredWeight , closeTo(1, DELTA)

    }

    void "Without WeightFactors weight is always 1 when calculated for CsiAggregations"() {

        when: "Weight gets calculated for a CsiAggregation without WeightFactors."
        Double deliveredWeight = service.getWeight(CsiAggregation.build(), [] as Set, CSI_CONFIGURATION)

        then: "That weight is 1."
        that deliveredWeight , closeTo(1, DELTA)

    }

    void "Weight gets calculated correctly with HOUROFDAY as WeightFactor for EventResults."() {

        given: "HOUROFDAY WeightFactor and some EventResults in place."
        Set<WeightFactor> weightFactors = [WeightFactor.HOUROFDAY] as Set
        CsiValue eventResultTwoAClockAm = EventResult.buildWithoutSave(jobResultDate: MAPPED_TO_TWO_A_CLOCK_AM, jobGroup: jobGroup1)
        CsiValue eventResultFiveAClockPm = EventResult.buildWithoutSave(jobResultDate: MAPPED_TO_FIVE_A_CLOCK_PM, jobGroup: jobGroup2)
        double expectedWeightTwoAClockAm = 0.2d
        double expectedWeightFiveAClockPm = 7.3d

        when: "Weights get calculated."
        double weightTwoAClockAm = service.getWeight(eventResultTwoAClockAm, weightFactors, CSI_CONFIGURATION)
        double weightFiveAClockPm = service.getWeight(eventResultFiveAClockPm, weightFactors, CSI_CONFIGURATION)

        then: "They are calculated correctly respective hour of day."
        that weightTwoAClockAm, closeTo(expectedWeightTwoAClockAm, DELTA)
        that weightFiveAClockPm, closeTo(expectedWeightFiveAClockPm, DELTA)

    }

    void "Weight gets calculated correctly with HOUROFDAY as WeightFactor for CsiAggregations."() {

        given: "HOUROFDAY WeightFactor and some CsiAggregations in place."
        Set<WeightFactor> weightFactors = [WeightFactor.HOUROFDAY] as Set
        CsiValue csiAggregationTwoAClockAm = CsiAggregation.buildWithoutSave(started: MAPPED_TO_TWO_A_CLOCK_AM, jobGroup: jobGroup1)
        CsiValue csiAggregationFiveAClockPm = CsiAggregation.buildWithoutSave(started: MAPPED_TO_FIVE_A_CLOCK_PM, jobGroup: jobGroup2)
        double expectedWeightTwoAClockAm = 0.2d
        double expectedWeightFiveAClockPm = 7.3d

        when: "Weights get calculated."
        double calculatedWeight_CA_TwoAClockAm = service.getWeight(csiAggregationTwoAClockAm, weightFactors, CSI_CONFIGURATION)
        double calculatedWeight_CA_FiveAClockPm = service.getWeight(csiAggregationFiveAClockPm, weightFactors, CSI_CONFIGURATION)

        then: "They are calculated correctly respective hour of day."
        that calculatedWeight_CA_TwoAClockAm, closeTo(expectedWeightTwoAClockAm, DELTA)
        that calculatedWeight_CA_FiveAClockPm, closeTo(expectedWeightFiveAClockPm, DELTA)

    }

    void "Weight gets calculated correctly with BROWSER_CONNECTIVITY_COMBINATION as WeightFactor for EventResults."() {

        given: "BROWSER_CONNECTIVITY_COMBINATION WeightFactor and some EventResults in place."
        Set<WeightFactor> weightFactors = [WeightFactor.BROWSER_CONNECTIVITY_COMBINATION] as Set
        EventResult er50Percent = EventResult.build(browser: BROWSER_WEIGHTED_50, connectivityProfile: CONNECTIVITY_WEIGHTED_50)
        EventResult er70Percent = EventResult.build(browser: BROWSER_WEIGHTED_70, connectivityProfile: CONNECTIVITY_WEIGHTED_70)
        EventResult erNoWeightFor = EventResult.build(browser: BROWSER_WEIGHTED_70, connectivityProfile: CONNECTIVITY_WEIGHTED_50)

        when: "Weights get calculated."
        double weight50 = service.getWeight(er50Percent, weightFactors, CSI_CONFIGURATION)
        double weight70 = service.getWeight(er70Percent, weightFactors, CSI_CONFIGURATION)
        double noWeight = service.getWeight(erNoWeightFor, weightFactors, CSI_CONFIGURATION)

        then: "They are calculated correctly if respective BrowserConnectivityWeight exists. Otherwise weight is zero."
        that weight50, closeTo(0.5d, DELTA)
        that weight70, closeTo(0.7d, DELTA)
        that noWeight, closeTo(0d, DELTA)

    }

    void "Weight gets calculated correctly with BROWSER_CONNECTIVITY_COMBINATION as WeightFactor for CsiAggregations."() {

        given: "BROWSER_CONNECTIVITY_COMBINATION WeightFactor and some CsiAggregations in place."
        Set<WeightFactor> weightFactors = [WeightFactor.BROWSER_CONNECTIVITY_COMBINATION] as Set
        CsiAggregation ca50Percent = CsiAggregation.build(browser: BROWSER_WEIGHTED_50, connectivityProfile: CONNECTIVITY_WEIGHTED_50)
        CsiAggregation ca70Percent = CsiAggregation.build(browser: BROWSER_WEIGHTED_70, connectivityProfile: CONNECTIVITY_WEIGHTED_70)
        CsiAggregation caNoWeightFor = CsiAggregation.build(browser: BROWSER_WEIGHTED_70, connectivityProfile: CONNECTIVITY_WEIGHTED_50)

        when: "Weights get calculated."
        double weight50 = service.getWeight(ca50Percent, weightFactors, CSI_CONFIGURATION)
        double weight70 = service.getWeight(ca70Percent, weightFactors, CSI_CONFIGURATION)
        double noWeight = service.getWeight(caNoWeightFor, weightFactors, CSI_CONFIGURATION)

        then: "They are calculated correctly if respective BrowserConnectivityWeight exists. Otherwise weight is zero."
        that weight50, closeTo(0.5d, DELTA)
        that weight70, closeTo(0.7d, DELTA)
        that noWeight, closeTo(0d, DELTA)

    }

    void "Weight gets calculated correctly with PAGE as WeightFactor for EventResults."() {

        given: "PAGE WeightFactor and some EventResults in place."
        Set<WeightFactor> weightFactors = [WeightFactor.PAGE] as Set
        CsiValue erWeight50 = EventResult.build(page: PAGE_WEIGHTED_50)
        CsiValue erWeight70 = EventResult.build(page: PAGE_WEIGHTED_70)
        CsiValue erNoWeight = EventResult.build(page: Page.build())

        when: "Weights get calculated."
        double weight50 = service.getWeight(erWeight50, weightFactors, CSI_CONFIGURATION)
        double weight70 = service.getWeight(erWeight70, weightFactors, CSI_CONFIGURATION)
        double noWeight = service.getWeight(erNoWeight, weightFactors, CSI_CONFIGURATION)

        then: "They are calculated correctly if PageWeight for respective PAGE exists. Otherwise weight is zero."
        that weight50, closeTo(0.5d, DELTA)
        that weight70, closeTo(0.7d, DELTA)
        that noWeight, closeTo(0d, DELTA)

    }

    void "Weight gets calculated correctly with PAGE as WeightFactor for CsiAggregations."() {

        given: "PAGE WeightFactor and some CsiAggregations in place."
        Set<WeightFactor> weightFactors = [WeightFactor.PAGE] as Set
        CsiAggregation caWeight50 = CsiAggregation.build(page: PAGE_WEIGHTED_50)
        CsiAggregation caWeight70 = CsiAggregation.build(page: PAGE_WEIGHTED_70)
        CsiAggregation caNoWeight = CsiAggregation.build(page: Page.build())

        when: "Weights get calculated."
        double weight50 = service.getWeight(caWeight50, weightFactors, CSI_CONFIGURATION)
        double weight70 = service.getWeight(caWeight70, weightFactors, CSI_CONFIGURATION)
        double noWeight = service.getWeight(caNoWeight, weightFactors, CSI_CONFIGURATION)

        then: "They are calculated correctly if PageWeight for respective PAGE exists. Otherwise weight is zero."
        that weight50, closeTo(0.5d, DELTA)
        that weight70, closeTo(0.7d, DELTA)
        that noWeight, closeTo(0d, DELTA)

    }

    void "Weight gets calculated correctly with multiple WeightFactors for EventResults."() {

        given:
        Set<WeightFactor> allWeightFactors = [WeightFactor.HOUROFDAY, WeightFactor.PAGE, WeightFactor.BROWSER_CONNECTIVITY_COMBINATION] as Set
        Set<WeightFactor> pageAndBrConnWeightFactors = [WeightFactor.PAGE, WeightFactor.BROWSER_CONNECTIVITY_COMBINATION] as Set
        Set<WeightFactor> hourOfDayAndBrConnWeightFactors = [WeightFactor.HOUROFDAY, WeightFactor.BROWSER_CONNECTIVITY_COMBINATION] as Set
        CsiValue erWeightFiftyTwoAm = EventResult.build(
            browser: BROWSER_WEIGHTED_50,
            page: PAGE_WEIGHTED_50,
            jobResultDate: MAPPED_TO_TWO_A_CLOCK_AM,
            connectivityProfile: CONNECTIVITY_WEIGHTED_50,
            jobGroup:  jobGroup1
        )
        CsiValue erWeightSeventyFivePm = EventResult.build(
            browser: BROWSER_WEIGHTED_70,
            page: PAGE_WEIGHTED_70,
            jobResultDate: MAPPED_TO_FIVE_A_CLOCK_PM,
            connectivityProfile: CONNECTIVITY_WEIGHTED_70,
            jobGroup: jobGroup2
        )

        when:
        Double weight = service.getWeight(erWeightFiftyTwoAm, allWeightFactors, CSI_CONFIGURATION)
        double hourofdayWeight = 0.2d
        double browserConnectivityWeight = 0.5d
        double pageWeight = 0.5d
        double expectedWeight = hourofdayWeight * browserConnectivityWeight * pageWeight
        then:
        that weight, closeTo(expectedWeight, DELTA)

        when:
        weight = service.getWeight(erWeightSeventyFivePm, allWeightFactors, CSI_CONFIGURATION)
        hourofdayWeight = 7.3d
        browserConnectivityWeight = 0.7d
        pageWeight = 0.7d
        expectedWeight = hourofdayWeight * browserConnectivityWeight * pageWeight
        then:
        that weight, closeTo(expectedWeight, DELTA)

        when:
        weight = service.getWeight(erWeightSeventyFivePm, pageAndBrConnWeightFactors, CSI_CONFIGURATION)
        browserConnectivityWeight = 0.7d
        pageWeight = 0.7d
        expectedWeight = browserConnectivityWeight * pageWeight
        then:
        that weight, closeTo(expectedWeight, DELTA)

        when:
        weight = service.getWeight(erWeightSeventyFivePm, hourOfDayAndBrConnWeightFactors, CSI_CONFIGURATION)
        hourofdayWeight = 7.3d
        browserConnectivityWeight = 0.7d
        expectedWeight = hourofdayWeight * browserConnectivityWeight
        then:
        that weight, closeTo(expectedWeight, DELTA)

    }

    void testGetWeightedCsiValuesFromEventResults() {
        // test specific data
        CsiValue eventResultWeightFiftyTwoAm = new EventResult(
                id: 1l,
                jobGroup: jobGroup1,
                csByWptDocCompleteInPercent: 10d,
                browser: BROWSER_WEIGHTED_50,
                page: PAGE_WEIGHTED_50,
                jobResultDate: MAPPED_TO_TWO_A_CLOCK_AM,
                docCompleteTimeInMillisecs: 1000,
                connectivityProfile: CONNECTIVITY_WEIGHTED_50).save(validate: false)
        CsiValue eventResultWeightSeventyFivePm = new EventResult(
                id: 2l,
                jobGroup: jobGroup2,
                csByWptDocCompleteInPercent: 20d,
                page: PAGE_WEIGHTED_70,
                browser: BROWSER_WEIGHTED_70,
                jobResultDate: MAPPED_TO_FIVE_A_CLOCK_PM,
                docCompleteTimeInMillisecs: 1000,
                connectivityProfile: CONNECTIVITY_WEIGHTED_70).save(validate: false)

        //tests with all three WeightFactors

        List<WeightedCsiValue> weightedValues = service.getWeightedCsiValues(
                [eventResultWeightFiftyTwoAm, eventResultWeightSeventyFivePm],
                [WeightFactor.HOUROFDAY, WeightFactor.PAGE, WeightFactor.BROWSER_CONNECTIVITY_COMBINATION] as Set, CSI_CONFIGURATION)

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

        weightedValues = service.getWeightedCsiValues(
                [eventResultWeightFiftyTwoAm, eventResultWeightSeventyFivePm],
                [WeightFactor.HOUROFDAY, WeightFactor.PAGE] as Set, CSI_CONFIGURATION)

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

        weightedValues = service.getWeightedCsiValues(
                [eventResultWeightFiftyTwoAm, eventResultWeightSeventyFivePm],
                [WeightFactor.BROWSER_CONNECTIVITY_COMBINATION, WeightFactor.PAGE] as Set, CSI_CONFIGURATION)

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

        weightedValues = service.getWeightedCsiValues(
                [eventResultWeightFiftyTwoAm, eventResultWeightSeventyFivePm],
                [WeightFactor.BROWSER_CONNECTIVITY_COMBINATION] as Set, CSI_CONFIGURATION)

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

        weightedValues = service.getWeightedCsiValues(
                [eventResultWeightFiftyTwoAm, eventResultWeightSeventyFivePm],
                [] as Set, CSI_CONFIGURATION)

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
                browser: BROWSER_WEIGHTED_50,
                page: PAGE_WEIGHTED_50,
                started: MAPPED_TO_TWO_A_CLOCK_AM,
                connectivityProfile: CONNECTIVITY_WEIGHTED_50,
                jobGroup: jobGroup1
        )
        csiAggregationWeightFiftyTwoAm.addAllToUnderlyingEventResultsByWptDocComplete([1l, 2l, 3l])
        csiAggregationWeightFiftyTwoAm.save(validate: false)
        TestDataUtil.createUpdateEvent(csiAggregationWeightFiftyTwoAm.ident(), CsiAggregationUpdateEvent.UpdateCause.CALCULATED)
        CsiAggregation csiAggregationWeightSeventyFivePm = new CsiAggregation(
                csByWptDocCompleteInPercent: 20d,
                browser: BROWSER_WEIGHTED_70,
                page: PAGE_WEIGHTED_70,
                started: MAPPED_TO_FIVE_A_CLOCK_PM,
                connectivityProfile: CONNECTIVITY_WEIGHTED_70,
                jobGroup: jobGroup1
        )
        csiAggregationWeightSeventyFivePm.addAllToUnderlyingEventResultsByWptDocComplete([4l, 5l, 6l])
        csiAggregationWeightSeventyFivePm.save(validate: false)
        TestDataUtil.createUpdateEvent(csiAggregationWeightSeventyFivePm.ident(), CsiAggregationUpdateEvent.UpdateCause.CALCULATED)

        //tests with all three WeightFactors
        List<WeightedCsiValue> weightedValues = service.getWeightedCsiValues(
                [csiAggregationWeightFiftyTwoAm, csiAggregationWeightSeventyFivePm],
                [WeightFactor.HOUROFDAY, WeightFactor.PAGE, WeightFactor.BROWSER_CONNECTIVITY_COMBINATION] as Set, CSI_CONFIGURATION)

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

        weightedValues = service.getWeightedCsiValues(
                [csiAggregationWeightFiftyTwoAm, csiAggregationWeightSeventyFivePm],
                [WeightFactor.HOUROFDAY, WeightFactor.PAGE] as Set, CSI_CONFIGURATION)

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

        weightedValues = service.getWeightedCsiValues(
                [csiAggregationWeightFiftyTwoAm, csiAggregationWeightSeventyFivePm],
                [WeightFactor.BROWSER_CONNECTIVITY_COMBINATION, WeightFactor.PAGE] as Set, CSI_CONFIGURATION)

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

        weightedValues = service.getWeightedCsiValues(
                [csiAggregationWeightFiftyTwoAm, csiAggregationWeightSeventyFivePm],
                [] as Set, CSI_CONFIGURATION)

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
                started: MAPPED_TO_TWO_A_CLOCK_AM,
                connectivityProfile: CONNECTIVITY_WEIGHTED_50,
        )
        csiAggregation1.addAllToUnderlyingEventResultsByWptDocComplete([1l, 2l, 3l])
        csiAggregation1.save(validate: false)
        TestDataUtil.createUpdateEvent(csiAggregation1.ident(), CsiAggregationUpdateEvent.UpdateCause.CALCULATED)
        CsiAggregation csiAggregation2 = new CsiAggregation(
                csByWptDocCompleteInPercent: 20d,
                jobGroup: jobGroup2,
                started: MAPPED_TO_FIVE_A_CLOCK_PM,
                connectivityProfile: CONNECTIVITY_WEIGHTED_70
        )
        csiAggregation2.addAllToUnderlyingEventResultsByWptDocComplete([4l, 5l, 6l])
        csiAggregation2.save(validate: false)
        TestDataUtil.createUpdateEvent(csiAggregation2.ident(), CsiAggregationUpdateEvent.UpdateCause.CALCULATED)

        //tests with all three WeightFactors
        List<WeightedCsiValue> weightedValues = service.getWeightedCsiValues(
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
                started: MAPPED_TO_TWO_A_CLOCK_AM,
                connectivityProfile: CONNECTIVITY_WEIGHTED_50,
                underlyingEventResultsByVisuallyComplete: "1L,2L",
                jobGroup: jobGroup1
        )
        CsiValue csiAggregationWithVisuallyComplete2 = new CsiAggregation(
                csByWptDocCompleteInPercent: 10d,
                csByWptVisuallyCompleteInPercent: 20d,
                started: MAPPED_TO_TWO_A_CLOCK_AM,
                connectivityProfile: CONNECTIVITY_WEIGHTED_50,
                underlyingEventResultsByVisuallyComplete: "1L,2L",
                jobGroup: jobGroup1
        )
        CsiValue csiAggregationWithVisuallyComplete3 = new CsiAggregation(
                csByWptDocCompleteInPercent: 10d,
                csByWptVisuallyCompleteInPercent: 20d,
                started: MAPPED_TO_TWO_A_CLOCK_AM,
                connectivityProfile: CONNECTIVITY_WEIGHTED_50,
                underlyingEventResultsByVisuallyComplete: "1L,2L",
                jobGroup: jobGroup2
        )
        CsiValue csiAggregationWithoutVisuallyComplete1 = new CsiAggregation(
                csByWptDocCompleteInPercent: 10d,
                started: MAPPED_TO_TWO_A_CLOCK_AM,
                connectivityProfile: CONNECTIVITY_WEIGHTED_50,
                underlyingEventResultsByVisuallyComplete: "1L,2L",
                jobGroup: jobGroup1
        )
        CsiValue csiAggregationWithoutVisuallyComplete2 = new CsiAggregation(
                csByWptDocCompleteInPercent: 10d,
                started: MAPPED_TO_TWO_A_CLOCK_AM,
                connectivityProfile: CONNECTIVITY_WEIGHTED_50,
                underlyingEventResultsByVisuallyComplete: "1L,2L",
                jobGroup: jobGroup2
        )

        // execution
        // test all underlying csiAggregations have csByWptVisuallyCompleteInPercent
        List<WeightedCsiValue> weightedCsiValuesAllHaveVisuallyComplete = service.getWeightedCsiValuesByVisuallyComplete([csiAggregationWithVisuallyComplete1, csiAggregationWithVisuallyComplete2, csiAggregationWithVisuallyComplete3], csiSystem)

        // test some underlying csiAggregations have csByWptVisuallyCompleteInPercent
        List<WeightedCsiValue> weightedCsiValuesSomeHaveVisuallyComplete = service.getWeightedCsiValuesByVisuallyComplete([csiAggregationWithVisuallyComplete1, csiAggregationWithoutVisuallyComplete1], csiSystem)

        // test no underlying csiAggregations have csByWptVisuallyCompleteInPercent
        List<WeightedCsiValue> weightedCsiValuesNoHaveVisuallyComplete = service.getWeightedCsiValuesByVisuallyComplete([csiAggregationWithoutVisuallyComplete1, csiAggregationWithoutVisuallyComplete2], csiSystem)

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
                browser: BROWSER_WEIGHTED_50,
                started: MAPPED_TO_TWO_A_CLOCK_AM,
                connectivityProfile: CONNECTIVITY_WEIGHTED_50,
                underlyingEventResultsByVisuallyComplete: "1L,2L",
                jobGroup: jobGroup1,
                page: PAGE_WEIGHTED_50
        )
        CsiValue csiAggregationWithVisuallyComplete2 = new CsiAggregation(
                csByWptDocCompleteInPercent: 10d,
                csByWptVisuallyCompleteInPercent: 20d,
                browser: BROWSER_WEIGHTED_50,
                started: MAPPED_TO_TWO_A_CLOCK_AM,
                connectivityProfile: CONNECTIVITY_WEIGHTED_50,
                underlyingEventResultsByVisuallyComplete: "1L,2L",
                jobGroup: jobGroup1,
                page: PAGE_WEIGHTED_50
        )
        CsiValue csiAggregationWithVisuallyComplete3 = new CsiAggregation(
                csByWptDocCompleteInPercent: 10d,
                csByWptVisuallyCompleteInPercent: 20d,
                browser: BROWSER_WEIGHTED_70,
                started: MAPPED_TO_TWO_A_CLOCK_AM,
                connectivityProfile: CONNECTIVITY_WEIGHTED_70,
                underlyingEventResultsByVisuallyComplete: "1L,2L",
                jobGroup: jobGroup1,
                page: PAGE_WEIGHTED_70
        )
        CsiValue csiAggregationWithoutVisuallyComplete1 = new CsiAggregation(
                csByWptDocCompleteInPercent: 10d,
                browser: BROWSER_WEIGHTED_50,
                started: MAPPED_TO_TWO_A_CLOCK_AM,
                connectivityProfile: CONNECTIVITY_WEIGHTED_50,
                underlyingEventResultsByVisuallyComplete: "1L,2L",
                jobGroup: jobGroup1,
                page: PAGE_WEIGHTED_50
        )
        CsiValue csiAggregationWithoutVisuallyComplete2 = new CsiAggregation(
                csByWptDocCompleteInPercent: 10d,
                browser: BROWSER_WEIGHTED_70,
                started: MAPPED_TO_TWO_A_CLOCK_AM,
                connectivityProfile: CONNECTIVITY_WEIGHTED_70,
                underlyingEventResultsByVisuallyComplete: "1L,2L",
                jobGroup: jobGroup1,
                page: PAGE_WEIGHTED_70
        )


        // execution
        // test all underlying csiAggregations have csByWptVisuallyCompleteInPercent
        List<WeightedCsiValue> weightedCsiValuesAllHaveVisuallyComplete = service.getWeightedCsiValuesByVisuallyComplete([csiAggregationWithVisuallyComplete1, csiAggregationWithVisuallyComplete2, csiAggregationWithVisuallyComplete3], [WeightFactor.PAGE] as Set, CSI_CONFIGURATION)

        // test some underlying csiAggregations have csByWptVisuallyCompleteInPercent
        List<WeightedCsiValue> weightedCsiValuesSomeHaveVisuallyComplete = service.getWeightedCsiValuesByVisuallyComplete([csiAggregationWithVisuallyComplete1, csiAggregationWithoutVisuallyComplete1], [WeightFactor.PAGE] as Set, CSI_CONFIGURATION)

        // test no underlying csiAggregations have csByWptVisuallyCompleteInPercent
        List<WeightedCsiValue> weightedCsiValuesNoHaveVisuallyComplete = service.getWeightedCsiValuesByVisuallyComplete([csiAggregationWithoutVisuallyComplete1, csiAggregationWithoutVisuallyComplete2], [WeightFactor.PAGE] as Set, CSI_CONFIGURATION)

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
        List<WeightedCsiValue> flattened = service.flattenWeightedCsiValues(valuesToFlatten)
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
        List<WeightedCsiValue> flattened = service.flattenWeightedCsiValues(valuesToFlatten)

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
        service.csiValueService = Stub(CsiValueService){
            isCsiRelevant(_) >> true
        }
    }

    private createTestDataCommonToAllTests() {

        PAGE_WEIGHTED_50 = Page.build()
        PAGE_WEIGHTED_70 = Page.build()

        BROWSER_WEIGHTED_50 = Browser.build()
        BROWSER_WEIGHTED_70 = Browser.build()

        CONNECTIVITY_WEIGHTED_50 = ConnectivityProfile.build()
        CONNECTIVITY_WEIGHTED_50.connectivityProfileService = Mock(ConnectivityProfileService)
        CONNECTIVITY_WEIGHTED_70 = ConnectivityProfile.build()
        CONNECTIVITY_WEIGHTED_70.connectivityProfileService = Mock(ConnectivityProfileService)

        CSI_CONFIGURATION = CsiConfiguration.build(
            csiDay: CsiDay.build(
                    hour0Weight: 2.9d,
                    hour1Weight: 0.4d,
                    hour2Weight: 0.2d,
                    hour3Weight: 0.1d,
                    hour4Weight: 0.1d,
                    hour5Weight: 0.2d,
                    hour6Weight: 0.7d,
                    hour7Weight: 1.7d,
                    hour8Weight: 3.2d,
                    hour9Weight: 4.8d,
                    hour10Weight: 5.6d,
                    hour11Weight: 5.7d,
                    hour12Weight: 5.5d,
                    hour13Weight: 5.8d,
                    hour14Weight: 5.9d,
                    hour15Weight: 6.0d,
                    hour16Weight: 6.7d,
                    hour17Weight: 7.3d,
                    hour18Weight: 7.6d,
                    hour19Weight: 8.8d,
                    hour20Weight: 9.3d,
                    hour21Weight: 7.0d,
                    hour22Weight: 3.6d,
                    hour23Weight: 0.9d
            ),
            browserConnectivityWeights: [
                BrowserConnectivityWeight.build(
                    connectivity: CONNECTIVITY_WEIGHTED_50, browser: BROWSER_WEIGHTED_50, weight: 0.5d
                ),
                BrowserConnectivityWeight.build(
                    connectivity: CONNECTIVITY_WEIGHTED_70, browser: BROWSER_WEIGHTED_70, weight: 0.7d
                )
            ],
            pageWeights: [PageWeight.build(page: PAGE_WEIGHTED_50, weight: 0.5d), PageWeight.build(page: PAGE_WEIGHTED_70, weight: 0.7d)]
        )

        jobGroup1 = JobGroup.build(csiConfiguration: CSI_CONFIGURATION)
        jobGroup2 = JobGroup.build(csiConfiguration: CSI_CONFIGURATION)

//        csiSystem = CsiSystem.buildWithoutSave()
//            .addToJobGroupWeights(jobGroup: jobGroup1, weight: 0.5)
//            .addToJobGroupWeights(jobGroup: jobGroup2, weight: 2.0)
//            .save()

//        csiSystem = new CsiSystem(label: "csiSystem")
//        csiSystem.jobGroupWeights.add(new JobGroupWeight(jobGroup: jobGroup1, weight: 0.5, csiSystem: csiSystem))
//        csiSystem.jobGroupWeights.add(new JobGroupWeight(jobGroup: jobGroup2, weight: 2.0, csiSystem: csiSystem))
//        csiSystem.save(failOnError: true)
    }

}

package de.iteratec.osm.result

import de.iteratec.osm.csi.*
import de.iteratec.osm.csi.weighting.WeightedCsiValue
import de.iteratec.osm.csi.weighting.WeightedValue
import de.iteratec.osm.csi.weighting.WeightingService
import de.iteratec.osm.measurement.environment.Browser
import de.iteratec.osm.measurement.schedule.ConnectivityProfile
import de.iteratec.osm.measurement.schedule.ConnectivityProfileService
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.report.chart.CsiAggregation
import de.iteratec.osm.report.chart.CsiAggregationUpdateEvent
import de.iteratec.osm.util.PerformanceLoggingService
import grails.buildtestdata.BuildDataTest
import grails.buildtestdata.mixin.Build
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import org.joda.time.DateTime
import spock.lang.Specification
import spock.lang.Unroll

import static de.iteratec.osm.csi.weighting.WeightFactor.*
import static de.iteratec.osm.report.chart.CsiAggregationUpdateEvent.UpdateCause.CALCULATED
import static spock.util.matcher.HamcrestMatchers.closeTo
import static spock.util.matcher.HamcrestSupport.that

@TestFor(CsiValueService)
@Mock([EventResult, CsiAggregation, CsiAggregationUpdateEvent, BrowserConnectivityWeight, Browser, ConnectivityProfile,
        JobGroup, CsiDay, CsiConfiguration, CsiSystem])
@Build([EventResult, CsiAggregation, CsiConfiguration, Browser, Page, BrowserConnectivityWeight, PageWeight, JobGroup,
        CsiSystem, CsiDay, ConnectivityProfile, CsiAggregationUpdateEvent, JobGroupWeight])
@Unroll
class CsiValueProcessingSpec extends Specification implements BuildDataTest {

    CsiValueService serviceUnderTest

    private static final double DELTA = 1e-15

    static JobGroup JOB_GROUP_1, JOB_GROUP_2
    public static final BigDecimal WEIGHT_JOB_GROUP_1 = 0.5
    public static final BigDecimal WEIGHT_JOB_GROUP_2 = 2.0

    static final Date TWO_A_CLOCK_AM = new DateTime(2014, 1, 1, 2, 30, 12).toDate()
    static final Date FIVE_A_CLOCK_PM = new DateTime(2014, 1, 1, 17, 43, 56).toDate()

    static Page PAGE_WEIGHTED_50
    static Page PAGE_WEIGHTED_70
    static Browser BROWSER_WEIGHTED_50
    static Browser BROWSER_WEIGHTED_70
    static ConnectivityProfile CONNECTIVITY_WEIGHTED_50
    static ConnectivityProfile CONNECTIVITY_WEIGHTED_70

    static CsiConfiguration CSI_CONFIGURATION
    static CsiSystem CSI_SYSTEM

    def doWithSpring = {
        performanceLoggingService(PerformanceLoggingService)
        weightingService(WeightingService)
    }

    void setup() {

        serviceUnderTest = service

        mocksCommonToAllTests()
        createTestDataCommonToAllTests()
        initSpringBeans()

    }

    void "Method getWeightedCsiValues works as expected without any WeightFactors for EventResults."() {

        given: "A List of two different EventResults."
        long eventResult1_id = 1l
        double eventResult1_docComplete = 10d
        CsiValue eventResult1 = EventResult.build(
                id: eventResult1_id,
                jobGroup: JOB_GROUP_1,
                csByWptDocCompleteInPercent: eventResult1_docComplete,
                browser: BROWSER_WEIGHTED_50,
                page: PAGE_WEIGHTED_50,
                jobResultDate: TWO_A_CLOCK_AM,
                connectivityProfile: CONNECTIVITY_WEIGHTED_50).save(validate: false)
        long eventResult2_id = 2l
        double eventResult2_docComplete = 20d
        CsiValue eventResult2 = EventResult.build(
                id: eventResult2_id,
                jobGroup: JOB_GROUP_1,
                csByWptDocCompleteInPercent: eventResult2_docComplete,
                page: PAGE_WEIGHTED_70,
                browser: BROWSER_WEIGHTED_70,
                jobResultDate: FIVE_A_CLOCK_PM,
                connectivityProfile: CONNECTIVITY_WEIGHTED_70).save(validate: false)
        double expectedWeightWithoutFactors = 1d
        double expectedValue = (eventResult1_docComplete + eventResult2_docComplete) / 2

        when: "WeightedValues are calculated for that list."
        List<WeightedValue> weightedValues = serviceUnderTest.getWeightedCsiValues(
            [eventResult1, eventResult2],
            [] as Set,
            CSI_CONFIGURATION
        )

        then: "The resulting list of WeightedCsiValues is flattened to one single element, the weight is 1 and the value is the average of the two EventResults."
        weightedValues.size() == 1
        WeightedCsiValue flattenedCauseOfMissingWeightFactors = weightedValues[0]

        flattenedCauseOfMissingWeightFactors.underlyingEventResultIds == [eventResult1_id, eventResult2_id]
        that flattenedCauseOfMissingWeightFactors.weightedValue.value, closeTo(expectedValue, DELTA)
        that flattenedCauseOfMissingWeightFactors.weightedValue.weight, closeTo(expectedWeightWithoutFactors, DELTA)
    }

    void "Weight is calculated correctly for multiple EventResults using WeightFactors #weightFactors"() {

        given: "A List of two different EventResults."
        long eventResult1_id = 1l
        double eventResult1_docComplete = 10d
        CsiValue eventResult1 = EventResult.build(
                id: eventResult1_id,
                jobGroup: JOB_GROUP_1,
                csByWptDocCompleteInPercent: eventResult1_docComplete,
                browser: BROWSER_WEIGHTED_50,
                page: PAGE_WEIGHTED_50,
                jobResultDate: TWO_A_CLOCK_AM,
                connectivityProfile: CONNECTIVITY_WEIGHTED_50).save(validate: false)
        long eventResult2_id = 2l
        double eventResult2_docComplete = 20d
        CsiValue eventResult2 = EventResult.build(
                id: eventResult2_id,
                jobGroup: JOB_GROUP_1,
                csByWptDocCompleteInPercent: eventResult2_docComplete,
                page: PAGE_WEIGHTED_70,
                browser: BROWSER_WEIGHTED_70,
                jobResultDate: FIVE_A_CLOCK_PM,
                connectivityProfile: CONNECTIVITY_WEIGHTED_70).save(validate: false)

        when: "WeightedValues are calculated for that list."
        List<WeightedCsiValue> weightedValues = serviceUnderTest.getWeightedCsiValues(
            [eventResult1, eventResult2],
            weightFactors,
            CSI_CONFIGURATION
        )

        then: "The resulting list of WeightedCsiValues contains correctly weighted values."
        weightedValues.size() == 2
        weightedValues.findAll {
            it.underlyingEventResultIds == [eventResult1_id] &&
            it.weightedValue.value == eventResult1_docComplete &&
            it.weightedValue.weight == hourWeight_1 * brConnWeight_1 * pageWeight_1
        }.size() == 1
        weightedValues.findAll {
            it.underlyingEventResultIds == [eventResult2_id] &&
            it.weightedValue.value == eventResult2_docComplete &&
            it.weightedValue.weight == hourWeight_2 * brConnWeight_2 * pageWeight_2
        }.size() == 1

        where: "WeightFactors used for calculation of weights differ."
        weightFactors                                               || hourWeight_1 | pageWeight_1  | brConnWeight_1    | hourWeight_2  | pageWeight_2  | brConnWeight_2
        [HOUROFDAY, PAGE, BROWSER_CONNECTIVITY_COMBINATION] as Set  || 0.2d         | 0.5d          | 0.5d              | 7.3d          | 0.7d          | 0.7d
        [HOUROFDAY, PAGE] as Set                                    || 0.2d         | 0.5d          | 1d                | 7.3d          | 0.7d          | 1d
        [BROWSER_CONNECTIVITY_COMBINATION, PAGE] as Set             || 1d           | 0.5d          | 0.5d              | 1d            | 0.7d          | 0.7d
        [BROWSER_CONNECTIVITY_COMBINATION] as Set                   || 1d           | 1d            | 0.5d              | 1d            | 1d            | 0.7d

    }

    void "Method getWeightedCsiValues works as expected without any WeightFactors for CsiAggregations."() {

        given: "A list of two CsiAggregations."
        double csiAgg1_csByWpt = 10d
        List<Long> csiAgg1_underlyingErIds = [1l, 2l, 3l]
        CsiAggregation csiAggregation1 = CsiAggregation.build(
                csByWptDocCompleteInPercent: csiAgg1_csByWpt,
                browser: BROWSER_WEIGHTED_50,
                page: PAGE_WEIGHTED_50,
                started: TWO_A_CLOCK_AM,
                connectivityProfile: CONNECTIVITY_WEIGHTED_50,
                jobGroup: JOB_GROUP_1,
                underlyingEventResultsByWptDocComplete: ''
        )
        csiAggregation1.addAllToUnderlyingEventResultsByWptDocComplete(csiAgg1_underlyingErIds)
        CsiAggregationUpdateEvent.build(dateOfUpdate: new Date(), csiAggregationId: csiAggregation1.ident(), updateCause: CALCULATED)

        double csiAgg2_csByWpt = 20d
        List<Long> csiAgg2_underlyingErIds = [4l, 5l, 6l]
        CsiAggregation csiAggregation2 = CsiAggregation.build(
                csByWptDocCompleteInPercent: csiAgg2_csByWpt,
                browser: BROWSER_WEIGHTED_70,
                page: PAGE_WEIGHTED_70,
                started: FIVE_A_CLOCK_PM,
                connectivityProfile: CONNECTIVITY_WEIGHTED_70,
                jobGroup: JOB_GROUP_1,
                underlyingEventResultsByWptDocComplete: ''
        )
        csiAggregation2.addAllToUnderlyingEventResultsByWptDocComplete(csiAgg2_underlyingErIds)
        CsiAggregationUpdateEvent.build(dateOfUpdate: new Date(), csiAggregationId: csiAggregation2.ident(), updateCause: CALCULATED)

        double expectedWeightWithoutFactors = 1d
        List<Long> expectedUnderlyingResultIds = [1l, 2l, 3l, 4l, 5l, 6l]
        double expectedValue = (csiAgg1_csByWpt + csiAgg2_csByWpt) / 2

        when: "WeightedValues get calculated for that list."
        List<WeightedValue> weightedValues = serviceUnderTest.getWeightedCsiValues(
            [csiAggregation1, csiAggregation2],
            [] as Set,
            CSI_CONFIGURATION
        )

        then: "The resulting list of WeightedCsiValues is flattened to one single element, the weight is 1 and the value is the average of the two EventResults."
        weightedValues.size() == 1

        WeightedCsiValue flattenedCauseOfMissingWeightFactors = weightedValues[0]
        flattenedCauseOfMissingWeightFactors.underlyingEventResultIds == expectedUnderlyingResultIds
        that flattenedCauseOfMissingWeightFactors.weightedValue.value, closeTo(expectedValue, DELTA)
        that flattenedCauseOfMissingWeightFactors.weightedValue.weight, closeTo(expectedWeightWithoutFactors, DELTA)

    }

    void "Weight is calculated correctly for multiple CsiAggregations using WeightFactors #weightFactors"() {

        given: "A list of two CsiAggregations."
        double csiAgg1_csByWpt = 10d
        List<Long> csiAgg1_underlyingErIds = [1l, 2l, 3l]
        CsiAggregation csiAggregation1 = CsiAggregation.build(
            csByWptDocCompleteInPercent: csiAgg1_csByWpt,
            browser: BROWSER_WEIGHTED_50,
            page: PAGE_WEIGHTED_50,
            started: TWO_A_CLOCK_AM,
            connectivityProfile: CONNECTIVITY_WEIGHTED_50,
            jobGroup: JOB_GROUP_1,
            underlyingEventResultsByWptDocComplete: ''
        )
        csiAggregation1.addAllToUnderlyingEventResultsByWptDocComplete(csiAgg1_underlyingErIds)
        CsiAggregationUpdateEvent.build(dateOfUpdate: new Date(), csiAggregationId: csiAggregation1.ident(), updateCause: CALCULATED)

        double csiAgg2_csByWpt = 20d
        List<Long> csiAgg2_underlyingErIds = [4l, 5l, 6l]
        CsiAggregation csiAggregation2 = CsiAggregation.build(
            csByWptDocCompleteInPercent: csiAgg2_csByWpt,
            browser: BROWSER_WEIGHTED_70,
            page: PAGE_WEIGHTED_70,
            started: FIVE_A_CLOCK_PM,
            connectivityProfile: CONNECTIVITY_WEIGHTED_70,
            jobGroup: JOB_GROUP_1,
            underlyingEventResultsByWptDocComplete: ''
        )
        csiAggregation2.addAllToUnderlyingEventResultsByWptDocComplete(csiAgg2_underlyingErIds)
        CsiAggregationUpdateEvent.build(dateOfUpdate: new Date(), csiAggregationId: csiAggregation2.ident(), updateCause: CALCULATED)

        when: "WeightedValues get calculated for that list."
        List<WeightedCsiValue> weightedValues = serviceUnderTest.getWeightedCsiValues(
            [csiAggregation1, csiAggregation2],
            weightFactors,
            CSI_CONFIGURATION
        )

        then: "The resulting list of WeightedCsiValues contains correctly weighted values."
        weightedValues.size() == 2

        weightedValues.findAll {
            it.underlyingEventResultIds == csiAgg1_underlyingErIds &&
            it.weightedValue.value == csiAgg1_csByWpt &&
            it.weightedValue.weight == hourWeight_1 * brConnWeight_1 * pageWeight_1
        }.size() == 1

        weightedValues.findAll {
            it.underlyingEventResultIds == csiAgg2_underlyingErIds &&
            it.weightedValue.value == csiAgg2_csByWpt &&
            it.weightedValue.weight == hourWeight_2 * brConnWeight_2 * pageWeight_2
        }.size() == 1

        where: "WeightFactors used for calculation of weights differ."
        weightFactors                                               || hourWeight_1 | pageWeight_1  | brConnWeight_1    | hourWeight_2  | pageWeight_2  | brConnWeight_2
        [HOUROFDAY, PAGE, BROWSER_CONNECTIVITY_COMBINATION] as Set  || 0.2d         | 0.5d          | 0.5d              | 7.3d          | 0.7d          | 0.7d
        [HOUROFDAY, PAGE] as Set                                    || 0.2d         | 0.5d          | 1d                | 7.3d          | 0.7d          | 1d
        [BROWSER_CONNECTIVITY_COMBINATION, PAGE] as Set             || 1d           | 0.5d          | 0.5d              | 1d            | 0.7d          | 0.7d
        [BROWSER_CONNECTIVITY_COMBINATION] as Set                   || 1d           | 1d            | 0.5d              | 1d            | 1d            | 0.7d

    }

    void "Weight is calculated correctly for multiple CsiSystem CsiAggregations."() {

        given: "A list of two CsiSystem CsiAggregations."
        double csiAgg1_csByWpt = 10d
        List<Long> csiAgg1_underlyingErIds = [1l, 2l, 3l]
        CsiAggregation csiAggregation1 = CsiAggregation.build(
            jobGroup: JOB_GROUP_1,
            csByWptDocCompleteInPercent: csiAgg1_csByWpt,
            underlyingEventResultsByWptDocComplete: ''
        )
        csiAggregation1.addAllToUnderlyingEventResultsByWptDocComplete(csiAgg1_underlyingErIds)
        CsiAggregationUpdateEvent.build(dateOfUpdate: new Date(), csiAggregationId: csiAggregation1.ident(), updateCause: CALCULATED)

        double csiAgg2_csByWpt = 20d
        List<Long> csiAgg2_underlyingErIds = [4l, 5l, 6l]
        CsiAggregation csiAggregation2 = CsiAggregation.build(
            jobGroup: JOB_GROUP_2,
            csByWptDocCompleteInPercent: csiAgg2_csByWpt,
            underlyingEventResultsByWptDocComplete: ''
        )
        csiAggregation2.addAllToUnderlyingEventResultsByWptDocComplete(csiAgg2_underlyingErIds)
        CsiAggregationUpdateEvent.build(dateOfUpdate: new Date(), csiAggregationId: csiAggregation1.ident(), updateCause: CALCULATED)

        when: "WeightedValues get calculated for that list."
        List<WeightedCsiValue> weightedValues = serviceUnderTest.getWeightedCsiValues(
            [csiAggregation1, csiAggregation2],
            CSI_SYSTEM
        )

        then: "The resulting list of WeightedCsiValues contains correctly weighted values."
        weightedValues.size() == 2

        weightedValues.findAll {
            it.weightedValue.weight == WEIGHT_JOB_GROUP_1 &&
            it.weightedValue.value == csiAgg1_csByWpt &&
            it.underlyingEventResultIds == csiAgg1_underlyingErIds
        }.size() == 1
        weightedValues.findAll {
            it.weightedValue.weight == WEIGHT_JOB_GROUP_2 &&
            it.weightedValue.value == csiAgg2_csByWpt &&
            it.underlyingEventResultIds == csiAgg2_underlyingErIds
        }.size() == 1

    }

    void "Weight is calculated correctly ByVisuallyComplete for multiple CsiSystem CsiAggregations."() {

        given: "3 CsiAggregations with csByWptVisuallyCompleteInPercent, 2 for one JobGroup, 1 for another."
        CsiValue csiAggWithVisually1 = CsiAggregation.build(
                csByWptVisuallyCompleteInPercent: 10d,
                jobGroup: JOB_GROUP_1
        )
        CsiValue csiAggWithVisually2 = CsiAggregation.build(
                csByWptVisuallyCompleteInPercent: 20d,
                jobGroup: JOB_GROUP_1
        )
        double visuallyCompleteSingleCsiAgg = 30d
        CsiValue csiAggWithVisually3 = CsiAggregation.build(
                csByWptVisuallyCompleteInPercent: visuallyCompleteSingleCsiAgg,
                jobGroup: JOB_GROUP_2
        )
        double avgVisuallyCompleteTwoEquallyWeightedValues = (10.0 + 20.0) / 2

        when: "WeightedValues get calculated for these."
        List<WeightedCsiValue> weightedCsiValues = serviceUnderTest.getWeightedCsiValuesByVisuallyComplete(
            [csiAggWithVisually1, csiAggWithVisually2, csiAggWithVisually3],
            CSI_SYSTEM
        )

        then: "Equally weighted CsiAggregations (same JobGroup) get aggregated/flattened, third one not."
        weightedCsiValues.size() == 2
        weightedCsiValues.findAll {
            it.weightedValue.weight == WEIGHT_JOB_GROUP_1 &&
            it.weightedValue.value == avgVisuallyCompleteTwoEquallyWeightedValues
        }.size() == 1
        weightedCsiValues.findAll {
            it.weightedValue.weight == WEIGHT_JOB_GROUP_2 &&
            it.weightedValue.value == visuallyCompleteSingleCsiAgg
        }.size() == 1

    }

    void "Weight is calculated correctly ByVisuallyComplete for multiple CsiSystem CsiAggregations if not all have a csByVisuallyComplete."() {

        given:
        double csByVisuallyComplete = 10d
        List<Long> underlyingErIdsWithVisCompl = [1,2,3]
        CsiValue csiAggWithVisually = CsiAggregation.build(
                csByWptVisuallyCompleteInPercent: csByVisuallyComplete,
                jobGroup: JOB_GROUP_1
        )
        csiAggWithVisually.underlyingEventResultsByVisuallyComplete.addAll(underlyingErIdsWithVisCompl)

        List<Long> underlyingErIdsWithoutVisCompl = [4,5,6]
        CsiValue csiAggWithoutVisually = CsiAggregation.build(
                jobGroup: JOB_GROUP_1
        )
        csiAggWithoutVisually.underlyingEventResultsByVisuallyComplete.addAll(underlyingErIdsWithoutVisCompl)

        when:
        List<WeightedCsiValue> weightedCsiValues = serviceUnderTest.getWeightedCsiValuesByVisuallyComplete(
            [csiAggWithVisually, csiAggWithoutVisually],
            CSI_SYSTEM
        )

        then:
        weightedCsiValues.size() == 1
        weightedCsiValues[0].weightedValue.weight == WEIGHT_JOB_GROUP_1
        weightedCsiValues[0].weightedValue.value == csByVisuallyComplete
        weightedCsiValues[0].underlyingEventResultIds == underlyingErIdsWithVisCompl

    }

    void "No WeightedValues get calculated ByVisuallyComplete for multiple CsiSystem CsiAggregations if no one has csByVisuallyComplete."() {
        
        given: "2 CsiAggregation without csByWptVisuallyCompleteInPercent."
        CsiValue csiAggWithoutVisually1 = CsiAggregation.build(
            jobGroup: JOB_GROUP_1
        )
        CsiValue csiAggWithoutVisually2 = CsiAggregation.build(
            jobGroup: JOB_GROUP_1
        )

        when: "WeightedCsiValues get calculated for these."
        List<WeightedCsiValue> weightedCsiValues = serviceUnderTest.getWeightedCsiValuesByVisuallyComplete(
            [csiAggWithoutVisually1, csiAggWithoutVisually2],
            CSI_SYSTEM
        )

        then: "Returned list of WeightedCsiValue is empty because no CsiAggrgation had a csByWptVisuallyCompleteInPercent."
        weightedCsiValues.isEmpty()

    }

    void "Method to flatten WeightedCsiValues doesn't throw an Exception if given list is empty."() {

        given: "An empty list of WeightedCsiValues."
        List<WeightedCsiValue> valuesToFlatten = []

        when: "This empty list is flattened."
        List<WeightedCsiValue> flattened = serviceUnderTest.flattenWeightedCsiValues(valuesToFlatten)

        then: "Returned list is also empty."
        flattened.isEmpty()

    }

    void "Flattening of WeightedCsiValues aggregates correctly for values with same weight."() {

        given:
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

        BigDecimal avgAllValuesOfFirstWeight = (1 + 2 + 3) / 3
        BigDecimal avgAllValuesOfSecondWeight = (10 + 11 + 12 + 13 + 14) / 5
        BigDecimal avgAllValuesOfThirdWeight = (100 + 101) / 2

        when:
        List<WeightedCsiValue> flattened = serviceUnderTest.flattenWeightedCsiValues(valuesToFlatten)

        List<WeightedCsiValue> ofFirstWeight = flattened.findAll {
            Math.abs(it.weightedValue.weight - firstWeight) < DELTA
        }
        List<WeightedCsiValue> ofSecondWeight = flattened.findAll {
            Math.abs(it.weightedValue.weight - secondWeight) < DELTA
        }
        List<WeightedCsiValue> ofThirdWeight = flattened.findAll {
            Math.abs(it.weightedValue.weight - thirdWeight) < DELTA
        }

        then:
        flattened.size() == 3

        ofFirstWeight.size() == 1
        ofFirstWeight[0].underlyingEventResultIds == [1, 2, 3, 4, 5]
        that ofFirstWeight[0].weightedValue.value, closeTo(avgAllValuesOfFirstWeight, DELTA)

        ofSecondWeight.size() == 1
        ofSecondWeight[0].underlyingEventResultIds == [10, 11, 12, 13, 14, 15, 16, 17]
        that ofSecondWeight[0].weightedValue.value, closeTo (avgAllValuesOfSecondWeight, DELTA)

        ofThirdWeight.size() == 1
        ofThirdWeight[0].underlyingEventResultIds == [100, 101, 102, 103]
        that ofThirdWeight[0].weightedValue.value, closeTo(avgAllValuesOfThirdWeight, DELTA)
    }

    private void mocksCommonToAllTests() {
        serviceUnderTest = Spy(CsiValueService){
            isCsiRelevant(_) >> true //not the concern of these tests
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

        JOB_GROUP_1 = JobGroup.build(csiConfiguration: CSI_CONFIGURATION)
        JOB_GROUP_2 = JobGroup.build(csiConfiguration: CSI_CONFIGURATION)

        CSI_SYSTEM = CsiSystem.buildWithoutSave()
        CSI_SYSTEM.addToJobGroupWeights(jobGroup: JOB_GROUP_1, weight: WEIGHT_JOB_GROUP_1)
        CSI_SYSTEM.addToJobGroupWeights(jobGroup: JOB_GROUP_2, weight: WEIGHT_JOB_GROUP_2)
        CSI_SYSTEM.save()

    }

    /**
     * Necessary only if serviceUnderTest variable is used instead of injected service variable. Otherwise static
     * doWithSpring block suffices.
     * serviceUnderTest variable is used here because some inner methods of service under test should be stubbed and that
     * is only possible with a Spy. Because the injected service instance is readonly it can't be used for a Spock Spy.
     */
    private initSpringBeans() {
        serviceUnderTest.performanceLoggingService = grailsApplication.getMainContext().getBean('performanceLoggingService')
        serviceUnderTest.weightingService = grailsApplication.getMainContext().getBean('weightingService')
        serviceUnderTest.weightingService.performanceLoggingService = grailsApplication.getMainContext().getBean('performanceLoggingService')
    }
}

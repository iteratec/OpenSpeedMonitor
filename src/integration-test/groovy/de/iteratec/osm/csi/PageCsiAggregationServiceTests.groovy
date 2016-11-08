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
import de.iteratec.osm.measurement.environment.WebPageTestServer
import de.iteratec.osm.measurement.schedule.ConnectivityProfile
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.report.chart.*
import de.iteratec.osm.result.EventResult
import de.iteratec.osm.result.MeasuredEvent
import grails.test.mixin.integration.Integration
import grails.transaction.Rollback
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import spock.util.mop.ConfineMetaClassChanges

import static org.junit.Assert.*

/**
 * Test-suite of {@link PageCsiAggregationService}.
 */

@Integration
@Rollback
@ConfineMetaClassChanges([WeightingService])
class PageCsiAggregationServiceTests extends NonTransactionalIntegrationSpec {

    static final double DELTA = 1e-15

    CsiAggregationInterval weeklyInterval, dailyInterval, hourlyInterval
    JobGroup jobGroup1, jobGroup2, jobGroup3
    Page page1, page2, page3
    AggregatorType pageAggregator;
    Browser browser;
    ConnectivityProfile connectivityProfile
    MeasuredEvent measuredEvent1
    Location location1

    DateTime startDate = new DateTime(2013, 5, 16, 0, 0, 0)
    String jobGroupName1 = 'myJobGroup1'
    String jobGroupName2 = 'myJobGroup2'
    String jobGroupName3 = 'myJobGroup3'
    String pageName1 = 'myPageName1'
    String pageName2 = 'myPageName2'
    String pageName3 = 'myPageName3'

    PageCsiAggregationService pageCsiAggregationService

    def setup() {
        createTestDataForAllTests()
    }

    def cleanup() {
        // undo mocked service
        pageCsiAggregationService.weightingService = grailsApplication.mainContext.getBean('weightingService')
    }

    //tests////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    void "test findAllByJobGroupsAndPages"() {
        when:
        JobGroup group1 = JobGroup.findByName(jobGroupName1)
        JobGroup group2 = JobGroup.findByName(jobGroupName2)
        JobGroup group3 = JobGroup.findByName(jobGroupName3)
        Page page1 = Page.findByName(pageName1)
        Page page2 = Page.findByName(pageName2)
        Page page3 = Page.findByName(pageName3)

        Integer with_group1to3_page1to3 = 5
        Integer with_group1_page1to3 = 2
        Integer with_group2_page1to3 = 0
        Integer with_group3_page1to3 = 3
        Integer with_group1to3_page1 = 2
        Integer with_group1to3_page2 = 2
        Integer with_group1to3_page3 = 1
        Integer with_group1to2_page3 = 0
        Integer with_group1to2_page1 = 1
        Integer with_group3_page1or3 = 2

        List<JobGroup> group1To3 = [group1, group2, group3]
        List<JobGroup> group1To2 = [group1, group2]
        List<Page> page1To3 = [page1, page2, page3]
        List<Page> page1Or3 = [page1, page3]

        then:
        with_group1to3_page1to3 == pageCsiAggregationService.findAll(startDate.toDate(), startDate.toDate(), weeklyInterval, group1To3, page1To3).size()
        with_group1_page1to3 == pageCsiAggregationService.findAll(startDate.toDate(), startDate.toDate(), weeklyInterval, [group1], page1To3).size()
        with_group2_page1to3 == pageCsiAggregationService.findAll(startDate.toDate(), startDate.toDate(), weeklyInterval, [group2], page1To3).size()
        with_group3_page1to3 == pageCsiAggregationService.findAll(startDate.toDate(), startDate.toDate(), weeklyInterval, [group3], page1To3).size()
        with_group1to3_page1 == pageCsiAggregationService.findAll(startDate.toDate(), startDate.toDate(), weeklyInterval, group1To3, [page1]).size()
        with_group1to3_page2 == pageCsiAggregationService.findAll(startDate.toDate(), startDate.toDate(), weeklyInterval, group1To3, [page2]).size()
        with_group1to3_page3 == pageCsiAggregationService.findAll(startDate.toDate(), startDate.toDate(), weeklyInterval, group1To3, [page3]).size()
        with_group1to2_page3 == pageCsiAggregationService.findAll(startDate.toDate(), startDate.toDate(), weeklyInterval, group1To2, [page3]).size()
        with_group1to2_page1 == pageCsiAggregationService.findAll(startDate.toDate(), startDate.toDate(), weeklyInterval, group1To2, [page2]).size()
        with_group3_page1or3 == pageCsiAggregationService.findAll(startDate.toDate(), startDate.toDate(), weeklyInterval, [group3], page1Or3).size()
    }

    /**
     * Tests the marking of dependent (weekly-pageAggregator-){@link CsiAggregation}s, which aren't calculated when new {@link EventResult}s get persisted.
     * {@link de.iteratec.osm.report.chart.CsiAggregationUpdateEvent}s with {@link CsiAggregationUpdateEvent#UpdateCause} OUTDATED should be written to db.
     */
    void "test markingWeeklyPageMvsAsOutdated"() {
        given:
        EventResult eventResult = TestDataUtil.createEventResult(jobGroup1, measuredEvent1, page1, browser, location1)
        DateTime startTimeOfWeek = new DateTime(2013, 8, 5, 0, 0, 0, DateTimeZone.UTC)

        List<CsiAggregation> wpmvs = pageCsiAggregationService.findAll(startTimeOfWeek.toDate(), startTimeOfWeek.toDate(), weeklyInterval, JobGroup.list(), Page.list())
        assertEquals(0, wpmvs.size())

        when:
        pageCsiAggregationService.markMvAsOutdated(startTimeOfWeek, eventResult, weeklyInterval)
        wpmvs = pageCsiAggregationService.findAll(startTimeOfWeek.toDate(), startTimeOfWeek.toDate(), weeklyInterval, JobGroup.list(), Page.list())
        CsiAggregation wpmvMarked = wpmvs[0]

        then:
        assertEquals(1, wpmvs.size())
        assertFalse(wpmvMarked.isCalculated())
        assertTrue(wpmvMarked.hasToBeCalculated())

        when: "second mark of the same weekly-pageAggregator-mv"
        pageCsiAggregationService.markMvAsOutdated(startTimeOfWeek, eventResult, weeklyInterval)
        wpmvs = pageCsiAggregationService.findAll(startTimeOfWeek.toDate(), startTimeOfWeek.toDate(), weeklyInterval, JobGroup.list(), Page.list())
        wpmvMarked = wpmvs[0]

        then:
        assertEquals(1, wpmvs.size())
        assertFalse(wpmvMarked.isCalculated())
        assertTrue(wpmvMarked.hasToBeCalculated())
    }

    /**
     * Tests the marking of dependent (weekly-pageAggregator-){@link CsiAggregation}s, which aren't calculated when new {@link EventResult}s get persisted.
     * {@link de.iteratec.osm.report.chart.CsiAggregationUpdateEvent}s with {@link CsiAggregationUpdateEvent#UpdateCause} OUTDATED should be written to db.
     */
    void "test markingDailyPageMvsAsOutdated"() {
        given:
        EventResult eventResult = TestDataUtil.createEventResult(jobGroup1, measuredEvent1, page1, browser, location1)
        DateTime startTime = new DateTime(2013, 8, 5, 0, 0, 0, DateTimeZone.UTC)

        List<CsiAggregation> mvs = pageCsiAggregationService.findAll(startTime.toDate(), startTime.toDate(), dailyInterval, JobGroup.list(), Page.list())
        assertEquals(0, mvs.size())

        when:
        pageCsiAggregationService.markMvAsOutdated(startTime, eventResult, dailyInterval)
        mvs = pageCsiAggregationService.findAll(startTime.toDate(), startTime.toDate(), dailyInterval, JobGroup.list(), Page.list())
        CsiAggregation wpmvMarked = mvs[0]

        then:
        assertEquals(1, mvs.size())
        assertFalse(wpmvMarked.isCalculated())
        assertTrue(wpmvMarked.hasToBeCalculated())

        when: "second mark of the same weekly-pageAggregator-mv"
        pageCsiAggregationService.markMvAsOutdated(startTime, eventResult, dailyInterval)
        mvs = pageCsiAggregationService.findAll(startTime.toDate(), startTime.toDate(), dailyInterval, JobGroup.list(), Page.list())
        wpmvMarked = mvs[0]
        then:
        assertEquals(1, mvs.size())
        assertFalse(wpmvMarked.isCalculated())
        assertTrue(wpmvMarked.hasToBeCalculated())

    }

    /**
     * Tests calculation of daily-pageAggregator-{@link CsiAggregation}s, which aren't calculated when new {@link EventResult}s get persisted.
     */
    void "test calculation_DailyInterval_SingleHourlyMv"() {
        given:
        DateTime startedTime = new DateTime(2013, 5, 16, 12, 12, 11)
        MeasuredEvent measuredEvent = new MeasuredEvent(testedPage: page1, name: "measuredEvent").save(failOnError: true)

        AggregatorType measuredEventAggregator = AggregatorType.findByName(AggregatorType.MEASURED_EVENT)
        CsiAggregation hpmv = new CsiAggregation(interval: hourlyInterval, aggregator: measuredEventAggregator, jobGroup: jobGroup1, measuredEvent: measuredEvent, page: page1, browser: browser, location: location1, started: startedTime.plusMinutes(2).toDate(), underlyingEventResultsByWptDocComplete: "1", csByWptDocCompleteInPercent: 12d, connectivityProfile: connectivityProfile).save(failOnError: true);
        TestDataUtil.createUpdateEvent(hpmv.ident(), CsiAggregationUpdateEvent.UpdateCause.CALCULATED)

        List<CsiAggregation> mvs = pageCsiAggregationService.findAll(startedTime.toDate(), startedTime.toDate(), dailyInterval, JobGroup.list(), Page.list())
        assertEquals(0, mvs.size())

        List<WeightedCsiValue> weightedCsiValuesToReturnInMock = [
                new WeightedCsiValue(weightedValue: new WeightedValue(value: 12d, weight: 1d), underlyingEventResultIds: [])]

        //mocking inner services

        mockWeightingService(weightedCsiValuesToReturnInMock, [])

        when:
        List<CsiAggregation> calculatedMvs = pageCsiAggregationService.getOrCalculatePageCsiAggregations(startedTime.toDate(), startedTime.toDate(), dailyInterval, [jobGroup1], [page1])
        mvs = pageCsiAggregationService.findAll(startedTime.toDate(), startedTime.toDate(), dailyInterval, JobGroup.list(), Page.list())

        then:
        assertEquals(1, calculatedMvs.size())
        assertTrue(calculatedMvs[0].isCalculated())
        assertEquals(12d, calculatedMvs[0].csByWptDocCompleteInPercent, DELTA)
        assertEquals(1, mvs.size())
    }

    /**
     * Tests calculation of daily-pageAggregator-{@link CsiAggregation}s, which aren't calculated when new {@link EventResult}s get persisted.
     */
    void "test calculation_DailyInterval_MultipleHourlyMv"() {
        given:
        DateTime startedTime = new DateTime(2013, 5, 16, 12, 12, 11)
        CsiAggregation mv1 = new CsiAggregation(interval: hourlyInterval, aggregator: AggregatorType.findByName(AggregatorType.MEASURED_EVENT), jobGroup: jobGroup1, page: page1, measuredEvent: measuredEvent1, browser: browser, location: location1, started: startedTime.plusMinutes(2).toDate(), underlyingEventResultsByWptDocComplete: "1", csByWptDocCompleteInPercent: 12d, connectivityProfile: connectivityProfile).save(failOnError: true);
        TestDataUtil.createUpdateEvent(mv1.ident(), CsiAggregationUpdateEvent.UpdateCause.CALCULATED)
        CsiAggregation mv2 = new CsiAggregation(interval: hourlyInterval, aggregator: AggregatorType.findByName(AggregatorType.MEASURED_EVENT), jobGroup: jobGroup1, page: page1, measuredEvent: measuredEvent1, browser: browser, location: location1, started: startedTime.plusHours(2).toDate(), underlyingEventResultsByWptDocComplete: "1", csByWptDocCompleteInPercent: 10d, connectivityProfile: connectivityProfile).save(failOnError: true);
        TestDataUtil.createUpdateEvent(mv2.ident(), CsiAggregationUpdateEvent.UpdateCause.CALCULATED)
        CsiAggregation mv3 = new CsiAggregation(interval: hourlyInterval, aggregator: AggregatorType.findByName(AggregatorType.MEASURED_EVENT), jobGroup: jobGroup1, page: page1, measuredEvent: measuredEvent1, browser: browser, location: location1, started: startedTime.plusHours(10).toDate(), underlyingEventResultsByWptDocComplete: "1", csByWptDocCompleteInPercent: 11d, connectivityProfile: connectivityProfile).save(failOnError: true);
        TestDataUtil.createUpdateEvent(mv3.ident(), CsiAggregationUpdateEvent.UpdateCause.CALCULATED)

        //MVs outside of Interval
        CsiAggregation mv4 = new CsiAggregation(interval: hourlyInterval, aggregator: AggregatorType.findByName(AggregatorType.MEASURED_EVENT), jobGroup: jobGroup1, page: page1, measuredEvent: measuredEvent1, browser: browser, location: location1, started: startedTime.minusMinutes(1).toDate(), underlyingEventResultsByWptDocComplete: "1", csByWptDocCompleteInPercent: 1000d, connectivityProfile: connectivityProfile).save(failOnError: true);
        TestDataUtil.createUpdateEvent(mv4.ident(), CsiAggregationUpdateEvent.UpdateCause.CALCULATED)
        CsiAggregation mv5 = new CsiAggregation(interval: hourlyInterval, aggregator: AggregatorType.findByName(AggregatorType.MEASURED_EVENT), jobGroup: jobGroup1, page: page1, measuredEvent: measuredEvent1, browser: browser, location: location1, started: startedTime.plusMinutes(CsiAggregationInterval.DAILY + 1).toDate(), underlyingEventResultsByWptDocComplete: "1", csByWptDocCompleteInPercent: 1000d, connectivityProfile: connectivityProfile).save(failOnError: true);
        TestDataUtil.createUpdateEvent(mv5.ident(), CsiAggregationUpdateEvent.UpdateCause.CALCULATED)

        //mocking inner services
        List<WeightedCsiValue> weightedCsiValuesToReturnInMock = [
                new WeightedCsiValue(weightedValue: new WeightedValue(value: 12d, weight: 1d), underlyingEventResultIds: []),
                new WeightedCsiValue(weightedValue: new WeightedValue(value: 10d, weight: 1d), underlyingEventResultIds: []),
                new WeightedCsiValue(weightedValue: new WeightedValue(value: 11d, weight: 1d), underlyingEventResultIds: [])]
        mockWeightingService(weightedCsiValuesToReturnInMock, [])

        //precondition

        List<CsiAggregation> mvs = pageCsiAggregationService.findAll(startedTime.toDate(), startedTime.toDate(), dailyInterval, JobGroup.list(), Page.list())
        assertEquals(0, mvs.size())

        when:
        List<CsiAggregation> calculatedMvs = pageCsiAggregationService.getOrCalculatePageCsiAggregations(startedTime.toDate(), startedTime.toDate(), dailyInterval, [jobGroup1], [page1])
        mvs = pageCsiAggregationService.findAll(startedTime.toDate(), startedTime.toDate(), dailyInterval, JobGroup.list(), Page.list())
        CsiAggregation wpmvMarked = mvs[0]

        then:
        assertEquals(1, calculatedMvs.size())
        assertTrue(calculatedMvs[0].isCalculated())
        assertEquals(11d, calculatedMvs[0].csByWptDocCompleteInPercent, DELTA)
        assertEquals(1, mvs.size())
        assertTrue(wpmvMarked.isCalculated())

    }

    /**
     * Tests calculation of daily-pageAggregator-{@link CsiAggregation}s, which aren't calculated when new {@link EventResult}s get persisted.
     */
    void "test calculation_DailyInterval_MultipleHourlyMv_differentWeights"() {

        given:
        DateTime startedTime = new DateTime(2013, 5, 16, 12, 12, 11)

        CsiAggregation mv1 = new CsiAggregation(interval: hourlyInterval, aggregator: AggregatorType.findByName(AggregatorType.MEASURED_EVENT), jobGroup: jobGroup1, page: page1, measuredEvent: measuredEvent1, browser: browser, location: location1, started: startedTime.toDate(), underlyingEventResultsByWptDocComplete: "1", csByWptDocCompleteInPercent: 1d, connectivityProfile: connectivityProfile).save(failOnError: true)
        TestDataUtil.createUpdateEvent(mv1.ident(), CsiAggregationUpdateEvent.UpdateCause.CALCULATED)
        CsiAggregation mv2 = new CsiAggregation(interval: hourlyInterval, aggregator: AggregatorType.findByName(AggregatorType.MEASURED_EVENT), jobGroup: jobGroup1, page: page1, measuredEvent: measuredEvent1, browser: browser, location: location1, started: startedTime.plusHours(2).toDate(), underlyingEventResultsByWptDocComplete: "1", csByWptDocCompleteInPercent: 10d, connectivityProfile: connectivityProfile).save(failOnError: true)
        TestDataUtil.createUpdateEvent(mv2.ident(), CsiAggregationUpdateEvent.UpdateCause.CALCULATED)
        CsiAggregation mv3 = new CsiAggregation(interval: hourlyInterval, aggregator: AggregatorType.findByName(AggregatorType.MEASURED_EVENT), jobGroup: jobGroup1, page: page1, measuredEvent: measuredEvent1, browser: browser, location: location1, started: startedTime.plusHours(10).toDate(), underlyingEventResultsByWptDocComplete: "1", csByWptDocCompleteInPercent: 11d, connectivityProfile: connectivityProfile).save(failOnError: true)
        TestDataUtil.createUpdateEvent(mv3.ident(), CsiAggregationUpdateEvent.UpdateCause.CALCULATED)

        //MVs outside of Interval
        CsiAggregation mv4 = new CsiAggregation(interval: hourlyInterval, aggregator: AggregatorType.findByName(AggregatorType.MEASURED_EVENT), jobGroup: jobGroup1, page: page1, measuredEvent: measuredEvent1, browser: browser, location: location1, started: startedTime.minusMinutes(1).toDate(), underlyingEventResultsByWptDocComplete: "1", csByWptDocCompleteInPercent: 1000d, connectivityProfile: connectivityProfile).save(failOnError: true)
        TestDataUtil.createUpdateEvent(mv4.ident(), CsiAggregationUpdateEvent.UpdateCause.CALCULATED)
        CsiAggregation mv5 = new CsiAggregation(interval: hourlyInterval, aggregator: AggregatorType.findByName(AggregatorType.MEASURED_EVENT), jobGroup: jobGroup1, page: page1, measuredEvent: measuredEvent1, browser: browser, location: location1, started: startedTime.plusMinutes(CsiAggregationInterval.DAILY + 1).toDate(), underlyingEventResultsByWptDocComplete: "1", csByWptDocCompleteInPercent: 1000d, connectivityProfile: connectivityProfile).save(failOnError: true)
        TestDataUtil.createUpdateEvent(mv5.ident(), CsiAggregationUpdateEvent.UpdateCause.CALCULATED)

        //mocking inner services
        List<WeightedCsiValue> weightedCsiValuesToReturnInMock = [
                new WeightedCsiValue(weightedValue: new WeightedValue(value: 1d, weight: 10d), underlyingEventResultIds: []),
                new WeightedCsiValue(weightedValue: new WeightedValue(value: 10d, weight: 1d), underlyingEventResultIds: []),
                new WeightedCsiValue(weightedValue: new WeightedValue(value: 11d, weight: 1d), underlyingEventResultIds: [])]
        mockWeightingService(weightedCsiValuesToReturnInMock, [])

        //precondition
        List<CsiAggregation> mvs = pageCsiAggregationService.findAll(startedTime.toDate(), startedTime.toDate(), dailyInterval, JobGroup.list(), Page.list())
        assertEquals(0, mvs.size())

        double browserWeightAllMvs = 1d
        double valueFirstMv = 1d
        double hourofdayWeightFirstMv = 10d
        double valueSecondMv = 10d
        double hourofdayWeightSecondMv = 1d
        double valueThirdMv = 11d
        double hourofdayWeightThirdMv = 1d
        double sumOfAllWeights = 12d

        when:
        List<CsiAggregation> calculatedMvs = pageCsiAggregationService.getOrCalculatePageCsiAggregations(startedTime.toDate(), startedTime.toDate(), dailyInterval, [jobGroup1], [page1])
        mvs = pageCsiAggregationService.findAll(startedTime.toDate(), startedTime.toDate(), dailyInterval, JobGroup.list(), Page.list())
        CsiAggregation wpmvMarked = mvs[0]

        then:
        assertEquals(1, calculatedMvs.size())
        assertTrue(calculatedMvs[0].isCalculated())
        assertEquals(
                ((valueFirstMv * hourofdayWeightFirstMv * browserWeightAllMvs) + (valueSecondMv * hourofdayWeightSecondMv * browserWeightAllMvs) + (valueThirdMv * hourofdayWeightThirdMv * browserWeightAllMvs)) / sumOfAllWeights,
                calculatedMvs[0].csByWptDocCompleteInPercent,
                DELTA
        );

        assertEquals(1, mvs.size())
        assertTrue(wpmvMarked.isCalculated())

    }

    /**
     * Tests calculation of daily-pageAggregator-{@link CsiAggregation}s, which aren't calculated when new {@link EventResult}s get persisted.
     */
    void "test calculation_DailyInterval_MultipleHourlyMv_YesCalculatedNoData"() {
        DateTime startedTime = new DateTime(2013, 5, 16, 12, 12, 11)

        //MVs outside of Interval
        CsiAggregation mv1 = new CsiAggregation(interval: hourlyInterval, aggregator: AggregatorType.findByName(AggregatorType.MEASURED_EVENT), jobGroup: jobGroup1, page: page1, measuredEvent: measuredEvent1, browser: browser, location: location1, started: startedTime.minusMinutes(1).toDate(), underlyingEventResultsByWptDocComplete: "1", csByWptDocCompleteInPercent: 1000d, connectivityProfile: connectivityProfile).save(failOnError: true);
        TestDataUtil.createUpdateEvent(mv1.ident(), CsiAggregationUpdateEvent.UpdateCause.CALCULATED)
        CsiAggregation mv2 = new CsiAggregation(interval: hourlyInterval, aggregator: AggregatorType.findByName(AggregatorType.MEASURED_EVENT), jobGroup: jobGroup1, page: page1, measuredEvent: measuredEvent1, browser: browser, location: location1, started: startedTime.plusMinutes(CsiAggregationInterval.DAILY + 1).toDate(), underlyingEventResultsByWptDocComplete: "1", csByWptDocCompleteInPercent: 1000d, connectivityProfile: connectivityProfile).save(failOnError: true);
        TestDataUtil.createUpdateEvent(mv2.ident(), CsiAggregationUpdateEvent.UpdateCause.CALCULATED)

        //precondition

        List<CsiAggregation> mvs = pageCsiAggregationService.findAll(startedTime.toDate(), startedTime.toDate(), dailyInterval, JobGroup.list(), Page.list())
        assertEquals(0, mvs.size())

        when:
        List<CsiAggregation> calculatedMvs = pageCsiAggregationService.getOrCalculatePageCsiAggregations(startedTime.toDate(), startedTime.toDate(), dailyInterval, [jobGroup1], [page1])
        mvs = pageCsiAggregationService.findAll(startedTime.toDate(), startedTime.toDate(), dailyInterval, JobGroup.list(), Page.list())

        then:
        assertEquals(1, calculatedMvs.size())
        assertTrue(calculatedMvs[0].isCalculated())
        assertNull(calculatedMvs[0].csByWptDocCompleteInPercent)
        assertEquals(1, mvs.size())
    }

    /**
     * Mocks methods of {@link WeightingService}.
     */
    private void mockWeightingService(List<WeightedCsiValue> toReturnFromGetWeightedCsiValues, List<WeightedCsiValue> toReturnFromGetWeightedCsiValuesByVisuallyComplete) {
        def weightingService = new WeightingService()
        weightingService.metaClass.getWeightedCsiValues = {
            List<CsiValue> csiValues, Set<WeightFactor> weightFactors, CsiConfiguration csiConfiguration ->
                return toReturnFromGetWeightedCsiValues
        }
        weightingService.metaClass.getWeightedCsiValuesByVisuallyComplete = {
            List<CsiValue> csiValues, Set<WeightFactor> weightFactors, CsiConfiguration csiConfiguration ->
                return toReturnFromGetWeightedCsiValuesByVisuallyComplete
        }
        pageCsiAggregationService.weightingService = weightingService
    }

    //testdata////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private void createTestDataForAllTests() {
        weeklyInterval = new CsiAggregationInterval(name: 'weekly', intervalInMinutes: CsiAggregationInterval.WEEKLY).save(failOnError: true)
        dailyInterval = new CsiAggregationInterval(name: 'daily', intervalInMinutes: CsiAggregationInterval.DAILY).save(failOnError: true)
        hourlyInterval = new CsiAggregationInterval(name: 'hourly', intervalInMinutes: CsiAggregationInterval.HOURLY).save(failOnError: true)

        pageAggregator = new AggregatorType(name: AggregatorType.PAGE, measurandGroup: MeasurandGroup.NO_MEASURAND).save(failOnError: true)
        new AggregatorType(name: AggregatorType.MEASURED_EVENT, measurandGroup: MeasurandGroup.NO_MEASURAND).save(failOnError: true)

        browser = new Browser(name: "Test", weight: 1).save(failOnError: true);

        connectivityProfile = TestDataUtil.createConnectivityProfile("DSL1")

        page1 = new Page(name: pageName1).save(failOnError: true)
        page2 = new Page(name: pageName2).save(failOnError: true)
        page3 = new Page(name: pageName3).save(failOnError: true)
        CsiConfiguration csiConfiguration = TestDataUtil.createCsiConfiguration()
        csiConfiguration.label = 'csiConf'
        csiConfiguration.timeToCsMappings = TestDataUtil.createTimeToCsMappingForAllPages([page1, page2, page3])

        jobGroup1 = new JobGroup(name: jobGroupName1, csiConfiguration: csiConfiguration).save(failOnError: true)
        jobGroup2 = new JobGroup(name: jobGroupName2, csiConfiguration: csiConfiguration).save(failOnError: true)
        jobGroup3 = new JobGroup(name: jobGroupName3, csiConfiguration: csiConfiguration).save(failOnError: true)

        measuredEvent1 = new MeasuredEvent(testedPage: page1, name: "measuredEventAggregator").save(failOnError: true)
        WebPageTestServer wptServer = TestDataUtil.createWebPageTestServer("unusedLabel", "unusedProxyIdentifier", false, "http://example.com")
        location1 = TestDataUtil.createLocation(wptServer, "unusedIdentifier", browser, false)

        //with existing JobGroup and Page:
        TestDataUtil.createCsiAggregation(startDate.toDate(), weeklyInterval, pageAggregator, jobGroup1, page1, null, "", false)
        TestDataUtil.createCsiAggregation(startDate.toDate(), weeklyInterval, pageAggregator, jobGroup1, page2, null, "", false)
        TestDataUtil.createCsiAggregation(startDate.toDate(), weeklyInterval, pageAggregator, jobGroup3, page1, null, "", false)
        TestDataUtil.createCsiAggregation(startDate.toDate(), weeklyInterval, pageAggregator, jobGroup3, page2, null, "", false)
        TestDataUtil.createCsiAggregation(startDate.toDate(), weeklyInterval, pageAggregator, jobGroup3, page3, null, "", false)

        List<CsiAggregation> csiAggregationsWithConnectivityProfile = []
        csiAggregationsWithConnectivityProfile << TestDataUtil.createCsiAggregation(startDate.toDate(), hourlyInterval, pageAggregator, jobGroup1, page1, 12, "", false)
        csiAggregationsWithConnectivityProfile << TestDataUtil.createCsiAggregation(startDate.toDate(), hourlyInterval, pageAggregator, jobGroup1, page2, 10, "", false)
        csiAggregationsWithConnectivityProfile << TestDataUtil.createCsiAggregation(startDate.toDate(), hourlyInterval, pageAggregator, jobGroup3, page1, 12, "", false)
        csiAggregationsWithConnectivityProfile << TestDataUtil.createCsiAggregation(startDate.toDate(), hourlyInterval, pageAggregator, jobGroup3, page2, 12, "", false)
        csiAggregationsWithConnectivityProfile << TestDataUtil.createCsiAggregation(startDate.toDate(), hourlyInterval, pageAggregator, jobGroup3, page3, 10, "", false)
        csiAggregationsWithConnectivityProfile.each {
            it.connectivityProfile = connectivityProfile
            it.save(failOnError: true)
        }
    }
}

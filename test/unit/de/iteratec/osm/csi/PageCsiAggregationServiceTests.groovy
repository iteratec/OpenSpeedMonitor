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

import de.iteratec.osm.ConfigService
import de.iteratec.osm.measurement.schedule.ConnectivityProfile
import de.iteratec.osm.measurement.schedule.ConnectivityProfileService
import de.iteratec.osm.persistence.OsmDataSourceService
import de.iteratec.osm.util.ServiceMocker
import org.junit.Assert

import static org.junit.Assert.assertEquals
import grails.test.mixin.*
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.junit.After
import org.junit.Before
import org.junit.Test

import de.iteratec.osm.report.chart.CsiAggregationDaoService
import de.iteratec.osm.report.chart.CsiAggregationUtilService
import de.iteratec.osm.measurement.schedule.JobGroup

import de.iteratec.osm.report.chart.AggregatorType
import de.iteratec.osm.report.chart.MeasurandGroup
import de.iteratec.osm.report.chart.CsiAggregation
import de.iteratec.osm.report.chart.CsiAggregationInterval
import de.iteratec.osm.report.chart.CsiAggregationUpdateEvent
import de.iteratec.osm.report.chart.CsiAggregationUpdateEventDaoService
import de.iteratec.osm.csi.weighting.WeightFactor
import de.iteratec.osm.csi.weighting.WeightedCsiValue
import de.iteratec.osm.csi.weighting.WeightedValue
import de.iteratec.osm.csi.weighting.WeightingService
import de.iteratec.osm.result.EventResult
import de.iteratec.osm.result.MeasuredEvent
import de.iteratec.osm.result.dao.DefaultMeasuredEventDaoService
import de.iteratec.osm.util.PerformanceLoggingService
import de.iteratec.osm.measurement.environment.Browser
import de.iteratec.osm.measurement.environment.Location

/**
 * Test-suite of {@link PageCsiAggregationService}.
 */
@TestFor(PageCsiAggregationService)
@Mock([CsiAggregation, CsiAggregationInterval, AggregatorType, JobGroup, Page, MeasuredEvent, Browser, Location,
        EventResult, CsiAggregationDaoService, DefaultMeasuredEventDaoService, EventCsiAggregationService,
        CustomerSatisfactionWeightService, CsiDay, MeanCalcService, CsiAggregationUpdateEvent, ConnectivityProfile,
        ConnectivityProfileService, CsiConfiguration, ConfigService, OsmDataSourceService, EventCsiAggregationService,
        PerformanceLoggingService, CsiAggregationUtilService])

class PageCsiAggregationServiceTests {

    static final double DELTA = 1e-15

    CsiAggregationInterval weeklyInterval, dailyInterval, hourlyInterval
    JobGroup jobGroup1, jobGroup2, jobGroup3
    Map<String, JobGroup> allCsiGroups
    Page page1, page2, page3
    Map<String, Page> allPages
    AggregatorType pageAggregator;
    Browser browser;
    ConnectivityProfile connectivityProfile

    DateTime startDate = new DateTime(2013, 5, 16, 0, 0, 0)
    String jobGroupName1 = 'myJobGroup1'
    String jobGroupName2 = 'myJobGroup2'
    String jobGroupName3 = 'myJobGroup3'
    String pageName1 = 'myPageName1'
    String pageName2 = 'myPageName2'
    String pageName3 = 'myPageName3'

    PageCsiAggregationService serviceUnderTest

    @Before
    void setUp() {
        serviceUnderTest = service

        //mocks common for all tests
        serviceUnderTest.measuredEventDaoService = new DefaultMeasuredEventDaoService();
        mockCsiAggregationUpdateEventDaoService()

        //test-data common for all tests
        createTestDataForAllTests()
    }

    @After
    void tearDown() {
        deleteTestData()
    }

    //tests////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Test
    void testFindAll() {
        Assert.assertEquals(10, serviceUnderTest.findAll(startDate.toDate(), startDate.toDate(), weeklyInterval).size())
    }

    @Test
    void testFindAllByJobGroupsAndPages() {
        mockCsiAggregationDaoService()

        //getting test-specific data

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

        //test execution, mocking inner services and assertions

        Map<String, JobGroup> groups = ['1': group1, '2': group2, '3': group3]
        Map<String, Page> pages = ['1': page1, '2': page2, '3': page3]
        List<JobGroup> groupsAsList = groups.values().toList()
        List<Page> pagesAsList = pages.values().toList()
        mockCsiAggregationTagService(groups, pages)
        Assert.assertEquals(with_group1to3_page1to3, serviceUnderTest.findAll(startDate.toDate(), startDate.toDate(), weeklyInterval, groupsAsList, pagesAsList).size())
        groups = ['1': group1]
        pages = ['1': page1, '2': page2, '3': page3]
        mockCsiAggregationTagService(groups, pages)
        Assert.assertEquals(with_group1_page1to3, serviceUnderTest.findAll(startDate.toDate(), startDate.toDate(), weeklyInterval, groupsAsList, pagesAsList).size())
        groups = ['1': group2]
        pages = ['1': page1, '2': page2, '3': page3]
        mockCsiAggregationTagService(groups, pages)
        Assert.assertEquals(with_group2_page1to3, serviceUnderTest.findAll(startDate.toDate(), startDate.toDate(), weeklyInterval, groupsAsList, pagesAsList).size())
        groups = ['1': group3]
        pages = ['1': page1, '2': page2, '3': page3]
        mockCsiAggregationTagService(groups, pages)
        Assert.assertEquals(with_group3_page1to3, serviceUnderTest.findAll(startDate.toDate(), startDate.toDate(), weeklyInterval, groupsAsList, pagesAsList).size())
        groups = ['1': group1, '2': group2, '3': group3]
        pages = ['1': page1]
        mockCsiAggregationTagService(groups, pages)
        Assert.assertEquals(with_group1to3_page1, serviceUnderTest.findAll(startDate.toDate(), startDate.toDate(), weeklyInterval, groupsAsList, pagesAsList).size())
        groups = ['1': group1, '2': group2, '3': group3]
        pages = ['1': page2]
        mockCsiAggregationTagService(groups, pages)
        Assert.assertEquals(with_group1to3_page2, serviceUnderTest.findAll(startDate.toDate(), startDate.toDate(), weeklyInterval, groupsAsList, pagesAsList).size())
        groups = ['1': group1, '2': group2, '3': group3]
        pages = ['1': page3]
        mockCsiAggregationTagService(groups, pages)
        Assert.assertEquals(with_group1to3_page3, serviceUnderTest.findAll(startDate.toDate(), startDate.toDate(), weeklyInterval, groupsAsList, pagesAsList).size())
        groups = ['1': group1, '2': group2]
        pages = ['1': page3]
        mockCsiAggregationTagService(groups, pages)
        Assert.assertEquals(with_group1to2_page3, serviceUnderTest.findAll(startDate.toDate(), startDate.toDate(), weeklyInterval, groupsAsList, pagesAsList).size())
        groups = ['1': group1, '2': group2]
        pages = ['1': page1]
        mockCsiAggregationTagService(groups, pages)
        Assert.assertEquals(with_group1to2_page1, serviceUnderTest.findAll(startDate.toDate(), startDate.toDate(), weeklyInterval, groupsAsList, pagesAsList).size())
        groups = ['1': group3]
        pages = ['1': page1, '2': page3]
        mockCsiAggregationTagService(groups, pages)
        Assert.assertEquals(with_group3_page1or3, serviceUnderTest.findAll(startDate.toDate(), startDate.toDate(), weeklyInterval, groupsAsList, pagesAsList).size())
    }

    /**
     * Tests the marking of dependent (weekly-page-){@link CsiAggregation}s, which aren't calculated when new {@link EventResult}s get persisted.
     * {@link de.iteratec.osm.report.chart.CsiAggregationUpdateEvent}s with {@link CsiAggregationUpdateEvent#UpdateCause} OUTDATED should be written to db.
     */
    @Test
    void testMarkingWeeklyPageMvsAsOutdated() {

        //getting test-specific data

        EventResult eventResult = new EventResult().save(validate: false)
        DateTime startTimeOfWeek = new DateTime(2013, 8, 5, 0, 0, 0, DateTimeZone.UTC)

        //mocking inner services

        mockCsiAggregationTagService(['1': jobGroup1], ['1': page1])

        //precondition

        List<CsiAggregation> wpmvs = serviceUnderTest.findAll(startTimeOfWeek.toDate(), startTimeOfWeek.toDate(), weeklyInterval)
        assertEquals(0, wpmvs.size())

        //test execution
        serviceUnderTest.markMvAsOutdated(startTimeOfWeek, eventResult, weeklyInterval)

        //assertions

        wpmvs = serviceUnderTest.findAll(startTimeOfWeek.toDate(), startTimeOfWeek.toDate(), weeklyInterval)
        assertEquals(1, wpmvs.size())
        CsiAggregation wpmvMarked = wpmvs[0]
        assertFalse(wpmvMarked.isCalculated())
        assertTrue(wpmvMarked.hasToBeCalculated())

        //second mark of the same weekly-page-mv
        serviceUnderTest.markMvAsOutdated(startTimeOfWeek, eventResult, weeklyInterval)
        wpmvs = serviceUnderTest.findAll(startTimeOfWeek.toDate(), startTimeOfWeek.toDate(), weeklyInterval)
        assertEquals(1, wpmvs.size())
        wpmvMarked = wpmvs[0]
        assertFalse(wpmvMarked.isCalculated())
        assertTrue(wpmvMarked.hasToBeCalculated())

    }

    /**
     * Tests the marking of dependent (weekly-page-){@link CsiAggregation}s, which aren't calculated when new {@link EventResult}s get persisted.
     * {@link de.iteratec.osm.report.chart.CsiAggregationUpdateEvent}s with {@link CsiAggregationUpdateEvent#UpdateCause} OUTDATED should be written to db.
     */
    @Test
    void testMarkingDailyPageMvsAsOutdated() {

        //getting test-specific data

        EventResult eventResult = new EventResult().save(validate: false)
        DateTime startTime = new DateTime(2013, 8, 5, 0, 0, 0, DateTimeZone.UTC)

        //mocking inner services

        mockCsiAggregationTagService(['1': jobGroup1], ['1': page1])

        //precondition

        List<CsiAggregation> mvs = serviceUnderTest.findAll(startTime.toDate(), startTime.toDate(), dailyInterval)
        assertEquals(0, mvs.size())

        //test execution
        serviceUnderTest.markMvAsOutdated(startTime, eventResult, dailyInterval)

        //assertions

        mvs = serviceUnderTest.findAll(startTime.toDate(), startTime.toDate(), dailyInterval)
        assertEquals(1, mvs.size())
        CsiAggregation wpmvMarked = mvs[0]
        assertFalse(wpmvMarked.isCalculated())
        assertTrue(wpmvMarked.hasToBeCalculated())

        //second mark of the same weekly-page-mv
        serviceUnderTest.markMvAsOutdated(startTime, eventResult, dailyInterval)
        mvs = serviceUnderTest.findAll(startTime.toDate(), startTime.toDate(), dailyInterval)
        assertEquals(1, mvs.size())
        wpmvMarked = mvs[0]
        assertFalse(wpmvMarked.isCalculated())
        assertTrue(wpmvMarked.hasToBeCalculated())

    }

    /**
     * Tests calculation of daily-page-{@link CsiAggregation}s, which aren't calculated when new {@link EventResult}s get persisted.
     */
    @Test
    void testCalculation_DailyInterval_SingleHourlyMv() {
        DateTime startedTime = new DateTime(2013, 5, 16, 12, 12, 11)

        CsiAggregation hpmv = new CsiAggregation(interval: hourlyInterval, aggregator: AggregatorType.findByName(AggregatorType.MEASURED_EVENT), tag: jobGroup1.ident() + ';' + page1.ident() + ';1;1;1', started: startedTime.plusMinutes(2).toDate(), underlyingEventResultsByWptDocComplete: "1", csByWptDocCompleteInPercent: 12d, connectivityProfile: connectivityProfile).save(failOnError: true);
        TestDataUtil.createUpdateEvent(hpmv.ident(), CsiAggregationUpdateEvent.UpdateCause.CALCULATED)

        CsiDay testDay = new CsiDay()
        (0..23).each {
            testDay.setHourWeight(it, 1)
        }
        testDay.save(failOnError: true)

        List<WeightedCsiValue> weightedCsiValuesToReturnInMock = [
                new WeightedCsiValue(weightedValue: new WeightedValue(value: 12d, weight: 1d), underlyingEventResultIds: [])]

        //mocking inner services

        mockCsiAggregationDaoService()
        mockCsiAggregationTagService(allCsiGroups, allPages)
        mockWeightingService(weightedCsiValuesToReturnInMock, [])

        //precondition

        List<CsiAggregation> mvs = serviceUnderTest.findAll(startedTime.toDate(), startedTime.toDate(), dailyInterval)
        assertEquals(0, mvs.size())

        //test execution
        List<CsiAggregation> calculatedMvs = serviceUnderTest.getOrCalculatePageCsiAggregations(startedTime.toDate(), startedTime.toDate(), dailyInterval, [jobGroup1], [page1])

        //assertions

        assertEquals(1, calculatedMvs.size())
        assertTrue(calculatedMvs[0].isCalculated())
        assertEquals(12d, calculatedMvs[0].csByWptDocCompleteInPercent, DELTA)

        mvs = serviceUnderTest.findAll(startedTime.toDate(), startedTime.toDate(), dailyInterval)
        assertEquals(1, mvs.size())
    }

    /**
     * Tests calculation of daily-page-{@link CsiAggregation}s, which aren't calculated when new {@link EventResult}s get persisted.
     */
    @Test
    void testCalculation_DailyInterval_MultipleHourlyMv() {
        DateTime startedTime = new DateTime(2013, 5, 16, 12, 12, 11)

        CsiAggregation mv1 = new CsiAggregation(interval: hourlyInterval, aggregator: AggregatorType.findByName(AggregatorType.MEASURED_EVENT), tag: jobGroup1.ident() + ';' + page1.ident() + ';1;1;1', started: startedTime.plusMinutes(2).toDate(), underlyingEventResultsByWptDocComplete: "1", csByWptDocCompleteInPercent: 12d, connectivityProfile: connectivityProfile).save(failOnError: true);
        TestDataUtil.createUpdateEvent(mv1.ident(), CsiAggregationUpdateEvent.UpdateCause.CALCULATED)
        CsiAggregation mv2 = new CsiAggregation(interval: hourlyInterval, aggregator: AggregatorType.findByName(AggregatorType.MEASURED_EVENT), tag: jobGroup1.ident() + ';' + page1.ident() + ';1;1;1', started: startedTime.plusHours(2).toDate(), underlyingEventResultsByWptDocComplete: "1", csByWptDocCompleteInPercent: 10d, connectivityProfile: connectivityProfile).save(failOnError: true);
        TestDataUtil.createUpdateEvent(mv2.ident(), CsiAggregationUpdateEvent.UpdateCause.CALCULATED)
        CsiAggregation mv3 = new CsiAggregation(interval: hourlyInterval, aggregator: AggregatorType.findByName(AggregatorType.MEASURED_EVENT), tag: jobGroup1.ident() + ';' + page1.ident() + ';1;1;1', started: startedTime.plusHours(10).toDate(), underlyingEventResultsByWptDocComplete: "1", csByWptDocCompleteInPercent: 11d, connectivityProfile: connectivityProfile).save(failOnError: true);
        TestDataUtil.createUpdateEvent(mv3.ident(), CsiAggregationUpdateEvent.UpdateCause.CALCULATED)

        //MVs outside of Interval
        CsiAggregation mv4 = new CsiAggregation(interval: hourlyInterval, aggregator: AggregatorType.findByName(AggregatorType.MEASURED_EVENT), tag: jobGroup1.ident() + ';' + page1.ident() + ';1;1;1', started: startedTime.minusMinutes(1).toDate(), underlyingEventResultsByWptDocComplete: "1", csByWptDocCompleteInPercent: 1000d, connectivityProfile: connectivityProfile).save(failOnError: true);
        TestDataUtil.createUpdateEvent(mv4.ident(), CsiAggregationUpdateEvent.UpdateCause.CALCULATED)
        CsiAggregation mv5 = new CsiAggregation(interval: hourlyInterval, aggregator: AggregatorType.findByName(AggregatorType.MEASURED_EVENT), tag: jobGroup1.ident() + ';' + page1.ident() + ';1;1;1', started: startedTime.plusMinutes(CsiAggregationInterval.DAILY + 1).toDate(), underlyingEventResultsByWptDocComplete: "1", csByWptDocCompleteInPercent: 1000d, connectivityProfile: connectivityProfile).save(failOnError: true);
        TestDataUtil.createUpdateEvent(mv5.ident(), CsiAggregationUpdateEvent.UpdateCause.CALCULATED)

        CsiDay testDay = new CsiDay()
        (0..23).each {
            testDay.setHourWeight(it, 1)
        }
        testDay.save(failOnError: true)

        List<WeightedCsiValue> weightedCsiValuesToReturnInMock = [
                new WeightedCsiValue(weightedValue: new WeightedValue(value: 12d, weight: 1d), underlyingEventResultIds: []),
                new WeightedCsiValue(weightedValue: new WeightedValue(value: 10d, weight: 1d), underlyingEventResultIds: []),
                new WeightedCsiValue(weightedValue: new WeightedValue(value: 11d, weight: 1d), underlyingEventResultIds: [])]

        //mocking inner services

        mockCsiAggregationDaoService()
        mockCsiAggregationTagService(allCsiGroups, allPages)
        mockWeightingService(weightedCsiValuesToReturnInMock, [])

        //precondition

        List<CsiAggregation> mvs = serviceUnderTest.findAll(startedTime.toDate(), startedTime.toDate(), dailyInterval)
        assertEquals(0, mvs.size())

        //test execution
        List<CsiAggregation> calculatedMvs = serviceUnderTest.getOrCalculatePageCsiAggregations(startedTime.toDate(), startedTime.toDate(), dailyInterval, [jobGroup1], [page1])

        //assertions

        assertEquals(1, calculatedMvs.size())
        assertTrue(calculatedMvs[0].isCalculated())
        assertEquals(11d, calculatedMvs[0].csByWptDocCompleteInPercent, DELTA)

        mvs = serviceUnderTest.findAll(startedTime.toDate(), startedTime.toDate(), dailyInterval)
        assertEquals(1, mvs.size())

        CsiAggregation wpmvMarked = mvs[0]
        assertTrue(wpmvMarked.isCalculated())

    }

    /**
     * Tests calculation of daily-page-{@link CsiAggregation}s, which aren't calculated when new {@link EventResult}s get persisted.
     */
    @Test
    void testCalculation_DailyInterval_MultipleHourlyMv_differentWeights() {

        //test-specific data

        DateTime startedTime = new DateTime(2013, 5, 16, 12, 12, 11)

        CsiAggregation mv1 = new CsiAggregation(interval: hourlyInterval, aggregator: AggregatorType.findByName(AggregatorType.MEASURED_EVENT), tag: jobGroup1.ident() + ';' + page1.ident() + ';1;1;1', started: startedTime.toDate(), underlyingEventResultsByWptDocComplete: "1", csByWptDocCompleteInPercent: 1d, connectivityProfile: connectivityProfile).save(failOnError: true)
        TestDataUtil.createUpdateEvent(mv1.ident(), CsiAggregationUpdateEvent.UpdateCause.CALCULATED)
        CsiAggregation mv2 = new CsiAggregation(interval: hourlyInterval, aggregator: AggregatorType.findByName(AggregatorType.MEASURED_EVENT), tag: jobGroup1.ident() + ';' + page1.ident() + ';1;1;1', started: startedTime.plusHours(2).toDate(), underlyingEventResultsByWptDocComplete: "1", csByWptDocCompleteInPercent: 10d, connectivityProfile: connectivityProfile).save(failOnError: true)
        TestDataUtil.createUpdateEvent(mv2.ident(), CsiAggregationUpdateEvent.UpdateCause.CALCULATED)
        CsiAggregation mv3 = new CsiAggregation(interval: hourlyInterval, aggregator: AggregatorType.findByName(AggregatorType.MEASURED_EVENT), tag: jobGroup1.ident() + ';' + page1.ident() + ';1;1;1', started: startedTime.plusHours(10).toDate(), underlyingEventResultsByWptDocComplete: "1", csByWptDocCompleteInPercent: 11d, connectivityProfile: connectivityProfile).save(failOnError: true)
        TestDataUtil.createUpdateEvent(mv3.ident(), CsiAggregationUpdateEvent.UpdateCause.CALCULATED)

        //MVs outside of Interval
        CsiAggregation mv4 = new CsiAggregation(interval: hourlyInterval, aggregator: AggregatorType.findByName(AggregatorType.MEASURED_EVENT), tag: jobGroup1.ident() + ';' + page1.ident() + ';1;1;1', started: startedTime.minusMinutes(1).toDate(), underlyingEventResultsByWptDocComplete: "1", csByWptDocCompleteInPercent: 1000d, connectivityProfile: connectivityProfile).save(failOnError: true)
        TestDataUtil.createUpdateEvent(mv4.ident(), CsiAggregationUpdateEvent.UpdateCause.CALCULATED)
        CsiAggregation mv5 = new CsiAggregation(interval: hourlyInterval, aggregator: AggregatorType.findByName(AggregatorType.MEASURED_EVENT), tag: jobGroup1.ident() + ';' + page1.ident() + ';1;1;1', started: startedTime.plusMinutes(CsiAggregationInterval.DAILY + 1).toDate(), underlyingEventResultsByWptDocComplete: "1", csByWptDocCompleteInPercent: 1000d, connectivityProfile: connectivityProfile).save(failOnError: true)
        TestDataUtil.createUpdateEvent(mv5.ident(), CsiAggregationUpdateEvent.UpdateCause.CALCULATED)

        CsiDay testDay = new CsiDay()
        (0..23).each {
            testDay.setHourWeight(it, 1)
        }
        testDay.setHourWeight(12, 10)
        testDay.save(failOnError: true)

        List<WeightedCsiValue> weightedCsiValuesToReturnInMock = [
                new WeightedCsiValue(weightedValue: new WeightedValue(value: 1d, weight: 10d), underlyingEventResultIds: []),
                new WeightedCsiValue(weightedValue: new WeightedValue(value: 10d, weight: 1d), underlyingEventResultIds: []),
                new WeightedCsiValue(weightedValue: new WeightedValue(value: 11d, weight: 1d), underlyingEventResultIds: [])]

        //mocking inner services

        mockCsiAggregationDaoService()
        mockCsiAggregationTagService(allCsiGroups, allPages)
        mockWeightingService(weightedCsiValuesToReturnInMock,[])

        //precondition

        List<CsiAggregation> mvs = serviceUnderTest.findAll(startedTime.toDate(), startedTime.toDate(), dailyInterval)
        assertEquals(0, mvs.size())

        //test execution
        List<CsiAggregation> calculatedMvs = serviceUnderTest.getOrCalculatePageCsiAggregations(startedTime.toDate(), startedTime.toDate(), dailyInterval, [jobGroup1], [page1])

        //assertions

        assertEquals(1, calculatedMvs.size())
        assertTrue(calculatedMvs[0].isCalculated())
        double browserWeightAllMvs = 1d
        double valueFirstMv = 1d
        double hourofdayWeightFirstMv = 10d
        double valueSecondMv = 10d
        double hourofdayWeightSecondMv = 1d
        double valueThirdMv = 11d
        double hourofdayWeightThirdMv = 1d
        double sumOfAllWeights = 12d
        assertEquals(
                ((valueFirstMv * hourofdayWeightFirstMv * browserWeightAllMvs) + (valueSecondMv * hourofdayWeightSecondMv * browserWeightAllMvs) + (valueThirdMv * hourofdayWeightThirdMv * browserWeightAllMvs)) / sumOfAllWeights,
                calculatedMvs[0].csByWptDocCompleteInPercent,
                DELTA
        );

        mvs = serviceUnderTest.findAll(startedTime.toDate(), startedTime.toDate(), dailyInterval)
        assertEquals(1, mvs.size())

        CsiAggregation wpmvMarked = mvs[0]
        assertTrue(wpmvMarked.isCalculated())

    }

    /**
     * Tests calculation of daily-page-{@link CsiAggregation}s, which aren't calculated when new {@link EventResult}s get persisted.
     */
    @Test
    void testCalculation_DailyInterval_MultipleHourlyMv_YesCalculatedNoData() {
        DateTime startedTime = new DateTime(2013, 5, 16, 12, 12, 11)

        //MVs outside of Interval
        CsiAggregation mv1 = new CsiAggregation(interval: hourlyInterval, aggregator: AggregatorType.findByName(AggregatorType.MEASURED_EVENT), tag: jobGroup1.ident() + ';' + page1.ident() + ';1;1;1', started: startedTime.minusMinutes(1).toDate(), underlyingEventResultsByWptDocComplete: "1", csByWptDocCompleteInPercent: 1000d, connectivityProfile: connectivityProfile).save(failOnError: true);
        TestDataUtil.createUpdateEvent(mv1.ident(), CsiAggregationUpdateEvent.UpdateCause.CALCULATED)
        CsiAggregation mv2 = new CsiAggregation(interval: hourlyInterval, aggregator: AggregatorType.findByName(AggregatorType.MEASURED_EVENT), tag: jobGroup1.ident() + ';' + page1.ident() + ';1;1;1', started: startedTime.plusMinutes(CsiAggregationInterval.DAILY + 1).toDate(), underlyingEventResultsByWptDocComplete: "1", csByWptDocCompleteInPercent: 1000d, connectivityProfile: connectivityProfile).save(failOnError: true);
        TestDataUtil.createUpdateEvent(mv2.ident(), CsiAggregationUpdateEvent.UpdateCause.CALCULATED)

        CsiDay testDay = new CsiDay()
        (0..23).each {
            testDay.setHourWeight(it, 1)
        }
        testDay.save(failOnError: true)

        //mocking inner services

        mockCsiAggregationDaoService()
        mockCsiAggregationTagService(allCsiGroups, allPages)

        //precondition

        List<CsiAggregation> mvs = serviceUnderTest.findAll(startedTime.toDate(), startedTime.toDate(), dailyInterval)
        assertEquals(0, mvs.size())

        //test execution
        List<CsiAggregation> calculatedMvs = serviceUnderTest.getOrCalculatePageCsiAggregations(startedTime.toDate(), startedTime.toDate(), dailyInterval, [jobGroup1], [page1])

        //assertions

        assertEquals(1, calculatedMvs.size())
        assertTrue(calculatedMvs[0].isCalculated())
        assertNull(calculatedMvs[0].csByWptDocCompleteInPercent)

        mvs = serviceUnderTest.findAll(startedTime.toDate(), startedTime.toDate(), dailyInterval)
        assertEquals(1, mvs.size())
    }

    //mocking inner services////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Mocks used methods of {@link EventCsiAggregationService}.
     * @param csiGroups
     * @param pages
     */
    private void mockCsiAggregationTagService(Map<String, JobGroup> allCsiGroups, Map<String, Page> allPages) {
        ServiceMocker serviceMocker = ServiceMocker.create()
        serviceMocker.mockCsiAggregationTagService(serviceUnderTest, allCsiGroups, null, allPages, null, null)
        serviceMocker.mockCsiAggregationTagService(serviceUnderTest.eventCsiAggregationService, allCsiGroups, null, allPages, null, null)
    }


    private void mockCsiAggregationDaoService() {
    }

    /**
     * Mocks methods of {@link WeightingService}.
     */
    private void mockWeightingService(List<WeightedCsiValue> toReturnFromGetWeightedCsiValues, List<WeightedCsiValue> toReturnFromGetWeightedCsiValuesByVisuallyComplete) {
        def weightingService = mockFor(WeightingService, true)
        weightingService.demand.getWeightedCsiValues(1..10000) {
            List<CsiValue> csiValues, Set<WeightFactor> weightFactors, CsiConfiguration csiConfiguration ->
                return toReturnFromGetWeightedCsiValues
        }
        weightingService.demand.getWeightedCsiValuesByVisuallyComplete(1..10000) {
            List<CsiValue> csiValues, Set<WeightFactor> weightFactors, CsiConfiguration csiConfiguration ->
                return toReturnFromGetWeightedCsiValuesByVisuallyComplete
        }
        serviceUnderTest.weightingService = weightingService.createMock()
    }

    /**
     * Mocks methods of {@link CsiAggregationUpdateEventDaoService}.
     */
    private void mockCsiAggregationUpdateEventDaoService() {
        def csiAggregationUpdateEventDaoService = mockFor(CsiAggregationUpdateEventDaoService, true)
        csiAggregationUpdateEventDaoService.demand.createUpdateEvent(1..10000) {
            Long csiAggregationId, CsiAggregationUpdateEvent.UpdateCause cause ->

                new CsiAggregationUpdateEvent(
                        dateOfUpdate: new Date(),
                        csiAggregationId: csiAggregationId,
                        updateCause: cause
                ).save(failOnError: true)

        }
        serviceUnderTest.csiAggregationUpdateEventDaoService = csiAggregationUpdateEventDaoService.createMock()
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

        page1 = new Page(name: pageName1).save(validate: false)
        page2 = new Page(name: pageName2).save(validate: false)
        page3 = new Page(name: pageName3).save(validate: false)
        CsiConfiguration csiConfiguration = TestDataUtil.createCsiConfiguration()
        csiConfiguration.label = 'csiConf'
        csiConfiguration.timeToCsMappings = TestDataUtil.createTimeToCsMappingForAllPages([page1,page2,page3])

        jobGroup1 = new JobGroup(name: jobGroupName1, csiConfiguration: csiConfiguration).save(failOnError: true)
        jobGroup2 = new JobGroup(name: jobGroupName2, csiConfiguration: csiConfiguration).save(failOnError: true)
        jobGroup3 = new JobGroup(name: jobGroupName3, csiConfiguration: csiConfiguration).save(failOnError: true)

        //with existing JobGroup and Page:
        new CsiAggregation(interval: weeklyInterval, aggregator: pageAggregator, tag: '1;1', started: startDate.toDate()).save(validate: false)
        new CsiAggregation(interval: weeklyInterval, aggregator: pageAggregator, tag: '1;2', started: startDate.toDate()).save(validate: false)
        new CsiAggregation(interval: weeklyInterval, aggregator: pageAggregator, tag: '3;1', started: startDate.toDate()).save(validate: false)
        new CsiAggregation(interval: weeklyInterval, aggregator: pageAggregator, tag: '3;2', started: startDate.toDate()).save(validate: false)
        new CsiAggregation(interval: weeklyInterval, aggregator: pageAggregator, tag: '3;3', started: startDate.toDate()).save(validate: false)

        new CsiAggregation(interval: hourlyInterval, aggregator: pageAggregator, tag: '1;1', started: startDate.toDate(), csByWptDocCompleteInPercent: 12, connectivityProfile: connectivityProfile).save(validate: true)
        new CsiAggregation(interval: hourlyInterval, aggregator: pageAggregator, tag: '1;2', started: startDate.toDate(), csByWptDocCompleteInPercent: 10, connectivityProfile: connectivityProfile).save(validate: false)
        new CsiAggregation(interval: hourlyInterval, aggregator: pageAggregator, tag: '3;1', started: startDate.toDate(), csByWptDocCompleteInPercent: 12, connectivityProfile: connectivityProfile).save(validate: false)
        new CsiAggregation(interval: hourlyInterval, aggregator: pageAggregator, tag: '3;2', started: startDate.toDate(), csByWptDocCompleteInPercent: 12, connectivityProfile: connectivityProfile).save(validate: false)
        new CsiAggregation(interval: hourlyInterval, aggregator: pageAggregator, tag: '3;3', started: startDate.toDate(), csByWptDocCompleteInPercent: 10, connectivityProfile: connectivityProfile).save(validate: false)

        //not with existing page
        new CsiAggregation(interval: weeklyInterval, aggregator: pageAggregator, tag: '3;4', started: startDate.toDate()).save(validate: false)
        //not with existing JobGroup
        new CsiAggregation(interval: weeklyInterval, aggregator: pageAggregator, tag: '4;2', started: startDate.toDate()).save(validate: false)
        //not with existing JobGroup and Page:
        new CsiAggregation(interval: weeklyInterval, aggregator: pageAggregator, tag: '4;4', started: startDate.toDate()).save(validate: false)
        new CsiAggregation(interval: weeklyInterval, aggregator: pageAggregator, tag: '5;14', started: startDate.toDate()).save(validate: false)
        new CsiAggregation(interval: weeklyInterval, aggregator: pageAggregator, tag: '6;7', started: startDate.toDate()).save(validate: false)

        allCsiGroups = ['1': jobGroup1, '2': jobGroup2, '3': jobGroup3]
        allPages = ['1': page1, '2': page2, '3': page3]
    }

    private void deleteTestData() {
        Page.list()*.delete(flush: true)
        CsiAggregation.list()*.delete(flush: true)
        JobGroup.list()*.delete(flush: true)
    }
}

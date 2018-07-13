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
import de.iteratec.osm.measurement.environment.WebPageTestServer
import de.iteratec.osm.measurement.schedule.ConnectivityProfile
import de.iteratec.osm.measurement.schedule.Job
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.report.chart.AggregationType
import de.iteratec.osm.report.chart.CsiAggregation
import de.iteratec.osm.report.chart.CsiAggregationInterval
import de.iteratec.osm.report.chart.CsiAggregationUpdateEvent
import de.iteratec.osm.result.CsiValueService
import de.iteratec.osm.result.EventResult
import de.iteratec.osm.result.JobResult
import de.iteratec.osm.result.MeasuredEvent
import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import org.joda.time.DateTime
import org.joda.time.DateTimeZone

/**
 * Test-suite of {@link PageCsiAggregationService}.
 */

@Integration
@Rollback
class PageCsiAggregationServiceIntegrationSpec extends NonTransactionalIntegrationSpec {

    CsiAggregationInterval weeklyInterval, dailyInterval, hourlyInterval
    JobGroup jobGroup1, jobGroup2, jobGroup3
    Page page1, page2, page3
    List<Page> pages
    Browser browser
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
        pageCsiAggregationService.csiValueService = grailsApplication.mainContext.getBean('csiValueService')
    }

    //tests////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    void "test findAllByJobGroupsAndPages"() {
        when: "foo"
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

        then: "bar"
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
        given: "an eventResult and a date"
        EventResult eventResult = EventResult.build(
                jobGroup: jobGroup1,
                page: page1,
                browser: browser,
                location: location1,
                connectivityProfile: connectivityProfile,
                measuredEvent: measuredEvent1,
                jobResult: JobResult.build(job: Job.build(jobGroup: jobGroup1))
        )
        DateTime startTimeOfWeek = new DateTime(2013, 8, 5, 0, 0, 0, DateTimeZone.UTC)

        when: "checking for page CsiAggregations"
        List<CsiAggregation> wpmvs = pageCsiAggregationService.findAll(startTimeOfWeek.toDate(), startTimeOfWeek.toDate(), weeklyInterval, JobGroup.list(), Page.list())
        0 == wpmvs.size()

        pageCsiAggregationService.markMvAsOutdated(startTimeOfWeek, eventResult, weeklyInterval)
        wpmvs = pageCsiAggregationService.findAll(startTimeOfWeek.toDate(), startTimeOfWeek.toDate(), weeklyInterval, JobGroup.list(), Page.list())
        CsiAggregation wpmvMarked = wpmvs[0]

        then: "check if any page CsiAggregations exist"
        1 == wpmvs.size()
        !wpmvMarked.isCalculated()
        wpmvMarked.hasToBeCalculated()

        when: "second mark of the same weekly-pageAggregator-mv"
        pageCsiAggregationService.markMvAsOutdated(startTimeOfWeek, eventResult, weeklyInterval)
        wpmvs = pageCsiAggregationService.findAll(startTimeOfWeek.toDate(), startTimeOfWeek.toDate(), weeklyInterval, JobGroup.list(), Page.list())
        wpmvMarked = wpmvs[0]

        then: "check if page CsiAggregations are calculated"
        1 == wpmvs.size()
        !wpmvMarked.isCalculated()
        wpmvMarked.hasToBeCalculated()
    }

    /**
     * Tests the marking of dependent (weekly-pageAggregator-){@link CsiAggregation}s, which aren't calculated when new {@link EventResult}s get persisted.
     * {@link de.iteratec.osm.report.chart.CsiAggregationUpdateEvent}s with {@link CsiAggregationUpdateEvent#UpdateCause} OUTDATED should be written to db.
     */
    void "test markingDailyPageMvsAsOutdated"() {
        given: "an eventResult and a date"
        EventResult eventResult = EventResult.build(
                jobGroup: jobGroup1,
                page: page1,
                browser: browser,
                location: location1,
                connectivityProfile: connectivityProfile,
                measuredEvent: measuredEvent1,
                jobResult: JobResult.build(job: Job.build(jobGroup: jobGroup1))
        )
        DateTime startTime = new DateTime(2013, 8, 5, 0, 0, 0, DateTimeZone.UTC)

        when: "checking for page CsiAggregations"
        List<CsiAggregation> mvs = pageCsiAggregationService.findAll(startTime.toDate(), startTime.toDate(), dailyInterval, JobGroup.list(), Page.list())
        0 == mvs.size()

        pageCsiAggregationService.markMvAsOutdated(startTime, eventResult, dailyInterval)
        mvs = pageCsiAggregationService.findAll(startTime.toDate(), startTime.toDate(), dailyInterval, JobGroup.list(), Page.list())
        CsiAggregation wpmvMarked = mvs[0]

        then: "check if page CsiAggregations are calculated"
        1 == mvs.size()
        !wpmvMarked.isCalculated()
        wpmvMarked.hasToBeCalculated()

        when: "second mark of the same weekly-pageAggregator-mv"
        pageCsiAggregationService.markMvAsOutdated(startTime, eventResult, dailyInterval)
        mvs = pageCsiAggregationService.findAll(startTime.toDate(), startTime.toDate(), dailyInterval, JobGroup.list(), Page.list())
        wpmvMarked = mvs[0]

        then: "check if page CsiAggregations are calculated"
        1 == mvs.size()
        !wpmvMarked.isCalculated()
        wpmvMarked.hasToBeCalculated()

    }

    /**
     * Tests calculation of daily-pageAggregator-{@link CsiAggregation}s, which aren't calculated when new {@link EventResult}s get persisted.
     */
    void "test calculation_DailyInterval_SingleHourlyMv"() {
        given: " a date, a measured event, a csiAggregation and a csiAggregationUpdateEvent "
        DateTime startedTime = new DateTime(2013, 5, 16, 12, 12, 11)
        MeasuredEvent measuredEvent = MeasuredEvent.build(testedPage: page1, name: "measuredEvent")

        CsiAggregation csiAggregation = CsiAggregation.build(
                interval: hourlyInterval,
                aggregationType: AggregationType.MEASURED_EVENT,
                jobGroup: jobGroup1,
                measuredEvent: measuredEvent,
                page: page1,
                browser: browser,
                location: location1,
                started: startedTime.plusMinutes(2).toDate(),
                underlyingEventResultsByWptDocComplete: "1",
                csByWptDocCompleteInPercent: 12d,
                connectivityProfile: connectivityProfile)

        CsiAggregationUpdateEvent.build(
                csiAggregationId: csiAggregation.ident(),
                updateCause: CsiAggregationUpdateEvent.UpdateCause.CALCULATED)

        List<WeightedCsiValue> weightedCsiValuesToReturnInMock = [
                new WeightedCsiValue(weightedValue: new WeightedValue(value: 12d, weight: 1d), underlyingEventResultIds: [])]

        //mocking inner services
        mockCsiValueService(weightedCsiValuesToReturnInMock, [])

        when: "getting csiAggregations through pageCsiAggregationService"
        List<CsiAggregation> calculatedMvs = pageCsiAggregationService.getOrCalculatePageCsiAggregations(startedTime.toDate(), startedTime.toDate(), dailyInterval, [jobGroup1], [page1])
        List<CsiAggregation> mvs = pageCsiAggregationService.findAll(startedTime.toDate(), startedTime.toDate(), dailyInterval, JobGroup.list(), Page.list())

        then: "check if page csiAggregations are calculated"
        1 == calculatedMvs.size()
        calculatedMvs[0].isCalculated()
        12d == calculatedMvs[0].csByWptDocCompleteInPercent
        1 == mvs.size()
    }

    /**
     * Tests calculation of daily-pageAggregator-{@link CsiAggregation}s, which aren't calculated when new {@link EventResult}s get persisted.
     */
    void "test calculation_DailyInterval_MultipleHourlyMv"() {
        given: "a date, five csiAggregations and five csiAggregationUpdateEvent"
        DateTime startedTime = new DateTime(2013, 5, 16, 12, 12, 11)
        CsiAggregation mv1 = CsiAggregation.build(
                interval: hourlyInterval,
                aggregationType: AggregationType.MEASURED_EVENT,
                jobGroup: jobGroup1,
                page: page1, measuredEvent: measuredEvent1,
                browser: browser, location: location1,
                started: startedTime.plusMinutes(2).toDate(),
                underlyingEventResultsByWptDocComplete: "1",
                csByWptDocCompleteInPercent: 12d,
                connectivityProfile: connectivityProfile)

        CsiAggregation mv2 = CsiAggregation.build(
                interval: hourlyInterval,
                aggregationType: AggregationType.MEASURED_EVENT,
                jobGroup: jobGroup1,
                page: page1,
                measuredEvent: measuredEvent1,
                browser: browser,
                location: location1,
                started: startedTime.plusHours(2).toDate(),
                underlyingEventResultsByWptDocComplete: "1",
                csByWptDocCompleteInPercent: 10d,
                connectivityProfile: connectivityProfile)

        CsiAggregation mv3 = CsiAggregation.build(
                interval: hourlyInterval,
                aggregationType: AggregationType.MEASURED_EVENT,
                jobGroup: jobGroup1,
                page: page1,
                measuredEvent: measuredEvent1,
                browser: browser,
                location: location1,
                started: startedTime.plusHours(10).toDate(),
                underlyingEventResultsByWptDocComplete: "1",
                csByWptDocCompleteInPercent: 11d,
                connectivityProfile: connectivityProfile)

        //MVs outside of Interval
        CsiAggregation mv4 = CsiAggregation.build(
                interval: hourlyInterval,
                aggregationType: AggregationType.MEASURED_EVENT,
                jobGroup: jobGroup1,
                page: page1,
                measuredEvent: measuredEvent1,
                browser: browser,
                location: location1,
                started: startedTime.minusMinutes(1).toDate(),
                underlyingEventResultsByWptDocComplete: "1",
                csByWptDocCompleteInPercent: 1000d,
                connectivityProfile: connectivityProfile)

        CsiAggregation mv5 = CsiAggregation.build(
                interval: hourlyInterval,
                aggregationType: AggregationType.MEASURED_EVENT,
                jobGroup: jobGroup1,
                page: page1,
                measuredEvent: measuredEvent1,
                browser: browser,
                location: location1,
                started: startedTime.plusMinutes(CsiAggregationInterval.DAILY + 1).toDate(),
                underlyingEventResultsByWptDocComplete: "1",
                csByWptDocCompleteInPercent: 1000d,
                connectivityProfile: connectivityProfile)

        List<CsiAggregation> csiAggregationList = [mv1,mv2,mv3,mv4,mv5]
        csiAggregationList.each {
            CsiAggregationUpdateEvent.build(
                    csiAggregationId: it.ident(),
                    updateCause: CsiAggregationUpdateEvent.UpdateCause.CALCULATED)
        }

        //mocking inner services
        List<WeightedCsiValue> weightedCsiValuesToReturnInMock = [
                new WeightedCsiValue(weightedValue: new WeightedValue(value: 12d, weight: 1d), underlyingEventResultIds: []),
                new WeightedCsiValue(weightedValue: new WeightedValue(value: 10d, weight: 1d), underlyingEventResultIds: []),
                new WeightedCsiValue(weightedValue: new WeightedValue(value: 11d, weight: 1d), underlyingEventResultIds: [])]
        mockCsiValueService(weightedCsiValuesToReturnInMock, [])

        //precondition
        List<CsiAggregation> mvs = pageCsiAggregationService.findAll(startedTime.toDate(), startedTime.toDate(), dailyInterval, JobGroup.list(), Page.list())
        0 == mvs.size()

        when: "getting csiAggregations through pageCsiAggregationService"
        List<CsiAggregation> calculatedMvs = pageCsiAggregationService.getOrCalculatePageCsiAggregations(startedTime.toDate(), startedTime.toDate(), dailyInterval, [jobGroup1], [page1])
        mvs = pageCsiAggregationService.findAll(startedTime.toDate(), startedTime.toDate(), dailyInterval, JobGroup.list(), Page.list())
        CsiAggregation wpmvMarked = mvs[0]

        then: "check if page csiAggregations are calculated"
        1 == calculatedMvs.size()
        calculatedMvs[0].isCalculated()
        11d == calculatedMvs[0].csByWptDocCompleteInPercent
        1 == mvs.size()
        wpmvMarked.isCalculated()
    }

    /**
     * Tests calculation of daily-pageAggregator-{@link CsiAggregation}s, which aren't calculated when new {@link EventResult}s get persisted.
     */
    void "test calculation_DailyInterval_MultipleHourlyMv_differentWeights"() {
        given: "a date, five csiAggregations and five csiAggregationUpdateEvent"
        DateTime startedTime = new DateTime(2013, 5, 16, 12, 12, 11)

        CsiAggregation mv1 = CsiAggregation.build(
                interval: hourlyInterval,
                aggregationType: AggregationType.MEASURED_EVENT,
                jobGroup: jobGroup1,
                page: page1,
                measuredEvent: measuredEvent1,
                browser: browser,
                location: location1,
                started: startedTime.toDate(),
                underlyingEventResultsByWptDocComplete: "1",
                csByWptDocCompleteInPercent: 1d,
                connectivityProfile: connectivityProfile)

        CsiAggregation mv2 = CsiAggregation.build(
                interval: hourlyInterval,
                aggregationType: AggregationType.MEASURED_EVENT,
                jobGroup: jobGroup1,
                page: page1,
                measuredEvent: measuredEvent1,
                browser: browser,
                location: location1,
                started: startedTime.plusHours(2).toDate(),
                underlyingEventResultsByWptDocComplete: "1",
                csByWptDocCompleteInPercent: 10d,
                connectivityProfile: connectivityProfile)

        CsiAggregation mv3 = CsiAggregation.build(
                interval: hourlyInterval,
                aggregationType: AggregationType.MEASURED_EVENT,
                jobGroup: jobGroup1,
                page: page1,
                measuredEvent: measuredEvent1,
                browser: browser,
                location: location1,
                started: startedTime.plusHours(10).toDate(),
                underlyingEventResultsByWptDocComplete: "1",
                csByWptDocCompleteInPercent: 11d,
                connectivityProfile: connectivityProfile)

        //MVs outside of Interval
        CsiAggregation mv4 = CsiAggregation.build(
                interval: hourlyInterval,
                aggregationType: AggregationType.MEASURED_EVENT,
                jobGroup: jobGroup1,
                page: page1,
                measuredEvent: measuredEvent1,
                browser: browser,
                location: location1,
                started: startedTime.minusMinutes(1).toDate(),
                underlyingEventResultsByWptDocComplete: "1",
                csByWptDocCompleteInPercent: 1000d,
                connectivityProfile: connectivityProfile)

        CsiAggregation mv5 = CsiAggregation.build(
                interval: hourlyInterval,
                aggregationType: AggregationType.MEASURED_EVENT,
                jobGroup: jobGroup1,
                page: page1,
                measuredEvent: measuredEvent1,
                browser: browser,
                location: location1,
                started: startedTime.plusMinutes(CsiAggregationInterval.DAILY + 1).toDate(),
                underlyingEventResultsByWptDocComplete: "1",
                csByWptDocCompleteInPercent: 1000d,
                connectivityProfile: connectivityProfile)

        List<CsiAggregation> csiAggregationList = [mv1,mv2,mv3,mv4,mv5]
        csiAggregationList.each {
            CsiAggregationUpdateEvent.build(
                    csiAggregationId: it.ident(),
                    updateCause: CsiAggregationUpdateEvent.UpdateCause.CALCULATED)
        }

        //mocking inner services
        List<WeightedCsiValue> weightedCsiValuesToReturnInMock = [
                new WeightedCsiValue(weightedValue: new WeightedValue(value: 1d, weight: 10d), underlyingEventResultIds: []),
                new WeightedCsiValue(weightedValue: new WeightedValue(value: 10d, weight: 1d), underlyingEventResultIds: []),
                new WeightedCsiValue(weightedValue: new WeightedValue(value: 11d, weight: 1d), underlyingEventResultIds: [])]
        mockCsiValueService(weightedCsiValuesToReturnInMock, [])

        //precondition
        List<CsiAggregation> mvs = pageCsiAggregationService.findAll(startedTime.toDate(), startedTime.toDate(), dailyInterval, JobGroup.list(), Page.list())
        0 == mvs.size()

        double browserWeightAllMvs = 1d
        double valueFirstMv = 1d
        double hourofdayWeightFirstMv = 10d
        double valueSecondMv = 10d
        double hourofdayWeightSecondMv = 1d
        double valueThirdMv = 11d
        double hourofdayWeightThirdMv = 1d
        double sumOfAllWeights = 12d

        when: "getting csiAggregations through pageCsiAggregationService"
        List<CsiAggregation> calculatedMvs = pageCsiAggregationService.getOrCalculatePageCsiAggregations(startedTime.toDate(), startedTime.toDate(), dailyInterval, [jobGroup1], [page1])
        mvs = pageCsiAggregationService.findAll(startedTime.toDate(), startedTime.toDate(), dailyInterval, JobGroup.list(), Page.list())
        CsiAggregation wpmvMarked = mvs[0]

        then: "check if page csiAggregations are calculated"
        1 == calculatedMvs.size()
        calculatedMvs[0].isCalculated()
                ((valueFirstMv * hourofdayWeightFirstMv * browserWeightAllMvs) + (valueSecondMv * hourofdayWeightSecondMv * browserWeightAllMvs) + (valueThirdMv * hourofdayWeightThirdMv * browserWeightAllMvs)) / sumOfAllWeights ==
                calculatedMvs[0].csByWptDocCompleteInPercent

        1 == mvs.size()
        wpmvMarked.isCalculated()

    }

    /**
     * Tests calculation of daily-pageAggregator-{@link CsiAggregation}s, which aren't calculated when new {@link EventResult}s get persisted.
     */
    void "test calculation_DailyInterval_MultipleHourlyMv_YesCalculatedNoData"() {
        given: "a date, two csiAggregations and two csiAggregationUpdateEvent"
        DateTime startedTime = new DateTime(2013, 5, 16, 12, 12, 11)
        //MVs outside of Interval
        CsiAggregation mv1 = CsiAggregation.build(
                interval: hourlyInterval,
                aggregationType: AggregationType.MEASURED_EVENT,
                jobGroup: jobGroup1,
                page: page1,
                measuredEvent: measuredEvent1,
                browser: browser,
                location: location1,
                started: startedTime.minusMinutes(1).toDate(),
                underlyingEventResultsByWptDocComplete: "1",
                csByWptDocCompleteInPercent: 1000d,
                connectivityProfile: connectivityProfile)

        CsiAggregation mv2 = CsiAggregation.build(
                interval: hourlyInterval,
                aggregationType: AggregationType.MEASURED_EVENT,
                jobGroup: jobGroup1,
                page: page1,
                measuredEvent: measuredEvent1,
                browser: browser,
                location: location1,
                started: startedTime.plusMinutes(CsiAggregationInterval.DAILY + 1).toDate(),
                underlyingEventResultsByWptDocComplete: "1",
                csByWptDocCompleteInPercent: 1000d,
                connectivityProfile: connectivityProfile)

        List<CsiAggregation> csiAggregationList = [mv1,mv2]
        csiAggregationList.each {
            CsiAggregationUpdateEvent.build(
                    csiAggregationId: it.ident(),
                    updateCause: CsiAggregationUpdateEvent.UpdateCause.CALCULATED)
        }

        //precondition
        List<CsiAggregation> mvs = pageCsiAggregationService.findAll(startedTime.toDate(), startedTime.toDate(), dailyInterval, JobGroup.list(), Page.list())
        0 == mvs.size()

        when: "getting csiAggregations through pageCsiAggregationService"
        List<CsiAggregation> calculatedMvs = pageCsiAggregationService.getOrCalculatePageCsiAggregations(startedTime.toDate(), startedTime.toDate(), dailyInterval, [jobGroup1], [page1])
        mvs = pageCsiAggregationService.findAll(startedTime.toDate(), startedTime.toDate(), dailyInterval, JobGroup.list(), Page.list())

        then: "check if page csiAggregations are calculated"
        1 == calculatedMvs.size()
        calculatedMvs[0].isCalculated()
        !calculatedMvs[0].csByWptDocCompleteInPercent
        1 == mvs.size()
    }

    /**
     * Mocks methods of {@link WeightingService}.
     */
    private void mockCsiValueService(List<WeightedCsiValue> toReturnFromGetWeightedCsiValues, List<WeightedCsiValue> toReturnFromGetWeightedCsiValuesByVisuallyComplete) {
        pageCsiAggregationService.csiValueService = Stub(CsiValueService){
            getWeightedCsiValues(_, _, _) >> toReturnFromGetWeightedCsiValues
            getWeightedCsiValuesByVisuallyComplete(_, _, _) >> toReturnFromGetWeightedCsiValuesByVisuallyComplete
        }
    }

    //testdata////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private void createTestDataForAllTests() {
        weeklyInterval = CsiAggregationInterval.build(name: 'weekly', intervalInMinutes: CsiAggregationInterval.WEEKLY)
        dailyInterval = CsiAggregationInterval.build(name: 'daily', intervalInMinutes: CsiAggregationInterval.DAILY)
        hourlyInterval = CsiAggregationInterval.build(name: 'hourly', intervalInMinutes: CsiAggregationInterval.HOURLY)
        browser = Browser.build(name: "Test")

        connectivityProfile = ConnectivityProfile.build(
                name: "DSL1",
                bandwidthDown: 6000,
                bandwidthUp: 512,
                latency: 40,
                packetLoss: 0,
                active: true
        )

        page1 = Page.build(name: pageName1)
        page2 = Page.build(name: pageName2)
        page3 = Page.build(name: pageName3)
        pages = [page1, page2, page3]
        CsiConfiguration csiConfiguration = CsiConfiguration.build(label: 'csiConf')
        configureCsiConfigurationsWithAllWeightsEqualToOne([csiConfiguration], browser, connectivityProfile)

        jobGroup1 = JobGroup.build(name: jobGroupName1, csiConfiguration: csiConfiguration)
        jobGroup2 = JobGroup.build(name: jobGroupName2, csiConfiguration: csiConfiguration)
        jobGroup3 = JobGroup.build(name: jobGroupName3, csiConfiguration: csiConfiguration)

        measuredEvent1 = MeasuredEvent.build(testedPage: page1, name: "measuredEventAggregator")

        WebPageTestServer wptServer = WebPageTestServer.build(
                label: "unusedLabel",
                proxyIdentifier: "unusedProxyIdentifier",
                active: false,
                baseUrl: "http://example.com"
        )
        location1 = Location.build(
                active: false,
                uniqueIdentifierForServer: "unusedIdentifier",
                location: "unusedIdentifier",
                label: "unusedIdentifier",
                browser: browser,
                wptServer: wptServer)

        List<CsiAggregation> csiAggregationsWithConnectivityProfile = []

        //with existing JobGroup and Page:
        csiAggregationsWithConnectivityProfile << CsiAggregation.build(
                started: startDate.toDate(),
                interval: weeklyInterval,
                aggregationType: AggregationType.PAGE,
                jobGroup: jobGroup1,
                page: page1,
                closedAndCalculated: "false")
        csiAggregationsWithConnectivityProfile << CsiAggregation.build(
                started: startDate.toDate(),
                interval: weeklyInterval,
                aggregationType: AggregationType.PAGE,
                jobGroup: jobGroup1,
                page: page2,
                closedAndCalculated: "false")
        pages.each {
            csiAggregationsWithConnectivityProfile << CsiAggregation.build(
                    started: startDate.toDate(),
                    interval: weeklyInterval,
                    aggregationType: AggregationType.PAGE,
                    jobGroup: jobGroup3,
                    page: it,
                    closedAndCalculated: "false")
        }

        csiAggregationsWithConnectivityProfile.each {
            it.connectivityProfile = connectivityProfile
            it.save(failOnError: true)
        }
    }

    private void configureCsiConfigurationsWithAllWeightsEqualToOne(List<CsiConfiguration> csiConfigurations, Browser browser, ConnectivityProfile connectivityProfile) {
        csiConfigurations.each { csiConfiguration ->
            csiConfiguration.browserConnectivityWeights.add(new BrowserConnectivityWeight(
                    browser: browser,
                    connectivity: connectivityProfile,
                    weight: 1
            ))

            pages.each { page ->
                csiConfiguration.pageWeights.add(new PageWeight(
                        page: page,
                        weight: 1
                ))
            }
            csiConfiguration.save()
        }
    }
}

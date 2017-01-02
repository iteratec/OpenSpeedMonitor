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
import de.iteratec.osm.measurement.script.Script
import de.iteratec.osm.report.chart.*
import de.iteratec.osm.result.CachedView
import de.iteratec.osm.result.EventResult
import de.iteratec.osm.result.JobResult
import de.iteratec.osm.result.MeasuredEvent
import de.iteratec.osm.util.PerformanceLoggingService
import de.iteratec.osm.util.ServiceMocker
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import groovy.mock.interceptor.MockFor
import org.joda.time.DateTime
import org.junit.Test
import spock.lang.Shared
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
@TestMixin(GrailsUnitTestMixin)
@TestFor(CsiSystemCsiAggregationService)
@Mock([MeanCalcService, CsiAggregation, CsiAggregationInterval, AggregatorType, Browser, JobGroup,
        Page, CsiAggregationUpdateEvent, CsiSystem, Job, ConnectivityProfile, JobResult, EventResult, WebPageTestServer,
        Location, Script, MeasuredEvent, JobGroupWeight, CsiConfiguration, PageWeight, TimeToCsMapping, CsiDay, PerformanceLoggingService])
class CsiSystemCsiAggregationServiceSpec extends Specification {

    @Shared
    static final ServiceMocker SERVICE_MOCKER = ServiceMocker.create()

    CsiAggregationInterval weeklyInterval, dailyInterval, hourlyInterval
    EventResult eventResult
    JobGroup jobGroup1, jobGroup2, jobGroup3
    Job job1
    ConnectivityProfile conn1
    CsiSystem csiSystem1, csiSystem2

    AggregatorType shop, csiSystemAggregator

    Page page1
    Browser browser

    DateTime startDate = new DateTime(2013, 5, 16, 0, 0, 0)
    String jobGroupName1 = 'myJobGroup1'
    String jobGroupName2 = 'myJobGroup2'
    String jobGroupName3 = 'myJobGroup3'

    CsiSystemCsiAggregationService serviceUnderTest

    def doWithSpring = {
        csiAggregationUtilService(CsiAggregationUtilService)
        csiAggregationDaoService(CsiAggregationDaoService)
        performanceLoggingService(PerformanceLoggingService)
    }

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
        createCsiAggregations()
        List<CsiSystem> groups1 = [csiSystem1]
        List<CsiSystem> groups2 = [csiSystem2]

        when:
        List<CsiAggregation> mvs1 = serviceUnderTest.findAll(startDate.toDate(), startDate.toDate(), weeklyInterval, groups1)
        List<CsiAggregation> mvs2 = serviceUnderTest.findAll(startDate.toDate(), startDate.toDate(), weeklyInterval, groups2)

        then:
        mvs1.size() == 1
        mvs2.size() == 1
    }

    /**
     * Tests calculation of daily-CsiSystem{@link CsiAggregation}s, which aren't calculated when new {@link EventResult}s get persisted.
     * In this test one single shopAggregator-{@link CsiAggregation}s exists, which should be the database of the calculation of the daily-csiSystem-{@link CsiAggregation}.
     */
    @Test
    void "don't calc daily-Mv if only findAll()"() {
        given:
        //test-specific data
        DateTime startedTime = new DateTime(2013, 5, 16, 12, 12, 11)
        List<WeightedCsiValue> weightedCsiValuesToReturnInMock = [
                new WeightedCsiValue(weightedValue: new WeightedValue(value: 12d, weight: 1d), underlyingEventResultIds: [1, 2, 3])]

        //mocking inner services
        mockWeightingService(weightedCsiValuesToReturnInMock, [])

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
        mockWeightingService(weightedCsiValuesToReturnInMock, [])

        when:
        //test execution
        List<CsiAggregation> calculatedMvs = serviceUnderTest.getOrCalculateCsiSystemCsiAggregations(startedTime.toDate(), startedTime.toDate(), dailyInterval, [csiSystem1])
        List<CsiAggregation> mvs = serviceUnderTest.findAll(startedTime.toDate(), startedTime.toDate(), dailyInterval)

        then:
        calculatedMvs.size() == 1
        calculatedMvs[0].isCalculated()
        calculatedMvs[0].csByWptDocCompleteInPercent == 12d

        mvs.size() == 1
    }

    /**
     * Tests calculation of daily-csiSystem-{@link CsiAggregation}s, which aren't calculated when new {@link EventResult}s get persisted.
     * In this test shopAggregator-{@link CsiAggregation}s with different weights exist.
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
        mockWeightingService(weightedCsiValuesToReturnInMock, [])

        when:
        List<CsiAggregation> calculatedMvs = serviceUnderTest.getOrCalculateCsiSystemCsiAggregations(startedTime.toDate(), startedTime.toDate(), dailyInterval, [csiSystem2])
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
     * In this test no shopAggregator-{@link CsiAggregation}s exist, which are database of the calculation of daily-csiSystem-{@link CsiAggregation}s. So all calculated values should have state {@link Calculated#YesNoData}
     */
    @Test
    void "calc multiple daily-Mv, but no value calculated"() {
        given:
        //test-specific data
        DateTime startedTime = new DateTime(2013, 5, 16, 12, 12, 11)

        //mocking inner services
        mockWeightingService([], [])

        when:
        List<CsiAggregation> calculatedMvs = serviceUnderTest.getOrCalculateCsiSystemCsiAggregations(startedTime.toDate(), startedTime.toDate(), dailyInterval, [csiSystem2])
        List<CsiAggregation> mvs = serviceUnderTest.findAll(startedTime.toDate(), startedTime.toDate(), dailyInterval)

        then:
        calculatedMvs.size() == 1
        calculatedMvs[0].aggregator == csiSystemAggregator
        calculatedMvs[0].isCalculated()
        calculatedMvs[0].csByWptDocCompleteInPercent == null
        calculatedMvs[0].underlyingEventResultsByWptDocCompleteAsList.isEmpty()

        mvs.size() == 1
    }

    @Test
    void "mark outdated CsiAggregation as outdated"() {
        given:
        //test-specific data
        createEventResult()
        createCsiAggregations()

        when:
        List<CsiAggregationUpdateEvent> beforeEventList = CsiAggregationUpdateEvent.findAll()
        List<CsiAggregation> beforeList = CsiAggregation.findAll()
        serviceUnderTest.markCaAsOutdated(startDate, eventResult, weeklyInterval)
        List<CsiAggregation> afterList = CsiAggregation.findAll()

        List<CsiAggregationUpdateEvent> afterEventList = CsiAggregationUpdateEvent.findAll()


        then:
        int expectedAfterSize = beforeList.size()
        afterList.size() == expectedAfterSize
        int expectedAfterEventSize = JobGroupWeight.findAllByJobGroup(eventResult.jobResult.job.jobGroup)*.csiSystem.unique(false).size()
        beforeEventList.empty
        afterEventList.size() == expectedAfterEventSize

    }

    // mocks

    /**
     * Mocks methods of {@link WeightingService}.
     */
    private void mockWeightingService(List<WeightedCsiValue> toReturnFromGetWeightedCsiValues, List<WeightedCsiValue> toReturnFromGetWeightedCsiValuesByVisuallyComplete) {
        def weightingService = new MockFor(WeightingService, true)
        weightingService.demand.getWeightedCsiValues(1..10000) {
            List<CsiValue> csiValues, CsiSystem csiSystem ->
                return toReturnFromGetWeightedCsiValues
        }
        weightingService.demand.getWeightedCsiValuesByVisuallyComplete(1..10000) {
            List<CsiValue> csiValues, CsiSystem csiSystem ->
                return toReturnFromGetWeightedCsiValuesByVisuallyComplete
        }
        serviceUnderTest.weightingService = weightingService.proxyInstance()
    }

    /**
     * Mocks methods of {@link CsiAggregationUpdateEventDaoService}.
     */
    private void mockCsiAggregationUpdateEventDaoService() {
        def csiAggregationUpdateEventDaoService = new MockFor(CsiAggregationUpdateEventDaoService, true)
        csiAggregationUpdateEventDaoService.demand.createUpdateEvent(1..10000) {
            Long csiAggregationId, CsiAggregationUpdateEvent.UpdateCause cause ->

                new CsiAggregationUpdateEvent(
                        dateOfUpdate: new Date(),
                        csiAggregationId: csiAggregationId,
                        updateCause: cause
                ).save(failOnError: true, flush: true)

        }
        serviceUnderTest.csiAggregationUpdateEventDaoService = csiAggregationUpdateEventDaoService.proxyInstance()
    }

    private mocksCommonForAllTests(CsiSystemCsiAggregationService serviceUnderTest) {
        serviceUnderTest.csiAggregationUtilService = grailsApplication.mainContext.getBean('csiAggregationUtilService')
        serviceUnderTest.csiAggregationDaoService = grailsApplication.mainContext.getBean('csiAggregationDaoService')

        Map<String, JobGroup> idAsStringToJobGroupMap = ['1': jobGroup1, '2': jobGroup2, '3': jobGroup3]

        SERVICE_MOCKER.mockShopCsiAggregationService(serviceUnderTest, [new CsiAggregation()])

        SERVICE_MOCKER.mockPerformanceLoggingService(serviceUnderTest)

        mockCsiAggregationUpdateEventDaoService()

        serviceUnderTest.performanceLoggingService = grailsApplication.mainContext.getBean('performanceLoggingService')

    }

    private void createTestDataCommonForAllTests() {
        weeklyInterval = new CsiAggregationInterval(name: 'weekly', intervalInMinutes: CsiAggregationInterval.WEEKLY).save(failOnError: true, flush: true)
        dailyInterval = new CsiAggregationInterval(name: 'daily', intervalInMinutes: CsiAggregationInterval.DAILY).save(failOnError: true, flush: true)
        hourlyInterval = new CsiAggregationInterval(name: 'hourly', intervalInMinutes: CsiAggregationInterval.HOURLY).save(failOnError: true, flush: true)

        shop = new AggregatorType(name: AggregatorType.SHOP, measurandGroup: MeasurandGroup.NO_MEASURAND).save(failOnError: true, flush: true)
        csiSystemAggregator = new AggregatorType(name: AggregatorType.CSI_SYSTEM, measurandGroup: MeasurandGroup.NO_MEASURAND).save(failOnError: true, flush: true)
        new AggregatorType(name: AggregatorType.MEASURED_EVENT, measurandGroup: MeasurandGroup.NO_MEASURAND).save(failOnError: true, flush: true)

        CsiConfiguration csiConfiguration = TestDataUtil.createCsiConfiguration()

        jobGroup1 = new JobGroup(name: jobGroupName1).save(validate: false)
        jobGroup1.csiConfiguration = csiConfiguration
        jobGroup2 = new JobGroup(name: jobGroupName2).save(validate: false)
        jobGroup2.csiConfiguration = csiConfiguration
        jobGroup3 = new JobGroup(name: jobGroupName3).save(validate: false)
        jobGroup3.csiConfiguration = csiConfiguration

        browser = new Browser(name: "Test").save(failOnError: true);

        csiSystem1 = new CsiSystem(label: "system1")
        csiSystem1.addToJobGroupWeights(new JobGroupWeight(jobGroup: jobGroup1, weight: 1.0, csiSystem: csiSystem1))
        csiSystem1.addToJobGroupWeights(new JobGroupWeight(jobGroup: jobGroup2, weight: 1.0, csiSystem: csiSystem1))
        csiSystem1.addToJobGroupWeights(new JobGroupWeight(jobGroup: jobGroup3, weight: 1.0, csiSystem: csiSystem1))
        csiSystem1.save(failOnError: true, flush: true)

        csiSystem2 = new CsiSystem(label: "system2")
        csiSystem2.addToJobGroupWeights(new JobGroupWeight(jobGroup: jobGroup1, weight: 1.0, csiSystem: csiSystem1))
        csiSystem2.addToJobGroupWeights(new JobGroupWeight(jobGroup: jobGroup2, weight: 2.0, csiSystem: csiSystem1))
        csiSystem2.addToJobGroupWeights(new JobGroupWeight(jobGroup: jobGroup3, weight: 3.0, csiSystem: csiSystem1))
        csiSystem2.save(failOnError: true, flush: true)


    }

    private void createCsiAggregations() {
        //with existing JobGroup:
        new CsiAggregation(interval: weeklyInterval, aggregator: csiSystemAggregator, csiSystem: csiSystem1, started: startDate.toDate()).save(validate: false)
        new CsiAggregation(interval: weeklyInterval, aggregator: csiSystemAggregator, csiSystem: csiSystem2, started: startDate.toDate()).save(validate: false)
        //not with existing CsiSystem:
        new CsiAggregation(interval: weeklyInterval, aggregator: csiSystemAggregator, csiSystem: null, started: startDate.toDate()).save(validate: false)
    }

    private void createEventResult() {
        WebPageTestServer server = new WebPageTestServer(
                baseUrl: 'http://server1.wpt.server.de',
                active: true,
                label: 'server 1 - wpt server',
                proxyIdentifier: 'server 1 - wpt server',
                dateCreated: new Date(),
                lastUpdated: new Date()
        ).save(failOnError: true, flush: true)
        Location ffAgent1 = new Location(
                active: true,
                location: 'physNetLabAgent01-FF',
                label: 'physNetLabAgent01 - FF up to date',
                browser: browser,
                dateCreated: new Date(),
                lastUpdated: new Date(),
                wptServer: server
        ).save(failOnError: true, flush: true)

        job1 = TestDataUtil.createJob("job1", TestDataUtil.createScript("script1", "", "auf gehts"), ffAgent1, jobGroup1, "", 1, false, 20)
        conn1 = TestDataUtil.createConnectivityProfile("conn1")

        JobResult jobResult1 = new JobResult(
                job: job1,
                date: startDate.toDate(),
                testId: '1',
                description: '',
                jobConfigLabel: job1.label,
                jobConfigRuns: job1.runs,
                jobGroupName: job1.jobGroup.name,
                frequencyInMin: 5,
                locationLocation: job1.location.location,
                locationBrowser: job1.location.browser.name,
                httpStatusCode: 200,
        ).save(failOnError: true, flush: true)

        page1 = TestDataUtil.createPage("page1")

        MeasuredEvent measuredEvent = TestDataUtil.createMeasuredEvent("meEvent1", page1)

        eventResult = new EventResult(
                numberOfWptRun: 1,
                cachedView: CachedView.UNCACHED,
                medianValue: true,
                docCompleteIncomingBytes: 1000,
                docCompleteRequests: 1000,
                docCompleteTimeInMillisecs: 1000,
                domTimeInMillisecs: 1000,
                firstByteInMillisecs: 1000,
                fullyLoadedIncomingBytes: 1000,
                fullyLoadedRequestCount: 1000,
                fullyLoadedTimeInMillisecs: 1000,
                loadTimeInMillisecs: 1000,
                startRenderInMillisecs: 1000,
                downloadAttempts: 1,
                firstStatusUpdate: startDate.toDate(),
                lastStatusUpdate: startDate.toDate(),
                wptStatus: 0,
                validationState: 'validationState',
                harData: 'harData',
                csByWptDocCompleteInPercent: 1,
                jobResult: jobResult1,
                jobResultDate: startDate.toDate(),
                jobResultJobConfigId: jobResult1.job.ident(),
                jobGroup: jobGroup1,
                measuredEvent: measuredEvent,
                page: page1,
                browser: browser,
                location: ffAgent1,
                speedIndex: EventResult.SPEED_INDEX_DEFAULT_VALUE,
                connectivityProfile: conn1,
                customConnectivityName: null,
                noTrafficShapingAtAll: false
        ).save(failOnError: true, flush: true)
    }
}

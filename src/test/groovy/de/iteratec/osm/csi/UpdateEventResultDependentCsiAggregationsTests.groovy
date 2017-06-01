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
import de.iteratec.osm.OsmConfigCacheService
import de.iteratec.osm.OsmConfiguration
import de.iteratec.osm.measurement.environment.Browser
import de.iteratec.osm.measurement.environment.BrowserAlias
import de.iteratec.osm.measurement.environment.Location
import de.iteratec.osm.measurement.environment.WebPageTestServer
import de.iteratec.osm.measurement.schedule.ConnectivityProfile
import de.iteratec.osm.measurement.schedule.ConnectivityProfileService
import de.iteratec.osm.measurement.schedule.Job
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.measurement.script.Script
import de.iteratec.osm.report.chart.*
import de.iteratec.osm.result.*
import de.iteratec.osm.util.PerformanceLoggingService
import de.iteratec.osm.util.ServiceMocker
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.junit.Test

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertNull

/**
 * Tests the updating of hourly event-{@link CsiAggregation}s when a new {@link EventResult} is coming in.
 */
@TestMixin(GrailsUnitTestMixin)
@TestFor(EventCsiAggregationService)
@Mock([Browser, BrowserAlias, JobGroup, Location, MeasuredEvent, Page, WebPageTestServer, CsiAggregation, CsiAggregationInterval,
        AggregatorType, Location, EventResult, JobResult, Job, OsmConfiguration, CsiDay, Script, CsiAggregationUpdateEvent,
        ConnectivityProfile, CsiConfiguration])
class UpdateEventResultDependentCsiAggregationsTests {

    static final double DELTA = 1e-15
    static final DateTime resultsExecutionTime = new DateTime(2012, 1, 1, 0, 0, 0, DateTimeZone.UTC)
    static final String measuredEventName = 'HP:::BV1 - Step 01'
    static final String pageNameHp = 'HP'
    static final String browserName = 'IE'
    static final String locationLocation = 'myLocation'
    static final String serverLabel = 'server 1 - wpt server'
    static final String labelJobOfCsiGroup1 = 'HP-Job of csiGroup1'
    static final String labelJobOfCsiGroup2 = 'HP-Job of csiGroup2'
    static final String testIdOfJobRunCsiGroup1 = 'testId1'
    static final String testIdOfJobRunCsiGroup2 = 'testId2'
    static final String group1Name = 'group1'
    static final String group2Name = 'group2'

    CsiAggregationInterval hourly
    AggregatorType measuredEvent
    ConnectivityProfile connectivityProfile

    EventCsiAggregationService serviceUnderTest
    ServiceMocker mockGenerator

    def doWithSpring = {
        performanceLoggingService(PerformanceLoggingService)
        csiValueService(CsiValueService)
        configService(ConfigService)
        connectivityProfileService(ConnectivityProfileService)
        osmConfigCacheService(OsmConfigCacheService)
    }

    void setUp() {
        Job.metaClass.static.performQuartzScheduling << { Boolean active ->

        }

        serviceUnderTest = service

        //mocks common for all tests
        serviceUnderTest.performanceLoggingService = grailsApplication.mainContext.getBean('performanceLoggingService')
        mockGenerator = ServiceMocker.create()
        mockGenerator.mockOsmConfigCacheService(serviceUnderTest)
        mockGenerator.mockCsiAggregationUpdateEventDaoService(serviceUnderTest)
        serviceUnderTest.csiValueService = grailsApplication.mainContext.getBean('csiValueService')
        serviceUnderTest.csiValueService.osmConfigCacheService = grailsApplication.mainContext.getBean('osmConfigCacheService')
        serviceUnderTest.csiValueService.osmConfigCacheService.configService = grailsApplication.mainContext.getBean('configService')
        createTestDataForAllTests()
        initializeFields()

    }

    void tearDown() {
        deleteTestData()
    }

    //tests////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Tests the update of dependent hourly event-{@link CsiAggregation}s.
     */
    @Test
    void testUpdateDependentCsiAggregations() {

        //create test-specific data
        JobResult run = JobResult.findByTestId(testIdOfJobRunCsiGroup1)
        EventResult result1 = createNewResult(run, 50, null, JobGroup.findByName(group1Name))
        EventResult result2 = createNewResult(run, 60, 60, JobGroup.findByName(group1Name))
        EventResult result3 = createNewResult(run, 70, 70, JobGroup.findByName(group1Name))
        EventResult result4 = createNewResult(run, 80, 80, JobGroup.findByName(group1Name))
        EventResult result5 = createNewResult(run, 90, 90, JobGroup.findByName(group1Name))

        mockGenerator.mockOsmConfigCacheService(result1)
        mockGenerator.mockOsmConfigCacheService(result2)
        mockGenerator.mockOsmConfigCacheService(result3)
        mockGenerator.mockOsmConfigCacheService(result4)
        mockGenerator.mockOsmConfigCacheService(result5)

        //execute test

        serviceUnderTest.createOrUpdateHourlyValue(resultsExecutionTime, result1)

        //assertions (and following executions of tested method)

        List<CsiAggregation> hourlyMvs = serviceUnderTest.findAll(resultsExecutionTime.toDate(), resultsExecutionTime.toDate(), hourly, result1.connectivityProfile)
        Integer countEvents = 1
        assertEquals(countEvents, hourlyMvs.size())

        CsiAggregation calculated = hourlyMvs[0]
        Double expectedCsByDocComplete = 50
        proofHourlyCsiAggregation(calculated, true, 1, expectedCsByDocComplete, null)

        serviceUnderTest.createOrUpdateHourlyValue(resultsExecutionTime, result2)
        expectedCsByDocComplete = (50 + 60) / 2
        proofHourlyCsiAggregation(calculated, true, 2, expectedCsByDocComplete, 60 / 2)

        serviceUnderTest.createOrUpdateHourlyValue(resultsExecutionTime, result3)
        serviceUnderTest.createOrUpdateHourlyValue(resultsExecutionTime, result4)
        serviceUnderTest.createOrUpdateHourlyValue(resultsExecutionTime, result5)
        expectedCsByDocComplete = (50 + 60 + 70 + 80 + 90) / 5
        Double expectedCsByVisuallyComplete = (60 + 70 + 80 + 90) / 5
        proofHourlyCsiAggregation(calculated, true, 5, expectedCsByDocComplete, expectedCsByVisuallyComplete)

    }

    /**
     * Tests the update of dependent hourly event-{@link CsiAggregation}s for different CSI-{@link JobGroup}s.
     */
    @Test
    void testUpdateDependentCsiAggregationsForMultipleCsiGroups() {
        //create test-specific data

        JobResult runOfJobOfCsiGroup1 = JobResult.findByTestId(testIdOfJobRunCsiGroup1)
        JobResult runOfJobOfCsiGroup2 = JobResult.findByTestId(testIdOfJobRunCsiGroup2)
        JobGroup group1 = JobGroup.findByName(group1Name)
        JobGroup group2 = JobGroup.findByName(group2Name)
        Long jobGroup1Id = group1.ident()
        Long jobGroup2Id = group2.ident()
        EventResult result1OfCsiGroup1 = createNewResult(runOfJobOfCsiGroup1, 80, null, group1)
        EventResult result2OfCsiGroup1 = createNewResult(runOfJobOfCsiGroup1, 90, 90, group1)
        EventResult result1OfCsiGroup2 = createNewResult(runOfJobOfCsiGroup2, 10, 10, group2)
        EventResult result2OfCsiGroup2 = createNewResult(runOfJobOfCsiGroup2, 20, 20, group2)
        EventResult result3OfCsiGroup2 = createNewResult(runOfJobOfCsiGroup2, 30, 30, group2)

        //execute test
        serviceUnderTest.createOrUpdateHourlyValue(resultsExecutionTime, result1OfCsiGroup1)
        serviceUnderTest.createOrUpdateHourlyValue(resultsExecutionTime, result2OfCsiGroup1)
        serviceUnderTest.createOrUpdateHourlyValue(resultsExecutionTime, result1OfCsiGroup2)
        serviceUnderTest.createOrUpdateHourlyValue(resultsExecutionTime, result2OfCsiGroup2)
        serviceUnderTest.createOrUpdateHourlyValue(resultsExecutionTime, result3OfCsiGroup2)

        //assertions

        List<CsiAggregation> hourlyMvs = serviceUnderTest.findAll(resultsExecutionTime.toDate(), resultsExecutionTime.toDate(), hourly)
        Integer countEvents = 2
        assertEquals(countEvents, hourlyMvs.size())

        List<CsiAggregation> hourlyMvsOfGroup1 = hourlyMvs.findAll { it.jobGroupId == jobGroup1Id }
        assertEquals(1, hourlyMvsOfGroup1.size())
        proofHourlyCsiAggregation(hourlyMvsOfGroup1[0], true, 2, 85, 45)

        List<CsiAggregation> hourlyMvsOfGroup2 = hourlyMvs.findAll { it.jobGroupId == jobGroup2Id }
        assertEquals(1, hourlyMvsOfGroup2.size())
        proofHourlyCsiAggregation(hourlyMvsOfGroup2[0], true, 3, 20, 20)

    }

    /**
     * Executes assertions to proof calculated {@link CsiAggregation}.
     * @param hourlyCsiAggregation
     * {@link CsiAggregation} to proof
     * @param expectedCalculatedState
     * 			Calculated-state of calculated {@link CsiAggregation} to expect.
     * @param expectedResultCount
     * 			Count of {@link EventResult}s of calculated {@link CsiAggregation} to expect.
     * @param expectedByDocComplete
     * 			Double-value of calculated {@link CsiAggregation} to expect.
     */
    private void proofHourlyCsiAggregation(
            CsiAggregation hourlyCsiAggregation,
            boolean expectedCalculatedState,
            Integer expectedResultCount,
            Double expectedByDocComplete,
            Double expectedByVisuallyComplete
    ) {

        assertEquals(resultsExecutionTime.toDate(), hourlyCsiAggregation.started)
        assertEquals(hourly.intervalInMinutes, hourlyCsiAggregation.interval.intervalInMinutes)
        assertEquals(measuredEvent.name, hourlyCsiAggregation.aggregator.name)
        assertEquals(expectedCalculatedState, hourlyCsiAggregation.isCalculated())
        assertEquals(expectedResultCount, hourlyCsiAggregation.countUnderlyingEventResultsByWptDocComplete())
        assertEquals(expectedByDocComplete, hourlyCsiAggregation.csByWptDocCompleteInPercent, DELTA)
        if (expectedByVisuallyComplete != null) {
            assertEquals(expectedByVisuallyComplete, hourlyCsiAggregation.csByWptVisuallyCompleteInPercent, DELTA)
        } else {
            assertNull(hourlyCsiAggregation.csByWptVisuallyCompleteInPercent)
        }
    }

    /**
     * Creates a new {@link EventResult} and persists it.
     * @param jobResult
     * @param csByDocComplete
     * @return
     */
    private EventResult createNewResult(JobResult jobResult, Integer csByDocComplete, Integer csByVisuallyComplete, JobGroup jobGroup) {
        MeasuredEvent event = MeasuredEvent.findByName(measuredEventName)
        Browser browser = Browser.findByName(browserName)
        EventResult returnValue = new EventResult(
                numberOfWptRun: 1,
                cachedView: CachedView.UNCACHED,
                medianValue: true,
                docCompleteBytesIn: 1,
                docCompleteRequests: 1,
                docCompleteTimeInMillisecs: 1000,
                domTimeInMillisecs: 1,
                firstByteInMillisecs: 1,
                fullyLoadedBytesIn: 1,
                fullyLoadedRequests: 1,
                fullyLoadedTimeInMillisecs: 1,
                loadTimeInMillisecs: 1,
                startRenderInMillisecs: 1,
                downloadAttempts: 1,
                firstStatusUpdate: resultsExecutionTime.toDate(),
                lastStatusUpdate: resultsExecutionTime.toDate(),
                wptStatus: 0,
                validationState: 'validationState',
                csByWptDocCompleteInPercent: csByDocComplete,
                csByWptVisuallyCompleteInPercent: csByVisuallyComplete,
                jobResult: jobResult,
                jobResultDate: jobResult.date,
                jobResultJobConfigId: jobResult.job.ident(),
                jobGroup: jobGroup,
                measuredEvent: event,
                page: event.testedPage,
                browser: browser,
                location: jobResult.job.location,
                speedIndex: EventResult.SPEED_INDEX_DEFAULT_VALUE,
                connectivityProfile: connectivityProfile,
                customConnectivityName: null,
                noTrafficShapingAtAll: false).save(failOnError: true)

        jobResult.save(failOnError: true)
        mockGenerator.mockOsmConfigCacheService(returnValue)

        return returnValue
    }

    //testdata////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private void createTestDataForAllTests() {
        createConnectivityProfile()
        createOsmConfiguration()
        createCsiAggregationIntervals()
        createAggregatorTypes()
        createPages()
        createBrowsers()
        createServer()
        createLocations()
        createJobGroups()
        createJobConfigRunAndResult()
        createMeasuredEvents()
    }

    private void initializeFields() {
        hourly = CsiAggregationInterval.findByIntervalInMinutes(CsiAggregationInterval.HOURLY)
        measuredEvent = AggregatorType.findByName(AggregatorType.MEASURED_EVENT)
    }

    private void createConnectivityProfile() {
        connectivityProfile = TestDataUtil.createConnectivityProfile("conn1")
        connectivityProfile.connectivityProfileService = grailsApplication.mainContext.getBean('connectivityProfileService')
    }

    private void createOsmConfiguration() {
        OsmConfiguration.findAll() ?: new OsmConfiguration(
                detailDataStorageTimeInWeeks: 2,
                defaultMaxDownloadTimeInMinutes: 60,
                minDocCompleteTimeInMillisecs: 250,
                maxDocCompleteTimeInMillisecs: 180000
        ).save(failOnError: true)
    }

    private void createJobGroups() {
        //create CsiConfiguration for JobGroups
        List<Page> allPages = [new Page(name: 'HP'), new Page(name: 'ADS')]
        CsiConfiguration csiConfiguration = TestDataUtil.createCsiConfiguration()
        csiConfiguration.timeToCsMappings = TestDataUtil.createTimeToCsMappingForAllPages(allPages)
        //create JobGroups
        JobGroup.findByName(group1Name) ?: new JobGroup(
                name: group1Name,
                csiConfiguration: csiConfiguration).save(failOnError: true)
        JobGroup.findByName(group2Name) ?: new JobGroup(
                name: group2Name,
                csiConfiguration: csiConfiguration).save(failOnError: true)
    }

    private void createAggregatorTypes() {
        new AggregatorType(name: AggregatorType.MEASURED_EVENT, measurandGroup: MeasurandGroup.NO_MEASURAND).save(failOnError: true)
    }

    private void createCsiAggregationIntervals() {
        new CsiAggregationInterval(
                name: "hourly",
                intervalInMinutes: CsiAggregationInterval.HOURLY
        ).save(failOnError: true)
    }

    private void createPages() {
        new Page(name: pageNameHp).save(failOnError: true)
    }

    private void createBrowsers() {
        new Browser(name: browserName)
                .addToBrowserAliases(alias: "IE")
                .addToBrowserAliases(alias: "IE8")
                .addToBrowserAliases(alias: "Internet Explorer")
                .addToBrowserAliases(alias: "Internet Explorer 8")
                .save(failOnError: true)
    }

    private void createServer() {
        WebPageTestServer server1
        server1 = new WebPageTestServer(
                baseUrl: 'http://server1.wpt.server.de',
                active: true,
                label: serverLabel,
                proxyIdentifier: 'server 1 - wpt server',
                dateCreated: new Date(),
                lastUpdated: new Date()
        ).save(failOnError: true)
    }

    private void createLocations() {
        WebPageTestServer server1 = WebPageTestServer.findByLabel(serverLabel)
        Browser browser = Browser.findByName(browserName)
        new Location(
                active: true,
                location: locationLocation,
                label: 'physNetLabAgent01 - FF up to date',
                browser: browser,
                wptServer: server1,
                dateCreated: new Date(),
                lastUpdated: new Date()
        ).save(failOnError: true)
    }

    private void createJobConfigRunAndResult() {
        //job
        Job job1, job2
        Page homepage = Page.findByName(pageNameHp)
        Location agent = Location.findByLocation(locationLocation)
        JobGroup csiGroup1 = JobGroup.findByName(group1Name)
        JobGroup csiGroup2 = JobGroup.findByName(group2Name)
        Script script = Script.createDefaultScript('Unnamed').save(failOnError: true)
        job1 = new Job(
                active: false,
                label: labelJobOfCsiGroup1,
                description: 'job1',
                location: agent,
                frequencyInMin: 5,
                runs: 1,
                jobGroup: csiGroup1,
                script: script,
                maxDownloadTimeInMinutes: 60,
                customConnectivityProfile: false,
                connectivityProfile: connectivityProfile,
                noTrafficShapingAtAll: false
        ).save(failOnError: true)

        job2 = new Job(
                active: false,
                label: labelJobOfCsiGroup2,
                description: 'job2',
                location: agent,
                frequencyInMin: 5,
                runs: 1,
                jobGroup: csiGroup2,
                script: script,
                maxDownloadTimeInMinutes: 60,
                customConnectivityProfile: false,
                connectivityProfile: connectivityProfile,
                noTrafficShapingAtAll: false
        ).save(failOnError: true)

        //wptjobrun
        new JobResult(
                job: job1,
                date: resultsExecutionTime.toDate(),
                testId: testIdOfJobRunCsiGroup1,
                description: '',
                jobConfigLabel: job1.label,
                jobConfigRuns: job1.runs,
                frequencyInMin: 5,
                locationLocation: locationLocation,
                locationBrowser: browserName,
                httpStatusCode: 200,
                testedPage: homepage,
                jobGroupName: group1Name
        ).save(failOnError: true)
        new JobResult(
                job: job2,
                date: resultsExecutionTime.toDate(),
                testId: testIdOfJobRunCsiGroup2,
                description: '',
                jobConfigLabel: job2.label,
                jobConfigRuns: job2.runs,
                frequencyInMin: 5,
                locationLocation: locationLocation,
                locationBrowser: browserName,
                httpStatusCode: 200,
                testedPage: homepage,
                jobGroupName: group2Name
        ).save(failOnError: true)
    }

    private void createMeasuredEvents() {
        Page homepage = Page.findByName(pageNameHp)
        new MeasuredEvent(name: measuredEventName, testedPage: homepage).save(failOnError: true)
    }
    /**
     * Deleting testdata.
     */
    private void deleteTestData() {
        OsmConfiguration.list()*.delete(flush: true)
        CsiAggregation.list()*.delete(flush: true)
        CsiDay.list()*.delete(flush: true)
        EventResult.list()*.delete(flush: true)
        JobResult.list()*.delete(flush: true)
        Job.list()*.delete(flush: true)
        Location.list()*.delete(flush: true)
        Browser.list()*.delete(flush: true)
        WebPageTestServer.list()*.delete(flush: true)
        Page.list()*.delete(flush: true)
        JobGroup.list()*.delete(flush: true)
    }
}

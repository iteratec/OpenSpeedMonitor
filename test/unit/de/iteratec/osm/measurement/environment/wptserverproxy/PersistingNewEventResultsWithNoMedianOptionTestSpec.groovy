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

package de.iteratec.osm.measurement.environment.wptserverproxy

import de.iteratec.osm.ConfigService
import de.iteratec.osm.csi.MeasuredValueUpdateService
import de.iteratec.osm.csi.Page
import de.iteratec.osm.csi.TestDataUtil
import de.iteratec.osm.csi.transformation.TimeToCsMappingService
import de.iteratec.osm.measurement.environment.Browser
import de.iteratec.osm.measurement.environment.BrowserAlias
import de.iteratec.osm.measurement.environment.Location
import de.iteratec.osm.measurement.environment.WebPageTestServer
import de.iteratec.osm.measurement.schedule.Job
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.measurement.schedule.JobGroupType
import de.iteratec.osm.measurement.schedule.JobService
import de.iteratec.osm.measurement.script.Script
import de.iteratec.osm.report.external.MetricReportingService
import de.iteratec.osm.result.*
import de.iteratec.osm.result.detail.HarParserService
import de.iteratec.osm.result.detail.WaterfallEntry
import de.iteratec.osm.result.detail.WebPerformanceWaterfall
import de.iteratec.osm.util.PerformanceLoggingService
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import groovy.util.slurpersupport.GPathResult
import org.junit.Before
import org.junit.Test

import static org.junit.Assert.assertEquals

/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
@TestFor(LocationAndResultPersisterService)
@Mock([WebPageTestServer, Browser, Location, Job, JobResult, EventResult, BrowserAlias, Page, MeasuredEvent, JobGroup, Script, WebPerformanceWaterfall, WaterfallEntry])
class PersistingNewEventResultsWithNoMedianOptionTestSpec {

    WebPageTestServer server, server2

    JobGroup undefinedJobGroup;

    Browser undefinedBrowser;

    LocationAndResultPersisterService serviceUnderTest

    @Before
    void setUp() {
        serviceUnderTest = service

        server = new WebPageTestServer(
                label: "TestServer 1",
                proxyIdentifier: "TestServer1",
                baseUrl: "http://wptUnitTest.dev.hh.iteratec.local",
                active: true
        ).save(failOnError: true, validate: false)

        undefinedJobGroup=new JobGroup(
                name: JobGroup.UNDEFINED_CSI,
                groupType: JobGroupType.CSI_AGGREGATION
        );
        undefinedJobGroup.save(failOnError: true);

        //creating test-data common to all tests
        createPages()
        createBrowsers()
        createLocationsAndJobs()
        //mocks common for all tests
        mockMetricReportingService()
        mockHarParserService()

        serviceUnderTest.configService = [ getDetailDataStorageTimeInWeeks: { 12 },
                                           getDefaultMaxDownloadTimeInMinutes: { 60 } ] as ConfigService
        serviceUnderTest.performanceLoggingService = new PerformanceLoggingService()
    }

    void createLocationsAndJobs(){
        Location testLocation = TestDataUtil.createLocation(server, 'test-location', Browser.findByName('IE'), true)
        if(Job.findByLabel('FF_Otto_multistep') == null) TestDataUtil.createJob('FF_Otto_multistep', Script.createDefaultScript('FF_Otto_multistep'), testLocation, undefinedJobGroup, '', 1 , false, 60)
        if(Job.findByLabel('IE_otto_hp_singlestep') == null) TestDataUtil.createJob('IE_otto_hp_singlestep', Script.createDefaultScript('IE_otto_hp_singlestep'), testLocation, undefinedJobGroup, '', 1 , false, 60)
    }

    @Test
    void testCreatedEventsAfterListeningToMultistepResultAndPersistNonMedian() {
        //create test-specific data
        String nameOfResultXmlFile = 'Result_wptserver2.13-multistep7_5Runs_3Events_JustFirstView_WithVideo.xml'
        File file = new File("test/resources/WptResultXmls/${nameOfResultXmlFile}")
        GPathResult xmlResult = new XmlSlurper().parse(file)
        String har = new File('test/resources/HARs/singleResult.har').getText()
        deleteResults()
        setNonMedianPersistanceForJob('FF_Otto_multistep', true)

        //mocking of inner services

        mockMeasuredValueUpdateService()
        mockTimeToCsMappingService()
        mockPageService()
        mockJobService()
        mockMeasuredValueTagService('notTheConcernOfThisTest')

        // Mock Location needed!
        mockLocation(xmlResult.data.location.toString(), undefinedBrowser, server);

        //test execution

        serviceUnderTest.listenToResult(xmlResult, har, server)

        //assertions

        //check for job-runs
        Collection<JobResult> jobRuns = JobResult.list()
        assertEquals("Test persisted JobResults", 1, jobRuns.size())

        //check for steps
        List<MeasuredEvent> steps = MeasuredEvent.list()
        assertEquals("Test expected number of steps", 3, steps.size())

        //check for results
        List<EventResult> allResults = EventResult.list()
        assertEquals("Test expected size of all results", 15, allResults.size())

        assertEquals("Test expected size of all median values", 3, EventResult.findAllByMedianValue(true).size())

        assertEquals("Test expected size of all non median values", 12, EventResult.findAllByMedianValue(false).size())
    }

    void setNonMedianPersistanceForJob(String jobLabel, boolean nonMedianPersistance) {
        Job job = Job.findByLabel(jobLabel)
        job.persistNonMedianResults = nonMedianPersistance
        job.save(failOnError: true)
    }

    @Test
    void testCreatedEventsAfterListeningToMultistepResultAndPersistOnlyMedian() {
        //create test-specific data
        String nameOfResultXmlFile = 'Result_wptserver2.13-multistep7_5Runs_3Events_JustFirstView_WithVideo.xml'
        File file = new File("test/resources/WptResultXmls/${nameOfResultXmlFile}")
        GPathResult xmlResult = new XmlSlurper().parse(file)
        String har = new File('test/resources/HARs/singleResult.har').getText()
        deleteResults()
        setNonMedianPersistanceForJob('FF_Otto_multistep', false)

        //mocking of inner services

        mockMeasuredValueUpdateService()
        mockTimeToCsMappingService()
        mockPageService()
        mockJobService()
        mockMeasuredValueTagService('notTheConcernOfThisTest')

        // Mock Location needed!
        mockLocation(xmlResult.data.location.toString(), undefinedBrowser, server);

        //test execution
        serviceUnderTest.listenToResult(xmlResult, har, server)

        //assertions

        //check for job-runs
        Collection<JobResult> jobRuns = JobResult.list()
        assertEquals("Test persisted JobResults", 1, jobRuns.size())

        //check for steps
        List<MeasuredEvent> steps = MeasuredEvent.list()
        assertEquals("Test expected number of steps", 3, steps.size())

        //check for results
        List<EventResult> allResults = EventResult.list()
        assertEquals("Test expected size of all results", 3, allResults.size())

        assertEquals("Test expected size of all median values", 3, EventResult.findAllByMedianValue(true).size())

        assertEquals("Test expected size of all non median values", 0, EventResult.findAllByMedianValue(false).size())
    }

    @Test
    void testCreatedEventsAfterListeningToSinglestepResultAndPersistNonMedian() {
        //create test-specific data
        String nameOfResultXmlFile = 'Result_wptserver2.15_singlestep_5Runs_WithVideo.xml'
        File file = new File("test/resources/WptResultXmls/${nameOfResultXmlFile}")
        GPathResult xmlResult = new XmlSlurper().parse(file)
        String har = new File('test/resources/HARs/singleResult.har').getText()
        deleteResults()
        setNonMedianPersistanceForJob('IE_otto_hp_singlestep', true)

        //mocking of inner services

        mockMeasuredValueUpdateService()
        mockTimeToCsMappingService()
        mockPageService()
        mockJobService()
        mockMeasuredValueTagService('notTheConcernOfThisTest')


        // Mock Location needed!
        mockLocation(xmlResult.data.location.toString(), undefinedBrowser, server);

        //test execution

        serviceUnderTest.listenToResult(xmlResult, har, server)

        //assertions

        //check for job-runs
        Collection<JobResult> jobRuns = JobResult.list()
        assertEquals("Test persisted JobResults", 1, jobRuns.size())

        //check for steps
        List<MeasuredEvent> steps = MeasuredEvent.list()
        assertEquals("Test expected number of steps", 1, steps.size())

        //check for results
        List<EventResult> allResults = EventResult.list()
        assertEquals("Test expected size of all results", 10, allResults.size())

        assertEquals("Test expected size of all median values", 2, EventResult.findAllByMedianValue(true).size())

        assertEquals("Test expected size of all non median values", 8, EventResult.findAllByMedianValue(false).size())
    }

    @Test
    void testCreatedEventsAfterListeningToSinglestepResultAndPersistOnlyMedian() {
        //create test-specific data
        String nameOfResultXmlFile = 'Result_wptserver2.15_singlestep_5Runs_WithVideo.xml'
        File file = new File("test/resources/WptResultXmls/${nameOfResultXmlFile}")
        GPathResult xmlResult = new XmlSlurper().parse(file)
        String har = new File('test/resources/HARs/singleResult.har').getText()
        setNonMedianPersistanceForJob('IE_otto_hp_singlestep', false)
        deleteResults()

        //mocking of inner services

        mockMeasuredValueUpdateService()
        mockTimeToCsMappingService()
        mockPageService()
        mockJobService()
        mockMeasuredValueTagService('notTheConcernOfThisTest')

        // Mock Location needed!
        mockLocation(xmlResult.data.location.toString(), undefinedBrowser, server);

        //test execution

        serviceUnderTest.listenToResult(xmlResult, har, server)

        //assertions

        //check for job-runs
        Collection<JobResult> jobRuns = JobResult.list()
        assertEquals("Test persisted JobResults", 1, jobRuns.size())

        //check for steps
        List<MeasuredEvent> steps = MeasuredEvent.list()
        assertEquals("Test expected number of steps", 1, steps.size())

        //check for results
        List<EventResult> allResults = EventResult.list()
        assertEquals("Test expected size of all results", 2, allResults.size())

        assertEquals("Test expected size of all median values", 2, EventResult.findAllByMedianValue(true).size())

        assertEquals("Test expected size of all non median values", 0, EventResult.findAllByMedianValue(false).size())
    }

    private void deleteResults(){
        EventResult.list()*.delete(flush: true)
        JobResult.list()*.delete(flush: true)
        MeasuredEvent.list()*.delete(flush: true)
    }

    // mocks ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private void mockLocation(String locationIdentifier, Browser browser, WebPageTestServer server) {
        new Location(
                active: true,
                valid: 1,
                uniqueIdentifierForServer: locationIdentifier, // z.B. Agent1-wptdriver:Chrome
                location: "UNIT_TEST_LOCATION",//z.B. Agent1-wptdriver
                label: "Unit Test Location: Browser?",//z.B. Agent 1: Windows 7 (S008178178)
                browser: browser,//z.B. Firefox
                wptServer: server
        ).save(failOnError: true);
    }
    private void mockTimeToCsMappingService(){
        def timeToCsMappingService = mockFor(TimeToCsMappingService, true)
        timeToCsMappingService.demand.getCustomerSatisfactionInPercent(0..100) { Integer docCompleteTime, Page testedPage ->
            //not the concern of this test
        }
        timeToCsMappingService.demand.validFrustrationsExistFor(0..100) { Page testedPage ->
            //not the concern of this test
        }
        timeToCsMappingService.demand.validMappingsExistFor(0..100) { Page testedPage ->
            //not the concern of this test
        }
        serviceUnderTest.timeToCsMappingService = timeToCsMappingService.createMock()
    }
    private void mockMeasuredValueUpdateService(){
        def measuredValueUpdateServiceMocked = mockFor(MeasuredValueUpdateService, true)
        measuredValueUpdateServiceMocked.demand.createOrUpdateDependentMvs(0..100) { EventResult newResult ->
            //not the concern of this test
            return 34d
        }
        serviceUnderTest.measuredValueUpdateService = measuredValueUpdateServiceMocked.createMock()
    }
    private void mockPageService(){
        def pageServiceMocked = mockFor(PageService, true)
        pageServiceMocked.demand.getPageByStepName(0..1000) { String pageName ->
            def tokenized = pageName.tokenize(PageService.STEPNAME_DELIMITTER)
            return tokenized.size() == 2 ? Page.findByName(tokenized[0]):Page.findByName(Page.UNDEFINED)
        }
        pageServiceMocked.demand.getDefaultStepNameForPage(0..100) { Page page ->
            //not the concern of this test
            return Page.findByName('HP').name + PageService.STEPNAME_DELIMITTER + PageService.STEPNAME_DEFAULT_STEPNUMBER
        }
        pageServiceMocked.demand.excludePagenamePart(0..100) { String stepName ->
            return stepName.contains(PageService.STEPNAME_DELIMITTER)?
                    stepName.substring(stepName.indexOf(PageService.STEPNAME_DELIMITTER)+PageService.STEPNAME_DELIMITTER.length(), stepName.length()):
                    stepName
        }
        serviceUnderTest.pageService = pageServiceMocked.createMock()
    }
    private void mockJobService(){
        def jobServiceMocked = mockFor(JobService, true)
        jobServiceMocked.demand.getCsiJobGroupOf(0..100) { Job job ->
            //not the concern of this test
            return null
        }
        serviceUnderTest.jobService = jobServiceMocked.createMock()
    }
    private void mockMeasuredValueTagService(String tagToReturn){
        def measuredValueTagService = mockFor(MeasuredValueTagService, true)
        measuredValueTagService.demand.createEventResultTag(0..100) {
            JobGroup jobGroup,
            MeasuredEvent measuredEvent,
            Page page,
            Browser browser,
            Location location ->
                return tagToReturn
        }
        measuredValueTagService.demand.findJobGroupOfEventResultTag(0..100) {
            String tag ->
                return undefinedJobGroup
        }
        serviceUnderTest.measuredValueTagService = measuredValueTagService.createMock()
    }
    private void mockMetricReportingService(){
        def metricReportingService = mockFor(MetricReportingService, true)
        metricReportingService.demand.reportEventResultToGraphite(0..100) {
            EventResult result ->
                // not the concern of this test
        }
        serviceUnderTest.metricReportingService = metricReportingService.createMock()
    }
    private void mockHarParserService(){
        def harParserService = mockFor(HarParserService, true)
        harParserService.demand.getWaterfalls(0..100) {
            String har ->
                return [:]	//not the concern of this test
        }
        harParserService.demand.removeWptMonitorSuffixAndPagenamePrefixFromEventnames(0..100) {
            Map<String, WebPerformanceWaterfall> pageidToWaterfallMap ->
                return [:]	//not the concern of this test
        }
        harParserService.demand.createPageIdFrom(0..100) {
            Integer run, String eventName, CachedView cachedView ->
                return 'page_1_eventName_0'
        }
        serviceUnderTest.harParserService = harParserService.createMock()
    }

    // create testdata common to all tests /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private createBrowsers(){
        String browserName=Browser.UNDEFINED
        undefinedBrowser=new Browser(
                name: browserName,
                weight: 0)
                .addToBrowserAliases(alias: Browser.UNDEFINED)
                .save(failOnError: true)

        browserName="IE"
        new Browser(
                name: browserName,
                weight: 45)
                .addToBrowserAliases(alias: "IE")
                .addToBrowserAliases(alias: "IE8")
                .addToBrowserAliases(alias: "Internet Explorer")
                .addToBrowserAliases(alias: "Internet Explorer 8")
                .save(failOnError: true)
        browserName="FF"
        new Browser(
                name: browserName,
                weight: 55)
                .addToBrowserAliases(alias: "FF")
                .addToBrowserAliases(alias: "FF7")
                .addToBrowserAliases(alias: "Firefox")
                .addToBrowserAliases(alias: "Firefox7")
                .save(failOnError: true)

        browserName="Chrome"
        new Browser(
                name: browserName,
                weight: 55)
                .addToBrowserAliases(alias: "Chrome")
                .save(failOnError: true)
    }
    private static createPages(){
        ['HP', 'MES', Page.UNDEFINED].each{pageName ->
            Double weight = 0
            switch(pageName){
                case 'HP' : weight = 6		; break
                case 'MES' : weight = 9		; break
                case 'SE' : weight = 36		; break
                case 'ADS' : weight = 43		; break
                case 'WKBS' : weight = 3		; break
                case 'WK' : weight = 3		; break
            }
            new Page(
                    name: pageName,
                    weight: weight).save(failOnError: true)
        }
    }
}

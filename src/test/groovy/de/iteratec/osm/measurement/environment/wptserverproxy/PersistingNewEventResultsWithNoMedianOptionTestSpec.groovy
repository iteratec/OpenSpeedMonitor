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

import de.iteratec.osm.csi.CsiAggregationUpdateService
import de.iteratec.osm.csi.Page
import de.iteratec.osm.csi.TestDataUtil
import de.iteratec.osm.csi.transformation.TimeToCsMappingService
import de.iteratec.osm.measurement.environment.Browser
import de.iteratec.osm.measurement.environment.BrowserAlias
import de.iteratec.osm.measurement.environment.Location
import de.iteratec.osm.measurement.environment.WebPageTestServer
import de.iteratec.osm.measurement.schedule.Job
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.measurement.script.Script
import de.iteratec.osm.report.external.MetricReportingService
import de.iteratec.osm.result.*
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
@Mock([WebPageTestServer, Browser, Location, Job, JobResult, EventResult, BrowserAlias, Page, MeasuredEvent, JobGroup, Script])
class PersistingNewEventResultsWithNoMedianOptionTestSpec {

    WebPageTestServer server, server2

    JobGroup undefinedJobGroup;

    Browser undefinedBrowser;

    LocationAndResultPersisterService serviceUnderTest

    def doWithSpring = {
        metricReportingService(MetricReportingService)
        performanceLoggingService(PerformanceLoggingService)
        timeToCsMappingService(TimeToCsMappingService)
        pageService(PageService)
        csiAggregationUpdateService(CsiAggregationUpdateService)
        csiAggregationTagService(CsiAggregationTagService)
    }

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
                name: JobGroup.UNDEFINED_CSI
        );
        undefinedJobGroup.save(failOnError: true);

        //creating test-data common to all tests
        createPages()
        createBrowsers()
        createLocationsAndJobs()
        //mocks common for all tests
        mockMetricReportingService()

        serviceUnderTest.performanceLoggingService = grailsApplication.mainContext.getBean('performanceLoggingService')
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

        mockCsiAggregationUpdateService()
        mockTimeToCsMappingService()
        mockPageService()
        mockCsiAggregationTagService('notTheConcernOfThisTest')

        // Mock Location needed!
        mockLocation(xmlResult.data.location.toString(), undefinedBrowser, server);

        //test execution

        serviceUnderTest.listenToResult(xmlResult, server)

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

        mockCsiAggregationUpdateService()
        mockTimeToCsMappingService()
        mockPageService()
        mockCsiAggregationTagService('notTheConcernOfThisTest')

        // Mock Location needed!
        mockLocation(xmlResult.data.location.toString(), undefinedBrowser, server);

        //test execution
        serviceUnderTest.listenToResult(xmlResult, server)

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

        mockCsiAggregationUpdateService()
        mockTimeToCsMappingService()
        mockPageService()
        mockCsiAggregationTagService('notTheConcernOfThisTest')


        // Mock Location needed!
        mockLocation(xmlResult.data.location.toString(), undefinedBrowser, server);

        //test execution

        serviceUnderTest.listenToResult(xmlResult, server)

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

        mockCsiAggregationUpdateService()
        mockTimeToCsMappingService()
        mockPageService()
        mockCsiAggregationTagService('notTheConcernOfThisTest')

        // Mock Location needed!
        mockLocation(xmlResult.data.location.toString(), undefinedBrowser, server);

        //test execution

        serviceUnderTest.listenToResult(xmlResult, server)

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
                wptServer: server,
                dateCreated: new Date(),
                lastUpdated: new Date()
        ).save(failOnError: true);
    }
    private void mockTimeToCsMappingService(){
        def timeToCsMappingService = grailsApplication.mainContext.getBean('timeToCsMappingService')
        timeToCsMappingService.metaClass.getCustomerSatisfactionInPercent = { Integer docCompleteTime, Page testedPage, csiConfiguration ->
            return 1
        }
        timeToCsMappingService.metaClass.validFrustrationsExistFor = { Page testedPage ->
            //not the concern of this test
        }
        timeToCsMappingService.metaClass.validMappingsExistFor = { Page testedPage ->
            //not the concern of this test
        }
        serviceUnderTest.timeToCsMappingService = timeToCsMappingService
    }
    private void mockCsiAggregationUpdateService(){
        def csiAggregationUpdateServiceMocked = grailsApplication.mainContext.getBean('csiAggregationUpdateService')
        csiAggregationUpdateServiceMocked.metaClass.createOrUpdateDependentMvs = { EventResult newResult ->
            //not the concern of this test
            return 34d
        }
        serviceUnderTest.csiAggregationUpdateService = csiAggregationUpdateServiceMocked
    }
    private void mockPageService(){
        def pageServiceMocked = grailsApplication.mainContext.getBean('pageService')
        pageServiceMocked.metaClass.getPageByStepName = { String pageName ->
            def tokenized = pageName.tokenize(PageService.STEPNAME_DELIMITTER)
            return tokenized.size() == 2 ? Page.findByName(tokenized[0]):Page.findByName(Page.UNDEFINED)
        }
        pageServiceMocked.metaClass.getDefaultStepNameForPage = { Page page ->
            //not the concern of this test
            return Page.findByName('HP').name + PageService.STEPNAME_DELIMITTER + PageService.STEPNAME_DEFAULT_STEPNUMBER
        }
        pageServiceMocked.metaClass.excludePagenamePart = { String stepName ->
            return stepName.contains(PageService.STEPNAME_DELIMITTER)?
                    stepName.substring(stepName.indexOf(PageService.STEPNAME_DELIMITTER)+PageService.STEPNAME_DELIMITTER.length(), stepName.length()):
                    stepName
        }
        serviceUnderTest.pageService = pageServiceMocked
    }
    private void mockCsiAggregationTagService(String tagToReturn){
        def csiAggregationTagService = grailsApplication.mainContext.getBean('csiAggregationTagService')
        csiAggregationTagService.metaClass.createEventResultTag = {
            JobGroup jobGroup,
            MeasuredEvent measuredEvent,
            Page page,
            Browser browser,
            Location location ->
                return tagToReturn
        }
        csiAggregationTagService.metaClass.findJobGroupOfEventResultTag = {
            String tag ->
                return undefinedJobGroup
        }
        serviceUnderTest.csiAggregationTagService = csiAggregationTagService
    }
    private void mockMetricReportingService(){
        def metricReportingService = grailsApplication.mainContext.getBean('metricReportingService')
        metricReportingService.metaClass.reportEventResultToGraphite = {
            EventResult result ->
                // not the concern of this test
        }
        serviceUnderTest.metricReportingService = metricReportingService
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

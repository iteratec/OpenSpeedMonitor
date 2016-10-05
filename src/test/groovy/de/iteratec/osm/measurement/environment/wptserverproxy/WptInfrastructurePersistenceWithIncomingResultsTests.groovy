package de.iteratec.osm.measurement.environment.wptserverproxy

import de.iteratec.osm.csi.*
import de.iteratec.osm.measurement.environment.*
import de.iteratec.osm.measurement.schedule.Job
import de.iteratec.osm.measurement.schedule.JobDaoService
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.measurement.script.Script
import de.iteratec.osm.report.external.MetricReportingService
import de.iteratec.osm.result.*
import de.iteratec.osm.util.PerformanceLoggingService
import de.iteratec.osm.util.ServiceMocker
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import org.joda.time.DateTime
import org.junit.Before
import org.junit.Test

import static org.junit.Assert.assertEquals

/**
 * Tests persistence of wpt infrastructure domains like locations, browsers, WebPagetestserver, etc while new EventResults get persisted.
 *
 * TODO: This test is to complicated - make it simpler!
 */
@TestFor(ResultPersisterService)
@Mock([WebPageTestServer, Browser, Location, Job, JobResult, EventResult, BrowserAlias, Page, MeasuredEvent, JobGroup, Script, CsiConfiguration, TimeToCsMapping, CsiDay])
class WptInfrastructurePersistenceWithIncomingResultsTests {
    private static final ServiceMocker SERVICE_MOCKER = ServiceMocker.create()

    WebPageTestServer server1, server2

    JobGroup undefinedJobGroup;

    Browser undefinedBrowser;

    /**
     * Map with expected values for assertions in test {@link #testListenToSuccessfullyMeasuredResults}.
     * 			Structure of the map:<br>
     * 			[name of expected value 1: expectedValue1,<br>
     * 			name of expected value 2: expectedValue2,<br>
     * 			...<br>
     * 			name of expected value n: expectedValueN]
     */
    static final Map expectedAfterResultListening = [
            'BEFORE_MULTISTEP_3Runs.xml'                                         :
                    ['expectedNumberOfLocations'              : 1,
                     'expectedJobLabel'                       : 'ie_step_testjob',
                     'expectedNumberOfJobRuns'                : 1,
                     'expectedNumberOfRuns'                   : 3,
                     'expectedResultExecutionDateTime'        : new DateTime(new Date('Thu, 25 Apr 2013 09:52:21 +0000')),
                     'expectedNumberOfSteps'                  : 1,
                     'expectedNumberOfStepsWithAssociatedPage': 0,
                     'expectedNumberOfCachedViews'            : 2,
                     'expectedWptResultVersion'               : WptXmlResultVersion.BEFORE_MULTISTEP
                    ],
            'MULTISTEP_FORK_ITERATEC_3Runs_6EventNames.xml'                               :
                    ['expectedNumberOfLocations'              : 1,
                     'expectedJobLabel'                       : 'http://www.example.de.de - Multiple steps with event names + dom elements',
                     'expectedNumberOfJobRuns'                : 1,
                     'expectedNumberOfRuns'                   : 3,
                     'expectedResultExecutionDateTime'        : new DateTime(new Date('Thu, 25 Apr 2013 09:52:21 +0000')),
                     'expectedNumberOfSteps'                  : 6,
                     'expectedNumberOfStepsWithAssociatedPage': 0,
                     'expectedNumberOfCachedViews'            : 2,
                     'expectedWptResultVersion'               : WptXmlResultVersion.MULTISTEP_FORK_ITERATEC
                    ],
            'BEFORE_MULTISTEP_1Run_NotCsiRelevantCauseDocTimeTooHighResponse.xml':
                    ['expectedNumberOfLocations'              : 1,
                     'expectedJobLabel'                       : 'FF_BV1_Step01_Homepage - netlab',
                     'expectedNumberOfJobRuns'                : 1,
                     'expectedNumberOfRuns'                   : 1,
                     'expectedResultExecutionDateTime'        : new DateTime(new Date('Wed, 03 Apr 2013 11:46:22 +0000')),
                     'expectedNumberOfSteps'                  : 1,
                     'expectedNumberOfStepsWithAssociatedPage': 0,
                     'expectedNumberOfCachedViews'            : 2,
                     'expectedWptResultVersion'               : WptXmlResultVersion.BEFORE_MULTISTEP
                    ],
            'BEFORE_MULTISTEP_1Run_JustFirstView.xml'                            :
                    ['expectedNumberOfLocations'              : 1,
                     'expectedJobLabel'                       : 'testjob',
                     'expectedNumberOfJobRuns'                : 1,
                     'expectedNumberOfRuns'                   : 1,
                     'expectedResultExecutionDateTime'        : new DateTime(new Date('Sat, 22 Jun 2013 20:33:35 +0000')),
                     'expectedNumberOfSteps'                  : 1,
                     'expectedNumberOfStepsWithAssociatedPage': 0,
                     'expectedNumberOfCachedViews'            : 1,
                     'expectedWptResultVersion'               : WptXmlResultVersion.BEFORE_MULTISTEP
                    ],
            'MULTISTEP_FORK_ITERATEC_1Run_2EventNamesWithPagePrefix_JustFirstView.xml'    :
                    ['expectedNumberOfLocations'              : 1,
                     'expectedJobLabel'                       : 'testjob',
                     'expectedNumberOfJobRuns'                : 1,
                     'expectedNumberOfRuns'                   : 1,
                     'expectedResultExecutionDateTime'        : new DateTime(new Date('Wed, 30 Jan 2013 12:00:48 +0000')),
                     'expectedNumberOfSteps'                  : 2,
                     'expectedNumberOfStepsWithAssociatedPage': 2,
                     'expectedNumberOfCachedViews'            : 1,
                     'expectedWptResultVersion'               : WptXmlResultVersion.MULTISTEP_FORK_ITERATEC
                    ],
            'MULTISTEP_FORK_ITERATEC_1Run_2EventNames_PagePrefix.xml'                     :
                    ['expectedNumberOfLocations'              : 1,
                     'expectedJobLabel'                       : 'FF_BV1_Multistep_2',
                     'expectedNumberOfJobRuns'                : 1,
                     'expectedNumberOfRuns'                   : 1,
                     'expectedResultExecutionDateTime'        : new DateTime(new Date('Wed, 11 Dec 2013 15:42:43 +0000')),
                     'expectedNumberOfSteps'                  : 2,
                     'expectedNumberOfStepsWithAssociatedPage': 2,
                     'expectedNumberOfCachedViews'            : 2,
                     'expectedWptResultVersion'               : WptXmlResultVersion.MULTISTEP_FORK_ITERATEC
                    ],
            'BEFORE_MULTISTEP_1Run_WithoutVideo.xml'                :
                    ['expectedNumberOfLocations'              : 1,
                     'expectedJobLabel'                       : 'IE_otto_hp_singlestep',
                     'expectedNumberOfJobRuns'                : 1,
                     'expectedNumberOfRuns'                   : 1,
                     'expectedResultExecutionDateTime'        : new DateTime(new Date('Wed, 21 Apr 2016 12:06:53 +0000')),
                     'expectedNumberOfSteps'                  : 1,
                     'expectedNumberOfStepsWithAssociatedPage': 0,
                     'expectedNumberOfCachedViews'            : 2,
                     'expectedWptResultVersion'               : WptXmlResultVersion.BEFORE_MULTISTEP
                    ],
            'BEFORE_MULTISTEP_1Run_WithVideo.xml'                   :
                    ['expectedNumberOfLocations'              : 1,
                     'expectedJobLabel'                       : 'IE_otto_hp_singlestep',
                     'expectedNumberOfJobRuns'                : 1,
                     'expectedNumberOfRuns'                   : 1,
                     'expectedResultExecutionDateTime'        : new DateTime(new Date('Wed, 21 Apr 2016 12:03:14 +0000')),
                     'expectedNumberOfSteps'                  : 1,
                     'expectedNumberOfStepsWithAssociatedPage': 0,
                     'expectedNumberOfCachedViews'            : 2,
                     'expectedWptResultVersion'               : WptXmlResultVersion.BEFORE_MULTISTEP
                    ]
    ]

    ResultPersisterService serviceUnderTest

    def doWithSpring = {
        metricReportingService(MetricReportingService)
        performanceLoggingService(PerformanceLoggingService)
        proxyService(ProxyService)
        browserService(BrowserService)
        csiAggregationUpdateService(CsiAggregationUpdateService)
        pageService(PageService)
        csiAggregationTagService(CsiAggregationTagService)
        csiValueService(CsiValueService)
        jobDaoService(JobDaoService)
    }

    @Before
    void setUp() {
        serviceUnderTest = service
        createTestDataCommonForAllTests()

        //mocks common for all tests
        SERVICE_MOCKER.mockTTCsMappingService(serviceUnderTest)
        mockMetricReportingService()
        serviceUnderTest.performanceLoggingService = grailsApplication.mainContext.getBean('performanceLoggingService')
    }

    // tests///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    /**
     * Tests the persisting of various domain-objects while listening to incoming {@link EventResult}s.
     * @see #listenToResult(String)
     * @see #expectedAfterResultListening
     */
    @Test
    void testCreationOfDomainsAfterListenToIncomingResults() {
        mockInnerServices()

        //test execution and assertions
        expectedAfterResultListening.each { k, v ->
            deleteAllRelevantDomains()
            listenToResultAndProofCreatedDomains(k, v)
        }
    }

    /**
     * Tests the persisting of various domain-objects while listening to incoming {@link EventResult}s.
     * @see #listenToResult(String)
     * @see #expectedAfterResultListening
     */
    @Test
    void testCreationOfDomainsAfterListenToIncomingInvalidResults() {
        mockInnerServices()

        //test execution and assertions
        String k = 'BEFORE_MULTISTEP_Error_testCompletedButThereWereNoSuccessfulResults.xml'
        Map v = ['expectedNumberOfLocations'              : 1,
                 'expectedJobLabel'                       : 'vb_agent1_IE8_BV1_Step05_Warenkorbbestaetigung',
                 'expectedNumberOfJobRuns'                : 1,
                 'expectedNumberOfRuns'                   : 0,
                 'expectedResultExecutionDateTime'        : new DateTime(new Date('Wed, 30 Jan 2013 12:00:48 +0000')),
                 'expectedNumberOfSteps'                  : 0,
                 'expectedNumberOfStepsWithAssociatedPage': 0,
                 'expectedNumberOfCachedViews'            : 0,
        ]

        deleteAllRelevantDomains()
        shouldFail() {
            listenToResultAndProofCreatedDomains(k, v)
        }
        assertEquals(0, JobResult.count())
    }

    @Test
    void testFetchLocationIfNoneIsFound() {

        //create test-specific data
        String testNameXML = "BEFORE_MULTISTEP_1Run_JustFirstView.xml";
        File file = new File("test/resources/WptResultXmls/${testNameXML}")
        WptResultXml xmlResult = new WptResultXml (new XmlSlurper().parse(file))

        //mocking of inner services

        mockCsiAggregationUpdateService()
        ServiceMocker.create().mockTTCsMappingService(serviceUnderTest)
        mockPageService()
        mockCsiAggregationTagService('notTheConcernOfThisTest')
        mockProxyService(xmlResult.responseNode.data.location.toString())

        deleteAllRelevantDomains() // No Locations left!

        //test execution
        serviceUnderTest.listenToResult(xmlResult, server1)

        //assertions
        Job job = Job.findByLabel('testjob')
        assertEquals(job.getLocation().getWptServer(), server1);
    }

    @Test
    void testSaveOfWptServerOfJob() {

        //create test-specific data
        String testNameXML = "BEFORE_MULTISTEP_1Run_JustFirstView.xml";
        File file = new File("test/resources/WptResultXmls/${testNameXML}")
        WptResultXml xmlResult = new WptResultXml (new XmlSlurper().parse(file))
        createLocationIfNotExistent(xmlResult.responseNode.data.location.toString(), undefinedBrowser, server1);

        //mocking of inner services

        mockCsiAggregationUpdateService()
        ServiceMocker.create().mockTTCsMappingService(serviceUnderTest)
        mockPageService()
        mockCsiAggregationTagService('notTheConcernOfThisTest')
        deleteAllRelevantDomains()

        //test execution

        serviceUnderTest.listenToResult(xmlResult, server1)

        //assertions
        Job job = Job.findByLabel('testjob')
        assertEquals(job.getLocation().getWptServer(), server1);
    }

    @Test
    void testUpdateOfWptServerOfJob() {

        //create test-specific data
        String testNameXML = "BEFORE_MULTISTEP_1Run_JustFirstView.xml";
        File file = new File("test/resources/WptResultXmls/${testNameXML}")
        WptResultXml xmlResult = new WptResultXml(new XmlSlurper().parse(file))
        createLocationIfNotExistent(xmlResult.responseNode.data.location.toString(), undefinedBrowser, server1);
        createLocationIfNotExistent(xmlResult.responseNode.data.location.toString(), undefinedBrowser, server2);

        //XML-Result (TestID) reset:
        String newTestId = "130622_FA_1AX2"

        //mocking of inner services

        mockCsiAggregationUpdateService()
        ServiceMocker.create().mockTTCsMappingService(serviceUnderTest)
        mockPageService()
        mockCsiAggregationTagService('notTheConcernOfThisTest')
        deleteAllRelevantDomains()

        //test execution

        serviceUnderTest.listenToResult(xmlResult, server1)

        //reset xmlResultID
        xmlResult.responseNode.data.testId = newTestId;
        serviceUnderTest.listenToResult(xmlResult, server2)

        System.out.println(xmlResult.responseNode.data.testId);

        //assertions

        Job job = Job.findByLabel('testjob')
        assertEquals(server2, job.getLocation().getWptServer());

        List<JobResult> jobResults = JobResult.findAll();
        assertEquals(2, jobResults.size());

        jobResults.each { JobResult jr ->
            if (jr.getTestId() == newTestId) {
                assertEquals(server2.getBaseUrl(), jr.getWptServerBaseurl());
                assertEquals(server2.getLabel(), jr.getWptServerLabel());
            } else {
                assertEquals(server1.getBaseUrl(), jr.getWptServerBaseurl());
                assertEquals(server1.getLabel(), jr.getWptServerLabel());

            }

        }
    }

    /**
     * Executes test for given webpagetest-result-xml-file (nameOfResultXmlFile) and proofs existing domain-objects afterwards:<br>
     * Proofs for {@link Location}:
     * <ul>
     * <li>Number of created objects</li>
     * </ul>
     * Proofs for {@link Job}:
     * <ul>
     * <li>Number of created objects</li>
     * <li>Whether {@link Job#lastRun} is expectedResultExecutionDateTime</li>
     * </ul>
     * Proofs for {@link JobResult}:
     * <ul>
     * <li>Number of created objects</li>
     * <li>Whether {@link JobResult#date} is expectedResultExecutionDateTime</li>
     * </ul>
     * Proofs for {@link MeasuredEvent}:
     * <ul>
     * <li>Number of created objects</li>
     * </ul>
     * Proofs for {@link EventResult}:
     * <ul>
     * <li>various tests</li>
     * </ul>
     * @param nameOfResultXmlFile
     * 			Name of the result-xml-file from webpagetest (testdata from test/resources/).
     * @param expectedValues
     * 			Map with expected values for assertions.
     * 			Structure of the map:<br>
     * 			[name of expected value 1: expectedValue1,<br>
     * 			name of expected value 2: expectedValue2,<br>
     * 			...<br>
     * 			name of expected value n: expectedValueN]
     */
    private void listenToResultAndProofCreatedDomains(String nameOfResultXmlFile, Map expectedValues) {
        //test specific data
        File file = new File("test/resources/WptResultXmls/${nameOfResultXmlFile}")
        WptResultXml xmlResult = new WptResultXml (new XmlSlurper().parse(file))
        createLocationIfNotExistent(xmlResult.responseNode.data.location.toString(), undefinedBrowser, server1);

        //test execution
        serviceUnderTest.listenToResult(xmlResult, server1)

        //check for job-runs
        Collection<JobResult> jobRuns = JobResult.list()
        assertEquals("xml-result '${nameOfResultXmlFile}' expectedNumberOfJobRuns - ", expectedValues['expectedNumberOfJobRuns'], jobRuns.size())

        //check dates of job and job_results
        Job job = Job.findByLabel(expectedValues['expectedJobLabel'])
        assertEquals("xml-result '${nameOfResultXmlFile}' expectedResultExecutionDateTime in job.lastRun - ",
                expectedValues['expectedResultExecutionDateTime'], new DateTime(job.lastRun))
        assertEquals("xml-result '${nameOfResultXmlFile}' expectedResultExecutionDateTime in jobResult.date - ",
                expectedValues['expectedResultExecutionDateTime'], new DateTime(jobRuns[0].date))
        //TODO 2013-10-24: proof all CSI-relevant attributes of job_results

        //check for steps
        List<MeasuredEvent> steps = MeasuredEvent.list()
        assertEquals("xml-result '${nameOfResultXmlFile}' expectedNumberOfSteps - ", expectedValues['expectedNumberOfSteps'], steps.size())
        assertEquals("xml-result '${nameOfResultXmlFile}' expectedNumberOfStepsWithAssociatedPage - ", expectedValues['expectedNumberOfStepsWithAssociatedPage'],
                steps.findAll { !it.testedPage.isUndefinedPage() }.size())

        //check for results
        List<EventResult> allResults = EventResult.list()
        int expectedSizeOfAllResults = expectedValues['expectedNumberOfRuns'] * expectedValues['expectedNumberOfCachedViews'] * expectedValues['expectedNumberOfSteps']
        assertEquals("xml-result '${nameOfResultXmlFile}' expectedSizeOfAllResults - ", expectedSizeOfAllResults, allResults.size())

        int expectedSizeOfAllMedianValues = expectedValues['expectedNumberOfCachedViews'] * expectedValues['expectedNumberOfSteps']
        assertEquals("xml-result '${nameOfResultXmlFile}' expectedSizeOfAllMedianValues - ", expectedSizeOfAllMedianValues, EventResult.findAllByMedianValue(true).size())

        int expectedSizeOfAllNonMedianValues = (expectedValues['expectedNumberOfRuns'] - 1) * expectedValues['expectedNumberOfCachedViews'] * expectedValues['expectedNumberOfSteps']
        assertEquals("xml-result '${nameOfResultXmlFile}' expectedSizeOfAllNonMedianValues - ", expectedSizeOfAllNonMedianValues, EventResult.findAllByMedianValue(false).size())

        expectedValues['expectedNumberOfRuns'].times {
            int expectedSizeOfResults = expectedValues['expectedNumberOfCachedViews'] * expectedValues['expectedNumberOfSteps']
            assertEquals("xml-result '${nameOfResultXmlFile}' expectedSizeOfResults - ", expectedSizeOfResults, EventResult.findAllByNumberOfWptRun(it + 1).size())
        }

        int expectedSizeOfUnCachedViewResults = expectedValues['expectedNumberOfRuns'] * expectedValues['expectedNumberOfSteps']
        assertEquals("xml-result '${nameOfResultXmlFile}' expectedSizeOfUnCachedViewResults - ", expectedSizeOfUnCachedViewResults, EventResult.findAllByCachedView(CachedView.UNCACHED).size())

        // TODO Do not use branches in verification part
        if (expectedValues['expectedNumberOfCachedViews'] == 2) {
            int expectedSizeOfCachedResultsForTwoViews = expectedValues['expectedNumberOfRuns'] * expectedValues['expectedNumberOfSteps']
            assertEquals("xml-result '${nameOfResultXmlFile}' expectedSizeOfCachedResultsForTwoViews - ", expectedSizeOfCachedResultsForTwoViews, EventResult.findAllByCachedView(CachedView.CACHED).size())
        }
    }

    private void deleteAllRelevantDomains() {
//		Location.findAll().each {it.delete(flush: true)}
//		Job.list()*.delete(flush: true)
        JobResult.list()*.delete(flush: true)
        MeasuredEvent.list()*.delete(flush: true)
        EventResult.list()*.delete(flush: true)
    }

    // mocks ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    private void mockInnerServices() {
        //mocking of inner services

        mockCsiAggregationUpdateService()
        ServiceMocker.create().mockTTCsMappingService(serviceUnderTest)
        mockPageService()
        mockCsiAggregationTagService('notTheConcernOfThisTest')
    }

    private void createLocationIfNotExistent(String locationIdentifier, Browser browser, WebPageTestServer server) {
        Location alreadyExistent = Location.findByWptServerAndUniqueIdentifierForServer(server, locationIdentifier)
        if (!alreadyExistent) {
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
    }

    private void mockProxyService(String locationIdentifier) {
        def proxyService = grailsApplication.mainContext.getBean('proxyService')
        proxyService.metaClass.fetchLocations = { WebPageTestServer server ->
            createLocationIfNotExistent(locationIdentifier, undefinedBrowser, server);
        }
        serviceUnderTest.proxyService = proxyService
    }

    private void mockCsiAggregationUpdateService() {
        def csiAggregationUpdateServiceMocked = grailsApplication.mainContext.getBean('csiAggregationUpdateService')
        csiAggregationUpdateServiceMocked.metaClass.createOrUpdateDependentMvs = { EventResult newResult ->
            //not the concern of this test
            return 34d
        }
        serviceUnderTest.csiAggregationUpdateService = csiAggregationUpdateServiceMocked
    }

    private void mockPageService() {
        def pageServiceMocked = grailsApplication.mainContext.getBean('pageService')
        pageServiceMocked.metaClass.getPageByStepName = { String pageName ->
            def tokenized = pageName.tokenize(PageService.STEPNAME_DELIMITTER)
            return tokenized.size() == 2 ? Page.findByName(tokenized[0]) : Page.findByName(Page.UNDEFINED)
        }
        pageServiceMocked.metaClass.getDefaultStepNameForPage = { Page page ->
            //not the concern of this test
            return Page.findByName('HP').name + PageService.STEPNAME_DELIMITTER + PageService.STEPNAME_DEFAULT_STEPNUMBER
        }
        pageServiceMocked.metaClass.excludePagenamePart = { String stepName ->
            return stepName.contains(PageService.STEPNAME_DELIMITTER) ?
                    stepName.substring(stepName.indexOf(PageService.STEPNAME_DELIMITTER) + PageService.STEPNAME_DELIMITTER.length(), stepName.length()) :
                    stepName
        }
        serviceUnderTest.pageService = pageServiceMocked
    }

    private void mockCsiAggregationTagService(String tagToReturn) {
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

    private void mockMetricReportingService() {
        def metricReportingService = grailsApplication.mainContext.getBean('metricReportingService')
        metricReportingService.metaClass.reportEventResultToGraphite = {
            EventResult result ->
                // not the concern of this test
        }
        serviceUnderTest.metricReportingService = metricReportingService
    }

    // create testdata common to all tests /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private createBrowsers() {
        String browserName = Browser.UNDEFINED
        undefinedBrowser = new Browser(
                name: browserName,
                weight: 0)
                .addToBrowserAliases(alias: Browser.UNDEFINED)
                .save(failOnError: true)

        browserName = "IE"
        new Browser(
                name: browserName,
                weight: 45)
                .addToBrowserAliases(alias: "IE")
                .addToBrowserAliases(alias: "IE8")
                .addToBrowserAliases(alias: "Internet Explorer")
                .addToBrowserAliases(alias: "Internet Explorer 8")
                .save(failOnError: true)
        browserName = "FF"
        new Browser(
                name: browserName,
                weight: 55)
                .addToBrowserAliases(alias: "FF")
                .addToBrowserAliases(alias: "FF7")
                .addToBrowserAliases(alias: "Firefox")
                .addToBrowserAliases(alias: "Firefox7")
                .save(failOnError: true)

        browserName = "Chrome"
        new Browser(
                name: browserName,
                weight: 55)
                .addToBrowserAliases(alias: "Chrome")
                .save(failOnError: true)
    }

    private void createPages() {
        ['HP', 'MES', Page.UNDEFINED].each { pageName ->
            Double weight = 0
            switch (pageName) {
                case 'HP': weight = 6; break
                case 'MES': weight = 9; break
                case 'SE': weight = 36; break
                case 'ADS': weight = 43; break
                case 'WKBS': weight = 3; break
                case 'WK': weight = 3; break
            }
            new Page(
                    name: pageName,
                    weight: weight).save(failOnError: true)
        }
    }

    void createTestDataCommonForAllTests() {
        server1 = new WebPageTestServer(
                label: "TestServer 1",
                proxyIdentifier: "TestServer1",
                baseUrl: "http://wptUnitTest.dev.hh.iteratec.local",
                active: true,
                dateCreated: new Date(),
                lastUpdated: new Date()
        ).save(failOnError: true, validate: false)

        server2 = new WebPageTestServer(
                label: "TestServer 2",
                proxyIdentifier: "TestServer2",
                baseUrl: "http://wptUnitTest2.dev.hh.iteratec.local",
                active: 1,
                dateCreated: new Date(),
                lastUpdated: new Date()
        ).save(failOnError: true, validate: false)

        undefinedJobGroup = new JobGroup(
                name: JobGroup.UNDEFINED_CSI
        );
        undefinedJobGroup.save(failOnError: true);

        //creating test-data common to all tests
        createPages()
        createBrowsers()
        Location testLocation = TestDataUtil.createLocation(server1, 'test-location', Browser.findByName('IE'), true)
        Script testScript = TestDataUtil.createScript('test-script', 'description', 'navigate   http://my-url.de')
        TestDataUtil.createJob('ie_step_testjob', testScript, testLocation, undefinedJobGroup, '', 1, false, 60)
        TestDataUtil.createJob('http://www.example.de.de - Multiple steps with event names + dom elements', testScript, testLocation, undefinedJobGroup, '', 1, false, 60)
        TestDataUtil.createJob('FF_BV1_Step01_Homepage - netlab', testScript, testLocation, undefinedJobGroup, '', 1, false, 60)
        TestDataUtil.createJob('testjob', testScript, testLocation, undefinedJobGroup, '', 1, false, 60)
        TestDataUtil.createJob('FF_BV1_Multistep_2', testScript, testLocation, undefinedJobGroup, '', 1, false, 60)
        TestDataUtil.createJob('IE_otto_hp_singlestep', testScript, testLocation, undefinedJobGroup, '', 1, false, 60)
        TestDataUtil.createJob('HP:::FF_BV1_Step01_Homepage - netlab', testScript, testLocation, undefinedJobGroup, '', 1, false, 60)
        TestDataUtil.createJob('example.de - Multiple steps with event names + dom elements', testScript, testLocation, undefinedJobGroup, '', 1, false, 60)
        TestDataUtil.createJob('FF_Otto_multistep', testScript, testLocation, undefinedJobGroup, '', 1, false, 60)

        CsiConfiguration csiConfiguration = TestDataUtil.createCsiConfiguration()
        csiConfiguration.timeToCsMappings = TestDataUtil.createTimeToCsMappingForAllPages(Page.list())
        undefinedJobGroup.csiConfiguration = csiConfiguration
        ServiceMocker mockGenerator = ServiceMocker.create()
        serviceUnderTest.csiValueService = grailsApplication.mainContext.getBean('csiValueService')
        mockGenerator.mockOsmConfigCacheService(serviceUnderTest.csiValueService)
    }
}
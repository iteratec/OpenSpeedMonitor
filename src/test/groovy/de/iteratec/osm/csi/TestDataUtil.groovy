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

import de.iteratec.osm.OsmConfiguration
import de.iteratec.osm.batch.Activity
import de.iteratec.osm.batch.BatchActivity
import de.iteratec.osm.batch.Status
import de.iteratec.osm.measurement.environment.Browser
import de.iteratec.osm.measurement.environment.Location
import de.iteratec.osm.measurement.environment.WebPageTestServer
import de.iteratec.osm.measurement.environment.wptserverproxy.Protocol
import de.iteratec.osm.measurement.schedule.ConnectivityProfile
import de.iteratec.osm.measurement.schedule.ConnectivityProfileService
import de.iteratec.osm.measurement.schedule.Job
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.measurement.script.Script
import de.iteratec.osm.report.chart.*
import de.iteratec.osm.report.external.GraphitePath
import de.iteratec.osm.report.external.GraphiteServer
import de.iteratec.osm.result.CachedView
import de.iteratec.osm.result.EventResult
import de.iteratec.osm.result.JobResult
import de.iteratec.osm.result.MeasuredEvent
import de.iteratec.osm.security.Role
import de.iteratec.osm.security.User
import de.iteratec.osm.security.UserRole
import de.iteratec.osm.util.OsmTestLogin
import grails.buildtestdata.mixin.Build
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin

import java.util.regex.Pattern

import static org.junit.Assert.assertNotNull
/**
 * <p>
 * A test-utility to load data from customers CSV-files
 * in "pre CSI-Dashboard times".
 * </p>
 *
 * <p>
 * <em>DEV-Note:</em>
 * Because this utility is used in both, unit and integration tests, it must
 * be placed in the productive source folder.
 * </p>
 *
 * @author mze
 * @since IT-8
 */
@TestMixin(GrailsUnitTestMixin)
@Build([CsiSystem, JobGroup, CsiConfiguration])
class TestDataUtil implements OsmTestLogin {

    static CsiSystem buildCsiSystem() {
        return CsiSystem.buildWithoutSave()
                .addToJobGroupWeights(jobGroup: JobGroup.build(csiConfiguration: CsiConfiguration.build()), weight: 0.5)
                .addToJobGroupWeights(jobGroup: JobGroup.build(csiConfiguration: CsiConfiguration.build()), weight: 0.5)
                .save(flush: true, failOnError: true)
    }

    static ConnectivityProfile createConnectivityProfile(String profileName) {
        ConnectivityProfile existingWithName = ConnectivityProfile.findByName(profileName)
        if (existingWithName) {
            return existingWithName
        }
        ConnectivityProfile result = new ConnectivityProfile(
                name: profileName,
                bandwidthDown: 6000,
                bandwidthUp: 512,
                latency: 40,
                packetLoss: 0,
                active: true
        ).save(failOnError: true)
        result.connectivityProfileService = new ConnectivityProfileService()
        return result
    }

    static createTemplate() {
        new GraphitePath(
        ).save(failOnError: true)
    }

    static createCsTargetGraph(CsTargetValue pointOne, CsTargetValue pointTwo) {
        new CsTargetGraph(
                label: 'TestCsTargetGraph',
                pointOne: pointOne,
                pointTwo: pointTwo,
                defaultVisibility: true
        ).save(failOnError: true)
    }

    static CsTargetValue createCsTargetValue() {
        return new CsTargetValue(
                date: new Date(),
                csInPercent: 42
        ).save(failOnError: true)
    }

    static createCustomerFrustration(Page page) {
        new CustomerFrustration(
                page: page,
                loadTimeInMilliSecs: 1000,
                investigationVersion: 1
        ).save(failOnError: true)
    }

    static createTimeToCsMapping(Page page) {
        new TimeToCsMapping(
                page: page,
                loadTimeInMilliSecs: 1,
                customerSatisfaction: 0.9,
                mappingVersion: 1
        ).save(failOnError: true)
    }

    static CsiConfiguration createCsiConfiguration(
            String label = 'testCsiConfiguration',
            String description = 'CsiConfiguration for tests',
            CsiDay csiDay = createCsiDay(),
            List<BrowserConnectivityWeight> browserConnectivityWeights = new ArrayList<BrowserConnectivityWeight>(),
            List<PageWeight> pageWeights = new ArrayList<PageWeight>(),
            List<TimeToCsMapping> timeToCsMappings = new ArrayList<TimeToCsMapping>()
    ) {
        return new CsiConfiguration(
                label: label,
                description: description,
                csiDay: csiDay,
                browserConnectivityWeights: browserConnectivityWeights,
                pageWeights: pageWeights,
                timeToCsMappings: timeToCsMappings
        ).save(failOnError: true)
    }

    static BrowserConnectivityWeight createBrowserConnectivityWeight(
            Browser browser,
            ConnectivityProfile connectivityProfile,
            double weight
    ) {
        return new BrowserConnectivityWeight(
                browser: browser,
                connectivity: connectivityProfile,
                weight: weight
        ).save(failOnError: true)
    }

    static PageWeight createPageWeight(
            Page page,
            double weight
    ) {
        return new PageWeight(
                page: page,
                weight: weight
        ).save(failOnError: true)
    }

    static CsiSystem createCsiSystem(
            String label = "testCsiSystem",
            List<JobGroupWeight> jobGroupWeights
    ) {
        return new CsiSystem(
                label: label,
                jobGroupWeights: jobGroupWeights
        )
    }

    static JobGroupWeight createJobGroupWeight(
            CsiSystem csiSystem,
            JobGroup jobGroup,
            double weight
    ) {
        return new JobGroupWeight(
                csiSystem: csiSystem,
                jobGroup: jobGroup,
                weight: weight
        )
    }

    /*
     * @param hourWeights
     *          set your specific weights for any hour. if no weight is given for an hour, '1.0' will be used.
     */

    static CsiDay createCsiDay(Map<Integer, Double> hourWeights = new HashMap<>()) {
        CsiDay csiDay = new CsiDay()
        (0..23).each { hour ->
            if (hourWeights.containsKey(hour)) {
                csiDay.setHourWeight(hour, hourWeights.get(hour))
            } else {
                csiDay.setHourWeight(hour, 1.0)
            }
        }
        return csiDay.save(failOnError: true)
    }

    static Collection<TimeToCsMapping> createTimeToCsMappingForAllPages(List<Page> allPages) {
        List<TimeToCsMapping> timeToCsMappingList = new ArrayList<>()
        allPages.each { page ->
            (0..10000).each { loadTime ->
                if (loadTime % 20 == 0) {
                    timeToCsMappingList.add(
                            new TimeToCsMapping(
                                    page: page,
                                    loadTimeInMilliSecs: loadTime,
                                    customerSatisfaction: 0.5,
                                    mappingVersion: 1
                            )
                    )
                }
            }
        }

        return timeToCsMappingList
    }

    static OsmConfiguration createOsmConfiguration(int detailDataStorageTimeInWeeks) {
        return new OsmConfiguration(
                detailDataStorageTimeInWeeks: detailDataStorageTimeInWeeks
        ).save(failOnError: true)
    }

    static Job createJob(String label, Script script, Location location, JobGroup group) {
        return createJob(label, script, location, group, "Test Job", 1, true, 60, null);
    }

    static Job createJob(String label, Script script, Location location, JobGroup group, String description, int runs, boolean active, Integer maxDownloadTimeInMinutes, ConnectivityProfile profile = null) {
        return createJobWithoutSaving(label, script, location, group, description, runs, active, maxDownloadTimeInMinutes, profile).save(failOnError: true)
    }

    static Job createJobWithoutSaving(String label, Script script, Location location, JobGroup group, String description, int runs, boolean active, Integer maxDownloadTimeInMinutes, ConnectivityProfile profile = null) {
        return new Job(
                label: label,
                script: script,
                location: location,
                jobGroup: group,
                description: description,
                runs: runs,
                active: active,
                maxDownloadTimeInMinutes: maxDownloadTimeInMinutes,
                customConnectivityProfile: true,
                customConnectivityName: 'Custom (6.000/512 Kbps, 50ms)',
                bandwidthDown: 6000,
                bandwidthUp: 512,
                latency: 50,
                packetLoss: 0,
                connectivityProfile: profile,
                executionSchedule: '0 0 */2 * * ? *'
        )
    }

    /**
     * Creates script with default values for label, description, navigationScript and provideAuthenticateInformation.
     * @return Default script.
     */
    static Script createScript() {
        return createScript(
                'script label',
                'script description',
                'navigate http://www.osm.org'
        )
    }

    static Script createScript(String label, String description, String navigationScript) {
        return new Script(
                label: label,
                description: description,
                navigationScript: navigationScript,
        ).save(failOnError: true)
    }

    static WebPageTestServer createWebPageTestServer(String label, String proxyIdentifier, boolean active, String baseUrl) {
        return new WebPageTestServer(
                label: label,
                proxyIdentifier: proxyIdentifier,
                dateCreated: new Date(),
                lastUpdated: new Date(),
                active: active,
                baseUrl: baseUrl
        ).save(failOnError: true)
    }

    static MeasuredEvent createMeasuredEvent(String eventName, Page page) {
        return new MeasuredEvent(
                name: eventName,
                testedPage: page
        ).save(failOnError: true)
    }

    static createCsiAggregationUpdateEvent(Date dateOfUpdate, CsiAggregationUpdateEvent.UpdateCause cause, String mvId) {
        new CsiAggregationUpdateEvent(
                dateOfUpdate: dateOfUpdate,
                updateCause: cause,
                csiAggregationId: mvId
        ).save(failOnError: true)
    }

    static GraphitePath createGraphitePath(String prefix, AggregatorType aggregator) {
        new GraphitePath(
                prefix: prefix,
                measurand: aggregator
        ).save(failOnError: true)
    }

    static GraphiteServer createGraphiteServer(String serverAdress, int port, List paths, Boolean reportHealthMetrics, String healthMetricsReportPrefix) {
        new GraphiteServer(
                serverAdress: serverAdress,
                port: port,
                graphitePaths: paths,
                reportHealthMetrics: reportHealthMetrics,
                healthMetricsReportPrefix: healthMetricsReportPrefix,
                webappUrl: "https://my-graphite.webapp.com",
                webappProtocol: Protocol.HTTPS,
                webappPathToRenderingEngine: "/render"
        ).save(failOnError: true)
    }

    /**
     * <p>
     * Loads test-data from customers CSV file and creates missing elements.
     * </p>
     *
     * @param csvFile
     *         The CSV file with customer data,
     *         not <code>null</code>.
     * @param pagesToGenerateDataFor
     *         The names of the pages to process (see {@link Page}),
     *         not <code>null</code>,
     *         not {@linkplain Collection#isEmpty() empty}.
     */
    public
    static void loadTestDataFromCustomerCSV(File csvFile, List<String> pagesToGenerateDataFor, List<String> allPages) {
        createJobGroups()
        createServer()
        createPages(allPages)
        createBrowsersAndAliases()
        createLocations()
        decodeCSVTestDataLine(csvFile.readLines(), pagesToGenerateDataFor)
    }

    static List<Location> createLocations() {
        WebPageTestServer server1 = WebPageTestServer.findByLabel('server 1 - wpt example')
        Browser browserFF = Browser.findByName("FF")
        Browser browserIE = Browser.findByName("IE")
        Location ffAgent1, ieAgent1
        ffAgent1 = new Location(
                active: true,
                location: 'ffLocationLocation',
                label: 'ffLocationLabel',
                browser: browserFF,
                wptServer: server1,
                dateCreated: new Date(),
                lastUpdated: new Date()
        ).save(failOnError: true)
        ieAgent1 = new Location(
                active: true,
                location: 'ieLocationLocation',
                label: 'ieLocationLabel',
                browser: browserIE,
                wptServer: server1,
                dateCreated: new Date(),
                lastUpdated: new Date()
        ).save(failOnError: true)
        return [ffAgent1, ieAgent1]
    }

    static Location createLocation(WebPageTestServer server, String uniqueIdentifierForServer, Browser browser, Boolean active) {
        return new Location(
                active: active,
                uniqueIdentifierForServer: uniqueIdentifierForServer,
                location: uniqueIdentifierForServer,
                label: uniqueIdentifierForServer,
                browser: browser,
                wptServer: server,
                dateCreated: new Date(),
                lastUpdated: new Date()
        ).save(failOnError: true)
    }

    static Location createLocation() {
        return createLocation(
                createWebPageTestServer("For Sample Location", "proxyId", true, "http://testwpt.org/"),
                "sampleLocationWPT", createBrowser("TestLocationBrowser"), true
        );
    }

    static List<Browser> createBrowsersAndAliases() {
        List<Browser> browsers = []
        String browserName = "undefined"
        browsers.add(new Browser(name: browserName)
                        .addToBrowserAliases(alias: "undefined"
                ).save(failOnError: true)
        )
        browserName = "IE"
        browsers.add(new Browser(name: browserName)
                        .addToBrowserAliases(alias: "IE")
                        .addToBrowserAliases(alias: "IE8")
                        .addToBrowserAliases(alias: "Internet Explorer")
                        .addToBrowserAliases(alias: "Internet Explorer 8"
                ).save(failOnError: true)
        )
        browserName = "FF"
        browsers.add(new Browser(name: browserName)
                        .addToBrowserAliases(alias: "FF")
                        .addToBrowserAliases(alias: "FF7")
                        .addToBrowserAliases(alias: "Firefox")
                        .addToBrowserAliases(alias: "Firefox7"
                ).save(failOnError: true)
        )
        return browsers
    }

    static Browser createBrowser(String name) {
        Browser browser = Browser.findByName(name)
        browser = browser == null ?
                new Browser(name: name).save(failOnError: true) : browser
        return browser
    }

    static WebPageTestServer createServer() {
        WebPageTestServer server1
        server1 = new WebPageTestServer(
                baseUrl: 'http://wpt.server.de',
                active: true,
                label: 'server 1 - wpt example',
                proxyIdentifier: 'server 1 - wpt example',
                dateCreated: new Date(),
                lastUpdated: new Date()

        ).save(failOnError: true)
        return server1
    }

    static Page createUndefinedPage() {
        return new Page(name: Page.UNDEFINED).save(failOnError: true)
    }

    static List<Page> createPages(List<String> allPageNames) {
        return allPageNames.collect { pageName ->
            new Page(name: pageName).save(failOnError: true)
        }
    }

    static Page createPage(String name) {
        return new Page(name: name).save(failOnError: true)
    }

    static List<JobGroup> createJobGroups() {
        JobGroup group1 = new JobGroup(
                name: "CSI", csiConfiguration: createCsiConfiguration("CSI_TEST_LABEL")).save(failOnError: true)
        JobGroup group2 = new JobGroup(
                name: 'csiGroup1', csiConfiguration: createCsiConfiguration("csiGroup1_TEST_LABEL")).save(failOnError: true)
        JobGroup group3 = new JobGroup(
                name: 'csiGroup2', csiConfiguration: createCsiConfiguration("csiGroup2_TEST_LABEL")).save(failOnError: true)
        return [group1, group2, group3]
    }

    static JobGroup createJobGroup(String groupName) {
        return new JobGroup(
                name: groupName
        ).save(failOnError: true)
    }

    /**
     * <p>
     * Checks weather the current CSV-line should be treated as header-line.
     * </p>
     *
     * @param csvLine The CSV-line, not <code>null</code>.
     * @return <code>true</code> if the line should be treated as header line,
     *         <code>false</code> else.
     */
    private static boolean isHeaderLine(String csvLine) {
        return csvLine.startsWith('job;') || csvLine.startsWith(';')
    }

    /**
     * <p>
     * Checks weather the current CSV-line should be treated as an empty-line
     * which mostly means the line is to be ignored.
     * </p>
     *
     * @param csvLine The CSV-line, not <code>null</code>.
     * @return <code>true</code> if the line should be treated as empty line,
     *         <code>false</code> else.
     */
    private static boolean isEmptyLine(String csvLine) {
        return csvLine.split(';')[0].isEmpty()
    }

    /**
     * <p>
     * Checks if data for the pageAggregator is relevant for the test.
     * </p>
     *
     * @param page
     *         The pageAggregator that relevance is to check, not <code>null</code>.
     * @param pagesToGenerateDataFor
     *         The relevant pages by name, not <code>null</code>; an empty
     *         collection would cause nothing to be relevant.
     * @return <code>true</code> if pageAggregator is relevant, <code>false</code> else.
     */
    private static boolean isResultForPageRequired(Page page, List<String> pagesToGenerateDataFor) {
        return pagesToGenerateDataFor.contains(page.name)
    }

    /**
     * <p>
     * Interprets the job name in CSV as a Location. This is a manually mapping
     * cause in real-world-scenario the jobs are assigned to locations by the
     * agents job queue which would be known on receiving a fresh result.
     * </p>
     *
     * @param csvJobColumn CSV Job-column contents.
     * @return An assigned location, null if not interpretable.
     */
    private static Location getLocationCSVJobName(String csvJobColumn) {
        if (Pattern.matches('.*FF_.*', csvJobColumn)) {
            return Location.findByLabel('ffLocationLabel');
        }

        return Location.findByLabel('ieLocationLabel');
    }

    /**
     * <p>
     * Gets or create an {@link MeasuredEvent} for the specified pageAggregator and job combination.
     * </p>
     *
     * @param job The job an event is to get for, not <code>null</code>.
     * @param page The pageAggregator an event is to get for, not <code>null</code>.
     *
     * @return not <code>null</code>.
     */
    private static MeasuredEvent getMeasuredEvent(JobGroup job, Page page) {
        String eventName = 'TestEvent-' + page + "-" + job.name;

        return MeasuredEvent.findByName(eventName) ?: new MeasuredEvent(name: eventName, testedPage: page).save(failOnError: true)
    }

    /**
     * <p>
     * Gets the Job for the specified job-name. If an aproximate job does not
     * exists now, it will be created.
     * </p>
     *
     * @param csvJobColumn CSV Job-column contents.
     * @return The Job for the CSV-column-value, not null.
     */
    private static Job getJobOfCSVJobName(String csvJobCoulumn, JobGroup jobGroup, Location location) {
        Job result = Job.findByLabel(csvJobCoulumn);

        AggregatorType page = AggregatorType.findByName(AggregatorType.PAGE) ?: new AggregatorType(name: AggregatorType.PAGE).save(failOnError: true)

        if (!result) {
            result = new Job(
                    label: csvJobCoulumn,
                    location: location,
                    connectivityProfile: createConnectivityProfile("conn"),
                    page: page,
                    active: false,
                    description: '',
                    runs: 1,
                    jobGroup: jobGroup,
                    script: Script.createDefaultScript(csvJobCoulumn).save(failOnError: true),
                    maxDownloadTimeInMinutes: 60,
                    noTrafficShapingAtAll: true
            ).save(failOnError: true);
        }

        return result;
    }

    /**
     * <p>
     * Creates a job result for the specified data.
     * </p>
     *
     * <p>
     * None of the arguments may be <code>null</code>.
     * </p>
     *
     * @param testId The ID of the test-result.
     * @param dateOfJobRun The date of the test run.
     * @param parentJob The job the result belongs to.
     * @param agentLocation The location where the agent is working.
     * @param httpStatusCode Optional httpStatusCode, 200 by default
     *
     * @return A newly created result, not <code>null</code>.
     */
    static JobResult createJobResult(String testId, Date dateOfJobRun, Job parentJob, Location agentLocation) {
        return createJobResult(testId, dateOfJobRun, parentJob, agentLocation, 200)
    }

    static JobResult createJobResult(String testId, Date dateOfJobRun, Job parentJob, Location agentLocation, Integer httpStatusCode) {
        return new JobResult(
                date: dateOfJobRun,
                testId: testId,
                httpStatusCode: httpStatusCode,
                jobConfigLabel: parentJob.label,
                jobConfigRuns: 1,
                description: '',
                locationBrowser: agentLocation.browser.name,
                locationLocation: agentLocation.location,
                jobGroupName: parentJob.jobGroup.name,
                job: parentJob
        ).save(failOnError: true)
    }

    /**
     * <p>
     * Creates an event result and assigns it to the specified
     * {@link JobResult}.
     * </p>
     *
     * <p>
     * None of the arguments may be <code>null</code>.
     * </p>
     *
     * @param job
     *         The parent job.
     * @param jobResult
     *         The job result the event result should be assigned to.
     * @param docCompleteTimeInMillisecs
     *         The doc-complete-time in milliseconds.
     * @param customerSatisfactionInPercent
     *         The customer-satisfaction-index in percent.
     */
    static EventResult createEventResult(
            Job job,
            JobResult jobResult,
            int docCompleteTimeInMillisecs,
            double customerSatisfactionInPercent,
            MeasuredEvent event,
            Browser browser,
            ConnectivityProfile connectivityProfile = null) {
        EventResult eventResult = new EventResult(
                numberOfWptRun: 1,
                cachedView: CachedView.UNCACHED,
                medianValue: true,
                wptStatus: 200,
                docCompleteTimeInMillisecs: docCompleteTimeInMillisecs,
                csByWptDocCompleteInPercent: customerSatisfactionInPercent,
                jobResult: jobResult,
                jobResultDate: jobResult.date,
                jobResultJobConfigId: jobResult.job.ident(),
                measuredEventAggr: event,
                speedIndex: EventResult.SPEED_INDEX_DEFAULT_VALUE,
                connectivityProfile: connectivityProfile,
                customConnectivityName: null,
                noTrafficShapingAtAll: (connectivityProfile ? false : true),
                jobGroup: job.jobGroup,
                measuredEvent: event,
                page: event.testedPage,
                browser: browser,
                location: job.location
        ).save(failOnError: true)

        return eventResult
    }

    /**
     * <p>
     * Creates an event result and assigns it to the specified
     * {@link JobResult}.
     * </p>
     *
     * <p>
     * None of the arguments may be <code>null</code>.
     * </p>
     *
     * @param job
     *         The parent job.
     * @param jobResult
     *         The job result the event result should be assigned to.
     * @param docCompleteTimeInMillisecs
     *         The doc-complete-time in milliseconds.
     * @param customerSatisfactionInPercent
     *         The customer-satisfaction-index in percent.
     */
    static EventResult createEventResult(
            JobGroup jobGroup, MeasuredEvent measuredEvent, Page page, Browser browser, Location location,
            ConnectivityProfile connectivityProfile = null) {
        Job job = createJob("jobLabel", createScript(), location, jobGroup, "description", 1, false, 200)
        JobResult jobResult = createJobResult("123", new Date(), job, location)
        EventResult eventResult = new EventResult(
                numberOfWptRun: 1,
                cachedView: CachedView.UNCACHED,
                medianValue: true,
                wptStatus: 200,
                docCompleteTimeInMillisecs: null,
                csByWptDocCompleteInPercent: null,
                jobResult: jobResult,
                jobResultDate: jobResult.date,
                jobResultJobConfigId: jobResult.job.ident(),
                speedIndex: EventResult.SPEED_INDEX_DEFAULT_VALUE,
                connectivityProfile: connectivityProfile,
                customConnectivityName: null,
                noTrafficShapingAtAll: true,
                jobGroup: jobGroup,
                measuredEvent: measuredEvent,
                page: page,
                browser: browser,
                location: location
        ).save(failOnError: true)

        return eventResult
    }

    /**
     * Decodes all lines of test data CSV.
     *
     * @param listOfLines
     */
    private static void decodeCSVTestDataLine(List listOfLines, List<String> pagesToGenerateDataFor) {
        JobGroup defaultJobGroup = JobGroup.findByName('CSI');
        JobGroup jobGroup1 = JobGroup.findByName('csiGroup1');
        JobGroup jobGroup2 = JobGroup.findByName('csiGroup2');
        ConnectivityProfile connectivityProfile = createConnectivityProfile('the profile')

        //There are only a few amount of different jobs, pages and locations within the csv.
        //So we put them in a map, to reduce overhead within the transaction
        Map<String, Job> jobMap = [:]
        Map<String, Page> pageMap = [:]
        Map<String, Location> locationMap = [:]

        assertNotNull(defaultJobGroup)
        assertNotNull(jobGroup1)
        assertNotNull(jobGroup2)

        int i = listOfLines.size()
        int j = 0
        listOfLines.each { String csvLine ->
            j += 1
            if (!isHeaderLine(csvLine) && !isEmptyLine(csvLine)) {
                System.out.println(j + ' von ' + i);

                String[] columns = csvLine.split(';');
                String jobName = columns[0]

                // Create the job:
                JobGroup groupOfJob
                switch (jobName.split('_')[0]) {
                    case 'csiGroup1':
                        groupOfJob = jobGroup1
                        break;
                    case 'csiGroup2':
                        groupOfJob = jobGroup2
                        break;
                    default:
                        groupOfJob = defaultJobGroup
                }

                Page page = pageMap[jobName]
                if (!page) {
                    page = getPageFromCSVJobName(jobName)
                    pageMap[jobName] = page
                }
                assertNotNull('Page for job-name ' + jobName + ' may not be null.', page)

                if (!isResultForPageRequired(page, pagesToGenerateDataFor)) {
                    // If the result is not needed for this test, just return and
                    // do nothing more...
                    return;
                }
                Location location = locationMap[jobName]
                if (!location) {
                    location = getLocationCSVJobName(jobName)
                    locationMap[jobName] = location
                }
                assertNotNull(location)

                Job job = jobMap[jobName]
                if (!job) {
                    job = getJobOfCSVJobName(jobName, groupOfJob, location)
                    jobMap[jobName] = job
                }
                assertNotNull(job)

                Date dateOfJobRun = new Date(columns[2] + " " + columns[3]);
                assertNotNull(dateOfJobRun)

                MeasuredEvent eventOfPage = getMeasuredEvent(groupOfJob, page);
                assertNotNull(eventOfPage)

                JobResult jobResult = createJobResult(columns[6], dateOfJobRun, job, location)
                assertNotNull(jobResult)

                if (columns.length > 8 && !columns[8].isEmpty()) {
                    Browser dummyBrowser = createBrowser("bro")
                    createEventResult(job, jobResult, Integer.valueOf(columns[7]), Double.valueOf(columns[8]), eventOfPage, dummyBrowser, connectivityProfile);
                }
            }
        }

    }

    /**
     * <p>
     * Interprets the job name in CSV as a Page. This is a manually mapping
     * cause in real-world-scenario the jobs are assigned to pages during
     * CSI-configuration.
     * </p>
     *
     * @param csvJobColumn CSV Job-column contents.
     * @return An assigned pageAggregator, null if not interpretable.
     */
    private static Page getPageFromCSVJobName(String csvJobColumn) {
        switch (csvJobColumn.toLowerCase()) {
            case { Pattern.matches('.*step01.*', it) }:
                return Page.findByName('HP');
            case { Pattern.matches('.*step02.*', it) }:
                return Page.findByName('MES');
            case { Pattern.matches('.*step03.*', it) }:
                return Page.findByName('SE');
            case { Pattern.matches('.*step04.*', it) }:
                return Page.findByName('ADS');
            case { Pattern.matches('.*step05.*', it) }:
                return Page.findByName('WKBS');
            case { Pattern.matches('.*step06.*', it) }:
                return Page.findByName('WK');
            default:
                return null;
        }
    }

    /**
     * <p>
     * Creates an OsmConfiguration and persists it.
     * This method uses default values for minDocCompleteTimeInMillisecs (250), maxDocCompleteTimeInMillisecs (180000).
     * </p>
     */
    public static void createOsmConfig() {
        if (OsmConfiguration.count == 0) {
            new OsmConfiguration(
                    detailDataStorageTimeInWeeks: 2,
                    defaultMaxDownloadTimeInMinutes: 60,
                    minDocCompleteTimeInMillisecs: 250,
                    maxDocCompleteTimeInMillisecs: 180000,
                    initialChartHeightInPixels: 400,
                    maxDataStorageTimeInMonths: 12,
                    csiTransformation: CsiTransformation.BY_MAPPING,
                    internalMonitoringStorageTimeInDays: OsmConfiguration.INTERNAL_MONITORING_STORAGETIME_IN_DAYS
            ).save(failOnError: true)
        }
    }

    /**
     * <p>
     * Creates the default AggregatorTypes (PAGE; MEASURED_STEP; PAGE_AND_BROWSER; SHOP).
     * </p>
     */
    public static List<AggregatorType> createAggregatorTypes() {
        AggregatorType eventAggregator = new AggregatorType(name: AggregatorType.MEASURED_EVENT, measurandGroup: MeasurandGroup.NO_MEASURAND).save(failOnError: true)
        AggregatorType pageAggregator = new AggregatorType(name: AggregatorType.PAGE, measurandGroup: MeasurandGroup.NO_MEASURAND).save(failOnError: true)
        AggregatorType shopAggregator = new AggregatorType(name: AggregatorType.SHOP, measurandGroup: MeasurandGroup.NO_MEASURAND).save(failOnError: true)
        AggregatorType csiSystemAggregator = new AggregatorType(name: AggregatorType.CSI_SYSTEM, measurandGroup: MeasurandGroup.NO_MEASURAND).save(failOnError: true)
        return [eventAggregator, pageAggregator, shopAggregator, csiSystemAggregator]
    }

    /**
     * <p>
     * Creates an AggregatorType with given name and group.
     * </p>
     */
    public static AggregatorType createAggregatorType(String name, MeasurandGroup group) {
        return new AggregatorType(name: name, measurandGroup: group).save(failOnError: true)
    }

    /**
     * <p>
     * Creates the default CsiAggregationIntervals (hourly; daily; weekly).
     * </p>
     */
    public static List<CsiAggregationInterval> createCsiAggregationIntervals() {
        CsiAggregationInterval hourly = new CsiAggregationInterval(
                name: "hourly",
                intervalInMinutes: CsiAggregationInterval.HOURLY
        ).save(failOnError: true)
        CsiAggregationInterval daily = new CsiAggregationInterval(
                name: "daily",
                intervalInMinutes: CsiAggregationInterval.DAILY
        ).save(failOnError: true)
        CsiAggregationInterval weekly = new CsiAggregationInterval(
                name: "weekly",
                intervalInMinutes: CsiAggregationInterval.WEEKLY
        ).save(failOnError: true)
        return [hourly, daily, weekly]
    }

    /**
     * <p>
     * Gets the previously created {@link MeasuredEvent}s for the specified pageAggregator and jobgroup.
     * </p>
     *
     * @param jobgroup The jobgroup an event is to get for, not <code>null</code>.
     * @param page The pageAggregator an event is to get for, not <code>null</code>.
     *
     * @return not <code>null</code>.
     */
    public static MeasuredEvent findMeasuredEvent(JobGroup jobgroup, Page page) {
//		List<MeasuredEvent> event = MeasuredEvent.findAllByTestedPage(pageAggregator)
        String eventName = 'TestEvent-' + page + "-" + jobgroup.name;

        MeasuredEvent event = MeasuredEvent.findByName(eventName);

        assertNotNull("The event should be created by test-data-load.", event)

        return event;
    }

    /**
     * Writes new {@link CsiAggregation} to db without validating it.
     * @param date
     * @param csiAggregationInterval
     * @param aggregator
     * @param tag
     * @param value
     * @param resultIdsAsString
     * @param closed
     */
    public
    static CsiAggregation createSimpleCsiAggregation(Date date, CsiAggregationInterval csiAggregationInterval, AggregatorType aggregator, boolean closed) {
        JobGroup group = JobGroup.list()[0]
        Page page
        if (aggregator.name == AggregatorType.PAGE) {
            page = Page.list()[0]
        }
        return new CsiAggregation(
                started: date,
                interval: csiAggregationInterval,
                underlyingEventResultsByWptDocComplete: "",
                underlyingEventResultsByVisuallyComplete: "",
                aggregator: aggregator,
                csByWptDocCompleteInPercent: 0.0d,
                closedAndCalculated: closed,
                jobGroup: group,
                page: page
        ).save(validate: false)
    }

    /**
     * * Writes new {@link CsiAggregation} to db.
     * @param started
     * @param csiAggregationInterval
     * @param aggregator
     * @param jobGroup
     * @param page
     * @param csByWptDocCompleteInPercent
     * @param resultIds
     * @param closed
     * @return
     */
    public
    static CsiAggregation createCsiAggregation(Date started, CsiAggregationInterval csiAggregationInterval, AggregatorType aggregator, JobGroup jobGroup, Page page, Double csByWptDocCompleteInPercent, String resultIds, boolean closed) {
        return new CsiAggregation(
                started: started,
                interval: csiAggregationInterval,
                aggregator: aggregator,
                jobGroup: jobGroup,
                page: page,
                underlyingEventResultsByWptDocComplete: resultIds,
                csByWptDocCompleteInPercent: csByWptDocCompleteInPercent,
                closedAndCalculated: closed,
        ).save(failOnError: true)
    }

    /**
     * Writes a new {@link CsiAggregationUpdateEvent} with dateOfUpdate = NOW.
     * @param csiAggregationId
     * @param cause
     */
    public
    static CsiAggregationUpdateEvent createUpdateEvent(Long csiAggregationId, CsiAggregationUpdateEvent.UpdateCause cause) {
        return new CsiAggregationUpdateEvent(
                dateOfUpdate: new Date(),
                csiAggregationId: csiAggregationId,
                updateCause: cause
        ).save(failOnError: true)
    }

    public static setPredefinedConnectivityForJob(ConnectivityProfile profile, Job job) {
        job.connectivityProfile = profile
        job.noTrafficShapingAtAll = false
        job.customConnectivityProfile = false
        job.customConnectivityName = null
        job.bandwidthDown = null
        job.bandwidthUp = null
        job.latency = null
        job.packetLoss = null
        job.save()
    }

    public static setNativeConnectivityForJob(Job job) {
        job.connectivityProfile = null
        job.noTrafficShapingAtAll = true
        job.customConnectivityProfile = false
        job.customConnectivityName = null
        job.bandwidthDown = null
        job.bandwidthUp = null
        job.latency = null
        job.packetLoss = null
        job.save()
    }

    public static setCustomConnectivityForJob(Job job) {
        job.connectivityProfile = null
        job.noTrafficShapingAtAll = false
        job.customConnectivityProfile = true
        job.customConnectivityName = 'Custom (60.000/512 Kbps, 40ms, 0% PLR)'
        job.bandwidthDown = 60000
        job.bandwidthUp = 512
        job.latency = 40
        job.packetLoss = 0
        job.save()
    }

    public static WebPageTestServer createUnusedWptServer() {
        return createWebPageTestServer("webPageTestServer", "proxyId", true, "http://internet.de/")
    }

    public static Job createSimpleJob() {
        JobGroup group = createJobGroup("group")
        Location location = createLocation(createWebPageTestServer("label2", "proxyId", true, "http://server1.iteratec.de/"), "veryUnique",
                createBrowser("FF"), true)
        return createJob("label", createScript("label1", "description", "navi",),
                location, group, "description", 1, false, 10)
    }

    public static User createAdminUser() {
        String adminUserName = getConfiguredUsername()
        User user = User.findByUsername(adminUserName)
        if (!user){
            user = User.build(
                    username: adminUserName,
                    password: getConfiguredPassword(),
                    enabled: true,
                    accountExpired: false,
                    accountLocked: false,
                    passwordExpired: false
            )
            Role adminRole = Role.build(authority: 'ROLE_ADMIN')
            // UserRole doesn't work with build-test-data plugin :(
            new UserRole(user: user, role: adminRole).save(failOnError: true)
        }
        return user
    }

    static BatchActivity createBatchActivity(String name, Status status) {
        new BatchActivity(name: name, domain: "irrelevant domain", activity: Activity.UPDATE,
                status: status, maximumStages: 1, startDate: new Date(),
                lastUpdate: new Date()).save(flush: true, failOnError: true)
    }

    public static void setInfrastructureSetupStatus(OsmConfiguration.InfrastructureSetupStatus status){
        OsmConfiguration configuration = OsmConfiguration.list()[0]
        if (!configuration){
            configuration = OsmConfiguration.build()
        }
        configuration.infrastructureSetupRan = status
        configuration.save(failOnError: true)
    }
}

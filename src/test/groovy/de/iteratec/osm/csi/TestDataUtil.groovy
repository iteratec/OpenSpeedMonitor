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
import de.iteratec.osm.report.external.GraphiteServer
import de.iteratec.osm.result.CachedView
import de.iteratec.osm.result.EventResult
import de.iteratec.osm.result.JobResult
import de.iteratec.osm.result.MeasurandGroup
import de.iteratec.osm.result.MeasuredEvent
import de.iteratec.osm.security.Role
import de.iteratec.osm.security.User
import de.iteratec.osm.security.UserRole
import de.iteratec.osm.util.OsmTestLogin
import grails.buildtestdata.mixin.Build
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin

import java.util.regex.Pattern

import static de.iteratec.osm.OsmConfiguration.DEFAULT_MIN_VALID_LOADTIME
import static de.iteratec.osm.OsmConfiguration.DEFAULT_MAX_VALID_LOADTIME
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

    static Browser createBrowser(String name) {
        Browser browser = Browser.findByName(name)
        browser = browser == null ?
                new Browser(name: name).save(failOnError: true) : browser
        return browser
    }

    static Page createPage(String name) {
        return new Page(name: name).save(failOnError: true)
    }

    static JobGroup createJobGroup(String groupName) {
        return new JobGroup(
                name: groupName
        ).save(failOnError: true)
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
     * Creates an OsmConfiguration and persists it.
     * This method uses default values for minValidLoadtime and maxValidLoadtime.
     * </p>
     */
    public static void createOsmConfig() {
        if (OsmConfiguration.count == 0) {
            new OsmConfiguration(
                    detailDataStorageTimeInWeeks: 2,
                    defaultMaxDownloadTimeInMinutes: 60,
                    minValidLoadtime: DEFAULT_MIN_VALID_LOADTIME,
                    maxValidLoadtime: DEFAULT_MAX_VALID_LOADTIME,
                    initialChartHeightInPixels: 400,
                    maxDataStorageTimeInMonths: 12,
                    csiTransformation: CsiTransformation.BY_MAPPING,
                    internalMonitoringStorageTimeInDays: OsmConfiguration.INTERNAL_MONITORING_STORAGETIME_IN_DAYS
            ).save(failOnError: true)
        }
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

    public static User createAdminUser() {
        String adminUserName = getConfiguredUsername()
        User user = User.findByUsername(adminUserName)
        if (!user) {
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
}

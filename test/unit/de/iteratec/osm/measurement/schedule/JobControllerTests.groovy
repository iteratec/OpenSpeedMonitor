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

package de.iteratec.osm.measurement.schedule

import de.iteratec.osm.csi.Page
import de.iteratec.osm.csi.TestDataUtil
import de.iteratec.osm.measurement.environment.Browser
import de.iteratec.osm.measurement.environment.Location
import de.iteratec.osm.measurement.environment.WebPageTestServer
import de.iteratec.osm.measurement.script.Script
import de.iteratec.osm.result.*
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import org.joda.time.DateTime

import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.*

/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
@TestFor(JobController)
@Mock([Job, JobResult, EventResult, MeasuredEvent, HttpArchive, WebPageTestServer, JobGroup, Browser, Location, Page, Script])
class JobControllerTests {
    private int jobIdCount = 0
    private int serverIdCount = 0
    private int jobResultCount = 0

    JobController controllerUnderTest
    DateTime executionDateBeforeCleanUpDate = new DateTime()
    Job deleteJob


    void setUp() {
        controllerUnderTest = controller
        createData()
    }

    void testDelete() {
        controller.params.id = deleteJob.id.toString()
        JobResult deleteResult = deleteJob.results.iterator().next()
        HttpArchive deleteArchive = deleteResult.httpArchives.iterator().next()
        EventResult deleteEventResult = deleteResult.eventResults.iterator().next()
        assertThat(deleteResult, (notNullValue()))
        assertThat(deleteArchive, (notNullValue()))
        assertThat(deleteEventResult, (notNullValue()))

        int oldJobCount = Job.count()

        assert JobResult.count() == 2
        assert EventResult.count() == 2
        assert HttpArchive.count() == 2

        assertThat(Job.list(), hasItem(deleteJob))
        assertThat(Job.list(), hasItem(deleteJob))
        assertThat(EventResult.list(), hasItem(deleteEventResult))
        assertThat(HttpArchive.list(), hasItem(deleteArchive))

        controllerUnderTest.delete()

        List<Job> allJobs = Job.list()
        List<JobResult> allJobResults = JobResult.list()
        List<EventResult> allEventResults = EventResult.list()
        List<HttpArchive> allHttpArchives = HttpArchive.list()

        assertThat(allJobs, not(hasItem(deleteJob)))
        assertThat(allHttpArchives, not(hasItem(deleteArchive)))
        assertThat(allJobResults, not(hasItem(deleteResult)))
        assertThat(allEventResults, not(hasItem(deleteEventResult)))

        assert allJobs.size() == oldJobCount - 1
//        assert allJobResults.size() == 1
//        assert allEventResults.size() == 1
//        assert allHttpArchives.size() == 1
    }

    private void createData() {
        WebPageTestServer server = createServer()
        JobGroup group = createJobGroup("TestGroup")
        Browser browser = createBrowser("FF")
        Location ffAgent1 = createLocation(browser, server)

        Page homepage = createPage("homepage")

        Script script = Script.createDefaultScript('Unnamed').save(failOnError: true)

        deleteJob = createJob(ffAgent1, script, group)
        for (i in 1..9) {
            createJob(ffAgent1, script, group)
        }

        JobResult jobResultWithBeforeCleanupDate = createJobResult(deleteJob)

        JobResult jobResultWithAfterCleanupDate = createJobResult(createJob(ffAgent1, script, group))

        MeasuredEvent measuredEvent = new MeasuredEvent()
        measuredEvent.setName('Test event')
        measuredEvent.setTestedPage(homepage)
        measuredEvent.save(failOnError: true)

        String eventResultTag = "$group.id;$measuredEvent.id;$homepage.id;$browser.id;$ffAgent1.id";

        EventResult eventResult1 = createEventResult(jobResultWithBeforeCleanupDate, eventResultTag)
        jobResultWithBeforeCleanupDate.eventResults.add(eventResult1)
        jobResultWithBeforeCleanupDate.save(failOnError: true)

        new HttpArchive(jobResult: jobResultWithBeforeCleanupDate).save(failOnError: true)

        EventResult eventResult2 = createEventResult(jobResultWithAfterCleanupDate, eventResultTag)
        jobResultWithAfterCleanupDate.eventResults.add(eventResult2)
        jobResultWithAfterCleanupDate.save(failOnError: true)

        new HttpArchive(jobResult: jobResultWithAfterCleanupDate).save(failOnError: false)
    }

    private WebPageTestServer createServer() {
        new WebPageTestServer(
                baseUrl: "http://server${serverIdCount}.wpt.server.de",
                active: true,
                label: "server ${serverIdCount++} - wpt server",
                proxyIdentifier: "server ${serverIdCount++} - wpt server"
        ).save(failOnError: true);
    }

    private static JobGroup createJobGroup(String groupName) {
        new JobGroup(
                name: groupName,
                groupType: JobGroupType.CSI_AGGREGATION
        ).save(failOnError: true)
    }

    private static Browser createBrowser(String browserName) {
        new Browser(
                name: browserName,
                weight: 0.55).save(failOnError: true)

    }

    private static Location createLocation(Browser browser, WebPageTestServer server) {
        new Location(
                active: true,
                valid: 1,
                location: 'physNetLabAgent01-FF',
                label: 'physNetLabAgent01 - FF up to date',
                browser: browser,
                wptServer: server
        ).save(failOnError: true)
    }

    private static Page createPage(String pageName) {
        new Page(
                name: pageName,
                weight: 0.5
        ).save(failOnError: true)
    }

    private Job createJob(Location agent, Script script, JobGroup group) {
        new Job(
                id: jobIdCount++,
                active: false,
                label: "BV${jobIdCount} - Step 01",
                description: "This is job ${jobIdCount++}...",
                location: agent,
                frequencyInMin: 5,
                runs: 1,
                jobGroup: group,
                script: script,
                maxDownloadTimeInMinutes: 60
        ).save(failOnError: true)
    }

    private JobResult createJobResult(Job job) {
        new JobResult(
                job: job,
                date: executionDateBeforeCleanUpDate.toDate(),
                testId: "${jobResultCount++}",
                description: 'greatDescription',
                jobConfigLabel: job.label,
                jobConfigRuns: job.runs,
                jobGroupName: job.jobGroup.name,
                frequencyInMin: 5,
                locationLocation: job.location.location,
                locationBrowser: job.location.browser.name,
                httpStatusCode: 200,
        ).save(failOnError: true)
    }

    private static EventResult createEventResult(JobResult jobResult, eventResultTag) {
        new EventResult(
                numberOfWptRun: 1,
                cachedView: CachedView.UNCACHED,
                medianValue: true,
                docCompleteIncomingBytes: 1,
                docCompleteRequests: 2,
                docCompleteTimeInMillisecs: 3,
                domTimeInMillisecs: 4,
                firstByteInMillisecs: 5,
                fullyLoadedIncomingBytes: 6,
                fullyLoadedRequestCount: 7,
                fullyLoadedTimeInMillisecs: 8,
                loadTimeInMillisecs: 9,
                startRenderInMillisecs: 10,
                downloadAttempts: 1,
                firstStatusUpdate: jobResult.date,
                lastStatusUpdate: jobResult.date,
                wptStatus: 0,
                validationState: 'validationState',
                customerSatisfactionInPercent: 1,
                jobResultDate: jobResult.date,
                jobResultJobConfigId: jobResult.job.ident(),
                measuredEvent: null,
                speedIndex: EventResult.SPEED_INDEX_DEFAULT_VALUE,
                tag: eventResultTag
        ).save(failOnError: true)
    }
}

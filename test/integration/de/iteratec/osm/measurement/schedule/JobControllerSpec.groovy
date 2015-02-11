package de.iteratec.osm.measurement.schedule

import de.iteratec.osm.csi.IntTestWithDBCleanup
import de.iteratec.osm.csi.Page
import de.iteratec.osm.csi.TestDataUtil
import de.iteratec.osm.measurement.environment.Browser
import de.iteratec.osm.measurement.environment.Location
import de.iteratec.osm.measurement.environment.WebPageTestServer
import de.iteratec.osm.result.CachedView
import de.iteratec.osm.result.EventResult
import de.iteratec.osm.result.HttpArchive
import de.iteratec.osm.result.JobResult
import de.iteratec.osm.result.MeasuredEvent
import grails.test.mixin.TestMixin
import grails.test.mixin.integration.IntegrationTestMixin
import org.joda.time.DateTime
import org.junit.Before
import org.junit.Test
import de.iteratec.osm.measurement.script.Script

@TestMixin(IntegrationTestMixin)
class JobControllerSpec extends IntTestWithDBCleanup {

    JobController controllerUnderTest = new JobController()
    private int jobIdCount = 0
    private int serverIdCount = 0

    DateTime executionDateBeforeCleanUpDate = new DateTime()
    Job deleteJob


    @Before
    void setup() {
        TestDataUtil.createOsmConfig()
        createData()
    }

    @Test
    void delete() {
        controllerUnderTest.params.id = deleteJob.id.toString()
        int oldJobCount = Job.count()
        assert JobResult.count() == 2
        assert EventResult.count() == 2
        assert MeasuredEvent.count() == 1
        assert HttpArchive.count() == 2
        controllerUnderTest.delete()
        assert Job.count() == oldJobCount-1
        assert !Job.list().contains(deleteJob)
        assert JobResult.count() == 1
        assert EventResult.count() == 1
        assert MeasuredEvent.count() == 1
        assert HttpArchive.count() == 1
        deleteJob.results.each {
            assert !JobResult.list().contains(it)
            it.eventResults.each {event ->
                assert !EventResult.list().contains(event)
            }
            it.httpArchives.each {archive->
                assert !HttpArchive.list().contains(archive)
            }
        }
    }

    private void createData() {
        WebPageTestServer server = createServer()
        JobGroup group = createJobGroup("TestGroup")
        Browser browser = createBrowser("FF")
        Location ffAgent1 = createLocation(browser, server)

        Page homepage = createPage("homepage")

        Script script = Script.createDefaultScript('Unnamed').save(failOnError: true)

        deleteJob = createJob(ffAgent1, script, group)
        for(i in 1..9){
            createJob(ffAgent1, script, group)
        }

        JobResult jobResultWithBeforeCleanupDate =createJobResult(deleteJob)

        JobResult jobResultWithAfterCleanupDate = createJobResult(createJob(ffAgent1,script,group))

        MeasuredEvent measuredEvent = new MeasuredEvent()
        measuredEvent.setName('Test event')
        measuredEvent.setTestedPage(homepage)
        measuredEvent.save(failOnError: true)

        String eventResultTag = "$group.id;$measuredEvent.id;$homepage.id;$browser.id;$ffAgent1.id";

        EventResult eventResult1 = createEventResult(jobResultWithBeforeCleanupDate,eventResultTag)
        jobResultWithBeforeCleanupDate.eventResults.add(eventResult1)
        jobResultWithBeforeCleanupDate.save(failOnError: true)

        new HttpArchive(jobResult: jobResultWithBeforeCleanupDate).save(failOnError: true)

        EventResult eventResult2 =createEventResult(jobResultWithAfterCleanupDate,eventResultTag)
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

    private JobResult createJobResult(Job job){
        new JobResult(
                job: job,
                date: executionDateBeforeCleanUpDate.toDate(),
                testId: '1',
                description: 'jobResultWithBeforeCleanupDate',
                jobConfigLabel: job.label,
                jobConfigRuns: job.runs,
                jobGroupName: job.jobGroup.name,
                frequencyInMin: 5,
                locationLocation: job.location.location,
                locationBrowser: job.location.browser.name,
                httpStatusCode: 200,
        ).save(failOnError: true)
    }

    private static EventResult createEventResult(JobResult jobResult, eventResultTag){
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

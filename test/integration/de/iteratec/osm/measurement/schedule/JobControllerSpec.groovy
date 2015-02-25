package de.iteratec.osm.measurement.schedule

import de.iteratec.osm.csi.IntTestWithDBCleanup
import de.iteratec.osm.csi.Page
import de.iteratec.osm.measurement.environment.Browser
import de.iteratec.osm.measurement.environment.Location
import de.iteratec.osm.measurement.environment.WebPageTestServer
import de.iteratec.osm.measurement.script.Script
import de.iteratec.osm.result.*
import grails.test.mixin.TestMixin
import grails.test.mixin.integration.IntegrationTestMixin
import org.joda.time.DateTime
import org.junit.Before
import org.junit.Test

import static de.iteratec.osm.csi.TestDataUtil.*
import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.*

/**
 * We need an integration test because there is no support for unit test with cascading in Hibernate 3.X
 *
 */
@TestMixin(IntegrationTestMixin)
class JobControllerSpec extends IntTestWithDBCleanup {

    JobController controllerUnderTest
    private int jobIdCount = 0

    DateTime executionDateBeforeCleanUpDate = new DateTime()
    Job deleteJob


    @Before
    void setup() {
        controllerUnderTest = new JobController()
        createOsmConfig()
        createData()
    }

    @Test
    void delete() {
        controllerUnderTest.params.id = deleteJob.id.toString()
        List<JobResult> jobResults = JobResult.findAllByJob(deleteJob)
        List<HttpArchive> httpArchives =  []
        List<EventResult> eventResults = []
        jobResults.each {
            httpArchives.addAll(HttpArchive.findAllByJobResult(it))
            eventResults.addAll(EventResult.findAllByJobResult(it))
        }
        int oldJobCount = Job.count()
        //We have to assert that no MeasuredEvent will be deleted
        int oldMeasuredEventCount = MeasuredEvent.count()
        assert jobResults.size() == 1
        assert eventResults.size() == 1
        assert httpArchives.size() == 1

        controllerUnderTest.delete()

        List<Job> allJobs = Job.list()
        List<JobResult> allJobResults = JobResult.list()
        List<EventResult> allEventResults = EventResult.list()
        List<HttpArchive> allHttpArchives = HttpArchive.list()

        assertThat(allJobs, not(hasItem(deleteJob)))
        assertThat(allHttpArchives as Iterable<ArrayList<HttpArchive>>, not(hasItems(httpArchives)))
        assertThat(allJobResults as Iterable<List<JobResult>>, not(hasItems(jobResults)))
        assertThat(allEventResults as Iterable<ArrayList<EventResult>>, not(hasItems(eventResults)))

        assert allJobs.size() == oldJobCount - 1
        assert allJobResults.size() == 1
        assert allEventResults.size() == 1
        assert allHttpArchives.size() == 1
        assert MeasuredEvent.count() == oldMeasuredEventCount

    }

    private void createData() {
        WebPageTestServer server = createWebPageTestServer("server - wpt server", "server - wpt server", true, "http://iteratec.de")
        JobGroup group = createJobGroup("Testgroup", JobGroupType.CSI_AGGREGATION)
        Browser browser = createBrowser("FF", 0.55)
        Location ffAgent1 = createLocation(server, "1", browser, true)
        Page homepage = createPages(["homepage"]).get(0)

        Script script = Script.createDefaultScript('Unnamed').save(failOnError: true)

        deleteJob = createJob("BV${jobIdCount} - Step 01", script, ffAgent1, group, "This is job ${jobIdCount++}...", 5, false, 10)
        for (i in 1..9) {
            createJob("BV${jobIdCount} - Step 01", script, ffAgent1, group, "This is job ${jobIdCount++}...", 5, false, 10)
        }

        JobResult jobResultWithBeforeCleanupDate = createJobResult(deleteJob)

        JobResult jobResultWithAfterCleanupDate = createJobResult(createJob("BV${jobIdCount} - Step 01", script, ffAgent1, group, "This is job ${jobIdCount++}...", 5, false, 10))

        MeasuredEvent measuredEvent = createMeasuredEvent('Test event', homepage).save(failOnError: true)

        String eventResultTag = "$group.id;$measuredEvent.id;$homepage.id;$browser.id;$ffAgent1.id";

        EventResult eventResult1 = createEventResult(jobResultWithBeforeCleanupDate, eventResultTag)
        jobResultWithBeforeCleanupDate.addToEventResults(eventResult1)
        jobResultWithBeforeCleanupDate.save(failOnError: true)

        createHttpArchive(jobResultWithBeforeCleanupDate)
        EventResult eventResult2 = createEventResult(jobResultWithAfterCleanupDate, eventResultTag)
        jobResultWithAfterCleanupDate.addToEventResults(eventResult2)
        jobResultWithAfterCleanupDate.save(failOnError: true)

        createHttpArchive(jobResultWithAfterCleanupDate)
    }

       private JobResult createJobResult(Job job) {
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

    private static EventResult createEventResult(JobResult jobResult, eventResultTag) {
        new EventResult(
                jobResult: jobResult,
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
                jobResultJobConfigId: 1,
                measuredEvent: null,
                speedIndex: EventResult.SPEED_INDEX_DEFAULT_VALUE,
                tag: eventResultTag
        ).save(failOnError: true)
    }

}

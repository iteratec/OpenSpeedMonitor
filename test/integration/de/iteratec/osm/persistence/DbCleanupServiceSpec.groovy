package de.iteratec.osm.persistence

import de.iteratec.osm.csi.Page
import de.iteratec.osm.measurement.environment.Browser
import de.iteratec.osm.measurement.environment.Location
import de.iteratec.osm.measurement.environment.WebPageTestServer
import de.iteratec.osm.measurement.schedule.Job
import de.iteratec.osm.measurement.script.Script
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.measurement.schedule.JobGroupType
import de.iteratec.osm.report.chart.AggregatorType
import de.iteratec.osm.report.chart.MeasuredValue
import de.iteratec.osm.report.chart.MeasuredValueInterval
import de.iteratec.osm.report.chart.MeasuredValueUpdateEvent
import de.iteratec.osm.result.CachedView
import de.iteratec.osm.result.EventResult
import de.iteratec.osm.result.HttpArchive
import de.iteratec.osm.result.JobResult
import de.iteratec.osm.result.MeasuredEvent
import grails.test.mixin.TestMixin
import grails.test.mixin.integration.IntegrationTestMixin
import org.joda.time.DateTime
import org.junit.Before

import static org.hamcrest.Matchers.*
import static org.junit.Assert.assertThat

import de.iteratec.osm.csi.IntTestWithDBCleanup
import de.iteratec.osm.csi.TestDataUtil
import de.iteratec.osm.persistence.DbCleanupService

/**
 * Integration test for DbCleanupService
 *
 * @author rhc
 */
@TestMixin(IntegrationTestMixin)
class DbCleanupServiceSpec extends IntTestWithDBCleanup {

    DbCleanupService dbCleanupService

    DateTime executionDateBeforeCleanUpDate = new DateTime(2014,2,9,0,0,0)
    DateTime executionDateAfterCleanUpDate = new DateTime()
    JobResult jobResultWithBeforeCleanupDate
    JobResult jobResultWithAfterCleanupDate
    MeasuredValue measuredValueWithBeforeCleanupDate
    MeasuredValue measuredValueWithAfterCleanupDate

    /**
     *  Test for  {@link DbCleanupService.deleteResultsDataBefore(toDeleteBefore)}.
     */
    void testDeleteResultsDataBefore() {

        // before DbCleanupJob execution
        assertThat(JobResult.list().size(), is(2))
        assertThat(EventResult.list().size(), is(2))
        assertThat(HttpArchive.list().size(), is(2))

        assertThat(WebPageTestServer.list().size(), is(1))
        assertThat(JobGroup.list().size(), is(1))
        assertThat(Browser.list().size(), is(1))
        assertThat(Location.list().size(), is(1))
        assertThat(Page.list().size(), is(1))
        assertThat(Script.list().size(), is(1))
        assertThat(MeasuredEvent.list().size(), is(1))
        assertThat(MeasuredValue.list().size(), is(2))
        assertThat(MeasuredValueUpdateEvent.list().size(), is(2))

        //delete all {@link JobResult}s, {@link EventResult}s, {@link HttpArchive}s, {@link MeasuredValue}s, {@link MeasuredValueUpdateEvent}s older then one year (12 months)
        dbCleanupService.deleteResultsDataBefore(new DateTime().minusMonths(12).toDate())

        //after DbCleanupJob execution
        assertThat(WebPageTestServer.list().size(), is(1))
        assertThat(JobGroup.list().size(), is(1))
        assertThat(Browser.list().size(), is(1))
        assertThat(Location.list().size(), is(1))
        assertThat(Page.list().size(), is(1))
        assertThat(Script.list().size(), is(1))
        assertThat(MeasuredEvent.list().size(), is(1))

        assertThat(JobResult.list().size(), is(1))
        //check that the correct JobResult is deleted
        assertThat(JobResult.findById(jobResultWithBeforeCleanupDate.ident()), is(nullValue()))
        assertThat(JobResult.findById(jobResultWithAfterCleanupDate.ident()), is(notNullValue()))

        assertThat(EventResult.list().size(), is(1))
        //check that the correct EventResult is deleted
        assertThat(EventResult.findByJobResultDate(executionDateBeforeCleanUpDate.toDate()), is(nullValue()))
        assertThat(EventResult.findByJobResultDate(executionDateAfterCleanUpDate.toDate()), is(notNullValue()))

        assertThat(HttpArchive.list().size(), is(1))
        //check that the correct HttpArchive is deleted
        assertThat(HttpArchive.findByJobResult(jobResultWithBeforeCleanupDate), is(nullValue()))
        assertThat(HttpArchive.findByJobResult(jobResultWithAfterCleanupDate), is(notNullValue()))

        assertThat(MeasuredValue.list().size(), is(1))
        //check that the correct MeasuredValue is deleted
        assertThat(MeasuredValue.findByStarted(executionDateBeforeCleanUpDate.toDate()), is(nullValue()))
        assertThat(MeasuredValue.findByStarted(executionDateAfterCleanUpDate.toDate()), is(notNullValue()))

        assertThat(MeasuredValueUpdateEvent.list().size(), is(1))
        //check that the correct MeasuredValueUpdateEvent is deleted
        assertThat(MeasuredValueUpdateEvent.findByMeasuredValueId(measuredValueWithBeforeCleanupDate.ident()), is(nullValue()))
        assertThat(MeasuredValueUpdateEvent.findByMeasuredValueId(measuredValueWithAfterCleanupDate.ident()), is(notNullValue()))
    }

    @Before
    void createData(){

        TestDataUtil.createOsmConfig()
        TestDataUtil.createMeasuredValueIntervals()
        TestDataUtil.createAggregatorTypes()

        WebPageTestServer server = TestDataUtil.createWebPageTestServer("server 1 - wpt server", "server 1 - wpt server", true, "http://server1.wpt.server.de")
        JobGroup jobGroup = TestDataUtil.createJobGroup("TestGroup", JobGroupType.CSI_AGGREGATION)
        Browser fireFoxBrowser = TestDataUtil.createBrowser("FF", 0.55)
        Location ffAgent1 = TestDataUtil.createLocation(server, "physNetLabAgent01-FF", fireFoxBrowser, true)

        Page homepage = new Page(
                name: 'homepage',
                weight: 0.5
        ).save(failOnError: true)

        Script script = Script.createDefaultScript('Unnamed').save(failOnError: true)
        Job job = TestDataUtil.createJob("BV1 - Step 01", script, ffAgent1, jobGroup, "This is job 01...", 1, false, 60)
        MeasuredEvent measuredEvent = TestDataUtil.createMeasuredEvent("Test event", homepage)
        String eventResultTag = "$jobGroup.id;$measuredEvent.id;$homepage.id;$fireFoxBrowser.id;$ffAgent1.id";

        //create {@link JobResult} {@link EventResult} {@link HttpArchive} {@link MeasuredValue} {@link MeasuredValueUpdateEvent} with date before cleanupDate
        jobResultWithBeforeCleanupDate = new JobResult(
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
                httpStatusCode : 200,
        ).save(failOnError: true)

        EventResult eventResult1 = new EventResult(
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
                firstStatusUpdate: jobResultWithBeforeCleanupDate.date,
                lastStatusUpdate: jobResultWithBeforeCleanupDate.date,
                wptStatus: 0,
                validationState : 'validationState',
                harData: 'harData',
                customerSatisfactionInPercent:  1,
                jobResultDate: jobResultWithBeforeCleanupDate.date,
                jobResultJobConfigId: jobResultWithBeforeCleanupDate.job.ident(),
                measuredEvent: null,
                speedIndex: EventResult.SPEED_INDEX_DEFAULT_VALUE,
                tag: eventResultTag
        ).save(failOnError: true)
        jobResultWithBeforeCleanupDate.eventResults.add(eventResult1)
        jobResultWithBeforeCleanupDate.save(failOnError: true)

        TestDataUtil.createHttpArchive(jobResultWithBeforeCleanupDate)

        measuredValueWithBeforeCleanupDate = TestDataUtil.createMeasuredValue(
                jobResultWithBeforeCleanupDate.date,
                MeasuredValueInterval.findByIntervalInMinutes(MeasuredValueInterval.HOURLY),
                AggregatorType.findByName(AggregatorType.MEASURED_EVENT),
                eventResultTag,
                0.9902,
                eventResult1.ident().toString(),
                true
            ).save(failOnError: true)
        TestDataUtil.createMeasuredValueUpdateEvent(
                jobResultWithBeforeCleanupDate.date,
                MeasuredValueUpdateEvent.UpdateCause.CALCULATED,
                measuredValueWithBeforeCleanupDate.ident().toString()
            )

        //create {@link JobResult} {@link EventResult} {@link HttpArchive} {@link MeasuredValue} {@link MeasuredValueUpdateEvent} with date after cleanupDate
        jobResultWithAfterCleanupDate = new JobResult(
                job: job,
                date: executionDateAfterCleanUpDate.toDate(),
                testId: '1',
                description: 'jobResultWithAfterCleanupDate',
                jobConfigLabel: job.label,
                jobConfigRuns: job.runs,
                jobGroupName: job.jobGroup.name,
                frequencyInMin: 5,
                locationLocation: job.location.location,
                locationBrowser: job.location.browser.name,
                httpStatusCode : 200,
        ).save(failOnError: true)

        EventResult eventResult2 = new EventResult(
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
                firstStatusUpdate: jobResultWithAfterCleanupDate.date,
                lastStatusUpdate: jobResultWithAfterCleanupDate.date,
                wptStatus: 0,
                validationState : 'validationState',
                harData: 'harData',
                customerSatisfactionInPercent:  1,
                jobResultDate: jobResultWithAfterCleanupDate.date,
                jobResultJobConfigId: jobResultWithAfterCleanupDate.job.ident(),
                measuredEvent: null,
                speedIndex: EventResult.SPEED_INDEX_DEFAULT_VALUE,
                tag: eventResultTag
        ).save(failOnError: true)
        jobResultWithAfterCleanupDate.eventResults.add(eventResult2)
        jobResultWithAfterCleanupDate.save(failOnError: true)

        TestDataUtil.createHttpArchive(jobResultWithAfterCleanupDate)

        measuredValueWithAfterCleanupDate = TestDataUtil.createMeasuredValue(
                jobResultWithAfterCleanupDate.date,
                MeasuredValueInterval.findByIntervalInMinutes(MeasuredValueInterval.HOURLY),
                AggregatorType.findByName(AggregatorType.MEASURED_EVENT),
                eventResultTag,
                0.9902,
                eventResult2.ident().toString(),
                true
        ).save(failOnError: true)
        TestDataUtil.createMeasuredValueUpdateEvent(
                jobResultWithAfterCleanupDate.date,
                MeasuredValueUpdateEvent.UpdateCause.CALCULATED,
                measuredValueWithAfterCleanupDate.ident().toString()
        )
    }
}
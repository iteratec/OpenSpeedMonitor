package de.iteratec.osm.result.dao

import de.iteratec.osm.OsmConfiguration
import de.iteratec.osm.api.dto.ApplicationCsiDto
import de.iteratec.osm.csi.CsiConfiguration
import de.iteratec.osm.csi.NonTransactionalIntegrationSpec
import de.iteratec.osm.csi.Page
import de.iteratec.osm.measurement.schedule.Job
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.measurement.script.Script
import de.iteratec.osm.report.chart.CsiAggregationInterval
import de.iteratec.osm.result.ApplicationDashboardService
import de.iteratec.osm.result.EventResult
import de.iteratec.osm.result.JobResult
import de.iteratec.osm.result.WptStatus
import grails.testing.mixin.integration.Integration
import grails.transaction.Rollback

@Integration(applicationClass = openspeedmonitor.Application.class)
@Rollback
class ApplicationDashboardServiceIntSpec extends NonTransactionalIntegrationSpec {
    ApplicationDashboardService applicationDashboardService

    JobGroup jobGroup1, jobGroup2
    Page page1, page2, page3, pageUndefined
    Job job1
    JobResult jobResult1, jobResult2
    Script script1
    CsiConfiguration existingCsiConfiguration

    private static final String NAVIGATION_SCRIPT =
            "setEventName\tHomepage:::Homepage\n" +
                    "navigate\thttps://www.iteratec.de/\n" +
                    "setEventName\tDetail:::Branchen\n" +
                    "navigate\thttps://www.iteratec.de/branch"


    def setup() {
        createTestDataCommonForAllTests()
    }

    def cleanup() {
    }

    void "get pages by event results and active jobs"() {
        given: "one matching EventResults, one 'undefined' matching EventResult and one other Result"
        EventResult.build(
                page: page1,
                jobGroup: jobGroup2,
                fullyLoadedTimeInMillisecs: 100,
                medianValue: true,
                dateCreated: new Date(),
                flush: true
        )

        EventResult.build(
                page: page3,
                jobGroup: jobGroup1,
                fullyLoadedTimeInMillisecs: 100,
                medianValue: true,
                dateCreated: new Date(),
                flush: true
        )

        EventResult.build(
                page: pageUndefined,
                jobGroup: jobGroup1,
                fullyLoadedTimeInMillisecs: 100,
                medianValue: true,
                dateCreated: new Date(),
                flush: true
        )

        when: "the application service determines the recent measured or/and active pages (without undefined page)"
        def recentMeasuredPages = applicationDashboardService.getRecentMetricsForJobGroup(jobGroup1.id)
        def activePages = applicationDashboardService.getPagesOfActiveJobs(jobGroup1.id)
        def allActiveOrMeasuredPages = applicationDashboardService.getAllActivePagesAndMetrics(jobGroup1.id)

        then: "there is one page found by the event result and two pages by an active job"
        recentMeasuredPages.size() == 1
        activePages.size() == 2
        allActiveOrMeasuredPages.size() == 3
    }

    void "get pages only by an active job without results"() {
        given: "one 'undefined' matching and one other EventResult"
        EventResult.build(
                page: page3,
                jobGroup: jobGroup2,
                fullyLoadedTimeInMillisecs: 100,
                medianValue: true,
                dateCreated: new Date(),
                flush: true
        )
        EventResult.build(
                page: pageUndefined,
                jobGroup: jobGroup1,
                fullyLoadedTimeInMillisecs: 100,
                medianValue: true,
                dateCreated: new Date(),
                flush: true
        )

        when: "the application service determines the recent measured or/and active pages (without undefined page)"
        def recentMeasuredPages = applicationDashboardService.getRecentMetricsForJobGroup(jobGroup1.id)
        def activePages = applicationDashboardService.getPagesOfActiveJobs(jobGroup1.id)
        def allActiveOrMeasuredPages = applicationDashboardService.getAllActivePagesAndMetrics(jobGroup1.id)

        then: "there is no page found by an event result and two by the active job"
        recentMeasuredPages.size() == 0
        activePages.size() == 2
        allActiveOrMeasuredPages.size() == 2
    }

    void "get pages only by event results"() {
        given: "one matching EventResults, one 'undefined' matching EventResult and one other Result"
        EventResult.build(
                page: page3,
                jobGroup: jobGroup1,
                fullyLoadedTimeInMillisecs: 100,
                medianValue: true,
                dateCreated: new Date(),
                flush: true
        )

        EventResult.build(
                page: page3,
                jobGroup: jobGroup2,
                fullyLoadedTimeInMillisecs: 100,
                medianValue: true,
                dateCreated: new Date(),
                flush: true
        )

        EventResult.build(
                page: pageUndefined,
                jobGroup: jobGroup2,
                fullyLoadedTimeInMillisecs: 100,
                medianValue: true,
                dateCreated: new Date(),
                flush: true
        )

        when: "the application service determines the recent measured or/and active pages without undefined"
        def recentMeasuredPages = applicationDashboardService.getRecentMetricsForJobGroup(jobGroup2.id)
        def activePages = applicationDashboardService.getPagesOfActiveJobs(jobGroup2.id)
        def allActiveOrMeasuredPages = applicationDashboardService.getAllActivePagesAndMetrics(jobGroup2.id)

        then: "there is one page found by an event result and no page by an active job"
        recentMeasuredPages.size() == 1
        activePages.size() == 0
        allActiveOrMeasuredPages.size() == 1
    }

    void "get no page by EventResults or active jobs"() {
        given: "one 'undefined' matching EventResult and one other Result"
        EventResult.build(
                page: page3,
                jobGroup: jobGroup1,
                fullyLoadedTimeInMillisecs: 100,
                medianValue: true,
                dateCreated: new Date(),
                flush: true
        )

        EventResult.build(
                page: pageUndefined,
                jobGroup: jobGroup2,
                fullyLoadedTimeInMillisecs: 100,
                medianValue: true,
                dateCreated: new Date(),
                flush: true
        )

        when: "the application service determines the recent measured or/and active pages (without undefined page)"
        def recentMeasuredPages = applicationDashboardService.getRecentMetricsForJobGroup(jobGroup2.id)
        def activePages = applicationDashboardService.getPagesOfActiveJobs(jobGroup2.id)
        def allActiveOrMeasuredPages = applicationDashboardService.getAllActivePagesAndMetrics(jobGroup2.id)

        then: "there is no page found by an event result and no page by an active job"
        recentMeasuredPages.size() == 0
        activePages.size() == 0
        allActiveOrMeasuredPages.size() == 0
    }

    void "create a new inital csi for a job group if none exists"() {
        given: "one JobGroup without and one JobGroup with CSI Configuration"
        existingCsiConfiguration = CsiConfiguration.build()
        jobGroup1.csiConfiguration = existingCsiConfiguration
        jobGroup1.save(failOnError: true, flush: true)

        when: "the application service creates a csi configuration for a job group if none exists"
        def csiConfigurationId1 = applicationDashboardService.createOrReturnCsiConfiguration(jobGroup1.id)
        def csiConfigurationId2 = applicationDashboardService.createOrReturnCsiConfiguration(jobGroup2.id)

        then: "a csi has been created for the jobgroup which had none"
        jobGroup1.csiConfiguration.id == csiConfigurationId1
        JobGroup jobGroupWithCreatedCsiConfiguration = JobGroup.findById(jobGroup2.id)
        jobGroupWithCreatedCsiConfiguration.csiConfiguration.id == csiConfigurationId2
    }

    void "return error for invalid JobResults if all JobResults are invalid"() {
        given: "two JobResults with an error"
        existingCsiConfiguration = CsiConfiguration.build()
        CsiAggregationInterval.build(intervalInMinutes: CsiAggregationInterval.DAILY)
        jobGroup1.csiConfiguration = existingCsiConfiguration
        jobResult1 = JobResult.build(httpStatusCode: WptStatus.TIME_OUT.getWptStatusCode(), job: job1)
        jobResult2 = JobResult.build(httpStatusCode: WptStatus.TIME_OUT.getWptStatusCode(), job: job1)

        when: "the application service tests for errors"
        ApplicationCsiDto applicationCsiDto = applicationDashboardService.getCsiValuesAndErrorsForJobGroup(jobGroup1)

        then: "an error is returned, all JobResults are invalid"
        applicationCsiDto.hasInvalidJobResults
    }

    void "return no error for invalid JobResults if not all JobResults are invalid"() {
        given: "one JobResult without and one with error"
        existingCsiConfiguration = CsiConfiguration.build()
        CsiAggregationInterval.build(intervalInMinutes: CsiAggregationInterval.DAILY)
        jobGroup1.csiConfiguration = existingCsiConfiguration
        jobResult1 = JobResult.build(httpStatusCode: WptStatus.SUCCESSFUL.getWptStatusCode(), job: job1)
        jobResult2 = JobResult.build(httpStatusCode: WptStatus.TIME_OUT.getWptStatusCode(), job: job1)

        when: "the application service tests for errors"
        ApplicationCsiDto applicationCsiDto = applicationDashboardService.getCsiValuesAndErrorsForJobGroup(jobGroup1)

        then: "no error is returned, not all JobResults are invalid"
        !applicationCsiDto.hasInvalidJobResults
    }

    private void createTestDataCommonForAllTests() {
        OsmConfiguration.build()

        page1 = Page.build(name: "Homepage")
        page2 = Page.build(name: "Detail")
        page3 = Page.build(name: "Test")
        pageUndefined = Page.build(name: Page.UNDEFINED)

        jobGroup1 = JobGroup.build()
        jobGroup2 = JobGroup.build()

        script1 = Script.build(navigationScript: NAVIGATION_SCRIPT)

        job1 = Job.build(
                active: true,
                executionSchedule: '0 */15 * * * ? 2015',
                script: script1,
                jobGroup: jobGroup1
        )
    }
}

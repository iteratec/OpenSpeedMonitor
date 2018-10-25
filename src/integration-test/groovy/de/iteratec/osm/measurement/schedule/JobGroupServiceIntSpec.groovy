package de.iteratec.osm.measurement.schedule

import de.iteratec.osm.OsmConfiguration
import de.iteratec.osm.csi.NonTransactionalIntegrationSpec
import de.iteratec.osm.csi.Page
import de.iteratec.osm.measurement.script.Script
import de.iteratec.osm.result.EventResult
import de.iteratec.osm.result.JobResult
import de.iteratec.osm.result.ResultSelectionService
import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration

@Integration(applicationClass = openspeedmonitor.Application.class)
@Rollback
class JobGroupServiceIntSpec extends NonTransactionalIntegrationSpec {
    JobGroupService jobGroupService

    JobGroup notActiveButMeasured, jobGroup1, jobGroup2
    Page page1, page2, page3, pageUndefined

    private static final String NAVIGATION_SCRIPT =
            "setEventName\tHomepage:::Homepage\n" +
                    "navigate\thttps://www.iteratec.de/\n" +
                    "setEventName\tDetail:::Branchen\n" +
                    "navigate\thttps://www.iteratec.de/branch"

    def setup() {
        jobGroupService.resultSelectionService = Stub(ResultSelectionService) {
            query(_, _, _) >> {
                List result = new ArrayList<>()
                result.add(notActiveButMeasured)
                return result
            }
        }
        createTestDataCommonForAllTests()
    }

    def cleanup() {
        jobGroupService.resultSelectionService = grailsApplication.mainContext.getBean('resultSelectionService') as ResultSelectionService
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
        def recentMeasuredPages = jobGroupService.getPagesWithExistingEventResults(jobGroup1.id)
        def activePages = jobGroupService.getPagesOfActiveJobs(jobGroup1.id)
        def allActiveOrMeasuredPages = jobGroupService.getPagesWithResultsOrActiveJobsForJobGroup(jobGroup1.id)

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
        def recentMeasuredPages = jobGroupService.getPagesWithExistingEventResults(jobGroup1.id)
        def activePages = jobGroupService.getPagesOfActiveJobs(jobGroup1.id)
        def allActiveOrMeasuredPages = jobGroupService.getPagesWithResultsOrActiveJobsForJobGroup(jobGroup1.id)

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
        def recentMeasuredPages = jobGroupService.getPagesWithExistingEventResults(jobGroup2.id)
        def activePages = jobGroupService.getPagesOfActiveJobs(jobGroup2.id)
        def allActiveOrMeasuredPages = jobGroupService.getPagesWithResultsOrActiveJobsForJobGroup(jobGroup2.id)

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
        def recentMeasuredPages = jobGroupService.getPagesWithExistingEventResults(jobGroup2.id)
        def activePages = jobGroupService.getPagesOfActiveJobs(jobGroup2.id)
        def allActiveOrMeasuredPages = jobGroupService.getPagesWithResultsOrActiveJobsForJobGroup(jobGroup2.id)

        then: "there is no page found by an event result and no page by an active job"
        recentMeasuredPages.size() == 0
        activePages.size() == 0
        allActiveOrMeasuredPages.size() == 0
    }

    void "find active and recently measured job groups"() {
        given: "one active but no measured, not active but recently measured and one neither active nor measured"
        JobGroup activeNotMeasured = JobGroup.build(name: "activeNotMeasured")
        notActiveButMeasured = JobGroup.build(name: "notActiveButMeasured")
        JobGroup notActiveNotMeasured = JobGroup.build(name: "notActiveNotMeasured")

        Job activeNotMeasuredJob = Job.build(jobGroup: activeNotMeasured, active: true, executionSchedule: "1/1 * * ? * *")
        Job notActiveButMeasuredJob = Job.build(jobGroup: notActiveButMeasured, active: false)

        JobResult notActiveButMeasuredJobResult = JobResult.build(jobGroupName: notActiveButMeasured.name, date: new Date())

        when: "service is asked for active or recently measured"
        List activeOrRecent = jobGroupService.getAllActiveAndRecentWithResultInformation()

        then: "only active or recently measured are returned"
        activeOrRecent.size() == 2
        activeOrRecent.every {
            it.name != notActiveNotMeasured.name
        }
        notActiveButMeasured.name == activeOrRecent.find {
            it.dateOfLastResults == notActiveButMeasuredJobResult.date.format("yyyy-MM-dd")
        }.name
        activeNotMeasured.name == activeOrRecent.find {
            it.dateOfLastResults != notActiveButMeasuredJobResult.date.format("yyyy-MM-dd")
        }.name
    }

    private void createTestDataCommonForAllTests() {
        OsmConfiguration.build()

        page1 = Page.build(name: "Homepage")
        page2 = Page.build(name: "Detail")
        page3 = Page.build(name: "Test")
        pageUndefined = Page.build(name: Page.UNDEFINED)

        jobGroup1 = JobGroup.build()
        jobGroup2 = JobGroup.build()

        Job.build(
                active: true,
                executionSchedule: '0 */15 * * * ? 2015',
                script: Script.build(navigationScript: NAVIGATION_SCRIPT),
                jobGroup: jobGroup1
        )
    }
}

package de.iteratec.osm.result.dao

import de.iteratec.osm.OsmConfiguration
import de.iteratec.osm.csi.NonTransactionalIntegrationSpec
import de.iteratec.osm.csi.Page
import de.iteratec.osm.measurement.schedule.Job
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.measurement.script.Script
import de.iteratec.osm.result.ApplicationDashboardService
import de.iteratec.osm.result.EventResult
import grails.testing.mixin.integration.Integration
import grails.transaction.Rollback

@Integration(applicationClass = openspeedmonitor.Application.class)
@Rollback
class ApplicationDashboardServiceIntSpec extends NonTransactionalIntegrationSpec {
    ApplicationDashboardService applicationDashboardService

    JobGroup jobGroup1
    Page page1, page2, page3, pageUndefiend
    Job job1
    Script script1

    def setup() {
        createTestDataCommonForAllTests()
    }

    def cleanup() {
    }

    void "get active or measured pages and metrics of jobgroup without undefined"() {
        EventResult.build(
                page: page1,
                jobGroup: jobGroup1,
                fullyLoadedTimeInMillisecs: 100,
                medianValue: true,
                dateCreated: new Date()
        )
        EventResult.build(
                page: page2,
                jobGroup: jobGroup1,
                fullyLoadedTimeInMillisecs: 100,
                medianValue: true,
                dateCreated: new Date()
        )
        EventResult.build(
                page: page3,
                jobGroup: jobGroup1,
                fullyLoadedTimeInMillisecs: 100,
                medianValue: true,
                dateCreated: new Date()
        )
        EventResult.build(
                page: pageUndefiend,
                jobGroup: jobGroup1,
                fullyLoadedTimeInMillisecs: 100,
                medianValue: true,
                dateCreated: new Date()
        )

        def result = applicationDashboardService.getAllActivePagesAndMetrics(jobGroup1.id)

        expect: "fix me"
        result.size() == 2
    }

    void "get empty result if no page of jobgroup is active or has relevant measured values"() {
        EventResult.build(
                page: page3,
                jobGroup: jobGroup1,
                fullyLoadedTimeInMillisecs: 100,
                medianValue: true,
                dateCreated: new Date()
        )
        EventResult.build(
                page: pageUndefiend,
                jobGroup: jobGroup1,
                fullyLoadedTimeInMillisecs: 100,
                medianValue: true,
                dateCreated: new Date()
        )

        def result = applicationDashboardService.getAllActivePagesAndMetrics(jobGroup1.id)

        expect: "fix me"
        result.size() == 0
    }

    private void createTestDataCommonForAllTests() {
        OsmConfiguration.build()

        page1 = Page.build()
        page2 = Page.build()
        page3 = Page.build()
        pageUndefiend = Page.build(name: Page.UNDEFINED)

        script1 = Script.build(
                testedPages: [page1, page2, pageUndefiend]
        )
        jobGroup1 = JobGroup.build()

        job1 = Job.build(
                active: true,
                executionSchedule: '0 */15 * * * ? 2015',
                script: script1,
                jobGroup: jobGroup1
        )
    }
}

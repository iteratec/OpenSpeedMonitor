package de.iteratec.osm.result.dao

import de.iteratec.osm.OsmConfiguration
import de.iteratec.osm.api.dto.ApplicationCsiDto
import de.iteratec.osm.csi.CsiConfiguration
import de.iteratec.osm.csi.NonTransactionalIntegrationSpec
import de.iteratec.osm.csi.Page
import de.iteratec.osm.measurement.schedule.Job
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.measurement.schedule.JobStatistic
import de.iteratec.osm.measurement.script.Script
import de.iteratec.osm.report.chart.AggregationType
import de.iteratec.osm.report.chart.CsiAggregation
import de.iteratec.osm.report.chart.CsiAggregationInterval
import de.iteratec.osm.result.ApplicationDashboardService
import de.iteratec.osm.result.JobResult
import de.iteratec.osm.result.JobResultStatus
import grails.testing.mixin.integration.Integration
import grails.transaction.Rollback
import org.joda.time.DateTime

@Integration(applicationClass = openspeedmonitor.Application.class)
@Rollback
class ApplicationDashboardServiceIntSpec extends NonTransactionalIntegrationSpec {
    ApplicationDashboardService applicationDashboardService

    JobGroup jobGroup1, jobGroup2
    Page page1, page2, page3, pageUndefined
    Job job1
    JobStatistic jobStatistic
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

    void "return CSI from today for two job groups"() {
        given: "two CSI Aggregations from today"
        existingCsiConfiguration = CsiConfiguration.build()
        def interval = CsiAggregationInterval.build(intervalInMinutes: CsiAggregationInterval.DAILY)
        jobGroup1.csiConfiguration = existingCsiConfiguration
        jobGroup2.csiConfiguration = existingCsiConfiguration
        def today = new DateTime().withTimeAtStartOfDay().toDate()
        def yesterday = new DateTime().withTimeAtStartOfDay().minusDays(1).toDate()
        CsiAggregation.build(jobGroup: jobGroup1, started: yesterday, interval: interval, aggregationType: AggregationType.JOB_GROUP,
                closedAndCalculated: true, csByWptDocCompleteInPercent: 20.0d, csByWptVisuallyCompleteInPercent: 20.0d)
        CsiAggregation.build(jobGroup: jobGroup1, started: today, interval: interval, aggregationType: AggregationType.JOB_GROUP,
                closedAndCalculated: true, csByWptDocCompleteInPercent: 50.0d, csByWptVisuallyCompleteInPercent: 60.0d)
        CsiAggregation.build(jobGroup: jobGroup2, started: today, interval: interval, aggregationType: AggregationType.JOB_GROUP,
                closedAndCalculated: true, csByWptDocCompleteInPercent: 70.0d, csByWptVisuallyCompleteInPercent: 80.0d)


        when: "the application service tests for errors"
        Map<Long, ApplicationCsiDto> applicationCsiDtos = applicationDashboardService.getTodaysCsiValueForJobGroups([jobGroup1, jobGroup2])

        then: "an error is returned, all JobResults are invalid"
        applicationCsiDtos[jobGroup1.id].hasCsiConfiguration == true
        applicationCsiDtos[jobGroup1.id].hasJobResults == true
        applicationCsiDtos[jobGroup1.id].hasInvalidJobResults == false
        applicationCsiDtos[jobGroup1.id].csiValues.length == 1
        applicationCsiDtos[jobGroup1.id].csiValues[0].date == today.format("yyyy-MM-dd")
        applicationCsiDtos[jobGroup1.id].csiValues[0].csiDocComplete == 50.0d
        applicationCsiDtos[jobGroup1.id].csiValues[0].csiVisComplete == 60.0d

        applicationCsiDtos[jobGroup2.id].hasCsiConfiguration == true
        applicationCsiDtos[jobGroup2.id].hasJobResults == true
        applicationCsiDtos[jobGroup2.id].hasInvalidJobResults == false
        applicationCsiDtos[jobGroup2.id].csiValues.length == 1
        applicationCsiDtos[jobGroup2.id].csiValues[0].date == today.format("yyyy-MM-dd")
        applicationCsiDtos[jobGroup2.id].csiValues[0].csiDocComplete == 70.0d
        applicationCsiDtos[jobGroup2.id].csiValues[0].csiVisComplete == 80.0d

    }

    void "return error for invalid JobResults if all JobResults are invalid"() {
        given: "two JobResults with an error"
        existingCsiConfiguration = CsiConfiguration.build()
        CsiAggregationInterval.build(intervalInMinutes: CsiAggregationInterval.DAILY)
        jobGroup1.csiConfiguration = existingCsiConfiguration
        jobResult1 = JobResult.build(jobResultStatus: JobResultStatus.TIMEOUT, job: job1)
        jobResult2 = JobResult.build(jobResultStatus: JobResultStatus.TIMEOUT, job: job1)

        when: "the application service tests for errors"
        ApplicationCsiDto applicationCsiDto = applicationDashboardService.getCsiValuesAndErrorsForJobGroup(jobGroup1)

        then: "an error is returned, all JobResults are invalid"
        applicationCsiDto.hasInvalidJobResults == true
    }

    void "return no error for invalid JobResults if not all JobResults are invalid"() {
        given: "one JobResult without and one with error"
        existingCsiConfiguration = CsiConfiguration.build()
        CsiAggregationInterval.build(intervalInMinutes: CsiAggregationInterval.DAILY)
        jobGroup1.csiConfiguration = existingCsiConfiguration
        jobResult1 = JobResult.build(jobResultStatus: JobResultStatus.SUCCESS, job: job1)
        jobResult2 = JobResult.build(jobResultStatus: JobResultStatus.TIMEOUT, job: job1)

        when: "the application service tests for errors"
        ApplicationCsiDto applicationCsiDto = applicationDashboardService.getCsiValuesAndErrorsForJobGroup(jobGroup1)

        then: "no error is returned, not all JobResults are invalid"
        applicationCsiDto.hasInvalidJobResults == false
    }

    void "check if csi config flag is set correctly"() {
        given: "one JobGroup with CsiConfiguration and one without"
        existingCsiConfiguration = CsiConfiguration.build()
        CsiAggregationInterval.build(intervalInMinutes: CsiAggregationInterval.DAILY)
        jobGroup2.csiConfiguration = existingCsiConfiguration
        jobGroup2.save(failOnError: true, flush: true)

        when: "the application service tests for csi configuration"
        ApplicationCsiDto applicationCsiDto1 = applicationDashboardService.getCsiValuesAndErrorsForJobGroup(jobGroup1)
        ApplicationCsiDto applicationCsiDto2 = applicationDashboardService.getCsiValuesAndErrorsForJobGroup(jobGroup2)

        then: "the JobGroup without config should return false, the other true"
        applicationCsiDto1.hasCsiConfiguration == false
        applicationCsiDto1.csiValues.length == 0
        applicationCsiDto2.hasCsiConfiguration == true
    }

    void "check if failing job statistic is returned correctly"() {
        given: "three Jobs, one with matching JobStatistic"
        jobStatistic = JobStatistic.build(percentageSuccessfulTestsOfLast5: 80)
        job1.jobStatistic = jobStatistic
        Job job2 = Job.build(active: true, executionSchedule: '0 */15 * * * ? 2015', jobGroup: jobGroup1, jobStatistic: JobStatistic.build())
        Job job3 = Job.build(active: true, executionSchedule: '0 */15 * * * ? 2015', jobGroup: jobGroup1)
        job1.save(failOnError: true, flush: true)
        job2.save(failOnError: true, flush: true)
        job3.save(failOnError: true, flush: true)

        when: "the failingJobStatistics are retrieved"
        def failingJobStatistics = applicationDashboardService.getFailingJobStatistics(jobGroup1.id)

        then: "the returned statistic is correct"
        failingJobStatistics[0] == 1
        failingJobStatistics[1] == 80d
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

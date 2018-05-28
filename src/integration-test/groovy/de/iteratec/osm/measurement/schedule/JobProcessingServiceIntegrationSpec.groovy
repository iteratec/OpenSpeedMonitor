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

import de.iteratec.osm.InMemoryConfigService
import de.iteratec.osm.csi.NonTransactionalIntegrationSpec
import de.iteratec.osm.measurement.environment.Browser
import de.iteratec.osm.measurement.environment.Location
import de.iteratec.osm.measurement.environment.QueueAndJobStatusService
import de.iteratec.osm.measurement.environment.WebPageTestServer
import de.iteratec.osm.measurement.environment.wptserverproxy.ProxyService
import de.iteratec.osm.measurement.script.Script
import de.iteratec.osm.result.JobResult
import de.iteratec.osm.result.WptStatus
import grails.test.mixin.integration.Integration
import grails.transaction.Rollback
import org.joda.time.DateTime
import org.joda.time.DateTimeUtils
import org.quartz.Trigger
import org.quartz.TriggerKey
import org.quartz.impl.triggers.CronTriggerImpl

/**
 * Integration test for JobProcessingService
 *
 * @author dri
 */
@Integration
@Rollback
class JobProcessingServiceIntegrationSpec extends NonTransactionalIntegrationSpec {
    JobProcessingService jobProcessingService
    QueueAndJobStatusService queueAndJobStatusService
    JobDaoService jobDaoService

    final static String UNNAMED_JOB_LABEL = 'Unnamed Job'
    /**
     * Cron strings designed for Quartz jobs to never be executed before integration test ends
     */
    private final static String CRON_STRING_1 = '* * */12 * * ?'
    private final static String CRON_STRING_2 = '* * */13 * * ?'

    ConnectivityProfile connectivityProfile
    Script script
    Location location
    JobGroup jobGroup

    def setup() {

        // mocks common for all tests
        jobProcessingService.proxyService = Stub(ProxyService) {
            runtest(_, _) >> new XmlSlurper().parseText("""
                    <response>
                        <statusCode>200</statusCode>
                        <statusText>Ok</statusText>
                        <data>
                            <testId>${HttpRequestServiceMock.testId}</testId>
                            <ownerKey>f942624563a31387d20025513d50a350b37a17f1</ownerKey>
                            <xmlUrl>http://dev.server01.wpt.iteratec.de/xmlResult/${HttpRequestServiceMock.testId}/</xmlUrl>
                            <userUrl>${HttpRequestServiceMock.redirectUserUrl}</userUrl>
                            <summaryCSV>http://dev.server01.wpt.iteratec.de/result/${HttpRequestServiceMock.testId}/page_data.csv</summaryCSV>
                            <detailCSV>http://dev.server01.wpt.iteratec.de/result/${HttpRequestServiceMock.testId}/requests.csv</detailCSV>
                            <jsonUrl>http://dev.server01.wpt.iteratec.de/jsonResult.php?test=${
                HttpRequestServiceMock.testId
            }/</jsonUrl>
                        </data>
                    </response>
            """)
        }
        jobProcessingService.proxyService.httpRequestService = new HttpRequestServiceMock()

        //test data common for all tests
        jobProcessingService.inMemoryConfigService = new InMemoryConfigService()
        jobProcessingService.inMemoryConfigService.activateMeasurementsGenerally()

        WebPageTestServer wptServer = WebPageTestServer.build(
                label: 'Unnamed server',
                proxyIdentifier: 'proxy_identifier',
                dateCreated: new Date(),
                lastUpdated: new Date(),
                active: true,
                baseUrl: 'http://example.com').save(failOnError: true)
        Browser browser = Browser.build(name: 'browser').save(failOnError: true)
        jobGroup = JobGroup.build(
                name: 'Unnamed group',
                graphiteServers: []).save(failOnError: true)

        script = Script.createDefaultScript('Unnamed job').save(failOnError: true)
        location = Location.build(
                label: 'Unnamed location',
                dateCreated: new Date(),
                active: true,
                wptServer: wptServer,
                location: 'location',
                browser: browser
        ).save(failOnError: true)

        connectivityProfile = ConnectivityProfile.build(
                name: "unused",
                bandwidthDown: 6000,
                bandwidthUp: 512,
                latency: 40,
                packetLoss: 0,
                active: true
        ).save(failOnError: true)
        connectivityProfile.connectivityProfileService = new ConnectivityProfileService()
    }


    void "scheduleJob test"() {
        when: "getting trigger from quartzscheduler and triggerKey from scheduled job"
        Job wptJobToSchedule = createAndScheduleJob(CRON_STRING_1)
        TriggerKey triggerKey = getTriggerKeyOf(wptJobToSchedule)
        // check if Job was scheduled with correct Trigger identifier and group
        Trigger insertedTrigger = jobProcessingService.quartzScheduler.getTrigger(triggerKey)

        then: "triggerKey from scheduled job and triggerKey from the trigger of the scheduler are the same"
        insertedTrigger != null
        triggerKey == insertedTrigger.getKey()
        // check if schedule of inserted Trigger matches Cron expression of wptJobToSchedule
        wptJobToSchedule.executionSchedule == getCronExpressionByTriggerKey(triggerKey)
    }

    void "unscheduleJob test"() {
        given: "a scheduled job"
        Job wptJobToSchedule = createAndScheduleJob(CRON_STRING_1)

        when: "unscheduling a job"
        jobProcessingService.unscheduleJob(wptJobToSchedule)

        then: "no trigger for the job is set"
        jobProcessingService.quartzScheduler.getTrigger(getTriggerKeyOf(wptJobToSchedule)) == null
    }

    void "rescheduleJob test"() {
        given: "a scheduled job"
        Job wptJobToSchedule = createAndScheduleJob(CRON_STRING_1)
        wptJobToSchedule.executionSchedule = CRON_STRING_2

        when: "scheduling or rescheduling a job"
        jobProcessingService.scheduleJob(wptJobToSchedule)

        then: "check if schedule matches updated Cron expression of wptJobToSchedule"
        wptJobToSchedule.executionSchedule == getCronExpressionByTriggerKey(getTriggerKeyOf(wptJobToSchedule))

        cleanup: "Unschedule all jobs, to prevent failures in other tests"
        jobProcessingService.unscheduleJob(wptJobToSchedule)
    }

    void "scheduleAllJobs test"() {
        given: "a set of active and inactive jobs"
        Job inactiveJob = createJob(false)
        Job activeJob1 = createJob(true, CRON_STRING_1)
        Job activeJob2 = createJob(true, CRON_STRING_2)

        when: "launching all active jobs"
        jobProcessingService.scheduleAllActiveJobs();

        then: "check if the triggers in the scheduler are correctly set"
        jobProcessingService.quartzScheduler.getTrigger(getTriggerKeyOf(inactiveJob)) == null
        jobProcessingService.quartzScheduler.getTrigger(getTriggerKeyOf(activeJob1)) != null
        jobProcessingService.quartzScheduler.getTrigger(getTriggerKeyOf(activeJob2)) != null

        activeJob1.executionSchedule == getCronExpressionByTriggerKey(getTriggerKeyOf(activeJob1))
        activeJob2.executionSchedule == getCronExpressionByTriggerKey(getTriggerKeyOf(activeJob2))

        cleanup: "Unschedule all jobs, to prevent failures in other tests"
        jobProcessingService.unscheduleJob(activeJob1)
        jobProcessingService.unscheduleJob(activeJob2)
    }

    /**
     * This test creates several JobResults with different status codes and checks whether
     * JobProcessingService.getRunningAndRecentlyFinishedJobs() filters these results correctly.
     * Only results that are no errors or the most recent one should be retained.
     */

    void "closeRunningAndPengingJobs test"() {
        given: "a set of persisted jobResults"
        // fix current date for test purposes
        DateTimeUtils.setCurrentMillisFixed(1482395596904);
        DateTime currentDate = new DateTime()
        Job jobWithMaxDownloadTime = createJob(false)
        jobWithMaxDownloadTime.maxDownloadTimeInMinutes = 60

        createAndPersistJobResult(jobWithMaxDownloadTime, currentDate.toDate(), "running test", location, WptStatus.RUNNING.getWptStatusCode())
        createAndPersistJobResult(jobWithMaxDownloadTime, currentDate.toDate(), "pending test", location, WptStatus.PENDING.getWptStatusCode())
        createAndPersistJobResult(jobWithMaxDownloadTime, currentDate.minusMinutes(2 * jobWithMaxDownloadTime.maxDownloadTimeInMinutes).toDate(), "barely running test", location, WptStatus.RUNNING.getWptStatusCode())
        createAndPersistJobResult(jobWithMaxDownloadTime, currentDate.minusMinutes(2 * jobWithMaxDownloadTime.maxDownloadTimeInMinutes + 1).toDate(), "outdated running test", location, WptStatus.PENDING.getWptStatusCode())
        createAndPersistJobResult(jobWithMaxDownloadTime, currentDate.minusDays(5).toDate(), "outdated pending test", location, WptStatus.PENDING.getWptStatusCode())
        createAndPersistJobResult(jobWithMaxDownloadTime, currentDate.minusDays(5).toDate(), "finished test", location, WptStatus.COMPLETED.getWptStatusCode())
        createAndPersistJobResult(jobWithMaxDownloadTime, currentDate.minusDays(5).toDate(), "failed test", location, WptStatus.TIME_OUT.getWptStatusCode())

        when: "closing running and pending job results"
        jobProcessingService.closeRunningAndPengingJobResults()

        then: "find jobResults and check their httpStatusCode"
        JobResult.findByTestId("running test").httpStatusCode == WptStatus.RUNNING.getWptStatusCode()
        JobResult.findByTestId("pending test").httpStatusCode == WptStatus.PENDING.getWptStatusCode()
        JobResult.findByTestId("barely running test").httpStatusCode == WptStatus.RUNNING.getWptStatusCode()
        JobResult.findByTestId("outdated running test").httpStatusCode == WptStatus.OUTDATED_JOB.getWptStatusCode()
        JobResult.findByTestId("outdated pending test").httpStatusCode == WptStatus.OUTDATED_JOB.getWptStatusCode()
        JobResult.findByTestId("finished test").httpStatusCode == WptStatus.COMPLETED.getWptStatusCode()
        JobResult.findByTestId("failed test").httpStatusCode == WptStatus.TIME_OUT.getWptStatusCode()
    }

    void "statusOfRepeatedJobExecution test"() {
        given: "an inactive job and a date"
        Job.withNewTransaction {
            createJob(false)
        }
        Job job = jobDaoService.getJobById(1)
        Date now = new Date()
        Date oldestDate = now - 5

        when: "creating and persisting jobResults with statusCodes and a job"
        inputStatusCodes.reverse().eachWithIndex { int httpStatusCode, int i ->
            JobResult result = jobProcessingService.persistUnfinishedJobResult(job.id, null, httpStatusCode)
            result.date = now - i
            result.save(flush: true, failOnError: true)
        }
        // test execution
        List recentRuns = queueAndJobStatusService.getRunningAndRecentlyFinishedJobs(oldestDate, oldestDate, oldestDate)[job.id]

        then: "check if the number of jobResults matches the number of statusCodes"
        inputStatusCodes.size() == JobResult.count()
        expectedStatusCodes.size() == recentRuns.size()
        expectedStatusCodes.eachWithIndex { int statusCode, int i ->
            statusCode == recentRuns[i]['status']
        }

        where: "the input status codes are the expected status codes"
        inputStatusCodes || expectedStatusCodes
        [100]            || [100]
        [200]            || [200]
        [400]            || [400]
        [400, 100]       || [100]
        [100, 400]       || [100, 400]
        [200, 400]       || [200, 400]
        [400, 200]       || [200]
        [100, 200]       || [100, 200]
        [200, 100]       || [200, 100]
        [100, 200, 400]  || [100, 200, 400]
        [100, 400, 200]  || [100, 200]
        [200, 100, 400]  || [200, 100, 400]
        [200, 400, 100]  || [200, 100]
        [400, 100, 200]  || [100, 200]
        [400, 200, 100]  || [200, 100]
    }

    private
    static void createAndPersistJobResult(Job job, Date date, String nameOrId, Location location, Integer httpStatusCode) {
        JobResult.build(
                job: job,
                date: date,
                testId: nameOrId,
                jobConfigLabel: job.label,
                jobGroupName: job.jobGroup.name,
                locationLocation: location.location,
                locationBrowser: location.browser.name,
                httpStatusCode: httpStatusCode,
        ).save(failOnError: true)
    }

    private Job createJob(boolean active, String executionSchedule = null) {
        Job wptJobToSchedule = Job.build(
                label: UNNAMED_JOB_LABEL + ' ' + UUID.randomUUID() as String,
                description: '',
                executionSchedule: executionSchedule,
                runs: 1,
                active: active,
                script: script,
                location: location,
                jobGroup: jobGroup,
                maxDownloadTimeInMinutes: 60,
                connectivityProfile: connectivityProfile
        ).save(failOnError: true)

        return wptJobToSchedule
    }

    private Job createAndScheduleJob(String executionSchedule) {
        Job wptJobToSchedule = createJob(true, executionSchedule)
        jobProcessingService.scheduleJob(wptJobToSchedule)
        return wptJobToSchedule
    }

    private String getCronExpressionByTriggerKey(TriggerKey triggerKey) {
        Trigger insertedTrigger = jobProcessingService.quartzScheduler.getTrigger(triggerKey)
        CronTriggerImpl cronScheduleBuilder = insertedTrigger.getScheduleBuilder().build()
        return cronScheduleBuilder.cronExpression
    }

    private TriggerKey getTriggerKeyOf(Job job) {
        return new TriggerKey(job.id.toString(), TriggerGroup.JOB_TRIGGER_LAUNCH.value())
    }
}
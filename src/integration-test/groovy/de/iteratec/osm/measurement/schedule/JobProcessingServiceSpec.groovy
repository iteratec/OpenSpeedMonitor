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
import de.iteratec.osm.csi.TestDataUtil
import de.iteratec.osm.measurement.environment.Browser
import de.iteratec.osm.measurement.environment.Location
import de.iteratec.osm.measurement.environment.QueueAndJobStatusService
import de.iteratec.osm.measurement.environment.WebPageTestServer
import de.iteratec.osm.measurement.environment.wptserverproxy.ProxyService
import de.iteratec.osm.measurement.script.Script
import de.iteratec.osm.result.JobResult
import grails.test.mixin.integration.Integration
import grails.transaction.NotTransactional
import grails.transaction.Rollback
import groovyx.net.http.HttpResponseDecorator
import org.apache.http.HttpVersion
import org.apache.http.message.BasicHttpResponse
import org.quartz.Trigger
import org.quartz.TriggerKey
import org.quartz.impl.triggers.CronTriggerImpl

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertNotNull

/**
 * Integration test for JobProcessingService
 *
 * @author dri
 */
@Integration
@Rollback
class JobProcessingServiceSpec extends NonTransactionalIntegrationSpec {
    JobProcessingService jobProcessingService
    QueueAndJobStatusService queueAndJobStatusService
    JobDaoService jobDaoService

    final static String UNNAMED_JOB_LABEL = 'Unnamed Job'
    /**
     * Cron strings designed for Quartz jobs to never be executed before integration test ends
     */
    private final static String CRON_STRING_1 = '* * */12 * * ?'
    private final static String CRON_STRING_2 = '* * */13 * * ?'
    private final static String EVERY_15_SECONDS = '*/15 * * * * ? *'

    Script script
    Location location
    JobGroup jobGroup

    def setup() {

        // mocks common for all tests
        jobProcessingService.proxyService = [runtest: { WebPageTestServer wptserver, Map params ->
            if (params.f == 'xml') {
                String xml = """\
                    <?xml version="1.0" encoding="UTF-8"?>
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
			"""
                BasicHttpResponse httpResponse = new BasicHttpResponse(HttpVersion.HTTP_1_1, 200, null)
                return new HttpResponseDecorator(httpResponse, [text: xml] as Object)
            } else {
                BasicHttpResponse httpResponse = new BasicHttpResponse(HttpVersion.HTTP_1_1, 302, null)
                HttpResponseDecorator d = new HttpResponseDecorator(httpResponse, null)
                d.addHeader('Location', HttpRequestServiceMock.redirectUserUrl)
                return d
            }
        }] as ProxyService
        jobProcessingService.proxyService.httpRequestService = new HttpRequestServiceMock()

        //test data common for all tests


        jobProcessingService.inMemoryConfigService = new InMemoryConfigService()
        jobProcessingService.inMemoryConfigService.activateMeasurementsGenerally()

        TestDataUtil.createOsmConfig()

        WebPageTestServer wptServer = new WebPageTestServer(
                label: 'Unnamed server',
                proxyIdentifier: 'proxy_identifier',
                dateCreated: new Date(),
                lastUpdated: new Date(),
                active: true,
                baseUrl: 'http://example.com').save(failOnError: true)
        Browser browser = new Browser(name: 'browser').save(failOnError: true)
        jobGroup = new JobGroup(
                name: 'Unnamed group',
                graphiteServers: []).save(failOnError: true)

        script = Script.createDefaultScript('Unnamed job').save(failOnError: true)
        location = new Location(
                label: 'Unnamed location',
                dateCreated: new Date(),
                active: true,
                valid: 1,
                wptServer: wptServer,
                location: 'location',
                browser: browser
        ).save(failOnError: true)
    }


    void "scheduleJob test"() {
        when:
        Job wptJobToSchedule = createAndScheduleJob(CRON_STRING_1)
        TriggerKey triggerKey = getTriggerKeyOf(wptJobToSchedule)
        // check if Job was scheduled with correct Trigger identifier and group
        Trigger insertedTrigger = jobProcessingService.quartzScheduler.getTrigger(triggerKey)

        then:
        insertedTrigger != null
        triggerKey == insertedTrigger.getKey()
        // check if schedule of inserted Trigger matches Cron expression of wptJobToSchedule
        wptJobToSchedule.executionSchedule == getCronExpressionByTriggerKey(triggerKey, wptJobToSchedule)
    }

    void "unscheduleJob test"() {
        given:
        Job wptJobToSchedule = createAndScheduleJob(CRON_STRING_1)

        when:
        jobProcessingService.unscheduleJob(wptJobToSchedule)

        then:
        jobProcessingService.quartzScheduler.getTrigger(getTriggerKeyOf(wptJobToSchedule)) == null
    }

    void "rescheduleJob test"() {
        given:
        Job wptJobToSchedule = createAndScheduleJob(CRON_STRING_1)
        wptJobToSchedule.executionSchedule = CRON_STRING_2

        when:
        // reschedules Job if already scheduled
        jobProcessingService.scheduleJob(wptJobToSchedule)

        then:
        // check if schedule matches updated Cron expression of wptJobToSchedule
        wptJobToSchedule.executionSchedule == getCronExpressionByTriggerKey(getTriggerKeyOf(wptJobToSchedule), wptJobToSchedule)

        cleanup: "Unschedule all jobs, to prevent failures in other tests"
        jobProcessingService.unscheduleJob(wptJobToSchedule)
    }

    void "scheduleAllJobs test"() {
        given:
        Job inactiveJob = createJob(false)
        Job activeJob1 = createJob(true, CRON_STRING_1)
        Job activeJob2 = createJob(true, CRON_STRING_2)

        when:
        jobProcessingService.scheduleAllActiveJobs();

        then:
        jobProcessingService.quartzScheduler.getTrigger(getTriggerKeyOf(inactiveJob)) == null
        jobProcessingService.quartzScheduler.getTrigger(getTriggerKeyOf(activeJob1)) != null
        jobProcessingService.quartzScheduler.getTrigger(getTriggerKeyOf(activeJob2)) != null

        activeJob1.executionSchedule == getCronExpressionByTriggerKey(getTriggerKeyOf(activeJob1), activeJob1)
        activeJob2.executionSchedule == getCronExpressionByTriggerKey(getTriggerKeyOf(activeJob2), activeJob2)

        cleanup: "Unschedule all jobs, to prevent failures in other tests"
        jobProcessingService.unscheduleJob(activeJob1)
        jobProcessingService.unscheduleJob(activeJob2)
    }

    /**
     * Tests the following methods of JobProcessingService:
     * 	getCurrentlyRunningJobs
     * 	pollJobRun
     * 	launchJobRun
     *
     * 	The @Transactional(propagation = Propagation.REQUIRES_NEW) annotation on persistUnfinishedJobResult requires that the method calls in this test are encapsulated in individual transactions.
     */
    @NotTransactional
    void "launchJobAndPoll test"() {
        def jobId
        Job job
        when:
        Job.withNewTransaction {
            jobId = createJob(true, EVERY_15_SECONDS).id
        }

        Job.withNewTransaction {
            job = jobDaoService.getJobById(jobId)
            //launchJobRun returns false, because it fails and catch the exception
            jobProcessingService.launchJobRun(job)
        }
        // manual first execution
        // cause quartz scheduling doesn't seem to work trustable in tests
        Job.withNewTransaction {
            job = jobDaoService.getJobById(jobId)
        jobProcessingService.pollJobRun(job, HttpRequestServiceMock.testId)
        }
        TriggerKey subtriggerKey

        Job.withNewTransaction {
            // ensure launchJobRun created a Quartz trigger (called subtrigger) to repeatedly execute JobProcessingService.pollJubRun()
             subtriggerKey = new TriggerKey(jobProcessingService.getSubtriggerId(job, HttpRequestServiceMock.testId), TriggerGroup.JOB_TRIGGER_POLL.value())
        }
        Trigger subtrigger
        // no Job in  JobStore, because no Triger was created --> returns null
        Job.withNewTransaction {
            subtrigger = jobProcessingService.quartzScheduler.getTrigger(subtriggerKey)
        }
        then:
        subtriggerKey != null
        subtrigger == null

        for (int i = 0; i < HttpRequestServiceMock.statusCodes.length; i++) {
            // assert that an unfinished JobResult with correct status code has been persisted
            if (HttpRequestServiceMock.statusCodes[i] < 200) {
                JobResult unfinishedResult
                Job.withNewTransaction {
                    unfinishedResult = JobResult.findByJobConfigLabelAndTestId(job.label, HttpRequestServiceMock.testId)
                }
                assertNotNull(unfinishedResult)
                assertEquals(HttpRequestServiceMock.statusCodes[i], unfinishedResult.httpStatusCode)
            }
            Job.withNewTransaction {
                job = jobDaoService.getJobById(jobId)
                jobProcessingService.pollJobRun(job, HttpRequestServiceMock.testId)
            }
        }

        // ensure that upon successful completion of the test (statusCode 200) pollJobRun() removed the subtrigger
        jobProcessingService.quartzScheduler.getTrigger(subtriggerKey) == null
    }

    void "launchJobInteractive test"() {
        when:
        Job job = createJob(false)
        String redirectUrl = jobProcessingService.launchJobRunInteractive(job)

        then:
        redirectUrl != null
        HttpRequestServiceMock.redirectUserUrl == redirectUrl
    }

    /**
     * This test creates several JobResults with different status codes and checks whether
     * JobProcessingService.getRunningAndRecentlyFinishedJobs() filters these results correctly.
     * Only results that are no errors or the most recent one should be retained.
     */

    void "statusOfRepeatedJobExecution test"() {
        given:
        Job.withNewTransaction {
            createJob(false)
        }
        Job job = jobDaoService.getJobById(1)
        Date now = new Date()
        Date oldestDate = now - 5

        when:
        inputStatusCodes.reverse().eachWithIndex { int httpStatusCode, int i ->
            JobResult result = jobProcessingService.persistUnfinishedJobResult(job.id, null, httpStatusCode)
            result.date = now - i
            result.save(flush: true, failOnError: true)
        }
        // test execution
        List recentRuns = queueAndJobStatusService.getRunningAndRecentlyFinishedJobs(oldestDate, oldestDate, oldestDate)[job.id]

        then:
        inputStatusCodes.size() == JobResult.count()
        expectedStatusCodes.size() == recentRuns.size()
        expectedStatusCodes.eachWithIndex { int statusCode, int i ->
            statusCode == recentRuns[i]['status']
        }

        where:
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


    private Job createJob(boolean active, String executionSchedule = null) {
        Job wptJobToSchedule = new Job(
                label: UNNAMED_JOB_LABEL + ' ' + UUID.randomUUID() as String,
                description: '',
                executionSchedule: executionSchedule,
                runs: 1,
                active: active,
                script: script,
                location: location,
                jobGroup: jobGroup,
                maxDownloadTimeInMinutes: 60,
                connectivityProfile: TestDataUtil.createConnectivityProfile("unused")
        ).save(failOnError: true)

        return wptJobToSchedule
    }

    private Job createAndScheduleJob(String executionSchedule) {
        Job wptJobToSchedule = createJob(true, executionSchedule)
        jobProcessingService.scheduleJob(wptJobToSchedule)
        return wptJobToSchedule
    }

    private String getCronExpressionByTriggerKey(TriggerKey triggerKey, Job wptJobToSchedule) {
        Trigger insertedTrigger = jobProcessingService.quartzScheduler.getTrigger(triggerKey)
        CronTriggerImpl cronScheduleBuilder = insertedTrigger.getScheduleBuilder().build()
        return cronScheduleBuilder.cronExpression
    }

    private TriggerKey getTriggerKeyOf(Job job) {
        return new TriggerKey(job.id.toString(), TriggerGroup.JOB_TRIGGER_LAUNCH.value())
    }
}
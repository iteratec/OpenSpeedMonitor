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
import de.iteratec.osm.measurement.environment.WebPageTestServer
import de.iteratec.osm.measurement.script.Script
import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import org.quartz.Trigger
import org.quartz.TriggerKey
import org.quartz.impl.triggers.CronTriggerImpl
/**
 * Integration test for JobSchedulingService
 *
 * @author dri
 */
@Integration(applicationClass = openspeedmonitor.Application.class)
@Rollback
class JobSchedulingServiceIntegrationSpec extends NonTransactionalIntegrationSpec {
    JobSchedulingService jobSchedulingService

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
        //test data common for all tests
        jobSchedulingService.inMemoryConfigService = new InMemoryConfigService()
        jobSchedulingService.inMemoryConfigService.activateMeasurementsGenerally()

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

    def cleanup() {
        jobSchedulingService.inMemoryConfigService = grailsApplication.mainContext.getBean('inMemoryConfigService')
    }


    void "scheduleJob test"() {
        when: "getting trigger from quartzscheduler and triggerKey from scheduled job"
        Job wptJobToSchedule = createAndScheduleJob(CRON_STRING_1)
        TriggerKey triggerKey = getTriggerKeyOf(wptJobToSchedule)
        // check if Job was scheduled with correct Trigger identifier and group
        Trigger insertedTrigger = jobSchedulingService.quartzScheduler.getTrigger(triggerKey)

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
        jobSchedulingService.unscheduleJob(wptJobToSchedule)

        then: "no trigger for the job is set"
        jobSchedulingService.quartzScheduler.getTrigger(getTriggerKeyOf(wptJobToSchedule)) == null
    }

    void "rescheduleJob test"() {
        given: "a scheduled job"
        Job wptJobToSchedule = createAndScheduleJob(CRON_STRING_1)
        wptJobToSchedule.executionSchedule = CRON_STRING_2

        when: "scheduling or rescheduling a job"
        jobSchedulingService.scheduleJob(wptJobToSchedule)

        then: "check if schedule matches updated Cron expression of wptJobToSchedule"
        wptJobToSchedule.executionSchedule == getCronExpressionByTriggerKey(getTriggerKeyOf(wptJobToSchedule))

        cleanup: "Unschedule all jobs, to prevent failures in other tests"
        jobSchedulingService.unscheduleJob(wptJobToSchedule)
    }

    void "scheduleAllJobs test"() {
        given: "a set of active and inactive jobs"
        Job inactiveJob = createJob(false)
        Job activeJob1 = createJob(true, CRON_STRING_1)
        Job activeJob2 = createJob(true, CRON_STRING_2)

        when: "launching all active jobs"
        jobSchedulingService.scheduleAllActiveJobs()

        then: "check if the triggers in the scheduler are correctly set"
        jobSchedulingService.quartzScheduler.getTrigger(getTriggerKeyOf(inactiveJob)) == null
        jobSchedulingService.quartzScheduler.getTrigger(getTriggerKeyOf(activeJob1)) != null
        jobSchedulingService.quartzScheduler.getTrigger(getTriggerKeyOf(activeJob2)) != null

        activeJob1.executionSchedule == getCronExpressionByTriggerKey(getTriggerKeyOf(activeJob1))
        activeJob2.executionSchedule == getCronExpressionByTriggerKey(getTriggerKeyOf(activeJob2))

        cleanup: "Unschedule all jobs, to prevent failures in other tests"
        jobSchedulingService.unscheduleJob(activeJob1)
        jobSchedulingService.unscheduleJob(activeJob2)
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
        jobSchedulingService.scheduleJob(wptJobToSchedule)
        return wptJobToSchedule
    }

    private String getCronExpressionByTriggerKey(TriggerKey triggerKey) {
        Trigger insertedTrigger = jobSchedulingService.quartzScheduler.getTrigger(triggerKey)
        CronTriggerImpl cronScheduleBuilder = insertedTrigger.getScheduleBuilder().build()
        return cronScheduleBuilder.cronExpression
    }

    private TriggerKey getTriggerKeyOf(Job job) {
        return new TriggerKey(job.id.toString(), TriggerGroup.JOB_TRIGGER_LAUNCH.value())
    }
}

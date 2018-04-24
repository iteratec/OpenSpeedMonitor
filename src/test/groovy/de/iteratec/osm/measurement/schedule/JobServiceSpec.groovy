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

import de.iteratec.osm.csi.CsiConfiguration
import de.iteratec.osm.csi.CsiDay
import de.iteratec.osm.measurement.environment.Browser
import de.iteratec.osm.measurement.environment.BrowserAlias
import de.iteratec.osm.measurement.environment.Location
import de.iteratec.osm.measurement.environment.WebPageTestServer
import de.iteratec.osm.measurement.script.Script
import de.iteratec.osm.result.Measurand
import grails.buildtestdata.BuildDataTest
import grails.buildtestdata.mixin.Build
import grails.testing.services.ServiceUnitTest
import grails.validation.ValidationException
import spock.lang.Specification

@Build([Job, JobGroup, CsiConfiguration])
class JobServiceSpec extends Specification implements BuildDataTest, ServiceUnitTest<JobService> {

    JobService serviceUnderTest

    Job jobWithCsiJobGroup, jobWithNonCsiJobGroup, jobWithValidExecSchedule
    JobGroup csiGroup, nonCsiGroup

    void setup() {
        serviceUnderTest = service
        service.jobDaoService = new JobDaoService()
        createTestDataCommonForAllTests()
    }

    void setupSpec() {
        mockDomains(Job, Location, WebPageTestServer, Browser, BrowserAlias, JobGroup, Script, CsiConfiguration, CsiDay)
    }

    void "get csi JobGroup of Job associated to a non csi JobGroup"() {
        when: "getCsiJobGroupOf method is called for a Job with a non csi JobGroup"
        JobGroup assignedJobGroup = serviceUnderTest.getCsiJobGroupOf(jobWithNonCsiJobGroup)

        then: "null is returned"
        assignedJobGroup == null
    }

    void "get csi JobGroup of Job associated to a csi JobGroup"() {
        when:
        JobGroup jobGroup = serviceUnderTest.getCsiJobGroupOf(jobWithCsiJobGroup)

        then:
        jobGroup == csiGroup
    }

    void "get all csi Jobs"() {
        when: "getAllCsiJobs method is called"
        List<Job> csiJobs = serviceUnderTest.getAllCsiJobs()

        then: "it returns all Jobs associated to a csi JobGroup"
        csiJobs.size() == 1
        csiJobs[0] == jobWithCsiJobGroup
    }


    void "UpdateActivityFromFalseToTrue"() {
        given: "an inactive Job"
        jobWithValidExecSchedule.active = false
        jobWithValidExecSchedule.save(failOnError: true)

        when: "activate it"
        serviceUnderTest.updateActivity(jobWithValidExecSchedule, true)

        then: "it's active afterwards"
        jobWithValidExecSchedule.refresh().active == true
    }

    void "UpdateActivityFromFalseToFalse"() {
        given: "an inactive Job"
        jobWithValidExecSchedule.active = false
        jobWithValidExecSchedule.save(failOnError: true)
        when: "deactivate it"
        serviceUnderTest.updateActivity(jobWithValidExecSchedule, false)
        then: "no exception is thrown and the Job is still deactivated afterwards"
        jobWithValidExecSchedule.refresh().active == false
    }


    void "UpdateActivityFromTrueToFalse"() {
        given: "an active Job"
        jobWithValidExecSchedule.active = true
        jobWithValidExecSchedule.save(failOnError: true)
        when: "deactivate it"
        serviceUnderTest.updateActivity(jobWithValidExecSchedule, false)
        then: "it's deactivated afterwards"
        jobWithValidExecSchedule.refresh().active == false
    }

    void "UpdateActivityFromTrueToTrue"() {
        given: "an active Job"
        jobWithValidExecSchedule.active = false
        jobWithValidExecSchedule.save(failOnError: true)
        when: "activate it"
        serviceUnderTest.updateActivity(jobWithValidExecSchedule, true)
        then: "no exception is thrown and the Job is still active afterwards"
        jobWithValidExecSchedule.refresh().active == true
    }


    void "UpdatingExecutionScheduleWithValidSchedule"() {
        when: "updating the execution schedule of a Job with a valid quartz schedule"
        String validSchedule = '0 0 */15 * * ? *'
        serviceUnderTest.updateExecutionSchedule(jobWithValidExecSchedule, validSchedule)
        then: "the schedule is updated as expected"
        jobWithValidExecSchedule.refresh().executionSchedule == validSchedule
    }

    void "UpdatingExecutionScheduleWithInvalidSchedule"() {
        given: "a Job with a valid execution schedule"
        String invalidSchedule = '0 0 */15 * * * *'
        when: "updating the execution schedule with an invalid schedule"
        serviceUnderTest.updateExecutionSchedule(jobWithValidExecSchedule, invalidSchedule)
        then: "a ValidationException is thrown and the Job has it's previous execution schedule"
        thrown ValidationException
    }

    void "test deleteJob"() {
        given: "a Job not marked as deleted previously"
        Job jobToDelete = Job.build(deleted: false)

        when: "this job gets deleted (by a user)"
        service.deleteJob(jobToDelete)

        then: "the job isn't deleted in database but is marked as deleted"
        jobToDelete.refresh().deleted
        jobToDelete.refresh().label.contains("_deleted_")
    }

    void "test createTimeSeriesDataForJob gets a valid measurand"(){
        given: "a job"
        Job job = Job.build()

        when:
        Map result = service.createTimeSeriesParamsFor(job)

        then:
        Measurand.valueOf(result."selectedAggrGroupValuesUnCached")

    }

    private void createTestDataCommonForAllTests() {
        csiGroup = JobGroup.build(
            csiConfiguration: CsiConfiguration.build()
        )
        nonCsiGroup = JobGroup.build()
        jobWithCsiJobGroup = Job.build(jobGroup: csiGroup)
        jobWithNonCsiJobGroup = Job.build(jobGroup: nonCsiGroup)
        jobWithValidExecSchedule = Job.build(executionSchedule: "0 0 * * * ? *")
    }
}

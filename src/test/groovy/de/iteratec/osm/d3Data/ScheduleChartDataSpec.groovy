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

package de.iteratec.osm.d3Data

import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import org.joda.time.DateTime
import spock.lang.Specification

@TestMixin(GrailsUnitTestMixin)
class ScheduleChartDataSpec extends Specification{

    def "initialisation test" () {
        when: "ScheduleChartData is created"
        ScheduleChartData scheduleChartData = new ScheduleChartData()

        then: "the ScheduleChartData has been initialised correctly"
        scheduleChartData.name == ScheduleChartData.DEFAULT_NAME
        scheduleChartData.discountedJobsLabel == ScheduleChartData.DEFAULT_DISCOUNTED_JOBS_LABEL
        scheduleChartData.jobs.size() == 0
        scheduleChartData.discountedJobs.size() == 0
        scheduleChartData.agentCount == 0
        scheduleChartData.allExecutionDates.size() == 0
        scheduleChartData.allEndDates.size() == 0
    }

    def "addJob adds schedule chart job to list" () {
        given: "some jobs with dates"
        ScheduleChartData scheduleChartData = new ScheduleChartData()
        List executionDates = new ArrayList<>()
        DateTime date1 = new DateTime()
        DateTime date2 = date1.plusDays(1)
        DateTime date3 = date1.plusDays(5)
        executionDates.add(date1)
        executionDates.add(date2)
        executionDates.add(date3)

        ScheduleChartJob job = new ScheduleChartJob(executionDates: executionDates, durationInSeconds: 60)
        ScheduleChartJob job2 = new ScheduleChartJob(executionDates: executionDates, durationInSeconds: 120)

        when: "the jobs are added to ScheduleChartData"
        scheduleChartData.addJob(job)
        scheduleChartData.addJob(job2)

        then: "the ScheduleChartData contains the correct jobs and dates"
        scheduleChartData.jobs.size() == 2
        scheduleChartData.jobs[0] == job
        scheduleChartData.allExecutionDates.size() == job.executionDates.size() + job2.executionDates.size()
        scheduleChartData.allEndDates.size() == job.executionDates.size() + job2.executionDates.size()
        // lists should be sorted
        scheduleChartData.allExecutionDates[0] == date1
        scheduleChartData.allExecutionDates[1] == date1
        scheduleChartData.allExecutionDates[2] == date2
        scheduleChartData.allEndDates[0] == date1.plusSeconds(job.durationInSeconds)
        scheduleChartData.allEndDates[1] == date1.plusSeconds(job2.durationInSeconds)
        scheduleChartData.allEndDates[2] == date2.plusSeconds(job.durationInSeconds)
    }

    def "addDiscountedJob adds job to list" () {
        given: "a job"
        ScheduleChartData scheduleChartData = new ScheduleChartData()
        String job = "Job"

        when: "the job is added as DiscountedJob"
        scheduleChartData.addDiscountedJob(job)

        then: "ScheduleChartData contains the job"
        scheduleChartData.discountedJobs.size() == 1
        scheduleChartData.discountedJobs[0] == job
    }
}

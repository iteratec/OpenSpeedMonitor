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
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
@TestMixin(GrailsUnitTestMixin)
class ScheduleChartDataSpec extends Specification{

    def "initialisation test" () {
        when:
        ScheduleChartData scheduleChartData = new ScheduleChartData()

        then:
        !scheduleChartData.name.isEmpty()
        !scheduleChartData.discountedJobsLabel.isEmpty()
        scheduleChartData.jobs.size() == 0
        scheduleChartData.discountedJobs.size() == 0
        scheduleChartData.agentCount == 0;
    }

    def "addJob adds schedule chart job to list" () {
        given:
        ScheduleChartData scheduleChartData = new ScheduleChartData()
        ScheduleChartJob job = new ScheduleChartJob()

        when:
        scheduleChartData.addJob(job)

        then:
        scheduleChartData.jobs.size() == 1
        scheduleChartData.jobs[0] == job
    }

    def "addDiscountedJob adds job to list" () {
        given:
        ScheduleChartData scheduleChartData = new ScheduleChartData()
        String job = "Job"

        when:
        scheduleChartData.addDiscountedJob(job)

        then:
        scheduleChartData.discountedJobs.size() == 1
        scheduleChartData.discountedJobs[0] == job
    }
}

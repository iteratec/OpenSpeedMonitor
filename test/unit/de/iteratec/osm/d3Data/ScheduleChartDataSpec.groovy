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

    def "schedule chart data initialisation test" () {
        when:
        ScheduleChartData scheduleChartData = new ScheduleChartData()

        then:
        !scheduleChartData.name.isEmpty()
        !scheduleChartData.discountedJobsLabel.isEmpty()
        !scheduleChartData.discountedLocationsLabel.isEmpty()
        scheduleChartData.locations.size() == 0
        scheduleChartData.discountedJobs.size() == 0
        scheduleChartData.discountedLocations.size() == 0
        scheduleChartData.startDate.getMillis() != 0
        scheduleChartData.endDate.getMillis() != 0
    }

    def "addLocation adds a location to list"() {
        given:
        ScheduleChartData scheduleChartData = new ScheduleChartData()
        ScheduleChartLocation location = new ScheduleChartLocation()

        when:
        scheduleChartData.addLocation(location)

        then:
        scheduleChartData.locations.size() == 1
        scheduleChartData.locations[0] == location
    }

    def "addDiscountedJob adds String to List of discounted job"() {
        given:
        ScheduleChartData scheduleChartData = new ScheduleChartData()
        String discription = "discountedJob"

        when:
        scheduleChartData.addDiscountedJob(discription)

        then:
        scheduleChartData.discountedJobs.size() == 1
        scheduleChartData.discountedJobs[0] == discription
        scheduleChartData.discountedLocations.size() == 0
    }

    def "addDiscountedLocation adds String to List of discounted locations"() {
        given:
        ScheduleChartData scheduleChartData = new ScheduleChartData()
        String discription = "discountedLocation"

        when:
        scheduleChartData.addDiscountedLocation(discription)

        then:
        scheduleChartData.discountedLocations.size() == 1
        scheduleChartData.discountedLocations[0] == discription
        scheduleChartData.discountedJobs.size() == 0
    }
}

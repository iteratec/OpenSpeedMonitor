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

package de.iteratec.osm.measurement.environment

import de.iteratec.osm.d3Data.ScheduleChartData
import de.iteratec.osm.d3Data.ScheduleChartJob
import de.iteratec.osm.d3Data.ScheduleChartLocation
import de.iteratec.osm.measurement.schedule.Job
import de.iteratec.osm.measurement.schedule.JobService
import de.iteratec.osm.measurement.script.ScriptParser
import de.iteratec.osm.result.PageService
import de.iteratec.osm.util.I18nService
import grails.converters.JSON
import org.joda.time.DateTime
import org.springframework.context.MessageSource

/**
 * JobScheduleController
 */
class JobScheduleController {
//
    QueueAndJobStatusService queueAndJobStatusService
//
//    JobService jobService
//    PageService pageService
//
//    I18nService i18nService

    def index() {
        redirect(action: 'schedules')
    }

    def schedules() {
        // Interval which is observed (1 day from now)
        DateTime start = new DateTime();
        DateTime end = start.plusDays(1);

        // Create data for chart in interval
        List<ScheduleChartData> chartDataList = queueAndJobStatusService.createChartData(start, end)

        [chartList: chartDataList]
    }

//    /**
//     *
//     * @param start starting point of specified interval
//     * @param end ending point of specified interval
//     * @return List of JSON objects for schedule chart
//     */
//    private def createChartData(DateTime start, DateTime end) {
//
//        def chartDataList = new ArrayList();
//        def wptServer = WebPageTestServer.findAllByActive(true)
//
//        String discountedLocationsLabel = i18nService.msg("de.iteratec.osm.d3Data.ScheduleChart.discardedLocationsLabel", "Discarded Locations")
//        String discountedJobsLabel = i18nService.msg("de.iteratec.osm.d3Data.ScheduleChart.discardedJobsLabel", "Discarded Jobs")
//
//
//        // Iterate over active servers
//        for (WebPageTestServer server : wptServer) {
//            ScheduleChartData scheduleChartServer = new ScheduleChartData(name: server.label,
//                                                                        startDate: start, endDate: end,
//                                                                        discountedLocationsLabel: discountedLocationsLabel,
//                                                                        discountedJobsLabel: discountedJobsLabel)
//
//            def locations = queueAndJobStatusService.getFilteredLocations(server)
//
//            // iterate over locations
//            locations.each { loc ->
//                ScheduleChartLocation scheduleChartLocation = new ScheduleChartLocation(name: loc.location.uniqueIdentifierForServer)
//                def jobs = Job.findAllByLocation(loc.location)
//
//                // iterate over jobs
//                for (Job j : jobs) {
//                    ScriptParser parser = new ScriptParser(pageService, j.script.navigationScript);
//                    def minutes = parser.calculateDurationInMinutes()
//                    // Add jobs which are going to run in given interval to the list
//                    // otherwise the job is added to the list of discounted jobs
//                    ScheduleChartJob scheduleChartJob = new ScheduleChartJob(executionDates: jobService.getExecutionDatesInInterval(j, start, end), name: j.label, durationInMinutes: minutes)
//                    if (!scheduleChartJob.executionDates.isEmpty()) {
//                        scheduleChartLocation.addJob(scheduleChartJob)
//                    } else {
//                        scheduleChartServer.addDiscountedJob(loc.location.uniqueIdentifierForServer + ": " + j.label)
//                    }
//                }
//
//                // if a location has no job which is going to run in the interval
//                // the whole location is added to the list of discarded locations
//                if (!scheduleChartLocation.jobs.isEmpty()) {
//                    scheduleChartServer.addLocation(scheduleChartLocation)
//                } else {
//                    scheduleChartServer.addDiscountedLocation(loc.location.uniqueIdentifierForServer)
//                }
//            }
//
//            chartDataList.add(scheduleChartServer as JSON)
//        }
//
//        return chartDataList
//    }
}

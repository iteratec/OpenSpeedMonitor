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
import org.joda.time.DateTime

/**
 * JobScheduleController
 */
class JobScheduleController {

    QueueAndJobStatusService queueAndJobStatusService

    def index() {
        redirect(action: 'schedules')
    }

    def schedules() {
        // Interval which is observed (1 csiDay from now)
        DateTime start = new DateTime();
        DateTime end = start.plusDays(1);

        // Create data for chart in interval
        Map<WebPageTestServer, List<ScheduleChartData>> serverData = queueAndJobStatusService.createChartData(start, end)

        [chartMap : serverData,
         startDate: start,
         endDate  : end]
    }
}

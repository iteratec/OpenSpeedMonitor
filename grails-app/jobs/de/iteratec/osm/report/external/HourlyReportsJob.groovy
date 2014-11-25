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

package de.iteratec.osm.report.external

import org.joda.time.DateTime

import de.iteratec.osm.report.external.MetricReportingService


/**
 * Triggers hourly csi-{@link MeasuredValue}-reporting.
 * @author nkuhn
 * @author mze
 * @since IT-199
 *
 */
class HourlyReportsJob {
	
	MetricReportingService metricReportingService
	
	static triggers = {
		/** Twenty minutes after each full hour. */
		cron(name: 'HourlyCsiReports', cronExpression: '0 20 * ? * *')
		//for testing purposes:
//		cron(name: 'WeeklyCsiReports', cronExpression: '0 */3 * ? * *')
	}

    def execute() {
		DateTime now = new DateTime();
		metricReportingService.reportEventCSIValuesOfLastHour(now)
    }
}

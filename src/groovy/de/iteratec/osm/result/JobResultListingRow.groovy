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

package de.iteratec.osm.result

import de.iteratec.osm.result.EventResult
import de.iteratec.osm.result.JobResult

/**
 * @deprecated Use {@link EventResultListingRow}.
 */
@Deprecated
class JobResultListingRow {
	
	/**
	 * <p>
	 * Creates a new row.
	 * </p>
	 * 
	 * @param jobResult The Job
	 * @param eventResult
	 */
	public JobResultListingRow(JobResult jobResult, EventResult eventResult)
	{
		label = jobResult.jobConfigLabel;
		measuringDate = jobResult.date;
		testsDetailsURL = jobResult.tryToGetTestsDetailsURL();
	}
	
	String label;

	Date measuringDate;

	URL testsDetailsURL;

	// ADD
	// 5.2: The CSI group / Shop name
	// 5.1: The page name
	// 5.3: The MeasuredEvent name
	// 5.4: The Browser name
	// 5.5: The location name
}

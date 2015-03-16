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

package de.iteratec.osm.batch

/**
 * BatchActivity
 * Representation of a batch activity like job deletion
 */
class BatchActivity {

    String domain = ""
    long idWithinDomain
    String name
    Activity activity
    Date startDate = new Date()
    Date endDate
    Date lastUpdated
    Status status
    String stage
    String progressWithinStage
    String progress
    Integer failures = 0
    Integer successfulActions = 0
    String lastFailureMessage = ""

    static mapping = {
    }

    static constraints = {
        domain()
        idWithinDomain()
        name()
        activity()
        startDate()
        status()
        progress()
        successfulActions()
        stage()
        progressWithinStage()
        failures()
        lastFailureMessage(nullable: true)
        endDate(nullable: true)
    }

	@Override
	public String toString() {
		return "Domain: $domain Progress: $progress";
	}
}

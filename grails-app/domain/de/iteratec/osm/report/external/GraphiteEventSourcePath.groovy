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

import de.iteratec.osm.measurement.schedule.JobGroup

/**
 * GraphiteEventSourcePath
 * A domain class describes the data object and it's mapping to the database
 */
class GraphiteEventSourcePath {

    String path;

	static hasMany = [jobGroups:JobGroup]	// tells GORM to associate other domain objects for a 1-n or n-m mapping

    static mapping = {
    }

    static constraints = {
    }

    /*
     * Methods of the Domain Class
     */
	@Override	// Override toString for a nicer / more descriptive UI
	public String toString() {
		return "${path}";
	}
}

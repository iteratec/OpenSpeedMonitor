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
import grails.gorm.annotation.Entity

/**
 * GraphiteEventSourcePath
 * Defines a graphite source to fetch {@link Event}s from.
 */
@Entity
class GraphiteEventSourcePath {

    /**
     * Serves as prefix in osm dashboard representation of fetched {@link Event}s.
     */
    String staticPrefix
    /**
     * Graphite metric name. Used to request events from graphite's webapp.
     */
    String targetMetricName

	static hasMany = [jobGroups:JobGroup]
    static belongsTo = [GraphiteServer]

    static mapping = {
    }

    static constraints = {
        staticPrefix(nullable: true)
        targetMetricName(maxSize: 255)
    }

	@Override
	public String toString() {
		return "staticPrefix=${staticPrefix}|targetMetricName=${targetMetricName}"
	}
}

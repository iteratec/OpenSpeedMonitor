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

package de.iteratec.osm.report.chart

import de.iteratec.osm.measurement.schedule.JobGroup

/**
 * Represents an arbitrary event to be shown in dashboard diagramms as verticals with info text as hover info.
 * Could be deployments of measured web application for example.
 */
class Event {

    Date eventDate = new Date()
    String shortName
    String htmlDescription
    Boolean globallyVisible = false

    /**
     * The {@link JobGroup} this event is assigned to
     */
    Collection<JobGroup> jobGroup
    static hasMany = [jobGroup: JobGroup]

    static mapping = {
        eventDate()
        globallyVisible(defaultValue: false)
    }

    def beforeValidate() {
        shortName = shortName.replaceAll(/<!--.*?-->/, '').replaceAll(/<.*?>/, '')
        htmlDescription = htmlDescription.replaceAll(/<!--.*?-->/, '').replaceAll(/<.*?>/, '')
    }

	static constraints = {
        eventDate()
        shortName(maxSize: 255)
        htmlDescription(maxSize: 255, nullable: true)
        globallyVisible()
    }
}

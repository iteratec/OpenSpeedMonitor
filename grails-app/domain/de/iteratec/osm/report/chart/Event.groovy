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
 * Represents an arbitrary event to be shown in dashboard diagrams as vertical lines with info text as hover info.
 * Could be deployments of measured web application for example.
 */
class Event {

    Date eventDate
    String shortName
    String description
    /**
     * If true this event is shown in all diagrams in dashboards of osm.
     * If false this event is just shown in diagrams which contain data of associated {@link JobGroup}s.
     */
    Boolean globallyVisible = false

    /**
     * The {@link JobGroup} this event is assigned to
     */
    Collection<JobGroup> jobGroups
    static hasMany = [jobGroups: JobGroup]

    static mapping = {
        globallyVisible(defaultValue: false)
    }

    def beforeValidate() {
        //remove all html tags, because we don't want arbitrary links to get injected into the gui of osm
        if(shortName != null) shortName = shortName.replaceAll(/<!--.*?-->/, '').replaceAll(/<.*?>/, '')
        if(description != null) description = description?.replaceAll(/<!--.*?-->/, '')?.replaceAll(/<.*?>/, '')
    }

	static constraints = {
        eventDate()
        shortName(maxSize: 255)
        description(maxSize: 255, nullable: true)
        globallyVisible()
    }
}

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

import org.joda.time.DateTime

/**
 * Provides methods for persistence of {@link Event}s.
 */
class EventDaoService {

    static transactional = true

    /**
     * Creates an {@link Event} with given attributes, associates given jobGroups to it and persists it.
     * @param shortName
     * @param eventTimestamp
     * @param description
     * @param globallyVisible
     * @param jobGroups
     * @return The persisted {@link Event}.
     */
    public Event createEvent(
            String shortName,
            DateTime eventTimestamp,
            String description,
            Boolean globallyVisible,
            Collection<JobGroup> jobGroups) {

        Event event = new Event(
            shortName: shortName,
            eventDate: eventTimestamp != null ? eventTimestamp.toDate() : eventTimestamp,
            description: description,
            globallyVisible: globallyVisible
        )
        jobGroups.each {group->
            event.addToJobGroups(group)
        }
        return event.save(failOnError: true)

    }
}

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

package de.iteratec.osm.report.ui

import de.iteratec.osm.result.EventResult
import de.iteratec.osm.result.JobResult
import grails.buildtestdata.mixin.Build
import grails.test.mixin.Mock
import spock.lang.Specification

@Build([EventResult, JobResult])
@Mock([EventResult, JobResult])
class EventResultListingSpec extends Specification {

    void "add rows to event result listing"(int count) {
        setup:
        EventResult eventResult = EventResult.build()
        JobResult jobResult = JobResult.build()
        EventResultListingRow row = new EventResultListingRow(jobResult, eventResult)
        EventResultListing out = new EventResultListing();

        when: "x amount of rows are added"
        count.times { out.addRow(row) }

        then: "they are all available"
        out.getRows().isEmpty() == false
        out.getRows().size() == count

        where:
        count | _
        1     | _
        2     | _
        3     | _
        4     | _
    }
}
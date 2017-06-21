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

import spock.lang.Specification


class PaginationListingSpec extends Specification {

    void "test add row"(def count) {
        setup:
        PaginationListing out = new PaginationListing()
        PaginationListingRow row = new PaginationListingRow(1, "www.example.de/params")

        when: "count amount of rows are added"
        count.times {
            out.addRow(row, 0, 10, 100, "www.example.de/paramsPreviousLink", "www.example.de/paramsNextLink")
        }

        then: "there are all available"
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

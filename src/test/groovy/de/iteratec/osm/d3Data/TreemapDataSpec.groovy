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

package de.iteratec.osm.d3Data

import org.grails.testing.GrailsUnitTest
import spock.lang.Specification

class TreemapDataSpec extends Specification implements GrailsUnitTest {

    def "initialisation test" () {
        when: "new TreemapData is created"
        TreemapData treemapData = new TreemapData()

        then: "the treemapData has been initialised"
        treemapData.children.size() == 0
        treemapData.zeroWeightLabel == TreemapData.DEFAULT_ZERO_WEIGHT_LABEL
        treemapData.dataName == TreemapData.DEFAULT_DATA_NAME
        treemapData.weightName == TreemapData.DEFAULT_WEIGHT_NAME
    }

    def "addNode adds chart entry to list"() {
        given: "an entry"
        TreemapData treemapData = new TreemapData()
        ChartEntry entry = new ChartEntry()

        when: "the entry is added to the TreemapData"
        treemapData.addNode(entry)

        then: "the TreemapData contains the entry"
        treemapData.children.size() == 1
        treemapData.children[0] == entry
    }
}

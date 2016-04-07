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

import spock.lang.Specification

import static org.junit.Assert.*

import grails.test.mixin.*
import grails.test.mixin.support.*
import org.junit.*

/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
@TestMixin(GrailsUnitTestMixin)
class TreemapDataSpec extends Specification {

    def "initialisation test" () {
        when:
        TreemapData treemapData = new TreemapData()

        then:
        treemapData.children.size() == 0
        !treemapData.zeroWeightLabel.isEmpty()
        !treemapData.dataName.isEmpty()
        !treemapData.weightName.isEmpty()
    }

    def "addNode adds chart entry to list"() {
        given:
        TreemapData treemapData = new TreemapData()
        ChartEntry entry = new ChartEntry()

        when:
        treemapData.addNode(entry)

        then:
        treemapData.children.size() == 1
        treemapData.children[0] == entry
    }
}

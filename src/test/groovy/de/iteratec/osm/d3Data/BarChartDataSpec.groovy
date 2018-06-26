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

class BarChartDataSpec extends Specification implements GrailsUnitTest {

    def "bar chart data object initialised with labels and an empty list"() {
        when: "new BarChartData is created"
        BarChartData barChartData = new BarChartData()

        then: "the BarChartData has been initialised correctly"
        barChartData.xLabel == BarChartData.DEFAULT_X_LABEL
        barChartData.yLabel == BarChartData.DEFAULT_Y_LABEL
        barChartData.bars.isEmpty()
    }

    def "addDatum adds a chart entry object to list of bars" () {
        given: "some entry"
        BarChartData barChartData = new BarChartData()
        ChartEntry chartEntry = new ChartEntry()

        when: "the entry is added to barChartData"
        barChartData.addDatum(chartEntry)

        then: "barChartData contains the correct entry"
        barChartData.bars.size() == 1
        barChartData.bars[0] == chartEntry
    }
}

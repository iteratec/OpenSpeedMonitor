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

import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
@TestMixin(GrailsUnitTestMixin)
class MultiLineChartSpec extends Specification{

    def "MultiLineChart initialisation test" () {
        when:
        MultiLineChart multiLineChart = new MultiLineChart()

        then:
        multiLineChart.lines.size() == 0
        !multiLineChart.xLabel.isEmpty()
        !multiLineChart.yLabel.isEmpty()
    }

    def "addLine adds line to list of lines"() {
        given:
        MultiLineChart multiLineChart = new MultiLineChart()
        MultiLineChartLineData line = new MultiLineChartLineData()

        when:
        multiLineChart.addLine(line)

        then:
        multiLineChart.lines.size() == 1
        multiLineChart.lines[0] == line
    }
}

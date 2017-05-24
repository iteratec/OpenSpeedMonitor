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
class MultiLineChartLineDataSpec extends Specification{

   def "MultiLineChartLineData initialisation test"() {
       when: "new MultiLineChartLineData is created"
       MultiLineChartLineData lineData = new MultiLineChartLineData()

       then: "the MultiLineChartLineData has been initialised"
       lineData.xPoints.size() == 0
       lineData.yPoints.size() == 0
       !lineData.name.isEmpty()
   }

    def "addDataPoint adds dataPoint and keeps right order" () {
        given: "some data points"
        MultiLineChartLineData lineData = new MultiLineChartLineData()
        double x1 = 12.0
        double x2 = 24.5
        double y1 = 7.4
        double y2 = 1.0

        when: "the data points are added to MultiLineChartLineData"
        lineData.addDataPoint(x1, y1)
        lineData.addDataPoint(x2, y2)

        then: "the MultiLineChartLineData contains the correct coordinates"
        lineData.xPoints.size() == 2
        lineData.yPoints.size() == 2
        lineData.xPoints[0] == x1
        lineData.xPoints[1] == x2
        lineData.yPoints[0] == y1
        lineData.yPoints[1] == y2
    }
}

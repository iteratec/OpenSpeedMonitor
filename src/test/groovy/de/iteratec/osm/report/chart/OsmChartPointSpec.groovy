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

import spock.lang.Specification

/**
 * <p>
 * Test-suite of {@link OsmChartPoint}.
 * </p>
 *
 * @author mze
 * @since IT-78
 */
class OsmChartPointSpec extends Specification {

    private static String TEST_AGENT = 'myAgent - 192.168.1.1'

    private static long TIME = 1378976834000L

    private static double VALUE = 42d

    private static int COUNT_OF_AGGREGATED_RESULTS = 4

    private static URL URL = new URL("https://www.google.de/search?q=42")

    void "test EqualsAndHashCode"() {
        given:
        OsmChartPoint out = createOsmChartPoint()
        OsmChartPoint equalsToOut = createOsmChartPoint()
        OsmChartPoint differentValue = createOsmChartPoint {OsmChartPoint p -> p.csiAggregation=23d}
        OsmChartPoint differentCountOfAggregatedResults = createOsmChartPoint {OsmChartPoint p -> p.countOfAggregatedResults=3}
        OsmChartPoint differentTime = createOsmChartPoint {OsmChartPoint p -> p.time=1378976835000L}
        OsmChartPoint differentURL = createOsmChartPoint {OsmChartPoint p ->p.sourceURL= "https://www.example.com/42".toURL() }
        OsmChartPoint differentTestAgent = createOsmChartPoint {OsmChartPoint p -> p.testingAgent='myAgent - 192.168.1.2'}

        expect:
        out == equalsToOut
        out.hashCode() == equalsToOut.hashCode()
        out != differentTime
        out != differentValue
        out != differentCountOfAggregatedResults
        out != differentURL
        out != differentTestAgent
    }

    void "test hasAnSourceURL to be false if no url was specified"() {
        given: "a OsmChartPoint with a null sourceUrl"
        final OsmChartPoint out = createOsmChartPoint {OsmChartPoint p -> p.sourceURL=null}
        expect: "it shouldn't have an url"
        !out.hasAnSourceURL()
    }

    void "test hasAnSourceURL to be true if an url was specified"() {
        given: "a valid OsmChartPoint"
        final OsmChartPoint out = createOsmChartPoint()
        expect: "it should have a sourceurl"
        out.hasAnSourceURL()
    }

    void "Chartpoints with an invalid time should return false on isValid"() throws Throwable {
        given:"a OsmChartPoint with a negative time"
        OsmChartPoint out = createOsmChartPoint {OsmChartPoint p -> p.time = -1l}
        expect: "it should't be valid"
        !out.isValid()
    }

    /**
     * Helper Method to create always the same valid ChartPoint, but with a closure to change a value and still create a oneliner
     * @param c with OsmChartPoint als parameter
     * @return
     */
    OsmChartPoint createOsmChartPoint(Closure c = {}){
        OsmChartPoint point = new OsmChartPoint(time: TIME, csiAggregation: VALUE, countOfAggregatedResults: COUNT_OF_AGGREGATED_RESULTS, sourceURL: URL, testingAgent: TEST_AGENT)
        c(point)
        return point
    }
}

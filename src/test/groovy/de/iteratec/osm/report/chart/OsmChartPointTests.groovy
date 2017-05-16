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
class OsmChartPointTests extends Specification {

    private static String TEST_AGENT = 'myAgent - 192.168.1.1'

    private static long TIME = 1378976834000L

    private static double VALUE = 42d

    private static int COUNT_OF_AGGREGATED_RESULTS = 4

    private static URL URL = new URL("https://www.google.de/search?q=42")

    void testEqualsAndHashCode() {
        given:
        OsmChartPoint out = new OsmChartPoint(time: TIME, csiAggregation: VALUE, countOfAggregatedResults: COUNT_OF_AGGREGATED_RESULTS, sourceURL: URL, testingAgent: TEST_AGENT)

        OsmChartPoint equalsToOut = new OsmChartPoint(time: TIME, csiAggregation: VALUE, countOfAggregatedResults: COUNT_OF_AGGREGATED_RESULTS, sourceURL: URL, testingAgent: TEST_AGENT)

        OsmChartPoint differentValue = new OsmChartPoint(time: TIME, csiAggregation: 23d, countOfAggregatedResults: COUNT_OF_AGGREGATED_RESULTS, sourceURL: URL, testingAgent: TEST_AGENT)

        OsmChartPoint differentCountOfAggregatedResults = new OsmChartPoint(time: TIME, csiAggregation: VALUE, countOfAggregatedResults: 3, sourceURL: URL, testingAgent: TEST_AGENT)

        OsmChartPoint differentTime = new OsmChartPoint(time: 1378976835000L, csiAggregation: VALUE, countOfAggregatedResults: COUNT_OF_AGGREGATED_RESULTS, sourceURL: URL, testingAgent: TEST_AGENT)

        OsmChartPoint differentURL = new OsmChartPoint(time: TIME, csiAggregation: VALUE, countOfAggregatedResults: COUNT_OF_AGGREGATED_RESULTS, sourceURL: new URL("https://www.example.com/42"), testingAgent: TEST_AGENT)

        OsmChartPoint differentTestAgent = new OsmChartPoint(time: TIME, csiAggregation: VALUE, countOfAggregatedResults: COUNT_OF_AGGREGATED_RESULTS, sourceURL: URL, testingAgent: 'myAgent - 192.168.1.2')

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
        given:
        final OsmChartPoint out = new OsmChartPoint(time: TIME, csiAggregation: VALUE,
                countOfAggregatedResults: COUNT_OF_AGGREGATED_RESULTS, sourceURL: null, testingAgent: TEST_AGENT)
        expect:
        !out.hasAnSourceURL()
    }

    void "test hasAnSourceURL to be true if an url was specified"() {
        given:
        final OsmChartPoint out = new OsmChartPoint(time: TIME, csiAggregation: VALUE,
                countOfAggregatedResults: COUNT_OF_AGGREGATED_RESULTS, sourceURL: URL, testingAgent: TEST_AGENT)
        expect:
        out.hasAnSourceURL()
    }

    void "Chartpoints with an invalid time should return false on isValid"() throws Throwable {
        given:
        OsmChartPoint out = new OsmChartPoint(time: -1L, csiAggregation: VALUE, countOfAggregatedResults: 3, sourceURL: URL, testingAgent: TEST_AGENT)
        expect:
        !out.isValid()
    }
}

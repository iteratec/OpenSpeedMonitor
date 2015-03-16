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

import static org.hamcrest.Matchers.*
import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertNotEquals
import static org.junit.Assert.assertThat

import org.junit.Test

import static org.junit.Assert.assertTrue

/**
 * <p>
 * Test-suite of {@link OsmChartPoint}.
 * </p>
 * 
 * @author mze
 * @since IT-78
 */
public class OsmChartPointTests {
	
	private static final String TEST_AGENT = 'myAgent - 192.168.1.1'

	private static final long TIME = 1378976834000L

	private static final double VALUE = 42d

	private static final int COUNT_OF_AGGREGATED_RESULTS = 4

	private static final URL URL = new URL("https://www.google.de/search?q=42")

	@Test
	public void testEqualsAndHashCode() throws Throwable {

        OsmChartPoint out = new OsmChartPoint(
                TIME, VALUE, COUNT_OF_AGGREGATED_RESULTS, URL, TEST_AGENT)

        OsmChartPoint equalsToOut = new OsmChartPoint(
                TIME, VALUE, COUNT_OF_AGGREGATED_RESULTS, URL, TEST_AGENT)

        OsmChartPoint differentValue = new OsmChartPoint(
                TIME, 23d, COUNT_OF_AGGREGATED_RESULTS, URL, TEST_AGENT)

        OsmChartPoint differentCountOfAggregatedResults = new OsmChartPoint(
                TIME, VALUE, 3, URL, TEST_AGENT)

        OsmChartPoint differentTime = new OsmChartPoint(
                1378976835000L, VALUE, COUNT_OF_AGGREGATED_RESULTS, URL, TEST_AGENT)

        OsmChartPoint differentURL = new OsmChartPoint(
                TIME, VALUE, COUNT_OF_AGGREGATED_RESULTS, new URL("https://www.example.com/42"), TEST_AGENT)

        OsmChartPoint differentTestAgent = new OsmChartPoint(
                TIME, VALUE, COUNT_OF_AGGREGATED_RESULTS, URL, 'myAgent - 192.168.1.2')

//        assertTrue(out.equals(equalsToOut))
		assertThat(out, equalTo(equalsToOut))
		assertThat(out.hashCode(), equalTo(equalsToOut.hashCode()))

		assertThat(out, not(equalTo(differentTime)))
		assertThat(out, not(equalTo(differentValue)))
		assertThat(out, not(equalTo(differentCountOfAggregatedResults)))
		assertThat(out, not(equalTo(differentURL)))
		assertThat(out, not(equalTo(differentTestAgent)))
	}

	@Test
	public void testHighchartPoin_noSourceURLt() throws Throwable {
		final OsmChartPoint out = new OsmChartPoint(TIME, VALUE, COUNT_OF_AGGREGATED_RESULTS,
				null, TEST_AGENT)

		assertThat(out.time, is(TIME))
		assertThat(out.measuredValue, closeTo(VALUE, 0.0d))
		assertThat(out.countOfAggregatedResults, is(COUNT_OF_AGGREGATED_RESULTS))
		assertThat(out.hasAnSourceURL(), is(false))
		assertThat(out.sourceURL, nullValue())
	}

	@Test
	public void testHighchartPoint() throws Throwable {
		final OsmChartPoint out = new OsmChartPoint(TIME, VALUE, 3,
				URL, TEST_AGENT)

		assertThat(out.time, is(TIME))
		assertThat(out.measuredValue, closeTo(VALUE, 0.0d))
		assertThat(out.countOfAggregatedResults, is(3))
		assertThat(out.hasAnSourceURL(), is(true))
		assertThat(out.sourceURL, is(URL))
	}

	@Test(expected = IllegalArgumentException.class)
	public void testHighchartPoint_InvalidTime() throws Throwable {
		new OsmChartPoint(-1L, VALUE, 3, URL, TEST_AGENT)
	}
}

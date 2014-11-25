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

package de.iteratec.osm.report.chart;

import java.net.URL;

import org.junit.Assert;
import org.junit.Test;

/**
 * <p>
 * Test-suite of {@link OsmChartPoint}.
 * </p>
 * 
 * @author mze
 * @since IT-78
 */
public class OsmChartPointTests {

	@Test
	public void testEqualsAndHashCode() throws Throwable {
		final OsmChartPoint out = new OsmChartPoint(1378976834000L, 42d, 4,
				new URL("https://www.google.de/search?q=42"));

		final OsmChartPoint equalsToOut = new OsmChartPoint(1378976834000L,
				42d, 4, new URL("https://www.google.de/search?q=42"));

		final OsmChartPoint differentValue = new OsmChartPoint(
				1378976834000L, 23d, 3, new URL(
						"https://www.google.de/search?q=42"));

		final OsmChartPoint differentCountOfAggregatedResults = new OsmChartPoint(
				1378976834000L, 42d, 3, new URL(
						"https://www.google.de/search?q=42"));

		final OsmChartPoint differentTime = new OsmChartPoint(1378976835000L,
				42d, 4, new URL("https://www.google.de/search?q=42"));

		final OsmChartPoint differentURL = new OsmChartPoint(1378976835000L,
				42d, 4, new URL("https://www.example.com/42"));

		Assert.assertEquals(out, equalsToOut);
		Assert.assertEquals(out.hashCode(), equalsToOut.hashCode());

		Assert.assertFalse(out.equals(differentTime));
		Assert.assertFalse(out.equals(differentValue));
		Assert.assertFalse(out.equals(differentCountOfAggregatedResults));
		Assert.assertFalse(out.equals(differentURL));
	}

	@Test
	public void testHighchartPoin_noSourceURLt() throws Throwable {
		final OsmChartPoint out = new OsmChartPoint(1378976834000L, 42d, 4,
				null);

		Assert.assertEquals(1378976834000L, out.time);
		Assert.assertEquals(42d, out.measuredValue, 0.0d);
		Assert.assertEquals(4, out.countOfAggregatedResults);
		Assert.assertFalse(out.hasAnSourceURL());

		Assert.assertNull(out.sourceURL);
	}

	@Test
	public void testHighchartPoint() throws Throwable {
		final OsmChartPoint out = new OsmChartPoint(1378976834000L, 42d, 3,
				new URL("https://www.google.de/search?q=42"));

		Assert.assertEquals(1378976834000L, out.time);
		Assert.assertEquals(42d, out.measuredValue, 0.0d);
		Assert.assertEquals(3, out.countOfAggregatedResults);
		Assert.assertTrue(out.hasAnSourceURL());
		Assert.assertEquals(new URL("https://www.google.de/search?q=42"),
				out.sourceURL);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testHighchartPoint_InvalidTime() throws Throwable {
		new OsmChartPoint(-1L, 42d, 3, new URL(
				"https://www.google.de/search?q=42"));
	}
}

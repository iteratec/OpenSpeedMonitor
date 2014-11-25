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

package de.iteratec.osm.report.external;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * <p>
 * Test-suite of {@link GraphitePathName}.
 * </p>
 * 
 * @author mze
 * @since 2013-11-06 (currently no JIRA-Ticket, draft)
 */
public class GraphitePathNameTests {

	@Test
	public void testDesign() {
		GraphitePathName out = GraphitePathName
				.valueOf("wpt.lhotse.daily.hp.csi");

		assertEquals("wpt.lhotse.daily.hp.csi", out.toString());
	}

	@Test
	public void testDesign_PathConstruction() {
		GraphitePathName out = GraphitePathName.valueOf("wpt", "lhotse",
				"daily", "hp", "csi");

		assertEquals("wpt.lhotse.daily.hp.csi", out.toString());
	}

	@Test
	public void testEscaping() {
		GraphitePathName out = GraphitePathName.valueOf("wpt", "lhotse",
				"daily/update", "hp AB 123", "csi");

		assertEquals("wpt.lhotse.daily-update.hp_AB_123.csi", out.toString());
	}

	@Test
	public void testEqualsAndHashCode() {
		GraphitePathName out = GraphitePathName
				.valueOf("wpt.lhotse.daily.hp.csi");
		GraphitePathName equalToOut = GraphitePathName
				.valueOf("wpt.lhotse.daily.hp.csi");
		GraphitePathName notEqualToOut = GraphitePathName
				.valueOf("wpt.lhotse.weekly.hp.csi");

		assertEquals(out, equalToOut);
		assertEquals(out.hashCode(), equalToOut.hashCode());

		assertFalse(out.equals(notEqualToOut));
	}

	@Test
	public void testToString() {
		GraphitePathName out1 = GraphitePathName
				.valueOf("wpt.lhotse.daily.hp.csi");
		GraphitePathName out2 = GraphitePathName
				.valueOf("wpt.lhotse.weekly.hp.csi");

		assertNotNull(out1.toString());
		assertEquals("wpt.lhotse.daily.hp.csi", out1.toString());

		assertNotNull(out2.toString());
		assertEquals("wpt.lhotse.weekly.hp.csi", out2.toString());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testValueOf_invalidPath_doubleDots() {
		GraphitePathName.valueOf("wpt.lhotse.daily.hp..csi");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testValueOf_invalidPath_empty() {
		GraphitePathName.valueOf("");
	}

	@Test(expected = NullPointerException.class)
	public void testValueOf_nullArg_String() {
		GraphitePathName.valueOf((String)null);
	}
	
	@Test(expected = NullPointerException.class)
	public void testValueOf_nullArg_StringArray() {
		GraphitePathName.valueOf((String[])null);
	}

	@Test
	public void testValueOf_validPath() {
		GraphitePathName out = GraphitePathName
				.valueOf("wpt.lhotse.daily.hp.csi");

		assertNotNull(out);
	}
}

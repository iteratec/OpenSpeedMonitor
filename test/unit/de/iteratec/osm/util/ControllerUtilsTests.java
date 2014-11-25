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

package de.iteratec.osm.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

/**
 * <p>
 * Test-suite of {@link ControllerUtils}.
 * 
 * @author mze
 * @since IT-106
 */
public class ControllerUtilsTests {

	@Test
	public void testIsEmptyRequest_emptyRequest() {
		Map<String, Object> params = new HashMap<String, Object>();

		assertTrue(ControllerUtils.isEmptyRequest(params));
	}

	@Test
	public void testIsEmptyRequest_nonEmptyRequest() {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("selectedGroupIDs",
				Arrays.asList(new Object[] { 1L, 2L, 3L }));

		assertFalse(ControllerUtils.isEmptyRequest(params));
	}

	@Test
	public void testIsEmptyRequest_treatAsEmptyRequest() {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("lang", "not-of-intresst-for-test");
		params.put("action", "not-of-intresst-for-test");
		params.put("controller", "not-of-intresst-for-test");

		assertTrue(ControllerUtils.isEmptyRequest(params));
	}
}

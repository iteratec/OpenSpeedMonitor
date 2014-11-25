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

package de.iteratec.osm.result

import static org.junit.Assert.*

import org.junit.Test

import de.iteratec.osm.result.MvQueryParams;

/**
 * Test-suite for {@link MvQueryParams}.
 * 
 * @author mze
 */
class MvQueryParamsTests {

	@Test
	public void testDefaultConstructor() {
		MvQueryParams queryParams = new MvQueryParams();

		assertNotNull(queryParams.jobGroupIds)
		assertNotNull(queryParams.measuredEventIds)
		assertNotNull(queryParams.pageIds)
		assertNotNull(queryParams.browserIds)
		assertNotNull(queryParams.locationIds)
	}

	@Test
	public void testToString() {
		MvQueryParams out = new MvQueryParams();
		out.jobGroupIds.addAll([8, 9]);
		out.measuredEventIds.addAll([38, 77]);
		out.pageIds.addAll([1, 3, 8]);
		out.browserIds.addAll([7]);
		out.locationIds.addAll([99, 101]);

		assertEquals(
				'jobGroupIds=[8, 9], pageIds=[1, 3, 8], measuredEventIds=[38, 77], browserIds[7], locationIds=[99, 101]',
				out.toString())
	}
}

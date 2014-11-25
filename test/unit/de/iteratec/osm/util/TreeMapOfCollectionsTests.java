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

import static org.junit.Assert.*;

import java.util.Collection;
import java.util.LinkedList;

import de.iteratec.osm.util.TreeMapOfCollections;
import org.junit.Test;

/**
 * <p>
 * Test-suite of {@link de.iteratec.osm.util.TreeMapOfCollections}.
 * </p>
 * 
 * @author mze
 * @since IT-60
 */
public class TreeMapOfCollectionsTests {

	@Test
	public void testDesign() {
		TreeMapOfCollections<String, Long> numbersByName = new TreeMapOfCollections<String, Long>(
				LinkedList.class);

		assertTrue(numbersByName.isEmpty());
		assertFalse(numbersByName.containsKey("primes"));
		assertFalse(numbersByName.containsKey("odds"));
		assertNull(numbersByName.get("primes"));
		assertNull(numbersByName.get("odds"));

		Collection<Long> primes = numbersByName.getOrCreate("primes");
		assertNotNull(primes);
		assertTrue(primes.isEmpty());
		assertTrue(numbersByName.containsKey("primes"));
		assertNotNull(numbersByName.get("primes"));
		primes.add(1L);
		primes.add(3L);

		assertFalse(numbersByName.containsKey("odds"));
		assertNull(numbersByName.get("odds"));

		Collection<Long> odds = numbersByName.getOrCreate("odds");
		assertNotNull(odds);
		assertTrue(odds.isEmpty());
		assertTrue(numbersByName.containsKey("odds"));
		assertNotNull(numbersByName.get("odds"));
		odds.add(2L);
		odds.add(4L);

		Collection<Long> primesAgain = numbersByName.getOrCreate("primes");
		assertNotNull(primesAgain);
		assertFalse(primesAgain.isEmpty());
		assertTrue(primesAgain.contains(1L));
		assertTrue(primesAgain.contains(3L));
		assertSame(primes, primesAgain);
	}

}

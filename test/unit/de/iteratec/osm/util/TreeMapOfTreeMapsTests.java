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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.TreeMap;

import de.iteratec.osm.util.TreeMapOfTreeMaps;
import org.junit.Test;

/**
 * <p>
 * Test-suite of {@link de.iteratec.osm.util.TreeMapOfTreeMaps}.
 * </p>
 * 
 * @author mze
 * @since IT-60
 */
public class TreeMapOfTreeMapsTests {

	@Test
	public void testDesign() {
		TreeMapOfTreeMaps<String, Long, Double> out = new TreeMapOfTreeMaps<String, Long, Double>();

		assertTrue(out.isEmpty());
		assertFalse(out.containsKey("prime-scoring"));
		assertFalse(out.containsKey("odd-scoring"));
		assertNull(out.get("prime-scoring"));
		assertNull(out.get("odd-scoring"));

		TreeMap<Long, Double> primeScoring = out.getOrCreate("prime-scoring");
		assertNotNull(primeScoring);
		assertTrue(primeScoring.isEmpty());
		primeScoring.put(1L, 3.0d);
		primeScoring.put(3L, 7.0d);

		assertTrue(out.containsKey("prime-scoring"));
		assertNotNull(out.get("prime-scoring"));
		assertFalse(out.containsKey("odd-scoring"));
		assertNull(out.get("odd-scoring"));

		TreeMap<Long, Double> oddScoring = out.getOrCreate("odd-scoring");
		assertNotNull(oddScoring);
		assertTrue(oddScoring.isEmpty());
		assertTrue(out.containsKey("odd-scoring"));
		assertNotNull(out.get("odd-scoring"));
		oddScoring.put(2L, 5.0d);
		oddScoring.put(4L, 6.0d);

		assertTrue(out.containsKey("prime-scoring"));
		assertNotNull(out.get("prime-scoring"));
		assertTrue(out.containsKey("odd-scoring"));
		assertNotNull(out.get("odd-scoring"));

		TreeMap<Long, Double> primeScoringAgain = out
				.getOrCreate("prime-scoring");
		assertNotNull(primeScoringAgain);
		assertFalse(primeScoringAgain.isEmpty());
		assertTrue(primeScoringAgain.containsKey(1L));
		assertEquals(3.0d, primeScoringAgain.get(1L), 0.0d);
		assertTrue(primeScoringAgain.containsKey(3L));
		assertEquals(7.0d, primeScoringAgain.get(3L), 0.0d);

		assertSame(primeScoring, primeScoringAgain);
	}

}

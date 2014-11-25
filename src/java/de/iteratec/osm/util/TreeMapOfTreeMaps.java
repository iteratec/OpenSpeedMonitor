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

import java.util.TreeMap;

/**
 * <p>
 * A {@link TreeMap} containing TreeMaps of values. This map provides a special
 * operation {@link #getOrCreate(Object)} which returns an inner map, potently
 * newly created but never <code>null</code>.
 * </p>
 * 
 * @author mze
 * @since IT-60
 * @param <KO>
 *            The key type of the outer keys, the keys of the first map.
 * @param <KI>
 *            The key type of the inner keys, the keys of the contained maps.
 * @param <V>
 *            The type of the values contained in the inner Map.
 */
public class TreeMapOfTreeMaps<KO, KI, V> extends TreeMap<KO, TreeMap<KI, V>> {

	private static final long serialVersionUID = -2734255924036229074L;

	/**
	 * <p>
	 * Gets the map assigned to the specified key or creates and assigns it if
	 * absent before.
	 * </p>
	 * 
	 * @param key
	 *            The key of the map to deliver, not <code>null</code>.
	 * @return The map previously assigned to the key or a newly created and
	 *         newly assigned map; never <code>null</code>
	 * @NotThreadSafe
	 */
	public TreeMap<KI, V> getOrCreate(KO key) {
		TreeMap<KI, V> result = this.get(key);

		if (result == null) {
			result = new TreeMap<KI, V>();
			this.put(key, result);
		}

		return result;
	}
}

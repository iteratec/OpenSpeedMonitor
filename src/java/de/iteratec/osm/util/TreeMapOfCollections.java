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

import java.util.Collection;
import java.util.TreeMap;

/**
 * <p>
 * A {@link TreeMap} containing collections.
 * </p>
 * 
 * <p>
 * This class primary exists because
 * {@link java.util.Map#withDefault(Closure init)} from Groovy JDK does not work
 * probably with sorted sets.
 * </p>
 * 
 * @author mze
 * @since IT-60
 * @param <K>
 *            The key type of the Map.
 * @param <E>
 *            The entry type of the contained collections.
 */
public class TreeMapOfCollections<K, E> extends TreeMap<K, Collection<E>> {

	private static final long serialVersionUID = 6776294204293800080L;

	/**
	 * The type of the collection to create an instance of if a requested
	 * collection for a key is absent.
	 */
	private Class<? extends Collection<E>> collectionType;

	/**
	 * <p>
	 * Creates a map of collections with inner collections of specified type.
	 * </p>
	 * 
	 * @param collectionType
	 */
	@SuppressWarnings("unchecked")
	public TreeMapOfCollections(
			@SuppressWarnings("rawtypes") Class<? extends Collection> collectionType) {
		this.collectionType = (Class<? extends Collection<E>>) collectionType;
	}

	/**
	 * <p>
	 * Gets the collection assigned to the specified key or creates and assigns
	 * it if absent before.
	 * </p>
	 * 
	 * @param key
	 *            The key of the collection to deliver, not <code>null</code>.
	 * @return The Collection previously assigned to the key or a newly created
	 *         and newly assigned Collection; never <code>null</code>
	 * @NotThreadSafe
	 */
	public Collection<E> getOrCreate(K key) {
		Collection<E> result = this.get(key);

		if (result == null) {
			try {
				result = collectionType.newInstance();
			} catch (InstantiationException cause) {
				throw new RuntimeException(
						"Unexpected failure on creation of inner collection",
						cause);
			} catch (IllegalAccessException cause) {
				throw new RuntimeException(
						"Unexpected failure on creation of inner collection",
						cause);
			}
			this.put(key, result);
		}

		return result;
	}
}

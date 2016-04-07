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

package de.iteratec.osm.measurement.environment.dao;

import de.iteratec.osm.measurement.environment.Location;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * <p>
 * An data-access object (DAO) for {@link de.iteratec.osm.measurement.environment.Location}.
 * </p>
 * 
 * <p>
 * Contains only methods that query {@link de.iteratec.osm.measurement.environment.Location}s from database. Doesn't
 * contain any dependencies to other domains or service-logic.
 * </p>
 * 
 * @author nkuhn
 * @author mze
 */
public interface LocationDaoService {

	/**
	 * <p>
	 * Finds all {@linkplain de.iteratec.osm.measurement.environment.Location locations} currently known in database.
	 * The returned {@link Set} is unmodifiable.
	 * </p>
	 * 
	 * @return Never <code>null</code> but possibly
	 *         {@linkplain Collection#isEmpty() empty}.
	 */
	Set<Location> findAll();

	/**
	 * <p>
	 * Gets a {@link Map} with an entry for every persisted {@link Location}
	 * from db.
	 * </p>
	 * 
	 * @return Map with id of {@link Location}s as key and the objects itself as
	 *         value.
	 */
	Map<Long, Location> getIdToObjectMap();
	
	/**
	 * <p>
	 * Tries to find a {@link Location} by its WPT-Location. This is the agents 
	 * location queue name.
	 * </p>
	 * 
	 * @param wptLocation The WPT-Location to search for, not <code>null</code>. 
	 * @return The matching location or <code>null</code> if no matching one 
	 *         was found.
	 */
	Location tryToFindByWPTLocation(String wptLocation);
}

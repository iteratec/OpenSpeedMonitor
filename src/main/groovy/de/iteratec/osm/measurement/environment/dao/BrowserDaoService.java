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

import de.iteratec.osm.measurement.environment.Browser;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * <p>
 * An data-access object (DAO) for {@link de.iteratec.osm.measurement.environment.Browser}.
 * </p>
 * 
 * <p>
 * Contains only methods that query {@link de.iteratec.osm.measurement.environment.Browser}s from database. Doesn't
 * contain any dependencies to other domains or service-logic.
 * </p>
 * 
 * @author nkuhn
 * @author mze
 */
public interface BrowserDaoService {

	/**
	 * <p>
	 * Finds all {@linkplain de.iteratec.osm.measurement.environment.Browser browser} currently known in database. The
	 * returned {@link Set} is unmodifiable.
	 * </p>
	 * 
	 * @return Never <code>null</code> but possibly
	 *         {@linkplain Collection#isEmpty() empty}.
	 */
	Set<Browser> findAll();

	/**
	 * Gets a {@link Map} with an entry for every persisted {@link Browser} from
	 * db.
	 * 
	 * @return Map with id of {@link Browser}s as key and the objects itself as
	 *         value.
	 */
	Map<Long, Browser> getIdToObjectMap();
	
	/**
	 * Searches for a {@link Browser} identified by its name/alias.
	 * 
	 * @param browserNameOrAlias the name or alias of the browser, never <code>null</code>!
	 * @return the first {@link Browser} found. Returns <code>null</code>, if no browser was found. 
	 */
	Browser tryToFindByNameOrAlias(String browserNameOrAlias);

}

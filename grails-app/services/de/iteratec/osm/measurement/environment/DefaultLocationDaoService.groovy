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

package de.iteratec.osm.measurement.environment

import de.iteratec.osm.measurement.environment.dao.LocationDaoService
import grails.transaction.Transactional

/**
 * <p>
 * Default implementation for {@link de.iteratec.osm.measurement.environment.dao.LocationDaoService}.
 * </p>
 * 
 * @author nkuhn
 * @author mze
 */
@Transactional
class DefaultLocationDaoService implements LocationDaoService {

	@Override
	Map<Serializable, Location> getIdToObjectMap(){
		return Location.list().collectEntries { Location eachLocation -> [
				eachLocation.ident(),
				eachLocation]
		}
	}

	@Override
	public Set<Location> findAll() {
		Set<Location> result = Collections.checkedSet(new HashSet<Location>(), Location.class);
		result.addAll(Location.list());
		return Collections.unmodifiableSet(result);
	}
	
	@Override
	public Location tryToFindByWPTLocation(String wptLocation)
	{
		return Location.findByLocation(wptLocation);
	}
}

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

package de.iteratec.osm.report.chart.dao;

import de.iteratec.osm.report.chart.AggregatorType;

import java.util.Map;
import java.util.Set;

/**
 * <p>
 * An data-access object (DAO) for {@link AggregatorType}.
 * </p>
 * 
 * @author mze
 * @since IT-74
 */
public interface AggregatorTypeDaoService {

	/**
	 * <p>
	 * Finds all {@linkplain AggregatorType aggregation types} currently 
	 * known in database. The returned {@link Set} is unmodifiable. 
	 * </p>
	 * 
	 * @return Never <code>null</code> but 
	 *         possibly {@linkplain Collection#isEmpty() empty}.
	 */
	Set<AggregatorType> findAll();
	
	/**
	 * <p>
	 * Returns a map with the names of all {@link AggregatorType}s from db as keys. Values in the map are respective AggregatorType-instances.
	 * </p>
	 * @return
	 */
	Map<String, AggregatorType> getNameToObjectMap();
}

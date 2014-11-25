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

package de.iteratec.osm.report.chart

import de.iteratec.osm.report.chart.dao.AggregatorTypeDaoService

/**
 * <p>
 * Default implementation of {@link de.iteratec.osm.report.chart.dao.AggregatorTypeDaoService}.
 * </p>
 * 
 * @author mze
 * @since IT-74
 */
class DefaultAggregatorTypeDaoService implements AggregatorTypeDaoService {

	@Override
	public Set<AggregatorType> findAll() {
		Set<AggregatorType> result = Collections.checkedSet(new HashSet<AggregatorType>(), AggregatorType.class);
		result.addAll(AggregatorType.list());
		return Collections.unmodifiableSet(result);
	}
	
	@Override
	public Map<String, AggregatorType> getNameToObjectMap(){
		return AggregatorType.list().collectEntries { AggregatorType eachAggregatorType ->
			[
				eachAggregatorType.name,
				eachAggregatorType
			]
		}
	}
}

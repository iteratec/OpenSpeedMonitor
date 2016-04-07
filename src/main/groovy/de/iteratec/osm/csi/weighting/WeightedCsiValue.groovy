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

package de.iteratec.osm.csi.weighting

/**
 * A {@link WeightedValue} extended by the Long-ID's of all {@link EventResult}s, which underly the calculation of the value.  
 * @author nkuhn
 *
 */
class WeightedCsiValue {
	/**
	 * Underly the calculation of the value of this WeightedCsiValue.
	 */
	List<Long> underlyingEventResultIds
	WeightedValue weightedValue

	@Override
	public String toString() {
		return "WeightedCsiValue [underlyingEventResultIds=${underlyingEventResultIds}, weightedValue=${weightedValue}]"
	}	

}

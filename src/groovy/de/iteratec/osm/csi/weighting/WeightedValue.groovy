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
 * A value with a weight for building weighted means.
 * @author nkuhn
 * @see de.iteratec.isocsi.CsiAggregationService
 *
 */
class WeightedValue {
	
	Double value
	Double weight
	
	@Override
	public String toString() {
		return "WeightedValue [value=${value}, weight=${weight}]"
	}

	boolean equals(o) {
		if (this.is(o)) return true
		if (getClass() != o.class) return false

		WeightedValue that = (WeightedValue) o

		if (value != that.value) return false
		if (weight != that.weight) return false

		return true
	}

	int hashCode() {
		int result
		result = (value != null ? value.hashCode() : 0)
		result = 31 * result + (weight != null ? weight.hashCode() : 0)
		return result
	}
}

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

package de.iteratec.osm.csi

import de.iteratec.osm.csi.weighting.WeightedValue
import grails.transaction.Transactional;


/**
 * 
 * @author nkuhn, fpavkovic
 *
 */
@Transactional
class MeanCalcService {
	
	/**
	 * Calculates weighted mean.
	 * @param weightedValues
	 * @return The weighted mean.
	 * @throws IllegalArgumentException if given list weightedValues is empty or the sum of the weights of all contained {@link WeightedValue}s is 0.  
	 */
	Double calculateWeightedMean(List<WeightedValue> weightedValues) {
		if (weightedValues) {
			Double sumOfWeights = 0
			Double sumOfWeightedValues = 0
			weightedValues.each {
				if (it.value!=null && it.weight!=null) {
					sumOfWeights += it.weight
					sumOfWeightedValues += it.value * it.weight
				}
			}
			if (sumOfWeights==0) {
				throw new IllegalArgumentException("incorrect weights: sumOfWeights=0")
			}
			return sumOfWeightedValues / sumOfWeights
		}else{
			throw new IllegalArgumentException("no number and/or weights transferred")
		}
	}
}

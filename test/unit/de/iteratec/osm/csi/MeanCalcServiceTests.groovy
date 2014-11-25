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

import grails.test.mixin.*

import org.junit.Test

import de.iteratec.osm.csi.weighting.WeightedValue

/**
 * Test-suite of {@link MeanCalcService}.
 */
@TestFor(MeanCalcService)
class MeanCalcServiceTests {

	MeanCalcService serviceUnderTest
	static final double DELTA = 1e-15
	
	void setUp() {
		serviceUnderTest = service
	}
	
	@Test
	void testCalculateWeightedMean_ValidWeights() {
		// Create some test data:
		List<WeightedValue> weights = new ArrayList<WeightedValue>()
		
		WeightedValue valueAndWeight = new WeightedValue(value: 1.20, weight: 10.00)
		weights.add(valueAndWeight)
		
		valueAndWeight = new WeightedValue(value: 1.34, weight: 5.00)
		weights.add(valueAndWeight)
		
		valueAndWeight = new WeightedValue(value: 1.56, weight: 6.00)
		weights.add(valueAndWeight)
		
		valueAndWeight = new WeightedValue(value: 1.50, weight: 3.00)
		weights.add(valueAndWeight)
		
		valueAndWeight = new WeightedValue(value: 2.10, weight: 3.00)
		weights.add(valueAndWeight)
		
		valueAndWeight = new WeightedValue(value: 2.30, weight: 12.00)
		weights.add(valueAndWeight)
		
		valueAndWeight = new WeightedValue(value: 3.20, weight: 5.00)
		weights.add(valueAndWeight)
		
		valueAndWeight = new WeightedValue(value: 0.10, weight: 6.00)
		weights.add(valueAndWeight)
		
		valueAndWeight = new WeightedValue(value: 0.38, weight: 10.00)
		weights.add(valueAndWeight)
		
		valueAndWeight = new WeightedValue(value: 8.34, weight: 5.00)
		weights.add(valueAndWeight)
		
		valueAndWeight = new WeightedValue(value: 6.23, weight: 8.00)
		weights.add(valueAndWeight)
		
		valueAndWeight = new WeightedValue(value: 9.10, weight: 3.00)
		weights.add(valueAndWeight)
		
		valueAndWeight = new WeightedValue(value: 10.00, weight: 3.00)
		weights.add(valueAndWeight)
		
		valueAndWeight = new WeightedValue(value: 2.30, weight: 10.00)
		weights.add(valueAndWeight)
		
		valueAndWeight = new WeightedValue(value: 1.20, weight: 5.00)
		weights.add(valueAndWeight)
		
		valueAndWeight = new WeightedValue(value: 1.62, weight: 6.00)
		weights.add(valueAndWeight)
		
		// Run the test:
		assertEquals(2.7442, serviceUnderTest.calculateWeightedMean(weights), DELTA)
	}
	
	@Test(expected=IllegalArgumentException.class)
	void testCalculateWeightedMean_InvalidWeights() {
		// Create some test data:
		List<WeightedValue> invalidWeights = new ArrayList<WeightedValue>()
		
		WeightedValue firstNoWeight = new WeightedValue(value: 1.20, weight: null)
		invalidWeights.add(firstNoWeight)
		
		WeightedValue secondNoWeight = new WeightedValue(value: 3.40, weight: null)
		invalidWeights.add(secondNoWeight)
		
		// Run the Test:
		serviceUnderTest.calculateWeightedMean(invalidWeights)
	}
	
	@Test(expected=IllegalArgumentException.class)
	void testCalculateWeightedMean_NoWeightsAtAll() {
		// Create some test data:
		List<WeightedValue> emptyListOfWeights = Collections.emptyList()
		
		// Run the Test:
		serviceUnderTest.calculateWeightedMean(emptyListOfWeights)
	}

}

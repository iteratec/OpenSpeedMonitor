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
import de.iteratec.osm.csi.weighting.WeightedValue
import spock.lang.Specification

/**
 * Test-suite of {@link MeanCalcService}.
 */
@TestFor(MeanCalcService)
class MeanCalcServiceSpec extends Specification {

    MeanCalcService serviceUnderTest
    static final double DELTA = 1e-15

    void setup() {
        serviceUnderTest = service
    }

    void "test calculate weighted mean with valid weights"() {
        given: "some data"
        List<WeightedValue> weights = new ArrayList<WeightedValue>()
        weights.add(new WeightedValue(value: 1.20, weight: 10.00))
        weights.add(new WeightedValue(value: 1.34, weight: 5.00))
        weights.add(new WeightedValue(value: 1.56, weight: 6.00))
        weights.add(new WeightedValue(value: 1.50, weight: 3.00))
        weights.add(new WeightedValue(value: 2.10, weight: 3.00))
        weights.add(new WeightedValue(value: 2.30, weight: 12.00))
        weights.add(new WeightedValue(value: 3.20, weight: 5.00))
        weights.add(new WeightedValue(value: 0.10, weight: 6.00))
        weights.add(new WeightedValue(value: 0.38, weight: 10.00))
        weights.add(new WeightedValue(value: 8.34, weight: 5.00))
        weights.add(new WeightedValue(value: 6.23, weight: 8.00))
        weights.add(new WeightedValue(value: 9.10, weight: 3.00))
        weights.add(new WeightedValue(value: 10.00, weight: 3.00))
        weights.add(new WeightedValue(value: 2.30, weight: 10.00))
        weights.add(new WeightedValue(value: 1.20, weight: 5.00))
        weights.add(new WeightedValue(value: 1.62, weight: 6.00))

        when: "calculating the weighted mean"
        Double mean = serviceUnderTest.calculateWeightedMean(weights)

        then: "the error should be less than DELTA"
        Math.abs(mean - 2.7442d) < DELTA
    }

    void "test calculate weighted mean with invalid weights"() {
        given: "data with invalid weights"
        List<WeightedValue> invalidWeights = new ArrayList<WeightedValue>()
        invalidWeights.add(new WeightedValue(value: 1.20, weight: null))
        invalidWeights.add(new WeightedValue(value: 3.40, weight: null))

        when: "calculating the weighted mean"
        serviceUnderTest.calculateWeightedMean(invalidWeights)

        then: "an IllegalArgumentException should be thrown"
        thrown(IllegalArgumentException)
    }

    void "test calculate weighted mean with no weights at all"() {
        given: "an empty list"
        List<WeightedValue> emptyListOfWeights = Collections.emptyList()

        when: "calculating the weighted mean"
        serviceUnderTest.calculateWeightedMean(emptyListOfWeights)

        then: "an IllegalArgumentException should be thrown"
        thrown(IllegalArgumentException)
    }

}

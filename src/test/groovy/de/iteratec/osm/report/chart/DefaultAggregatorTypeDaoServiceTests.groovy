/* 
* OpenSpeedMonitor (OSM)
* Copyright 2014 iteratec GmbH
* 
* Licensed under the Apache License, Version 2.0 (the "License")
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
import grails.buildtestdata.mixin.Build
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import spock.lang.Specification

/**
 * Test-suite of {@link AggregatorTypeDaoService}.
 */
@TestFor(DefaultAggregatorTypeDaoService)
@Mock([AggregatorType])
@Build([AggregatorType])
class DefaultAggregatorTypeDaoServiceTests extends Specification {

    AggregatorTypeDaoService serviceUnderTest

    def setup() {
        this.serviceUnderTest = service
    }


    void "test all created aggregator types should be returned by findAll"() {
        when: "we create multiple AggregatorTypes"
        AggregatorType firstAggregator = AggregatorType.build(name: "uniqueName1")
        AggregatorType secondAggregator = AggregatorType.build(name: "uniqueName2")

        then: "our results should contain each of them"
        Set<AggregatorType> result = serviceUnderTest.findAll()
        result
        result.size() == 2
        result.find({ it == firstAggregator })
        result.find({ it == secondAggregator })
    }

    def "test that AggregatorTypes that where inserted after a first findAll call will be returned by following findAll"() {
        when: "we call findAll and create afterwars another AggregatorType"
        AggregatorType.build(name: "uniqueName1")
        Set<AggregatorType> resultBefore = serviceUnderTest.findAll()
        AggregatorType additionalAggregator = AggregatorType.build(name: "uniqueName2")
        Set<AggregatorType> resultAfter = serviceUnderTest.findAll()

        then: "our next findAll should have one more element"
        resultBefore.size() == 1
        resultAfter.size() == 2
        resultAfter.find({ it == additionalAggregator })
    }

    def "findAll result should be immutable: remove"() {
        when: "we try to remove a object from our result"
        AggregatorType.build()
        def result = serviceUnderTest.findAll()
        result.remove(result.first())
        then: "we should face an exception"
        thrown UnsupportedOperationException
    }

    def "findAll result should be immutable: clear"() {
        when: "we try to clear our result"
        AggregatorType.build()
        serviceUnderTest.findAll().clear()

        then: "we should face an exception"
        thrown UnsupportedOperationException
    }

    def "findAll result should be immutable: add"() {
        when: "we try to add a Object to our result"
        AggregatorType.build(name: "1")
        def result = serviceUnderTest.findAll()
        result.add(AggregatorType.build(name: "2"))

        then: "we should face an exception"
        thrown UnsupportedOperationException
    }


    def "nameToObjectMap should contain every created AggregatorType"() {
        when: "we create 4 different AggregatorTypes"
        def meAggregator = AggregatorType.build(name: AggregatorType.MEASURED_EVENT)
        def paAggregator = AggregatorType.build(name: AggregatorType.PAGE)
        def rcAggregator = AggregatorType.build(name: AggregatorType.RESULT_CACHED_FULLY_LOADED_TIME)
        def ruAggregator = AggregatorType.build(name: AggregatorType.RESULT_UNCACHED_DOM_TIME)

        Map<String, AggregatorType> nameToObjectMap = serviceUnderTest.getNameToObjectMap()

        then: "the map should contain exactly this 4 AggregatorTypes"
        nameToObjectMap.size() == 4

        nameToObjectMap[AggregatorType.MEASURED_EVENT] == meAggregator
        nameToObjectMap[AggregatorType.PAGE] == paAggregator
        nameToObjectMap[AggregatorType.RESULT_CACHED_FULLY_LOADED_TIME] == rcAggregator
        nameToObjectMap[AggregatorType.RESULT_UNCACHED_DOM_TIME] == ruAggregator
    }
}

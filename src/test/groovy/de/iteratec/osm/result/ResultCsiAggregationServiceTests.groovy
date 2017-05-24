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

package de.iteratec.osm.result

import grails.buildtestdata.mixin.Build
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import spock.lang.Specification
import de.iteratec.osm.report.chart.AggregatorType


@TestFor(ResultCsiAggregationService)
@Mock([AggregatorType])
@Build([AggregatorType])
class ResultCsiAggregationServiceTests extends Specification {

    ResultCsiAggregationService serviceUnderTest

    void setup() {
        serviceUnderTest = service
        buildTestData()
    }

    void "get aggregator map for cached view"() {
        List<String> cached = [
                AggregatorType.RESULT_CACHED_DOC_COMPLETE_INCOMING_BYTES,
                AggregatorType.RESULT_CACHED_DOC_COMPLETE_REQUESTS,
                AggregatorType.RESULT_CACHED_FULLY_LOADED_INCOMING_BYTES,
                AggregatorType.RESULT_CACHED_DOC_COMPLETE_TIME,
                AggregatorType.RESULT_CACHED_DOM_TIME,
                AggregatorType.RESULT_CACHED_FIRST_BYTE,
                AggregatorType.RESULT_CACHED_FULLY_LOADED_REQUEST_COUNT,
                AggregatorType.RESULT_CACHED_FULLY_LOADED_TIME,
                AggregatorType.RESULT_CACHED_LOAD_TIME,
                AggregatorType.RESULT_CACHED_START_RENDER,
                AggregatorType.RESULT_CACHED_CS_BASED_ON_DOC_COMPLETE_IN_PERCENT,
                AggregatorType.RESULT_CACHED_SPEED_INDEX,
                AggregatorType.RESULT_CACHED_VISUALLY_COMPLETE,
                AggregatorType.RESULT_CACHED_CS_BASED_ON_VISUALLY_COMPLETE_IN_PERCENT
        ]

        expect:
        cached == ResultCsiAggregationService.getAggregatorMap().get(CachedView.CACHED)
    }

    void "get aggregator map for uncached views"() {
        List<AggregatorType> uncached = [
                AggregatorType.RESULT_UNCACHED_DOC_COMPLETE_INCOMING_BYTES,
                AggregatorType.RESULT_UNCACHED_DOC_COMPLETE_REQUESTS,
                AggregatorType.RESULT_UNCACHED_FULLY_LOADED_INCOMING_BYTES,
                AggregatorType.RESULT_UNCACHED_DOC_COMPLETE_TIME,
                AggregatorType.RESULT_UNCACHED_DOM_TIME,
                AggregatorType.RESULT_UNCACHED_FIRST_BYTE,
                AggregatorType.RESULT_UNCACHED_FULLY_LOADED_REQUEST_COUNT,
                AggregatorType.RESULT_UNCACHED_FULLY_LOADED_TIME,
                AggregatorType.RESULT_UNCACHED_LOAD_TIME,
                AggregatorType.RESULT_UNCACHED_START_RENDER,
                AggregatorType.RESULT_UNCACHED_CS_BASED_ON_DOC_COMPLETE_IN_PERCENT,
                AggregatorType.RESULT_UNCACHED_SPEED_INDEX,
                AggregatorType.RESULT_UNCACHED_VISUALLY_COMPLETE,
                AggregatorType.RESULT_UNCACHED_CS_BASED_ON_VISUALLY_COMPLETE_IN_PERCENT
        ]

        expect:
        uncached == ResultCsiAggregationService.getAggregatorMap().get(CachedView.UNCACHED)
    }

    private void buildTestData() {
        AggregatorType.build(name: AggregatorType.RESULT_UNCACHED_DOC_COMPLETE_TIME)
        AggregatorType.build(name: AggregatorType.RESULT_UNCACHED_DOC_COMPLETE_INCOMING_BYTES)
        AggregatorType.build(name: AggregatorType.RESULT_UNCACHED_DOC_COMPLETE_REQUESTS)
        AggregatorType.build(name: AggregatorType.RESULT_UNCACHED_FULLY_LOADED_INCOMING_BYTES)
        AggregatorType.build(name: AggregatorType.RESULT_UNCACHED_DOM_TIME)
        AggregatorType.build(name: AggregatorType.RESULT_UNCACHED_FIRST_BYTE)
        AggregatorType.build(name: AggregatorType.RESULT_UNCACHED_FULLY_LOADED_REQUEST_COUNT)
        AggregatorType.build(name: AggregatorType.RESULT_UNCACHED_FULLY_LOADED_TIME)
        AggregatorType.build(name: AggregatorType.RESULT_UNCACHED_LOAD_TIME)
        AggregatorType.build(name: AggregatorType.RESULT_UNCACHED_START_RENDER)
        AggregatorType.build(name: AggregatorType.RESULT_UNCACHED_CS_BASED_ON_DOC_COMPLETE_IN_PERCENT)
        AggregatorType.build(name: AggregatorType.RESULT_UNCACHED_SPEED_INDEX)
        AggregatorType.build(name: AggregatorType.RESULT_UNCACHED_VISUALLY_COMPLETE)
        AggregatorType.build(name: AggregatorType.RESULT_UNCACHED_CS_BASED_ON_VISUALLY_COMPLETE_IN_PERCENT)

        AggregatorType.build(name: AggregatorType.RESULT_CACHED_DOC_COMPLETE_TIME)
        AggregatorType.build(name: AggregatorType.RESULT_CACHED_DOC_COMPLETE_INCOMING_BYTES)
        AggregatorType.build(name: AggregatorType.RESULT_CACHED_DOC_COMPLETE_REQUESTS)
        AggregatorType.build(name: AggregatorType.RESULT_CACHED_FULLY_LOADED_INCOMING_BYTES)
        AggregatorType.build(name: AggregatorType.RESULT_CACHED_DOM_TIME)
        AggregatorType.build(name: AggregatorType.RESULT_CACHED_FIRST_BYTE)
        AggregatorType.build(name: AggregatorType.RESULT_CACHED_FULLY_LOADED_REQUEST_COUNT)
        AggregatorType.build(name: AggregatorType.RESULT_CACHED_FULLY_LOADED_TIME)
        AggregatorType.build(name: AggregatorType.RESULT_CACHED_LOAD_TIME)
        AggregatorType.build(name: AggregatorType.RESULT_CACHED_START_RENDER)
        AggregatorType.build(name: AggregatorType.RESULT_CACHED_CS_BASED_ON_DOC_COMPLETE_IN_PERCENT)
        AggregatorType.build(name: AggregatorType.RESULT_CACHED_SPEED_INDEX)
        AggregatorType.build(name: AggregatorType.RESULT_CACHED_VISUALLY_COMPLETE)
        AggregatorType.build(name: AggregatorType.RESULT_CACHED_CS_BASED_ON_VISUALLY_COMPLETE_IN_PERCENT)
    }
}

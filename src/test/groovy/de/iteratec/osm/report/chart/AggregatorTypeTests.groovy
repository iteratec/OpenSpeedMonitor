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

import spock.lang.Specification

import grails.test.mixin.TestFor;


@TestFor(AggregatorType)
class AggregatorTypeTests extends Specification{

	void testIsCached_MEASURED_EVENT() {
		given:
		AggregatorType out = new AggregatorType(name: AggregatorType.MEASURED_EVENT);
		expect:
		!out.isCached()
	}
	
	void testIsCached_RESULT_UNCACHED_DOC_COMPLETE_TIME() {
		given:
		AggregatorType out = new AggregatorType(name: AggregatorType.RESULT_UNCACHED_DOC_COMPLETE_TIME);
		expect:
		!out.isCached()
	}
	
	void testIsCached_RESULT_RESULT_CACHED_DOC_COMPLETE_TIME() {
		given:
		AggregatorType out = new AggregatorType(name: AggregatorType.RESULT_CACHED_DOC_COMPLETE_TIME);
		expect:
		out.isCached()
	}
}

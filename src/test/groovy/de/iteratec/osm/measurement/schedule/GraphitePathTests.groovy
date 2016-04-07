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

package de.iteratec.osm.measurement.schedule

import de.iteratec.osm.report.external.GraphitePath

import static org.junit.Assert.*;

import grails.test.mixin.*

import org.junit.*

import de.iteratec.osm.report.chart.AggregatorType
import de.iteratec.osm.report.chart.MeasurandGroup

/**
 * See the API for {@link grails.test.mixin.domain.DomainClassUnitTestMixin} for usage instructions
 */
@TestFor(GraphitePath)
@Mock([GraphitePath, AggregatorType])
class GraphitePathTests {

	@Test
    void testCreationWithDifferentInvalidPrefixes() {
		
		// Test-data
		AggregatorType validMeasurand = new AggregatorType(name: AggregatorType.RESULT_UNCACHED_DOC_COMPLETE_TIME, measurandGroup: MeasurandGroup.LOAD_TIMES)
		
		// Mocking domain-class for constraints
		mockForConstraintsTests(GraphitePath)
		
		// Run the tests...
		GraphitePath out = new GraphitePath(prefix: "", measurand: validMeasurand)
		assertFalse( out.validate() );
		assertEquals( "blank", out.errors["prefix"] );
		
		out = new GraphitePath(prefix: "wpt..", measurand: validMeasurand)
		assertFalse( out.validate() );
		assertEquals( "matches", out.errors["prefix"] );
		
		out = new GraphitePath(prefix: "wpt.testdt", measurand: validMeasurand)
		assertFalse( out.validate() );
		assertEquals( "matches", out.errors["prefix"] );
		
		out = new GraphitePath(prefix: "wpt", measurand: validMeasurand)
		assertFalse( out.validate() );
		assertEquals( "matches", out.errors["prefix"] );
		
		out = new GraphitePath(prefix: ".wpt.", measurand: validMeasurand)
		assertFalse( out.validate() );
		assertEquals( "matches", out.errors["prefix"] );
		
		out = new GraphitePath(prefix: ".wpt", measurand: validMeasurand)
		assertFalse( out.validate() );
		assertEquals( "matches", out.errors["prefix"] ); 
		
		out = new GraphitePath(prefix: "wpt.server.server.wpt..", measurand: validMeasurand)
		assertFalse( out.validate() );
		assertEquals( "matches", out.errors["prefix"] );
		
		out = new GraphitePath(prefix: "wpt.server.server.wpt", measurand: validMeasurand)
		assertFalse( out.validate() );
		assertEquals( "matches", out.errors["prefix"] );
	}
	
	@Test
	void testCreationWithDifferentValidPrefixes() {
		
		// Test-data
		AggregatorType validMeasurand = new AggregatorType(name: AggregatorType.RESULT_UNCACHED_DOC_COMPLETE_TIME, measurandGroup: MeasurandGroup.LOAD_TIMES)
		
		// Mocking domain-class for constraints
		mockForConstraintsTests(GraphitePath)
		
		// Run the tests...
		GraphitePath out = new GraphitePath(prefix: "wpt.", measurand: validMeasurand)
		assertTrue( out.validate() );
		
		out = new GraphitePath(prefix: "w43p43t.", measurand: validMeasurand)
		assertTrue( out.validate() );
		
		out = new GraphitePath(prefix: "wpt.server.", measurand: validMeasurand)
		assertTrue( out.validate() );
		
		out = new GraphitePath(prefix: "wpt.server.server.", measurand: validMeasurand)
		assertTrue( out.validate() );
		
		out = new GraphitePath(prefix: "wpt.server.server.wpt.", measurand: validMeasurand)
		assertTrue( out.validate() );
    }
}

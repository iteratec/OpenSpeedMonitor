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
import de.iteratec.osm.report.chart.DefaultAggregatorTypeDaoService
import grails.test.mixin.*

import org.junit.Before
import org.junit.Test

import de.iteratec.osm.report.chart.AggregatorType
import de.iteratec.osm.report.chart.MeasurandGroup

/**
 * Test-suite of {@link AggregatorTypeDaoService}.
 */
@TestFor(DefaultAggregatorTypeDaoService)
@Mock([AggregatorType])
class DefaultAggregatorTypeDaoServiceTests {

	AggregatorTypeDaoService serviceUnderTest

	@Before
	public void setUp() {
		this.serviceUnderTest = service
	}

	@Test
	public void testFindAll() {
		new AggregatorType(name: AggregatorType.MEASURED_EVENT, measurandGroup: MeasurandGroup.NO_MEASURAND, id: 1).save(failOnError:true);
		new AggregatorType(name: AggregatorType.PAGE, measurandGroup: MeasurandGroup.NO_MEASURAND, id: 2).save(failOnError:true);

		Set<AggregatorType> result = serviceUnderTest.findAll();
		assertNotNull(result);
		assertEquals(2, result.size());
		assertEquals(1, result.count( { it.id == 1 } ));
		assertEquals(1, result.count( { it.id == 2 } ));

		new AggregatorType(name: AggregatorType.SHOP, measurandGroup: MeasurandGroup.NO_MEASURAND, id: 3).save(failOnError:true);
		Set<AggregatorType> resultAfterAdding = serviceUnderTest.findAll();
		assertNotNull(resultAfterAdding);
		assertEquals(3, resultAfterAdding.size());
		assertEquals(1, resultAfterAdding.count( { it.id == 1 } ));
		assertEquals(1, resultAfterAdding.count( { it.id == 2 } ));
		assertEquals(1, resultAfterAdding.count( { it.id == 3 } ));

		try {
			resultAfterAdding.clear();
			fail('Set should be unmodifyable - UnsupportedOperationException expected');
		} catch(UnsupportedOperationException expected) {
			// was expected
		}
	}
	
	@Test
	public void testGetNameToObjectMap(){
		//test-specific data
		new AggregatorType(name: AggregatorType.MEASURED_EVENT, measurandGroup: MeasurandGroup.NO_MEASURAND, id: 1).save(failOnError:true);
		new AggregatorType(name: AggregatorType.PAGE, measurandGroup: MeasurandGroup.NO_MEASURAND, id: 2).save(failOnError:true);
		new AggregatorType(name: AggregatorType.RESULT_CACHED_FULLY_LOADED_TIME, measurandGroup: MeasurandGroup.LOAD_TIMES, id: 3).save(failOnError:true);
		new AggregatorType(name: AggregatorType.RESULT_UNCACHED_DOM_TIME, measurandGroup: MeasurandGroup.LOAD_TIMES, id: 4).save(failOnError:true);
		
		//test-execution
		Map<String, AggregatorType> nameToObjectMap = serviceUnderTest.getNameToObjectMap()
		 
		//assertions
		assertEquals(4, nameToObjectMap.size())
		
		AggregatorType eventAggrFromMap = nameToObjectMap[AggregatorType.MEASURED_EVENT]
		assertEquals(AggregatorType.MEASURED_EVENT, eventAggrFromMap.name)
		assertEquals(MeasurandGroup.NO_MEASURAND, eventAggrFromMap.measurandGroup)
		
		AggregatorType pageAggrFromMap = nameToObjectMap[AggregatorType.PAGE]
		assertEquals(AggregatorType.PAGE, pageAggrFromMap.name)
		assertEquals(MeasurandGroup.NO_MEASURAND, pageAggrFromMap.measurandGroup)
		
		AggregatorType fullyLoadedAggrFromMap = nameToObjectMap[AggregatorType.RESULT_CACHED_FULLY_LOADED_TIME]
		assertEquals(AggregatorType.RESULT_CACHED_FULLY_LOADED_TIME, fullyLoadedAggrFromMap.name)
		assertEquals(MeasurandGroup.LOAD_TIMES, fullyLoadedAggrFromMap.measurandGroup)
		
		AggregatorType domTimeAggrFromMap = nameToObjectMap[AggregatorType.RESULT_UNCACHED_DOM_TIME]
		assertEquals(AggregatorType.RESULT_UNCACHED_DOM_TIME, domTimeAggrFromMap.name)
		assertEquals(MeasurandGroup.LOAD_TIMES, domTimeAggrFromMap.measurandGroup)
	}
}

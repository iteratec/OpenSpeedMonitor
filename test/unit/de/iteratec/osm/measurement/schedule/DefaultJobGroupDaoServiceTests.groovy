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

import de.iteratec.osm.measurement.schedule.dao.JobGroupDaoService
import grails.test.mixin.*

import org.junit.*

/**
 * Test-suite for {@link de.iteratec.osm.measurement.schedule.dao.JobGroupDaoService}.
 */
@TestFor(DefaultJobGroupDaoService)
@Mock([JobGroup])
class DefaultJobGroupDaoServiceTests {

	public static final String nameGroup1OfType1 = 'group1type1'
	public static final String nameGroup2OfType1 = 'group2type1'
	public static final String nameGroup3OfType1 = 'group3type1'
	public static final String nameGroup1OfType2 = 'group1type2'
	public static final String nameGroup2OfType2 = 'group2type2'
	public static final String nameGroup3OfType2 = 'group3type2'
	JobGroupDaoService serviceUnderTest
	
    @Before
	void setUp(){
		serviceUnderTest = service
		createDataCommonForAllTests()
	}
	
	@Test
	void testFindCSIGroups() {
		new JobGroup(name: 'CSI-Group1', groupType: JobGroupType.CSI_AGGREGATION).save(failOnError: true)
		new JobGroup(name: 'CSI-Group2', groupType: JobGroupType.CSI_AGGREGATION).save(failOnError: true)
		new JobGroup(name: 'Another-Group', groupType: JobGroupType.RAW_DATA_SELECTION).save(failOnError: true)
		
		Set<JobGroup> result = serviceUnderTest.findCSIGroups()
		
		assertNotNull(result);
		assertEquals(2, result.size());
		assertEquals(1, result.count( { it.name == 'CSI-Group1' } ));
		assertEquals(1, result.count( { it.name == 'CSI-Group2' } ));
	}
	
	@Test
	void testFindAll() {
		new JobGroup(name: 'Group1', groupType: JobGroupType.CSI_AGGREGATION).save(failOnError: true)
		new JobGroup(name: 'Group2', groupType: JobGroupType.RAW_DATA_SELECTION).save(failOnError: true)
		
		Set<JobGroup> result = serviceUnderTest.findAll();
		
		assertNotNull(result);
		assertEquals(2, result.size());
		assertEquals(1, result.count( { it.name == 'Group1' } ));
		assertEquals(1, result.count( { it.name == 'Group2' } ));
		
		new JobGroup(name: 'Group3', groupType: JobGroupType.CSI_AGGREGATION).save(failOnError: true)
		
		Set<JobGroup> resultAfterAdding = serviceUnderTest.findAll();
		
		assertNotNull(resultAfterAdding);
		assertEquals(3, resultAfterAdding.size());
		assertEquals(1, resultAfterAdding.count( { it.name == 'Group1' } ));
		assertEquals(1, resultAfterAdding.count( { it.name == 'Group2' } ));
		assertEquals(1, resultAfterAdding.count( { it.name == 'Group3' } ));
	}
	
	@Test
    void testGetIdToObjectMap() {
		
		//create test-specific data
		
		//group type 1
		JobGroup group1OfType1 = new JobGroup(name: nameGroup1OfType1, groupType: JobGroupType.CSI_AGGREGATION).save(failOnError: true)
		JobGroup group2OfType1 = new JobGroup(name: nameGroup2OfType1, groupType: JobGroupType.CSI_AGGREGATION).save(failOnError: true)
		JobGroup group3OfType1 = new JobGroup(name: nameGroup3OfType1, groupType: JobGroupType.CSI_AGGREGATION).save(failOnError: true)
		//group type 2
		JobGroup group1OfType2 = new JobGroup(name: nameGroup1OfType2, groupType: JobGroupType.RAW_DATA_SELECTION).save(failOnError: true)
		JobGroup group2OfType2 = new JobGroup(name: nameGroup2OfType2, groupType: JobGroupType.RAW_DATA_SELECTION).save(failOnError: true)
		JobGroup group3OfType2 = new JobGroup(name: nameGroup3OfType2, groupType: JobGroupType.RAW_DATA_SELECTION).save(failOnError: true)
		
		//execute test
		
		Map<Long, JobGroup> idToObjectMap = serviceUnderTest.getIdToObjectMap()
		
		//assertions
		
		assertEquals(
			[
				(group1OfType1.ident()) : group1OfType1,
				(group2OfType1.ident()) : group2OfType1,
				(group3OfType1.ident()) : group3OfType1,
				(group1OfType2.ident()) : group1OfType2,
				(group2OfType2.ident()) : group2OfType2,
				(group3OfType2.ident()) : group3OfType2
				], 
			idToObjectMap)
    }
	private void createDataCommonForAllTests(){
		//nothing to do yet
	}
}

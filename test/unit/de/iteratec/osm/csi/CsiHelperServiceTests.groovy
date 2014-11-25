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

import org.joda.time.DateTime
import org.junit.*

import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.measurement.schedule.JobGroupType
import de.iteratec.osm.report.chart.MeasuredValue
import de.iteratec.osm.report.chart.MeasuredValueInterval

/**
 * Test-suite of {@link CsiHelperService}.
 */
@TestFor(CsiHelperService)
@Mock([MeasuredValue, JobGroup])
class CsiHelperServiceTests {
	
	CsiHelperService serviceUnderTest
	
	@Before
	void setUp() {
		serviceUnderTest=service
	}

	@Test
    void testResetToStartOfActualInterval() {
		DateTime now = new DateTime(2013, 1, 10, 13, 43, 12, 234)
		
		DateTime hourlyReseted = new DateTime(2013, 1, 10, 13, 0, 0, 0)
		assertEquals(hourlyReseted, serviceUnderTest.resetToStartOfActualInterval(now, MeasuredValueInterval.HOURLY))
		
		DateTime dailyReseted = new DateTime(2013, 1, 10, 0, 0, 0, 0)
		assertEquals(dailyReseted, serviceUnderTest.resetToStartOfActualInterval(now, MeasuredValueInterval.DAILY))
		
		DateTime weeklyReseted = new DateTime(2013, 1, 7, 0, 0, 0, 0)
		assertEquals(weeklyReseted, serviceUnderTest.resetToStartOfActualInterval(now, MeasuredValueInterval.WEEKLY))
    }
	
	@Test
	void testResetToEndOfActualInterval() {
		DateTime dateTime = new DateTime(2013, 1, 10, 13, 43, 12)
		DateTime hourlyReseted = new DateTime(2013, 1, 10, 14, 0, 0, 0)
		DateTime dailyReseted = new DateTime(2013, 1, 11, 0, 0, 0, 0)
		DateTime weeklyReseted = new DateTime(2013, 1, 14, 0, 0, 0, 0)
		
		assertEquals(hourlyReseted, serviceUnderTest.resetToEndOfActualInterval(dateTime, MeasuredValueInterval.HOURLY))
		assertEquals(dailyReseted, serviceUnderTest.resetToEndOfActualInterval(dateTime, MeasuredValueInterval.DAILY))
		assertEquals(weeklyReseted, serviceUnderTest.resetToEndOfActualInterval(dateTime, MeasuredValueInterval.WEEKLY))
	}
	
	@Test
	void testGetCsiGroups(){
		List<JobGroup> csiGroups = serviceUnderTest.getCsiJobGroups()
		assertEquals(0, csiGroups.size())
		
		String csiGroupName = 'CSI'
		new JobGroup(
				name:csiGroupName,
				groupType: JobGroupType.CSI_AGGREGATION).save(failOnError: true)
		csiGroupName = 'CSI Lhotse'
		new JobGroup(
				name:csiGroupName,
				groupType: JobGroupType.CSI_AGGREGATION).save(failOnError: true)
				
		csiGroups = serviceUnderTest.getCsiJobGroups()
		assertEquals(2, csiGroups.size())
	}
}

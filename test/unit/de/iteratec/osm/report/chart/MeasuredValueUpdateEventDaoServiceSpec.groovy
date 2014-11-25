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

import static org.hamcrest.CoreMatchers.*
import static org.hamcrest.Matchers.*
import static org.junit.Assert.assertThat

import grails.test.mixin.*

import org.joda.time.DateTime
import org.junit.*

/**
 * See the API for {@link grails.test.mixin.services.ServiceUnitTestMixin} for usage instructions
 */
@TestFor(MeasuredValueUpdateEventDaoService)
@Mock([MeasuredValueUpdateEvent])
class MeasuredValueUpdateEventDaoServiceSpec {

	MeasuredValueUpdateEventDaoService serviceUnderTest
	
	@Before
	public void setUp(){
		serviceUnderTest = service
	} 
	
	@Test
    void testWritingUpdateEvents() {
		
		//test-specific data
		Long idOutdatedMeasuredValue = 1
		Long idCalculatedMeasuredValue = 1
		DateTime oneMinuteAgo = new DateTime().minusMinutes(1)
		
		//test-execution
		serviceUnderTest.createUpdateEvent(idOutdatedMeasuredValue, MeasuredValueUpdateEvent.UpdateCause.OUTDATED)
		serviceUnderTest.createUpdateEvent(idCalculatedMeasuredValue, MeasuredValueUpdateEvent.UpdateCause.CALCULATED)
		
		//assertions
		List<MeasuredValueUpdateEvent> updateEvents = MeasuredValueUpdateEvent.list()
		assertThat(updateEvents.size(), is(2))
		
		MeasuredValueUpdateEvent outdatedEvent = updateEvents.find{ it.updateCause == MeasuredValueUpdateEvent.UpdateCause.OUTDATED }
		assertNotNull(outdatedEvent)
		assertThat(outdatedEvent.measuredValueId, is(idOutdatedMeasuredValue))
		boolean updateEventNewerThanOneMinute = new DateTime(outdatedEvent.dateOfUpdate).isAfter(oneMinuteAgo)
		assertTrue(updateEventNewerThanOneMinute)
		
		MeasuredValueUpdateEvent calculatedEvent = updateEvents.find{ it.updateCause == MeasuredValueUpdateEvent.UpdateCause.CALCULATED }
		assertNotNull(calculatedEvent)
		assertThat(calculatedEvent.measuredValueId, is(idCalculatedMeasuredValue))
		updateEventNewerThanOneMinute = new DateTime(calculatedEvent.dateOfUpdate).isAfter(oneMinuteAgo) 
		assertTrue(updateEventNewerThanOneMinute)
    }
}

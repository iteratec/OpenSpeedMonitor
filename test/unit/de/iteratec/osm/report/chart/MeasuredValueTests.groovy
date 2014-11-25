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

import static org.junit.Assert.*
import static org.hamcrest.CoreMatchers.*
import static org.hamcrest.Matchers.*
import static org.junit.Assert.assertThat
import grails.test.mixin.*
import grails.test.mixin.support.*

import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.junit.*

import de.iteratec.osm.report.chart.AggregatorType
import de.iteratec.osm.report.chart.MeasurandGroup
import de.iteratec.osm.report.chart.MeasuredValue
import de.iteratec.osm.report.chart.MeasuredValueInterval
import de.iteratec.osm.report.chart.MeasuredValueUpdateEvent

/**
 * Test-suite for {@link MeasuredValue}.
 */
@TestMixin(GrailsUnitTestMixin)
@Mock([MeasuredValue, AggregatorType, MeasuredValueInterval, MeasuredValueUpdateEvent])
class MeasuredValueTests {
	
	Date dateOfMv1
	Date dateOfMv2
	AggregatorType measuredEvent, page, shop
	MeasuredValueInterval hourly, daily, weekly

    void setUp() {
		measuredEvent = AggregatorType.findByName(AggregatorType.MEASURED_EVENT) ?: new AggregatorType(
			name: AggregatorType.MEASURED_EVENT, measurandGroup: MeasurandGroup.NO_MEASURAND).save(failOnError: true)
		page = AggregatorType.findByName(AggregatorType.PAGE) ?: new AggregatorType(
			name: AggregatorType.PAGE, measurandGroup: MeasurandGroup.NO_MEASURAND).save(failOnError: true)
		shop = AggregatorType.findByName(AggregatorType.SHOP) ?: new AggregatorType(
			name: AggregatorType.SHOP, measurandGroup: MeasurandGroup.NO_MEASURAND).save(failOnError: true)
		hourly = MeasuredValueInterval.findByIntervalInMinutes(MeasuredValueInterval.HOURLY) ?: new MeasuredValueInterval(
			name: "hourly",
			intervalInMinutes: MeasuredValueInterval.HOURLY
			).save(failOnError: true)
		daily  = MeasuredValueInterval.findByIntervalInMinutes(MeasuredValueInterval.DAILY) ?: new MeasuredValueInterval(
			name: "daily",
			intervalInMinutes: MeasuredValueInterval.DAILY
			).save(failOnError: true)
		weekly = MeasuredValueInterval.findByIntervalInMinutes(MeasuredValueInterval.WEEKLY) ?: new MeasuredValueInterval(
			name: "weekly",
			intervalInMinutes: MeasuredValueInterval.WEEKLY
			).save(failOnError: true)
		
		dateOfMv1 = new DateTime(2012,1,1,0,0, DateTimeZone.UTC).toDate()
		dateOfMv2 = new DateTime(2012,1,2,0,0, DateTimeZone.UTC).toDate()
		new MeasuredValue(
			started: dateOfMv1,
			interval: hourly,
			aggregator: measuredEvent,
			tag: '1;1;1;1;1',
			resultIds: ''
		).save(failOnError: true)
		new MeasuredValue(
			started: dateOfMv2,
			interval: hourly,
			aggregator: measuredEvent,
			tag: '1;1;1;1;1',
			resultIds: '1,2'
		).save(failOnError: true)
    }
	
	@Test
    void testAccessingResultIds() {
		MeasuredValue mvInitialWithoutResultids = MeasuredValue.findByStarted(dateOfMv1)
		
		assertEquals(0, mvInitialWithoutResultids.countResultIds())
		assertEquals(0, mvInitialWithoutResultids.getResultIdsAsList().size())
		
		mvInitialWithoutResultids.addToResultIds(3)
		assertEquals(1, mvInitialWithoutResultids.countResultIds())
		assertEquals(1, mvInitialWithoutResultids.getResultIdsAsList().size())
		
		MeasuredValue mvWith2Results = MeasuredValue.findByStarted(dateOfMv2)
		assertEquals(2,  mvWith2Results.countResultIds())
		assertEquals(2, mvWith2Results.getResultIdsAsList().size())
		
		mvInitialWithoutResultids.addAllToResultIds(mvWith2Results.getResultIdsAsList())
		assertEquals(3, mvInitialWithoutResultids.getResultIdsAsList().size())
		
		mvInitialWithoutResultids.addAllToResultIds(mvWith2Results.resultIds)
		assertEquals(5,  mvInitialWithoutResultids.countResultIds())
		assertEquals(5, mvInitialWithoutResultids.getResultIdsAsList().size())
		
		mvInitialWithoutResultids.clearResultIds()
		assertEquals(0,  mvInitialWithoutResultids.countResultIds())
		assertEquals(0, mvInitialWithoutResultids.getResultIdsAsList().size())
		
    }
	
	/**
	 * This test adds duplicate {@link EventResult}-ID's to {@link MeasuredValue}. This shouldn't occur in production.
	 * We had a bug with this (see https://seu.hh.iteratec.de:8444/browse/IT-381) and decided to remove persistence of result_ids-String for daily and weekly-aggregated {@link MeasuredValue}s.
	 * If duplicate ID's get persisted we just log an error but don't throw an exception, cause the error just arose with daily and/or weekly-result_ids-Strings and didn't had any productive impact. 
	 * So this test just guarantees, that duplicate result-ids get persisted, although this doesn't make much sense.
	 */
	@Test
	void testAccessingResultIdsWhileAddingDuplicateIds() {
		
		//test specific data/////////////////////////////////////////////////////
		
		MeasuredValue mvInitialWithoutResultids = MeasuredValue.findByStarted(dateOfMv1)
		
		//test execution and assertions/////////////////////////////////////////////////////
		
		//precondition
		mvInitialWithoutResultids.clearResultIds()
		assertEquals(0, mvInitialWithoutResultids.countResultIds())
		assertEquals(0, mvInitialWithoutResultids.getResultIdsAsList().size())
		//adding single resulit-id's NO DUPLICATES
		mvInitialWithoutResultids.addToResultIds(1)
		mvInitialWithoutResultids.addToResultIds(2)
		mvInitialWithoutResultids.addToResultIds(3)
		assertEquals(3, mvInitialWithoutResultids.countResultIds())
		//adding single resulit-id's DUPLICATES
		mvInitialWithoutResultids.addToResultIds(3)
		mvInitialWithoutResultids.addToResultIds(4)
		assertEquals(5, mvInitialWithoutResultids.countResultIds())
		//adding long-list of resulit-id's NO DUPLICATES
		mvInitialWithoutResultids.addAllToResultIds([5,6,7])
		assertEquals(8, mvInitialWithoutResultids.countResultIds())
		//adding long-list of resulit-id's DUPLICATES
		mvInitialWithoutResultids.addAllToResultIds([5,6,7,8])
		assertEquals(12, mvInitialWithoutResultids.countResultIds())
		//adding string-list of resulit-id's NO DUPLICATES
		mvInitialWithoutResultids.addAllToResultIds('9,10,11')
		assertEquals(15, mvInitialWithoutResultids.countResultIds())
		//adding string-list of resulit-id's DUPLICATES
		mvInitialWithoutResultids.addAllToResultIds('11,12,13')
		assertEquals(18, mvInitialWithoutResultids.countResultIds())
	}
	
	@Test
	public void testHasToBeCalculatedWithoutUpdateEvents() {
		//create test-specific data
		MeasuredValue mv = new MeasuredValue().save(validate: false)
		//test
		assertThat(mv.hasToBeCalculated(), is(true))
	}
	
	@Test
	public void testHasToBeCalculatedLastUpdateEventOutdated() {
		//create test-specific data
		MeasuredValue mv = new MeasuredValue().save(validate: false)
		Long mvId = mv.ident()
		Date timestamp1 = new DateTime(2014,6,25,0,1,0).toDate()
		Date timestamp2 = new DateTime(2014,6,25,0,2,0).toDate()
		Date timestamp3 = new DateTime(2014,6,25,0,3,0).toDate()
		Date timestamp4 = new DateTime(2014,6,25,0,4,0).toDate()
		
		//creation of update events and test
		new MeasuredValueUpdateEvent(
			dateOfUpdate: timestamp1,
			measuredValueId: mvId,
			updateCause: MeasuredValueUpdateEvent.UpdateCause.OUTDATED
		).save(failOnError: true)
		assertThat(mv.hasToBeCalculated(), is(true))
		
		new MeasuredValueUpdateEvent(
			dateOfUpdate: timestamp2,
			measuredValueId: mvId,
			updateCause: MeasuredValueUpdateEvent.UpdateCause.CALCULATED
		).save(failOnError: true)
		new MeasuredValueUpdateEvent(
			dateOfUpdate: timestamp3,
			measuredValueId: mvId,
			updateCause: MeasuredValueUpdateEvent.UpdateCause.OUTDATED
		).save(failOnError: true)
		assertThat(mv.hasToBeCalculated(), is(true))
		
		new MeasuredValueUpdateEvent(
			dateOfUpdate: timestamp4,
			measuredValueId: mvId,
			updateCause: MeasuredValueUpdateEvent.UpdateCause.OUTDATED
		).save(failOnError: true)
		assertThat(mv.hasToBeCalculated(), is(true))
	}
	
	@Test
	public void testHasToBeCalculatedLastUpdateEventCalculated() {
		//create test-specific data
		MeasuredValue mv = new MeasuredValue().save(validate: false)
		Long mvId = mv.ident()
		Date timestamp1 = new DateTime(2014,6,25,0,1,0).toDate()
		Date timestamp2 = new DateTime(2014,6,25,0,2,0).toDate()
		Date timestamp3 = new DateTime(2014,6,25,0,3,0).toDate()
		Date timestamp4 = new DateTime(2014,6,25,0,4,0).toDate()
		
		//creation of update events and test
		new MeasuredValueUpdateEvent(
			dateOfUpdate: timestamp1,
			measuredValueId: mvId,
			updateCause: MeasuredValueUpdateEvent.UpdateCause.CALCULATED
		).save(failOnError: true)
		assertThat(mv.hasToBeCalculated(), is(false))
		
		new MeasuredValueUpdateEvent(
			dateOfUpdate: timestamp2,
			measuredValueId: mvId,
			updateCause: MeasuredValueUpdateEvent.UpdateCause.OUTDATED
		).save(failOnError: true)
		new MeasuredValueUpdateEvent(
			dateOfUpdate: timestamp3,
			measuredValueId: mvId,
			updateCause: MeasuredValueUpdateEvent.UpdateCause.CALCULATED
		).save(failOnError: true)
		assertThat(mv.hasToBeCalculated(), is(false))
		
		new MeasuredValueUpdateEvent(
			dateOfUpdate: timestamp3,
			measuredValueId: mvId,
			updateCause: MeasuredValueUpdateEvent.UpdateCause.CALCULATED
		).save(failOnError: true)
		assertThat(mv.hasToBeCalculated(), is(false))
	}
	
	@Test
	public void testHasToBeCalculatedAccordingEvents_WithoutUpdateEvents(){
		//create test-specific data
		MeasuredValue mv = new MeasuredValue().save(validate: false)
		Long mvId = mv.ident()
		Date timestamp1 = new DateTime(2014,6,25,0,1,0).toDate()
		MeasuredValueUpdateEvent calculationOfOtherMeasuredValue = new MeasuredValueUpdateEvent(
			dateOfUpdate: timestamp1,
			measuredValueId: mvId+1,
			updateCause: MeasuredValueUpdateEvent.UpdateCause.CALCULATED
		)
		//tests
		assertThat(mv.hasToBeCalculatedAccordingEvents([]), is(true))
		assertThat(mv.hasToBeCalculatedAccordingEvents([calculationOfOtherMeasuredValue]), is(true))
	}
	
	@Test
	public void testHasToBeCalculatedAccordingEvents_LastUpdateEventOutdated(){
		
		//create test-specific data
		
		MeasuredValue mv = new MeasuredValue().save(validate: false)
		Long mvId = mv.ident()
		Date timestamp1 = new DateTime(2014,6,25,0,1,0).toDate()
		Date timestamp2 = new DateTime(2014,6,25,0,2,0).toDate()
		Date timestamp3 = new DateTime(2014,6,25,0,3,0).toDate()
		Date timestamp4 = new DateTime(2014,6,25,0,4,0).toDate()
		Date timestamp5 = new DateTime(2014,6,25,0,5,0).toDate()
		
		MeasuredValueUpdateEvent outdated1 = new MeasuredValueUpdateEvent(
			dateOfUpdate: timestamp1,
			measuredValueId: mvId,
			updateCause: MeasuredValueUpdateEvent.UpdateCause.OUTDATED
		)
		MeasuredValueUpdateEvent calculated1 = new MeasuredValueUpdateEvent(
			dateOfUpdate: timestamp2,
			measuredValueId: mvId,
			updateCause: MeasuredValueUpdateEvent.UpdateCause.CALCULATED
		)
		MeasuredValueUpdateEvent outdated2 = new MeasuredValueUpdateEvent(
			dateOfUpdate: timestamp3,
			measuredValueId: mvId,
			updateCause: MeasuredValueUpdateEvent.UpdateCause.OUTDATED
		)
		MeasuredValueUpdateEvent outdated3 = new MeasuredValueUpdateEvent(
			dateOfUpdate: timestamp4,
			measuredValueId: mvId,
			updateCause: MeasuredValueUpdateEvent.UpdateCause.OUTDATED
		)
		MeasuredValueUpdateEvent calculationOfOtherMeasuredValue = new MeasuredValueUpdateEvent(
			dateOfUpdate: timestamp5,
			measuredValueId: mvId+1,
			updateCause: MeasuredValueUpdateEvent.UpdateCause.CALCULATED
		)
		List<MeasuredValueUpdateEvent> events = [calculationOfOtherMeasuredValue]
		
		//test
		
		events.add(outdated1)
		assertThat(mv.hasToBeCalculatedAccordingEvents(events), is(true))
		
		events.add(calculated1)
		events.add(outdated2)
		assertThat(mv.hasToBeCalculatedAccordingEvents(events), is(true))
		
		events.add(outdated3)
		assertThat(mv.hasToBeCalculatedAccordingEvents(events), is(true))
	}
	
	@Test
	public void testHasToBeCalculatedAccordingEvents_LastUpdateEventCalculated(){
		
		//create test-specific data
		
		MeasuredValue mv = new MeasuredValue().save(validate: false)
		Long mvId = mv.ident()
		Date timestamp1 = new DateTime(2014,6,25,0,1,0).toDate()
		Date timestamp2 = new DateTime(2014,6,25,0,2,0).toDate()
		Date timestamp3 = new DateTime(2014,6,25,0,3,0).toDate()
		Date timestamp4 = new DateTime(2014,6,25,0,4,0).toDate()
		Date timestamp5 = new DateTime(2014,6,25,0,5,0).toDate()
		
		MeasuredValueUpdateEvent calculated1 = new MeasuredValueUpdateEvent(
			dateOfUpdate: timestamp1,
			measuredValueId: mvId,
			updateCause: MeasuredValueUpdateEvent.UpdateCause.CALCULATED
		)
		MeasuredValueUpdateEvent outdated1 = new MeasuredValueUpdateEvent(
			dateOfUpdate: timestamp2,
			measuredValueId: mvId,
			updateCause: MeasuredValueUpdateEvent.UpdateCause.OUTDATED
		)
		MeasuredValueUpdateEvent calculated2 = new MeasuredValueUpdateEvent(
			dateOfUpdate: timestamp3,
			measuredValueId: mvId,
			updateCause: MeasuredValueUpdateEvent.UpdateCause.CALCULATED
		)
		MeasuredValueUpdateEvent calculated3 = new MeasuredValueUpdateEvent(
			dateOfUpdate: timestamp4,
			measuredValueId: mvId,
			updateCause: MeasuredValueUpdateEvent.UpdateCause.CALCULATED
		)
		MeasuredValueUpdateEvent outdatedOfOtherMeasuredValue = new MeasuredValueUpdateEvent(
			dateOfUpdate: timestamp5,
			measuredValueId: mvId+1,
			updateCause: MeasuredValueUpdateEvent.UpdateCause.OUTDATED
		)
		List<MeasuredValueUpdateEvent> events = [outdatedOfOtherMeasuredValue]
		
		//test
		
		events.add(calculated1)
		assertThat(mv.hasToBeCalculatedAccordingEvents(events), is(false))
		
		events.add(outdated1)
		events.add(calculated2)
		assertThat(mv.hasToBeCalculatedAccordingEvents(events), is(false))
		
		events.add(calculated3)
		assertThat(mv.hasToBeCalculatedAccordingEvents(events), is(false))
	}
}

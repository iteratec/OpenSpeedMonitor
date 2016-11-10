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

import de.iteratec.osm.csi.Page
import de.iteratec.osm.measurement.environment.Browser
import de.iteratec.osm.measurement.environment.Location
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.result.MeasuredEvent
import grails.test.mixin.Mock
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.junit.Test

import static org.hamcrest.Matchers.is
import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertThat

/**
 * Test-suite for {@link CsiAggregation}.
 */
@TestMixin(GrailsUnitTestMixin)
@Mock([CsiAggregation, AggregatorType, CsiAggregationInterval, CsiAggregationUpdateEvent, JobGroup, MeasuredEvent, Page, Browser, Location])
class CsiAggregationTests {

    Date dateOfMv1
    Date dateOfMv2
    AggregatorType measuredEventAggregator, pageAggregator, shopAggregator
    CsiAggregationInterval hourly, daily, weekly

    void setUp() {
        measuredEventAggregator = AggregatorType.findByName(AggregatorType.MEASURED_EVENT) ?: new AggregatorType(
                name: AggregatorType.MEASURED_EVENT, measurandGroup: MeasurandGroup.NO_MEASURAND).save(failOnError: true)
        pageAggregator = AggregatorType.findByName(AggregatorType.PAGE) ?: new AggregatorType(
                name: AggregatorType.PAGE, measurandGroup: MeasurandGroup.NO_MEASURAND).save(failOnError: true)
        shopAggregator = AggregatorType.findByName(AggregatorType.SHOP) ?: new AggregatorType(
                name: AggregatorType.SHOP, measurandGroup: MeasurandGroup.NO_MEASURAND).save(failOnError: true)
        hourly = CsiAggregationInterval.findByIntervalInMinutes(CsiAggregationInterval.HOURLY) ?: new CsiAggregationInterval(
                name: "hourly",
                intervalInMinutes: CsiAggregationInterval.HOURLY
        ).save(failOnError: true)
        daily = CsiAggregationInterval.findByIntervalInMinutes(CsiAggregationInterval.DAILY) ?: new CsiAggregationInterval(
                name: "daily",
                intervalInMinutes: CsiAggregationInterval.DAILY
        ).save(failOnError: true)
        weekly = CsiAggregationInterval.findByIntervalInMinutes(CsiAggregationInterval.WEEKLY) ?: new CsiAggregationInterval(
                name: "weekly",
                intervalInMinutes: CsiAggregationInterval.WEEKLY
        ).save(failOnError: true)

        dateOfMv1 = new DateTime(2012, 1, 1, 0, 0, DateTimeZone.UTC).toDate()
        dateOfMv2 = new DateTime(2012, 1, 2, 0, 0, DateTimeZone.UTC).toDate()

        JobGroup jobGroup = new JobGroup(name: "unusedJobGroup").save(validate: false)
        Browser browser = new Browser(name: "unusedBrowser").save(validate: false)
        Location location = new Location(location: "unusedLocation").save(validate: false)
        Page page = new Page(name: "unusedPage").save(validate: false)
        MeasuredEvent measuredEvent = new MeasuredEvent(name: "unusedMeasuredEvent").save(validate: false)

        new CsiAggregation(
                started: dateOfMv1,
                interval: hourly,
                aggregator: measuredEventAggregator,
                jobGroup: jobGroup,
                measuredEvent: measuredEvent,
                page: page,
                browser: browser,
                location: location,
                underlyingEventResultsByWptDocComplete: ''
        ).save(failOnError: true)
        new CsiAggregation(
                started: dateOfMv2,
                interval: hourly,
                aggregator: measuredEventAggregator,
                jobGroup: jobGroup,
                measuredEvent: measuredEvent,
                page: page,
                browser: browser,
                location: location,
                underlyingEventResultsByWptDocComplete: '1,2'
        ).save(failOnError: true)
    }

    @Test
    void testAccessingResultIds() {
        CsiAggregation mvInitialWithoutResultids = CsiAggregation.findByStarted(dateOfMv1)

        assertEquals(0, mvInitialWithoutResultids.countUnderlyingEventResultsByWptDocComplete())
        assertEquals(0, mvInitialWithoutResultids.getUnderlyingEventResultsByWptDocCompleteAsList().size())

        mvInitialWithoutResultids.addToUnderlyingEventResultsByWptDocComplete(3)
        assertEquals(1, mvInitialWithoutResultids.countUnderlyingEventResultsByWptDocComplete())
        assertEquals(1, mvInitialWithoutResultids.getUnderlyingEventResultsByWptDocCompleteAsList().size())

        CsiAggregation mvWith2Results = CsiAggregation.findByStarted(dateOfMv2)
        assertEquals(2, mvWith2Results.countUnderlyingEventResultsByWptDocComplete())
        assertEquals(2, mvWith2Results.getUnderlyingEventResultsByWptDocCompleteAsList().size())

        mvInitialWithoutResultids.addAllToUnderlyingEventResultsByWptDocComplete(mvWith2Results.getUnderlyingEventResultsByWptDocCompleteAsList())
        assertEquals(3, mvInitialWithoutResultids.getUnderlyingEventResultsByWptDocCompleteAsList().size())

        mvInitialWithoutResultids.addAllToResultIds(mvWith2Results.underlyingEventResultsByWptDocComplete)
        assertEquals(5, mvInitialWithoutResultids.countUnderlyingEventResultsByWptDocComplete())
        assertEquals(5, mvInitialWithoutResultids.getUnderlyingEventResultsByWptDocCompleteAsList().size())

        mvInitialWithoutResultids.clearUnderlyingEventResultsByWptDocComplete()
        assertEquals(0, mvInitialWithoutResultids.countUnderlyingEventResultsByWptDocComplete())
        assertEquals(0, mvInitialWithoutResultids.getUnderlyingEventResultsByWptDocCompleteAsList().size())

    }

    /**
     * This test adds duplicate {@link EventResult}-ID's to {@link CsiAggregation}. This shouldn't occur in production.
     * We had a bug with this (see https://seu.hh.iteratec.de:8444/browse/IT-381) and decided to remove persistence of result_ids-String for daily and weekly-aggregated {@link CsiAggregation}s.
     * If duplicate ID's get persisted we just log an error but don't throw an exception, cause the error just arose with daily and/or weekly-result_ids-Strings and didn't had any productive impact.
     * So this test just guarantees, that duplicate result-ids get persisted, although this doesn't make much sense.
     */
    @Test
    void testAccessingResultIdsWhileAddingDuplicateIds() {

        //test specific data/////////////////////////////////////////////////////

        CsiAggregation mvInitialWithoutResultids = CsiAggregation.findByStarted(dateOfMv1)

        //test execution and assertions/////////////////////////////////////////////////////

        //precondition
        mvInitialWithoutResultids.clearUnderlyingEventResultsByWptDocComplete()
        assertEquals(0, mvInitialWithoutResultids.countUnderlyingEventResultsByWptDocComplete())
        assertEquals(0, mvInitialWithoutResultids.getUnderlyingEventResultsByWptDocCompleteAsList().size())
        //adding single resulit-id's NO DUPLICATES
        mvInitialWithoutResultids.addToUnderlyingEventResultsByWptDocComplete(1)
        mvInitialWithoutResultids.addToUnderlyingEventResultsByWptDocComplete(2)
        mvInitialWithoutResultids.addToUnderlyingEventResultsByWptDocComplete(3)
        assertEquals(3, mvInitialWithoutResultids.countUnderlyingEventResultsByWptDocComplete())
        //adding single resulit-id's DUPLICATES
        mvInitialWithoutResultids.addToUnderlyingEventResultsByWptDocComplete(3)
        mvInitialWithoutResultids.addToUnderlyingEventResultsByWptDocComplete(4)
        assertEquals(5, mvInitialWithoutResultids.countUnderlyingEventResultsByWptDocComplete())
        //adding long-list of resulit-id's NO DUPLICATES
        mvInitialWithoutResultids.addAllToUnderlyingEventResultsByWptDocComplete([5, 6, 7])
        assertEquals(8, mvInitialWithoutResultids.countUnderlyingEventResultsByWptDocComplete())
        //adding long-list of resulit-id's DUPLICATES
        mvInitialWithoutResultids.addAllToUnderlyingEventResultsByWptDocComplete([5, 6, 7, 8])
        assertEquals(12, mvInitialWithoutResultids.countUnderlyingEventResultsByWptDocComplete())
        //adding string-list of resulit-id's NO DUPLICATES
        mvInitialWithoutResultids.addAllToResultIds('9,10,11')
        assertEquals(15, mvInitialWithoutResultids.countUnderlyingEventResultsByWptDocComplete())
        //adding string-list of resulit-id's DUPLICATES
        mvInitialWithoutResultids.addAllToResultIds('11,12,13')
        assertEquals(18, mvInitialWithoutResultids.countUnderlyingEventResultsByWptDocComplete())
    }

    @Test
    public void testHasToBeCalculatedWithoutUpdateEvents() {
        //create test-specific data
        CsiAggregation mv = new CsiAggregation().save(validate: false)
        //test
        assertThat(mv.hasToBeCalculated(), is(true))
    }

    @Test
    public void testHasToBeCalculatedLastUpdateEventOutdated() {
        //create test-specific data
        CsiAggregation mv = new CsiAggregation().save(validate: false)
        Long mvId = mv.ident()
        Date timestamp1 = new DateTime(2014, 6, 25, 0, 1, 0).toDate()
        Date timestamp2 = new DateTime(2014, 6, 25, 0, 2, 0).toDate()
        Date timestamp3 = new DateTime(2014, 6, 25, 0, 3, 0).toDate()
        Date timestamp4 = new DateTime(2014, 6, 25, 0, 4, 0).toDate()

        //creation of update events and test
        new CsiAggregationUpdateEvent(
                dateOfUpdate: timestamp1,
                csiAggregationId: mvId,
                updateCause: CsiAggregationUpdateEvent.UpdateCause.OUTDATED
        ).save(failOnError: true)
        assertThat(mv.hasToBeCalculated(), is(true))

        new CsiAggregationUpdateEvent(
                dateOfUpdate: timestamp2,
                csiAggregationId: mvId,
                updateCause: CsiAggregationUpdateEvent.UpdateCause.CALCULATED
        ).save(failOnError: true)
        new CsiAggregationUpdateEvent(
                dateOfUpdate: timestamp3,
                csiAggregationId: mvId,
                updateCause: CsiAggregationUpdateEvent.UpdateCause.OUTDATED
        ).save(failOnError: true)
        assertThat(mv.hasToBeCalculated(), is(true))

        new CsiAggregationUpdateEvent(
                dateOfUpdate: timestamp4,
                csiAggregationId: mvId,
                updateCause: CsiAggregationUpdateEvent.UpdateCause.OUTDATED
        ).save(failOnError: true)
        assertThat(mv.hasToBeCalculated(), is(true))
    }

    @Test
    public void testHasToBeCalculatedLastUpdateEventCalculated() {
        //create test-specific data
        CsiAggregation mv = new CsiAggregation().save(validate: false)
        Long mvId = mv.ident()
        Date timestamp1 = new DateTime(2014, 6, 25, 0, 1, 0).toDate()
        Date timestamp2 = new DateTime(2014, 6, 25, 0, 2, 0).toDate()
        Date timestamp3 = new DateTime(2014, 6, 25, 0, 3, 0).toDate()
        Date timestamp4 = new DateTime(2014, 6, 25, 0, 4, 0).toDate()

        //creation of update events and test
        new CsiAggregationUpdateEvent(
                dateOfUpdate: timestamp1,
                csiAggregationId: mvId,
                updateCause: CsiAggregationUpdateEvent.UpdateCause.CALCULATED
        ).save(failOnError: true)
        assertThat(mv.hasToBeCalculated(), is(false))

        new CsiAggregationUpdateEvent(
                dateOfUpdate: timestamp2,
                csiAggregationId: mvId,
                updateCause: CsiAggregationUpdateEvent.UpdateCause.OUTDATED
        ).save(failOnError: true)
        new CsiAggregationUpdateEvent(
                dateOfUpdate: timestamp3,
                csiAggregationId: mvId,
                updateCause: CsiAggregationUpdateEvent.UpdateCause.CALCULATED
        ).save(failOnError: true)
        assertThat(mv.hasToBeCalculated(), is(false))

        new CsiAggregationUpdateEvent(
                dateOfUpdate: timestamp3,
                csiAggregationId: mvId,
                updateCause: CsiAggregationUpdateEvent.UpdateCause.CALCULATED
        ).save(failOnError: true)
        assertThat(mv.hasToBeCalculated(), is(false))
    }

    @Test
    public void testHasToBeCalculatedAccordingEvents_WithoutUpdateEvents() {
        //create test-specific data
        CsiAggregation mv = new CsiAggregation().save(validate: false)
        Long mvId = mv.ident()
        Date timestamp1 = new DateTime(2014, 6, 25, 0, 1, 0).toDate()
        CsiAggregationUpdateEvent calculationOfOtherCsiAggregation = new CsiAggregationUpdateEvent(
                dateOfUpdate: timestamp1,
                csiAggregationId: mvId + 1,
                updateCause: CsiAggregationUpdateEvent.UpdateCause.CALCULATED
        )
        //tests
        assertThat(mv.hasToBeCalculatedAccordingEvents([]), is(true))
        assertThat(mv.hasToBeCalculatedAccordingEvents([calculationOfOtherCsiAggregation]), is(true))
    }

    @Test
    public void testHasToBeCalculatedAccordingEvents_LastUpdateEventOutdated() {

        //create test-specific data

        CsiAggregation mv = new CsiAggregation().save(validate: false)
        Long mvId = mv.ident()
        Date timestamp1 = new DateTime(2014, 6, 25, 0, 1, 0).toDate()
        Date timestamp2 = new DateTime(2014, 6, 25, 0, 2, 0).toDate()
        Date timestamp3 = new DateTime(2014, 6, 25, 0, 3, 0).toDate()
        Date timestamp4 = new DateTime(2014, 6, 25, 0, 4, 0).toDate()
        Date timestamp5 = new DateTime(2014, 6, 25, 0, 5, 0).toDate()

        CsiAggregationUpdateEvent outdated1 = new CsiAggregationUpdateEvent(
                dateOfUpdate: timestamp1,
                csiAggregationId: mvId,
                updateCause: CsiAggregationUpdateEvent.UpdateCause.OUTDATED
        )
        CsiAggregationUpdateEvent calculated1 = new CsiAggregationUpdateEvent(
                dateOfUpdate: timestamp2,
                csiAggregationId: mvId,
                updateCause: CsiAggregationUpdateEvent.UpdateCause.CALCULATED
        )
        CsiAggregationUpdateEvent outdated2 = new CsiAggregationUpdateEvent(
                dateOfUpdate: timestamp3,
                csiAggregationId: mvId,
                updateCause: CsiAggregationUpdateEvent.UpdateCause.OUTDATED
        )
        CsiAggregationUpdateEvent outdated3 = new CsiAggregationUpdateEvent(
                dateOfUpdate: timestamp4,
                csiAggregationId: mvId,
                updateCause: CsiAggregationUpdateEvent.UpdateCause.OUTDATED
        )
        CsiAggregationUpdateEvent calculationOfOtherCsiAggregation = new CsiAggregationUpdateEvent(
                dateOfUpdate: timestamp5,
                csiAggregationId: mvId + 1,
                updateCause: CsiAggregationUpdateEvent.UpdateCause.CALCULATED
        )
        List<CsiAggregationUpdateEvent> events = [calculationOfOtherCsiAggregation]

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
    public void testHasToBeCalculatedAccordingEvents_LastUpdateEventCalculated() {

        //create test-specific data

        CsiAggregation mv = new CsiAggregation().save(validate: false)
        Long mvId = mv.ident()
        Date timestamp1 = new DateTime(2014, 6, 25, 0, 1, 0).toDate()
        Date timestamp2 = new DateTime(2014, 6, 25, 0, 2, 0).toDate()
        Date timestamp3 = new DateTime(2014, 6, 25, 0, 3, 0).toDate()
        Date timestamp4 = new DateTime(2014, 6, 25, 0, 4, 0).toDate()
        Date timestamp5 = new DateTime(2014, 6, 25, 0, 5, 0).toDate()

        CsiAggregationUpdateEvent calculated1 = new CsiAggregationUpdateEvent(
                dateOfUpdate: timestamp1,
                csiAggregationId: mvId,
                updateCause: CsiAggregationUpdateEvent.UpdateCause.CALCULATED
        )
        CsiAggregationUpdateEvent outdated1 = new CsiAggregationUpdateEvent(
                dateOfUpdate: timestamp2,
                csiAggregationId: mvId,
                updateCause: CsiAggregationUpdateEvent.UpdateCause.OUTDATED
        )
        CsiAggregationUpdateEvent calculated2 = new CsiAggregationUpdateEvent(
                dateOfUpdate: timestamp3,
                csiAggregationId: mvId,
                updateCause: CsiAggregationUpdateEvent.UpdateCause.CALCULATED
        )
        CsiAggregationUpdateEvent calculated3 = new CsiAggregationUpdateEvent(
                dateOfUpdate: timestamp4,
                csiAggregationId: mvId,
                updateCause: CsiAggregationUpdateEvent.UpdateCause.CALCULATED
        )
        CsiAggregationUpdateEvent outdatedOfOtherCsiAggregation = new CsiAggregationUpdateEvent(
                dateOfUpdate: timestamp5,
                csiAggregationId: mvId + 1,
                updateCause: CsiAggregationUpdateEvent.UpdateCause.OUTDATED
        )
        List<CsiAggregationUpdateEvent> events = [outdatedOfOtherCsiAggregation]

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

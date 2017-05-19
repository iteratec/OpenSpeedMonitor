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
import grails.buildtestdata.mixin.Build
import grails.test.mixin.Mock
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.junit.Test
import spock.lang.Specification

import static de.iteratec.osm.report.chart.CsiAggregationUpdateEvent.UpdateCause.CALCULATED
import static de.iteratec.osm.report.chart.CsiAggregationUpdateEvent.UpdateCause.OUTDATED
import static org.hamcrest.Matchers.is
import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertThat

/**
 * Test-suite for {@link CsiAggregation}.
 */
@TestMixin(GrailsUnitTestMixin)
@Mock([CsiAggregation, AggregatorType, CsiAggregationInterval, CsiAggregationUpdateEvent, JobGroup, MeasuredEvent, Page, Browser, Location])
@Build([CsiAggregation, JobGroup])
class CsiAggregationTests extends Specification{

    Date dateOfMv1
    Date dateOfMv2
    AggregatorType measuredEventAggregator, pageAggregator, shopAggregator
    CsiAggregationInterval hourly, daily, weekly

    void setup() {
        measuredEventAggregator = new AggregatorType(name: AggregatorType.MEASURED_EVENT, measurandGroup: MeasurandGroup.NO_MEASURAND).save(failOnError: true)
        pageAggregator = new AggregatorType( name: AggregatorType.PAGE, measurandGroup: MeasurandGroup.NO_MEASURAND).save(failOnError: true)
        shopAggregator =  new AggregatorType( name: AggregatorType.SHOP, measurandGroup: MeasurandGroup.NO_MEASURAND).save(failOnError: true)
        hourly = new CsiAggregationInterval( name: "hourly", intervalInMinutes: CsiAggregationInterval.HOURLY ).save(failOnError: true)
        daily = new CsiAggregationInterval( name: "daily", intervalInMinutes: CsiAggregationInterval.DAILY ).save(failOnError: true)
        weekly = new CsiAggregationInterval( name: "weekly", intervalInMinutes: CsiAggregationInterval.WEEKLY ).save(failOnError: true)

        dateOfMv1 = new DateTime(2012, 1, 1, 0, 0, DateTimeZone.UTC).toDate()
        dateOfMv2 = new DateTime(2012, 1, 2, 0, 0, DateTimeZone.UTC).toDate()

        CsiAggregation.build(started: dateOfMv1, interval: hourly, aggregator: measuredEventAggregator, underlyingEventResultsByWptDocComplete: '')
        CsiAggregation.build(started: dateOfMv2, interval: hourly, aggregator: measuredEventAggregator, underlyingEventResultsByWptDocComplete: '1,2')
    }


    void testAccessingResultIds() {
        given:
        CsiAggregation mvInitialWithoutResultids = CsiAggregation.findByStarted(dateOfMv1)
        JobGroup.build()
        println JobGroup.count()

        expect:
        mvInitialWithoutResultids.countUnderlyingEventResultsByWptDocComplete() == 0
        mvInitialWithoutResultids.getUnderlyingEventResultsByWptDocCompleteAsList().size() == 0

        when:
        mvInitialWithoutResultids.addToUnderlyingEventResultsByWptDocComplete(3)
        then:
        mvInitialWithoutResultids.countUnderlyingEventResultsByWptDocComplete() == 1
        mvInitialWithoutResultids.getUnderlyingEventResultsByWptDocCompleteAsList().size() == 1

        when:
        CsiAggregation mvWith2Results = CsiAggregation.findByStarted(dateOfMv2)
        then:
        mvWith2Results.countUnderlyingEventResultsByWptDocComplete() == 2
        mvWith2Results.getUnderlyingEventResultsByWptDocCompleteAsList().size() == 2

        when:
        mvInitialWithoutResultids.addAllToUnderlyingEventResultsByWptDocComplete(mvWith2Results.getUnderlyingEventResultsByWptDocCompleteAsList())
        then:
        mvInitialWithoutResultids.getUnderlyingEventResultsByWptDocCompleteAsList().size() == 3

        when:
        mvInitialWithoutResultids.addAllToResultIds(mvWith2Results.underlyingEventResultsByWptDocComplete)
        then:
        mvInitialWithoutResultids.countUnderlyingEventResultsByWptDocComplete() == 5
        mvInitialWithoutResultids.getUnderlyingEventResultsByWptDocCompleteAsList().size() == 5

        when:
        mvInitialWithoutResultids.clearUnderlyingEventResultsByWptDocComplete()
        then:
        mvInitialWithoutResultids.countUnderlyingEventResultsByWptDocComplete() == 0
        mvInitialWithoutResultids.getUnderlyingEventResultsByWptDocCompleteAsList().size() == 0

    }

    /**
     * This tests adds duplicate {@link EventResult}-ID's to {@link CsiAggregation}. This shouldn't occur in production.
     * We had a bug with this (see https://seu.hh.iteratec.de:8444/browse/IT-381) and decided to remove persistence of result_ids-String for daily and weekly-aggregated {@link CsiAggregation}s.
     * If duplicate ID's get persisted we just log an error but don't throw an exception, cause the error just arose with daily and/or weekly-result_ids-Strings and didn't had any productive impact.
     * So this test just guarantees, that duplicate result-ids get persisted, although this doesn't make much sense.
     */
    void "test accessing result while ids while adding duplicate ids with single ids"() {
        given:
        CsiAggregation mvInitialWithoutResultids = CsiAggregation.findByStarted(dateOfMv1)

        when:
        mvInitialWithoutResultids.addToUnderlyingEventResultsByWptDocComplete(1)
        mvInitialWithoutResultids.addToUnderlyingEventResultsByWptDocComplete(2)
        mvInitialWithoutResultids.addToUnderlyingEventResultsByWptDocComplete(3)
        then:
        mvInitialWithoutResultids.countUnderlyingEventResultsByWptDocComplete() == 3

        when: "we add two single ids, with one duplicate"
        mvInitialWithoutResultids.addToUnderlyingEventResultsByWptDocComplete(3)
        mvInitialWithoutResultids.addToUnderlyingEventResultsByWptDocComplete(4)
        then: "both should be counted"
        mvInitialWithoutResultids.countUnderlyingEventResultsByWptDocComplete() == 5
    }

    void "test accessing result while ids while adding duplicate ids with lists"(){
        given:
        CsiAggregation mvInitialWithoutResultids = CsiAggregation.findByStarted(dateOfMv1)

        when: "we add a list of multiple ids without duplicates"
        mvInitialWithoutResultids.addAllToUnderlyingEventResultsByWptDocComplete([1, 2, 3])
        then: "they all should be counted"
        mvInitialWithoutResultids.countUnderlyingEventResultsByWptDocComplete() == 3

        when: "we add a list of multiple ids with duplicates"
        mvInitialWithoutResultids.addAllToUnderlyingEventResultsByWptDocComplete([3, 4, 5])
        then: "all should be counted"
        mvInitialWithoutResultids.countUnderlyingEventResultsByWptDocComplete() == 6
    }

    void "test accessing result ids while adding duplicateIds with string lists"(){
        given:
        CsiAggregation mvInitialWithoutResultids = CsiAggregation.findByStarted(dateOfMv1)

        when: "we add a string list of ids"
        mvInitialWithoutResultids.addAllToResultIds('9,10,11')
        then: "all should be counted"
        mvInitialWithoutResultids.countUnderlyingEventResultsByWptDocComplete() == 3

        when: "we add a string list with duplicates"
        mvInitialWithoutResultids.addAllToResultIds('11,12,13')
        then: "all should be counted"
        mvInitialWithoutResultids.countUnderlyingEventResultsByWptDocComplete() == 6
    }

    void "test hasToBeCalculated"() {
        when:
        CsiAggregation mv = new CsiAggregation().save(validate: false)
        then:
        mv.hasToBeCalculated()
    }
    void testHasToBeCalculatedLastUpdateEventOutdated() {
        //create test-specific data
        given:
        CsiAggregation mv = new CsiAggregation().save(validate: false)
        Long mvId = mv.ident()
        DateTime dateTime = new DateTime(2014, 6, 25, 0, 1, 0)
        Date timestamp1 = dateTime.toDate()
        Date timestamp2 = dateTime.plusMinutes(1).toDate()
        Date timestamp3 = dateTime.plusMinutes(2).toDate()
        Date timestamp4 = dateTime.plusMinutes(3).toDate()

        when:
        new CsiAggregationUpdateEvent( dateOfUpdate: timestamp1, csiAggregationId: mvId, updateCause: OUTDATED ).save(failOnError: true)
        then:
        mv.hasToBeCalculated()

        when:
        new CsiAggregationUpdateEvent(dateOfUpdate: timestamp2,csiAggregationId: mvId, updateCause: CALCULATED ).save(failOnError: true)
        new CsiAggregationUpdateEvent( dateOfUpdate: timestamp3, csiAggregationId: mvId, updateCause: OUTDATED ).save(failOnError: true)
        then:
        mv.hasToBeCalculated()

        when:
        new CsiAggregationUpdateEvent( dateOfUpdate: timestamp4, csiAggregationId: mvId, updateCause: OUTDATED ).save(failOnError: true)
        then:
        mv.hasToBeCalculated()
    }

    void testHasToBeCalculatedLastUpdateEventCalculated() {
        given:
        CsiAggregation mv = new CsiAggregation().save(validate: false)
        Long mvId = mv.ident()
        DateTime dateTime = new DateTime(2014, 6, 25, 0, 1, 0)
        Date timestamp1 = dateTime.toDate()
        Date timestamp2 = dateTime.plusMinutes(1).toDate()
        Date timestamp3 = dateTime.plusMinutes(2).toDate()

        when: "we add a calculated event"
        new CsiAggregationUpdateEvent( dateOfUpdate: timestamp1, csiAggregationId: mvId, updateCause: CALCULATED ).save(failOnError: true)
        then: "it doesn't have to be calculated again"
        !mv.hasToBeCalculated()

        when: "we add a outdated event and then a calculated event"
        new CsiAggregationUpdateEvent( dateOfUpdate: timestamp2, csiAggregationId: mvId, updateCause: OUTDATED ).save(failOnError: true)
        new CsiAggregationUpdateEvent( dateOfUpdate: timestamp3, csiAggregationId: mvId, updateCause: CALCULATED ).save(failOnError: true)
        then: "it should't be recalculated"
        !mv.hasToBeCalculated()

        when: "we add an additional calculated event"
        new CsiAggregationUpdateEvent( dateOfUpdate: timestamp3, csiAggregationId: mvId, updateCause: CALCULATED ).save(failOnError: true)
        then: "it still should't be recalculated"
        !mv.hasToBeCalculated()
    }

    void testHasToBeCalculatedAccordingEvents_WithoutUpdateEvents() {
        given:
        CsiAggregation mv = new CsiAggregation().save(validate: false)
        Long mvId = mv.ident()
        Date timestamp1 = new DateTime(2014, 6, 25, 0, 1, 0).toDate()
        CsiAggregationUpdateEvent calculationOfOtherCsiAggregation = new CsiAggregationUpdateEvent(
                dateOfUpdate: timestamp1,
                csiAggregationId: mvId + 1,
                updateCause: CALCULATED
        )

        expect:
        mv.hasToBeCalculatedAccordingEvents([])
        mv.hasToBeCalculatedAccordingEvents([calculationOfOtherCsiAggregation])
    }

    void testHasToBeCalculatedAccordingEvents_LastUpdateEventOutdated() {
        given:
        CsiAggregation mv = new CsiAggregation().save(validate: false)
        Long mvId = mv.ident()
        DateTime dateTime = new DateTime(2014, 6, 25, 0, 1, 0)
        Date timestamp1 = dateTime.toDate()
        Date timestamp2 = dateTime.plusMinutes(1).toDate()
        Date timestamp3 = dateTime.plusMinutes(2).toDate()
        Date timestamp4 = dateTime.plusMinutes(3).toDate()
        Date timestamp5 = dateTime.plusMinutes(4).toDate()

        CsiAggregationUpdateEvent outdated1 = new CsiAggregationUpdateEvent( dateOfUpdate: timestamp1, csiAggregationId: mvId, updateCause: OUTDATED )
        CsiAggregationUpdateEvent calculated1 = new CsiAggregationUpdateEvent( dateOfUpdate: timestamp2, csiAggregationId: mvId, updateCause: CALCULATED )
        CsiAggregationUpdateEvent outdated2 = new CsiAggregationUpdateEvent( dateOfUpdate: timestamp3, csiAggregationId: mvId, updateCause: OUTDATED )
        CsiAggregationUpdateEvent outdated3 = new CsiAggregationUpdateEvent( dateOfUpdate: timestamp4, csiAggregationId: mvId, updateCause: OUTDATED )
        CsiAggregationUpdateEvent calculationOfOtherCsiAggregation = new CsiAggregationUpdateEvent( dateOfUpdate: timestamp5, csiAggregationId: mvId + 1, updateCause: CALCULATED )
        List<CsiAggregationUpdateEvent> events = [calculationOfOtherCsiAggregation]

        when:
        events.add(outdated1)
        then:
        mv.hasToBeCalculatedAccordingEvents(events)

        when:
        events.add(calculated1)
        events.add(outdated2)

        then:
        mv.hasToBeCalculatedAccordingEvents(events)

        when:
        events.add(outdated3)
        then:
        mv.hasToBeCalculatedAccordingEvents(events)
    }

    void testHasToBeCalculatedAccordingEvents_LastUpdateEventCalculated() {
        given:
        CsiAggregation mv = new CsiAggregation().save(validate: false)
        Long mvId = mv.ident()
        DateTime dateTime =  new DateTime(2014, 6, 25, 0, 1, 0)
        Date timestamp1 = dateTime.toDate()
        Date timestamp2 = dateTime.plusMinutes(1).toDate()
        Date timestamp3 = dateTime.plusMinutes(2).toDate()
        Date timestamp4 = dateTime.plusMinutes(3).toDate()
        Date timestamp5 = dateTime.plusMinutes(4).toDate()

        CsiAggregationUpdateEvent calculated1 = new CsiAggregationUpdateEvent( dateOfUpdate: timestamp1, csiAggregationId: mvId, updateCause: CALCULATED )
        CsiAggregationUpdateEvent outdated1 = new CsiAggregationUpdateEvent( dateOfUpdate: timestamp2, csiAggregationId: mvId, updateCause: OUTDATED )
        CsiAggregationUpdateEvent calculated2 = new CsiAggregationUpdateEvent( dateOfUpdate: timestamp3, csiAggregationId: mvId, updateCause: CALCULATED )
        CsiAggregationUpdateEvent calculated3 = new CsiAggregationUpdateEvent( dateOfUpdate: timestamp4, csiAggregationId: mvId, updateCause: CALCULATED )
        CsiAggregationUpdateEvent outdatedOfOtherCsiAggregation = new CsiAggregationUpdateEvent( dateOfUpdate: timestamp5, csiAggregationId: mvId + 1, updateCause: OUTDATED )
        List<CsiAggregationUpdateEvent> events = [outdatedOfOtherCsiAggregation]

        //test
        when:
        events.add(calculated1)
        then:
        !mv.hasToBeCalculatedAccordingEvents(events)

        when:
        events.add(outdated1)
        events.add(calculated2)
        then:
        !mv.hasToBeCalculatedAccordingEvents(events)

        when:
        events.add(calculated3)
        then:
        !mv.hasToBeCalculatedAccordingEvents(events)
    }
}

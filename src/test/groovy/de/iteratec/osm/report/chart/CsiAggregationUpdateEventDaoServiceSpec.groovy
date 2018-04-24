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

import grails.buildtestdata.BuildDomainTest
import grails.testing.services.ServiceUnitTest
import spock.lang.Specification

import org.joda.time.DateTime

import static de.iteratec.osm.report.chart.CsiAggregationUpdateEvent.UpdateCause.CALCULATED
import static de.iteratec.osm.report.chart.CsiAggregationUpdateEvent.UpdateCause.OUTDATED

class CsiAggregationUpdateEventDaoServiceSpec extends Specification implements
        BuildDomainTest<CsiAggregationUpdateEvent>, ServiceUnitTest<CsiAggregationUpdateEventDaoService> {

    CsiAggregationUpdateEventDaoService serviceUnderTest

    def setup(){
        serviceUnderTest = service
    }

    def "test writing update events"() {
        given:
        Long idOutdatedCsiAggregation = 1
        Long idCalculatedCsiAggregation = 1
        DateTime oneMinuteAgo = new DateTime().minusMinutes(1)

        when:
        serviceUnderTest.createUpdateEvent(idOutdatedCsiAggregation, OUTDATED)
        serviceUnderTest.createUpdateEvent(idCalculatedCsiAggregation, CALCULATED)
        List<CsiAggregationUpdateEvent> updateEvents = CsiAggregationUpdateEvent.list()
        CsiAggregationUpdateEvent outdatedEvent = updateEvents.find{ it.updateCause == OUTDATED }
        CsiAggregationUpdateEvent calculatedEvent = updateEvents.find{ it.updateCause == CALCULATED }

        then:
        updateEvents.size() == 2

        outdatedEvent.csiAggregationId == idOutdatedCsiAggregation
        new DateTime(outdatedEvent.dateOfUpdate).isAfter(oneMinuteAgo)

        calculatedEvent.csiAggregationId == idCalculatedCsiAggregation
        new DateTime(calculatedEvent.dateOfUpdate).isAfter(oneMinuteAgo)
    }
}

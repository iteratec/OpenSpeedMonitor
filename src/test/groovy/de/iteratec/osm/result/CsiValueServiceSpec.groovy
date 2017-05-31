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

package de.iteratec.osm.result

import grails.buildtestdata.mixin.Build
import grails.test.mixin.*
import spock.lang.Specification
import de.iteratec.osm.OsmConfigCacheService
import de.iteratec.osm.csi.CsiConfiguration
import de.iteratec.osm.csi.CsiValue
import de.iteratec.osm.measurement.schedule.Job
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.report.chart.CsiAggregation

@TestFor(CsiValueService)
@Mock([EventResult, CsiAggregation])
@Build([EventResult, CsiAggregation])
class CsiValueServiceSpec extends Specification {

    CsiValueService serviceUnderTest

    final int MAX_DOC_COMPLETE = 400
    final int MIN_DOC_COMPLETE = 200

    EventResult eventResult
    CsiAggregation csiAggregation

    def doWithSpring = {
        osmConfigCacheService(OsmConfigCacheService)
    }

    void setup() {
        serviceUnderTest = service

        serviceUnderTest.osmConfigCacheService = Stub(OsmConfigCacheService) {
            getCachedMaxDocCompleteTimeInMillisecs (_) >> { return MAX_DOC_COMPLETE }
            getCachedMinDocCompleteTimeInMillisecs (_) >> { return MIN_DOC_COMPLETE }
        }

        eventResult = EventResult.build(csByWptDocCompleteInPercent: 50, docCompleteTimeInMillisecs: 300)
        csiAggregation = CsiAggregation.build(csByWptDocCompleteInPercent: 50.0)
    }

    void "Different functionality is applied respective polymorphism of CsiValue implementations"() {
        setup:
        serviceUnderTest.metaClass.isCsiRelevant = { EventResult eventResult->
            return true
        }
        serviceUnderTest.metaClass.isCsiRelevant = { CsiAggregation csiAggregation ->
            return false
        }

        when:
        CsiValue csiAgg = new CsiAggregation()
        CsiValue eventResult = new EventResult()

        then:
        !serviceUnderTest.isCsiRelevant(csiAgg)
        serviceUnderTest.isCsiRelevant(eventResult)
    }

    void "CSI relevant EventResult is recognized as relevant"() {
        given: "a CSI relevant event result"
        eventResult

        expect: "the event result is CSI relevant"
        serviceUnderTest.isCsiRelevant(eventResult)
    }

    void "EventResult with docCompleteTimeInMillisecs equal to max doc complete time is relevant"() {
        when: "doc complete is equal to max doc complete"
        eventResult.docCompleteTimeInMillisecs = MAX_DOC_COMPLETE

        then: "the event result is CSI relevant"
        serviceUnderTest.isCsiRelevant(eventResult)
    }

    void "EventResult with docCompleteTimeInMillisecs is lower than max doc complete time is relevant"() {
        when: "doc complete is lower than max doc complete"
        eventResult.docCompleteTimeInMillisecs = MAX_DOC_COMPLETE - 1

        then: "the event result is CSI relevant"
        serviceUnderTest.isCsiRelevant(eventResult)
    }

    void "EventResult which docCompleteTimeInMillisecs greater than max doc complete time is not relevant"() {
        when: "doc complete is higher than max doc complete"
        eventResult.docCompleteTimeInMillisecs = MAX_DOC_COMPLETE + 1

        then: "the event result is not CSI relevant"
        !serviceUnderTest.isCsiRelevant(eventResult)
    }

    void "EventResult which docCompleteTimeInMillisecs equals min doc complete time is relevant"() {
        when: "doc complete is equal to min doc complete"
        eventResult.docCompleteTimeInMillisecs = MIN_DOC_COMPLETE

        then: "the event result is CSI relevant"
        serviceUnderTest.isCsiRelevant(eventResult)
    }

    void "EventResult which docCompleteTimeInMillisecs greater than min doc complete time is relevant"() {
        when: "doc complete is higher than min doc complete"
        eventResult.docCompleteTimeInMillisecs = MIN_DOC_COMPLETE + 1

        then: "the event result is CSI relevant"
        serviceUnderTest.isCsiRelevant(eventResult)
    }

    void "EventResult which docCompleteTimeInMillisecs lower  than min doc complete time is not relevant"() {
        when: "doc complete is lower than min doc complete"
        eventResult.docCompleteTimeInMillisecs = MIN_DOC_COMPLETE - 1

        then: "the event result is not CSI relevant"
        !serviceUnderTest.isCsiRelevant(eventResult)
    }

    void "CsiAggregations not calculated are irrelevant"() {
        when: "CSI aggregation is not calculated"
        csiAggregation = Stub(CsiAggregation) {
            isCalculated() >> false
        }

        then: "is this CSI aggregation not relevant"
        !serviceUnderTest.isCsiRelevant(csiAggregation)
    }

    void "Csi relevant CsiAggregation is recognized as relevant"() {
        when: "CSI aggregation is calculated"
        csiAggregation = Stub(CsiAggregation) {
            isCalculated() >> true
        }

        then: "is this CSI aggregation relevant"
        serviceUnderTest.isCsiRelevant(csiAggregation)
    }

    void "CsiAggregation without csByWptDocCompleteInPercent is irrelevant"() {
        when: "CSI aggregation is calculated but csByWptDocCompleteInPercent is null"
        csiAggregation = Stub(CsiAggregation) {
            isCalculated() >> true
            getCsByWptDocCompleteInPercent() >> null
        }

        then: "is this CSI aggregation not relevant"
        !serviceUnderTest.isCsiRelevant(csiAggregation)
    }
}

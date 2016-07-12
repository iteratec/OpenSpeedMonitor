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

import de.iteratec.osm.OsmConfigCacheService
import de.iteratec.osm.csi.CsiConfiguration
import de.iteratec.osm.csi.CsiValue
import de.iteratec.osm.measurement.schedule.Job
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.report.chart.CsiAggregation
import grails.test.mixin.*
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.services.ServiceUnitTestMixin} for usage instructions
 */
@TestFor(CsiValueService)
class CsiValueServiceSpec extends Specification{

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

        mocksCommonForAllTests()

        prepareTestDataCommonForAllTests()

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
    void "Csi relevant EventResult is recognized as relevant"(){
        when:
        eventResult
        then:
        serviceUnderTest.isCsiRelevant(eventResult)
    }
    void "EventResult which docCompleteTimeInMillisecs equals max doc complete time is relevant"(){
        when:
        eventResult.docCompleteTimeInMillisecs = MAX_DOC_COMPLETE
        then:
        serviceUnderTest.isCsiRelevant(eventResult)
    }
    void "EventResult which docCompleteTimeInMillisecs lower than max doc complete time is relevant"(){
        when:
        eventResult.docCompleteTimeInMillisecs = MAX_DOC_COMPLETE-1
        then:
        serviceUnderTest.isCsiRelevant(eventResult)
    }
    void "EventResult which docCompleteTimeInMillisecs greater than max doc complete time is not relevant"(){
        when:
        eventResult.docCompleteTimeInMillisecs = MAX_DOC_COMPLETE+1
        then:
        !serviceUnderTest.isCsiRelevant(eventResult)
    }
    void "EventResult which docCompleteTimeInMillisecs equals min doc complete time is relevant"(){
        when:
        eventResult.docCompleteTimeInMillisecs = MIN_DOC_COMPLETE
        then:
        serviceUnderTest.isCsiRelevant(eventResult)
    }
    void "EventResult which docCompleteTimeInMillisecs greater than min doc complete time is relevant"(){
        when:
        eventResult.docCompleteTimeInMillisecs = MIN_DOC_COMPLETE+1
        then:
        serviceUnderTest.isCsiRelevant(eventResult)
    }
    void "EventResult which docCompleteTimeInMillisecs lower  than min doc complete time is not relevant"(){
        when:
        eventResult.docCompleteTimeInMillisecs = MIN_DOC_COMPLETE-1
        then:
        !serviceUnderTest.isCsiRelevant(eventResult)
    }

    void "EventResults without csi configuration are irrelevant"(){
        when:
        eventResult.jobResult.job.jobGroup.csiConfiguration = null
        then:
        !serviceUnderTest.isCsiRelevant(eventResult)
    }
    void "CsiAggregations not calculated are irrelevant"(){
        when:
        csiAggregation.metaClass.isCalculated = {return false}
        then:
        !serviceUnderTest.isCsiRelevant(csiAggregation)
    }
    void "Csi relevant CsiAggregation is recognized as relevant"(){
        when:
        csiAggregation.metaClass.isCalculated = {return true}
        then:
        serviceUnderTest.isCsiRelevant(csiAggregation)
    }
    void "CsiAggregation without csByWptDocCompleteInPercent is irrelevant"(){
        when:
        csiAggregation.metaClass.isCalculated = {return true}
        csiAggregation.csByWptDocCompleteInPercent = null
        then:
        !serviceUnderTest.isCsiRelevant(csiAggregation)
    }

    void mocksCommonForAllTests(){
        serviceUnderTest.osmConfigCacheService = grailsApplication.mainContext.getBean('osmConfigCacheService')
        serviceUnderTest.osmConfigCacheService.metaClass.getCachedMaxDocCompleteTimeInMillisecs = {int i -> return MAX_DOC_COMPLETE}
        serviceUnderTest.osmConfigCacheService.metaClass.getCachedMinDocCompleteTimeInMillisecs= {int i -> return MIN_DOC_COMPLETE}
    }

    void prepareTestDataCommonForAllTests(){
        eventResult = new EventResult(csByWptDocCompleteInPercent: 50, docCompleteTimeInMillisecs: 300)
        eventResult.jobResult = new JobResult()
        eventResult.jobResult.job = new Job()
        eventResult.jobResult.job.jobGroup = new JobGroup()
        eventResult.jobResult.job.jobGroup.csiConfiguration = new CsiConfiguration()
        csiAggregation = new CsiAggregation()
        csiAggregation.csByWptDocCompleteInPercent = 50.0
    }
}

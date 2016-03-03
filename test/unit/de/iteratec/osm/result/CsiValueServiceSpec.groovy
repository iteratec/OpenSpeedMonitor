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
import org.junit.*

/**
 * See the API for {@link grails.test.mixin.services.ServiceUnitTestMixin} for usage instructions
 */
@TestFor(CsiValueService)
class CsiValueServiceSpec {

    CsiValueService serviceUnderTest
    int maxDocComplete = 400
    int minDocComplete = 200
    EventResult eventResult
    CsiAggregation csiAggregation

    @Before
    void setUp() {
        serviceUnderTest = service
        serviceUnderTest.osmConfigCacheService = new OsmConfigCacheService()
        serviceUnderTest.osmConfigCacheService.metaClass.getCachedMaxDocCompleteTimeInMillisecs = {int i -> return maxDocComplete}
        serviceUnderTest.osmConfigCacheService.metaClass.getCachedMinDocCompleteTimeInMillisecs= {int i -> return minDocComplete}
        eventResult = new EventResult(csByWptDocCompleteInPercent: 50, docCompleteTimeInMillisecs: 300)
        eventResult.jobResult = new JobResult()
        eventResult.jobResult.job = new Job()
        eventResult.jobResult.job.jobGroup = new JobGroup()
        eventResult.jobResult.job.jobGroup.csiConfiguration = new CsiConfiguration()
        csiAggregation = new CsiAggregation()
        csiAggregation.csByWptDocCompleteInPercent = 50.0

    }


    @Test
    void testRightMethod() {
        serviceUnderTest.metaClass.isCsiRelevant = { EventResult eventResult->
            return true
        }
        serviceUnderTest.metaClass.isCsiRelevant = { CsiAggregation csiAggregation ->
            return false
        }
        assertFalse(serviceUnderTest.isCsiRelevant(new CsiAggregation()))
        assertTrue(serviceUnderTest.isCsiRelevant(new EventResult()))
    }

    @Test
    void testRelevantEventResult(){
        assertTrue(serviceUnderTest.isCsiRelevant(eventResult))
    }

    @Test
    void testEventResultMaxDocCompleteTime(){
        eventResult.docCompleteTimeInMillisecs = maxDocComplete
        assertFalse(serviceUnderTest.isCsiRelevant(eventResult))
        eventResult.docCompleteTimeInMillisecs = maxDocComplete-1
        assertTrue(serviceUnderTest.isCsiRelevant(eventResult))
    }

    @Test
    void testEventResultMinDocCompleteTime(){
        eventResult.docCompleteTimeInMillisecs = minDocComplete
        assertFalse(serviceUnderTest.isCsiRelevant(eventResult))
        eventResult.docCompleteTimeInMillisecs = minDocComplete+1
        assertTrue(serviceUnderTest.isCsiRelevant(eventResult))
    }

    @Test
    void testEventResultCsiConfiguration(){
        eventResult.jobResult.job.jobGroup.csiConfiguration = null
        assertFalse(serviceUnderTest.isCsiRelevant(eventResult))
    }
    @Test
    void testCsiAggregationIsCalculated(){
        csiAggregation.metaClass.isCalculated = {return false}
        assertFalse(serviceUnderTest.isCsiRelevant(csiAggregation))
    }
    @Test
    void testCsiAggregation(){
        csiAggregation.metaClass.isCalculated = {return true}
        assertTrue(serviceUnderTest.isCsiRelevant(csiAggregation))
    }
    @Test
    void testCsiAggregationMissingCs(){
        csiAggregation.metaClass.isCalculated = {return true}
        csiAggregation.csByWptDocCompleteInPercent = null
        assertFalse(serviceUnderTest.isCsiRelevant(csiAggregation))
    }
}

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

package de.iteratec.osm.batch

import spock.lang.Specification

import static org.junit.Assert.*

import grails.test.mixin.*
import grails.test.mixin.support.*
import org.junit.*

/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
@TestFor(BatchActivityService)
@Mock(BatchActivity)
class BatchActivityServiceSpec extends Specification {

    BatchActivityService serviceUnderTest

    void "testBatchActivityCreation"() {
        given:
            serviceUnderTest = service
            serviceUnderTest.timer.cancel()

        when:
            BatchActivity batchActivity = serviceUnderTest.getActiveBatchActivity(Object.class, 1, Activity.DELETE, "Object test deletion")
            serviceUnderTest.updateActivities()

        then:
            batchActivity != null
    }

    void "testBatchActivityProcess"() {
        given:
            serviceUnderTest = service
            serviceUnderTest.timer.cancel()

        when:
            BatchActivity batchActivity = serviceUnderTest.getActiveBatchActivity(Object.class, 2, Activity.DELETE, "Object test deletion")
            batchActivity.updateStatus(['progress': serviceUnderTest.calculateProgress(100, 5), 'stage': "firstStage"])
            serviceUnderTest.updateActivities()

        then:
            batchActivity.progress.contains("5 %")
            batchActivity.stage.contains("firstStage")
            batchActivity.status == Status.ACTIVE
    }

    void "testBatchActivityProcessAbortion"(){
        given:
            serviceUnderTest = service
            serviceUnderTest.timer.cancel()

        when:
            BatchActivity batchActivity = serviceUnderTest.getActiveBatchActivity(Object.class, 3, Activity.DELETE, "Object test deletion")
            batchActivity.updateStatus(['progress': serviceUnderTest.calculateProgress(100, 5), 'stage': "firstStage"])
            batchActivity.updateStatus(['status': Status.CANCELLED])
            serviceUnderTest.updateActivities()
        then:
            batchActivity.progress.contains("5 %")
            batchActivity.stage.contains("firstStage")
            batchActivity.status == Status.CANCELLED
            !serviceUnderTest.runningBatch(Object.class, 3)
    }
}
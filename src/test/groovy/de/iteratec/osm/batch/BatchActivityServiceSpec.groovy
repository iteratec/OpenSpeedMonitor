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

import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import spock.lang.Specification

@TestFor(BatchActivityService)
@Mock(BatchActivity)
class BatchActivityServiceSpec extends Specification {

    void "get batch activity returns a real BatchActivityUpdater when observe is true"() {
        given:
        String name = "BatchActivityCreateTest"
        Class aClass = this.class
        Activity activity = Activity.CREATE

        when: "We order a BatchActivityUpdater from this service"
        BatchActivityUpdater updater = service.getActiveBatchActivity(aClass, activity, name, 1, true)

        then: "The underlying BatchActivity should be persisted"
        BatchActivity.count() == 1
        service.runningBatch(aClass,name, activity)

        cleanup:
        updater.cancel()
    }

    void "get batch activity returns a dummy BatchActivityUpdater when observe is false"() {
        given:
        String name = "BatchActivityCreateTest"
        Class aClass = this.class
        Activity activity = Activity.CREATE

        when: "We order a BatchActivityUpdaterDummy from this service"
        BatchActivityUpdater updater = service.getActiveBatchActivity(aClass, activity, name, 1, false)

        then: "The should'nt be a real BatchActivity"
        BatchActivity.count() == 0
        !service.runningBatch(aClass,name, activity)

        cleanup:
        updater.cancel()
    }

}

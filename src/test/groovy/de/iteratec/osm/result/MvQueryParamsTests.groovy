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

import spock.lang.Specification


/**
 * Test-suite for {@link MvQueryParams}.
 *
 * @author mze
 */
class MvQueryParamsTests extends Specification {

    def "test to string"() {
        given: "some query parameters"
        MvQueryParams queryParams = new MvQueryParams()
        queryParams.jobGroupIds.addAll([8, 9])
        queryParams.measuredEventIds.addAll([38, 77])
        queryParams.pageIds.addAll([1, 3, 8])
        queryParams.browserIds.addAll([7])
        queryParams.locationIds.addAll([99, 101])
        queryParams.connectivityProfileIds.addAll([1, 2, 4])

        when: "the to string method is called"
        String queryParamsString = queryParams.toString()

        then: "the returned string should be equal to the expected one"
        queryParamsString == 'jobGroupIds=[8, 9], pageIds=[1, 3, 8], measuredEventIds=[38, 77], browserIds[7], locationIds=[99, 101], connectivityProfileIds=[1, 2, 4]'
    }
}

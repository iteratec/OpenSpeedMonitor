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

package de.iteratec.osm.util

import spock.lang.Specification


class ControllerUtilsSpec extends Specification {

    void "an empty request returns true if checked for beeing empty"() {
        given: "an empty parameter map"
        Map<String, Object> params = new HashMap<String, Object>()

        expect: "isEmptyRequests returns true"
        ControllerUtils.isEmptyRequest(params)
    }

    void "a non empty request returns false if checked for beeing empty"() {
        given: "a non empty request parameter map"
        Map<String, Object> params = new HashMap<String, Object>()
        params.put("selectedGroupIDs", [1L, 2L, 3L])

        expect: "isEmptyRequests returns true"
        !ControllerUtils.isEmptyRequest(params)
    }

    void "request with grails magic keys is still empty"() {
        given: "a parameter map with grails magic keys"
        Map<String, Object> params = new HashMap<String, Object>()
        params.put("lang", "not-of-interest-for-test")
        params.put("action", "not-of-interest-for-test")
        params.put("controller", "not-of-interest-for-test")

        expect: "isEmptyRequests returns true"
        ControllerUtils.isEmptyRequest(params)
    }
}

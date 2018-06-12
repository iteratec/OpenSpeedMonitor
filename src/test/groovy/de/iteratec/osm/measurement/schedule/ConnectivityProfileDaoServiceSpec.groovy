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

package de.iteratec.osm.measurement.schedule

import grails.buildtestdata.BuildDataTest
import grails.buildtestdata.mixin.Build
import grails.testing.services.ServiceUnitTest
import spock.lang.Specification

@Build(ConnectivityProfile)
class ConnectivityProfileDaoServiceSpec extends Specification implements BuildDataTest,
        ServiceUnitTest<ConnectivityProfileDaoService> {
    void setupSpec() {
        mockDomain(ConnectivityProfile)
    }

    void "findAll delivers all profiles"() {
        when: "4 ConnectivityProfiles exit in db."
        ConnectivityProfile.build(name: 'first')
        ConnectivityProfile.build(name: 'second')
        ConnectivityProfile.build(name: 'third')
        ConnectivityProfile.build(name: 'fourth')

        then: "findAll() retrieves them all"
        service.findAll().size() == 4
    }

}

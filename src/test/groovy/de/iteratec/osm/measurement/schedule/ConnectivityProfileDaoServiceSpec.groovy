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

import grails.buildtestdata.mixin.Build
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import spock.lang.Specification
/**
 * See the API for {@link grails.test.mixin.services.ServiceUnitTestMixin} for usage instructions
 */
@TestFor(ConnectivityProfileDaoService)
@Mock([ConnectivityProfile])
@Build(ConnectivityProfile)
class ConnectivityProfileDaoServiceSpec extends Specification{

    void "findAll delivers all profiles"() {
        when:
        ConnectivityProfile.build(name: 'first')
        ConnectivityProfile.build(name: 'second')
        ConnectivityProfile.build(name: 'third')
        ConnectivityProfile.build(name: 'fourth')

        then:
        service.findAll().size() == 4
    }

}

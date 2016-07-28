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

import de.iteratec.osm.csi.TestDataUtil
import grails.test.mixin.*
import org.junit.*
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.services.ServiceUnitTestMixin} for usage instructions
 */
@TestFor(ConnectivityProfileDaoService)
@Mock([ConnectivityProfile])
class ConnectivityProfileDaoServiceSpec extends Specification{
    ConnectivityProfileDaoService serviceUnderTest
    void setup(){
        serviceUnderTest = service
    }
    void "findAll delivers all profiles"() {
        when:
        TestDataUtil.createConnectivityProfile('first')
        TestDataUtil.createConnectivityProfile('second')
        TestDataUtil.createConnectivityProfile('third')
        TestDataUtil.createConnectivityProfile('fourth')

        then:
        serviceUnderTest.findAll().size() == 4
    }
}

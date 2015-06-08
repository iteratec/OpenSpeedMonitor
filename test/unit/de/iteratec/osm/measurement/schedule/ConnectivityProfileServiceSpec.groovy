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


import grails.test.mixin.*
import org.junit.*
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.services.ServiceUnitTestMixin} for usage instructions
 */
@TestFor(ConnectivityProfileService)
class ConnectivityProfileServiceSpec extends Specification{

    ConnectivityProfileService serviceUnderTest

    void setup(){
        //eclipse doesn't know injected service object, so we help ;)
        serviceUnderTest = service
    }
    void "get custom name for connectivity without packet loss"() {
        when:
        int bwDown = 50000
        int bwUp = 6000
        int latency = 50
        int plr = 0

        then:
        serviceUnderTest.getCustomConnectivityNameFor(bwDown, bwUp, latency, plr) == 'Custom (50.000/6.000 Kbps, 50ms Latency)'
    }
    void "get custom name for connectivity with packet loss"() {
        when:
        int bwDown = 50000
        int bwUp = 6000
        int latency = 50
        int plr = 5

        then:
        serviceUnderTest.getCustomConnectivityNameFor(bwDown, bwUp, latency, plr) == 'Custom (50.000/6.000 Kbps, 50ms Latency, 5% PLR)'
    }
    void "validation of invalid connectivity attributes"(){
        when:
        int validBwDown = ConnectivityProfile.BANDWIDTH_DOWN_MIN
        int invalidBwDown = ConnectivityProfile.BANDWIDTH_DOWN_MIN - 1
        int validBwUp = ConnectivityProfile.BANDWIDTH_UP_MIN
        int invalidBwUp = ConnectivityProfile.BANDWIDTH_UP_MIN - 1
        int validLatency = ConnectivityProfile.LATENCY_MIN
        int invalidLatency = ConnectivityProfile.LATENCY_MIN - 1
        int validPlr = ConnectivityProfile.PLR_MIN
        int invalidPlr = ConnectivityProfile.PLR_MIN - 1


        then:
        //should fail cause of null values
        shouldFail(IllegalArgumentException){
            serviceUnderTest.validateConnectivityAttributes(null, validBwUp, validLatency, validPlr)
        }
        shouldFail(IllegalArgumentException){
            serviceUnderTest.validateConnectivityAttributes(validBwDown, null, validLatency, validPlr)
        }
        shouldFail(IllegalArgumentException){
            serviceUnderTest.validateConnectivityAttributes(validBwDown, validBwUp, null, validPlr)
        }
        shouldFail(IllegalArgumentException){
            serviceUnderTest.validateConnectivityAttributes(validBwDown, validBwUp, validLatency, null)
        }
        //should fail cause of invalid values
        shouldFail(IllegalArgumentException){
            serviceUnderTest.validateConnectivityAttributes(invalidBwDown, validBwUp, validLatency, validPlr)
        }
        shouldFail(IllegalArgumentException){
            serviceUnderTest.validateConnectivityAttributes(validBwDown, invalidBwUp, validLatency, validPlr)
        }
        shouldFail(IllegalArgumentException){
            serviceUnderTest.validateConnectivityAttributes(validBwDown, validBwUp, invalidLatency, validPlr)
        }
        shouldFail(IllegalArgumentException){
            serviceUnderTest.validateConnectivityAttributes(validBwDown, validBwUp, validLatency, invalidPlr)
        }

    }
    void "validation of valid connectivity attributes"(){
        when:
        int validBwDown = ConnectivityProfile.BANDWIDTH_DOWN_MIN
        int validBwUp = ConnectivityProfile.BANDWIDTH_UP_MIN
        int validLatency = ConnectivityProfile.LATENCY_MIN
        int validPlr = ConnectivityProfile.PLR_MIN

        then:
        serviceUnderTest.validateConnectivityAttributes(validBwDown, validBwUp, validLatency, validPlr)
    }
}

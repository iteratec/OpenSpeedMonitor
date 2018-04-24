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

import grails.testing.services.ServiceUnitTest
import spock.lang.Specification

class ConnectivityProfileServiceSpec extends Specification implements ServiceUnitTest<ConnectivityProfileService> {
    static int validBwDown = ConnectivityProfile.BANDWIDTH_DOWN_MIN
    static int invalidBwDown = ConnectivityProfile.BANDWIDTH_DOWN_MIN - 1
    static int validBwUp = ConnectivityProfile.BANDWIDTH_UP_MIN
    static int invalidBwUp = ConnectivityProfile.BANDWIDTH_UP_MIN - 1
    static int validLatency = ConnectivityProfile.LATENCY_MIN
    static int invalidLatency = ConnectivityProfile.LATENCY_MIN - 1
    static int validPlr = ConnectivityProfile.PLR_MIN
    static int invalidPlr = ConnectivityProfile.PLR_MIN - 1

    void "get custom name for connectivity without packet loss"() {
        when: "all attributes but packet loss are set"
        int bwDown = 50000
        int bwUp = 6000
        int latency = 50
        int plr = 0

        then: "a readable name without packetloss is generated"
        service.getCustomConnectivityNameFor(bwDown, bwUp, latency, plr) == 'Custom (50000/6000 Kbps, 50ms Latency)'
    }

    void "get custom name for connectivity with packet loss"() {
        when: "all attributes including packet loss are set"
        int bwDown = 50000
        int bwUp = 6000
        int latency = 50
        int plr = 5

        then: "a readable name including packetloss is generated"
        service.getCustomConnectivityNameFor(bwDown, bwUp, latency, plr) == 'Custom (50000/6000 Kbps, 50ms Latency, 5% PLR)'
    }

    void "invalid connectivity attributes throw an exception"(Integer bwDown, Integer bwUp, Integer latency, Integer plr) {
        when: "an invalid argument is used vor validation"
        service.validateConnectivityAttributes(bwDown, bwUp, latency, plr)

        then: "an IllegalArgumentException is thrown"
        thrown(IllegalArgumentException)

        where:
        bwDown        | bwUp        | latency        | plr
        null          | validBwUp   | validLatency   | validPlr
        validBwDown   | null        | validLatency   | validPlr
        validBwDown   | validBwUp   | null           | validPlr
        validBwDown   | validBwUp   | validLatency   | null
        invalidBwDown | validBwUp   | validLatency   | validPlr
        validBwDown   | invalidBwUp | validLatency   | validPlr
        validBwDown   | validBwUp   | invalidLatency | validPlr
        validBwDown   | validBwUp   | validLatency   | invalidPlr
    }

    void "validation of valid connectivity attributes" () {
        when: "valid connectivity attributes are chosen"

        then: "they are recognized as valid"
        service.validateConnectivityAttributes(validBwDown, validBwUp, validLatency, validPlr)
    }
}

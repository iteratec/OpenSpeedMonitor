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

package de.iteratec.osm.csi

import de.iteratec.osm.measurement.environment.Browser
import de.iteratec.osm.measurement.schedule.ConnectivityProfile
import grails.buildtestdata.mixin.Build
import grails.test.mixin.TestFor
import spock.lang.Specification

@TestFor(BrowserConnectivityWeight)
@Build([Browser,ConnectivityProfile])
class BrowserConnectivityWeightSpec extends Specification {

    void "BrowserConnectivityWeight without attributes is invalid"() {
        expect:
        !new BrowserConnectivityWeight().validate()
    }

    void "BrowserConnectivityWeight with just a Browser is invalid"() {
        expect:
        !new BrowserConnectivityWeight(browser: Browser.build()).validate()
    }

    void "BrowserConnectivityWeight with just a ConnectivityProfile is invalid"() {
        expect:
        !new BrowserConnectivityWeight(connectivity: ConnectivityProfile.build()).validate()
    }

    void "BrowserConnectivityWeight with all required values is valid"() {
        expect:
        new BrowserConnectivityWeight(
            browser: Browser.build(),
            connectivity: ConnectivityProfile.build(),
            weight: 12.5
        ).validate()
    }
}

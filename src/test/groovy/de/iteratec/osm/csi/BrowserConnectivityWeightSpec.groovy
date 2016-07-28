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
import grails.test.mixin.TestFor
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.domain.DomainClassUnitTestMixin} for usage instructions
 */
@TestFor(BrowserConnectivityWeight)
class BrowserConnectivityWeightSpec extends Specification {

    void "setup"() {
    }

    void "test nullable"() {
        when:
        BrowserConnectivityWeight browserConnectivityWeight = new BrowserConnectivityWeight()

        then:
        !browserConnectivityWeight.validate()
    }

    void "test only browser not valid"() {
        when:
        BrowserConnectivityWeight browserConnectivityWeight = new BrowserConnectivityWeight(browser: new Browser())

        then:
        !browserConnectivityWeight.validate()
    }

    void "test only connectivity not valid"() {
        when:
        BrowserConnectivityWeight browserConnectivityWeight = new BrowserConnectivityWeight(connectivity: new ConnectivityProfile())

        then:
        !browserConnectivityWeight.validate()
    }

    void "all fields defined valid"() {
        when:
        BrowserConnectivityWeight browserConnectivityWeight = new BrowserConnectivityWeight(browser: new Browser(), connectivity: new ConnectivityProfile(), weight: 12.5)

        then:
        browserConnectivityWeight.validate()
    }
}

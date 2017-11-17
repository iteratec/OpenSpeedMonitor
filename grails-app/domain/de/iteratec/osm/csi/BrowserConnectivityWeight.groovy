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
import grails.gorm.annotation.Entity

/**
 * BrowserConnectivityWeight
 * A domain class describes the data object and it's mapping to the database
 */
@Entity
class BrowserConnectivityWeight {

    Browser browser
    ConnectivityProfile connectivity
    Double weight

    static constraints = {
        browser(nullable: false)
        connectivity(nullable: false)
        weight(nullable: false)
    }

    static BrowserConnectivityWeight copyBrowserConnectivityWeight(BrowserConnectivityWeight source) {
        return new BrowserConnectivityWeight(browser: source.browser, connectivity: source.connectivity, weight: source.weight)
    }
}

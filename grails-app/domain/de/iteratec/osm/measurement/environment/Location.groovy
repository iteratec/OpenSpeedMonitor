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

package de.iteratec.osm.measurement.environment



/**
 * A combination of wpt-agent and browser that is configured in the settings/locations.ini-file of one wptserver.
 * Uniquely identified through {@link #location} and {@link #wptServer}.
 */
class Location {

    String label
    /**
     * <h1>While fetching results from server:</h1>
     * Fits location-tag in result-xml.<br>
     * <h1>While fetching locations from server:</h1>
     * Fits id-tag in location-xml.<br>
     * Does <b>not</b> fit location-tag in location-xml.
     */
    String uniqueIdentifierForServer

    Long	id
    Date	dateCreated
    Date	lastUpdated

    Boolean active
    int valid
    /**
     * <h1>While fetching locations from server:</h1>
     * Fits location-tag in location-xml.
     */
    String location
    Browser browser
    int activeagents
    int queuethreshold
    int queuethresholdgreenlimit
    int queuethresholdyellowlimit
    int queuethresholdredlimit

    WebPageTestServer wptServer
    static belongsTo = [ wptServer: WebPageTestServer ]

    static constraints = {
        label(maxSize: 150)
        uniqueIdentifierForServer(nullable: true, maxSize: 255, unique: ['wptServer', 'browser'])
        dateCreated()
        active()
        valid(range: 0 .. 1)
        wptServer()
        location(maxSize: 100)
        browser(maxSize: 20)
        activeagents(nullable: true, min: -2147483648, max: 2147483647)
        queuethreshold(nullable: true, min: -2147483648, max: 2147483647)
        queuethresholdgreenlimit(nullable: true, min: -2147483648, max: 2147483647)
        queuethresholdyellowlimit(nullable: true, min: -2147483648, max: 2147483647)
        queuethresholdredlimit(nullable: true, min: -2147483648, max: 2147483647)
        wptServer()
    }

    static mapping = {
        //locations cascade: "all-delete-orphan"
    }

    /**
     * Removes browser.name from labelWithBrowser if it is preceded by a non-word character
     * So if browser.name == 'Chrome' and labelWithBrowser == 'Agent3-wptdriver:Chrome', 'Agent3-wptdriver' is returned
     */
    public String removeBrowser(String labelWithBrowser) {
        return (!browser) ? labelWithBrowser : labelWithBrowser - ~/\W${browser.name}$/
    }

    @Override	// Override toString for a nicer / more descriptive UI
    public String toString() {
        return this.location + ' @ ' + this.wptServer.label + ' (' + this.browser.name + ')'
    }
}

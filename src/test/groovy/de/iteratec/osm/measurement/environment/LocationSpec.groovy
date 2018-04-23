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

import grails.buildtestdata.BuildDataTest
import grails.buildtestdata.mixin.Build
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import spock.lang.Specification
import spock.lang.Unroll

/**
 * Test-suite for {@link Location}
 */
@TestFor(Location)
@Build([Location, WebPageTestServer, Browser])
@Mock([Location, WebPageTestServer,Browser])
class LocationSpec extends Specification implements BuildDataTest {

    @Unroll
    void "A location with a label of a length of #labelLength characters validates to #valid"() {

        when: "a location with a label of a length of #labelLength characters is created"
        Location location = Location.buildWithoutSave(label: "*".padLeft(labelLength,"*"))

        then: "the location validates to #valid"
        location.validate() == valid

        where:
        labelLength | valid
        100         | true
        200         | true
        255         | true
        256         | false
        1000        | false

    }

    void "toString includes location, WPT server and browser name, but not the location label"() {
        given: "a WPT server, a browser, and a location associated with them"
        WebPageTestServer server = WebPageTestServer.build(label: 'wpt1')
        Browser browser = Browser.build(name: 'Firefox')
        Location location = Location.build(
                label: 'Agent 1: Offizielles Monitoring',
                location: 'Agent1-wptdriver:Firefox7',
                browser: browser,
                wptServer: server
        )

        when: "toString is called"
        String result = location.toString()

        then: "the result contains the location, server and browser"
        result == "Agent1-wptdriver:Firefox7 @ wpt1 (Firefox)"
    }

    void "removeBrowser returns the label without the browser name"(String uniqueIdentifierForServer) {
        given: "a location with associated browser"
        Location location = Location.build(
                uniqueIdentifierForServer: uniqueIdentifierForServer,
                browser: Browser.build(name: "Chrome")
        )

        when: "The method is called on the unique identifier for server"
        String result = location.removeBrowser(location.uniqueIdentifierForServer)

        then: "The string without the browser name and joining character is returned"
        result == "Agent3-wptdriver"

        where:
        uniqueIdentifierForServer | _
        "Agent3-wptdriver:Chrome" | _
        "Agent3-wptdriver-Chrome" | _
    }

}

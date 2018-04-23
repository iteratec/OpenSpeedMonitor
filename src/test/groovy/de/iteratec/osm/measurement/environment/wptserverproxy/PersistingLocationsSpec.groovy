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


package de.iteratec.osm.measurement.environment.wptserverproxy

import de.iteratec.osm.csi.Page
import de.iteratec.osm.measurement.environment.Browser
import de.iteratec.osm.measurement.environment.BrowserAlias
import de.iteratec.osm.measurement.environment.BrowserService
import de.iteratec.osm.measurement.environment.Location
import de.iteratec.osm.measurement.environment.WebPageTestServer
import de.iteratec.osm.measurement.schedule.Job
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.measurement.script.Script
import de.iteratec.osm.result.EventResult
import de.iteratec.osm.result.JobResult
import de.iteratec.osm.result.MeasuredEvent
import grails.buildtestdata.BuildDataTest
import grails.buildtestdata.mixin.Build
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import groovy.util.slurpersupport.GPathResult
import spock.lang.Specification

/**
 * Tests the saving of locations and results. These functions are used in proxy-mechanism.
 * Testing the mapping of load-times to customer satisfactions or the persisting of dependent {@link CsiAggregation}s is not the concern of the tests in this class.
 * @author nkuhn
 * @see {@link ProxyService}
 *
 */
@TestFor(LocationPersisterService)
@Build([WebPageTestServer, Location, Browser])
@Mock([WebPageTestServer, Browser, Location, Job, JobResult, EventResult, BrowserAlias, Page, MeasuredEvent, JobGroup, Script])
class PersistingLocationsSpec extends Specification implements BuildDataTest {

    def doWithSpring = {
        browserService(BrowserService)
    }

    void "when listening to a WPT location respons, missing locations are added for this server"() {
        setup:
        def file = new File('src/test/resources/WptLocationXmls/locationResponse.xml')
        GPathResult result = new XmlSlurper().parse(file)
        WebPageTestServer wptServer1 = WebPageTestServer.build()
        WebPageTestServer wptServer2 = WebPageTestServer.build()

        when: "the service listens for new locations for one server"
        service.listenToLocations(result, wptServer1)

        then: "all 5 are added"
        Location.count() == 5

        when: "the services sees the same locations for the same server"
        Location.count() == 5
        Location.findAllByWptServer(wptServer1).size() == 5
        Location.findAllByWptServer(wptServer2).size() == 0

        then: "they are not added again"
        Location.count() == 5
        Location.findAllByWptServer(wptServer1).size() == 5
        Location.findAllByWptServer(wptServer2).size() == 0

        when: "the service sees these locations, but for another server"
        service.listenToLocations(result, wptServer2)

        then: "they are added for the other server"
        Location.count() == 10
        Location.findAllByWptServer(wptServer1).size() == 5
        Location.findAllByWptServer(wptServer2).size() == 5
    }

    void "new locations is created for new browser in location response"() {
        setup:
        def file = new File('src/test/resources/WptLocationXmls/locationResponse.xml')
        GPathResult result = new XmlSlurper().parse(file)
        WebPageTestServer wptServer = WebPageTestServer.build()
        ["Chrome", "IE", "Firefox", "Canary"].each { name -> Browser.build(name: name) }

        when: "the service listens for new locations"
        service.listenToLocations(result, wptServer)

        then: "all locations are added"
        Location.count() == 5

        when: "the services listens to a new result with a different browser"
        result.data.location.each { xmlResult ->
            if (xmlResult.Browser == "Chrome") {
                xmlResult.Browser = "Canary"
            }
        }
        service.listenToLocations(result, wptServer)

        then: "a new browser and location are created"
        Location.count() == 6
        Location.findAllByBrowser(Browser.findByName("Canary")).size() == 1
    }

    void "existing location gets deactivated if not existing in location response"() {
        setup:
        def file = new File('src/test/resources/WptLocationXmls/locationResponse.xml')
        GPathResult result = new XmlSlurper().parse(file)
        WebPageTestServer wptServer = WebPageTestServer.build(active: true)
        Location.build(uniqueIdentifierForServer: "sampleLocation", wptServer: wptServer, active: true)

        when: "the service listens for new locations"
        service.listenToLocations(result, wptServer)

        then: "all locations are added, with the existing one being deactivated"
        Location.count() == 6
        !Location.findByUniqueIdentifierForServer("sampleLocation").active
    }

    void "deactivation of not matching locations for WPT server"() {
        given: "three active locations"
        WebPageTestServer wptServer = WebPageTestServer.build(active: true)
        Location.build(uniqueIdentifierForServer: "location1", active: true, wptServer: wptServer)
        Location.build(uniqueIdentifierForServer: "location2", active: true, wptServer: wptServer)
        Location.build(uniqueIdentifierForServer: "location3", active: true, wptServer: wptServer)

        when: "the service should deactivate all locations but the passed ones"
        service.deactivateNotMatchingLocations(wptServer, ["location1", "location3"])

        then: "the two locations are still active, but the other was deactivated"
        Location.count() == 3
        Location.findByUniqueIdentifierForServer("location1").active
        !Location.findByUniqueIdentifierForServer("location2").active
        Location.findByUniqueIdentifierForServer("location3").active
    }

}

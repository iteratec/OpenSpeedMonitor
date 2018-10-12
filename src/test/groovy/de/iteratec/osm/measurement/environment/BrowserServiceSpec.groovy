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
import grails.testing.services.ServiceUnitTest
import spock.lang.Specification

/**
 * Test-suite for {@link BrowserService}.
 */
@Build([Browser])
class BrowserServiceSpec extends Specification implements BuildDataTest, ServiceUnitTest<BrowserService> {

    void setupSpec() {
        mockDomains(Browser, BrowserAlias)
    }

    void "find by name or alias returns correct browsers"(String nameOrAlias, String expectedBrowserName) {
        given: "Two browsers with aliases"
        Browser.build(name: "Firefox")
                .addToBrowserAliases(alias: "FF")
                .addToBrowserAliases(alias: "Firefox7").save()
        Browser.build(name: "IE").addToBrowserAliases(alias:  "Internet Explorer").save()

        when: "a browser should be found by name or alias"
        def browser = service.findByNameOrAlias(nameOrAlias)

        then: "the corresponding browser is found, the browser that was not defined is created"
        browser.name == expectedBrowserName
        where:
        nameOrAlias         | expectedBrowserName
        "Internet Explorer" | "IE"
        "IE"                | "IE"
        "Firefox"           | "Firefox"
        "FF"                | "Firefox"
        "Firefox7"          | "Firefox"
    }

    void "findAll by name returns correct browsers"() {
        given: "Three browsers and aliases"
        Browser.build(name: "Firefox")
                .addToBrowserAliases(alias: "FF")
                .addToBrowserAliases(alias: "Firefox7").save()
        Browser.build(name: "IE").addToBrowserAliases(alias:  "Internet Explorer").save()
        Browser.build(name: "Edge")

        when: "all browsers should be found by name or alias"
        def browser = service.findAllByName(["FF", "IE", "Chrome", "Firefox7", "Edge"])

        then: "the corresponding browsers are found"
        browser*.name == ["Firefox", "IE", null, "Firefox", "Edge"]
    }

    void "find all returns all browsers"() {
        given: "three browsers and the undefined"
        Browser.build(name: "Firefox")
        Browser.build(name: "IE")
        Browser.build(name: "Edge")
        Browser.build(name: Browser.UNDEFINED)

        when: "find all is called"
        def browsers = service.findAll()

        then: "all browsers are returned, including the undefined"
        browsers*.name as Set == ["Firefox", "IE", "Edge", Browser.UNDEFINED] as Set
    }

    void "findOrCreateByNameOrAlias creates non-existing browsers"() {
        given: "one browser with aliases"
        Browser.build(name: "Firefox")
                .addToBrowserAliases(alias: "FF")
                .addToBrowserAliases(alias: "Firefox7").save()

        when: "browsers are searched"
        def shouldCreate = service.findOrCreateByNameOrAlias("Chrome")
        def shouldnCreate = service.findOrCreateByNameOrAlias("FF")

        then: "browsers are created if they don't exist"
        shouldCreate.name == "Chrome"
        shouldnCreate.name == "Firefox"
    }
}

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

import de.iteratec.osm.api.dto.BrowserInfoDto
import de.iteratec.osm.result.DeviceType
import de.iteratec.osm.result.OperatingSystem
import grails.buildtestdata.BuildDataTest
import grails.buildtestdata.mixin.Build
import grails.testing.services.ServiceUnitTest
import spock.lang.Specification
import spock.lang.Unroll
/**
 * Test-suite for {@link BrowserService}.
 */
@Build([Browser, Location, WebPageTestServer])
class BrowserServiceSpec extends Specification implements BuildDataTest, ServiceUnitTest<BrowserService> {

    void "find by name or alias returns correct browsers"(String nameOrAlias, String expectedBrowserName) {
        given: "Two browsers with aliases"
        Browser.build(name: "Firefox")
                .addToBrowserAliases(alias: "FF")
                .addToBrowserAliases(alias: "Firefox7").save()
        Browser.build(name: "IE").addToBrowserAliases(alias: "Internet Explorer").save()

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

    @Unroll
    void "Get extended Browser informations for Browser #browserName"() {
        given: "A browser with an associated Location with information"
        Browser b = Browser.build(name: "Chrome")
        new Location(
                browser: b, active: true, operatingSystem: os, deviceType: dt,
                label: 'loc', wptServer: WebPageTestServer.build(), location: 'loc'
        ).save()

        when: "Getting Browser infos and look up for the created browser"
        List<BrowserInfoDto> browserInfos = service.getBrowserInfos()
        BrowserInfoDto browserInfo = browserInfos.find { it.browserId == b.ident() }

        then: "Location information operating system and device type are provided correctly"
        browserInfo.operatingSystem == os.getOSLabel()
        browserInfo.deviceType.name == dt.getDeviceTypeLabel()
        browserInfo.deviceType.icon == dt.getDeviceTypeIcon()

        where:
        browserName    | os                      | dt
        "Chrome"       | OperatingSystem.WINDOWS | DeviceType.DESKTOP
        "Firefox"      | OperatingSystem.WINDOWS | DeviceType.DESKTOP
        "Galaxy Tab A" | OperatingSystem.ANDROID | DeviceType.TABLET
        "iPad"         | OperatingSystem.IOS     | DeviceType.TABLET
        "iPhone"       | OperatingSystem.IOS     | DeviceType.SMARTPHONE

    }

    void "Get extended Browser informations for Browser with inconclusive location informations"() {
        given: "Two browsers with conclusive and one with inconclusive location information"
        Browser chrome = Browser.build(name: "Chrome")
        Location.build(browser: chrome, active: true, operatingSystem: OperatingSystem.WINDOWS, deviceType: DeviceType.DESKTOP)
        Location.build(browser: chrome, active: true, operatingSystem: OperatingSystem.ANDROID, deviceType: DeviceType.SMARTPHONE)
        Browser firefox = Browser.build(name: "Firefox")
        Location.build(browser: firefox, active: true, operatingSystem: OperatingSystem.WINDOWS, deviceType: DeviceType.DESKTOP)
        Browser androidSmartphone = Browser.build(name: "Galaxy S8")
        Location.build(browser: androidSmartphone, active: true, operatingSystem: OperatingSystem.ANDROID, deviceType: DeviceType.SMARTPHONE)
        Location.list().each { it.active = true; it.save(flush: true) }

        when: "Getting Browser infos and look up for the created browsers"
        List<BrowserInfoDto> browserInfos = service.getBrowserInfos()
        BrowserInfoDto firefoxInfo = browserInfos.find { it.browserId == firefox.ident() }
        BrowserInfoDto androidInfos = browserInfos.find { it.browserId == androidSmartphone.ident() }

        then: "Location information operating system and device type are provided just for browsers with conclusive informations available"
        browserInfos.size() == 2
        firefoxInfo.operatingSystem == OperatingSystem.WINDOWS.getOSLabel()
        firefoxInfo.deviceType.name == DeviceType.DESKTOP.getDeviceTypeLabel()
        firefoxInfo.deviceType.icon == DeviceType.DESKTOP.getDeviceTypeIcon()
        androidInfos.operatingSystem == OperatingSystem.ANDROID.getOSLabel()
        androidInfos.deviceType.name == DeviceType.SMARTPHONE.getDeviceTypeLabel()
        androidInfos.deviceType.icon == DeviceType.SMARTPHONE.getDeviceTypeIcon()
    }
}

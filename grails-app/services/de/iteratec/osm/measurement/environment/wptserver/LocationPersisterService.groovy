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

package de.iteratec.osm.measurement.environment.wptserver

import de.iteratec.osm.measurement.environment.Browser
import de.iteratec.osm.measurement.environment.BrowserService
import de.iteratec.osm.measurement.environment.Location
import de.iteratec.osm.measurement.environment.WebPageTestServer
import de.iteratec.osm.result.DeviceType
import de.iteratec.osm.result.OperatingSystem
import de.iteratec.osm.util.PerformanceLoggingService
import grails.gorm.transactions.Transactional
import groovy.util.slurpersupport.GPathResult

/**
 * Persists locations and results. Observer of WptInstructionService.
 * @author rschuett , nkuhn
 * grails-app/services/de/iteratec/ispc/ResultPersisterService.groovy
 */
class LocationPersisterService implements iLocationListener {

    BrowserService browserService
    PerformanceLoggingService performanceLoggingService

    /**
     * Persisting non-existent locations.
     */
    @Override
    String getListenerName() {
        "LocationPersisterService"
    }

    @Override
    @Transactional
    List<Location> listenToLocations(GPathResult result, WebPageTestServer wptserverForLocation) {
        List<Location> addedLocations = []

        log.info("Location.count before creating non-existent= ${Location.count()}")
        List<String> locationIdentifiersForWptServer = []
        result.data.location.each { locationTagInXml ->
            List<Location> locations
            List<String> browserNames = locationTagInXml.Browsers.size() != 0 ? locationTagInXml.Browsers.toString().split(",") : [locationTagInXml.Browser.toString()]
            browserNames.each { browserName ->
                Browser browserForLocation = browserService.findOrCreateByNameOrAlias(browserName)
                String uniqueIdentfierForServer = locationTagInXml.id.toString().endsWith(":${browserName}") ?: locationTagInXml.id.toString() + ":${browserName}"
                locationIdentifiersForWptServer << uniqueIdentfierForServer
                List<Location> locationsForCurrentBrowserAndWptServer = Location.findAllByWptServerAndUniqueIdentifierForServerAndBrowser(wptserverForLocation, uniqueIdentfierForServer, browserForLocation)
                if (!locationsForCurrentBrowserAndWptServer) {
                    Location newLocation = new Location(
                            active: true,
                            uniqueIdentifierForServer: uniqueIdentfierForServer,
                            location: locationTagInXml.location.toString(),
                            label: locationTagInXml.Label.toString(),
                            browser: browserForLocation,
                            wptServer: wptserverForLocation,
                            dateCreated: new Date(),
                            lastUpdated: new Date(),
                            deviceType: parseDeviceType(locationTagInXml.Label.toString()),
                            operatingSystem: parseOperatingSystem(locationTagInXml.Label.toString())
                    ).save(failOnError: true)
                    addedLocations << newLocation
                    log.info("new location written while fetching locations: ${newLocation}")
                } else if (locationsForCurrentBrowserAndWptServer.size() > 1) {
                    log.error("Multiple Locations (${locations.size()}) found for WPT-Server: ${wptserverForLocation}, Browser: ${browserForLocation}, Location: ${locationTagInXml.id.toString()} - Skipping work!")
                }
            }
        }
        deactivateNotMatchingLocations(wptserverForLocation, locationIdentifiersForWptServer)
        log.info("Location.count after creating non-existent= ${Location.count()}")
        return addedLocations
    }

    /**
     * Deactivates all stored locations which do not match the passed identifiers
     * @param webPageTestServer the webPageTestServer
     * @param uniqueIdentifiersToKeepActive the locationIdentifiers for the webPageTestServer which should not be deactivated
     */
    void deactivateNotMatchingLocations(WebPageTestServer webPageTestServer, List<String> uniqueIdentifiersToKeepActive) {
        if (uniqueIdentifiersToKeepActive) {
            List<Location> locationsToDeactivate = Location.createCriteria().list {
                eq('wptServer', webPageTestServer)
                not { 'in'('uniqueIdentifierForServer', uniqueIdentifiersToKeepActive) }
                eq('active', true)
            }
            locationsToDeactivate.each { currentLocation ->
                currentLocation.active = false
                currentLocation.save(failOnError: true)
            }
        }
    }

    /**
     * Parses the location label to determine the possible device type
     * @param label location label for wptServer
     */
    DeviceType parseDeviceType(String label) {
        switch (label) {
            case ~/(?i).*(-Win|IE\\s*[1-9]*|firefox|nuc).*/ :
                return DeviceType.DESKTOP
            case ~/(?i).*(Pad|Tab|Note|Xoom|Book|Tablet).*/ :
                return DeviceType.TABLET
            case ~/(?i)(?!(.*(Pad|Tab|Note|Xoom|Book|Tablet).*)).*(Samsung|Moto|Sony|Nexus|Huawei|Nokia|Alcatel|LG|OnePlus|HTC|Phone).*/ :
                return DeviceType.SMARTPHONE
            default: return DeviceType.UNDEFINED
        }
    }

    /**
     * Parses the location label to determine the possible operating system
     * @param label location label for wptServer
     */
    OperatingSystem parseOperatingSystem(String label) {
        switch (label) {
            case ~/(?i).*(-Win|IE\\s*[1-9]*|firefox|nuc).*/ :
                return OperatingSystem.WINDOWS
            case ~/(?i)(?!(.*(Android|Desktop).*)).*(ios|iphone|ipad).*/ :
                return OperatingSystem.IOS
            case ~/(?i).*(Samsung|Moto|Sony|Nexus|Huawei|Nokie|LG|HTC|Alcatel|OnePlus).*/ :
                return OperatingSystem.ANDROID
            default: return OperatingSystem.UNKOWN
        }
    }
}

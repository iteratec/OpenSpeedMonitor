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

import de.iteratec.osm.measurement.environment.Browser
import de.iteratec.osm.measurement.environment.BrowserService
import de.iteratec.osm.measurement.environment.Location
import de.iteratec.osm.measurement.environment.WebPageTestServer
import de.iteratec.osm.util.PerformanceLoggingService
import grails.transaction.Transactional
import groovy.util.slurpersupport.GPathResult

/**
 * Persists locations and results. Observer of ProxyService.
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
            // for wpt versions above 2.18 there is a Browsers-Attribute
            List<String> browserNames = locationTagInXml.Browsers.size() != 0 ? locationTagInXml.Browsers.toString().split(",") : [locationTagInXml.Browser.toString()]
            List<Browser> browsersForLocation = browserService.findAllByNameOrAlias(browserNames)
            browsersForLocation.each { currentBrowser ->
                String uniqueIdentfierForServer = locationTagInXml.id.toString().endsWith(":${currentBrowser.name}") ?: locationTagInXml.id.toString() + ":${currentBrowser.name}"
                locationIdentifiersForWptServer << uniqueIdentfierForServer
                List<Location> locationsForCurrentBrowserAndWptServer = Location.findAllByWptServerAndUniqueIdentifierForServerAndBrowser(wptserverForLocation, uniqueIdentfierForServer, currentBrowser)
                if (!locationsForCurrentBrowserAndWptServer) {
                    Location newLocation = new Location(
                            active: true,
                            uniqueIdentifierForServer: uniqueIdentfierForServer, // z.B. Agent1-wptdriver:Firefox
                            location: locationTagInXml.location.toString(),//z.B. Agent1-wptdriver
                            label: locationTagInXml.Label.toString(),//z.B. Agent 1: Windows 7 (S008178178)
                            browser: currentBrowser,//z.B. Firefox
                            wptServer: wptserverForLocation,
                            dateCreated: new Date(),
                            lastUpdated: new Date()
                    ).save(failOnError: true);
                    addedLocations << newLocation
                    log.info("new location written while fetching locations: ${newLocation}")
                } else if (locationsForCurrentBrowserAndWptServer.size() > 1) {
                    log.error("Multiple Locations (${locations.size()}) found for WPT-Server: ${wptserverForLocation}, Browser: ${currentBrowser}, Location: ${locationTagInXml.id.toString()} - Skipping work!")
                }
            }
        }

        deactivateLocations(wptserverForLocation, locationIdentifiersForWptServer)

        log.info("Location.count after creating non-existent= ${Location.count()}")

        return addedLocations
    }

    /**
     * Deactivates all stored locations which no longer exist for given webPageTestServer
     * @param webPageTestServer the webPageTestServer
     * @param uniqueIdentifiersForServer the locationIdentifiers for the webPageTestServer
     */
    void deactivateLocations(WebPageTestServer webPageTestServer, List<String> uniqueIdentifiersForServer) {
        if (uniqueIdentifiersForServer) {
            List<Location> locationsToDeactivate = Location.createCriteria().list {
                eq('wptServer', webPageTestServer)
                not { 'in'('uniqueIdentifierForServer', uniqueIdentifiersForServer) }
                eq('active', true)
            }
            locationsToDeactivate.each { currentLocation ->
                currentLocation.active = false
                currentLocation.save(failOnError: true)
            }
        }
    }
}

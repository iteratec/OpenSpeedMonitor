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

import de.iteratec.osm.csi.transformation.TimeToCsMappingService
import de.iteratec.osm.measurement.environment.Browser
import de.iteratec.osm.measurement.environment.BrowserService
import de.iteratec.osm.measurement.environment.Location
import de.iteratec.osm.measurement.environment.WebPageTestServer
import de.iteratec.osm.result.PageService
import de.iteratec.osm.util.PerformanceLoggingService
import grails.transaction.Transactional
import grails.web.mapping.LinkGenerator
import groovy.util.slurpersupport.GPathResult

/**
 * Persists locations and results. Observer of ProxyService.
 * @author rschuett , nkuhn
 * grails-app/services/de/iteratec/ispc/ResultPersisterService.groovy
 */
class LocationPersisterService implements iLocationListener {

    BrowserService browserService
    TimeToCsMappingService timeToCsMappingService
    PageService pageService
    PerformanceLoggingService performanceLoggingService
    LinkGenerator grailsLinkGenerator

    /**
     * Persisting non-existent locations.
     */
    @Override
    String getListenerName() {
        "LocationPersisterService"
    }

    @Override
    @Transactional
    public List<Location> listenToLocations(GPathResult result, WebPageTestServer wptserverForLocation) {
        List<Location> addedLocations = []

        log.info("Location.count before creating non-existent= ${Location.count()}")
        def query
        result.data.location.each { locationTagInXml ->
            Browser browserOfLocation = browserService.findByNameOrAlias(locationTagInXml.Browser.toString())
            query = Location.where {
                wptServer == wptserverForLocation && browser == browserOfLocation && uniqueIdentifierForServer == locationTagInXml.id.toString()
            }

            List<Location> locations = query.list();

            if (locations.size() == 0) {
                Location newLocation = new Location(
                        active: true,
                        valid: 1,
                        uniqueIdentifierForServer: locationTagInXml.id.toString(), // z.B. Agent1-wptdriver:Chrome
                        location: locationTagInXml.location.toString(),//z.B. Agent1-wptdriver
                        label: locationTagInXml.Label.toString(),//z.B. Agent 1: Windows 7 (S008178178)
                        browser: browserOfLocation,//z.B. Firefox
                        wptServer: wptserverForLocation,
                        dateCreated: new Date(),
                        lastUpdated: new Date()
                ).save(failOnError: true);
                addedLocations << newLocation
                log.info("new location written while fetching locations: ${newLocation}")
            } else if (locations.size() > 1) {
                log.error("Multiple Locations (${locations.size()}) found for WPT-Server: ${wptserverForLocation}, Browser: ${browserOfLocation}, Location: ${locationTagInXml.id.toString()} - Skipping work!")
            }

        }
        log.info("Location.count after creating non-existent= ${Location.count()}")

        return addedLocations
    }
}

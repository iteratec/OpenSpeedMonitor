package de.iteratec.osm.measurement.environment

import de.iteratec.osm.measurement.environment.wptserverproxy.ProxyService
import grails.transaction.Transactional

@Transactional
class WptServerService {

    ProxyService proxyService

    static final String OFFICIAL_WPT_URL = "www.webpagetest.org"

    List<Location> tryMakeServerAndGetLocations(String serverSelect, String serverName, String serverUrl, String serverApiKey) {
        WebPageTestServer server = new WebPageTestServer()
        server.active = true
        if (serverSelect == OFFICIAL_WPT_URL) {
            server.label = OFFICIAL_WPT_URL
            server.proxyIdentifier = OFFICIAL_WPT_URL
            server.baseUrl = "http://${OFFICIAL_WPT_URL}"
            server.apiKey = serverApiKey
        }
        else {
            server.label = serverName
            server.proxyIdentifier = serverName
            server.baseUrl = serverUrl.startsWith("http://") || serverUrl.startsWith("https://")|| serverUrl.startsWith("localhost:") ? serverUrl : "http://${serverUrl}"
            server.apiKey = serverApiKey
        }

        if (server.validate()) {
            try {
                WebPageTestServer.withNewTransaction {
                    server.save(failOnError: true)
                }
            }
            catch (Exception e) {
                log.error("An error occured while saving the wpt server '${server.label}'.", e)
                return [];
            }
            return tryFetchLocations(server);
        }
        else {
            log.error("WebPagetest server '${server.label}' couldn't be saved cause of validation errors: ${server.errors}")
            return [];
        }
    }

    List<Location> tryFetchLocations(WebPageTestServer server) {
        try {
            List<Location> addedLocations = proxyService.fetchLocations(server)
            return addedLocations;
        }
        catch (Exception e) {
            log.error("An error occured while fetching locations from wpt server '${server.label}'.", e)
            server.delete()
            return [];
        }
    }
}

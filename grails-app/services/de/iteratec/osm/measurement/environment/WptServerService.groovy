package de.iteratec.osm.measurement.environment

import de.iteratec.osm.ConfigService
import de.iteratec.osm.measurement.environment.wptserverproxy.ProxyService
import grails.transaction.Transactional

@Transactional
class WptServerService {
    ProxyService proxyService

    static final String WPT_URL = "www.webpagetest.org"

    List<Location> tryMakeServerAndGetLocations(String serverSelect, String inputWPTKey, String inputServerName, String inputServerAddress) {
        WebPageTestServer server = new WebPageTestServer()
        server.active = true
        if (serverSelect == "WPTServer") {
            server.label = WPT_URL
            server.proxyIdentifier = WPT_URL
            server.baseUrl = "http://"+WPT_URL
            server.apiKey = inputWPTKey
        }
        else {
            server.label = inputServerName
            server.proxyIdentifier = inputServerName
            server.baseUrl = inputServerAddress
            if (!server.baseUrl.contains("://")) {
                server.baseUrl = "http://" + server.baseUrl
            }
        }

        if (server.validate()) {
            try {
                WebPageTestServer.withNewTransaction {
                    server.save(failOnError: true)
                }
            }
            catch (Exception e) {
                log.error("An error occured while saving the wpt server '${server.label}'.", e)
                System.out.println(e);
                return [];
            }
            return tryFetchLocations(server);
        }
        else {
            log.error("WebPagetest server '${server.label}'couldn't be saved cause of validation errors")
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
            System.out.println(e);
            server.delete()
            return [];
        }
    }
}

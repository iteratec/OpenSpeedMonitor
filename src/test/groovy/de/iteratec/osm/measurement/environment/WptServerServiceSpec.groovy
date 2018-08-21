package de.iteratec.osm.measurement.environment


import de.iteratec.osm.measurement.environment.wptserver.WptInstructionService
import grails.buildtestdata.BuildDataTest
import grails.buildtestdata.mixin.Build
import grails.testing.services.ServiceUnitTest
import spock.lang.Specification

@Build([Location, Browser, WebPageTestServer])
class WptServerServiceSpec extends Specification implements BuildDataTest, ServiceUnitTest<WptServerService> {
    void setupSpec() {
        mockDomains(WebPageTestServer, Location)
    }

    void "Create official wpt-server (webpagetest.org)"() {
        setup: "WptInstructionService returns 3 locations for the server to create."
        service.wptInstructionService = Stub(WptInstructionService) {
            fetchLocations(_) >> { WebPageTestServer server -> [Location.build(wptServer: server)] }
        }
        when: "Trying to make official wpt-server."
        List<Location> addedLocations = service.tryMakeServerAndGetLocations(
            WptServerService.OFFICIAL_WPT_URL,
            "",
            "",
            "apiKey"
        )
        then: "Official wpt server was created with the default wpt url and tryMakeServerAndGetLocations returns all locations."
        List<WebPageTestServer> wptServers = WebPageTestServer.list()
        wptServers.size() == 1
        wptServers[0].label == WptServerService.OFFICIAL_WPT_URL
        wptServers[0].proxyIdentifier == WptServerService.OFFICIAL_WPT_URL
        wptServers[0].baseUrl == "http://${WptServerService.OFFICIAL_WPT_URL}/"
        wptServers[0].apiKey
        addedLocations.size() == 1
    }

    void "Create custom server without API key."() {
        setup: "WptInstructionService returns 3 locations for the server to create."
        service.wptInstructionService = Stub(WptInstructionService) {
            fetchLocations(_) >> { WebPageTestServer server -> [Location.build(wptServer: server)] }
        }
        when: "Trying to make a custom server without API key."
        List<Location> addedLocations = service.tryMakeServerAndGetLocations(
            "custom",
            "custom.de",
            "www.custom.de",
            ""
        )
        then: "Custom wpt server without API key got created."
        List<WebPageTestServer> wptServers = WebPageTestServer.list()
        wptServers.size() == 1
        wptServers[0].label == "custom.de"
        wptServers[0].proxyIdentifier == "custom.de"
        wptServers[0].baseUrl == "http://www.custom.de/"
        !wptServers[0].apiKey
        addedLocations.size() == 1
    }

    void "Create custom server with API key."() {
        setup: "WptInstructionService returns 3 locations for the server to create."
        service.wptInstructionService = Stub(WptInstructionService) {
            fetchLocations(_) >> { WebPageTestServer server -> [Location.build(wptServer: server)] }
        }
        when: "Trying to make a custom server with API key."
        List<Location> addedLocations = service.tryMakeServerAndGetLocations(
                "custom",
                "custom.de",
                "www.custom.de",
                "apiKey"
        )
        then: "Custom wpt server with API key got created."
        List<WebPageTestServer> wptServers = WebPageTestServer.list()
        wptServers.size() == 1
        wptServers[0].label == "custom.de"
        wptServers[0].proxyIdentifier == "custom.de"
        wptServers[0].baseUrl == "http://www.custom.de/"
        wptServers[0].apiKey
        addedLocations.size() == 1
    }

    void "test with invalid url"() {
        setup: "WptInstructionService returns no locations for the server to create."
        service.wptInstructionService = Stub(WptInstructionService) {
            fetchLocations(_) >> []
        }
        when: "Trying to make a custom wpt server with an invalid url."
        List<Location> addedLocations = service.tryMakeServerAndGetLocations(
            "custom",
            "name",
            "?invalidUrl?",
            ""
        )
        then: "It should not be saved and tryMakeServerAndGetLocations returns an empty list of Locations."
        WebPageTestServer.list().size() == 0
        addedLocations.size() == 0
    }

    void "test with invalid server"() {
        setup: "WptInstructionService throws exception trying to fetch Locations."
        service.wptInstructionService = Stub(WptInstructionService) {
            fetchLocations(_) >> { WebPageTestServer server -> throw Exception("No valid WptServer.") }
        }
        when: "Trying to make a custom wpt server which can't be reached to fetch Locations."
        List<Location> addedLocations = service.tryMakeServerAndGetLocations(
            "custom",
            "test",
            "http://no.wpt.server.com",
            ""
        )
        then: "It should not be saved and tryMakeServerAndGetLocations returns an empty list of Locations."
        WebPageTestServer.list().size() == 0
        addedLocations.size() == 0
    }

}

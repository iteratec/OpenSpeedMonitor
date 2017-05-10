package de.iteratec.osm.measurement.environment

import de.iteratec.osm.measurement.environment.wptserverproxy.ProxyService
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.web.ControllerUnitTestMixin} for usage instructions
 */
@TestFor(WptServerService)
@Mock([WebPageTestServer,Location])
class WptServerServiceSpec extends Specification {

    def setup() {
    }

    def cleanup() {
    }

    void "test with working wpt-server"() {
        setup: "the expected locations"
            mockProxyService(3)
        when: "trying to make a standard wpt-server with a valid key"
            List<Location> addedLocations = service.tryMakeServerAndGetLocations("WPTServer","key","","","")
        then:"it should make a server with the default wpt url and return a list with all the locations"
            WebPageTestServer.list().size() == 1
            WebPageTestServer.list()[0].label == WptServerService.WPT_URL
            WebPageTestServer.list()[0].proxyIdentifier == WptServerService.WPT_URL
            WebPageTestServer.list()[0].baseUrl == "http://${WptServerService.WPT_URL}/"
            addedLocations.size() == 3
    }

    void "test with working custom server"() {
        setup: "the expected locations"
            mockProxyService(3)
        when: "trying to make a custom server with a valid key"
            List<Location> addedLocations = service.tryMakeServerAndGetLocations("CustomServer","","custom","www.custom.de","")
        then:"it should make a server with the specified data and return a list with all the locations"
            WebPageTestServer.list().size() == 1
            WebPageTestServer.list()[0].label == "custom"
            WebPageTestServer.list()[0].proxyIdentifier == "custom"
            WebPageTestServer.list()[0].baseUrl == "http://www.custom.de/"
            addedLocations.size() == 3
    }

    void "test with invalid address"() {
        setup: "no locations"
            mockProxyService(0)
        when: "trying to make a custom server with an invalid address"
            List<Location> addedLocations = service.tryMakeServerAndGetLocations("CustomServer","","name","","")
        then:"it should not be saved and return an empty list"
            WebPageTestServer.list().size() == 0
            addedLocations.size() == 0
    }

    void "test with invalid server"() {
        setup: "no locations"
            mockProxyService(0)
        when: "trying to make a custom server which is not a wpt-server"
            List<Location> addedLocations = service.tryMakeServerAndGetLocations("CustomServer","","test","www.test.de","")
        then:"it should not be saved and return an empty list"
            WebPageTestServer.list().size() == 0
            addedLocations.size() == 0
    }

    private mockProxyService(int n){
        ProxyService.metaClass.fetchLocations = {WebPageTestServer server ->
            if (n > 0) {
                List<Location> list = [];
                for (int i = 0; i < n; i++) {
                    list.add(new Location(label: "label", active: true, wptServer: server, location: "location", browser: new Browser(name: "browser")));
                }
                return list;
            }
            else {
                throw new Exception("exception: can't get locations");
            }
        }
        service.proxyService = new ProxyService()
    }
}

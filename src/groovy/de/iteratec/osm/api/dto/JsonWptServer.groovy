package de.iteratec.osm.api.dto

import de.iteratec.osm.csi.Page
import de.iteratec.osm.measurement.environment.WebPageTestServer


class JsonWptServer {

    long id
    String label
    String proxyIdentifier

    Date dateCreated
    Date lastUpdated

    String baseUrl
    Boolean active
    String description
    String contactPersonName
    String contactPersonEmail

    public static JsonWptServer create(WebPageTestServer webPageTestServer) {
        JsonWptServer result = new JsonWptServer()

        result.id = webPageTestServer.id
        result.label = webPageTestServer.label
        result.proxyIdentifier = webPageTestServer.proxyIdentifier
        result.dateCreated = webPageTestServer.dateCreated
        result.lastUpdated = webPageTestServer.lastUpdated
        result.baseUrl = webPageTestServer.baseUrl
        result.active = webPageTestServer.active
        result.description = webPageTestServer.description
        result.contactPersonName = webPageTestServer.contactPersonName
        result.contactPersonEmail = webPageTestServer.contactPersonEmail

        return result
    }

    public static Collection<JsonWptServer> create(Collection<WebPageTestServer> webPageTestServers) {
        Set<JsonWptServer> result = []

        webPageTestServers.each {
            result.add(create(it))
        }

        return result
    }
}

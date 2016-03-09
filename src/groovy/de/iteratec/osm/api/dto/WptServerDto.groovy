package de.iteratec.osm.api.dto

import de.iteratec.osm.measurement.environment.WebPageTestServer


class WptServerDto {

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

    public static WptServerDto create(WebPageTestServer webPageTestServer) {
        WptServerDto result = new WptServerDto()

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

    public static Collection<WptServerDto> create(Collection<WebPageTestServer> webPageTestServers) {
        Set<WptServerDto> result = []

        webPageTestServers.each {
            result.add(create(it))
        }

        return result
    }
}

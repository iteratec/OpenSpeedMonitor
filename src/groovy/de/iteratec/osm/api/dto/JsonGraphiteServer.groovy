package de.iteratec.osm.api.dto

import de.iteratec.osm.report.external.GraphiteServer

/**
 * Created by birger on 04/03/16.
 */
class JsonGraphiteServer {

    long id

    String serverAddress

    int port

    public static JsonGraphiteServer create(GraphiteServer graphiteServer) {
        JsonGraphiteServer result = new JsonGraphiteServer()

        result.id = graphiteServer.id
        result.serverAddress = graphiteServer.serverAdress
        result.port = graphiteServer.port

        return result
    }
    public static Set<JsonGraphiteServer> create(Collection<GraphiteServer> graphiteServers) {
        Set<JsonGraphiteServer> result = []

        graphiteServers.each {
            result.add(create(it))
        }

        return result
    }
}

package de.iteratec.osm.api.dto

class GraphiteServerDto {

    long id

    String serverAddress

    int port

    public static GraphiteServerDto create(de.iteratec.osm.report.external.GraphiteServer graphiteServer) {
        GraphiteServerDto result = new GraphiteServerDto()

        result.id = graphiteServer.id
        result.serverAddress = graphiteServer.serverAdress
        result.port = graphiteServer.port

        return result
    }
    public static Set<GraphiteServerDto> create(Collection<de.iteratec.osm.report.external.GraphiteServer> graphiteServers) {
        Set<GraphiteServerDto> result = []

        graphiteServers.each {
            result.add(create(it))
        }

        return result
    }
}

package de.iteratec.osm.result

class WptStatusFactory {
    def buildWptStatus(Integer wptStatusCode) {
        return WptStatus.values().find { it.matchesStatusCode(wptStatusCode) } ?: WptStatus.UNKNOWN
    }
}

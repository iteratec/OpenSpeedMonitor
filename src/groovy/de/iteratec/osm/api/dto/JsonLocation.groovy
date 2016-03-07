package de.iteratec.osm.api.dto

import de.iteratec.osm.measurement.environment.Location


class JsonLocation {

    long id
    String label
    String uniqueIdentifierForServer
    Date dateCreated
    Date lastUpdated
    boolean active
    int valid
    String location
    JsonBrowser browser
    JsonWptServer wptServer
    int activeagents
    int queuethreshold
    int queuethresholdgreenlimit
    int queuethresholdyellowlimit
    int queuethresholdredlimit

    public static JsonLocation create(Location location) {
        JsonLocation result = new JsonLocation()

        result.id = location.id
        result.label = location.label
        result.dateCreated = location.dateCreated
        result.lastUpdated = location.lastUpdated
        result.active = location.active
        result.valid = location.valid
        result.location = location.location
        result.browser = JsonBrowser.create(location.browser)
        result.wptServer = JsonWptServer.create(location.wptServer)
        result.activeagents = location.activeagents
        result.queuethreshold = location.queuethreshold
        result.queuethresholdgreenlimit = location.queuethresholdgreenlimit
        result.queuethresholdyellowlimit = location.queuethresholdyellowlimit
        result.queuethresholdredlimit = location.queuethresholdredlimit

        return result
    }

    public static Collection<JsonLocation> create(Collection<Location> locations) {
        Set<JsonLocation> result = []

        locations.each {
            result.add(create(it))
        }

        return result
    }
}

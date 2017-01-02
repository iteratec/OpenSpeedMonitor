package de.iteratec.osm.api.dto

import de.iteratec.osm.measurement.environment.Location


class LocationDto {

    long id
    String label
    String uniqueIdentifierForServer
    Date dateCreated
    Date lastUpdated
    boolean active
    String location
    BrowserDto browser
    WptServerDto wptServer
    int activeagents
    int queuethreshold
    int queuethresholdgreenlimit
    int queuethresholdyellowlimit
    int queuethresholdredlimit

    public static LocationDto create(Location location) {
        LocationDto result = new LocationDto()

        result.id = location.id
        result.uniqueIdentifierForServer = location.uniqueIdentifierForServer
        result.label = location.label
        result.dateCreated = location.dateCreated
        result.lastUpdated = location.lastUpdated
        result.active = location.active
        result.location = location.location
        result.browser = BrowserDto.create(location.browser)
        result.wptServer = WptServerDto.create(location.wptServer)
        result.activeagents = location.activeagents
        result.queuethreshold = location.queuethreshold
        result.queuethresholdgreenlimit = location.queuethresholdgreenlimit
        result.queuethresholdyellowlimit = location.queuethresholdyellowlimit
        result.queuethresholdredlimit = location.queuethresholdredlimit

        return result
    }

    public static Collection<LocationDto> create(Collection<Location> locations) {
        Set<LocationDto> result = []

        locations.each {
            result.add(create(it))
        }

        return result
    }
}

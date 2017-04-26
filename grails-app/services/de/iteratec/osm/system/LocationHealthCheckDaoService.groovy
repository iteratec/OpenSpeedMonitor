package de.iteratec.osm.system

import de.iteratec.osm.measurement.environment.Location
import grails.transaction.Transactional

@Transactional
class LocationHealthCheckDaoService {

    List<LocationHealthCheck> getLatestHealthChecksFor(List<Location> locations) {
        List<LocationHealthCheck> healthChecks = []
        locations.each {Location location ->
            healthChecks.addAll(
                (List<LocationHealthCheck>)LocationHealthCheck.createCriteria().list(max: 1) {
                    eq("location", location)
                    order("date", "desc")
                }
            )
        }
        return healthChecks
    }

}

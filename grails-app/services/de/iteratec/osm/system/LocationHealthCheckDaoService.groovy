package de.iteratec.osm.system

import de.iteratec.osm.measurement.environment.Location
import grails.gorm.transactions.Transactional

@Transactional
class LocationHealthCheckDaoService {

    /**
     * Gets the most up to date {@link LocationHealthCheck} for each of the given locations.
     * @param locations
     *          List of {@link Location}s for which the most up to date {@link LocationHealthCheck} should be found.
     * @return The most up to date {@link LocationHealthCheck} for each of the given locations.
     */
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

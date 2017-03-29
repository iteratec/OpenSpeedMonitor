package de.iteratec.osm.system

import de.iteratec.osm.measurement.environment.Location

/**
 * Result of a single health check for a single {@link Location}.
 * These checks should run regularly via quartz.
 */
class LocationHealthCheck {

    Date date

    Integer numberOfAgents
    Integer numberOfPendingJobsInWpt

    Integer numberOfJobResultsLastHour
    Integer numberOfEventResultsLastHour
    Integer numberOfErrorsLastHour

    Integer numberOfJobResultsNextHour
    Integer numberOfEventResultsNextHour

    Integer numberOfCurrentlyPendingJobs
    Integer numberOfCurrentlyRunningJobs

    static belongsTo = [location: Location]

    static constraints = {
    }
}

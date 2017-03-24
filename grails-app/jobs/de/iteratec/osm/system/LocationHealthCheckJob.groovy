package de.iteratec.osm.system

class LocationHealthCheckJob {

    LocationHealthCheckService locationHealthCheckService

    static triggers = {
        /**
         * Every 30 minutes.
         */
        cron(name: 'locationHealthCheckJob', cronExpression: '0 */30 * ? * *')
    }

    /**
     * Gathering informations for every active location of every active wpt server (in osm).
     * These informations are shown in queue status view and used to decide whether a queue is overflowing and job scheduling
     * should be paused.
     */
    def execute() {
        locationHealthCheckService.runHealthChecksForAllActiveLocations()

    }
}

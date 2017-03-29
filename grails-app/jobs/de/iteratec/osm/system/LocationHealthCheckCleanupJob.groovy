package de.iteratec.osm.system

class LocationHealthCheckCleanupJob {

    LocationHealthCheckService locationHealthCheckService

    static triggers = {
        /**
         * Each Day at 2:15 am.
         */
        cron(name: 'LocationHealthCheckCleanupJob', cronExpression: '0 15 2 ? * *')
    }

    def execute() {
        locationHealthCheckService.cleanupHealthChecks()
    }
}

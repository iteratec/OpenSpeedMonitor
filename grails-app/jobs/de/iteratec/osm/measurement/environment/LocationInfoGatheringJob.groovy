package de.iteratec.osm.measurement.environment

class LocationInfoGatheringJob {
    static triggers = {
        /**
         * Once every 10 minutes
         */
        simple repeatInterval: (1000*60*10)l
    }

    /**
     * Gathering informations for every active location of every active wpt server (in osm), which exists in locations.ini
     * of wpt server.
     * These informations are shown in queue status view and used to decide whether a queue is overflowing and job scheduling
     * should be paused.
     */
    def execute() {

    }
}

package de.iteratec.osm.report.external

/**
 * Triggers daily Graphite Event fetching
 */
class GraphiteEventCollectorJob {

    GraphiteEventService graphiteEventService
    boolean createBatchActivity = true

    static triggers = {
        /** Each Day at 0:35 am. */
        cron(name: 'DailyGraphiteEventCollection', cronExpression: '0 35 0 ? * *')
    }

    def execute() {
        //graphiteEventService.fetchGraphiteEvents(createBatchActivity)
    }
}

package de.iteratec.osm.report.external

/**
 * Triggers daily Graphite Event fetching
 */
class GraphiteEventCollectorJob {

    GraphiteEventService graphiteEventService
    boolean createBatchActivity = false

    static triggers = {
        /** Each Hour at Minute 30. */
        cron(name: 'HourlyGraphiteEventCollection', cronExpression: '0 30 * ? * *')
    }

    def execute() {
        graphiteEventService.fetchGraphiteEvents(createBatchActivity, 60)
    }
}

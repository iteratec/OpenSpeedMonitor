package de.iteratec.osm.report.external


import de.iteratec.osm.result.EventResult
import de.iteratec.osm.util.PerformanceLoggingService
import org.quartz.JobExecutionContext

class GraphiteReportJob {

    MetricReportingService metricReportingService

    static triggers = {}

    def execute(JobExecutionContext context) {
        EventResult result = context.mergedJobDataMap.get('eventResult')

        try {
            metricReportingService.reportEventResultToGraphite(result)
        } catch (GraphiteComunicationFailureException gcfe) {
            log.error("Can't report EventResult to graphite-server: ${gcfe.message}")
        } catch (Exception e) {
            log.error("An error occurred while reporting EventResult to graphite.", e)
        }
    }
}

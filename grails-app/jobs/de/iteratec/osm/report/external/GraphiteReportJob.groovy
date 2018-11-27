package de.iteratec.osm.report.external

import de.iteratec.osm.measurement.environment.wptserver.EventResultPersisterService
import de.iteratec.osm.measurement.environment.wptserver.WptResultXml
import de.iteratec.osm.util.PerformanceLoggingService
import org.quartz.JobExecutionContext
import static de.iteratec.osm.util.PerformanceLoggingService.LogLevel.DEBUG

class GraphiteReportJob {

    EventResultPersisterService eventResultPersisterService
    PerformanceLoggingService performanceLoggingService

    static triggers = {}

    def execute(JobExecutionContext context) {
        WptResultXml resultXml = context.mergedJobDataMap.get('resultXml')
        Long jobId = context.mergedJobDataMap.getLong('jobId')
        if(resultXml && jobId) {
            performanceLoggingService.logExecutionTime(DEBUG, "Inform Dependents for Job $jobId", 1) {
                eventResultPersisterService.informDependents(resultXml, jobId)
            }
        }
    }
}

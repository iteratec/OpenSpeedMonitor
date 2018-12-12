package de.iteratec.osm.report.external

import de.iteratec.osm.measurement.environment.wptserver.OsmResultPersistanceException
import de.iteratec.osm.measurement.schedule.Job
import de.iteratec.osm.result.EventResult
import de.iteratec.osm.result.JobResult
import org.quartz.JobExecutionContext

class GraphiteReportJob {

    MetricReportingService metricReportingService
    static triggers = {}

    def execute(JobExecutionContext context) {
        Long jobId = context.mergedJobDataMap.get('jobId')
        String testId = context.mergedJobDataMap.get('testId')
        if(jobId && testId) {
            Job job = Job.findById(jobId)
            JobResult jobResult = JobResult.findByJobAndTestId(job, testId)
            if(jobResult) {
                List<EventResult> results = jobResult.getEventResults()
                results.each { EventResult result ->
                    if(result.medianValue) {
                        log.debug('reporting persisted event result ...')
                        report(result)
                        log.debug('reporting persisted event result ... DONE')
                    }
                }
            }
            else {
                throw new OsmResultPersistanceException("No JobResult for Job $jobId with TestId $testId")
            }
        }
    }

    private void report(EventResult result) {
        try {
            metricReportingService.reportEventResultToGraphite(result)
        } catch (GraphiteComunicationFailureException gcfe) {
            log.error("Can't report EventResult to graphite-server: ${gcfe.message}")
        } catch (Exception e) {
            log.error("An error occurred while reporting EventResult to graphite.", e)
        }
    }
}

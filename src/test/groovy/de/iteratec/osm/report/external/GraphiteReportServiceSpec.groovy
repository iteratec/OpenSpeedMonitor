package de.iteratec.osm.report.external

import de.iteratec.osm.measurement.schedule.Job
import de.iteratec.osm.result.EventResult
import de.iteratec.osm.result.JobResult
import de.iteratec.osm.result.JobResultStatus
import de.iteratec.osm.result.WptStatus
import grails.buildtestdata.BuildDataTest
import grails.buildtestdata.mixin.Build
import org.quartz.JobExecutionContext
import spock.lang.Specification
import spock.lang.Unroll

@Unroll
@Build([EventResult, Job])
class GraphiteReportServiceSpec extends Specification implements BuildDataTest {

    void "GraphiteReportJob should report from JobExecutionContext for Job #jobId"() {
        given:
        GraphiteReportJob graphiteReportJob = new GraphiteReportJob()
        graphiteReportJob.metricReportingService = Mock(MetricReportingService)

        Job job = Job.build(id: jobId)
        JobResult result = JobResult.build(job: job, testId: testId, id: resultId)
        EventResult eventResult = EventResult.build(jobResult: result, medianValue: true)

        JobExecutionContext context = Stub(JobExecutionContext) {
            getMergedJobDataMap() >> [jobId: jobId, testId: testId]
        }

        when:
        graphiteReportJob.execute(context)

        then:
        1 * graphiteReportJob.metricReportingService.reportEventResultToGraphite(eventResult)

        where:
        jobId   | testId        | resultId
        -1      | "testId-1"    | -100
        -2      | "testId-2"    | -200
        -3      | "testId-3"    | -300
    }

}

package de.iteratec.osm.report.external

import de.iteratec.osm.ConfigService
import de.iteratec.osm.csi.CsiAggregationUpdateService
import de.iteratec.osm.csi.Page
import de.iteratec.osm.measurement.environment.wptserver.EventResultPersisterService
import de.iteratec.osm.measurement.environment.wptserver.WptResultXml
import de.iteratec.osm.measurement.schedule.ConnectivityProfile
import de.iteratec.osm.measurement.schedule.Job
import de.iteratec.osm.measurement.schedule.JobDaoService
import de.iteratec.osm.measurement.script.Script
import de.iteratec.osm.result.EventResult
import de.iteratec.osm.result.JobResult
import de.iteratec.osm.result.MeasuredEvent
import grails.buildtestdata.BuildDataTest
import grails.buildtestdata.mixin.Build
import grails.testing.services.ServiceUnitTest
import spock.lang.Specification
import spock.lang.Unroll

@Unroll
@Build([Page, EventResult, MeasuredEvent])
class FilterResultsToReportSpec extends Specification implements BuildDataTest,
        ServiceUnitTest<EventResultPersisterService> {

    def setup() {
        service.csiAggregationUpdateService = Mock(CsiAggregationUpdateService)
        service.jobDaoService = new JobDaoService()

        service.configService = Stub(ConfigService) {
            getMaxValidLoadtime() >> 1800
            getMinValidLoadtime() >> 100
        }
    }

    void setupSpec() {
        mockDomains(Page, EventResult, MeasuredEvent, ConnectivityProfile, Script)
    }

    void "Report Results for Job #jobId with test #testId"() {
        given:"A JobId and testId"

        Job job = Job.build(id: jobId)
        WptResultXml wptResultXml = Stub(WptResultXml) {
            getTestId() >> testId
        }
        JobResult.build(job: job, testId: testId)

        service.graphiteReportService = Mock(GraphiteReportService)
        service.metricReportingService = Mock(MetricReportingService)

        when: "EventResultPersisterService.informDependents() is called with result and id."
        service.informDependents(wptResultXml, jobId)

        then: "The Result is reported to Graphite."
        1 * service.graphiteReportService.report(jobId, testId)

        where:
        jobId       | testId
        -1          | "test-1"
        -2          | "test-2"

    }
}

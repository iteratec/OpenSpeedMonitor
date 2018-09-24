package de.iteratec.osm.report.external

import de.iteratec.osm.csi.CsiAggregationUpdateService
import de.iteratec.osm.csi.Page
import de.iteratec.osm.measurement.environment.wptserver.ResultPersisterService
import de.iteratec.osm.measurement.schedule.ConnectivityProfile
import de.iteratec.osm.measurement.script.Script
import de.iteratec.osm.result.EventResult
import de.iteratec.osm.result.MeasuredEvent
import grails.buildtestdata.BuildDataTest
import grails.buildtestdata.mixin.Build
import grails.testing.services.ServiceUnitTest
import spock.lang.Specification
import spock.lang.Unroll

@Unroll
@Build([Page, EventResult, MeasuredEvent])
class FilterResultsToReportSpec extends Specification implements BuildDataTest,
        ServiceUnitTest<ResultPersisterService> {

    static final PAGE_NAME_HOMEPAGE = 'Homepage'

    def setup() {
        Page.build(name: PAGE_NAME_HOMEPAGE)
        Page.build(name: Page.UNDEFINED)
        service.csiAggregationUpdateService = Mock(CsiAggregationUpdateService)
    }

    void setupSpec() {
        mockDomains(Page, EventResult, MeasuredEvent, ConnectivityProfile, Script)
    }

    void "Report Results for Page #pageName"() {
        given:"An EventResult with Page #pageName"
        Page page = Page.findByName(pageName)
        EventResult er = EventResult.build(
            measuredEvent: MeasuredEvent.build(testedPage: page),
            page: page,
            medianValue: true
        )
        service.metricReportingService = Mock(MetricReportingService)

        when: "ResultPersisterService.informDependent() is called with the Result."
        service.informDependent(er)

        then: "The Result is reported to Graphite."
        1 * service.metricReportingService.reportEventResultToGraphite(er)

        where:
        pageName            |   _
        PAGE_NAME_HOMEPAGE  |   _
        Page.UNDEFINED      |   _

    }
}
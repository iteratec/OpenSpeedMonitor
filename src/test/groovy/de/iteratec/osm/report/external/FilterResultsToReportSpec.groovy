package de.iteratec.osm.report.external

import de.iteratec.osm.csi.CsiAggregationUpdateService
import de.iteratec.osm.csi.Page
import de.iteratec.osm.measurement.environment.wptserverproxy.ResultPersisterService
import de.iteratec.osm.result.EventResult
import de.iteratec.osm.result.MeasuredEvent
import grails.buildtestdata.BuildDataTest
import grails.buildtestdata.mixin.Build
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import spock.lang.*

/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
@TestMixin(GrailsUnitTestMixin)
@TestFor(ResultPersisterService)
@Unroll
@Mock([Page, EventResult, MeasuredEvent])
@Build([Page, EventResult, MeasuredEvent])
class FilterResultsToReportSpec extends Specification implements BuildDataTest {

    static final PAGE_NAME_HOMEPAGE = 'Homepage'

    def setup() {
        Page.build(name: PAGE_NAME_HOMEPAGE)
        Page.build(name: Page.UNDEFINED)
        service.csiAggregationUpdateService = Mock(CsiAggregationUpdateService)
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
package de.iteratec.osm.result.dao

import de.iteratec.osm.csi.NonTransactionalIntegrationSpec
import de.iteratec.osm.csi.Page
import de.iteratec.osm.measurement.environment.Browser
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.result.*
import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration

@Integration(applicationClass = openspeedmonitor.Application.class)
@Rollback
class QueryAspectsViaBuilderRawSpec extends NonTransactionalIntegrationSpec {

    JobGroup jobGroup
    Page page
    Browser browser
    SelectedMeasurand docComplete, countRequest, startRender, firstContentfulPaint, visuallyCompleteInMillisecs, consistentlyInteractiveInMillisecs

    def setup() {
        jobGroup = JobGroup.build()
        page = Page.build()
        browser = Browser.build()
        docComplete = createMeasurand("DOC_COMPLETE_TIME")
        countRequest = createMeasurand("DOC_COMPLETE_REQUESTS")
        startRender = createMeasurand("START_RENDER")
        firstContentfulPaint = createMeasurand("FIRST_CONTENTFUL_PAINT")
        visuallyCompleteInMillisecs = createMeasurand("VISUALLY_COMPLETE")
        consistentlyInteractiveInMillisecs = createMeasurand("CONSISTENTLY_INTERACTIVE")
    }

    void "query builder doesn't add aspect metrics if no aspect type is queried"() {
        given: "An EventResult with some metrics and an Aspect in db."
        persistAspect(PerformanceAspectType.PAGE_CONSTRUCTION_STARTED, firstContentfulPaint)
        persistEventResult([
                startRenderInMillisecs            : 300,
                firstContentfulPaint              : 400,
                docCompleteTimeInMillisecs        : 1200,
                docCompleteRequests               : 35,
                visuallyCompleteInMillisecs       : 500,
                consistentlyInteractiveInMillisecs: 600
        ])

        when: "the builder is used without querying an aspect"
        EventResultQueryBuilder builder = new EventResultQueryBuilder()
        List<EventResultProjection> results = builder
                .withJobGroupIn([jobGroup])
                .withPageIn([page])
                .withBrowserIdsIn([browser.ident()])
                .withSelectedMeasurands([docComplete])
                .getRawData()

        then: "found EventResult contains directly queried metric BUT no other metrics"
        results.size() == 1
        EventResultProjection result = results[0]
        !result.startRenderInMillisecs
        !result.firstContentfulPaint
        result.docCompleteTimeInMillisecs == 1200
        !result.docCompleteRequests
        !result.visuallyCompleteInMillisecs
        !result.consistentlyInteractiveInMillisecs
        !result.PAGE_CONSTRUCTION_STARTED
    }

    void "query builder gets metrics of selected aspect type additionally if an aspect is persisted"() {
        given: "An EventResult with some metrics and a matching Aspect in db."
        persistAspect(PerformanceAspectType.PAGE_CONSTRUCTION_STARTED, firstContentfulPaint)
        persistEventResult([
                startRenderInMillisecs            : 300,
                firstContentfulPaint              : 400,
                docCompleteTimeInMillisecs        : 1200,
                docCompleteRequests               : 35,
                visuallyCompleteInMillisecs       : 500,
                consistentlyInteractiveInMillisecs: 600
        ])

        when: "the builder is given the aspect type of matching aspect"
        EventResultQueryBuilder builder = new EventResultQueryBuilder()
        List<EventResultProjection> results = builder
                .withJobGroupIn([jobGroup])
                .withPageIn([page])
                .withBrowserIdsIn([browser.ident()])
                .withSelectedMeasurands([docComplete])
                .withPerformanceAspects([PerformanceAspectType.PAGE_CONSTRUCTION_STARTED])
                .getRawData()

        then: "found EventResult contains directly queried metric AND aspect metric"
        results.size() == 1
        EventResultProjection result = results[0]
        !result.startRenderInMillisecs
        result.firstContentfulPaint == 400
        result.docCompleteTimeInMillisecs == 1200
        !result.docCompleteRequests
        !result.visuallyCompleteInMillisecs
        !result.consistentlyInteractiveInMillisecs
        result.PAGE_CONSTRUCTION_STARTED == 400
    }

    void "query builder gets default metric of selected aspect type additionally if no matching aspect is persisted"() {
        given: "An EventResult with some metrics and no matching Aspect in db."
        persistAspect(PerformanceAspectType.PAGE_CONSTRUCTION_STARTED, firstContentfulPaint)
        persistEventResult([
                startRenderInMillisecs            : 300,
                firstContentfulPaint              : 400,
                docCompleteTimeInMillisecs        : 1200,
                docCompleteRequests               : 35,
                visuallyCompleteInMillisecs       : 500,
                consistentlyInteractiveInMillisecs: 600
        ])

        when: "the builder is given the an aspect type without matching aspect"
        EventResultQueryBuilder builder = new EventResultQueryBuilder()
        List<EventResultProjection> results = builder
                .withJobGroupIn([jobGroup])
                .withPageIn([page])
                .withBrowserIdsIn([browser.ident()])
                .withSelectedMeasurands([docComplete])
                .withPerformanceAspects([PerformanceAspectType.PAGE_SHOWS_USEFUL_CONTENT])
                .getRawData()

        then: "found EventResult contains directly queried metric AND aspects default metric"
        results.size() == 1
        EventResultProjection result = results[0]
        !result.startRenderInMillisecs
        !result.firstContentfulPaint
        result.docCompleteTimeInMillisecs == 1200
        !result.docCompleteRequests
        result.visuallyCompleteInMillisecs == 500
        !result.consistentlyInteractiveInMillisecs
        result.PAGE_SHOWS_USEFUL_CONTENT == 500
    }

    void "query builder gets multiple metrics of selected aspect types additionally"() {
        given: "An EventResult with some metrics and one matching Aspect in db."
        persistAspect(PerformanceAspectType.PAGE_CONSTRUCTION_STARTED, firstContentfulPaint)
        persistEventResult([
                startRenderInMillisecs            : 300,
                firstContentfulPaint              : 400,
                docCompleteTimeInMillisecs        : 1200,
                docCompleteRequests               : 35,
                visuallyCompleteInMillisecs       : 500,
                consistentlyInteractiveInMillisecs: 600
        ])

        when: "the builder is given the aspect type of matching aspect and another one without matching aspect"
        EventResultQueryBuilder builder = new EventResultQueryBuilder()
        List<EventResultProjection> results = builder
                .withJobGroupIn([jobGroup])
                .withPageIn([page])
                .withBrowserIdsIn([browser.ident()])
                .withSelectedMeasurands([docComplete])
                .withPerformanceAspects([PerformanceAspectType.PAGE_CONSTRUCTION_STARTED, PerformanceAspectType.PAGE_SHOWS_USEFUL_CONTENT])
                .getRawData()

        then: "found EventResult contains directly queried metric AND aspect metric"
        results.size() == 1
        EventResultProjection result = results[0]
        !result.startRenderInMillisecs
        result.firstContentfulPaint == 400
        result.docCompleteTimeInMillisecs == 1200
        !result.docCompleteRequests
        result.visuallyCompleteInMillisecs == 500
        !result.consistentlyInteractiveInMillisecs
        result.PAGE_CONSTRUCTION_STARTED == 400
        result.PAGE_SHOWS_USEFUL_CONTENT == 500
    }

    void "query builder gets metric of selected aspect type when NO measurand is selected"() {
        given: "an EventResult with some metrics and one matching aspect in db"
        persistAspect(PerformanceAspectType.PAGE_IS_USABLE, consistentlyInteractiveInMillisecs)
        persistEventResult([
                startRenderInMillisecs            : 300,
                firstContentfulPaint              : 400,
                docCompleteTimeInMillisecs        : 1200,
                docCompleteRequests               : 35,
                visuallyCompleteInMillisecs       : 500,
                consistentlyInteractiveInMillisecs: 600
        ])

        when: "the builder is given the correct aspect type and no measurand"
        EventResultQueryBuilder builder = new EventResultQueryBuilder()
        List<EventResultProjection> results = builder
                .withJobGroupIn([jobGroup])
                .withPageIn([page])
                .withBrowserIdsIn([browser.ident()])
                .withPerformanceAspects([PerformanceAspectType.PAGE_IS_USABLE])
                .getRawData()

        then: "the found EventResult contains aspect metric and corresponding metric"
        results.size() == 1
        EventResultProjection result = results[0]
        result.PAGE_IS_USABLE == 600
        result.consistentlyInteractiveInMillisecs == 600
        !result.startRenderInMillisecs
        !result.firstContentfulPaint
        !result.docCompleteTimeInMillisecs
        !result.docCompleteRequests
        !result.visuallyCompleteInMillisecs
    }

    private void persistEventResult(Map<String, Integer> measurands) {
        EventResult result = EventResult.build(
                jobGroup: jobGroup,
                page: page,
                browser: browser,
                cachedView: CachedView.UNCACHED,
                medianValue: true,
        )
        measurands.each { name, value ->
            result[name] = value
        }
        result.save(flush: true)
    }

    private void persistAspect(PerformanceAspectType type, SelectedMeasurand metric) {
        PerformanceAspect.build(
                jobGroup: jobGroup,
                page: page,
                browser: browser,
                performanceAspectType: type,
                metricIdentifier: metric.optionValue,
                cachedView: metric.cachedView
        )
    }

    private SelectedMeasurand createMeasurand(String measurand) {
        return new SelectedMeasurand(measurand, CachedView.UNCACHED)
    }
}

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
class QueryAspectsViaBuilderAverageSpec extends NonTransactionalIntegrationSpec {

    JobGroup jobGroup
    Page page1, page2
    Browser browser1, browser2
    SelectedMeasurand docComplete, countRequest, startRender, firstContentfulPaint, visuallyCompleteInMillisecs, consistentlyInteractiveInMillisecs

    def setup() {
        jobGroup = JobGroup.build()
        page1 = Page.build()
        page2 = Page.build()
        browser1 = Browser.build()
        browser2 = Browser.build()
        docComplete = createMeasurand("DOC_COMPLETE_TIME")
        countRequest = createMeasurand("DOC_COMPLETE_REQUESTS")
        startRender = createMeasurand("START_RENDER")
        firstContentfulPaint = createMeasurand("FIRST_CONTENTFUL_PAINT")
        visuallyCompleteInMillisecs = createMeasurand("VISUALLY_COMPLETE")
        consistentlyInteractiveInMillisecs = createMeasurand("CONSISTENTLY_INTERACTIVE")
    }

    void "avg of aspect metrics, all results share the same aspect metrics, grouped by jobgroup, page + browser"() {
        given: "Ten EventResults, each with some metrics and a matching Aspect in db."
        persistAspect(PerformanceAspectType.PAGE_CONSTRUCTION_STARTED, firstContentfulPaint)
        int resultCounter = 0
        10.times {
            persistEventResult([
                    startRenderInMillisecs            : 300 + resultCounter,
                    firstContentfulPaint              : 400 + resultCounter,
                    docCompleteTimeInMillisecs        : 1200 + resultCounter,
                    docCompleteRequests               : 35 + resultCounter,
                    visuallyCompleteInMillisecs       : 500 + resultCounter,
                    consistentlyInteractiveInMillisecs: 600 + resultCounter
            ])
            resultCounter++
        }

        when: "the builder is given the aspect type of matching aspect"
        EventResultQueryBuilder builder = new EventResultQueryBuilder()
        List<EventResultProjection> results = builder
                .withJobGroupIn([jobGroup])
                .withPageIn([page1])
                .withBrowserIdsIn([browser1.ident()])
                .withSelectedMeasurands([docComplete])
                .withPerformanceAspects([PerformanceAspectType.PAGE_CONSTRUCTION_STARTED])
                .getAverageData()

        then: "avg of EventResults got calculated correctly for aspect metric"
        results.size() == 1
        EventResultProjection result = results[0]
        !result.startRenderInMillisecs
        result.docCompleteTimeInMillisecs == 1204.5
        !result.docCompleteRequests
        !result.visuallyCompleteInMillisecs
        !result.consistentlyInteractiveInMillisecs
        result.PAGE_CONSTRUCTION_STARTED == 404.5
    }

    void "avg of aspect metrics, results with different aspect metrics, grouped by jobgroup + page"() {
        given: "Twenty EventResults with different aspect metrics and matching Aspects in db."
        persistAspect(PerformanceAspectType.PAGE_CONSTRUCTION_STARTED, firstContentfulPaint, page1, browser1)
        persistAspect(PerformanceAspectType.PAGE_CONSTRUCTION_STARTED, startRender, page1, browser2)
        10.times {
            LinkedHashMap<String, Integer> metrics = [
                    startRenderInMillisecs            : 300,
                    firstContentfulPaint              : 400,
                    docCompleteTimeInMillisecs        : 1200,
                    docCompleteRequests               : 35,
                    visuallyCompleteInMillisecs       : 500,
                    consistentlyInteractiveInMillisecs: 600
            ]
            persistEventResult(metrics, page1, browser1)
            persistEventResult(metrics, page1, browser2)
        }

        when: "the builder is given the aspect type of matching aspect"
        EventResultQueryBuilder builder = new EventResultQueryBuilder()
        List<EventResultProjection> results = builder
                .withJobGroupIn([jobGroup])
                .withPageIn([page1])
                .withSelectedMeasurands([docComplete])
                .withPerformanceAspects([PerformanceAspectType.PAGE_CONSTRUCTION_STARTED])
                .getAverageData()

        then: "avg of EventResults got calculated correctly for aspect metric"
        results.size() == 1
        EventResultProjection result = results[0]
        result.docCompleteTimeInMillisecs == 1200
        !result.docCompleteRequests
        !result.visuallyCompleteInMillisecs
        !result.consistentlyInteractiveInMillisecs
        result.PAGE_CONSTRUCTION_STARTED == (10*300+10*400)/20
    }

    void "avg of aspect metrics for multiple aspects, results with different aspect metrics, grouped by jobgroup + page"() {
        given: "Twenty EventResults with different aspect metrics and matching Aspects in db."
        persistAspect(PerformanceAspectType.PAGE_CONSTRUCTION_STARTED, firstContentfulPaint, page1, browser1)
        persistAspect(PerformanceAspectType.PAGE_CONSTRUCTION_STARTED, startRender, page1, browser2)
        persistAspect(PerformanceAspectType.PAGE_SHOWS_USEFUL_CONTENT, consistentlyInteractiveInMillisecs, page1, browser1)
        int visCompleteAsDefault = 500 // for PAGE_SHOWS_USEFUL_CONTENT
        10.times {
            LinkedHashMap<String, Integer> metrics = [
                    startRenderInMillisecs            : 300,
                    firstContentfulPaint              : 400,
                    docCompleteTimeInMillisecs        : 1200,
                    docCompleteRequests               : 35,
                    visuallyCompleteInMillisecs       : visCompleteAsDefault,
                    consistentlyInteractiveInMillisecs: 600
            ]
            persistEventResult(metrics, page1, browser1)
            persistEventResult(metrics, page1, browser2)
        }

        when: "the builder is given the aspect type of matching aspect"
        EventResultQueryBuilder builder = new EventResultQueryBuilder()
        List<EventResultProjection> results = builder
                .withJobGroupIn([jobGroup])
                .withPageIn([page1])
                .withSelectedMeasurands([docComplete])
                .withPerformanceAspects([PerformanceAspectType.PAGE_CONSTRUCTION_STARTED, PerformanceAspectType.PAGE_SHOWS_USEFUL_CONTENT])
                .getAverageData()

        then: "avg of EventResults got calculated correctly for aspect metric"
        results.size() == 1
        EventResultProjection result = results[0]
        result.docCompleteTimeInMillisecs == 1200
        !result.docCompleteRequests
        !result.visuallyCompleteInMillisecs
        !result.consistentlyInteractiveInMillisecs
        result.PAGE_CONSTRUCTION_STARTED == (10*300+10*400)/20
        result.PAGE_SHOWS_USEFUL_CONTENT == (10*600+10*visCompleteAsDefault)/20
    }

    void "avg of aspect metric for one aspects with NO selected measurand, results in one metric, grouped by jobgroup + page"() {
        given: "Ten EventResults with different aspect metrics and matching Aspects in db."
        persistAspect(PerformanceAspectType.PAGE_SHOWS_USEFUL_CONTENT, firstContentfulPaint, page1, browser1)
        int firstContentfulPaintValue = 300
        10.times {
            LinkedHashMap<String, Integer> metrics = [
                    startRenderInMillisecs            : 300,
                    firstContentfulPaint              : firstContentfulPaintValue,
                    docCompleteTimeInMillisecs        : 1200,
                    docCompleteRequests               : 35,
                    visuallyCompleteInMillisecs       : 500,
                    consistentlyInteractiveInMillisecs: 600
            ]
            persistEventResult(metrics, page1, browser1)
        }

        when: "the builder is given the aspect type of matching aspect and no measurand"
        EventResultQueryBuilder builder = new EventResultQueryBuilder()
        List<EventResultProjection> results = builder
                .withJobGroupIn([jobGroup])
                .withPageIn([page1])
                .withPerformanceAspects([PerformanceAspectType.PAGE_SHOWS_USEFUL_CONTENT])
                .getAverageData()

        then: "avg of EventResults got calculated correctly for aspect metric"
        results.size() == 1
        EventResultProjection result = results[0]
        result.PAGE_SHOWS_USEFUL_CONTENT == (10*firstContentfulPaintValue)/10
        !result.startRenderInMillisecs
        !result.firstContentfulPaint
        !result.docCompleteTimeInMillisecs
        !result.docCompleteRequests
        !result.visuallyCompleteInMillisecs
        !result.consistentlyInteractiveInMillisecs
    }

    private void persistEventResult(Map<String, Integer> measurands, Page page = page1, Browser browser = browser1) {
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

    private void persistAspect(PerformanceAspectType type, SelectedMeasurand metric, Page page = page1, Browser browser = browser1) {
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

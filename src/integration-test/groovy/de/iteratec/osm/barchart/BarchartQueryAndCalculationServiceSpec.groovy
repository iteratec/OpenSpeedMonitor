package de.iteratec.osm.barchart

import de.iteratec.osm.OsmConfiguration
import de.iteratec.osm.csi.NonTransactionalIntegrationSpec
import de.iteratec.osm.csi.Page
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.result.*
import grails.test.mixin.integration.Integration
import grails.transaction.Rollback
import spock.lang.Unroll

/**
 * Test-suite for {@link BarchartQueryAndCalculationService}.
 */

@Integration
@Rollback
@Unroll
class BarchartQueryAndCalculationServiceSpec extends NonTransactionalIntegrationSpec {

    BarchartQueryAndCalculationService barchartMedianService

    private SelectedMeasurand measurand
    private SelectedMeasurand userTimingMeasurand
    private Page page_one
    private Page page_two
    private JobGroup jobGroup_one
    private JobGroup jobGroup_two

    def setup() {
        OsmConfiguration.build()
        buildTestData()
        measurand = new SelectedMeasurand(name: "DOC_COMPLETE_TIME", cachedView: CachedView.UNCACHED, selectedType: SelectedMeasurandType.MEASURAND)
        userTimingMeasurand = new SelectedMeasurand(name: "mapsapi_apiboot2_firstmap", cachedView: CachedView.UNCACHED, selectedType: SelectedMeasurandType.USERTIMING_MARK)
    }

    private Date yesterday() {
        final Calendar cal = Calendar.getInstance()
        cal.add(Calendar.DATE, -1)
        return cal.getTime()
    }

    private Date tomorrow() {
        final Calendar cal = Calendar.getInstance()
        cal.add(Calendar.DATE, +1)
        return cal.getTime()
    }

    void "Test median for measurand"() {
        when: "Median service is called with query parameters"
        def medians = barchartMedianService.getMediansFor(
                [jobGroup_one, jobGroup_two],
                [page_one, page_two],
                yesterday(),
                tomorrow(),
                [measurand, userTimingMeasurand]
        )
        then: "Get calculated medians in EventResultProjections"
        medians[0].projectedProperties.get(measurand.getDatabaseRelevantName()) == 1500
        medians[0].projectedProperties.get(userTimingMeasurand.getDatabaseRelevantName()) == 2000
        medians[1].projectedProperties.get(measurand.getDatabaseRelevantName()) == 1750
    }

    private buildTestJobGroups() {
        jobGroup_one = JobGroup.build(name: "test job one")
        jobGroup_two = JobGroup.build(name: "test job two")
    }

    private buildTestPages() {
        page_one = Page.build(name: "test page one")
        page_two = Page.build(name: "test page two")
    }

    private buildEventResultsWithUserTimings() {
        EventResult.build(docCompleteTimeInMillisecs: 2000,
                fullyLoadedTimeInMillisecs: 5000,
                page: page_one,
                jobGroup: jobGroup_one,
                cachedView: CachedView.UNCACHED,
                userTimings: [
                        UserTiming.build(name: "mapsapi_apiboot2_firstmap",
                                startTime: 3000.0,
                                type: UserTimingType.MARK)
                ])
        EventResult.build(docCompleteTimeInMillisecs: 1500,
                fullyLoadedTimeInMillisecs: 5000,
                page: page_one,
                jobGroup: jobGroup_one,
                cachedView: CachedView.UNCACHED,
                userTimings: [
                        UserTiming.build(name: "mapsapi_apiboot2_firstmap",
                                startTime: 2000.0,
                                type: UserTimingType.MARK)
                ])
        EventResult.build(docCompleteTimeInMillisecs: 1000,
                fullyLoadedTimeInMillisecs: 5000,
                page: page_one,
                jobGroup: jobGroup_one,
                cachedView: CachedView.UNCACHED,
                userTimings: [
                        UserTiming.build(name: "mapsapi_apiboot2_firstmap",
                                startTime: 1000.0,
                                type: UserTimingType.MARK)
                ])
    }

    private buildEventResultsWithoutUserTimings() {
        EventResult.build(docCompleteTimeInMillisecs: 1000,
                fullyLoadedTimeInMillisecs: 5000,
                page: page_two,
                jobGroup: jobGroup_two,
                cachedView: CachedView.UNCACHED)
        EventResult.build(docCompleteTimeInMillisecs: 1500,
                fullyLoadedTimeInMillisecs: 5000,
                page: page_two,
                jobGroup: jobGroup_two,
                cachedView: CachedView.UNCACHED)
        EventResult.build(docCompleteTimeInMillisecs: 1000,
                fullyLoadedTimeInMillisecs: 5000,
                page: page_two,
                jobGroup: jobGroup_two,
                cachedView: CachedView.UNCACHED)
        EventResult.build(docCompleteTimeInMillisecs: 2000,
                fullyLoadedTimeInMillisecs: 5000,
                page: page_two,
                jobGroup: jobGroup_two,
                cachedView: CachedView.UNCACHED)
        EventResult.build(docCompleteTimeInMillisecs: 2500,
                fullyLoadedTimeInMillisecs: 5000,
                page: page_two,
                jobGroup: jobGroup_two,
                cachedView: CachedView.UNCACHED)
        EventResult.build(docCompleteTimeInMillisecs: 1000000,
                fullyLoadedTimeInMillisecs: 5000,
                page: page_two,
                jobGroup: jobGroup_two,
                cachedView: CachedView.UNCACHED)
    }

    private buildTestData() {
        buildTestJobGroups()
        buildTestPages()
        buildEventResultsWithUserTimings()
        buildEventResultsWithoutUserTimings()
    }
}

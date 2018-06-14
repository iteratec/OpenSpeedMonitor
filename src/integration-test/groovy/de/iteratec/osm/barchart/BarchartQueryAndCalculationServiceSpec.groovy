package de.iteratec.osm.barchart

import de.iteratec.osm.OsmConfiguration
import de.iteratec.osm.csi.NonTransactionalIntegrationSpec
import de.iteratec.osm.csi.Page
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.result.*
import de.iteratec.osm.result.dao.EventResultProjection
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

    BarchartQueryAndCalculationService barchartQueryAndCalculationService

    private SelectedMeasurand measurand
    private SelectedMeasurand userTimingMeasurand
    private Page page_one, page_two
    private JobGroup jobGroup_one, jobGroup_two, jobGroup_three
    private def TEST

    def setup() {
        OsmConfiguration.build()
        buildTestData()
        measurand = new SelectedMeasurand(name: "DOC_COMPLETE_TIME", cachedView: CachedView.UNCACHED, selectedType: SelectedMeasurandType.MEASURAND)
        userTimingMeasurand = new SelectedMeasurand(name: "mapsapi_apiboot2_firstmap", cachedView: CachedView.UNCACHED, selectedType: SelectedMeasurandType.USERTIMING_MARK)
    }

    void "Test service for measurand and usertimings"() {
        when: "BarchartQueryAndCalculationService is called with query parameters"
        def medians = barchartQueryAndCalculationService.getMediansFor(
                [jobGroup_one, jobGroup_two],
                [page_one, page_two],
                yesterday(),
                tomorrow(),
                [measurand, userTimingMeasurand]
        )
        def avgs = barchartQueryAndCalculationService.getAveragesFor(
                [jobGroup_one, jobGroup_two],
                [page_one, page_two],
                yesterday(),
                tomorrow(),
                [measurand, userTimingMeasurand]
        )
        then: "Get averages and calculated medians in EventResultProjections"


        findEventResultProjectionByJobGroupAndPage(medians, jobGroup_one, page_one).projectedProperties.get(measurand.getDatabaseRelevantName()) == 1500
        findEventResultProjectionByJobGroupAndPage(medians, jobGroup_one, page_one).projectedProperties.get(userTimingMeasurand.getDatabaseRelevantName()) == 2000
        findEventResultProjectionByJobGroupAndPage(medians, jobGroup_two, page_one).projectedProperties.get(measurand.getDatabaseRelevantName()) == 3000
        findEventResultProjectionByJobGroupAndPage(medians, jobGroup_two, page_two).projectedProperties.get(measurand.getDatabaseRelevantName()) == 1750

        findEventResultProjectionByJobGroupAndPage(avgs, jobGroup_one, page_one).projectedProperties.get(measurand.getDatabaseRelevantName()) == 1500
        findEventResultProjectionByJobGroupAndPage(avgs, jobGroup_one, page_one).projectedProperties.get(userTimingMeasurand.getDatabaseRelevantName()) == 2000
        findEventResultProjectionByJobGroupAndPage(avgs, jobGroup_two, page_one).projectedProperties.get(measurand.getDatabaseRelevantName()) == 3000
        findEventResultProjectionByJobGroupAndPage(avgs, jobGroup_two, page_two).projectedProperties.get(measurand.getDatabaseRelevantName()) == 168000
    }

    void "Test service for jobGroups only"() {
        when: "BarchartQueryAndCalculationService is called with query parameters"
        def medians = barchartQueryAndCalculationService.getMediansFor(
                [jobGroup_one, jobGroup_two],
                [],
                yesterday(),
                tomorrow(),
                [measurand]
        )
        def avgs = barchartQueryAndCalculationService.getAveragesFor(
                [jobGroup_one, jobGroup_two],
                [],
                yesterday(),
                tomorrow(),
                [measurand]
        )
        then: "Get averages and calculated medians for jobGroupAggregation"

        findEventResultProjectionByJobGroup(medians, jobGroup_one).projectedProperties.get(measurand.getDatabaseRelevantName()) == 1500
        findEventResultProjectionByJobGroup(medians, jobGroup_two).projectedProperties.get(measurand.getDatabaseRelevantName()) == 2000

        findEventResultProjectionByJobGroup(avgs, jobGroup_one).projectedProperties.get(measurand.getDatabaseRelevantName()) == 1500
        Math.round(findEventResultProjectionByJobGroup(avgs, jobGroup_two).projectedProperties.get(measurand.getDatabaseRelevantName())) == 144429
    }

    void "Test service for jobGroup with no results"() {
        when: "BarchartQueryAndCalculationService is called with query parameters"
        def medians = barchartQueryAndCalculationService.getMediansFor(
                [jobGroup_three],
                [],
                yesterday(),
                tomorrow(),
                [measurand]
        )
        def avgs = barchartQueryAndCalculationService.getAveragesFor(
                [jobGroup_three],
                [],
                yesterday(),
                tomorrow(),
                [measurand]
        )
        then: "Get no EventResultProjections since jobGroup is empty"
        medians.size() == 0
        avgs.size() == 0
    }

    private buildTestJobGroups() {
        jobGroup_one = JobGroup.build(name: "test job one")
        jobGroup_two = JobGroup.build(name: "test job two")
        jobGroup_three = JobGroup.build(name: "test job three")
    }

    private EventResultProjection findEventResultProjectionByJobGroupAndPage(List eventResultProjections, JobGroup jobGroup, Page page) {
        EventResultProjection result
        eventResultProjections.each {
            if (it.jobGroup == jobGroup && it.page == page) {
                result = it
            }
        }
        return result
    }

    private EventResultProjection findEventResultProjectionByJobGroup(List eventResultProjections, JobGroup jobGroup) {
        EventResultProjection result
        eventResultProjections.each {
            if (it.jobGroup == jobGroup) {
                result = it
            }
        }
        return result
    }

    private buildTestPages() {
        page_one = Page.build(name: "test page one")
        page_two = Page.build(name: "test page two")
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

    private buildTestData() {
        buildTestJobGroups()
        buildTestPages()
        buildEventResultsWithUserTimings()
        buildEventResultsWithoutUserTimings()
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

        EventResult.build(docCompleteTimeInMillisecs: 3000,
                fullyLoadedTimeInMillisecs: 5000,
                page: page_one,
                jobGroup: jobGroup_two,
                cachedView: CachedView.UNCACHED)
        EventResult.build(docCompleteTimeInMillisecs: null,
                fullyLoadedTimeInMillisecs: 5000,
                page: page_one,
                jobGroup: jobGroup_two,
                cachedView: CachedView.UNCACHED)
        EventResult.build(docCompleteTimeInMillisecs: null,
                fullyLoadedTimeInMillisecs: 5000,
                page: page_one,
                jobGroup: jobGroup_two,
                cachedView: CachedView.UNCACHED)
    }
}

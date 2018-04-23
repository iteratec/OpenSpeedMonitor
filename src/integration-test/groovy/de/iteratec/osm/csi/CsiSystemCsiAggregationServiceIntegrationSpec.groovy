package de.iteratec.osm.csi

import de.iteratec.osm.measurement.environment.Browser
import de.iteratec.osm.measurement.environment.Location
import de.iteratec.osm.measurement.schedule.ConnectivityProfile
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.report.chart.AggregationType
import de.iteratec.osm.report.chart.CsiAggregation
import de.iteratec.osm.report.chart.CsiAggregationInterval
import de.iteratec.osm.result.MeasuredEvent
import de.iteratec.osm.result.MvQueryParams
import grails.testing.mixin.integration.Integration
import grails.transaction.Rollback

import java.time.Duration

@Integration
@Rollback
class CsiSystemCsiAggregationServiceIntegrationSpec extends NonTransactionalIntegrationSpec {

    static final Date fromHourly = new Date(2013, 8, 5, 6, 0, 0)
    static final Date toHourly = new Date(2013, 8, 5, 15, 0, 0)
    static final Date fromWeekly = new Date(2013, 8, 5, 0, 0, 0)
    static final Date toWeekly = new Date(2013, 8, 10, 0, 0, 0)
    static final Date inHourlyInterval1 = new Date(2013, 8, 5, 8, 0, 0)
    static final Date inHourlyInterval2 = new Date(2013, 8, 5, 9, 0, 0)
    static final Date inWeeklyInterval1 = new Date(2013, 8, 6, 0, 0, 0)
    static final Date inWeeklyInterval2 = new Date(2013, 8, 9, 0, 0, 0)
    static final double DEFAULT_MV_VALUE = 1

    CsiSystemCsiAggregationService csiSystemCsiAggregationService
    EventCsiAggregationService eventCsiAggregationService
    PageCsiAggregationService pageCsiAggregationService
    JobGroupCsiAggregationService jobGroupCsiAggregationService

    CsiAggregationInterval hourly
    CsiAggregationInterval daily
    CsiAggregationInterval weekly
    AggregationType aggregationType
    ConnectivityProfile connectivityProfile
    JobGroup jobGroup1
    JobGroup jobGroup2
    Page page1
    Page page2
    MeasuredEvent measuredEvent
    Browser browser
    Location location
    CsiConfiguration csiConfiguration
    CsiSystem csiSystem

    def setup() {
        createCommonTestData()
    }

    def cleanup() {
        CsiAggregation.list()*.delete()
    }

    void "Find no hourly CsiAggregations if no existing"() {
        expect: "No Aggregations"
        eventCsiAggregationService.findAll(fromHourly, toHourly, hourly, connectivityProfile).isEmpty()
    }

    void "Find no page CsiAggregations if no existing"() {
        expect: "No Aggregations"
        pageCsiAggregationService.findAll(fromHourly, toHourly, daily, [jobGroup1, jobGroup2], [page1, page2]).isEmpty()
        pageCsiAggregationService.findAll(fromHourly, toHourly, weekly, [jobGroup1, jobGroup2], [page1, page2]).isEmpty()
    }

    void "Find no JobGroup CsiAggregations if no existing"() {
        expect: "No Aggregations"
        jobGroupCsiAggregationService.findAll(fromHourly, toHourly, daily, [jobGroup1, jobGroup2]).isEmpty()
        jobGroupCsiAggregationService.findAll(fromHourly, toHourly, weekly, [jobGroup1, jobGroup2]).isEmpty()
    }

    void "Find no CsiSystem CsiAggregations if no existing"() {
        expect: "No Aggregations"
        csiSystemCsiAggregationService.findAll(fromHourly, toHourly, daily).isEmpty()
        csiSystemCsiAggregationService.findAll(fromHourly, toHourly, weekly).isEmpty()
    }

    void "Calculate empty Page CsiAggregation if no existing"() {
        when: "We get daily and weekly page aggregations"
        List<CsiAggregation> pageDailyAggregations = pageCsiAggregationService.getOrCalculatePageCsiAggregations(fromHourly, toHourly, daily, [jobGroup1])
        List<CsiAggregation> pageWeeklyMvAggregations = pageCsiAggregationService.getOrCalculatePageCsiAggregations(fromHourly, toHourly, weekly, [jobGroup1])

        then: "for each page and interval one aggregations should exist and they are calculated"
        pageDailyAggregations.size() == Page.count()
        !pageDailyAggregations.any {
            it.csByWptDocCompleteInPercent != null && it.csByWptVisuallyCompleteInPercent != null
        }

        pageWeeklyMvAggregations.size() == Page.count()
        !pageWeeklyMvAggregations.any {
            it.csByWptDocCompleteInPercent != null && it.csByWptVisuallyCompleteInPercent != null
        }
    }

    void "Calculate empty JobGroup CsiAggregation if no existing"() {
        when:"We get daily and weekly aggregations"
        List<CsiAggregation> jobGroupDailyAggregations = jobGroupCsiAggregationService.getOrCalculateShopCsiAggregations(fromHourly, toHourly, daily, [jobGroup1])
        List<CsiAggregation> jobGroupWeeklyAggregations = jobGroupCsiAggregationService.getOrCalculateShopCsiAggregations(fromHourly, toHourly, weekly, [jobGroup1])

        then: "for each jobGroup and interval one aggregation should exist and they are calculated"
        jobGroupDailyAggregations.size() == 1
        !jobGroupDailyAggregations.any {
            it.csByWptDocCompleteInPercent != null && it.csByWptVisuallyCompleteInPercent != null
        }

        jobGroupWeeklyAggregations.size() == 1
        !jobGroupWeeklyAggregations.any {
            it.csByWptDocCompleteInPercent != null && it.csByWptVisuallyCompleteInPercent != null
        }
    }

    void "Calculate empty CsiSystem CsiAggregation if no existing"() {
        when:"We get daily and weekly aggregations"
        List<CsiAggregation> csiSystemDailyAggregations = csiSystemCsiAggregationService.getOrCalculateCsiSystemCsiAggregations(fromHourly, toHourly, daily, CsiSystem.findAll())
        List<CsiAggregation> csiSystemWeeklyAggregations = csiSystemCsiAggregationService.getOrCalculateCsiSystemCsiAggregations(fromHourly, toHourly, weekly, CsiSystem.findAll())

        then:"for each CsiSystem and interval one aggregation should exist and they are calculated"
        csiSystemDailyAggregations.size() == CsiSystem.count()
        !csiSystemDailyAggregations.any {
            it.csByWptDocCompleteInPercent != null && it.csByWptVisuallyCompleteInPercent != null
        }

        csiSystemWeeklyAggregations.size() == CsiSystem.count()
        !csiSystemWeeklyAggregations.any {
            it.csByWptDocCompleteInPercent != null && it.csByWptVisuallyCompleteInPercent != null
        }
    }

    void "Calculate single Page CsiAggregation if 1 is existing"() {
        given:
        createSingleHourlyCsiAggregations()

        when:"We get daily and weekly aggregations"
        List<CsiAggregation> pageDailyAggregations = pageCsiAggregationService.getOrCalculatePageCsiAggregations(fromHourly, toHourly, daily, [jobGroup1])
        List<CsiAggregation> pageWeeklyAggregations = pageCsiAggregationService.getOrCalculatePageCsiAggregations(fromHourly, toHourly, weekly, [jobGroup1])

        then: "for each page and interval on aggregation should exist and have the default value"
        pageDailyAggregations.size() == Page.count()
        isDocCompleteAndVisualComplete(pageDailyAggregations[0], DEFAULT_MV_VALUE)
        pageWeeklyAggregations.size() == Page.count()
        isDocCompleteAndVisualComplete(pageWeeklyAggregations[0], DEFAULT_MV_VALUE)

        and: "The cascading values should also be calculated"
        eventCsiAggregationService.findAll(fromHourly, toHourly, hourly).size() == 1
    }

    void "Calculate single JobGroup CsiAggregation if 1 is existing"() {
        given:
        createSingleHourlyCsiAggregations()

        when:"We get daily and weekly aggregations"
        List<CsiAggregation> jobGroupDailyAggregations = jobGroupCsiAggregationService.getOrCalculateShopCsiAggregations(fromHourly, toHourly, daily, [jobGroup1])
        List<CsiAggregation> jobGroupWeeklyAggregations = jobGroupCsiAggregationService.getOrCalculateShopCsiAggregations(fromHourly, toHourly, weekly, [jobGroup1])

        then:"for each jobGroup and interval on aggregation should exist and have the default value"
        jobGroupDailyAggregations.size() == 1
        isDocCompleteAndVisualComplete(jobGroupDailyAggregations[0], DEFAULT_MV_VALUE)
        jobGroupWeeklyAggregations.size() == 1
        isDocCompleteAndVisualComplete(jobGroupWeeklyAggregations[0], DEFAULT_MV_VALUE)

        and: "The cascading values should also be calculated"
        pageCsiAggregationService.findAll(fromHourly, toHourly, weekly, [jobGroup1], [page1]).size() == 1
        pageCsiAggregationService.findAll(fromHourly, toHourly, daily, [jobGroup1], [page1]).size() == 1
        eventCsiAggregationService.findAll(fromHourly, toHourly, hourly).size() == 1
    }

    void "Calculate single CsiSystem CsiAggregation if 1 is existing"() {
        given:
        createSingleHourlyCsiAggregations()

        when:"We get daily and weekly aggregations"
        List<CsiAggregation> csiSystemDailyAggregations = csiSystemCsiAggregationService.getOrCalculateCsiSystemCsiAggregations(fromHourly, toHourly, daily, CsiSystem.findAll())
        List<CsiAggregation> csiSystemWeeklyAggregations = csiSystemCsiAggregationService.getOrCalculateCsiSystemCsiAggregations(fromHourly, toHourly, weekly, CsiSystem.findAll())

        then:"for each CsiSystem and interval on aggregation should exist and have the default value"
        csiSystemWeeklyAggregations.size() == CsiSystem.count()
        isDocCompleteAndVisualComplete(csiSystemWeeklyAggregations[0], DEFAULT_MV_VALUE)
        csiSystemDailyAggregations.size() == CsiSystem.count()
        isDocCompleteAndVisualComplete(csiSystemDailyAggregations[0], DEFAULT_MV_VALUE)

        and: "The cascading values should also be calculated"
        jobGroupCsiAggregationService.findAll(fromHourly, toHourly, daily, [jobGroup1]).size() == 1
        jobGroupCsiAggregationService.findAll(fromHourly, toHourly, weekly, [jobGroup1]).size() == 1
        pageCsiAggregationService.findAll(fromHourly, toHourly, weekly, [jobGroup1], [page1]).size() == 1
        pageCsiAggregationService.findAll(fromHourly, toHourly, daily, [jobGroup1], [page1]).size() == 1
        eventCsiAggregationService.findAll(fromHourly, toHourly, hourly).size() == 1
    }

    void "calculate single weekly Mv if 1 is existing and only highest weekly-aggregation is called"() {
        given:
        createSingleHourlyCsiAggregations()

        when:
        List<CsiAggregation> csiSystemWeeklyAggregations = csiSystemCsiAggregationService.getOrCalculateCsiSystemCsiAggregations(fromHourly, toHourly, weekly, [csiSystem])
        then:
        csiSystemWeeklyAggregations.size() == 1
        isDocCompleteAndVisualComplete(csiSystemWeeklyAggregations[0], DEFAULT_MV_VALUE)

        when:
        List<CsiAggregation> hourlyAggregations = eventCsiAggregationService.findAll(fromHourly, toHourly, hourly, connectivityProfile)
        then:
        hourlyAggregations.size() == 1
        isDocCompleteAndVisualComplete(hourlyAggregations[0], DEFAULT_MV_VALUE)

        when:
        List<CsiAggregation> pageDailyAggregations = pageCsiAggregationService.findAll(fromHourly, toHourly, daily, JobGroup.list(), Page.list())
        List<CsiAggregation> pageWeeklyAggregations = pageCsiAggregationService.findAll(fromHourly, toHourly, weekly, JobGroup.list(), Page.list())
        then:
        pageDailyAggregations.size() == 0
        pageWeeklyAggregations.size() == csiSystem.affectedJobGroups.size() * Page.count()
        isDocCompleteAndVisualComplete(pageWeeklyAggregations[0], DEFAULT_MV_VALUE)

        when:
        List<CsiAggregation> jobGroupDailyAggregations = jobGroupCsiAggregationService.findAll(fromHourly, toHourly, daily, JobGroup.list())
        List<CsiAggregation> jobGroupWeeklyAggregations = jobGroupCsiAggregationService.findAll(fromHourly, toHourly, weekly, JobGroup.list())
        then:
        jobGroupDailyAggregations.size() == 0
        jobGroupWeeklyAggregations.size() == csiSystem.affectedJobGroups.size()
        isDocCompleteAndVisualComplete(jobGroupWeeklyAggregations[0], DEFAULT_MV_VALUE)

        when:
        List<CsiAggregation> csiSystemDailyAggregations = csiSystemCsiAggregationService.findAll(fromHourly, toHourly, daily)
        then:
        csiSystemDailyAggregations.size() == 0
    }

    void "Aggregate to single Page CsiAggregation if 2 hourlies are existing"() {
        given: "Two Aggregations"
        createTwoHourlyCsiAggregations()

        when:"We get daily and weekly aggregations"
        List<CsiAggregation> pageDailyAggregations = pageCsiAggregationService.getOrCalculatePageCsiAggregations(fromHourly, toHourly, daily, [jobGroup1])
        List<CsiAggregation> pageWeeklyAggregations = pageCsiAggregationService.getOrCalculatePageCsiAggregations(fromHourly, toHourly, weekly, [jobGroup1])

        then: "for each page and interval only one aggregation should exist"
        pageDailyAggregations.size() == Page.count()
        areAllValuesInListAsExpected(pageDailyAggregations, 1, 1, DEFAULT_MV_VALUE)
        pageWeeklyAggregations.size() == Page.count()
        areAllValuesInListAsExpected(pageWeeklyAggregations, 1, 1, DEFAULT_MV_VALUE)

        and: "The cascading values should also be calculated"
        eventCsiAggregationService.findAll(fromHourly, toHourly, hourly).size() == 2
    }

    void "Aggregate to single JobGroup csiAggregation if 2 hourlies are existing"() {
        given: "Two Aggregations"
        createTwoHourlyCsiAggregations()

        when:"We get daily and weekly aggregations"
        List<CsiAggregation> jobGroupDailyAggregations = jobGroupCsiAggregationService.getOrCalculateShopCsiAggregations(fromHourly, toHourly, daily, [jobGroup1])
        List<CsiAggregation> jobGroupWeeklyAggregations = jobGroupCsiAggregationService.getOrCalculateShopCsiAggregations(fromHourly, toHourly, weekly, [jobGroup1])

        then: "for each jobGroup and interval only one aggregation should exist"
        jobGroupDailyAggregations.size() == 1
        isDocCompleteAndVisualComplete(jobGroupDailyAggregations[0], DEFAULT_MV_VALUE)
        jobGroupWeeklyAggregations.size() == 1
        isDocCompleteAndVisualComplete(jobGroupWeeklyAggregations[0], DEFAULT_MV_VALUE)

        and: "The cascading values should also be calculated"
        pageCsiAggregationService.findAll(fromHourly, toHourly, weekly, [jobGroup1], [page1, page2]).size() == Page.count()
        pageCsiAggregationService.findAll(fromHourly, toHourly, daily, [jobGroup1], [page1, page2]).size() == Page.count()
        eventCsiAggregationService.findAll(fromHourly, toHourly, hourly).size() == 2
    }

    void "Aggregate to single CsiSystem CsiAggregation if 2 hourlies are existing"() {
        given: "Two Aggregations"
        createTwoHourlyCsiAggregations()

        when:"We get daily and weekly aggregations"
        List<CsiAggregation> csiSystemDailyAggregations = csiSystemCsiAggregationService.getOrCalculateCsiSystemCsiAggregations(fromHourly, toHourly, daily, [csiSystem])
        List<CsiAggregation> csiSystemWeeklyAggregations = csiSystemCsiAggregationService.getOrCalculateCsiSystemCsiAggregations(fromHourly, toHourly, weekly, [csiSystem])

        then: "for each CsiSystem and interval only one aggregation should exist"
        csiSystemDailyAggregations.size() == CsiSystem.count()
        isDocCompleteAndVisualComplete(csiSystemDailyAggregations[0], DEFAULT_MV_VALUE)
        csiSystemWeeklyAggregations.size() == CsiSystem.count()
        isDocCompleteAndVisualComplete(csiSystemWeeklyAggregations[0], DEFAULT_MV_VALUE)

        and: "The cascading values should also be calculated"
        jobGroupCsiAggregationService.findAll(fromHourly, toHourly, daily, [jobGroup1]).size() == 1
        jobGroupCsiAggregationService.findAll(fromHourly, toHourly, weekly, [jobGroup1]).size() == 1
        pageCsiAggregationService.findAll(fromHourly, toHourly, weekly, [jobGroup1], [page1, page2]).size() == Page.count()
        pageCsiAggregationService.findAll(fromHourly, toHourly, daily, [jobGroup1], [page1, page2]).size() == Page.count()
        eventCsiAggregationService.findAll(fromHourly, toHourly, hourly).size() == 2
    }

    void "aggregate to 2 daily- and a single weekly-csiAggregation if 2 hourlies on different days are existing"() {
        given: "Two hourly aggregations on different days"
        MvQueryParams getAllParams = createGetAllQueryParam()
        createTwoHourlyCsiAggregationsOnDifferentDays()

        when:"We get daily and weekly aggregations"
        List<CsiAggregation> hourlyAggregations = eventCsiAggregationService.getHourlyCsiAggregations(fromWeekly, toWeekly, getAllParams)
        List<CsiAggregation> pageDailyAggregations = pageCsiAggregationService.getOrCalculatePageCsiAggregations(fromWeekly, toWeekly, daily, [jobGroup1])
        List<CsiAggregation> pageWeeklyAggregations = pageCsiAggregationService.getOrCalculatePageCsiAggregations(fromWeekly, toWeekly, weekly, [jobGroup1])
        List<CsiAggregation> jobGroupDailyAggregations = jobGroupCsiAggregationService.getOrCalculateShopCsiAggregations(fromWeekly, toWeekly, daily, [jobGroup1])
        List<CsiAggregation> jobGroupWeeklyAggregations = jobGroupCsiAggregationService.getOrCalculateShopCsiAggregations(fromWeekly, toWeekly, weekly, [jobGroup1])
        List<CsiAggregation> csiSystemDailyAggregations = csiSystemCsiAggregationService.getOrCalculateCsiSystemCsiAggregations(fromWeekly, toWeekly, daily, CsiSystem.findAll())
        List<CsiAggregation> csiSystemWeeklyAggregations = csiSystemCsiAggregationService.getOrCalculateCsiSystemCsiAggregations(fromWeekly, toWeekly, weekly, CsiSystem.findAll())

        then: "for each aggregation type a daily and a weekly aggregation should exist"
        int expectedHourlySize = 2
        hourlyAggregations.size() == expectedHourlySize
        isDocCompleteAndVisualComplete(hourlyAggregations[0], DEFAULT_MV_VALUE)

        int daysInInterval = (Duration.between(fromWeekly.toInstant(), toWeekly.toInstant()).toDays()) + 1
        int expectedDailyCsiAggregations = Page.count() * daysInInterval
        pageDailyAggregations.size() == expectedDailyCsiAggregations
        int addedHourlyCsiAggregations = 2
        areAllValuesInListAsExpected(pageDailyAggregations, addedHourlyCsiAggregations, expectedDailyCsiAggregations - addedHourlyCsiAggregations, DEFAULT_MV_VALUE)

        pageWeeklyAggregations.size() == Page.count()
        areAllValuesInListAsExpected(pageWeeklyAggregations, 1, 1, DEFAULT_MV_VALUE)

        jobGroupDailyAggregations.size() == daysInInterval
        areAllValuesInListAsExpected(jobGroupDailyAggregations, addedHourlyCsiAggregations, daysInInterval - addedHourlyCsiAggregations, DEFAULT_MV_VALUE)

        jobGroupWeeklyAggregations.size() == 1
        isDocCompleteAndVisualComplete(jobGroupWeeklyAggregations[0], DEFAULT_MV_VALUE)

        int expectedDailyCsiSystemAggregations = CsiSystem.count() * daysInInterval
        csiSystemDailyAggregations.size() == expectedDailyCsiSystemAggregations
        areAllValuesInListAsExpected(jobGroupDailyAggregations, addedHourlyCsiAggregations, expectedDailyCsiSystemAggregations - addedHourlyCsiAggregations, DEFAULT_MV_VALUE)

        csiSystemWeeklyAggregations.size() == CsiSystem.count()
        isDocCompleteAndVisualComplete(csiSystemWeeklyAggregations[0], DEFAULT_MV_VALUE)
    }

    private boolean isDocCompleteAndVisualComplete(CsiAggregation csiAggregation, double expectedValue) {
        csiAggregation.csByWptDocCompleteInPercent == expectedValue &&
                csiAggregation.csByWptVisuallyCompleteInPercent == expectedValue &&
                csiAggregation.isCalculatedWithData()

    }

    private boolean areAllValuesInListAsExpected(List<CsiAggregation> list, int expectedValueWithCs, int expectedValueWithoutCs, double expectedCsValue) {
        List<CsiAggregation> withCs = list.findAll {
            it.csByWptDocCompleteInPercent != null && it.csByWptVisuallyCompleteInPercent != null
        }
        List<CsiAggregation> withoutCs = list.findAll {
            it.csByWptDocCompleteInPercent == null && it.csByWptVisuallyCompleteInPercent == null
        }

        boolean allValuesLikeExpected = !withCs.any {
            it.csByWptDocCompleteInPercent != expectedCsValue && it.csByWptVisuallyCompleteInPercent != expectedCsValue
        }

        return withCs.size() == expectedValueWithCs && withoutCs.size() == expectedValueWithoutCs && allValuesLikeExpected
    }

    private void createCommonTestData() {
        connectivityProfile = ConnectivityProfile.build()
        browser = Browser.build()
        page1 = Page.build()
        page2 = Page.build()

        csiConfiguration = CsiConfiguration.build(
                pageWeights: [page1, page2].collect { PageWeight.build(page: it, weight: 1.0) },
                browserConnectivityWeights: [BrowserConnectivityWeight.build(browser: browser, connectivity: connectivityProfile, weight: 1.0)]
        )

        jobGroup1 = JobGroup.build(csiConfiguration: csiConfiguration)
        jobGroup2 = JobGroup.build(csiConfiguration: csiConfiguration)

        csiSystem = CsiSystem.buildWithoutSave(jobGroupWeights: [jobGroup1, jobGroup2].collect {
            new JobGroupWeight(jobGroup: it, weight: 1.0)
        })
        csiSystem.jobGroupWeights*.csiSystem = csiSystem
        csiSystem.save(failOnError: true)

        measuredEvent = MeasuredEvent.build(testedPage: page1)

        location = Location.build()

        hourly = CsiAggregationInterval.build(name: "hourly", intervalInMinutes: CsiAggregationInterval.HOURLY)
        daily = CsiAggregationInterval.build(name: "daily", intervalInMinutes: CsiAggregationInterval.DAILY)
        weekly = CsiAggregationInterval.build(name: "weekly", intervalInMinutes: CsiAggregationInterval.WEEKLY)
    }

    private MvQueryParams createGetAllQueryParam() {
        MvQueryParams result = new MvQueryParams()
        result.jobGroupIds.addAll(JobGroup.findAll()*.id)
        result.pageIds.addAll(Page.findAll()*.id)
        result.browserIds.addAll(Browser.findAll()*.id)
        result.connectivityProfileIds.addAll(ConnectivityProfile.findAll()*.id)
        result.locationIds.addAll(Location.findAll()*.id)

        return result
    }


    private void createSingleHourlyCsiAggregations() {
        createCsiAggregations(inHourlyInterval1)
    }

    private void createTwoHourlyCsiAggregations() {
        createCsiAggregations(inHourlyInterval1)
        createCsiAggregations(inHourlyInterval2)
    }

    private void createTwoHourlyCsiAggregationsOnDifferentDays() {
        createCsiAggregations(inWeeklyInterval1)
        createCsiAggregations(inWeeklyInterval2)
    }

    private void createCsiAggregations(Date started) {
        CsiAggregation.build(
                started: started,
                interval: hourly,
                aggregationType: AggregationType.MEASURED_EVENT,
                jobGroup: jobGroup1,
                measuredEvent: measuredEvent,
                page: page1,
                browser: browser,
                location: location,
                csiSystem: csiSystem,
                csByWptDocCompleteInPercent: DEFAULT_MV_VALUE,
                csByWptVisuallyCompleteInPercent: DEFAULT_MV_VALUE,
                closedAndCalculated: true,
                connectivityProfile: connectivityProfile,
                underlyingEventResultsByWptDocComplete: '1,2,3,4')
    }
}

package de.iteratec.osm.csi

import de.iteratec.osm.measurement.environment.Browser
import de.iteratec.osm.measurement.environment.Location
import de.iteratec.osm.measurement.environment.WebPageTestServer
import de.iteratec.osm.measurement.schedule.ConnectivityProfile
import de.iteratec.osm.measurement.schedule.JobGroup

import de.iteratec.osm.report.chart.AggregatorType
import de.iteratec.osm.report.chart.CsiAggregation
import de.iteratec.osm.report.chart.CsiAggregationInterval
import de.iteratec.osm.result.MeasuredEvent
import de.iteratec.osm.result.CsiAggregationTagService
import de.iteratec.osm.result.MvQueryParams
import grails.test.mixin.integration.Integration
import grails.transaction.Rollback
import org.joda.time.DateTime
import org.junit.Test
import spock.lang.Specification
@Integration
@Rollback
class CsiSystemCsiAggregationServiceIntegrationSpec extends NonTransactionalIntegrationSpec {

    static final String jobGroupName1 = 'jobGroupName1'
    static final String jobGroupName2 = 'jobGroupName2'
    static final String pageName1 = 'pageName1'
    static final String pageName2 = 'pageName2'
    static final String eventName1 = 'eventName1'
    static final String eventName2 = 'eventName2'
    static final String browserName1 = 'browserName1'
    static final String browserName2 = 'browserName2'
    static final String locationName1 = 'locationName1'
    static final String locationName2 = 'locationName2'
    static final DateTime fromHourly = new DateTime(2013,8,5,6,0,0)
    static final DateTime fromWeekly = new DateTime(2013,8,5,0,0,0)
    static final DateTime toHourly = new DateTime(2013,8,5,15,0,0)
    static final DateTime toWeekly = new DateTime(2013,8,10,0,0,0)
    static final DateTime inHourlyInterval1 = new DateTime(2013,8,5,8,0,0)
    static final DateTime inHourlyInterval2 = new DateTime(2013,8,5,9,0,0)
    static final DateTime inWeeklyInterval1 = new DateTime(2013,8,6,0,0,0)
    static final DateTime inWeeklyInterval2 = new DateTime(2013,8,9,0,0,0)
    static final double DEFAULT_MV_VALUE = 1

    CsiSystemCsiAggregationService csiSystemCsiAggregationService
    CsiAggregationTagService csiAggregationTagService
    EventCsiAggregationService eventCsiAggregationService
    PageCsiAggregationService pageCsiAggregationService
    ShopCsiAggregationService shopCsiAggregationService

    CsiAggregationInterval hourly
    CsiAggregationInterval daily
    CsiAggregationInterval weekly
    AggregatorType measuredEvent
    ConnectivityProfile connectivityProfile
    JobGroup jobGroup1
    JobGroup jobGroup2
    Page page1
    Page page2
    MeasuredEvent measuredEvent1
    MeasuredEvent measuredEvent2
    Browser browser1
    Browser browser2
    Location location1
    Location location2
    CsiConfiguration csiConfiguration
    CsiSystem csiSystem

    def setup() {
        createCommonTestData()
    }

    def cleanup() {
        CsiAggregation.list()*.delete()
    }

    void "find no csiAggregations if no existing"() {
        when:
        List<CsiAggregation> hourlyMvs = eventCsiAggregationService.findAll(fromHourly.toDate(),toHourly.toDate(),hourly,connectivityProfile)
        List<CsiAggregation> pageDailyMvs = pageCsiAggregationService.findAll(fromHourly.toDate(),toHourly.toDate(),daily)
        List<CsiAggregation> pageWeeklyMvs = pageCsiAggregationService.findAll(fromHourly.toDate(),toHourly.toDate(),weekly)
        List<CsiAggregation> shopDailyMvs = shopCsiAggregationService.findAll(fromHourly.toDate(),toHourly.toDate(),daily)
        List<CsiAggregation> shopWeeklyMvs = shopCsiAggregationService.findAll(fromHourly.toDate(),toHourly.toDate(),weekly)
        List<CsiAggregation> csiSystemDailyMvs = csiSystemCsiAggregationService.findAll(fromHourly.toDate(),toHourly.toDate(),daily)
        List<CsiAggregation> csiSystemWeeklyMvs = csiSystemCsiAggregationService.findAll(fromHourly.toDate(),toHourly.toDate(),weekly)

        then:
        hourlyMvs.isEmpty()
        pageDailyMvs.isEmpty()
        pageWeeklyMvs.isEmpty()
        shopDailyMvs.isEmpty()
        shopWeeklyMvs.isEmpty()
        csiSystemDailyMvs.isEmpty()
        csiSystemWeeklyMvs.isEmpty()
    }

    void "calculate empty csiAggregation if no existing"() {
        given:
        MvQueryParams getAllParams = createGetAllQueryParam()

        when:
        List<CsiAggregation> hourlyMvs = eventCsiAggregationService.getHourlyCsiAggregations(fromHourly.toDate(),toHourly.toDate(),getAllParams)
        List<CsiAggregation> pageDailyMvs = pageCsiAggregationService.getOrCalculatePageCsiAggregations(fromHourly.toDate(),toHourly.toDate(),daily, [jobGroup1])
        List<CsiAggregation> pageWeeklyMvs = pageCsiAggregationService.getOrCalculatePageCsiAggregations(fromHourly.toDate(),toHourly.toDate(),weekly, [jobGroup1])
        List<CsiAggregation> shopDailyMvs = shopCsiAggregationService.getOrCalculateShopCsiAggregations(fromHourly.toDate(),toHourly.toDate(),daily, [jobGroup1])
        List<CsiAggregation> shopWeeklyMvs = shopCsiAggregationService.getOrCalculateShopCsiAggregations(fromHourly.toDate(),toHourly.toDate(),weekly, [jobGroup1])
        List<CsiAggregation> csiSystemDailyMvs = csiSystemCsiAggregationService.getOrCalculateCsiSystemCsiAggregations(fromHourly.toDate(),toHourly.toDate(),daily, CsiSystem.findAll())
        List<CsiAggregation> csiSystemWeeklyMvs = csiSystemCsiAggregationService.getOrCalculateCsiSystemCsiAggregations(fromHourly.toDate(),toHourly.toDate(),weekly, CsiSystem.findAll())

        then:
        hourlyMvs.size() == 0

        pageDailyMvs.size() == Page.count()
        !pageDailyMvs.any {
            it.csByWptDocCompleteInPercent != null && it.csByWptVisuallyCompleteInPercent != null
        }

        pageWeeklyMvs.size() == Page.count()
        !pageWeeklyMvs.any {
            it.csByWptDocCompleteInPercent != null && it.csByWptVisuallyCompleteInPercent != null
        }

        shopDailyMvs.size() == 1
        !shopDailyMvs.any {
            it.csByWptDocCompleteInPercent != null && it.csByWptVisuallyCompleteInPercent != null
        }

        shopWeeklyMvs.size() == 1
        !shopWeeklyMvs.any {
            it.csByWptDocCompleteInPercent != null && it.csByWptVisuallyCompleteInPercent != null
        }

        csiSystemDailyMvs.size() == CsiSystem.count()
        !csiSystemDailyMvs.any {
            it.csByWptDocCompleteInPercent != null && it.csByWptVisuallyCompleteInPercent != null
        }

        csiSystemWeeklyMvs.size() == CsiSystem.count()
        !csiSystemWeeklyMvs.any {
            it.csByWptDocCompleteInPercent != null && it.csByWptVisuallyCompleteInPercent != null
        }
    }

    void "calculate single csiAggregation if 1 is existing"() {
        given:
        MvQueryParams getAllParams = createGetAllQueryParam()
        createSingleHourlyCsiAggregations()

        when:
        List<CsiAggregation> hourlyMvs = eventCsiAggregationService.getHourlyCsiAggregations(fromHourly.toDate(),toHourly.toDate(),getAllParams)
        List<CsiAggregation> pageDailyMvs = pageCsiAggregationService.getOrCalculatePageCsiAggregations(fromHourly.toDate(),toHourly.toDate(),daily, [jobGroup1])
        List<CsiAggregation> pageWeeklyMvs = pageCsiAggregationService.getOrCalculatePageCsiAggregations(fromHourly.toDate(),toHourly.toDate(),weekly, [jobGroup1])
        List<CsiAggregation> shopDailyMvs = shopCsiAggregationService.getOrCalculateShopCsiAggregations(fromHourly.toDate(),toHourly.toDate(),daily, [jobGroup1])
        List<CsiAggregation> shopWeeklyMvs = shopCsiAggregationService.getOrCalculateShopCsiAggregations(fromHourly.toDate(),toHourly.toDate(),weekly, [jobGroup1])
        List<CsiAggregation> csiSystemDailyMvs = csiSystemCsiAggregationService.getOrCalculateCsiSystemCsiAggregations(fromHourly.toDate(),toHourly.toDate(),daily, CsiSystem.findAll())
        List<CsiAggregation> csiSystemWeeklyMvs = csiSystemCsiAggregationService.getOrCalculateCsiSystemCsiAggregations(fromHourly.toDate(),toHourly.toDate(),weekly, CsiSystem.findAll())

        then:
        hourlyMvs.size() == 1
        csByDocCompleteAndByVisualCompleteIs(hourlyMvs[0],DEFAULT_MV_VALUE)

        pageDailyMvs.size() == Page.count()
        csByDocCompleteAndByVisualCompleteIs(pageDailyMvs[0],DEFAULT_MV_VALUE)

        pageWeeklyMvs.size() == Page.count()
        csByDocCompleteAndByVisualCompleteIs(pageWeeklyMvs[0],DEFAULT_MV_VALUE)

        shopDailyMvs.size() == 1
        csByDocCompleteAndByVisualCompleteIs(shopDailyMvs[0],DEFAULT_MV_VALUE)

        shopWeeklyMvs.size() == 1
        csByDocCompleteAndByVisualCompleteIs(shopWeeklyMvs[0],DEFAULT_MV_VALUE)

        csiSystemDailyMvs.size() == CsiSystem.count()
        csByDocCompleteAndByVisualCompleteIs(csiSystemDailyMvs[0],DEFAULT_MV_VALUE)

        csiSystemWeeklyMvs.size() == CsiSystem.count()
        csByDocCompleteAndByVisualCompleteIs(csiSystemWeeklyMvs[0],DEFAULT_MV_VALUE)
    }

    void "calculate single weekly Mv if 1 is existing and only highest weekly-aggregation is called"() {
        given:
        createSingleHourlyCsiAggregations()

        when:
        List<CsiAggregation> csiSystemWeeklyMvs = csiSystemCsiAggregationService.getOrCalculateCsiSystemCsiAggregations(fromHourly.toDate(),toHourly.toDate(),weekly, [csiSystem])
        List<CsiAggregation> hourlyMvs = eventCsiAggregationService.findAll(fromHourly.toDate(),toHourly.toDate(),hourly,connectivityProfile)
        List<CsiAggregation> pageDailyMvs = pageCsiAggregationService.findAll(fromHourly.toDate(),toHourly.toDate(),daily)
        List<CsiAggregation> pageWeeklyMvs = pageCsiAggregationService.findAll(fromHourly.toDate(),toHourly.toDate(),weekly)
        List<CsiAggregation> shopDailyMvs = shopCsiAggregationService.findAll(fromHourly.toDate(),toHourly.toDate(),daily)
        List<CsiAggregation> shopWeeklyMvs = shopCsiAggregationService.findAll(fromHourly.toDate(),toHourly.toDate(),weekly)
        List<CsiAggregation> csiSystemDailyMvs = csiSystemCsiAggregationService.findAll(fromHourly.toDate(),toHourly.toDate(),daily)

        then:
        hourlyMvs.size() == 1
        csByDocCompleteAndByVisualCompleteIs(hourlyMvs[0],DEFAULT_MV_VALUE)

        pageDailyMvs.size() == 0

        pageWeeklyMvs.size() == csiSystem.affectedJobGroups.size() * Page.count()
        csByDocCompleteAndByVisualCompleteIs(pageWeeklyMvs[0],DEFAULT_MV_VALUE)

        shopDailyMvs.size() == 0

        shopWeeklyMvs.size() == csiSystem.affectedJobGroups.size()
        csByDocCompleteAndByVisualCompleteIs(shopWeeklyMvs[0],DEFAULT_MV_VALUE)

        csiSystemDailyMvs.size() == 0

        csiSystemWeeklyMvs.size() == 1
        csByDocCompleteAndByVisualCompleteIs(csiSystemWeeklyMvs[0],DEFAULT_MV_VALUE)
    }

    void "aggregate to single csiAggregation if 2 hourlys are existing"() {
        given:
        MvQueryParams getAllParams = createGetAllQueryParam()
        createTwoHourlyCsiAggregations()

        when:
        List<CsiAggregation> hourlyMvs = eventCsiAggregationService.getHourlyCsiAggregations(fromHourly.toDate(),toHourly.toDate(),getAllParams)
        List<CsiAggregation> pageDailyMvs = pageCsiAggregationService.getOrCalculatePageCsiAggregations(fromHourly.toDate(),toHourly.toDate(),daily, [jobGroup1])
        List<CsiAggregation> pageWeeklyMvs = pageCsiAggregationService.getOrCalculatePageCsiAggregations(fromHourly.toDate(),toHourly.toDate(),weekly, [jobGroup1])
        List<CsiAggregation> shopDailyMvs = shopCsiAggregationService.getOrCalculateShopCsiAggregations(fromHourly.toDate(),toHourly.toDate(),daily, [jobGroup1])
        List<CsiAggregation> shopWeeklyMvs = shopCsiAggregationService.getOrCalculateShopCsiAggregations(fromHourly.toDate(),toHourly.toDate(),weekly, [jobGroup1])
        List<CsiAggregation> csiSystemDailyMvs = csiSystemCsiAggregationService.getOrCalculateCsiSystemCsiAggregations(fromHourly.toDate(),toHourly.toDate(),daily, [csiSystem])
        List<CsiAggregation> csiSystemWeeklyMvs = csiSystemCsiAggregationService.getOrCalculateCsiSystemCsiAggregations(fromHourly.toDate(),toHourly.toDate(),weekly, [csiSystem])

        then:
        double expectedHourlySize = 2.0
        hourlyMvs.size() == expectedHourlySize.toInteger()
        csByDocCompleteAndByVisualCompleteIs(hourlyMvs[0],DEFAULT_MV_VALUE)


        pageDailyMvs.size() == Page.count()
        amountEntriesWithAndWithoutCS(pageDailyMvs, 1, 1, DEFAULT_MV_VALUE)


        pageWeeklyMvs.size() == Page.count()
        amountEntriesWithAndWithoutCS(pageWeeklyMvs, 1, 1, DEFAULT_MV_VALUE)

        shopDailyMvs.size() == 1
        csByDocCompleteAndByVisualCompleteIs(shopDailyMvs[0],DEFAULT_MV_VALUE)

        shopWeeklyMvs.size() == 1
        csByDocCompleteAndByVisualCompleteIs(shopWeeklyMvs[0],DEFAULT_MV_VALUE)

        csiSystemDailyMvs.size() == CsiSystem.count()
        csByDocCompleteAndByVisualCompleteIs(csiSystemDailyMvs[0],DEFAULT_MV_VALUE)

        csiSystemWeeklyMvs.size() == CsiSystem.count()
        csByDocCompleteAndByVisualCompleteIs(csiSystemWeeklyMvs[0],DEFAULT_MV_VALUE)
    }

    void "aggregate to 2 daily- and a single weekly-csiAggregation if 2 hourlys on different days are existing"() {
        given:
        MvQueryParams getAllParams = createGetAllQueryParam()
        createTwoHourlyCsiAggregationsOnDifferentDays()

        when:
        List<CsiAggregation> hourlyMvs = eventCsiAggregationService.getHourlyCsiAggregations(fromWeekly.toDate(),toWeekly.toDate(),getAllParams)
        List<CsiAggregation> pageDailyMvs1 = pageCsiAggregationService.getOrCalculatePageCsiAggregations(fromWeekly.toDate(),toWeekly.toDate(),daily, [jobGroup1])
        List<CsiAggregation> pageDailyMvs2 = pageCsiAggregationService.findAll(fromWeekly.toDate(), toWeekly.toDate(), daily)
        List<CsiAggregation> pageWeeklyMvs = pageCsiAggregationService.getOrCalculatePageCsiAggregations(fromWeekly.toDate(),toWeekly.toDate(),weekly, [jobGroup1])
        List<CsiAggregation> shopDailyMvs = shopCsiAggregationService.getOrCalculateShopCsiAggregations(fromWeekly.toDate(),toWeekly.toDate(),daily, [jobGroup1])
        List<CsiAggregation> shopWeeklyMvs = shopCsiAggregationService.getOrCalculateShopCsiAggregations(fromWeekly.toDate(),toWeekly.toDate(),weekly, [jobGroup1])
        List<CsiAggregation> csiSystemDailyMvs = csiSystemCsiAggregationService.getOrCalculateCsiSystemCsiAggregations(fromWeekly.toDate(),toWeekly.toDate(),daily, CsiSystem.findAll())
        List<CsiAggregation> csiSystemWeeklyMvs = csiSystemCsiAggregationService.getOrCalculateCsiSystemCsiAggregations(fromWeekly.toDate(),toWeekly.toDate(),weekly, CsiSystem.findAll())

        then:
        int expectedHourlySize = 2
        hourlyMvs.size() == expectedHourlySize
        csByDocCompleteAndByVisualCompleteIs(hourlyMvs[0],DEFAULT_MV_VALUE)


        int daysInInterval = (toWeekly.dayOfMonth - fromWeekly.dayOfMonth) + 1
        int expectedDailyCsiAggregations = Page.count() * daysInInterval
        pageDailyMvs1.size() == expectedDailyCsiAggregations
        pageDailyMvs1 == pageDailyMvs2
        int addedHourlyCsiAggregations = 2
        amountEntriesWithAndWithoutCS(pageDailyMvs1, addedHourlyCsiAggregations, expectedDailyCsiAggregations-addedHourlyCsiAggregations, DEFAULT_MV_VALUE)


        pageWeeklyMvs.size() == Page.count()
        amountEntriesWithAndWithoutCS(pageWeeklyMvs, 1, 1, DEFAULT_MV_VALUE)

        shopDailyMvs.size() == daysInInterval
        amountEntriesWithAndWithoutCS(shopDailyMvs, addedHourlyCsiAggregations, daysInInterval-addedHourlyCsiAggregations, DEFAULT_MV_VALUE)

        shopWeeklyMvs.size() == 1
        csByDocCompleteAndByVisualCompleteIs(shopWeeklyMvs[0],DEFAULT_MV_VALUE)

        int expectedDailyCsiSystemAggregations = CsiSystem.count() * daysInInterval
        csiSystemDailyMvs.size() == expectedDailyCsiSystemAggregations
        amountEntriesWithAndWithoutCS(shopDailyMvs, addedHourlyCsiAggregations, expectedDailyCsiSystemAggregations-addedHourlyCsiAggregations, DEFAULT_MV_VALUE)

        csiSystemWeeklyMvs.size() == CsiSystem.count()
        csByDocCompleteAndByVisualCompleteIs(csiSystemWeeklyMvs[0],DEFAULT_MV_VALUE)
    }

    private boolean csByDocCompleteAndByVisualCompleteIs(CsiAggregation csiAggregation, double expectedValue) {
        csiAggregation.csByWptDocCompleteInPercent == expectedValue &&
                csiAggregation.csByWptVisuallyCompleteInPercent == expectedValue &&
                csiAggregation.isCalculatedWithData()

    }

    private boolean amountEntriesWithAndWithoutCS(List<CsiAggregation> list, int expectedValueWithCs, int expectedValueWithoutCs, double expectedCsValue) {
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
        connectivityProfile = TestDataUtil.createConnectivityProfile("Conn1")

        browser1 = TestDataUtil.createBrowser(browserName1,0.0)
        browser2 = TestDataUtil.createBrowser(browserName2,0.0)

        page1 = TestDataUtil.createPage(pageName1,0.0)
        page2 = TestDataUtil.createPage(pageName2,0.0)

        csiConfiguration = TestDataUtil.createCsiConfiguration()
        csiConfiguration.timeToCsMappings = TestDataUtil.createTimeToCsMappingForAllPages([page1,page2])
        csiConfiguration.pageWeights = [TestDataUtil.createPageWeight(page1,1.0),TestDataUtil.createPageWeight(page2,1.0)]
        csiConfiguration.browserConnectivityWeights = [TestDataUtil.createBrowserConnectivityWeight(browser1,connectivityProfile,1.0),TestDataUtil.createBrowserConnectivityWeight(browser2,connectivityProfile,1.0)]

        jobGroup1 = TestDataUtil.createJobGroup(jobGroupName1)
        jobGroup1.csiConfiguration = csiConfiguration
        jobGroup2 = TestDataUtil.createJobGroup(jobGroupName2)
        jobGroup2.csiConfiguration = csiConfiguration

        csiSystem = TestDataUtil.createCsiSystem("label",[])
        csiSystem.addToJobGroupWeights(TestDataUtil.createJobGroupWeight(csiSystem, jobGroup1, 1.0))
        csiSystem.addToJobGroupWeights(TestDataUtil.createJobGroupWeight(csiSystem, jobGroup2,1.0))
        csiSystem.save(failOnError: true)


        measuredEvent1 = TestDataUtil.createMeasuredEvent(eventName1,page1)
        measuredEvent2 = TestDataUtil.createMeasuredEvent(eventName2,page2)

        WebPageTestServer server = TestDataUtil.createWebPageTestServer("test","test",true,"http://test.com")
        location1 = TestDataUtil.createLocation(server,locationName1,browser1,true)
        location2 = TestDataUtil.createLocation(server,locationName2,browser2,true)

        hourly = new CsiAggregationInterval(name: "hourly", intervalInMinutes: CsiAggregationInterval.HOURLY).save(validate: false)
        daily = new CsiAggregationInterval(name: "daily", intervalInMinutes: CsiAggregationInterval.DAILY).save(validate: false)
        weekly = new CsiAggregationInterval(name: "weekly", intervalInMinutes: CsiAggregationInterval.WEEKLY).save(validate: false)

        TestDataUtil.createAggregatorTypes()
        measuredEvent = AggregatorType.findByName(AggregatorType.MEASURED_EVENT)

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

    private void createCsiAggregations(DateTime started) {
        new CsiAggregation(
                started:started.toDate(),
                interval: hourly,
                aggregator: measuredEvent,
                tag: csiAggregationTagService.createHourlyEventTag(jobGroup1,measuredEvent1,page1,browser1,location1),
                csiSystem: csiSystem,
                csByWptDocCompleteInPercent: DEFAULT_MV_VALUE,
                csByWptVisuallyCompleteInPercent: DEFAULT_MV_VALUE,
                closedAndCalculated: true,
                connectivityProfile: connectivityProfile,
                underlyingEventResultsByWptDocComplete: '1,2,3,4').save(failOnError: true)
    }
}

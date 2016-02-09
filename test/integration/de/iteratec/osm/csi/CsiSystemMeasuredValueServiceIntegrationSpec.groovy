package de.iteratec.osm.csi

import de.iteratec.osm.measurement.environment.Browser
import de.iteratec.osm.measurement.environment.Location
import de.iteratec.osm.measurement.environment.WebPageTestServer
import de.iteratec.osm.measurement.schedule.ConnectivityProfile
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.measurement.schedule.JobGroupType
import de.iteratec.osm.report.chart.AggregatorType
import de.iteratec.osm.report.chart.CsiAggregation
import de.iteratec.osm.report.chart.MeasuredValueInterval
import de.iteratec.osm.result.MeasuredEvent
import de.iteratec.osm.result.MeasuredValueTagService
import de.iteratec.osm.result.MvQueryParams
import grails.test.spock.IntegrationSpec
import org.joda.time.DateTime
import org.junit.Test

class CsiSystemMeasuredValueServiceIntegrationSpec extends IntegrationSpec {

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

    CsiSystemMeasuredValueService csiSystemMeasuredValueService
    MeasuredValueTagService measuredValueTagService
    EventMeasuredValueService eventMeasuredValueService
    PageMeasuredValueService pageMeasuredValueService
    ShopMeasuredValueService shopMeasuredValueService

    MeasuredValueInterval hourly
    MeasuredValueInterval daily
    MeasuredValueInterval weekly
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
        CsiAggregation.list().each {
            it.delete()
        }
    }

    void "test something"() {
    }

    @Test
    void "find no measuredValues if no existing"() {
        when:
        List<CsiAggregation> hourlyMvs = eventMeasuredValueService.findAll(fromHourly.toDate(),toHourly.toDate(),hourly,connectivityProfile)
        List<CsiAggregation> pageDailyMvs = pageMeasuredValueService.findAll(fromHourly.toDate(),toHourly.toDate(),daily)
        List<CsiAggregation> pageWeeklyMvs = pageMeasuredValueService.findAll(fromHourly.toDate(),toHourly.toDate(),weekly)
        List<CsiAggregation> shopDailyMvs = shopMeasuredValueService.findAll(fromHourly.toDate(),toHourly.toDate(),daily)
        List<CsiAggregation> shopWeeklyMvs = shopMeasuredValueService.findAll(fromHourly.toDate(),toHourly.toDate(),weekly)
        List<CsiAggregation> csiSystemDailyMvs = csiSystemMeasuredValueService.findAll(fromHourly.toDate(),toHourly.toDate(),daily)
        List<CsiAggregation> csiSystemWeeklyMvs = csiSystemMeasuredValueService.findAll(fromHourly.toDate(),toHourly.toDate(),weekly)

        then:
        hourlyMvs.isEmpty()
        pageDailyMvs.isEmpty()
        pageWeeklyMvs.isEmpty()
        shopDailyMvs.isEmpty()
        shopWeeklyMvs.isEmpty()
        csiSystemDailyMvs.isEmpty()
        csiSystemWeeklyMvs.isEmpty()
    }

    @Test
    void "calculate empty measuredValue if no existing"() {
        given:
        MvQueryParams getAllParams = createGetAllQueryParam()

        when:
        List<CsiAggregation> hourlyMvs = eventMeasuredValueService.getHourylMeasuredValues(fromHourly.toDate(),toHourly.toDate(),getAllParams)
        List<CsiAggregation> pageDailyMvs = pageMeasuredValueService.getOrCalculatePageMeasuredValues(fromHourly.toDate(),toHourly.toDate(),daily, [jobGroup1])
        List<CsiAggregation> pageWeeklyMvs = pageMeasuredValueService.getOrCalculatePageMeasuredValues(fromHourly.toDate(),toHourly.toDate(),weekly, [jobGroup1])
        List<CsiAggregation> shopDailyMvs = shopMeasuredValueService.getOrCalculateShopMeasuredValues(fromHourly.toDate(),toHourly.toDate(),daily, [jobGroup1])
        List<CsiAggregation> shopWeeklyMvs = shopMeasuredValueService.getOrCalculateShopMeasuredValues(fromHourly.toDate(),toHourly.toDate(),weekly, [jobGroup1])
        List<CsiAggregation> csiSystemDailyMvs = csiSystemMeasuredValueService.getOrCalculateCsiSystemMeasuredValues(fromHourly.toDate(),toHourly.toDate(),daily, CsiSystem.findAll())
        List<CsiAggregation> csiSystemWeeklyMvs = csiSystemMeasuredValueService.getOrCalculateCsiSystemMeasuredValues(fromHourly.toDate(),toHourly.toDate(),weekly, CsiSystem.findAll())

        then:
        hourlyMvs.size() == 0

        pageDailyMvs.size() == Page.count()
        !pageDailyMvs.any {
            it.csByWptDocCompleteInPercent != null
        }

        pageWeeklyMvs.size() == Page.count()
        !pageWeeklyMvs.any {
            it.csByWptDocCompleteInPercent != null
        }

        shopDailyMvs.size() == 1
        !shopDailyMvs.any {
            it.csByWptDocCompleteInPercent != null
        }

        shopWeeklyMvs.size() == 1
        !shopWeeklyMvs.any {
            it.csByWptDocCompleteInPercent != null
        }

        csiSystemDailyMvs.size() == CsiSystem.count()
        !csiSystemDailyMvs.any {
            it.csByWptDocCompleteInPercent != null
        }

        csiSystemWeeklyMvs.size() == CsiSystem.count()
        !csiSystemWeeklyMvs.any {
            it.csByWptDocCompleteInPercent != null
        }
    }

    @Test
    void "calculate single measuredValue if 1 is existing"() {
        given:
        MvQueryParams getAllParams = createGetAllQueryParam()
        createSingleHourlyMeasuredValues()

        when:
        List<CsiAggregation> hourlyMvs = eventMeasuredValueService.getHourylMeasuredValues(fromHourly.toDate(),toHourly.toDate(),getAllParams)
        List<CsiAggregation> pageDailyMvs = pageMeasuredValueService.getOrCalculatePageMeasuredValues(fromHourly.toDate(),toHourly.toDate(),daily, [jobGroup1])
        List<CsiAggregation> pageWeeklyMvs = pageMeasuredValueService.getOrCalculatePageMeasuredValues(fromHourly.toDate(),toHourly.toDate(),weekly, [jobGroup1])
        List<CsiAggregation> shopDailyMvs = shopMeasuredValueService.getOrCalculateShopMeasuredValues(fromHourly.toDate(),toHourly.toDate(),daily, [jobGroup1])
        List<CsiAggregation> shopWeeklyMvs = shopMeasuredValueService.getOrCalculateShopMeasuredValues(fromHourly.toDate(),toHourly.toDate(),weekly, [jobGroup1])
        List<CsiAggregation> csiSystemDailyMvs = csiSystemMeasuredValueService.getOrCalculateCsiSystemMeasuredValues(fromHourly.toDate(),toHourly.toDate(),daily, CsiSystem.findAll())
        List<CsiAggregation> csiSystemWeeklyMvs = csiSystemMeasuredValueService.getOrCalculateCsiSystemMeasuredValues(fromHourly.toDate(),toHourly.toDate(),weekly, CsiSystem.findAll())

        then:
        hourlyMvs.size() == 1
        hourlyMvs[0].csByWptDocCompleteInPercent == DEFAULT_MV_VALUE

        pageDailyMvs.size() == Page.count()
        pageDailyMvs[0].csByWptDocCompleteInPercent == DEFAULT_MV_VALUE

        pageWeeklyMvs.size() == Page.count()
        pageWeeklyMvs[0].csByWptDocCompleteInPercent == DEFAULT_MV_VALUE

        shopDailyMvs.size() == 1
        shopDailyMvs[0].csByWptDocCompleteInPercent == DEFAULT_MV_VALUE

        shopWeeklyMvs.size() == 1
        shopWeeklyMvs[0].csByWptDocCompleteInPercent == DEFAULT_MV_VALUE

        csiSystemDailyMvs.size() == CsiSystem.count()
        csiSystemDailyMvs[0].csByWptDocCompleteInPercent == DEFAULT_MV_VALUE

        csiSystemWeeklyMvs.size() == CsiSystem.count()
        csiSystemWeeklyMvs[0].csByWptDocCompleteInPercent == DEFAULT_MV_VALUE
    }

    @Test
    void "calculate single weekly Mv if 1 is existing and only highest weekly-aggregation is called"() {
        given:
        createSingleHourlyMeasuredValues()

        when:
        List<CsiAggregation> csiSystemWeeklyMvs = csiSystemMeasuredValueService.getOrCalculateCsiSystemMeasuredValues(fromHourly.toDate(),toHourly.toDate(),weekly, [csiSystem])
        List<CsiAggregation> hourlyMvs = eventMeasuredValueService.findAll(fromHourly.toDate(),toHourly.toDate(),hourly,connectivityProfile)
        List<CsiAggregation> pageDailyMvs = pageMeasuredValueService.findAll(fromHourly.toDate(),toHourly.toDate(),daily)
        List<CsiAggregation> pageWeeklyMvs = pageMeasuredValueService.findAll(fromHourly.toDate(),toHourly.toDate(),weekly)
        List<CsiAggregation> shopDailyMvs = shopMeasuredValueService.findAll(fromHourly.toDate(),toHourly.toDate(),daily)
        List<CsiAggregation> shopWeeklyMvs = shopMeasuredValueService.findAll(fromHourly.toDate(),toHourly.toDate(),weekly)
        List<CsiAggregation> csiSystemDailyMvs = csiSystemMeasuredValueService.findAll(fromHourly.toDate(),toHourly.toDate(),daily)

        then:
        hourlyMvs.size() == 1
        hourlyMvs[0].csByWptDocCompleteInPercent == DEFAULT_MV_VALUE

        pageDailyMvs.size() == 0

        pageWeeklyMvs.size() == csiSystem.affectedJobGroups.size() * Page.count()
        pageWeeklyMvs[0].csByWptDocCompleteInPercent == DEFAULT_MV_VALUE

        shopDailyMvs.size() == 0

        shopWeeklyMvs.size() == csiSystem.affectedJobGroups.size()
        shopWeeklyMvs[0].csByWptDocCompleteInPercent == DEFAULT_MV_VALUE

        csiSystemDailyMvs.size() == 0

        csiSystemWeeklyMvs.size() == 1
        csiSystemWeeklyMvs[0].csByWptDocCompleteInPercent == DEFAULT_MV_VALUE
    }

    @Test
    void "aggregate to single measuredValue if 2 hourlys are existing"() {
        given:
        MvQueryParams getAllParams = createGetAllQueryParam()
        createTwoHourlyMeasuredValues()

        when:
        List<CsiAggregation> hourlyMvs = eventMeasuredValueService.getHourylMeasuredValues(fromHourly.toDate(),toHourly.toDate(),getAllParams)
        List<CsiAggregation> pageDailyMvs = pageMeasuredValueService.getOrCalculatePageMeasuredValues(fromHourly.toDate(),toHourly.toDate(),daily, [jobGroup1])
        List<CsiAggregation> pageWeeklyMvs = pageMeasuredValueService.getOrCalculatePageMeasuredValues(fromHourly.toDate(),toHourly.toDate(),weekly, [jobGroup1])
        List<CsiAggregation> shopDailyMvs = shopMeasuredValueService.getOrCalculateShopMeasuredValues(fromHourly.toDate(),toHourly.toDate(),daily, [jobGroup1])
        List<CsiAggregation> shopWeeklyMvs = shopMeasuredValueService.getOrCalculateShopMeasuredValues(fromHourly.toDate(),toHourly.toDate(),weekly, [jobGroup1])
        List<CsiAggregation> csiSystemDailyMvs = csiSystemMeasuredValueService.getOrCalculateCsiSystemMeasuredValues(fromHourly.toDate(),toHourly.toDate(),daily, [csiSystem])
        List<CsiAggregation> csiSystemWeeklyMvs = csiSystemMeasuredValueService.getOrCalculateCsiSystemMeasuredValues(fromHourly.toDate(),toHourly.toDate(),weekly, [csiSystem])

        then:
        double expectedHourlySize = 2.0
        hourlyMvs.size() == expectedHourlySize.toInteger()
        hourlyMvs[0].csByWptDocCompleteInPercent == DEFAULT_MV_VALUE


        pageDailyMvs.size() == Page.count()
        pageDailyMvs.find {
            it.csByWptDocCompleteInPercent == null
        } != null
        pageDailyMvs.find {
            it.csByWptDocCompleteInPercent != null
        }.csByWptDocCompleteInPercent == DEFAULT_MV_VALUE


        pageWeeklyMvs.size() == Page.count()
        pageWeeklyMvs.find {
            it.csByWptDocCompleteInPercent == null
        } != null
        pageWeeklyMvs.find {
            it.csByWptDocCompleteInPercent != null
        }.csByWptDocCompleteInPercent == DEFAULT_MV_VALUE

        shopDailyMvs.size() == 1
        shopDailyMvs[0].csByWptDocCompleteInPercent == DEFAULT_MV_VALUE

        shopWeeklyMvs.size() == 1
        shopWeeklyMvs[0].csByWptDocCompleteInPercent == DEFAULT_MV_VALUE

        csiSystemDailyMvs.size() == CsiSystem.count()
        csiSystemDailyMvs[0].csByWptDocCompleteInPercent == DEFAULT_MV_VALUE

        csiSystemWeeklyMvs.size() == CsiSystem.count()
        csiSystemWeeklyMvs[0].csByWptDocCompleteInPercent == DEFAULT_MV_VALUE
    }

    @Test
    void "aggregate to 2 daily- and a single weekly-measuredValue if 2 hourlys on different days are existing"() {
        given:
        MvQueryParams getAllParams = createGetAllQueryParam()
        createTwoHourlyMeasuredValuesOnDifferentDays()

        when:
        List<CsiAggregation> hourlyMvs = eventMeasuredValueService.getHourylMeasuredValues(fromWeekly.toDate(),toWeekly.toDate(),getAllParams)
        List<CsiAggregation> pageDailyMvs = pageMeasuredValueService.getOrCalculatePageMeasuredValues(fromWeekly.toDate(),toWeekly.toDate(),daily, [jobGroup1])
        List<CsiAggregation> pageWeeklyMvs = pageMeasuredValueService.getOrCalculatePageMeasuredValues(fromWeekly.toDate(),toWeekly.toDate(),weekly, [jobGroup1])
        List<CsiAggregation> shopDailyMvs = shopMeasuredValueService.getOrCalculateShopMeasuredValues(fromWeekly.toDate(),toWeekly.toDate(),daily, [jobGroup1])
        List<CsiAggregation> shopWeeklyMvs = shopMeasuredValueService.getOrCalculateShopMeasuredValues(fromWeekly.toDate(),toWeekly.toDate(),weekly, [jobGroup1])
        List<CsiAggregation> csiSystemDailyMvs = csiSystemMeasuredValueService.getOrCalculateCsiSystemMeasuredValues(fromWeekly.toDate(),toWeekly.toDate(),daily, CsiSystem.findAll())
        List<CsiAggregation> csiSystemWeeklyMvs = csiSystemMeasuredValueService.getOrCalculateCsiSystemMeasuredValues(fromWeekly.toDate(),toWeekly.toDate(),weekly, CsiSystem.findAll())

        then:
        int expectedHourlySize = 2
        hourlyMvs.size() == expectedHourlySize
        hourlyMvs[0].csByWptDocCompleteInPercent == DEFAULT_MV_VALUE


        int daysInInterval = (toWeekly.dayOfMonth - fromWeekly.dayOfMonth) + 1
        pageDailyMvs.size() == Page.count() * daysInInterval
        pageDailyMvs.find {
            it.csByWptDocCompleteInPercent == null
        } != null
        pageDailyMvs.find {
            it.csByWptDocCompleteInPercent != null
        }.csByWptDocCompleteInPercent == DEFAULT_MV_VALUE


        pageWeeklyMvs.size() == Page.count()
        pageWeeklyMvs.find {
            it.csByWptDocCompleteInPercent == null
        } != null
        pageWeeklyMvs.find {
            it.csByWptDocCompleteInPercent != null
        }.csByWptDocCompleteInPercent == DEFAULT_MV_VALUE

        shopDailyMvs.size() == daysInInterval
        shopDailyMvs[0].csByWptDocCompleteInPercent == DEFAULT_MV_VALUE

        shopWeeklyMvs.size() == 1
        shopWeeklyMvs[0].csByWptDocCompleteInPercent == DEFAULT_MV_VALUE

        csiSystemDailyMvs.size() == CsiSystem.count() * daysInInterval
        csiSystemDailyMvs[0].csByWptDocCompleteInPercent == DEFAULT_MV_VALUE

        csiSystemWeeklyMvs.size() == CsiSystem.count()
        csiSystemWeeklyMvs[0].csByWptDocCompleteInPercent == DEFAULT_MV_VALUE
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

        jobGroup1 = TestDataUtil.createJobGroup(jobGroupName1,JobGroupType.CSI_AGGREGATION)
        jobGroup1.csiConfiguration = csiConfiguration
        jobGroup2 = TestDataUtil.createJobGroup(jobGroupName2,JobGroupType.CSI_AGGREGATION)
        jobGroup2.csiConfiguration = csiConfiguration

        csiSystem = TestDataUtil.createCsiSystem("label",[TestDataUtil.createJobGroupWeight(jobGroup1,1.0),TestDataUtil.createJobGroupWeight(jobGroup2,1.0)])

        measuredEvent1 = TestDataUtil.createMeasuredEvent(eventName1,page1)
        measuredEvent2 = TestDataUtil.createMeasuredEvent(eventName2,page2)

        WebPageTestServer server = TestDataUtil.createWebPageTestServer("test","test",true,"http://test.com")
        location1 = TestDataUtil.createLocation(server,locationName1,browser1,true)
        location2 = TestDataUtil.createLocation(server,locationName2,browser2,true)

        hourly = new MeasuredValueInterval(name: "hourly", intervalInMinutes: MeasuredValueInterval.HOURLY).save(validate: false)
        daily = new MeasuredValueInterval(name: "daily", intervalInMinutes: MeasuredValueInterval.DAILY).save(validate: false)
        weekly = new MeasuredValueInterval(name: "weekly", intervalInMinutes: MeasuredValueInterval.WEEKLY).save(validate: false)

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


    private void createSingleHourlyMeasuredValues() {
        createMeasuredValues(inHourlyInterval1)
    }

    private void createTwoHourlyMeasuredValues() {
        createMeasuredValues(inHourlyInterval1)
        createMeasuredValues(inHourlyInterval2)
    }

    private void createTwoHourlyMeasuredValuesOnDifferentDays() {
        createMeasuredValues(inWeeklyInterval1)
        createMeasuredValues(inWeeklyInterval2)
    }

    private void createMeasuredValues(DateTime started) {
        new CsiAggregation(
                started:started.toDate(),
                interval: hourly,
                aggregator: measuredEvent,
                tag: measuredValueTagService.createHourlyEventTag(jobGroup1,measuredEvent1,page1,browser1,location1),
                csiSystem: csiSystem,
                csByWptDocCompleteInPercent: DEFAULT_MV_VALUE,
                closedAndCalculated: true,
                connectivityProfile: connectivityProfile,
                underlyingEventResultsByWptDocComplete: '1,2,3,4').save(failOnError: true)
    }
}

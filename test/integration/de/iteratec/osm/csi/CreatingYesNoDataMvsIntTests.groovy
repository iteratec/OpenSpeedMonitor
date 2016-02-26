/* 
* OpenSpeedMonitor (OSM)
* Copyright 2014 iteratec GmbH
* 
* Licensed under the Apache License, Version 2.0 (the "License"); 
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* 
* 	http://www.apache.org/licenses/LICENSE-2.0
* 
* Unless required by applicable law or agreed to in writing, software 
* distributed under the License is distributed on an "AS IS" BASIS, 
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
* See the License for the specific language governing permissions and 
* limitations under the License.
*/

package de.iteratec.osm.csi

import de.iteratec.osm.report.chart.CsiAggregationUtilService

import static org.hamcrest.Matchers.*
import static org.junit.Assert.*
import grails.test.mixin.TestMixin
import grails.test.mixin.integration.IntegrationTestMixin

import org.joda.time.DateTime
import org.junit.After
import org.junit.Before
import org.junit.BeforeClass

import de.iteratec.osm.measurement.schedule.Job
import de.iteratec.osm.measurement.schedule.JobGroup

import de.iteratec.osm.report.chart.AggregatorType
import de.iteratec.osm.report.chart.MeasurandGroup
import de.iteratec.osm.report.chart.CsiAggregation
import de.iteratec.osm.report.chart.CsiAggregationInterval
import de.iteratec.osm.result.MeasuredEvent
import de.iteratec.osm.measurement.script.Script
import de.iteratec.osm.measurement.environment.Browser
import de.iteratec.osm.measurement.environment.Location
import de.iteratec.osm.measurement.environment.WebPageTestServer

/**
 * Contains tests which test the creation of {@link de.iteratec.osm.report.chart.CsiAggregation}s without the existence of corresponding {@link EventResult}s.<br>
 * For all persisted {@link de.iteratec.osm.report.chart.CsiAggregation}s a {@link CsiAggregationUpdateEvent} should be created, which marks measured value as calculated.
 * @author nkuhn
 *
 */
@TestMixin(IntegrationTestMixin)
class CreatingYesNoDataMvsIntTests extends IntTestWithDBCleanup {

    static transactional = false

    /** injected by grails */
    PageCsiAggregationService pageCsiAggregationService
    ShopCsiAggregationService shopCsiAggregationService
    CsiAggregationUtilService csiAggregationUtilService

    CsiAggregationInterval hourly = CsiAggregationInterval.findByIntervalInMinutes(CsiAggregationInterval.HOURLY)
    CsiAggregationInterval weekly = CsiAggregationInterval.findByIntervalInMinutes(CsiAggregationInterval.WEEKLY)
    AggregatorType job = AggregatorType.findByName(AggregatorType.MEASURED_EVENT)
    AggregatorType page = AggregatorType.findByName(AggregatorType.PAGE)
    AggregatorType shop = AggregatorType.findByName(AggregatorType.SHOP)
    DateTime startOfCreatingHourlyEventValues = new DateTime(2012, 1, 9, 0, 0, 0)
    DateTime startOfCreatingWeeklyPageValues = new DateTime(2012, 2, 6, 0, 0, 0)
    DateTime startOfCreatingWeeklyShopValues = new DateTime(2012, 3, 12, 0, 0, 0)

    @Before
    void setUp() {
    }

    @After
    void tearDown() {
    }

    /**
     * Creating weekly-page {@link CsiAggregation}s without data.
     */
    void testCreatingWeeklyPageValues() {
        DateTime endDate = startOfCreatingWeeklyPageValues.plusWeeks(1)
        List<CsiAggregation> wpmvs = pageCsiAggregationService.getOrCalculateWeeklyPageCsiAggregations(startOfCreatingWeeklyPageValues.toDate(), endDate.toDate())
        Integer countWeeks = 2
        Integer countPages = 7
        assertThat(wpmvs.size(), is(countWeeks * countPages))
        wpmvs.each {
            assertTrue(it.isCalculated())
        }
    }

    /**
     * Creating weekly-shop {@link de.iteratec.osm.report.chart.CsiAggregation}s without data.
     */
    void testCreatingWeeklyShopValues() {
        DateTime endDate = startOfCreatingWeeklyShopValues.plusWeeks(1)
        List<CsiAggregation> wsmvs = shopCsiAggregationService.getOrCalculateWeeklyShopCsiAggregations(startOfCreatingWeeklyShopValues.toDate(), endDate.toDate())
        Integer countWeeks = 2
        Integer countPages = 7
        assertThat(wsmvs.size(), is(countWeeks))
        wsmvs.each {
            assertTrue(it.isCalculated())
        }

        Date endOfLastWeek = csiAggregationUtilService.resetToEndOfActualInterval(endDate, CsiAggregationInterval.WEEKLY).toDate()
        assert pageCsiAggregationService.findAll(startOfCreatingWeeklyShopValues.toDate(), endDate.toDate(), weekly).size() == countWeeks * countPages
    }

    /**
     * Creating testdata.
     */
    @BeforeClass
    static void createTestData() {
        createCsiAggregationIntervals()
        createAggregatorTypes()
        createPagesAndEvents()
        createBrowsers()
        createHoursOfDay()
        createServer()
        createLocations()
        createJobGroups()
        createJobs()
    }

    private static createAggregatorTypes() {
        AggregatorType.findByName(AggregatorType.MEASURED_EVENT) ?: new AggregatorType(
                name: AggregatorType.MEASURED_EVENT, measurandGroup: MeasurandGroup.NO_MEASURAND).save(failOnError: true)

        AggregatorType.findByName(AggregatorType.PAGE) ?: new AggregatorType(
                name: AggregatorType.PAGE, measurandGroup: MeasurandGroup.NO_MEASURAND).save(failOnError: true)

        AggregatorType.findByName(AggregatorType.PAGE_AND_BROWSER) ?: new AggregatorType(
                name: AggregatorType.PAGE_AND_BROWSER, measurandGroup: MeasurandGroup.NO_MEASURAND).save(failOnError: true)

        AggregatorType.findByName(AggregatorType.SHOP) ?: new AggregatorType(
                name: AggregatorType.SHOP, measurandGroup: MeasurandGroup.NO_MEASURAND).save(failOnError: true)
    }

    private static createCsiAggregationIntervals() {
        CsiAggregationInterval.findByIntervalInMinutes(CsiAggregationInterval.HOURLY) ?: new CsiAggregationInterval(
                name: "hourly",
                intervalInMinutes: CsiAggregationInterval.HOURLY
        ).save(failOnError: true)

        CsiAggregationInterval.findByIntervalInMinutes(CsiAggregationInterval.DAILY) ?: new CsiAggregationInterval(
                name: "daily",
                intervalInMinutes: CsiAggregationInterval.DAILY
        ).save(failOnError: true)

        CsiAggregationInterval.findByIntervalInMinutes(CsiAggregationInterval.WEEKLY) ?: new CsiAggregationInterval(
                name: "weekly",
                intervalInMinutes: CsiAggregationInterval.WEEKLY
        ).save(failOnError: true)
    }

    private static void createJobGroups() {
        String csiGroupName = 'CSI'
        JobGroup.findByName(csiGroupName) ?: new JobGroup(
                name: csiGroupName).save(failOnError: true)
    }

    private static void createJobs() {
        Script script = Script.createDefaultScript('Unnamed').save(failOnError: true)
        Location locationFf = Location.findByLabel('ffLocationLabel')
        Location locationIe = Location.findByLabel('ieLocationLabel')
        JobGroup jobGroup = JobGroup.findByName('CSI')
        Page pageHp = Page.findByName('HP')
        Page pageMes = Page.findByName('MES')

        Job testjob_HP = new Job(
                label: 'testjob_HP',
                location: locationFf,
                page: pageHp,
                active: false,
                description: '',
                runs: 1,
                jobGroup: jobGroup,
                script: script,
                maxDownloadTimeInMinutes: 60,
                noTrafficShapingAtAll: true
        ).save(failOnError: true)

        Job testjob_MES = new Job(
                label: 'testjob_MES',
                location: locationIe,
                page: pageMes,
                active: false,
                description: '',
                runs: 1,
                jobGroup: jobGroup,
                script: script,
                maxDownloadTimeInMinutes: 60,
                noTrafficShapingAtAll: true
        ).save(failOnError: true)
    }

    private static void createHoursOfDay() {
        CsiDay day = new CsiDay()
        day.with {
            hour0Weight = 2.9
            hour1Weight = 0.4
            hour2Weight = 0.2
            hour3Weight = 0.1
            hour4Weight = 0.1
            hour5Weight = 0.2
            hour6Weight = 0.7
            hour7Weight = 1.7
            hour8Weight = 3.2
            hour9Weight = 4.8
            hour10Weight = 5.6
            hour11Weight = 5.7
            hour12Weight = 5.5
            hour13Weight = 5.8
            hour14Weight = 5.9
            hour15Weight = 6.0
            hour16Weight = 6.7
            hour17Weight = 7.3
            hour18Weight = 7.7
            hour19Weight = 8.8
            hour20Weight = 9.3
            hour21Weight = 7.0
            hour22Weight = 3.6
            hour23Weight = 0.9
        }
        day.save(failOnError: true)
    }

    private static void createPagesAndEvents() {
        ['HP', 'MES', 'SE', 'ADS', 'WKBS', 'WK', Page.UNDEFINED].each { pageName ->
            Double weight = 0
            switch (pageName) {
                case 'HP': weight = 6; break
                case 'MES': weight = 9; break
                case 'SE': weight = 36; break
                case 'ADS': weight = 43; break
                case 'WKBS': weight = 3; break
                case 'WK': weight = 3; break
            }
            Page page = Page.findByName(pageName) ?: new Page(
                    name: pageName,
                    weight: weight).save(failOnError: true)

            // Simply create one event
            new MeasuredEvent(
                    name: 'CreatingYesNoDataMvsIntTests-' + pageName,
                    testedPage: page
            ).save(failOnError: true)
        }
    }

    private static void createBrowsers() {
        String browserName = "undefined"
        Browser.findByName(browserName) ?: new Browser(
                name: browserName,
                weight: 0)
                .addToBrowserAliases(alias: "undefined")
                .save(failOnError: true)
        browserName = "IE"
        Browser browserIE = Browser.findByName(browserName) ?: new Browser(
                name: browserName,
                weight: 45)
                .addToBrowserAliases(alias: "IE")
                .addToBrowserAliases(alias: "IE8")
                .addToBrowserAliases(alias: "Internet Explorer")
                .addToBrowserAliases(alias: "Internet Explorer 8")
                .save(failOnError: true)
        browserName = "FF"
        Browser browserFF = Browser.findByName(browserName) ?: new Browser(
                name: browserName,
                weight: 55)
                .addToBrowserAliases(alias: "FF")
                .addToBrowserAliases(alias: "FF7")
                .addToBrowserAliases(alias: "Firefox")
                .addToBrowserAliases(alias: "Firefox7")
                .save(failOnError: true)
    }

    private static void createServer() {
        WebPageTestServer server1
        server1 = new WebPageTestServer(
                baseUrl: 'http://wpt.server.de',
                active: true,
                label: 'server 1 - wpt server',
                proxyIdentifier: 'server 1 - wpt server'
        ).save(failOnError: true)
    }

    private static void createLocations() {
        WebPageTestServer server1 = WebPageTestServer.findByLabel('server 1 - wpt server')
        Browser browserFF = Browser.findByName("FF")
        Browser browserIE = Browser.findByName("IE")
        Location ffAgent1, ieAgent1
        ffAgent1 = new Location(
                active: true,
                valid: 1,
                location: 'ffLocationLocation',
                label: 'ffLocationLabel',
                browser: browserFF,
                wptServer: server1
        ).save(failOnError: true)
        ieAgent1 = new Location(
                active: true,
                valid: 1,
                location: 'ieLocationLocation',
                label: 'ieLocationLabel',
                browser: browserIE,
                wptServer: server1
        ).save(failOnError: true)
    }
}

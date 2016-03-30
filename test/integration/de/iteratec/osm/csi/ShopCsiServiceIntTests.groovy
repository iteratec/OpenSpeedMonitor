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

import de.iteratec.osm.OsmConfiguration
import de.iteratec.osm.api.CsiByEventResultsService
import de.iteratec.osm.api.dto.CsiByEventResultsDto
import de.iteratec.osm.csi.weighting.WeightFactor
import de.iteratec.osm.measurement.environment.Browser
import de.iteratec.osm.measurement.environment.Location
import de.iteratec.osm.measurement.environment.WebPageTestServer
import de.iteratec.osm.measurement.schedule.ConnectivityProfile
import de.iteratec.osm.measurement.schedule.Job
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.measurement.script.Script
import de.iteratec.osm.result.*
import grails.test.spock.IntegrationSpec
import org.joda.time.DateTime
import org.joda.time.DateTimeZone

/**
 *
 */
class ShopCsiServiceIntTests extends IntegrationSpec {

    static final double DELTA = 1e-10

    // csi values
    public static final double csiHpIe1 = 52d
    public static final double csiHpIe2 = 54d
    public static final double csiHpIe3 = 52d
    public static final double csiHpFf1 = 54d
    public static final double csiHpFf2 = 52d
    public static final double csiHpFf3 = 54d
    public static final double csiMesIe1 = 92d
    public static final double csiMesIe2 = 94d
    public static final double csiMesIe3 = 92d
    public static final double csiMesFf1 = 94d
    public static final double csiMesFf2 = 92d
    public static final double csiMesFf3 = 94d

    CsiByEventResultsService csiByEventResultsService
    private static final DateTime START = new DateTime(2014, 1, 1, 0, 0, DateTimeZone.UTC)
    private static final DateTime END = new DateTime(2014, 12, 31, 0, 0, DateTimeZone.UTC)
    private static int groups = 0

    void setupSpec() {
        //test-data common to all tests
        createOsmConfig()
        createPagesAndEvents()
        createBrowsers()
        createJobGroup()
        createEventResults()
        //mocks
    }

    void setup() {
        csiByEventResultsService.csTargetGraphDaoService.metaClass.getActualCsTargetGraph = { return null }
    }

    void "test retrieveCsi with system and page"() {
        given:
        MvQueryParams queryParams = new MvQueryParams()
        queryParams.jobGroupIds.addAll(JobGroup.list()*.id)

        when:
        CsiByEventResultsDto systemCsi = csiByEventResultsService.retrieveCsi(START, END, queryParams, [WeightFactor.BROWSER_CONNECTIVITY_COMBINATION] as Set)

        Double ieWeight = 45d
        Double ffWeight = 55d
        Double avgHpIe = (csiHpIe1 + csiHpIe2 + csiHpIe3) / 3
        Double avgHpFf = (csiHpFf1 + csiHpFf2 + csiHpFf3) / 3
        Double avgMesIe = (csiMesIe1 + csiMesIe2 + csiMesIe3) / 3
        Double avgMesFf = (csiMesFf1 + csiMesFf2 + csiMesFf3) / 3

        Double expectedCsi = ((avgHpIe * ieWeight) + (avgHpFf * ffWeight) + (avgMesIe * ieWeight) + (avgMesFf * ffWeight)) /
                (ieWeight + ieWeight + ffWeight + ffWeight)

        then:
        EventResult.list().size() == 12
        Math.abs(expectedCsi - systemCsi.csiValueAsPercentage) < DELTA
    }

    void "test retrieveCsi only with system"() {
        given:
        MvQueryParams queryParams = new MvQueryParams()
        queryParams.jobGroupIds.addAll(JobGroup.list()*.id)

        when:
        CsiByEventResultsDto systemCsi = csiByEventResultsService.retrieveCsi(START, END, queryParams, [WeightFactor.PAGE, WeightFactor.BROWSER_CONNECTIVITY_COMBINATION] as Set)

        Double ieWeight = 45d
        Double ffWeight = 55d
        Double hpWeight = 6d
        Double mesWeight = 9d
        Double avgHpIe = (csiHpIe1 + csiHpIe2 + csiHpIe3) / 3
        Double avgHpFf = (csiHpFf1 + csiHpFf2 + csiHpFf3) / 3
        Double avgMesIe = (csiMesIe1 + csiMesIe2 + csiMesIe3) / 3
        Double avgMesFf = (csiMesFf1 + csiMesFf2 + csiMesFf3) / 3

        Double expectedCsi = ((avgHpIe * hpWeight * ieWeight) + (avgHpFf * hpWeight * ffWeight) + (avgMesIe * mesWeight * ieWeight) + (avgMesFf * mesWeight * ffWeight)) /
                ((ieWeight * hpWeight) + (ieWeight * mesWeight) + (ffWeight * hpWeight) + (ffWeight * mesWeight))

        then:
        EventResult.list().size() == 12
        Math.abs(expectedCsi - systemCsi.csiValueAsPercentage) < DELTA
    }

    void "test duplicate hp-results which shouldn't change system-csi at all (should just improve accuracy of hp-proportion of csi)" () {
        given:
        MvQueryParams queryParams = new MvQueryParams()
        queryParams.jobGroupIds.addAll(JobGroup.list()*.id)


        Double ieWeight = 45d
        Double ffWeight = 55d
        Double hpWeight = 6d
        Double mesWeight = 9d
        Double avgHpIe = (csiHpIe1 + csiHpIe2 + csiHpIe3) / 3
        Double avgHpFf = (csiHpFf1 + csiHpFf2 + csiHpFf3) / 3
        Double avgMesIe = (csiMesIe1 + csiMesIe2 + csiMesIe3) / 3
        Double avgMesFf = (csiMesFf1 + csiMesFf2 + csiMesFf3) / 3

        Double expectedCsi = ((avgHpIe * hpWeight * ieWeight) + (avgHpFf * hpWeight * ffWeight) + (avgMesIe * mesWeight * ieWeight) + (avgMesFf * mesWeight * ffWeight)) /
                ((ieWeight * hpWeight) + (ieWeight * mesWeight) + (ffWeight * hpWeight) + (ffWeight * mesWeight))

        when:
        //duplicate hp-results which shouldn't change system-csi at all (should just improve accuracy of hp-proportion of csi)
        MeasuredEvent eventHomepage = MeasuredEvent.findByName('event-HP')
        String pageHP_ID = Page.findByName('HP').ident().toString()
        String browserIE_ID = Browser.findByName('IE').ident().toString()
        String browserFF_ID = Browser.findByName('FF').ident().toString()
        createEventResult(eventHomepage, "1;${eventHomepage.ident().toString()};${pageHP_ID};${browserIE_ID};1", csiHpIe1)
        createEventResult(eventHomepage, "1;${eventHomepage.ident().toString()};${pageHP_ID};${browserIE_ID};1", csiHpIe2)
        createEventResult(eventHomepage, "1;${eventHomepage.ident().toString()};${pageHP_ID};${browserIE_ID};1", csiHpIe3)
        createEventResult(eventHomepage, "1;${eventHomepage.ident().toString()};${pageHP_ID};${browserFF_ID};1", csiHpFf1)
        createEventResult(eventHomepage, "1;${eventHomepage.ident().toString()};${pageHP_ID};${browserFF_ID};1", csiHpFf2)
        createEventResult(eventHomepage, "1;${eventHomepage.ident().toString()};${pageHP_ID};${browserFF_ID};1", csiHpFf3)

        CsiByEventResultsDto systemCsi = csiByEventResultsService.retrieveCsi(START, END, queryParams, [WeightFactor.PAGE, WeightFactor.BROWSER_CONNECTIVITY_COMBINATION] as Set)

        then:
        EventResult.list().size() == 18
        Math.abs(expectedCsi - systemCsi.csiValueAsPercentage) < DELTA

        cleanup:
        // delete last six eventResults
        def eventResults = EventResult.list(sort: "id", order: "desc")
        (0..6).each {eventResults[it].delete()}
    }


    private void createEventResults() {
        MeasuredEvent eventHomepage = MeasuredEvent.findByName('event-HP')
        MeasuredEvent eventMes = MeasuredEvent.findByName('event-MES')
        String pageHP_ID = Page.findByName('HP').ident().toString()
        String pageMES_ID = Page.findByName('MES').ident().toString()
        String browserIE_ID = Browser.findByName('IE').ident().toString()
        String browserFF_ID = Browser.findByName('FF').ident().toString()
        // HP|IE
        createEventResult(eventHomepage, "1;${eventHomepage.ident().toString()};${pageHP_ID};${browserIE_ID};1", csiHpIe1)
        createEventResult(eventHomepage, "1;${eventHomepage.ident().toString()};${pageHP_ID};${browserIE_ID};1", csiHpIe2)
        createEventResult(eventHomepage, "1;${eventHomepage.ident().toString()};${pageHP_ID};${browserIE_ID};1", csiHpIe3)
        // HP|FF
        createEventResult(eventHomepage, "1;${eventHomepage.ident().toString()};${pageHP_ID};${browserFF_ID};1", csiHpFf1)
        createEventResult(eventHomepage, "1;${eventHomepage.ident().toString()};${pageHP_ID};${browserFF_ID};1", csiHpFf2)
        createEventResult(eventHomepage, "1;${eventHomepage.ident().toString()};${pageHP_ID};${browserFF_ID};1", csiHpFf3)
        // MES|IE
        createEventResult(eventMes, "1;${eventMes.ident().toString()};${pageMES_ID};${browserIE_ID};1", csiMesIe1)
        createEventResult(eventMes, "1;${eventMes.ident().toString()};${pageMES_ID};${browserIE_ID};1", csiMesIe2)
        createEventResult(eventMes, "1;${eventMes.ident().toString()};${pageMES_ID};${browserIE_ID};1", csiMesIe3)
        // MES|FF
        createEventResult(eventMes, "1;${eventMes.ident().toString()};${pageMES_ID};${browserFF_ID};1", csiMesFf1)
        createEventResult(eventMes, "1;${eventMes.ident().toString()};${pageMES_ID};${browserFF_ID};1", csiMesFf2)
        createEventResult(eventMes, "1;${eventMes.ident().toString()};${pageMES_ID};${browserFF_ID};1", csiMesFf3)
    }

    void createEventResult(MeasuredEvent event, String tag, double value) {
        //data is needed to create a JobResult
        JobGroup group = JobGroup.list()[0]
        Script script = TestDataUtil.createScript("label${groups}", "description", "navigationScript", true)
        WebPageTestServer webPageTestServer = TestDataUtil.createWebPageTestServer("label", "1", true, "http://www.url.de")
        Browser browser = TestDataUtil.createBrowser("browser${groups}", 1)
        Location location = TestDataUtil.createLocation(webPageTestServer, "id", browser, true)
        Job job = TestDataUtil.createJob("Label${groups++}", script, location, group, "descirpiton", 1, false, 20)

        JobResult expectedResult = new JobResult(jobGroupName: "Group", jobConfigLabel: "label", jobConfigRuns: 1, httpStatusCode: 200, job: job, description: "description", date: new Date(), testId: "TestJob").save(validate: false);

        new EventResult(
                measuredEvent: event,
                wptStatus: 200,
                medianValue: true,
                numberOfWptRun: 1,
                cachedView: CachedView.UNCACHED,
                jobResult: expectedResult,
                jobResultDate: START.plusDays(1).toDate(),
                jobResultJobConfigId: 1,
                tag: tag,
                speedIndex: 1,
                csByWptDocCompleteInPercent: value,
                docCompleteTimeInMillisecs: 1000,
                connectivityProfile: ConnectivityProfile.list()[0]
        ).save(failOnError: true)
    }

    private void createJobGroup() {
        JobGroup group = TestDataUtil.createJobGroup("JobGroup")
        ConnectivityProfile profile = TestDataUtil.createConnectivityProfile("unused")
        CsiConfiguration csiConfiguration = TestDataUtil.createCsiConfiguration("CsiConfig${groups}")
        csiConfiguration.browserConnectivityWeights.add(new BrowserConnectivityWeight(browser: Browser.findByName("FF"), connectivity: profile, weight: 55))
        csiConfiguration.browserConnectivityWeights.add(new BrowserConnectivityWeight(browser: Browser.findByName("IE"), connectivity: profile, weight: 45))
        csiConfiguration.pageWeights.add(new PageWeight(page: Page.findByName("HP"), weight: 6))
        csiConfiguration.pageWeights.add(new PageWeight(page: Page.findByName("MES"), weight: 9))
        group.csiConfiguration = csiConfiguration
        group.save(flush: true, failOnError: true)
    }

    void createPagesAndEvents() {
        ['HP', 'MES', 'SE', 'ADS', 'WKBS', 'WK', Page.UNDEFINED].each { pageName ->
            Double weight = 0
            Page page = Page.findByName(pageName) ?: new Page(
                    name: pageName,
                    weight: weight).save(failOnError: true)

            // Simply create one event
            new MeasuredEvent(
                    name: 'event-' + pageName,
                    testedPage: page
            ).save(failOnError: true)
        }

    }

    void createBrowsers() {
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

    void createOsmConfig() {
        new OsmConfiguration(
                detailDataStorageTimeInWeeks: 2,
                defaultMaxDownloadTimeInMinutes: 60,
                minDocCompleteTimeInMillisecs: 250,
                maxDocCompleteTimeInMillisecs: 180000).save(failOnError: true)
    }
}

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
import de.iteratec.osm.report.chart.CsiAggregation
import de.iteratec.osm.result.*
import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import spock.util.mop.ConfineMetaClassChanges

import static de.iteratec.osm.OsmConfiguration.DEFAULT_MAX_VALID_LOADTIME
import static de.iteratec.osm.OsmConfiguration.DEFAULT_MIN_VALID_LOADTIME

@Integration
@Rollback
@ConfineMetaClassChanges([CsiByEventResultsService])
class ShopCsiServiceIntegrationSpec extends NonTransactionalIntegrationSpec {

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


    def setup() {
        CsiAggregation.withNewTransaction {
            createOsmConfig()
            createPagesAndEvents()
            createBrowsers()
            createJobGroup()
            createEventResults()
        }
        csiByEventResultsService.csTargetGraphDaoService = Spy(CsTargetGraphDaoService) {
            getActualCsTargetGraph() >> null
        }
    }

    def cleanup() {
        csiByEventResultsService.csTargetGraphDaoService = grailsApplication.mainContext.getBean("csTargetGraphDaoService")
    }

    void "test retrieveCsi with system and page"() {
        given: "a query for all Jobgroups and a pre-calculated expected csi"
        MvQueryParams queryParams = new MvQueryParams()
        queryParams.jobGroupIds.addAll(JobGroup.list()*.id)

        Double ieWeight = 45d
        Double ffWeight = 55d
        Double avgHpIe = (csiHpIe1 + csiHpIe2 + csiHpIe3) / 3
        Double avgHpFf = (csiHpFf1 + csiHpFf2 + csiHpFf3) / 3
        Double avgMesIe = (csiMesIe1 + csiMesIe2 + csiMesIe3) / 3
        Double avgMesFf = (csiMesFf1 + csiMesFf2 + csiMesFf3) / 3

        Double expectedCsi = ((avgHpIe * ieWeight) + (avgHpFf * ffWeight) + (avgMesIe * ieWeight) + (avgMesFf * ffWeight)) /
                (ieWeight + ieWeight + ffWeight + ffWeight)

        when: "calculating csi for query with weightfactorbrowser connectivity"
        CsiByEventResultsDto systemCsi = csiByEventResultsService.retrieveCsi(START, END, queryParams, [WeightFactor.BROWSER_CONNECTIVITY_COMBINATION] as Set)

        then: "Number of event results is 12 and difference between expected csi and csi from service is below delta"
        EventResult.list().size() == 12
        Math.abs(expectedCsi - systemCsi.csiValueAsPercentage) < DELTA
    }

    void "test retrieveCsi only with system"() {
        given: "a query for all Jobgroups and a pre-calculated expected csi"
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

        when: "calculating csi for query with weightfactors page and browser connectivity"
        CsiByEventResultsDto systemCsi = csiByEventResultsService.retrieveCsi(START, END, queryParams, [WeightFactor.PAGE, WeightFactor.BROWSER_CONNECTIVITY_COMBINATION] as Set)

        then: "Number of event results is 12 and difference between expected csi and csi from service is below delta"
        EventResult.list().size() == 12
        Math.abs(expectedCsi - systemCsi.csiValueAsPercentage) < DELTA
    }

    void "test duplicate hp-results which shouldn't change system-csi at all (should just improve accuracy of hp-proportion of csi)" () {
        given: "a query for all Jobgroups and a pre-calculated expected csi"
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

        when: "calculating csi for query with weightfactors page and browser connectivity with duplicated results"
        //duplicate hp-results which shouldn't change system-csi at all (should just improve accuracy of hp-proportion of csi)
        MeasuredEvent eventHomepage = MeasuredEvent.findByName('event-HP')
        Page pageHP_ID = Page.findByName('HP')
        Browser browserIE_ID = Browser.findByName('IE')
        Browser browserFF_ID = Browser.findByName('FF')
        createEventResult(eventHomepage, pageHP_ID, browserIE_ID, csiHpIe1)
        createEventResult(eventHomepage, pageHP_ID, browserIE_ID, csiHpIe2)
        createEventResult(eventHomepage, pageHP_ID, browserIE_ID, csiHpIe3)
        createEventResult(eventHomepage, pageHP_ID, browserFF_ID, csiHpFf1)
        createEventResult(eventHomepage, pageHP_ID, browserFF_ID, csiHpFf2)
        createEventResult(eventHomepage, pageHP_ID, browserFF_ID, csiHpFf3)

        CsiByEventResultsDto systemCsi = csiByEventResultsService.retrieveCsi(START, END, queryParams, [WeightFactor.PAGE, WeightFactor.BROWSER_CONNECTIVITY_COMBINATION] as Set)

        then: "Number of event results is 12 and difference between expected csi and csi from service is below delta"
        EventResult.list().size() == 18
        Math.abs(expectedCsi - systemCsi.csiValueAsPercentage) < DELTA

        cleanup: "clean up and delete the duplicated event results"
        // delete last six eventResults
        def eventResults = EventResult.list(sort: "id", order: "desc")
        (0..6).each {eventResults[it].delete()}
    }


    private void createEventResults() {
        MeasuredEvent eventHomepage = MeasuredEvent.findByName('event-HP')
        MeasuredEvent eventMes = MeasuredEvent.findByName('event-MES')
        Page pageHP = Page.findByName('HP')
        Page pageMES = Page.findByName('MES')
        Browser browserIE = Browser.findByName('IE')
        Browser browserFF = Browser.findByName('FF')
        // HP|IE
        createEventResult(eventHomepage, pageHP, browserIE, csiHpIe1)
        createEventResult(eventHomepage, pageHP, browserIE, csiHpIe2)
        createEventResult(eventHomepage, pageHP, browserIE, csiHpIe3)
        // HP|FF
        createEventResult(eventHomepage, pageHP, browserFF, csiHpFf1)
        createEventResult(eventHomepage, pageHP, browserFF, csiHpFf2)
        createEventResult(eventHomepage, pageHP, browserFF, csiHpFf3)
        // MES|IE
        createEventResult(eventMes, pageMES, browserIE, csiMesIe1)
        createEventResult(eventMes, pageMES, browserIE, csiMesIe2)
        createEventResult(eventMes, pageMES, browserIE, csiMesIe3)
        // MES|FF
        createEventResult(eventMes, pageMES, browserFF, csiMesFf1)
        createEventResult(eventMes, pageMES, browserFF, csiMesFf2)
        createEventResult(eventMes, pageMES, browserFF, csiMesFf3)
    }

    private void createEventResult(MeasuredEvent event, Page page, Browser browser, double value) {
        //data is needed to create a JobResult
        JobGroup group = JobGroup.list()[0]
        Script script = Script.build(
                label:"label${groups}",
                description:  "description",
                navigationScript:"navigationScript")
        WebPageTestServer webPageTestServer = WebPageTestServer.build(
                label: "label",
                proxyIdentifier: "1",
                active: true,
                baseUrl: "http://www.url.de")
        Location location = Location.build(
                wptServer: webPageTestServer,
                uniqueIdentifierForServer: "id",
                browser: browser,
                active:  true)
        Job job = Job.build(
                label: "Label${groups++}",
                script: script,
                location: location,
                jobGroup: group,
                runs: 1,
                active:  false,
                maxDownloadTimeInMinutes: 20)

        JobResult expectedResult = new JobResult(jobGroupName: "Group", jobConfigLabel: "label", jobConfigRuns: 1, httpStatusCode: 200, job: job, description: "description", date: new Date(), testId: "TestJob").save(validate: false);

        new EventResult(
                jobGroup: group,
                measuredEvent: event,
                page: page,
                browser: browser,
                location: location,
                wptStatus: WptStatus.COMPLETED.getWptStatusCode(),
                medianValue: true,
                numberOfWptRun: 1,
                cachedView: CachedView.UNCACHED,
                jobResult: expectedResult,
                jobResultDate: START.plusDays(1).toDate(),
                jobResultJobConfigId: 1,
                speedIndex: 1,
                csByWptDocCompleteInPercent: value,
                docCompleteTimeInMillisecs: 1000,
                connectivityProfile: ConnectivityProfile.list()[0]
        ).save(failOnError: true)
    }

    private void createJobGroup() {
        JobGroup group = JobGroup.build(name: "JobGroup")
        ConnectivityProfile profile = ConnectivityProfile.build(name: "unused")
        CsiConfiguration csiConfiguration = CsiConfiguration.build(label: "CsiConfig${groups}")
        csiConfiguration.browserConnectivityWeights.add(new BrowserConnectivityWeight(browser: Browser.findByName("FF"), connectivity: profile, weight: 55))
        csiConfiguration.browserConnectivityWeights.add(new BrowserConnectivityWeight(browser: Browser.findByName("IE"), connectivity: profile, weight: 45))
        csiConfiguration.pageWeights.add(new PageWeight(page: Page.findByName("HP"), weight: 6))
        csiConfiguration.pageWeights.add(new PageWeight(page: Page.findByName("MES"), weight: 9))
        group.csiConfiguration = csiConfiguration
        group.save(flush: true, failOnError: true)
    }

    private void createPagesAndEvents() {
        ['HP', 'MES', 'SE', 'ADS', 'WKBS', 'WK', Page.UNDEFINED].each { pageName ->
            Page page = Page.findByName(pageName) ?: new Page(name: pageName).save(failOnError: true)

            // Simply create one event
            new MeasuredEvent(
                    name: 'event-' + pageName,
                    testedPage: page
            ).save(failOnError: true)
        }

    }

    private void createBrowsers() {
        String browserName = "undefined"
        Browser.findByName(browserName) ?: new Browser(name: browserName)
                .addToBrowserAliases(alias: "undefined")
                .save(failOnError: true)
        browserName = "IE"
        Browser browserIE = Browser.findByName(browserName) ?: new Browser(name: browserName)
                .addToBrowserAliases(alias: "IE")
                .addToBrowserAliases(alias: "IE8")
                .addToBrowserAliases(alias: "Internet Explorer")
                .addToBrowserAliases(alias: "Internet Explorer 8")
                .save(failOnError: true)
        browserName = "FF"
        Browser browserFF = Browser.findByName(browserName) ?: new Browser(name: browserName)
                .addToBrowserAliases(alias: "FF")
                .addToBrowserAliases(alias: "FF7")
                .addToBrowserAliases(alias: "Firefox")
                .addToBrowserAliases(alias: "Firefox7")
                .save(failOnError: true)
    }

    private void createOsmConfig() {
        new OsmConfiguration(
                detailDataStorageTimeInWeeks: 2,
                defaultMaxDownloadTimeInMinutes: 60,
                minValidLoadtime: DEFAULT_MIN_VALID_LOADTIME,
                maxValidLoadtime: DEFAULT_MAX_VALID_LOADTIME).save(failOnError: true)
    }
}

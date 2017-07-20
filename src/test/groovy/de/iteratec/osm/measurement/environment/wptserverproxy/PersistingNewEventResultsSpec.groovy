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

package de.iteratec.osm.measurement.environment.wptserverproxy

import de.iteratec.osm.csi.CsiConfiguration
import de.iteratec.osm.csi.CsiDay
import de.iteratec.osm.csi.Page
import de.iteratec.osm.csi.TimeToCsMapping
import de.iteratec.osm.csi.transformation.TimeToCsMappingService
import de.iteratec.osm.measurement.environment.Browser
import de.iteratec.osm.measurement.environment.BrowserAlias
import de.iteratec.osm.measurement.environment.Location
import de.iteratec.osm.measurement.environment.WebPageTestServer
import de.iteratec.osm.measurement.schedule.Job
import de.iteratec.osm.measurement.schedule.JobDaoService
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.measurement.script.Script
import de.iteratec.osm.report.external.MetricReportingService
import de.iteratec.osm.result.*
import de.iteratec.osm.util.PerformanceLoggingService
import grails.buildtestdata.mixin.Build
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import spock.lang.Specification

import static de.iteratec.osm.result.CachedView.CACHED
import static de.iteratec.osm.result.CachedView.UNCACHED

/**
 * Tests the saving of EventResults.
 * Testing the mapping of load-times to customer satisfactions or the persisting of dependent {@link CsiAggregation}s is not the concern of the tests in this class.
 * @author nkuhn
 * @see {@link ProxyService}
 *
 */
@TestFor(ResultPersisterService)
@Build([Location, WebPageTestServer, Job, Page])
@Mock([WebPageTestServer, Browser, Location, Job, JobResult, EventResult, BrowserAlias, Page, MeasuredEvent, JobGroup, Script, CsiConfiguration, TimeToCsMapping, CsiDay])
class PersistingNewEventResultsSpec extends Specification {

    def doWithSpring = {
        performanceLoggingService(PerformanceLoggingService)
        pageService(PageService)
        jobDaoService(JobDaoService)
    }

    void "setup"() {
        service.metricReportingService = Mock(MetricReportingService)
        service.csiValueService = Mock(CsiValueService)
    }

    void "result persistance with old (single step) WPT server"(String fileName, String jobLabel, String pageName) {
        setup:
        File file = new File("src/test/resources/WptResultXmls/" + fileName)
        WptResultXml xmlResult = new WptResultXml(new XmlSlurper().parse(file))
        String locationIdentifier = xmlResult.responseNode.data.location.toString()

        Job.build(label: jobLabel)
        WebPageTestServer wptServer = WebPageTestServer.build(baseUrl: "http://wpt.org")
        Location.build(uniqueIdentifierForServer: locationIdentifier, wptServer: wptServer)
        Page page = Page.build(name: pageName)

        when: "services listens to XML result of a single step test with 3 runs"
        service.listenToResult(xmlResult, wptServer)

        then: "it creates the right measured event and event results"
        List<MeasuredEvent> measuredEvents = MeasuredEvent.list()
        measuredEvents*.name == ['FF_BV1_Step01_Homepage - netlab']
        measuredEvents[0].testedPage == page

        List<EventResult> medianUncachedResults = EventResult.findAllByMedianValueAndCachedView(true, UNCACHED)
        medianUncachedResults.size() == 1
        medianUncachedResults[0].docCompleteTimeInMillisecs == 5873
        medianUncachedResults[0].docCompleteRequests == 157
        medianUncachedResults[0].wptStatus == 0
        medianUncachedResults[0].testDetailsWaterfallURL.toString() == 'http://wpt.org/details.php?test=121212_NH_6a2777a9c09ac89e108d1f2b94e74b83&run=2&cached=0#waterfall_viewFF_BV1_Step01_Homepage - netlab'

        List<EventResult> cachedRun3Results = EventResult.findAllByCachedViewAndNumberOfWptRun(CACHED, 3)
        cachedRun3Results.size() == 1
        cachedRun3Results[0].docCompleteTimeInMillisecs == 3977
        cachedRun3Results[0].docCompleteRequests == 36
        cachedRun3Results[0].wptStatus == 0
        cachedRun3Results[0].testDetailsWaterfallURL.toString() == 'http://wpt.org/details.php?test=121212_NH_6a2777a9c09ac89e108d1f2b94e74b83&run=3&cached=1#waterfall_viewFF_BV1_Step01_Homepage - netlab'

        where:
        fileName                                              | jobLabel                               | pageName
        'BEFORE_MULTISTEP_3Runs_CsiRelevant.xml'              | 'FF_BV1_Step01_Homepage - netlab'      | Page.UNDEFINED
        'BEFORE_MULTISTEP_3Runs_WithPageName_CsiRelevant.xml' | 'HP:::FF_BV1_Step01_Homepage - netlab' | 'HP'
    }

    void "result persistance with iteratec multistep fork"(String fileName, String productPageName, String searchPageName, String jobLabel) {
        setup:
        File file = new File("src/test/resources/WptResultXmls/" + fileName)
        WptResultXml xmlResult = new WptResultXml(new XmlSlurper().parse(file))
        String locationIdentifier = xmlResult.responseNode.data.location.toString()

        Job.build(label: jobLabel)
        WebPageTestServer wptServer = WebPageTestServer.build(baseUrl: "http://wpt.org")
        Location.build(uniqueIdentifierForServer: locationIdentifier, wptServer: wptServer)
        Page.build(name: "Produkt")
        Page.build(name: "Suche")

        when: "service listens to XML of a WPT multistep fork (2.18)"
        service.listenToResult(xmlResult, wptServer)

        then: "the correct measured events and event results are created; pages are associated if provided"

        MeasuredEvent.list()*.name as Set == ['Seite laden', 'Artikel suchen', 'Artikel-Detailseite laden',
                                              'Produkt auswaehlen', 'Produkt in Warenkorb legen',
                                              'Warenkorb oeffnen'] as Set

        MeasuredEvent measuredEventProduct = MeasuredEvent.findByName('Produkt auswaehlen')
        List<EventResult> productResults = EventResult.findAllByMeasuredEventAndMedianValueAndCachedView(
                measuredEventProduct, true, UNCACHED)
        productResults.size() == 1
        productResults[0].page.name == productPageName
        productResults[0].docCompleteTimeInMillisecs == 2218
        productResults[0].docCompleteRequests == 29
        productResults[0].wptStatus == 99999
        productResults[0].getTestDetailsWaterfallURL().toString() == "http://wpt.org/details.php?test=130425_W1_f606bebc977a3b22c1a9205f70d07a00&run=1&cached=0#waterfall_viewProdukt auswaehlen"


        MeasuredEvent measuredEventSearch = MeasuredEvent.findByName('Artikel suchen')
        List<EventResult> searchResults = EventResult.findAllByMeasuredEventAndMedianValueAndCachedViewAndNumberOfWptRun(
                measuredEventSearch, true, CACHED, 1)
        searchResults.size() == 1
        searchResults[0].page.name == searchPageName
        searchResults[0].docCompleteTimeInMillisecs == 931
        searchResults[0].docCompleteRequests == 6
        searchResults[0].wptStatus == 99999
        searchResults[0].getTestDetailsWaterfallURL().toString() == "http://wpt.org/details.php?test=130425_W1_f606bebc977a3b22c1a9205f70d07a00&run=1&cached=1#waterfall_viewArtikel suchen"

        where:
        fileName                                                     | productPageName | searchPageName | jobLabel
        "MULTISTEP_FORK_ITERATEC_3Runs_6EventNames.xml"              | Page.UNDEFINED  | Page.UNDEFINED | "3Runs_6Events"
        "MULTISTEP_FORK_ITERATEC_3Runs_6EventNames_WithPageName.xml" | "Produkt"       | "Suche"        | "example.de - Multiple steps with event names + dom elements"
    }

    void "new results have cs by visually complete"() {
        setup:
        File file = new File("src/test/resources/WptResultXmls/MULTISTEP_FORK_ITERATEC_5Runs_3Events_JustFirstView_WithVideo.xml")
        WptResultXml xmlResult = new WptResultXml(new XmlSlurper().parse(file))
        WebPageTestServer wptServer = WebPageTestServer.build()
        Location.build(uniqueIdentifierForServer: xmlResult.responseNode.data.location.toString(), wptServer: wptServer)
        Job.build(label: "FF_Otto_multistep")
        service.timeToCsMappingService = Stub(TimeToCsMappingService) {
            getCustomerSatisfactionInPercent(_) >> { value -> value }
        }

        when: "the service creates new event results from the XML result"
        service.listenToResult(xmlResult, wptServer)
        List<EventResult> eventResults = EventResult.list()

        then: "there are 15 EventResults with visually complete and cs values"
        eventResults.size() == 15
        !eventResults.any { it.visuallyCompleteInMillisecs == null }
        !eventResults.any { it.csByWptVisuallyCompleteInPercent == null }
    }

    void "new results have all visually complete percentages"() {
        setup:
        File file = new File("src/test/resources/WptResultXmls/DEV_EXAMPLE_JOB_FIREFOX_17JUL2017_1Run_3Events.xml")
        WptResultXml xmlResult = new WptResultXml(new XmlSlurper().parse(file))
        WebPageTestServer wptServer = WebPageTestServer.build()
        Location.build(uniqueIdentifierForServer: xmlResult.responseNode.data.location.toString(), wptServer: wptServer)
        Job.build(label: "ExampleJob Firefox")
        service.timeToCsMappingService = Stub(TimeToCsMappingService) {
            getCustomerSatisfactionInPercent(_) >> { value -> value }
        }

        when: "the service creates new event results from the XML result"
        service.listenToResult(xmlResult, wptServer)
        List<EventResult> eventResults = EventResult.list()

        then: "there are 6 EventResults with all kinds of visually complete values"
        eventResults.size() == 6
        !eventResults.any { it.visuallyCompleteInMillisecs == null }
        !eventResults.any { it.visuallyComplete85InMillisecs == null }
        !eventResults.any { it.visuallyComplete90InMillisecs == null }
        !eventResults.any { it.visuallyComplete95InMillisecs == null }
        !eventResults.any { it.visuallyComplete99InMillisecs == null }
    }

    void "new results have all time to interactive"() {
        setup:
        File file = new File("src/test/resources/WptResultXmls/WPT_EXAMPLE_CHROME_TTI_1Run_1Event.xml")
        WptResultXml xmlResult = new WptResultXml(new XmlSlurper().parse(file))
        WebPageTestServer wptServer = WebPageTestServer.build()
        Location.build(uniqueIdentifierForServer: xmlResult.responseNode.data.location.toString(), wptServer: wptServer)
        Job.build(label: "ExampleJob Chrome TTI")
        service.timeToCsMappingService = Stub(TimeToCsMappingService) {
            getCustomerSatisfactionInPercent(_) >> { value -> value }
        }

        when: "the service creates new event results from the XML result"
        service.listenToResult(xmlResult, wptServer)
        List<EventResult> eventResults = EventResult.list()

        then: "there is one EventResult with both time to interactive values"
        eventResults.size() == 1
        eventResults.get(0).firstInteractiveInMillisecs == 2286
        eventResults.get(0).consistentlyInteractiveInMillisecs == 2286
    }

    void "test waterfall URL creation for WPT >2.19 with multistep"(int run, String measuredEvent, CachedView cachedView, String expectedUrl) {
        setup:
        File file = new File("src/test/resources/WptResultXmls/MULTISTEP_2Run.xml")
        WptResultXml xmlResult = new WptResultXml(new XmlSlurper().parse(file))
        WebPageTestServer wptServer = WebPageTestServer.build(baseUrl: "http://wpt.org")
        Location.build(uniqueIdentifierForServer: xmlResult.responseNode.data.location.toString(), wptServer: wptServer)
        Job.build(label: "FF_Otto_multistep")

        when: "the service listens to results from a WPT server > 2.19 with multistep"
        service.listenToResult(xmlResult, wptServer)

        then: "all event results are created and the new URL format is used to create the waterfall URL"
        List<EventResult> eventResults = EventResult.list()
        eventResults.size() == 8
        !eventResults.any { it.getTestDetailsWaterfallURL() == null }

        EventResult result = eventResults.find {
            it.numberOfWptRun == run && it.measuredEvent.name == measuredEvent && it.cachedView == cachedView
        }
        result.getTestDetailsWaterfallURL().toString() == expectedUrl

        where:
        run | measuredEvent   | cachedView | expectedUrl
        1   | 'beforeTest'    | UNCACHED   | 'http://wpt.org/details.php?test=160727_EV_4&run=1&cached=0#waterfall_view_step1'
        1   | 'beforeTest'    | CACHED     | 'http://wpt.org/details.php?test=160727_EV_4&run=1&cached=1#waterfall_view_step1'
        1   | 'testExecution' | UNCACHED   | 'http://wpt.org/details.php?test=160727_EV_4&run=1&cached=0#waterfall_view_step2'
        1   | 'testExecution' | CACHED     | 'http://wpt.org/details.php?test=160727_EV_4&run=1&cached=1#waterfall_view_step2'
        2   | 'beforeTest'    | UNCACHED   | 'http://wpt.org/details.php?test=160727_EV_4&run=2&cached=0#waterfall_view_step1'
        2   | 'beforeTest'    | CACHED     | 'http://wpt.org/details.php?test=160727_EV_4&run=2&cached=1#waterfall_view_step1'
        2   | 'testExecution' | UNCACHED   | 'http://wpt.org/details.php?test=160727_EV_4&run=2&cached=0#waterfall_view_step2'
        2   | 'testExecution' | CACHED     | 'http://wpt.org/details.php?test=160727_EV_4&run=2&cached=1#waterfall_view_step2'
    }

    void "test handling of failure in multistep result"() {
        setup:
        File file = new File("src/test/resources/WptResultXmls/MULTISTEP_Error_Failure_In_Step.xml")
        WptResultXml xmlResult = new WptResultXml(new XmlSlurper().parse(file))
        Location.build(uniqueIdentifierForServer: xmlResult.responseNode.data.location.toString())
        Job.build(label: "testjob")

        when: "the service tries to persist results for an XML result with failed step"
        service.persistResultsForAllTeststeps(xmlResult)

        then: "it throws an exception and doesn't create any measured events"
        OsmResultPersistanceException exception = thrown()
        MeasuredEvent.list().size() == 0
    }
}

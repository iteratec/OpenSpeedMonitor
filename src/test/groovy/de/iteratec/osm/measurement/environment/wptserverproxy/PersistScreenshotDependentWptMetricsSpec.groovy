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

import de.iteratec.osm.csi.Page
import de.iteratec.osm.measurement.environment.Browser
import de.iteratec.osm.measurement.environment.BrowserAlias
import de.iteratec.osm.measurement.environment.Location
import de.iteratec.osm.measurement.environment.WebPageTestServer
import de.iteratec.osm.measurement.schedule.Job
import de.iteratec.osm.measurement.schedule.JobDaoService
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.measurement.script.Script
import de.iteratec.osm.report.external.MetricReportingService
import de.iteratec.osm.result.EventResult
import de.iteratec.osm.result.JobResult
import de.iteratec.osm.result.MeasuredEvent
import de.iteratec.osm.result.PageService
import de.iteratec.osm.util.PerformanceLoggingService
import grails.buildtestdata.mixin.Build
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import spock.lang.Specification
import spock.lang.Unroll

import static de.iteratec.osm.result.CachedView.CACHED
import static de.iteratec.osm.result.CachedView.UNCACHED

@TestMixin(GrailsUnitTestMixin)
@TestFor(ResultPersisterService)
@Mock([WebPageTestServer, Browser, Location, Job, JobResult, EventResult, BrowserAlias, Page, MeasuredEvent, JobGroup, Script])
@Build([Job, MeasuredEvent, Location, WebPageTestServer, Page])
@Unroll
class PersistScreenshotDependentWptMetricsSpec extends Specification{

    static WebPageTestServer WPT_SERVER

    public static final String NAME_EVENT_1 = 'otto_homepage'
    public static final String NAME_EVENT_2 = 'otto_search_shoes'
    public static final String NAME_EVENT_3 = 'otto_product_boots'

    def doWithSpring = {
        pageService(PageService)
        performanceLoggingService(PerformanceLoggingService)
        jobDaoService(JobDaoService)
    }
    void setup() {
        createTestDataCommonForAllTests()
        createMocksCommonForAllTests()
    }

    void "Screenshot dependent measurands get persisted for step #eventName of Multistep1Run3EventsFvOnlyWithVideo correctly."() {

        given: "Some test data and results xml file in place."
        File xmlResultFile = new File("src/test/resources/WptResultXmls/MULTISTEP_FORK_ITERATEC_1Run_3Events_JustFirstView_WithVideo.xml")
        WptResultXml xmlResult = new WptResultXml (new XmlSlurper().parse(xmlResultFile))
        MeasuredEvent.build(name: NAME_EVENT_1)
        MeasuredEvent.build(name: NAME_EVENT_2)
        MeasuredEvent.build(name: NAME_EVENT_3)

        when: "ResultPersisterService listens to result."
        service.listenToResult(xmlResult, WPT_SERVER)

        then: "Screenshot dependent measurands get persisted correctly."
        List<EventResult> allResults = EventResult.getAll()
        allResults.size() == 3

        EventResult resultOfCurrentStep = allResults.find {it.measuredEvent.name == eventName}
        resultOfCurrentStep.visuallyCompleteInMillisecs == expVisualComplete
        resultOfCurrentStep.speedIndex == expSpeedIndex

        where:
        eventName       || expVisualComplete    | expSpeedIndex
        NAME_EVENT_1    || 4500                 | 1647
        NAME_EVENT_2    || 1300                 | 838
        NAME_EVENT_3    || 1100                 | 847

    }
    void "Screenshot dependent measurands get not persisted for Multistep1Run3EventsFvOnlyWithoutVideo because capture video is disabled."() {

        given: "Some test data and results xml file in place."
        File xmlResultFile = new File("src/test/resources/WptResultXmls/MULTISTEP_FORK_ITERATEC_1Run_3Events_JustFirstView_WithoutVideo.xml")
        WptResultXml xmlResult = new WptResultXml (new XmlSlurper().parse(xmlResultFile))
        MeasuredEvent.build(name: NAME_EVENT_1)
        MeasuredEvent.build(name: NAME_EVENT_2)
        MeasuredEvent.build(name: NAME_EVENT_3)

        when: "ResultPersisterService listens to result."
        service.listenToResult(xmlResult, WPT_SERVER)

        then: "Screenshot dependent measurands are missing on written EventResults."
        List<EventResult> allResults = EventResult.getAll()
        allResults.size() == 3

        allResults.every{ it.visuallyCompleteInMillisecs == null }
        allResults.every{ it.speedIndex == EventResult.SPEED_INDEX_DEFAULT_VALUE }

    }
    void "Screenshot dependent measurands get persisted for run #run, step #eventName of Multistep5Run3EventsFvOnlyWithVideo."() {

        given: "Some test data and results xml file in place."
        File xmlResultFile = new File("src/test/resources/WptResultXmls/MULTISTEP_FORK_ITERATEC_5Runs_3Events_JustFirstView_WithVideo.xml")
        WptResultXml xmlResult = new WptResultXml (new XmlSlurper().parse(xmlResultFile))
        MeasuredEvent.build(name: NAME_EVENT_1)
        MeasuredEvent.build(name: NAME_EVENT_2)
        MeasuredEvent.build(name: NAME_EVENT_3)

        when: "ResultPersisterService listens to result."
        service.listenToResult(xmlResult, WPT_SERVER)

        then: "Screenshot dependent measurands get persisted correctly for each step in each run."
        int numberOfRuns = 5
        int numberOfSteps = 3
        List<EventResult> allResults = EventResult.getAll()
        allResults.size() == numberOfRuns * numberOfSteps

        EventResult resultOfCurrentRunAndStep = allResults.find {
            it.numberOfWptRun == run && it.measuredEvent.name == eventName
        }
        resultOfCurrentRunAndStep.visuallyCompleteInMillisecs == expVisualComplete
        resultOfCurrentRunAndStep.speedIndex == expSpeedIndex

        where:
        run | eventName     || expVisualComplete    | expSpeedIndex
        1   | NAME_EVENT_1  || 3000                 | 2241
        1   | NAME_EVENT_2  || 2000                 | 574
        1   | NAME_EVENT_3  || 3400                 | 745
        2   | NAME_EVENT_1  || 2900                 | 2318
        2   | NAME_EVENT_2  || 2000                 | 620
        2   | NAME_EVENT_3  || 1900                 | 770
        3   | NAME_EVENT_1  || 3000                 | 2155
        3   | NAME_EVENT_2  || 2000                 | 621
        3   | NAME_EVENT_3  || 1800                 | 691
        4   | NAME_EVENT_1  || 2900                 | 2177
        4   | NAME_EVENT_2  || 2000                 | 542
        4   | NAME_EVENT_3  || 1900                 | 765
        5   | NAME_EVENT_1  || 2800                 | 2253
        5   | NAME_EVENT_2  || 2300                 | 644
        5   | NAME_EVENT_3  || 2300                 | 874


    }

    void "Screenshot dependent measurands get persisted correctly for cachedView #cachedView of Singlestep1RunWithVideo."() {

        given: "Some test data and results xml file in place."
        File xmlResultFile = new File("src/test/resources/WptResultXmls/BEFORE_MULTISTEP_1Run_WithVideo.xml")
        WptResultXml xmlResult = new WptResultXml (new XmlSlurper().parse(xmlResultFile))
        MeasuredEvent.build(name: 'IE_otto_hp_singlestep')

        when: "ResultPersisterService listens to result."
        service.listenToResult(xmlResult, WPT_SERVER)

        then: "Screenshot dependent measurands get persisted correctly for first and repeated view."
        List<EventResult> allResults = EventResult.list()
        allResults.size() == 2
        EventResult result = allResults.find {it.cachedView == cachedView}
        result.visuallyCompleteInMillisecs == expVisualCompl
        result.speedIndex == expSpeedIndex

        where:
        cachedView  || expVisualCompl   | expSpeedIndex
        UNCACHED    || 4500             | 2430
        CACHED      || 2200             | 1693

    }
    void "Screenshot dependent measurands get persisted correctly for run #run, cachedView #cachedView of Singlestep5RunsWithVideo"() {

        given: "Some test data and results xml file in place."
        File xmlResultFile = new File("src/test/resources/WptResultXmls/BEFORE_MULTISTEP_5Runs_WithVideo.xml")
        WptResultXml xmlResult = new WptResultXml (new XmlSlurper().parse(xmlResultFile))
        MeasuredEvent.build(name: 'IE_otto_hp_singlestep')

        when: "ResultPersisterService listens to result."
        service.listenToResult(xmlResult, WPT_SERVER)

        then: "Screenshot dependent measurands get persisted correctly for first and repeated view of all runs."
        List<EventResult> allResults = EventResult.getAll()
        allResults.size() == 10

        EventResult result = allResults.find {it.numberOfWptRun == run && it.cachedView == cachedView}
        result.visuallyCompleteInMillisecs == expVisualComplete
        result.speedIndex == expSpeedIndex

        where:
        run | cachedView    || expVisualComplete    | expSpeedIndex
        1   | UNCACHED      || 11700                | 5001
        2   | UNCACHED      || 12000                | 7000
        3   | UNCACHED      || 12000                | 6882
        4   | UNCACHED      || 12000                | 7025
        5   | UNCACHED      || 12000                | 7017
        1   | CACHED        || 3800                 | 1970
        2   | CACHED        || 10500                | 5510
        3   | CACHED        || 10000                | 2780
        4   | CACHED        || 10700                | 5665
        5   | CACHED        || 9700                 | 1732

    }

    void createTestDataCommonForAllTests() {
        WPT_SERVER = WebPageTestServer.build(proxyIdentifier: 'dev.server02.wpt.iteratec.de')
        Location.build(uniqueIdentifierForServer: 'iteratec-dev-hetzner-win7:Firefox', wptServer: WPT_SERVER)
        Location.build(uniqueIdentifierForServer: 'NewYork:IE 11', wptServer: WPT_SERVER)
        Location.build(uniqueIdentifierForServer: 'iteratec-dev-hetzner-64bit-ssd:Firefox', wptServer: WPT_SERVER)
        Location.build(uniqueIdentifierForServer: 'iteratec-dev-netlab-win7:IE', wptServer: WPT_SERVER)
        Job.build(label: 'FF_Otto_multistep')
        Job.build(label: 'IE_otto_hp_singlestep')
    }

    void createMocksCommonForAllTests() {
        service.proxyService = Mock(ProxyService)
        service.metricReportingService = Mock(MetricReportingService)
    }

}

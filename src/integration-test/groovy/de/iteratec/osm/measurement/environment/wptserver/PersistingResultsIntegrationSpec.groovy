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

package de.iteratec.osm.measurement.environment.wptserver

import de.iteratec.osm.ConfigService
import de.iteratec.osm.OsmConfiguration
import de.iteratec.osm.csi.CsiAggregationUpdateService
import de.iteratec.osm.csi.NonTransactionalIntegrationSpec
import de.iteratec.osm.csi.Page
import de.iteratec.osm.csi.transformation.TimeToCsMappingService
import de.iteratec.osm.measurement.environment.Browser
import de.iteratec.osm.measurement.environment.Location
import de.iteratec.osm.measurement.environment.WebPageTestServer
import de.iteratec.osm.measurement.schedule.Job
import de.iteratec.osm.measurement.script.Script
import de.iteratec.osm.report.external.MetricReportingService
import de.iteratec.osm.result.CachedView
import de.iteratec.osm.result.EventResult
import de.iteratec.osm.result.JobResult
import de.iteratec.osm.result.JobResultStatus
import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration

import static de.iteratec.osm.OsmConfiguration.getDEFAULT_MAX_VALID_LOADTIME
import static de.iteratec.osm.OsmConfiguration.getDEFAULT_MIN_VALID_LOADTIME

@Integration(applicationClass = openspeedmonitor.Application.class)
@Rollback
class PersistingResultsIntegrationSpec extends NonTransactionalIntegrationSpec {

    JobResultPersisterService jobResultPersisterService

    private static final String LOCATION_IDENTIFIER = 'Agent1-wptdriver:Firefox'
    WebPageTestServer server
    Job job
    Script script
    String navigationScript = "setEventName\tHP:::LH_Homepage_2\n" +
            "navigate\thttps://www.example.de\n" +
            "setEventName\tMES:::LH_Moduleinstieg_2\n" +
            "navigate\thttps://www.example.de/subsite/"

    def setupData() {
        OsmConfiguration.build()
        createTestDataCommonToAllTests()
        createMocksCommonToAllTests()
    }

    def cleanup() {
        (jobResultPersisterService.resultListeners[0] as EventResultPersisterService).timeToCsMappingService =
                grailsApplication.mainContext.getBean('timeToCsMappingService')
        (jobResultPersisterService.resultListeners[0] as EventResultPersisterService).csiAggregationUpdateService =
                grailsApplication.mainContext.getBean('csiAggregationUpdateService')
        (jobResultPersisterService.resultListeners[0] as EventResultPersisterService).metricReportingService =
                grailsApplication.mainContext.getBean('metricReportingService')
        (jobResultPersisterService.resultListeners[0] as EventResultPersisterService).configService =
                grailsApplication.mainContext.getBean('configService')
    }

    void "Only first step of cached view gets persisted if TTFB of the first uncached step is invalid"() {

        given: "a wpt result and a faulty result (TTFB is 0) in firstView"
        setupData()
        WptResultXml xmlResult = new WptResultXml(new XmlSlurper().parse(new File("src/test/resources/WptResultXmls/MULTISTEP_FORK_ITERATEC_1Run_2EventNames_FaultyTTFB_PagePrefix.xml")))

        when: "the results get persisted"
        jobResultPersisterService.persistUnfinishedJobResult(job, xmlResult.testId, JobResultStatus.RUNNING)
        jobResultPersisterService.handleWptResult(xmlResult, xmlResult.testId, job)

        then: "1 run, 1 successful events, but first result is faulty"
        JobResult.list().size() == 1
        EventResult.list().size() == 1
        EventResult.list()[0].cachedView == CachedView.CACHED
    }

    void "Only first step of cached view gets persisted if LoadTime  of the first uncached step is larger than max"() {
        given: "a wpt result and a faulty result (LoadTime is larger than the allowed max value)"
        setupData()
        WptResultXml xmlResult = new WptResultXml(new XmlSlurper().parse(new File("src/test/resources/WptResultXmls/MULTISTEP_FORK_ITERATEC_1Run_2EventNames_FaultyLoadTime_PagePrefix.xml")))

        when: "the results get persisted"
        jobResultPersisterService.persistUnfinishedJobResult(job, xmlResult.testId, JobResultStatus.RUNNING)
        jobResultPersisterService.handleWptResult(xmlResult, xmlResult.testId, job)

        then: "1 run, 1 successful events, but first result is faulty"
        JobResult.list().size() == 1
        EventResult.list().size() == 1
        EventResult.list()[0].cachedView == CachedView.CACHED
    }

    void "Only first step of cached view gets persisted if result of the first uncached step was not successfully"() {
        given: "a wpt result and a faulty result (Result Code is 404)"
        setupData()
        WptResultXml xmlResult = new WptResultXml(new XmlSlurper().parse(new File("src/test/resources/WptResultXmls/MULTISTEP_FORK_ITERATEC_1Run_2EventNames_FaultyResultCode_PagePrefix.xml")))

        when: "the results get persisted"
        jobResultPersisterService.persistUnfinishedJobResult(job, xmlResult.testId, JobResultStatus.RUNNING)
        jobResultPersisterService.handleWptResult(xmlResult, xmlResult.testId, job)

        then: "1 run, 1 successful events, but first result is faulty"
        JobResult.list().size() == 1
        EventResult.list().size() == 1
        EventResult.list()[0].cachedView == CachedView.CACHED
    }

    void "Results get persisted even after failed csi aggregation."() {

        given: "a wpt result and a failing CsiAggregationUpdateService"
        setupData()
        WptResultXml xmlResult = new WptResultXml(new XmlSlurper().parse(new File("src/test/resources/WptResultXmls/MULTISTEP_FORK_ITERATEC_1Run_2EventNames_PagePrefix.xml")))
        mockCsiAggregationUpdateService()

        when: "the results get persisted"
        jobResultPersisterService.persistUnfinishedJobResult(job, xmlResult.testId, JobResultStatus.RUNNING)
        jobResultPersisterService.handleWptResult(xmlResult, xmlResult.testId, job)

        then: "1 run, 2 successful events + 2 cached views should be persisted"
        JobResult.list().size() == 1
        EventResult.list().size() == 4
    }

    void "Results get persisted even after failed metric reporting."() {

        given: "a wpt result and a failing MetricReportingService"
        setupData()
        WptResultXml xmlResult = new WptResultXml(new XmlSlurper().parse(new File("src/test/resources/WptResultXmls/MULTISTEP_FORK_ITERATEC_1Run_2EventNames_PagePrefix.xml")))
        mockMetricReportingService()

        when: "the results get persisted"
        jobResultPersisterService.persistUnfinishedJobResult(job, xmlResult.testId, JobResultStatus.RUNNING)
        jobResultPersisterService.handleWptResult(xmlResult, xmlResult.testId, job)

        then: "1 run, 2 successful events + 2 cached views should be persisted"
        JobResult.list().size() == 1
        EventResult.list().size() == 4
    }

    void "No EventResults get persisted when Persistence of JobResults throws an exception."() {

        given: "a wpt result without a test id"
        setupData()
        WptResultXml xmlResult = Spy(WptResultXml, constructorArgs: [new XmlSlurper().parse(new File("src/test/resources/WptResultXmls/MULTISTEP_FORK_ITERATEC_1Run_2EventNames_PagePrefix.xml"))])
        xmlResult.getTestId() >> null

        when: "the service tries to persist the results an exception gets thrown"
        jobResultPersisterService.persistUnfinishedJobResult(job, xmlResult.testId, JobResultStatus.RUNNING)
        jobResultPersisterService.handleWptResult(xmlResult, xmlResult.testId, job)

        then: "nothing should be persisted"
        thrown OsmResultPersistanceException
        EventResult.list().size() == 0
    }

    void "If saving of EventResults of one step throws an Exception EventResults of other steps will be saved even though."() {

        given: "a wpt result, a failing MetricReportingService and a failing CsiAggregationUpdateService"
        setupData()
        WptResultXml xmlResult = Spy(WptResultXml, constructorArgs: [new XmlSlurper().parse(new File("src/test/resources/WptResultXmls/MULTISTEP_FORK_ITERATEC_1Run_2EventNames_PagePrefix.xml"))])
        xmlResult.getEventName(_, 0) >> { job, step ->
            throw new RuntimeException()
        }

        when: "the results get persisted but the first step throws an exception"
        jobResultPersisterService.persistUnfinishedJobResult(job, xmlResult.testId, JobResultStatus.RUNNING)
        jobResultPersisterService.handleWptResult(xmlResult, xmlResult.testId, job)

        then: "1 run, 1 successful events + 1 cached views should be persisted"
        JobResult.list().size() == 1
        EventResult.list().size() == 2
    }

    private createTestDataCommonToAllTests() {
        ['HP', 'MES', 'ADS', 'SE', Page.UNDEFINED].each { pageName ->
            Page.build(name: pageName)
        }
        server = WebPageTestServer.build(baseUrl: "http://osm.intgerationtest.org")
        Browser browser = Browser.build()
        Location loc = Location.build(
                wptServer: server,
                uniqueIdentifierForServer: LOCATION_IDENTIFIER,
                browser: browser
        )
        script = Script.build(navigationScript: navigationScript).save(flush: true)
        job = Job.build(
                label: 'FF_BV1_Multistep_2',
                location: loc,
                script: script,
                runs: 1,
                firstViewOnly: false
        )

        Job.build(
                label: '1Run_11events_4Faulty',
                location: loc
        )
    }

    void createMocksCommonToAllTests() {
        TimeToCsMappingService timeToCsMappingService = Stub(TimeToCsMappingService)
        timeToCsMappingService.getCustomerSatisfactionInPercent(_, _, _) >> 42
        (jobResultPersisterService.resultListeners[0] as EventResultPersisterService).timeToCsMappingService = timeToCsMappingService

        (jobResultPersisterService.resultListeners[0] as EventResultPersisterService).configService = Stub(ConfigService) {
            getMaxValidLoadtime() >> DEFAULT_MAX_VALID_LOADTIME
            getMinValidLoadtime() >> DEFAULT_MIN_VALID_LOADTIME
        }
    }

    void mockCsiAggregationUpdateService() {
        CsiAggregationUpdateService csiAggregationUpdateService = Stub(CsiAggregationUpdateService)
        csiAggregationUpdateService.createOrUpdateDependentMvs(_) >> {
             throw new RuntimeException('Faked failing of csi aggregation in integration test')
        }
        (jobResultPersisterService.resultListeners[0] as EventResultPersisterService).csiAggregationUpdateService = csiAggregationUpdateService
    }

    void mockMetricReportingService() {
        MetricReportingService metricReportingService = Stub(MetricReportingService)
        metricReportingService.reportEventResultToGraphite(_) >> {
            throw new RuntimeException('Faked failing of metric reporting in integration test')
        }
        (jobResultPersisterService.resultListeners[0] as EventResultPersisterService).metricReportingService = metricReportingService
    }
}

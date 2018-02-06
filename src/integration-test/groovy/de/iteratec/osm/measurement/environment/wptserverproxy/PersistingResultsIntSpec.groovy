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

import de.iteratec.osm.OsmConfiguration
import de.iteratec.osm.csi.*
import de.iteratec.osm.csi.transformation.TimeToCsMappingService
import de.iteratec.osm.measurement.environment.Location
import de.iteratec.osm.measurement.environment.WebPageTestServer
import de.iteratec.osm.measurement.schedule.Job
import de.iteratec.osm.report.external.MetricReportingService
import de.iteratec.osm.result.EventResult
import de.iteratec.osm.result.JobResult
import grails.test.mixin.integration.Integration
import grails.transaction.Rollback


@Integration
@Rollback
class PersistingResultsIntSpec extends NonTransactionalIntegrationSpec {

    ResultPersisterService resultPersisterService

    private static final String LOCATION_IDENTIFIER = 'Agent1-wptdriver:Firefox'
    private static Closure originalPersistJobResultsMethod
    private static Closure originalPersistEventResultsMethod
    WebPageTestServer server

    def setup() {


        originalPersistJobResultsMethod = resultPersisterService.&persistJobResult
        originalPersistEventResultsMethod = resultPersisterService.&persistResultsOfOneTeststep
        WebPageTestServer.withNewTransaction {
            OsmConfiguration.build()
            createTestDataCommonToAllTests()
        }
        mocksCommonToAllTests()

    }

    void "Results get persisted even after failed csi aggregation."() {

        given: "a wpt result and a failing CsiAggregationUpdateService"
        WptResultXml xmlResult = new WptResultXml(new XmlSlurper().parse(new File("src/test/resources/WptResultXmls/MULTISTEP_FORK_ITERATEC_1Run_2EventNames_PagePrefix.xml")))
        mockCsiAggregationUpdateService(true)

        when: "the results get persisted"
        resultPersisterService.listenToResult(xmlResult, server)

        then: "1 run, 2 successful events + 2 cached views should be persisted"
        JobResult.list().size() == 1
        EventResult.list().size() == 4

    }

    void "Results get persisted even after failed metric reporting."() {

        given: "a wpt result and a failing MetricReportingService"
        WptResultXml xmlResult = new WptResultXml(new XmlSlurper().parse(new File("src/test/resources/WptResultXmls/MULTISTEP_FORK_ITERATEC_1Run_2EventNames_PagePrefix.xml")))
        mockMetricReportingService(true)

        when: "the results get persisted"
        resultPersisterService.listenToResult(xmlResult, server)

        then: "1 run, 2 successful events + 2 cached views should be persisted"
        JobResult.list().size() == 1
        EventResult.list().size() == 4

    }

    void "No EventResults get persisted when Persistence of JobResults throws an exception."() {

        given: "a wpt result without a test id"
        WptResultXml xmlResult = Spy(WptResultXml, constructorArgs: [new XmlSlurper().parse(new File("src/test/resources/WptResultXmls/MULTISTEP_FORK_ITERATEC_1Run_2EventNames_PagePrefix.xml"))])
        xmlResult.getTestId() >> null

        when: "the service tries to persist the results an exception gets thrown"
        resultPersisterService.listenToResult(xmlResult, server)

        then: "nothing should be persisted"
        JobResult.list().size() == 0
        EventResult.list().size() == 0

    }

//    void "If saving of EventResults of one step throws an Exception EventResults of other steps will be saved even though."() {
//
//        given: "a wpt result, a failing MetricReportingService and a failing CsiAggregationUpdateService"
//        WptResultXml xmlResult = new WptResultXml(new XmlSlurper().parse(new File("src/test/resources/WptResultXmls/MULTISTEP_FORK_ITERATEC_1Run_2EventNames_PagePrefix.xml")))
//
//        when: "the results get persisted but the first step throws an exception"
//        letPersistingEventResultsOfSpecificStepThrowAnException(0)
//        resultPersisterService.listenToResult(xmlResult, server)
//
//        then: "1 run, 1 successful events + 1 cached views should be persisted"
//        JobResult.list().size() == 1
//        EventResult.list().size() == 2
//
//    }

    // create testdata common to all tests /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * All test data created here has to be deleted in cleanup method after every test!!!
     * That's because these integration tests have to run without an own transaction which would be
     * rolled back in the end of every test.
     *
     * Integration tests that test code with own separate transactions wouldn't see test data if creation in test would
     * happen in an own transaction.
     */
    private createTestDataCommonToAllTests() {
        ['HP', 'MES', Page.UNDEFINED].each { pageName ->
            Page.build(name: pageName)
        }
        server = WebPageTestServer.build(baseUrl: "http://osm.intgerationtest.org")
        Location loc = Location.build(
                wptServer: server,
                uniqueIdentifierForServer: LOCATION_IDENTIFIER,
        )
        Job.build(
                label: 'FF_BV1_Multistep_2',
                location: loc
        )
    }

    void mocksCommonToAllTests() {
        mockTimeToCsMappingService()
    }

    // mocks common to all tests /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    void mockTimeToCsMappingService() {
        TimeToCsMappingService timeToCsMappingService = Stub(TimeToCsMappingService)
        timeToCsMappingService.getCustomerSatisfactionInPercent(_, _, _) >> 42

        resultPersisterService.timeToCsMappingService = timeToCsMappingService
    }

    void mockCsiAggregationUpdateService(boolean shouldFail) {
        CsiAggregationUpdateService csiAggregationUpdateService = Stub(CsiAggregationUpdateService)
        csiAggregationUpdateService.createOrUpdateDependentMvs(_) >> {
            if (shouldFail) throw new RuntimeException('Faked failing of csi aggregation in integration test')
        }

        resultPersisterService.csiAggregationUpdateService = csiAggregationUpdateService
    }

    void mockMetricReportingService(boolean shouldFail) {
        MetricReportingService metricReportingService = Stub(MetricReportingService)
        metricReportingService.reportEventResultToGraphite(_) >> {
            if (shouldFail) throw new RuntimeException('Faked failing of metric reporting in integration test')
        }

        resultPersisterService.metricReportingService = metricReportingService
    }

    void letPersistingEventResultsOfSpecificStepThrowAnException(int stepNumber) {
        ResultPersisterService stubbedResultPersisterService = Stub(ResultPersisterService)
        stubbedResultPersisterService.persistResultsOfOneTeststep(_, _) >> { Integer testStepZeroBasedIndex, WptResultXml resultXml ->
            if (testStepZeroBasedIndex == stepNumber) {
                throw new OsmResultPersistanceException('Faked failing of EventResult persistance in integration test')
            } else {
                originalPersistEventResultsMethod(testStepZeroBasedIndex, resultXml)
            }
        }

        resultPersisterService = stubbedResultPersisterService
    }
}

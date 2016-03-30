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
import de.iteratec.osm.csi.Page
import de.iteratec.osm.csi.TestDataUtil
import de.iteratec.osm.measurement.environment.Browser
import de.iteratec.osm.measurement.environment.Location
import de.iteratec.osm.measurement.environment.WebPageTestServer
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.measurement.script.Script
import de.iteratec.osm.result.EventResult
import de.iteratec.osm.result.JobResult
import grails.test.spock.IntegrationSpec
import groovy.util.slurpersupport.GPathResult
/**
 *
 */
class PersistingResultsIntSpec extends IntegrationSpec {

    static transactional = false //necessary because we test transactional service methods

    LocationAndResultPersisterService locationAndResultPersisterService
	
	private static final String LOCATION_IDENTIFIER  = 'Agent1-wptdriver:Firefox'
    private static Closure originalPersistJobResultsMethod
    private static Closure originalPersistEventResultsMethod
	WebPageTestServer server1

    def setup() {

        originalPersistJobResultsMethod = locationAndResultPersisterService.&persistJobResult
        originalPersistEventResultsMethod = locationAndResultPersisterService.&persistResultsOfOneTeststep

        createTestDataCommonToAllTests()

        mocksCommonToAllTests()

    }

    def cleanup(){

        TestDataUtil.cleanUpDatabase() //necessary because our tests have to be non-transactional here

        resetLarpServiceMetaclass()

    }

	void "Results get persisted even after failed csi aggregation."() {

        setup:
		//create test-specific data
		GPathResult xmlResult = new XmlSlurper().parse(new File("test/resources/WptResultXmls/Result_Multistep_1Run_2EventNames_PagePrefix.xml"))
		//test specific mocks
		mockCsiAggregationUpdateService(true)
        mockMetricReportingService(false)
        //expected values
        int runs = 1
        int events = 2
        int cachedViews = 2
        int expectedNumberOfResults = runs * events * cachedViews

        when:
		locationAndResultPersisterService.listenToResult(xmlResult, server1)

		then:
        JobResult.list().size() == 1
		EventResult.list().size == expectedNumberOfResults

	}
    void "Results get persisted even after failed metric reporting."() {

        setup:
        //create test-specific data
        GPathResult xmlResult = new XmlSlurper().parse(new File("test/resources/WptResultXmls/Result_Multistep_1Run_2EventNames_PagePrefix.xml"))
        //test specific mocks
        mockCsiAggregationUpdateService(false)
        mockMetricReportingService(true)
        //expected values
        int runs = 1
        int events = 2
        int cachedViews = 2
        int expectedNumberOfResults = runs * events * cachedViews

        when:
        locationAndResultPersisterService.listenToResult(xmlResult, server1)

        then:
        JobResult.list().size() == 1
        EventResult.list().size == expectedNumberOfResults

    }
    void "No EventResults get persisted when Persistence of JobResults  throws an Exception."() {

        setup:
        //create test-specific data
        GPathResult xmlResult = new XmlSlurper().parse(new File("test/resources/WptResultXmls/Result_Multistep_1Run_2EventNames_PagePrefix.xml"))
        //test specific mocks
        mockCsiAggregationUpdateService(false)
        mockMetricReportingService(false)
        letPersistingJobResultThrowAnException(true)
        //expected values
        int expectedNumberOfResults = 0

        when:
        locationAndResultPersisterService.listenToResult(xmlResult, server1)

        then:
        JobResult.list().size() == expectedNumberOfResults
        EventResult.list().size == expectedNumberOfResults

    }
    void "If saving of EventResults of one step throws an Exception EventResults of other steps will be saved even though."() {

        setup:
        //create test-specific data
        GPathResult xmlResult = new XmlSlurper().parse(new File("test/resources/WptResultXmls/Result_Multistep_1Run_2EventNames_PagePrefix.xml"))
        //test specific mocks
        mockCsiAggregationUpdateService(false)
        mockMetricReportingService(false)
        letPersistingEventResultsOfSpecificStepThrowAnException(0)
        //expected values
        int runs = 1
        int events = 2
        int failedEvents = 1
        int cachedViews = 2
        int expectedNumberOfJobResults = 1
        int expectedNumberOfEventResults = runs * (events-failedEvents) * cachedViews

        when:
        locationAndResultPersisterService.listenToResult(xmlResult, server1)

        then:
        JobResult.list().size() == expectedNumberOfJobResults
        EventResult.list().size == expectedNumberOfEventResults

    }
	
	// create testdata common to all tests /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * All test data created here has to be deleted in cleanup method after every test!!!
     * That's because these integration tests have to run without an own transaction which would be
     * rolled back in the end of every test.
     *
     * Integration tests that test code with own separate transactions wouldn't see test data if creation in test would
     * happen in an own transaction.
     */
    private createTestDataCommonToAllTests(){

        TestDataUtil.createPages(['HP', 'MES', Page.UNDEFINED])
        Browser undefBrowser = TestDataUtil.createBrowser(Browser.UNDEFINED, 1)
        JobGroup jobGroup = TestDataUtil.createJobGroup(JobGroup.UNDEFINED_CSI)
        server1 = TestDataUtil.createWebPageTestServer(
                "TestServer 1",
                "TestServer1",
                true,
                "http://osm.intgerationtest.org"
        )
        Location loc = TestDataUtil.createLocation(
                server1,
                LOCATION_IDENTIFIER,
                undefBrowser,
                true
        )
        Script script = TestDataUtil.createScript('script', 'description', 'navigate tralala', false)
        TestDataUtil.createJob(
                'FF_BV1_Multistep_2',
                script,
                loc,
                jobGroup,
                'job description',
                1,
                true,
                60
        )
        TestDataUtil.createOsmConfig()

    }

    void mocksCommonToAllTests(){
        mockTimeToCsMappingService()
    }

	// mocks common to all tests /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	void mockTimeToCsMappingService(){
		locationAndResultPersisterService.timeToCsMappingService.metaClass.getCustomerSatisfactionInPercent = {
            Integer docReadyTimeInMilliSecs, Page page, CsiConfiguration csiConfiguration = null ->
			return 42 //not the concern of this tests
		}
	}
	void mockCsiAggregationUpdateService(boolean shouldFail){
		locationAndResultPersisterService.csiAggregationUpdateService.metaClass.createOrUpdateDependentMvs = {EventResult result ->
			if(shouldFail) throw new RuntimeException('Faked failing of csi aggregation in integration test')
		}	
	}
	void mockMetricReportingService(boolean shouldFail){
		locationAndResultPersisterService.metricReportingService.metaClass.reportEventResultToGraphite = {EventResult result ->
            if(shouldFail) throw new RuntimeException('Faked failing of metric reporting in integration test')
		}
	}
    void letPersistingJobResultThrowAnException(boolean throwException){
        locationAndResultPersisterService.metaClass.persistJobResult = {WptResultXml resultXml ->
            if(throwException) throw new OsmResultPersistanceException('Faked failing of JobResult persistance in integration test')
        }
    }
    void letPersistingEventResultsOfSpecificStepThrowAnException(int stepNumber){
        locationAndResultPersisterService.metaClass.persistResultsOfOneTeststep = {Integer testStepZeroBasedIndex, WptResultXml resultXml ->
            if (testStepZeroBasedIndex == stepNumber){
                throw new OsmResultPersistanceException('Faked failing of EventResult persistance in integration test')
            }else{
                originalPersistEventResultsMethod(testStepZeroBasedIndex, resultXml)
            }
        }
    }
    void resetLarpServiceMetaclass(){
        locationAndResultPersisterService.metaClass.persistJobResult = originalPersistJobResultsMethod
        locationAndResultPersisterService.metaClass.persistResultsOfOneTeststep = originalPersistEventResultsMethod
    }
}

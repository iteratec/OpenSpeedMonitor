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

import de.iteratec.osm.csi.transformation.TimeToCsMappingService
import de.iteratec.osm.measurement.environment.Browser
import de.iteratec.osm.measurement.environment.Location
import de.iteratec.osm.measurement.environment.WebPageTestServer
import de.iteratec.osm.measurement.environment.wptserverproxy.ResultPersisterService
import de.iteratec.osm.measurement.environment.wptserverproxy.WptResultXml
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.measurement.script.Script
import de.iteratec.osm.result.EventResult
import grails.test.mixin.integration.Integration
import grails.transaction.Rollback
import groovy.mock.interceptor.StubFor
import groovy.util.slurpersupport.GPathResult

/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
@Integration
@Rollback
class CsiCalculationSpec extends NonTransactionalIntegrationSpec {
    ResultPersisterService resultPersisterService

    static final String jobGroupName_csi_1 = "jobGroup1"
    static final String jobGroupName_csi_05 = "jobGroup2"
    static final List<Page> allPages = ['HP', 'MES', 'PL', 'SE', 'HP_entry', 'ADS', 'WK', Page.UNDEFINED]

    WptResultXml xmlResult
    CsiConfiguration csiConfiguration_all_1
    CsiConfiguration csiConfiguration_all_05

    WebPageTestServer server1
    Location testLocation
    Script testScript

    def setup() {
        createTestDataCommonForAllTests()
        mocksCommonForAllTests()
    }


    void "csi won't be calculated without csi-configuration"() {
        setup: "prepare Job and JobGroup"
        JobGroup.withNewTransaction {
            JobGroup jobGroupWithoutCsiConf = JobGroup.findByName(jobGroupName_csi_1)
            TestDataUtil.
                    createJob('FF_LH_BV1_hetzner', testScript, testLocation, jobGroupWithoutCsiConf, '', 3, false, 60)
        }

        when: "larpService listens to result of JobGroup without csi configuration"
        resultPersisterService.listenToResult(xmlResult, server1)

        then: "persisted EventResult has no csi value"
        Collection<EventResult> resultsWithCsiCalculated = EventResult.findAll {
            csByWptDocCompleteInPercent != null
        }
        resultsWithCsiCalculated.size() == 0
    }

    void "csi must be calculated with csi-configuration, all values are 100%"() {
        setup: "prepare Job and JobGroup"
        JobGroup.withNewTransaction {
            JobGroup jobGroupWithCsiConf = JobGroup.findByName(jobGroupName_csi_1)
            jobGroupWithCsiConf.csiConfiguration = csiConfiguration_all_1
            jobGroupWithCsiConf.save(failOnError: true)
            TestDataUtil.createJob('FF_LH_BV1_hetzner', testScript, testLocation, jobGroupWithCsiConf, '', 3, false, 60)
        }

        when: "larpService listens to result of JobGroup with csi configuration that translates all load times to 100%"
        resultPersisterService.listenToResult(xmlResult, server1)

        then: "persisted EventResult has csi value of 100%"
        List<EventResult> results = EventResult.findAll {
            csByWptDocCompleteInPercent != null
        }
        results.size() > 0
        results*.csByWptDocCompleteInPercent.unique(false) == [100]
    }

    void "csi must be calculated with csi-configuration, all values are 50%"() {
        setup: "prepare Job and JobGroup"
        JobGroup.withNewTransaction {
            JobGroup jobGroup = JobGroup.findByName(jobGroupName_csi_05)
            jobGroup.csiConfiguration = csiConfiguration_all_05
            jobGroup.save(failOnError: true)
            TestDataUtil.createJob('FF_LH_BV1_hetzner', testScript, testLocation, jobGroup, '', 3, false, 60)
        }

        when: "larpService listens to result of JobGroup with csi configuration that translates all load times to 50%"
        resultPersisterService.listenToResult(xmlResult, server1)

        then: "persisted EventResult has csi value of 50%"
        List<EventResult> results = EventResult.findAll {
            csByWptDocCompleteInPercent != null
        }
        results.size() > 0
        results*.csByWptDocCompleteInPercent.unique(false) == [50]
    }

    private void createTestDataCommonForAllTests() {

        JobGroup.withNewTransaction {

            TestDataUtil.createOsmConfig()

            String nameOfResultXmlFile = 'MULTISTEP_FORK_ITERATEC_1Run_WithVideo.xml'
            File file = new File("test/resources/WptResultXmls/${nameOfResultXmlFile}")
            xmlResult = new WptResultXml(new XmlSlurper().parse(file))

            TestDataUtil.createCsiAggregationIntervals()
            TestDataUtil.createAggregatorTypes()
            TestDataUtil.createBrowsersAndAliases()
            createPages()
            server1 = TestDataUtil.createServer()
            testLocation = TestDataUtil.createLocation(server1, 'otto-prod-hetzner:Firefox', Browser.findByName('FF'), true)
            testScript = TestDataUtil.createScript('test-script', 'description', 'navigate   http://my-url.de')
            TestDataUtil.createJobGroup(jobGroupName_csi_1)
            TestDataUtil.createJobGroup(jobGroupName_csi_05)

            createCsiConfigurations()

        }

    }

    private void createPages() {
        allPages.each { pageName ->
            new Page(
                    name: pageName
            ).save(failOnError: true)
        }
    }

    private void createCsiConfigurations() {
        csiConfiguration_all_1 = TestDataUtil.createCsiConfiguration("All 1")
        csiConfiguration_all_05 = TestDataUtil.createCsiConfiguration("All 0.5")
    }

    private void mocksCommonForAllTests() {
        def timeToCsMappingService = new StubFor(TimeToCsMappingService, true)
        timeToCsMappingService.demand.getCustomerSatisfactionInPercent(0..10000) {
            Integer docReadyTimeInMilliSecs, Page page, CsiConfiguration csiConfiguration = null ->
                if (csiConfiguration == null) return null
                else if (csiConfiguration.label == csiConfiguration_all_1.label) return 100d
                else if (csiConfiguration.label == csiConfiguration_all_05.label) return 50d
        }
        resultPersisterService.timeToCsMappingService = timeToCsMappingService.proxyInstance()
    }
}
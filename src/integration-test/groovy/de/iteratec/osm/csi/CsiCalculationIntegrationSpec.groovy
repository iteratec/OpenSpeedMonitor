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

import de.iteratec.osm.ConfigService
import de.iteratec.osm.csi.transformation.TimeToCsMappingService
import de.iteratec.osm.measurement.environment.Browser
import de.iteratec.osm.measurement.environment.Location
import de.iteratec.osm.measurement.environment.WebPageTestServer
import de.iteratec.osm.measurement.environment.wptserver.ResultPersisterService
import de.iteratec.osm.measurement.environment.wptserver.WptResultXml
import de.iteratec.osm.measurement.schedule.Job
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.result.EventResult
import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration

import static de.iteratec.osm.OsmConfiguration.DEFAULT_MAX_VALID_LOADTIME
import static de.iteratec.osm.OsmConfiguration.DEFAULT_MIN_VALID_LOADTIME

/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
@Integration(applicationClass = openspeedmonitor.Application.class)
@Rollback
class CsiCalculationIntegrationSpec extends NonTransactionalIntegrationSpec {
    ResultPersisterService resultPersisterService

    WptResultXml xmlResult
    CsiConfiguration csiConfiguration_all_1
    CsiConfiguration csiConfiguration_all_05
    static String jobLabelFromXML = 'FF_LH_BV1_hetzner'

    WebPageTestServer server1

    def setup() {
        createTestDataCommonForAllTests()
        mocksCommonForAllTests()
    }

    def cleanup() {
        resultPersisterService.timeToCsMappingService = grailsApplication.mainContext.getBean('timeToCsMappingService')
        resultPersisterService.configService = grailsApplication.mainContext.getBean('configService')
    }

    void "csi won't be calculated without csi-configuration"() {
        setup: "prepare Job and JobGroup"
        Job.build(label: jobLabelFromXML)

        when: "larpService listens to result of JobGroup without csi configuration"
        resultPersisterService.listenToResult(xmlResult, server1)
        Collection<EventResult> resultsWithCsiCalculated = EventResult.findAllByCsByWptDocCompleteInPercentIsNotNull()

        then: "persisted EventResult has no csi value"
        resultsWithCsiCalculated.size() == 0
    }

    void "csi must be calculated with csi-configuration, all values are 100%"() {
        setup: "prepare Job and JobGroup"
        JobGroup jobGroupWithCsiConf = JobGroup.build(csiConfiguration: csiConfiguration_all_1)
        Job.build(label: jobLabelFromXML, jobGroup: jobGroupWithCsiConf)

        when: "larpService listens to result of JobGroup with csi configuration that translates all load times to 100%"
        resultPersisterService.listenToResult(xmlResult, server1)
        List<EventResult> results = EventResult.findAllByCsByWptDocCompleteInPercentIsNotNull()

        then: "persisted EventResult has csi value of 100%"
        results.size() > 0
        results*.csByWptDocCompleteInPercent.unique(false) == [100d]
    }

    void "csi must be calculated with csi-configuration, all values are 50%"() {
        setup: "prepare Job and JobGroup"
        JobGroup jobGroup = JobGroup.build(csiConfiguration: csiConfiguration_all_05)
        Job.build(label: jobLabelFromXML, jobGroup: jobGroup)

        when: "larpService listens to result of JobGroup with csi configuration that translates all load times to 50%"
        resultPersisterService.listenToResult(xmlResult, server1)
        List<EventResult> results = EventResult.findAllByCsByWptDocCompleteInPercentIsNotNull()

        then: "persisted EventResult has csi value of 50%"
        results.size() > 0
        results*.csByWptDocCompleteInPercent.unique(false) == [50d]
    }

    private void createTestDataCommonForAllTests() {
        String nameOfResultXmlFile = 'MULTISTEP_FORK_ITERATEC_1Run_WithVideo.xml'
        File file = new File("src/test/resources/WptResultXmls/${nameOfResultXmlFile}")
        xmlResult = new WptResultXml(new XmlSlurper().parse(file))

        server1 = WebPageTestServer.build(active: true, baseUrl: 'http://wpt.server.de')
        Browser browser = Browser.build()
        Location.build(wptServer: server1, uniqueIdentifierForServer: 'otto-prod-hetzner:Firefox', browser: browser)
        createCsiConfigurations()
    }

    private void createCsiConfigurations() {
        csiConfiguration_all_1 = CsiConfiguration.build(label: "All 1")
        csiConfiguration_all_05 = CsiConfiguration.build(label: "All 0.5")
    }

    private void mocksCommonForAllTests() {
        TimeToCsMappingService service = Stub(TimeToCsMappingService)
        service.getCustomerSatisfactionInPercent(_, _, _) >> { a, b, csiConfiguration ->
            if (csiConfiguration == null) return null
            else if (csiConfiguration.label == csiConfiguration_all_1.label) return 100d
            else if (csiConfiguration.label == csiConfiguration_all_05.label) return 50d
        }
        resultPersisterService.timeToCsMappingService = service
        resultPersisterService.configService = Stub(ConfigService) {
            getMaxValidLoadtime() >> DEFAULT_MAX_VALID_LOADTIME
            getMinValidLoadtime() >> DEFAULT_MIN_VALID_LOADTIME
        }
    }
}
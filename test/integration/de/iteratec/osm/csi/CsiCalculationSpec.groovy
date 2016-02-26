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
import de.iteratec.osm.measurement.environment.wptserverproxy.LocationAndResultPersisterService
import de.iteratec.osm.measurement.schedule.Job
import de.iteratec.osm.measurement.schedule.JobGroup

import de.iteratec.osm.measurement.schedule.JobService
import de.iteratec.osm.measurement.script.Script
import de.iteratec.osm.result.EventResult
import de.iteratec.osm.result.JobResult
import de.iteratec.osm.result.MeasuredEvent
import de.iteratec.osm.result.CsiAggregationTagService
import de.iteratec.osm.result.PageService
import de.iteratec.osm.util.ServiceMocker
import groovy.util.slurpersupport.GPathResult
import spock.lang.Shared
import spock.lang.Specification

import grails.test.mixin.*

/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */

@TestFor(LocationAndResultPersisterService)
@Mock([WebPageTestServer,Location,Script,JobGroup, Browser,Job,JobResult, Page, MeasuredEvent, EventResult, CsiConfiguration, TimeToCsMapping])
class CsiCalculationSpec extends Specification {
    @Shared
    static final String jobGroupName_csi_1 = "jobGroup1"
    @Shared
    static final String jobGroupName_csi_05 = "jobGroup2"
    @Shared
    static final List<Page> allPages = ['HP', 'MES', 'PL','SE','HP_entry','ADS','WK',Page.UNDEFINED]

    @Shared
    LocationAndResultPersisterService serviceUnderTest
    @Shared
    GPathResult xmlResult
    @Shared
    CsiConfiguration csiConfiguration_all_1
    @Shared
    CsiConfiguration csiConfiguration_all_05

    WebPageTestServer server1
    Location testLocation
    Script testScript


    def setupSpec() {
    }

    def setup() {
        ServiceMocker serviceMocker = ServiceMocker.create()
        serviceUnderTest = service
        serviceUnderTest.jobService = new JobService()
        serviceUnderTest.pageService = new PageService()
        serviceUnderTest.csiAggregationTagService = new CsiAggregationTagService()
        serviceUnderTest.timeToCsMappingService = new TimeToCsMappingService()
        serviceMocker.mockPerformanceLoggingService(serviceUnderTest)
        serviceMocker.mockProxyService(serviceUnderTest)
        serviceMocker.mockMetricReportingService(serviceUnderTest)
        serviceMocker.mockCsiAggregationUpdateService(serviceUnderTest)
        serviceMocker.mockConfigService(serviceUnderTest.timeToCsMappingService, 'org.h2.Driver', 60, CsiTransformation.BY_MAPPING)

        //create test-specific data
        String nameOfResultXmlFile = 'Result_wptserver2.15_multistep_1Run_WithVideo.xml'
        File file = new File("test/resources/WptResultXmls/${nameOfResultXmlFile}")
        xmlResult = new XmlSlurper().parse(file)

        TestDataUtil.createBrowsersAndAliases()
        createPages()
        server1 = TestDataUtil.createServer()
        testLocation = TestDataUtil.createLocation(server1, 'otto-prod-hetzner:Firefox', Browser.findByName('FF'), true)
        testScript = TestDataUtil.createScript('test-script', 'description', 'navigate   http://my-url.de', false)
        TestDataUtil.createJobGroup(jobGroupName_csi_1)
        TestDataUtil.createJobGroup(jobGroupName_csi_05)

        createCsiConfigurations()
    }

    void "csi won't be calculated without csi-configuration"() {
        setup: "persist result"
        JobGroup jobGroup = JobGroup.findByName(jobGroupName_csi_1)
        TestDataUtil.createJob('FF_LH_BV1_hetzner', testScript, testLocation, jobGroup, '', 3 , false, 60)
        serviceUnderTest.listenToResult(xmlResult,"",server1)
        Collection<EventResult> results = EventResult.findAll {
            csByWptDocCompleteInPercent != null
        }

        expect:
        results.empty
    }

    void "csi must be calculated with csi-configuration, all values are 1.0"() {
        setup: "csiConfiguration_all_1 to JobGroup"
        JobGroup jobGroup = JobGroup.findByName(jobGroupName_csi_1)
        TestDataUtil.createJob('FF_LH_BV1_hetzner', testScript, testLocation, jobGroup, '', 3 , false, 60)
        jobGroup.csiConfiguration = csiConfiguration_all_1
        and: "and persist result for calculating csi"
        serviceUnderTest.listenToResult(xmlResult,"",server1)
        double csiValue = EventResult.findAll {
            csByWptDocCompleteInPercent != null
        }.first().csByWptDocCompleteInPercent

        expect:
        csiValue == 1.0
    }

    void "csi must be calculated with csi-configuration, all values are 0.5"() {
        setup: "csiConfiguration_all_05 to JobGroup"
        JobGroup jobGroup = JobGroup.findByName(jobGroupName_csi_05)
        TestDataUtil.createJob('FF_LH_BV1_hetzner', testScript, testLocation, jobGroup, '', 3 , false, 60)
        jobGroup.csiConfiguration = csiConfiguration_all_05
        and: "and persist result for calculating csi"
        serviceUnderTest.listenToResult(xmlResult,"",server1)
        double csiValue = EventResult.findAll {
            csByWptDocCompleteInPercent != null
        }.first().csByWptDocCompleteInPercent

        expect:
        csiValue == 0.5
    }

    private createPages(){
        allPages.each{pageName ->
            new Page(
                    name: pageName
            ).save(failOnError: true)
        }
    }

    private createCsiConfigurations() {
        List<TimeToCsMapping> timeToCsMappingList1 = new ArrayList<>()
        allPages.each { page ->
            (0..10000).each { loadTime ->
                if(loadTime % 20 == 0) {
                    timeToCsMappingList1.add(
                            new TimeToCsMapping(
                                    page: Page.findByName(page),
                                    loadTimeInMilliSecs: loadTime,
                                    customerSatisfaction: 1.0,
                                    mappingVersion: 1
                            )
                    )
                }
            }
        }
        csiConfiguration_all_1 = TestDataUtil.createCsiConfiguration()
        csiConfiguration_all_1.label = "All 1"
        csiConfiguration_all_1.timeToCsMappings = timeToCsMappingList1

        List<TimeToCsMapping> timeToCsMappingList2 = new ArrayList<>()
        timeToCsMappingList2.clear()
        allPages.each { page ->
            (0..10000).each { loadTime ->
                if(loadTime % 20 == 0) {
                    timeToCsMappingList2.add(
                            new TimeToCsMapping(
                                    page: Page.findByName(page),
                                    loadTimeInMilliSecs: loadTime,
                                    customerSatisfaction: 0.5,
                                    mappingVersion: 1
                            )
                    )
                }
            }
        }
        csiConfiguration_all_05 = TestDataUtil.createCsiConfiguration()
        csiConfiguration_all_05.label = "All 0.5"
        csiConfiguration_all_05.timeToCsMappings = timeToCsMappingList2
    }
}
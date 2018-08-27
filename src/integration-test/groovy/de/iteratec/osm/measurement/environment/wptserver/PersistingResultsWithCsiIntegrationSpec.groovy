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

import de.iteratec.osm.OsmConfiguration
import de.iteratec.osm.csi.CsiConfiguration
import de.iteratec.osm.csi.DefaultTimeToCsMapping
import de.iteratec.osm.csi.NonTransactionalIntegrationSpec
import de.iteratec.osm.csi.Page
import de.iteratec.osm.csi.transformation.DefaultTimeToCsMappingService
import de.iteratec.osm.measurement.environment.Browser
import de.iteratec.osm.measurement.environment.Location
import de.iteratec.osm.measurement.environment.WebPageTestServer
import de.iteratec.osm.measurement.schedule.Job
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.report.external.MetricReportingService
import de.iteratec.osm.result.EventResult
import de.iteratec.osm.result.JobResult
import de.iteratec.osm.result.MeasuredEvent
import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration

@Integration(applicationClass = openspeedmonitor.Application.class)
@Rollback
class PersistingResultsWithCsiIntegrationSpec extends NonTransactionalIntegrationSpec {

    ResultPersisterService resultPersisterService
    DefaultTimeToCsMappingService defaultTimeToCsMappingService


    private static final String LOCATION_IDENTIFIER = 'Agent1-wptdriver:Firefox'
    WebPageTestServer server

    def setup() {
        OsmConfiguration.build()
    }

    def cleanup() {
        resultPersisterService.metricReportingService = grailsApplication.mainContext.getBean('metricReportingService')
    }

    void "EventResults of all steps will be saved if some have a customer satisfaction while others have not."() {

        given: ""
        createTestDataCommonToAllTests()
        File file = new File("src/test/resources/WptResultXmls/MULTISTEP_1Run_5Steps.xml")
        WptResultXml xmlResult = new WptResultXml(new XmlSlurper().parse(file))

        when: ""
        resultPersisterService.listenToResult(xmlResult, server)
        List<EventResult> eventResults = EventResult.list()

        then: ""
        JobResult.list().size() == 1
        eventResults.size() == 5
        eventResults.findAll { it.csByWptDocCompleteInPercent }.size() == 3

    }

    private createTestDataCommonToAllTests() {
        createDefaultTimeToCsiMapping()
        Page hp = Page.build(name: 'HP')
        Page ads = Page.build(name: 'ADS')
        Page adsEntry = Page.build(name: 'ADS_entry')
        Page undefined = Page.build(name: Page.UNDEFINED)
        MeasuredEvent meEsprit = MeasuredEvent.build(testedPage: undefined, name: 'esprit_infrontofotto')
        MeasuredEvent meGoogle = MeasuredEvent.build(testedPage: undefined, name: 'google_infrontofotto')
        MeasuredEvent meAdsEntry = MeasuredEvent.build(testedPage: adsEntry, name: 'OTTO_ADS_Einstiegsseite_flashlisghts-hose_XL')
        MeasuredEvent meAds = MeasuredEvent.build(testedPage: ads, name: 'OTTO_ADS_arizona-jeans_XL')
        MeasuredEvent meHp = MeasuredEvent.build(testedPage: hp, name: 'OTTO_HP_NichtEinstiegsseite_XL')
        server = WebPageTestServer.build(baseUrl: "http://prod.server01.wpt.iteratec.de")
        Browser browser = Browser.build()
        Location loc = Location.build(
                wptServer: server,
                uniqueIdentifierForServer: LOCATION_IDENTIFIER,
                browser: browser,
        )
        CsiConfiguration csiConf = CsiConfiguration.build()
        JobGroup jobGroup = JobGroup.build(csiConfiguration: csiConf)
        Job.build(
                label: 'CH_OTTO_ADS_hetzner',
                location: loc,
                jobGroup: jobGroup
        )

        defaultTimeToCsMappingService.copyDefaultMappingToPage(adsEntry, '3', csiConf)
        defaultTimeToCsMappingService.copyDefaultMappingToPage(ads, '3', csiConf)
        defaultTimeToCsMappingService.copyDefaultMappingToPage(hp, '3', csiConf)
    }

    /**
     * These default mappings can be assigned to measured pages if no data of a real customer survey exist.
     * Get created only if no one exist at all.
     */
    void createDefaultTimeToCsiMapping() {

        if (DefaultTimeToCsMapping.list().size() == 0) {

            Map indexToMappingName = [1: '1 - impatient', 2: '2', 3: '3', 4: '4', 5: '5 - patient']
            String fileName = 'Default_CSI_Mappings.csv'
            InputStream csvIs = this.class.classLoader.getResourceAsStream(fileName)
            BufferedReader csvFileReader = new BufferedReader(new InputStreamReader(csvIs))
            int lineCounter = 0
            String line
            while ((line = csvFileReader.readLine()) != null) {
                // exclude header
                if (lineCounter >= 1) {
                    def tokenized = line.tokenize(';')
                    5.times { defaultMappingindex ->
                        new DefaultTimeToCsMapping(
                                name: indexToMappingName[defaultMappingindex + 1],
                                loadTimeInMilliSecs: tokenized[0],
                                customerSatisfactionInPercent: tokenized[defaultMappingindex + 1]
                        ).save(failOnError: true, flush: true)
                    }

                }
                lineCounter++
            }
            csvFileReader.close();
        }

    }

    void mockMetricReportingService() {
        MetricReportingService metricReportingService = Stub(MetricReportingService)
        resultPersisterService.metricReportingService = metricReportingService
    }
}

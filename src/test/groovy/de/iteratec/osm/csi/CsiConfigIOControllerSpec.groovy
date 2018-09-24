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

import de.iteratec.osm.measurement.environment.Browser
import de.iteratec.osm.measurement.schedule.ConnectivityProfile
import de.iteratec.osm.util.I18nService
import grails.buildtestdata.BuildDataTest
import grails.buildtestdata.mixin.Build
import grails.testing.web.controllers.ControllerUnitTest
import org.apache.commons.io.FileUtils
import org.grails.plugins.testing.GrailsMockMultipartFile
import spock.lang.Specification

@Build([CsiConfiguration, BrowserConnectivityWeight, PageWeight, Browser, ConnectivityProfile, Page])
class CsiConfigIOControllerSpec extends Specification implements BuildDataTest,
        ControllerUnitTest<CsiConfigIOController> {

    CsiConfiguration csiConfigurationFilled
    CsiConfiguration csiConfigurationEmpty

    Closure doWithSpring() {
        return {
            customerSatisfactionWeightService(CustomerSatisfactionWeightService)
        }
    }

    void setup() {
        initializeSpringBeanServices()
        createTestDataCommonToAllTestsViaBuild()
        createMocksCommonToAllTests()
    }

    void setupSpec() {
        mockDomains(CsiConfiguration, BrowserConnectivityWeight, ConnectivityProfile, Browser, Page, PageWeight, CsiDay)
    }

    //################### EXPORTS ###################

    void "download BrowserConnectivityWeights-CSV should equal CsiConfiguration.browserConnectivityWeights"() {
        given: "Some BrowserConnectivityWeights exist in db."
        String csvContent = FileUtils.readFileToString(new File("src/test/resources/CsiData/BROWSER_CONNECTIVITY_COMBINATION_weights.csv")).normalize()

        when: "These BrowserConnectivityWeights get downloaded."
        params.id = csiConfigurationFilled.ident()
        controller.downloadBrowserConnectivityWeights()

        then: "The response equals these weights."
        response.contentAsString == (csvContent + "\n")
    }

    void "download BrowserConnectivityWeights-CSV should not create BrowserConnectivityWeights"() {
        given: "Some BrowserConnectivityWeights exist in db."
        int beforeAmountBrowserConnectivityWeights = BrowserConnectivityWeight.count

        when: "These BrowserConnectivityWeights get downloaded."
        params.id = csiConfigurationFilled.ident()
        controller.downloadBrowserConnectivityWeights()

        then: "No new BrowserConnectivityWeights got persisted in db."
        BrowserConnectivityWeight.count == beforeAmountBrowserConnectivityWeights
    }

    void "download PageWeights-CSV should equal CsiConfiguration.pageWeights"() {
        given: "Some PageWeights exist in db."
        String csvContent = FileUtils.readFileToString(new File("src/test/resources/CsiData/PAGE_weights.csv")).normalize()

        when: "These PageWeights get downloaded."
        params.id = csiConfigurationFilled.ident()
        controller.downloadPageWeights()

        then: "The response equals these weights."
        response.contentAsString == (csvContent + "\n")
    }

    void "download PageWeights-CSV should not create PageWeights"() {
        given: "Some PageWeights exist in db."
        int beforeAmountPageWeights = PageWeight.count

        when: "These PageWeights get downloaded."
        params.id = csiConfigurationFilled.ident()
        controller.downloadPageWeights()

        then: "No new PageWeights got persisted in db."
        PageWeight.count == beforeAmountPageWeights
    }

    void "download HourOfDay-CSV should equal CsiConfiguration.csiDay"() {
        given: "A CsiDay exists in db."
        String csvContent = FileUtils.readFileToString(new File("src/test/resources/CsiData/HOUROFDAY_weights.csv")).normalize()

        when: "The hour of day weights get downloaded."
        params.id = csiConfigurationFilled.ident()
        controller.downloadHourOfDayWeights()

        then: "The response equals the CsiDay."
        response.contentAsString == (csvContent + "\n")
    }

    void "download HourOfDay-CSV should not create CsiDay"() {
        given: "A CsiDay exists in db."
        int beforeAmountCsiDays = CsiDay.count

        when: "The hour of day weights get downloaded."
        params.id = csiConfigurationFilled.ident()
        controller.downloadHourOfDayWeights()

        then: "No new CsiDays got persisted in db."
        CsiDay.count == beforeAmountCsiDays
    }

    //################# END EXPORTS #################

    //################### UPLOADS ###################

    void "upload BrowserConnectivityWeights-CSV should change CsiConfiguration.browserConnectivityWeights"() {
        given: "No BrowserConnectivityWeights exist in db."
        String csvContent = FileUtils.readFileToString(new File("src/test/resources/CsiData/BROWSER_CONNECTIVITY_COMBINATION_weights.csv"))
        def multipartFile = new GrailsMockMultipartFile('browserConnectivityCsv', csvContent.bytes)
        request.addFile(multipartFile)
        assert csiConfigurationEmpty.browserConnectivityWeights.size() == 0

        when: "A CSV file with BrowserConnectivityWeights is uploaded."
        params.selectedCsiConfigurationId = csiConfigurationEmpty.ident()
        controller.uploadBrowserConnectivityWeights()

        then: "The BrowserConnectivityWeights of CSV got persisted and associated to correct CsiConfiguration."
        csiConfigurationEmpty.browserConnectivityWeights.size() == 2
        csiConfigurationEmpty.browserConnectivityWeights*.weight.containsAll([45d, 12d])
    }

    void "upload BrowserConnectivityWeights-CSV should not create BrowserConnectivityWeights if existing"() {
        given: "BrowserConnectivityWeights already exist in db."
        int beforeAmountBrowserConnectivityWeights = BrowserConnectivityWeight.count
        String csvContent = FileUtils.readFileToString(new File("src/test/resources/CsiData/BROWSER_CONNECTIVITY_COMBINATION_weights.csv"))
        def multipartFile = new GrailsMockMultipartFile('browserConnectivityCsv', csvContent.bytes)
        request.addFile(multipartFile)

        when: "A CSV file with BrowserConnectivityWeights is uploaded."
        params.selectedCsiConfigurationId = csiConfigurationFilled.ident()
        controller.uploadBrowserConnectivityWeights()

        then: "No new BrowserConnectivityWeights got persisted in db."
        BrowserConnectivityWeight.count == beforeAmountBrowserConnectivityWeights
    }

    void "upload PageWeights-CSV should change CsiConfiguration.pageWeights"() {
        given: "No PageWeights exist in db."
        String csvContent = FileUtils.readFileToString(new File("src/test/resources/CsiData/PAGE_weights.csv"))
        def multipartFile = new GrailsMockMultipartFile('pageCsv', csvContent.bytes)
        request.addFile(multipartFile)

        when: "A CSV file with PageWeights is uploaded."
        params.selectedCsiConfigurationId = csiConfigurationEmpty.ident()
        controller.uploadPageWeights()

        then: "The BrowserConnectivityWeights of CSV got persisted and associated to correct CsiConfiguration."
        csiConfigurationEmpty.pageWeights*.id.containsAll(csiConfigurationFilled.pageWeights*.id)
    }

    void "upload PageWeights-CSV should not create PageWeights if existing"() {
        given: "PageWeights already exist in db."
        int beforeAmountPageWeights = PageWeight.count
        String csvContent = FileUtils.readFileToString(new File("src/test/resources/CsiData/PAGE_weights.csv"))
        def multipartFile = new GrailsMockMultipartFile('pageCsv', csvContent.bytes)
        request.addFile(multipartFile)

        when: "A CSV file with PageWeights is uploaded."
        params.selectedCsiConfigurationId = csiConfigurationFilled.ident()
        controller.uploadPageWeights()

        then: "No new PageWeights got persisted in db."
        PageWeight.count == beforeAmountPageWeights
    }

    void "upload HourOfDay-CSV should change CsiConfiguration.csiDay"() {
        given: "No CsiDays exist in db."
        String csvContent = FileUtils.readFileToString(new File("src/test/resources/CsiData/HOUROFDAY_weights.csv"))
        def multipartFile = new GrailsMockMultipartFile('hourOfDayCsv', csvContent.bytes)
        request.addFile(multipartFile)

        when: "A CSV file with CsiDay is uploaded."
        params.selectedCsiConfigurationId = csiConfigurationEmpty.ident()
        controller.uploadHourOfDayWeights()

        then: "The CsiDay of CSV got persisted and associated to correct CsiConfiguration."
        csiConfigurationEmpty.csiDay == csiConfigurationFilled.csiDay
    }

    void "upload HourOfDay-CSV should not create CsiDay if existing"() {
        given: "CsiDay already exist in db."
        int beforeAmountCsiDays = CsiDay.count
        String csvContent = FileUtils.readFileToString(new File("src/test/resources/CsiData/HOUROFDAY_weights.csv"))
        def multipartFile = new GrailsMockMultipartFile('hourOfDayCsv', csvContent.bytes)
        request.addFile(multipartFile)

        when: "A CSV file with CsiDay is uploaded."
        params.selectedCsiConfigurationId = csiConfigurationFilled.ident()
        controller.uploadHourOfDayWeights()

        then: "No new CsiDay got persisted in db."
        CsiDay.count == beforeAmountCsiDays
    }

    //################# END UPLOADS #################

    private createMocksCommonToAllTests() {
        controller.customerSatisfactionWeightService.i18nService = Mock(I18nService)
        controller.customerSatisfactionWeightService.i18nService.msg(_) >> "not relevant in these tests"
    }

    private void createTestDataCommonToAllTestsViaBuild() {
        csiConfigurationFilled = CsiConfiguration.build(
            browserConnectivityWeights: [
                BrowserConnectivityWeight.build(browser: Browser.build(name: "Browser1"), connectivity: ConnectivityProfile.build(name: "DSL1"), weight: 45.0),
                BrowserConnectivityWeight.build(browser: Browser.build(name: "Browser2"), connectivity: ConnectivityProfile.build(name: "DSL2"), weight: 12.0)
            ],
            pageWeights: [
                PageWeight.build(page: Page.build(name: 'HP'), weight: 12),
                PageWeight.build(page: Page.build(name: 'MES'), weight: 3.4),
                PageWeight.build(page: Page.build(name: 'SE'), weight: 6.7),
                PageWeight.build(page: Page.build(name: 'ADS'), weight: 0.3),
                PageWeight.build(page: Page.build(name: 'WKBS'), weight: 45),
                PageWeight.build(page: Page.build(name: 'WK'), weight: 26.1)
            ],
            csiDay: createCsiDay()
        )
        csiConfigurationEmpty = CsiConfiguration.build()
    }

    private CsiDay createCsiDay() {
        CsiDay tempDay = new CsiDay()
        tempDay.setHourWeight(0, 2.9)
        tempDay.setHourWeight(1, 0.4)
        tempDay.setHourWeight(2, 0.2)
        tempDay.setHourWeight(3, 0.1)
        tempDay.setHourWeight(4, 0.1)
        tempDay.setHourWeight(5, 0.2)
        tempDay.setHourWeight(6, 0.7)
        tempDay.setHourWeight(7, 1.7)
        tempDay.setHourWeight(8, 3.2)
        tempDay.setHourWeight(9, 4.8)
        tempDay.setHourWeight(10, 5.6)
        tempDay.setHourWeight(11, 5.7)
        tempDay.setHourWeight(12, 5.5)
        tempDay.setHourWeight(13, 5.8)
        tempDay.setHourWeight(14, 5.9)
        tempDay.setHourWeight(15, 6.0)
        tempDay.setHourWeight(16, 6.7)
        tempDay.setHourWeight(17, 7.3)
        tempDay.setHourWeight(18, 7.6)
        tempDay.setHourWeight(19, 8.8)
        tempDay.setHourWeight(20, 9.3)
        tempDay.setHourWeight(21, 7.0)
        tempDay.setHourWeight(22, 3.6)
        tempDay.setHourWeight(23, 0.9)
        return tempDay
    }

    private void initializeSpringBeanServices() {
        controller.customerSatisfactionWeightService = grailsApplication.mainContext.getBean('customerSatisfactionWeightService')
    }
}

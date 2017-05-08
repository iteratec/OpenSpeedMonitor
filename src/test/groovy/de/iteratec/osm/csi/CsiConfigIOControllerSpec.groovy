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
import de.iteratec.osm.util.ServiceMocker
import org.apache.commons.io.FileUtils
import org.grails.plugins.testing.GrailsMockMultipartFile
import spock.lang.Specification
import grails.test.mixin.*
import grails.test.mixin.support.*

/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
@TestMixin(GrailsUnitTestMixin)
@TestFor(CsiConfigIOController)
@Mock([CsiConfiguration, BrowserConnectivityWeight, ConnectivityProfile, Browser, Page, PageWeight, CsiDay])
class CsiConfigIOControllerSpec extends Specification{
    CsiConfiguration csiConfigurationFilled
    CsiConfiguration csiConfigurationEmpty
    def doWithSpring = {
        customerSatisfactionWeightService(CustomerSatisfactionWeightService)
    }

    void setup() {
        controller.customerSatisfactionWeightService = grailsApplication.mainContext.getBean('customerSatisfactionWeightService')
        Browser browser1 = TestDataUtil.createBrowser("Browser1")
        Browser browser2 = TestDataUtil.createBrowser("Browser2")
        ConnectivityProfile connectivityProfile1 = TestDataUtil.createConnectivityProfile("DSL1")
        ConnectivityProfile connectivityProfile2 = TestDataUtil.createConnectivityProfile("DSL2")
        TestDataUtil.createPages(['HP','MES','SE','ADS','WKBS','WK'])
        CsiDay tempDay = new CsiDay()
        tempDay.setHourWeight(0,2.9)
        tempDay.setHourWeight(1,0.4)
        tempDay.setHourWeight(2,0.2)
        tempDay.setHourWeight(3,0.1)
        tempDay.setHourWeight(4,0.1)
        tempDay.setHourWeight(5,0.2)
        tempDay.setHourWeight(6,0.7)
        tempDay.setHourWeight(7,1.7)
        tempDay.setHourWeight(8,3.2)
        tempDay.setHourWeight(9,4.8)
        tempDay.setHourWeight(10,5.6)
        tempDay.setHourWeight(11,5.7)
        tempDay.setHourWeight(12,5.5)
        tempDay.setHourWeight(13,5.8)
        tempDay.setHourWeight(14,5.9)
        tempDay.setHourWeight(15,6.0)
        tempDay.setHourWeight(16,6.7)
        tempDay.setHourWeight(17,7.3)
        tempDay.setHourWeight(18,7.6)
        tempDay.setHourWeight(19,8.8)
        tempDay.setHourWeight(20,9.3)
        tempDay.setHourWeight(21,7.0)
        tempDay.setHourWeight(22,3.6)
        tempDay.setHourWeight(23,0.9)
        csiConfigurationFilled = TestDataUtil.createCsiConfiguration()
        csiConfigurationFilled.label = "conf1"
        csiConfigurationFilled.browserConnectivityWeights.add(new BrowserConnectivityWeight(browser: browser1,connectivity: connectivityProfile1, weight: 45.0))
        csiConfigurationFilled.browserConnectivityWeights.add(new BrowserConnectivityWeight(browser: browser2,connectivity: connectivityProfile2, weight: 12.0))
        csiConfigurationFilled.pageWeights.add(new PageWeight(page: Page.findByName('HP'),weight: 12))
        csiConfigurationFilled.pageWeights.add(new PageWeight(page: Page.findByName('MES'),weight: 3.4))
        csiConfigurationFilled.pageWeights.add(new PageWeight(page: Page.findByName('SE'),weight: 6.7))
        csiConfigurationFilled.pageWeights.add(new PageWeight(page: Page.findByName('ADS'),weight: 0.3))
        csiConfigurationFilled.pageWeights.add(new PageWeight(page: Page.findByName('WKBS'),weight: 45))
        csiConfigurationFilled.pageWeights.add(new PageWeight(page: Page.findByName('WK'),weight: 26.1))
        csiConfigurationFilled.csiDay = tempDay
        csiConfigurationFilled.save(flush: true)

        csiConfigurationEmpty = TestDataUtil.createCsiConfiguration()
        csiConfigurationEmpty.label = "emptyConf"
        csiConfigurationEmpty.save(flush: true)
        ServiceMocker.create().mockI18nService(controller.customerSatisfactionWeightService)
    }

    void tearDown() {
        // Tear down logic here
    }

    //################### EXPORTS ###################

    void "download BrowserConnectivityWeights-CSV should equal CsiConfiguration.browserConnectivityWeights"() {
        given:
        String csvContent = FileUtils.readFileToString(new File("src/test/resources/CsiData/BROWSER_CONNECTIVITY_COMBINATION_weights.csv"))

        when:
        params.id = csiConfigurationFilled.ident()
        controller.downloadBrowserConnectivityWeights()

        then:
        response.contentAsString == (csvContent + "\n")
    }

    void "download BrowserConnectivityWeights-CSV should not create BrowserConnectivityWeights"() {
        given:
        int beforeAmountBrowserConnectivityWeights = BrowserConnectivityWeight.count

        when:
        params.id = csiConfigurationFilled.ident()
        controller.downloadBrowserConnectivityWeights()

        then:
        BrowserConnectivityWeight.count == beforeAmountBrowserConnectivityWeights
    }

    void "download PageWeights-CSV should equal CsiConfiguration.pageWeights"() {
        given:
        String csvContent = FileUtils.readFileToString(new File("src/test/resources/CsiData/PAGE_weights.csv"))

        when:
        params.id = csiConfigurationFilled.ident()
        controller.downloadPageWeights()

        then:
        response.contentAsString == (csvContent + "\n")
    }

    void "download PageWeights-CSV should not create PageWeights"() {
        given:
        int beforeAmountPageWeights = PageWeight.count

        when:
        params.id = csiConfigurationFilled.ident()
        controller.downloadPageWeights()

        then:
        PageWeight.count == beforeAmountPageWeights
    }

    void "download HourOfDay-CSV should equal CsiConfiguration.csiDay"() {
        given:
        String csvContent = FileUtils.readFileToString(new File("src/test/resources/CsiData/HOUROFDAY_weights.csv"))

        when:
        params.id = csiConfigurationFilled.ident()
        controller.downloadHourOfDayWeights()

        then:
        response.contentAsString == (csvContent + "\n")
    }

    void "download HourOfDay-CSV should not create CsiDay"() {
        given:
        int beforeAmountCsiDays = CsiDay.count

        when:
        params.id = csiConfigurationFilled.ident()
        controller.downloadHourOfDayWeights()

        then:
        CsiDay.count == beforeAmountCsiDays
    }

    //################# END EXPORTS #################

    //################### UPLOADS ###################

    void "upload BrowserConnectivityWeights-CSV should change CsiConfiguration.browserConnectivityWeights"() {
        given:
        String csvContent = FileUtils.readFileToString(new File("src/test/resources/CsiData/BROWSER_CONNECTIVITY_COMBINATION_weights.csv"))
        def multipartFile = new GrailsMockMultipartFile('browserConnectivityCsv', csvContent.bytes)
        request.addFile(multipartFile)
        assert csiConfigurationEmpty.browserConnectivityWeights.size() == 0

        when:
        params.selectedCsiConfigurationId = csiConfigurationEmpty.ident()
        controller.uploadBrowserConnectivityWeights()

        then:
        csiConfigurationEmpty.browserConnectivityWeights.size() == 2
        csiConfigurationEmpty.browserConnectivityWeights*.weight.containsAll([45d, 12d])
    }

    void "upload BrowserConnectivityWeights-CSV should not create BrowserConnectivityWeights if existing"() {
        given:
        int beforeAmountBrowserConnectivityWeights = BrowserConnectivityWeight.count
        String csvContent = FileUtils.readFileToString(new File("src/test/resources/CsiData/BROWSER_CONNECTIVITY_COMBINATION_weights.csv"))
        def multipartFile = new GrailsMockMultipartFile('browserConnectivityCsv', csvContent.bytes)
        request.addFile(multipartFile)

        when:
        params.selectedCsiConfigurationId = csiConfigurationFilled.ident()
        controller.uploadBrowserConnectivityWeights()

        then:
        BrowserConnectivityWeight.count == beforeAmountBrowserConnectivityWeights
    }

    void "upload PageWeights-CSV should change CsiConfiguration.pageWeights"() {
        given:
        String csvContent = FileUtils.readFileToString(new File("src/test/resources/CsiData/PAGE_weights.csv"))
        def multipartFile = new GrailsMockMultipartFile('pageCsv', csvContent.bytes)
        request.addFile(multipartFile)

        when:
        params.selectedCsiConfigurationId = csiConfigurationEmpty.ident()
        controller.uploadPageWeights()

        then:
        csiConfigurationEmpty.pageWeights*.id.containsAll(csiConfigurationFilled.pageWeights*.id)
    }

    void "upload PageWeights-CSV should not create PageWeights if existing"() {
        given:
        int beforeAmountPageWeights = PageWeight.count
        String csvContent = FileUtils.readFileToString(new File("src/test/resources/CsiData/PAGE_weights.csv"))
        def multipartFile = new GrailsMockMultipartFile('pageCsv', csvContent.bytes)
        request.addFile(multipartFile)

        when:
        params.selectedCsiConfigurationId = csiConfigurationFilled.ident()
        controller.uploadPageWeights()

        then:
        PageWeight.count == beforeAmountPageWeights
    }

    void "upload HourOfDay-CSV should change CsiConfiguration.csiDay"() {
        given:
        String csvContent = FileUtils.readFileToString(new File("src/test/resources/CsiData/HOUROFDAY_weights.csv"))
        def multipartFile = new GrailsMockMultipartFile('hourOfDayCsv', csvContent.bytes)
        request.addFile(multipartFile)

        when:
        params.selectedCsiConfigurationId = csiConfigurationEmpty.ident()
        controller.uploadHourOfDayWeights()

        then:
        csiConfigurationEmpty.csiDay == csiConfigurationFilled.csiDay
    }

    void "upload HourOfDay-CSV should not create CsiDay if existing"() {
        given:
        int beforeAmountCsiDays = CsiDay.count
        String csvContent = FileUtils.readFileToString(new File("src/test/resources/CsiData/HOUROFDAY_weights.csv"))
        def multipartFile = new GrailsMockMultipartFile('hourOfDayCsv', csvContent.bytes)
        request.addFile(multipartFile)

        when:
        params.selectedCsiConfigurationId = csiConfigurationFilled.ident()
        controller.uploadHourOfDayWeights()

        then:
        CsiDay.count == beforeAmountCsiDays
    }

    //################# END UPLOADS #################
}

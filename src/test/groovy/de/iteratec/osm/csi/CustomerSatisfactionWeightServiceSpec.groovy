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

import de.iteratec.osm.csi.weighting.WeightFactor
import de.iteratec.osm.measurement.environment.Browser
import de.iteratec.osm.measurement.schedule.ConnectivityProfile
import de.iteratec.osm.util.I18nService
import grails.buildtestdata.BuildDataTest
import grails.buildtestdata.mixin.Build
import grails.testing.services.ServiceUnitTest
import spock.lang.Specification

/**
 * Test-suite of {@link CustomerSatisfactionWeightService}.
 */
@Build([Browser, ConnectivityProfile, Page, CsiConfiguration, BrowserConnectivityWeight, PageWeight, CsiDay])
class CustomerSatisfactionWeightServiceSpec extends Specification implements BuildDataTest,
        ServiceUnitTest<CustomerSatisfactionWeightService> {

    void setupSpec() {
        mockDomains(Page, Browser, BrowserConnectivityWeight, ConnectivityProfile, DefaultTimeToCsMapping,
                CsiConfiguration, PageWeight, CsiDay)
    }

    void "validate csv format"(String path, String fileNameExtension, WeightFactor weightFactor, int expectedEmptyfErrors, int expectedLineErrors, int expectedHeaderErrors, int nonFormatErrors) {
        setup: "I18Service mock with expected calls"
        I18nService i18nService = Mock(I18nService) {
            expectedEmptyfErrors * msg("de.iteratec.osm.csi.csvErrors.empty") >> "empty"
            expectedLineErrors * msg("de.iteratec.osm.csi.csvErrors.incorrectLine", _, _) >> "incorrectLine"
            expectedHeaderErrors * msg("de.iteratec.osm.csi.csvErrors.header", _, _) >> "header"
        }
        service.i18nService = i18nService

        when: "set csv file is turned into inputstream"
        File csvFile = new File(path + weightFactor + fileNameExtension)
        InputStream csvStream = new FileInputStream(csvFile)

        then: "the validation of this csv finds expected errors"
        service.validateWeightCsv(weightFactor, csvStream).size() == expectedLineErrors + expectedHeaderErrors + expectedEmptyfErrors + nonFormatErrors

        where:
        path                          | fileNameExtension            | weightFactor                                  | expectedEmptyfErrors | expectedLineErrors | expectedHeaderErrors | nonFormatErrors
        "src/test/resources/CsiData/" | "_weights.csv"               | WeightFactor.PAGE                             | 0                    | 0                  | 0                    | 0
        "src/test/resources/CsiData/" | "_weights_should_fail.csv"   | WeightFactor.PAGE                             | 0                    | 1                  | 0                    | 0
        "src/test/resources/CsiData/" | "_weights.csv"               | WeightFactor.BROWSER_CONNECTIVITY_COMBINATION | 0                    | 0                  | 0                    | 4
        "src/test/resources/CsiData/" | "_weights_should_fail.csv"   | WeightFactor.BROWSER_CONNECTIVITY_COMBINATION | 0                    | 1                  | 1                    | 6
        "src/test/resources/CsiData/" | "_weights.csv"               | WeightFactor.HOUROFDAY                        | 0                    | 0                  | 0                    | 0
        "src/test/resources/CsiData/" | "_weights_should_fail.csv"   | WeightFactor.HOUROFDAY                        | 0                    | 0                  | 0                    | 1
        "src/test/resources/CsiData/" | "_weights_should_fail_2.csv" | WeightFactor.HOUROFDAY                        | 0                    | 0                  | 0                    | 1
    }

    void "validate csv for Browser Connectivity Combination"(String path, String fileNameExtension, WeightFactor weightFactor, int expectedBrowserErrors, int expectedConnectivityErrors, int formatErrors) {
        setup: "I18Service mock with expected calls and provide needed Browsers as well as ConnectivityProfiles"
        I18nService i18nService = Mock(I18nService) {
            expectedBrowserErrors * msg("de.iteratec.osm.csi.csvErrors.browserDoesNotExist", "browser nicht vorhanden", _) >> "browser"
            expectedConnectivityErrors * msg("de.iteratec.osm.csi.csvErrors.connectivityDoesNotExist", "verbindung nicht vorhanden", _) >> "connectivity"
        }
        service.i18nService = i18nService
        Browser.build(name: "Browser1")
        Browser.build(name: "Browser2")
        ConnectivityProfile.build(name: "DSL1", active: true, bandwidthDown: 0, bandwidthUp: 0, latency: 0, packetLoss: 0)
        ConnectivityProfile.build(name: "DSL2", active: true, bandwidthDown: 0, bandwidthUp: 0, latency: 0, packetLoss: 0)

        when: "set csv file is turned into inputstream"
        File csvFile = new File(path + weightFactor + fileNameExtension)
        InputStream csvStream = new FileInputStream(csvFile)

        then: "the validation of this csv finds expected errors"
        service.validateWeightCsv(weightFactor, csvStream).size() == expectedConnectivityErrors + expectedBrowserErrors + formatErrors

        where:
        path                          | fileNameExtension          | weightFactor                                  | expectedConnectivityErrors | expectedBrowserErrors | formatErrors
        "src/test/resources/CsiData/" | "_weights.csv"             | WeightFactor.BROWSER_CONNECTIVITY_COMBINATION | 0                          | 0                     | 0
        "src/test/resources/CsiData/" | "_weights_should_fail.csv" | WeightFactor.BROWSER_CONNECTIVITY_COMBINATION | 1                          | 1                     | 2
    }

    void "validate csv for Hour Of Day"(String path, String fileNameExtension, WeightFactor weightFactor, int expectedHourErrors) {
        setup: "I18Service mock with expected calls"
        I18nService i18nService = Mock(I18nService) {
            expectedHourErrors * msg("de.iteratec.osm.csi.csvErrors.hourOfDay", "nicht alle 24 Stunden berücksichtigt") >> "hour"
        }
        service.i18nService = i18nService

        when: "set csv file is turned into inputstream"
        File csvFile = new File(path + weightFactor + fileNameExtension)
        InputStream csvStream = new FileInputStream(csvFile)

        then: "the validation of this csv finds expected errors"
        service.validateWeightCsv(weightFactor, csvStream).size() == expectedHourErrors

        where:
        path                          | fileNameExtension            | weightFactor           | expectedHourErrors
        "src/test/resources/CsiData/" | "_weights.csv"               | WeightFactor.HOUROFDAY | 0
        "src/test/resources/CsiData/" | "_weights_should_fail.csv"   | WeightFactor.HOUROFDAY | 1
        "src/test/resources/CsiData/" | "_weights_should_fail_2.csv" | WeightFactor.HOUROFDAY | 1
    }

    void "check Browser Connectivity Weight update or insert"(boolean isUpdate) {
        given: "browser and connectivity profiles"
        Browser browser1 = Browser.build(name: "Browser1")
        ConnectivityProfile connectivityProfile1 = ConnectivityProfile.build(name: "DSL1", active: true, bandwidthDown: 0, bandwidthUp: 0, latency: 0, packetLoss: 0)
        Browser browser2 = Browser.build(name: "Browser2")
        ConnectivityProfile connectivityProfile2 = ConnectivityProfile.build(name: "DSL2", active: true, bandwidthDown: 0, bandwidthUp: 0, latency: 0, packetLoss: 0)

        CsiConfiguration csiConfiguration
        if (isUpdate) {
            BrowserConnectivityWeight weight1 = BrowserConnectivityWeight.build(browser: browser1, connectivity: connectivityProfile1, weight: 1000)
            BrowserConnectivityWeight weight2 = BrowserConnectivityWeight.build(browser: browser2, connectivity: connectivityProfile2, weight: 1000)
            csiConfiguration = CsiConfiguration.build(browserConnectivityWeights: [weight1, weight2])
        } else {
            csiConfiguration = CsiConfiguration.build()
        }

        when: "csv is imported"
        File csvFile = new File("src/test/resources/CsiData/" + WeightFactor.BROWSER_CONNECTIVITY_COMBINATION + "_weights.csv")
        InputStream csvStream = new FileInputStream(csvFile)
        service.persistNewWeights(WeightFactor.BROWSER_CONNECTIVITY_COMBINATION, csvStream, csiConfiguration)

        then: "weights match"
        csiConfiguration.browserConnectivityWeights.findAll().size() == 2
        csiConfiguration.browserConnectivityWeights.find {it.browser == browser1}.weight == 45
        csiConfiguration.browserConnectivityWeights.find {it.browser == browser2}.weight == 12

        where:
        isUpdate | _
        false    | _
        true     | _
    }

    void "check Page Weight insert and update" (boolean isUpdate){
        given: "pages are set"
        Page page1 = Page.build(name: "HP")
        Page page2 = Page.build(name: "MES")
        Page page3 = Page.build(name: "SE")
        Page page4 = Page.build(name: "ADS")
        Page page5 = Page.build(name: "WKBS")
        Page page6 = Page.build(name: "WK")

        CsiConfiguration csiConfiguration
        if(isUpdate){
            PageWeight pageWeight1 = PageWeight.build(page: page1, weight: 1000)
            PageWeight pageWeight2 = PageWeight.build(page: page2, weight: 1000)
            PageWeight pageWeight3 = PageWeight.build(page: page3, weight: 1000)
            PageWeight pageWeight4 = PageWeight.build(page: page4, weight: 1000)
            PageWeight pageWeight5 = PageWeight.build(page: page5, weight: 1000)
            PageWeight pageWeight6 = PageWeight.build(page: page6, weight: 1000)
            csiConfiguration = CsiConfiguration.build(pageWeights: [pageWeight1, pageWeight2, pageWeight3, pageWeight4, pageWeight5, pageWeight6])
        } else{
            csiConfiguration = CsiConfiguration.build()
        }

        when: "csv is imported"
        File csvFile = new File("src/test/resources/CsiData/" + WeightFactor.PAGE + "_weights.csv")
        InputStream csvStream = new FileInputStream(csvFile)
        service.persistNewWeights(WeightFactor.PAGE, csvStream, csiConfiguration)

        then: "weights match"
        csiConfiguration.pageWeights.findAll().size() == 6
        csiConfiguration.pageWeights.find { it.page == page1}.weight == 12.0
        csiConfiguration.pageWeights.find { it.page == page2}.weight == 3.4
        csiConfiguration.pageWeights.find { it.page == page3}.weight == 6.7
        csiConfiguration.pageWeights.find { it.page == page4}.weight == 0.3
        csiConfiguration.pageWeights.find { it.page == page5}.weight == 45.0
        csiConfiguration.pageWeights.find { it.page == page6}.weight == 26.1

        where:
        isUpdate | _
        false    | _
        true     | _
    }

    void "check hour of day insert and update" (boolean isUpdate){
        given: "expected CsiDay is build"
        File csvFile = new File("src/test/resources/CsiData/" + WeightFactor.HOUROFDAY + "_weights.csv")
        InputStream csvStream = new FileInputStream(csvFile)
        Map<String,Double> hourWeights = new HashMap<>()
        Integer lineCounter = 0
        csvStream.eachLine { line ->
            if (lineCounter > 0) {
                List tokenized = line.tokenize(";")

                if (tokenized[0] && tokenized[1]) {
                    hourWeights.put("hour" + tokenized[0] + "Weight",Double.parseDouble(tokenized[1]))
                }
            }
            lineCounter++
        }
        CsiDay expectedCsiDay = CsiDay.build(hourWeights)

        CsiConfiguration csiConfiguration
        if(isUpdate){
            Map<String,Double> oldHourWeights = new HashMap<>()
            24.times { oldHourWeights.put("hour" + it + "Weight" , 1000)}
            CsiDay oldDay = CsiDay.build(oldHourWeights)
            csiConfiguration = CsiConfiguration.build(csiDay: oldDay)
        } else{
            csiConfiguration = CsiConfiguration.build()
        }

        when: "csv is imported"
        csvStream = new FileInputStream(csvFile)
        service.persistNewWeights(WeightFactor.HOUROFDAY, csvStream, csiConfiguration)

        then: "weights in CSI configuration match"
        csiConfiguration.csiDay == expectedCsiDay

        where:
        isUpdate | _
        false    | _
        true     | _
    }
}
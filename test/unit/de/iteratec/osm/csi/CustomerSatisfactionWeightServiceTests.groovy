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
import de.iteratec.osm.util.ServiceMocker
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import org.junit.Before
import org.junit.Test

/**
 * Test-suite of {@link CustomerSatisfactionWeightService}.
 */
@TestFor(CustomerSatisfactionWeightService)
@Mock([Page, Browser, HourOfDay, BrowserConnectivityWeight, ConnectivityProfile])
class CustomerSatisfactionWeightServiceTests {

    CustomerSatisfactionWeightService serviceUnderTest

    @Before
    void setUp() {
        serviceUnderTest = service
        ServiceMocker mocker = new ServiceMocker()
        mocker.mockI18nService(serviceUnderTest)

        createSomeBroserAndConnectivites()
    }

    @Test
    void testValidateWeightCsv() {

        // test correct files
        WeightFactor.each { weightCategory ->
            def csv = new File("test/resources/CsiData/${weightCategory}_weights.csv")
            InputStream csvStream = new FileInputStream(csv)
            List<String> errorMessages = serviceUnderTest.validateWeightCsv(weightCategory, csvStream)

            assertNotNull(errorMessages)
            assertEquals(0, errorMessages.size())
        }

        // test incorrect files
        File csvFalse = new File("test/resources/CsiData/BROWSER_weights_should_fail.csv")
        List<String> errorMessages = serviceUnderTest.validateWeightCsv(WeightFactor.BROWSER, new FileInputStream(csvFalse))
        assertNotNull(errorMessages)
        assertEquals(1, errorMessages.size())

        csvFalse = new File("test/resources/CsiData/PAGE_weights_should_fail.csv")
        errorMessages = serviceUnderTest.validateWeightCsv(WeightFactor.PAGE, new FileInputStream(csvFalse))
        assertNotNull(errorMessages)
        assertEquals(1, errorMessages.size())

        csvFalse = new File("test/resources/CsiData/HOUROFDAY_weights_should_fail.csv")
        errorMessages = serviceUnderTest.validateWeightCsv(WeightFactor.HOUROFDAY, new FileInputStream(csvFalse))
        assertNotNull(errorMessages)
        assertEquals(1, errorMessages.size())

        csvFalse = new File("test/resources/CsiData/HOUROFDAY_weights_should_fail_2.csv")
        errorMessages = serviceUnderTest.validateWeightCsv(WeightFactor.HOUROFDAY, new FileInputStream(csvFalse))
        assertNotNull(errorMessages)
        assertEquals(1, errorMessages.size())

        csvFalse = new File("test/resources/CsiData/BROWSER_CONNECTIVITY_COMBINATION_weights_should_fail.csv")
        errorMessages = serviceUnderTest.validateWeightCsv(WeightFactor.BROWSER_CONNECTIVITY_COMBINATION, new FileInputStream(csvFalse))
        assertNotNull(errorMessages)
        assertEquals(4, errorMessages.size())
    }

    @Test
    void testPersistNewWeights() {
        Integer browsersBeforeUpload = Browser.findAll().size()
        File csv = new File("test/resources/CsiData/BROWSER_weights.csv")
        InputStream csvStream = new FileInputStream(csv)
        serviceUnderTest.persistNewWeights(WeightFactor.BROWSER, csvStream)
        assertTrue(Browser.findAll().size() > browsersBeforeUpload)

        Integer pagesBeforeUpload = Page.findAll().size()
        csv = new File("test/resources/CsiData/PAGE_weights.csv")
        csvStream = new FileInputStream(csv)
        serviceUnderTest.persistNewWeights(WeightFactor.PAGE, csvStream)
        assertTrue(Page.findAll().size() > pagesBeforeUpload)

        Integer hoursofdayBeforeUpload = HourOfDay.findAll().size()
        csv = new File("test/resources/CsiData/HOUROFDAY_weights.csv")
        csvStream = new FileInputStream(csv)
        serviceUnderTest.persistNewWeights(WeightFactor.HOUROFDAY, csvStream)
        assertTrue(HourOfDay.findAll().size() > hoursofdayBeforeUpload)

        Integer browserConnectivityWeights = BrowserConnectivityWeight.findAll().size()
        csv = new File("test/resources/CsiData/BROWSER_CONNECTIVITY_COMBINATION_weights.csv")
        csvStream = new FileInputStream(csv)
        serviceUnderTest.persistNewWeights(WeightFactor.BROWSER_CONNECTIVITY_COMBINATION, csvStream)
        assertTrue(BrowserConnectivityWeight.findAll().size() > browserConnectivityWeights)
    }

    @Test
    void testUpdateWeights() {
        testPersistNewWeights()

        Integer browsersBeforeUpload = Browser.findAll().size()
        File csv = new File("test/resources/CsiData/BROWSER_weights.csv")
        InputStream csvStream = new FileInputStream(csv)
        serviceUnderTest.persistNewWeights(WeightFactor.BROWSER, csvStream)
        assertEquals(browsersBeforeUpload, Browser.findAll().size())

        Integer pagesBeforeUpload = Page.findAll().size()
        csv = new File("test/resources/CsiData/PAGE_weights.csv")
        csvStream = new FileInputStream(csv)
        serviceUnderTest.persistNewWeights(WeightFactor.PAGE, csvStream)
        assertEquals(pagesBeforeUpload, Page.findAll().size())

        Integer hoursofdayBeforeUpload = HourOfDay.findAll().size()
        csv = new File("test/resources/CsiData/HOUROFDAY_weights.csv")
        csvStream = new FileInputStream(csv)
        serviceUnderTest.persistNewWeights(WeightFactor.HOUROFDAY, csvStream)
        assertEquals(hoursofdayBeforeUpload, HourOfDay.findAll().size())

        Integer browserConnectivityWeights = BrowserConnectivityWeight.findAll().size()
        csv = new File("test/resources/CsiData/BROWSER_CONNECTIVITY_COMBINATION_weights.csv")
        csvStream = new FileInputStream(csv)
        serviceUnderTest.persistNewWeights(WeightFactor.BROWSER_CONNECTIVITY_COMBINATION, csvStream)
        assertEquals(browserConnectivityWeights, BrowserConnectivityWeight.findAll().size())
    }

    private void createSomeBroserAndConnectivites() {
        new Browser(name: "Browser1", weight: 0).save(failOnError: true)
        new Browser(name: "Browser2", weight: 0).save(failOnError: true)

        new ConnectivityProfile(name: "DSL1", active: true, bandwidthDown: 0, bandwidthUp: 0, latency: 0, packetLoss: 0).save(failOnError: true)
        new ConnectivityProfile(name: "DSL2", active: true, bandwidthDown: 0, bandwidthUp: 0, latency: 0, packetLoss: 0).save(failOnError: true)
    }
}

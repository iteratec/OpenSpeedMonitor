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

import static org.junit.Assert.assertTrue
import static org.junit.Assert.assertNotNull
import static org.junit.Assert.assertEquals

/**
 * Test-suite of {@link CustomerSatisfactionWeightService}.
 */
@TestFor(CustomerSatisfactionWeightService)
@Mock([Page, Browser, BrowserConnectivityWeight, ConnectivityProfile, DefaultTimeToCsMapping, CsiConfiguration, PageWeight, CsiDay])
class CustomerSatisfactionWeightServiceTests {
    CustomerSatisfactionWeightService serviceUnderTest

    CsiConfiguration csiConfiguration

    def doWithSpring = {
        i18nService(I18nService)
    }
    @Before
    void setUp() {
        serviceUnderTest = service
        ServiceMocker mocker = ServiceMocker.create()
        mocker.mockI18nService(serviceUnderTest)
        csiConfiguration = TestDataUtil.createCsiConfiguration()
        csiConfiguration.label = "conf1"

        createSomeBrowserAndConnectivitesAndPages()
    }

    @Test
    void testValidateWeightCsv() {

        // test correct files
        WeightFactor.each { weightCategory ->
            def csv = new File("src/test/resources/CsiData/${weightCategory}_weights.csv")
            InputStream csvStream = new FileInputStream(csv)
            List<String> errorMessages = serviceUnderTest.validateWeightCsv(weightCategory, csvStream)

            assertNotNull(errorMessages)
            assertEquals(0, errorMessages.size())
        }

        // test incorrect files
        File csvFalse = new File("src/test/resources/CsiData/BROWSER_weights_should_fail.csv")
        List<String> errorMessages = serviceUnderTest.validateWeightCsv(WeightFactor.BROWSER, new FileInputStream(csvFalse))
        assertNotNull(errorMessages)
        assertEquals(2, errorMessages.size())

        csvFalse = new File("src/test/resources/CsiData/PAGE_weights_should_fail.csv")
        errorMessages = serviceUnderTest.validateWeightCsv(WeightFactor.PAGE, new FileInputStream(csvFalse))
        assertNotNull(errorMessages)
        assertEquals(1, errorMessages.size())

        csvFalse = new File("src/test/resources/CsiData/HOUROFDAY_weights_should_fail.csv")
        errorMessages = serviceUnderTest.validateWeightCsv(WeightFactor.HOUROFDAY, new FileInputStream(csvFalse))
        assertNotNull(errorMessages)
        assertEquals(1, errorMessages.size())

        csvFalse = new File("src/test/resources/CsiData/HOUROFDAY_weights_should_fail_2.csv")
        errorMessages = serviceUnderTest.validateWeightCsv(WeightFactor.HOUROFDAY, new FileInputStream(csvFalse))
        assertNotNull(errorMessages)
        assertEquals(1, errorMessages.size())

        csvFalse = new File("src/test/resources/CsiData/BROWSER_CONNECTIVITY_COMBINATION_weights_should_fail.csv")
        errorMessages = serviceUnderTest.validateWeightCsv(WeightFactor.BROWSER_CONNECTIVITY_COMBINATION, new FileInputStream(csvFalse))
        assertNotNull(errorMessages)
        assertEquals(4, errorMessages.size())
    }

    @Test
    void testPersistNewWeights() {
        Integer browserConnectivityWeights = BrowserConnectivityWeight.findAll().size()
        File csv = new File("src/test/resources/CsiData/BROWSER_CONNECTIVITY_COMBINATION_weights.csv")
        InputStream csvStream = new FileInputStream(csv)
        serviceUnderTest.persistNewWeights(WeightFactor.BROWSER_CONNECTIVITY_COMBINATION, csvStream, csiConfiguration)
        assertTrue(BrowserConnectivityWeight.findAll().size() > browserConnectivityWeights)

        Integer pageWeightsBeforeUpload = PageWeight.findAll().size()
        csv = new File("src/test/resources/CsiData/PAGE_weights.csv")
        csvStream = new FileInputStream(csv)
        serviceUnderTest.persistNewWeights(WeightFactor.PAGE, csvStream, csiConfiguration)
        assertTrue(PageWeight.findAll().size() > pageWeightsBeforeUpload)

        Integer hoursofdayBeforeUpload = CsiDay.findAll().size()
        csv = new File("src/test/resources/CsiData/HOUROFDAY_weights.csv")
        csvStream = new FileInputStream(csv)
        serviceUnderTest.persistNewWeights(WeightFactor.HOUROFDAY, csvStream, csiConfiguration)
        assertTrue(CsiDay.findAll().size() > hoursofdayBeforeUpload)
    }

    @Test
    void testUpdateWeights() {
        Integer browserConnectivityWeights = BrowserConnectivityWeight.findAll().size()
        File csv = new File("src/test/resources/CsiData/BROWSER_CONNECTIVITY_COMBINATION_weights.csv")
        InputStream csvStream = new FileInputStream(csv)
        serviceUnderTest.persistNewWeights(WeightFactor.BROWSER_CONNECTIVITY_COMBINATION, csvStream, csiConfiguration)
        assertTrue(BrowserConnectivityWeight.findAll().size() > browserConnectivityWeights)

        Integer pageWeightsBeforeUpload = PageWeight.findAll().size()
        csv = new File("src/test/resources/CsiData/PAGE_weights.csv")
        csvStream = new FileInputStream(csv)
        serviceUnderTest.persistNewWeights(WeightFactor.PAGE, csvStream, csiConfiguration)
        assertTrue(PageWeight.findAll().size() > pageWeightsBeforeUpload)

        Integer hoursofdayBeforeUpload = CsiDay.findAll().size()
        csv = new File("src/test/resources/CsiData/HOUROFDAY_weights.csv")
        csvStream = new FileInputStream(csv)
        serviceUnderTest.persistNewWeights(WeightFactor.HOUROFDAY, csvStream, csiConfiguration)
        assertTrue(CsiDay.findAll().size() > hoursofdayBeforeUpload)
    }

    @Test
    void testPersistMappingCsv() {
        int defaultMappingSizeBeforePersist = DefaultTimeToCsMapping.findAll().size()
        File csv = new File("src/test/resources/CsiData/DefaultMappings.csv")
        InputStream csvStream = new FileInputStream(csv)
        serviceUnderTest.persistNewDefaultMapping(csvStream);

        assertTrue(DefaultTimeToCsMapping.findAll().size() > defaultMappingSizeBeforePersist)
    }

    private void createSomeBrowserAndConnectivitesAndPages() {
        new Browser(name: "Browser1").save(failOnError: true)
        new Browser(name: "Browser2").save(failOnError: true)

        new ConnectivityProfile(name: "DSL1", active: true, bandwidthDown: 0, bandwidthUp: 0, latency: 0, packetLoss: 0).save(failOnError: true)
        new ConnectivityProfile(name: "DSL2", active: true, bandwidthDown: 0, bandwidthUp: 0, latency: 0, packetLoss: 0).save(failOnError: true)

        TestDataUtil.createPages(['HP','MES','SE','ADS','WKBS','WK'])
    }
}

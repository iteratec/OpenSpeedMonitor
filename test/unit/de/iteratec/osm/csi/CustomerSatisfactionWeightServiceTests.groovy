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

import grails.test.mixin.*

import org.junit.Test
import de.iteratec.osm.csi.weighting.WeightFactor
import de.iteratec.osm.measurement.environment.Browser

/**
 * Test-suite of {@link CustomerSatisfactionWeightService}.
 */
@TestFor(CustomerSatisfactionWeightService)
@Mock([Page, Browser, HourOfDay])
class CustomerSatisfactionWeightServiceTests {

	CustomerSatisfactionWeightService serviceUnderTest

	@Before
	void setUp() {
		serviceUnderTest=service
	}

	@Test
	void testValidateWeightCsv() {

		WeightFactor.each{weightCategory ->
			def csv = new File("test/resources/CsiData/${weightCategory}_weights.csv")
			InputStream csvStream = new FileInputStream(csv)
			List<String> errorMessages = serviceUnderTest.validateWeightCsv(weightCategory, csvStream)

			assertNotNull( errorMessages)
			assertEquals(0, errorMessages.size())
		}

		File csvFalse = new File("test/resources/CsiData/BROWSER_weights_should_fail.csv")
		List<String> errorMessages = serviceUnderTest.validateWeightCsv(WeightFactor.BROWSER, new FileInputStream(csvFalse))
		assertNotNull(errorMessages)
		assertEquals(2, errorMessages.size())
		assertTrue(errorMessages.contains("Header-Zeile enthält weniger oder mehr als 2 Spaltenüberschriften!"))
		assertTrue(errorMessages.contains("Nicht alle Datenzeilen der CSV-Datei entsprechen dem Format [String;Double]!\n"+
				"Zahlenwerte bitte im amerikanischen Format eingeben."))

		csvFalse = new File("test/resources/CsiData/PAGE_weights_should_fail.csv")
		errorMessages = serviceUnderTest.validateWeightCsv(WeightFactor.PAGE, new FileInputStream(csvFalse))
		assertNotNull(errorMessages)
		assertEquals(1, errorMessages.size())
		assertEquals("Nicht alle Datenzeilen der CSV-Datei entsprechen dem Format [String;Double]!\n"+
				"Zahlenwerte bitte im amerikanischen Format eingeben.", errorMessages[0])

		csvFalse = new File("test/resources/CsiData/HOUROFDAY_weights_should_fail.csv")
		errorMessages = serviceUnderTest.validateWeightCsv(WeightFactor.HOUROFDAY, new FileInputStream(csvFalse))
		assertNotNull(errorMessages)
		assertEquals(1, errorMessages.size())
		assertEquals("Die CSV-Datei enthält nicht für alle 24 Stunden des Tages eine Datenzeile!" ,errorMessages[0])

		csvFalse = new File("test/resources/CsiData/HOUROFDAY_weights_should_fail_2.csv")
		errorMessages = serviceUnderTest.validateWeightCsv(WeightFactor.HOUROFDAY, new FileInputStream(csvFalse))
		assertNotNull(errorMessages)
		assertEquals(1, errorMessages.size())
		assertEquals("Die CSV-Datei enthält nicht für alle 24 Stunden des Tages eine Datenzeile!", errorMessages[0])
	}

	@Test
	void testPersistNewWeights() {
		Integer browsersBeforeUpload = Browser.findAll().size()
		File csv = new File("test/resources/CsiData/BROWSER_weights.csv")
		InputStream csvStream = new FileInputStream(csv)
		serviceUnderTest.persistNewWeights(WeightFactor.BROWSER, csvStream)
		assertTrue( Browser.findAll().size() > browsersBeforeUpload)

		Integer pagesBeforeUpload = Page.findAll().size()
		csv = new File("test/resources/CsiData/PAGE_weights.csv")
		csvStream = new FileInputStream(csv)
		serviceUnderTest.persistNewWeights(WeightFactor.PAGE, csvStream)
		assertTrue(Page.findAll().size() > pagesBeforeUpload)

		Integer hoursofdayBeforeUpload = HourOfDay.findAll().size()
		csv = new File("test/resources/CsiData/HOUROFDAY_weights.csv")
		csvStream = new FileInputStream(csv)
		serviceUnderTest.persistNewWeights(WeightFactor.HOUROFDAY, csvStream)
		assertTrue( HourOfDay.findAll().size() > hoursofdayBeforeUpload)
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
	}
}

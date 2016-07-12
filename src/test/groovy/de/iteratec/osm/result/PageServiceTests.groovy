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

package de.iteratec.osm.result

import de.iteratec.osm.csi.Page
import org.junit.*
import static org.junit.Assert.*

import grails.test.mixin.*

/**
 * Test-suite for {@link PageServiceTests}.
 */
@TestFor(PageService)
@Mock([Page,MeasuredEvent])
class PageServiceTests {

	static final Integer pageCount = 7
	PageService serviceUnderTest

	@Before
	void setUp() {
		serviceUnderTest = service

		createPages()
		createMeasuredEvents()
	}

	@Test
	void testGetCachedPageMap() {
		Map<Long, Page> dataToProof = serviceUnderTest.getCachedPageMap()
		assertEquals(pageCount, dataToProof.size())

		// Map may not contain a null or a value without name
		for(Map.Entry<Long, Page> eachPage : dataToProof)
		{
			assertNotNull eachPage.key
			assertNotNull eachPage.value
			assertNotNull eachPage.value.name
		}
	}

	@Test
	void testGetPageMap() {
		Map<Long, Page> dataToProof = serviceUnderTest.getPageMap()
		assertEquals(pageCount, dataToProof.size())

		// Map may not contain a null or a value without name
		for(Map.Entry<Long, Page> eachPage : dataToProof)
		{
			assertNotNull eachPage.key
			assertNotNull eachPage.value
			assertNotNull eachPage.value.name
		}
	}

	@Test
	void testGetPageNameFromStepName(){
		final String stepName_HP = 'HP:::BV1_IE_homepage'
		final String stepName_MES = 'MES:::BV1_FF_Moduleinstieg'
		final String stepName_unnkownPage = 'XFXHVXGT:::BV1_FF_Moduleinstieg'
		final String stepName_noPage = 'BV1_FF_Moduleinstieg'

		assertEquals('HP', serviceUnderTest.getPageNameFromStepName(stepName_HP))
		assertEquals('MES', serviceUnderTest.getPageNameFromStepName(stepName_MES))
		assertEquals('XFXHVXGT', serviceUnderTest.getPageNameFromStepName(stepName_unnkownPage))
		assertEquals(Page.UNDEFINED, serviceUnderTest.getPageNameFromStepName(stepName_noPage))
		assertEquals('HP', serviceUnderTest.getPageNameFromStepName('HP:::LH_Homepage'))
	}

	@Test
	void testGetPageByStepName(){
		final String stepName_HP = 'HP:::BV1_IE_homepage'
		final String stepName_MES = 'MES:::BV1_FF_Moduleinstieg'
		final String stepName_unnkownPage = 'XFXHVXGT:::BV1_FF_Moduleinstieg'
		final String stepName_noPage = 'BV1_FF_Moduleinstieg'

		final Page hp = Page.findByName('HP')
		final Page mes = Page.findByName('MES')
		final Page undefined = Page.findByName(Page.UNDEFINED)

		assertEquals(hp, serviceUnderTest.getPageByStepName(stepName_HP))
		assertEquals(mes, serviceUnderTest.getPageByStepName(stepName_MES))
		assertEquals(undefined, serviceUnderTest.getPageByStepName(stepName_unnkownPage))
		assertEquals(undefined, serviceUnderTest.getPageByStepName(stepName_noPage))
		assertEquals(hp, serviceUnderTest.getPageByStepName('HP:::LH_Homepage'))
	}
	
	@Test
	void testExcludePagenamePart(){
		
		final String correctDelimitter = PageService.STEPNAME_DELIMITTER
		final String incorrectDelimitter = correctDelimitter + 'incorrect'
		final String stepNameWithoutPage = 'step' 
		final String stepName_withPage = "HP${correctDelimitter}${stepNameWithoutPage}"
		final String stepName_withIncorrectDelimittedPage = 'MES${incorrectDelimitter}${stepName}'
		
		assertEquals(stepNameWithoutPage, serviceUnderTest.excludePagenamePart(stepNameWithoutPage))
		assertEquals(stepNameWithoutPage, serviceUnderTest.excludePagenamePart(stepName_withPage))
		assertEquals(stepName_withIncorrectDelimittedPage, serviceUnderTest.excludePagenamePart(stepName_withIncorrectDelimittedPage))
		
		assertEquals('LH_Homepage', serviceUnderTest.excludePagenamePart('HP:::LH_Homepage'))
		
	}

	private createPages(){
		new Page(name: 'HP', weight: 6.0d).save(failOnError: true)
		new Page(name: 'MES', weight: 9.0d).save(failOnError: true)
		new Page(name: 'SE', weight: 36.0d).save(failOnError: true)
		new Page(name: 'ADS', weight: 43.0d).save(failOnError: true)
		new Page(name: 'WKBS', weight: 3.0d).save(failOnError: true)
		new Page(name: 'WK', weight: 3.0d).save(failOnError: true)
		new Page(name: Page.UNDEFINED, weight: 0.0d).save(failOnError: true)
	}

	private createMeasuredEvents() {
		String stepName_HP = 'HP:::BV1_IE_homepage'
		new MeasuredEvent(
				name: stepName_HP,
				testedPage: Page.findByName('HP')
				).save(failOnError: true)

		String stepName_MES = 'MES:::BV1_FF_Moduleinstieg'
		new MeasuredEvent(
				name: stepName_MES,
				testedPage: Page.findByName('MES')
				).save(failOnError: true)
	}
}

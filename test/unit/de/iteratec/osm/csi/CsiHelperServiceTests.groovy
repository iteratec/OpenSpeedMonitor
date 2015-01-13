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

import static org.hamcrest.Matchers.*
import static org.junit.Assert.assertThat
import grails.test.mixin.TestFor

import org.junit.Before
import org.junit.Test

import de.iteratec.osm.ConfigService
import de.iteratec.osm.util.I18nService
import de.iteratec.osm.util.MethodToMock
import de.iteratec.osm.util.OsmCookieService
import de.iteratec.osm.util.ServiceMocker

/**
 * Test-suite of {@link CsiHelperService}.
 */
@TestFor(CsiHelperService)
class CsiHelperServiceTests {
	
	CsiHelperService serviceUnderTest
	ServiceMocker mocker
	
	@Before
	void setUp() {
		serviceUnderTest=service
		mocker = ServiceMocker.create()
	}

	@Test
	void testGetDefaultCsiChartTitle(){
		
		//test specific data
		final String expectedDefaultTitleFromCookie = 'Customer satisfaction index (CSI) - cookie'
		final String expectedMainUrlUnderTest = 'www.example.com'
		
		//test specific mocks
		mocker.mockService(
				ConfigService.class,
				serviceUnderTest,
				[new MethodToMock(method: ConfigService.getMethod('getMainUrlUnderTest'), toReturn: expectedMainUrlUnderTest)])
		mocker.mockService(
				OsmCookieService.class,
				serviceUnderTest,
				[new MethodToMock(method: OsmCookieService.getMethod('getBase64DecodedCookieValue', String.class), toReturn: expectedDefaultTitleFromCookie)])
		
		//test execution and assertions
		assertThat(serviceUnderTest.getCsiChartDefaultTitle(), is("${expectedDefaultTitleFromCookie} ${expectedMainUrlUnderTest}".toString()))
	}
	@Test
	void testGetDefaultCsiChartTitleWithNullAsMainUrlUnderTest(){
		
		//test specific data
		final String expectedDefaultTitleFromCookie = 'Customer satisfaction index (CSI) - cookie'
		final String expectedDefaultTitleFromI18n = 'Customer satisfaction index (CSI) - i18n'
		final String expectedMainUrlUnderTest = 'www.example.com'
		
		//test specific mocks
		mocker.mockService(
				ConfigService.class,
				serviceUnderTest,
				[new MethodToMock(method: ConfigService.getMethod('getMainUrlUnderTest'), toReturn: null)])
		mocker.mockService(
				OsmCookieService.class,
				serviceUnderTest,
				[new MethodToMock(method: OsmCookieService.getMethod('getBase64DecodedCookieValue', String.class), toReturn: expectedDefaultTitleFromCookie)])
		
		//test execution and assertions
		assertThat(serviceUnderTest.getCsiChartDefaultTitle(), is("${expectedDefaultTitleFromCookie}".toString()))
	}
	@Test
	void testGetDefaultCsiChartTitleWithNullAsDefaultTitleFromCookie(){
		
		//test specific data
		final String expectedDefaultTitleFromCookie = 'Customer satisfaction index (CSI) - cookie'
		final String expectedDefaultTitleFromI18n = 'Customer satisfaction index (CSI) - i18n'
		final String expectedMainUrlUnderTest = 'www.example.com'
		
		//test specific mocks
		mocker.mockService(
				ConfigService.class,
				serviceUnderTest,
				[new MethodToMock(method: ConfigService.getMethod('getMainUrlUnderTest'), toReturn: expectedMainUrlUnderTest)])
		mocker.mockService(
				OsmCookieService.class,
				serviceUnderTest,
				[new MethodToMock(method: OsmCookieService.getMethod('getBase64DecodedCookieValue', String.class), toReturn: null)])
		mocker.mockService(
			I18nService.class, 
			serviceUnderTest,
			[new MethodToMock(method: I18nService.getMethod('msg', String.class, String.class), toReturn: expectedDefaultTitleFromI18n)])
		//test execution and assertions
		assertThat(serviceUnderTest.getCsiChartDefaultTitle(), is("${expectedDefaultTitleFromI18n} ${expectedMainUrlUnderTest}".toString()))
	}
	@Test
	void testGetDefaultCsiChartTitleWithNullAsDefaultTitleFromCookieAndMainUrlUnderTest(){
		
		//test specific data
		final String expectedDefaultTitleFromCookie = 'Customer satisfaction index (CSI) - cookie'
		final String expectedDefaultTitleFromI18n = 'Customer satisfaction index (CSI) - i18n'
		final String expectedMainUrlUnderTest = 'www.example.com'
		
		//test specific mocks
		mocker.mockService(
				ConfigService.class,
				serviceUnderTest,
				[new MethodToMock(method: ConfigService.getMethod('getMainUrlUnderTest'), toReturn: null)])
		mocker.mockService(
				OsmCookieService.class,
				serviceUnderTest,
				[new MethodToMock(method: OsmCookieService.getMethod('getBase64DecodedCookieValue', String.class), toReturn: null)])
		mocker.mockService(
			I18nService.class,
			serviceUnderTest,
			[new MethodToMock(method: I18nService.getMethod('msg', String.class, String.class), toReturn: expectedDefaultTitleFromI18n)])
		//test execution and assertions
		assertThat(serviceUnderTest.getCsiChartDefaultTitle(), is(expectedDefaultTitleFromI18n))
	}
}

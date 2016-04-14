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

package de.iteratec.osm.measurement.environment

import grails.test.mixin.*

import org.junit.*
import spock.lang.Specification

/**
 * Test-suite for {@link BrowserService}.
 */
@TestFor(BrowserService)
@Mock([Browser, BrowserAlias])
class BrowserServiceTests extends Specification{

	static final String nonExistentBrowserName = "myBrowsername"
	static final String nonExistentBrowserAlias = "myBrowseralias"
	static final String existentBrowserName = "IE"
	static final String existentBrowserAlias = "Internet Explorer"
	static final int browserCount = 3

	BrowserService serviceUnderTest

	void "setup"(){
		serviceUnderTest = service

		// Test data
		//undefined browser
		String browserName="undefined"
		new Browser(
				name: browserName,
				weight: 0)
				.addToBrowserAliases(alias: "undefined")
				.save(failOnError: true)

		//IE
		browserName="IE"
		new Browser(
				name: browserName,
				weight: 45)
				.addToBrowserAliases(alias: "IE")
				.addToBrowserAliases(alias: "IE8")
				.addToBrowserAliases(alias: "Internet Explorer")
				.addToBrowserAliases(alias: "Internet Explorer 8")
				.save(failOnError: true)

		//FF
		browserName="Firefox"
		new Browser(
				name: browserName,
				weight: 55)
				.addToBrowserAliases(alias: "FF")
				.addToBrowserAliases(alias: "FF7")
				.addToBrowserAliases(alias: "Firefox")
				.addToBrowserAliases(alias: "Firefox7")
				.save(failOnError: true)
	}
	
	void "test Find By Name Or Alias"() {
		when:
		Browser existentName = serviceUnderTest.findByNameOrAlias(existentBrowserName)
		Browser nonExistentName = serviceUnderTest.findByNameOrAlias(nonExistentBrowserName)
		Browser existentAlias= serviceUnderTest.findByNameOrAlias(existentBrowserAlias)
		Browser nonExistentAlias = serviceUnderTest.findByNameOrAlias(nonExistentBrowserAlias)

		then:
		existentName != null
		nonExistentName != null
		existentAlias != null
		nonExistentAlias != null

		existentName.name == existentBrowserName
		nonExistentName.name == "undefined"
		existentAlias.name == existentBrowserName
		nonExistentAlias.name == "undefined"
	}

	void "test get cached browser map"() {
		when:
		Map<Long, Browser> dataToProve = serviceUnderTest.getCachedBrowserMap()

		then:
		dataToProve.size() == browserCount

		// Check if for all browsers relevant data was placed instead of null
		for(Map.Entry<Long, Browser> eachEntry : dataToProve.entrySet())
		{
			eachEntry.key != null
			eachEntry.value != null
			eachEntry.value.name != null
		}

		// subsequent call should return equal result:
		Map<Long, Browser> subsequentRetrievedData = serviceUnderTest.getCachedBrowserMap()
		subsequentRetrievedData == dataToProve
	}

	void "test getBrowserMap"() {
		when:
		Map<Long, Browser> dataToProve = serviceUnderTest.getBrowserMap()

		then:
		dataToProve.size() == browserCount

		// Check if for all browsers relevant data was placed instead of null
		for(Map.Entry<Long, Browser> eachEntry : dataToProve.entrySet())
		{
			eachEntry.key != null
			eachEntry.value != null
			eachEntry.value.name != null
		}

		// subsequent call should return equal result:
		Map<Long, Browser> subsequentRetrievedData = serviceUnderTest.getBrowserMap()
		subsequentRetrievedData == dataToProve
	}
}

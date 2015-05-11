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

package de.iteratec.osm.api

import de.iteratec.osm.filters.SecureApiFunctionsFilters
import de.iteratec.osm.measurement.schedule.Job
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import spock.lang.Specification

/**
 * Test-suite of {@link RestApiController}.
 *
 * @author mze
 * @since IT-81
 */
@TestFor(RestApiController)
@Mock([SecureApiFunctionsFilters, Job, ApiKey])
class SecuredRestApiSpec extends Specification {

	private RestApiController controllerUnderTest

	void setup(){
		controllerUnderTest = controller
	}

	void "test securing action method - should fail cause of missing apiKey param"() {
		when:
		withFilters(action:"securedViaApiKeyActivateJob") {
			controllerUnderTest.securedViaApiKeyActivateJob()
		}

		then:
		response.status == 400
		response.contentAsString.equals("This api function requires an apiKey with respected permission. You " +
				"have to submit this key as param 'apiKey'.")
	}
	void "test securing action method - should fail cause submitted apiKey doesn't exist"() {
		when:
		params.apiKey = 'missingApiKey'
		withFilters(action:"securedViaApiKeyDeactivateJob") {
			controllerUnderTest.securedViaApiKeyDeactivateJob()
		}

		then:
		response.status == 404
		response.contentAsString.equals("The submitted apiKey doesn't exist.")
	}
	void "test securing action method - should fail cause submitted apiKey is invalid"() {
		setup:
		String existingButInvalidKey = 'keyvalue'
		new ApiKey(secretKey: existingButInvalidKey, valid: false).save(failOnError: true)

		when:
		params.apiKey = existingButInvalidKey
		withFilters(action:"securedViaApiKeySetExecutionSchedule") {
			controllerUnderTest.securedViaApiKeySetExecutionSchedule()
		}

		then:
		response.status == 403
		response.contentAsString.equals("The submitted apiKey is invalid.")
	}
	void "test creation of event"(){
		setup:
		String existingValidAndAllowed = 'keyvalue'
		new ApiKey(secretKey: existingValidAndAllowed, valid: true, allowedForCreateEvent: true).save(failOnError: true)

		when:
		params.validApiKey = 'my-valid-key'
		params.shortName = 'my-event'
		params.system = ['JobGroup1', 'JobGroup1']
		params.eventDate = new DateTime(2015,5,8,0,0,0, DateTimeZone.UTC)
		params.eventTime = '11:00'
		params.htmlDescription = 'description'
		params.globallyVisible = false
		withFilters(action:"securedViaApiKeyCreateEvent") {
			controllerUnderTest.securedViaApiKeyCreateEvent()
		}

		then:
		response.text == '{"book":"Great"}'
		response.json.book == 'Great'
	}
}

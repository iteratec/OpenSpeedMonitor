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

/**
 * Used to secure api functions. Contains one boolean for each api function or group of functions that has to be secured.
 * These booleans should only be used directly
 * @author nkuhn
 * @see ApiSecurityService
 */
class ApiKey {

	String secretKey
	Boolean valid = true
	Boolean allowedForJobActivation = false
	Boolean allowedForJobDeactivation = false
    Boolean allowedForJobSetExecutionSchedule = false
    Boolean allowedForCreateEvent = false

    static mapping = {
    }

	static constraints = {
		secretKey(nullable: false, blank: false)
		valid(nullable: false, defaultValue: true)
		allowedForJobActivation(nullable: false, defaultValue: false)
		allowedForJobDeactivation(nullable: false, defaultValue: false)
        allowedForJobSetExecutionSchedule(nullable: false, defaultValue: false)
        allowedForCreateEvent(nullable: false, defaultValue: false)
    }

}

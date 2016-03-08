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

package de.iteratec.osm.result.detail

/**
 * Asset
 * Representation of one single request.
 */
class Asset {

	/**
	 * Compare values
	 */
	int bytesIn
	int bytesOut
	int connectTimeMs
	int downloadTimeMs
	int loadTimeMs
	int timeToFirstByteMs
	//Resource order for one page call
	int indexWithinHar
	int sslNegotiationTimeMs

	/**
	 * Grouping values
	 */
	String contentType
	String fullURL
	String host
	String mediaType
	String subtype
	String urlWithoutParams

	static mapWith = 'mongo'
    static mapping = {
		bytesIn defaultVaule:-1
		bytesOut defaultVaule:-1
		connectTimeMs defaultVaule:-1
		downloadTimeMs defaultVaule:-1
		indexWithinHar defaultVaule:-1
		loadTimeMs defaultVaule:-1
		sslNegotiationTimeMs defaultVaule:-1
		timeToFirstByteMs defaultVaule:-1
		contentType defaultVaule: "undefined"
		fullURL defaultVaule: "undefined"
		host defaultVaule: "undefined"
		mediaType defaultVaule: "undefined"
		subtype defaultVaule: "undefined"
		urlWithoutParams defaultVaule: "undefined"
    }



	static constraints = {
		contentType nullable: true
		fullURL nullable: true
		host nullable: true
		urlWithoutParams nullable: true
		mediaType nullable: true
		subtype nullable: true
	}
}

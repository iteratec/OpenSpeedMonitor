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
 * WaterfallEntry
 * A domain class describes the data object and it's mapping to the database
 */
class WaterfallEntry {

	Date	dateCreated
	Date	lastUpdated
	
	static belongsTo = WebPerformanceWaterfall
	
	Boolean blocked = false
	Integer httpStatus
	String path
	String host
	String mimeType
	Integer startOffset
	Integer oneBasedIndexInWaterfall
	
	Integer dnsLookupTimeStartInMillisecs
	Integer initialConnectTimeStartInMillisecs
	Integer sslNegotationTimeStartInMillisecs
	Integer timeToFirstByteStartInMillisecs
	Integer downloadTimeStartInMillisecs
	Integer dnsLookupTimeEndInMillisecs
	Integer initialConnectTimeEndInMillisecs
	Integer sslNegotationTimeEndInMillisecs
	Integer timeToFirstByteEndInMillisecs
	Integer downloadTimeEndInMillisecs
	Integer downloadedBytes
	Integer uploadedBytes
	
    static mapping = {
		path(type: 'text')
    }
    
	static constraints = {
		blocked()
		httpStatus()
		path()
		host()
		mimeType()
		startOffset()
		oneBasedIndexInWaterfall()
		dnsLookupTimeStartInMillisecs()
		initialConnectTimeStartInMillisecs()
		sslNegotationTimeStartInMillisecs()
		timeToFirstByteStartInMillisecs()
		downloadTimeStartInMillisecs()
		dnsLookupTimeEndInMillisecs()
		initialConnectTimeEndInMillisecs()
		sslNegotationTimeEndInMillisecs()
		timeToFirstByteEndInMillisecs()
		downloadTimeEndInMillisecs()
		downloadedBytes()
		uploadedBytes()
    }
	
	static transients = [
		'dnsLookupTimeInMillisecs',
		'initialConnectTimeInMillisecs',
		'sslNegotationTimeInMillisecs',
		'timeToFirstByteInMillisecs',
		'downloadTimeInMillisecs']
	
	/*
	 * Methods of the Domain Class
	 */
	Integer getDnsLookupTimeInMillisecs() {
		return dnsLookupTimeEndInMillisecs - dnsLookupTimeStartInMillisecs
	}
	Integer getInitialConnectTimeInMillisecs() {
		return initialConnectTimeEndInMillisecs - initialConnectTimeStartInMillisecs
	}
	Integer getSslNegotationTimeInMillisecs() {
		return sslNegotationTimeEndInMillisecs - sslNegotationTimeStartInMillisecs
	}
	Integer getTimeToFirstByteInMillisecs() {
		return timeToFirstByteEndInMillisecs - timeToFirstByteStartInMillisecs
	}
	Integer getDownloadTimeInMillisecs() {
		return downloadTimeEndInMillisecs - downloadTimeStartInMillisecs
	}
}

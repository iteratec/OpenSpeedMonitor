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



import grails.test.mixin.*

import org.junit.*

/**
 * See the API for {@link grails.test.mixin.domain.DomainClassUnitTestMixin} for usage instructions
 */
@TestFor(WaterfallEntry)
@Mock([WaterfallEntry])
class WaterfallEntrySpec {
	void setUp(){
		createTestDataCommonToAllTests()
	}
	void createTestDataCommonToAllTests(){
		new WaterfallEntry(
			dnsLookupTimeStartInMillisecs: 0,
			dnsLookupTimeEndInMillisecs: 100,
			initialConnectTimeStartInMillisecs: 200,
			initialConnectTimeEndInMillisecs: 400,
			sslNegotationTimeStartInMillisecs: 400,
			sslNegotationTimeEndInMillisecs: 700,
			timeToFirstByteStartInMillisecs: 800,
			timeToFirstByteEndInMillisecs: 1000,
			downloadTimeStartInMillisecs: 1000,
			downloadTimeEndInMillisecs: 1900,
			httpStatus: 200,
			path: '/valid',
			host: 'my.host.de',
			mimeType: 'text/javascript',
			startOffset: 0,
			downloadedBytes: 250,
			uploadedBytes: 0,
			oneBasedIndexInWaterfall: 1
			).save()
		new WaterfallEntry(
			dnsLookupTimeStartInMillisecs: 0,
			dnsLookupTimeEndInMillisecs: 100,
			initialConnectTimeStartInMillisecs: 200,
			initialConnectTimeEndInMillisecs: 400,
			sslNegotationTimeStartInMillisecs: 400,
			sslNegotationTimeEndInMillisecs: 700,
			timeToFirstByteStartInMillisecs: 600,
			timeToFirstByteEndInMillisecs: 1000,
			downloadTimeStartInMillisecs: 1000,
			downloadTimeEndInMillisecs: 1900,
			httpStatus: 200,
			path: '/invalid/ttfbStartsBeforeSslEnds',
			host: 'my.host.de',
			mimeType: 'text/javascript',
			startOffset: 0,
			downloadedBytes: 250,
			uploadedBytes: 0,
			oneBasedIndexInWaterfall: 1
			).save()
	}

    void testCalculationOfSingleTimes() {
		//test-specific data
		WaterfallEntry validEntry = WaterfallEntry.findByPath('/valid')
		//test-execution and assertions
		assertEquals(100, validEntry.getDnsLookupTimeInMillisecs())
		assertEquals(200, validEntry.getInitialConnectTimeInMillisecs())
		assertEquals(300, validEntry.getSslNegotationTimeInMillisecs())
		assertEquals(200, validEntry.getTimeToFirstByteInMillisecs())
		assertEquals(900, validEntry.getDownloadTimeInMillisecs())
    }
	void testConstraints() {
		//test-specific data
		WaterfallEntry validEntry = WaterfallEntry.findByPath('/valid')
		WaterfallEntry invalidEntry_ttfbStartsBeforeSslEnds = WaterfallEntry.findByPath('/invalid/ttfbStartsBeforeSslEnds')
		// Mocking domain-class for constraints
		mockForConstraintsTests(WaterfallEntry)
		//test-execution and assertions
		assertTrue(validEntry.validate())
		
		//TODO: constraints like the following should be implemented
//		assertFalse( invalidEntry_ttfbStartsBeforeSslEnds.validate() );
//		assertEquals( "min", invalidEntry_ttfbStartsBeforeSslEnds.errors["timeToFirstByteStartInMillisecs"] );
	}
}

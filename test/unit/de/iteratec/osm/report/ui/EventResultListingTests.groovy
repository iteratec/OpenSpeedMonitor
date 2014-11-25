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

package de.iteratec.osm.report.ui

import static org.junit.Assert.*;

import org.junit.Test;

import de.iteratec.osm.csi.Page;
import de.iteratec.osm.result.CachedView;
import de.iteratec.osm.result.EventResult;
import de.iteratec.osm.result.JobResult;
import de.iteratec.osm.result.MeasuredEvent;

/**
 * Test-suite of {@link de.iteratec.osm.report.ui.EventResultListing}.
 * 
 * @author mze
 * @since IT-106
 */
class EventResultListingTests {
	
	@Test
	public void testDesign()
	{
		// Create some data:
		EventResult eventResult = new EventResult();
		eventResult.firstByteInMillisecs = 10; 
		eventResult.startRenderInMillisecs = 11;
		eventResult.domTimeInMillisecs = 12;
		eventResult.docCompleteIncomingBytes = 13;
		eventResult.docCompleteRequests = 14;
		eventResult.docCompleteTimeInMillisecs = 15;
		eventResult.fullyLoadedIncomingBytes = 16;
		eventResult.fullyLoadedRequestCount = 17;
		eventResult.fullyLoadedTimeInMillisecs = 18;
		eventResult.cachedView = CachedView.CACHED;
		
		MeasuredEvent measuredEvent = new MeasuredEvent();
		measuredEvent.name = 'HelloEvent';
		measuredEvent.testedPage = new Page(name:'HelloPage');
		eventResult.measuredEvent = measuredEvent;
		
		JobResult jobResult = new JobResult();
		jobResult.date = new Date(1383574075000L); // 04.11.2013 - 15:07:55
		jobResult.jobConfigLabel = 'HelloLabel';
		
		// A row: 
		EventResultListingRow row1 = new EventResultListingRow(jobResult, eventResult); 
		
		// Run tests:
		EventResultListing out = new EventResultListing();
		
		assertTrue(out.getRows().isEmpty());
		assertTrue(out.isEmpty());
		
		out.addRow(row1);
		
		assertFalse(out.getRows().isEmpty());
		assertFalse(out.isEmpty());
		assertEquals(1, out.getRows().size());
		
		// A second row (we don't care about the equal content here):
		EventResultListingRow row2 = new EventResultListingRow(jobResult, eventResult);
		
		out.addRow(row2);
		
		assertFalse(out.getRows().isEmpty());
		assertFalse(out.isEmpty());
		assertEquals(2, out.getRows().size());
	}
}

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

import de.iteratec.osm.report.ui.PaginationListing
import de.iteratec.osm.report.ui.PaginationListingRow

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * Test-suite of {@link PaginationListing}.
 * 
 * @author rhc
 */
class PaginationListingTest {
	
	@Test
	public void testDesign(){
		//A row
		PaginationListingRow row1 = new PaginationListingRow(1, "www.example.de/params")
		
		//Run tests
		PaginationListing out = new PaginationListing()
		
		assertTrue(out.getRows().isEmpty())
		assertTrue(out.isEmpty())
		
		out.addRow(row1, 0, 10, 100, "www.example.de/paramsPreviousLink", "www.example.de/paramsNextLink")
		
		assertFalse(out.getRows().isEmpty())
		assertFalse(out.isEmpty())
		assertEquals(1, out.getRows().size())
		
		//A second row
		PaginationListingRow row2 = new PaginationListingRow(1, "www.example.de/params")
		
		out.addRow(row2, 0, 10, 100, "www.example.de/paramsPreviousLink", "www.example.de/paramsNextLink")
		
		assertFalse(out.getRows().isEmpty());
		assertFalse(out.isEmpty());
		assertEquals(2, out.getRows().size());
	}
}

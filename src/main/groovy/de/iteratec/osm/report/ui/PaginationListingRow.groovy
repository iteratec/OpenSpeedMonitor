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

/**
 * <p>
 * Visualization of one page buttons for pagination.
 * </p>
 * 
 * <p>
 * Objects of this class intended to be unmodifiable
 * </p>
 * 
 * @author rhc
 *
 */
class PaginationListingRow {
	
	/**
	 * <p>
	 * Creates a new row.
	 * </p>
	 * 
	 * @param pageNumber
	 * 			The {@link Integer} of shown page number at pagination
	 * @param pageLink
	 * 			The link as a {@link String} of pagination,
	 * 			offset and max must be contained
	 */
	public PaginationListingRow(Integer pageNumber, String pageLink){
		this.pageNumber = pageNumber
		this.pageLink = pageLink
	}
	
	public final Integer pageNumber
	
	public final String pageLink
}

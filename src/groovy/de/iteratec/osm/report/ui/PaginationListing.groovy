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
 * Visualization of a list of page buttons for pagination.
 * Used by template {@code grails-app/views/eventResult/_pagination.gsp}
 * </p>
 * 
 * <p>
 * Unless otherwise noted passing <code>null</code> as method argument will
 * result in a {@link NullPointerException}.
 * </p>
 * 
 * @author rhc
 *
 */
public class PaginationListing {
	
	/**
	 * <p>
	 * Adds a row to this listing.
	 * </p>
	 * 
	 * @param rowToAdd 
	 * 			The row to add, not <code>null</code>.
	 * @param currentOffset 
	 * 			The offset of the current shown Record.
	 * @param max 
	 * 			The number of the shown records at page.
	 * @param total 
	 * 			The total records count.
	 * @param prevLink 
	 * 			The link as {@link String} of the next page.
	 * @param nextLink 
	 * 			The link as {@link String} of the previous page.
	 */
	public void addRow(PaginationListingRow rowToAdd, Integer currentOffset, Integer max, Integer total, String prevLink, String nextLink){
		this.paginationRows.add(rowToAdd)
		this.currentOffset = currentOffset
		this.total = total
		this.max = max
		this.nextLink = nextLink
		this.prevLink = prevLink
	}
	
	/**
	 * <p>
	 * Is this pagination listing empty? An pagination listing is said
	 * to be empty, if and only if it has no rows
	 * </p>
	 * 
	 * @return 	<code>true</code> if this listing is empty,
	 * 			<code>false</code> else.
	 */
	public boolean isEmpty(){
		return this.paginationRows.isEmpty()
	}
	
	/**
	 * <p>
	 * A list of {@link PaginationListingRow} which represents the pages to list.
	 * </p>
	 * 
	 * @return Never <code>null</code>.
	 */
	public List<PaginationListingRow> getRows(){
		return this.paginationRows
	}
	
	/**
	 * <p>
	 * Shows if the current shown records not element of the first page.
	 * </p>
	 * 
	 * 
	 * @return 	<code>true</code> if the current shown records not elements of the first page
	 * 			<code>false</code> else.
	 */
	public boolean isNotFirst(){
		if(currentOffset < max)
			return false
		true
	}
	
	/**
	 * <p>
	 * Gives the link as {@link String} of the next page
	 * </p>
	 * 
	 * @return {@link String} with the link of the next page
	 */
	public String getNextLink(){
		return this.nextLink
	}
	
	/**
	 * <p>
	 * Shows if the current shown records not element of the last page.
	 * </p>
	 * 
	 * 
	 * @return	<code>true</code> if the current shown records not elements of the last page
	 * 			<code>false</code> else.
	 */
	public boolean isNotLast(){
		if(total > max + currentOffset )
			return true
		false
	}
	
	/**
	 * <p>
	 * Gives the link as {@link String} of the previous page
	 * </p>
	 * 
	 * @return  {@link String} with the link of the previous page
	 */
	public String getPrevLink(){
		return this.prevLink
	}
	
	/**
	 * <p>
	 * Gives the maximum number of shown records at paginated page
	 * </p>
	 * 
	 * @return The maximum number of shown records
	 */
	public Integer getMaxCount() {
		return this.max
	}
	
	/**
	 * The listing rows.
	 */
	private final List<PaginationListingRow> paginationRows = //
	Collections.checkedList(new LinkedList(), PaginationListingRow.class)
	
	private Integer currentOffset
	
	private Integer total
	
	private Integer max
	
	private String nextLink
	
	private String prevLink
}

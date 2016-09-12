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

import grails.web.mapping.LinkGenerator

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
	LinkGenerator grailsLinkGenerator
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

	public String getFirstLink(){
		return paginationRows.first().pageLink
	}

	public String getLastLink(){
		return paginationRows.last().pageLink
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
     * Has current offset more then five pagination pages before
     * </p>
     * @return true if current offset has more then five pagination pages before
     */
    public moreThenFivePagesBefore(){
        if(currentOffset > max*5)
            return true
        return false
    }

    /**
     * <p>
     * Has current offset more then five pages after
     * </p>
     * @return true if current offset has more then five page after
     */
    public moreThenFivePagesAfter(){
        if(currentOffset + max*5 < total)
            return true
        return false
    }

    /**
     * <p>
     * Returns count of beginning pagination
     * </p>
     * @return count of beginning pagination
     */
    public Integer calculateStartListing(){
        Integer begin;
        if(moreThenFivePagesBefore())
            return ( (offset - max*5)/max )
        else
            return 0
    }

    /**
     * <p>
     * Return count of ending pagination
     * </p>
     * @return count of ending pagination
     */
    public Integer calculateEndListing(){
        Integer end;
        if(moreThenFivePagesAfter())
            return ( (offset + max*5 -1)/max )
        else
            return (total -1)/max
    }

    /**
     * <p>
     * Is the given pageCount the active page
     * </p>
     * @param pageCount
     * @return true if the pageCount is the active page
     */
    public boolean isActive(Integer pageCount){
        if(pageCount == currentOffset/max)
            return true
        return false
    }

    /**
     * <p>
     * Returns the current offset
     * </p>
     * @return current offset
     */
    public Integer getOffset(){
        return currentOffset
    }

    /**
     * <p>
     * Gives the maximum number of shown records at paginated page
     * </p>
     *
     * @return The maximum number of shown records
     */
    public Integer getMax(){
        return max
    }

    /**
     * <p>
     * Returns total count of elements from selection
     * </p>
     * @return total count of elements from selection
     */
    public Integer getTotal(){
        return total
    }
	
	/**
	 * The listing rows.
	 */
	private final List<PaginationListingRow> paginationRows = Collections.checkedList(new LinkedList(), PaginationListingRow.class)
	
	private Integer currentOffset
	
	private Integer total
	
	private Integer max
	
	private String nextLink
	
	private String prevLink
}

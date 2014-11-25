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
 * Visualization of a list of {@link EventResult}s as table. Used by template
 * {@code grails-app/views/eventResult/_listResults.gsp}
 * </p>
 * 
 * <p>
 * Unless otherwise noted passing <code>null</code> as method argument will 
 * result in a {@link NullPointerException}.
 * </p>
 * 
 * <p>
 * Objects of this class intended to be unmodifiable. 
 * </p>
 * 
 * @author mze
 * @since IT-106
 */
public class EventResultListing {

	/**
	 * <p>
	 * Adds a row to this listing.
	 * </p>
	 * 
	 * @param rowToAdd The row to add, not <code>null</code>.
	 */
	public void addRow(EventResultListingRow rowToAdd) {
		this.resultRows.add(rowToAdd);
	}

	/**
	 * <p>
	 * Is this event result listing empty? An event result listing is said 
	 * to be empty, if and only if it has no rows.
	 * </p>
	 * 
	 * @return <code>true</code> if this listing is empty, 
	 *         <code>false</code> else.
	 */
	public boolean isEmpty() {
		return this.resultRows.isEmpty();
	}

	/**
	 * <p>
	 * A list of {@link EventResultListingRow} which represents the results to
	 * list.
	 * </p>
	 * 
	 * @return Never <code>null</code>.
	 */
	public List<EventResultListingRow> getRows() {
		return this.resultRows;
	}

	/**
	 * The listings rows.
	 */
	private final List<EventResultListingRow> resultRows = //
	Collections.checkedList(new LinkedList(), EventResultListingRow.class);
}

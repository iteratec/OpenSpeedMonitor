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

package de.iteratec.osm.result

import de.iteratec.osm.result.EventResultController.EventResultsCommandBase
import de.iteratec.osm.result.EventResultController.ListResultsCommand;
import de.iteratec.osm.result.EventResultController.ListResultsForSpecificJobCommand
import de.iteratec.osm.report.ui.PaginationListing
import de.iteratec.osm.report.ui.PaginationListingRow

import java.text.SimpleDateFormat
import org.codehaus.groovy.grails.web.mapping.LinkGenerator

/**
 * Provides methods to get {@link PaginationListing} for pagination.
 * @author rhc
 *
 */
class PaginationService {

	LinkGenerator grailsLinkGenerator
	
	/**
	 * <p>
	 * Build a {@link PaginationListing} that contains all pagebuttons for pagination at showListResultsForJob.
	 * </p>
	 * 
	 * @param cmd
	 * 			The ListResultsForSpecificJobCommand contained already data of {@link EventResultsCommandBase}.
	 * @param eventResultsTotalRecords
	 * 			The total records count.
	 * @return
	 */
	public PaginationListing buildListResultsForJobPagination(ListResultsForSpecificJobCommand cmd, int eventResultsTotalRecords){
		PaginationListing paginationListing = new PaginationListing()
		for(int i=0; i < eventResultsTotalRecords/cmd.max; i++){
			String paginationLink = createListResultsForJobPaginationLink(cmd, i*cmd.max)
			String nextPaginationLink = createListResultsForJobPaginationLink(cmd, cmd.offset+cmd.max);
			String prevPaginationLink = createListResultsForJobPaginationLink(cmd, cmd.offset-cmd.max);
			paginationListing.addRow(new PaginationListingRow(i+1, paginationLink), cmd.offset, cmd.max, eventResultsTotalRecords, prevPaginationLink, nextPaginationLink)
		}
		
		return paginationListing
	}
	
	/**
	 * <p>
	 * Returns the PaginationLink as {@link String} for ListResultsForSpecificJobCommand.
	 * </p>
	 * 
	 * @param cmd
	 * 			The ListResultsForSpecificJobCommand contained already data of {@link EventResultsCommandBase}.
	 * @param offset
	 * 			The offset of the current shown Record.
	 * @return
	 */
	private String createListResultsForJobPaginationLink(ListResultsForSpecificJobCommand cmd, Integer offset){
		SimpleDateFormat fmtDate = new SimpleDateFormat("dd.MM.yyyy");
		
		String paginationLink = grailsLinkGenerator.link([
				'controller': 'eventResult',
				'action': 'showListResultsForJob',
				'params': [
						'selectedTimeFrameInterval': 0,
						'job.id': cmd.job.getId(),
						'from': fmtDate.format(cmd.getFrom()),
						'fromHour': cmd.getFromHour(),
						'to': fmtDate.format(cmd.getTo()),
						'toHour': cmd.getToHour(),
						'max': cmd.getMax(),
						'offset': offset
					]
			]);
	
		return paginationLink
	}
	
	
	/**
	 * <p>
	 * Build a {@link PaginationListing} that contains all pagebuttons for pagination at listResults.
	 * </p>
	 * 
	 * @param cmd
	 *			The listResultsCommand contained already data of {@link EventResultsCommandBase}.
	 * @param eventResultsTotalRecords
	 * 			The total records count.
	 * @return
	 */
    public PaginationListing buildListResultsPagination(ListResultsCommand cmd, int eventResultsTotalRecords){
		PaginationListing paginationListing = new PaginationListing()
		
		for(int i=0; i < eventResultsTotalRecords/cmd.max; i++){
			String paginationLink = createListResultsPaginationLink(cmd, i*cmd.max)
			String nextPaginationLink = createListResultsPaginationLink(cmd, cmd.offset+cmd.max);
			String prevPaginationLink = createListResultsPaginationLink(cmd, cmd.offset-cmd.max);
			
			paginationListing.addRow(new PaginationListingRow(i+1, paginationLink), cmd.offset, cmd.max, eventResultsTotalRecords, prevPaginationLink, nextPaginationLink)
		}
		
		return paginationListing
	}
	
	/**
	 * <p>
	 * Returns the PaginationLink as {@link String} for ListResultsCommand.
	 * </p>
	 * 
	 * @param cmd
	 * 			The ListResultsCommand contained already data of {@link EventResultsCommandBase}.
	 * @param offset
	 * 			The offset of the current shown Record.
	 * @return
	 */
	private String createListResultsPaginationLink(ListResultsCommand cmd, int offset){
		
		SimpleDateFormat fmtDate = new SimpleDateFormat("dd.MM.yyyy");
		
		String paginationLink = grailsLinkGenerator.link([
			'controller': 'eventResult',
			'action': 'listResults',
			'params': [
						'selectedTimeFrameInterval': 0,
						'from': fmtDate.format(cmd.getFrom()),
						'fromHour': cmd.getFromHour(),
						'to': fmtDate.format(cmd.getTo()),
						'toHour': cmd.getToHour(),
						'selectedFolder': cmd.getSelectedFolder(),
						'selectedPage': cmd.getSelectedPage(),
						'selectedBrowsers': cmd.getSelectedBrowsers(),
						'_selectedAllBrowsers': cmd.getSelectedAllBrowsers(),
						'_selectedAllMeasuredEvents': cmd.getSelectedMeasuredEventIds(),
						'selectedAllMeasuredEvents': cmd.getSelectedAllMeasuredEvents(),
						'_selectedAllLocations': cmd.getSelectedLocations(),
						'selectedAllLocations': cmd.getSelectedAllLocations(),
						'max': cmd.getMax(),
						'offset': offset
					]
			]);
		
		return paginationLink
	} 
}

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

import de.iteratec.osm.report.ui.PaginationListing
import de.iteratec.osm.report.ui.PaginationListingRow
import de.iteratec.osm.util.ParameterBindingUtility
import grails.gorm.transactions.Transactional
import grails.web.mapping.LinkGenerator

/**
 * Provides methods to get {@link PaginationListing} for pagination.
 * @author rhc
 *
 */
@Transactional
class PaginationService {

    LinkGenerator grailsLinkGenerator

    /**
     * <p>
     * Build a {@link PaginationListing} that contains all pagebuttons for pagination at showListResultsForJob.
     * </p>
     *
     * @param cmd
     * 			The ListResultsForSpecificJobCommand contained already data of {@link TabularResultEventResultsCommandBase}.
     * @param eventResultsTotalRecords
     * 			The total records count.
     * @return
     */
    PaginationListing buildListResultsForJobPagination(TabularResultListResultsForSpecificJobCommand cmd, int eventResultsTotalRecords) {
        PaginationListing paginationListing = new PaginationListing()
        for (int i = 0; i < eventResultsTotalRecords / cmd.max; i++) {
            String paginationLink = createListResultsForJobPaginationLink(cmd, i * cmd.max)
            String nextPaginationLink = createListResultsForJobPaginationLink(cmd, cmd.offset + cmd.max)
            String prevPaginationLink = createListResultsForJobPaginationLink(cmd, cmd.offset - cmd.max)
            paginationListing.addRow(new PaginationListingRow(i + 1, paginationLink), cmd.offset, cmd.max, eventResultsTotalRecords, prevPaginationLink, nextPaginationLink)
        }

        return paginationListing
    }

    /**
     * <p>
     * Returns the PaginationLink as {@link String} for ListResultsForSpecificJobCommand.
     * </p>
     *
     * @param cmd
     * 			The ListResultsForSpecificJobCommand contained already data of {@link TabularResultEventResultsCommandBase}.
     * @param offset
     * 			The offset of the current shown Record.
     * @return
     */
    private String createListResultsForJobPaginationLink(TabularResultListResultsForSpecificJobCommand cmd, Integer offset) {
        String paginationLink = grailsLinkGenerator.link([
                'controller': 'tabularResultPresentation',
                'action'    : 'showListResultsForJob',
                'params'    : [
                        'selectedTimeFrameInterval': 0,
                        'job.id'                   : cmd.job.getId(),
                        'from'                     : ParameterBindingUtility.formatDateTimeParameter(cmd.from),
                        'to'                       : ParameterBindingUtility.formatDateTimeParameter(cmd.to),
                        'max'                      : cmd.getMax(),
                        'offset'                   : offset
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
     * 			The listResultsCommand contained already data of {@link TabularResultEventResultsCommandBase}.
     * @param eventResultsTotalRecords
     * 			The total records count.
     * @return
     */
    PaginationListing buildListResultsPagination(TabularResultListResultsCommand cmd, int eventResultsTotalRecords) {
        PaginationListing paginationListing = new PaginationListing()

        for (int i = 0; i < eventResultsTotalRecords / cmd.max; i++) {
            String paginationLink = createListResultsPaginationLink(cmd, i * cmd.max)
            String nextPaginationLink = createListResultsPaginationLink(cmd, cmd.offset + cmd.max)
            String prevPaginationLink = createListResultsPaginationLink(cmd, cmd.offset - cmd.max)

            paginationListing.addRow(new PaginationListingRow(i + 1, paginationLink), cmd.offset, cmd.max, eventResultsTotalRecords, prevPaginationLink, nextPaginationLink)
        }

        return paginationListing
    }

    /**
     * <p>
     * Returns the PaginationLink as {@link String} for ListResultsCommand.
     * </p>
     *
     * @param cmd
     * 			The ListResultsCommand contained already data of {@link TabularResultEventResultsCommandBase}.
     * @param offset
     * 			The offset of the current shown Record.
     * @return
     */
    private String createListResultsPaginationLink(TabularResultListResultsCommand cmd, int offset) {
        String paginationLink = grailsLinkGenerator.link([
                'controller': 'tabularResultPresentation',
                'action'    : 'listResults',
                'params'    : [
                        'selectedTimeFrameInterval': 0,
                        'from'                     : ParameterBindingUtility.formatDateTimeParameter(cmd.from),
                        'to'                       : ParameterBindingUtility.formatDateTimeParameter(cmd.to),
                        'selectedFolder'           : cmd.getSelectedFolder(),
                        'selectedPages'            : cmd.getSelectedPages(),
                        'selectedBrowsers'         : cmd.getSelectedBrowsers(),
                        'selectedConnectivities'   : cmd.getSelectedConnectivityProfiles(),
                        'includeNativeConnectivity': cmd.getIncludeNativeConnectivity(),
                        'customConnectivityName'   : cmd.getSelectedCustomConnectivityNames(),
                        'max'                      : cmd.getMax(),
                        'offset'                   : offset
                ]
        ]);

        return paginationLink
    }
}

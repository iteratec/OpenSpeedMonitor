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

package de.iteratec.osm.report

import de.iteratec.osm.csi.CsiDashboardShowAllCommand

/**
 * UserspecificCsiDashboard
 * A domain class describes the data object and it's mapping to the database
 */
class UserspecificCsiDashboard extends UserspecificDashboardBase{

    /**
     * The name of the {@link de.iteratec.osm.report.chart.AggregatorType}.
     *
     * @see de.iteratec.osm.report.chart.AggregatorType#getName()
     * @see de.iteratec.osm.report.chart.AggregatorType#PAGE
     * @see de.iteratec.osm.report.chart.AggregatorType#PAGE_AND_BROWSER
     * @see de.iteratec.osm.report.chart.AggregatorType#SHOP
     */
    String aggrGroup

    /**
     * Whether or not current and not yet finished intervals should be loaded and displayed
     */
    Boolean includeInterval

    /**
     * The selected csiSystem Ids
     */
    String selectedCsiSystems = ""

    boolean csiTypeDocComplete
    boolean csiTypeVisuallyComplete

    static mapping = {
    }

    static constraints = {
        aggrGroup(nullable: true)
        includeInterval(nullable: true)
        selectedCsiSystems(nullable: true)
    }

    UserspecificCsiDashboard(CsiDashboardShowAllCommand cmd, Boolean publiclyVisible,
                             String wideScreenDiagramMontage, String dashboardName, String username) {
        this.publiclyVisible = publiclyVisible
        this.wideScreenDiagramMontage = wideScreenDiagramMontage == "true"
        this.dashboardName = dashboardName
        this.username = username

        // Get data from command
        debug = cmd.debug
        fromDate = cmd.from
        toDate = cmd.to
        fromHour = cmd.fromHour
        toHour = cmd.toHour
        aggrGroup = cmd.aggrGroupAndInterval
        selectedTimeFrameInterval = cmd.selectedTimeFrameInterval
        includeInterval = cmd.includeInterval
        setFromHour = cmd.setFromHour
        setToHour = cmd.setToHour
        selectedAllMeasuredEvents = cmd.selectedAllMeasuredEvents
        selectedAllBrowsers = cmd.selectedAllBrowsers
        selectedAllLocations = cmd.selectedAllLocations
        graphNameAliases = cmd.graphNameAliases
        graphColors = cmd.graphColors
        selectedAllConnectivityProfiles = cmd.selectedAllConnectivityProfiles
        selectedConnectivities = cmd.selectedConnectivities

        // Create strings for db
        String selectedFolderString = ""
        String selectedPagesString = ""
        String selectedMeasuredEventIdsString = ""
        String selectedBrowsersString = ""
        String selectedLocationsString = ""
        String selectedCsiSystemsString = ""

        // generate Strings for db
        cmd.selectedFolder.each {f -> selectedFolderString += f + ","}
        // trim last comma
        if(selectedFolderString.length() > 0) selectedFolderString = selectedFolderString.substring(0, selectedFolderString.length()-1)

        cmd.selectedPages.each {f -> selectedPagesString += f + ","}
        if(selectedPagesString.length() > 0) selectedPagesString = selectedPagesString.substring(0, selectedPagesString.length()-1)
        cmd.selectedMeasuredEventIds.each {f -> selectedMeasuredEventIdsString += f + ","}
        if(selectedMeasuredEventIdsString.length() > 0) selectedMeasuredEventIdsString = selectedMeasuredEventIdsString.substring(0, selectedMeasuredEventIdsString.length()-1)
        cmd.selectedBrowsers.each {f -> selectedBrowsersString += f + ","}
        if(selectedBrowsersString.length() > 0) selectedBrowsersString = selectedBrowsersString.substring(0, selectedBrowsersString.length()-1)
        cmd.selectedLocations.each {f -> selectedLocationsString += f + ","}
        if(selectedLocationsString.length() > 0) selectedLocationsString = selectedLocationsString.substring(0, selectedLocationsString.length()-1)
        cmd.selectedCsiSystems.each {f -> selectedCsiSystemsString += f + ","}
        if(selectedCsiSystemsString.length() > 0) selectedCsiSystemsString = selectedCsiSystemsString.substring(0, selectedCsiSystemsString.length()-1)

        selectedFolder = selectedFolderString
        selectedPages = selectedPagesString
        selectedMeasuredEventIds = selectedMeasuredEventIdsString
        selectedBrowsers = selectedBrowsersString
        selectedLocations = selectedLocationsString
        selectedCsiSystems = selectedCsiSystemsString
        chartTitle = cmd.chartTitle
        chartWidth = cmd.chartWidth
        chartHeight = cmd.chartHeight
        loadTimeMinimum = cmd.loadTimeMinimum
        loadTimeMaximum = cmd.loadTimeMaximum?:"auto"
        showDataMarkers = cmd.showDataMarkers
        showDataLabels = cmd.showDataLabels
        csiTypeDocComplete = cmd.csiTypeDocComplete
        csiTypeVisuallyComplete = cmd.csiTypeVisuallyComplete
    }
}

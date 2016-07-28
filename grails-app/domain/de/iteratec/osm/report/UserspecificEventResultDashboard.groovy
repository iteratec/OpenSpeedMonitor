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

import de.iteratec.osm.result.EventResultDashboardShowAllCommand
import grails.plugin.springsecurity.SpringSecurityUtils

import java.text.SimpleDateFormat

/**
 * UserspecificEventResultDashboard
 * A domain class describes the data object and it's mapping to the database
 */
class UserspecificEventResultDashboard extends UserspecificDashboardBase{

    /**
     * The time of the {@link CsiAggregationInterval}.
     */
    Integer selectedInterval

    /**
     * Database name of the selected {@link AggregatorType}, selected by the user.
     * Determines wich {@link CachedView#CACHED} results should be shown.
     */
    String selectedAggrGroupValuesCached = ""

    /**
     * Database name of the selected {@link AggregatorType}, selected by the user.
     * Determines wich {@link CachedView#UNCACHED} results should be shown.
     */
    String selectedAggrGroupValuesUnCached = ""

    /**
     * Lower bound for load-time-measurands. Values lower than this will be excluded from graphs.
     */
    Integer trimBelowLoadTimes

    /**
     * Upper bound for load-time-measurands. Values greater than this will be excluded from graphs.
     */
    Integer trimAboveLoadTimes

    /**
     * Lower bound for request-count-measurands. Values lower than this will be excluded from graphs.
     */
    Integer trimBelowRequestCounts

    /**
     * Upper bound for request-count-measurands. Values greater than this will be excluded from graphs.
     */
    Integer trimAboveRequestCounts

    /**
     * Lower bound for request-sizes-measurands. Values lower than this will be excluded from graphs.
     */
    Integer trimBelowRequestSizes

    /**
     * Upper bound for request-sizes-measurands. Values greater than this will be excluded from graphs.
     */
    Integer trimAboveRequestSizes


    Boolean includeNativeConnectivity
    Boolean selectedAllConnectivityProfiles
    String customConnectivityName
    String selectedConnectivityProfiles
    Boolean includeCustomConnectivity

    static mapping = {
    }

    static constraints = {
        selectedAggrGroupValuesCached(nullable: true)
        selectedInterval(nullable: true)
        selectedAggrGroupValuesUnCached(nullable: true)
        trimBelowLoadTimes(nullable: true)
        trimAboveLoadTimes(nullable: true)
        trimBelowRequestCounts(nullable: true)
        trimAboveRequestCounts(nullable: true)
        trimBelowRequestSizes(nullable: true)
        trimAboveRequestSizes(nullable: true)
        includeNativeConnectivity(nullable: true)
        selectedAllConnectivityProfiles(nullable: true)
        customConnectivityName(nullable: true)
        selectedConnectivityProfiles(nullable: true)
        includeCustomConnectivity(nullable: true)
    }

    /**
     * Creates Userspecific Dashboard from EventResultDashbordShowAllCommand
     * @param cmd the command the set values
     * @param dashboardName a unique name for the dashboard
     * @param publiclyVisible true if the dashboard should be visible for all
     * @param wideScreenDiagramMontage true if there should be optimisation for wideScreen
     * @param username the creator of the dashboard
     */
    UserspecificEventResultDashboard(EventResultDashboardShowAllCommand cmd, String dashboardName, Boolean publiclyVisible, String wideScreenDiagramMontage, String username) {

        this.dashboardName = dashboardName
        this.publiclyVisible = Boolean.valueOf(publiclyVisible)
        this.wideScreenDiagramMontage = wideScreenDiagramMontage == "true"
        this.username = username

        // Get Data from command
        fromDate = cmd.from
        toDate = cmd.to
        fromHour = cmd.fromHour
        toHour = cmd.toHour
        selectedInterval = cmd.selectedInterval
        selectedTimeFrameInterval = cmd.selectedTimeFrameInterval
        selectedAllMeasuredEvents = cmd.selectedAllMeasuredEvents
        selectedAllBrowsers = cmd.selectedAllBrowsers
        selectedAllLocations = cmd.selectedAllLocations
        trimBelowLoadTimes = cmd.trimBelowLoadTimes
        trimAboveLoadTimes = cmd.trimAboveLoadTimes
        trimBelowRequestCounts = cmd.trimBelowRequestCounts
        trimAboveRequestCounts = cmd.trimAboveRequestCounts
        trimBelowRequestSizes = cmd.trimBelowRequestSizes
        trimAboveRequestSizes = cmd.trimAboveRequestSizes
        overwriteWarningAboutLongProcessingTime = cmd.overwriteWarningAboutLongProcessingTime
        debug = cmd.debug
        setFromHour = cmd.setFromHour
        setToHour = cmd.setToHour
        includeCustomConnectivity = cmd.includeCustomConnectivity
        includeNativeConnectivity = cmd.includeNativeConnectivity
        customConnectivityName = cmd.customConnectivityName
        selectedAllConnectivityProfiles = cmd.selectedAllConnectivityProfiles
        chartTitle = cmd.chartTitle
        chartWidth = cmd.chartWidth
        chartHeight = cmd.chartHeight
        loadTimeMinimum = cmd.loadTimeMinimum
        loadTimeMaximum = cmd.loadTimeMaximum?:"auto"
        showDataMarkers = cmd.showDataMarkers
        showDataLabels = cmd.showDataLabels
        graphNameAliases = cmd.graphNameAliases
        graphColors = cmd.graphColors

        // generate Strings for db
        String selectedFolderString = ""
        String selectedPagesString = ""
        String selectedMeasuredEventIdsString = ""
        String selectedBrowsersString = ""
        String selectedLocationsString = ""
        String selectedAggrGroupValuesCachedString = ""
        String selectedAggrGroupValuesUnCachedString = ""
        String selectedConnectivityProfilesString = ""

        // generate Strings for db
        cmd.selectedFolder.each {f -> selectedFolderString += f + ","}
        // trim last comma
        if(selectedFolderString.length() > 0) selectedFolderString = selectedFolderString.substring(0, selectedFolderString.length()-1)
        cmd.selectedPages.each {p -> selectedPagesString += p + ","}
        if(selectedPagesString.length() > 0) selectedPagesString = selectedPagesString.substring(0, selectedPagesString.length()-1)
        cmd.selectedMeasuredEventIds.each {m -> selectedMeasuredEventIdsString += m + ","}
        if(selectedMeasuredEventIdsString.length() > 0) selectedMeasuredEventIdsString = selectedMeasuredEventIdsString.substring(0, selectedMeasuredEventIdsString.length()-1)
        cmd.selectedBrowsers.each {b -> selectedBrowsersString += b + ","}
        if(selectedBrowsersString.length() > 0) selectedBrowsersString = selectedBrowsersString.substring(0, selectedBrowsersString.length()-1)
        cmd.selectedLocations.each {l -> selectedLocationsString += l + ","}
        if(selectedLocationsString.length() > 0) selectedLocationsString = selectedLocationsString.substring(0, selectedLocationsString.length()-1)
        cmd.selectedAggrGroupValuesCached.each {a -> selectedAggrGroupValuesCachedString += a + ","}
        if(selectedAggrGroupValuesCachedString.length() > 0) selectedAggrGroupValuesCachedString = selectedAggrGroupValuesCachedString.substring(0, selectedAggrGroupValuesCachedString.length()-1)
        cmd.selectedAggrGroupValuesUnCached.each {a -> selectedAggrGroupValuesUnCachedString += a + ","}
        if(selectedAggrGroupValuesUnCachedString.length() > 0) selectedAggrGroupValuesUnCachedString = selectedAggrGroupValuesUnCachedString.substring(0, selectedAggrGroupValuesUnCachedString.length()-1)
        cmd.selectedConnectivityProfiles.each {p -> selectedConnectivityProfilesString += p + ","}
        if(selectedConnectivityProfilesString.length() > 0) selectedConnectivityProfilesString = selectedConnectivityProfilesString.substring(0, selectedConnectivityProfilesString.length()-1)

        selectedFolder = selectedFolderString
        selectedPages = selectedPagesString
        selectedMeasuredEventIds = selectedMeasuredEventIdsString
        selectedBrowsers = selectedBrowsersString
        selectedLocations = selectedLocationsString
        selectedAggrGroupValuesCached = selectedAggrGroupValuesCachedString
        selectedAggrGroupValuesUnCached = selectedAggrGroupValuesUnCachedString
        selectedConnectivityProfiles = selectedConnectivityProfilesString
    }
}

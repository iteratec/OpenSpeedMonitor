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
import org.codehaus.groovy.grails.web.json.JSONObject

import java.text.SimpleDateFormat

/**
 * UserspecificEventResultDashboard
 * A domain class describes the data object and it's mapping to the database
 */
class UserspecificEventResultDashboard {

    def springSecurityService
    public final static String DATE_FORMAT_STRING = 'dd.MM.yyyy'
    private final static SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat(DATE_FORMAT_STRING)

    /**
     * if dashboard is visible to all or just to admins and to user that created it
     */
    Boolean publiclyVisible

    /**
     * name of dashboard
     */
    String dashboardName

    /**
     * name of user that created it
     */
    String username

    //from csi start

    /**
     * The selected start date (inclusive).
     *
     * Please use {@link #getSelectedTimeFrame()}.
     */
    Date fromDate

    /**
     * The selected end date (inclusive).
     *
     * Please use {@link #getSelectedTimeFrame()}.
     */
    Date toDate

    /**
     * The selected start hour of date.
     *
     * Please use {@link #getSelectedTimeFrame()}.
     */
    String fromHour

    /**
     * The selected end hour of date.
     *
     * Please use {@link #getSelectedTimeFrame()}.
     */
    String toHour

    /**
     * The database IDs of the selected {@linkplain de.iteratec.osm.measurement.schedule.JobGroup CSI groups}
     * which are the systems measured for a CSI value
     *
     */
    String selectedFolder = ""

    /**
     * The database IDs of the selected {@linkplain Page pages}
     * which results to be shown.
     */
    String selectedPages = ""

    /**
     * The database IDs of the selected {@linkplain de.iteratec.osm.result.MeasuredEvent
     * measured events} which results to be shown.
     *
     * These selections are only relevant if
     * {@link #selectedAllMeasuredEvents} is evaluated to
     * <code>false</code>.
     */
    String selectedMeasuredEventIds = ""

    /**
     * User enforced the selection of all measured events.
     * This selection <em>is not</em> reflected in
     * {@link #selectedMeasuredEventIds} cause of URL length
     * restrictions. If this flag is evaluated to
     * <code>true</code>, the selections in
     * {@link #selectedMeasuredEventIds} should be ignored.
     */
    Boolean selectedAllMeasuredEvents

    /**
     * The database IDs of the selected {@linkplain de.iteratec.osm.measurement.environment.Browser
     * browsers} which results to be shown.
     *
     * These selections are only relevant if
     * {@link #selectedAllBrowsers} is evaluated to
     * <code>false</code>.
     */
    String selectedBrowsers = ""

    /**
     * User enforced the selection of all browsers.
     * This selection <em>is not</em> reflected in
     * {@link #selectedBrowsers} cause of URL length
     * restrictions. If this flag is evaluated to
     * <code>true</code>, the selections in
     * {@link #selectedBrowsers} should be ignored.
     */
    Boolean selectedAllBrowsers

    /**
     * The database IDs of the selected {@linkplain de.iteratec.osm.measurement.environment.Location
     * locations} which results to be shown.
     *
     * These selections are only relevant if
     * {@link #selectedAllLocations} is evaluated to
     * <code>false</code>.
     */
    String selectedLocations = ""

    /**
     * User enforced the selection of all locations.
     * This selection <em>is not</em> reflected in
     * {@link #selectedLocations} cause of URL length
     * restrictions. If this flag is evaluated to
     * <code>true</code>, the selections in
     * {@link #selectedLocations} should be ignored.
     */
    Boolean selectedAllLocations

    /**
     * If the user has been warned about a potentially long processing
     * time, did he overwrite the waring and really want to perform
     * the request?
     *
     * A value of <code>true</code> indicates that overwrite, everything
     * should be done as requested, <code>false</code> indicates that
     * the user hasn't been warned before, so there is no overwrite.
     */
    Boolean overwriteWarningAboutLongProcessingTime = true

    /**
     * Flag for manual debugging.
     * Used for debugging highcharts-export-server, e.g.
     */
    Boolean debug

    /**
     * A predefined time frame.
     */
    int selectedTimeFrameInterval = 259200

    /**
     * Whether or not the time of the start-date should be selected manually.
     */
    Boolean setFromHour
    /**
     * Whether or not the time of the start-date should be selected manually.
     */
    Boolean setToHour

    /**
     * The time of the {@link MeasuredValueInterval}.
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

    /**
     * toggle formatting rickshaw export to wide screen format
     */
    Boolean wideScreenDiagramMontage

    Boolean includeNativeConnectivity
    Boolean selectedAllConnectivityProfiles
    String customConnectivityName
    String selectedConnectivityProfiles
    Boolean includeCustomConnectivity

    static mapping = {
    }

    //csi
    static constraints = {
        dashboardName(nullable: true, unique: true)
        username(nullable: true)
        publiclyVisible(nullable: true)
        fromDate(nullable: true)
        toDate(nullable: true)
        fromHour(nullable: true)
        toHour(nullable: true)
        selectedFolder(nullable: true)
        selectedPages(nullable: true)
        selectedMeasuredEventIds(nullable: true)
        selectedBrowsers(nullable: true)
        selectedLocations(nullable: true)
        overwriteWarningAboutLongProcessingTime(nullable: true)
        selectedAggrGroupValuesCached(nullable: true)
        selectedAllMeasuredEvents(nullable: true)
        selectedAllBrowsers(nullable: true)
        selectedAllLocations(nullable: true)
        debug(nullable: true)
        selectedTimeFrameInterval(nullable: true)
        setFromHour(nullable: true)
        setToHour(nullable: true)
        selectedInterval(nullable: true)
        selectedAggrGroupValuesUnCached(nullable: true)
        trimBelowLoadTimes(nullable: true)
        trimAboveLoadTimes(nullable: true)
        trimBelowRequestCounts(nullable: true)
        trimAboveRequestCounts(nullable: true)
        trimBelowRequestSizes(nullable: true)
        trimAboveRequestSizes(nullable: true)
        wideScreenDiagramMontage(nullable: true)
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
    UserspecificEventResultDashboard(EventResultDashboardShowAllCommand cmd, String dashboardName, String publiclyVisible, String wideScreenDiagramMontage, String username) {

        this.dashboardName = dashboardName
        this.publiclyVisible = publiclyVisible
        this.wideScreenDiagramMontage = wideScreenDiagramMontage
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

    /**
     * Checks if the currentUser is admin or creator of the given dashboard
     * @param dashboardId the dashboard to check
     * @return true if currentUser is admin or creator, false otherwise
     */
    boolean isCurrentUserDashboardOwner(String dashboardId) {
        if (SpringSecurityUtils.ifAnyGranted("ROLE_ADMIN,ROLE_SUPER_ADMIN")) {
            return true
        } else {
            // get owner name
            UserspecificEventResultDashboard currentBoard = UserspecificEventResultDashboard.get(dashboardId)
            if (!currentBoard) {
                return false
            }
            String boardCreator = currentBoard.username
            String currentUser = ""
            if (springSecurityService.isLoggedIn()) {
                currentUser = springSecurityService.authentication.principal.getUsername()
            }
            if (currentUser == boardCreator) {
                return true
            } else {
                return false
            }
        }
    }


    def getListOfAvailableDashboards() {
        List result = []
        List fullList = []
        fullList = UserspecificEventResultDashboard.findAll().sort{it.dashboardName}

        String currentUser = ""
        if (springSecurityService.isLoggedIn()) {
            currentUser = springSecurityService.authentication.principal.getUsername()
        }
        for (board in fullList) {
            if ((board.publiclyVisible == true) || (board.username == currentUser)) {
                String link = ""
                link += "showAll?"
                link += "selectedTimeFrameInterval=" + board.selectedTimeFrameInterval
                link += "&_setFromHour="
                if (board.setFromHour != null) {
                    link += "&setFromHour=on"
                }
                link += "&from=" + SIMPLE_DATE_FORMAT.format(board.fromDate)
                link += "&fromHour=" + board.fromHour.replace(':', '%3A')
                link += "&_setToHour="
                if (board.setToHour != null) {
                    link += "&setToHour=on"
                }
                link += "&to=" + SIMPLE_DATE_FORMAT.format(board.toDate)
                link += "&toHour=" + board.toHour.replace(':', '%3A')
                if ((board.selectedFolder != null) && (board.selectedFolder.size() > 0)) {
                    for (item in board.selectedFolder.tokenize(',')) {
                        link += "&selectedFolder=" + item
                    }
                }
                if ((board.selectedPages != null) && (board.selectedPages.size() > 0)) {
                    for (item in board.selectedPages.tokenize(',')) {
                        link += "&selectedPages=" + item
                    }
                }
                link += "&_selectedAllBrowsers="
                if (board.selectedAllBrowsers != null) {
                    link += "&selectedAllBrowsers=on"
                }
                link += "&_selectedAllMeasuredEvents="
                if (board.selectedAllMeasuredEvents != null) {
                    link += "&selectedAllMeasuredEvents=on"
                }
                link += "&_selectedAllLocations="
                if (board.selectedAllLocations != null) {
                    link += "&selectedAllLocations=on"
                }
                if ((board.selectedMeasuredEventIds != null) && (board.selectedMeasuredEventIds.size() > 0)) {
                    for (item in board.selectedMeasuredEventIds.tokenize(',')) {
                        link += "&selectedMeasuredEventIds=" + item
                    }
                }
                if ((board.selectedBrowsers != null) && (board.selectedBrowsers.size() > 0)) {
                    for (item in board.selectedBrowsers.tokenize(',')) {
                        link += "&selectedBrowsers=" + item
                    }
                }
                if ((board.selectedLocations != null) && (board.selectedLocations.size() > 0)) {
                    for (item in board.selectedLocations.tokenize(',')) {
                        link += "&selectedLocations=" + item
                    }
                }
                link += "&action_showAll=Show&_overwriteWarningAboutLongProcessingTime=&overwriteWarningAboutLongProcessingTime=on"
                link += "&id=" + board.id
                link += "&dbname=" + java.net.URLEncoder.encode(board.dashboardName, "UTF-8")
                if (board.wideScreenDiagramMontage == true) {
                    link += "&wideScreenDiagramMontage=on"
                }
                link += "&selectedInterval=" + board.selectedInterval

                if ((board.selectedAggrGroupValuesUnCached != null) && (board.selectedAggrGroupValuesUnCached.size() > 0)) {
                    for (item in board.selectedAggrGroupValuesUnCached.tokenize(',')) {
                        link += "&selectedAggrGroupValuesUnCached=" + item
                    }
                }
                if ((board.selectedAggrGroupValuesCached != null) && (board.selectedAggrGroupValuesCached.size() > 0)) {
                    for (item in board.selectedAggrGroupValuesCached.tokenize(',')) {
                        link += "&selectedAggrGroupValuesCached=" + item
                    }
                }
                link += "&trimBelowLoadTimes="
                if (board.trimBelowLoadTimes != null) {
                    link += board.trimBelowLoadTimes
                }
                link += "&trimAboveLoadTimes="
                if (board.trimAboveLoadTimes != null) {
                    link += board.trimAboveLoadTimes
                }
                link += "&trimBelowRequestCounts="
                if (board.trimBelowRequestCounts != null) {
                    link += board.trimBelowRequestCounts
                }
                link += "&trimAboveRequestCounts="
                if (board.trimAboveRequestCounts != null) {
                    link += board.trimAboveRequestCounts
                }
                link += "&trimBelowRequestSizes="
                if (board.trimBelowRequestSizes != null) {
                    link += board.trimBelowRequestSizes
                }
                link += "&trimAboveRequestSizes="
                if (board.trimAboveRequestSizes != null) {
                    link += board.trimAboveRequestSizes
                }
                link += "&_includeNativeConnectivity="
                if(board.includeNativeConnectivity == true) {
                    link += "&includeNativeConnectivity=on"
                }
                link += "&_selectedAllConnectivityProfiles="
                if(board.selectedAllConnectivityProfiles == true) {
                    link += "&selectedAllConnectivityProfiles=on"
                }
                link += "&customConnectivityName="
                if(board.customConnectivityName != null){
                    link += board.customConnectivityName
                }
                if ((board.selectedConnectivityProfiles != null) && (board.selectedConnectivityProfiles.size() > 0)) {
                    for (item in board.selectedConnectivityProfiles.tokenize(',')) {
                        link += "&selectedConnectivityProfiles=" + item
                    }
                }
                link += "&_includeCustomConnectivity="
                if(board.includeCustomConnectivity == true) {
                    link += "&includeCustomConnectivity=on"
                }



                result.add([dashboardName: board.dashboardName, link: link])
            }
        }
        return result
    }
}

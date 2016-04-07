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
import grails.plugin.springsecurity.SpringSecurityUtils

import java.text.SimpleDateFormat

/**
 * UserspecificCsiDashboard
 * A domain class describes the data object and it's mapping to the database
 */
class UserspecificCsiDashboard {

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
     */
    Date fromDate

    /**
     * The selected end date (inclusive).
     *
     */
    Date toDate

    /**
     * The selected start hour of date.
     *
     */
    String fromHour

    /**
     * The selected end hour of date.
     *
     */
    String toHour

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
     * The database IDs of the selected {@linkplain de.iteratec.osm.measurement.schedule.JobGroup CSI groups}
     * which are the systems measured for a CSI value
     *
     */
    String selectedFolder = ""

    /**
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
     * Whether or not current and not yet finished intervals should be loaded and displayed
     */
    Boolean includeInterval

    /**
     */
    String selectedAggrGroupValuesCached = ""

    /**
     */
    String selectedAggrGroupValuesUnCached = ""

    /**
     * The selected csiSystem Ids
     */
    String selectedCsiSystems = ""

    //#####Chart Adjustments#####
    String chartTitle
    int chartWidth
    int chartHeight
    int loadTimeMinimum

    // If map is not specified, it acts as a string/string mapping (gorm default)
    Map graphNameAliases = [:]
    Map graphColors = [:]

    /**
     * The maximum load time could be set to 'auto', so we handle it as a string
     */
    String loadTimeMaximum
    boolean showDataMarkers
    boolean showDataLabels

    boolean csiTypeDocComplete
    boolean csiTypeVisuallyComplete

    /**
     * toggle formatting rickshaw export to wide screen format
     */
    Boolean wideScreenDiagramMontage = false

    static mapping = {
    }

    static constraints = {
        dashboardName(nullable: false, unique: true)
        username(nullable: false)
        publiclyVisible(nullable: true)
        fromDate(nullable: true)
        toDate(nullable: true)
        fromHour(nullable: true)
        toHour(nullable: true)
        aggrGroup(nullable: true)
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
        includeInterval(nullable: true)
        selectedAggrGroupValuesUnCached(nullable: true)
        wideScreenDiagramMontage(nullable: true)
        selectedCsiSystems(nullable: true)
    }

    UserspecificCsiDashboard(CsiDashboardShowAllCommand cmd, String publiclyVisible,
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
            UserspecificCsiDashboard currentBoard = UserspecificCsiDashboard.get(dashboardId)
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
        List<UserspecificCsiDashboard> fullList = []
        fullList = UserspecificCsiDashboard.findAll().sort {it.dashboardName}

        String currentUser = ""
        if (springSecurityService.isLoggedIn()) {
            currentUser = springSecurityService.authentication.principal.getUsername()
        }
        for (board in fullList) {
            if ((board.publiclyVisible) || (board.username == currentUser)) {
                result.add([dashboardName: board.dashboardName, dashboardID: board.id])
            }
        }
        return result
    }
}



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

import java.text.SimpleDateFormat

import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils







public enum UserspecificDashboardDiagramType {
    CSI,
    EVENT,
}

/**
 * UserspecificDashboard
 * A domain class describes the data object and it's mapping to the database
 */
class UserspecificDashboard {

    def springSecurityService
    public final static String DATE_FORMAT_STRING = 'dd.MM.yyyy'
    private final static SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat(DATE_FORMAT_STRING)

    //display type
    UserspecificDashboardDiagramType diagramType

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
    String  fromHour
    String  fromMinute

    /**
     * The selected end hour of date.
     *
     * Please use {@link #getSelectedTimeFrame()}.
     */
    String toHour
    String toMinute

    /**
     * The name of the {@link de.iteratec.osm.report.chart.AggregatorType}.
     *
     * @see de.iteratec.osm.report.chart.AggregatorType#getName()
     * @see de.iteratec.osm.report.chart.AggregatorType#MEASURED_STEP
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
     * Whether or not current and not yet finished intervals should be loaded and displayed
     */
    Boolean includeInterval

    /**
     * The time of the {@link MeasuredValueInterval}.
     */
    Integer selectedInterval

    /**
     * The Selected chart type (line or point)
     */
    Integer selectChartType;

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

    //from event end


	/* Default (injected) attributes of GORM */
//	Long	id
//	Long	version

	/* Automatic timestamping of GORM */
//	Date	dateCreated
//	Date	lastUpdated

//	static belongsTo	= []	// tells GORM to cascade commands: e.g., delete this object if the "parent" is deleted.
//	static hasOne		= []	// tells GORM to associate another domain object as an owner in a 1-1 mapping
//	static hasMany		= []	// tells GORM to associate other domain objects for a 1-n or n-m mapping
//	static mappedBy		= []	// specifies which property should be used in a mapping

    static mapping = {
    }

    //csi
    static constraints = {
        dashboardName(nullable: true, unique:true)
        diagramType(nullable: true)
        username(nullable: true)
        publiclyVisible(nullable: true)
        fromDate(nullable: true)
        toDate(nullable:true)
        fromHour(nullable: true)
        toHour(nullable: true)
        aggrGroup(nullable:true)
        selectedFolder(nullable:true)
        selectedPages(nullable:true)
        selectedMeasuredEventIds(nullable:true)
        selectedBrowsers(nullable:true)
        selectedLocations(nullable:true)
        overwriteWarningAboutLongProcessingTime(nullable:true)
        fromHour(nullable: true)
        toHour(nullable: true)
        selectedAggrGroupValuesCached(nullable:true)
        selectedAllMeasuredEvents(nullable: true)
        selectedAllBrowsers(nullable: true)
        selectedAllLocations(nullable: true)
        fromMinute(nullable: true)
        toMinute(nullable: true)
        debug(nullable: true)
        selectedTimeFrameInterval(nullable: true)
        setFromHour(nullable: true)
        setToHour(nullable: true)
        includeInterval(nullable: true)
        selectedInterval(nullable: true)
        selectChartType(nullable: true)
        selectedAggrGroupValuesUnCached(nullable: true)
        trimBelowLoadTimes(nullable: true)
        trimAboveLoadTimes(nullable: true)
        trimBelowRequestCounts(nullable: true)
        trimAboveRequestCounts(nullable: true)
        trimBelowRequestSizes(nullable: true)
        trimAboveRequestSizes(nullable: true)
        wideScreenDiagramMontage(nullable: true)
    }

    def isCurrentUserDashboardOwner(String dashboardId) {
        if (SpringSecurityUtils.ifAnyGranted("ROLE_ADMIN,ROLE_SUPER_ADMIN")) {
            return true
        } else {
            // get owner name
            UserspecificDashboard currentBoard = UserspecificDashboard.get(dashboardId)
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

    def getListOfAvailableDashboards(String diagramType) {
        List result = []
        List fullList = []
        if (diagramType == "EVENT") {
            fullList = UserspecificDashboard.findAllByDiagramType(UserspecificDashboardDiagramType.EVENT)
        } else {
            fullList = UserspecificDashboard.findAllByDiagramType(UserspecificDashboardDiagramType.CSI)
        }
        String currentUser = ""
        if (springSecurityService.isLoggedIn()) {
            currentUser = springSecurityService.authentication.principal.getUsername()
        }
        for(board in fullList){
            if ((board.publiclyVisible == true) || (board.username == currentUser)) {
                String link = ""
                link += "showAll?"
                link += "selectedTimeFrameInterval=" + board.selectedTimeFrameInterval
                link += "&_setFromHour="
                if (board.setFromHour != null) { link += "&setFromHour=on" }
                link += "&from=" + SIMPLE_DATE_FORMAT.format(board.fromDate)
                link += "&fromHour=" + board.fromHour.replace( ':', '%3A' )
                link += "&_setToHour="
                if (board.setToHour != null) { link += "&setToHour=on" }
                link += "&to=" + SIMPLE_DATE_FORMAT.format(board.toDate)
                link += "&toHour=" + board.toHour.replace( ':', '%3A' )
                if ((board.selectedFolder != null) && (board.selectedFolder.size() > 0)) {
                    for(item in board.selectedFolder.tokenize( ',' )){
                        link += "&selectedFolder=" + item
                    }
                }
                if ((board.selectedPages != null) && (board.selectedPages.size() > 0)) {
                    for(item in board.selectedPages.tokenize( ',' )){
                        link += "&selectedPages=" + item
                    }
                }
                link += "&_selectedAllBrowsers="
                if (board.selectedAllBrowsers != null) { link += "&selectedAllBrowsers=on" }
                link += "&_selectedAllMeasuredEvents="
                if (board.selectedAllMeasuredEvents != null) { link += "&selectedAllMeasuredEvents=on" }
                link += "&_selectedAllLocations="
                if (board.selectedAllLocations != null) { link += "&selectedAllLocations=on" }
                if ((board.selectedMeasuredEventIds != null) && (board.selectedMeasuredEventIds.size() > 0)) {
                    for(item in board.selectedMeasuredEventIds.tokenize( ',' )){
                        link += "&selectedMeasuredEventIds=" + item
                    }
                }
                if ((board.selectedBrowsers != null) && (board.selectedBrowsers.size() > 0)) {
                    for(item in board.selectedBrowsers.tokenize( ',' )){
                        link += "&selectedBrowsers=" + item
                    }
                }
                if ((board.selectedLocations != null) && (board.selectedLocations.size() > 0)) {
                    for(item in board.selectedLocations.tokenize( ',' )){
                        link += "&selectedLocations=" + item
                    }
                }
                link += "&_action_showAll=Show&selectedChartType=0&_overwriteWarningAboutLongProcessingTime=&overwriteWarningAboutLongProcessingTime=on"
                link += "&id=" + board.id
                link += "&dbname=" + java.net.URLEncoder.encode(board.dashboardName, "UTF-8")
                if (board.wideScreenDiagramMontage == true) {
                    link += "&wideScreenDiagramMontage=on"
                }
                if (diagramType == "EVENT") {
                    link += "&selectedInterval=" + board.selectedInterval

                    if ((board.selectedAggrGroupValuesUnCached != null) && (board.selectedAggrGroupValuesUnCached.size() > 0)) {
                        for(item in board.selectedAggrGroupValuesUnCached.tokenize( ',' )){
                            link += "&selectedAggrGroupValuesUnCached=" + item
                        }
                    }
                    if ((board.selectedAggrGroupValuesCached != null) && (board.selectedAggrGroupValuesCached.size() > 0)) {
                        for(item in board.selectedAggrGroupValuesCached.tokenize( ',' )){
                            link += "&selectedAggrGroupValuesCached=" + item
                        }
                    }
                    link += "&trimBelowLoadTimes="
                    if (board.trimBelowLoadTimes != null) { link += board.trimBelowLoadTimes }
                    link += "&trimAboveLoadTimes="
                    if (board.trimAboveLoadTimes != null) { link += board.trimAboveLoadTimes }
                    link += "&trimBelowRequestCounts="
                    if (board.trimBelowRequestCounts != null) { link += board.trimBelowRequestCounts }
                    link += "&trimAboveRequestCounts="
                    if (board.trimAboveRequestCounts != null) { link += board.trimAboveRequestCounts }
                    link += "&trimBelowRequestSizes="
                    if (board.trimBelowRequestSizes != null) { link += board.trimBelowRequestSizes }
                    link += "&trimAboveRequestSizes="
                    if (board.trimAboveRequestSizes != null) { link += board.trimAboveRequestSizes }
                } else {
                    link += "&aggrGroup=" + board.aggrGroup
                    if (board.includeInterval != null) { link += "&includeInterval=on" }
                }
                result.add([dashboardName: board.dashboardName, link: link])
            }
        }
        return result
    }

	/*
	 * Methods of the Domain Class
	 */
//	@Override	// Override toString for a nicer / more descriptive UI
//	public String toString() {
//		return "${name}";
//	}
}

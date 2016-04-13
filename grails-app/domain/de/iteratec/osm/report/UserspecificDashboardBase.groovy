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
 * UserspecificDashboardBase
 * A domain class describes the data object and it's mapping to the database
 */
class UserspecificDashboardBase {
    public final static String DATE_FORMAT_STRING = 'dd.MM.yyyy'
    protected final static SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat(DATE_FORMAT_STRING)

    /**
     * name of dashboard
     */
    String dashboardName

    /**
     * name of user that created it
     */
    String username

    /**
     * if dashboard is visible to all or just to admins and to user that created it
     */
    Boolean publiclyVisible

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

    /**
     * toggle formatting rickshaw export to wide screen format
     */
    Boolean wideScreenDiagramMontage = false
    
	static constraints = {
        dashboardName(nullable: false, blank: false)
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
        selectedAllMeasuredEvents(nullable: true)
        selectedAllBrowsers(nullable: true)
        selectedAllLocations(nullable: true)
        debug(nullable: true)
        selectedTimeFrameInterval(nullable: true)
        setFromHour(nullable: true)
        setToHour(nullable: true)
        wideScreenDiagramMontage(nullable: true)
        chartTitle(nullable: true)
        loadTimeMaximum(nullable: true)
    }
}

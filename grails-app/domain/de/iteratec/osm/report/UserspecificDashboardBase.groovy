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
import de.iteratec.osm.result.TimeSeriesShowCommandBase
import org.joda.time.DateTime

/**
 * UserspecificDashboardBase
 * A domain class describes the data object and it's mapping to the database
 */
class UserspecificDashboardBase {

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
     */
    DateTime from

    /**
     * The selected end date (inclusive).
     *
     */
    DateTime to

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
     * The database IDs of the selected {@linkplain de.iteratec.osm.measurement.environment.Browser
     * browsers} which results to be shown.
     *
     * These selections are only relevant if
     * {@link #selectedAllBrowsers} is evaluated to
     * <code>false</code>.
     */
    String selectedBrowsers = ""

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
     * A predefined time frame.
     */
    int selectedTimeFrameInterval = 259200

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

    Collection<String> selectedConnectivities

    static hasMany = [selectedConnectivities:String]

    static constraints = {
        dashboardName(nullable: false, blank: false)
        username(nullable: true)
        publiclyVisible(nullable: true)
        from(nullable: true)
        to(nullable: true)
        selectedFolder(nullable: true)
        selectedPages(nullable: true)
        selectedMeasuredEventIds(nullable: true)
        selectedBrowsers(nullable: true)
        selectedLocations(nullable: true)
        selectedTimeFrameInterval(nullable: true)
        chartTitle(nullable: true)
        loadTimeMaximum(nullable: true)
        selectedConnectivities(nullable: true)
    }

    /**
     * Creates Userspecific Dashboard from TimeSeriesShowCommandBase
     * @param cmd the command the set values
     * @param dashboardName a unique name for the dashboard
     * @param publiclyVisible true if the dashboard should be visible for all
     * @param username the creator of the dashboard
     */
    UserspecificDashboardBase(TimeSeriesShowCommandBase cmd, String dashboardName, Boolean publiclyVisible, String username) {

        this.dashboardName = dashboardName
        this.publiclyVisible = Boolean.valueOf(publiclyVisible)
        this.username = username

        // Get Data from command
        from = cmd.from
        to = cmd.to
        selectedTimeFrameInterval = cmd.selectedTimeFrameInterval
        selectedConnectivities = cmd.selectedConnectivities
        chartTitle = cmd.chartTitle
        chartWidth = cmd.chartWidth
        chartHeight = cmd.chartHeight
        loadTimeMinimum = cmd.loadTimeMinimum
        loadTimeMaximum = cmd.loadTimeMaximum?:"auto"
        showDataMarkers = cmd.showDataMarkers
        showDataLabels = cmd.showDataLabels
        graphNameAliases = cmd.graphNameAliases
        graphColors = cmd.graphColors

        selectedFolder = cmd.selectedFolder.join(",")
        selectedPages = cmd.selectedPages.join(",")
        selectedMeasuredEventIds = cmd.selectedMeasuredEventIds.join(",")
        selectedBrowsers = cmd.selectedBrowsers.join(",")
        selectedLocations = cmd.selectedLocations.join(",")
    }
}

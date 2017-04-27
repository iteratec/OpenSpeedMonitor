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

    boolean overwriteWarningAboutLongProcessingTime = false

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
                             String dashboardName, String username) {
        super(cmd, dashboardName, publiclyVisible, username)
        aggrGroup = cmd.aggrGroupAndInterval
        includeInterval = cmd.includeInterval
        selectedCsiSystems = cmd.selectedCsiSystems.join(",")
        overwriteWarningAboutLongProcessingTime = cmd.overwriteWarningAboutLongProcessingTime
        csiTypeDocComplete = cmd.csiTypeDocComplete
        csiTypeVisuallyComplete = cmd.csiTypeVisuallyComplete
    }
}

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
    }

    /**
     * Creates Userspecific Dashboard from EventResultDashbordShowAllCommand
     * @param cmd the command the set values
     * @param dashboardName a unique name for the dashboard
     * @param publiclyVisible true if the dashboard should be visible for all
     * @param username the creator of the dashboard
     */
    UserspecificEventResultDashboard(EventResultDashboardShowAllCommand cmd, String dashboardName, Boolean publiclyVisible, String username) {
        super(cmd, dashboardName, publiclyVisible, username)

        selectedInterval = cmd.selectedInterval
        trimBelowLoadTimes = cmd.trimBelowLoadTimes
        trimAboveLoadTimes = cmd.trimAboveLoadTimes
        trimBelowRequestCounts = cmd.trimBelowRequestCounts
        trimAboveRequestCounts = cmd.trimAboveRequestCounts
        trimBelowRequestSizes = cmd.trimBelowRequestSizes
        trimAboveRequestSizes = cmd.trimAboveRequestSizes
        selectedAggrGroupValuesCached = cmd.selectedAggrGroupValuesCached.join(",")
        selectedAggrGroupValuesUnCached = cmd.selectedAggrGroupValuesUnCached.join(",")
    }

    void fillCommand(EventResultDashboardShowAllCommand cmd) {
        super.fillCommand(cmd)
        cmd.selectedInterval = selectedInterval
        cmd.trimBelowLoadTimes = trimBelowLoadTimes
        cmd.trimAboveLoadTimes = trimAboveLoadTimes
        cmd.trimBelowRequestCounts = trimBelowRequestCounts
        cmd.trimAboveRequestCounts = trimAboveRequestCounts
        cmd.trimBelowRequestSizes = trimBelowRequestSizes
        cmd.trimAboveRequestSizes = trimAboveRequestSizes
        selectedAggrGroupValuesCached = cmd.selectedAggrGroupValuesCached.join(",")
        selectedAggrGroupValuesUnCached = cmd.selectedAggrGroupValuesUnCached.join(",")

        if (selectedAggrGroupValuesCached) {
            for (item in selectedAggrGroupValuesCached.tokenize(',')) {
                cmd.selectedAggrGroupValuesCached.add(item)
            }
        }
        if (selectedAggrGroupValuesUnCached) {
            for (item in selectedAggrGroupValuesUnCached.tokenize(',')) {
                cmd.selectedAggrGroupValuesUnCached.add(item)
            }
        }
    }
}

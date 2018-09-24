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

import grails.plugin.springsecurity.SpringSecurityUtils
import grails.gorm.transactions.Transactional

/**
 * UserspecificDashboardService
 * A service class encapsulates the core business logic of a Grails application
 */
@Transactional
class UserspecificDashboardService {

    def springSecurityService

    /**
     * Checks if the currentUser is admin or creator of the given dashboard
     * @param dashboardId the dashboard to check
     * @return true if currentUser is admin or creator, false otherwise
     */
    public boolean isCurrentUserDashboardOwner(String dashboardId) {
        if (SpringSecurityUtils.ifAnyGranted("ROLE_ADMIN,ROLE_SUPER_ADMIN")) {
            return true
        } else {
            // get owner name
            UserspecificDashboardBase currentBoard = UserspecificDashboardBase.get(dashboardId)
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

    def getListOfAvailableEventResultDashboards() {
        return getDashboardsForUser(UserspecificEventResultDashboard.list())
    }


    def getListOfAvailableCsiDashboards() {
        return getDashboardsForUser(UserspecificCsiDashboard.list())
    }

    /**
     * filters given dashboardList so that dashboards are returned
     *  that are publicly visible or where the current user is the dashboard's owner
     * @param allDashboard the list of userspecificDashboard
     * @return a filterd list of userspecificDashboard
     */
    private List getDashboardsForUser(List<UserspecificDashboardBase> allDashboard) {
        List<UserspecificDashboardBase> result = []
        for (board in allDashboard) {
            if ((board.publiclyVisible) || isCurrentUserDashboardOwner(board.id.toString())) {
                result.add([dashboardName: board.dashboardName, dashboardID: board.id])
            }
        }

        result.sort(true) { it.dashboardName }
        return result
    }
}

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

package de.iteratec.osm.p13n

import de.iteratec.osm.report.UserspecificEventResultDashboard
import grails.transaction.Transactional

/**
 * CustomDashboardService
 * A service class encapsulates the core business logic of a Grails application
 */
@Transactional
class CustomDashboardService {


    /**
     * <p>
     *  Ajax service to confirm that dashboard name entered for saving custom dashboard was unique.
     * </p>
     *
     * @param proposedDashboardName
     *         The proposed Dashboard Name;
     *         not <code>null</code>.
     * @return nothing, immediately sends HTTP response codes to client.
     */
    def validateDashboardName(String proposedDashboardName) {
        UserspecificEventResultDashboard newCustomDashboard = new UserspecificEventResultDashboard(dashboardName: proposedDashboardName)
        if (!newCustomDashboard.validate()) {
            response.sendError(302, 'dashboard by that name exists already')
            return null
        } else {
            response.sendError(200, 'OK')
            return null
        }
    }

    /**
     * Parse a list of longs from a json entry. If this entry is not a list, this entry will be returned in a list.
     * @param json
     * @param name
     * @return
     */
    public Collection<Long> getValuesFromJSON(def json, String name){
        def collection = []
        def content = json?."$name"
        if(content){
            if(content instanceof String){
                collection << content
            } else{
                collection.addAll(content*.toLong())
            }
        }
        return collection
    }
}

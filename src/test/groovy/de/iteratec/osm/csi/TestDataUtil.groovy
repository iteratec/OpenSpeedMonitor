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

package de.iteratec.osm.csi

import de.iteratec.osm.OsmConfiguration
import de.iteratec.osm.security.Role
import de.iteratec.osm.security.User
import de.iteratec.osm.security.UserRole
import de.iteratec.osm.util.OsmTestLogin

/**
 * @author mze
 * @since IT-8
 */
class TestDataUtil implements OsmTestLogin {
    /**
     * <p>
     * Creates an OsmConfiguration and persists it.
     * This method uses default values for minValidLoadtime and maxValidLoadtime.
     * </p>
     */
    public static void createOsmConfig() {
        if (OsmConfiguration.count == 0) {
            OsmConfiguration.build().save(failOnError: true)
        }
    }

    public static User createAdminUser() {
        String adminUserName = getConfiguredUsername()
        User user = User.findByUsername(adminUserName)
        if (!user) {
            user = User.build(
                    username: adminUserName,
                    password: getConfiguredPassword(),
                    enabled: true,
                    accountExpired: false,
                    accountLocked: false,
                    passwordExpired: false
            )
            Role adminRole = Role.build(authority: 'ROLE_ADMIN')
            // UserRole doesn't work with build-test-data plugin :(
            new UserRole(user: user, role: adminRole).save(failOnError: true)
        }
        return user
    }
}

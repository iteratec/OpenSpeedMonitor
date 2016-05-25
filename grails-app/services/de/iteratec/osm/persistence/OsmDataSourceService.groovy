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

package de.iteratec.osm.persistence

import de.iteratec.osm.ConfigService
import grails.transaction.Transactional;

/**
 * OsmDataSourceService
 * A service class encapsulates the core business logic of a Grails application
 */
@Transactional
class OsmDataSourceService {
	
	ConfigService configService	
	
	/**
	 * Checks if rLike is supported in used databases
	 * @return <code>true</code> if it is supported
	 */
	public Boolean getRLikeSupport(){

        String actualDriverClassName = configService.getDatabaseDriverClassName()
        boolean isMysql = actualDriverClassName.equals("com.mysql.jdbc.Driver")
        boolean isOracle = actualDriverClassName.equals("oracle.jdbc.driver.OracleDriver")
        boolean isP6spy = actualDriverClassName.equals("com.p6spy.engine.spy.P6SpyDriver")

        return isMysql || isOracle || isP6spy

	}
}

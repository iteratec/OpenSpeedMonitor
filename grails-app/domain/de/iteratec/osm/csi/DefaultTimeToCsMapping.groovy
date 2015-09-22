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

/**
 * Default mappings 'load time to percentage of customers of your site who would be satisfied
 * by that load time' are defined by all elements of this domain with the same name.
 * These defaults can be used for pages if no data of a customer survey exist.

 * <b>Note:</b> Some of these defaults get created in Bootstrap.groovy.
 *
 */
class DefaultTimeToCsMapping implements RickshawTransformableCsMapping {

    String name
	Integer loadTimeInMilliSecs
	Double customerSatisfactionInPercent

    static constraints = {
        name()
		loadTimeInMilliSecs()
		customerSatisfactionInPercent()
    }

    public String retrieveGroupingCriteria(){
        return name
    }
    public Integer retrieveLoadTimeInMilliSecs(){
        return loadTimeInMilliSecs
    }
    public Double retrieveCustomerSatisfactionInPercent(){
        return customerSatisfactionInPercent
    }
}

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


class CsiConfiguration {

    String label
    String description
    List<HourOfDay> hourOfDays = []
    List<BrowserConnectivityWeight> browserConnectivityWeights = []
    List<PageWeight> pageWeights = []
    List<TimeToCsMapping> timeToCsMappings = []

    static hasMany = [hourOfDays:HourOfDay, browserConnectivityWeights:BrowserConnectivityWeight,
                      pageWeights:PageWeight, timeToCsMappings:TimeToCsMapping]


    static mapping = {
        description type: 'text'
    }

    static constraints = {
        label unique: true
        description nullable: true
    }
}

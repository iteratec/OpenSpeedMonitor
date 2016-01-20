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
 * CsiDay
 * A domain class describes the data object and it's mapping to the database
 */
class CsiDay {

    double hour0Weight
    double hour1Weight
    double hour2Weight
    double hour3Weight
    double hour4Weight
    double hour5Weight
    double hour6Weight
    double hour7Weight
    double hour8Weight
    double hour9Weight
    double hour10Weight
    double hour11Weight
    double hour12Weight
    double hour13Weight
    double hour14Weight
    double hour15Weight
    double hour16Weight
    double hour17Weight
    double hour18Weight
    double hour19Weight
    double hour20Weight
    double hour21Weight
    double hour22Weight
    double hour23Weight

    static belongsTo = [CsiConfiguration]
    static mapping = {
    }

    static constraints = {

    }

    public void setHourWeight(int hour, double weight) {
        String property = "hour" + hour + "Weight"
        this[property] = weight
    }

    public double getHourWeight(int hour) {
        String property = "hour" + hour + "Weight"
        return this[property]
    }

    static CsiDay copyDay(CsiDay source) {
        CsiDay toReturn = new CsiDay(source.properties)

        return toReturn
    }
}

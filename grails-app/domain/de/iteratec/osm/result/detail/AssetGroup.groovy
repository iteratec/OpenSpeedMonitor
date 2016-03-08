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

package de.iteratec.osm.result.detail

/**
 * AssetGroup
 * A group of assets which shares the same media type and belongs to the same event.
 */
class AssetGroup {

    String url
    boolean cached
    String eventName
    String title
    long page
    long jobGroup
    String connectivity
    long jobResult
    long location
    long browser
    //The Mongoplugin maps dates to a epoch time, but as String. So we manually persist it as long
    long date
    String mediaType
    long _id

    List<Asset> assets

    static mapWith = "mongo"
    static embedded = ['assets']
    static constraints = {
        url nullable: true
        title nullable: true
        eventName nullable: true
    }


}

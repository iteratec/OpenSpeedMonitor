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
    CsiDay csiDay
    List<BrowserConnectivityWeight> browserConnectivityWeights = []
    List<PageWeight> pageWeights = []
    List<TimeToCsMapping> timeToCsMappings = []

    static hasMany = [browserConnectivityWeights:BrowserConnectivityWeight,
                      pageWeights:PageWeight, timeToCsMappings:TimeToCsMapping]


    static mapping = {
        description type: 'text'
    }

    static constraints = {
        label unique: true
        description nullable: true
    }

    static CsiConfiguration copyConfiguration(CsiConfiguration source) {
        CsiConfiguration configToReturn = new CsiConfiguration()
        configToReturn.with {
            label = source.label
            description = source.description
            csiDay = CsiDay.copyDay(source.csiDay)
        }

        source.browserConnectivityWeights.each {
            configToReturn.addToBrowserConnectivityWeights(BrowserConnectivityWeight.copyBrowserConnectivityWeight(it))
        }

        source.pageWeights.each {
            configToReturn.addToPageWeights(PageWeight.copyPageWeight(it))
        }

        source.timeToCsMappings.each {
            configToReturn.addToTimeToCsMappings(TimeToCsMapping.copyTimeToCsMapping(it))
        }

        return configToReturn
    }

    public List<TimeToCsMapping> getTimeToCsMappingByPage(Page page) {
        return this.timeToCsMappings.findAll {it -> it.page.name == page.name}
    }
}

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

import de.iteratec.osm.util.PerformanceLoggingService


class CsiConfiguration {

    String label
    String description
    CsiDay csiDay

    Collection<BrowserConnectivityWeight> browserConnectivityWeights = []
    Collection<PageWeight> pageWeights = []
    Collection<TimeToCsMapping> timeToCsMappings = []

    static hasMany = [browserConnectivityWeights: BrowserConnectivityWeight,
                      pageWeights               : PageWeight,
                      timeToCsMappings          : TimeToCsMapping]


    static mapping = {
        description type: 'text'
        browserConnectivityWeights cascade: 'all-delete-orphan'
        pageWeights cascade: 'all-delete-orphan'
        timeToCsMappings cascade: 'all-delete-orphan'
    }

    static constraints = {
        label unique: true
        description nullable: true
    }

    static CsiConfiguration copyConfiguration(CsiConfiguration source) {
        CsiConfiguration configToReturn = new CsiConfiguration()

        PerformanceLoggingService performanceLoggingService = new PerformanceLoggingService()

        performanceLoggingService.logExecutionTime(
                PerformanceLoggingService.LogLevel.DEBUG,
                "creating CsiConfiguration with hour of day weights",
                1
        ){
            configToReturn.with {
                label = source.label
                description = source.description
                csiDay = CsiDay.copyDay(source.csiDay)
            }
        }
        performanceLoggingService.logExecutionTime(
                PerformanceLoggingService.LogLevel.DEBUG,
                "copy browser weights",
                1
        ){
            source.browserConnectivityWeights.each {
                configToReturn.addToBrowserConnectivityWeights(BrowserConnectivityWeight.copyBrowserConnectivityWeight(it))
            }
        }

        performanceLoggingService.logExecutionTime(
                PerformanceLoggingService.LogLevel.DEBUG,
                "copy page weights",
                1
        ){
            source.pageWeights.each {
                configToReturn.addToPageWeights(PageWeight.copyPageWeight(it))
            }
        }

//        configToReturn.save(failOnError: true)

        performanceLoggingService.logExecutionTime(
                PerformanceLoggingService.LogLevel.DEBUG,
                "copy time to cs mappings",
                1
        ){

//            TimeToCsMapping.executeUpdate(
//                    "update CsiConfiguration set timeToCsMappings=:sourceTimeToCsMappings where id=:newCsiConfId",
//                    [newCsiConfId: configToReturn.id, sourceTimeToCsMappings: source.timeToCsMappings]
////                    "insert into TimeToCsMapping(page, loadTimeInMilliSecs, customerSatisfaction, mappingVersion) select  from TimeToCsMapping where id in :sourceIds",
////                    [sourceIds: source.timeToCsMappings*.ident()]
//            )

            source.timeToCsMappings.each {
                configToReturn.addToTimeToCsMappings(TimeToCsMapping.copyTimeToCsMapping(it))
            }
        }

        return configToReturn
    }

    public List<TimeToCsMapping> getTimeToCsMappingByPage(Page page) {
        return this.timeToCsMappings.findAll { it -> it.page.name == page.name }
    }

    @Override
    public String toString(){
        return label
    }
}

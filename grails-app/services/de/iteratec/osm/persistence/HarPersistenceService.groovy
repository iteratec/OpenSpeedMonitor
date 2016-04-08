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

//import com.gmongo.GMongo
//import com.mongodb.AggregationOptions
//import com.mongodb.DB
//import com.mongodb.DBCollection
import de.iteratec.osm.result.JobResult
import de.iteratec.osm.result.JobResultDaoService
import de.iteratec.osm.result.detail.Asset
import de.iteratec.osm.result.detail.AssetGroup
import de.iteratec.osm.result.detail.HARStatus
import de.iteratec.osm.result.detail.HarConvertService

/**
 */
class HarPersistenceService {


    JobResultDaoService jobResultDaoService
    HarConvertService harConvertService
    static transactional = false


    /**
     * Parses a HAR and saves all Assets
     * @param result JobResult which belongs to this HAR
     * @param har The HAR which belongs to this JobResult
     * @return HARStatus PERSISTED or NOT_AVAILABLE
     */
    public void saveHARDataForJobResults(JobResult result, Map har) {
        HARStatus status = HARStatus.NOT_AVAILABLE
        if(har){
            List<AssetGroup> assetGroups = harConvertService.convertHarToAssetGroups(har, result)
            assetGroups.each {
                it.save(failOnError: true)
            }
            status = HARStatus.PERSISTED
        }
        jobResultDaoService.changeHARStatus(result.id, status)
        log.debug("JobResult ${result.id} harStatus: $status")
    }

    public def getAssets(Date from, Date to, List<Long> jobGroups, List<Long> pages, List<Long> browser, List<Long> locations, List<String> connectivity){
        Map matchList = [:]
//        GMongo mongo = new GMongo()
//        DB db = mongo.getDB("OpenSpeedMonitor")
//        matchList<<[date:[$gte:from.getTime(), $lte:to.getTime()]]
//        if(jobGroups) matchList["jobGroup"] = [$in:jobGroups]
//        if(pages) matchList << [pages:[$in:pages]]
//        if(browser) matchList << [browser:[$in:browser]]
//        if(locations) matchList << [location:[$in:locations]]
//        if(connectivity) matchList << [connectivity: [$in:connectivity]]
//
//        def options = AggregationOptions.builder().allowDiskUse(true).outputMode(AggregationOptions.OutputMode.CURSOR).build()
//        db.assetGroup.aggregate([[$match:matchList],
//                                 [$unwind:"\$assets"],
//                                 [$project:[
//                                         _id:0,
//                                         bytesIn:'\$assets.bytesIn',
//                                         bytesOut:'\$assets.bytesOut',
//                                         connectTime:'\$assets.connectTimeMs',
//                                         downloadTimeMs:'\$assets.downloadTimeMs',
//                                         loadTimeMs:'\$assets.loadTimeMs',
//                                         timeToFirstByteMs:'\$assets.timeToFirstByteMs',
//                                         indexWithinHar:'\$assets.indexWithinHar',
//                                         sslNegotiationTimeMs:'\$assets.sslNegotiationTimeMs',
//                                         mediaType:'\$assets.mediaType',
//                                         subtype:'\$assets.subtype',
//                                         url:1,
//                                         page:1,
//                                         //we rename this variable, because otherwise it may look like this specific asset was cached
//                                         //but the cached attribute belongs to the whole page
//                                         pageFromCache:'\$cached',
//                                         jobGroup:1,
//                                         connectivity:1,
//                                         location:1,
//                                         browser:1,
//                                         date:1
//                                 ]]
//        ],options).collect()
        return []
    }
}

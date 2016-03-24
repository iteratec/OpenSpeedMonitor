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

import de.iteratec.osm.result.JobResult
import de.iteratec.osm.result.JobResultDaoService
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
        }
        jobResultDaoService.changeHARStatus(result, status)
        log.debug("JobResult ${result.id} harStatus: $status")
    }


}

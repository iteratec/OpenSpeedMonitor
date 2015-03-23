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

package de.iteratec.osm.batch

/**
 * BatchActivityController
 * A controller class handles incoming web requests and performs actions such as redirects, rendering views and so on.
 */
class BatchActivityController {

    BatchActivityService batchActivityService
//    static Date last = new Date()

    def list(){
        [batchActivities:BatchActivity.list(sort: "startDate", order: "desc")]
    }

    def index(){
        redirect(action: 'list')
    }

    def edit(){
        [batchActivityInstance:  BatchActivity.get(params.id)]
    }

    def show(){
        [batchActivityInstance: BatchActivity.get(params.id)]
    }

    def delete(){
        BatchActivity activity = BatchActivity.get(params.id)
        activity.delete(flush: true)
        redirect(action: 'list')
    }

    /**
     *
     * @return new Content from BatchActivityTable
     */
    def updateTable(){
        render(view: '_batchActivityTable',model: [batchActivities:  BatchActivity.list()])
    }

    def checkForUpdate(){
        if(BatchActivity.findByStatus(Status.ACTIVE)){
            render("true")
        } else{
            render("false")
        }
    }
}

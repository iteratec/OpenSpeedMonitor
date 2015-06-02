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

import de.iteratec.osm.InMemoryConfigService
import de.iteratec.osm.api.json.BatchActivityRow
import de.iteratec.osm.util.I18nService
import grails.converters.JSON
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import org.springframework.http.HttpStatus

/**
 * BatchActivityController
 * A controller class handles incoming web requests and performs actions such as redirects, rendering views and so on.
 */
class BatchActivityController {

    public final static DateTimeFormatter DATE_FORMAT_BATCH_ACTIVITIES = DateTimeFormat.forPattern("dd. MMMMM. yyyy hh:mm")

    BatchActivityService batchActivityService
    InMemoryConfigService inMemoryConfigService
    I18nService i18nService

    def list(){
        params.order = params.order?:"desc"
        params.sort = params.sort?:"startDate"
        [batchActivities:BatchActivity.list(params), batchActivityCount:BatchActivity.count(), dbCleanupEnabled:inMemoryConfigService.isDatabaseCleanupEnabled()]
    }

    def index(){
        redirect(action: 'list',params: [max:10])
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

        params.order = "desc"
        params.sort = "startDate"
        params.max = 10

        String templateAsPlainText = g.render(
                template: 'batchActivityTable',
                model: [batchActivities:  BatchActivity.list(params),batchActivityCount:BatchActivity.count()]
        )
        sendSimpleResponseAsStream(response, HttpStatus.OK, templateAsPlainText)

    }

    /**
     *
     * @param activeIds ids of all BatchActivities to collect
     * @return JSON of all requested BatchActivities, ids without a matching BatchActivity will be ignored
     */
    def getUpdate(){
        def updates = []
        def ids = []
        if(params?.activeIds){
            ids.addAll(params.activeIds);
            ids.each{activeId ->
                BatchActivity batchActivity = BatchActivity.get(new Long(activeId as String))
                if(batchActivity){
                    updates.add(
                            new BatchActivityRow (
                                    htmlId: "batchActivity_${activeId}",
                                    activity: i18nService.msg(batchActivity.activity.getI18nCode(), batchActivity.activity.toString()),
                                    status: i18nService.msg(batchActivity.status.getI18nCode(),batchActivity.status.toString()),
                                    progress: batchActivity.progress,
                                    lastFailureMessage: batchActivity.lastFailureMessage,
                                    startDate: DATE_FORMAT_BATCH_ACTIVITIES.print(new DateTime(batchActivity.startDate)),
                                    lastUpdated: DATE_FORMAT_BATCH_ACTIVITIES.print(new DateTime(batchActivity.lastUpdated)),
                                    endDate: (batchActivity.endDate)? DATE_FORMAT_BATCH_ACTIVITIES.print(new DateTime(batchActivity.endDate)):"",
                                    statusEN: batchActivity.status.toString()
                            )
                    )
                } else{
                    log.error("Couldn't find a matching BatchActivty with id: $activeId")
                }
            }
        }

        render updates as JSON

    }

    def checkForUpdate(){
        if(BatchActivity.findByStatus(Status.ACTIVE)){
            render("true")
        } else{
            render("false")
        }
    }

    def activateDatabaseCleanup(){
        inMemoryConfigService.activateDatabaseCleanup()
        redirect(action: 'list', max: 10)
    }

    def deactivateDatabaseCleanup(){
        inMemoryConfigService.deactivateDatabaseCleanup()
        redirect(action: 'list', max: 10)
    }

    private void sendSimpleResponseAsStream(javax.servlet.http.HttpServletResponse response, HttpStatus httpStatus, String message) {

        response.setContentType('text/plain;charset=UTF-8')
        response.status=httpStatus.value()

        Writer textOut = new OutputStreamWriter(response.getOutputStream())
        textOut.write(message)
        textOut.flush()
        response.getOutputStream().flush()

    }
}

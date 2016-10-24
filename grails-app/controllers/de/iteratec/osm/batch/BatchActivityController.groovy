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
import de.iteratec.osm.api.dto.BatchActivityRowDto
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
        redirect(action: 'list',params: [max:100])
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
        def paramsForCount = Boolean.valueOf(params.limitResults) ? [max:1000]:[:]

        params.order = params.order ? params.order : "desc"
        params.sort = params.sort ? params.sort : "startDate"
        params.sort = params.sort == "remainingTime"? "status":params.sort  // remainingTime is a transient value - mysql
                                                                            // can't sort by it. But sorting by status should
                                                                            //display active batches first
        params.max = params.max as Integer
        params.offset = params.offset as Integer
        params.onlyActive = params.onlyActive=="true"?true:false
        List<BatchActivity> result
        int count
        result = BatchActivity.createCriteria().list(params) {
            if(params.filter)ilike("name","%"+params.filter+"%")
            if(params.onlyActive)eq("status",Status.ACTIVE)
        }
        count = BatchActivity.createCriteria().list(paramsForCount) {
            if(params.filter)ilike("name","%"+params.filter+"%")
            if(params.onlyActive)eq("status",Status.ACTIVE)
        }.size()

        String templateAsPlainText = g.render(
                template: 'batchActivityTable',
                model: [batchActivities: result]
        )
        def jsonResult = [table:templateAsPlainText, count:count]as JSON

        sendSimpleResponseAsStream(response, HttpStatus.OK, jsonResult.toString(false))
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
                        new BatchActivityRowDto (
                                htmlId: "batchActivity_${activeId}",
                                activity: i18nService.msg(batchActivity.activity.getI18nCode(), batchActivity.activity.toString()),
                                status: i18nService.msg(batchActivity.status.getI18nCode(),batchActivity.status.toString()),
                                stage: batchActivity.actualStage + "/" + batchActivity.maximumStages ,
                                progress: batchActivity.calculateProgressInStage() ,
                                lastFailureMessage: batchActivity.lastFailureMessage,
                                startDate: DATE_FORMAT_BATCH_ACTIVITIES.print(new DateTime(batchActivity.startDate)),
                                lastUpdated: DATE_FORMAT_BATCH_ACTIVITIES.print(new DateTime(batchActivity.lastUpdate)),
                                endDate: (batchActivity.endDate)? DATE_FORMAT_BATCH_ACTIVITIES.print(new DateTime(batchActivity.endDate)):"",
                                remainingTime: batchActivity.calculateRemainingTime(),
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
        def activeOnSite = 0
        if(params.activeCount && params.activeCount != ""){
            activeOnSite = params.activeCount as Integer
        }
        def activeBatches = BatchActivity.findAllByStatus(Status.ACTIVE).size()
        if(activeBatches!= activeOnSite){
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

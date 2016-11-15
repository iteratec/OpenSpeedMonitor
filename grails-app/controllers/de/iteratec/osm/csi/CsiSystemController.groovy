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

import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.report.chart.CsiAggregation
import de.iteratec.osm.util.ControllerUtils
import grails.converters.JSON
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus

import javax.servlet.http.HttpServletResponse

/**
 * CsiSystemController
 * A controller class handles incoming web requests and performs actions such as redirects, rendering views and so on.
 */
class CsiSystemController {

    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    def index() {
    }

    def list() {
        redirect(action: "index", params: params)
    }

    def create() {
        [csiSystem: new CsiSystem(params)]
    }

    def save() {
        def csiSystem = new CsiSystem(label: params.label)

        List<String> identifiers = params.jobGroupWeightIdentifiers.tokenize(',')

        boolean jobGroupsWeightsCorrect = true

        identifiers.each {
            def identifier = params[it]
            JobGroup jobGroup
            String weightString
            if (identifier) {
                jobGroup = JobGroup.get(identifier.jobGroup)
                weightString = identifier.weight
            }
            if (jobGroup && weightString && weightString.isDouble()) {
                Double weight = Double.parseDouble(weightString)
                csiSystem.addToJobGroupWeights(new JobGroupWeight(jobGroup: jobGroup, weight: weight))
            } else {
                jobGroupsWeightsCorrect = false
            }
        }

        if (!jobGroupsWeightsCorrect) {
            flash.error = message(code: 'de.iteratec.osm.csi.CsiSystem.weightError', default: "Gewichtungen muessen vom Typ Double sein")
            render(view: "create", model: [csiSystem: csiSystem])
            return
        }


        if (!csiSystem.save(flush: true)) {
            render(view: "create", model: [csiSystem: csiSystem])
            return
        }

        flash.message = message(code: 'default.created.message', args: [message(code: 'csiSystem.label', default: 'CsiSystem'), csiSystem.id])
        redirect(action: "show", id: csiSystem.id)
    }

    def show() {
        def csiSystem = CsiSystem.get(params.id)
        if (!csiSystem) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'csiSystem.label', default: 'CsiSystem'), params.id])
            redirect(action: "list")
            return
        }

        [csiSystem: csiSystem]
    }

    def edit() {
        def csiSystem = CsiSystem.get(params.id)
        if (!csiSystem ) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'csiSystem.label', default: 'CsiSystem'), params.id])
            redirect(action: "list")
            return
        }

        [csiSystem: csiSystem ]
    }

    def update() {
        def csiSystem = CsiSystem.get(params.id)
        if (!csiSystem) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'csiSystem.label', default: 'CsiSystem'), params.id])
            redirect(action: "list")
            return
        }

        if (params.version) {
            def version = params.version.toLong()
            if (csiSystem.version > version) {
                csiSystem.errors.rejectValue("version", "default.optimistic.locking.failure",
                        [message(code: 'csiSystem.label', default: 'CsiSystem')] as Object[],
                        "Another user has updated this CsiSystem while you were editing")
                render(view: "edit", model: [csiSystem: csiSystem])
                return
            }
        }

        csiSystem.label = params.label
        List<String> identifiers = params.jobGroupWeightIdentifiers.tokenize(',')

        csiSystem.jobGroupWeights.collect().each {
            csiSystem.removeFromJobGroupWeights(it)
            it.delete()
        }

        boolean jobGroupsWeightsHaveErrors = false

        identifiers.each {
            JobGroup jobGroup
            String weightString
            def identifier = params[it]
            if (identifier) {
                jobGroup = JobGroup.get(identifier.jobGroup)
                weightString = identifier.weight
            }
            if (jobGroup && weightString && weightString.isDouble()) {
                Double weight = Double.parseDouble(weightString)
                JobGroupWeight newWeight = new JobGroupWeight(jobGroup: jobGroup, weight: weight)
                csiSystem.addToJobGroupWeights(newWeight)
            } else {
                jobGroupsWeightsHaveErrors = true
            }
        }

        if (jobGroupsWeightsHaveErrors) {
            flash.error = message(code: 'de.iteratec.osm.csi.CsiSystem.weightError', default: "Gewichtungen muessen vom Typ Double sein")
            render(view: "edit", model: [csiSystem: csiSystem])
            return
        }

        if (!csiSystem.save(flush: true)) {
            render(view: "edit", model: [csiSystem: csiSystem])
            return
        }

        flash.message = message(code: 'default.updated.message', args: [message(code: 'csiSystem.label', default:
                'CsiSystem'), csiSystem.id])
        redirect(action: "show", id: csiSystem.id)
    }

    def delete() {
        def csiSystem = CsiSystem.get(params.id)
        if (!csiSystem) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'csiSystem.label', default: 'CsiSystem'), params.id])
            redirect(action: "list")
            return
        }

        try {
            csiSystem.jobGroupWeights.collect().each {
                csiSystem.removeFromJobGroupWeights(it)
                it.delete()
            }

            CsiAggregation.findAllByCsiSystem(csiSystem)*.delete()

            csiSystem.delete(flush: true)
            flash.message = message(code: 'default.deleted.message', args: [message(code: 'csiSystem.label', default: 'CsiSystem'), params.id])
            redirect(action: "list")
        }
        catch (DataIntegrityViolationException e) {
            flash.message = message(code: 'default.not.deleted.message', args: [message(code: 'csiSystem.label', default: 'CsiSystem'), params.id])
            redirect(action: "show", id: params.id)
        }
    }
    /**
     * Creates a text to represent which data will be gone if the job with the given id will be deleted
     * @param id Job id
     * @return
     */
    def createDeleteConfirmationText(int id) {
        CsiSystem csiSystem1 = CsiSystem.get(id)
        def query = CsiAggregation.where { csiSystem == csiSystem1 }
        List<Date> dateList = CsiAggregation.createCriteria().get {
            eq("csiSystem", csiSystem1)
            projections {
                min "started"
                max "started"
            }
        }
        Date minDate
        Date maxDate
        if (dateList.size() > 1) {
            minDate = dateList[0]
            maxDate = dateList[1]
        }
        int count = query.count()

        String first = minDate ? "${g.message(code: "de.iteratec.osm.measurement.schedule.JobController.firstResult", default: "Date of first result")}: ${minDate.format('dd.MM.yy')} <br>" : ""
        String last = maxDate ? "${g.message(code: "de.iteratec.osm.measurement.schedule.JobController.lastResult", default: "Date of last result")}: ${maxDate.format('dd.MM.yy')} <br>" : ""
        render("$first$last" + "${g.message(code: "de.iteratec.osm.measurement.schedule.JobController.resultAmount", default: "Amount of results")}: ${count}")
    }
    def updateTable(){
        params.order = params.order ? params.order : "asc"
        params.sort = params.sort ? params.sort : "label"

        params.max = params.max as Integer
        params.offset = params.offset as Integer
        List<CsiSystem> result = CsiSystem.createCriteria().list(params) {
            if(params.filter)ilike("label","%"+params.filter+"%")
        }
        String templateAsPlainText = g.render(
                template: 'csiSystemTable',
                model: [csiSystems: result]
        )
        ControllerUtils.sendObjectAsJSON(response, [
                table: templateAsPlainText,
                count: result.totalCount
        ])
    }
}

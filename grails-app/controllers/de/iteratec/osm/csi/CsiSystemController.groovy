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
import org.springframework.dao.DataIntegrityViolationException

/**
 * CsiSystemController
 * A controller class handles incoming web requests and performs actions such as redirects, rendering views and so on.
 */
class CsiSystemController {

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def index() {
        redirect(action: "list", params: params)
    }

    def list() {
        params.max = Math.min(params.max ? params.int('max') : 10, 100)
        [csiSystemInstanceList: CsiSystem.list(params), csiSystemInstanceTotal: CsiSystem.count()]
    }

    def create() {
        [csiSystemInstance: new CsiSystem(params)]
    }

    def save() {
        def csiSystemInstance = new CsiSystem(label: params.label)

        List<String> identifiers = params.jobGroupWeightIdentifiers.tokenize(',')

        boolean jobGroupsWeightsCorrect = true

        identifiers.each {
            JobGroup jobGroup = JobGroup.get(params[it].jobGroup)
            String weightString = params[it].weight
            if (weightString && weightString.isDouble() && jobGroup) {
                Double weight = Double.parseDouble(weightString)
                csiSystemInstance.addToJobGroupWeights(new JobGroupWeight(jobGroup: jobGroup, weight: weight))
            } else {
                jobGroupsWeightsCorrect = false
            }
        }


        if (!jobGroupsWeightsCorrect || !csiSystemInstance.save(flush: true)) {
            render(view: "create", model: [csiSystemInstance: csiSystemInstance])
            return
        }

        flash.message = message(code: 'default.created.message', args: [message(code: 'csiSystem.label', default: 'CsiSystem'), csiSystemInstance.id])
        redirect(action: "show", id: csiSystemInstance.id)
    }

    def show() {
        def csiSystemInstance = CsiSystem.get(params.id)
        if (!csiSystemInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'csiSystem.label', default: 'CsiSystem'), params.id])
            redirect(action: "list")
            return
        }

        [csiSystemInstance: csiSystemInstance]
    }

    def edit() {
        def csiSystemInstance = CsiSystem.get(params.id)
        if (!csiSystemInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'csiSystem.label', default: 'CsiSystem'), params.id])
            redirect(action: "list")
            return
        }

        [csiSystemInstance: csiSystemInstance]
    }

    def update() {
        def csiSystemInstance = CsiSystem.get(params.id)
        if (!csiSystemInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'csiSystem.label', default: 'CsiSystem'), params.id])
            redirect(action: "list")
            return
        }

        if (params.version) {
            def version = params.version.toLong()
            if (csiSystemInstance.version > version) {
                csiSystemInstance.errors.rejectValue("version", "default.optimistic.locking.failure",
                        [message(code: 'csiSystem.label', default: 'CsiSystem')] as Object[],
                        "Another user has updated this CsiSystem while you were editing")
                render(view: "edit", model: [csiSystemInstance: csiSystemInstance])
                return
            }
        }

        csiSystemInstance.label = params.label
        List<String> identifiers = params.jobGroupWeightIdentifiers.tokenize(',')

        boolean jobGroupsWeightsHaveErrors = false

        List<Long> updatedJobGroupWeightIds = []

        identifiers.each {
            if (params[it].deleted == "false") {
                JobGroup jobGroup = JobGroup.get(params[it].jobGroup)
                String weightString = params[it].weight

                if (weightString && weightString.isDouble() && jobGroup) {
                    Double weight = Double.parseDouble(weightString)
                    JobGroupWeight oldJobGroupWeight = csiSystemInstance.jobGroupWeights.find {
                        it.jobGroup == jobGroup
                    }
                    if (oldJobGroupWeight && oldJobGroupWeight.weight != weight) {
                        // An existing jobGroupWeight gets a new weight
                        oldJobGroupWeight.weight = weight
                        updatedJobGroupWeightIds.add(oldJobGroupWeight.id)
                    } else if (!oldJobGroupWeight) {
                        JobGroupWeight newWeight = new JobGroupWeight(jobGroup: jobGroup, weight: weight)
                        // A new jobGroupWeight is added
                        csiSystemInstance.addToJobGroupWeights(newWeight)
                        updatedJobGroupWeightIds.add(newWeight.id)
                    } else {    // an existing jobGroupWeight that doesn't have to be updated
                        updatedJobGroupWeightIds.add(oldJobGroupWeight.id)
                    }
                } else {
                    jobGroupsWeightsHaveErrors = true
                }
            }
        }

        if (jobGroupsWeightsHaveErrors) {
            // TODOMARCUS error erstellen
            flash.message = message(code: 'sadfjs', default: "Vom Typ Double")
            render(view: "edit", model: [csiSystemInstance: csiSystemInstance])
            return
        }

        // Delete all JobGroupWeights wich are deprecated
        List<JobGroupWeight> toDelete = csiSystemInstance.jobGroupWeights.findAll { w -> !updatedJobGroupWeightIds.contains(w.id) }
        toDelete.each { d ->
            csiSystemInstance.removeFromJobGroupWeights(d)
            d.delete()
        }

        if (!csiSystemInstance.save(flush: true)) {
            render(view: "edit", model: [csiSystemInstance: csiSystemInstance])
            return
        }

        flash.message = message(code: 'default.updated.message', args: [message(code: 'csiSystem.label', default: 'CsiSystem'), csiSystemInstance.id])
        redirect(action: "show", id: csiSystemInstance.id)
    }

    def delete() {
        def csiSystemInstance = CsiSystem.get(params.id)
        if (!csiSystemInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'csiSystem.label', default: 'CsiSystem'), params.id])
            redirect(action: "list")
            return
        }

        try {
            csiSystemInstance.delete(flush: true)
            flash.message = message(code: 'default.deleted.message', args: [message(code: 'csiSystem.label', default: 'CsiSystem'), params.id])
            redirect(action: "list")
        }
        catch (DataIntegrityViolationException e) {
            flash.message = message(code: 'default.not.deleted.message', args: [message(code: 'csiSystem.label', default: 'CsiSystem'), params.id])
            redirect(action: "show", id: params.id)
        }
    }
}

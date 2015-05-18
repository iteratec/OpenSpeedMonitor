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

package de.iteratec.osm.measurement.schedule

import org.springframework.dao.DataIntegrityViolationException

/**
 * JobGroupController
 * A controller class handles incoming web requests and performs actions such as redirects, rendering views and so on.
 */
class JobGroupController {

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def index() {
        redirect(action: "list", params: params)
    }

    def list() {
        params.max = Math.min(params.max ? params.int('max') : 10, 100)
        [jobGroupInstanceList: JobGroup.list(params), jobGroupInstanceTotal: JobGroup.count()]
    }

    def create() {
        [jobGroupInstance: new JobGroup(params)]
    }

    def save() {
        def jobGroupInstance = new JobGroup(params)
        if (!jobGroupInstance.save(flush: true)) {
            render(view: "create", model: [jobGroupInstance: jobGroupInstance])
            return
        }

		flash.message = message(code: 'default.created.message', args: [message(code: 'jobGroup.label', default: 'JobGroup'), jobGroupInstance.id])
        redirect(action: "show", id: jobGroupInstance.id)
    }

    def show() {
        def jobGroupInstance = JobGroup.get(params.id)
        if (!jobGroupInstance) {
			flash.message = message(code: 'default.not.found.message', args: [message(code: 'jobGroup.label', default: 'JobGroup'), params.id])
            redirect(action: "list")
            return
        }

        [jobGroupInstance: jobGroupInstance]
    }

    def edit() {
        def jobGroupInstance = JobGroup.get(params.id)
        if (!jobGroupInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'jobGroup.label', default: 'JobGroup'), params.id])
            redirect(action: "list")
            return
        }

        [jobGroupInstance: jobGroupInstance]
    }

    def update() {
        def jobGroupInstance = JobGroup.get(params.id)
        if (!jobGroupInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'jobGroup.label', default: 'JobGroup'), params.id])
            redirect(action: "list")
            return
        }
        if (!(params?.graphiteServers)) {
            jobGroupInstance.graphiteServers.clear()
        }
        if (params.version) {
            def version = params.version.toLong()
            if (jobGroupInstance.version > version) {
                jobGroupInstance.errors.rejectValue("version", "default.optimistic.locking.failure",
                          [message(code: 'jobGroup.label', default: 'JobGroup')] as Object[],
                          "Another user has updated this JobGroup while you were editing")
                render(view: "edit", model: [jobGroupInstance: jobGroupInstance])
                return
            }
        }

        jobGroupInstance.properties = params

        if (!jobGroupInstance.save(flush: true)) {
            render(view: "edit", model: [jobGroupInstance: jobGroupInstance])
            return
        }

		flash.message = message(code: 'default.updated.message', args: [message(code: 'jobGroup.label', default: 'JobGroup'), jobGroupInstance.id])
        redirect(action: "show", id: jobGroupInstance.id)
    }

    def delete() {
        def jobGroupInstance = JobGroup.get(params.id)
        if (!jobGroupInstance) {
			flash.message = message(code: 'default.not.found.message', args: [message(code: 'jobGroup.label', default: 'JobGroup'), params.id])
            redirect(action: "list")
            return
        }

        try {
            jobGroupInstance.delete(flush: true)
			flash.message = message(code: 'default.deleted.message', args: [message(code: 'jobGroup.label', default: 'JobGroup'), params.id])
            redirect(action: "list")
        }
        catch (DataIntegrityViolationException e) {
			flash.message = message(code: 'default.not.deleted.message', args: [message(code: 'jobGroup.label', default: 'JobGroup'), params.id])
            redirect(action: "show", id: params.id)
        }
    }
}

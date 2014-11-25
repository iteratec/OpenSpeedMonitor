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
 * ConnectivityProfileController
 * A controller class handles incoming web requests and performs actions such as redirects, rendering views and so on.
 */
class ConnectivityProfileController {

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def index() {
        redirect(action: "list", params: params)
    }

    def list() {
        params.max = Math.min(params.max ? params.int('max') : 10, 100)
        [connectivityProfileInstanceList: ConnectivityProfile.list(params), connectivityProfileInstanceTotal: ConnectivityProfile.count()]
    }

    def create() {
        [connectivityProfileInstance: new ConnectivityProfile(params)]
    }

    def save() {
        def connectivityProfileInstance = new ConnectivityProfile(params)
        if (!connectivityProfileInstance.save(flush: true)) {
            render(view: "create", model: [connectivityProfileInstance: connectivityProfileInstance])
            return
        }

		flash.message = message(code: 'default.created.message', args: [message(code: 'connectivityProfile.label', default: 'ConnectivityProfile'), connectivityProfileInstance.name])
        redirect(action: "list")
    }

    def edit() {
        def connectivityProfileInstance = ConnectivityProfile.get(params.id)
        if (!connectivityProfileInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'connectivityProfile.label', default: 'ConnectivityProfile'), params.id])
            redirect(action: "list")
            return
        }

        [connectivityProfileInstance: connectivityProfileInstance]
    }

    def update() {
        def connectivityProfileInstance = ConnectivityProfile.get(params.id)
        if (!connectivityProfileInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'connectivityProfile.label', default: 'ConnectivityProfile'), params.id])
            redirect(action: "list")
            return
        }

        if (params.version) {
            def version = params.version.toLong()
            if (connectivityProfileInstance.version > version) {
                connectivityProfileInstance.errors.rejectValue("version", "default.optimistic.locking.failure",
                          [message(code: 'connectivityProfile.label', default: 'ConnectivityProfile')] as Object[],
                          "Another user has updated this ConnectivityProfile while you were editing")
                render(view: "edit", model: [connectivityProfileInstance: connectivityProfileInstance])
                return
            }
        }

        connectivityProfileInstance.properties = params

        if (!connectivityProfileInstance.save(flush: true)) {
            render(view: "edit", model: [connectivityProfileInstance: connectivityProfileInstance])
            return
        }

		flash.message = message(code: 'default.updated.message', args: [message(code: 'connectivityProfile.label', default: 'ConnectivityProfile'), connectivityProfileInstance.name])
        redirect(action: "list")
    }

    def delete() {
        def connectivityProfileInstance = ConnectivityProfile.get(params.id)
        if (!connectivityProfileInstance) {
			flash.message = message(code: 'default.not.found.message', args: [message(code: 'connectivityProfile.label', default: 'ConnectivityProfile'), params.id])
            redirect(action: "list")
            return
        }

        try {
            connectivityProfileInstance.delete(flush: true)
			flash.message = message(code: 'default.deleted.message', args: [message(code: 'connectivityProfile.label', default: 'ConnectivityProfile'), connectivityProfileInstance.name])
            redirect(action: "list")
        }
        catch (DataIntegrityViolationException e) {
			flash.message = message(code: 'default.not.deleted.message', args: [message(code: 'connectivityProfile.label', default: 'ConnectivityProfile'), params.id])
            redirect(action: "list")
        }
    }
}

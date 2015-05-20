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

package de.iteratec.osm.report.external

import org.springframework.dao.DataIntegrityViolationException

/**
 * GraphiteServerController
 * A controller class handles incoming web requests and performs actions such as redirects, rendering views and so on.
 */
class GraphiteServerController {

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def index() {
        redirect(action: "list", params: params)
    }

    def list() { log.error("rklist")
        params.max = Math.min(params.max ? params.int('max') : 10, 100)
        [graphiteServerInstanceList: GraphiteServer.list(params), graphiteServerInstanceTotal: GraphiteServer.count()]
    }

    def create() {
        [graphiteServerInstance: new GraphiteServer(params)]
    }

    def save() {
        def graphiteServerInstance = new GraphiteServer(params)
        if (!graphiteServerInstance.save(flush: true)) {
            render(view: "create", model: [graphiteServerInstance: graphiteServerInstance])
            return
        }

		flash.message = message(code: 'default.created.message', args: [message(code: 'graphiteServer.label', default: 'GraphiteServer'), graphiteServerInstance.id])
        redirect(action: "show", id: graphiteServerInstance.id)
    }

    def show() {
        def graphiteServerInstance = GraphiteServer.get(params.id)
        if (!graphiteServerInstance) {
			flash.message = message(code: 'default.not.found.message', args: [message(code: 'graphiteServer.label', default: 'GraphiteServer'), params.id])
            redirect(action: "list")
            return
        }

        [graphiteServerInstance: graphiteServerInstance]
    }

    def edit() {
        def graphiteServerInstance = GraphiteServer.get(params.id)
        if (!graphiteServerInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'graphiteServer.label', default: 'GraphiteServer'), params.id])
            redirect(action: "list")
            return
        }

        [graphiteServerInstance: graphiteServerInstance]
    }

    def update() {
        def graphiteServerInstance = GraphiteServer.get(params.id)
        if (!graphiteServerInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'graphiteServer.label', default: 'GraphiteServer'), params.id])
            redirect(action: "list")
            return
        }
        if (!(params?.graphitePaths)) {
            graphiteServerInstance.graphitePaths.clear()
        }
        if (params.version) {
            def version = params.version.toLong()
            if (graphiteServerInstance.version > version) {
                graphiteServerInstance.errors.rejectValue("version", "default.optimistic.locking.failure",
                          [message(code: 'graphiteServer.label', default: 'GraphiteServer')] as Object[],
                          "Another user has updated this GraphiteServer while you were editing")
                render(view: "edit", model: [graphiteServerInstance: graphiteServerInstance])
                return
            }
        }

        graphiteServerInstance.properties = params

        if (!graphiteServerInstance.save(flush: true)) {
            render(view: "edit", model: [graphiteServerInstance: graphiteServerInstance])
            return
        }

		flash.message = message(code: 'default.updated.message', args: [message(code: 'graphiteServer.label', default: 'GraphiteServer'), graphiteServerInstance.id])
        redirect(action: "show", id: graphiteServerInstance.id)
    }

    def delete() {
        def graphiteServerInstance = GraphiteServer.get(params.id)
        if (!graphiteServerInstance) {
			flash.message = message(code: 'default.not.found.message', args: [message(code: 'graphiteServer.label', default: 'GraphiteServer'), params.id])
            redirect(action: "list")
            return
        }

        try {
            graphiteServerInstance.delete(flush: true)
			flash.message = message(code: 'default.deleted.message', args: [message(code: 'graphiteServer.label', default: 'GraphiteServer'), params.id])
            redirect(action: "list")
        }
        catch (DataIntegrityViolationException e) {
			flash.message = message(code: 'default.not.deleted.message', args: [message(code: 'graphiteServer.label', default: 'GraphiteServer'), params.id])
            redirect(action: "show", id: params.id)
        }
    }
}

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

import de.iteratec.osm.measurement.schedule.Job
import de.iteratec.osm.measurement.script.Script
import de.iteratec.osm.measurement.script.ScriptService
import de.iteratec.osm.result.MeasuredEvent
import de.iteratec.osm.result.PageService
import de.iteratec.osm.result.SetupDashboardService
import de.iteratec.osm.util.ControllerUtils
import de.iteratec.osm.util.I18nService
import groovy.transform.EqualsAndHashCode
import org.hibernate.criterion.CriteriaSpecification

/**
 * PageController
 * A controller class handles incoming web requests and performs actions such as redirects, rendering views and so on.
 */
class PageController {
    PageService pageService
    SetupDashboardService setupDashboardService
    I18nService i18nService

    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    def index() {

    }

    def list() {
        redirect(action: "index", params: params)
    }

    def create() {
        [pageInstance: new Page(params)]
    }

    def save() {
        def pageInstance = new Page(params)
        if (!pageInstance.save(flush: true)) {
            render(view: "create", model: [pageInstance: pageInstance])
            return
        }

        flash.message = message(code: 'default.created.message', args: [message(code: 'page.label', default: 'Page'), pageInstance.id])
        redirect(action: "show", id: pageInstance.id)
    }

    def show() {

        Page pageInstance = Page.get(params.id)
        if (!pageInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'page.label', default: 'Page'), params.id])
            redirect(action: "list")
            return
        }

        return [pageInstance: pageInstance]
    }

    def edit() {
        def pageInstance = Page.get(params.id)
        if (!pageInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'page.label', default: 'Page'), params.id])
            redirect(action: "list")
            return
        }

        return [pageInstance: pageInstance]
    }

    def update() {
        def pageInstance = Page.get(params.id)
        if (!pageInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'page.label', default: 'Page'), params.id])
            redirect(action: "list")
            return
        }

        if (params.version) {
            def version = params.version.toLong()
            if (pageInstance.version > version) {
                pageInstance.errors.rejectValue("version", "default.optimistic.locking.failure",
                        [message(code: 'page.label', default: 'Page')] as Object[],
                        "Another user has updated this Page while you were editing")
                render(view: "edit", model: [pageInstance: pageInstance])
                return
            }
        }

        pageInstance.properties = params

        if (!pageInstance.save(flush: true)) {
            render(view: "edit", model: [pageInstance: pageInstance])
            return
        }

        flash.message = message(code: 'default.updated.message', args: [message(code: 'page.label', default: 'Page'), pageInstance.id])
        redirect(action: "show", id: pageInstance.id)
    }

    def updateTable() {
        params.order = params.order ? params.order : "asc"
        params.sort = params.sort ? params.sort : "name"
        params.max = params.max as Integer
        params.offset = params.offset as Integer
        List<Page> result
        int count
        result = Page.createCriteria().list(params) {
            if (params.filter) ilike("name", "%" + params.filter + "%")
        }
        String templateAsPlainText = g.render(
                template: 'pageTable',
                model: [pages: result]
        )
        ControllerUtils.sendObjectAsJSON(response, [
                table: templateAsPlainText,
                count: result.totalCount
        ])
    }

    def getPagesForMeasuredEvents(GetPagesForMeasuredEventsCommand command) {
        render command.measuredEventList*.testedPageId as Set
    }

    def getPagesForActiveJobGroups() {

        return ControllerUtils.sendObjectAsJSON(response, setupDashboardService.getPagesForActiveJobGroups())
    }
}

class GetPagesForMeasuredEventsCommand {
    List<MeasuredEvent> measuredEventList

    static constraints = {
        measuredEventList(nullable: false)
    }
}
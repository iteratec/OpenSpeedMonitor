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

import de.iteratec.osm.csi.transformation.DefaultTimeToCsMappingService
import de.iteratec.osm.csi.transformation.TimeToCsMappingCacheService
import de.iteratec.osm.d3Data.MultiLineChart
import de.iteratec.osm.d3Data.MultiLineChartLineData
import de.iteratec.osm.report.chart.RickshawHtmlCreater
import de.iteratec.osm.util.I18nService
import grails.converters.JSON
import org.springframework.dao.DataIntegrityViolationException

/**
 * PageController
 * A controller class handles incoming web requests and performs actions such as redirects, rendering views and so on.
 */
class PageController {

    TimeToCsMappingCacheService timeToCsMappingCacheService
    DefaultTimeToCsMappingService defaultTimeToCsMappingService
    I18nService i18nService

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def index() {
        redirect(action: "list", params: params)
    }

    def list() {
        params.max = Math.min(params.max ? params.int('max') : 10, 100)
        [pageInstanceList: Page.list(params), pageInstanceTotal: Page.count()]
    }

    def create() {
        List<DefaultTimeToCsMapping> defaultTimeToCsMappings = defaultTimeToCsMappingService.getAll().findAll {
            it.loadTimeInMilliSecs <= 8000
        }

        def defaultTimeToCsMappingsChart = createMultiLineChartData(new ArrayList<Page>())

        [pageInstance   : new Page(params),
         defaultMappings: defaultTimeToCsMappings,
         defaultMultiLineChart: defaultTimeToCsMappingsChart as JSON
        ]
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
        List<TimeToCsMapping> timeToCsMappings = timeToCsMappingCacheService.getMappingsFor(pageInstance)

        // Arrange chart data
        List<Page> pages = new ArrayList<Page>()
        pages.add(pageInstance)
        MultiLineChart multiLineChart = createMultiLineChartData(pages)

        return [
                pageInstance  : pageInstance,
                mappingsOfPage: timeToCsMappings,
                multiLineChart: multiLineChart as JSON
        ]
    }

    def edit() {
        def pageInstance = Page.get(params.id)
        if (!pageInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'page.label', default: 'Page'), params.id])
            redirect(action: "list")
            return
        }

        // Arrange chart data
        List<Page> pages = new ArrayList<Page>()
        pages.add(pageInstance)
        MultiLineChart multiLineChart = createMultiLineChartData(pages)

        MultiLineChart defaultTimeToCsMappingsChart = createMultiLineChartData(new ArrayList<Page>())

        List<TimeToCsMapping> timeToCsMappings = timeToCsMappingCacheService.getMappingsFor(pageInstance)
        List<DefaultTimeToCsMapping> defaultTimeToCsMappings = defaultTimeToCsMappingService.getAll().findAll {
            it.loadTimeInMilliSecs <= 8000
        }
        return [
                pageInstance         : pageInstance,
                mappingsOfPage       : timeToCsMappings,
                defaultMappings      : defaultTimeToCsMappings,
                multiLineChart       : multiLineChart as JSON,
                defaultMultiLineChart: defaultTimeToCsMappingsChart as JSON
        ]
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

    def delete() {
        def pageInstance = Page.get(params.id)
        if (!pageInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'page.label', default: 'Page'), params.id])
            redirect(action: "list")
            return
        }

        try {
            pageInstance.delete(flush: true)
            flash.message = message(code: 'default.deleted.message', args: [message(code: 'page.label', default: 'Page'), params.id])
            redirect(action: "list")
        }
        catch (DataIntegrityViolationException e) {
            flash.message = message(code: 'default.not.deleted.message', args: [message(code: 'page.label', default: 'Page'), params.id])
            redirect(action: "show", id: params.id)
        }
    }

    /**
     * Gets called asynchronously from modal dialog.
     * @return
     */
    def applyMappingToPage() {

        response.setContentType('application/json')

        Page page = Page.findByName(params.page)
        if (!page) {
            response.sendError(404, "No page with name ${params.page} exists!")
        }
        try {
            defaultTimeToCsMappingService.copyDefaultMappingToPage(page, params.selectedDefaultMapping)
        } catch (IllegalArgumentException iae) {
            response.sendError(404, "No default csi mapping with name ${params.selectedDefaultMapping} exists!")
        }

        sendSimpleResponseAsStream(
                response,
                200,
                new RickshawHtmlCreater().transformCSIMappingData(
                        timeToCsMappingCacheService.getMappingsFor(page)
                ).toString()
        )
//        render(contentType: "application/json") {
//            timeToCsMappingCacheService.getMappingsFor(page)
//		}
    }

    /**
     * Sends error message with given error httpStatus and message as http response and breaks action (no subsequent
     * action code is executed).
     * @param response
     * @param httpStatus
     * @param message
     */
    private void sendSimpleResponseAsStream(javax.servlet.http.HttpServletResponse response, Integer httpStatus, String message) {
        response.setContentType('text/plain;charset=UTF-8')
        response.status = httpStatus

        Writer textOut = new OutputStreamWriter(response.getOutputStream())
        textOut.write(message)
        response.status = httpStatus

        textOut.flush()
        response.getOutputStream().flush()

        render ''
    }

    /**
     * Creates the data for a multiLineChart with a line for each page.
     * If pages.size == 0 or pages == null it creates chart for defaultTimeToCsMapping
     * @param pages a list of pages to create the chart for
     * @return the MultiLineChart
     */
    private MultiLineChart createMultiLineChartData(List<Page> pages) {
        String xLabel = i18nService.msg("de.iteratec.osm.d3Data.multiLineChart.xAxisLabel", "ms")
        String yLabel = i18nService.msg("de.iteratec.osm.d3Data.multiLineChart.yAxisLabel", "Kundenzufriedenheit in %")
        MultiLineChart multiLineChart = new MultiLineChart(xLabel: xLabel, yLabel: yLabel)

        if (pages != null && pages.size() > 0) {
            pages.each { p ->
                List<TimeToCsMapping> timeToCsMappings = timeToCsMappingCacheService.getMappingsFor(p)
                MultiLineChartLineData line = new MultiLineChartLineData(name: p.name)
                timeToCsMappings.each { line.addDataPoint(it.loadTimeInMilliSecs, it.customerSatisfaction) }
                multiLineChart.addLine(line)
            }
        } else {
            List<DefaultTimeToCsMapping> defaultTimeToCsMappings = defaultTimeToCsMappingService.getAll().findAll { it.loadTimeInMilliSecs <= 8000 }
            Map<String, MultiLineChartLineData> map = new HashMap<>()

            defaultTimeToCsMappings.each { entry ->
                if (!map.containsKey(entry.name)) {
                    map.put(entry.name, new MultiLineChartLineData(name: entry.name))
                }
                map.get(entry.name).addDataPoint(entry.loadTimeInMilliSecs, entry.customerSatisfactionInPercent)
            }

            // sort and add
            map.values().sort{a,b -> a.name.compareTo(b.name)}.each{e -> multiLineChart.addLine(e)}
        }

        return multiLineChart
    }
}

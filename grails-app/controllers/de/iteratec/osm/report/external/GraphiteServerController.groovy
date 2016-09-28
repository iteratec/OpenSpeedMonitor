package de.iteratec.osm.report.external

import grails.converters.JSON
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus

import javax.servlet.http.HttpServletResponse

import static org.springframework.http.HttpStatus.*
//TODO: This controller was generated due to a scaffolding bug (https://github.com/grails3-plugins/scaffolding/issues/24). The dynamically scaffolded controllers cannot handle database exceptions
//TODO: save, edit and delete are altered to support on-the-fly start/stop of health reporting
class GraphiteServerController {
    HealthReportService healthReportService
    static scaffold = GraphiteServer
    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    def index() {
        
    }

    def show(GraphiteServer graphiteServer) {
        respond graphiteServer
    }

    def create() {
        respond new GraphiteServer(params)
    }

    def save(GraphiteServer graphiteServer) {
        if (graphiteServer == null) {
            
            notFound()
            return
        }

        if (graphiteServer.hasErrors()) {

            respond graphiteServer.errors, view:'create'
            return
        }

        graphiteServer.save flush:true
        healthReportService.handleGraphiteServer(graphiteServer)

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.created.message', args: [message(code: 'graphiteServer.label', default: 'GraphiteServer'), graphiteServer.id])
                redirect graphiteServer
            }
            '*' { respond graphiteServer, [status: CREATED] }
        }
    }

    def edit(GraphiteServer graphiteServer) {
        respond graphiteServer
    }

    def update(GraphiteServer graphiteServer) {
        if (graphiteServer == null) {

            notFound()
            return
        }

        if (graphiteServer.hasErrors()) {

            respond graphiteServer.errors, view:'edit'
            return
        }

        graphiteServer.save flush:true
        healthReportService.handleGraphiteServer(graphiteServer)

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.updated.message', args: [message(code: 'graphiteServer.label', default: 'GraphiteServer'), graphiteServer.id])
                redirect graphiteServer
            }
            '*'{ respond graphiteServer, [status: OK] }
        }
    }

    def delete(GraphiteServer graphiteServer) {

        if (graphiteServer == null) {
            notFound()
            return
        }

        try {
            graphiteServer.reportHealthMetrics = false
            healthReportService.handleGraphiteServer(graphiteServer)
            graphiteServer.delete(flush: true)
            flash.message = message(code: 'default.deleted.message', args: [message(code: 'graphiteServer.label', default: 'GraphiteServer'), params.id])
            redirect(action: "index")
        }
        catch (DataIntegrityViolationException e) {
            flash.message = message(code: 'default.not.deleted.message', args: [message(code: 'graphiteServer.label', default: 'GraphiteServer'), params.id])
            redirect(action: "show", id: params.id)
        }
    }
    def updateTable(){
        params.order = params.order ? params.order : "desc"
        params.sort = params.sort ? params.sort : "serverAdress"
        def paramsForCount = Boolean.valueOf(params.limitResults) ? [max:1000]:[:]
        params.max = params.max as Integer
        params.offset = params.offset as Integer
        List<GraphiteServer> result
        int count
        result = GraphiteServer.createCriteria().list(params) {
            if(params.filter)
                or{
                    ilike("serverAdress","%"+params.filter+"%")
                    if(params.filter.isInteger())eq("port",Integer.valueOf(params.filter))
                    ilike("webappUrl","%"+params.filter+"%")
                    ilike("webappPathToRenderingEngine","%"+params.filter+"%")

                }
        }
        count = GraphiteServer.createCriteria().list(paramsForCount) {
            if(params.filter)
                or{
                    ilike("serverAdress","%"+params.filter+"%")
                    if(params.filter.isInteger())eq("port",Integer.valueOf(params.filter))
                    ilike("webappUrl","%"+params.filter+"%")
                    ilike("webappPathToRenderingEngine","%"+params.filter+"%")

                }
        }.size()
        String templateAsPlainText = g.render(
                template: 'graphiteServerTable',
                model: [graphiteServers: result]
        )
        def jsonResult = [table:templateAsPlainText, count:count]as JSON
        sendSimpleResponseAsStream(response, HttpStatus.OK, jsonResult.toString(false))
    }


    private void sendSimpleResponseAsStream(HttpServletResponse response, HttpStatus httpStatus, String message) {

        response.setContentType('text/plain;charset=UTF-8')
        response.status=httpStatus.value()

        Writer textOut = new OutputStreamWriter(response.getOutputStream())
        textOut.write(message)
        textOut.flush()
        response.getOutputStream().flush()

    }
    protected void notFound() {
        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.not.found.message', args: [message(code: 'graphiteServer.label', default: 'GraphiteServer'), params.id])
                redirect action: "index", method: "GET"
            }
            '*'{ render status: NOT_FOUND }
        }
    }
}
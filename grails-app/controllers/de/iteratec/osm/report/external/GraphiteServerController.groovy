package de.iteratec.osm.report.external

import org.springframework.dao.DataIntegrityViolationException
import static org.springframework.http.HttpStatus.*
//TODO: This controller was generated due to a scaffolding bug (https://github.com/grails3-plugins/scaffolding/issues/24). The dynamically scaffolded controllers cannot handle database exceptions
//TODO: save, edit and delete are altered to support on-the-fly start/stop of health reporting
class GraphiteServerController {
    HealthReportService healthReportService
    static scaffold = GraphiteServer
    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    def index(Integer max) {
        def maxDefault = 100
        if (max) maxDefault = max
        params.max = maxDefault
        respond GraphiteServer.list(params), model:[graphiteServerCount: GraphiteServer.count()]
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

package de.iteratec.osm.report.external

import org.springframework.dao.DataIntegrityViolationException
import static org.springframework.http.HttpStatus.*
//TODO: This controller was generated due to a scaffolding bug (https://github.com/grails3-plugins/scaffolding/issues/24). The dynamically scaffolded controllers cannot handle database exceptions
class GraphiteEventSourcePathController {

    static scaffold = GraphiteEventSourcePath
    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        respond GraphiteEventSourcePath.list(params), model:[graphiteEventSourcePathCount: GraphiteEventSourcePath.count()]
    }

    def show(GraphiteEventSourcePath graphiteEventSourcePath) {
        respond graphiteEventSourcePath
    }

    def create() {
        respond new GraphiteEventSourcePath(params)
    }

    def save(GraphiteEventSourcePath graphiteEventSourcePath) {
        if (graphiteEventSourcePath == null) {
            
            notFound()
            return
        }

        if (graphiteEventSourcePath.hasErrors()) {

            respond graphiteEventSourcePath.errors, view:'create'
            return
        }

        graphiteEventSourcePath.save flush:true

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.created.message', args: [message(code: 'graphiteEventSourcePath.label', default: 'GraphiteEventSourcePath'), graphiteEventSourcePath.id])
                redirect graphiteEventSourcePath
            }
            '*' { respond graphiteEventSourcePath, [status: CREATED] }
        }
    }

    def edit(GraphiteEventSourcePath graphiteEventSourcePath) {
        respond graphiteEventSourcePath
    }

    def update(GraphiteEventSourcePath graphiteEventSourcePath) {
        if (graphiteEventSourcePath == null) {

            notFound()
            return
        }

        if (graphiteEventSourcePath.hasErrors()) {

            respond graphiteEventSourcePath.errors, view:'edit'
            return
        }

        graphiteEventSourcePath.save flush:true

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.updated.message', args: [message(code: 'graphiteEventSourcePath.label', default: 'GraphiteEventSourcePath'), graphiteEventSourcePath.id])
                redirect graphiteEventSourcePath
            }
            '*'{ respond graphiteEventSourcePath, [status: OK] }
        }
    }

    def delete(GraphiteEventSourcePath graphiteEventSourcePath) {

        if (graphiteEventSourcePath == null) {
            notFound()
            return
        }

        try {
            graphiteEventSourcePath.delete(flush: true)
            flash.message = message(code: 'default.deleted.message', args: [message(code: 'graphiteEventSourcePath.label', default: 'GraphiteEventSourcePath'), params.id])
            redirect(action: "index")
        }
        catch (DataIntegrityViolationException e) {
            flash.message = message(code: 'default.not.deleted.message', args: [message(code: 'graphiteEventSourcePath.label', default: 'GraphiteEventSourcePath'), params.id])
            redirect(action: "show", id: params.id)
        }
    }

    protected void notFound() {
        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.not.found.message', args: [message(code: 'graphiteEventSourcePath.label', default: 'GraphiteEventSourcePath'), params.id])
                redirect action: "index", method: "GET"
            }
            '*'{ render status: NOT_FOUND }
        }
    }
}

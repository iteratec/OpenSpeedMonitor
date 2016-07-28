package de.iteratec.osm.report.external

import org.springframework.dao.DataIntegrityViolationException
import static org.springframework.http.HttpStatus.*
//TODO: This controller was generated due to a scaffolding bug (https://github.com/grails3-plugins/scaffolding/issues/24). The dynamically scaffolded controllers cannot handle database exceptions
class GraphitePathController {

    static scaffold = GraphitePath
    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        respond GraphitePath.list(params), model:[graphitePathCount: GraphitePath.count()]
    }

    def show(GraphitePath graphitePath) {
        respond graphitePath
    }

    def create() {
        respond new GraphitePath(params)
    }

    def save(GraphitePath graphitePath) {
        if (graphitePath == null) {
            
            notFound()
            return
        }

        if (graphitePath.hasErrors()) {

            respond graphitePath.errors, view:'create'
            return
        }

        graphitePath.save flush:true

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.created.message', args: [message(code: 'graphitePath.label', default: 'GraphitePath'), graphitePath.id])
                redirect graphitePath
            }
            '*' { respond graphitePath, [status: CREATED] }
        }
    }

    def edit(GraphitePath graphitePath) {
        respond graphitePath
    }

    def update(GraphitePath graphitePath) {
        if (graphitePath == null) {

            notFound()
            return
        }

        if (graphitePath.hasErrors()) {

            respond graphitePath.errors, view:'edit'
            return
        }

        graphitePath.save flush:true

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.updated.message', args: [message(code: 'graphitePath.label', default: 'GraphitePath'), graphitePath.id])
                redirect graphitePath
            }
            '*'{ respond graphitePath, [status: OK] }
        }
    }

    def delete(GraphitePath graphitePath) {

        if (graphitePath == null) {
            notFound()
            return
        }

        try {
            graphitePath.delete(flush: true)
            flash.message = message(code: 'default.deleted.message', args: [message(code: 'graphitePath.label', default: 'GraphitePath'), params.id])
            redirect(action: "index")
        }
        catch (DataIntegrityViolationException e) {
            flash.message = message(code: 'default.not.deleted.message', args: [message(code: 'graphitePath.label', default: 'GraphitePath'), params.id])
            redirect(action: "show", id: params.id)
        }
    }

    protected void notFound() {
        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.not.found.message', args: [message(code: 'graphitePath.label', default: 'GraphitePath'), params.id])
                redirect action: "index", method: "GET"
            }
            '*'{ render status: NOT_FOUND }
        }
    }
}

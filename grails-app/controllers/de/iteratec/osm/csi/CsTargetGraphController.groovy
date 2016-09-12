package de.iteratec.osm.csi

import org.springframework.dao.DataIntegrityViolationException
import static org.springframework.http.HttpStatus.*
//TODO: This controller was generated due to a scaffolding bug (https://github.com/grails3-plugins/scaffolding/issues/24). The dynamically scaffolded controllers cannot handle database exceptions

class CsTargetGraphController {

    static scaffold = CsTargetGraph
    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    def index(Integer max) {
        def maxDefault = 100
        if (max) maxDefault = max
        params.max = maxDefault
        respond CsTargetGraph.list(params), model:[csTargetGraphCount: CsTargetGraph.count()]
    }

    def show(CsTargetGraph csTargetGraph) {
        respond csTargetGraph
    }

    def create() {
        respond new CsTargetGraph(params)
    }

    def save(CsTargetGraph csTargetGraph) {
        if (csTargetGraph == null) {
            
            notFound()
            return
        }

        if (csTargetGraph.hasErrors()) {

            respond csTargetGraph.errors, view:'create'
            return
        }

        csTargetGraph.save flush:true

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.created.message', args: [message(code: 'csTargetGraph.label', default: 'CsTargetGraph'), csTargetGraph.id])
                redirect csTargetGraph
            }
            '*' { respond csTargetGraph, [status: CREATED] }
        }
    }

    def edit(CsTargetGraph csTargetGraph) {
        respond csTargetGraph
    }

    def update(CsTargetGraph csTargetGraph) {
        if (csTargetGraph == null) {

            notFound()
            return
        }

        if (csTargetGraph.hasErrors()) {

            respond csTargetGraph.errors, view:'edit'
            return
        }

        csTargetGraph.save flush:true

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.updated.message', args: [message(code: 'csTargetGraph.label', default: 'CsTargetGraph'), csTargetGraph.id])
                redirect csTargetGraph
            }
            '*'{ respond csTargetGraph, [status: OK] }
        }
    }

    def delete(CsTargetGraph csTargetGraph) {

        if (csTargetGraph == null) {
            notFound()
            return
        }

        try {
            csTargetGraph.delete(flush: true)
            flash.message = message(code: 'default.deleted.message', args: [message(code: 'csTargetGraph.label', default: 'CsTargetGraph'), params.id])
            redirect(action: "index")
        }
        catch (DataIntegrityViolationException e) {
            flash.message = message(code: 'default.not.deleted.message', args: [message(code: 'csTargetGraph.label', default: 'CsTargetGraph'), params.id])
            redirect(action: "show", id: params.id)
        }
    }

    protected void notFound() {
        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.not.found.message', args: [message(code: 'csTargetGraph.label', default: 'CsTargetGraph'), params.id])
                redirect action: "index", method: "GET"
            }
            '*'{ render status: NOT_FOUND }
        }
    }
}

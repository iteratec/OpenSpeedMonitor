package de.iteratec.osm.report.chart

import org.springframework.dao.DataIntegrityViolationException
import static org.springframework.http.HttpStatus.*
//TODO: This controller was generated due to a scaffolding bug (https://github.com/grails3-plugins/scaffolding/issues/24). The dynamically scaffolded controllers cannot handle database exceptions
class CsiAggregationController {

    static scaffold = CsiAggregation
    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        respond CsiAggregation.list(params), model:[csiAggregationCount: CsiAggregation.count()]
    }

    def show(CsiAggregation csiAggregation) {
        respond csiAggregation
    }

    def create() {
        respond new CsiAggregation(params)
    }

    def save(CsiAggregation csiAggregation) {
        if (csiAggregation == null) {
            
            notFound()
            return
        }

        if (csiAggregation.hasErrors()) {

            respond csiAggregation.errors, view:'create'
            return
        }

        csiAggregation.save flush:true

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.created.message', args: [message(code: 'csiAggregation.label', default: 'CsiAggregation'), csiAggregation.id])
                redirect csiAggregation
            }
            '*' { respond csiAggregation, [status: CREATED] }
        }
    }

    def edit(CsiAggregation csiAggregation) {
        respond csiAggregation
    }

    def update(CsiAggregation csiAggregation) {
        if (csiAggregation == null) {

            notFound()
            return
        }

        if (csiAggregation.hasErrors()) {

            respond csiAggregation.errors, view:'edit'
            return
        }

        csiAggregation.save flush:true

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.updated.message', args: [message(code: 'csiAggregation.label', default: 'CsiAggregation'), csiAggregation.id])
                redirect csiAggregation
            }
            '*'{ respond csiAggregation, [status: OK] }
        }
    }

    def delete(CsiAggregation csiAggregation) {

        if (csiAggregation == null) {
            notFound()
            return
        }

        try {
            csiAggregation.delete(flush: true)
            flash.message = message(code: 'default.deleted.message', args: [message(code: 'csiAggregation.label', default: 'CsiAggregation'), params.id])
            redirect(action: "index")
        }
        catch (DataIntegrityViolationException e) {
            flash.message = message(code: 'default.not.deleted.message', args: [message(code: 'csiAggregation.label', default: 'CsiAggregation'), params.id])
            redirect(action: "show", id: params.id)
        }
    }

    protected void notFound() {
        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.not.found.message', args: [message(code: 'csiAggregation.label', default: 'CsiAggregation'), params.id])
                redirect action: "index", method: "GET"
            }
            '*'{ render status: NOT_FOUND }
        }
    }
}

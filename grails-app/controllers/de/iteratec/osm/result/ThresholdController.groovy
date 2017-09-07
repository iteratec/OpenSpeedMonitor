package de.iteratec.osm.result

import org.springframework.dao.DataIntegrityViolationException
import static org.springframework.http.HttpStatus.*
//TODO: This controller-templated was edited due to a scaffolding bug (https://github.com/grails3-plugins/scaffolding/issues/24). The dynamically scaffolded controllers cannot handle database exceptions

class ThresholdController {

    static scaffold = Threshold
    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    def index(Integer max) {
        def maxDefault = 100
        if (max) maxDefault = max
        params.max = maxDefault
        respond Threshold.list(params), model:[thresholdCount: Threshold.count()]
    }

    def show(Threshold threshold) {
        respond threshold
    }

    def create() {
        respond new Threshold(params)
    }

    def save(Threshold threshold) {
        if (threshold == null) {
            
            notFound()
            return
        }

        if (threshold.hasErrors() || !threshold.validate()) {

            respond threshold.errors, view:'create'
            return
        }


        threshold.save flush:true

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.created.message', args: [message(code: 'threshold.label', default: 'Threshold'), threshold.id])
                redirect threshold
            }
            '*' { respond threshold, [status: CREATED] }
        }
    }

    def edit(Threshold threshold) {
        respond threshold
    }

    def update(Threshold threshold) {
        if (threshold == null) {

            notFound()
            return
        }

        if (threshold.hasErrors()) {

            respond threshold.errors, view:'edit'
            return
        }

        threshold.save flush:true

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.updated.message', args: [message(code: 'threshold.label', default: 'Threshold'), threshold.id])
                redirect threshold
            }
            '*'{ respond threshold, [status: OK] }
        }
    }

    def delete(Threshold threshold) {

        if (threshold == null) {
            notFound()
            return
        }

        try {
            threshold.delete(flush: true)
            flash.message = message(code: 'default.deleted.message', args: [message(code: 'threshold.label', default: 'Threshold'), params.id])
            redirect(action: "index")
        }
        catch (DataIntegrityViolationException e) {
            flash.message = message(code: 'default.not.deleted.message', args: [message(code: 'threshold.label', default: 'Threshold'), params.id])
            redirect(action: "show", id: params.id)
        }
    }

    protected void notFound() {
        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.not.found.message', args: [message(code: 'threshold.label', default: 'Threshold'), params.id])
                redirect action: "index", method: "GET"
            }
            '*'{ render status: NOT_FOUND }
        }
    }
}

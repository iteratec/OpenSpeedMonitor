package de.iteratec.osm.result

import org.springframework.dao.DataIntegrityViolationException
import static org.springframework.http.HttpStatus.*
//TODO: This controller was generated due to a scaffolding bug (https://github.com/grails3-plugins/scaffolding/issues/24). The dynamically scaffolded controllers cannot handle database exceptions
class MeasuredEventController {

    static scaffold = MeasuredEvent
    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        respond MeasuredEvent.list(params), model:[measuredEventCount: MeasuredEvent.count()]
    }

    def show(MeasuredEvent measuredEvent) {
        respond measuredEvent
    }

    def create() {
        respond new MeasuredEvent(params)
    }

    def save(MeasuredEvent measuredEvent) {
        if (measuredEvent == null) {
            
            notFound()
            return
        }

        if (measuredEvent.hasErrors()) {

            respond measuredEvent.errors, view:'create'
            return
        }

        measuredEvent.save flush:true

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.created.message', args: [message(code: 'measuredEvent.label', default: 'MeasuredEvent'), measuredEvent.id])
                redirect measuredEvent
            }
            '*' { respond measuredEvent, [status: CREATED] }
        }
    }

    def edit(MeasuredEvent measuredEvent) {
        respond measuredEvent
    }

    def update(MeasuredEvent measuredEvent) {
        if (measuredEvent == null) {

            notFound()
            return
        }

        if (measuredEvent.hasErrors()) {

            respond measuredEvent.errors, view:'edit'
            return
        }

        measuredEvent.save flush:true

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.updated.message', args: [message(code: 'measuredEvent.label', default: 'MeasuredEvent'), measuredEvent.id])
                redirect measuredEvent
            }
            '*'{ respond measuredEvent, [status: OK] }
        }
    }

    def delete(MeasuredEvent measuredEvent) {

        if (measuredEvent == null) {
            notFound()
            return
        }

        try {
            measuredEvent.delete(flush: true)
            flash.message = message(code: 'default.deleted.message', args: [message(code: 'measuredEvent.label', default: 'MeasuredEvent'), params.id])
            redirect(action: "index")
        }
        catch (DataIntegrityViolationException e) {
            flash.message = message(code: 'default.not.deleted.message', args: [message(code: 'measuredEvent.label', default: 'MeasuredEvent'), params.id])
            redirect(action: "show", id: params.id)
        }
    }

    protected void notFound() {
        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.not.found.message', args: [message(code: 'measuredEvent.label', default: 'MeasuredEvent'), params.id])
                redirect action: "index", method: "GET"
            }
            '*'{ render status: NOT_FOUND }
        }
    }
}

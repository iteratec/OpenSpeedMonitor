package de.iteratec.osm.measurement.environment

import org.springframework.dao.DataIntegrityViolationException
import static org.springframework.http.HttpStatus.*
//TODO: This controller was generated due to a scaffolding bug (https://github.com/grails3-plugins/scaffolding/issues/24). The dynamically scaffolded controllers cannot handle database exceptions

class LocationController {

    static scaffold = Location
    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    def index(Integer max) {
        def maxDefault = 100
        if (max) maxDefault = max
        params.max = maxDefault
        respond Location.list(params), model:[locationCount: Location.count()]
    }

    def show(Location location) {
        respond location
    }

    def create() {
        respond new Location(params)
    }

    def save(Location location) {
        if (location == null) {
            
            notFound()
            return
        }

        if (location.hasErrors()) {

            respond location.errors, view:'create'
            return
        }

        location.save flush:true

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.created.message', args: [message(code: 'location.label', default: 'Location'), location.id])
                redirect location
            }
            '*' { respond location, [status: CREATED] }
        }
    }

    def edit(Location location) {
        respond location
    }

    def update(Location location) {
        if (location == null) {

            notFound()
            return
        }

        if (location.hasErrors()) {

            respond location.errors, view:'edit'
            return
        }

        location.save flush:true

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.updated.message', args: [message(code: 'location.label', default: 'Location'), location.id])
                redirect location
            }
            '*'{ respond location, [status: OK] }
        }
    }

    def delete(Location location) {

        if (location == null) {
            notFound()
            return
        }

        try {
            location.delete(flush: true)
            flash.message = message(code: 'default.deleted.message', args: [message(code: 'location.label', default: 'Location'), params.id])
            redirect(action: "index")
        }
        catch (DataIntegrityViolationException e) {
            flash.message = message(code: 'default.not.deleted.message', args: [message(code: 'location.label', default: 'Location'), params.id])
            redirect(action: "show", id: params.id)
        }
    }

    protected void notFound() {
        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.not.found.message', args: [message(code: 'location.label', default: 'Location'), params.id])
                redirect action: "index", method: "GET"
            }
            '*'{ render status: NOT_FOUND }
        }
    }
}

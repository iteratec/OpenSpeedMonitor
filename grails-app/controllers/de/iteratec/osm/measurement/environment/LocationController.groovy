package de.iteratec.osm.measurement.environment

import grails.converters.JSON
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus

import javax.servlet.http.HttpServletResponse

import static org.springframework.http.HttpStatus.*
//TODO: This controller was generated due to a scaffolding bug (https://github.com/grails3-plugins/scaffolding/issues/24). The dynamically scaffolded controllers cannot handle database exceptions

class LocationController {

    static scaffold = Location
    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    def index() {
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

    def updateTable(){
        params.order = params.order ? params.order : "asc"
        params.sort = params.sort ? params.sort : "label"
        params.max = params.max as Integer
        params.offset = params.offset as Integer
        List<Location> result = Location.createCriteria().list(params) {
            if(params.filter)
                or{
                    ilike("label","%"+params.filter+"%")
                    ilike("uniqueIdentifierForServer","%"+params.filter+"%")
                    ilike("location","%"+params.filter+"%")
                    wptServer{
                        ilike("label","%"+params.filter+"%")
                    }
                }
        }
        String templateAsPlainText = g.render(
                template: 'locationTable',
                model: [locations: result]
        )
        def jsonResult = [table:templateAsPlainText, count:result.totalCount]as JSON
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
                flash.message = message(code: 'default.not.found.message', args: [message(code: 'location.label', default: 'Location'), params.id])
                redirect action: "index", method: "GET"
            }
            '*'{ render status: NOT_FOUND }
        }
    }
}

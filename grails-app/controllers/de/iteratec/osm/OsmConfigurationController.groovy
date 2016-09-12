package de.iteratec.osm

import org.springframework.dao.DataIntegrityViolationException
import static org.springframework.http.HttpStatus.*
//TODO: This controller was generated due to a scaffolding bug (https://github.com/grails3-plugins/scaffolding/issues/24). The dynamically scaffolded controllers cannot handle database exceptions
//TODO: create and delete were NOT generated

class OsmConfigurationController {

    static scaffold = OsmConfiguration
    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    def create() {
        redirect(action: 'list')
    }

    def delete(){
        def osmConfiguration = OsmConfiguration.get(params.id)
        if(OsmConfiguration.count() == 1) {
            flash.message = message(code: 'de.iteratec.osm.configuration.deletion.not_allowed')
            redirect(action: "show", id: params.id)
        } else {
            osmConfiguration.delete(flush:true)
            redirect(action: 'list')
        }
    }

    def index(Integer max) {
        def maxDefault = 100
        if (max) maxDefault = max
        params.max = maxDefault
        respond OsmConfiguration.list(params), model:[osmConfigurationCount: OsmConfiguration.count()]
    }

    def show(OsmConfiguration osmConfiguration) {
        respond osmConfiguration
    }


    def save(OsmConfiguration osmConfiguration) {
        if (osmConfiguration == null) {
            
            notFound()
            return
        }

        if (osmConfiguration.hasErrors()) {

            respond osmConfiguration.errors, view:'create'
            return
        }

        osmConfiguration.save flush:true

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.created.message', args: [message(code: 'osmConfiguration.label', default: 'OsmConfiguration'), osmConfiguration.id])
                redirect osmConfiguration
            }
            '*' { respond osmConfiguration, [status: CREATED] }
        }
    }

    def edit(OsmConfiguration osmConfiguration) {
        respond osmConfiguration
    }

    def update(OsmConfiguration osmConfiguration) {
        if (osmConfiguration == null) {

            notFound()
            return
        }

        if (osmConfiguration.hasErrors()) {

            respond osmConfiguration.errors, view:'edit'
            return
        }

        osmConfiguration.save flush:true

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.updated.message', args: [message(code: 'osmConfiguration.label', default: 'OsmConfiguration'), osmConfiguration.id])
                redirect osmConfiguration
            }
            '*'{ respond osmConfiguration, [status: OK] }
        }
    }



    protected void notFound() {
        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.not.found.message', args: [message(code: 'osmConfiguration.label', default: 'OsmConfiguration'), params.id])
                redirect action: "index", method: "GET"
            }
            '*'{ render status: NOT_FOUND }
        }
    }
}

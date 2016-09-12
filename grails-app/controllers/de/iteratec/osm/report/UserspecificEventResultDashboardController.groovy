package de.iteratec.osm.report

import org.springframework.dao.DataIntegrityViolationException
import static org.springframework.http.HttpStatus.*
//TODO: This controller was generated due to a scaffolding bug (https://github.com/grails3-plugins/scaffolding/issues/24). The dynamically scaffolded controllers cannot handle database exceptions
class UserspecificEventResultDashboardController {

    static scaffold = UserspecificEventResultDashboard
    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    def index(Integer max) {
        def maxDefault = 100
        if (max) maxDefault = max
        params.max = maxDefault
        respond UserspecificEventResultDashboard.list(params), model:[userspecificEventResultDashboardCount: UserspecificEventResultDashboard.count()]
    }

    def show(UserspecificEventResultDashboard userspecificEventResultDashboard) {
        respond userspecificEventResultDashboard
    }

    def create() {
        respond new UserspecificEventResultDashboard(params)
    }

    def save(UserspecificEventResultDashboard userspecificEventResultDashboard) {
        if (userspecificEventResultDashboard == null) {
            
            notFound()
            return
        }

        if (userspecificEventResultDashboard.hasErrors()) {

            respond userspecificEventResultDashboard.errors, view:'create'
            return
        }

        userspecificEventResultDashboard.save flush:true

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.created.message', args: [message(code: 'userspecificEventResultDashboard.label', default: 'UserspecificEventResultDashboard'), userspecificEventResultDashboard.id])
                redirect userspecificEventResultDashboard
            }
            '*' { respond userspecificEventResultDashboard, [status: CREATED] }
        }
    }

    def edit(UserspecificEventResultDashboard userspecificEventResultDashboard) {
        respond userspecificEventResultDashboard
    }

    def update(UserspecificEventResultDashboard userspecificEventResultDashboard) {
        if (userspecificEventResultDashboard == null) {

            notFound()
            return
        }

        if (userspecificEventResultDashboard.hasErrors()) {

            respond userspecificEventResultDashboard.errors, view:'edit'
            return
        }

        userspecificEventResultDashboard.save flush:true

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.updated.message', args: [message(code: 'userspecificEventResultDashboard.label', default: 'UserspecificEventResultDashboard'), userspecificEventResultDashboard.id])
                redirect userspecificEventResultDashboard
            }
            '*'{ respond userspecificEventResultDashboard, [status: OK] }
        }
    }

    def delete(UserspecificEventResultDashboard userspecificEventResultDashboard) {

        if (userspecificEventResultDashboard == null) {
            notFound()
            return
        }

        try {
            userspecificEventResultDashboard.delete(flush: true)
            flash.message = message(code: 'default.deleted.message', args: [message(code: 'userspecificEventResultDashboard.label', default: 'UserspecificEventResultDashboard'), params.id])
            redirect(action: "index")
        }
        catch (DataIntegrityViolationException e) {
            flash.message = message(code: 'default.not.deleted.message', args: [message(code: 'userspecificEventResultDashboard.label', default: 'UserspecificEventResultDashboard'), params.id])
            redirect(action: "show", id: params.id)
        }
    }

    protected void notFound() {
        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.not.found.message', args: [message(code: 'userspecificEventResultDashboard.label', default: 'UserspecificEventResultDashboard'), params.id])
                redirect action: "index", method: "GET"
            }
            '*'{ render status: NOT_FOUND }
        }
    }
}

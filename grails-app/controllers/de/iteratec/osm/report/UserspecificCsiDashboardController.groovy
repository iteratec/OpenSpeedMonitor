package de.iteratec.osm.report

import org.springframework.dao.DataIntegrityViolationException
import static org.springframework.http.HttpStatus.*
//TODO: This controller was generated due to a scaffolding bug (https://github.com/grails3-plugins/scaffolding/issues/24). The dynamically scaffolded controllers cannot handle database exceptions
class UserspecificCsiDashboardController {

    static scaffold = UserspecificCsiDashboard
    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        respond UserspecificCsiDashboard.list(params), model:[userspecificCsiDashboardCount: UserspecificCsiDashboard.count()]
    }

    def show(UserspecificCsiDashboard userspecificCsiDashboard) {
        respond userspecificCsiDashboard
    }

    def create() {
        respond new UserspecificCsiDashboard(params)
    }

    def save(UserspecificCsiDashboard userspecificCsiDashboard) {
        if (userspecificCsiDashboard == null) {
            
            notFound()
            return
        }

        if (userspecificCsiDashboard.hasErrors()) {

            respond userspecificCsiDashboard.errors, view:'create'
            return
        }

        userspecificCsiDashboard.save flush:true

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.created.message', args: [message(code: 'userspecificCsiDashboard.label', default: 'UserspecificCsiDashboard'), userspecificCsiDashboard.id])
                redirect userspecificCsiDashboard
            }
            '*' { respond userspecificCsiDashboard, [status: CREATED] }
        }
    }

    def edit(UserspecificCsiDashboard userspecificCsiDashboard) {
        respond userspecificCsiDashboard
    }

    def update(UserspecificCsiDashboard userspecificCsiDashboard) {
        if (userspecificCsiDashboard == null) {

            notFound()
            return
        }

        if (userspecificCsiDashboard.hasErrors()) {

            respond userspecificCsiDashboard.errors, view:'edit'
            return
        }

        userspecificCsiDashboard.save flush:true

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.updated.message', args: [message(code: 'userspecificCsiDashboard.label', default: 'UserspecificCsiDashboard'), userspecificCsiDashboard.id])
                redirect userspecificCsiDashboard
            }
            '*'{ respond userspecificCsiDashboard, [status: OK] }
        }
    }

    def delete(UserspecificCsiDashboard userspecificCsiDashboard) {

        if (userspecificCsiDashboard == null) {
            notFound()
            return
        }

        try {
            userspecificCsiDashboard.delete(flush: true)
            flash.message = message(code: 'default.deleted.message', args: [message(code: 'userspecificCsiDashboard.label', default: 'UserspecificCsiDashboard'), params.id])
            redirect(action: "index")
        }
        catch (DataIntegrityViolationException e) {
            flash.message = message(code: 'default.not.deleted.message', args: [message(code: 'userspecificCsiDashboard.label', default: 'UserspecificCsiDashboard'), params.id])
            redirect(action: "show", id: params.id)
        }
    }

    protected void notFound() {
        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.not.found.message', args: [message(code: 'userspecificCsiDashboard.label', default: 'UserspecificCsiDashboard'), params.id])
                redirect action: "index", method: "GET"
            }
            '*'{ render status: NOT_FOUND }
        }
    }
}

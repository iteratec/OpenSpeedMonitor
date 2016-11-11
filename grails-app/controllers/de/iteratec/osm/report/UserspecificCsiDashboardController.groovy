package de.iteratec.osm.report

import grails.converters.JSON
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus

import javax.servlet.http.HttpServletResponse

import static org.springframework.http.HttpStatus.*
//TODO: This controller was generated due to a scaffolding bug (https://github.com/grails3-plugins/scaffolding/issues/24). The dynamically scaffolded controllers cannot handle database exceptions
class UserspecificCsiDashboardController {

    static scaffold = UserspecificCsiDashboard
    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    def index() {
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
    def updateTable(){
        params.order = params.order ? params.order : "asc"
        params.sort = params.sort ? params.sort : "dashboardName"
        params.max = params.max as Integer
        params.offset = params.offset as Integer
        List<UserspecificCsiDashboard> result = UserspecificCsiDashboard.createCriteria().list(params) {
            if(params.filter)
                or{
                    ilike("dashboardName","%"+params.filter+"%")
                    ilike("username","%"+params.filter+"%")
                }
        }
        String templateAsPlainText = g.render(
                template: 'userspecificCsiDashboardTable',
                model: [userspecificCsiDashboards: result]
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
                flash.message = message(code: 'default.not.found.message', args: [message(code: 'userspecificCsiDashboard.label', default: 'UserspecificCsiDashboard'), params.id])
                redirect action: "index", method: "GET"
            }
            '*'{ render status: NOT_FOUND }
        }
    }
}

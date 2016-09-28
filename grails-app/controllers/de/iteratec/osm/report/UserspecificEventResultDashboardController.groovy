package de.iteratec.osm.report

import grails.converters.JSON
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus

import javax.servlet.http.HttpServletResponse

import static org.springframework.http.HttpStatus.*
//TODO: This controller was generated due to a scaffolding bug (https://github.com/grails3-plugins/scaffolding/issues/24). The dynamically scaffolded controllers cannot handle database exceptions
class UserspecificEventResultDashboardController {

    static scaffold = UserspecificEventResultDashboard
    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    def index() {
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
    def updateTable(){
        params.order = params.order ? params.order : "desc"
        params.sort = params.sort ? params.sort : "dashboardName"
        def paramsForCount = Boolean.valueOf(params.limitResults) ? [max:1000]:[:]
        params.max = params.max as Integer
        params.offset = params.offset as Integer
        List<UserspecificEventResultDashboard> result
        int count
        result = UserspecificEventResultDashboard.createCriteria().list(params) {
            if(params.filter)
                or{
                    ilike("dashboardName","%"+params.filter+"%")
                    ilike("username","%"+params.filter+"%")
                }
        }
        count = UserspecificEventResultDashboard.createCriteria().list(paramsForCount) {
            if(params.filter)
                or{
                    ilike("dashboardName","%"+params.filter+"%")
                    ilike("username","%"+params.filter+"%")
                }
        }.size()
        String templateAsPlainText = g.render(
                template: 'userspecificEventResultDashboardTable',
                model: [userspecificEventResultDashboards: result]
        )
        def jsonResult = [table:templateAsPlainText, count:count]as JSON
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
                flash.message = message(code: 'default.not.found.message', args: [message(code: 'userspecificEventResultDashboard.label', default: 'UserspecificEventResultDashboard'), params.id])
                redirect action: "index", method: "GET"
            }
            '*'{ render status: NOT_FOUND }
        }
    }
}

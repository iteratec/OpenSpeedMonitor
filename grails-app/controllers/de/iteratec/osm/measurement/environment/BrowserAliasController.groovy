package de.iteratec.osm.measurement.environment

import grails.converters.JSON
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus

import javax.servlet.http.HttpServletResponse

import static org.springframework.http.HttpStatus.*
//TODO: This controller was generated due to a scaffolding bug (https://github.com/grails3-plugins/scaffolding/issues/24). The dynamically scaffolded controllers cannot handle database exceptions

class BrowserAliasController {

    static scaffold = BrowserAlias
    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    def index() {
    }

    def show(BrowserAlias browserAlias) {
        respond browserAlias
    }

    def create() {
        respond new BrowserAlias(params)
    }

    def save(BrowserAlias browserAlias) {
        if (browserAlias == null) {
            
            notFound()
            return
        }

        if (browserAlias.hasErrors()) {

            respond browserAlias.errors, view:'create'
            return
        }

        browserAlias.save flush:true

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.created.message', args: [message(code: 'browserAlias.label', default: 'BrowserAlias'), browserAlias.id])
                redirect browserAlias
            }
            '*' { respond browserAlias, [status: CREATED] }
        }
    }

    def edit(BrowserAlias browserAlias) {
        respond browserAlias
    }

    def update(BrowserAlias browserAlias) {
        if (browserAlias == null) {

            notFound()
            return
        }

        if (browserAlias.hasErrors()) {

            respond browserAlias.errors, view:'edit'
            return
        }

        browserAlias.save flush:true

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.updated.message', args: [message(code: 'browserAlias.label', default: 'BrowserAlias'), browserAlias.id])
                redirect browserAlias
            }
            '*'{ respond browserAlias, [status: OK] }
        }
    }

    def delete(BrowserAlias browserAlias) {

        if (browserAlias == null) {
            notFound()
            return
        }

        try {
            browserAlias.delete(flush: true)
            flash.message = message(code: 'default.deleted.message', args: [message(code: 'browserAlias.label', default: 'BrowserAlias'), params.id])
            redirect(action: "index")
        }
        catch (DataIntegrityViolationException e) {
            flash.message = message(code: 'default.not.deleted.message', args: [message(code: 'browserAlias.label', default: 'BrowserAlias'), params.id])
            redirect(action: "show", id: params.id)
        }
    }

    def updateTable(){
        params.order = params.order ? params.order : "asc"
        params.sort = params.sort ? params.sort : "alias"
        params.max = params.max as Integer
        params.offset = params.offset as Integer

        List<Browser> result = BrowserAlias.createCriteria().list(params) {
            if(params.filter)ilike("alias","%"+params.filter+"%")
        }
        String templateAsPlainText = g.render(
                template: 'browserAliasTable',
                model: [browserAliases: result]
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
                flash.message = message(code: 'default.not.found.message', args: [message(code: 'browserAlias.label', default: 'BrowserAlias'), params.id])
                redirect action: "index", method: "GET"
            }
            '*'{ render status: NOT_FOUND }
        }
    }
}

package de.iteratec.osm.measurement.environment

import de.iteratec.osm.result.EventResult
import de.iteratec.osm.util.ControllerUtils
import de.iteratec.osm.util.DataIntegrityViolationExpectionUtil
import de.iteratec.osm.util.DataIntegrityViolationUtil
import grails.converters.JSON
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus

import javax.servlet.http.HttpServletResponse

import static org.springframework.http.HttpStatus.*

//TODO: This controller was generated due to a scaffolding bug (https://github.com/grails3-plugins/scaffolding/issues/24). The dynamically scaffolded controllers cannot handle database exceptions

class BrowserController {

    static scaffold = Browser
    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    def index() {

    }

    def show(Browser browser) {
        respond browser
    }

    def create() {
        respond new Browser(params)
    }

    def save(Browser browser) {
        if (browser == null) {

            notFound()
            return
        }

        if (browser.hasErrors()) {

            respond browser.errors, view: 'create'
            return
        }

        browser.save flush: true

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.created.message', args: [message(code: 'browser.label', default: 'Browser'), browser.id])
                redirect browser
            }
            '*' { respond browser, [status: CREATED] }
        }
    }

    def edit(Browser browser) {
        respond browser
    }

    def update(Browser browser) {
        if (browser == null) {

            notFound()
            return
        }

        if (browser.hasErrors()) {

            respond browser.errors, view: 'edit'
            return
        }

        browser.save flush: true

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.updated.message', args: [message(code: 'browser.label', default: 'Browser'), browser.id])
                redirect browser
            }
            '*' { respond browser, [status: OK] }
        }
    }

    def delete(Browser browser) {

        if (browser == null) {
            notFound()
            return
        }

        try {
            browser.delete(flush: true)
            flash.message = message(code: 'default.deleted.message', args: [message(code: 'browser.label', default: 'Browser'), params.id])
            redirect(action: "index")
        }
        catch (DataIntegrityViolationException e) {
            String dependency = DataIntegrityViolationExpectionUtil.getEntityNameForForeignKeyViolation(e)
            if(dependency){
                flash.message = message(code: 'default.not.deleted.foreignKeyConstraint.message', args: [message(code: 'browser.label', default: 'Browser'), params.id, dependency])
            }else{
                flash.message = message(code: 'default.not.deleted.message', args: [message(code: 'browser.label', default: 'Browser'), params.id])
            }
            redirect(action: "index")
        }
    }

    def updateTable() {
        params.order = params.order ? params.order : "asc"
        params.sort = params.sort ? params.sort : "name"
        params.sort = params.sort == "browserAliases" ? "name" : params.sort
        params.max = params.max as Integer
        params.offset = params.offset as Integer

        List<Browser> result = Browser.createCriteria().list(params) {
            if (params.filter) ilike("name", "%" + params.filter + "%")
        }
        String templateAsPlainText = g.render(
                template: 'browserTable',
                model: [browsers: result]
        )
        ControllerUtils.sendObjectAsJSON(response, [
                table: templateAsPlainText,
                count: result.totalCount
        ])
    }

    protected void notFound() {
        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.not.found.message', args: [message(code: 'browser.label', default: 'Browser'), params.id])
                redirect action: "index", method: "GET"
            }
            '*' { render status: NOT_FOUND }
        }
    }

    private static String toCamelCase( String text ) {
        text = text.replaceAll( "(_)([A-Za-z0-9])", { Object[] it -> it[2].toUpperCase() } )
        return text.capitalize()
    }
}

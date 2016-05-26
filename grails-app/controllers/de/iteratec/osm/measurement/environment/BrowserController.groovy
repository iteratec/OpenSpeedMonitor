package de.iteratec.osm.measurement.environment

import org.springframework.dao.DataIntegrityViolationException
import static org.springframework.http.HttpStatus.*
//TODO: This controller was generated due to a scaffolding bug (https://github.com/grails3-plugins/scaffolding/issues/24). The dynamically scaffolded controllers cannot handle database exceptions

class BrowserController {

    static scaffold = Browser
    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        respond Browser.list(params), model:[browserCount: Browser.count()]
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

            respond browser.errors, view:'create'
            return
        }

        browser.save flush:true

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

            respond browser.errors, view:'edit'
            return
        }

        browser.save flush:true

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.updated.message', args: [message(code: 'browser.label', default: 'Browser'), browser.id])
                redirect browser
            }
            '*'{ respond browser, [status: OK] }
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
            flash.message = message(code: 'default.not.deleted.message', args: [message(code: 'browser.label', default: 'Browser'), params.id])
            redirect(action: "show", id: params.id)
        }
    }

    protected void notFound() {
        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.not.found.message', args: [message(code: 'browser.label', default: 'Browser'), params.id])
                redirect action: "index", method: "GET"
            }
            '*'{ render status: NOT_FOUND }
        }
    }
}

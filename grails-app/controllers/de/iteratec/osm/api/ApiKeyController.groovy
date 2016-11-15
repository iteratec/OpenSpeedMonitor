package de.iteratec.osm.api

import de.iteratec.osm.util.ControllerUtils
import grails.converters.JSON
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus

import javax.servlet.http.HttpServletResponse

import static org.springframework.http.HttpStatus.*
//TODO: This controller was generated due to a scaffolding bug (https://github.com/grails3-plugins/scaffolding/issues/24). The dynamically scaffolded controllers cannot handle database exceptions

class ApiKeyController {

    static scaffold = ApiKey
    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    def index(Integer max) {
    }

    def show(ApiKey apiKey) {
        respond apiKey
    }

    def create() {
        respond new ApiKey(params)
    }

    def save(ApiKey apiKey) {
        if (apiKey == null) {

            notFound()
            return
        }

        if (apiKey.hasErrors()) {

            respond apiKey.errors, view:'create'
            return
        }

        apiKey.save flush:true

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.created.message', args: [message(code: 'apiKey.label', default: 'ApiKey'), apiKey.id])
                redirect apiKey
            }
            '*' { respond apiKey, [status: CREATED] }
        }
    }

    def edit(ApiKey apiKey) {
        respond apiKey
    }

    def update(ApiKey apiKey) {
        if (apiKey == null) {

            notFound()
            return
        }

        if (apiKey.hasErrors()) {

            respond apiKey.errors, view:'edit'
            return
        }

        apiKey.save flush:true

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.updated.message', args: [message(code: 'apiKey.label', default: 'ApiKey'), apiKey.id])
                redirect apiKey
            }
            '*'{ respond apiKey, [status: OK] }
        }
    }

    def delete(ApiKey apiKey) {

        if (apiKey == null) {
            notFound()
            return
        }

        try {
            apiKey.delete(flush: true)
            flash.message = message(code: 'default.deleted.message', args: [message(code: 'apiKey.label', default: 'ApiKey'), params.id])
            redirect(action: "index")
        }
        catch (DataIntegrityViolationException e) {
            flash.message = message(code: 'default.not.deleted.message', args: [message(code: 'apiKey.label', default: 'ApiKey'), params.id])
            redirect(action: "show", id: params.id)
        }
    }

    def updateTable(){
        params.order = params.order ? params.order : "asc"
        params.sort = params.sort ? params.sort : "secretKey"
        params.max = params.max as Integer
        params.offset = params.offset as Integer

        List<ApiKey> result = ApiKey.createCriteria().list(params) {
            if(params.filter)
                or{ ilike("secretKey","%"+params.filter+"%")
                    ilike("description","%"+params.filter+"%")}

        }

        String templateAsPlainText = g.render(
                template: 'apiKeyTable',
                model: [apiKeys: result]
        )
        ControllerUtils.sendObjectAsJSON(response, [
                table: templateAsPlainText,
                count: result.totalCount
        ])
    }

    protected void notFound() {
        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.not.found.message', args: [message(code: 'apiKey.label', default: 'ApiKey'), params.id])
                redirect action: "index", method: "GET"
            }
            '*'{ render status: NOT_FOUND }
        }
    }
}

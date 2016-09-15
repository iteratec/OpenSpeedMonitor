package de.iteratec.osm.measurement.environment

import de.iteratec.osm.measurement.environment.wptserverproxy.ProxyService
import de.iteratec.osm.util.I18nService
import groovy.json.JsonSlurper
import org.springframework.dao.DataIntegrityViolationException
import static org.springframework.http.HttpStatus.*
//TODO: This controller was generated due to a scaffolding bug (https://github.com/grails3-plugins/scaffolding/issues/24). The dynamically scaffolded controllers cannot handle database exceptions
//TODO: loadLocations was NOT generated
class WebPageTestServerController {

    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]
    ProxyService proxyService
    I18nService i18nService

    static scaffold = WebPageTestServer

    public Map<String, Object> loadLocations() {
        WebPageTestServer webPageTestServer = WebPageTestServer.get(params.id)
        if (webPageTestServer == null){
            flash.message = i18nService.msg('', "No WebPageTestServer found with id ${params.id}")
        }else{
            List<Location> addedLocations = proxyService.fetchLocations(webPageTestServer)
            String message = "Location-Request was successful."
            if(addedLocations.empty) {
                message += " But no locations were added"
            } else {
                message += " And some locations were added: <br>"
                addedLocations.each {
                    message += it.toString() + " <br>"
                }
            }
            flash.message = message

        }
        redirect(action: "show", id: params.id)
    }

    def index(Integer max, String filter) {
        def maxDefault = 100
        if (max) maxDefault = max
        params.max = maxDefault

        List<WebPageTestServer> result
        int count
        if(!filter) {
            result = WebPageTestServer.list(params)
            count = WebPageTestServer.list().size()
        }
        else {
            result = WebPageTestServer.findAllByLabelIlike("%"+filter+"%",params)
            count = WebPageTestServer.findAllByLabelIlike("%"+filter+"%").size()
        }


        respond result, model:[webPageTestServerCount: count, filter:filter?filter:""]
    }

    def show(WebPageTestServer webPageTestServer) {
        respond webPageTestServer
    }

    def create() {
        respond new WebPageTestServer(params)
    }

    def save(WebPageTestServer webPageTestServer) {
        if (webPageTestServer == null) {
            
            notFound()
            return
        }

        if (webPageTestServer.hasErrors()) {

            respond webPageTestServer.errors, view:'create'
            return
        }

        webPageTestServer.save flush:true

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.created.message', args: [message(code: 'webPageTestServer.label', default: 'WebPageTestServer'), webPageTestServer.id])
                redirect webPageTestServer
            }
            '*' { respond webPageTestServer, [status: CREATED] }
        }
    }

    def edit(WebPageTestServer webPageTestServer) {
        respond webPageTestServer
    }

    def update(WebPageTestServer webPageTestServer) {
        if (webPageTestServer == null) {

            notFound()
            return
        }

        if (webPageTestServer.hasErrors()) {

            respond webPageTestServer.errors, view:'edit'
            return
        }

        webPageTestServer.save flush:true

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.updated.message', args: [message(code: 'webPageTestServer.label', default: 'WebPageTestServer'), webPageTestServer.id])
                redirect webPageTestServer
            }
            '*'{ respond webPageTestServer, [status: OK] }
        }
    }

    def delete(WebPageTestServer webPageTestServer) {

        if (webPageTestServer == null) {
            notFound()
            return
        }

        try {
            webPageTestServer.delete(flush: true)
            flash.message = message(code: 'default.deleted.message', args: [message(code: 'webPageTestServer.label', default: 'WebPageTestServer'), params.id])
            redirect(action: "index")
        }
        catch (DataIntegrityViolationException e) {
            flash.message = message(code: 'default.not.deleted.message', args: [message(code: 'webPageTestServer.label', default: 'WebPageTestServer'), params.id])
            redirect(action: "show", id: params.id)
        }
    }

    protected void notFound() {
        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.not.found.message', args: [message(code: 'webPageTestServer.label', default: 'WebPageTestServer'), params.id])
                redirect action: "index", method: "GET"
            }
            '*'{ render status: NOT_FOUND }
        }
    }
}

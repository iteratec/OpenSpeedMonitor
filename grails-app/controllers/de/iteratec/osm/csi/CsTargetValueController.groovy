package de.iteratec.osm.csi

import de.iteratec.osm.util.ControllerUtils
import grails.converters.JSON
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus

import javax.servlet.http.HttpServletResponse

import static org.springframework.http.HttpStatus.*
//TODO: This controller was generated due to a scaffolding bug (https://github.com/grails3-plugins/scaffolding/issues/24). The dynamically scaffolded controllers cannot handle database exceptions

class CsTargetValueController {

    static scaffold = CsTargetValue
    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    def index(Integer max) {
        def maxDefault = 100
        if (max) maxDefault = max
        params.max = maxDefault
        respond CsTargetValue.list(params), model:[csTargetValueCount: CsTargetValue.count()]
    }

    def show(CsTargetValue csTargetValue) {
        respond csTargetValue
    }

    def create() {
        respond new CsTargetValue(params)
    }

    def save(CsTargetValue csTargetValue) {
        if (csTargetValue == null) {
            
            notFound()
            return
        }

        if (csTargetValue.hasErrors()) {

            respond csTargetValue.errors, view:'create'
            return
        }

        csTargetValue.save flush:true

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.created.message', args: [message(code: 'csTargetValue.label', default: 'CsTargetValue'), csTargetValue.id])
                redirect csTargetValue
            }
            '*' { respond csTargetValue, [status: CREATED] }
        }
    }

    def edit(CsTargetValue csTargetValue) {
        respond csTargetValue
    }

    def update(CsTargetValue csTargetValue) {
        if (csTargetValue == null) {

            notFound()
            return
        }

        if (csTargetValue.hasErrors()) {

            respond csTargetValue.errors, view:'edit'
            return
        }

        csTargetValue.save flush:true

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.updated.message', args: [message(code: 'csTargetValue.label', default: 'CsTargetValue'), csTargetValue.id])
                redirect csTargetValue
            }
            '*'{ respond csTargetValue, [status: OK] }
        }
    }

    def delete(CsTargetValue csTargetValue) {

        if (csTargetValue == null) {
            notFound()
            return
        }

        try {
            csTargetValue.delete(flush: true)
            flash.message = message(code: 'default.deleted.message', args: [message(code: 'csTargetValue.label', default: 'CsTargetValue'), params.id])
            redirect(action: "index")
        }
        catch (DataIntegrityViolationException e) {
            flash.message = message(code: 'default.not.deleted.message', args: [message(code: 'csTargetValue.label', default: 'CsTargetValue'), params.id])
            redirect(action: "show", id: params.id)
        }
    }

    def updateTable(){
        params.order = params.order ? params.order : "asc"
        params.sort = params.sort ? params.sort : "label"
        params.max = params.max as Integer
        params.offset = params.offset as Integer

        List<CsTargetValue> result = CsTargetValue.createCriteria().list(params) {
            if(params.filter &&params.filter.isNumber())
                or{ eq("csInPercent",Double.valueOf(params.filter)) }
        }
        String templateAsPlainText = g.render(
                template: 'csTargetValueTable',
                model: [csTargetValues: result]
        )
        ControllerUtils.sendObjectAsJSON(response, [
                table: templateAsPlainText,
                count: result.totalCount
        ])
    }

    protected void notFound() {
        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.not.found.message', args: [message(code: 'csTargetValue.label', default: 'CsTargetValue'), params.id])
                redirect action: "index", method: "GET"
            }
            '*'{ render status: NOT_FOUND }
        }
    }
}

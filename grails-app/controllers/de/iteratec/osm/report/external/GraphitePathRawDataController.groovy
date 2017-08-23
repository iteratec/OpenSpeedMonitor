package de.iteratec.osm.report.external

import de.iteratec.osm.util.ControllerUtils
import grails.converters.JSON
import org.hibernate.Criteria
import org.hibernate.sql.JoinType
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus

import javax.servlet.http.HttpServletResponse

import static org.springframework.http.HttpStatus.*
//TODO: This controller was generated due to a scaffolding bug (https://github.com/grails3-plugins/scaffolding/issues/24). The dynamically scaffolded controllers cannot handle database exceptions
class GraphitePathRawDataController {

    static scaffold = GraphitePathRawData
    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    def index() {
     
    }

    def show(GraphitePathRawData graphitePathRawData) {
        respond graphitePathRawData
    }

    def create() {
        respond new GraphitePathRawData(params)
    }

    def save(GraphitePathRawData graphitePathRawData) {
        if (graphitePathRawData == null) {
            
            notFound()
            return
        }

        if (graphitePathRawData.hasErrors()) {

            respond graphitePathRawData.errors, view:'create'
            return
        }

        graphitePathRawData.save flush:true

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.created.message', args: [message(code: 'graphitePathRawData.label', default: 'GraphitePathRawData'), graphitePathRawData.id])
                redirect graphitePathRawData
            }
            '*' { respond graphitePathRawData, [status: CREATED] }
        }
    }

    def edit(GraphitePathRawData graphitePathRawData) {
        respond graphitePathRawData
    }

    def update(GraphitePathRawData graphitePathRawData) {
        if (graphitePathRawData == null) {

            notFound()
            return
        }

        if (graphitePathRawData.hasErrors()) {

            respond graphitePathRawData.errors, view:'edit'
            return
        }

        graphitePathRawData.save flush:true

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.updated.message', args: [message(code: 'graphitePathRawData.label', default: 'GraphitePathRawData'), graphitePathRawData.id])
                redirect graphitePathRawData
            }
            '*'{ respond graphitePathRawData, [status: OK] }
        }
    }

    def delete(GraphitePathRawData graphitePathRawData) {

        if (graphitePathRawData == null) {
            notFound()
            return
        }

        try {
            graphitePathRawData.delete(flush: true)
            flash.message = message(code: 'default.deleted.message', args: [message(code: 'graphitePathRawData.label', default: 'GraphitePathRawData'), params.id])
            redirect(action: "index")
        }
        catch (DataIntegrityViolationException e) {
            flash.message = message(code: 'default.not.deleted.message', args: [message(code: 'graphitePathRawData.label', default: 'GraphitePathRawData'), params.id])
            redirect(action: "show", id: params.id)
        }
    }
    def updateTable(){
        params.order = params.order ? params.order : "asc"
        params.sort = params.sort ? params.sort : "prefix"
        params.max = params.max as Integer
        params.offset = params.offset as Integer

        List<GraphitePathRawData> result = GraphitePathRawData.createCriteria().list(params) {
            if(params.filter)
                or{
                    ilike("prefix","%"+params.filter+"%")
                    ilike("measurand","%"+params.filter+"%")
                    ilike("cachedView","%"+params.filter+"%")
                }
        }
        String templateAsPlainText = g.render(
                template: 'graphitePathRawDataTable',
                model: [graphitePathsRawData: result]
        )
        ControllerUtils.sendObjectAsJSON(response, [
                table: templateAsPlainText,
                count: result.totalCount
        ])
    }

    protected void notFound() {
        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.not.found.message', args: [message(code: 'graphitePathRawData.label', default: 'GraphitePathRawData'), params.id])
                redirect action: "index", method: "GET"
            }
            '*'{ render status: NOT_FOUND }
        }
    }
}

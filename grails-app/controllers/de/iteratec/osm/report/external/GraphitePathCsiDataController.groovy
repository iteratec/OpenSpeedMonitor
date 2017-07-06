package de.iteratec.osm.report.external

import de.iteratec.osm.util.ControllerUtils
import org.springframework.dao.DataIntegrityViolationException

import static org.springframework.http.HttpStatus.CREATED
import static org.springframework.http.HttpStatus.NOT_FOUND
import static org.springframework.http.HttpStatus.OK

class GraphitePathCsiDataController {

    static scaffold = GraphitePathCsiData
    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    def index() { }

    def show(GraphitePathCsiData graphitePathCsiData) {
        respond graphitePathCsiData
    }

    def create() {
        respond new GraphitePathCsiData(params)
    }

    def save(GraphitePathCsiData graphitePathCsiData) {
        if (graphitePathCsiData == null) {

            notFound()
            return
        }

        if (graphitePathCsiData.hasErrors()) {

            respond graphitePathCsiData.errors, view:'create'
            return
        }

        graphitePathCsiData.save flush:true

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.created.message', args: [message(code: 'graphitePathCsiData.label', default: 'GraphitePathCsiData'), graphitePathCsiData.id])
                redirect(action: "show", id: graphitePathCsiData.id)
            }
            '*' { respond graphitePathCsiData, [status: CREATED] }
        }
    }

    def edit(GraphitePathCsiData graphitePathCsiData) {
        respond graphitePathCsiData
    }

    def update(GraphitePathCsiData graphitePathCsiData) {
        if (graphitePathCsiData == null) {

            notFound()
            return
        }

        if (graphitePathCsiData.hasErrors()) {

            respond graphitePathCsiData.errors, view:'edit'
            return
        }

        graphitePathCsiData.save flush:true

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.updated.message', args: [message(code: 'graphitePathCsiData.label', default: 'gGraphitePathCsiData'), graphitePathCsiData.id])
                redirect graphitePathCsiData
            }
            '*'{ respond graphitePathCsiData, [status: OK] }
        }
    }

    def delete(GraphitePathCsiData graphitePathCsiData) {

        if (graphitePathCsiData == null) {
            notFound()
            return
        }

        try {
            graphitePathCsiData.delete(flush: true)
            flash.message = message(code: 'default.deleted.message', args: [message(code: 'graphitePathCsiData.label', default: 'GraphitePathCsiData'), params.id])
            redirect(action: "index")
        }
        catch (DataIntegrityViolationException e) {
            flash.message = message(code: 'default.not.deleted.message', args: [message(code: 'graphitePathCsiData.label', default: 'GraphitePathCsiData'), params.id])
            redirect(action: "show", id: params.id)
        }
    }
    def updateTable(){
        params.order = params.order ? params.order : "asc"
        params.sort = params.sort ? params.sort : "prefix"
        params.max = params.max as Integer
        params.offset = params.offset as Integer

        List<GraphitePathCsiData> result = GraphitePathCsiData.createCriteria().list(params) {
            if(params.filter)
                or{
                    ilike("prefix","%"+params.filter+"%")
                    ilike("aggregationType","%"+params.filter+"%")
                }
        }
        String templateAsPlainText = g.render(
                template: 'graphitePathCsiDataTable',
                model: [graphitePathsCsiData: result]
        )
        ControllerUtils.sendObjectAsJSON(response, [
                table: templateAsPlainText,
                count: result.totalCount
        ])
    }

    protected void notFound() {
        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.not.found.message', args: [message(code: 'graphitePathCsiData.label', default: 'GraphitePathCsiData'), params.id])
                redirect action: "index", method: "GET"
            }
            '*'{ render status: NOT_FOUND }
        }
    }
}

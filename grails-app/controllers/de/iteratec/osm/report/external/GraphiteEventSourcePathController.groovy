package de.iteratec.osm.report.external

import grails.converters.JSON
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus

import javax.servlet.http.HttpServletResponse

import static org.springframework.http.HttpStatus.*
//TODO: This controller was generated due to a scaffolding bug (https://github.com/grails3-plugins/scaffolding/issues/24). The dynamically scaffolded controllers cannot handle database exceptions
class GraphiteEventSourcePathController {

    static scaffold = GraphiteEventSourcePath
    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    def index(Integer max) {
        def maxDefault = 100
        if (max) maxDefault = max
        params.max = maxDefault
        respond GraphiteEventSourcePath.list(params), model:[graphiteEventSourcePathCount: GraphiteEventSourcePath.count()]
    }

    def show(GraphiteEventSourcePath graphiteEventSourcePath) {
        respond graphiteEventSourcePath
    }

    def create() {
        respond new GraphiteEventSourcePath(params)
    }

    def save(GraphiteEventSourcePath graphiteEventSourcePath) {
        if (graphiteEventSourcePath == null) {
            
            notFound()
            return
        }

        if (graphiteEventSourcePath.hasErrors()) {

            respond graphiteEventSourcePath.errors, view:'create'
            return
        }

        graphiteEventSourcePath.save flush:true

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.created.message', args: [message(code: 'graphiteEventSourcePath.label', default: 'GraphiteEventSourcePath'), graphiteEventSourcePath.id])
                redirect graphiteEventSourcePath
            }
            '*' { respond graphiteEventSourcePath, [status: CREATED] }
        }
    }

    def edit(GraphiteEventSourcePath graphiteEventSourcePath) {
        respond graphiteEventSourcePath
    }

    def update(GraphiteEventSourcePath graphiteEventSourcePath) {
        if (graphiteEventSourcePath == null) {

            notFound()
            return
        }

        if (graphiteEventSourcePath.hasErrors()) {

            respond graphiteEventSourcePath.errors, view:'edit'
            return
        }

        graphiteEventSourcePath.save flush:true

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.updated.message', args: [message(code: 'graphiteEventSourcePath.label', default: 'GraphiteEventSourcePath'), graphiteEventSourcePath.id])
                redirect graphiteEventSourcePath
            }
            '*'{ respond graphiteEventSourcePath, [status: OK] }
        }
    }

    def delete(GraphiteEventSourcePath graphiteEventSourcePath) {

        if (graphiteEventSourcePath == null) {
            notFound()
            return
        }

        try {
            graphiteEventSourcePath.delete(flush: true)
            flash.message = message(code: 'default.deleted.message', args: [message(code: 'graphiteEventSourcePath.label', default: 'GraphiteEventSourcePath'), params.id])
            redirect(action: "index")
        }
        catch (DataIntegrityViolationException e) {
            flash.message = message(code: 'default.not.deleted.message', args: [message(code: 'graphiteEventSourcePath.label', default: 'GraphiteEventSourcePath'), params.id])
            redirect(action: "show", id: params.id)
        }
    }
    def updateTable(){
        params.order = params.order ? params.order : "asc"
        params.sort = params.sort ? params.sort : "staticPrefix"
        params.sort = params.sort=="jobGroups" ? "staticPrefix": params.sort // mysql cannot sort by a list
        params.max = params.max as Integer
        params.offset = params.offset as Integer
        List<GraphiteEventSourcePath> result = GraphiteEventSourcePath.createCriteria().list(params) {
            if(params.filter)
                or{ilike("staticPrefix","%"+params.filter+"%")
                   ilike("targetMetricName","%"+params.filter+"%")}
        }
        String templateAsPlainText = g.render(
                template: 'graphiteEventSourcePathTable',
                model: [graphiteEventSourcePaths: result]
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
                flash.message = message(code: 'default.not.found.message', args: [message(code: 'graphiteEventSourcePath.label', default: 'GraphiteEventSourcePath'), params.id])
                redirect action: "index", method: "GET"
            }
            '*'{ render status: NOT_FOUND }
        }
    }
}

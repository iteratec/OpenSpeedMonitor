package de.iteratec.osm.result

import de.iteratec.osm.util.ControllerUtils
import grails.converters.JSON
import org.hibernate.sql.JoinType
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus

import javax.servlet.http.HttpServletResponse

import static org.springframework.http.HttpStatus.*
//TODO: This controller was generated due to a scaffolding bug (https://github.com/grails3-plugins/scaffolding/issues/24). The dynamically scaffolded controllers cannot handle database exceptions
class MeasuredEventController {

    static scaffold = MeasuredEvent
    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    def index(Integer max) {
        def maxDefault = 100
        if (max) maxDefault = max
        params.max = maxDefault
        respond MeasuredEvent.list(params), model:[measuredEventCount: MeasuredEvent.count()]
    }

    def show(MeasuredEvent measuredEvent) {
        respond measuredEvent
    }

    def create() {
        respond new MeasuredEvent(params)
    }

    def save(MeasuredEvent measuredEvent) {
        if (measuredEvent == null) {
            
            notFound()
            return
        }

        if (measuredEvent.hasErrors()) {

            respond measuredEvent.errors, view:'create'
            return
        }

        measuredEvent.save flush:true

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.created.message', args: [message(code: 'measuredEvent.label', default: 'MeasuredEvent'), measuredEvent.id])
                redirect measuredEvent
            }
            '*' { respond measuredEvent, [status: CREATED] }
        }
    }

    def edit(MeasuredEvent measuredEvent) {
        respond measuredEvent
    }

    def update(MeasuredEvent measuredEvent) {
        if (measuredEvent == null) {

            notFound()
            return
        }

        if (measuredEvent.hasErrors()) {

            respond measuredEvent.errors, view:'edit'
            return
        }

        measuredEvent.save flush:true

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.updated.message', args: [message(code: 'measuredEvent.label', default: 'MeasuredEvent'), measuredEvent.id])
                redirect measuredEvent
            }
            '*'{ respond measuredEvent, [status: OK] }
        }
    }

    def updateTable(){
        params.order = params.order ? params.order : "asc"
        params.sort = params.sort ? params.sort : "name"
        params.max = params.max as Integer
        params.offset = params.offset as Integer
        List<MeasuredEvent> result = MeasuredEvent.createCriteria().list(params) {
            createAlias('csiConfiguration', 'testedPageAlias', JoinType.LEFT_OUTER_JOIN)

            if(params.filter)
                or{
                    ilike("name","%"+params.filter+"%")
                    ilike("testedPageAlias.name","%"+params.filter+"%")
                }
        }
        String templateAsPlainText = g.render(
                template: 'measuredEventTable',
                model: [measuredEvents: result]
        )
        ControllerUtils.sendObjectAsJSON(response, [
                table: templateAsPlainText,
                count: result.totalCount
        ])
    }

    protected void notFound() {
        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.not.found.message', args: [message(code: 'measuredEvent.label', default: 'MeasuredEvent'), params.id])
                redirect action: "index", method: "GET"
            }
            '*'{ render status: NOT_FOUND }
        }
    }
}

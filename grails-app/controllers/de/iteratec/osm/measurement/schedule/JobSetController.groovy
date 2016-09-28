package de.iteratec.osm.measurement.schedule

import grails.converters.JSON
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus

import javax.servlet.http.HttpServletResponse

import static org.springframework.http.HttpStatus.*
//TODO: This controller was generated due to a scaffolding bug (https://github.com/grails3-plugins/scaffolding/issues/24). The dynamically scaffolded controllers cannot handle database exceptions
class JobSetController {

    static scaffold = JobSet
    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    JobDaoService jobDaoService

    def index() {
    }

    def show(JobSet jobSet) {
        respond jobSet
    }

    def create() {
        respond new JobSet(params), model:[allJobs: jobDaoService.getAllJobs()]
    }

    def save(JobSet jobSet) {
        if (jobSet == null) {
            notFound()
            return
        }

        if (jobSet.hasErrors()) {

            respond jobSet.errors, view:'create'
            return
        }

        jobSet.save flush:true

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.created.message', args: [message(code: 'jobSet.label', default: 'JobSet'), jobSet.id])
                redirect jobSet
            }
            '*' { respond jobSet, [status: CREATED] }
        }
    }

    def edit(JobSet jobSet) {
        respond jobSet, model:[allJobs: jobDaoService.getAllJobs()]
    }

    def update(JobSet jobSet) {
        if (jobSet == null) {

            notFound()
            return
        }

        if (jobSet.hasErrors()) {

            respond jobSet.errors, view:'edit', model:[allJobs: jobDaoService.getAllJobs()]
            return
        }

        jobSet.save flush:true

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.updated.message', args: [message(code: 'jobSet.label', default: 'JobSet'), jobSet.id])
                redirect jobSet
            }
            '*'{ respond jobSet, [status: OK] }
        }
    }

    def delete(JobSet jobSet) {

        if (jobSet == null) {
            notFound()
            return
        }

        try {
            jobSet.delete(flush: true)
            flash.message = message(code: 'default.deleted.message', args: [message(code: 'jobSet.label', default: 'JobSet'), params.id])
            redirect(action: "index")
        }
        catch (DataIntegrityViolationException e) {
            flash.message = message(code: 'default.not.deleted.message', args: [message(code: 'jobSet.label', default: 'JobSet'), params.id])
            redirect(action: "show", id: params.id)
        }
    }
    def updateTable(){
        params.order = params.order ? params.order : "desc"
        params.sort = params.sort ? params.sort : "name"
        def paramsForCount = Boolean.valueOf(params.limitResults) ? [max:1000]:[:]
        params.sort = params.sort == "jobs" ? "name" :  params.sort // cannot order by a list
        params.max = params.max as Integer
        params.offset = params.offset as Integer
        List<JobSet> result
        int count
        result = JobSet.createCriteria().list(params) {
            if(params.filter)ilike("name","%"+params.filter+"%")
        }
        count = JobSet.createCriteria().list(paramsForCount) {
            if(params.filter)ilike("name","%"+params.filter+"%")
        }.size()
        String templateAsPlainText = g.render(
                template: 'jobSetTable',
                model: [jobSets: result]
        )
        def jsonResult = [table:templateAsPlainText, count:count]as JSON
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
                flash.message = message(code: 'default.not.found.message', args: [message(code: 'jobSet.label', default: 'JobSet'), params.id])
                redirect action: "index", method: "GET"
            }
            '*'{ render status: NOT_FOUND }
        }
    }
}

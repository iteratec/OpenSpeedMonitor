package de.iteratec.osm.measurement.schedule

import org.springframework.dao.DataIntegrityViolationException
import static org.springframework.http.HttpStatus.*
//TODO: This controller was generated due to a scaffolding bug (https://github.com/grails3-plugins/scaffolding/issues/24). The dynamically scaffolded controllers cannot handle database exceptions
class JobSetController {

    static scaffold = JobSet
    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    JobDaoService jobDaoService

    def index(Integer max) {
        def maxDefault = 100
        if (max) maxDefault = max
        params.max = maxDefault
        respond JobSet.list(params), model:[jobSetCount: JobSet.count()]
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

package de.iteratec.osm.measurement.setup

import de.iteratec.osm.annotations.RestAction
import de.iteratec.osm.csi.Page
import de.iteratec.osm.measurement.environment.Location
import de.iteratec.osm.measurement.schedule.ConnectivityProfile
import de.iteratec.osm.measurement.schedule.Job
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.measurement.script.Script
import de.iteratec.osm.result.MeasuredEvent
import de.iteratec.osm.util.ControllerUtils
import de.iteratec.osm.util.ExceptionHandlerController
import grails.converters.JSON

class MeasurementSetupController extends ExceptionHandlerController {

    def index() {
        forward(action: 'create')
    }

    def create() {
        return createStaticViewData()
    }

    def save() {
        Script script = new Script(params.script)
        JobGroup jobGroup = getOrCreateJobGroup(params.jobGroup.name)
        Location location = Location.findById(params.location)
        ConnectivityProfile connectivityProfile = ConnectivityProfile.findByName(params.connectivity)
        Job job = new Job()
        job.properties = params.job
        // update properties instead of using 'new Job(params.job)' to avoid overriding default values
        job.location = location
        job.connectivityProfile = connectivityProfile
        job.jobGroup = jobGroup
        job.script = script
        // set default to 60 minutes
        job.maxDownloadTimeInMinutes = 60

        def errors = []

        if (!jobGroup.validate()) errors.addAll(jobGroup.getErrors()*.fieldError)
        if (!script.validate()) errors.addAll(script.getErrors()*.fieldError)
        if (!job.validate()) errors.addAll(job.getErrors()*.fieldError)

        if (errors.size() == 0) {
            job.active = true
            job.save(failOnError: true)
            redirect(controller: 'job', action: 'index')
            return
        }

        // on error
        Map modelToRender = createStaticViewData()
        modelToRender['errors'] = errors
        modelToRender['script'] = params.script
        modelToRender['jobGroup'] = params.jobGroup
        modelToRender['location'] = params.location
        modelToRender['connectivity'] = params.connectivity

        render(view: 'create', model: modelToRender)
    }

    private JobGroup getOrCreateJobGroup(String jobGroupName) {
        JobGroup jobGroup = JobGroup.findByName(jobGroupName)
        if (!jobGroup) {
            jobGroup = new JobGroup(name: jobGroupName)
        }
        return jobGroup
    }

    private Map createStaticViewData() {
        return [script: new Script(params), pages: Page.list(), measuredEvents: MeasuredEvent.list() as JSON, archivedScripts: "", allJobGroups: JobGroup.list()]
    }

    @RestAction
    def getScriptNames() {
        def scriptNames = Script.createCriteria().list {
            projections {
                property('label')
            }
        }
        ControllerUtils.sendObjectAsJSON(response, scriptNames)
    }
}

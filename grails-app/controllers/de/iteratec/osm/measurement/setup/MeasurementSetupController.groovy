package de.iteratec.osm.measurement.setup

import de.iteratec.osm.csi.Page
import de.iteratec.osm.measurement.environment.Location
import de.iteratec.osm.measurement.schedule.ConnectivityProfile
import de.iteratec.osm.measurement.schedule.Job
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.measurement.script.Script
import de.iteratec.osm.result.MeasuredEvent
import grails.converters.JSON

class MeasurementSetupController {

    def index() {
        forward(action: 'create')
    }

    def create() {
        [script: new Script(params), pages: Page.list(), measuredEvents: MeasuredEvent.list() as JSON, archivedScripts: ""]
    }

    def save() {
        Script script = new Script(params.script)
        JobGroup jobGroup = new JobGroup(params.jobGroup)
        Location location = Location.findById(params.location)
        ConnectivityProfile connectivityProfile = ConnectivityProfile.findByName(params.connectivity)
        Job job = new Job()
        job.properties = params.job // update properties instead of using 'new Job(params.job)' to avoid overriding default values
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
        def modelToRender = [errors: errors, script: params.script, jobGroup: params.jobGroup, location: params.location, connectivity: params.connectivity, pages: Page.list(), measuredEvents: MeasuredEvent.list() as JSON, archivedScripts: ""]
        render(view: 'create', model: modelToRender)
    }
}

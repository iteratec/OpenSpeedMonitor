package de.iteratec.osm.measurement.setup

import de.iteratec.osm.annotations.RestAction
import de.iteratec.osm.csi.Page
import de.iteratec.osm.measurement.environment.Location
import de.iteratec.osm.measurement.environment.WebPageTestServer
import de.iteratec.osm.measurement.schedule.ConnectivityProfile
import de.iteratec.osm.measurement.schedule.Job
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.measurement.schedule.JobProcessingService
import de.iteratec.osm.measurement.script.PlaceholdersUtility
import de.iteratec.osm.measurement.script.Script
import de.iteratec.osm.measurement.script.ScriptParser
import de.iteratec.osm.measurement.script.ScriptService
import de.iteratec.osm.result.MeasuredEvent
import de.iteratec.osm.result.PageService
import de.iteratec.osm.util.ControllerUtils
import de.iteratec.osm.util.ExceptionHandlerController
import grails.converters.JSON

class MeasurementSetupController extends ExceptionHandlerController {
    ScriptService scriptService
    JobProcessingService jobProcessingService
    PageService pageService

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
        job.captureVideo = true
        job.executionSchedule = "0 " + job.executionSchedule // prepend seconds, user shouldn't do this
        // update properties instead of using 'new Job(params.job)' to avoid overriding default values
        job.location = location
        job.connectivityProfile = connectivityProfile
        job.jobGroup = jobGroup
        job.script = script
        // set default to 60 minutes
        job.maxDownloadTimeInMinutes = 60

        def errors = []
        def customError

        if (!jobGroup.validate()) errors.addAll(jobGroup.getErrors()*.fieldError)
        if (!script.validate()) errors.addAll(script.getErrors()*.fieldError)
        if (!job.validate()) errors.addAll(job.getErrors()*.fieldError)

        if (errors.empty) {
            if (!PlaceholdersUtility.getPlaceholdersUsedInScript(script).empty) {
                customError = "de.iteratec.osm.measurement.setup.variablesUsed"
            }
            if (customError == null) {
                job.active = true
                if (!job.save()) {
                    customError = "de.iteratec.osm.measurement.setup.jobPersistenceError"
                }
            }
            if (customError == null) {
                scriptService.createNewPagesAndMeasuredEvents(new ScriptParser(pageService, script.navigationScript))
                try {
                    jobProcessingService.launchJobRun(job)
                } catch (Exception exception) {
                    log.error(exception.getMessage(), exception)
                }

                redirect(controller: 'job', action: 'index')
                return
            }
        }

        // on error
        Map modelToRender = createStaticViewData()
        modelToRender['errors'] = errors
        modelToRender['customError'] = customError
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
        List<WebPageTestServer> wptServers = WebPageTestServer.findAllByActive(true, [sort: "label"])
        Map wptServersWithLocations = [:]
        wptServers.each { server ->
            wptServersWithLocations[server.label] = Location.findAllByWptServerAndActive(server, true, [sort: "label"])
        }
        String defaultScript = "setEventName\tHomepage:::Homepage\n" +
                               "navigate\thttp://my-awesome-application.com"
        return [script: new Script(params),
                pages: Page.list(),
                measuredEvents: MeasuredEvent.list() as JSON,
                archivedScripts: "",
                allJobGroups: JobGroup.list([sort: "name"]),
                connectivityProfiles: ConnectivityProfile.findAllByActive(true,  [sort: "name"]),
                wptServersWithLocations: wptServersWithLocations,
                defaultScript: defaultScript
        ]
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

    @RestAction
    def getJobNames() {
        def jobNames = Job.createCriteria().list {
            projections {
                property('label')
            }
        }
        ControllerUtils.sendObjectAsJSON(response, jobNames)
    }
}

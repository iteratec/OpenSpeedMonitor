package de.iteratec.osm.api

import de.iteratec.osm.api.dto.JobDto
import de.iteratec.osm.measurement.schedule.Job
import de.iteratec.osm.measurement.schedule.JobRunService
import de.iteratec.osm.measurement.schedule.JobService
import de.iteratec.osm.util.ControllerUtils
import groovy.json.JsonSlurper
import io.swagger.annotations.*
import org.quartz.CronExpression
import org.springframework.http.HttpStatus

import static de.iteratec.osm.util.Constants.DEFAULT_ACCESS_DENIED_MESSAGE

@Api(value = "/rest", tags = ["Measurement Jobs"], description = "Measurement Jobs Api", position = 1)
class JobApiController {

    JobRunService jobRunService
    JobService jobService

    @ApiOperation(
        value = "Runs measurement job with given jobId.",
        nickname = "job/{id}/run",
        produces = "application/json",
        httpMethod = "POST",
        response = String.class
    )
    @ApiResponses([
        @ApiResponse(
            code = 405,
            message = "Method Not Allowed. Only POST is allowed"
        ),
        @ApiResponse(
            code = 404,
            message = "Job with given id doesn't exist!"
        )
    ])
    @ApiImplicitParams([
        @ApiImplicitParam(
            name = "id",
            paramType = "path",
            required = true,
            value = "Id of Measurement Job to run.",
            dataType = "long"
        ),
        @ApiImplicitParam(
                name = "priority",
                paramType = "query",
                required = false,
                value = "Priority the test should be queued with. May be an Integer between 1 (high) and 9 (low)",
                dataType = "int",
                allowableValues = 'range[1,9]',
                defaultValue = '5'
        )
    ])
    Map<String, Object> runJob(Long id, Long priority) {

        Job job = Job.get(id)
        if (job == null) {
            ControllerUtils.sendSimpleResponseAsStream(response, HttpStatus.NOT_FOUND, "Job with id ${id} doesn't exist!")
        }

        String testId
        try {
            if (priority){
                testId = jobRunService.launchJobRun(job, priority)
            } else {
                testId = jobRunService.launchJobRun(job)
            }
        } catch(IllegalStateException exception) {
            ControllerUtils.sendSimpleResponseAsStream(response, HttpStatus.CONFLICT, exception.getMessage())
            log.error(exception.getMessage(), exception)
        } catch(Exception exception) {
            ControllerUtils.sendSimpleResponseAsStream(response, HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage())
            log.error(exception.getMessage(), exception)
        }

        ControllerUtils.sendObjectAsJSON(response, [target: testId], true)

    }

    /**
     * Activates the job of submitted id. It gets activated no matter whether it was active/inactive before.
     * This function can't be called without a valid apiKey as parameter.
     * @see de.iteratec.osm.filters.SecuredApiFunctionsFilters
     */
    @ApiOperation(
            value = "Activates measurement job with given id.",
            nickname = "job/{id}/activate",
            produces = "application/json",
            httpMethod = "PUT",
            response = JobDto.class,
            authorizations = @Authorization(value = "apiKey")
    )
    @ApiResponses([
            @ApiResponse(
                    code = 405,
                    message = "Method Not Allowed. Only PUT is allowed"
            ),
            @ApiResponse(
                    code = 403,
                    message = "Access denied! A valid API-Key with sufficient access rights is required!"
            ),
            @ApiResponse(
                    code = 404,
                    message = 'Job with given id doesn\'t exist'
            )
    ])
    @ApiImplicitParams([
            @ApiImplicitParam(
                    name = "apiKey",
                    paramType = "query",
                    required = true,
                    value = "An api key for OpenSpeedMonitor. Provides permissions for secured api functions.",
                    dataType = "string"
            ),
            @ApiImplicitParam(
                    name = "id",
                    paramType = "path",
                    required = true,
                    value = "Id of Measurement job to activate.",
                    dataType = "long"
            )
    ])
    public Map<String, Object> securedViaApiKeyActivateJob() {

        if (!params.validApiKey.allowedForJobActivation) {
            return ControllerUtils.sendSimpleResponseAsStream(response, HttpStatus.FORBIDDEN, DEFAULT_ACCESS_DENIED_MESSAGE)
        }

        Job job = Job.get(params.id)
        if (job == null) {
            return ControllerUtils.sendSimpleResponseAsStream(response, HttpStatus.NOT_FOUND, "Job with id ${params.id} doesn't exist!")
        }

        jobService.updateActivity(job, true)

        return ControllerUtils.sendObjectAsJSON(response, [target: JobDto.create(job.refresh())], params.pretty && params.pretty == 'true')
    }

    /**
     * Deactivates the job of submitted id. It gets deactivated no matter whether it was active/inactive before.
     * This function can't be called without a valid apiKey as parameter.
     * @see de.iteratec.osm.filters.SecuredApiFunctionsFilters
     */
    @ApiOperation(
            value = "Deactivates measurement job with given id.",
            nickname = "job/{id}/deactivate",
            produces = "application/json",
            httpMethod = "PUT",
            response = JobDto.class,
            authorizations = @Authorization(value = "apiKey")
    )
    @ApiResponses([
            @ApiResponse(
                    code = 405,
                    message = "Method Not Allowed. Only PUT is allowed"
            ),
            @ApiResponse(
                    code = 403,
                    message = "Access denied! A valid API-Key with sufficient access rights is required!"
            ),
            @ApiResponse(
                    code = 404,
                    message = 'Job with given id doesn\'t exist'
            )
    ])
    @ApiImplicitParams([
            @ApiImplicitParam(
                    name = "apiKey",
                    paramType = "query",
                    required = true,
                    value = "An api key for OpenSpeedMonitor. Provides permissions for secured api functions.",
                    dataType = "string"
            ),
            @ApiImplicitParam(
                    name = "id",
                    paramType = "path",
                    required = true,
                    value = "Id of Measurement job to deactivate.",
                    dataType = "long"
            )
    ])
    public Map<String, Object> securedViaApiKeyDeactivateJob() {

        if (!params.validApiKey.allowedForJobDeactivation) {
            return ControllerUtils.sendSimpleResponseAsStream(response, HttpStatus.FORBIDDEN, DEFAULT_ACCESS_DENIED_MESSAGE)
        }

        Job job = Job.get(params.id)
        if (job == null) {
            return ControllerUtils.sendSimpleResponseAsStream(response, HttpStatus.NOT_FOUND, "Job with id ${params.id} doesn't exist!")
        }

        jobService.updateActivity(job, false)

        return ControllerUtils.sendObjectAsJSON(response, [target: JobDto.create(job.refresh())], params.pretty && params.pretty == 'true')
    }

    /**
     * Updates execution schedule of Job with submitted id. Schedule must be a valid quartz schedule.
     * This function can't be called without a valid apiKey as parameter.
     * @see de.iteratec.osm.filters.SecuredApiFunctionsFilters
     * @see http://www.quartz-scheduler.org/documentation/quartz-2.x/tutorials/crontrigger.html
     */
    @ApiOperation(
            value = "Sets given cron expression as execution schedule for measurement job with given id.",
            nickname = "job/{id}/setExecutionSchedule",
            produces = "application/json",
            consumes = "application/json",
            httpMethod = "PUT",
            response = JobDto.class,
            authorizations = @Authorization(value = "apiKey")
    )
    @ApiResponses([
            @ApiResponse(
                    code = 405,
                    message = "Method Not Allowed. Only PUT is allowed"
            ),
            @ApiResponse(
                    code = 403,
                    message = "Access denied! A valid API-Key with sufficient access rights is required!"
            ),
            @ApiResponse(
                    code = 404,
                    message = 'Job with given id doesn\'t exist'
            ),
            @ApiResponse(
                    code = 400,
                    message = 'The body must be a valid JSON object containing \'executionSchedule\' attribute and the schedule must be a valid cron expression.'
            )
    ])
    @ApiImplicitParams([
            @ApiImplicitParam(
                    name = "apiKey",
                    paramType = "query",
                    required = true,
                    value = "An api key for OpenSpeedMonitor. Provides permissions for secured api functions.",
                    dataType = "string"
            ),
            @ApiImplicitParam(
                    name = "id",
                    paramType = "path",
                    required = true,
                    value = "Id of Measurement job to deactivate.",
                    dataType = "long"
            ),
            @ApiImplicitParam(
                    name = "executionSchedule",
                    paramType = "body",
                    required = true,
                    value = "Json containing the execution schedule to set, e.g.<pre>{executionSchedule: \"* */2 * * * ? *\"}</pre>",
                    dataType = "grails.converters.JSON"
            )
    ])
    public Map<String, Object> securedViaApiKeySetExecutionSchedule() {

        if (!params?.validApiKey?.allowedForJobSetExecutionSchedule) {
            return ControllerUtils.sendSimpleResponseAsStream(response, HttpStatus.FORBIDDEN, DEFAULT_ACCESS_DENIED_MESSAGE)
        }

        Job job = Job.get(params.id)
        if (job == null) {
            return ControllerUtils.sendSimpleResponseAsStream(response, HttpStatus.NOT_FOUND, "Job with id ${params.id} doesn't exist!")
        }

        JsonSlurper jsonSlurper = new JsonSlurper().parseText(request.getJSON().toString())
        String schedule = jsonSlurper.executionSchedule
        if (schedule == null) {
            return ControllerUtils.sendSimpleResponseAsStream(response, HttpStatus.BAD_REQUEST, "The body of your PUT request (JSON object) must contain executionSchedule.")
        }

        if (!CronExpression.isValidExpression(schedule)) {
            return ControllerUtils.sendSimpleResponseAsStream(response, HttpStatus.BAD_REQUEST, "The execution schedule you submitted in the body is invalid! ")
        }

        jobService.updateExecutionSchedule(job, schedule)

        return ControllerUtils.sendObjectAsJSON(response, [target: JobDto.create(job.refresh())], params.pretty && params.pretty == 'true')
    }

}

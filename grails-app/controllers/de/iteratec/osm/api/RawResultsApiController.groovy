package de.iteratec.osm.api

import de.iteratec.osm.api.dto.EventResultDto
import de.iteratec.osm.de.iteratec.osm.api.ResultsRequestCommand
import de.iteratec.osm.measurement.environment.BrowserService
import de.iteratec.osm.measurement.schedule.Job
import de.iteratec.osm.measurement.schedule.JobProcessingService
import de.iteratec.osm.result.EventResult
import de.iteratec.osm.result.JobResult
import de.iteratec.osm.result.MvQueryParams
import de.iteratec.osm.result.ThresholdService
import de.iteratec.osm.result.dao.EventResultDaoService
import de.iteratec.osm.util.ControllerUtils
import de.iteratec.osm.util.PerformanceLoggingService
import io.swagger.annotations.*
import org.joda.time.DateTime
import org.joda.time.Duration
import org.joda.time.format.DateTimeFormatter
import org.joda.time.format.ISODateTimeFormat
import org.springframework.http.HttpStatus

import javax.persistence.NoResultException

import static de.iteratec.osm.util.Constants.*

@Api(value = "/rest", tags = ["Measurement Results"], description = "Measurement Results Api", position = 0)
class RawResultsApiController {

    BrowserService browserService
    JobLinkService jobLinkService
    PerformanceLoggingService performanceLoggingService
    EventResultDaoService eventResultDaoService
    ThresholdService thresholdService
    JobProcessingService jobProcessingService

    /**
     * The maximum duration of time-frame sent to {@link
     * # getResults ( ResultsRequestCommand )} in hours.
     */
    private static int MAX_TIME_FRAME_DURATION_IN_HOURS = 48;

    public static final DateTimeFormatter API_DATE_FORMAT = ISODateTimeFormat.basicDateTimeNoMillis()

    /**
     * <p>
     * Returns results for specified request as JSON
     * (see {@link ResultsRequestCommand}).
     * </p>
     *
     * <p>
     * Potential outcomes of this method:
     * <dl>
     * 	<dt>HTTP status 200 OK</dt>
     *  <dd>The request handled successful, a
     * 	result in JSON notation is returned. TOOD Give response example
     *
     *  The response is of type application/json as described in RFC4627.
     *  </dd>
     *  <dt>HTTP status 400 Bad Request</dt>
     *  <dd>The end of the requested time frame is before the start of it.
     *  For sure, this is invalid. The end of the time-frame need
     *  to be after its start. A text/plain error message
     *  with details is attached as response.
     *  </dd>
     *  <dt>HTTP status 413 Request Entity Too Large</dt>
     *  <dd>
     *  The requested time-frames duration in days is wider than
     * {@link #MAX_TIME_FRAME_DURATION_IN_DAYS}. A text/plain error message
     *  with details is attached as response.
     *  </dd>
     *  <dt>HTTP status 404 Not Found</dt>
     *  <dd>
     *  If at least one of the requested elements was not found. If no further
     *  parameters specified, this need to be the specified system otherwise it
     *  could be any of them.
     *  A text/plain error message with details is attached as response.
     *  </dd>
     * </dl>
     * </p>
     *
     * @param cmd
     *         The command which need to be valid for a successful
     *         processing, not <code>null</code>.
     * @return Nothing , a JSON or an error status code is sent before.
     *
     * @see <a href="http://tools.ietf.org/html/rfc4627">RFC4627</a>
     */
    @ApiOperation(
            value = "Get Results for the given period and params.",
            nickname = "{system}/resultsbetween/{timestampFrom}/{timestampTo}",
            produces = "application/json",
            httpMethod = "GET",
            response = EventResultDto.class
    )
    @ApiResponses([
            @ApiResponse(
                    code = 405,
                    message = "Method Not Allowed. Only GET is allowed"
            ),
            @ApiResponse(
                    code = 400,
                    message = 'The end of requested period may not be before start of it or the requested period is too large.'
            ),
            @ApiResponse(
                    code = 404,
                    message = 'Some of the request arguments caused an error.'
            )
    ])
    @ApiImplicitParams([
            @ApiImplicitParam(
                    name = "system",
                    paramType = "path",
                    required = true,
                    value = "System (=JobGroup) results should be searched for.",
                    dataType = "string"
            ),
            @ApiImplicitParam(
                    name = "timestampFrom",
                    paramType = "path",
                    required = true,
                    value = "Start time in ISO 8601 format, e.g. 20140101T230000Z for 1st of January 2014, 11 PM (UTC)",
                    dataType = "string"
            ),
            @ApiImplicitParam(
                    name = "timestampTo",
                    paramType = "path",
                    required = true,
                    value = "End time in ISO 8601 format, e.g. 20140101T230000Z for 1st of January 2014, 11 PM (UTC)",
                    dataType = "string"
            ),
            @ApiImplicitParam(
                    name = "page",
                    paramType = "query",
                    required = false,
                    value = "The name of a page. If specified only results belonging to this page are returned. If parameter page and pageId are both provided pageId is used.",
                    dataType = "string"
            ),
            @ApiImplicitParam(
                    name = "pageId",
                    paramType = "query",
                    required = false,
                    value = "The ID of a page. If specified only results belonging to the page of this ID are returned. If parameter page and pageId are both provided pageId is used.",
                    dataType = "string"
            ),
            @ApiImplicitParam(
                    name = "step",
                    paramType = "query",
                    required = false,
                    value = "The name of a (measured) step. If specified only results belonging to this step are returned.",
                    dataType = "string"
            ),
            @ApiImplicitParam(
                    name = "stepId",
                    paramType = "query",
                    required = false,
                    value = "The ID of a (measured) step. If specified only results belonging to the step of this ID are returned. If parameter step and stepId are both provided stepId is used.",
                    dataType = "string"
            ),
            @ApiImplicitParam(
                    name = "browser",
                    paramType = "query",
                    required = false,
                    value = "The name of a browser. If specified only results belonging to this browser are returned.",
                    dataType = "string"
            ),
            @ApiImplicitParam(
                    name = "browserId",
                    paramType = "query",
                    required = false,
                    value = "The ID of a browser. If specified only results belonging to the browser of this ID are returned. If parameter browser and browserId are both provided browserId is used.",
                    dataType = "string"
            ),
            @ApiImplicitParam(
                    name = "location",
                    paramType = "query",
                    required = false,
                    value = "The location-address of a location. If specified only results belonging to this location are returned.",
                    dataType = "string"
            ),
            @ApiImplicitParam(
                    name = "locationId",
                    paramType = "query",
                    required = false,
                    value = "The ID of a location. If specified only results belonging to the location of this ID are returned. If parameter location and locationId are both provided locationId is used.",
                    dataType = "string"
            ),
            @ApiImplicitParam(
                    name = "cachedView",
                    paramType = "query",
                    required = false,
                    value = "The cached view. If specified only results of this cached view are returned..",
                    allowableValues = '[UNCACHED, CACHED]'
            )
    ])
    public Map<String, Object> getResults(ResultsRequestCommand cmd) {

        DateTime startDateTimeInclusive = API_DATE_FORMAT.parseDateTime(cmd.timestampFrom);
        DateTime endDateTimeInclusive = API_DATE_FORMAT.parseDateTime(cmd.timestampTo);
        if (endDateTimeInclusive.isBefore(startDateTimeInclusive)) {
            ControllerUtils.sendSimpleResponseAsStream(response, HttpStatus.BAD_REQUEST, 'End of requested time-frame may not be earlier that its requested start.')
            return
        }

        Duration requestedDuration = new Duration(startDateTimeInclusive, endDateTimeInclusive);

        if (requestedDuration.getStandardHours() > MAX_TIME_FRAME_DURATION_IN_HOURS) {
            String errorMessage = 'The requested time-frame is longer than ' +
                    MAX_TIME_FRAME_DURATION_IN_HOURS +
                    ' hours. This is too large to process. Please choose a smaller time-frame.'
            return ControllerUtils.sendSimpleResponseAsStream(response, HttpStatus.PAYLOAD_TOO_LARGE, errorMessage)
        }

        Date startTimeInclusive = startDateTimeInclusive.toDate();
        Date endTimeInclusive = endDateTimeInclusive.toDate();

        MvQueryParams queryParams = null;
        try {
            queryParams = cmd.createMvQueryParams(browserService);
        } catch (NoResultException nre) {
            return ControllerUtils.sendSimpleResponseAsStream(response, HttpStatus.NOT_FOUND, 'Some of the requests arguments caused an error: ' + nre.getMessage())
        }

        response.setContentType("application/json;charset=UTF-8");
        response.status = 200;

        List<EventResultDto> results = new LinkedList<EventResultDto>();

        performanceLoggingService.logExecutionTime(PerformanceLoggingService.LogLevel.INFO, 'assembling results for json', 1) {
            Collection<EventResult> eventResults = eventResultDaoService.getByStartAndEndTimeAndMvQueryParams(startTimeInclusive, endTimeInclusive, cmd.getCachedViewsToReturn(), queryParams)
            eventResults.each { eachEventResult ->
                results.add(new EventResultDto(eachEventResult));
            }
        }
        return ControllerUtils.sendObjectAsJSON(response, [target: results], params.pretty && params.pretty == 'true')
    }

    /**
     * Gets url's to visualize results of given {@link de.iteratec.osm.measurement.schedule.Job} in given period.
     * @param frequencyInMinutes
     * @param durationInMinutes
     * @return
     */
    @ApiOperation(
            value = "Get URL\'s to Dashboards showing Results of Measurement Job for given period.",
            nickname = "job/{id}/resultUrls/{timestampFrom}/{timestampTo}",
            produces = "application/json",
            httpMethod = "GET",
            response = EventResultDto.class
    )
    @ApiResponses([
            @ApiResponse(
                    code = 405,
                    message = "Method Not Allowed. Only GET  is allowed"
            ),
            @ApiResponse(
                    code = 400,
                    message = 'The end of requested period may not be before start of it or the requested period is too large.'
            ),
            @ApiResponse(
                    code = 404,
                    message = 'Some of the request arguments caused an error.'
            )
    ])
    @ApiImplicitParams([
            @ApiImplicitParam(
                    name = "id",
                    paramType = "path",
                    required = true,
                    value = "Id of Measurement Job to get Result Dashboard URL\'s for.",
                    dataType = "long"
            ),
            @ApiImplicitParam(
                    name = "timestampFrom",
                    paramType = "path",
                    required = true,
                    value = "Start time in ISO 8601 format, e.g. 20140101T230000Z for 1st of January 2014, 11 PM (UTC)",
                    dataType = "string"
            ),
            @ApiImplicitParam(
                    name = "timestampTo",
                    paramType = "path",
                    required = true,
                    value = "End time in ISO 8601 format, e.g. 20140101T230000Z for 1st of January 2014, 11 PM (UTC)",
                    dataType = "string"
            )
    ])
    public Map<String, Object> getResultUrls() {

        Job job = Job.get(params.id)
        if (job == null) {
            ControllerUtils.sendSimpleResponseAsStream(response, HttpStatus.NOT_FOUND, "Job with id ${params.id} doesn't exist!")
            return
        }

        if (params.timestampFrom == null || params.timestampTo == null) {
            ControllerUtils.sendSimpleResponseAsStream(response, HttpStatus.BAD_REQUEST, 'Params timestampFrom and timestampTo must be set.')
            return
        }

        DateTime start = API_DATE_FORMAT.parseDateTime(params.timestampFrom)
        DateTime end = API_DATE_FORMAT.parseDateTime(params.timestampTo)
        if (end.isBefore(start)) {
            ControllerUtils.sendSimpleResponseAsStream(response, HttpStatus.BAD_REQUEST, 'The end of requested time-frame could not be before start of time-frame.')
            return
        }

        Map<String, String> visualizingLinks = jobLinkService.getResultVisualizingLinksFor(job, start, end)

        Map objectToSend = [
            'job' : job.label,
            'from': start,
            'to'  : end
        ]
        objectToSend += visualizingLinks
        return ControllerUtils.sendObjectAsJSON(response, [target: objectToSend], params.pretty && params.pretty == 'true')
    }

    @ApiOperation(
            value = "Gets measurement result of given testId with threshold evaluation.",
            nickname = "job/thresholdResult/{testId}",
            produces = "application/json",
            httpMethod = "GET"
    )
    @ApiResponses([
            @ApiResponse(
                    code = 405,
                    message = "Method Not Allowed. Only GET is allowed"
            ),
            @ApiResponse(
                    code = 404,
                    message = 'Test with given testId is not available!'
            )
    ])
    @ApiImplicitParams([
            @ApiImplicitParam(
                    name = "testId",
                    paramType = "path",
                    required = true,
                    value = "Measurement Tests Id",
                    dataType = "string"
            )
    ])
    Map<String, Object> getThresholdResults() {
        String testId = params.testId

        if(testId == null){
            return ControllerUtils.sendSimpleResponseAsStream(response, HttpStatus.NOT_FOUND, "No test id available!")
        }

        JobResult jobResult = JobResult.findByTestId(testId)

        if(jobResult != null) {
            List results = thresholdService.checkResults(jobResult.eventResults)

            Map objectToSend = [
                    'status': jobResult.httpStatusCode,
                    'job': jobResult.job.label,
                    'results': results.flatten()
            ]

            return ControllerUtils.sendObjectAsJSON(response, [target: objectToSend], true)
        } else{
            return ControllerUtils.sendSimpleResponseAsStream(response, HttpStatus.NOT_FOUND, "Test with id ${params.testId} doesn't exist!")
        }
    }

    /**
     * Handles pending/running jobResults. Old jobResults get deleted - fresh jobResults get rescheduled
     * This function can't be called without a valid apiKey as parameter.
     * @see de.iteratec.osm.filters.SecuredApiFunctionsFilters
     */
    @ApiOperation(
            value = "Handles pending/running JobResults. Old JobResults get deleted - fresh JobResults get rescheduled.",
            nickname = "handleOldJobResults",
            produces = "application/json",
            httpMethod = "PUT",
            authorizations = @Authorization(value = "apiKey"),
            response = String.class
    )
    @ApiResponses([
            @ApiResponse(
                    code = 405,
                    message = "Method Not Allowed. Only PUT is allowed"
            ),
            @ApiResponse(
                    code = 403,
                    message = "Access denied! A valid API-Key with sufficient access rights is required!"
            )
    ])
    @ApiImplicitParams([
            @ApiImplicitParam(
                    name = "apiKey",
                    paramType = "query",
                    required = true,
                    value = "An api key for OpenSpeedMonitor. Provides permissions for secured api functions.",
                    dataType = "string"
            )
    ])
    public Map<String, Object> securedViaApiKeyHandleOldJobResults() {

        if (!params?.validApiKey?.allowedForMeasurementActivation) {
            return ControllerUtils.sendSimpleResponseAsStream(response, HttpStatus.FORBIDDEN, DEFAULT_ACCESS_DENIED_MESSAGE)
        }
        def handleOldJobResultsReturnValueMap = jobProcessingService.handleOldJobResults()
        return ControllerUtils.sendSimpleResponseAsStream(response, HttpStatus.OK, "Deleted ${handleOldJobResultsReturnValueMap["JobResultsToDeleteCount"]} JobResults and rescheduled ${handleOldJobResultsReturnValueMap["JobResultsToRescheduleCount"]} JobResults")
    }
}
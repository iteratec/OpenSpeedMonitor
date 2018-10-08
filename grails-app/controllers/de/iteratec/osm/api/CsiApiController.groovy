package de.iteratec.osm.api

import de.iteratec.osm.api.dto.CsiByEventResultsDto
import de.iteratec.osm.api.dto.CsiConfigurationDto
import de.iteratec.osm.api.dto.CsiTranslationDto
import de.iteratec.osm.csi.CsiConfiguration
import de.iteratec.osm.csi.Page
import de.iteratec.osm.csi.transformation.TimeToCsMappingService
import de.iteratec.osm.csi.weighting.WeightFactor
import de.iteratec.osm.de.iteratec.osm.api.ResultsRequestCommand
import de.iteratec.osm.measurement.environment.BrowserService
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.result.MvQueryParams
import de.iteratec.osm.util.ControllerUtils
import de.iteratec.osm.util.PerformanceLoggingService
import io.swagger.annotations.*
import org.joda.time.DateTime
import org.joda.time.Duration
import org.joda.time.format.DateTimeFormatter
import org.joda.time.format.ISODateTimeFormat
import org.springframework.http.HttpStatus

import javax.persistence.NoResultException

@Api(value = "/rest", tags = ["Customer Satisfaction"], description = "Customer Satisfaction concerned Api's", position = 3)
class CsiApiController {

    PerformanceLoggingService performanceLoggingService
    BrowserService browserService
    CsiByEventResultsService csiByEventResultsService
    TimeToCsMappingService timeToCsMappingService

    public static final DateTimeFormatter API_DATE_FORMAT = ISODateTimeFormat.basicDateTimeNoMillis()
    /**
     * The maximum duration of time-frame sent to {@link
     * # getEventResultBasedCsi ( ResultsRequestCommand )} in days.
     */
    private static int MAX_TIME_FRAME_DURATION_IN_DAYS_CSI = 8;

    @ApiOperation(
            value = "Calculates the over-all customer satisfaction index for the requested system and period.",
            nickname = "{system}/csi/{timestampFrom}/{timestampTo}",
            produces = "application/json",
            httpMethod = "GET",
            response = CsiByEventResultsDto.class
    )
    @ApiResponses([
        @ApiResponse(
            code = 400,
            message = 'The end of requested period may not be before start of it or the requested period is too large.'
        ),
        @ApiResponse(
                code = 404,
                message = 'The system/JobGroup with given id has no csi configuration or some request arguements could not be found.'
        )
    ])
    @ApiImplicitParams([
        @ApiImplicitParam(
            name = "system",
            paramType = "path",
            required = true,
            value = "System (=JobGroup) customer satisfaction index should be calculated for.",
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
        )
    ])
    public Map<String, Object> getEventResultBasedJobGroupCsi(ResultsRequestCommand cmd) {
        return getEventResultBasedCsi(cmd)
    }
    @ApiOperation(
        value = "Calculates customer satisfaction index for the requested system, page and period.",
        nickname = "{system}/{page}/csi/{timestampFrom}/{timestampTo}",
        produces = "application/json",
        httpMethod = "GET",
        response = CsiByEventResultsDto.class
    )
    @ApiResponses([
            @ApiResponse(
                    code = 400,
                    message = 'The end of requested period may not be before start of it or the requested period is too large.'
            ),
            @ApiResponse(
                    code = 404,
                    message = 'The system/JobGroup with given id has no csi configuration or some request arguements could not be found.'
            )
    ])
    @ApiImplicitParams([
            @ApiImplicitParam(
                    name = "system",
                    paramType = "path",
                    required = true,
                    value = "System (=JobGroup) customer satisfaction index should be calculated for.",
                    dataType = "string"
            ),
            @ApiImplicitParam(
                    name = "page",
                    paramType = "path",
                    required = true,
                    value = "System Page customer satisfaction index should be calculated for.",
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
            )
    ])
    public Map<String, Object> getEventResultBasedPageCsi(ResultsRequestCommand cmd) {
        return getEventResultBasedCsi(cmd)
    }

    /**
     * Gets csiConfiguration by id as JSON.
     * @param id
     * @return
     */
    @ApiOperation(
        value = "Gets csiConfiguration.",
        nickname = "csi/csiConfiguration",
        produces = "application/json",
        httpMethod = "GET",
        response = CsiConfigurationDto.class
    )
    @ApiResponses([
        @ApiResponse(
            code = 404,
            message = 'No CsiConfiguration with given id exists.'
        )
    ])
    @ApiImplicitParams([
        @ApiImplicitParam(
                name = "id",
                paramType = "query",
                required = true,
                value = "The unique id of the csiConfiguration to get.",
                dataType = "long"
        )
    ])
    public Map<String, Object> getCsiConfiguration() {

        CsiConfigurationDto jsonCsiConfiguration
        CsiConfiguration csiConfiguration = CsiConfiguration.get(params.id)
        if (csiConfiguration != null) {
            jsonCsiConfiguration = CsiConfigurationDto.create(csiConfiguration)
        } else {
            ControllerUtils.sendSimpleResponseAsStream(response, HttpStatus.NOT_FOUND, "CsiConfiguration with id ${params.id} doesn't exist!")
            return
        }

        return ControllerUtils.sendObjectAsJSON(response, [target: jsonCsiConfiguration], params.pretty && params.pretty == 'true')
    }

    /**
     * Calculates loadTimeInMillisecs to customer satisfaction. Assumption is that doc complete time is for {@link de.iteratec.osm.csi.Page} with name pageName.
     * @param loadTimeInMillisecs
     * 	Doc complete time to translate.
     * @param pageName
     * 	Name of the page the doc complete time was measured for.
     * @param csiConfiguration
     * 	Label of the csiConfiguration to use
     * @return
     * The calculated customer satisfaction for the given doc complete time.
     */
    @ApiOperation(
        value = "Calculates customer satisfaction for given loadTimeInMillisecs.",
        nickname = "csi/translateToCustomerSatisfaction",
        produces = "application/json",
        httpMethod = "GET",
        response = CsiTranslationDto.class
    )
    @ApiResponses([
        @ApiResponse(
            code = 400,
            message = 'Params loadTimeInMillisecs OR pageName OR (csiConfiguration OR system) aren\'t set.'
        ),
        @ApiResponse(
                code = 404,
                message = 'Page with given pageName doesn\'t exist.'
        ),
        @ApiResponse(
                code = 404,
                message = 'CsiConfiguration with given name doesn\'t exist.'
        )
    ])
    @ApiImplicitParams([
            @ApiImplicitParam(
                    name = "pageName",
                    paramType = "query",
                    required = true,
                    value = "The name of the page the doc complete time was measured for.",
                    dataType = "string"
            ),
            @ApiImplicitParam(
                    name = "loadTimeInMillisecs",
                    paramType = "query",
                    required = true,
                    value = "Load time to translate to customer satisfaction.",
                    dataType = "int"
            ),
            @ApiImplicitParam(
                    name = "csiConfiguration",
                    paramType = "query",
                    required = false,
                    value = "The name of the csiConfiguration to use for calculation. If omitted a system has to be given instead.",
                    dataType = "string"
            ),
            @ApiImplicitParam(
                    name = "system",
                    paramType = "query",
                    required = false,
                    value = "The name of the measurement system (=JobGroup) to use for calculation. If omitted a csiConfiguration has to be given instead.",
                    dataType = "string"
            )
    ])
    public Map<String, Object> translateToCustomerSatisfaction(TranslateCustomerSatisfactionCommand cmd) {

        if (cmd.loadTimeInMillisecs == null || cmd.pageName == null || (cmd.csiConfiguration == null && cmd.system == null)) {
            ControllerUtils.sendSimpleResponseAsStream(response, HttpStatus.BAD_REQUEST,
                    'Params loadTimeInMillisecs AND pageName AND (csiConfiguration or system) must be set.\n')
            return
        } else {
            Page page = Page.findByName(cmd.pageName)
            CsiConfiguration csiConfig = cmd.findCsiConfiguration()
            if (page == null) {
                ControllerUtils.sendSimpleResponseAsStream(response, HttpStatus.NOT_FOUND, "Page with name ${cmd.pageName} couldn't be found.\n")
                return
            } else if (csiConfig == null) {
                ControllerUtils.sendSimpleResponseAsStream(response, HttpStatus.NOT_FOUND, "CsiConfiguration couldn't be found\n")
                return
            } else {
                ControllerUtils.sendObjectAsJSON(
                    response,
                    new CsiTranslationDto(
                        loadTimeInMillisecs: cmd.loadTimeInMillisecs,
                        customerSatisfactionInPercent: timeToCsMappingService.getCustomerSatisfactionInPercent(cmd.loadTimeInMillisecs, page, csiConfig)),
                    params.pretty && params.pretty == 'true'
                )
            }
        }
    }

    private Map<String, Object> getEventResultBasedCsi(ResultsRequestCommand cmd) {

        if(!validateCsiParams(cmd)){
            return
        }

        MvQueryParams queryParams = null;
        try {
            performanceLoggingService.logExecutionTimeSilently(PerformanceLoggingService.LogLevel.DEBUG, "construct query params", 1) {
                queryParams = cmd.createMvQueryParams(browserService)
            }
        } catch (NoResultException nre) {
            ControllerUtils.sendSimpleResponseAsStream(response, HttpStatus.NOT_FOUND, 'Some request arguements could not be found: ' + nre.getMessage())
            return
        }

        DateTime startDateTimeInclusive = API_DATE_FORMAT.parseDateTime(cmd.timestampFrom)
        DateTime endDateTimeInclusive = API_DATE_FORMAT.parseDateTime(cmd.timestampTo)

        CsiByEventResultsDto csiDtoToReturn
        try {
            if (cmd.system && cmd.page) {
                performanceLoggingService.logExecutionTimeSilently(PerformanceLoggingService.LogLevel.DEBUG, "calculate page csi", 1) {
                    csiDtoToReturn = csiByEventResultsService.retrieveCsi(
                            startDateTimeInclusive,
                            endDateTimeInclusive,
                            queryParams,
                            [WeightFactor.BROWSER_CONNECTIVITY_COMBINATION] as Set
                    )
                }
            } else if (cmd.system) {
                performanceLoggingService.logExecutionTimeSilently(PerformanceLoggingService.LogLevel.DEBUG, "calculate shop csi", 1) {
                    csiDtoToReturn = csiByEventResultsService.retrieveCsi(
                            startDateTimeInclusive,
                            endDateTimeInclusive,
                            queryParams,
                            [WeightFactor.PAGE, WeightFactor.BROWSER_CONNECTIVITY_COMBINATION] as Set
                    )
                }
            }
        } catch (IllegalArgumentException e) {
            ControllerUtils.sendSimpleResponseAsStream(response, HttpStatus.NOT_FOUND, e.getMessage())
            return
        }
        log.debug("performance logdata getResults:\n" + performanceLoggingService.getExecutionTimeLoggingSessionData(PerformanceLoggingService.LogLevel.DEBUG))
        return ControllerUtils.sendObjectAsJSON(response, [target: csiDtoToReturn], params.pretty && params.pretty == 'true')

    }

    private boolean validateCsiParams(ResultsRequestCommand cmd){

        DateTime startDateTimeInclusive = API_DATE_FORMAT.parseDateTime(cmd.timestampFrom)
        DateTime endDateTimeInclusive = API_DATE_FORMAT.parseDateTime(cmd.timestampTo)

        performanceLoggingService.resetExecutionTimeLoggingSession()
        performanceLoggingService.logExecutionTimeSilently(PerformanceLoggingService.LogLevel.DEBUG, "cmd validation", 1) {

            if (endDateTimeInclusive.isBefore(startDateTimeInclusive)) {
                ControllerUtils.sendSimpleResponseAsStream(response, HttpStatus.BAD_REQUEST, 'The end of requested time-frame could not be before start of time-frame.')
                return false
            }

            Duration requestedDuration = new Duration(startDateTimeInclusive, endDateTimeInclusive);

            if (requestedDuration.getStandardDays() > MAX_TIME_FRAME_DURATION_IN_DAYS_CSI) {
                ControllerUtils.sendSimpleResponseAsStream(
                        response,
                        HttpStatus.BAD_REQUEST,
                        "The requested time-frame is wider than ${MAX_TIME_FRAME_DURATION_IN_DAYS_CSI} days. " +
                                "This is too large to process. Please choose a smaler time-frame."
                )
                return false
            }

            JobGroup csiSystem = JobGroup.findByName(cmd.system)
            if (csiSystem?.csiConfiguration == null) {
                ControllerUtils.sendSimpleResponseAsStream(response, HttpStatus.NOT_FOUND, "The JobGroup ${csiSystem} has no csi configuration.")
                return false
            }

        }

        return true
    }
}

/**
 * Parameters of rest api functions
 * /rest/csi/translateToCustomerSatisfaction and
 * /rest/$system/csi/translateToCustomerSatisfaction
 */
class TranslateCustomerSatisfactionCommand {
    String csiConfiguration
    Integer loadTimeInMillisecs
    String pageName
    String system

    static constraints = {
        csiConfiguration(nullable: true)
        system(validator: { val, obj ->
            obj.csiConfiguration || val
        })
    }

    public findCsiConfiguration() {
        if (csiConfiguration) {
            return CsiConfiguration.findByLabel(csiConfiguration)
        } else {
            JobGroup jobGroup = JobGroup.findByName(system)
            return jobGroup ? jobGroup.csiConfiguration : null
        }
    }
}

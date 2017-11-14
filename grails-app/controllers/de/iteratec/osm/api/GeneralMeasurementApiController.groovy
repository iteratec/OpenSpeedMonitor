package de.iteratec.osm.api

import de.iteratec.osm.InMemoryConfigService
import de.iteratec.osm.api.dto.*
import de.iteratec.osm.csi.Page
import de.iteratec.osm.de.iteratec.osm.api.CreateEventCommand
import de.iteratec.osm.de.iteratec.osm.api.MeasurementActivationCommand
import de.iteratec.osm.de.iteratec.osm.api.NightlyDatabaseCleanupActivationCommand
import de.iteratec.osm.measurement.environment.Browser
import de.iteratec.osm.measurement.environment.BrowserService
import de.iteratec.osm.measurement.environment.Location
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.measurement.schedule.dao.JobGroupDaoService
import de.iteratec.osm.report.chart.Event
import de.iteratec.osm.report.chart.EventDaoService
import de.iteratec.osm.result.MeasuredEvent
import de.iteratec.osm.util.ControllerUtils
import io.swagger.annotations.*
import org.springframework.http.HttpStatus

@Api(value = "/rest", tags = ["Generally measurement concerned"], description = "Generally measurement concerned Api", position = 2)
class GeneralMeasurementApiController {

    JobGroupDaoService jobGroupDaoService
    BrowserService browserService
    EventDaoService eventDaoService
    InMemoryConfigService inMemoryConfigService

    @ApiOperation(
        value = "Gets all systems (=JobGroups).",
        nickname = "allSystems",
        produces = "application/json",
        httpMethod = "GET",
        response = JobGroupDto.class,
        responseContainer = "List"
    )
    public Map<String, Object> allSystems() {
        Set<JobGroup> systems = jobGroupDaoService.findCSIGroups();
        Set<JobGroupDto> systemsAsJson = JobGroupDto.create(systems)
        return ControllerUtils.sendObjectAsJSON(response, [target: systemsAsJson], params.pretty && params.pretty == 'true')
    }

    @ApiOperation(
            value = "Gets all Measured Steps (=MeasuredEvents).",
            nickname = "allSteps",
            produces = "application/json",
            httpMethod = "GET",
            response = MeasuredEventDto.class,
            responseContainer = "List"
    )
    public Map<String, Object> allSteps() {
        Set<MeasuredEventDto> eventsAsJson = MeasuredEventDto.create(MeasuredEvent.list())
        return ControllerUtils.sendObjectAsJSON(response, [target: eventsAsJson], params.pretty && params.pretty == 'true')
    }

    @ApiOperation(
            value = "Gets all Browsers.",
            nickname = "allBrowsers",
            produces = "application/json",
            httpMethod = "GET",
            response = BrowserDto.class,
            responseContainer = "List"
    )
    public Map<String, Object> allBrowsers() {
        Set<Browser> browsers = browserService.findAll()
        Set<BrowserDto> browsersAsJson = BrowserDto.create(browsers)
        return ControllerUtils.sendObjectAsJSON(response, [target: browsersAsJson], params.pretty && params.pretty == 'true')
    }

    @ApiOperation(
            value = "Gets all Pages.",
            nickname = "allPages",
            produces = "application/json",
            httpMethod = "GET",
            response = PageDto.class,
            responseContainer = "List"
    )
    public Map<String, Object> allPages() {
        Set<Page> pages = Page.list()
        Set<PageDto> pagesAsJson = PageDto.create(pages)
        return ControllerUtils.sendObjectAsJSON(response, [target: pagesAsJson], params.pretty && params.pretty == 'true')
    }

    @ApiOperation(
            value = "Gets all Locations.",
            nickname = "allLocations",
            produces = "application/json",
            httpMethod = "GET",
            response = LocationDto.class,
            responseContainer = "List"
    )
    @ApiImplicitParams([
            @ApiImplicitParam(
                    name = "showInactive",
                    paramType = "query",
                    required = false,
                    value = "Whether to include inactive Locations in response.",
                    dataType = "boolean")
    ])
    public Map<String, Object> allLocations() {
        Collection<Location> locations = Location.list()
        if(params.showInactive != 'true') {
            locations = locations.findAll {
                it.active && it.wptServer.active
            }
        }
        Set<LocationDto> locationsAsJson = LocationDto.create(locations)
        return ControllerUtils.sendObjectAsJSON(response, [target: locationsAsJson], params.pretty && params.pretty == 'true')
    }

    /**
     * Creates Event with supplied parameters.
     * This function can't be called without a valid apiKey as parameter.
     * @see de.iteratec.osm.filters.SecuredApiFunctionsFilters
     */
    @ApiOperation(
            value = "Creates a new Event. Events are shown as marks in charts of OpenSpeedMonitor. They can be used for various use cases, e.g. own software or browser updates.",
            nickname = "event/create",
            produces = "application/json",
            httpMethod = "POST",
            response = Event,
            authorizations = @Authorization(value = "apiKey")
    )
    @ApiResponses([
            @ApiResponse(
                    code = 405,
                    message = "Method Not Allowed. Only POST is allowed"
            ),
            @ApiResponse(
                    code = 403,
                    message = "Access denied! A valid API-Key with sufficient access rights is required!"
            ),
            @ApiResponse(
                    code = 400,
                    message = "Some of the query params caused an error."
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
                    name = "shortName",
                    paramType = "query",
                    required = true,
                    value = "Short name of event to be created. This is shown on hover popup of dasboards.",
                    dataType = "string"
            ),
            @ApiImplicitParam(
                    name = "description",
                    paramType = "query",
                    required = false,
                    value = "More detailed description of event to be created. This is shown on hover popup of dasboards.",
                    dataType = "string"
            ),
            @ApiImplicitParam(
                    name = "system",
                    paramType = "query",
                    required = true,
                    value = "Name of the measurement system (=JobGroup) the created event should be associated to. Can be more than one system. Created events are shown only in dashboard views for respective system (except events with global visibility, see respective parameter).",
                    dataType = "string",
                    allowMultiple = true
            ),
            @ApiImplicitParam(
                    name = "globallyVisible",
                    paramType = "query",
                    required = false,
                    value = "Normally events are only shown in dashboard views for associated systems. Globally visible events are shown on all dashboards.",
                    dataType = "boolean",
                    allowableValues = '[true, false]'
            ),
            @ApiImplicitParam(
                    name = "eventTimestamp",
                    paramType = "query",
                    required = false,
                    value = "The timestamp the event occurred in ISO 8601 format, e.g. 20140101T230000Z for 1st of January 2014, 11 PM (UTC). If omitted the timestamp this api function is called is used as timestamp for the event.",
                    dataType = "string"
            )
    ])
    public Map<String, Object> securedViaApiKeyCreateEvent(CreateEventCommand cmd) {

        if (!cmd.validate()) {
            StringWriter sw = new StringWriter()
            cmd.errors.getFieldErrors().each { fieldError ->
                sw << "Error field ${fieldError.getField()}: ${fieldError.getCode()}\n"
            }
            return ControllerUtils.sendSimpleResponseAsStream(response, HttpStatus.BAD_REQUEST, sw.toString())
        }
        Event event = eventDaoService.createEvent(
                cmd.shortName,
                cmd.getEventTimeStampAsDateTime(),
                cmd.description ?: '',
                cmd.globallyVisible ?: false,
                cmd.getJobGroups()
        )
        return ControllerUtils.sendObjectAsJSON(response, event, params.pretty && params.pretty == 'true')
    }

    @ApiOperation(
            value = "Activates measurements globally.",
            nickname = "config/activateMeasurementsGenerally",
            produces = "application/json",
            httpMethod = "PUT",
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
                    code = 400,
                    message = "Some of the query params caused an error."
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
    public Map<String, Object> securedViaApiKeyActivateMeasurement(MeasurementActivationCommand cmd) {
        setMeasurementActivation(true, cmd)
    }
    @ApiOperation(
            value = "Dectivates measurements globally.",
            nickname = "config/activateMeasurementsGenerally",
            produces = "application/json",
            httpMethod = "PUT",
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
                    code = 400,
                    message = "Some of the query params caused an error."
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
    public Map<String, Object> securedViaApiKeyDeactivateMeasurement(MeasurementActivationCommand cmd) {
        setMeasurementActivation(false, cmd)
    }
    private Map<String, Object> setMeasurementActivation(boolean activate, MeasurementActivationCommand cmd) {
        if (!cmd.validate()) {
            StringWriter sw = new StringWriter()
            cmd.errors.getFieldErrors().each { fieldError ->
                sw << "Error field ${fieldError.getField()}: ${fieldError.getCode()}\n"
            }
            return ControllerUtils.sendSimpleResponseAsStream(response, HttpStatus.BAD_REQUEST, sw.toString())
        }
        inMemoryConfigService.setActiveStatusOfMeasurementsGenerally(activate)
        return ControllerUtils.sendSimpleResponseAsStream(response, HttpStatus.OK, "Set measurements activation to: ${activate}")
    }

    @ApiOperation(
            value = "Activates nightly DB cleanup.",
            nickname = "config/activateNightlyDatabaseCleanup",
            produces = "application/json",
            httpMethod = "PUT",
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
                    code = 400,
                    message = "Some of the query params caused an error."
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
    public Map<String, Object> securedViaApiKeyActivateNightlyCleanup(NightlyDatabaseCleanupActivationCommand cmd) {
        setNightlyDatabaseCleanupActivation(true, cmd)
    }
    @ApiOperation(
            value = "Deactivates nightly DB cleanup.",
            nickname = "config/activateNightlyDatabaseCleanup",
            produces = "application/json",
            httpMethod = "PUT",
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
                    code = 400,
                    message = "Some of the query params caused an error."
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
    public Map<String, Object> securedViaApiKeyDeactivateNightlyCleanup(NightlyDatabaseCleanupActivationCommand cmd) {
        setNightlyDatabaseCleanupActivation(false, cmd)
    }
    public Map<String, Object> setNightlyDatabaseCleanupActivation(boolean activate, NightlyDatabaseCleanupActivationCommand cmd) {
        if (!cmd.validate()) {
            StringWriter sw = new StringWriter()
            cmd.errors.getFieldErrors().each { fieldError ->
                sw << "Error field ${fieldError.getField()}: ${fieldError.getCode()}\n"
            }
            return ControllerUtils.sendSimpleResponseAsStream(response, HttpStatus.BAD_REQUEST, sw.toString())
        }
        inMemoryConfigService.setDatabaseCleanupEnabled(activate)
        return ControllerUtils.sendSimpleResponseAsStream(response, HttpStatus.OK, "Set nightly-database-cleanup activation to: ${activate}")
    }
}

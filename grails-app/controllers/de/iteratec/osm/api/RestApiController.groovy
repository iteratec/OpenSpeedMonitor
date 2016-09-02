/*
* OpenSpeedMonitor (OSM)
* Copyright 2014 iteratec GmbH
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* 	http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package de.iteratec.osm.api

import de.iteratec.osm.InMemoryConfigService
import de.iteratec.osm.api.dto.*
import de.iteratec.osm.csi.CsiConfiguration
import de.iteratec.osm.csi.Page
import de.iteratec.osm.csi.transformation.TimeToCsMappingService
import de.iteratec.osm.csi.weighting.WeightFactor
import de.iteratec.osm.measurement.environment.Browser
import de.iteratec.osm.measurement.environment.Location
import de.iteratec.osm.measurement.environment.dao.BrowserDaoService
import de.iteratec.osm.measurement.environment.dao.LocationDaoService
import de.iteratec.osm.measurement.schedule.Job
import de.iteratec.osm.measurement.schedule.JobDaoService
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.measurement.schedule.JobProcessingService
import de.iteratec.osm.measurement.schedule.JobService
import de.iteratec.osm.measurement.schedule.dao.JobGroupDaoService
import de.iteratec.osm.measurement.schedule.dao.PageDaoService
import de.iteratec.osm.report.chart.EventDaoService
import de.iteratec.osm.result.CachedView
import de.iteratec.osm.result.EventResult
import de.iteratec.osm.result.MeasuredEvent
import de.iteratec.osm.result.MvQueryParams
import de.iteratec.osm.result.dao.EventResultDaoService
import de.iteratec.osm.result.dao.MeasuredEventDaoService
import de.iteratec.osm.util.PerformanceLoggingService
import de.iteratec.osm.util.PerformanceLoggingService.IndentationDepth
import de.iteratec.osm.util.PerformanceLoggingService.LogLevel
import grails.converters.JSON
import grails.databinding.BindUsing
import grails.web.mapping.LinkGenerator
import groovy.json.JsonSlurper
import org.joda.time.DateTime
import org.joda.time.Duration
import org.joda.time.format.DateTimeFormatter
import org.joda.time.format.ISODateTimeFormat
import org.quartz.CronExpression

import javax.persistence.NoResultException

/**
 * RestApiController
 * <p>
 * Contains all the actions to handle requests of osm RESTful API.
 *
 * Note: If converted domain objects get returned as JSON representations some attributes get excluded
 * in Bootstrap method excludePropertiesInJsonRepresentationsofDomainObjects (see link below).
 * </p>
 *
 * @see BootStrap#excludePropertiesInJsonRepresentationsofDomainObjects
 */
class RestApiController {

    public static final DateTimeFormatter API_DATE_FORMAT = ISODateTimeFormat.basicDateTimeNoMillis()
    public static final String DEFAULT_ACCESS_DENIED_MESSAGE = "Access denied! A valid API-Key with sufficient access rights is required!"

    JobGroupDaoService jobGroupDaoService;
    PageDaoService pageDaoService;
    MeasuredEventDaoService measuredEventDaoService;
    BrowserDaoService browserDaoService;
    LocationDaoService locationDaoService;
    CsiByEventResultsService csiByEventResultsService
    TimeToCsMappingService timeToCsMappingService
    LinkGenerator grailsLinkGenerator
    JobService jobService
    JobLinkService jobLinkService
    EventResultDaoService eventResultDaoService
    PerformanceLoggingService performanceLoggingService
    EventDaoService eventDaoService
    InMemoryConfigService inMemoryConfigService
    JobProcessingService jobProcessingService
    JobDaoService jobDaoService

    /**
     * <p>
     * Performs a redirect with HTTP status code 303 (see other).
     * </p>
     *
     * <p>
     * COPIED FROM CSI DASHBOARD - SHOULD BE EXTRACTED TO A UTIL!
     * </p>
     *
     * <p>
     * Using this redirect enforces the client to perform the next request
     * with the HTTP method GET.
     * This method SHOULD be used in a redirect-after-post situation.
     * </p>
     *
     * <p>
     * After using this method, the response should be considered to be
     * committed and should not be written to.
     * </p>
     *
     * @param actionNameToRedirectTo The Name of the action to redirect to;
     *        not <code>null</code>.
     *
     * @see <a href="http://tools.ietf.org/html/rfc2616#section-10.3.4"
     *      >http://tools.ietf.org/html/rfc2616#section-10.3.4</a>
     */
    private void redirectWith303(String actionNameToRedirectTo) {
        // There is a missing feature to do this:
        // http://jira.grails.org/browse/GRAILS-8829

        // Workaround based on:
        // http://fillinginthegaps.wordpress.com/2008/12/26/grails-301-moved-permanently-redirect/
        String uri = grailsLinkGenerator.link(action: actionNameToRedirectTo)
        response.setStatus(303)
        response.setHeader("Location", uri)
        render(status: 303)
    }

    /**
     * Redirects to {@link #getResultsDocumentation()}.
     *
     * @return Nothing , redirects immediately.
     */
    Map<String, Object> index() {
        redirectWith303('man')
    }

    /**
     * <p>
     * Renders an API documentation HTML for
     * {@link #getResults(ResultsRequestCommand)}.
     * </p>
     *
     * @since IT-81
     */
    public Map<String, Object> man() {
        return [protectedFunctionHint: 'THIS METHOD IS PROTECTED: You need a valid api key, ask your osm administrator for one.']
    }

    /**
     * <p>
     * Returns a set of all Systems (CSI groups) as JSON.
     * </p>
     *
     * @return Nothing , a JSON is sent before.
     * @see JobGroup
     */
    public Map<String, Object> allSystems() {
        Set<JobGroup> systems = jobGroupDaoService.findCSIGroups();
        Set<JobGroupDto> systemsAsJson = JobGroupDto.create(systems)
        return sendObjectAsJSON(systemsAsJson, params.pretty && params.pretty == 'true')
    }

    /**
     * <p>
     * Returns a set of all steps (measured events) as JSON.
     * </p>
     *
     * @return Nothing , a JSON is sent before.
     * @see MeasuredEvent
     */
    public Map<String, Object> allSteps() {
        Set<MeasuredEvent> events = measuredEventDaoService.findAll();
        Set<MeasuredEventDto> eventsAsJson = MeasuredEventDto.create(events)
        return sendObjectAsJSON(eventsAsJson, params.pretty && params.pretty == 'true');
    }

    /**
     * <p>
     * Returns a set of all browser as JSON.
     * </p>
     *
     * @return Nothing , a JSON is sent before.
     * @see Browser
     */
    public Map<String, Object> allBrowsers() {
        Set<Browser> browsers = browserDaoService.findAll();
        Set<BrowserDto> browsersAsJson = BrowserDto.create(browsers)
        return sendObjectAsJSON(browsersAsJson, params.pretty && params.pretty == 'true');
    }

    /**
     * <p>
     * Returns a set of all pages as JSON.
     * </p>
     *
     * @return Nothing , a JSON is sent before.
     * @see Page
     */
    public Map<String, Object> allPages() {
        Set<Page> pages = pageDaoService.findAll();
        Set<PageDto> pagesAsJson = PageDto.create(pages)
        return sendObjectAsJSON(pagesAsJson, params.pretty && params.pretty == 'true');
    }

    /**
     * <p>
     * Returns a set of all locations as JSON.
     * </p>
     *
     * @return Nothing , a JSON is sent before.
     * @see Location
     */
    public Map<String, Object> allLocations() {
        Collection<Location> locations = locationDaoService.findAll()
        Set<LocationDto> locationsAsJson = LocationDto.create(locations)
        return sendObjectAsJSON(locationsAsJson, params.pretty && params.pretty == 'true');
    }

    /**
     * The maximum duration of time-frame sent to {@link
     * # getResults ( ResultsRequestCommand )} in hours.
     */
    private static int MAX_TIME_FRAME_DURATION_IN_HOURS = 48;

    /**
     * The maximum duration of time-frame sent to {@link
     * # getEventResultBasedCsi ( ResultsRequestCommand )} in days.
     */
    private static int MAX_TIME_FRAME_DURATION_IN_DAYS_CSI = 8;

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
    public Map<String, Object> getResults(ResultsRequestCommand cmd) {

        DateTime startDateTimeInclusive = API_DATE_FORMAT.parseDateTime(cmd.timestampFrom);
        DateTime endDateTimeInclusive = API_DATE_FORMAT.parseDateTime(cmd.timestampTo);
        if (endDateTimeInclusive.isBefore(startDateTimeInclusive)) {
            sendSimpleResponseAsStream(response, 400, 'End of requested time-frame may not be earlier that its requested start.')
            return
        }

        Duration requestedDuration = new Duration(startDateTimeInclusive, endDateTimeInclusive);

        if (requestedDuration.getStandardHours() > MAX_TIME_FRAME_DURATION_IN_HOURS) {
            String errorMessage = 'The requested time-frame is longer than ' +
                    MAX_TIME_FRAME_DURATION_IN_HOURS +
                    ' hours. This is too large to process. Please choose a smaller time-frame.'
            sendSimpleResponseAsStream(response, 413, errorMessage)
            return
        }

        Date startTimeInclusive = startDateTimeInclusive.toDate();
        Date endTimeInclusive = endDateTimeInclusive.toDate();

        MvQueryParams queryParams = null;
        try {
            queryParams = cmd.createMvQueryParams(jobGroupDaoService, pageDaoService, measuredEventDaoService, browserDaoService, locationDaoService);
        } catch (NoResultException nre) {
            sendSimpleResponseAsStream(response, 404, 'Some of the request arguments could not be found: ' + nre.getMessage())
            return
        }

        response.setContentType("application/json;charset=UTF-8");
        response.status = 200;

        List<EventResultDto> results = new LinkedList<EventResultDto>();

        performanceLoggingService.logExecutionTime(LogLevel.INFO, 'assembling results for json', IndentationDepth.ONE) {
            Collection<EventResult> eventResults = eventResultDaoService.getByStartAndEndTimeAndMvQueryParams(startTimeInclusive, endTimeInclusive, cmd.getCachedViewsToReturn(), queryParams)
            eventResults.each { eachEventResult ->
                results.add(new EventResultDto(eachEventResult));
            }
        }
        return sendObjectAsJSON(results, params.pretty && params.pretty == 'true');
    }

    public Map<String, Object> getEventResultBasedCsi(ResultsRequestCommand cmd) {

        DateTime startDateTimeInclusive = API_DATE_FORMAT.parseDateTime(cmd.timestampFrom);
        DateTime endDateTimeInclusive = API_DATE_FORMAT.parseDateTime(cmd.timestampTo);

        if (endDateTimeInclusive.isBefore(startDateTimeInclusive)) {
            response.setContentType('text/plain;charset=UTF-8');
            response.status = 400; // BAD REQUEST

            Writer textOut = new OutputStreamWriter(response.getOutputStream());
            textOut.write('The end of requested time-frame could not be before start of time-frame.');

            textOut.flush();
            response.getOutputStream().flush();
            return null;
        }

        Duration requestedDuration = new Duration(startDateTimeInclusive, endDateTimeInclusive);

        if (requestedDuration.getStandardDays() > MAX_TIME_FRAME_DURATION_IN_DAYS_CSI) {
            response.setContentType('text/plain;charset=UTF-8');
            response.status = 413; // Request Entity Too Large

            Writer textOut = new OutputStreamWriter(response.getOutputStream());
            textOut.write('The requested time-frame is wider than ' +
                    MAX_TIME_FRAME_DURATION_IN_DAYS_CSI +
                    ' days. This is too large to process. Please choose a smaler time-frame.');

            textOut.flush();
            response.getOutputStream().flush();
            return null;
        }

        MvQueryParams queryParams = null;
        try {
            queryParams = cmd.createMvQueryParams(jobGroupDaoService, pageDaoService, measuredEventDaoService, browserDaoService, locationDaoService);
        } catch (NoResultException nre) {
            response.setContentType('text/plain;charset=UTF-8');
            response.status = 404;
            // NOT FOUND (send Error does not work probably with Grails, it would render the default error page. So we use the deprecated setter.)

            Writer textOut = new OutputStreamWriter(response.getOutputStream());
            textOut.write('Some request arguements could not be found: ' + nre.getMessage());

            textOut.flush();
            response.getOutputStream().flush();
            return null;
        }

        CsiByEventResultsDto csiDtoToReturn
        try {
            if (cmd.system && cmd.page) {
                csiDtoToReturn = csiByEventResultsService.retrieveCsi(
                        startDateTimeInclusive,
                        endDateTimeInclusive,
                        queryParams,
                        [WeightFactor.BROWSER_CONNECTIVITY_COMBINATION] as Set)
            } else if (cmd.system) {
                csiDtoToReturn = csiByEventResultsService.retrieveCsi(
                        startDateTimeInclusive,
                        endDateTimeInclusive,
                        queryParams,
                        [WeightFactor.PAGE, WeightFactor.BROWSER_CONNECTIVITY_COMBINATION] as Set)
            }
        } catch (IllegalArgumentException e) {
            response.setContentType('text/plain;charset=UTF-8');
            response.status = 404;
            // NOT FOUND (send Error does not work probably with Grails, it would render the default error page. So we use the deprecated setter.)

            Writer textOut = new OutputStreamWriter(response.getOutputStream());
            textOut.write(e.getMessage())

            textOut.flush();
            response.getOutputStream().flush();
            return null;
        }

        return sendObjectAsJSON(csiDtoToReturn, params.pretty && params.pretty == 'true');

    }

    public Map<String, Object> getSystemPageCsi() {

    }

    /**
     * Calculates loadTimeInMillisecs to customer satisfaction. Assumption is that doc complete time is for {@link Page} with name pageName.
     * For the calculation most recent csi-mapping is used (see {@link CustomerFrustration}).
     * @param loadTimeInMillisecs
     * 	Doc complete time to translate.
     * @param pageName
     * 	Name of the page the doc complete time was measured for.
     * @param csiConfiguration
     * 	Label of the csiConfiguration to use
     * @return
     * The calculated customer satisfaction for the given doc complete time.
     */
    public Map<String, Object> translateToCustomerSatisfaction(TranslateCustomerSatisfactionCommand cmd) {

        if (cmd.loadTimeInMillisecs == null || cmd.pageName == null || (cmd.csiConfiguration == null && cmd.system == null)) {
            sendSimpleResponseAsStream(response, 400, 'Params loadTimeInMillisecs AND pageName AND csiConfiguration must be set.\n')
            return
        } else {
            Page page = Page.findByName(cmd.pageName)
            CsiConfiguration csiConfig = cmd.findCsiConfiguration()
            if (page == null) {
                sendSimpleResponseAsStream(response, 400, "Page with name ${cmd.pageName} couldn't be found.\n")
                return
            } else if (csiConfig == null) {
                sendSimpleResponseAsStream(response, 400, "CsiConfiguration couldn't be found\n")
                return
            } else {
                return sendObjectAsJSON(
                        ['loadTimeInMillisecs'          : cmd.loadTimeInMillisecs,
                         'customerSatisfactionInPercent': timeToCsMappingService.getCustomerSatisfactionInPercent(cmd.loadTimeInMillisecs, page, csiConfig)],
                        params.pretty && params.pretty == 'true'
                )
            }
        }
    }

    /**
     * Gets url's to visualize results of given {@link Job} in given period.
     * @param frequencyInMinutes
     * @param durationInMinutes
     * @return
     */
    public Map<String, Object> getResultUrls() {

        Job job = Job.get(params.id)
        if (job == null) {
            sendSimpleResponseAsStream(response, 404, "Job with id ${params.id} doesn't exist!")
            return
        }

        if (params.timestampFrom == null || params.timestampTo == null) {
            sendSimpleResponseAsStream(response, 400, 'Params timestampFrom and timestampTo must be set.')
            return
        }

        DateTime start = API_DATE_FORMAT.parseDateTime(params.timestampFrom)
        DateTime end = API_DATE_FORMAT.parseDateTime(params.timestampTo)
        if (end.isBefore(start)) {
            sendSimpleResponseAsStream(response, 400, 'The end of requested time-frame could not be before start of time-frame.')
            return
        }

        Map<String, String> visualizingLinks = jobLinkService.getResultVisualizingLinksFor(job, start, end)

        Map objectToSend =
                [
                        'job' : job.label,
                        'from': start,
                        'to'  : end
                ]
        return sendObjectAsJSON(
                objectToSend += visualizingLinks,
                params.pretty && params.pretty == 'true'
        )
    }

    /**
     * Gets csiConfiguration by id as JSON.
     * @param id
     * @return
     */
    public Map<String, Object> getCsiConfiguration() {

        CsiConfigurationDto jsonCsiConfiguration
        CsiConfiguration csiConfiguration = CsiConfiguration.get(params.id)
        if (csiConfiguration != null) {
            jsonCsiConfiguration = CsiConfigurationDto.create(csiConfiguration)
        } else {
            sendSimpleResponseAsStream(response, 400, "CsiConfiguration with id ${params.id} doesn't exist!")
            return
        }

        return sendObjectAsJSON(
                jsonCsiConfiguration,
                params.pretty && params.pretty == 'true'
        )
    }

    /**
     * Returns the names for given ids for each requested domain
     * @param mappingRequestCmd
     * @return
     */
    public Map<String, Object> getNamesForIds(MappingRequestCommand mappingRequestCmd) {
        Map<String, Map<Long, String>> resultMappings = [:].withDefault { [:] }

        mappingRequestCmd.requestedDomains.each { domain, idList ->
            switch (domain) {
                case "JobGroup":
                    List<JobGroup> jobGroups = JobGroup.findAllByIdInList(idList)
                    resultMappings[domain] = [:]
                    jobGroups.each {
                        resultMappings[domain].put(it.id, it.name)
                    }
                    break;
                case "Job":
                    List<Job> jobs = jobDaoService.getJobsByIds(idList)
                    resultMappings[domain] = [:]
                    jobs.each {
                        resultMappings[domain].put(it.id, it.label)
                    }
                    break;
                default:
                    sendSimpleResponseAsStream(response, 400, "Request not allowed or domain does not exist: ${domain}")
                    return null
            }
        }

        return sendObjectAsJSON(resultMappings as HashMap, params.pretty && params.pretty == 'true')
    }

    /**
     * Returns the ids for given names for each requested domain
     * @param mappingRequestCmd
     * @return
     */
    public Map<String, Object> getIdsForNames(MappingRequestCommand mappingRequestCmd) {
        Map<String, Map<Long, String>> resultMappings = [:].withDefault { [:] }

        mappingRequestCmd.requestedDomains.each { domain, names ->
            switch (domain) {
                case "Browser":
                    List<Browser> browsers = Browser.findAllByNameInList(names)
                    resultMappings[domain] = [:]
                    browsers.each {
                        resultMappings[domain].put(it.id, it.name)
                    }
                    break;
                case "Location":
                    List<Location> locations = Location.findAllByUniqueIdentifierForServerInList(names)
                    resultMappings[domain] = [:]
                    locations.each {
                        resultMappings[domain].put(it.id, it.uniqueIdentifierForServer)
                    }
                    break;
                case "Page":
                    List<Page> pages = Page.findAllByNameInList(names)
                    resultMappings[domain] = [:]
                    pages.each {
                        resultMappings[domain].put(it.id, it.name)
                    }
                    break;
                case "MeasuredEvent":
                    List<MeasuredEvent> measuredEvents = MeasuredEvent.findAllByNameInList(names)
                    resultMappings[domain] = [:]
                    measuredEvents.each {
                        resultMappings[domain].put(it.id, it.name)
                    }
                    break;
                default:
                    sendSimpleResponseAsStream(response, 400, "Request not allowed or domain does not exist: ${domain}")
                    return null
            }
        }

        return sendObjectAsJSON(resultMappings as HashMap, params.pretty && params.pretty == 'true')
    }

    /**
     * Activates the job of submitted id. It gets activated no matter whether it was active/inactive before.
     * This function can't be called without a valid apiKey as parameter.
     * @see de.iteratec.osm.filters.SecuredApiFunctionsFilters
     */
    public Map<String, Object> securedViaApiKeyActivateJob() {

        if (!params.validApiKey.allowedForJobActivation) {
            sendSimpleResponseAsStream(response, 403, DEFAULT_ACCESS_DENIED_MESSAGE)
            return
        }

        Job job = Job.get(params.id)
        if (job == null) {
            sendSimpleResponseAsStream(response, 404, "Job with id ${params.id} doesn't exist!")
            return
        }

        jobService.updateActivity(job, true)

        sendObjectAsJSON(
                JobDto.create(job.refresh()),
                params.pretty && params.pretty == 'true'
        )
    }
    /**
     * Handles pending/running jobResults. Old jobResults get deleted - fresh jobResults get rescheduled
     * This function can't be called without a valid apiKey as parameter.
     * @see de.iteratec.osm.filters.SecuredApiFunctionsFilters
     */
    public Map<String, Object> securedViaApiKeyHandleOldJobResults() {

        if (!params.validApiKey.allowedForMeasurementActivation) {
            sendSimpleResponseAsStream(response, 403, DEFAULT_ACCESS_DENIED_MESSAGE)
            return
        }
        def handleOldJobResultsReturnValueMap = jobProcessingService.handleOldJobResults()
        sendSimpleResponseAsStream(response, 200, "Deleted ${handleOldJobResultsReturnValueMap["JobResultsToDeleteCount"]} JobResults and rescheduled ${handleOldJobResultsReturnValueMap["JobResultsToRescheduleCount"]} JobResults")
    }

    /**
     * Deactivates the job of submitted id. It gets deactivated no matter whether it was active/inactive before.
     * This function can't be called without a valid apiKey as parameter.
     * @see de.iteratec.osm.filters.SecuredApiFunctionsFilters
     */
    public Map<String, Object> securedViaApiKeyDeactivateJob() {

        if (!params.validApiKey.allowedForJobDeactivation) {
            sendSimpleResponseAsStream(response, 403, DEFAULT_ACCESS_DENIED_MESSAGE)
            return
        }

        Job job = Job.get(params.id)
        if (job == null) {
            sendSimpleResponseAsStream(response, 404, "Job with id ${params.id} doesn't exist!")
            return
        }

        jobService.updateActivity(job, false)

        sendObjectAsJSON(
                JobDto.create(job.refresh()),
                params.pretty && params.pretty == 'true'
        )
    }

    /**
     * Updates execution schedule of Job with submitted id. Schedule must be a valid quartz schedule.
     * This function can't be called without a valid apiKey as parameter.
     * @see de.iteratec.osm.filters.SecuredApiFunctionsFilters
     * @see http://www.quartz-scheduler.org/documentation/quartz-1.x/tutorials/crontrigger
     */
    public Map<String, Object> securedViaApiKeySetExecutionSchedule() {

        if (!params.validApiKey.allowedForJobSetExecutionSchedule) {
            sendSimpleResponseAsStream(response, 403, DEFAULT_ACCESS_DENIED_MESSAGE)
            return
        }

        Job job = Job.get(params.id)
        if (job == null) {
            sendSimpleResponseAsStream(response, 404, "Job with id ${params.id} doesn't exist!")
            return
        }

        JsonSlurper jsonSlurper = new JsonSlurper().parseText(request.getJSON().toString())
        String schedule = jsonSlurper.executionSchedule
        if (schedule == null) {
            sendSimpleResponseAsStream(response, 400, "The body of your PUT request (JSON object) must contain executionSchedule.")
            return
        }

        if (!CronExpression.isValidExpression(schedule)) {
            sendSimpleResponseAsStream(response, 400, "The execution schedule you submitted in the body is invalid! " +
                    "(see http://www.quartz-scheduler.org/documentation/quartz-1.x/tutorials/crontrigger for details).")
            return
        }

        jobService.updateExecutionSchedule(job, schedule)

        sendObjectAsJSON(
                JobDto.create(job.refresh()),
                params.pretty && params.pretty == 'true'
        )
    }

    /**
     * Creates Event with supplied parameters.
     * This function can't be called without a valid apiKey as parameter.
     * @see de.iteratec.osm.filters.SecuredApiFunctionsFilters
     */
    public Map<String, Object> securedViaApiKeyCreateEvent(CreateEventCommand cmd) {

        if (cmd.hasErrors()) {
            StringWriter sw = new StringWriter()
            cmd.errors.getFieldErrors().each { fieldError ->
                sw << "Error field ${fieldError.getField()}: ${fieldError.getCode()}\n"
            }
            sendSimpleResponseAsStream(response, 400, sw.toString())
            return
        } else {
            render eventDaoService.createEvent(
                    cmd.shortName,
                    cmd.getEventTimeStampAsDateTime(),
                    cmd.description ?: '',
                    cmd.globallyVisible ?: false,
                    cmd.getJobGroups()) as JSON
        }

    }

    /**
     * <p>
     *     Activates or deactivates measurements generally respective param activationToSet.
     * </p>
     * @param cmd Binds parameters of requests.
     */
    public Map<String, Object> securedViaApiKeySetMeasurementActivation(MeasurementActivationCommand cmd) {
        if (cmd.hasErrors()) {
            StringWriter sw = new StringWriter()
            cmd.errors.getFieldErrors().each { fieldError ->
                sw << "Error field ${fieldError.getField()}: ${fieldError.getCode()}\n"
            }
            sendSimpleResponseAsStream(response, 400, sw.toString())
            return
        } else {
            inMemoryConfigService.setActiveStatusOfMeasurementsGenerally(cmd.activationToSet)
            sendSimpleResponseAsStream(response, 200, "Set measurements activation to: ${cmd.activationToSet}")
            return
        }
    }

    /**
     * <p>
     *     Activates or deactivates nightly cleanup.
     * </p>
     * @param cmd Binds parameters of requests.
     */
    public Map<String, Object> securedViaApiKeySetNightlyDatabaseCleanupActivation(NightlyDatabaseCleanupActivationCommand cmd) {
        if (cmd.hasErrors()) {
            StringWriter sw = new StringWriter()
            cmd.errors.getFieldErrors().each { fieldError ->
                sw << "Error field ${fieldError.getField()}: ${fieldError.getCode()}\n"
            }
            sendSimpleResponseAsStream(response, 400, sw.toString())
            return
        } else {
            inMemoryConfigService.setDatabaseCleanupEnabled(cmd.activationToSet)
            sendSimpleResponseAsStream(response, 200, "Set nightly-database-cleanup activation to: ${cmd.activationToSet}")
            return
        }
    }

    /**
     * <p>
     * Sends the object rendered as JSON. All public getters are used to
     * render the result. This call should be placed as last statement, the
     * return statement, of an action.
     * </p>
     *
     * @param objectToSend
     *         The object to render end to be sent to the client,
     *         not <code>null</code>.
     * @param usePrettyPrintingFormat
     *         Set to <code>true</code> if the JSON should be "pretty
     *         formated" (easy to read but larger file).
     * @return Always <code>null</code>.
     * @throws NullPointerException
     *         if {@code objectToSend} is <code>null</code>.
     */
    private void sendObjectAsJSON(Object objectToSend, boolean usePrettyPrintingFormat) {
        JSON converter = new JSON(target: objectToSend)
        converter.setPrettyPrint(usePrettyPrintingFormat)
        render converter
    }

    /**
     * Sends error message with given error httpStatus and message as http response and breaks action (no subsequent
     * action code is executed).
     * @param response
     * @param httpStatus
     * @param message
     */
    private void sendSimpleResponseAsStream(javax.servlet.http.HttpServletResponse response, Integer httpStatus, String message) {
        response.setContentType('text/plain;charset=UTF-8')
        response.status = httpStatus
        render message
    }

}

/**
 * <p>
 * A request to receive results via REST-API.
 * </p>
 *
 * @author mze
 * @since IT-81
 */
public class ResultsRequestCommand {

    /**
     * <p>
     * The start of the time-range for that results should be delivered;
     * this time-stamp is to be treated as inclusive. The format must
     * satisfy the format specified in ISO8601.
     * </p>
     *
     * <p>
     * Not <code>null</code>; not {@linkplain String#isEmpty() empty}.
     * </p>
     *
     * @see ISODateTimeFormat
     */
    String timestampFrom;

    /**
     * <p>
     * The end of the time-range for that results should be delivered;
     * this time-stamp is to be treated as inclusive. The format must
     * satisfy the format specified in ISO8601.
     * </p>
     *
     * <p>
     * Not <code>null</code>; not {@linkplain String#isEmpty() empty}.
     * </p>
     *
     * @see ISODateTimeFormat
     */
    String timestampTo;

    /**
     * <p>
     * The name of the system (CSI Group/Folder/Shop) for that results
     * should be delivered.
     * </p>
     *
     * <p>
     * Not <code>null</code>; not {@linkplain String#isEmpty() empty}.
     * </p>
     *
     * @see JobGroup
     * @see JobGroup#getName()
     */
    String system;

    /**
     * <p>
     * The page for that results should be delivered. If <code>null</code>
     * or {@linkplain String#isEmpty() empty} results for all pages will be
     * delivered.
     * </p>
     *
     * @see Page
     * @see Page#getName()
     */
    String page;

    /**
     * <p>
     * The step (measured event) for that results should be delivered.
     * If <code>null</code> or {@linkplain String#isEmpty() empty}
     * results for all steps will be delivered.
     * </p>
     *
     * @see MeasuredEvent
     * @see MeasuredEvent#getName()
     */
    String step;

    /**
     * <p>
     * The browser for that results should be delivered.
     * If <code>null</code> or {@linkplain String#isEmpty() empty}
     * results for all browser will be delivered.
     * </p>
     *
     * @see Browser
     * @see Browser#getName()
     */
    String browser;

    /**
     * <p>
     * The location for that results should be delivered.
     * If <code>null</code> or {@linkplain String#isEmpty() empty}
     * results for all locations will be delivered.
     * </p>
     *
     * @see Location
     * @see Location#getLocation()
     */
    String location;

    /**
     * Whether or not to pretty-print the json-response.
     */
    Boolean pretty

    /**
     * If this query parameter exist, only EventResults with this CachedView are returned.
     */
    CachedView cachedView

    /**
     * Constraints needs to fit.
     */
    static constraints = {
        timestampFrom(nullable: false, blank: false)
        timestampTo(nullable: false, blank: false)
        system(nullable: false, blank: false)
        page(nullable: true, blank: true)
        step(nullable: true, blank: true)
        browser(nullable: true, blank: true)
        location(nullable: true, blank: true)
        pretty(nullable: true, blank: true)
        cachedView(nullable: true, blank: true)
    }

    static transients = ['cachedViewsToReturn']

    /**
     * <p>
     * Creates {@link MvQueryParams} based on this command. This command
     * need to be valid for this operation to be successful.
     * </p>
     *
     * @return not <code>null</code>.
     * @throws IllegalStateException
     *         if called on an invalid instance.
     * @throws NoResultException
     *         if at least one of the specified parameters (system, page,
     *         event, location) could not be found.
     */
    public MvQueryParams createMvQueryParams(
            JobGroupDaoService jobGroupDaoService,
            PageDaoService pageDaoService,
            MeasuredEventDaoService measuredEventDaoService,
            BrowserDaoService browserDaoService,
            LocationDaoService locationDaoService) throws IllegalStateException, NoResultException {
        if (!this.validate()) {
            throw new IllegalStateException('Query params are not available from an invalid command.')
        }

        MvQueryParams result = new MvQueryParams();

        // system
        JobGroup theSystem = JobGroup.findByName(system)
        if (theSystem == null) {
            throw new NoResultException("Can not find CSI system named: " + system);
        } else if (theSystem.csiConfiguration == null) {
            throw new NoResultException("The JobGroup ${system} has no csi configuration")
        }
        result.jobGroupIds.add(theSystem.getId());

        // page
        if (page) {
            Page thePage = Page.findByName(page)
            if (thePage == null) {
                throw new NoResultException("Can not find Page named: " + page);
            }
            result.pageIds.add(thePage.getId());
        }

        if (step) {
            MeasuredEvent theStep = measuredEventDaoService.tryToFindByName(step);
            if (theStep == null) {
                throw new NoResultException("Can not find step named: " + step);
            }
            result.measuredEventIds.add(theStep.getId());
        }

        if (browser) {
            Browser theBrowser = browserDaoService.tryToFindByNameOrAlias(browser);
            if (theBrowser == null) {
                throw new NoResultException("Can not find browser named: " + browser);
            }
            result.browserIds.add(theBrowser.getId());
        }

        if (location) {
            List<Location> locations = Location.findAllByUniqueIdentifierForServer(location)
            if (locations.size() == 0) {
                throw new NoResultException("Can not find location with unique identifier \"" + location + "\"");
            }
            result.locationIds.addAll(locations*.ident())
        }

        return result;
    }

    /**
     * Returns cached views to return EventResults of. If no {@link CachedView} is provided as parameter
     * EventResults are not limited by CachedView.
     * @return
     */
    public Collection<CachedView> getCachedViewsToReturn() {
        return cachedView == null ? [CachedView.UNCACHED, CachedView.CACHED] : [cachedView]
    }
}

/**
 * Parameters of rest api function /rest/event/create.
 * Created by nkuhn on 08.05.15.
 */
class CreateEventCommand {
    String apiKey
    String shortName
    List<String> system = [].withLazyDefault { new String() }
    String eventTimestamp
    String description
    Boolean globallyVisible

    static transients = { ['eventTimeStampAsDateTime', 'jobGroups'] }

    /**
     * Constraints needs to fit.
     */
    static constraints = {
        apiKey(validator: { String currentKey, CreateEventCommand cmd ->
            ApiKey validApiKey = ApiKey.findBySecretKey(currentKey)
            if (!validApiKey.allowedForCreateEvent) return [RestApiController.DEFAULT_ACCESS_DENIED_MESSAGE]
            else return true
        })
        shortName(nullable: false, blank: false)
        system(validator: { List<String> currentSystems, CreateEventCommand cmd ->
            if (currentSystems.size() == 0) return ["You have to submit at least one system (technically: job group) for the event."]
            int invalidJobGroups = 0
            currentSystems.each { String system ->
                if (!JobGroup.findByName(system)) invalidJobGroups++
            }
            if (invalidJobGroups > 0) return ["At least one of the submitted systems doesn't exist."]
            else return true
        })
        eventTimestamp(nullable: true, validator: { String currentTimestamp, CreateEventCommand cmd ->
            if (currentTimestamp != null) {
                try {
                    RestApiController.API_DATE_FORMAT.parseDateTime(currentTimestamp)
                } catch (Exception e) {
                    return ["The date has the wrong format. Has to be in format ISO 8601. The 1st January 2014, 11 PM (UTC) in that format: 20140101T230000Z."]
                }
            }
            return true
        })
        description(nullable: true)
        globallyVisible(nullable: true)
    }

    public DateTime getEventTimeStampAsDateTime() {
        return eventTimestamp != null ? RestApiController.API_DATE_FORMAT.parseDateTime(eventTimestamp) : new DateTime();
    }

    public List<JobGroup> getJobGroups() {
        return system.collect { JobGroup.findByName(it) }
    }

}

/**
 * Parameters of rest api functions /rest/config/activateMeasurementsGenerally and
 * /rest/config/deactivateMeasurementsGenerally.
 * Created by nkuhn on 08.05.15.
 */
class MeasurementActivationCommand {

    String apiKey
    Boolean activationToSet

    static constraints = {
        activationToSet(nullable: false)
        apiKey(validator: { String currentKey, MeasurementActivationCommand cmd ->
            ApiKey validApiKey = ApiKey.findBySecretKey(currentKey)
            if (!validApiKey.allowedForMeasurementActivation) return [RestApiController.DEFAULT_ACCESS_DENIED_MESSAGE]
            else return true
        })
    }

}

/**
 * Parameters of rest api functions /rest/config/activateNightlyDatabaseCleanup and
 * /rest/config/deactivateNightlyCleanup.
 */
class NightlyDatabaseCleanupActivationCommand {

    String apiKey
    Boolean activationToSet

    static constraints = {
        activationToSet(nullable: false)
        apiKey(validator: { String currentKey, NightlyDatabaseCleanupActivationCommand cmd ->
            ApiKey validApiKey = ApiKey.findBySecretKey(currentKey)
            if (!validApiKey.allowedForNightlyDatabaseCleanupActivation) return [RestApiController.DEFAULT_ACCESS_DENIED_MESSAGE]
            else return true
        })
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

/**
 * The command object for the rest methods
 * /rest/domain/idsForNames
 * /rest/domain/namesForIds
 */
class MappingRequestCommand {
    @BindUsing({ obj, source ->
        if (source['requestedDomains'] instanceof HashMap) {
            obj = source['requestedDomains']
        } else {
            obj = JSON.parse(source['requestedDomains'])
        }
    })
    Map<String, List> requestedDomains = [:].withDefault { [] }
}
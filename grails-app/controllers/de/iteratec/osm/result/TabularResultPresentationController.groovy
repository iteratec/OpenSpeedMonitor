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

package de.iteratec.osm.result

import de.iteratec.osm.csi.Page
import de.iteratec.osm.measurement.environment.Browser
import de.iteratec.osm.measurement.environment.Location
import de.iteratec.osm.measurement.environment.dao.BrowserDaoService
import de.iteratec.osm.measurement.environment.dao.LocationDaoService
import de.iteratec.osm.measurement.schedule.Job
import de.iteratec.osm.measurement.schedule.dao.JobGroupDaoService
import de.iteratec.osm.measurement.schedule.dao.PageDaoService
import de.iteratec.osm.persistence.OsmDataSourceService
import de.iteratec.osm.report.ui.EventResultListing
import de.iteratec.osm.report.ui.EventResultListingRow
import de.iteratec.osm.report.ui.PaginationListing
import de.iteratec.osm.result.dao.EventResultDaoService
import de.iteratec.osm.result.dao.MeasuredEventDaoService
import de.iteratec.osm.util.ControllerUtils
import de.iteratec.osm.util.DateValueConverter
import de.iteratec.osm.util.PerformanceLoggingService
import grails.validation.Validateable

import java.text.SimpleDateFormat
import java.util.regex.Pattern

import org.joda.time.DateTime
import org.joda.time.Interval
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import org.springframework.beans.propertyeditors.CustomDateEditor

/**
 * <p>
 * UI controller for {@link de.iteratec.osm.result.EventResult} depending stuff.
 * </p>
 *
 * @author mze
 * @since IT-106
 */
class TabularResultPresentationController {

    private final static String DATE_FORMAT_STRING = 'dd.mm.yyyy'
    private final static int MONDAY_WEEKSTART = 1

    JobGroupDaoService jobGroupDaoService
    PageDaoService pageDaoService
    MeasuredEventDaoService measuredEventDaoService
    BrowserDaoService browserDaoService
    LocationDaoService locationDaoService
    PerformanceLoggingService performanceLoggingService

    OsmDataSourceService osmDataSourceService
    JobResultService jobResultService
    EventResultDaoService eventResultDaoService
    PaginationService paginationService
    //	LinkGenerator grailsLinkGenerator

    private Map<String, Object> listResultsByCommand(EventResultsCommandBase cmd) {

        Map<String, Object> modelToRender
        modelToRender = constructStaticViewDataOfListResults();
        cmd.copyRequestDataToViewModelMap(modelToRender);

        EventResultListing eventResultsListing = new EventResultListing();
        PaginationListing paginationListing = new PaginationListing();

        // Validate command for errors if there was a non-empty, non-"only-language-change" request:
        if( !ControllerUtils.isEmptyRequest(params) ) {
            if(!cmd.validate() )
            {
                modelToRender.put('command', cmd)
            } else {
                Interval timeFrame = cmd.getSelectedTimeFrame();
                Iterable<JobResult> jobResults = null
                Iterable<EventResult> eventResults = null
                Integer eventResultsTotalCount = null
                if (cmd instanceof ListResultsCommand) {
                    if(osmDataSourceService.getRLikeSupport()){
                        eventResultsTotalCount = eventResultDaoService.getCountedByStartAndEndTimeAndMvQueryParams(((ListResultsCommand)cmd).createMvQueryParams(), timeFrame.getStart().toDate(), timeFrame.getEnd().toDate(), cmd.getMax(), cmd.getOffset()).getTotalCount();
                        paginationListing = paginationService.buildListResultsPagination((ListResultsCommand)cmd, eventResultsTotalCount)
                    }
                    eventResults = eventResultDaoService.getCountedByStartAndEndTimeAndMvQueryParams(((ListResultsCommand)cmd).createMvQueryParams(), timeFrame.getStart().toDate(), timeFrame.getEnd().toDate(), cmd.getMax(), cmd.getOffset())
                } else if (cmd instanceof ListResultsForSpecificJobCommand) {
                    if(osmDataSourceService.getRLikeSupport()){
                        eventResultsTotalCount = eventResultDaoService.getEventResultsByJob(((ListResultsForSpecificJobCommand)cmd).job, timeFrame.getStart().toDate(), timeFrame.getEnd().toDate(), cmd.getMax(), cmd.getOffset()).getTotalCount();
                        paginationListing = paginationService.buildListResultsForJobPagination((ListResultsForSpecificJobCommand)cmd, eventResultsTotalCount)
                    }
                    eventResults = eventResultDaoService.getEventResultsByJob(((ListResultsForSpecificJobCommand)cmd).job, timeFrame.getStart().toDate(), timeFrame.getEnd().toDate(), cmd.getMax(), cmd.getOffset())
                }

                for(EventResult eachEventResult : eventResults)
                {
                    if(! eachEventResult.medianValue )
                    {
                        continue;
                    }
                    JobResult correspondingJobResult = eachEventResult.jobResult;
                    eventResultsListing.addRow(new EventResultListingRow(correspondingJobResult, eachEventResult))
//                    formerly: eventResultsListing.addRow(new EventResultListingRow(jobResultService.findJobResultByEventResult(eachEventResult), eachEventResult));
                }

                modelToRender.put('showEventResultsListing', true);
            }
        }
        modelToRender.put('eventResultsListing', eventResultsListing);
        modelToRender.put('paginationListing', paginationListing)
        return modelToRender;
    }

    /**
     * Thats the view used to list event results matching a previous selection
     * of date range, groups and more filter criteria. This page is intended
     * to be used by admins and developers.
     *
     * @param cmd The request / command send to this action,
     *            not <code>null</code>.
     * @return A model map to be used by the corresponding GSP,
     * 	       not <code>null</code> and never
     *         {@linkplain Map#isEmpty() empty}.
     */
    public Map<String, Object>listResults(ListResultsCommand cmd) {
        return listResultsByCommand(cmd)
    }

    /**
     * List event results matching selected date range for a specific job.
     *
     * @param cmd The request / command send to this action,
     *            not <code>null</code>.
     * @return A model map to be used by the corresponding GSP,
     * 	       not <code>null</code> and never
     *         {@linkplain Map#isEmpty() empty}.
     */
    public Map<String, Object> listResultsForJob(ListResultsForSpecificJobCommand cmd) {
        // default to last twelve hours if job but no date range has been specified
        if (cmd.job!=null && cmd.from==null && cmd.to==null) {

            // If this job's last run was more than twelve hours ago, show results from
            // one hour before last run until now
            DateTime now = new DateTime()
            int defaultTimeToShowResultsFrom = 12
            DateTime defaultFrom = now.minusHours(defaultTimeToShowResultsFrom)

            cmd.from = new DateTime(cmd.job.lastRun).isBefore(defaultFrom) ? new DateTime(cmd.job.lastRun).minusHours(defaultTimeToShowResultsFrom).toDate() : defaultFrom.toDate()
            cmd.fromHour = cmd.from.getAt(Calendar.HOUR_OF_DAY) +":"+ cmd.from.getAt(Calendar.MINUTE);

            cmd.to = new DateTime(cmd.job.lastRun).plusHours(defaultTimeToShowResultsFrom).toDate()
            cmd.toHour = cmd.to.getAt(Calendar.HOUR_OF_DAY) +":"+ cmd.to.getAt(Calendar.MINUTE);


            Interval timeFrame = cmd.getSelectedTimeFrame();

            SimpleDateFormat fmtDate = new SimpleDateFormat("dd.MM.yyyy");
            SimpleDateFormat fmtTime = new SimpleDateFormat("hh:mm");
            redirect(action: 'ShowListResultsForJob', params: [	'selectedTimeFrameInterval': '0',
                'job.id': cmd.job.getId(),
                'from': fmtDate.format(cmd.from),
                'fromHour': fmtTime.format(cmd.from),
                'to': fmtDate.format(cmd.to),
                'toHour': fmtTime.format(cmd.to)
            ])
        }
        Map<String, Object> modelToRender = listResultsByCommand(cmd)
        modelToRender.put('showSpecificJob', true)
        render(view: 'listResults', model: modelToRender)
    }

    /**
     * List event results matching generated date range (in listResultsForJob) for a specific job.
     * redirects to listResultsForJob if no Date param exists for Date calculation
     *
     * @param cmd The request / command send to this action,
     *            not <code>null</code>.
     * @return A model map to be used by the corresponding GSP,
     * 	       not <code>null</code> and never
     *         {@linkplain Map#isEmpty() empty}.
     */
    public Map<String, Object> ShowListResultsForJob(ListResultsForSpecificJobCommand cmd) {
        if (cmd.job!=null && cmd.from==null && cmd.to==null){
            redirect(action: 'listResultsForJob', params:['job.id': cmd.job.getId()])
        }
        Map<String, Object> modelToRender = listResultsByCommand(cmd)
        modelToRender.put('showSpecificJob', true)
        render(view: 'listResults', model: modelToRender)
    }

    @Validateable
    public static class EventResultsCommandBase {
        /**
         * The selected start date.
         *
         * Please use {@link #getSelectedTimeFrame()}.
         */
        Date from

        /**
         * The selected end date.
         *
         * Please use {@link #getSelectedTimeFrame()}.
         */
        Date to

        /**
         * The selected start hour of date.
         *
         * Please use {@link #getSelectedTimeFrame()}.
         */
        String fromHour

        /**
         * The selected end hour of date.
         *
         * Please use {@link #getSelectedTimeFrame()}.
         */
        String toHour

        /**
         * A predefined time frame.
         */
        int selectedTimeFrameInterval = 259200

        Integer max = 50

        Integer offset = 0

        /**
         * Constraints needs to fit.
         */
        static constraints = {
            from(nullable: true, validator: {Date currentFrom, EventResultsCommandBase cmd ->
                boolean manualTimeframe = cmd.selectedTimeFrameInterval == 0
                if(manualTimeframe && currentFrom == null) return ['de.iteratec.isr.TabularResultPresentationController$ListResultsCommand.from.nullWithManualSelection']
            })
            to(nullable:true, validator: { Date currentTo, EventResultsCommandBase cmd ->
                boolean manualTimeframe = cmd.selectedTimeFrameInterval == 0
                if(manualTimeframe && currentTo == null) return ['de.iteratec.isr.TabularResultPresentationController$ListResultsCommand.to.nullWithManualSelection']
                else if(manualTimeframe && currentTo != null && cmd.from != null && currentTo.before(cmd.from)) return ['de.iteratec.isr.TabularResultPresentationController$ListResultsCommand.to.beforeFromDate']
            })
            fromHour(nullable: true, validator: {String currentFromHour, EventResultsCommandBase cmd ->
                boolean manualTimeframe = cmd.selectedTimeFrameInterval == 0
                if(manualTimeframe && currentFromHour == null) return ['de.iteratec.isr.TabularResultPresentationController$ListResultsCommand.fromHour.nullWithManualSelection']
            })
            toHour(nullable: true, validator: {String currentToHour, EventResultsCommandBase cmd ->
                boolean manualTimeframe = cmd.selectedTimeFrameInterval == 0
                if(manualTimeframe && currentToHour == null) {
                    return ['de.iteratec.isr.TabularResultPresentationController$ListResultsCommand.toHour.nullWithManualSelection']
                }
                else if(manualTimeframe && cmd.from != null && cmd.to != null && cmd.from.equals(cmd.to) && cmd.fromHour != null && currentToHour != null) {
                    DateTime firstDayWithFromDaytime = getFirstDayWithTime(cmd.fromHour)
                    DateTime firstDayWithToDaytime = getFirstDayWithTime(currentToHour)
                    if(!firstDayWithToDaytime.isAfter(firstDayWithFromDaytime)) return ['de.iteratec.isr.TabularResultPresentationController$ListResultsCommand.toHour.inCombinationWithDateBeforeFrom']
                }
            })
            max(nullable:true)
            offset(nullable:true)
        }

        static transients = ['selectedTimeFrame']

        /**
         * <p>
         * Returns the selected time frame as {@link Interval}, which is the
         * result of the aggregation of {@link #from}, {@link #fromHour},
         * to {@link #to}, {@link #toHour}.
         * </p>
         *
         * @return not <code>null</code>; end is intended to be inclusive
         * @throws IllegalStateException
         *         if called on an invalid instance.
         */
        public Interval getSelectedTimeFrame() throws IllegalStateException
        {
            if( !this.validate() )
            {
                throw new IllegalStateException('A time frame is not available from an invalid command.')
            }

            DateTime start
            DateTime end

            Boolean manualTimeframe = this.selectedTimeFrameInterval == 0
            if (manualTimeframe && fromHour && toHour) {

                DateTime firstDayWithFromHourAsDaytime = getFirstDayWithTime(fromHour)
                DateTime firstDayWithToHourAsDaytime = getFirstDayWithTime(toHour)

                start = new DateTime(this.from.getTime())
                        .withTime(
                        firstDayWithFromHourAsDaytime.getHourOfDay(),
                        firstDayWithFromHourAsDaytime.getMinuteOfHour(),
                        0, 0
                        )
                end = new DateTime(this.to.getTime())
                        .withTime(
                        firstDayWithToHourAsDaytime.getHourOfDay(),
                        firstDayWithToHourAsDaytime.getMinuteOfHour(),
                        59, 999
                        )

            }else{

                end = new DateTime()
                start = end.minusSeconds(this.selectedTimeFrameInterval)

            }

            return new Interval(start, end);
        }

        /**
         * Returns a {@link DateTime} of the first day in unix-epoch with daytime respective param timeWithOrWithoutMeridian.
         * @param timeWithOrWithoutMeridian
         * 		The format can be with or without meridian (e.g. "04:45", "16:12" without or "02:00 AM", "11:23 PM" with meridian)
         * @return A {@link DateTime} of the first day in unix-epoch with daytime respective param timeWithOrWithoutMeridian.
         * @throws IllegalStateException If timeWithOrWithoutMeridian is in wrong format.
         */
        public static DateTime getFirstDayWithTime(String timeWithOrWithoutMeridian) throws IllegalStateException{

            Pattern regexWithMeridian = ~/\d{1,2}:\d\d [AP]M/
            Pattern regexWithoutMeridian = ~/\d{1,2}:\d\d/
            String dateFormatString

            if(timeWithOrWithoutMeridian ==~ regexWithMeridian) dateFormatString = "dd.MM.yyyy hh:mm"
            else if(timeWithOrWithoutMeridian ==~ regexWithoutMeridian) dateFormatString = "dd.MM.yyyy HH:mm"
            else throw new IllegalStateException("Wrong format of time: ${timeWithOrWithoutMeridian}")

            DateTimeFormatter fmt = DateTimeFormat.forPattern(dateFormatString)
            return fmt.parseDateTime("01.01.1970 ${timeWithOrWithoutMeridian}")

        }

        /**
         * <p>
         * Copies all request data to the specified map. This operation does
         * not care about the validation status of this instance.
         * For missing values the defaults are inserted.
         * </p>
         *
         * @param viewModelToCopyTo
         *         The {@link Map} the request data contained in this command
         *         object should be copied to. The map must be modifiable.
         *         Previously contained data will be overwritten.
         *         The argument might not be <code>null</code>.
         */
        public void copyRequestDataToViewModelMap(Map<String, Object> viewModelToCopyTo)
        {
            DateValueConverter converter = DateValueConverter.getConverter()

            viewModelToCopyTo.put('from', converter.convert(this.from))
            if(!this.fromHour.is(null)) {
                viewModelToCopyTo.put('fromHour',this.fromHour)
            }

            viewModelToCopyTo.put('to', converter.convert(this.to))
            if (!this.toHour.is(null)){
                viewModelToCopyTo.put('toHour', this.toHour)
            }
            viewModelToCopyTo.put('max', this.max)
            viewModelToCopyTo.put('offset', this.offset)
        }
    }

    @Validateable
    public static class ListResultsForSpecificJobCommand extends EventResultsCommandBase {
        Job job

        static constraints = { job(nullable: false) }

        @Override
        public void copyRequestDataToViewModelMap(Map<String, Object> viewModelToCopyTo)
        {
            super.copyRequestDataToViewModelMap(viewModelToCopyTo)
            viewModelToCopyTo.put('job', this.job)
        }
    }

    /**
     * <p>
     * Command of {@link TabularResultPresentationController#listResults(ListResultsCommand)}.
     * </p>
     *
     * <p>
     * None of the properties will be <code>null</code> for a valid instance.
     * </p>
     *
     * @author mze
     * @since IT-74
     */
    @Validateable
    public static class ListResultsCommand extends EventResultsCommandBase{
        /**
         * The database IDs of the selected {@linkplain JobGroup CSI groups}
         * which are the systems measured for a CSI value
         */
        Collection<Long> selectedFolder = []

        /**
         * The database IDs of the selected {@linkplain Page pages}
         * which results to be shown.
         *
         * TODO rename to selectedPages
         */
        Collection<Long> selectedPage = []

        /**
         * The database IDs of the selected {@linkplain de.iteratec.osm.result.MeasuredEvent
         * measured events} which results to be shown.
         *
         * These selections are only relevant if
         * {@link #selectedAllMeasuredEvents} is evaluated to
         * <code>false</code>.
         */
        Collection<Long> selectedMeasuredEventIds = []

        /**
         * User enforced the selection of all measured events.
         * This selection <em>is not</em> reflected in
         * {@link #selectedMeasuredEventIds} cause of URL length
         * restrictions. If this flag is evaluated to
         * <code>true</code>, the selections in
         * {@link #selectedMeasuredEventIds} should be ignored.
         */
        Boolean selectedAllMeasuredEvents

        /**
         * The database IDs of the selected {@linkplain Browser
         * browsers} which results to be shown.
         *
         * These selections are only relevant if
         * {@link #selectedAllBrowsers} is evaluated to
         * <code>false</code>.
         */
        Collection<Long> selectedBrowsers = []

        /**
         * User enforced the selection of all browsers.
         * This selection <em>is not</em> reflected in
         * {@link #selectedBrowsers} cause of URL length
         * restrictions. If this flag is evaluated to
         * <code>true</code>, the selections in
         * {@link #selectedBrowsers} should be ignored.
         */
        Boolean selectedAllBrowsers

        /**
         * The database IDs of the selected {@linkplain Location
         * locations} which results to be shown.
         *
         * These selections are only relevant if
         * {@link #selectedAllLocations} is evaluated to
         * <code>false</code>.
         */
        Collection<Long> selectedLocations = []

        /**
         * User enforced the selection of all locations.
         * This selection <em>is not</em> reflected in
         * {@link #selectedLocations} cause of URL length
         * restrictions. If this flag is evaluated to
         * <code>true</code>, the selections in
         * {@link #selectedLocations} should be ignored.
         */
        Boolean selectedAllLocations

        /**
         * Constraints needs to fit.
         */
        static constraints = {
            selectedFolder(nullable:false, minSize:1)

            // selectedPages is not allowed to be empty
            selectedPage(nullable:false, minSize:1)

            // selectedMeasuredEventIds is only allowed to be empty if selectedAllMeasuredEvents is true
            selectedMeasuredEventIds(nullable:false, validator: { Collection currentCollection, ListResultsCommand cmd ->
                return (cmd.selectedAllMeasuredEvents || (!currentCollection.isEmpty()))
            })

            // selectedBrowsers is only allowed to be empty if selectedAllBrowsers is true
            selectedBrowsers(nullable:false, validator: { Collection currentCollection, ListResultsCommand cmd ->
                return (cmd.selectedAllBrowsers || (!currentCollection.isEmpty()))
            })

            // selectedLocations is only allowed to be empty if selectedAllLocations is true
            selectedLocations(nullable:false, validator: { Collection currentCollection, ListResultsCommand cmd ->
                return (cmd.selectedAllLocations || (!currentCollection.isEmpty()))
            })
        }

        /**
         * <p>
         * Copies all request data to the specified map. This operation does
         * not care about the validation status of this instance.
         * For missing values the defaults are inserted.
         * </p>
         *
         * @param viewModelToCopyTo
         *         The {@link Map} the request data contained in this command
         *         object should be copied to. The map must be modifiable.
         *         Previously contained data will be overwritten.
         *         The argument might not be <code>null</code>.
         */
        @Override
        public void copyRequestDataToViewModelMap(Map<String, Object> viewModelToCopyTo)
        {
            viewModelToCopyTo.put('selectedFolder', this.selectedFolder)
            viewModelToCopyTo.put('selectedPages', this.selectedPage)

            viewModelToCopyTo.put('selectedAllMeasuredEvents', (this.selectedAllMeasuredEvents as boolean ? 'on' : ''))
            viewModelToCopyTo.put('selectedMeasuredEventIds', this.selectedMeasuredEventIds)

            viewModelToCopyTo.put('selectedAllBrowsers', (this.selectedAllBrowsers as boolean ? 'on' : ''))
            viewModelToCopyTo.put('selectedBrowsers', this.selectedBrowsers)

            viewModelToCopyTo.put('selectedAllLocations', (this.selectedAllLocations as boolean ? 'on' : ''))
            viewModelToCopyTo.put('selectedLocations', this.selectedLocations)

            super.copyRequestDataToViewModelMap(viewModelToCopyTo)
        }

        /**
         * <p>
         * Creates a query tag to find results matching the selections made
         * with this command.
         * </p>
         *
         * @param measuredValueTagService
         *         The {@link de.iteratec.osm.result.MeasuredValueTagService} to create the tag with.
         * @return Never <code>null</code>.
         */
        public Pattern createResultsQueryPattern(MeasuredValueTagService measuredValueTagService) {
            return measuredValueTagService.getTagPatternForHourlyMeasuredValues(createMvQueryParams());
        }

        /**
         * <p>
         * Creates {@link MvQueryParams} based on this command. This command
         * need to be valid for this operation to be successful.
         * </p>
         *
         * @return not <code>null</code>.
         * @throws IllegalStateException
         *         if called on an invalid instance.
         */
        private MvQueryParams createMvQueryParams() throws IllegalStateException
        {
            if( !this.validate() )
            {
                throw new IllegalStateException('Query params are not available from an invalid command.')
            }

            MvQueryParams result = new MvQueryParams();

            result.jobGroupIds.addAll(this.selectedFolder);

            if( !this.selectedAllMeasuredEvents )
            {
                result.measuredEventIds.addAll(this.selectedMeasuredEventIds);
            }

            result.pageIds.addAll(this.selectedPage);

            if( !this.selectedAllBrowsers )
            {
                result.browserIds.addAll(this.selectedBrowsers);
            }

            if( !this.selectedAllLocations )
            {
                result.locationIds.addAll(this.selectedLocations);
            }

            return result;
        }
    }

    /**
     * <p>
     * Constructs the static view data of the {@link #listResults(ListResultsCommand)}
     * view as {@link Map}.
     * </p>
     *
     * <p>
     * This map does always contain all available data for selections, previous
     * selections are not considered.
     * </p>
     *
     * @return A Map containing the static view data which are accessible
     *         through corresponding keys. The Map is modifiable to add
     *         further data. Subsequent calls will never return the same
     *         instance.
     */
    public Map<String, Object> constructStaticViewDataOfListResults()
    {
        Map<String, Object> result = [:]

        // JobGroups
        result.put('csiGroups', jobGroupDaoService.findAll().sort(false, { it.name }))

        // Pages
        List<Page> pages = pageDaoService.findAll().sort(false, { it.name });
        result.put('pages', pages)

        // MeasuredEvents
        List<MeasuredEvent> measuredEvents = measuredEventDaoService.findAll().sort(false, { it.name });
        result.put('measuredEvents', measuredEvents)

        // Browsers
        List<Browser> browsers = browserDaoService.findAll().sort(false, { it.name });
        result.put('browsers', browsers)

        // Locations
        List<Location> locations = locationDaoService.findAll().sort(false, { it.label });
        result.put('locations', locations)

        // JavaScript-Utility-Stuff:
        result.put("weekStart", MONDAY_WEEKSTART)

        // --- Map<PageID, Set<MeasuredEventID>> for fast view filtering:
        Map<Long, Set<Long>> eventsOfPages = new HashMap<Long, Set<Long>>()
        for(Page eachPage : pages)
        {
            Set<Long> eventIds = new HashSet<Long>();

            Collection<Long> ids = measuredEvents.findResults {
                it.testedPage.getId() == eachPage.getId() ? it.getId() : null }
            if( !ids.isEmpty() )
            {
                eventIds.addAll(ids);
            }

            eventsOfPages.put(eachPage.getId(), eventIds);
        }
        result.put('eventsOfPages', eventsOfPages);

        // --- Map<BrowserID, Set<LocationID>> for fast view filtering:
        Map<Long, Set<Long>> locationsOfBrowsers = new HashMap<Long, Set<Long>>()
        for(Browser eachBrowser : browsers)
        {
            Set<Long> locationIds = new HashSet<Long>();

            Collection<Long> ids = locations.findResults {
                it.browser.getId() == eachBrowser.getId() ? it.getId() : null }
            if( !ids.isEmpty() )
            {
                locationIds.addAll(ids);
            }

            locationsOfBrowsers.put(eachBrowser.getId(), locationIds);
        }
        result.put('locationsOfBrowsers', locationsOfBrowsers);

        // JavaScript-Utility-Stuff:
        result.put("dateFormat", DATE_FORMAT_STRING)
        result.put("weekStart", MONDAY_WEEKSTART)

        // Done! :)
        return result;
    }
}
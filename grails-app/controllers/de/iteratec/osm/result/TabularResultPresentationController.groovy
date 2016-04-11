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
import de.iteratec.osm.dao.CriteriaSorting
import de.iteratec.osm.measurement.environment.Browser
import de.iteratec.osm.measurement.environment.Location
import de.iteratec.osm.measurement.environment.dao.BrowserDaoService
import de.iteratec.osm.measurement.environment.dao.LocationDaoService
import de.iteratec.osm.measurement.schedule.ConnectivityProfile
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
import de.iteratec.osm.util.PerformanceLoggingService
import org.joda.time.DateTime
import org.joda.time.Interval
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter

import java.text.SimpleDateFormat
import java.util.regex.Pattern

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
    JobResultDaoService jobResultDaoService
    EventResultDaoService eventResultDaoService
    PaginationService paginationService
    EventResultDashboardService eventResultDashboardService

    private Map<String, Object> listResultsByCommand(TabularResultEventResultsCommandBase cmd) {

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
                List<EventResult> eventResults = null
                if (cmd instanceof TabularResultListResultsCommand) {
                    eventResults = eventResultDaoService.getCountedByStartAndEndTimeAndMvQueryParams(
                            ((TabularResultListResultsCommand)cmd).createMvQueryParams(),
                            timeFrame.getStart().toDate(),
                            timeFrame.getEnd().toDate(),
                            cmd.getMax(),
                            cmd.getOffset(),
                            new CriteriaSorting(sortAttribute: 'jobResultDate', sortOrder: CriteriaSorting.SortOrder.DESC)
                    )
                    paginationListing = paginationService.buildListResultsPagination((TabularResultListResultsCommand)cmd, eventResults.getTotalCount())
                } else if (cmd instanceof TabularResultListResultsForSpecificJobCommand) {
                    eventResults = eventResultDaoService.getEventResultsByJob(
                            ((TabularResultListResultsForSpecificJobCommand)cmd).job,
                            timeFrame.getStart().toDate(),
                            timeFrame.getEnd().toDate(),
                            cmd.getMax(),
                            cmd.getOffset()
                    )
                    paginationListing = paginationService.buildListResultsForJobPagination((TabularResultListResultsForSpecificJobCommand)cmd, eventResults.getTotalCount())
                }

                for(EventResult eachEventResult : eventResults)
                {
                    if(! eachEventResult.medianValue )
                    {
                        continue;
                    }
                    JobResult correspondingJobResult = eachEventResult.jobResult;
                    eventResultsListing.addRow(new EventResultListingRow(correspondingJobResult, eachEventResult))
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
    public Map<String, Object>listResults(TabularResultListResultsCommand cmd) {
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
    public Map<String, Object> listResultsForJob(TabularResultListResultsForSpecificJobCommand cmd) {
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
    public Map<String, Object> ShowListResultsForJob(TabularResultListResultsForSpecificJobCommand cmd) {
        if (cmd.job!=null && cmd.from==null && cmd.to==null){
            redirect(action: 'listResultsForJob', params:['job.id': cmd.job.getId()])
        }
        Map<String, Object> modelToRender = listResultsByCommand(cmd)
        modelToRender.put('showSpecificJob', true)
        render(view: 'listResults', model: modelToRender)
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

        result['connectivityProfiles'] = eventResultDashboardService.getAllConnectivityProfiles()
        result.put("tagToJobGroupNameMap", jobGroupDaoService.getTagToJobGroupNameMap())

        // Done! :)
        return result;
    }
}
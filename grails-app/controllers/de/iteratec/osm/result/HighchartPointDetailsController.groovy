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

import de.iteratec.osm.report.chart.AggregatorType
import de.iteratec.osm.report.chart.CsiAggregation
import de.iteratec.osm.report.chart.CsiAggregationDaoService
import de.iteratec.osm.report.ui.EventResultListing
import de.iteratec.osm.report.ui.EventResultListingRow
import de.iteratec.osm.result.dao.EventResultDaoService
import de.iteratec.osm.util.PerformanceLoggingService
import de.iteratec.osm.util.PerformanceLoggingService.IndentationDepth
import de.iteratec.osm.util.PerformanceLoggingService.LogLevel
import grails.web.mapping.LinkGenerator
import org.joda.time.DateTime

/**
 * <p>
 * A controller to show details of a {@link HighchartPoint} clicked in an 
 * dashboard.
 * </p>
 *
 * @author mze
 * @since IT-78
 */
class HighchartPointDetailsController {

    CsiAggregationDaoService csiAggregationDaoService;
    EventResultDaoService eventResultDaoService
    JobResultDaoService jobResultDaoService;
    PerformanceLoggingService performanceLoggingService;
    /**
     * The Grails engine to generate links.
     *
     * @see http://mrhaki.blogspot.ca/2012/01/grails-goodness-generate-links-outside.html
     */
    LinkGenerator grailsLinkGenerator

    /**
     * This is the count of results that are shown immediately by
     * {@link #listAggregatedResults(Long, Integer)}. The count could be
     * much larger, for that a message is shown.
     *
     * @see #WARN_IF_MORE_RESULTS_EXCEEDS
     */
    private static final int COUNT_OF_RESULTS_To_BE_LOADED_IMMEDIATELY = 25;

    /**
     * The number of more results could be shown for that a warning message
     * about long loading times should be shown if exceeded.
     *
     * This is the number of MORE results (excluding the already immediately
     * shown ones).
     *
     * @see #COUNT_OF_RESULTS_To_BE_LOADED_IMMEDIATELY
     */
    private static final int WARN_IF_MORE_RESULTS_EXCEEDS = 150;

    /**
     * <p>
     * Lists aggregated {@linkplain de.iteratec.osm.result.JobResult results} for a {@link
     * CsiAggregation} mostly afterwards a user clicked on an {@link
     * HighchartPoint}.
     * </p>
     *
     * <p>
     * The user is a list shown which contains all results that where
     * aggregated to calculate the value of the measured value specified by
     * {@code csiAggregationId}.
     * </p>
     *
     * <p>
     * If the {@link CsiAggregation#countUnderlyingEventResultsByWptDocComplete() count of results} in the
     * measured value differs from {@code lastKnownCountOfAggregatedResults}
     * a waring in shown to the user.
     * </p>
     *
     * @param csiAggregationId
     *         The database id of the {@link de.iteratec.osm.report.chart.CsiAggregation} for which the
     *         aggregated {@linkplain de.iteratec.osm.result.JobResult job results} should be listed.
     *         Passing <code>null</code> results in a "HTTP 400 Bad Request".
     * @param lastKnownCountOfAggregatedResultsOrNull
     *         The last known count of job results from which the measured
     *         value with id {@code csiAggregationId} was aggregated. This is
     *         the count known as the chart in which the user clicks, was
     *         generated. This value is passed to identify concurrent
     *         modifications on a value to communicate an possible change of
     *         value to the user when results are listed.
     *         Passing <code>null</code> results in a "HTTP 400 Bad Request".
     * @param showAll
     *         Optional parameters to show all results instead of limit the
     *         listed results to
     * {@link #COUNT_OF_RESULTS_To_BE_LOADED_IMMEDIATELY}. If this
     *         parameter is <code>null</code>, the list will be limited, else
     *         if and only if this parameter is <code>true</code> the list
     *         will not be limited.
     *
     * @return The model to render, not <code>null</code>.
     *         The structure of the map is:
     *         <dl>
     *             <dt>jobResultsListing</dt>
     *             <dd>
     *                 A list of {@link EventResultListingRow} which represents
     *                 the results to list. This value is a {@link Collection}
     *                 and never <code>null</code>.
     *             </dd>
     *             <dt>countOfAggregatedResultsDiffers
     *             (potentially <code>null</code>)
     *             </dt>
     *             <dd>
     *                 A Boolean. If set to <code>true</code>, the view should
     *                 show a warning about a difference between expected and
     *                 current job results count. If <code>true</code> the
     *                 current count is available from this map using the key
     * {@code currentCountOfAggregatedResults} and the expected
     *                 using {@code lastKnownCountOfAggregatedResultsOrNull}; both
     *                 values of type Integer.
     *             </dd>
     *             <dt>someEventResultsMissing</dt>
     *             <dd>
     *                 A Boolean. If set to <code>true</code> some results
     *                 required to be listed are missing. If <code>true</code>
     *                 the database IDs of the missing results are listed in
     *                 the collection available from this map using the key
     * {@code missingEventResultsIds}.
     *                 This value is potentially <code>null</code>.
     *             </dd>
     *             <dt>remainingResultsCount</dt>
     *             <dd>
     *                 The count of remaining results which were not loaded immediately.
     *                 For performance reasons only {@link #COUNT_OF_RESULTS_To_BE_LOADED_IMMEDIATELY}
     *                 are loaded immediately. If there are more, this value indicates
     *                 the number of not loaded / not listed results.
     *                 This value is an {@link Integer} and <code>null</code> if no
     *                 results remaining (skipped on load).
     *                 This key is set if and only if {@code showAll} is
     *                 false or <code>null</code>.
     *             </dd>
     *             <dt>showRemainingResultsCountVeryLargeWaring</dt>
     *             <dd>
     *                 If this value is <code>true</code>, the count of
     *                 remaining results is very large so the UI should
     *                 show a waring about very long loading time.
     *                 This value is an {@link Boolean} and
     *                 never <code>null</code>.
     *                 This key is set if and only if {@code showAll} is
     *                 false or <code>null</code>.
     *             </dd>
     *             <dt>measuringOfValueStartedAt</dt>
     *             <dd>
     *                 The date the measuring of the aggregated value started.
     *                 This is the date of the first aggregated test.
     *                 This value is a {@link Date} and is never
     *                 <code>null</code>.
     *             </dd>
     *             <dt>aggregatedCsiAggregation</dt>
     *             <dd>
     *                 The aggregated value as {@link Double} and is never
     *                 <code>null</code>.
     *             </dd>
     *         </dl>
     */
    public Map<String, Object> listAggregatedResults(Long csiAggregationId, Integer lastKnownCountOfAggregatedResultsOrNull, Boolean showAll) {

        if (csiAggregationId == null || csiAggregationId < 0 || lastKnownCountOfAggregatedResultsOrNull == null || lastKnownCountOfAggregatedResultsOrNull < 0) {
            render(status: 400, message: "Bad Request");
            return null;
        }

        // Limit listing?
        boolean limitListing = true;
        if (showAll) {
            limitListing = false;
        }

        // Prepare view model:
        Map<String, Object> modelToRender = Collections.checkedMap(new HashMap(), String.class, Object.class);

        // Load relevant data:
        CsiAggregation valueThatResultsShouldBeListed = csiAggregationDaoService.tryToFindById(csiAggregationId);

        Collection<Long> resultIds = valueThatResultsShouldBeListed.getUnderlyingEventResultsByWptDocCompleteAsList();

        addWaringIfResultCountDiffersFromExpectation(lastKnownCountOfAggregatedResultsOrNull, resultIds.size(), modelToRender);

        EventResultListing eventResultListing = new EventResultListing();
        modelToRender.put('eventResultListing', eventResultListing);

        Collection<Long> missingEventResults = []

        int immeditaelyLoadedResultsCount = 0;

        for (Long eachEventResultId : resultIds) {
            if (limitListing && immeditaelyLoadedResultsCount >= COUNT_OF_RESULTS_To_BE_LOADED_IMMEDIATELY) {
                break;
            }

            EventResult eventResult = eventResultDaoService.tryToFindById(eachEventResultId);

            if (eventResult == null) {
                missingEventResults.add(eachEventResultId);
                continue;
            }

            JobResult jobResult = eventResult.jobResult;
            eventResultListing.addRow(new EventResultListingRow(jobResult, eventResult));

            immeditaelyLoadedResultsCount++;
        }

        if (!missingEventResults.isEmpty()) {
            modelToRender.put('someEventResultsMissing', true);
            modelToRender.put('missingEventResultsIds', missingEventResults);
        }

        int allResultsCount = resultIds.size() - missingEventResults.size();
        if (limitListing && immeditaelyLoadedResultsCount < allResultsCount) {
            // There are more results!
            int remainingResultsCount = allResultsCount - immeditaelyLoadedResultsCount;

            if (remainingResultsCount > WARN_IF_MORE_RESULTS_EXCEEDS) {
                modelToRender.put('showRemainingResultsCountVeryLargeWaring', true);
            } else {
                modelToRender.put('showRemainingResultsCountVeryLargeWaring', false);
            }

            modelToRender.put('remainingResultsCount', remainingResultsCount);
        }

        modelToRender.put('measuringOfValueStartedAt', valueThatResultsShouldBeListed.started ?: new Date(0));
        modelToRender.put('aggregatedCsiAggregationDocComplete', valueThatResultsShouldBeListed.csByWptDocCompleteInPercent ?: 0.0d);
        modelToRender.put('aggregatedCsiAggregationVisuallyComplete', valueThatResultsShouldBeListed.csByWptVisuallyCompleteInPercent ?: 0.0d);

        return modelToRender;
    }

    /**
     * <p>
     * Adds a waring indicator to {@code modelToRender} if the expected
     * result count differs from the current.
     * </p>
     *
     * @param expectedCount
     *         The expected count of results.
     * @param currentCount
     *         The current count of results.
     * @param modelToRender
     *         The model to render to which the waring should be added if
     *         applicable.
     */
    private
    static void addWaringIfResultCountDiffersFromExpectation(int expectedCount, int currentCount, Map<String, Object> modelToRender) {
        if (currentCount != expectedCount) {
            modelToRender.put('countOfAggregatedResultsDiffers', true);
            modelToRender.put('lastKnownCountOfAggregatedResultsOrNull', expectedCount);
            modelToRender.put('currentCountOfAggregatedResults', currentCount);
        }
    }

    /**
     * <p>
     * Lists {@linkplain EventResult event results} for a selection
     * range mostly afterwards a user clicked on an {@link
     * HighchartPoint}.
     * </p>
     *
     * <p>
     * Note: This action does not hold a measured value, so the model
     * returned will contain the key {@code aggregatedCsiAggregation}
     * assigned to <code>null</code>. This is a special situation need
     * to handled in views. For more details about the returned model
     * refer the documentation of
     * {@link #listAggregatedResults(Long, Integer, Boolean)}.
     * Furthermore it adds the key {@code listAggregatedResultsByQueryParams}
     * with value <code>true</code> to the model to indicate to call this
     * action on "show-all" instead of the other one.
     * </p>
     *
     * <p>
     * This methods renders are "HTTP 400 Bad Request" status if at
     * least one of the parameters is invalid.
     * </p>
     *
     * @param from
     *         The time in milliseconds since epoch
     *         (see {@link Date#getTime()}) from when the listing starts.
     * @param to
     *         The time in milliseconds since epoch
     *         (see {@link Date#getTime()}) when the listing ends.
     *         Must be largen or equal to {@code from}.
     * @param aggregatorTypeNameOrNull
     *         The aggregator type name to use to indicate the caching-state
     *         of results. If this argument is <code>null</code> all possible
     *         caching states are included in search. See
     * {@link AggregatorType#isCached()}.
     * @param lastKnownCountOfAggregatedResultsOrNull
     *         The number of last known result count to identify concurrent
     *         changes or <code>null</code> to indicate that there was no
     *         count known before this call.
     * @param showAll
     *         Optional parameters to show all results instead of limit the
     *         listed results to
     * {@link #COUNT_OF_RESULTS_To_BE_LOADED_IMMEDIATELY}. If this
     *         parameter is <code>null</code>, the list will be limited, else
     *         if and only if this parameter is <code>true</code> the list
     *         will not be limited.
     * @return The model to render as also described in
     * {@link #listAggregatedResults(Long, Integer, Boolean)};
     *         never <code>null</code>.
     *
     * @since IT-109
     * @version 2 (IT-106)
     */
    public Map<String, Object> listAggregatedResultsByQueryParams(
            Long from, Long to, Long jobGroupId, Long measuredEventId, Long pageId, Long browserId, Long locationId, String aggregatorTypeNameOrNull, Integer lastKnownCountOfAggregatedResultsOrNull, Boolean showAll) {

        if (!from || !to) {
            render(status: 400, message: "Bad Request");
            return null;
        }

        DateTime fromDate = new DateTime(from);
        DateTime toDate = new DateTime(to);

        if (!fromDate.isBefore(toDate)) {
            render(status: 400, message: "Bad Request");
            return null;
        }

        // Select the cached view state:
        Set<CachedView> relevantCachedViews = Collections.checkedSet(new HashSet<CachedView>(), CachedView.class);
        if (aggregatorTypeNameOrNull != null && !aggregatorTypeNameOrNull.isEmpty()) {
            AggregatorType aggregatorType = AggregatorType.findByName(aggregatorTypeNameOrNull)
            if (!aggregatorType) {
                render(status: 400, message: "Bad Request");
                return null;
            }

            if (aggregatorType.isCachedCriteriaApplicable()) {
                relevantCachedViews.add(aggregatorType.isCached() ? CachedView.CACHED : CachedView.UNCACHED);
            } else {
                addAllCahcedStates(relevantCachedViews);
            }
        } else {
            addAllCahcedStates(relevantCachedViews);
        }

        // Limit listing?
        boolean limitListing = true;
        if (showAll) {
            limitListing = false;
        }

        // Prepare view model:
        Map<String, Object> modelToRender = Collections.checkedMap(new HashMap(), String.class, Object.class);

        // Set Marker for this action
        modelToRender.put('listAggregatedResultsByQueryParams', true)

        // Load relevant data:
        List<EventResult> eventResults
        performanceLoggingService.logExecutionTime(LogLevel.DEBUG, 'getting event-results - listAggregatedResultsByQueryParams - getMedianEventResultsBy', IndentationDepth.ONE) {
            eventResults = eventResultDaoService.getMedianEventResultsBy(
                    fromDate.toDate(), toDate.toDate(), relevantCachedViews, jobGroupId, measuredEventId, pageId, browserId, locationId)
        }
        if (lastKnownCountOfAggregatedResultsOrNull != null) {
            addWaringIfResultCountDiffersFromExpectation(lastKnownCountOfAggregatedResultsOrNull, eventResults.size(), modelToRender);
        }

        EventResultListing eventResultListing = new EventResultListing();
        modelToRender.put('eventResultListing', eventResultListing);

        int immeditaelyLoadedResultsCount = 0;

        for (EventResult eachEventResult : eventResults) {
            if (limitListing && immeditaelyLoadedResultsCount >= COUNT_OF_RESULTS_To_BE_LOADED_IMMEDIATELY) {
                break;
            }

            JobResult correspondingJobResult = eachEventResult.jobResult;
            eventResultListing.addRow(new EventResultListingRow(correspondingJobResult, eachEventResult))

            immeditaelyLoadedResultsCount++;
        }

        int allResultsCount = eventResults.size();
        if (limitListing && immeditaelyLoadedResultsCount < allResultsCount) {
            // There are more results!
            int remainingResultsCount = allResultsCount - immeditaelyLoadedResultsCount;

            if (remainingResultsCount > WARN_IF_MORE_RESULTS_EXCEEDS) {
                modelToRender.put('showRemainingResultsCountVeryLargeWaring', true);
            } else {
                modelToRender.put('showRemainingResultsCountVeryLargeWaring', false);
            }

            modelToRender.put('remainingResultsCount', remainingResultsCount);
        }

        modelToRender.put('measuringOfValueStartedAt', fromDate);
        //leads to NPE at java.util.Collections$CheckedMap.typeCheck(Collections.java:2558) ???
//		modelToRender.put('aggregatedCsiAggregation', null);
        modelToRender.put('aggregatedCsiAggregation', -999d);

        render([model: modelToRender, view: 'listAggregatedResults']);
        return null;
    }

    /**
     * <p>
     * Adds all cached states to the specified set.
     * </p>
     *
     * @param toAddStatesTo The set to add cached states to, not <code>null</code>.
     */
    private static void addAllCahcedStates(Set<CachedView> toAddStatesTo) {
        for (CachedView eachConstant : CachedView.class.getEnumConstants()) {
            toAddStatesTo.add(eachConstant);
        }
    }

    /**
     * <p>
     * Redirects to the measuring server of an event result.
     * </p>
     *
     * <p>
     * Mostly this is done afterwards a user clicked on an {@link
     * HighchartPoint}. This redirection is used to prevent
     * {@linkplain JobResult job results}, which produced the URLs from
     * being loaded during rendering an event result graph (performance
     * optimization)
     * </p>
     *
     * @param eventResultId
     *         The id of the event result for that a redirect
     *         is to be performed; not <code>null</code>.
     * @return Nothing. Redirects immediately if has valid arguments;
     *         else a status 400 is rendered.
     * @since IT-109
     */
    public Map<String, Object> redirectToWptServerDetailPage(Long eventResultId) {
        if (!eventResultId) {
            render(status: 400, message: "Bad Request");
            return null;
        }
        EventResult resultToShow = EventResult.get(eventResultId)
        if (!resultToShow) {
            render(status: 400, message: "Bad Request");
            return null;
        }
        JobResult jobResult = resultToShow.jobResult
        if (!jobResult) {
            render(status: 400, message: "Bad Request");
            return null;
        }

        URL serverURL = jobResult.tryToGetTestsDetailsURL();

        if (serverURL) {
            response.setStatus(303)
            response.setHeader("Location", jobResult.tryToGetTestsDetailsURL().toString())
            render(status: 303)
            return null;
        } else {
            response.setContentType('text/plain;charset=UTF-8');
            response.status = 404; // NOT FOUND

            // TODO mze-2013-10-10: Suggest to render a more common error page instead of a simple message.

            Writer textOut = new OutputStreamWriter(response.getOutputStream());
            textOut.write('No server details available for the specified event result.');

            textOut.flush();
            response.getOutputStream().flush();
            return null;
        }
    }

    /**
     * <p>
     * Performs a redirect with HTTP status code 303 (see other).
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
}

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

import de.iteratec.osm.OsmConfigCacheService
import de.iteratec.osm.csi.Page
import de.iteratec.osm.measurement.environment.Browser
import de.iteratec.osm.measurement.environment.BrowserService
import de.iteratec.osm.measurement.environment.Location
import de.iteratec.osm.measurement.schedule.ConnectivityProfile
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.measurement.schedule.dao.JobGroupDaoService
import de.iteratec.osm.report.chart.*
import de.iteratec.osm.result.dao.EventResultDaoService
import de.iteratec.osm.result.dao.EventResultProjection
import de.iteratec.osm.result.dao.EventResultQueryBuilder
import de.iteratec.osm.result.dao.query.TrimQualifier
import de.iteratec.osm.util.I18nService
import de.iteratec.osm.util.PerformanceLoggingService
import de.iteratec.osm.util.PerformanceLoggingService.LogLevel
import grails.transaction.Transactional
import grails.web.mapping.LinkGenerator
import org.joda.time.DateTime

import static de.iteratec.osm.util.Constants.TIMESERIES_CHART_LEGEND_DELIMITTER
/**
 * <p>
 * A utility service for the event result dashboard and related operations.
 * </p>
 */
@Transactional
public class EventResultDashboardService {

    BrowserService browserService
    JobGroupDaoService jobGroupDaoService
    I18nService i18nService
    EventResultDaoService eventResultDaoService
    CsiAggregationUtilService csiAggregationUtilService
    PerformanceLoggingService performanceLoggingService
    OsmChartProcessingService osmChartProcessingService
    OsmConfigCacheService osmConfigCacheService

    /**
     * The Grails engine to generate links.
     *
     * @see http://mrhaki.blogspot.ca/2012/01/grails-goodness-generate-links-outside.html
     */
    LinkGenerator grailsLinkGenerator

    /**
     * Fetches all {@link MeasuredEvent}s from Database.
     *
     * <p>
     * 	Proxy for {@link MeasuredEvent}
     * </p>
     *
     * @return all {@link MeasuredEvent} ordered by their name.
     */
    public List<MeasuredEvent> getAllMeasuredEvents() {
        return MeasuredEvent.findAll().sort(false, { it.name.toLowerCase() })
    }

    /**
     * Fetches all {@link Location}s from Database.
     *
     * @return all {@link Location} ordered by their label.
     */
    public List<Location> getAllLocations() {
        return Location.list().sort(false, { it.label.toLowerCase() })
    }

    /**
     * Fetches all {@link ConnectivityProfile}s from Database.
     *
     * <p>
     * 	Proxy for {@link ConnectivityDaoService}
     * </p>
     *
     * @return all {@link ConnectivityProfile} ordered by their toString() representation.
     */
    public List<ConnectivityProfile> getAllConnectivityProfiles() {
        return ConnectivityProfile.findAllByActive(true).sort(false, { it.name.toLowerCase() })
    }

    /**
     * Collects all available connectivities.
     * This includes all ConnectivityProfiles, all CustomConnectivityNames from EventResult and "native" if EventResults with native measurement exist
     * @param includeNative if set to true and eventResults exists with noTrafficShapingAtAll "native" is added to the list
     * @return
     */
    List<Map<String, String>> getAllConnectivities(boolean includeNative = true) {
        List<Map<String, String>> result = [].withDefault { [:] }
        result.addAll(getAllConnectivityProfiles().collect { ["id": it.id, "name": it.toString()] })

        if (includeNative) {
            result.add(["id": ResultSelectionController.MetaConnectivityProfileId.Native.value, "name": ResultSelectionController.MetaConnectivityProfileId.Native.value])
        }

        return result
    }

    /**
     * Fetches all {@link Browser}s from Database.
     *
     * <p>
     * 	Proxy for {@link BrowserService}
     * </p>
     *
     * @return all {@link Browser} ordered by their name.
     */
    public List<Browser> getAllBrowser() {

        return browserService.findAll().sort(false, { it.name.toLowerCase() })
    }

    /**
     * Fetches all {@link Page}s from Database.
     *
     * @return all {@link Page} ordered by their name.
     */
    public List<Page> getAllPages() {
        return Page.list().sort(false, { it.name.toLowerCase() })
    }

    /**
     * Fetches all {@link JobGroup}s from Database.
     *
     * <p>
     * 	Proxy for {@link JobGroupDaoService}
     * </p>
     *
     * @return all {@link JobGroup} ordered by their name.
     *
     */
    public List<JobGroup> getAllJobGroups() {
        return jobGroupDaoService.findAll().sort(false, { it.name.toLowerCase() })
    }

    /**
     * Returns a list of {@linkHighchartGraph}s for the highchart lib.
     *
     * @param startDate selected start date
     * @param endDate selected end date
     * @param interval selected interval
     * @param n selected AggregatorTypes
     * @param queryParams
     * 		Query params for querying {@link EventResult}s.
     * @return List of {@linkHighchartGraph}s
     *
     * @throws IllegalArgumentException if an argument is not found or supported.
     *
     * @todo TODO mze-2013-09-12: Suggest to move to a generic HighchartFactoryService.
     */
    public OsmRickshawChart getEventResultDashboardHighchartGraphs(
            Date startDate, Date endDate, Integer interval, List<SelectedMeasurand> selectedMeasurands, ErQueryParams queryParams) {

        Collection<EventResultProjection> eventResults
        performanceLoggingService.logExecutionTime(LogLevel.DEBUG, 'getting event-results - with builder', 1) {

            EventResultQueryBuilder queryBuilder
            performanceLoggingService.logExecutionTime(LogLevel.DEBUG, 'getting event-results - create query builder', 2) {
                queryBuilder = new EventResultQueryBuilder(osmConfigCacheService.getMinValidLoadtime(), osmConfigCacheService.getMaxValidLoadtime())
                        .withJobResultDateBetween(startDate, endDate)
                        .withJobGroupIdsIn(queryParams.jobGroupIds as List)
                        .withPageIdsIn(queryParams.pageIds as List)
                        .withLocationIdsIn(queryParams.locationIds as List)
                        .withBrowserIdsIn(queryParams.browserIds as List)
                        .withMeasuredEventIdsIn(queryParams.measuredEventIds as List)
                        .withSelectedMeasurands(selectedMeasurands)
            }

            performanceLoggingService.logExecutionTime(LogLevel.DEBUG, 'getting event-results - append connectivities', 2) {
                appendConnectivity(queryBuilder, queryParams)
            }
            performanceLoggingService.logExecutionTime(LogLevel.DEBUG, 'getting event-results - append trims', 2) {
                appendTrims(queryBuilder, queryParams)
            }

            performanceLoggingService.logExecutionTime(LogLevel.DEBUG, 'getting event-results - actually query the data', 2) {
                eventResults = queryBuilder.getRawData()
            }

        }
        log.debug("getting event-results - Got ${eventResults.size()} EventResultProjections")

        OsmRickshawChart osmRickshawChart
        performanceLoggingService.logExecutionTime(LogLevel.DEBUG, 'calculateResultMap', 1) {
            osmRickshawChart = calculateResultMap(eventResults, selectedMeasurands, interval)
        }
        return osmRickshawChart
    }

    private void appendConnectivity(EventResultQueryBuilder queryBuilder, ErQueryParams queryParams) {
        if (!queryParams.includeAllConnectivities) {
            queryBuilder.withConnectivity(
                    queryParams.connectivityProfileIds as List,
                    queryParams.customConnectivityNames as List,
                    queryParams.includeNativeConnectivity)
        }
    }

    private void appendTrims(EventResultQueryBuilder queryBuilder, ErQueryParams queryParams) {
        if (queryParams.minLoadTimeInMillisecs) queryBuilder.withTrim(queryParams.minLoadTimeInMillisecs, TrimQualifier.GREATER_THAN, MeasurandGroup.LOAD_TIMES)
        if (queryParams.maxLoadTimeInMillisecs) queryBuilder.withTrim(queryParams.maxLoadTimeInMillisecs, TrimQualifier.LOWER_THAN, MeasurandGroup.LOAD_TIMES)
        if (queryParams.minRequestCount) queryBuilder.withTrim(queryParams.minRequestCount, TrimQualifier.GREATER_THAN, MeasurandGroup.REQUEST_COUNTS)
        if (queryParams.maxRequestCount) queryBuilder.withTrim(queryParams.maxRequestCount, TrimQualifier.LOWER_THAN, MeasurandGroup.REQUEST_COUNTS)
        if (queryParams.minRequestSizeInBytes) queryBuilder.withTrim(queryParams.minRequestSizeInBytes, TrimQualifier.GREATER_THAN, MeasurandGroup.REQUEST_SIZES)
        if (queryParams.maxRequestSizeInBytes) queryBuilder.withTrim(queryParams.maxRequestSizeInBytes, TrimQualifier.LOWER_THAN, MeasurandGroup.REQUEST_SIZES)
    }

    /**
     * <p>
     * Transforms given eventResults to a list of {@link de.iteratec.osm.report.chart.OsmChartGraph}s respective given measurands (aggregators) and interval.
     * If interval is not {@link CsiAggregationInterval#RAW} the values for the measurands will be aggregated respective interval.
     * </p>
     *
     * @param eventResults
     * @param aggregators
     * @param interval
     * @return
     */
    private OsmRickshawChart calculateResultMap(Collection<EventResultProjection> eventResults, List<SelectedMeasurand> selectedMeasurands, Integer interval) {
        Map<GraphLabel, List<OsmChartPoint>> calculatedResultMap
        performanceLoggingService.logExecutionTime(LogLevel.DEBUG, 'getting result-map', 1) {
            if (interval == CsiAggregationInterval.RAW) {
                calculatedResultMap = calculateResultMapForRawData(selectedMeasurands, eventResults)
            } else {
                calculatedResultMap = calculateResultMapForAggregatedData(selectedMeasurands, eventResults, interval)
            }
        }
        List<OsmChartGraph> graphs = []
        OsmRickshawChart chart
        performanceLoggingService.logExecutionTime(LogLevel.DEBUG, 'set speaking graph labels and sorting', 1) {
            performanceLoggingService.logExecutionTime(LogLevel.DEBUG, 'set speaking graph labels', 2) {
                graphs = setSpeakingGraphLabelsAndSort(calculatedResultMap)
            }
            performanceLoggingService.logExecutionTime(LogLevel.DEBUG, 'sorting', 2) {
                chart = osmChartProcessingService.summarizeEventResultGraphs(graphs)
            }
        }
        return chart
    }

    private Map<GraphLabel, List<OsmChartPoint>> calculateResultMapForRawData(List<SelectedMeasurand> selectedMeasurands, Collection<EventResultProjection> eventResults) {

        performanceLoggingService.resetExecutionTimeLoggingSession()

        Map<GraphLabel, List<OsmChartPoint>> chartPointsForEachGraph = [:]
        selectedMeasurands.each { SelectedMeasurand selectedMeasurand ->

            eventResults.each { EventResultProjection eventResult ->

                Double value
                performanceLoggingService.logExecutionTimeSilently(LogLevel.DEBUG, 'getting result-map RAW - get normalized value', 2){
                    value = selectedMeasurand.getNormalizedValueFrom(eventResult)
                }

                if (selectedMeasurand.cachedView == eventResult.cachedView && value != null) {

                    GraphLabel graphLabel
                    performanceLoggingService.logExecutionTimeSilently(LogLevel.DEBUG, 'getting result-map RAW - create GraphLabels', 2){
                        graphLabel = new GraphLabel(eventResult, null, selectedMeasurand)
                    }
                    URL testsDetailsURL
                    performanceLoggingService.logExecutionTimeSilently(LogLevel.DEBUG, 'getting result-map RAW - building detail urls', 2){
                        testsDetailsURL = eventResult.testDetailsWaterfallURL ?: this.buildTestsDetailsURL(eventResult)
                    }
                    WptEventResultInfo chartPointWptInfo
                    performanceLoggingService.logExecutionTimeSilently(LogLevel.DEBUG, 'getting result-map RAW - get points wpt infos', 2){
                        chartPointWptInfo = getChartPointsWptInfos(eventResult)
                    }

                    try {
                        performanceLoggingService.logExecutionTimeSilently(LogLevel.DEBUG, 'getting result-map RAW - creating OsmChartPoints', 2){
                            long time
                            String agent
                            performanceLoggingService.logExecutionTimeSilently(LogLevel.DEBUG, 'creating OsmChartPoints - get values', 3){
                                time = eventResult.jobResultDate.time
                                agent = eventResult.testAgent
                            }
                            OsmChartPoint chartPoint
                            performanceLoggingService.logExecutionTimeSilently(LogLevel.DEBUG, 'creating OsmChartPoints - creation', 3){
                                chartPoint = new OsmChartPoint(
                                        time: time,
                                        csiAggregation: value,
                                        countOfAggregatedResults: 1,
                                        sourceURL: testsDetailsURL,
                                        testingAgent: agent,
                                        chartPointWptInfo: chartPointWptInfo
                                )
                            }
                            performanceLoggingService.logExecutionTimeSilently(LogLevel.DEBUG, 'creating OsmChartPoints - add to list', 3){
                                if (chartPoint.isValid()){
                                    // The following is a bit more verbose than using a groovy MapWithDefault, but significantly faster
                                    if (chartPointsForEachGraph[graphLabel] == null) {
                                        chartPointsForEachGraph[graphLabel] = []
                                    }
                                    chartPointsForEachGraph[graphLabel].add(chartPoint)
                                }
                            }
                        }
                    } catch (Exception e) {
                        log.error("The following EventResultProjection couldn't be used to create an OsmChartPoint: $eventResult")
                    }
                }

            }
        }

        log.debug(performanceLoggingService.getExecutionTimeLoggingSessionData(LogLevel.DEBUG))

        return chartPointsForEachGraph
    }

    private getChartPointsWptInfos(EventResultProjection eventResult) {
        String serverBaseUrl = eventResult.wptServerBaseurl
        String testId = eventResult.testId
        Integer numberOfWptRun = eventResult.numberOfWptRun
        CachedView cachedView = eventResult.cachedView
        Integer oneBaseStepIndexInJourney = eventResult.oneBasedStepIndexInJourney
        WptEventResultInfo chartPointWptInfo = new WptEventResultInfo(
                serverBaseUrl: serverBaseUrl,
                testId: testId,
                numberOfWptRun: numberOfWptRun,
                cachedView: cachedView,
                oneBaseStepIndexInJourney: oneBaseStepIndexInJourney
        )
        return chartPointWptInfo
    }

    private boolean isInBounds(
            def value, SelectedMeasurand selectedMeasurand, Map<String, Number> gtBoundary, Map<String, Number> ltBoundary) {
        if (value == null) {
            return false
        }

        Number lt = selectedMeasurand.normalizeValue(gtBoundary[selectedMeasurand.name])
        Number gt = selectedMeasurand.normalizeValue(ltBoundary[selectedMeasurand.name])

        boolean inBound = true
        if (lt) inBound &= value > lt
        if (gt) inBound &= value < gt

        return inBound
    }

    private Map<GraphLabel, List<OsmChartPoint>> calculateResultMapForAggregatedData(List<SelectedMeasurand> selectedMeasurands, Collection<EventResultProjection> eventResults, Integer interval) {

        Map<GraphLabel, List<OsmChartPoint>> chartPointsForEachGraph = [:].withDefault { [] }
        Map<GraphLabel, List<Double>> eventResultsToAggregate = [:].withDefault { [] }

        performanceLoggingService.logExecutionTime(LogLevel.DEBUG, 'put results to map for aggregation', 2) {
            eventResults.each { EventResultProjection eventResult ->
                selectedMeasurands.each { SelectedMeasurand selectedMeasurand ->
                    if (eventResult.cachedView == selectedMeasurand.cachedView) {
                        Double value = selectedMeasurand.getNormalizedValueFrom(eventResult)
                        if (value != null){
                            Long millisStartOfInterval = csiAggregationUtilService.resetToStartOfActualInterval(new DateTime(eventResult.jobResultDate), interval).getMillis()
                            GraphLabel key = new GraphLabel(eventResult, millisStartOfInterval, selectedMeasurand)
                            eventResultsToAggregate[key] << value
                        }
                    }
                }
            }
        }

        performanceLoggingService.logExecutionTime(LogLevel.DEBUG, 'iterate over aggregation-map', 2) {
            URL testsDetailsURL
            Double sum = 0
            Integer countValues = 0
            eventResultsToAggregate.each { key, value ->

                testsDetailsURL = buildTestsDetailsURL(key.jobGroupId, key.measuredEventId, key.pageId, key.browserId, key.locationId, key.selectedMeasurand, key.millisStartOfInterval, interval, value.size())

                performanceLoggingService.logExecutionTime(LogLevel.TRACE, 'calculate value and create OsmChartPoint', 3) {

                    GraphLabel graphLabel = key.createCopy(false)
                    countValues = value.size()
                    if (countValues > 0) {
                        sum = 0
                        value.each { singleValue -> sum += singleValue }
                        OsmChartPoint chartPoint = new OsmChartPoint(
                                time: key.millisStartOfInterval,
                                csiAggregation: sum / countValues,
                                countOfAggregatedResults: countValues,
                                sourceURL: testsDetailsURL,
                                testingAgent: null)
                        if (chartPoint.isValid())
                            chartPointsForEachGraph[graphLabel] << chartPoint
                    }
                }
            }
        }
        return chartPointsForEachGraph.sort()
    }

    private List<OsmChartGraph> setSpeakingGraphLabelsAndSort(Map<GraphLabel, List<OsmChartPoint>> chartPointsForEachGraphOrigin) {

        String repeatedViewEnding = i18nService.msg("de.iteratec.isr.measurand.endingCached", "Cached", null)
        String firstViewEnding = i18nService.msg("de.iteratec.isr.measurand.endingUncached", "Uncached", null)

        List<OsmChartGraph> graphs = []

        Map<Serializable, JobGroup> jobGroupMap = [:]
        Map<Serializable, MeasuredEvent> measuredEventMap = [:]
        Map<Serializable, Location> locationMap = [:]
        chartPointsForEachGraphOrigin.each { graphLabel, highChartPoints ->
            performanceLoggingService.logExecutionTime(LogLevel.DEBUG, 'TEST', 1) {

                graphLabel.validate()

                String measurand = i18nService.msg("de.iteratec.isr.measurand.${graphLabel.selectedMeasurand.name}", graphLabel.selectedMeasurand.name, null)

                if (graphLabel.selectedMeasurand.cachedView == CachedView.CACHED) {
                    if (!repeatedViewEnding.isEmpty()) {
                        measurand = repeatedViewEnding + " " + measurand
                    }
                } else {
                    if (!firstViewEnding.isEmpty()) {
                        measurand = firstViewEnding + " " + measurand
                    }
                }

                if (graphLabel.tag) {

                    JobGroup group = jobGroupMap[graphLabel.jobGroupId] ?: JobGroup.get(graphLabel.jobGroupId)
                    MeasuredEvent measuredEvent = measuredEventMap[graphLabel.measuredEventId] ?: MeasuredEvent.get(graphLabel.measuredEventId)
                    Location location = locationMap[graphLabel.locationId] ?: Location.get(graphLabel.locationId)

                    if (group && measuredEvent && location) {
                        String newGraphLabel = "${measurand}${TIMESERIES_CHART_LEGEND_DELIMITTER}${group.name}${TIMESERIES_CHART_LEGEND_DELIMITTER}" +
                                "${measuredEvent.name}${TIMESERIES_CHART_LEGEND_DELIMITTER}${location.uniqueIdentifierForServer == null ? location.location : location.uniqueIdentifierForServer}" +
                                "${TIMESERIES_CHART_LEGEND_DELIMITTER}${graphLabel.connectivity}"
                        graphs.add(new OsmChartGraph(
                                label: newGraphLabel,
                                measurandGroup: graphLabel.selectedMeasurand.getMeasurandGroup(),
                                points: highChartPoints))
                    } else {
                        graphs.add(new OsmChartGraph(
                                label: "${measurand}${TIMESERIES_CHART_LEGEND_DELIMITTER}${graphLabel.tag}",
                                measurandGroup: graphLabel.selectedMeasurand.getMeasurandGroup(),
                                points: highChartPoints))
                    }
                } else {
                    graphs.add(new OsmChartGraph(
                            label: "${measurand}${TIMESERIES_CHART_LEGEND_DELIMITTER}${graphLabel.tag}",
                            measurandGroup: graphLabel.selectedMeasurand.getMeasurandGroup(),
                            points: highChartPoints))
                }
            }
        }
        graphs.each { graph ->
            graph.points.sort { it.time }
        }
        return graphs.sort()
    }

    /**
     * <p>
     * Builds up an URL where details to the specified {@link CsiAggregation}
     * are available if possible.
     * </p>
     *
     * @param mv
     *         The measured value for which an URL should be build
     *         not <code>null</code>.
     * @return The created URL or <code>null</code> if not possible to
     *         build up an URL.
     */
    public URL tryToBuildTestsDetailsURL(CsiAggregation mv) {
        URL result = null
        List<Long> eventResultIds = mv.underlyingEventResultsByWptDocCompleteAsList

        if (!eventResultIds.isEmpty()) {
            String testsDetailsURLAsString = grailsLinkGenerator.link([
                    'controller': 'highchartPointDetails',
                    'action'    : 'listAggregatedResults',
                    'absolute'  : true,
                    'params'    : [
                            'csiAggregationId'                       : String.valueOf(mv.id),
                            'lastKnownCountOfAggregatedResultsOrNull': String.valueOf(eventResultIds.size())
                    ]
            ])
            result = testsDetailsURLAsString ? new URL(testsDetailsURLAsString) : null
        }

        return result
    }

    /**
     * <p>
     * Builds up an URL to display details of the given {@link EventResult}s.
     * </p>
     *
     * @param results
     *         The {@link EventResult}s which should be displayed via the returned URL.
     *         not <code>null</code>.
     * @return The created URL or <code>null</code> if not possible to
     *         build up an URL.
     */
    public URL buildTestsDetailsURL(EventResultProjection result) {
        URL resultUrl = null

        if (result) {
            String testsDetailsURLAsString = grailsLinkGenerator.link([
                    'controller': 'highchartPointDetails',
                    'action'    : 'redirectToWptServerDetailPage',
                    'absolute'  : true,
                    'params'    : [
                            'eventResultId': String.valueOf(result.id)
                    ]
            ])
            resultUrl = testsDetailsURLAsString ? new URL(testsDetailsURLAsString) : null
        }

        return resultUrl
    }

    public URL buildTestsDetailsURL(Long jobGroupId, Long measuredEventId, Long pageId, Long browserId, Long locationId, SelectedMeasurand selectedMeasurand, Long millisFrom, Integer intervalInMinutes, Integer lastKnownCountOfAggregatedResults) {
        URL result = null


        if (jobGroupId && measuredEventId && pageId && browserId && locationId && selectedMeasurand) {

            String testsDetailsURLAsString = grailsLinkGenerator.link([
                    'controller': 'highchartPointDetails',
                    'action'    : 'listAggregatedResultsByQueryParams',
                    'absolute'  : true,
                    'params'    : [
                            'from'                                   : String.valueOf(millisFrom),
                            'to'                                     : String.valueOf(millisFrom + intervalInMinutes * 60 * 1000),
                            'jobGroupId'                             : String.valueOf(jobGroupId),
                            'measuredEventId'                        : String.valueOf(measuredEventId),
                            'pageId'                                 : String.valueOf(pageId),
                            'browserId'                              : String.valueOf(browserId),
                            'locationId'                             : String.valueOf(locationId),
                            'aggregatorTypeNameOrNull'               : selectedMeasurand.toString(),
                            'lastKnownCountOfAggregatedResultsOrNull': String.valueOf(lastKnownCountOfAggregatedResults)
                    ]
            ])
            result = testsDetailsURLAsString ? new URL(testsDetailsURLAsString) : null

        }

        return result
    }
}


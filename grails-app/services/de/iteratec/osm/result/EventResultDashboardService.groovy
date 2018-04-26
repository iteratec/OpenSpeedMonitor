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
import de.iteratec.osm.measurement.environment.BrowserService
import de.iteratec.osm.measurement.environment.Location
import de.iteratec.osm.measurement.schedule.ConnectivityProfile
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.measurement.schedule.dao.JobGroupDaoService
import de.iteratec.osm.report.chart.*
import de.iteratec.osm.result.dao.EventResultDaoService
import de.iteratec.osm.util.I18nService

import de.iteratec.osm.util.PerformanceLoggingService
import de.iteratec.osm.util.PerformanceLoggingService.LogLevel
import grails.gorm.transactions.Transactional
import grails.web.mapping.LinkGenerator
import org.joda.time.DateTime

import static de.iteratec.osm.util.Constants.HIGHCHART_LEGEND_DELIMITTER

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

        Map<String, Number> gtValues = [:]
        Map<String, Number> ltValues = [:]
        selectedMeasurands.each {
            if (it.getMeasurandGroup() == MeasurandGroup.LOAD_TIMES) {
                if (queryParams.minLoadTimeInMillisecs) {
                    gtValues[it.name] = queryParams.minLoadTimeInMillisecs
                }
                if (queryParams.maxLoadTimeInMillisecs) {
                    ltValues[it.name] = queryParams.maxLoadTimeInMillisecs
                }
            } else if (it.getMeasurandGroup() == MeasurandGroup.REQUEST_COUNTS) {
                if (queryParams.minRequestCount) {
                    gtValues[it.name] = queryParams.minRequestCount
                }
                if (queryParams.maxRequestCount) {
                    ltValues[it.name] = queryParams.maxRequestCount
                }
            } else if (it.getMeasurandGroup() == MeasurandGroup.REQUEST_SIZES) {
                if (queryParams.minRequestSizeInBytes) {
                    gtValues[it.name] = queryParams.minRequestSizeInBytes
                }
                if (queryParams.maxRequestSizeInBytes) {
                    ltValues[it.name] = queryParams.maxRequestSizeInBytes
                }
            }
        }

        Collection<EventResult> eventResults
        performanceLoggingService.logExecutionTime(LogLevel.DEBUG, 'getting event-results - getEventResultDashboardHighchartGraphs - getLimitedMedianEventResultsBy', 1) {
            eventResults = eventResultDaoService.getLimitedMedianEventResultsBy(
                    startDate,
                    endDate,
                    [CachedView.CACHED, CachedView.UNCACHED] as Set,
                    queryParams,
                    [:],
                    new CriteriaSorting(sortingActive: false)
            )
        }
        return calculateResultMap(eventResults, selectedMeasurands, interval, gtValues, ltValues)
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
    private OsmRickshawChart calculateResultMap(Collection<EventResult> eventResults, List<SelectedMeasurand> selectedMeasurands, Integer interval, Map<String, Number> gtBoundary, Map<String, Number> ltBoundary) {
        Map<GraphLabel, List<OsmChartPoint>> calculatedResultMap
        performanceLoggingService.logExecutionTime(LogLevel.DEBUG, 'getting result-map', 1) {
            if (interval == CsiAggregationInterval.RAW) {
                calculatedResultMap = calculateResultMapForRawData(selectedMeasurands, eventResults, gtBoundary, ltBoundary)
            } else {
                calculatedResultMap = calculateResultMapForAggregatedData(selectedMeasurands, eventResults, interval, gtBoundary, ltBoundary)
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

    private Map<GraphLabel, List<OsmChartPoint>> calculateResultMapForRawData(List<SelectedMeasurand> selectedMeasurands, Collection<EventResult> eventResults, Map<String, Number> gtBoundary, Map<String, Number> ltBoundary) {

        Map<GraphLabel, List<OsmChartPoint>> highchartPointsForEachGraph = [:].withDefault { [] }
        selectedMeasurands.each { SelectedMeasurand selectedMeasurand ->

            eventResults.each { EventResult eventResult ->
                URL testsDetailsURL = eventResult.testDetailsWaterfallURL ?: this.buildTestsDetailsURL(eventResult)

                // Get WPT event result info to build the WPT url dynamically
                String serverBaseUrl = eventResult.jobResult.wptServerBaseurl
                String testId = eventResult.jobResult.testId
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

                if (selectedMeasurand.cachedView == eventResult.cachedView) {
                    Double value = selectedMeasurand.getNormalizedValueFrom(eventResult)
                    if (isInBounds(value, selectedMeasurand, gtBoundary, ltBoundary)) {
                        GraphLabel graphLabel = new GraphLabel(eventResult, null, selectedMeasurand)
                        OsmChartPoint chartPoint = new OsmChartPoint(
                                time: eventResult.getJobResultDate().getTime(),
                                csiAggregation: value,
                                countOfAggregatedResults: 1,
                                sourceURL: testsDetailsURL,
                                testingAgent: eventResult.testAgent,
                                chartPointWptInfo: chartPointWptInfo
                        )
                        // customer satisfaction can be 0.
                        if (chartPoint.isValid() || (selectedMeasurand.getMeasurandGroup() == MeasurandGroup.PERCENTAGES && chartPoint.time >= 0 && chartPoint.csiAggregation != null))
                            highchartPointsForEachGraph[graphLabel].add(chartPoint)
                    }
                }
            }
        }

        return highchartPointsForEachGraph
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

    private Map<GraphLabel, List<OsmChartPoint>> calculateResultMapForAggregatedData(List<SelectedMeasurand> selectedMeasurands, Collection<EventResult> eventResults, Integer interval, Map<String, Number> gtBoundary, Map<String, Number> ltBoundary) {

        Map<GraphLabel, List<OsmChartPoint>> highchartPointsForEachGraph = [:].withDefault { [] }
        Map<GraphLabel, List<Double>> eventResultsToAggregate = [:].withDefault { [] }

        performanceLoggingService.logExecutionTime(LogLevel.DEBUG, 'put results to map for aggregation', 2) {
            eventResults.each { EventResult eventResult ->
                selectedMeasurands.each { SelectedMeasurand selectedMeasurand ->
                    if (eventResult.cachedView == selectedMeasurand.cachedView) {
                        Double value = selectedMeasurand.getNormalizedValueFrom(eventResult)
                        if (isInBounds(value, selectedMeasurand, gtBoundary, ltBoundary)) {
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
                        OsmChartPoint chartPoint = new OsmChartPoint(time: key.millisStartOfInterval, csiAggregation: sum / countValues, countOfAggregatedResults: countValues, sourceURL: testsDetailsURL, testingAgent: null)
                        if (chartPoint.isValid())
                            highchartPointsForEachGraph[graphLabel] << chartPoint
                    }
                }
            }
        }
        return highchartPointsForEachGraph.sort()
    }

    private List<OsmChartGraph> setSpeakingGraphLabelsAndSort(Map<GraphLabel, List<OsmChartPoint>> highchartPointsForEachGraphOrigin) {

        String repeatedViewEnding = i18nService.msg("de.iteratec.isr.measurand.endingCached", "Cached", null)
        String firstViewEnding = i18nService.msg("de.iteratec.isr.measurand.endingUncached", "Uncached", null)

        List<OsmChartGraph> graphs = []

        Map<Serializable, JobGroup> jobGroupMap = [:]
        Map<Serializable, MeasuredEvent> measuredEventMap = [:]
        Map<Serializable, Location> locationMap = [:]
        highchartPointsForEachGraphOrigin.each { graphLabel, highChartPoints ->
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
                        String newGraphLabel = "${measurand}${HIGHCHART_LEGEND_DELIMITTER}${group.name}${HIGHCHART_LEGEND_DELIMITTER}" +
                                "${measuredEvent.name}${HIGHCHART_LEGEND_DELIMITTER}${location.uniqueIdentifierForServer == null ? location.location : location.uniqueIdentifierForServer}" +
                                "${HIGHCHART_LEGEND_DELIMITTER}${graphLabel.connectivity}"
                        graphs.add(new OsmChartGraph(
                                label: newGraphLabel,
                                measurandGroup: graphLabel.selectedMeasurand.getMeasurandGroup(),
                                points: highChartPoints))
                    } else {
                        graphs.add(new OsmChartGraph(
                                label: "${measurand}${HIGHCHART_LEGEND_DELIMITTER}${graphLabel.tag}",
                                measurandGroup: graphLabel.selectedMeasurand.getMeasurandGroup(),
                                points: highChartPoints))
                    }
                } else {
                    graphs.add(new OsmChartGraph(
                            label: "${measurand}${HIGHCHART_LEGEND_DELIMITTER}${graphLabel.tag}",
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
    public URL buildTestsDetailsURL(EventResult result) {
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


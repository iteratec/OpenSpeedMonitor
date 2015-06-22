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

import de.iteratec.osm.measurement.schedule.ConnectivityProfile

import static de.iteratec.osm.util.Constants.*

import org.codehaus.groovy.grails.web.mapping.LinkGenerator
import org.joda.time.DateTime

import de.iteratec.osm.report.chart.dao.AggregatorTypeDaoService
import de.iteratec.osm.report.chart.OsmChartGraph
import de.iteratec.osm.report.chart.OsmChartPoint
import de.iteratec.osm.report.chart.MeasuredValueUtilService
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.measurement.schedule.dao.JobGroupDaoService
import de.iteratec.osm.csi.Page
import de.iteratec.osm.measurement.schedule.dao.PageDaoService
import de.iteratec.osm.report.chart.AggregatorType
import de.iteratec.osm.report.chart.MeasurandGroup
import de.iteratec.osm.report.chart.MeasuredValue
import de.iteratec.osm.report.chart.MeasuredValueInterval
import de.iteratec.osm.result.dao.MeasuredEventDaoService
import de.iteratec.osm.result.dao.EventResultDaoService
import de.iteratec.osm.util.PerformanceLoggingService
import de.iteratec.osm.util.PerformanceLoggingService.IndentationDepth
import de.iteratec.osm.util.PerformanceLoggingService.LogLevel
import de.iteratec.osm.measurement.environment.Browser
import de.iteratec.osm.measurement.environment.dao.BrowserDaoService
import de.iteratec.osm.measurement.environment.Location
import de.iteratec.osm.measurement.environment.dao.LocationDaoService
import de.iteratec.osm.util.I18nService

/**
 * <p>
 * A utility service for the event result dashboard and related operations.
 * </p>
 */
public class EventResultDashboardService {

    BrowserDaoService browserDaoService
    JobGroupDaoService jobGroupDaoService
    PageDaoService pageDaoService
    LocationDaoService locationDaoService
    MeasuredEventDaoService measuredEventDaoService
    ResultMeasuredValueService resultMeasuredValueService
    I18nService i18nService
    MeasuredValueTagService measuredValueTagService
    JobResultService jobResultService
    EventResultDaoService eventResultDaoService
    MeasuredValueUtilService measuredValueUtilService
    PerformanceLoggingService performanceLoggingService
    AggregatorTypeDaoService aggregatorTypeDaoService

    /**
     * LabelSummary
     */
    private String labelSummary;

    String getLabelSummary() {
        return (labelSummary == null)? "" : labelSummary;
    }

    void setLabelSummary(String labelSummary) {
        this.labelSummary = labelSummary
    }
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
        return MeasuredEvent.findAll().sort(false, { it.name });
    }

    /**
     * Fetches all {@link Location}s from Database.
     *
     * <p>
     * 	Proxy for {@link LocationDaoService}
     * </p>
     *
     * @return all {@link Location} ordered by their label.
     */
    public List<Location> getAllLocations() {
        return locationDaoService.findAll().sort(false, { it.label });
    }

    /**
     * Fetches all {@link Browser}s from Database.
     *
     * <p>
     * 	Proxy for {@link BrowserDaoService}
     * </p>
     *
     * @return all {@link Browser} ordered by their name.
     */
    public List<Browser> getAllBrowser() {

        return browserDaoService.findAll().sort(false, { it.name });
    }

    /**
     * Fetches all {@link Page}s from Database.
     *
     * <p>
     * 	Proxy for {@link PageDaoService}
     * </p>
     *
     * @return all {@link Page} ordered by their name.
     */
    public List<Page> getAllPages() {
        return pageDaoService.findAll().sort(false, { it.name })
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
        return jobGroupDaoService.findAll().sort(false, { it.name });
    }

    /**
     * Returns a list of {@linkHighchartGraph}s for the highchart lib.
     *
     * @param startDate selected start date
     * @param endDate selected end date
     * @param interval selected interval
     * @param aggregators selected AggregatorTypes
     * @param queryParams
     * 		Query params for querying {@link EventResult}s.
     * @return List of {@linkHighchartGraph}s
     *
     * @throws IllegalArgumentException if an argument is not found or supported.
     *
     * @todo TODO mze-2013-09-12: Suggest to move to a generic HighchartFactoryService.
     */
    public List<OsmChartGraph> getEventResultDashboardHighchartGraphs(
            Date startDate, Date endDate, Integer interval, List<AggregatorType> aggregators, ErQueryParams queryParams) {

        Map<String, Number> gtValues = [:]
        Map<String, Number> ltValues = [:]
        aggregators.each { AggregatorType aggregator ->
            String associatedEventResultAttributeName = resultMeasuredValueService.getEventResultAttributeNameFromMeasurand(aggregator)
            if (aggregator.measurandGroup == MeasurandGroup.LOAD_TIMES) {
                if (queryParams.minLoadTimeInMillisecs) {
                    gtValues[associatedEventResultAttributeName] = queryParams.minLoadTimeInMillisecs
                }
                if (queryParams.maxLoadTimeInMillisecs) {
                    ltValues[associatedEventResultAttributeName] = queryParams.maxLoadTimeInMillisecs
                }
            } else if (aggregator.measurandGroup == MeasurandGroup.REQUEST_COUNTS) {
                if (queryParams.minRequestCount) {
                    gtValues[associatedEventResultAttributeName] = queryParams.minRequestCount
                }
                if (queryParams.maxRequestCount) {
                    ltValues[associatedEventResultAttributeName] = queryParams.maxRequestCount
                }
            } else if (aggregator.measurandGroup == MeasurandGroup.REQUEST_SIZES) {
                if (queryParams.minRequestSizeInBytes) {
                    gtValues[associatedEventResultAttributeName] = queryParams.minRequestSizeInBytes
                }
                if (queryParams.maxRequestSizeInBytes) {
                    ltValues[associatedEventResultAttributeName] = queryParams.maxRequestSizeInBytes
                }
            }
        }

        Collection<EventResult> eventResults
        performanceLoggingService.logExecutionTime(LogLevel.DEBUG, 'getting event-results', IndentationDepth.ONE) {
            eventResults = eventResultDaoService.getLimitedMedianEventResultsBy(
                    startDate, endDate, [CachedView.CACHED, CachedView.UNCACHED] as Set, queryParams, gtValues, ltValues)
        }

        return calculateResultMap(eventResults, aggregators, interval)

    }

    /**
     * <p>
     * Transforms given eventResults to a list of {@link de.iteratec.osm.report.chart.OsmChartGraph}s respective given measurands (aggregators) and interval.
     * If interval is not {@link MeasuredValueInterval#RAW} the values for the measurands will be aggregated respective interval.
     * </p>
     *
     * @param eventResults
     * @param aggregators
     * @param interval
     * @return
     */
    private List<OsmChartGraph> calculateResultMap(Collection<EventResult> eventResults, List<AggregatorType> aggregators, Integer interval) {
        def calculatedResultMap
        performanceLoggingService.logExecutionTime(LogLevel.DEBUG, 'getting result-map', IndentationDepth.ONE) {
            if (interval == MeasuredValueInterval.RAW) {
                calculatedResultMap = calculateResultMapForRawData(aggregators, eventResults)
            } else {
                calculatedResultMap = calculateResultMapForAggregatedData(aggregators, eventResults, interval)
            }
        }
        List<OsmChartGraph> graphs = []
        performanceLoggingService.logExecutionTime(LogLevel.DEBUG, 'set speaking graph labels and sorting', IndentationDepth.ONE) {
            graphs = setSpeakingGraphLabelsAndSort(calculatedResultMap)
            graphs = summarizeGraphs(graphs)
        }
        return graphs
    }

    private List<OsmChartGraph> summarizeGraphs(List<OsmChartGraph> graphs) {

        def summarizedLabelParts = [];

        if (graphs.size > 1) {

            def graph = graphs.get(0);
            def temp = graph.label.tokenize(HIGHCHART_LEGEND_DELIMITTER.trim());

            def labelParts = [
                    [ key: 'Messwert', value: temp[0] ]
            ]

            if(temp.size() == 5) {
                labelParts.add([ key: 'Gruppe', value: temp[1] ]);
                labelParts.add([ key: 'Event', value: temp[2] ]);
                labelParts.add([ key: 'Location', value: temp[3] ]);
                labelParts.add([ key: 'Connectivity', value: temp[4] ]);
            }else if(temp.size() == 2) {
                labelParts.add([ key: 'Identifier', value: temp[1] ]);
            }

            summarizedLabelParts = labelParts.findAll { part ->
                graphs.every { it ->
                    it.label.contains(part.value.trim());
                }
            }

            graphs.every { it ->
                summarizedLabelParts.each { part ->
                    it.label = (it.label - part.value.trim());
                }
                it.label = it.label.replaceAll("[|]\\s+[|]", "|");
                it.label = it.label.replaceFirst("^\\s+[|]\\s+", "");
            }

            String summary = "";
            summarizedLabelParts.each { part ->
                String labelNewPart = "<b>" + part.key + "</b>: " + part.value;
                summary == "" ? (summary = labelNewPart) : (summary += " | " + labelNewPart);
            }
            setLabelSummary(summary)
        }

        return graphs;
    }

    private Map<String, List<OsmChartPoint>> calculateResultMapForRawData(List<AggregatorType> aggregators, Collection<EventResult> eventResults) {

        Map<String, List<OsmChartPoint>> highchartPointsForEachGraph = [:].withDefault { [] }

        aggregators.each { AggregatorType aggregator ->

            CachedView aggregatorTypeCachedView = resultMeasuredValueService.getAggregatorTypeCachedViewType(aggregator)

            eventResults.each { EventResult eventResult ->

                String connectivity = eventResult.connectivityProfile != null ? eventResult.connectivityProfile.name : eventResult.customConnectivityName;

                URL testsDetailsURL = eventResult.getTestDetailsWaterfallURL()
                if (!testsDetailsURL) {
                    testsDetailsURL = this.buildTestsDetailsURL(eventResult)
                }

                if (isCachedViewEqualToAggregatorTypesView(eventResult, aggregatorTypeCachedView)) {
                    Double value = resultMeasuredValueService.getEventResultPropertyForCalculation(aggregator, eventResult)
                    if (value != null) {
                        String graphLabel = "${aggregator.name}${UNIQUE_STRING_DELIMITTER}${eventResult.tag}${UNIQUE_STRING_DELIMITTER}${connectivity}"
                        OsmChartPoint chartPoint = new OsmChartPoint(time: eventResult.getJobResultDate().getTime(), measuredValue: value, countOfAggregatedResults: 1, sourceURL: testsDetailsURL, testingAgent: eventResult.testAgent)
                        if (chartPoint.isValid())
                            highchartPointsForEachGraph[graphLabel].add(chartPoint)
                    }
                }
            }
        }
        return highchartPointsForEachGraph
    }

    private Map<String, List<OsmChartPoint>> calculateResultMapForAggregatedData(List<AggregatorType> aggregators, Collection<EventResult> eventResults, Integer interval) {

        Map<String, List<OsmChartPoint>> highchartPointsForEachGraph = [:].withDefault { [] }
        Map<String, List<Double>> eventResultsToAggregate = [:].withDefault { [] }

        performanceLoggingService.logExecutionTime(LogLevel.DEBUG, 'getting lookup-maps', IndentationDepth.TWO) {
            Map<Long, JobGroup> jobGroupMap = jobGroupDaoService.getIdToObjectMap()
            Map<Long, Page> pageMap = pageDaoService.getIdToObjectMap()
            Map<Long, MeasuredEvent> measuredEventMap = measuredEventDaoService.getIdToObjectMap()
            Map<Long, Browser> browserMap = browserDaoService.getIdToObjectMap()
            Map<Long, Location> locationMap = locationDaoService.getIdToObjectMap()
        }

        performanceLoggingService.logExecutionTime(LogLevel.DEBUG, 'put results to map for aggregation', IndentationDepth.TWO) {
            eventResults.each { EventResult eventResult ->
                aggregators.each { AggregatorType aggregator ->
                    if (isCachedViewEqualToAggregatorTypesView(eventResult, resultMeasuredValueService.getAggregatorTypeCachedViewType(aggregator))) {
                        Double value = resultMeasuredValueService.getEventResultPropertyForCalculation(aggregator, eventResult)
                        if (value != null) {
                            Long millisStartOfInterval = measuredValueUtilService.resetToStartOfActualInterval(new DateTime(eventResult.jobResultDate), interval).getMillis()
                            eventResultsToAggregate["${aggregator.name}${UNIQUE_STRING_DELIMITTER}${eventResult.tag}${UNIQUE_STRING_DELIMITTER}${millisStartOfInterval}"] << value
                        }
                    }
                }
            }
        }

        Map<String, AggregatorType> aggregatorTypeMap
        performanceLoggingService.logExecutionTime(LogLevel.DEBUG, 'get aggr-type-lookup-map', IndentationDepth.TWO) {
            aggregatorTypeMap = aggregatorTypeDaoService.getNameToObjectMap()
        }

        performanceLoggingService.logExecutionTime(LogLevel.DEBUG, 'iterate over aggregation-map', IndentationDepth.TWO) {
            URL testsDetailsURL
            Double sum = 0
            Integer countValues = 0
            eventResultsToAggregate.each { key, value ->

                List tokenized
                Long millisStartOfInterval
                AggregatorType aggregator

                performanceLoggingService.logExecutionTime(LogLevel.DEBUG, 'tokenize', IndentationDepth.THREE) {
                    performanceLoggingService.logExecutionTime(LogLevel.DEBUG, 'inner tokenize', IndentationDepth.FOUR) {
                        tokenized = key.tokenize(UNIQUE_STRING_DELIMITTER)
                    }
                    performanceLoggingService.logExecutionTime(LogLevel.DEBUG, 'Long.valueOf()', IndentationDepth.FOUR) {
                        millisStartOfInterval = Long.valueOf(tokenized[2])
                    }
                    performanceLoggingService.logExecutionTime(LogLevel.DEBUG, 'getting Aggregator from db', IndentationDepth.FOUR) {
                        aggregator = aggregatorTypeMap[tokenized[0]]
                    }
                }
                performanceLoggingService.logExecutionTime(LogLevel.DEBUG, 'buildTestsDetailsURL', IndentationDepth.THREE) {
                    testsDetailsURL = buildTestsDetailsURL(tokenized[1], aggregator, millisStartOfInterval, interval, value.size())
                }

                performanceLoggingService.logExecutionTime(LogLevel.DEBUG, 'calculate value and create OsmChartPoint', IndentationDepth.THREE) {
                    String graphLabel = "${tokenized[0]}${UNIQUE_STRING_DELIMITTER}${tokenized[1]}"
                    countValues = value.size()
                    if (countValues > 0) {
                        sum = 0
                        value.each { singleValue -> sum += singleValue }
                        OsmChartPoint chartPoint = new OsmChartPoint(time: millisStartOfInterval, measuredValue: sum / countValues, countOfAggregatedResults: countValues, sourceURL: testsDetailsURL, testingAgent: null)
                        if (chartPoint.isValid())
                            highchartPointsForEachGraph[graphLabel] << chartPoint
                    }
                }
            }
        }
        return highchartPointsForEachGraph.sort()
    }

    private List<OsmChartGraph> setSpeakingGraphLabelsAndSort(Map<String, List<OsmChartPoint>> highchartPointsForEachGraphOrigin) {

        String firstViewEnding = i18nService.msg("de.iteratec.isr.measurand.endingCached", "", null);
        String repeatedViewEnding = i18nService.msg("de.iteratec.isr.measurand.endingUncached", "", null);

        List<OsmChartGraph> graphs = []

        Map<Serializable, JobGroup> jobGroupMap = [:]
        Map<Serializable, MeasuredEvent> measuredEventMap = [:]
        Map<Serializable, Location> locationMap = [:]

        highchartPointsForEachGraphOrigin.each { graphLabel, highChartPoints ->
            List<String> tokenizedGraphLabel = graphLabel.tokenize(UNIQUE_STRING_DELIMITTER)
            if (tokenizedGraphLabel.size() != 3) {
                throw new IllegalArgumentException("The graph-label should consist of three parts: AggregatorType and tag. This is no correct graph-label: ${graphLabel}")
            }
            AggregatorType aggregator = AggregatorType.findByName(tokenizedGraphLabel[0])
            if (!aggregator) {
                throw new IllegalArgumentException("First part of graph-label should be the name of AggregatorType. This is no correct aggregator-name: ${tokenizedGraphLabel[0]}")
            }
            String connectivity = tokenizedGraphLabel[2]
            if(!connectivity) {
                throw new IllegalArgumentException("Thrid part of graph-label should be the the connectivity. This is no correct connectivity: ${tokenizedGraphLabel[2]}")
            }

            String measurand = i18nService.msg("de.iteratec.isr.measurand.${tokenizedGraphLabel[0].replace('Uncached', '').replace('Cached', '')}", tokenizedGraphLabel[0], null)

            if (tokenizedGraphLabel[0].endsWith("Uncached")) {
                if (!repeatedViewEnding.isEmpty()) {
                    measurand = repeatedViewEnding + " " + measurand;
                }
            } else {
                if (!firstViewEnding.isEmpty()) {
                    measurand = firstViewEnding + " " + measurand;
                }
            }

            String tag = tokenizedGraphLabel[1]
            if (tag) {
                Long jobGroupId = Long.valueOf(measuredValueTagService.findJobGroupIdOfHourlyEventTag(tag))
                JobGroup group = jobGroupMap[jobGroupId] ?: JobGroup.get(jobGroupId)
                Long eventId = Long.valueOf(measuredValueTagService.findMeasuredEventIdOfHourlyEventTag(tag))
                MeasuredEvent measuredEvent = measuredEventMap[eventId] ?: MeasuredEvent.get(eventId)
                Long locationId = Long.valueOf(measuredValueTagService.findLocationIdOfHourlyEventTag(tag))
                Location location = locationMap[locationId] ?: Location.get(locationId)

                if (group && measuredEvent && location) {
                    String newGraphLabel = "${measurand}${HIGHCHART_LEGEND_DELIMITTER}${group.name}${HIGHCHART_LEGEND_DELIMITTER}" +
                            "${measuredEvent.name}${HIGHCHART_LEGEND_DELIMITTER}${location.uniqueIdentifierForServer == null ? location.location : location.uniqueIdentifierForServer}" +
                            "${HIGHCHART_LEGEND_DELIMITTER}${connectivity}"
                    graphs.add(new OsmChartGraph(
                            label: newGraphLabel,
                            measurandGroup: aggregator.measurandGroup,
                            points: highChartPoints))
                } else {
                    graphs.add(new OsmChartGraph(
                            label: "${measurand}${HIGHCHART_LEGEND_DELIMITTER}${tokenizedGraphLabel[1]}",
                            measurandGroup: aggregator.measurandGroup,
                            points: highChartPoints))
                }
            } else {
                graphs.add(new OsmChartGraph(
                        label: "${measurand}${HIGHCHART_LEGEND_DELIMITTER}${tokenizedGraphLabel[1]}",
                        measurandGroup: aggregator.measurandGroup,
                        points: highChartPoints))
            }
        }
        return graphs.sort()
    }

    /**
     * <p>
     * Builds up an URL where details to the specified {@link MeasuredValue}
     * are available if possible.
     * </p>
     *
     * @param mv
     *         The measured value for which an URL should be build;
     *         not <code>null</code>.
     * @return The created URL or <code>null</code> if not possible to
     *         build up an URL.
     */
    public URL tryToBuildTestsDetailsURL(MeasuredValue mv) {
        URL result = null;
        List<Long> eventResultIds = mv.resultIdsAsList;

        if (!eventResultIds.isEmpty()) {
            String testsDetaialsURLAsString = grailsLinkGenerator.link([
                    'controller': 'highchartPointDetails',
                    'action'    : 'listAggregatedResults',
                    'absolute'  : true,
                    'params'    : [
                            'measuredValueId'                        : String.valueOf(mv.id),
                            'lastKnownCountOfAggregatedResultsOrNull': String.valueOf(eventResultIds.size())
                    ]
            ]);
            result = testsDetaialsURLAsString ? new URL(testsDetaialsURLAsString) : null;
        }

        return result;
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
            ]);
            resultUrl = testsDetailsURLAsString ? new URL(testsDetailsURLAsString) : null;
        }

        return resultUrl;
    }

    public URL buildTestsDetailsURL(String tag, AggregatorType aggregatorType, Long millisFrom, Integer intervalInMinutes, Integer lastKnownCountOfAggregatedResults) {
        URL result = null


        if (tag && aggregatorType) {

            String testsDetailsURLAsString = grailsLinkGenerator.link([
                    'controller': 'highchartPointDetails',
                    'action'    : 'listAggregatedResultsByQueryParams',
                    'absolute'  : true,
                    'params'    : [
                            'from'                                   : String.valueOf(millisFrom),
                            'to'                                     : String.valueOf(millisFrom + intervalInMinutes * 60 * 1000),
                            'tag'                                    : tag,
                            'aggregatorTypeNameOrNull'               : aggregatorType.isCachedCriteriaApplicable() ? aggregatorType.getName() : '',
                            'lastKnownCountOfAggregatedResultsOrNull': String.valueOf(lastKnownCountOfAggregatedResults)
                    ]
            ]);
            result = testsDetailsURLAsString ? new URL(testsDetailsURLAsString) : null;

        }

        return result;
    }

    private TreeMap addToPointMap(Map resultMap, String resultName, Long timeStamp, Integer value) {
        Map pointMap = resultMap.get(resultName)
        if (pointMap == null) {
            pointMap = new TreeMap<Long, Double>();
        }
        pointMap.put(timeStamp, value)
        return pointMap
    }

    private boolean isEventResultMachingQueryParams(EventResult eventResult, MvQueryParams queryParams) {
        boolean eins = (queryParams.getMeasuredEventIds().contains(eventResult.measuredEvent.id) || queryParams.getMeasuredEventIds().isEmpty());
        boolean zwei = (queryParams.getPageIds().contains(eventResult.measuredEvent.testedPage.id) || queryParams.getPageIds().isEmpty());
        return eins && zwei;
    }

    private boolean isCachedViewEqualToAggregatorTypesView(EventResult eventResult, CachedView aggregatorTypeCachedView) {
        return eventResult.cachedView.equals(aggregatorTypeCachedView)
    }
}

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

package de.iteratec.osm.csi

import de.iteratec.osm.measurement.environment.Location
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.report.chart.*
import de.iteratec.osm.result.EventResultDashboardService
import de.iteratec.osm.result.MeasuredEvent
import de.iteratec.osm.result.MvQueryParams
import grails.web.mapping.LinkGenerator
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.Interval
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter

import static de.iteratec.osm.util.Constants.HIGHCHART_LEGEND_DELIMITTER

/**
 * Provides methods to get {@link CsiAggregation}s from db and transform them into a chart processable format.
 */
class CustomerSatisfactionHighChartService {

    /**
     * The {@link DateTimeFormat} used for date-GET-params in created links.
     */
    static final DateTimeFormatter LINK_PARAMS_DATE_TIME_FORMAT = DateTimeFormat.forPattern("dd.MM.yyyy");
    static final Map<String, Integer> pageOrder = [
            "HP"  : 1,
            "MES" : 2,
            "SE"  : 3,
            "ADS" : 4,
            "WKBS": 5,
            "WK"  : 6]

    EventCsiAggregationService eventCsiAggregationService
    PageCsiAggregationService pageCsiAggregationService
    JobGroupCsiAggregationService jobGroupCsiAggregationService
    EventResultDashboardService eventResultDashboardService
    CsTargetGraphDaoService csTargetGraphDaoService
    CsiAggregationUtilService csiAggregationUtilService
    OsmChartProcessingService osmChartProcessingService
    CsiSystemCsiAggregationService csiSystemCsiAggregationService

    /**
     * The Grails engine to generate links.
     *
     * @see http://mrhaki.blogspot.ca/2012/01/grails-goodness-generate-links-outside.html
     */
    LinkGenerator grailsLinkGenerator

    Map<String, String> hourlyEventTagToGraphLabelMap = [:]
    def weeklyPageTagToGraphLabelMap = [:]

    /**
     * Get hourly Customer Satisfaction job {@link CsiAggregation}s as a List of{@link OsmChartGraph}s in format for highcharts-taglib.
     * see {@link CustomerSatisfactionHighChartService#convertToHighChartMap}
     *
     * @param fromDate The first date, inclusive, to find values for; not <code>null</code>.
     * @param toDate The last date, inclusive, to find values for; not <code>null</code>.
     * @param mvQueryParams The query parameters to find hourly values, not <code>null</code>.
     * @return not <code>null</code>.
     */
    OsmRickshawChart getCalculatedHourlyEventCsiAggregationsAsHighChartMap(Date fromDate, Date toDate, MvQueryParams mvQueryParams, List<CsiType> csiType) {
        List<CsiAggregation> csiValues = eventCsiAggregationService.getHourlyCsiAggregations(fromDate, toDate, mvQueryParams)
        return osmChartProcessingService.summarizeCsiGraphs(convertToHighchartGraphList(csiValues, csiType))
    }

    /**
     * <p>
     * Gets page CSI {@link CsiAggregation}s as a List of{@link OsmChartGraph}s in the highcharts
     * taglib format.
     * </p>
     *
     * @param timeFrame
     *         The time frame for which {@link CsiAggregation}s should be found. Both
     *         borders are included in search. This argument may not be
     *         <code>null</code>.
     * @param queryParams
     *         The {@linkplain MvQueryParams filter} to select relevant
     *         measured values, not <code>null</code>.
     * @param mvInterval
     * 		   The {@link CsiAggregationInterval} to be calculated, not <code>null</code>
     * @return not <code>null</code>.
     * @see CustomerSatisfactionHighChartService#convertToHighChartMap(List, AggregatorType)
     */
    OsmRickshawChart getCalculatedPageCsiAggregationsAsHighChartMap(Interval timeFrame, MvQueryParams queryParams, CsiAggregationInterval mvInterval, List<CsiType> csiType) {

        "Customer satisfaction index (CSI)"

        Date fromDate = timeFrame.getStart().toDate();
        Date toDate = timeFrame.getEnd().toDate();
        List<JobGroup> csiGroups = queryParams.jobGroupIds.collectNested { JobGroup.get(it) };
        List<Page> pages = queryParams.pageIds.collectNested { Page.get(it) };
        List<CsiAggregation> csiValues = pageCsiAggregationService.getOrCalculatePageCsiAggregations(fromDate, toDate, mvInterval, csiGroups, pages)
        log.debug("Number of CsiAggregations got from PageCsiAggregationService: ${csiValues.size()}")

        OsmRickshawChart chart = osmChartProcessingService.summarizeCsiGraphs(convertToHighchartGraphList(csiValues, csiType))
        log.debug("Number of ChartGraphs made from CsiAggregations: ${chart.osmChartGraphs.size()}")
        log.debug("Number of points in each ChartGraph made from CsiAggregations: ${chart.osmChartGraphs*.points.size()}")

        return chart
    }

    /**
     * <p>
     * Gets shop CSI {@link CsiAggregation}s as a list with {@link OsmChartGraph}s.
     * </p>
     *
     * @param timeFrame
     *         The time frame for which {@link CsiAggregation}s should be found. Both
     *         borders are included in search. This argument may not be
     *         <code>null</code>.
     * @param queryParams
     *         The {@linkplain MvQueryParams filter} to select relevant
     *         measured values, not <code>null</code>.
     * @return not <code>null</code>.
     * @see CustomerSatisfactionHighChartService#convertToHighChartMap(List, AggregatorType)
     */
    OsmRickshawChart getCalculatedShopCsiAggregationsAsHighChartMap(Interval timeFrame, CsiAggregationInterval interval, MvQueryParams queryParams, List<CsiType> csiType) {
        Date fromDate = timeFrame.getStart().toDate();
        Date toDate = timeFrame.getEnd().toDate();
        List<JobGroup> csiGroups = queryParams.jobGroupIds.collectNested { JobGroup.get(it) };
        List<CsiAggregation> csiValues = jobGroupCsiAggregationService.getOrCalculateShopCsiAggregations(fromDate, toDate, interval, csiGroups)

        return osmChartProcessingService.summarizeCsiGraphs(convertToHighchartGraphList(csiValues, csiType))
    }

    /**
     * <p>
     * Creates {@link OsmChartGraph}s from the specified
     * {@link Collection} of {@link CsiAggregation}s.
     * </p>
     *
     * @param csiValues
     *         The values from which the graph is to be calculated,
     *         not <code>null</code>.
     * @param csiType CsiType to create correct labels
     * @param getValue Closure to get the actual value. Expects one parameter CsiAggregation and should return a cs in percent
     * @return A list of graphs sorted ascending by {@link
     * OsmChartGraph # getLabel ( )}; never <code>null</code>.
     */
    private List<OsmChartGraph> convertToHighchartGraphListTemplate(Collection<CsiAggregation> csiValues, CsiType csiType, Closure getValue) {
        // Cache of already added graphs by tag
        Map<String, OsmChartGraph> tagToGraph = new HashMap<String, OsmChartGraph>();

        List<OsmChartGraph> graphs = new ArrayList<OsmChartGraph>();

        // start a new session for better performance
        CsiAggregation.withNewSession {

            for (CsiAggregation currentCsiAggregation : csiValues) {
                if (getValue(currentCsiAggregation) != null && getValue(currentCsiAggregation) >= 0) {

                    String valueTagToGraph = getCsiAggregationIdentifierForChart(currentCsiAggregation)

                    if (!tagToGraph.containsKey(valueTagToGraph)) {
                        OsmChartGraph graph = new OsmChartGraph();
                        graph.measurandGroup = MeasurandGroup.PERCENTAGES
                        graph.setLabel(getMapLabel(currentCsiAggregation, csiType));
                        tagToGraph.put(valueTagToGraph, graph);
                        graphs.add(graph);
                    }

                    OsmChartPoint chartPoint = new OsmChartPoint(
                            time: getHighchartCompatibleTimestampFrom(currentCsiAggregation.started),
                            csiAggregation: formatPercentage(getValue(currentCsiAggregation)),
                            countOfAggregatedResults: currentCsiAggregation.countUnderlyingEventResultsByWptDocComplete(),
                            sourceURL: getLinkFor(currentCsiAggregation, csiType),
                            testingAgent: null
                    )
                    tagToGraph[valueTagToGraph].getPoints().add(chartPoint)
                }
            }

        }

        return graphs;
    }

    private List<OsmChartGraph> convertToHighchartGraphListVisuallyCompleteBased(Collection<CsiAggregation> csiValues) {
        return convertToHighchartGraphListTemplate(csiValues, CsiType.VISUALLY_COMPLETE) { CsiAggregation it -> it.csByWptVisuallyCompleteInPercent }
    }

    private List<OsmChartGraph> convertToHighchartGraphListDocCompleteBased(Collection<CsiAggregation> csiValues) {
        return convertToHighchartGraphListTemplate(csiValues, CsiType.DOC_COMPLETE) { CsiAggregation it -> it.csByWptDocCompleteInPercent }
    }
    /**
     * <p>
     * Creates {@link OsmChartGraph}s from the specified
     * {@link Collection} of {@link CsiAggregation}s.
     * </p>
     *
     * @param csiValues
     *         The values from which the graph is to be calculated,
     *         not <code>null</code>.
     * @param csiType CsiType on which the cs is based
     * @return A list of graphs sorted ascending by {@link
     */
    private List<OsmChartGraph> convertToHighchartGraphList(Collection<CsiAggregation> csiValues, List<CsiType> csiType) {
        List<OsmChartGraph> graphs = []
        if (csiType.contains(CsiType.DOC_COMPLETE))
            graphs.addAll(convertToHighchartGraphListDocCompleteBased(csiValues));
        if (csiType.contains(CsiType.VISUALLY_COMPLETE))
            graphs.addAll(convertToHighchartGraphListVisuallyCompleteBased(csiValues))

        sortPointsInAllGraphsByTime(graphs)
        sortGraphsByLabel(graphs)
        return graphs
    }

    double formatPercentage(double value) {
        BigDecimal valueForRounding = new BigDecimal(value)
        return valueForRounding.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue()
    }

    void sortPointsInAllGraphsByTime(List<OsmChartGraph> graphs) {
        graphs.each { graph ->
            graph.getPoints().sort(true, { it.time })
        }
    }

    void sortGraphsByLabel(List<OsmChartGraph> graphs) {
        graphs.sort(true, { it.label })
    }

    /**
     * Creates an URL for a link for the {@link CsiAggregation} (MV) csiValue (to be used in diagrams). The URL links to the underlying data of csiValue:
     * <ul>
     * <li>If csiValue is a <b>weekly shop-MV: </b>Link to diagram-sight of daily shop-MV's of the respective week and shop/system.</li>
     * <li>If csiValue is a <b>daily shop-MV: </b>Link to diagram-sight of hourly measured step-MV's of the respective csiDay and shop/system.</li>
     * <li>If csiValue is a <b>weekly page-MV: </b>Link to diagram-sight of daily page-MV's of the respective week, shop/system and page.</li>
     * <li>If csiValue is a <b>daily page-MV: </b>Link to diagram-sight of hourly measured step-MV's of the respective csiDay, shop/system and page.</li>
     * <li>If csiValue is a <b>hourly measured step-MV: </b>Link to a list of the raw-data-results of the respective hour, shop/system, page and step.</li>
     * </ul>
     * @param csiValue
     * @return
     * @see https://seu.hh.iteratec.de:8444/browse/IT-381
     */
    private URL getLinkFor(CsiAggregation csiValue, CsiType csiType) {
        URL linkForPoint

        String aggregatorName = csiValue.aggregator.name

        if (aggregatorName.equals(AggregatorType.PAGE) ||
                aggregatorName.equals(AggregatorType.SHOP) ||
                aggregatorName.equals(AggregatorType.CSI_SYSTEM)
        ) {
            Map paramsToSend = getParamsForLink(csiValue)
            paramsToSend[CsiDashboardShowAllCommand.receiveControlnameFor(csiType)] = 'on'
            String testsDetailURLAsString = grailsLinkGenerator.link([
                    'controller': 'csiDashboard',
                    'action'    : 'showAll',
                    'absolute'  : true,
                    'params'    : paramsToSend
            ]);
            linkForPoint = testsDetailURLAsString ? new URL(testsDetailURLAsString) : null;
        } else if (aggregatorName.equals(AggregatorType.MEASURED_EVENT)) {
            linkForPoint = this.eventResultDashboardService.tryToBuildTestsDetailsURL(csiValue)
        }
        return linkForPoint
    }

    private Map getParamsForLink(CsiAggregation csiValue) {

        DateTime startOfInterval = new DateTime(csiValue.started)
        DateTime endOfInterval = csiAggregationUtilService.addOneInterval(startOfInterval, csiValue.interval.intervalInMinutes)
        Map paramsToSend = [
                'from'                     : LINK_PARAMS_DATE_TIME_FORMAT.print(startOfInterval),
                'fromHour'                 : '0:00',
                'to'                       : LINK_PARAMS_DATE_TIME_FORMAT.print(endOfInterval),
                'toHour'                   : '0:00',
                'selectedAllBrowsers'      : 'on',
                'selectedAllLocations'     : 'on',
                'selectedAllMeasuredEvents': 'on',
                '_action_showAll'          : 'Show'
        ]

        if (csiValue.aggregator.name.equals(AggregatorType.CSI_SYSTEM)) {

            if (csiValue.interval.intervalInMinutes == CsiAggregationInterval.WEEKLY) {
                paramsToSend['selectedCsiSystems'] = csiValue.csiSystem.ident()
                paramsToSend['aggrGroupAndInterval'] = CsiDashboardController.DAILY_AGGR_GROUP_SYSTEM
                paramsToSend['selectedTimeFrameInterval'] = "0"
            } else if (csiValue.interval.intervalInMinutes == CsiAggregationInterval.DAILY) {
                paramsToSend['to'] = LINK_PARAMS_DATE_TIME_FORMAT.print(startOfInterval)
                paramsToSend['toHour'] = '23:59'
                paramsToSend['aggrGroupAndInterval'] = CsiDashboardController.DAILY_AGGR_GROUP_SHOP
                paramsToSend['selectedFolder'] = csiValue.csiSystem.getAffectedJobGroups()*.ident()
                paramsToSend['selectedTimeFrameInterval'] = "0"
            }

        } else if (csiValue.aggregator.name.equals(AggregatorType.SHOP)) {

            if (csiValue.interval.intervalInMinutes == CsiAggregationInterval.WEEKLY) {
                paramsToSend['aggrGroupAndInterval'] = CsiDashboardController.DAILY_AGGR_GROUP_SHOP
                paramsToSend['selectedFolder'] = csiValue.jobGroup.ident()
                paramsToSend['selectedTimeFrameInterval'] = "0"
            } else if (csiValue.interval.intervalInMinutes == CsiAggregationInterval.DAILY) {
                paramsToSend['to'] = LINK_PARAMS_DATE_TIME_FORMAT.print(startOfInterval)
                paramsToSend['toHour'] = '23:59'
                paramsToSend['aggrGroupAndInterval'] = CsiDashboardController.DAILY_AGGR_GROUP_PAGE
                paramsToSend['selectedFolder'] = csiValue.jobGroup.ident()
                paramsToSend['selectedPages'] = Page.list()*.ident()
                paramsToSend['selectedTimeFrameInterval'] = "0"
            }

        } else if (csiValue.aggregator.name.equals(AggregatorType.PAGE)) {

            if (csiValue.interval.intervalInMinutes == CsiAggregationInterval.WEEKLY) {
                paramsToSend['aggrGroupAndInterval'] = CsiDashboardController.DAILY_AGGR_GROUP_PAGE
                paramsToSend['selectedFolder'] = csiValue.jobGroup.ident()
                paramsToSend['selectedPages'] = csiValue.page.ident()
                paramsToSend['selectedTimeFrameInterval'] = "0"
            } else if (csiValue.interval.intervalInMinutes == CsiAggregationInterval.DAILY) {
                paramsToSend['aggrGroupAndInterval'] = CsiDashboardController.HOURLY_MEASURED_EVENT
                paramsToSend['selectedFolder'] = csiValue.jobGroup.ident()
                paramsToSend['selectedPages'] = csiValue.page.ident()
                paramsToSend['selectedTimeFrameInterval'] = "0"
            }

        }
        return paramsToSend
    }

    /**
     * Get label for Map of {@link CustomerSatisfactionHighChartService#getOrCalculateCustomerSatisfactionCsiAggregationsAsHighChartMap}
     * for given {@link CsiAggregation} and {@link AggregatorType}
     *
     * @param mv
     * @param aggregator
     * @return Label for Map of {@link CustomerSatisfactionHighChartService#getOrCalculateCustomerSatisfactionCsiAggregationsAsHighChartMap}
     */
    private String getMapLabel(CsiAggregation mv, CsiType csiType) {
        String labelForValuesNotAssignable = 'n.a.'
        String csiTypeString = csiType.toString() + HIGHCHART_LEGEND_DELIMITTER
        String tag = getCsiAggregationIdentifierForChart(mv)
        switch (mv.aggregator.name) {
            case AggregatorType.MEASURED_EVENT:
                if (!hourlyEventTagToGraphLabelMap.containsKey(tag)) {

                    JobGroup group = mv.jobGroup
                    MeasuredEvent event = mv.measuredEvent
                    Location location = mv.location

                    //Removed Browser and Page See IT-153
                    String label = (group ? group.name : labelForValuesNotAssignable) + HIGHCHART_LEGEND_DELIMITTER;
                    label += (event ? event.name : labelForValuesNotAssignable) + HIGHCHART_LEGEND_DELIMITTER;
                    label += location ?
                            (location.uniqueIdentifierForServer == null ? location.location : location.uniqueIdentifierForServer) :
                            labelForValuesNotAssignable;

                    hourlyEventTagToGraphLabelMap.put(tag, label)

                }
                return csiTypeString + hourlyEventTagToGraphLabelMap[tag]
                break
            case AggregatorType.PAGE:
                if (!weeklyPageTagToGraphLabelMap.containsKey(tag)) {
                    JobGroup group = mv.jobGroup
                    Page page = mv.page
                    group && page ?
                            weeklyPageTagToGraphLabelMap.put(tag, "${group.name}${HIGHCHART_LEGEND_DELIMITTER}${page.name}") :
                            weeklyPageTagToGraphLabelMap.put(tag, labelForValuesNotAssignable)
                }
                return csiTypeString + weeklyPageTagToGraphLabelMap[tag]
                break
            case AggregatorType.SHOP:
                JobGroup group = mv.jobGroup
                return csiTypeString + (group ?
                        group.name :
                        labelForValuesNotAssignable)
                break
            case AggregatorType.CSI_SYSTEM:
                CsiSystem csiSystem = mv.csiSystem
                return csiTypeString + (csiSystem ? csiSystem.label : labelForValuesNotAssignable)
                break
        }
    }

    /**
     * <p>
     * Creates a Highchart-map containing the cs-relevant {@link CsTargetGraph}-points as stored in database.
     * </p>
     *
     * <p>
     * The result hast the following format:
     * <pre>
     * [
     *   label1: [highchartPoint(timestamp1:customerSatisfaction1), ..., highchartPoint(timestampN:customerSatisfactionN)],
     *   ...,
     *   labelN: [...]
     * ]
     * </pre>
     * </p>
     *
     * @param fromDate
     *         The relevant time frames start date, inclusive, not <code>null</code>.
     * @param toDate
     *         The relevant time frames end date, inclusive, not <code>null</code>.
     * @return A List of {@link OsmChartGraph} as described above, never <code>null</code>.
     */
    List<OsmChartGraph> getCsRelevantStaticGraphsAsResultMapForChart(DateTime fromDate, DateTime toDate) {

        List<OsmChartGraph> result = Collections.checkedList(new ArrayList<OsmChartGraph>(), OsmChartGraph.class);

        CsTargetGraph actualTargetGraph
        try {
            actualTargetGraph = csTargetGraphDaoService.getActualCsTargetGraph()
        } catch (NullPointerException npe) {
            log.info("No customer satisfaction target graph exist for actual locale.")
        }

        if (actualTargetGraph) {
            OsmChartPoint fromPoint = new OsmChartPoint(time: getHighchartCompatibleTimestampFrom(fromDate.toDate()), csiAggregation: (double) actualTargetGraph.getPercentOfDate(fromDate), countOfAggregatedResults: 1, sourceURL: null, testingAgent: null);
            OsmChartPoint toPoint = new OsmChartPoint(time: getHighchartCompatibleTimestampFrom(toDate.toDate()), csiAggregation: (double) actualTargetGraph.getPercentOfDate(toDate), countOfAggregatedResults: 1, sourceURL: null, testingAgent: null);

            OsmChartGraph graph = new OsmChartGraph();
            graph.measurandGroup = MeasurandGroup.PERCENTAGES
            graph.setLabel(actualTargetGraph.label);
            if (fromPoint.isValid())
                graph.getPoints().add(fromPoint);
            if (toPoint.isValid())
                graph.getPoints().add(toPoint);

            result.add(graph);
        }

        return result
    }

    private Long getHighchartCompatibleTimestampFrom(Date date) {
        return new DateTime(date, DateTimeZone.forID('MET')).getMillis()
    }

    /**
     * <p>
     * Gets shop CSI {@link CsiAggregation}s as a list with {@link OsmChartGraph}s.
     * </p>
     *
     * @param timeFrame
     *         The time frame for which {@link CsiAggregation}s should be found. Both
     *         borders are included in search. This argument may not be
     *         <code>null</code>.
     * @param queryParams
     *         The {@linkplain MvQueryParams filter} to select relevant
     *         measured values, not <code>null</code>.
     * @return not <code>null</code>.
     * @see CustomerSatisfactionHighChartService#convertToHighChartMap(List, AggregatorType)
     */
    OsmRickshawChart getCalculatedCsiSystemCsiAggregationsAsHighChartMap(Interval timeFrame, CsiAggregationInterval interval, Set<Long> selectedCsiSystems, List<CsiType> csiType) {
        Date fromDate = timeFrame.getStart().toDate();
        Date toDate = timeFrame.getEnd().toDate();
        List<CsiSystem> csiSystems = CsiSystem.getAll(selectedCsiSystems)
        List<CsiAggregation> csiValues = csiSystemCsiAggregationService.getOrCalculateCsiSystemCsiAggregations(fromDate, toDate, interval, csiSystems)

        return osmChartProcessingService.summarizeCsiGraphs(convertToHighchartGraphList(csiValues, csiType))
    }

    /**
     * builds an identifier for given csiAggregtaion
     * @param csiAggregation
     * @return
     */
    private String getCsiAggregationIdentifierForChart(CsiAggregation csiAggregation) {
        return "${csiAggregation.jobGroupId};${csiAggregation.measuredEventId};${csiAggregation.pageId};${csiAggregation.browserId};${csiAggregation.locationId};${csiAggregation.csiSystemId}"
    }
}

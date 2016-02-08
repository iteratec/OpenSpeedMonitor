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

import de.iteratec.osm.report.chart.OsmChartProcessingService
import de.iteratec.osm.report.chart.OsmRickshawChart

import static de.iteratec.osm.util.Constants.*
import de.iteratec.osm.measurement.environment.Browser
import de.iteratec.osm.measurement.environment.Location
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.report.chart.AggregatorType
import de.iteratec.osm.report.chart.MeasuredValue
import de.iteratec.osm.report.chart.MeasuredValueInterval
import de.iteratec.osm.report.chart.MeasuredValueUtilService
import de.iteratec.osm.report.chart.OsmChartGraph
import de.iteratec.osm.report.chart.OsmChartPoint
import de.iteratec.osm.result.EventResultDashboardService
import de.iteratec.osm.result.JobResultDaoService
import de.iteratec.osm.result.MeasuredEvent
import de.iteratec.osm.result.MeasuredValueTagService
import de.iteratec.osm.result.MvQueryParams

import org.codehaus.groovy.grails.web.mapping.LinkGenerator
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.Interval
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter


/**
 * Provides methods to get {@link MeasuredValue}s from db and transform them into a chart processable format.
 */
class CustomerSatisfactionHighChartService {

	/**
	 * The {@link DateTimeFormat} used for date-GET-params in created links.
	 */
	static final DateTimeFormatter LINK_PARAMS_DATE_TIME_FORMAT = DateTimeFormat.forPattern("dd.MM.yyyy");
	static final Map<String,Integer> pageOrder = [
		"HP":1,
		"MES":2,
		"SE":3,
		"ADS":4,
		"WKBS":5,
		"WK":6]

	EventMeasuredValueService eventMeasuredValueService
	PageMeasuredValueService pageMeasuredValueService
	ShopMeasuredValueService shopMeasuredValueService
	MeasuredValueTagService measuredValueTagService
	JobResultDaoService jobResultDaoService
	EventResultDashboardService eventResultDashboardService
	CsTargetGraphDaoService csTargetGraphDaoService
	MeasuredValueUtilService measuredValueUtilService
    OsmChartProcessingService osmChartProcessingService
	CsiSystemMeasuredValueService csiSystemMeasuredValueService

	/**
	 * The Grails engine to generate links.
	 *
	 * @see http://mrhaki.blogspot.ca/2012/01/grails-goodness-generate-links-outside.html
	 */
	LinkGenerator grailsLinkGenerator

	Map<String, String> hourlyEventTagToGraphLabelMap = [:]
	def weeklyPageTagToGraphLabelMap = [:]

	/**
	 * Get hourly Customer Satisfaction job {@link MeasuredValue}s as a List of{@link OsmChartGraph}s in format for highcharts-taglib.
	 * see {@link CustomerSatisfactionHighChartService#convertToHighChartMap}
	 *
	 * @param fromDate The first date, inclusive, to find values for; not <code>null</code>.
	 * @param toDate The last date, inclusive, to find values for; not <code>null</code>.
	 * @param mvQueryParams The query parameters to find hourly values, not <code>null</code>.
	 * @return not <code>null</code>.
	 */
	OsmRickshawChart getCalculatedHourlyEventMeasuredValuesAsHighChartMap(Date fromDate, Date toDate, MvQueryParams mvQueryParams) {
		List<OsmChartGraph> resultList = []

		List<MeasuredValue> csiValues = eventMeasuredValueService.getHourylMeasuredValues(fromDate, toDate, mvQueryParams)

		return osmChartProcessingService.summarizeCsiGraphs(convertToHighchartGraphList(csiValues))
	}

	/**
	 * <p>
	 * Gets page CSI {@link MeasuredValue}s as a List of{@link OsmChartGraph}s in the highcharts
	 * taglib format.
	 * </p>
	 *
	 * @param timeFrame
	 *         The time frame for which {@link MeasuredValue}s should be found. Both
	 *         borders are included in search. This argument may not be
	 *         <code>null</code>.
	 * @param queryParams
	 *         The {@linkplain MvQueryParams filter} to select relevant
	 *         measured values, not <code>null</code>.
	 * @param mvInterval
	 * 		   The {@link MeasuredValueInterval} to be calculated, not <code>null</code>
	 * @return not <code>null</code>.
	 * @see CustomerSatisfactionHighChartService#convertToHighChartMap(List, AggregatorType)
	 */
	OsmRickshawChart getCalculatedPageMeasuredValuesAsHighChartMap(Interval timeFrame, MvQueryParams queryParams, MeasuredValueInterval mvInterval) {

		"Customer satisfaction index (CSI)"

		Date fromDate = timeFrame.getStart().toDate();
		Date toDate = timeFrame.getEnd().toDate();
		List<JobGroup> csiGroups = queryParams.jobGroupIds.collectNested { JobGroup.get(it) };
		List<Page> pages = queryParams.pageIds.collectNested { Page.get(it) };
		List<MeasuredValue> csiValues = pageMeasuredValueService.getOrCalculatePageMeasuredValues(fromDate, toDate, mvInterval, csiGroups, pages)
        log.debug("Number of MeasuredValues got from PageMeasuredValueService: ${csiValues.size()}")

		OsmRickshawChart chart = osmChartProcessingService.summarizeCsiGraphs(convertToHighchartGraphList(csiValues))
        log.debug("Number of ChartGraphs made from MeasuredValues: ${chart.osmChartGraphs.size()}")
        log.debug("Number of points in each ChartGraph made from MeasuredValues: ${chart.osmChartGraphs*.points.size()}")

		return chart
	}

    /**
     * <p>
     * Gets shop CSI {@link MeasuredValue}s as a list with {@link OsmChartGraph}s.
     * </p>
     *
     * @param timeFrame
     *         The time frame for which {@link MeasuredValue}s should be found. Both
     *         borders are included in search. This argument may not be
     *         <code>null</code>.
     * @param queryParams
     *         The {@linkplain MvQueryParams filter} to select relevant
     *         measured values, not <code>null</code>.
     * @return not <code>null</code>.
     * @see CustomerSatisfactionHighChartService#convertToHighChartMap(List, AggregatorType)
     */
    OsmRickshawChart getCalculatedShopMeasuredValuesAsHighChartMap(Interval timeFrame, MeasuredValueInterval interval, MvQueryParams queryParams) {
        List<OsmChartGraph> resultList = []

        Date fromDate = timeFrame.getStart().toDate();
        Date toDate = timeFrame.getEnd().toDate();
        List<JobGroup> csiGroups = queryParams.jobGroupIds.collectNested { JobGroup.get(it) };
        List<MeasuredValue> csiValues = shopMeasuredValueService.getOrCalculateShopMeasuredValues(fromDate, toDate, interval, csiGroups)

        return osmChartProcessingService.summarizeCsiGraphs(convertToHighchartGraphList(csiValues))
    }

	/**
	 * <p>
	 * Creates {@link OsmChartGraph}s from the specified
	 * {@link Collection} of {@link MeasuredValue}s.
	 * </p>
	 *
	 * @param csiValues
	 *         The values from which the graph is to be calculated,
	 *         not <code>null</code>.
	 * @return A list of graphs sorted ascending by {@link
	 *         OsmChartGraph#getLabel()}; never <code>null</code>.
	 */
	private List<OsmChartGraph> convertToHighchartGraphList(Collection<MeasuredValue> csiValues) {
		// Cache of already added graphs by tag
		Map<String, OsmChartGraph> tagToGraph=new HashMap<String, OsmChartGraph>();

		List<OsmChartGraph> graphs=new ArrayList<OsmChartGraph>();

		for( MeasuredValue eachCsiVal : csiValues) {

			if (eachCsiVal.value) {

				if(!tagToGraph.containsKey(eachCsiVal.getTag())) {
					OsmChartGraph graph=new OsmChartGraph();
					graph.setLabel(getMapLabel(eachCsiVal));
					tagToGraph.put(eachCsiVal.getTag(), graph);
					graphs.add(graph);
				}

                OsmChartPoint chartPoint = new OsmChartPoint(
                        time: getHighchartCompatibleTimestampFrom(eachCsiVal.started),
                        measuredValue: formatPercentage(eachCsiVal),
                        countOfAggregatedResults: eachCsiVal.countResultIds(),
                        sourceURL: getLinkFor(eachCsiVal),
                        testingAgent: null
                )
                if(chartPoint.isValid())
                    tagToGraph[eachCsiVal.getTag()].getPoints().add(chartPoint)
			}
		}

        sortPointsInAllGraphsByTime(graphs)
        sortGraphsByLabel(graphs)

		return graphs;
	}

    double formatPercentage(value){
        BigDecimal valueForRounding = new BigDecimal(value.value)
        return valueForRounding.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue()
    }
    void sortPointsInAllGraphsByTime(List<OsmChartGraph> graphs){
        graphs.each{graph ->
            graph.getPoints().sort(true, { it.time })
        }
    }
    void sortGraphsByLabel(List<OsmChartGraph> graphs){
        graphs.sort(true, { it.label })
    }

	/**
	 * Creates an URL for a link for the {@link MeasuredValue} (MV) csiValue (to be used in diagrams). The URL links to the underlying data of csiValue:
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
	private URL getLinkFor(MeasuredValue csiValue){
		URL linkForPoint

		if (csiValue.aggregator.name.equals(AggregatorType.PAGE) || csiValue.aggregator.name.equals(AggregatorType.SHOP)) {
			def paramsToSend = getParamsForLink(csiValue)
			String testsDetailURLAsString = grailsLinkGenerator.link([
				'controller': 'csiDashboard',
				'action':'showAll',
				'absolute':true,
				'params':paramsToSend
			]);
			linkForPoint = testsDetailURLAsString ? new URL(testsDetailURLAsString) : null;
		}else if (csiValue.aggregator.name.equals(AggregatorType.MEASURED_EVENT)) {
			linkForPoint = this.eventResultDashboardService.tryToBuildTestsDetailsURL(csiValue)
		}
		return linkForPoint
	}

	private Map getParamsForLink(MeasuredValue csiValue){
		DateTime startOfInterval = new DateTime(csiValue.started)
		DateTime endOfInterval = measuredValueUtilService.addOneInterval(startOfInterval, csiValue.interval.intervalInMinutes)
		Map paramsToSend = [
			'from': LINK_PARAMS_DATE_TIME_FORMAT.print(startOfInterval),
			'fromHour': '0',
			'fromMinute': '0',
			'to': LINK_PARAMS_DATE_TIME_FORMAT.print(endOfInterval),
			'toHour': '0',
			'toMinute': '0',
			'selectedAllBrowsers': 'on',
			'selectedAllLocations': 'on',
			'selectedAllMeasuredEvents': 'on',
            '_action_showAll': 'Show'
		]
		if (csiValue.aggregator.name.equals(AggregatorType.SHOP)) {

			if (csiValue.interval.intervalInMinutes == MeasuredValueInterval.WEEKLY) {
				paramsToSend['aggrGroupAndInterval'] = CsiDashboardController.DAILY_AGGR_GROUP_SHOP
				paramsToSend['selectedFolder'] = measuredValueTagService.findJobGroupOfWeeklyShopTag(csiValue.tag).ident()
                paramsToSend['selectedTimeFrameInterval'] = "0"
			}else if (csiValue.interval.intervalInMinutes == MeasuredValueInterval.DAILY) {
				paramsToSend['aggrGroupAndInterval'] = AggregatorType.MEASURED_EVENT
				paramsToSend['selectedFolder'] = measuredValueTagService.findJobGroupOfWeeklyShopTag(csiValue.tag).ident()
				paramsToSend['selectedPages'] = Page.list()*.ident()
                paramsToSend['selectedTimeFrameInterval'] = "0"
			}

		}else if (csiValue.aggregator.name.equals(AggregatorType.PAGE)) {

			if (csiValue.interval.intervalInMinutes == MeasuredValueInterval.WEEKLY) {
				paramsToSend['aggrGroupAndInterval'] = CsiDashboardController.DAILY_AGGR_GROUP_PAGE
				paramsToSend['selectedFolder'] = measuredValueTagService.findJobGroupOfWeeklyPageTag(csiValue.tag).ident()
				paramsToSend['selectedPages'] = measuredValueTagService.findPageByPageTag(csiValue.tag).ident()
                paramsToSend['selectedTimeFrameInterval'] = "0"
			}else if (csiValue.interval.intervalInMinutes == MeasuredValueInterval.DAILY) {
				paramsToSend['aggrGroupAndInterval'] = AggregatorType.MEASURED_EVENT
				paramsToSend['selectedFolder'] = measuredValueTagService.findJobGroupOfWeeklyPageTag(csiValue.tag).ident()
				paramsToSend['selectedPages'] = measuredValueTagService.findPageByPageTag(csiValue.tag).ident()
                paramsToSend['selectedTimeFrameInterval'] = "0"
			}

		}
		return paramsToSend
	}

	/**
	 * Get label for Map of {@link CustomerSatisfactionHighChartService#getOrCalculateCustomerSatisfactionMeasuredValuesAsHighChartMap}
	 * for given {@link MeasuredValue} and {@link AggregatorType}
	 *
	 * @param mv
	 * @param aggregator
	 * @return Label for Map of {@link CustomerSatisfactionHighChartService#getOrCalculateCustomerSatisfactionMeasuredValuesAsHighChartMap}
	 */
	private String getMapLabel(MeasuredValue mv) {
		String labelForValuesNotAssignable = 'n.a.'
		switch (mv.aggregator.name) {
			case AggregatorType.MEASURED_EVENT:
				if (!hourlyEventTagToGraphLabelMap.containsKey(mv.tag)) {

					JobGroup group = measuredValueTagService.findJobGroupOfHourlyEventTag(mv.tag)
					Page page = measuredValueTagService.findPageOfHourlyEventTag(mv.tag)
					MeasuredEvent event = measuredValueTagService.findMeasuredEventOfHourlyEventTag(mv.tag)
					Browser browser = measuredValueTagService.findBrowserOfHourlyEventTag(mv.tag)
					Location location = measuredValueTagService.findLocationOfHourlyEventTag(mv.tag)

					//Removed Browser and Page See IT-153
					String label= (group?group.name:labelForValuesNotAssignable) + HIGHCHART_LEGEND_DELIMITTER;
//					label+= (page?page.name:labelForValuesNotAssignable) + UNIQUE_STRING_DELIMITTER;
					label+= (event?event.name:labelForValuesNotAssignable) + HIGHCHART_LEGEND_DELIMITTER;
//					label+= (browser?browser.name:labelForValuesNotAssignable) + UNIQUE_STRING_DELIMITTER;
					label+= location?
						(location.uniqueIdentifierForServer==null?location.location:location.uniqueIdentifierForServer):
						labelForValuesNotAssignable;

					hourlyEventTagToGraphLabelMap.put(mv.tag, label)

				}
				return hourlyEventTagToGraphLabelMap[mv.tag]
			break
			case AggregatorType.PAGE:
				if (!weeklyPageTagToGraphLabelMap.containsKey(mv.tag)) {
					JobGroup group = measuredValueTagService.findJobGroupOfWeeklyPageTag(mv.tag)
					Page page = measuredValueTagService.findPageByPageTag(mv.tag)
					group && page?
						weeklyPageTagToGraphLabelMap.put(mv.tag, "${group.name}${HIGHCHART_LEGEND_DELIMITTER}${page.name}"):
						weeklyPageTagToGraphLabelMap.put(mv.tag, labelForValuesNotAssignable)
				}
				return weeklyPageTagToGraphLabelMap[mv.tag]
			break
			case AggregatorType.SHOP:
				JobGroup group = measuredValueTagService.findJobGroupOfWeeklyShopTag(mv.tag)
				return group?
					group.name:
					labelForValuesNotAssignable
			break
			case AggregatorType.CSI_SYSTEM:
				CsiSystem csiSystem = mv.csiSystem
				return csiSystem? csiSystem.label: labelForValuesNotAssignable
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
	List<OsmChartGraph> getCsRelevantStaticGraphsAsResultMapForChart(DateTime fromDate, DateTime toDate){

		List<OsmChartGraph> result=Collections.checkedList(new ArrayList<OsmChartGraph>(), OsmChartGraph.class);

        CsTargetGraph actualTargetGraph
        try{
            actualTargetGraph = csTargetGraphDaoService.getActualCsTargetGraph()
        }catch(NullPointerException npe){
            log.info("No customer satisfaction target graph exist for actual locale.")
        }

		if (actualTargetGraph) {
			OsmChartPoint fromPoint = new OsmChartPoint(time: getHighchartCompatibleTimestampFrom(fromDate.toDate()), measuredValue: (double) actualTargetGraph.getPercentOfDate(fromDate), countOfAggregatedResults: 1, sourceURL: null, testingAgent: null);
			OsmChartPoint toPoint = new OsmChartPoint(time: getHighchartCompatibleTimestampFrom(toDate.toDate()), measuredValue: (double) actualTargetGraph.getPercentOfDate(toDate), countOfAggregatedResults: 1, sourceURL: null, testingAgent: null);

			OsmChartGraph graph=new OsmChartGraph();
			graph.setLabel(actualTargetGraph.label);
            if(fromPoint.isValid())
			    graph.getPoints().add(fromPoint);
            if(toPoint.isValid())
			    graph.getPoints().add(toPoint);

			result.add(graph);
		}

		return result
	}

	private Long getHighchartCompatibleTimestampFrom(Date date){
		return new DateTime(date, DateTimeZone.forID('MET')).getMillis()
	}


	/**
	 * <p>
	 * Gets shop CSI {@link MeasuredValue}s as a list with {@link OsmChartGraph}s.
	 * </p>
	 *
	 * @param timeFrame
	 *         The time frame for which {@link MeasuredValue}s should be found. Both
	 *         borders are included in search. This argument may not be
	 *         <code>null</code>.
	 * @param queryParams
	 *         The {@linkplain MvQueryParams filter} to select relevant
	 *         measured values, not <code>null</code>.
	 * @return not <code>null</code>.
	 * @see CustomerSatisfactionHighChartService#convertToHighChartMap(List, AggregatorType)
	 */
	OsmRickshawChart getCalculatedCsiSystemMeasuredValuesAsHighChartMap(Interval timeFrame, MeasuredValueInterval interval, Set<Long> selectedCsiSystems) {
		Date fromDate = timeFrame.getStart().toDate();
		Date toDate = timeFrame.getEnd().toDate();
		List<CsiSystem> csiSystems = CsiSystem.getAll(selectedCsiSystems)
		List<MeasuredValue> csiValues = csiSystemMeasuredValueService.getOrCalculateCsiSystemMeasuredValues(fromDate, toDate, interval, csiSystems)

		return osmChartProcessingService.summarizeCsiGraphs(convertToHighchartGraphList(csiValues))
	}
}

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

import static de.iteratec.osm.csi.Contract.requiresArgumentNotNull
import grails.validation.Validateable

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.regex.Pattern

import org.codehaus.groovy.grails.web.mapping.LinkGenerator
import org.joda.time.DateTime
import org.joda.time.Days
import org.joda.time.Duration
import org.joda.time.Interval
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import org.springframework.beans.propertyeditors.CustomDateEditor
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.servlet.support.RequestContextUtils
import org.supercsv.encoder.DefaultCsvEncoder
import org.supercsv.io.CsvListWriter
import org.supercsv.prefs.CsvPreference

import de.iteratec.osm.csi.weighting.WeightFactor
import de.iteratec.osm.measurement.environment.Browser
import de.iteratec.osm.measurement.environment.Location
import de.iteratec.osm.measurement.environment.dao.BrowserDaoService
import de.iteratec.osm.measurement.environment.dao.LocationDaoService
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.measurement.schedule.dao.JobGroupDaoService
import de.iteratec.osm.measurement.schedule.dao.PageDaoService
import de.iteratec.osm.p13n.CookieBasedSettingsService
import de.iteratec.osm.report.chart.*
import de.iteratec.osm.report.chart.dao.AggregatorTypeDaoService
import de.iteratec.osm.result.EventResultService
import de.iteratec.osm.result.MeasuredEvent
import de.iteratec.osm.result.MvQueryParams
import de.iteratec.osm.result.dao.MeasuredEventDaoService
import de.iteratec.osm.util.*

//TODO: implement some tests for this controller

/**
 * <p>
 * The controller for the customer satisfaction index (CSI) dashboard.
 * </p>
 */
class CsiDashboardController {

	AggregatorTypeDaoService aggregatorTypeDaoService
	JobGroupDaoService jobGroupDaoService
	PageDaoService pageDaoService
	MeasuredEventDaoService measuredEventDaoService
	BrowserDaoService browserDaoService
	LocationDaoService locationDaoService
	I18nService i18nService

	EventResultService eventResultService
	CustomerSatisfactionWeightService customerSatisfactionWeightService
	CustomerSatisfactionHighChartService customerSatisfactionHighChartService
	CsiHelperService csiHelperService
	MeasuredValueUtilService measuredValueUtilService
	CookieBasedSettingsService cookieBasedSettingsService

	/**
	 * The Grails engine to generate links.
	 * 
	 * @see http://mrhaki.blogspot.ca/2012/01/grails-goodness-generate-links-outside.html
	 */
	LinkGenerator grailsLinkGenerator

	public final static String DATE_FORMAT_STRING_FOR_HIGH_CHART = 'dd.mm.yyyy'
	public final static String DATE_FORMAT_STRING = 'dd.MM.yyyy'
	private final static SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat(DATE_FORMAT_STRING)
	
	public static final String DAILY_AGGR_GROUP_PAGE = 'daily_page'
	public static final String DAILY_AGGR_GROUP_SHOP = 'daily_shop'
	
	String DATE_TIME_FORMAT_STRING = 'dd.MM.yyyy HH:mm:ss'
	public final static int MONDAY_WEEKSTART = 1
	public final static List<String> AGGREGATOR_GROUP_VALUES = [
		AggregatorType.MEASURED_EVENT,
		DAILY_AGGR_GROUP_PAGE, // TODO mze-2013-11-06: Dirty, constants from AggregatorType should be used. Similar like in IT-210.
		AggregatorType.PAGE,
		DAILY_AGGR_GROUP_SHOP, // TODO mze-2013-11-06: Dirty, constants from AggregatorType should be used. Similar like in IT-210.
		AggregatorType.SHOP
	]
	public final static List<String> AGGREGATOR_GROUP_LABELS = [
		'de.iteratec.isocsi.csi.per.measured_event',
		'de.iteratec.isocsi.csi.per.page.daily',
		'de.iteratec.isocsi.csi.per.page',
		'de.iteratec.isocsi.csi.per.csi.group.daily',
		'de.iteratec.isocsi.csi.per.csi.group',
	]

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
	private void redirectWith303(String actionNameToRedirectTo)
	{
		// There is a missing feature to do this:
		// http://jira.grails.org/browse/GRAILS-8829

		// Workaround based on:
		// http://fillinginthegaps.wordpress.com/2008/12/26/grails-301-moved-permanently-redirect/
		String uri = grailsLinkGenerator.link(action: actionNameToRedirectTo)
		response.setStatus(303)
		response.setHeader("Location", uri)
		render(status:303)
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
	 * @param actionNameToRedirectTo 
	 *        The Name of the action to redirect to;
	 *        not <code>null</code>.
	 * @param urlParams 
	 *        The parameters to add as query string; 
	 *        not <code>null</code>.
	 *
	 * @see <a href="http://tools.ietf.org/html/rfc2616#section-10.3.4"
	 *      >http://tools.ietf.org/html/rfc2616#section-10.3.4</a>
	 */
	private void redirectWith303(String actionNameToRedirectTo, Map urlParams)
	{
		// There is a missing feature to do this:
		// http://jira.grails.org/browse/GRAILS-8829

		// Workaround based on:
		// http://fillinginthegaps.wordpress.com/2008/12/26/grails-301-moved-permanently-redirect/
		Map paramsWithoutGrailsActionNameOfOldAction = urlParams.findAll({ Map.Entry m -> !m.getKey().toString().startsWith('_action') })
		String uri = grailsLinkGenerator.link(action: actionNameToRedirectTo, params: paramsWithoutGrailsActionNameOfOldAction)
		response.setStatus(303)
		response.setHeader("Location", uri)
		render(status:303)
	}

	/**
	 * Redirects to {@link #showAll()}.
	 * 
	 * @return Nothing, redirects immediately.
	 */
	Map<String, Object> index() {
		redirectWith303('showAll')
	}

	/**
	 * Thats the view used to show CSI graphs with previous selection of date 
	 * range, groups and more filter criteria. This page is intended to be 
	 * used by admins and developers.
	 * 
	 * @param cmd The request / command send to this action, 
	 *            not <code>null</code>.
	 * @return A CSI model map to be used by the corresponding GSP, 
	 * 	       not <code>null</code> and never 
	 *         {@linkplain Map#isEmpty() empty}.
	 */
	Map<String, Object> showAll(ShowAllCommand cmd) {

		Map<String, Object> modelToRender = constructStaticViewDataOfShowAll()
		cmd.copyRequestDataToViewModelMap(modelToRender)
		
		// Validate command for errors if there was a non-empty, non-"only-language-change" request:
		if( !ControllerUtils.isEmptyRequest(params) ) {
			if(!cmd.validate() )
			{
				modelToRender.put('command', cmd)
			} else {
			
				boolean warnAboutLongProcessingTimeInsteadOfShowingData = false
				
				if(!cmd.overwriteWarningAboutLongProcessingTime)
				{
					int countOfSelectedEvents = cmd.selectedMeasuredEventIds.size()
					if(countOfSelectedEvents < 1)
					{
						// User checked: "Select all"
						countOfSelectedEvents = ((Collection)modelToRender.get('measuredEvents')).size()
					}
					
					int selectedAggregationIntervallInMintues = MeasuredValueInterval.HOURLY
					switch(cmd.aggrGroup)
					{
						case AggregatorType.PAGE:
						case DAILY_AGGR_GROUP_PAGE:
							selectedAggregationIntervallInMintues = MeasuredValueInterval.WEEKLY
							break
						case AggregatorType.SHOP:
						case DAILY_AGGR_GROUP_SHOP:
							selectedAggregationIntervallInMintues = MeasuredValueInterval.WEEKLY
							break
						default: // do nothing, take default.
							break
					}

					int countOfSelectedBrowser = cmd.selectedBrowsers.size()
					if( countOfSelectedBrowser < 1 )
					{
						countOfSelectedBrowser = ((List)modelToRender.get('browsers')).size()
					}
					
					warnAboutLongProcessingTimeInsteadOfShowingData = shouldWarnAboutLongProcessingTime(
							fixTimeFrame( cmd.getSelectedTimeFrame(), selectedAggregationIntervallInMintues ), 
							selectedAggregationIntervallInMintues,
							cmd.selectedFolder.size(),
							cmd.selectedPage.size(),
							countOfSelectedBrowser)
				}
			
				if( warnAboutLongProcessingTimeInsteadOfShowingData )
				{
					modelToRender.put('warnAboutLongProcessingTime', true)
				} else {
					fillWithAproximateMeasuredValueData(modelToRender, cmd, true)
				}
			}
		}

		return modelToRender
	}

	/**
	 * <p>
	 * Fills the specified map with approximate data based on {@linkplain 
	 * MeasuredValue measured values} correspond to the selection in 
	 * specified {@linkplain ShowAllCommand command object}.  
	 * </p>
	 * 
	 * @param modelToRender
	 *         The map to be filled. Previously added entries are overridden.
	 *         This map should not be <code>null</code>.
	 * @param cmd
	 *         The command with users selections, not <code>null</code>.
	 * @param withTargetGraph 
	 *         If <code>true</code> the CSI target graph will be added to 
	 *         the graphs in {@link modelToRender} else, if set to
	 *         <code>false</code> not.
	 */
	private void fillWithAproximateMeasuredValueData(Map<String, Object> modelToRender, ShowAllCommand cmd, boolean withTargetGraph)
	{
		// TODO Test this: Structure and data...

		requiresArgumentNotNull('modelToRender', modelToRender)
		requiresArgumentNotNull('cmd', cmd)

		Interval timeFrame = cmd.getSelectedTimeFrame()
		
		Set<JobGroup> csiGroups = jobGroupDaoService.findCSIGroups()
		Set<Long> csiGroupIds = csiGroups
		
		MvQueryParams measuredValuesQueryParams = cmd.createMvQueryParams()
		
		switch(cmd.aggrGroup)
		{
			case AggregatorType.PAGE:
				MeasuredValueInterval weeklyInterval=MeasuredValueInterval.findByIntervalInMinutes(MeasuredValueInterval.WEEKLY)
				fillWithPageValuesAsHighChartMap(modelToRender, timeFrame, weeklyInterval, measuredValuesQueryParams, withTargetGraph)
				break
			case DAILY_AGGR_GROUP_PAGE:
				MeasuredValueInterval dailyInterval=MeasuredValueInterval.findByIntervalInMinutes(MeasuredValueInterval.DAILY)
				fillWithPageValuesAsHighChartMap(modelToRender, timeFrame, dailyInterval, measuredValuesQueryParams, withTargetGraph)
				break
			case AggregatorType.SHOP:
				MeasuredValueInterval weeklyInterval=MeasuredValueInterval.findByIntervalInMinutes(MeasuredValueInterval.WEEKLY)
				fillWithShopValuesAsHighChartMap(modelToRender, timeFrame, weeklyInterval, measuredValuesQueryParams, withTargetGraph, false)
				break
			case DAILY_AGGR_GROUP_SHOP:
				MeasuredValueInterval dailyInterval=MeasuredValueInterval.findByIntervalInMinutes(MeasuredValueInterval.DAILY)
				fillWithShopValuesAsHighChartMap(modelToRender, timeFrame, dailyInterval, measuredValuesQueryParams, withTargetGraph, false)
				break
			default: // AggregatorType.MEASURED_EVENT
				fillWithHourlyValuesAsHighChartMap(modelToRender, timeFrame, measuredValuesQueryParams)
				break
		}
	}

	/**
	 * <p>
	 * Fills the view-model-map with page values. Calling this method
	 * should not be mixed with other operations than weekly page depended
	 * ones.
	 * </p>
	 *
	 * @param timeFrame
	 *         The time-frame for that data should be calculated,
	 *         not <code>null</code>.
	 * @param modelToRender
	 *         The map to be filled. Previously added entries are overridden.
	 *         This map should not be <code>null</code>.
	 * @param measuredValuesQueryParams 
	 *         The {@linkplain MvQueryParams filter} to select relevant 
	 *         measured values, not <code>null</code>.
	 */
	private void fillWithPageValuesAsHighChartMap(Map<String, Object> modelToRender, Interval timeFrame, MeasuredValueInterval interval, MvQueryParams measuredValuesQueryParams, boolean withTargetGraph)
	{
		// TODO Test this: Structure and data...

		Interval fixedTimeFrame = fixTimeFrame(timeFrame, interval.getIntervalInMinutes())
		
		
		List<OsmChartGraph> graphs = customerSatisfactionHighChartService.getCalculatedPageMeasuredValuesAsHighChartMap(fixedTimeFrame, measuredValuesQueryParams, interval)

		DateTime resetFromDate = fixedTimeFrame.getStart()
		DateTime resetToDate = fixedTimeFrame.getEnd()
		
		if( withTargetGraph ) 
		{
			graphs.addAll(customerSatisfactionHighChartService.getCsRelevantStaticGraphsAsResultMapForChart(
				resetFromDate.minusDays(1), resetToDate.plusDays(1)))
		}
		
		boolean includeCsTargetGraphs = false
		
		//add / remove 5 Minutes
		modelToRender.put('fromTimestampForHighChart', (resetFromDate.toDate().getTime() - 300000))
		modelToRender.put('toTimestampForHighChart', (resetToDate.toDate().getTime() + 300000))
		
		modelToRender.put('wptCustomerSatisfactionValues', graphs)
		modelToRender.put('wptCustomerSatisfactionValuesForTable', formatForTable(graphs, includeCsTargetGraphs))

		modelToRender.put('markerShouldBeEnabled', true)
	}

	/**
	 * <p>
	 * Fills the view-model-map with hourly measured values. Calling this 
	 * method should not be mixed with other operations than hourly depended
	 * ones.
	 * </p>
	 *
	 * @param timeFrame
	 *         The time-frame for that data should be calculated,
	 *         not <code>null</code>.
	 * @param timeFrame
	 *         The time frame values to be delivered for, 
	 *         not <code>null</code>.
	 * @param queryParams 
	 *         The query parameters to find corresponding {@linkplain 
	 *         MeasuredValue measured vales}.
	 * @param modelToRender
	 *         The map to be filled. Previously added entries are overridden.
	 *         This map should not be <code>null</code>.
	 */
	private void fillWithHourlyValuesAsHighChartMap(Map<String, Object> modelToRender, Interval timeFrame, MvQueryParams queryParams)
	{
		// TODO Test this: Structure and data...

		Interval fixedTimeFrame = fixTimeFrame(timeFrame, MeasuredValueInterval.HOURLY)
		
		List<OsmChartGraph> graphs = customerSatisfactionHighChartService.getCalculatedHourlyEventMeasuredValuesAsHighChartMap(
				fixedTimeFrame.getStart().toDate(), fixedTimeFrame.getEnd().toDate(), queryParams)

		DateTime resetFromDate = fixedTimeFrame.getStart()
		DateTime resetToDate = fixedTimeFrame.getEnd()

//		csiValueMap.putAll(customerSatisfactionHighChartService.getCsRelevantStaticGraphsAsResultMapForChart(
//			resetFromDate.minusDays(1), resetToDate.plusDays(1)))
		
		boolean includeCsTargetGraphs = true
		modelToRender.put('fromTimestampForHighChart', resetFromDate.toDate().getTime())
		modelToRender.put('toTimestampForHighChart', resetToDate.toDate().getTime())
		modelToRender.put('wptCustomerSatisfactionValues', graphs)
		modelToRender.put('wptCustomerSatisfactionValuesForTable', formatForTable(graphs, includeCsTargetGraphs))

		modelToRender.put('markerShouldBeEnabled', false)
	}

	/**
	 * <p>
	 * Fills the view-model-map with shop values. Calling this method 
	 * should not be mixed with other operations than weekly shop depended 
	 * ones. 
	 * </p>
	 * 
	 * @param modelToRender 
	 *         The map to be filled. Previously added entries are overridden. 
	 *         This map should not be <code>null</code>.
	 * @param timeFrame
	 *         The time-frame for that data should be calculated, 
	 *         not <code>null</code>.
	 * @param interval 
	 *         The interval of measured values to include in calculation and 
	 *         used for "fixing" the time-frame boundaries to find all measured 
	 *         values in this interval, not <code>null</code>. 
	 * @param measuredValuesQueryParams 
	 *         The {@linkplain MvQueryParams filter} to select relevant 
	 *         measured values, not <code>null</code>.
	 *  @param withTargetGraph
	 *  		Whether or not to include {@link CsTargetGraph}s.
	 *  @param moveGraphsByOneWeek
	 *  		Whether or not to move all {@link OsmChartPoint}s to the end of their interval (in default-CSI-dashboard this is the default-behaviour).
	 */
	private void fillWithShopValuesAsHighChartMap(
		Map<String, Object> modelToRender, 
		Interval timeFrame, 
		MeasuredValueInterval interval, 
		MvQueryParams measuredValuesQueryParams, 
		boolean withTargetGraph, 
		boolean moveGraphsByOneWeek)
	{
		// TODO Test this: Structure and data...
		Interval fixedTimeFrame = fixTimeFrame(timeFrame, interval.getIntervalInMinutes())
		
		DateTime resetFromDate = fixedTimeFrame.getStart()
		DateTime resetToDate = fixedTimeFrame.getEnd()
		
		List<OsmChartGraph> graphs = customerSatisfactionHighChartService.getCalculatedShopMeasuredValuesAsHighChartMap(
				fixedTimeFrame, interval, measuredValuesQueryParams)

		if(moveGraphsByOneWeek==true) {
			moveDataPointsOneWeekForward(graphs)
			resetFromDate=resetFromDate.plusWeeks(1)
		}
			
		Integer oneDayOffset = Math.round(MeasuredValueInterval.DAILY)
		Integer rightOffsetToGuaranteeExportButtonRemainsClickable = oneDayOffset * 4
		DateTime resetFromDateWithOffsetChange = resetFromDate.minusMinutes(oneDayOffset)
		DateTime resetToDateWithOffsetChange = resetToDate.plusMinutes(rightOffsetToGuaranteeExportButtonRemainsClickable)

		if( withTargetGraph )
		{
			graphs.addAll(customerSatisfactionHighChartService.getCsRelevantStaticGraphsAsResultMapForChart(
				resetFromDateWithOffsetChange.minusDays(1), resetToDateWithOffsetChange.plusDays(1)))
		}
		
		boolean includeCsTargetGraphs = true
		modelToRender.put('fromTimestampForHighChart', resetFromDateWithOffsetChange.toDate().getTime())
		modelToRender.put('toTimestampForHighChart', resetToDateWithOffsetChange.toDate().getTime())
		modelToRender.put('wptCustomerSatisfactionValues', graphs)
		modelToRender.put('wptCustomerSatisfactionValuesForTable', formatForTable(graphs, includeCsTargetGraphs))

		modelToRender.put('markerShouldBeEnabled', true)
	}
	
	/**
	 * <p>
	 * Fills the view-model-map with weekly shop values. Calling this method
	 * should not be mixed with other operations than weekly shop depended
	 * ones.
	 * </p>
	 *
	 * @param timeFrame
	 *         The time-frame for that data should be calculated,
	 *         not <code>null</code>.
	 * @param modelToRender
	 *         The map to be filled. Previously added entries are overridden.
	 *         This map should not be <code>null</code>.
	 * @param measuredValuesQueryParams
	 *         The {@linkplain MvQueryParams filter} to select relevant
	 *         measured values, not <code>null</code>.
	 * @param moveGraphsByOneWeek
	 * 			if true: moves the graph by one week (CSI-Default-View)
	 */
	private void fillWithWeeklyShopValuesAsHighChartMap(
		Map<String, Object> modelToRender, Interval timeFrame, MvQueryParams measuredValuesQueryParams, boolean withTargetGraph, boolean moveGraphsByOneWeek)
	{
		MeasuredValueInterval weekly = MeasuredValueInterval.findByIntervalInMinutes(MeasuredValueInterval.WEEKLY)
		fillWithShopValuesAsHighChartMap(modelToRender, timeFrame, weekly, measuredValuesQueryParams, withTargetGraph, moveGraphsByOneWeek)
	}
	
	/**
	 * Fixes the specified time frame to fit interval range.
	 * 
	 * @return The fixed time frame, never <code>null</code>.
	 */
	private Interval fixTimeFrame(Interval timeFrameToFix, int intervalRangeInMinutes) {
		return measuredValueUtilService.fixTimeFrameToMatchIntervalRange(timeFrameToFix, intervalRangeInMinutes)
	}

	/**
	 * Thats a view showing a graph for static defined criteria for the 
	 * current weeks CSI data. This page is intended to be used by the
	 * management and marketing group. There is nothing changeable on 
	 * this page and no further selection are possible.
	 *  
	 * @return A CSI model map to be used by the corresponding GSP, 
	 * 	       not <code>null</code> and never 
	 *         {@linkplain Map#isEmpty() empty}.
	 */
	Map<String, Object> showDefault() {

		DateTime toDate = new DateTime() // now
		DateTime fromDate = toDate.minusMonths(3)

		Map<String, Object> modelToRender = constructStaticViewDataOfShowAll()
		Interval timeFrame = new Interval(fromDate, toDate)
		
		MvQueryParams queryParams = new MvQueryParams()
		
		List<String> namesOfCsiGroupsAndStaticGraphsToShow = [
			'live',
			i18nService.msg('de.iteratec.isocsi.targetcsi.label', 'Ziel-Kundenzufriedenheit')]
		Set<JobGroup> csiGroupsToShow = jobGroupDaoService.findCSIGroups().findAll{namesOfCsiGroupsAndStaticGraphsToShow.contains(it.name)}
		Set<Long> csiGroupIds = csiGroupsToShow.collect({it.id})
		queryParams.jobGroupIds.addAll(csiGroupIds)
		
		fillWithWeeklyShopValuesAsHighChartMap(modelToRender, timeFrame, queryParams, true, true)

		modelToRender.put('dateFormatString', DATE_FORMAT_STRING_FOR_HIGH_CHART)
		modelToRender.put('weekStart', MONDAY_WEEKSTART)
		modelToRender.put('from', fromDate)
		modelToRender.put('to', toDate)
		modelToRender.put('fromFormatted', SIMPLE_DATE_FORMAT.format(fromDate.toDate()))
		modelToRender.put('toFormatted', SIMPLE_DATE_FORMAT.format(toDate.toDate()))
		modelToRender.put('markerShouldBeEnabled', true)
		modelToRender.put('debug', params.debug?true:false)
		modelToRender.put('namesOfCsiGroupsAndStaticGraphsToShow', namesOfCsiGroupsAndStaticGraphsToShow)
		return modelToRender
	}
	
	/**
	 * This method moves every Point in the Highchart Graph one week forward.
	 * @param graphs
	 * @return
	 * @since IT-217
	 */
	private List<OsmChartGraph> moveDataPointsOneWeekForward(List<OsmChartGraph> graphs) {
			
		List<OsmChartGraph> movedGraphs=[]
		
		graphs.each {OsmChartGraph graph ->
			
			List<OsmChartPoint> oldList=graph.getPoints()
			graph.setPoints([])
			
			oldList.each {OsmChartPoint point ->
				
				DateTime time=new DateTime(point.time)
				time=time.plusWeeks(1)
				
				OsmChartPoint movedPoint=new OsmChartPoint(time.toDate().getTime(), point.measuredValue, point.countOfAggregatedResults, point.sourceURL)
				graph.getPoints().add(movedPoint)
			}
		}
		
		return graphs
	}
 
	/**
	 * The record separator of a CSV as described in Informational RFC 4180.
	 * 
	 * @see <a href="http://tools.ietf.org/html/rfc4180#section-2">http://tools.ietf.org/html/rfc4180#section-2</a> (2).
	 */
	@Deprecated
	private static final String CRLF = String.valueOf((char)13) + String.valueOf((char)10) 
	
	/**
	 * The {@link DateTimeFormat} used for CSV export and table view.
	 */
	private static final DateTimeFormatter CSV_TABLE_DATE_TIME_FORMAT = DateTimeFormat.forPattern("dd.MM.yyyy HH:mm:ss")
	
//	/**
//	 * The {@link NumberFormat} used to format CSI values for CSV export and table view.
//	 */
//	private static final NumberFormat CSV_TABLE_CSI_VALUE_FORMAT_GERMAN = NumberFormat.getNumberInstance(Locale.GERMAN);
//	
//	/**
//	 * The {@link NumberFormat} used to format CSI values for CSV export and table view.
//	 */
//	private static final NumberFormat CSV_TABLE_CSI_VALUE_FORMAT_DEFAULT = NumberFormat.getNumberInstance(Locale.US);
	
	/**
	 * <p>
	 * Creates a CSV based on the selection passed as {@link ShowAllCommand}.
	 * </p>
	 * 
	 * @param cmd 
	 *         The command with the users selections; 
	 *         not <code>null</code>.
	 * @return nothing, immediately renders a CSV to response' output stream.
	 * @see <a href="http://tools.ietf.org/html/rfc4180">http://tools.ietf.org/html/rfc4180</a>
	 */
	public Map<String, Object> csiValuesCsv(ShowAllCommand cmd) {
		
		Map<String, Object> modelToRender = new HashMap<String, Object>()
		
		if( request.queryString && cmd.validate() )
		{
			fillWithAproximateMeasuredValueData(modelToRender, cmd, false)
			cmd.copyRequestDataToViewModelMap(modelToRender)
		} else {		
			redirectWith303('showAll', params)
			return
		}
		
		String filename = modelToRender['aggrGroup'] + '_' + modelToRender['fromFormatted']  + '_to_' +  modelToRender['toFormatted'] + '.csv'
		
		response.setHeader('Content-disposition', 'attachment; filename=' + filename)
		response.setContentType("text/csv;header=present;charset=UTF-8")
		
		Writer responseWriter = new OutputStreamWriter(response.getOutputStream())
		
		List<OsmChartGraph> csiValues = modelToRender['wptCustomerSatisfactionValues']
		writeCSV(csiValues, responseWriter, RequestContextUtils.getLocale(request), false)
		
		response.getOutputStream().flush()
		response.sendError(200, 'OK')
		return null
	}

	/**
	 * <p>
	 * Converts the specified CSI values in the source map to a CSV 
	 * corresponding to RFC 4180 written to specified {@link Writer}.
	 * </p>
	 * 
	 * @param source 
	 *         The CSI values a List of OsmChartGraph, not <code>null</code>.
	 * @param target 
	 *         The {@link Writer} to write CSV to, 
	 *         not <code>null</code>.
	 * @param localeForNumberFormat 
	 *         The locale used to format the numeric values, 
	 *         not <code>null</code>. 
	 * @param repeatCSITargetValueColumns 
	 *         Should the target CSI values be repeated? For UI table this is
	 *         required due readability, for a download CSV this is overhead; 
	 *         <code>true</code> if should be repeated, <code>false</code> 
	 *         else.
	 * 
	 * @throws IOException if write on {@code target} failed.
	 */
	private static void writeCSV(List<OsmChartGraph> source, Writer target, Locale localeForNumberFormat, boolean repeatCSITargetValueColumns) throws IOException
	{
		NumberFormat csvCSIValueFormat = NumberFormat.getNumberInstance(localeForNumberFormat)
		
		// Sort graph points by time
		TreeMapOfTreeMaps<Long, String, OsmChartPoint> pointsByGraphByTime = new TreeMapOfTreeMaps<Long, String, OsmChartPoint>()
		for(OsmChartGraph eachCSIValueEntry : source)
		{
			for(OsmChartPoint eachPoint : eachCSIValueEntry.getPoints())
			{
				pointsByGraphByTime.getOrCreate(eachPoint.time).put(eachCSIValueEntry.getLabel(), eachPoint)
			}
		}
		
		CsvListWriter csvWriter = new CsvListWriter(
			target, 
			new CsvPreference.Builder(CsvPreference.EXCEL_NORTH_EUROPE_PREFERENCE).useEncoder(new DefaultCsvEncoder()).build()
		)
		
		// Create CSV header:
		List<String> csvHeader = new LinkedList<String>()
		csvHeader.add('Zeitpunkt') // TODO i18n?
		csvHeader.add('Ziel-CSI') // TODO i18n?

		List<String> graphLabelsInOrderOfHeader = new LinkedList<String>()
		
		for(OsmChartGraph eachGraph : source)
		{
			csvHeader.add(eachGraph.getLabel())
			if(repeatCSITargetValueColumns)
			{
				csvHeader.add('Ziel-CSI') // TODO i18n?
			}
			csvHeader.add('Delta') // TODO i18n?
			
			graphLabelsInOrderOfHeader.add(eachGraph.getLabel())
		}
		
		csvWriter.writeHeader(csvHeader.toArray(new String[csvHeader.size()]))
		
		// The target graph:
		CsTargetGraph targetGraph = CsTargetGraph.list().get(0) // TODO Move DB access to DAO.
		
		for(Map.Entry<Long, TreeMap<String, OsmChartPoint>> eachPointByGraphOfTime : pointsByGraphByTime)
		{
			List<String> row = new LinkedList<String>()
			
			DateTime time = new DateTime(eachPointByGraphOfTime.getKey())
			double targetValue = targetGraph.getPercentOfDate(time)
			
			row.add(CSV_TABLE_DATE_TIME_FORMAT.print(time))
			
			row.add(csvCSIValueFormat.format(targetValue))
			
			for(String eachGraphLabel : graphLabelsInOrderOfHeader)
			{
				OsmChartPoint point = eachPointByGraphOfTime.getValue().get(eachGraphLabel)
				if( point != null )
				{
					row.add(csvCSIValueFormat.format(roundDouble(point.measuredValue)))
					if(repeatCSITargetValueColumns)
					{
						row.add(csvCSIValueFormat.format(roundDouble(targetValue)))
					}
					row.add(csvCSIValueFormat.format(roundDouble(point.measuredValue - targetValue)))
				} else {
					row.add("")
					if(repeatCSITargetValueColumns)
					{
						row.add("")
					}
					row.add("")
				}
			}
			
			csvWriter.writeRow(row)
		}
		
		csvWriter.flush()
	}
	
	/**
	 * Rounds a double according to CSV and Table requirements.
	 * 
	 * @param valueToRound 
	 *         The double value to round.
	 * @return The rounded value.
	 * @since IT-102
	 */
	static private double roundDouble(double valueToRound)
	{
		return new BigDecimal(valueToRound).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue()
	} 
	
	/**
	 * <p>
	 * Command of {@link CsiDashboardController#showAll(ShowAllCommand)} and 
	 * {@link CsiDashboardController#csiValuesCsv(ShowAllCommand)}.
	 * </p>
	 * 
	 * <p>
	 * None of the properties will be <code>null</code> for a valid instance.
	 * Some collections might be empty depending on the {@link #aggrGroup} 
	 * used.
	 * </p>
	 * 
	 * <p>
	 * <em>DEV-Note:</em> This command uses auto-binding for type {@link Date}.
	 * To make this possible, you need a custom {@link PropertyEditor}.
	 * See class {@link CustomDateEditorRegistrar} for details. If try an 
	 * auto-binding in a unit-test you need to register the class 
	 * CustomDateEditorRegistrar with a code-block like:
	 * <pre>
	 * defineBeans {
	 *     customPropertyEditorRegistrar(CustomDateEditorRegistrar)
	 * }
	 * </pre>
	 * in the set-up of your test. For productive use you need to add
	 * <pre>
	 * beans = {
	 *     customPropertyEditorRegistrar(CustomDateEditorRegistrar)
	 * }
	 * </pre>
	 * to the config file {@code grails-app/conf/spring/resources.groovy}
	 * </p>
	 * 
	 * @author mze
	 * @since IT-74
	 */
	@Validateable
	public static class ShowAllCommand {

		/**
		 * The selected start date (inclusive).
		 * 
		 * Please use {@link #getSelectedTimeFrame()}.
		 */
		Date from

		/**
		 * The selected end date (inclusive).
		 * 
		 * Please use {@link #getSelectedTimeFrame()}.
		 */
		Date to

		/**
		 * The selected start hour of date.
		 * 
		 * Please use {@link #getSelectedTimeFrame()}.
		 */
		String  fromHour

		/**
		 * The selected end hour of date.
		 * 
		 * Please use {@link #getSelectedTimeFrame()}.
		 */
		String toHour

		/**
		 * The name of the {@link AggregatorType}.
		 * 
		 * @see AggregatorType#getName()
		 * @see AggregatorType#MEASURED_STEP
		 * @see AggregatorType#PAGE
		 * @see AggregatorType#PAGE_AND_BROWSER
		 * @see AggregatorType#SHOP
		 */
		String aggrGroup

		/**
		 * The database IDs of the selected {@linkplain JobGroup CSI groups} 
		 * which are the systems measured for a CSI value 
		 * 
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
		 * The database IDs of the selected {@linkplain MeasuredEvent 
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
		 * If the user has been warned about a potentially long processing 
		 * time, did he overwrite the waring and really want to perform 
		 * the request?
		 * 
		 * A value of <code>true</code> indicates that overwrite, everything
		 * should be done as requested, <code>false</code> indicates that 
		 * the user hasn't been warned before, so there is no overwrite.
		 */
		Boolean overwriteWarningAboutLongProcessingTime
		
		/**
		 * Flag for manual debugging.
		 * Used for debugging highcharts-export-server, e.g.
		 */
		Boolean debug
		
		/**
		 * A predefined time frame.
		 */
		int selectedTimeFrameInterval = 259200
		
		/**
		 * Whether or not the time of the start-date should be selected manually.
		 */
		Boolean setFromHour
		/**
		 * Whether or not the time of the start-date should be selected manually.
		 */
		Boolean setToHour

		/**
		 * Constraints needs to fit.
		 */
		static constraints = {
			from(nullable: true, validator: {Date currentFrom, ShowAllCommand cmd ->
				boolean manualTimeframe = cmd.selectedTimeFrameInterval == 0
				if(manualTimeframe && currentFrom == null) return ['nullWithManualSelection']
			})
			to(nullable:true, validator: { Date currentTo, ShowAllCommand cmd ->
				boolean manualTimeframe = cmd.selectedTimeFrameInterval == 0
				if(manualTimeframe && currentTo == null) return ['nullWithManualSelection']
				else if(manualTimeframe && currentTo != null && cmd.from != null && currentTo.before(cmd.from)) return ['beforeFromDate']
			})
			fromHour(nullable: true, validator: {String currentFromHour, ShowAllCommand cmd ->
				boolean manualTimeframe = cmd.selectedTimeFrameInterval == 0
				if(manualTimeframe && currentFromHour == null) return ['nullWithManualSelection']
			})
			toHour(nullable: true, validator: {String currentToHour, ShowAllCommand cmd ->
				boolean manualTimeframe = cmd.selectedTimeFrameInterval == 0
				if(manualTimeframe && currentToHour == null) {
					return ['nullWithManualSelection']
				}
				else if(manualTimeframe && cmd.from != null && cmd.to != null && cmd.from.equals(cmd.to) && cmd.fromHour != null && currentToHour != null) {
					DateTime firstDayWithFromDaytime = getFirstDayWithTime(cmd.fromHour)
					DateTime firstDayWithToDaytime = getFirstDayWithTime(currentToHour)
					if(!firstDayWithToDaytime.isAfter(firstDayWithFromDaytime)) return ['inCombinationWithDateBeforeFrom']
				}
			})
			aggrGroup(nullable:false, inList: [
				AggregatorType.MEASURED_EVENT,
				AggregatorType.PAGE,
				AggregatorType.SHOP,
				CsiDashboardController.DAILY_AGGR_GROUP_PAGE,
				CsiDashboardController.DAILY_AGGR_GROUP_SHOP
			])
			selectedFolder(nullable:false, minSize:1)

			// selectedPage is only allowed to be empty if aggrGroup is AggregatorType.SHOP
			selectedPage(nullable:false, validator: { Collection currentCollection, ShowAllCommand cmd ->
				return (AggregatorType.MEASURED_EVENT.equals(cmd.aggrGroup) && (!currentCollection.isEmpty())) ||
				( (AggregatorType.PAGE.equals(cmd.aggrGroup) || CsiDashboardController.DAILY_AGGR_GROUP_PAGE.equals(cmd.aggrGroup)) && (!currentCollection.isEmpty())) ||
				AggregatorType.SHOP.equals(cmd.aggrGroup) || CsiDashboardController.DAILY_AGGR_GROUP_SHOP.equals(cmd.aggrGroup)
			})

			// selectedMeasuredEventIds is only allowed to be empty if aggrGroup is NOT AggregatorType.MEASURED_EVENT or selectedAllMeasuredEvents evaluates to true
			selectedMeasuredEventIds(nullable:false, validator: { Collection currentCollection, ShowAllCommand cmd ->
				return (AggregatorType.MEASURED_EVENT.equals(cmd.aggrGroup) && (!currentCollection.isEmpty() || cmd.selectedAllMeasuredEvents)) ||
				AggregatorType.PAGE.equals(cmd.aggrGroup)  || CsiDashboardController.DAILY_AGGR_GROUP_PAGE.equals(cmd.aggrGroup) ||
				AggregatorType.SHOP.equals(cmd.aggrGroup) || CsiDashboardController.DAILY_AGGR_GROUP_SHOP.equals(cmd.aggrGroup)
			})

			// selectedBrowsers is only allowed to be empty if aggrGroup is NOT AggregatorType.MEASURED_EVENT or selectedAllBrowsers evaluates to true
			selectedBrowsers(nullable:false, validator: { Collection currentCollection, ShowAllCommand cmd ->
				return (AggregatorType.MEASURED_EVENT.equals(cmd.aggrGroup) && (!currentCollection.isEmpty() || cmd.selectedAllBrowsers)) ||
				AggregatorType.PAGE.equals(cmd.aggrGroup)  || CsiDashboardController.DAILY_AGGR_GROUP_PAGE.equals(cmd.aggrGroup) ||
				AggregatorType.SHOP.equals(cmd.aggrGroup) || CsiDashboardController.DAILY_AGGR_GROUP_SHOP.equals(cmd.aggrGroup)
			})

			// selectedLocations is only allowed to be empty if aggrGroup is NOT AggregatorType.MEASURED_EVENT or selectedAllLocations evaluates to true
			selectedLocations(nullable:false, validator: { Collection currentCollection, ShowAllCommand cmd ->
				return (AggregatorType.MEASURED_EVENT.equals(cmd.aggrGroup) && (!currentCollection.isEmpty() || cmd.selectedAllLocations)) ||
				AggregatorType.PAGE.equals(cmd.aggrGroup) || CsiDashboardController.DAILY_AGGR_GROUP_PAGE.equals(cmd.aggrGroup) ||
				AggregatorType.SHOP.equals(cmd.aggrGroup) || CsiDashboardController.DAILY_AGGR_GROUP_SHOP.equals(cmd.aggrGroup)
			})
			overwriteWarningAboutLongProcessingTime(nullable:true)
		}

		static transients = ['selectedTimeFrame']

		/**
		 * <p>
		 * Returns the selected time frame as {@link Interval}.
		 * That is the interval from {@link #from} / {@link #fromHour} to {@link #to} / {@link #toHour} if {@link #selectedTimeFrameInterval} is 0 (that means manual).
		 * If {@link #selectedTimeFrameInterval} is greater 0 the returned time frame is now minus {@link #selectedTimeFrameInterval} minutes to now.
		 * </p>
		 *
		 * @return not <code>null</code>.
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

			return new Interval(start, end)
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
			viewModelToCopyTo.put('selectedTimeFrameInterval', this.selectedTimeFrameInterval)
			
			viewModelToCopyTo.put('selectedFolder', this.selectedFolder)
			viewModelToCopyTo.put('selectedPage', this.selectedPage)

			viewModelToCopyTo.put('selectedAllMeasuredEvents', (this.selectedAllMeasuredEvents as boolean ? 'on' : ''))
			viewModelToCopyTo.put('selectedMeasuredEventIds', this.selectedMeasuredEventIds)

			viewModelToCopyTo.put('selectedAllBrowsers', (this.selectedAllBrowsers as boolean ? 'on' : ''))
			viewModelToCopyTo.put('selectedBrowsers', this.selectedBrowsers)

			viewModelToCopyTo.put('selectedAllLocations', (this.selectedAllLocations as boolean ? 'on' : ''))
			viewModelToCopyTo.put('selectedLocations', this.selectedLocations)

			CustomDateEditor dateEditor = CustomDateEditorRegistrar.createCustomDateEditor()

			dateEditor.setValue(this.from)
			viewModelToCopyTo.put('from', dateEditor.getAsText())
			if(!this.fromHour.is(null)) {
				 viewModelToCopyTo.put('fromHour',this.fromHour)
			}

			dateEditor.setValue(this.to)
			viewModelToCopyTo.put('to', dateEditor.getAsText())
			if (!this.toHour.is(null)){
				viewModelToCopyTo.put('toHour', this.toHour)
			}
			viewModelToCopyTo.put('aggrGroup', this.aggrGroup ?: AggregatorType.MEASURED_EVENT)
			viewModelToCopyTo.put('debug', this.debug?:false)
			viewModelToCopyTo.put('setFromHour', this.setFromHour)
			viewModelToCopyTo.put('setToHour', this.setToHour)
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
		public MvQueryParams createMvQueryParams() throws IllegalStateException
		{
			if( !this.validate() )
			{
				throw new IllegalStateException('Query params are not available from an invalid command.')
			}

			MvQueryParams result = new MvQueryParams()

			result.jobGroupIds.addAll(this.selectedFolder)

			if( !this.selectedAllMeasuredEvents )
			{
				result.measuredEventIds.addAll(this.selectedMeasuredEventIds)
			}

			result.pageIds.addAll(this.selectedPage)

			if( !this.selectedAllBrowsers )
			{
				result.browserIds.addAll(this.selectedBrowsers)
			}

			if( !this.selectedAllLocations )
			{
				result.locationIds.addAll(this.selectedLocations)
			}

			return result
		}
	}

	/**
	 * <p>
	 * Constructs the static view data of the {@link #showAll(ShowAllCommand)}
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
	public Map<String, Object> constructStaticViewDataOfShowAll()
	{
		Map<String, Object> result = [:]

		// AggregatorTypes
		//		List<AggregatorType> allAggregatorTypes = aggregatorTypeDaoService.findAll().sort(false, { it.name });
		//		result.put('aggrGroupLabels', allAggregatorTypes.collect( { it.name } ))
		//		result.put('aggrGroupValues', allAggregatorTypes.collect( { String.valueOf( it.id) } ))
		result.put('aggrGroupLabels', AGGREGATOR_GROUP_LABELS)
		result.put('aggrGroupValues', AGGREGATOR_GROUP_VALUES)

		// CSI-JobGroups
		result.put('folders', jobGroupDaoService.findCSIGroups().sort(false, { it.name }))

		// Pages
		List<Page> pages = pageDaoService.findAll().sort(false, { it.name })
		result.put('pages', pages)

		// MeasuredEvents
		List<MeasuredEvent> measuredEvents = measuredEventDaoService.findAll().sort(false, { it.name })
		result.put('measuredEvents', measuredEvents)

		// Browsers
		List<Browser> browsers = browserDaoService.findAll().sort(false, { it.name })
		result.put('browsers', browsers)

		// Locations
		List<Location> locations = locationDaoService.findAll().
				sort(false, { Location it -> it.getWptServer().label }).
				sort(false, { Location it -> it.getBrowser().name }).
				sort(false, { Location it -> it.location })
		result.put('locations', locations)

		// JavaScript-Utility-Stuff:
		result.put("dateFormatString", DATE_FORMAT_STRING_FOR_HIGH_CHART)
		result.put("weekStart", MONDAY_WEEKSTART)

		// --- Map<PageID, Set<MeasuredEventID>> for fast view filtering:
		Map<Long, Set<Long>> eventsOfPages = new HashMap<Long, Set<Long>>()
		for(Page eachPage : pages)
		{
			Set<Long> eventIds = new HashSet<Long>()

			Collection<Long> ids = measuredEvents.findResults {
				it.testedPage.getId() == eachPage.getId() ? it.getId() : null }
			if( !ids.isEmpty() )
			{
				eventIds.addAll(ids)
			}

			eventsOfPages.put(eachPage.getId(), eventIds)
		}
		result.put('eventsOfPages', eventsOfPages)

		// --- Map<BrowserID, Set<LocationID>> for fast view filtering:
		Map<Long, Set<Long>> locationsOfBrowsers = new HashMap<Long, Set<Long>>()
		for(Browser eachBrowser : browsers)
		{
			Set<Long> locationIds = new HashSet<Long>()

			Collection<Long> ids = locations.findResults {
				it.browser.getId() == eachBrowser.getId() ? it.getId() : null }
			if( !ids.isEmpty() )
			{
				locationIds.addAll(ids)
			}

			locationsOfBrowsers.put(eachBrowser.getId(), locationIds)
		}
		result.put('locationsOfBrowsers', locationsOfBrowsers)
		result.put("chartRenderingLibrary", cookieBasedSettingsService.getChartingLibraryToUse())
		result.put('defaultChartTitle', csiHelperService.getCsiChartDefaultTitle())

		// Done! :)
		return result
	}

	def downloadBrowserWeights(){
		DateTimeFormatter dtFormater = DateTimeFormat.forPattern("yyyyMMdd")
		response.setHeader("Content-disposition",
				"attachment; filename=${dtFormater.print(new DateTime())}browser_weights.csv")
		response.contentType = "text/csv"
		StringBuilder builder = new StringBuilder()
		builder.append('name;weight\n')
		browserDaoService.findAll().each {
			builder.append("${it.name};${it.weight}\n")
		}
		response.outputStream << builder.toString()
	}
	def downloadPageWeights(){
		DateTimeFormatter dtFormater = DateTimeFormat.forPattern("yyyyMMdd")
		response.setHeader("Content-disposition",
				"attachment; filename=${dtFormater.print(new DateTime())}browser_weights.csv")
		response.contentType = "text/csv"
		StringBuilder builder = new StringBuilder()
		builder.append('name;weight\n')
		pageDaoService.findAll().each {
			builder.append("${it.name};${it.weight}\n")
		}
		response.outputStream << builder.toString()
	}
	def downloadHourOfDayWeights(){
		DateTimeFormatter dtFormater = DateTimeFormat.forPattern("yyyyMMdd")
		response.setHeader("Content-disposition",
				"attachment; filename=${dtFormater.print(new DateTime())}HourOfDays_weights.csv")
		response.contentType = "text/csv"
		StringBuilder builder = new StringBuilder()
		builder.append('fullHour;weight\n')
		// FIXME Change to use a DAO
		HourOfDay.list().each {
			builder.append("${it.fullHour};${it.weight}\n")
		}
		response.outputStream << builder.toString()
	}
	def weights() {
		log.info("params=$params")
		//		List<String> params.errorMessagesCsi instanceof String?[params.errorMessagesCsi]:params.errorMessagesCsi
		[browsers:browserDaoService.findAll(),
			pages: pageDaoService.findAll(),
			// FIXME Change to use a DAO
			hoursOfDay: HourOfDay.findAll(),
			errorMessagesCsi: params.list('errorMessagesCsi')]
	}
	def uploadBrowserWeights(){
		MultipartFile csv = request.getFile('browserCsv')
		List<String> errorMessagesCsiValidation = customerSatisfactionWeightService.validateWeightCsv(WeightFactor.BROWSER, csv.getInputStream())
		if (!errorMessagesCsiValidation) {
			customerSatisfactionWeightService.persistNewWeights(WeightFactor.BROWSER, csv.getInputStream())
		}
		log.info("errorMessagesCsiValidation=$errorMessagesCsiValidation")
		redirect(action: 'weights',
		params: [errorMessagesCsi: errorMessagesCsiValidation]	)
	}
	def uploadPageWeights(){
		MultipartFile csv = request.getFile('pageCsv')
		List<String> errorMessagesCsiValidation = customerSatisfactionWeightService.validateWeightCsv(WeightFactor.PAGE, csv.getInputStream())
		if (!errorMessagesCsiValidation) {
			customerSatisfactionWeightService.persistNewWeights(WeightFactor.PAGE, csv.getInputStream())
		}
		redirect(action: 'weights',
		params: [errorMessagesCsi: errorMessagesCsiValidation])
	}
	def uploadHourOfDayWeights(){
		MultipartFile csv = request.getFile('hourOfDayCsv')
		List<String> errorMessagesCsiValidation = customerSatisfactionWeightService.validateWeightCsv(WeightFactor.HOUROFDAY, csv.getInputStream())
		if (!errorMessagesCsiValidation) {
			customerSatisfactionWeightService.persistNewWeights(WeightFactor.HOUROFDAY, csv.getInputStream())
		}
		redirect(action: 'weights',
		params: [errorMessagesCsi: errorMessagesCsiValidation])
	}
	
	/**
	 * <p>
	 * Tests weather the UI should warn the user about an expected long 
	 * execution time for calculations on a time frame. 
	 * </p>
	 * 
	 * @param timeFrame 
	 *         The time frame to guess weather a user should be warned 
	 *         about potently very long calculation time; 
	 *         not <code>null</code>. 
	 * @param selectedAggregationIntervallInMintues
	 *         The number of minutes in selected measuring interval; >= 1.
	 * @param countOfSelectedSystems
	 *         The number of selected systems / {@link JobGroup}s; >= 1.
	 * @param countOfSelectedPages
	 *         The number of selected pages; >= 1.
	 * @param countOfSelectedBrowser
	 *         The number of selected browser; >= 1.
	 * 
	 * @return <code>true</code> if the user should be warned, 
	 *         <code>false</code> else.
	 * @since IT-95, significantly changed for IT-152.
	 */
	public boolean shouldWarnAboutLongProcessingTime(
		Interval timeFrame,
		int selectedAggregationIntervallInMintues,
		int countOfSelectedSystems,
		int countOfSelectedPages,
		int countOfSelectedBrowser)
	{
		int minutesInTimeFrame = new Duration(timeFrame.getStart(), timeFrame.getEnd()).getStandardMinutes()
		
		long expectedCountOfGraphs = countOfSelectedSystems * countOfSelectedPages * countOfSelectedBrowser
		long expectedPointsOfEachGraph = Math.round( minutesInTimeFrame / selectedAggregationIntervallInMintues )
		long expectedTotalNumberOfPoints = expectedCountOfGraphs * expectedPointsOfEachGraph

		return expectedTotalNumberOfPoints > 10000
	}
	
	/**
	 * Checks if hours between given fromDate and toDate is greater than 4 months.
	 *
	 * @param fromDate TODO Doc: Inclusive? Eclusive?
	 * @param toDate TODO Doc: Inclusive? Eclusive?
	 * @return TODO Doc
	 * @deprecated TODO Currently unused -> Discuss if this range check is required or just should be done in UI.
	 */
	@Deprecated
	private boolean exceedsTimeframeBoundary(Date fromDate, Date toDate, MeasuredValueInterval interval) {
		Days daysBetween = Days.daysBetween(new DateTime(fromDate), new DateTime(toDate))
		Integer maxDays
		switch (interval.intervalInMinutes) {
			case MeasuredValueInterval.WEEKLY:
				maxDays = 26*7
				break
			case MeasuredValueInterval.DAILY:
				maxDays = 6*7
				break
			default:
				maxDays = 2*7
				break
		}
		return daysBetween.isGreaterThan(new Days(maxDays))
	}

	/**
	 * Creates the CSV as String to be converted to HTML table by view. Thrown Exceptions get catched and an error-message is returned instead of csv-representation.
	 * @return String-representation of data or an error-message if any {@link Exception} is thrown.
	 */
	private String formatForTable(List<OsmChartGraph> csiValueMap, boolean includeCsTargetGraphs){
		String csvAsString 
		try {
			csvAsString = tryToFormatForTable(csiValueMap, includeCsTargetGraphs)
		} catch (Exception e) {
			log.error('An error occurred while creating csv-representation of measurement-data', e)
			flash.tableDataError = i18nService.msg('de.iteratec.ism.measurement.conversion.errormessage', 'Bei der Umwandlung der Messdaten f&uuml;r die tabellarischen Darstellung ist ein Fehler aufgetreten!')
		}
		return csvAsString
	}
	private String tryToFormatForTable(List<OsmChartGraph> csiValueMap, boolean includeCsTargetGraphs){
		StringWriter writer = new StringWriter()
		writeCSV(csiValueMap, writer, RequestContextUtils.getLocale(request), true)
		String csvAsString = writer.toString()
		if( csvAsString.endsWith('\n') )
		{
			csvAsString = csvAsString.substring(0, csvAsString.length() - 1)
		}
		return csvAsString
	}

}

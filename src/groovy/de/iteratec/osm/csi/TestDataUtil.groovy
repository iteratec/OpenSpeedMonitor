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

import static org.junit.Assert.*
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin

import java.util.regex.Pattern

import org.joda.time.DateTime
import org.springframework.transaction.TransactionStatus

import de.iteratec.osm.measurement.schedule.ConnectivityProfile
import de.iteratec.osm.report.external.GraphitePath
import de.iteratec.osm.report.external.GraphiteServer
import de.iteratec.osm.measurement.schedule.Job
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.measurement.schedule.JobGroupType
import de.iteratec.osm.OsmConfiguration
import de.iteratec.osm.report.chart.AggregatorType
import de.iteratec.osm.report.chart.MeasurandGroup
import de.iteratec.osm.report.chart.MeasuredValue
import de.iteratec.osm.report.chart.MeasuredValueInterval
import de.iteratec.osm.report.chart.MeasuredValueUpdateEvent
import de.iteratec.osm.report.chart.MeasuredValueUpdateEventDaoService
import de.iteratec.osm.result.detail.WaterfallEntry
import de.iteratec.osm.result.detail.WebPerformanceWaterfall
import de.iteratec.osm.csi.weighting.WeightedCsiValue
import de.iteratec.osm.csi.weighting.WeightingService
import de.iteratec.osm.result.CachedView
import de.iteratec.osm.result.EventResult
import de.iteratec.osm.result.EventResultService
import de.iteratec.osm.result.HttpArchive
import de.iteratec.osm.result.JobResult
import de.iteratec.osm.result.MeasuredEvent
import de.iteratec.osm.result.MeasuredValueTagService
import de.iteratec.osm.measurement.script.Script
import de.iteratec.osm.measurement.environment.Browser
import de.iteratec.osm.measurement.environment.BrowserAlias
import de.iteratec.osm.measurement.environment.Location
import de.iteratec.osm.measurement.environment.WebPageTestServer

/**
 * <p>
 * A test-utility to load data from customers CSV-files
 * in "pre CSI-Dashboard times".
 * </p>
 * 
 * <p>
 * <em>DEV-Note:</em>
 * Because this utility is used in both, unit and integration tests, it must 
 * be placed in the productive source folder.
 * </p>
 * 
 * @author mze
 * @since IT-8
 */
@TestMixin(GrailsUnitTestMixin)
class TestDataUtil {
	/**
	 * <p>
	 * Creates a map of {@link MeasuredValue}s referenced by a key consists
	 * of {@link JobGroup}- and {@link Page}-ID.
	 * </p>
	 *
	 * <p>
	 * The key-format is: {@code group.ident()+':::'+page.ident()}, for example:
	 * {@code 1:::2}. For all existing keys the referenced {@link List} is
	 * not <code>null</code> but possibly empty.
	 * </p>
	 
	 * @todo TODO mze-2013-08-15: Use {@link Collection} instead of List
	 *       because the values are not sorted!
	 *
	 * @param hourlyMeasuredValues The hourly measured value to insert in the
	 *         maps collections; not <code>null</code>.
	 * @param measuredValueTagService
	 *         The service to use for tag generation, not <code>null</code>.
	 *
	 * @return A map as described above, never <code>null</code>.
	 *
	 * @throws IllegalArgumentException
	 *         if at least one of the measured values is not an hourly measured value.
	 *         
	 * @since IT-43
	 */
	public static Map<String, List<MeasuredValue>> createHourlyMeasuredValueByGroupAndPageIdMap(
			List<MeasuredValue> hourlyMeasuredValues,
			MeasuredValueTagService measuredValueTagService) throws IllegalArgumentException {

		Map<String, List<MeasuredValue>> result = [:];

		for(MeasuredValue hmv : hourlyMeasuredValues) {
			Page page = measuredValueTagService.findPageOfHourlyEventTag(hmv.tag);
			assertNotNull("You must create a page for the measured value with id " + hmv.ident() + "first", page);

			JobGroup group = measuredValueTagService.findJobGroupOfHourlyEventTag(hmv.tag);
			assertNotNull("You must create a group for the measured value with id " + hmv.ident() + "first", group);

			String key = group.ident()+':::'+page.ident();
			Collection valuesForPageAndGroup = result[key];

			if( valuesForPageAndGroup == null ) {
				valuesForPageAndGroup = [];
				result[key] = valuesForPageAndGroup;
			}

			valuesForPageAndGroup.add(hmv);
		}

		return result;
	}

	/**
	 * <p>
	 * Removes all entries from the database.  The hole action is one hibernate-transaction which gets flushed afterwards.
	 * </p>
	 * 
	 * <p>
	 * <em>Note:</em> The remove-operations are flushed immediately.
	 * </p>
	 * 
	 * @since IT-43
	 */
	public static void cleanUpDatabase() {
		getAllDomainClasses().each {domainClass ->
			
			Job.withTransaction {TransactionStatus status ->
				
				removeAssociatedDomainsFromCollections()
				domainClass.list()*.delete(flush: true)
				
				status.flush()
			}
			
		}
	}
	
	public static void removeAssociatedDomainsFromCollections(){
		GraphiteServer.list().each{
			it.graphitePaths = []
			it.save(failOnError: true)
		}
	}
	
	public static int getCountOfAllObjectsInDatabase(){
		int count = 0
		getAllDomainClasses().each {domainClass ->
			count += domainClass.list().size()
		}
		return count
	}
	
	public static  List getAllDomainClasses(){
		return [
			GraphitePath.class,
			GraphiteServer.class,
			CsTargetGraph.class,
			CsTargetValue.class,
			CustomerFrustration.class,
			TimeToCsMapping.class,
			HttpArchive.class,
			OsmConfiguration.class,
			MeasuredValue.class,
			HourOfDay.class,
			JobResult.class,
			WaterfallEntry.class,
			WebPerformanceWaterfall.class,
			EventResult.class,
			Job.class,
			Script.class,
			JobGroup.class,
			Location.class,
			Browser.class,
			BrowserAlias.class,
			WebPageTestServer.class,
			MeasuredEvent.class,
			Page.class,
			AggregatorType.class,
			MeasuredValueInterval.class,
			MeasuredValueUpdateEvent.class,
			ConnectivityProfile.class,
			MeasuredEvent.class
		]
	}
	
	/**
	 * Creates at least one instance of each domain and persists these instances to database.
	 * For testing db-cleanup in tests. The instances doesn't make any sense in their content. 
	 */
	public static createAtLLeastOneObjectOfEachDomain(){
		
		Job.withTransaction {TransactionStatus status ->
			
			createMeasuredValueUpdateEvent(new Date(), MeasuredValueUpdateEvent.UpdateCause.CALCULATED, '1')
			List<MeasuredValueInterval> intervals = createMeasuredValueIntervals()
			List<AggregatorType> aggregators = createAggregatorTypes()
			List<Page> pages = createPages(['page01', 'page02', 'page03'])
			MeasuredEvent event = createMeasuredEvent('event', pages[0])
			createWebPageTestServer('server 1 - wpt example', '', 1, 'http://example.com')
			List<Browser> browsers = createBrowsersAndAliases()
			List<Location> locations = createLocations()
			List<JobGroup> jobGroups = createJobGroups()
			Script script = createScript('label', '', '', false)
			Job job = createJob('label', script, locations[0], jobGroups[0], '', 1, true)
			JobResult jobResult = createJobResult('label', new Date(), job, locations[0])
			createEventResult(job, jobResult, 1000, 100, event)
			WaterfallEntry waterfallEntry = createWaterfallEntry()
			createWebPerformanceWaterfall()
			createHoursOfDay()
			createMeasuredValue(new Date(), intervals[0], aggregators[0], '1', 42, "1,2,3", false)
			createOsmConfiguration(12)
			createOsmConfig()
			createHttpArchive(jobResult)
			createTimeToCsMapping(pages[0])
			createCustomerFrustration(pages[0])
			CsTargetValue csTargetValue = createCsTargetValue()
			createCsTargetGraph(csTargetValue, csTargetValue)
			GraphitePath graphitePath = createGraphitePath('prefix.', aggregators[0])
			createGraphiteServer("", 100, [graphitePath])
			createConnectivityProfile()
			
			status.flush()
		}
		
	}
	static createConnectivityProfile(){
		new GraphitePath(
			name: 'name',
			bandwidthDown: 6000,
			bandwidthUp: 512,
			latency: 40,
			packetLoss: 0
		).save(failOnError: true)
	}
	static createTemplate(){
		new GraphitePath(
		).save(failOnError: true)
	}
	static createCsTargetGraph(CsTargetValue pointOne, CsTargetValue pointTwo){
		new CsTargetGraph(
			label: '',
			pointOne: pointOne,
			pointTwo: pointTwo, 
			defaultVisibility: true
		).save(failOnError: true)
	}
	static CsTargetValue createCsTargetValue(){
		return new CsTargetValue(
			date: new Date(),
			csInPercent: 42
		).save(failOnError: true)
	}
	static createCustomerFrustration(Page page){
		new CustomerFrustration(
			page: page,
			loadTimeInMilliSecs: 1000,
			investigationVersion: 1
		).save(failOnError: true)
	}
	static createTimeToCsMapping(Page page){
		new TimeToCsMapping(
			page: page,
			loadTimeInMilliSecs: 1,
			customerSatisfaction: 0.9,
			mappingVersion: 1
		).save(failOnError: true)
	}
	static createHttpArchive(JobResult jobResult){
		new HttpArchive(
			jobResult: jobResult
		).save(failOnError: true)
	}
	static OsmConfiguration createOsmConfiguration(int detailDataStorageTimeInWeeks){
		return new OsmConfiguration(
			detailDataStorageTimeInWeeks: detailDataStorageTimeInWeeks
		).save(failOnError: true)
	}
	static WebPerformanceWaterfall createWebPerformanceWaterfall(){
		return new WebPerformanceWaterfall(
			url: '',
			startDate: new Date(),
			title: '',
			numberOfWptRun: 1,
			cachedView: CachedView.UNCACHED,
			startRenderInMillisecs: 1,
			docCompleteTimeInMillisecs: 1,
			domTimeInMillisecs: 1,
			fullyLoadedTimeInMillisecs: 1,
		).save(failOnError: true)
	}
	static WaterfallEntry createWaterfallEntry(){
		return new WaterfallEntry(
			httpStatus: 200,
			path: '',
			host: '',
			mimeType: '',
			startOffset: 0,
			oneBasedIndexInWaterfall: 1,
			dnsLookupTimeStartInMillisecs: 0,
			initialConnectTimeStartInMillisecs: 0,
			sslNegotationTimeStartInMillisecs: 0,
			timeToFirstByteStartInMillisecs: 0,
			downloadTimeStartInMillisecs: 0,
			dnsLookupTimeEndInMillisecs: 0,
			initialConnectTimeEndInMillisecs: 0,
			sslNegotationTimeEndInMillisecs: 0,
			timeToFirstByteEndInMillisecs: 0,
			downloadTimeEndInMillisecs: 0,
			downloadedBytes: 0,
			uploadedBytes: 0,
		).save(failOnError: true)
	}
	static Job createJob(String label, Script script, Location location, JobGroup group, String description, int runs, boolean active){
		return new Job(
			label: label,
			script: script,
			location: location,
			jobGroup: group,
			description: description,
			runs: runs,
			active: active
		).save(failOnError: true)
	}
	static createScript(String label, String description, String navigationScript, boolean provideAuthenticateInformation){
		return new Script(
			label: label,
			description: description,
			navigationScript: navigationScript,
			provideAuthenticateInformation: provideAuthenticateInformation, 
		).save(failOnError: true)
	}
	static WebPageTestServer createWebPageTestServer(String label, String proxyIdentifier, boolean active, String baseUrl){
		return new WebPageTestServer(
			label: label,
			proxyIdentifier: proxyIdentifier,
			dateCreated: new Date(),
			lastUpdated: new Date(),
			active: active,
			baseUrl: baseUrl
		).save(failOnError: true)
	}
	static MeasuredEvent createMeasuredEvent(String eventName, Page page){
		return new MeasuredEvent(
			name: eventName,
			testedPage: page
		).save(failOnError: true)
	}
	static createMeasuredValueUpdateEvent(Date dateOfUpdate, MeasuredValueUpdateEvent.UpdateCause cause, String mvId){
		new MeasuredValueUpdateEvent(
			dateOfUpdate: dateOfUpdate,
			updateCause: cause,
			measuredValueId: mvId
		).save(failOnError: true)
	}
	static GraphitePath createGraphitePath(String prefix, AggregatorType aggregator){
		new GraphitePath(
			prefix: prefix,
			measurand: aggregator
		).save(failOnError: true)
	}
	static GraphiteServer createGraphiteServer(String serverAdress, int port, List paths){
		new GraphiteServer(
			serverAdress: '',
			port: port,
			graphitePaths: paths
		).save(failOnError: true)
	}

	/**
	 * <p>
	 * Pre-calculates some hourly measured values based on previously created data.
	 * <b>Note: </b>Not in use. Doesn't work yet.
	 * </p>
	 *
	 * @param jobGroup 
	 *         The group to use, not <code>null</code>.
	 * @param pageName
	 *         The name of the page to calculate data for, 
	 *         not <code>null</code>.
	 * @param end
	 *         The last date to calculate for, 
	 *         not <code>null</code>.
	 * @param currentDate
	 *         The current date, should be before {@code end}, 
	 *         not <code>null</code>.
	 * @param hourlyInterval
	 *         The {@link MeasuredValueInterval} to use for 
	 *         calculation, should be an 
	 *         {@link MeasuredValueInterval#HOURLY} one; 
	 *         not <code>null</code>. 
	 * @param measuredValueTagService
	 *         The service to use for tag generation, not <code>null</code>.
	 * @param eventMeasuredValueService
	 *         The service to use for calcualtion, not <code>null</code>.
	 *         
	 * @return A collection of pre-calculated hourly values.
	 * 
	 * @see #loadTestDataFromCustomerCSV(File, List, List)
	 * @since IT-43
	 */
	public static List<MeasuredValue> precalculateHourlyMeasuredValues(
			JobGroup jobGroup, String pageName,
			DateTime end, DateTime currentDate,
			MeasuredValueInterval hourlyInterval,
			EventMeasuredValueService eventMeasuredValueService,
			MeasuredValueTagService measuredValueTagService,
			EventResultService eventResultService, 
			WeightingService weightingService, 
			MeanCalcService meanCalcService,
			MeasuredValueUpdateEventDaoService measuredValueUpdateEventDaoService) {

		List<MeasuredValue> createdHmvs = []
		while (!currentDate.isAfter(end)) {

			createdHmvs.addAll(calculateMvsOfOneHour(
					currentDate, hourlyInterval, jobGroup, pageName,
					eventMeasuredValueService,
					measuredValueTagService,
					eventResultService, 
					weightingService, 
					meanCalcService,
					measuredValueUpdateEventDaoService)
			)
			currentDate = currentDate.plusHours(1)
			
		}
		return createdHmvs
	}

	/**
	 * <p>
	 * Calculates the one specified hour.
	 * </p>
	 *
	 * @param dateTimeToCalculateMvFor The hour to create for.
	 * @param hourly The intervall which is {@link MeasuredValueInterval#HOURLY}.
	 * @param jobGroup The {@link JobGroup} to use.
	 * @param eventMeasuredValueService
	 *         The service to use for calcualtion, not <code>null</code>.
	 *
	 * @return The created measured values.
	 */
	private static List<MeasuredValue> calculateMvsOfOneHour(
			DateTime dateTimeToCalculateMvFor,
			MeasuredValueInterval hourly,
			JobGroup jobGroup,
			String pageName,
			EventMeasuredValueService eventMeasuredValueService,
			MeasuredValueTagService measuredValueTagService,
			EventResultService eventResultService, 
			WeightingService weightingService, 
			MeanCalcService meanCalcService,
			MeasuredValueUpdateEventDaoService measuredValueUpdateEventDaoService){
		List<MeasuredValue> createdHmvs = []

		Page page = Page.findByName(pageName)

		assertNotNull('Please use an existing page! The page with name ' + pageName + ' does not exisits.', page)

		MeasuredEvent event = findMeasuredEvent(jobGroup, page)

			// FF
			String tagFF = measuredValueTagService.createHourlyEventTag(
					jobGroup,
					event,
					page,
					Browser.findByName('FF'),
					Location.findByLocation('ffLocationLocation'));

			createdHmvs.add(
					ensurePresenceAndCalculation(dateTimeToCalculateMvFor, hourly, tagFF, 
						eventMeasuredValueService,
						measuredValueTagService,
						eventResultService, 
						weightingService, 
						meanCalcService,
						measuredValueUpdateEventDaoService)
					)

			// IE
			String tagIE = measuredValueTagService.createHourlyEventTag(
					jobGroup,
					event,
					page,
					Browser.findByName('IE'),
					Location.findByLocation('ieLocationLocation'));

			createdHmvs.add(
					ensurePresenceAndCalculation(dateTimeToCalculateMvFor, hourly, tagIE, 
						eventMeasuredValueService,
						measuredValueTagService,
						eventResultService, 
						weightingService, 
						meanCalcService,
						measuredValueUpdateEventDaoService)
					)
		
		return createdHmvs
	}
			
	/**
	 * Creates respective {@link MeasuredValue} if it doesn't exist and calculates it.
	 * After calculation status is {@link MeasuredValue.Calculated.Yes} or {@link MeasuredValue.Calculated.YesNoData}.
	 * @param startDate
	 * @param interval
	 * @param tag
	 * @return
	 */
	public static MeasuredValue ensurePresenceAndCalculation(
		DateTime startDate, 
		MeasuredValueInterval interval, 
		String tag, 
		EventMeasuredValueService eventMeasuredValueService,
		MeasuredValueTagService measuredValueTagService,
		EventResultService eventResultService, 
		WeightingService weightingService, 
		MeanCalcService meanCalcService,
		MeasuredValueUpdateEventDaoService measuredValueUpdateEventDaoService
	) {
		return ensurePresenceAndCalculation(
			startDate, 
			interval, 
			tag, 
			AggregatorType.findByName(AggregatorType.MEASURED_EVENT),
			eventMeasuredValueService,
			measuredValueTagService,
			eventResultService, 
			weightingService, 
			meanCalcService,
			measuredValueUpdateEventDaoService
		)
	}
		
	public static MeasuredValue ensurePresenceAndCalculation(
		DateTime startDate, 
		MeasuredValueInterval interval, 
		String tag, 
		AggregatorType eventAggregator,
		EventMeasuredValueService eventMeasuredValueService,
		MeasuredValueTagService measuredValueTagService,
		EventResultService eventResultService, 
		WeightingService weightingService, 
		MeanCalcService meanCalcService,
		MeasuredValueUpdateEventDaoService measuredValueUpdateEventDaoService
	) {
		MeasuredValue toCreateAndOrCalculate = eventMeasuredValueService.ensurePresence(startDate, interval, tag, eventAggregator, false)
		return calcMv(
			toCreateAndOrCalculate, 
			measuredValueTagService,
			eventResultService, 
			weightingService, 
			meanCalcService,
			measuredValueUpdateEventDaoService
		)
	}
			
	/**
	 * Calculates the given {@link MeasuredValue} toBeCalculated.
	 * After calculation status is {@link MeasuredValue.Calculated.Yes} or {@link MeasuredValue.Calculated.YesNoData}.
	 * @param toBeCalculated Should normally have status {@link MeasuredValue.Calculated.Not}.
	 * @return The calculated {@link MeasuredValue}.
	 */
	public static MeasuredValue calcMv(
		MeasuredValue toBeCalculated, 
		MeasuredValueTagService measuredValueTagService,
		EventResultService eventResultService, 
		WeightingService weightingService, 
		MeanCalcService meanCalcService,
		MeasuredValueUpdateEventDaoService measuredValueUpdateEventDaoService
	) {
		if (toBeCalculated) {
			
			Date fromDate = toBeCalculated.started
			Date toDate = new DateTime(fromDate).plusMinutes(toBeCalculated.interval.intervalInMinutes).toDate()

			if ( ! toBeCalculated.isCalculated() ) {
				reCalc(toBeCalculated, fromDate, toDate, 
					measuredValueTagService, eventResultService, weightingService, meanCalcService, measuredValueUpdateEventDaoService)
			}
			return toBeCalculated
		}
		return null
	}
	public static MeasuredValue reCalc(
		MeasuredValue toBeCalculated, 
		Date fromDate, 
		Date toDate,
		MeasuredValueTagService measuredValueTagService,
		EventResultService eventResultService, 
		WeightingService weightingService, 
		MeanCalcService meanCalcService, 
		MeasuredValueUpdateEventDaoService measuredValueUpdateEventDaoService) {
		
		MeasuredEvent measuredEvent = measuredValueTagService.findMeasuredEventOfHourlyEventTag(toBeCalculated.tag)
		JobGroup jobGroup = measuredValueTagService.findJobGroupOfHourlyEventTag(toBeCalculated.tag)
		Location location = measuredValueTagService.findLocationOfHourlyEventTag(toBeCalculated.tag)
		
		if (!measuredEvent||!jobGroup||jobGroup.groupType!=JobGroupType.CSI_AGGREGATION||!location) {
			return toBeCalculated
		}
		List<EventResult> results = []
		results.addAll(eventResultService.findByMeasuredEventBetweenDate(jobGroup, measuredEvent, location, fromDate, toDate))
		
		
		List<WeightedCsiValue> weightedCsiValues = []
		if (results.size() > 0) {
			weightedCsiValues = weightingService.getWeightedCsiValues(results, [] as Set)
		}
		if (weightedCsiValues.size() > 0) {
			toBeCalculated.value = meanCalcService.calculateWeightedMean(weightedCsiValues*.weightedValue)
			toBeCalculated.addAllToResultIds(weightedCsiValues*.underlyingEventResultIds.flatten())
		}else{
			toBeCalculated.clearResultIds()
		}
		measuredValueUpdateEventDaoService.createUpdateEvent(toBeCalculated.ident(), MeasuredValueUpdateEvent.UpdateCause.CALCULATED)
		toBeCalculated.save(failOnError:true)
		return toBeCalculated
	}

	/**
	 * <p>
	 * Loads test-data from customers CSV file and creates missing elements.
	 * </p>
	 * 
	 * @param csvFile 
	 *         The CSV file with customer data, 
	 *         not <code>null</code>.
	 * @param pagesToGenerateDataFor 
	 *         The names of the pages to process (see {@link Page}), 
	 *         not <code>null</code>, 
	 *         not {@linkplain Collection#isEmpty() empty}.
	 */
	public static void loadTestDataFromCustomerCSV(File csvFile, List<String> pagesToGenerateDataFor, List<String> allPages) {
		createJobGroups()
		createServer()
		createPages(allPages)
		createBrowsersAndAliases()
		createLocations()

		csvFile.eachLine{String csvLine ->
			if (!isHeaderLine(csvLine) && !isEmptyLine(csvLine)) {
				//				System.out.println('Processing line: ' + csvLine);
				decodeCSVTestDataLine(csvLine, pagesToGenerateDataFor)
			}
		}
	}
	
	/**
	 * <p>
	 * Loads test-data from customers CSV file and creates missing elements.
	 * </p>
	 *
	 * @param csvFile
	 *         The CSV file with customer data,
	 *         not <code>null</code>.
	 * @param pagesToGenerateDataFor
	 *         The names of the pages to process (see {@link Page}),
	 *         not <code>null</code>,
	 *         not {@linkplain Collection#isEmpty() empty}.
	 * @param measuredValueTagService
	 * 		   The {@link MeasuredValueTagService} for generating the tag of {@link EventResult}
	 */
	public static void loadTestDataFromCustomerCSV(File csvFile, List<String> pagesToGenerateDataFor, List<String> allPages, MeasuredValueTagService measuredValueTagService) {
		createJobGroups()
		createServer()
		createPages(allPages)
		createBrowsersAndAliases()
		createLocations()

		csvFile.eachLine{String csvLine ->
			if (!isHeaderLine(csvLine) && !isEmptyLine(csvLine)) {
				//				System.out.println('Processing line: ' + csvLine);
				decodeCSVTestDataLine(csvLine, pagesToGenerateDataFor, measuredValueTagService)
			}
		}
	}

	static List<Location> createLocations(){
		WebPageTestServer server1 = WebPageTestServer.findByLabel('server 1 - wpt example')
		Browser browserFF = Browser.findByName("FF")
		Browser browserIE = Browser.findByName("IE")
		Location ffAgent1, ieAgent1
		ffAgent1 = new Location(
			active: true,
			valid: 1,
			location: 'ffLocationLocation',
			label: 'ffLocationLabel',
			browser: browserFF,
			wptServer: server1
		).save(failOnError: true)
		ieAgent1 = new Location(
			active: true,
			valid: 1,
			location: 'ieLocationLocation',
			label: 'ieLocationLabel',
			browser: browserIE,
			wptServer: server1
		).save(failOnError: true)
		return [ffAgent1, ieAgent1]	
	}

	static Location createLocation(WebPageTestServer server, String uniqueIdentifierForServer, Browser browser, Boolean active){
		return new Location(
				active: active,
				valid: 1,
				uniqueIdentifierForServer: uniqueIdentifierForServer,
				location: uniqueIdentifierForServer,
				label: uniqueIdentifierForServer,
				browser: browser,
				wptServer: server
		).save(failOnError: true)
	}

	static List<Browser> createBrowsersAndAliases() {
		List<Browser> browsers = []
		String browserName="undefined"
		browsers.add(
			new Browser(
				name: browserName,
				weight: 0)
				.addToBrowserAliases(alias: "undefined"
			).save(failOnError: true)
		)
		browserName="IE"
		browsers.add(
			new Browser(
				name: browserName,
				weight: 45)
				.addToBrowserAliases(alias: "IE")
				.addToBrowserAliases(alias: "IE8")
				.addToBrowserAliases(alias: "Internet Explorer")
				.addToBrowserAliases(alias: "Internet Explorer 8"
			).save(failOnError: true)
		)
		browserName="FF"
		browsers.add(
			new Browser(
				name: browserName,
				weight: 55)
				.addToBrowserAliases(alias: "FF")
				.addToBrowserAliases(alias: "FF7")
				.addToBrowserAliases(alias: "Firefox")
				.addToBrowserAliases(alias: "Firefox7"
			).save(failOnError: true)
		)
		return browsers
	}

	static Browser createBrowser(String name, double weight){
		return new Browser(
			name: name,
			weight: weight
		).save(failOnError: true)
	}

	static void createServer(){
		WebPageTestServer server1
		server1 = new WebPageTestServer(
				baseUrl : 'http://wpt.server.de',
				active : true,
				label : 'server 1 - wpt example',
				proxyIdentifier : 'server 1 - wpt example'
				).save(failOnError: true)
	}

	static List<Page> createPages(List<String> allPageNames){
		List<Page> allPages = []
		allPageNames.each{String pageName ->
			Double weight = 0
			switch(pageName){
				case 'HP' : weight = 1		; break
				case 'MES' : weight = 1		; break
				case 'SE' : weight = 1		; break
				case 'ADS' : weight = 1		; break
				case 'WKBS' : weight = 1		; break
				case 'WK' : weight = 1		; break
			}
			allPages.add(
				new Page(
					name: pageName,
					weight: weight
				).save(failOnError: true)
			)
		}
		return allPages
	}

	static List<JobGroup> createJobGroups(){
		JobGroup group1 = new JobGroup(
				name: "CSI",
				groupType: JobGroupType.CSI_AGGREGATION).save(failOnError: true)
		JobGroup group2 = new JobGroup(
				name:'csiGroup1',
				groupType: JobGroupType.CSI_AGGREGATION).save(failOnError: true)
		JobGroup group3 = new JobGroup(
				name:'csiGroup2',
				groupType: JobGroupType.CSI_AGGREGATION).save(failOnError: true)
		return [group1, group2, group3]
	}

	static JobGroup createJobGroup(String groupName, JobGroupType groupType){
		return new JobGroup(
			name: groupName,
			groupType: groupType
		).save(failOnError: true)
	}

	/**
	 * <p>
	 * Checks weather the current CSV-line should be treated as header-line.
	 * </p>
	 * 
	 * @param csvLine The CSV-line, not <code>null</code>.
	 * @return <code>true</code> if the line should be treated as header line,
	 *         <code>false</code> else.
	 */
	private static boolean isHeaderLine(String csvLine){
		return csvLine.startsWith('job;') || csvLine.startsWith(';')
	}

	/**
	 * <p>
	 * Checks weather the current CSV-line should be treated as an empty-line
	 * which mostly means the line is to be ignored.
	 * </p>
	 *
	 * @param csvLine The CSV-line, not <code>null</code>.
	 * @return <code>true</code> if the line should be treated as empty line,
	 *         <code>false</code> else.
	 */
	private static boolean isEmptyLine(String csvLine){
		return csvLine.split(';')[0].isEmpty()
	}

	/**
	 * <p>
	 * Checks if data for the page is relevant for the test.  
	 * </p>
	 * 
	 * @param page 
	 *         The page that relevance is to check, not <code>null</code>.
	 * @param pagesToGenerateDataFor 
	 *         The relevant pages by name, not <code>null</code>; an empty 
	 *         collection would cause nothing to be relevant.
	 * @return <code>true</code> if page is relevant, <code>false</code> else.
	 */
	private static boolean isResultForPageRequired(Page page, List<String> pagesToGenerateDataFor){
		return pagesToGenerateDataFor.contains(page.name)
	}

	/**
	 * <p>
	 * Interprets the job name in CSV as a Location. This is a manually mapping
	 * cause in real-world-scenario the jobs are assigned to locations by the
	 * agents job queue which would be known on receiving a fresh result.
	 * </p>
	 *
	 * @param csvJobColumn CSV Job-column contents.
	 * @return An assigned location, null if not interpretable.
	 */
	private static Location getLocationCSVJobName(String csvJobColumn) {
		if(Pattern.matches('.*FF_.*', csvJobColumn)) {
			return Location.findByLabel('ffLocationLabel');
		}

		return Location.findByLabel('ieLocationLabel');
	}

	/**
	 * <p>
	 * Gets or create an {@link MeasuredEvent} for the specified page and job combination.
	 * </p>
	 *
	 * @param job The job an event is to get for, not <code>null</code>.
	 * @param page The page an event is to get for, not <code>null</code>.
	 *
	 * @return not <code>null</code>.
	 */
	private static MeasuredEvent getMeasuredEvent(JobGroup job, Page page) {
		String eventName = 'TestEvent-' + page + "-" + job.name;

		return  MeasuredEvent.findByName(eventName)?:new MeasuredEvent(name: eventName, testedPage: page).save(failOnError: true)
	}

	/**
	 * <p>
	 * Gets the Job for the specified job-name. If an aproximate job does not
	 * exists now, it will be created.
	 * </p>
	 *
	 * @param csvJobColumn CSV Job-column contents.
	 * @return The Job for the CSV-column-value, not null.
	 */
	private static Job getJobOfCSVJobName(String csvJobCoulumn, JobGroup jobGroup, Location location) {
		Job result = Job.findByLabel(csvJobCoulumn);

		AggregatorType page = AggregatorType.findByName(AggregatorType.PAGE)?:new AggregatorType(name: AggregatorType.PAGE).save(failOnError:true)

		if(!result){
			result = new Job(
					label: csvJobCoulumn,
					location: location,
					page: page,
					active: false,
					description: '',
					runs: 1,
					jobGroup: jobGroup,
					script: Script.createDefaultScript(csvJobCoulumn).save(failOnError: true),
					maxDownloadTimeInMinutes: 60	
					).save(failOnError: true);
		}

		return result;
	}

	/**
	 * <p>
	 * Creates a job result for the specified data.
	 * </p>
	 *
	 * <p>
	 * None of the arguments may be <code>null</code>.
	 * </p>
	 *
	 * @param testId The ID of the test-result.
	 * @param dateOfJobRun The date of the test run.
	 * @param parentJob The job the result belongs to.
	 * @param agentLocation The location where the agent is working.
	 *
	 * @return A newly created result, not <code>null</code>.
	 */
	private static JobResult createJobResult(String testId, Date dateOfJobRun, Job parentJob, Location agentLocation) {
		return new JobResult(
		date: dateOfJobRun,
		testId: testId,
		httpStatusCode: 200,
		jobConfigLabel: parentJob.label,
		jobConfigRuns: 1,
		description: '',
		locationBrowser: agentLocation.browser.name,
		locationLocation: agentLocation.location,
		jobGroupName: parentJob.jobGroup.name,
		job: parentJob
		).save(failOnError: true)
	}

	/**
	 * <p>
	 * Creates an event result and assigns it to the specified
	 * {@link JobResult}.
	 * </p>
	 *
	 * <p>
	 * None of the arguments may be <code>null</code>.
	 * </p>
	 *
	 * @param job
	 *         The parent job.
	 * @param jobResult
	 *         The job result the event result should be assigned to.
	 * @param docCompleteTimeInMillisecs
	 *         The doc-complete-time in milliseconds.
	 * @param customerSatisfactionInPercent
	 *         The customer-satisfaction-index in percent.
	 */
	private static void createEventResult(Job job, JobResult jobResult, int docCompleteTimeInMillisecs, double customerSatisfactionInPercent, MeasuredEvent event) {
		EventResult eventResult = new EventResult(
				numberOfWptRun: 1,
				cachedView: CachedView.UNCACHED,
				medianValue: true,
				wptStatus:200,
				docCompleteTimeInMillisecs: docCompleteTimeInMillisecs,
				customerSatisfactionInPercent: customerSatisfactionInPercent,
				jobResultDate: jobResult.date,
				jobResultJobConfigId: jobResult.job.ident(),
				measuredEvent: event,
				speedIndex: EventResult.SPEED_INDEX_DEFAULT_VALUE,
				).save(failOnError: true)

		jobResult.eventResults.add(eventResult);
		jobResult.save(failOnError: true)
	}
	
	/**
	 * <p>
	 * Creates an event result and assigns it to the specified
	 * {@link JobResult}.
	 * </p>
	 *
	 * <p>
	 * None of the arguments may be <code>null</code>.
	 * </p>
	 *
	 * @param job
	 *         The parent job.
	 * @param jobResult
	 *         The job result the event result should be assigned to.
	 * @param docCompleteTimeInMillisecs
	 *         The doc-complete-time in milliseconds.
	 * @param customerSatisfactionInPercent
	 *         The customer-satisfaction-index in percent.
	 * @param measuredValueTagService
	 * 		   The {@link MeasuredValueTagService} for generating the tag of {@link EventResult}
	 */
	private static void createEventResult(Job job, JobResult jobResult, int docCompleteTimeInMillisecs, double customerSatisfactionInPercent, MeasuredEvent event, MeasuredValueTagService measuredValueTagService) {
		JobGroup jobGroup = job.jobGroup
		Page page = event.testedPage
		Location location = job.location
		Browser browser = location.browser
		
		String resultTag = measuredValueTagService.createEventResultTag(jobGroup, event, page, browser, location)
		EventResult eventResult = new EventResult(
				numberOfWptRun: 1,
				cachedView: CachedView.UNCACHED,
				medianValue: true,
				wptStatus:200,
				docCompleteTimeInMillisecs: docCompleteTimeInMillisecs,
				customerSatisfactionInPercent: customerSatisfactionInPercent,
				jobResultDate: jobResult.date,
				jobResultJobConfigId: jobResult.job.ident(),
				measuredEvent: event,
				speedIndex: EventResult.SPEED_INDEX_DEFAULT_VALUE,
				tag: resultTag
				).save(failOnError: true)

		jobResult.eventResults.add(eventResult);
		jobResult.save(failOnError: true)
		
	}
	
	public static Map<Long, JobResult> generateMapToFindJobResultByEventResultId(List<JobResult> jobResults){
		Map<Long, JobResult> resultMap = null
		if(!jobResults.isEmpty()){
			resultMap = new HashMap<Long, JobResult>()
			jobResults.each{JobResult jobResult ->
				jobResult.eventResults.each{EventResult eventResult ->
					resultMap[eventResult.ident()] = jobResult
				}
			}
		}
		return resultMap
	}

	/**
	 * Decodes one line of test data CSV.
	 *
	 * @param csvLine
	 */
	private static void decodeCSVTestDataLine(String csvLine, List<String> pagesToGenerateDataFor){
		String[] columns = csvLine.split(';');

		String jobName = columns[0]

		Page page = getPageFromCSVJobName(jobName);

		assertNotNull('Page for job-name ' + jobName + ' may not be null.', page)

		if( !isResultForPageRequired(page, pagesToGenerateDataFor) ) {
			// If the result is not needed for this test, just return and
			// do nothing more...
			return ;
		}

		JobGroup defaultJobGroup = JobGroup.findByName('CSI');
		JobGroup jobGroup1 = JobGroup.findByName('csiGroup1');
		JobGroup jobGroup2 = JobGroup.findByName('csiGroup2');

		Location location =  getLocationCSVJobName(jobName);
		Date dateOfJobRun = new Date(columns[2]+" "+columns[3]);


		assertNotNull(defaultJobGroup)
		assertNotNull(jobGroup1)
		assertNotNull(jobGroup2)
		assertNotNull(location)
		assertNotNull(dateOfJobRun)

		// Create the job:
		JobGroup groupOfJob
		switch(jobName.split('_')[0])
		{
			case 'csiGroup1':
				groupOfJob = jobGroup1
				break;
			case 'csiGroup2':
				groupOfJob = jobGroup2
				break;
			default:
				groupOfJob = defaultJobGroup
		}
		Job job = getJobOfCSVJobName(jobName, groupOfJob, location);
		assertNotNull(job)

		MeasuredEvent eventOfPage = getMeasuredEvent(groupOfJob, page);
		assertNotNull(eventOfPage)

		JobResult jobResult = createJobResult(columns[6], dateOfJobRun, job, location)

		assertNotNull(jobResult)

		if( columns.length > 8 && !columns[8].isEmpty() ) {
			createEventResult(job, jobResult, Integer.valueOf(columns[7]), Double.valueOf(columns[8]), eventOfPage);
		}
	}
	
	/**
	 * Decodes one line of test data CSV.
	 *
	 * @param csvLine
	 */
	private static void decodeCSVTestDataLine(String csvLine, List<String> pagesToGenerateDataFor, MeasuredValueTagService measuredValueTagService){
		String[] columns = csvLine.split(';');

		String jobName = columns[0]

		Page page = getPageFromCSVJobName(jobName);

		assertNotNull('Page for job-name ' + jobName + ' may not be null.', page)

		if( !isResultForPageRequired(page, pagesToGenerateDataFor) ) {
			// If the result is not needed for this test, just return and
			// do nothing more...
			return ;
		}

		JobGroup defaultJobGroup = JobGroup.findByName('CSI');
		JobGroup jobGroup1 = JobGroup.findByName('csiGroup1');
		JobGroup jobGroup2 = JobGroup.findByName('csiGroup2');

		Location location =  getLocationCSVJobName(jobName);
		Date dateOfJobRun = new Date(columns[2]+" "+columns[3]);


		assertNotNull(defaultJobGroup)
		assertNotNull(jobGroup1)
		assertNotNull(jobGroup2)
		assertNotNull(location)
		assertNotNull(dateOfJobRun)

		// Create the job:
		JobGroup groupOfJob
		switch(jobName.split('_')[0])
		{
			case 'csiGroup1':
				groupOfJob = jobGroup1
				break;
			case 'csiGroup2':
				groupOfJob = jobGroup2
				break;
			default:
				groupOfJob = defaultJobGroup
		}
		Job job = getJobOfCSVJobName(jobName, groupOfJob, location);
		assertNotNull(job)

		MeasuredEvent eventOfPage = getMeasuredEvent(groupOfJob, page);
		assertNotNull(eventOfPage)

		JobResult jobResult = createJobResult(columns[6], dateOfJobRun, job, location)

		assertNotNull(jobResult)

		if( columns.length > 8 && !columns[8].isEmpty() ) {
			createEventResult(job, jobResult, Integer.valueOf(columns[7]), Double.valueOf(columns[8]), eventOfPage, measuredValueTagService);
		}
	}

	/**
	 * <p>
	 * Interprets the job name in CSV as a Page. This is a manually mapping
	 * cause in real-world-scenario the jobs are assigned to pages during
	 * CSI-configuration.
	 * </p>
	 *
	 * @param csvJobColumn CSV Job-column contents.
	 * @return An assigned page, null if not interpretable.
	 */
	private static Page getPageFromCSVJobName(String csvJobColumn) {
		switch (csvJobColumn.toLowerCase()){
			case {Pattern.matches('.*step01.*', it)}:
				return Page.findByName('HP');
			case {Pattern.matches('.*step02.*', it)}:
				return Page.findByName('MES');
			case {Pattern.matches('.*step03.*', it)}:
				return Page.findByName('SE');
			case {Pattern.matches('.*step04.*', it)}:
				return Page.findByName('ADS');
			case {Pattern.matches('.*step05.*', it)}:
				return Page.findByName('WKBS');
			case {Pattern.matches('.*step06.*', it)}:
				return Page.findByName('WK');
			default:
				return null;
		}
	}

	/**
	 * <p>
	 * Creates an OsmConfiguration and persists it. 
	 * This method uses default values for minDocCompleteTimeInMillisecs (250), maxDocCompleteTimeInMillisecs (180000).
	 * </p>
	 */
	public static void createOsmConfig(){
		new OsmConfiguration(
				detailDataStorageTimeInWeeks: 2,
				defaultMaxDownloadTimeInMinutes: 60,
				minDocCompleteTimeInMillisecs: 250,
				maxDocCompleteTimeInMillisecs: 180000,
				measurementsGenerallyEnabled: true, 
				initialChartHeightInPixels: 400
				).save(failOnError: true)
	}

	/**
	 * <p>
	 * Creates the default AggregatorTypes (PAGE; MEASURED_STEP; PAGE_AND_BROWSER; SHOP).
	 * </p>
	 */
	public static List<AggregatorType> createAggregatorTypes(){
		AggregatorType eventAggregator = new AggregatorType(name: AggregatorType.MEASURED_EVENT, measurandGroup: MeasurandGroup.NO_MEASURAND).save(failOnError: true)
		AggregatorType pageAggregator = new AggregatorType(name: AggregatorType.PAGE, measurandGroup: MeasurandGroup.NO_MEASURAND).save(failOnError: true)
		AggregatorType shopAggregator = new AggregatorType(name: AggregatorType.SHOP, measurandGroup: MeasurandGroup.NO_MEASURAND).save(failOnError: true)
		return [eventAggregator, pageAggregator, shopAggregator]
	}

	/**
	 * <p>
	 * Creates the default MeasuredValueIntervals (hourly; daily; weekly).
	 * </p>
	 */
	public static List<MeasuredValueInterval> createMeasuredValueIntervals(){
		MeasuredValueInterval hourly = new MeasuredValueInterval(
			name: "hourly",
			intervalInMinutes: MeasuredValueInterval.HOURLY
		).save(failOnError: true)
		MeasuredValueInterval daily = new MeasuredValueInterval(
			name: "daily",
			intervalInMinutes: MeasuredValueInterval.DAILY
		).save(failOnError: true)
		MeasuredValueInterval weekly = new MeasuredValueInterval(
			name: "weekly",
			intervalInMinutes: MeasuredValueInterval.WEEKLY
		).save(failOnError: true)
		return [hourly, daily, weekly]
	}

	/**
	 * <p>
	 * Creates the default hours of a Day with corresponding weights. (hourly; daily; weekly).
	 * </p>
	 */
	public static void createHoursOfDay(){
		(0..23).each{int hour ->
			Double weight = 0
			switch(hour){
				case 0 : weight = 2.9		; break
				case 1 : weight = 0.4		; break
				case 2 : weight = 0.2		; break
				case 3 : weight = 0.1		; break
				case 4 : weight = 0.1		; break
				case 5 : weight = 0.2		; break
				case 6 : weight = 0.7		; break
				case 7 : weight = 1.7		; break
				case 8 : weight = 3.2		; break
				case 9 : weight = 4.8		; break
				case 10 : weight = 5.6		; break
				case 11 : weight = 5.7		; break
				case 12 : weight = 5.5		; break
				case 13 : weight = 5.8		; break
				case 14 : weight = 5.9		; break
				case 15 : weight = 6.0		; break
				case 16 : weight = 6.7		; break
				case 17 : weight = 7.3		; break
				case 18 : weight = 7.6		; break
				case 19 : weight = 8.8		; break
				case 20 : weight = 9.3		; break
				case 21 : weight = 7.0		; break
				case 22 : weight = 3.6		; break
				case 23 : weight = 0.9		; break
			}
			HourOfDay.findByFullHour(hour)?:new HourOfDay(
					fullHour: hour,
					weight: weight).save(failOnError: true)
		}
	}


	/**
	 * <p>
	 * Gets the previously created {@link MeasuredEvent}s for the specified page and jobgroup.
	 * </p>
	 *
	 * @param jobgroup The jobgroup an event is to get for, not <code>null</code>.
	 * @param page The page an event is to get for, not <code>null</code>.
	 *
	 * @return not <code>null</code>.
	 */
	public static MeasuredEvent findMeasuredEvent(JobGroup jobgroup, Page page) {
//		List<MeasuredEvent> event = MeasuredEvent.findAllByTestedPage(page)
		String eventName = 'TestEvent-' + page + "-" + jobgroup.name;
		
		MeasuredEvent event = MeasuredEvent.findByName(eventName);
		
		assertNotNull("The event should be created by test-data-load.", event)

		return  event;
	}
	
	/**
	 * Writes new {@link MeasuredValue} to db.
	 * @param date
	 * @param mvInterval
	 * @param aggregator
	 * @param tag
	 * @param value
	 * @param resultIdsAsString
	 * @param closed
	 */
	public static MeasuredValue createMeasuredValue(Date date, MeasuredValueInterval mvInterval, AggregatorType aggregator, String tag, Double value, String resultIdsAsString, boolean closed){
		return new MeasuredValue(
			started: date,
			interval: mvInterval,
			aggregator: aggregator,
			tag: tag,
			value: value,
			resultIds: resultIdsAsString,
			closedAndCalculated: closed
		).save(failOnError: true)
	}
	/**
	 * Writes a new {@link MeasuredValueUpdateEvent} with dateOfUpdate = NOW.
	 * @param measuredValueId
	 * @param cause
	 */
	public static void createUpdateEvent(Long measuredValueId, MeasuredValueUpdateEvent.UpdateCause cause){
		new MeasuredValueUpdateEvent(
			dateOfUpdate: new Date(),
			measuredValueId: measuredValueId,
			updateCause: cause
		).save(failOnError: true)
	} 
	
}

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

import static org.hamcrest.Matchers.*
import static org.junit.Assert.*
import grails.test.mixin.TestMixin
import grails.test.mixin.integration.IntegrationTestMixin

import org.joda.time.DateTime
import org.junit.After
import org.junit.Before
import org.junit.BeforeClass

import de.iteratec.osm.measurement.schedule.Job
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.measurement.schedule.JobGroupType
import de.iteratec.osm.report.chart.AggregatorType
import de.iteratec.osm.report.chart.MeasurandGroup
import de.iteratec.osm.report.chart.MeasuredValue
import de.iteratec.osm.report.chart.MeasuredValueInterval
import de.iteratec.osm.result.MeasuredEvent
import de.iteratec.osm.measurement.script.Script
import de.iteratec.osm.measurement.environment.Browser
import de.iteratec.osm.measurement.environment.Location
import de.iteratec.osm.measurement.environment.WebPageTestServer

/**
 * Contains tests which test the creation of {@link MeasuredValue}s without the existence of corresponding {@link EventResult}s.<br>
 * For all persisted {@link MeasuredValue}s a {@link MeasuredValueUpdateEvent} should be created, which marks measured value as calculated. 
 * @author nkuhn
 *
 */
@TestMixin(IntegrationTestMixin)
class CreatingYesNoDataMvsIntTests extends IntTestWithDBCleanup {
	
	static transactional = false
	
	/** injected by grails */ 
	PageMeasuredValueService pageMeasuredValueService
	ShopMeasuredValueService shopMeasuredValueService
	CsiHelperService csiHelperService
	
	MeasuredValueInterval hourly = MeasuredValueInterval.findByIntervalInMinutes(MeasuredValueInterval.HOURLY)
	MeasuredValueInterval weekly = MeasuredValueInterval.findByIntervalInMinutes(MeasuredValueInterval.WEEKLY)
	AggregatorType job = AggregatorType.findByName(AggregatorType.MEASURED_EVENT)
	AggregatorType page = AggregatorType.findByName(AggregatorType.PAGE)
	AggregatorType shop = AggregatorType.findByName(AggregatorType.SHOP)
	DateTime startOfCreatingHourlyEventValues = new DateTime(2012,1,9,0,0,0)
	DateTime startOfCreatingWeeklyPageValues = new DateTime(2012,2,6,0,0,0)
	DateTime startOfCreatingWeeklyShopValues = new DateTime(2012,3,12,0,0,0)
	
	@Before
	void setUp() {
	}
	
	@After
	void tearDown() {
	}
	
	/**
	 * Creating weekly-page {@link MeasuredValue}s without data.
	 */
	void testCreatingWeeklyPageValues() {
		DateTime endDate = startOfCreatingWeeklyPageValues.plusWeeks(1)
		List<MeasuredValue> wpmvs = pageMeasuredValueService.getOrCalculateWeeklyPageMeasuredValues(startOfCreatingWeeklyPageValues.toDate(), endDate.toDate())
		Integer countWeeks = 2
		Integer countPages = 7
		assertThat(wpmvs.size(), is(countWeeks * countPages))
		wpmvs.each{
			assertTrue( it.isCalculated() )
		}
	}
	
	/**
	 * Creating weekly-shop {@link MeasuredValue}s without data.
	 */
	void testCreatingWeeklyShopValues() {
		DateTime endDate = startOfCreatingWeeklyShopValues.plusWeeks(1)
		List<MeasuredValue> wsmvs = shopMeasuredValueService.getOrCalculateWeeklyShopMeasuredValues(startOfCreatingWeeklyShopValues.toDate(), endDate.toDate())
		Integer countWeeks = 2
		Integer countPages = 7
		assertThat(wsmvs.size(), is(countWeeks))
		wsmvs.each{
			assertTrue( it.isCalculated() )
		}
		Date endOfLastWeek = csiHelperService.resetToEndOfActualInterval(endDate, MeasuredValueInterval.WEEKLY).toDate()
		assert pageMeasuredValueService.findAll(startOfCreatingWeeklyShopValues.toDate(), endDate.toDate(), weekly).size() == countWeeks * countPages 
	}
	
	/**
	 * Creating testdata.
	 */
	@BeforeClass
	static void createTestData() {
		createMeasuredValueIntervals()
		createAggregatorTypes()
		createPagesAndEvents()
		createBrowsers()
		createHoursOfDay()
		createServer()
		createLocations()
		createJobGroups()
		createJobs()
	}
	
	private static createAggregatorTypes(){
		AggregatorType.findByName(AggregatorType.MEASURED_EVENT) ?: new AggregatorType(
			name: AggregatorType.MEASURED_EVENT, measurandGroup: MeasurandGroup.NO_MEASURAND).save(failOnError: true)
			
		AggregatorType.findByName(AggregatorType.PAGE) ?: new AggregatorType(
			name: AggregatorType.PAGE, measurandGroup: MeasurandGroup.NO_MEASURAND).save(failOnError: true)
			
		AggregatorType.findByName(AggregatorType.PAGE_AND_BROWSER) ?: new AggregatorType(
			name: AggregatorType.PAGE_AND_BROWSER, measurandGroup: MeasurandGroup.NO_MEASURAND).save(failOnError: true)
			
		AggregatorType.findByName(AggregatorType.SHOP) ?: new AggregatorType(
			name: AggregatorType.SHOP, measurandGroup: MeasurandGroup.NO_MEASURAND).save(failOnError: true)
	}
	
	private static createMeasuredValueIntervals(){
		MeasuredValueInterval.findByIntervalInMinutes(MeasuredValueInterval.HOURLY) ?: new MeasuredValueInterval(
			name: "hourly",
			intervalInMinutes: MeasuredValueInterval.HOURLY
			).save(failOnError: true)
			
		MeasuredValueInterval.findByIntervalInMinutes(MeasuredValueInterval.DAILY) ?: new MeasuredValueInterval(
			name: "daily",
			intervalInMinutes: MeasuredValueInterval.DAILY
			).save(failOnError: true)
			
		MeasuredValueInterval.findByIntervalInMinutes(MeasuredValueInterval.WEEKLY) ?: new MeasuredValueInterval(
			name: "weekly",
			intervalInMinutes: MeasuredValueInterval.WEEKLY
			).save(failOnError: true)
	}
	
	private static void createJobGroups(){
		def csiGroupName = 'CSI'
		JobGroup.findByName(csiGroupName)?:new JobGroup(
				name:csiGroupName, 
				groupType: JobGroupType.CSI_AGGREGATION).save(failOnError: true)
	}
	
	private static void createJobs(){
		Script script = Script.createDefaultScript('Unnamed').save(failOnError: true)
		Location locationFf = Location.findByLabel('ffLocationLabel')
		Location locationIe = Location.findByLabel('ieLocationLabel')
		JobGroup jobGroup = JobGroup.findByName('CSI')
		def pageHp = Page.findByName('HP')
		def pageMes = Page.findByName('MES')
		
		Job testjob_HP = new Job(
			label: 'testjob_HP',
			location: locationFf,
			page: pageHp,
			active: false,
			description: '',
			runs: 1,
			jobGroup: jobGroup,
			script: script,
			maxDownloadTimeInMinutes: 60
		).save(failOnError: true)
		
		Job testjob_MES = new Job(
			label: 'testjob_MES',
			location: locationIe,
			page: pageMes,
			active: false,
			description: '',
			runs: 1,
			jobGroup: jobGroup,
			script: script,
			maxDownloadTimeInMinutes: 60
		).save(failOnError: true)
	}
	
	private static void createHoursOfDay(){
		(0..23).each{hour ->
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
	private static void createPagesAndEvents(){
		['HP', 'MES', 'SE', 'ADS', 'WKBS', 'WK', Page.UNDEFINED].each{pageName ->
			Double weight = 0
			switch(pageName){
				case 'HP' : weight = 6		; break
				case 'MES' : weight = 9		; break
				case 'SE' : weight = 36		; break
				case 'ADS' : weight = 43		; break
				case 'WKBS' : weight = 3		; break
				case 'WK' : weight = 3		; break
			}
			Page page = Page.findByName(pageName)?:new Page(
				name: pageName,
				weight: weight).save(failOnError: true)
				
			// Simply create one event
			new MeasuredEvent(
				name: 'CreatingYesNoDataMvsIntTests-' + pageName, 
				testedPage: page 	
			).save(failOnError:true)
		}
	}
	private static void createBrowsers(){
		String browserName="undefined"
		Browser.findByName(browserName)?:new Browser(
			name: browserName,
			weight: 0)
				.addToBrowserAliases(alias: "undefined")
				.save(failOnError: true)
		browserName="IE"
		Browser browserIE = Browser.findByName(browserName)?:new Browser(
			name: browserName,
			weight: 45)
				.addToBrowserAliases(alias: "IE")
				.addToBrowserAliases(alias: "IE8")
				.addToBrowserAliases(alias: "Internet Explorer")
				.addToBrowserAliases(alias: "Internet Explorer 8")
				.save(failOnError: true)
		browserName="FF"
		Browser browserFF = Browser.findByName(browserName)?:new Browser(
			name: browserName,
			weight: 55)
				.addToBrowserAliases(alias: "FF")
				.addToBrowserAliases(alias: "FF7")
				.addToBrowserAliases(alias: "Firefox")
				.addToBrowserAliases(alias: "Firefox7")
				.save(failOnError: true)
	}
	
	private static void createServer(){
		WebPageTestServer server1
		server1 = new WebPageTestServer(
			baseUrl : 'http://wpt.server.de',
			active : true,
			label : 'server 1 - wpt server',
			proxyIdentifier : 'server 1 - wpt server'
			).save(failOnError: true)
	}
	
	private static void createLocations(){
		WebPageTestServer server1 = WebPageTestServer.findByLabel('server 1 - wpt server')
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
	}
}

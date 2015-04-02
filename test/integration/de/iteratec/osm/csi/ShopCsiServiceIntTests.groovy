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
import de.iteratec.osm.measurement.environment.WebPageTestServer
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.measurement.schedule.JobGroupType
import de.iteratec.osm.measurement.script.Script

import static org.junit.Assert.*

import java.util.Date;

import grails.test.mixin.TestMixin
import grails.test.mixin.integration.IntegrationTestMixin

import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.junit.Before

import de.iteratec.osm.OsmConfiguration
import de.iteratec.osm.api.ShopCsiService
import de.iteratec.osm.api.SystemCSI
import de.iteratec.osm.csi.weighting.WeightFactor
import de.iteratec.osm.result.CachedView
import de.iteratec.osm.result.EventResult
import de.iteratec.osm.result.JobResult
import de.iteratec.osm.result.MeasuredEvent
import de.iteratec.osm.result.MvQueryParams
import de.iteratec.osm.measurement.environment.Browser
import de.iteratec.osm.measurement.schedule.Job

/**
 *
 */
@TestMixin(IntegrationTestMixin)
class ShopCsiServiceIntTests extends IntTestWithDBCleanup {
	
	static final double DELTA = 1e-15
	ShopCsiService shopCsiService
	DateTime START = new DateTime(2014,1,1,0,0, DateTimeZone.UTC)
	DateTime END = new DateTime(2014,12,31,0,0, DateTimeZone.UTC)
	int groups = 0

	@Before
    void setUp() {
		//test-data common to all tests
		createOsmConfig()
		createPagesAndEvents()
		createBrowsers()
		//mocks
		shopCsiService.csTargetGraphDaoService.metaClass.getActualCsTargetGraph = { return null }
    }
	
    void testRetrieveSystemCsiByRawData() {
		//test-specific data ////////////////////////////////////////////////////////////////////////////////////////////////////////
		MvQueryParams  queryParams = new MvQueryParams()
		
		MeasuredEvent eventHomepage = MeasuredEvent.findByName('event-HP')
		MeasuredEvent eventMes = MeasuredEvent.findByName('event-MES')
		String pageHP_ID = Page.findByName('HP').ident().toString()
		String pageMES_ID = Page.findByName('MES').ident().toString()
		String browserIE_ID = Browser.findByName('IE').ident().toString()
		String browserFF_ID = Browser.findByName('FF').ident().toString()
		
		// HP|IE
		double csiHpIe1 = 0.52d
		double csiHpIe2 = 0.54d
		double csiHpIe3 = 0.52d
    	createEventResult(eventHomepage, "1;${eventHomepage.ident().toString()};${pageHP_ID};${browserIE_ID};1", csiHpIe1)
		createEventResult(eventHomepage, "1;${eventHomepage.ident().toString()};${pageHP_ID};${browserIE_ID};1", csiHpIe2)
		createEventResult(eventHomepage, "1;${eventHomepage.ident().toString()};${pageHP_ID};${browserIE_ID};1", csiHpIe3)
		// HP|FF
		double csiHpFf1 = 0.54d
		double csiHpFf2 = 0.52d
		double csiHpFf3 = 0.54d
		createEventResult(eventHomepage, "1;${eventHomepage.ident().toString()};${pageHP_ID};${browserFF_ID};1", csiHpFf1)
		createEventResult(eventHomepage, "1;${eventHomepage.ident().toString()};${pageHP_ID};${browserFF_ID};1", csiHpFf2)
		createEventResult(eventHomepage, "1;${eventHomepage.ident().toString()};${pageHP_ID};${browserFF_ID};1", csiHpFf3)
		// MES|IE
		double csiMesIe1 = 0.92d
		double csiMesIe2 = 0.94d
		double csiMesIe3 = 0.92d
		createEventResult(eventMes, "1;${eventMes.ident().toString()};${pageMES_ID};${browserIE_ID};1", csiMesIe1)
		createEventResult(eventMes, "1;${eventMes.ident().toString()};${pageMES_ID};${browserIE_ID};1", csiMesIe2)
		createEventResult(eventMes, "1;${eventMes.ident().toString()};${pageMES_ID};${browserIE_ID};1", csiMesIe3)
		// MES|FF
		double csiMesFf1 = 0.94d
		double csiMesFf2 = 0.92d
		double csiMesFf3 = 0.94d
		createEventResult(eventMes, "1;${eventMes.ident().toString()};${pageMES_ID};${browserFF_ID};1", csiMesFf1)
		createEventResult(eventMes, "1;${eventMes.ident().toString()};${pageMES_ID};${browserFF_ID};1", csiMesFf2)
		createEventResult(eventMes, "1;${eventMes.ident().toString()};${pageMES_ID};${browserFF_ID};1", csiMesFf3)
		
		Double ieWeight = 45d
		Double ffWeight = 55d
		Double hpWeight = 6d
		Double mesWeight = 9d
		Double avgHpIe = (csiHpIe1 + csiHpIe2 + csiHpIe3) / 3
		Double avgHpFf = (csiHpFf1 + csiHpFf2 + csiHpFf3) / 3
		Double avgMesIe = (csiMesIe1 + csiMesIe2 + csiMesIe3) / 3
		Double avgMesFf = (csiMesFf1 + csiMesFf2 + csiMesFf3) / 3
		
		Double expectedCsi = ((avgHpIe * hpWeight * ieWeight) + (avgHpFf * hpWeight * ffWeight) + (avgMesIe * mesWeight * ieWeight) + (avgMesFf * mesWeight * ffWeight)) / 
			((ieWeight * hpWeight) + (ieWeight * mesWeight) + (ffWeight * hpWeight) + (ffWeight * mesWeight))
		
		//test-execution ////////////////////////////////////////////////////////////////////////////////////////////////////////
		SystemCSI systemCsi = shopCsiService.retrieveSystemCsiByRawData(START, END, queryParams, [WeightFactor.PAGE, WeightFactor.BROWSER] as Set)
		
		//assertions ////////////////////////////////////////////////////////////////////////////////////////////////////////
		assertEquals(12, EventResult.list().size())
		assertEquals(expectedCsi * 100, systemCsi.csiValueAsPercentage, DELTA)
		
		//duplicate hp-results which shouldn't change system-csi at all (should just improve accuracy of hp-proportion of csi) 
		createEventResult(eventHomepage, "1;${eventHomepage.ident().toString()};${pageHP_ID};${browserIE_ID};1", csiHpIe1)
		createEventResult(eventHomepage, "1;${eventHomepage.ident().toString()};${pageHP_ID};${browserIE_ID};1", csiHpIe2)
		createEventResult(eventHomepage, "1;${eventHomepage.ident().toString()};${pageHP_ID};${browserIE_ID};1", csiHpIe3)
		createEventResult(eventHomepage, "1;${eventHomepage.ident().toString()};${pageHP_ID};${browserFF_ID};1", csiHpFf1)
		createEventResult(eventHomepage, "1;${eventHomepage.ident().toString()};${pageHP_ID};${browserFF_ID};1", csiHpFf2)
		createEventResult(eventHomepage, "1;${eventHomepage.ident().toString()};${pageHP_ID};${browserFF_ID};1", csiHpFf3)
		
		assertEquals(18, EventResult.list().size())
		systemCsi = shopCsiService.retrieveSystemCsiByRawData(START, END, queryParams, [WeightFactor.PAGE, WeightFactor.BROWSER] as Set)
		assertEquals(expectedCsi * 100, systemCsi.csiValueAsPercentage, DELTA)
    }
	
	void createEventResult(MeasuredEvent event, String tag, double value){
		//data is needed to create a JobResult
		JobGroup group = TestDataUtil.createJobGroup("Group${groups}",JobGroupType.CSI_AGGREGATION)
		Script script = TestDataUtil.createScript("label${groups}","description","navigationScript",true)
		WebPageTestServer webPageTestServer = TestDataUtil.createWebPageTestServer("label","1",true,"http://www.url.de")
		Browser browser = TestDataUtil.createBrowser("browser${groups}",1)
		Location location = TestDataUtil.createLocation(webPageTestServer,"id",browser,true)
		Job job = TestDataUtil.createJob("Label${groups++}",script, location,group,"descirpiton",1,false,20)

		JobResult expectedResult = new JobResult(jobGroupName: "Group", jobConfigLabel:"label", jobConfigRuns: 1, httpStatusCode: 200, job:job,description: "description",date: new Date(), testId: "TestJob").save(validate: false);
		// TODO: Create a dummy data for expectedResult to pass it to EventResult
		
		new EventResult(
			measuredEvent: event,
			wptStatus: 200,
			medianValue: true,
			numberOfWptRun: 1,
			cachedView: CachedView.UNCACHED,
			jobResult: expectedResult,
			jobResultDate: START.plusDays(1).toDate(),
			jobResultJobConfigId: 1,
			tag: tag,
			speedIndex: 1,
			customerSatisfactionInPercent: value,
			docCompleteTimeInMillisecs: 1000
		).save(failOnError: true)
	}
	
	void createPagesAndEvents(){
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
				name: 'event-' + pageName, 
				testedPage: page 	
			).save(failOnError:true)
		}
		
	}
	
	void createBrowsers(){
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
	
	void createOsmConfig(){
		new OsmConfiguration(
			detailDataStorageTimeInWeeks: 2,
			defaultMaxDownloadTimeInMinutes: 60,
			minDocCompleteTimeInMillisecs: 250,
			maxDocCompleteTimeInMillisecs: 180000).save(failOnError: true)
	}
}

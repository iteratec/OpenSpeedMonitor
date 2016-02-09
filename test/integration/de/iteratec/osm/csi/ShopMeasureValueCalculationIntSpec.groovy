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
import grails.test.mixin.integration.IntegrationTestMixin

import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.junit.Before

import de.iteratec.osm.report.chart.MeasuredValueUtilService
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.report.chart.AggregatorType
import de.iteratec.osm.report.chart.CsiAggregation
import de.iteratec.osm.report.chart.MeasuredValueInterval
import de.iteratec.osm.report.chart.MeasuredValueUpdateEvent
import de.iteratec.osm.result.MeasuredEvent
import de.iteratec.osm.result.MvQueryParams
import de.iteratec.osm.measurement.environment.Browser

/**
 *
 */
@TestMixin(IntegrationTestMixin)
class ShopMeasureValueCalculationIntSpec extends de.iteratec.osm.csi.IntTestWithDBCleanup{

    static final double DELTA = 1e-15
	ShopMeasuredValueService shopMeasuredValueService
	MeasuredValueUtilService measuredValueUtilService
	DateTime START = new DateTime(2014,1,1,10,0, DateTimeZone.UTC)

	@Before
    void setUp() {
		
		//test-data common to all tests
		TestDataUtil.cleanUpDatabase()
		TestDataUtil.createOsmConfig()
		TestDataUtil.createMeasuredValueIntervals()
		TestDataUtil.createAggregatorTypes()
		TestDataUtil.createHoursOfDay()
		TestDataUtil.createJobGroups()
		TestDataUtil.createBrowsersAndAliases()
		createPages()
    }
	
    void testCalculationOfDailyShopMv() {
		
		// test-specific data ////////////////////////////////////////////////////////////////////////////////////////////////////////
		MvQueryParams  queryParams = new MvQueryParams()
		
		MeasuredEvent eventHomepage = MeasuredEvent.findByName('event-HP')
		MeasuredEvent eventMes = MeasuredEvent.findByName('event-MES')
		String pageHP_ID = Page.findByName('HP').ident().toString()
		String pageMES_ID = Page.findByName('MES').ident().toString()
		String browserIE_ID = Browser.findByName('IE').ident().toString()
		String browserFF_ID = Browser.findByName('FF').ident().toString()
		
		List<CsiAggregation> hmvs = []
		// HP|IE
		double csiHpIe1 = 0.52d
		double csiHpIe2 = 0.54d
		double csiHpIe3 = 0.52d
		hmvs.add(createMeasuredValue("1;${eventHomepage.ident().toString()};${pageHP_ID};${browserIE_ID};1", csiHpIe1))
		hmvs.add(createMeasuredValue("1;${eventHomepage.ident().toString()};${pageHP_ID};${browserIE_ID};1", csiHpIe2))
		hmvs.add(createMeasuredValue("1;${eventHomepage.ident().toString()};${pageHP_ID};${browserIE_ID};1", csiHpIe3))
		// HP|FF
		double csiHpFf1 = 0.54d
		double csiHpFf2 = 0.52d
		double csiHpFf3 = 0.54d
		hmvs.add(createMeasuredValue("1;${eventHomepage.ident().toString()};${pageHP_ID};${browserFF_ID};1", csiHpFf1))
		hmvs.add(createMeasuredValue("1;${eventHomepage.ident().toString()};${pageHP_ID};${browserFF_ID};1", csiHpFf2))
		hmvs.add(createMeasuredValue("1;${eventHomepage.ident().toString()};${pageHP_ID};${browserFF_ID};1", csiHpFf3))
		// MES|IE
		double csiMesIe1 = 0.92d
		double csiMesIe2 = 0.94d
		double csiMesIe3 = 0.92d
		hmvs.add(createMeasuredValue("1;${eventMes.ident().toString()};${pageMES_ID};${browserIE_ID};1", csiMesIe1))
		hmvs.add(createMeasuredValue("1;${eventMes.ident().toString()};${pageMES_ID};${browserIE_ID};1", csiMesIe2))
		hmvs.add(createMeasuredValue("1;${eventMes.ident().toString()};${pageMES_ID};${browserIE_ID};1", csiMesIe3))
		// MES|FF
		double csiMesFf1 = 0.94d
		double csiMesFf2 = 0.92d
		double csiMesFf3 = 0.94d
		hmvs.add(createMeasuredValue("1;${eventMes.ident().toString()};${pageMES_ID};${browserFF_ID};1", csiMesFf1))
		hmvs.add(createMeasuredValue("1;${eventMes.ident().toString()};${pageMES_ID};${browserFF_ID};1", csiMesFf2))
		hmvs.add(createMeasuredValue("1;${eventMes.ident().toString()};${pageMES_ID};${browserFF_ID};1", csiMesFf3))
		
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
			
		// mocking hourly event-MeasuredValues ////////////////////////////////////////////////////////////////////////////////////////////////////////
		shopMeasuredValueService.pageMeasuredValueService.eventMeasuredValueService.metaClass.getOrCalculateHourylMeasuredValues = {Date start, Date end, MvQueryParams mvQueryparams ->
			return hmvs
		}
		
		//test-execution ////////////////////////////////////////////////////////////////////////////////////////////////////////
		
		Date startOfDailyInterval = measuredValueUtilService.resetToStartOfActualInterval(START, MeasuredValueInterval.DAILY).toDate()
		List<CsiAggregation> smvs = shopMeasuredValueService.getOrCalculateShopMeasuredValues(
			startOfDailyInterval,
			startOfDailyInterval,
			MeasuredValueInterval.findByIntervalInMinutes(MeasuredValueInterval.DAILY),
			[JobGroup.findByName('CSI')]
		)

		//assertions ////////////////////////////////////////////////////////////////////////////////////////////////////////
		List<CsiAggregation> allMvs = CsiAggregation.list()
		List<CsiAggregation> hourlyEventMvs = allMvs.findAll{ it.interval.intervalInMinutes == MeasuredValueInterval.HOURLY && it.aggregator.name == AggregatorType.MEASURED_EVENT}
		List<CsiAggregation> dailyPageMvs = allMvs.findAll{ it.interval.intervalInMinutes == MeasuredValueInterval.DAILY && it.aggregator.name == AggregatorType.PAGE}
		List<CsiAggregation> dailyShopMvs = allMvs.findAll{ it.interval.intervalInMinutes == MeasuredValueInterval.DAILY && it.aggregator.name == AggregatorType.SHOP}
		
		assertEquals(12, hourlyEventMvs.size())
		assertEquals(12, hourlyEventMvs.findAll{ it.isCalculatedWithData() }.size())
		
		int numberOfPagesInDb = Page.list().size()
		assertEquals(numberOfPagesInDb, dailyPageMvs.size())
		int numberOfPagesForWhichHmvExist = 2
		assertEquals(numberOfPagesForWhichHmvExist, dailyPageMvs.findAll{ it.isCalculatedWithData() }.size())
		
		assertEquals(1, dailyShopMvs.size())
		assertTrue(dailyShopMvs[0].isCalculatedWithData())
		assertEquals(expectedCsi, dailyShopMvs[0].csByWptDocCompleteInPercent, DELTA)
		assertEquals(1, smvs.size())
		assertTrue(smvs[0].isCalculatedWithData())
		assertEquals(expectedCsi, smvs[0].csByWptDocCompleteInPercent, DELTA)
		
		//duplicate hp-results which shouldn't change system-csi at all (should just improve accuracy of hp-proportion of csi) 
		hmvs.add(createMeasuredValue("1;${eventHomepage.ident().toString()};${pageHP_ID};${browserIE_ID};1", csiHpIe1))
		hmvs.add(createMeasuredValue("1;${eventHomepage.ident().toString()};${pageHP_ID};${browserIE_ID};1", csiHpIe2))
		hmvs.add(createMeasuredValue("1;${eventHomepage.ident().toString()};${pageHP_ID};${browserIE_ID};1", csiHpIe3))
		hmvs.add(createMeasuredValue("1;${eventHomepage.ident().toString()};${pageHP_ID};${browserFF_ID};1", csiHpFf1))
		hmvs.add(createMeasuredValue("1;${eventHomepage.ident().toString()};${pageHP_ID};${browserFF_ID};1", csiHpFf2))
		hmvs.add(createMeasuredValue("1;${eventHomepage.ident().toString()};${pageHP_ID};${browserFF_ID};1", csiHpFf3))
		
		CsiAggregation.list().findAll{ ! it.aggregator.name.equals(AggregatorType.MEASURED_EVENT) }*.delete(flush: true)
		
		smvs = shopMeasuredValueService.getOrCalculateShopMeasuredValues(
			startOfDailyInterval,
			startOfDailyInterval,
			MeasuredValueInterval.findByIntervalInMinutes(MeasuredValueInterval.DAILY),
			[JobGroup.findByName('CSI')]
		)
		assertEquals(1, smvs.size())
		assertTrue(smvs[0].isCalculatedWithData())
		assertEquals(expectedCsi, smvs[0].csByWptDocCompleteInPercent, DELTA)
		
    }
	
	CsiAggregation createMeasuredValue(String tag, double value){
		CsiAggregation mv = new CsiAggregation(
			started: START.toDate(),
			interval: MeasuredValueInterval.findByIntervalInMinutes(MeasuredValueInterval.HOURLY),
			aggregator: AggregatorType.findByName(AggregatorType.MEASURED_EVENT),
			tag: tag,
			csByWptDocCompleteInPercent: value,
			underlyingEventResultsByWptDocComplete: '1',
		).save(failOnError: true)
		new MeasuredValueUpdateEvent(
			dateOfUpdate: new Date(),
			measuredValueId: mv.ident(),
			updateCause: MeasuredValueUpdateEvent.UpdateCause.CALCULATED
		).save(failOnError: true)
		return mv
	}
	
	void createPages(){
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
}

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

import org.apache.commons.logging.LogFactory
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.junit.After
import org.junit.Before
import org.junit.Test

import de.iteratec.osm.report.chart.MeasuredValueUtilService
import de.iteratec.osm.measurement.schedule.Job
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.measurement.schedule.JobGroupType
import de.iteratec.osm.report.chart.AggregatorType
import de.iteratec.osm.report.chart.MeasurandGroup
import de.iteratec.osm.report.chart.MeasuredValue
import de.iteratec.osm.report.chart.MeasuredValueInterval
import de.iteratec.osm.result.CachedView
import de.iteratec.osm.result.EventResult
import de.iteratec.osm.result.JobResult
import de.iteratec.osm.result.JobResultService
import de.iteratec.osm.result.MeasuredEvent
import de.iteratec.osm.measurement.script.Script
import de.iteratec.osm.measurement.environment.Browser
import de.iteratec.osm.measurement.environment.Location
import de.iteratec.osm.measurement.environment.WebPageTestServer


/**
 * <p>
 * Until 2014-07 the domain {@link MeasuredValue} had an (enum-)attribute calculated. The enum had the following values:
 * <ul>
 * <li><b>Not</b>: Never calculated or outdated.</li>
 * <li><b>Yes</b>: </li>Calculated based on existing data and not outdated afterwards.
 * <li><b>YesNoData</b>: Calculated without data and not outdated afterwards.</li>
 * </ul>
 * The {@link MeasuredValue}s get outdated if new {@link EventResult}s arrive and get calculated if somebody opens a csi-related dashboard and so requests and calculates {@link MeasuredValue}s.
 * Until 2014-07 obsolescence and calculation had to read and write the same object (the respective MeasuredValue). This led to org.hibernate.StaleObjectStateException's if both happened with a high frequency (some users
 * opened the csi-dashboard on monitors, auto-refreshing the page).
 * The test in this class failed due to thrown org.hibernate.StaleObjectStateException on executing <br><code>MeasuredValue.list()*.delete(failOnError: true, flush: true)</code><br>
 * Shouldn't happen after removing the attribute calculated from domain {@link MeasuredValue} and introduction of domain {@link MesauredValueUpdateEvent} instead.  
 * </p>
 *  
 * @author nkuhn
 * @see MeasuredValueUpdateEvent 
 */
class HighfrequencyMeasuredValueUpdateIntSpec extends IntTestWithDBCleanup {
	
	static transactional = false
	
	MeasuredValueUpdateService measuredValueUpdateService
	PageMeasuredValueService pageMeasuredValueService
	MeasuredValueUtilService measuredValueUtilService 
	def log = LogFactory.getLog(getClass())
	
	private static final aTuesday = new DateTime(2014,6,3,0,0,0, DateTimeZone.UTC)
	private static final fridayBeforeTuesday = new DateTime(2014,5,30,0,0,0, DateTimeZone.UTC)

	@Before
    void setUp() {
		
		//no clue why the criteria in the following service-method doesn't work in this integration test :-(
		JobResultService.metaClass.findJobResultByEventResult = {EventResult eventResult ->
			JobResult jobResultToReturn
			JobResult.list().each {jobResult ->
				if(jobResult.eventResults*.ident().contains(eventResult.ident())) {
					jobResultToReturn = jobResult
				}
			}
			return jobResultToReturn
		}

		Page page = new Page(name: 'HP', weight: 0.8).save(failOnError: true, flush: true)
		new MeasuredEvent(name: 'event', testedPage: page).save(failOnError: true, flush: true)
		JobGroup jobGroup = new JobGroup(name: 'group', groupType: JobGroupType.CSI_AGGREGATION).save(failOnError: true, flush: true)
		new AggregatorType(name: AggregatorType.MEASURED_EVENT, measurandGroup: MeasurandGroup.NO_MEASURAND).save(failOnError: true, flush: true)
		new AggregatorType(name: AggregatorType.PAGE, measurandGroup: MeasurandGroup.NO_MEASURAND).save(failOnError: true, flush: true)
		new AggregatorType(name: AggregatorType.SHOP, measurandGroup: MeasurandGroup.NO_MEASURAND).save(failOnError: true, flush: true)
		new MeasuredValueInterval(name: 'raw', intervalInMinutes: MeasuredValueInterval.RAW).save(failOnError: true, flush: true)
		new MeasuredValueInterval(name: 'hourly', intervalInMinutes: MeasuredValueInterval.HOURLY).save(failOnError: true, flush: true)
		new MeasuredValueInterval(name: 'daily', intervalInMinutes: MeasuredValueInterval.DAILY).save(failOnError: true, flush: true)
		new MeasuredValueInterval(name: 'weekly', intervalInMinutes: MeasuredValueInterval.WEEKLY).save(failOnError: true, flush: true)
		Script script = new Script(
			label: 'script',
			description: 'script',
			navigationScript: 'script',
			provideAuthenticateInformation: false
		).save(failOnError: true, flush: true)
		WebPageTestServer server = new WebPageTestServer(
			label: 'server',
			proxyIdentifier: 'proxyIdentifier',
			active: true,
			baseUrl: 'http://my-url.com'
		).save(failOnError: true, flush: true)
		Browser browser = new Browser(
			name: 'browser',
			weight: 0.8
		).save(failOnError: true, flush: true)
		Location location = new Location(
			label: 'location',
			active: true,
			valid: 1,
			wptServer: server,
			location: 'location',
			browser: browser
		).save(failOnError: true, flush: true)
		new Job(
			label: 'job',
			script: script,
			location: location,
			jobGroup: jobGroup,
			description: 'job',
			runs: 1,
			active: false,
			maxDownloadTimeInMinutes: 60
		).save(failOnError: true, flush: true)
    }

	@After
    void tearDown() {
    }

	@Test
    void testPersistingNewEventResultsWhileManyMeasuredValueCalculationsOccur() {
		
		MeasuredValueCalculator mvCalculator = new MeasuredValueCalculator()
		mvCalculator.start(pageMeasuredValueService, log)
		
		DateTime startOfDay = measuredValueUtilService.resetToStartOfActualInterval(aTuesday, MeasuredValueInterval.DAILY)
		DateTime startOfWeek = measuredValueUtilService.resetToStartOfActualInterval(aTuesday, MeasuredValueInterval.WEEKLY)
		JobResult jobResult = new JobResult(
			testId: 'testId',
			date: aTuesday.toDate(),
			wptStatus: 'wptStatus',
			httpStatusCode: 200,
			job: Job.findByLabel('job'),
			description: 'description',
			jobConfigLabel: 'job',
			jobConfigRuns: 1,
			jobGroupName: 'group'
		).save(failOnError: true)
		
		100.times{index ->
			Thread.sleep(10)
			EventResult.withTransaction {status ->
				
				EventResult eventResult = new EventResult(
						docCompleteTimeInMillisecs: 1000,
						wptStatus: 200,
						medianValue: true,
						numberOfWptRun: 1,
						cachedView: CachedView.UNCACHED,
						speedIndex: 1,
						jobResultDate: aTuesday.toDate(),
						jobResultJobConfigId: 1,
						measuredEvent: MeasuredEvent.findByName('event'),
						).save(failOnError: true, flush: true)
						
						jobResult.eventResults.add(eventResult)
						jobResult.save(failOnError: true, flush: true)
						
						//log.error "marking daily mv's with result nr. $index : startOfDay=$startOfDay result-date=$eventResult.jobResultDate"
						measuredValueUpdateService.markMvs(startOfDay, eventResult, MeasuredValueInterval.findByIntervalInMinutes(MeasuredValueInterval.DAILY))
						//log.error "marking weekly mv's with result nr. $index : startOfWee=$startOfWeek result-date=$eventResult.jobResultDate"
						measuredValueUpdateService.markMvs(startOfWeek, eventResult, MeasuredValueInterval.findByIntervalInMinutes(MeasuredValueInterval.WEEKLY))
			}
		}
		
		mvCalculator.stop()
		
		List<MeasuredValue> mvs = MeasuredValue.list()
		List<MeasuredValue> pmvs = mvs.findAll{it.aggregator.name==AggregatorType.PAGE}
		List<MeasuredValue> smvs = mvs.findAll{it.aggregator.name==AggregatorType.SHOP}
		mvs.each {mv ->
			//log.error '*************************'
			//log.error mv.started
			//log.error mv.interval.name
			//log.error mv.aggregator.name
			//log.error mv.tag
			//log.error mv.value
			//log.error mv.resultIds
			//log.error mv.isCalculated()
		}
		
		assertThat(mvs.size(), is(6))
		assertThat(pmvs.size(), is(4))
		assertThat(smvs.size(), is(2))
		
		MeasuredValue.list()*.delete(failOnError: true, flush: true)
		
    }
	
	public static getEventResult(){
		return new EventResult()
	}
	
	/**
	 * This thread retrieves daily and weekly-Page-{@link MeasuredValue}s continuously every 10 ms.
	 * While retrieving these values get calculated.
	 * @author nkuhn
	 *
	 */
	class MeasuredValueCalculator implements Runnable{
		
		private volatile Thread calculatorThread
		private PageMeasuredValueService pageMVService
		private def log

		public MeasuredValueCalculator(){}
		
		public void start(PageMeasuredValueService pageMeasuredValueService, outerLog){
			calculatorThread = new Thread(this)
			pageMVService = pageMeasuredValueService
			log = outerLog
			calculatorThread.start()
		}
		public void stop(){
			calculatorThread = null
		}
		public void run(){
			Thread thisThread = Thread.currentThread();
	        while (calculatorThread == thisThread) {
	            try {
	                Thread.sleep(10)
	            } catch (InterruptedException e){
	            }
				log.error "getting daily page-mv's from calculator-thread:"
				MeasuredValue.withTransaction {status ->
					 List<JobGroup> groups = JobGroup.findAllByName('group')
					 List<Page> pages = Page.findAllByName('HP')
					//daily page
					List<MeasuredValue> pmvs =  pageMVService.getOrCalculatePageMeasuredValues(
						HighfrequencyMeasuredValueUpdateIntSpec.aTuesday.minusDays(1).toDate(), 
						HighfrequencyMeasuredValueUpdateIntSpec.aTuesday.plusDays(1).toDate(), 
						MeasuredValueInterval.findByIntervalInMinutes(MeasuredValueInterval.DAILY), 
						groups, 
						pages
					)
					pmvs.each {
						log.error "read pmv: ${it.ident()}. Calculated? -> ${it.isCalculated()}"
					}
					//weekly page
					log.error "getting weekly page-mv's from calculator-thread:"
					pmvs =  pageMVService.getOrCalculatePageMeasuredValues(
						HighfrequencyMeasuredValueUpdateIntSpec.fridayBeforeTuesday.toDate(),
						HighfrequencyMeasuredValueUpdateIntSpec.fridayBeforeTuesday.toDate(),
						MeasuredValueInterval.findByIntervalInMinutes(MeasuredValueInterval.WEEKLY),
						groups,
						pages
					)
					pmvs.each {
						log.error "read pmv: ${it.ident()}. Calculated? -> ${it.isCalculated()}"
					}
					
				}
	        }
		}
	}
}

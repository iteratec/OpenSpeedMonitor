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

import grails.test.mixin.integration.Integration
import grails.transaction.Rollback
import org.apache.commons.logging.LogFactory
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import de.iteratec.osm.report.chart.CsiAggregationUtilService
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.report.chart.CsiAggregation
import de.iteratec.osm.report.chart.CsiAggregationInterval
import de.iteratec.osm.result.EventResult
import de.iteratec.osm.result.MeasuredEvent
import de.iteratec.osm.measurement.script.Script
import de.iteratec.osm.measurement.environment.Browser
import de.iteratec.osm.measurement.environment.Location
import de.iteratec.osm.measurement.environment.WebPageTestServer
import spock.lang.Ignore


/**
 * <p>
 * Until 2014-07 the domain {@link CsiAggregation} had an (enum-)attribute calculated. The enum had the following values:
 * <ul>
 * <li><b>Not</b>: Never calculated or outdated.</li>
 * <li><b>Yes</b>: </li>Calculated based on existing data and not outdated afterwards.
 * <li><b>YesNoData</b>: Calculated without data and not outdated afterwards.</li>
 * </ul>
 * The {@link CsiAggregation}s get outdated if new {@link EventResult}s arrive and get calculated if somebody opens a csi-related dashboard and so requests and calculates {@link CsiAggregation}s.
 * Until 2014-07 obsolescence and calculation had to read and write the same object (the respective CsiAggregation). This led to org.hibernate.StaleObjectStateException's if both happened with a high frequency (some users
 * opened the csi-dashboard on monitors, auto-refreshing the page).
 * The test in this class failed due to thrown org.hibernate.StaleObjectStateException on executing <br><code>CsiAggregation.list()*.delete(failOnError: true, flush: true)</code><br>
 * Shouldn't happen after removing the attribute calculated from domain {@link CsiAggregation} and introduction of domain {@link CsiAggregationUpdateEvent} instead.
 * </p>
 *
 * @author nkuhn
 * @see CsiAggregationUpdateEvent
 */
@Integration
@Rollback
class HighfrequencyCsiAggregationUpdateIntSpec extends NonTransactionalIntegrationSpec {

    CsiAggregationUpdateService csiAggregationUpdateService
    PageCsiAggregationService pageCsiAggregationService
    CsiAggregationUtilService csiAggregationUtilService
    def log = LogFactory.getLog(getClass())

    private static final aTuesday = new DateTime(2014, 6, 3, 0, 0, 0, DateTimeZone.UTC)
    private static final fridayBeforeTuesday = new DateTime(2014, 5, 30, 0, 0, 0, DateTimeZone.UTC)

    def setup() {

        /*/no clue why the criteria in the following service-method doesn't work in this integration test :-(
        JobResultService.metaClass.findJobResultByEventResult = { EventResult eventResult ->
            JobResult jobResultToReturn
            JobResult.list().each {jobResult ->
                if(jobResult.getEventResults()*.ident().contains(eventResult.ident())) {
                    jobResultToReturn = jobResult
                }
            }
            return jobResultToReturn
        }
        //*/

        Page page = new Page(name: 'HP').save(failOnError: true, flush: true)
        new MeasuredEvent(name: 'event', testedPage: page).save(failOnError: true, flush: true)
//		look at ToDo below
//		JobGroup jobGroup = new JobGroup(name: 'group', groupType: JobGroupType.CSI_AGGREGATION).save(failOnError: true, flush: true)
        new CsiAggregationInterval(name: 'raw', intervalInMinutes: CsiAggregationInterval.RAW).save(failOnError: true, flush: true)
        new CsiAggregationInterval(name: 'hourly', intervalInMinutes: CsiAggregationInterval.HOURLY).save(failOnError: true, flush: true)
        new CsiAggregationInterval(name: 'daily', intervalInMinutes: CsiAggregationInterval.DAILY).save(failOnError: true, flush: true)
        new CsiAggregationInterval(name: 'weekly', intervalInMinutes: CsiAggregationInterval.WEEKLY).save(failOnError: true, flush: true)
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
                baseUrl: 'http://my-url.com',
                dateCreated: new Date(),
                lastUpdated: new Date()
        ).save(failOnError: true, flush: true)
        Browser browser = new Browser(name: 'browser').save(failOnError: true, flush: true)
        Location location = new Location(
                label: 'location',
                active: true,
                wptServer: server,
                location: 'location',
                browser: browser,
                dateCreated: new Date(),
                lastUpdated: new Date()
        ).save(failOnError: true, flush: true)
//		look at ToDo below
//		new Job(
//				label: 'job',
//				script: script,
//				location: location,
//				jobGroup: jobGroup,
//				description: 'job',
//				runs: 1,
//				active: false,
//				maxDownloadTimeInMinutes: 60
//		).save(failOnError: true, flush: true)
    }

    @Ignore
    def testPersistingNewEventResultsWhileManyCsiAggregationCalculationsOccur() {

        /*TODO: enable this test again
         * If this test runs in front of ShopCsiAggregationCalculationIntSpec,
         * the ShopCsiAggregationCalculationIntSpec has a AssertionError and fails.
         * The dependencies based on JobGroup creation in this Test.
         *
         * This test fails when it is running in integration test sequence,
         * the dependency belongs on ?
         */
        expect:
        true
//		CsiAggregationCalculator mvCalculator = new CsiAggregationCalculator()
//		mvCalculator.start(pageCsiAggregationService, log)
//
//		DateTime startOfDay = csiAggregationUtilService.resetToStartOfActualInterval(aTuesday, CsiAggregationInterval.DAILY)
//		DateTime startOfWeek = csiAggregationUtilService.resetToStartOfActualInterval(aTuesday, CsiAggregationInterval.WEEKLY)
//		JobResult jobResult = new JobResult(
//				testId: 'testId',
//				date: aTuesday.toDate(),
//				wptStatus: 'wptStatus',
//				httpStatusCode: 200,
//				job: Job.findByLabel('job'),
//				description: 'description',
//				jobConfigLabel: 'job',
//				jobConfigRuns: 1,
//				jobGroupName: 'group'
//		).save(failOnError: true)
//
//		100.times { index ->
//			Thread.sleep(10)
//			EventResult.withTransaction { status ->
//
//				EventResult eventResult = new EventResult(
//						loadTimeInMillisecs: 1000,
//						wptStatus: 200,
//						medianValue: true,
//						numberOfWptRun: 1,
//						cachedView: CachedView.UNCACHED,
//						speedIndex: 1,
//						jobResult: jobResult,
//						jobResultDate: aTuesday.toDate(),
//						jobResultJobConfigId: 1,
//						measuredEvent: MeasuredEvent.findByName('event'),
//				).save(failOnError: true, flush: true)
//
//				jobResult.save(failOnError: true, flush: true)
//
//				//log.error "marking daily mv's with result nr. $index : startOfDay=$startOfDay result-date=$eventResult.jobResultDate"
//				csiAggregationUpdateService.markMvs(startOfDay, eventResult, CsiAggregationInterval.findByIntervalInMinutes(CsiAggregationInterval.DAILY))
//				//log.error "marking weekly mv's with result nr. $index : startOfWee=$startOfWeek result-date=$eventResult.jobResultDate"
//				csiAggregationUpdateService.markMvs(startOfWeek, eventResult, CsiAggregationInterval.findByIntervalInMinutes(CsiAggregationInterval.WEEKLY))
//			}
//		}
//
//		mvCalculator.stop()
//
//		List<CsiAggregation> mvs = CsiAggregation.list()
//		List<CsiAggregation> pmvs = mvs.findAll { it.aggregator.name == AggregatorType.PAGE }
//		List<CsiAggregation> smvs = mvs.findAll { it.aggregator.name == AggregatorType.SHOP }
////		mvs.each { mv ->
//			//log.error '*************************'
//			//log.error mv.started
//			//log.error mv.interval.name
//			//log.error mv.aggregator.name
//			//log.error mv.tag
//			//log.error mv.value
//			//log.error mv.resultIds
//			//log.error mv.isCalculated()
////		}
//
//		assertThat(mvs.size(), is(6))
//		assertThat(pmvs.size(), is(4))
//		assertThat(smvs.size(), is(2))
//
//		CsiAggregation.list()*.delete(failOnError: true, flush: true)

    }

    public static getEventResult() {
        return new EventResult()
    }
}

/**
 * This thread retrieves daily and weekly-Page-{@link CsiAggregation}s continuously every 10 ms.
 * While retrieving these values get calculated.
 * @author nkuhn
 *
 */
class CsiAggregationCalculator implements Runnable {

    private volatile Thread calculatorThread
    private PageCsiAggregationService pageMVService
    private def log

    public CsiAggregationCalculator() {}

    public void start(PageCsiAggregationService pageCsiAggregationService, outerLog) {
        calculatorThread = new Thread(this)
        pageMVService = pageCsiAggregationService
        log = outerLog
        calculatorThread.start()
    }

    public void stop() {
        calculatorThread = null
    }

    public void run() {
        Thread thisThread = Thread.currentThread();
        while (calculatorThread == thisThread) {
            try {
                Thread.sleep(10)
            } catch (InterruptedException e) {
            }
            log.error "getting daily page-mv's from calculator-thread:"
            CsiAggregation.withTransaction { status ->
                List<JobGroup> groups = JobGroup.findAllByName('group')
                List<Page> pages = Page.findAllByName('HP')
                //daily page
                List<CsiAggregation> pmvs = pageMVService.getOrCalculatePageCsiAggregations(
                        HighfrequencyCsiAggregationUpdateIntSpec.aTuesday.minusDays(1).toDate(),
                        HighfrequencyCsiAggregationUpdateIntSpec.aTuesday.plusDays(1).toDate(),
                        CsiAggregationInterval.findByIntervalInMinutes(CsiAggregationInterval.DAILY),
                        groups,
                        pages
                )
                pmvs.each {
                    log.error "read pmv: ${it.ident()}. Calculated? -> ${it.isCalculated()}"
                }
                //weekly page
                log.error "getting weekly page-mv's from calculator-thread:"
                pmvs = pageMVService.getOrCalculatePageCsiAggregations(
                        HighfrequencyCsiAggregationUpdateIntSpec.fridayBeforeTuesday.toDate(),
                        HighfrequencyCsiAggregationUpdateIntSpec.fridayBeforeTuesday.toDate(),
                        CsiAggregationInterval.findByIntervalInMinutes(CsiAggregationInterval.WEEKLY),
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

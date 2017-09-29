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

import de.iteratec.osm.OsmConfiguration
import de.iteratec.osm.measurement.environment.Browser
import de.iteratec.osm.measurement.environment.Location
import de.iteratec.osm.measurement.environment.WebPageTestServer
import de.iteratec.osm.measurement.environment.wptserverproxy.ResultPersisterService
import de.iteratec.osm.measurement.schedule.ConnectivityProfile
import de.iteratec.osm.measurement.schedule.Job
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.report.chart.AggregationType
import de.iteratec.osm.report.chart.CsiAggregation
import de.iteratec.osm.report.chart.CsiAggregationInterval
import de.iteratec.osm.result.CachedView
import de.iteratec.osm.result.EventResult
import de.iteratec.osm.result.JobResult
import de.iteratec.osm.result.MeasuredEvent
import de.iteratec.osm.util.PerformanceLoggingService
import grails.test.mixin.integration.Integration
import grails.transaction.Rollback
import org.apache.commons.logging.LogFactory
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import spock.lang.Shared
import spock.lang.Specification

import static spock.util.matcher.HamcrestMatchers.closeTo
import static spock.util.matcher.HamcrestSupport.that

@Integration
@Rollback
class WeeklyJobGroupIntTests extends NonTransactionalIntegrationSpec {

    def log = LogFactory.getLog(getClass())

    JobGroupCsiAggregationService jobGroupCsiAggregationService
    ResultPersisterService resultPersisterService

    CsiAggregationInterval weekly
    Map<String, Location> locations
    Long jobGroupId
    Map<String, MeasuredEvent> measuredEvents
    Map<String, Page> pages
    Map<String, Browser> browsers

    static final double DELTA = 1e-15

    def setup() {
        createTestDataCommonToAllTests()
    }

    void "Without EventResults a JobGroup CsiAggregation get created but csByWptDocCompleteInPercent is still null"() {

        setup: "Given a weekly JobGroup CsiAggregation with a date without EventResults."
        Date startDateWithoutResults = new DateTime(2012, 01, 12, 0, 0, DateTimeZone.UTC).toDate()
        CsiAggregation csiAggWeeklyShop
        CsiAggregation.withNewTransaction {
            csiAggWeeklyShop = CsiAggregation.build(
                started: startDateWithoutResults,
                interval: weekly,
                aggregationType: AggregationType.JOB_GROUP,
                jobGroup: JobGroup.get(jobGroupId),
                csByWptDocCompleteInPercent: null,
                underlyingEventResultsByWptDocComplete: ''
            )
        }

        when: "It get calculated."
        jobGroupCsiAggregationService.calcCsiAggregations([csiAggWeeklyShop.id])

        then: "It is marked as calculated but csByWptDocCompleteInPercent is still null afterwards."
        csiAggWeeklyShop.started == startDateWithoutResults
        csiAggWeeklyShop.interval.intervalInMinutes == weekly.intervalInMinutes
        csiAggWeeklyShop.aggregationType == AggregationType.JOB_GROUP
        csiAggWeeklyShop.jobGroupId == jobGroupId
        csiAggWeeklyShop.isCalculated()
        csiAggWeeklyShop.countUnderlyingEventResultsByWptDocComplete() == 0
        csiAggWeeklyShop.csByWptDocCompleteInPercent == null

    }

    /**
     * Tests the calculation of one weekly-JobGroup-{@link CsiAggregation}. Databasis for calculation are weekly page-{@link CsiAggregation}s. These get calculated
     * on-the-fly while calculating the respective weekly-JobGroup-{@link CsiAggregation}. The hourly-event-{@link CsiAggregation}s of the period have to exist (they
     * won't get calculated on-the-fly. Therefore these get precalculated in test here from {@link EventResult}s. These are constructed from csv data.
     *
     * Expected values got calculated by script ./calcJobGroupCsiFromCsvData.groovy
     */
    void "With EventResults a JobGroup CsiAggregation get created and calculation provides a valid csByWptDocCompleteInPercent"() {

        setup: "Given a weekly JobGroup CsiAggregation with a date with EventResults."
        createResultDataFromCsv()
        Date startDateWithData = new DateTime(2012, 11, 12, 0, 0, DateTimeZone.UTC).toDate()
        Double avgOfAllHourlyAvgsOfStep01 = 0.8764922727272727d
        Double avgOfAllHourlyAvgsOfStep02 = 0.7986763636363636d
        Double avgOfAllHourlyAvgsOfStep03 = 0.6526654545454545d
        Double avgOfAllHourlyAvgsOfStep04 = 0.5649671428571429d
        Double avgOfAllHourlyAvgsOfStep05 = 0.45301416666666655d
        Double avgOfAllHourlyAvgsOfStep06 = 0.3819126666666667d
        Double totalAvg = (
                avgOfAllHourlyAvgsOfStep01+
                avgOfAllHourlyAvgsOfStep02+
                avgOfAllHourlyAvgsOfStep03+
                avgOfAllHourlyAvgsOfStep04+
                avgOfAllHourlyAvgsOfStep05+
                avgOfAllHourlyAvgsOfStep06) / 6
        Double expectedValue = totalAvg
        long csiAggregationId
        CsiAggregation.withNewTransaction {
            CsiAggregation aggregation = CsiAggregation.build(
                started: startDateWithData,
                interval: weekly,
                aggregationType: AggregationType.JOB_GROUP,
                jobGroup: JobGroup.get(jobGroupId),
                csByWptDocCompleteInPercent: null,
                underlyingEventResultsByWptDocComplete: ''
            )
            csiAggregationId = aggregation.id
        }

        when: "It get calculated."
        CsiAggregation.withNewSession { session ->
            jobGroupCsiAggregationService.calcCsiAggregations([csiAggregationId])
            session.flush()
        }
        CsiAggregation csiAggWeeklyShop = CsiAggregation.get(csiAggregationId)

        then: "It is marked as calculated and has a valid csByWptDocCompleteInPercent."
        csiAggWeeklyShop.started == startDateWithData
        csiAggWeeklyShop.interval.intervalInMinutes == weekly.intervalInMinutes
        csiAggWeeklyShop.aggregationType == AggregationType.JOB_GROUP
        csiAggWeeklyShop.jobGroup.id == jobGroupId
        csiAggWeeklyShop.isCalculated()
        csiAggWeeklyShop.csByWptDocCompleteInPercent != null
        Double calculated = csiAggWeeklyShop.csByWptDocCompleteInPercent
        that calculated, closeTo(expectedValue, DELTA)
    }

    private createTestDataCommonToAllTests() {
        OsmConfiguration.build()
        jobGroupId = JobGroup.build().ident()
        CsiAggregation.withNewTransaction {
            CsiAggregationInterval.build(
                name: "hourly",
                intervalInMinutes: CsiAggregationInterval.HOURLY
            )
            CsiAggregationInterval.build(
                name: "daily",
                intervalInMinutes: CsiAggregationInterval.DAILY
            )
            weekly = CsiAggregationInterval.build(
                name: "weekly",
                intervalInMinutes: CsiAggregationInterval.WEEKLY
            )
        }
    }

    private createResultDataFromCsv() {
        CsiAggregation.withNewTransaction {

            browsers = [
                "IE8": Browser.build(name: "IE8"),
                "FF": Browser.build(name: "FF")
            ]
            locations = [
                "IE8": Location.build(label: "IE8",browser: browsers["IE8"]),
                "FF": Location.build(label: "FF",browser: browsers["FF"])
            ]
            measuredEvents = [
                'Step01': MeasuredEvent.build(name: 'step01'),
                'Step02': MeasuredEvent.build(name: 'step02'),
                'Step03': MeasuredEvent.build(name: 'step3'),
                'Step04': MeasuredEvent.build(name: 'step04'),
                'Step05': MeasuredEvent.build(name: 'step05'),
                'Step06': MeasuredEvent.build(name: 'step06')
            ]
            pages = [
                    'Step01': Page.build(name: 'step01'),
                    'Step02': Page.build(name: 'step02'),
                    'Step03': Page.build(name: 'step03'),
                    'Step04': Page.build(name: 'step04'),
                    'Step05': Page.build(name: 'step05'),
                    'Step06': Page.build(name: 'step06')
            ]

            ConnectivityProfile profile = ConnectivityProfile.build()
            Job job = Job.build()

            new File("src/test/resources/CsiData/weekly_page.csv").eachLine {String csvLine ->
                if (!isHeaderLine(csvLine)){

                    List<String> csvFields = csvLine.split(';')

                    String browserAndLocation = csvFields[0]
                    String pageAndMeasuredEvent = csvFields[1]
                    String docCompleteTime = csvFields[7]
                    String customerSatisfaction = csvFields[8]
                    Date dateOfJobRun = new Date(csvFields[3] + " " + csvFields[4]);

                    log.info("logged from test")

                    JobResult jobResult = JobResult.build(
                        date: dateOfJobRun,
                        job: job
                    )
                    EventResult.build(
                            cachedView: CachedView.UNCACHED,
                            numberOfWptRun: 1,
                            wptStatus: 200,
                            medianValue: true,
                            docCompleteTimeInMillisecs: docCompleteTime ? Integer.valueOf(docCompleteTime) : null,
                            csByWptDocCompleteInPercent: customerSatisfaction ? Double.valueOf(customerSatisfaction) : null,
                            connectivityProfile: profile,
                            jobResult: jobResult,
                            jobResultDate: dateOfJobRun,
                            jobGroup: JobGroup.get(jobGroupId),
                            measuredEvent:measuredEvents[pageAndMeasuredEvent],
                            page: pages[pageAndMeasuredEvent],
                            location: locations[browserAndLocation],
                            browser: browsers[browserAndLocation]
                    )
                }
            }

        }
        CsiAggregation.withNewTransaction {
            CsiConfiguration csiConfiguration = CsiConfiguration.build()
            ConnectivityProfile.findAll().each { connectivityProfile ->
                Browser.findAll().each { browser ->
                    csiConfiguration.browserConnectivityWeights.
                            add(new BrowserConnectivityWeight(browser: browser, connectivity: connectivityProfile, weight: 1))
                }
                Page.findAll().each { page ->
                    csiConfiguration.pageWeights.add(new PageWeight(page: page, weight: 1))
                }
            }
            JobGroup jobGroup = JobGroup.get(jobGroupId)
            jobGroup.csiConfiguration = csiConfiguration
            jobGroup.save(failOnError: true)
        }
        CsiAggregation.withNewTransaction {
            EventResult.findAll().each {
                resultPersisterService.informDependentCsiAggregations(it)
            }
        }
    }

    private boolean isHeaderLine(String csvLine) {
        return csvLine.startsWith('location;')
    }

}

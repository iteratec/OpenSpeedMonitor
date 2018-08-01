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
import de.iteratec.osm.measurement.environment.wptserverproxy.ResultPersisterService
import de.iteratec.osm.measurement.schedule.ConnectivityProfile
import de.iteratec.osm.measurement.schedule.Job
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.report.chart.AggregationType
import de.iteratec.osm.report.chart.CsiAggregation
import de.iteratec.osm.report.chart.CsiAggregationInterval
import de.iteratec.osm.result.CachedView
import de.iteratec.osm.result.JobResult
import de.iteratec.osm.result.MeasuredEvent
import de.iteratec.osm.result.WptStatus
import de.iteratec.osm.result.EventResult
import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import org.joda.time.DateTime

import static spock.util.matcher.HamcrestMatchers.closeTo
import static spock.util.matcher.HamcrestSupport.that

@Integration(applicationClass = openspeedmonitor.Application.class)
@Rollback
class WeeklyPageIntegrationSpec extends NonTransactionalIntegrationSpec {

    PageCsiAggregationService pageCsiAggregationService
    ResultPersisterService resultPersisterService

    CsiAggregationInterval weekly
    Map<String, Location> locations
    Long jobGroupId
    Map<String, MeasuredEvent> measuredEvents
    Map<String, Page> pages
    Map<String, Browser> browsers

    static final DateTime START_OF_WEEK = new DateTime(2012, 11, 12, 0, 0, 0)
    static final double DELTA = 1e-15

    def setup() {

        createTestDataCommonToAllTests()

    }

    /**
     * Tests the calculation of weekly-Page-{@link CsiAggregation}. Databasis for calculation are
     * hourly-event-{@link CsiAggregation}s of the period. They won't get calculated on-the-fly.
     * Therefore these get precalculated in test here from {@link EventResult}s. These are constructed from csv data.
     *
     * Expected values got calculated by script ./calcJobGroupCsiFromCsvData.groovy
     */
    void "For a week with EventResults calculating weekly page csiAgg for Page #pageName provides an agg with a valid csByWptDocCompleteInPercent"() {

        given: "A new weekly page CsiAgg for a week with existing EventResults."
        createResultDataFromCsv()
        Date startDate = START_OF_WEEK.toDate()
        long csiAggregationId
        CsiAggregation aggregation = CsiAggregation.build(
                started: startDate,
                interval: weekly,
                aggregationType: AggregationType.PAGE,
                jobGroup: JobGroup.get(jobGroupId),
                csByWptDocCompleteInPercent: null,
                underlyingEventResultsByWptDocComplete: '',
                page: Page.findByName(pageName)
        )
        csiAggregationId = aggregation.id

        when: "We calculate the CsiAgg."
        pageCsiAggregationService.calcCsiAggregations([csiAggregationId])
        CsiAggregation csiAggWeeklyPage = CsiAggregation.get(csiAggregationId)

        then: "There should be the correct value for csByWptDocCompleteInPercent."
        csiAggWeeklyPage.started == startDate
        csiAggWeeklyPage.interval.intervalInMinutes == weekly.intervalInMinutes
        csiAggWeeklyPage.aggregationType == AggregationType.PAGE
        csiAggWeeklyPage.isCalculated()
        that csiAggWeeklyPage.csByWptDocCompleteInPercent, closeTo(expectedValue, DELTA)

        where:
        pageName | expectedValue
        'Step01' | 0.8764922727272727d
        'Step02' | 0.7986763636363636d
        'Step03' | 0.6526654545454545d
    }

    private createTestDataCommonToAllTests() {
        OsmConfiguration.build()
        jobGroupId = JobGroup.build().ident()
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
    private createResultDataFromCsv() {

            browsers = [
                    "IE8": Browser.build(name: "IE8"),
                    "FF": Browser.build(name: "FF")
            ]
            locations = [
                    "IE8": Location.build(label: "IE8",browser: browsers["IE8"]),
                    "FF": Location.build(label: "FF",browser: browsers["FF"])
            ]
            measuredEvents = [
                    'Step01': MeasuredEvent.build(name: 'Step01'),
                    'Step02': MeasuredEvent.build(name: 'Step02'),
                    'Step03': MeasuredEvent.build(name: 'Step3'),
                    'Step04': MeasuredEvent.build(name: 'Step04'),
                    'Step05': MeasuredEvent.build(name: 'Step05'),
                    'Step06': MeasuredEvent.build(name: 'Step06')
            ]
            pages = [
                    'Step01': Page.build(name: 'Step01'),
                    'Step02': Page.build(name: 'Step02'),
                    'Step03': Page.build(name: 'Step03'),
                    'Step04': Page.build(name: 'Step04'),
                    'Step05': Page.build(name: 'Step05'),
                    'Step06': Page.build(name: 'Step06')
            ]

            ConnectivityProfile profile = ConnectivityProfile.build()
            JobGroup jobGroup = JobGroup.get(jobGroupId)
            Job job = Job.build(jobGroup: jobGroup)

            new File("src/test/resources/CsiData/weekly_page.csv").eachLine {String csvLine ->
                if (!isHeaderLine(csvLine)){

                    List<String> csvFields = csvLine.split(';')

                    String browserAndLocation = csvFields[0]
                    String pageAndMeasuredEvent = csvFields[1]
                    String docCompleteTime = csvFields[7]
                    String customerSatisfaction = csvFields[8]
                    Date dateOfJobRun = new Date(csvFields[3] + " " + csvFields[4]);

                    JobResult jobResult = JobResult.build(
                            date: dateOfJobRun,
                            job: job
                    )
                    EventResult.build(
                            cachedView: CachedView.UNCACHED,
                            numberOfWptRun: 1,
                            wptStatus: WptStatus.COMPLETED.getWptStatusCode(),
                            medianValue: true,
                            docCompleteTimeInMillisecs: docCompleteTime ? Integer.valueOf(docCompleteTime) : null,
                            csByWptDocCompleteInPercent: customerSatisfaction ? Double.valueOf(customerSatisfaction) : null,
                            connectivityProfile: profile,
                            jobResult: jobResult,
                            jobResultDate: dateOfJobRun,
                            jobGroup: jobGroup,
                            measuredEvent:measuredEvents[pageAndMeasuredEvent],
                            page: pages[pageAndMeasuredEvent],
                            location: locations[browserAndLocation],
                            browser: browsers[browserAndLocation]
                    )
                }

        }
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
        jobGroup = JobGroup.get(jobGroupId)
            jobGroup.csiConfiguration = csiConfiguration
            jobGroup.save(failOnError: true)
            EventResult.findAll().each {
                resultPersisterService.informDependentCsiAggregations(it)
            }
    }

    private boolean isHeaderLine(String csvLine) {
        return csvLine.startsWith('location;')
    }
}

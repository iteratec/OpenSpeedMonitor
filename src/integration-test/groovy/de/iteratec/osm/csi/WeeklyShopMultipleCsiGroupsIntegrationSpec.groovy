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
import org.joda.time.DateTime
import org.springframework.test.annotation.Rollback
import grails.test.mixin.integration.Integration
import de.iteratec.osm.OsmConfiguration
import de.iteratec.osm.measurement.environment.Browser
import de.iteratec.osm.measurement.environment.wptserverproxy.ResultPersisterService
import de.iteratec.osm.measurement.schedule.ConnectivityProfile
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.report.chart.AggregationType
import de.iteratec.osm.report.chart.CsiAggregation
import de.iteratec.osm.report.chart.CsiAggregationInterval
import de.iteratec.osm.result.EventResult
import de.iteratec.osm.result.JobResult
import de.iteratec.osm.result.MeasuredEvent

@Integration
@Rollback
class WeeklyShopMultipleCsiGroupsIntegrationSpec extends NonTransactionalIntegrationSpec {

    JobGroupCsiAggregationService jobGroupCsiAggregationService
    ResultPersisterService resultPersisterService

    static CsiAggregationInterval weeklyInterval
    static List<JobGroup> csiGroups
    static List<Page> pagesToTest
    static final DateTime startOfWeek = new DateTime(2012, 11, 12, 0, 0, 0)
    static final DateTime dateOfJobResult1 = new DateTime(2012, 11, 12, 15, 15, 59)
    static final DateTime dateOfJobResult2 = new DateTime(2012, 11, 12, 15, 48, 32)


    def "test creation and calculation of weekly job group CSI values"() {
        setup: "event results with document complete values and document complete customer satisfaction values in percent"
        EventResult.withNewSession { session ->
            setupData()
            session.flush()
        }
        List<CsiAggregation> weeklyJobGroupCsiAggregations = []
        Map<String, Double> targetValues = [
                csiGroup1: 0.4d,
                csiGroup2: 0.7d
        ]

        when: "the job group CSI aggregations gets calculated"
        EventResult.withNewSession {
            weeklyJobGroupCsiAggregations = jobGroupCsiAggregationService.getOrCalculateShopCsiAggregations(startOfWeek.toDate(), startOfWeek.toDate(), weeklyInterval, csiGroups)
        }

        then: "two job group CSI aggregations were created and the correct CSI values per job group were calculated"
        assertEquals(2, weeklyJobGroupCsiAggregations.size())
        CsiAggregation.withNewSession {
            weeklyJobGroupCsiAggregations*.id.each { id ->
                CsiAggregation csiAggregation = CsiAggregation.get(id)

                assertEquals(AggregationType.JOB_GROUP, csiAggregation.aggregationType)
                assertEquals(startOfWeek.toDate(), csiAggregation.started)
                assertEquals(weeklyInterval.intervalInMinutes, csiAggregation.interval.intervalInMinutes)
                assertTrue(csiAggregation.isCalculated())
                assertEquals(targetValues["${csiAggregation.jobGroup.name}"], csiAggregation.csByWptDocCompleteInPercent, 0.01d)
            }
        }
    }


    def setupData() {
        OsmConfiguration.build()

        CsiAggregationInterval.build(name: "hourly", intervalInMinutes: CsiAggregationInterval.HOURLY)
        CsiAggregationInterval.build(name: "daily", intervalInMinutes: CsiAggregationInterval.DAILY)
        weeklyInterval = CsiAggregationInterval.build(name: "weekly", intervalInMinutes: CsiAggregationInterval.WEEKLY)

        CsiConfiguration csiConfiguration1 = CsiConfiguration.build(label: "csiConfiguration1")
        CsiConfiguration csiConfiguration2 = CsiConfiguration.build(label: "csiConfiguration2")

        JobGroup csiGroup1 = JobGroup.build(name: 'csiGroup1', csiConfiguration: csiConfiguration1)
        JobGroup csiGroup2 = JobGroup.build(name: 'csiGroup2', csiConfiguration: csiConfiguration2)
        csiGroups = [csiGroup1, csiGroup2]

        pagesToTest = [
                Page.build(name: 'HP'),
                Page.build(name: 'MES'),
                Page.build(name: 'SE'),
                Page.build(name: 'ADS'),
                Page.build(name: 'WKBS'),
                Page.build(name: 'WK')
        ]

        Browser browser = Browser.build(name: "testBrowser")
        ConnectivityProfile connectivityProfile = ConnectivityProfile.build(name: "testConnectivityProfile")
        configureCsiConfigurationsWithAllWeightsEqualToOne([csiConfiguration1, csiConfiguration2], browser, connectivityProfile)

        List customerSatisfactionList = [0.3, 0.3, 0.4, 0.4, 0.5, 0.5, 0.6, 0.6, 0.7, 0.7, 0.8, 0.8]

        csiGroups.eachWithIndex { csiGroup, csiGroupIndex ->
            pagesToTest.eachWithIndex { page, pageIndex ->
                JobResult jobResult1 = JobResult.build(date: dateOfJobResult1.toDate())
                JobResult jobResult2 = JobResult.build(date: dateOfJobResult2.toDate())

                EventResult.build(
                        docCompleteTimeInMillisecs: 3167,
                        csByWptDocCompleteInPercent: customerSatisfactionList[csiGroupIndex * 6 + pageIndex],
                        jobResult: jobResult1,
                        jobResultDate: jobResult1.date,
                        jobGroup: csiGroup,
                        page: page,
                        browser: browser,
                        connectivityProfile: connectivityProfile,
                        measuredEvent: MeasuredEvent.build(testedPage: page)
                )

                EventResult.build(
                        docCompleteTimeInMillisecs: 2911,
                        csByWptDocCompleteInPercent: customerSatisfactionList[csiGroupIndex * 6 + pageIndex],
                        jobResult: jobResult2,
                        jobResultDate: jobResult2.date,
                        jobGroup: csiGroup,
                        page: page,
                        browser: browser,
                        connectivityProfile: connectivityProfile,
                        measuredEvent: MeasuredEvent.build(testedPage: page)
                )
            }
        }

        EventResult.findAll().each {
            resultPersisterService.informDependentCsiAggregations(it)
        }
    }

    def configureCsiConfigurationsWithAllWeightsEqualToOne(List<CsiConfiguration> csiConfigurations, Browser browser, ConnectivityProfile connectivityProfile) {
        csiConfigurations.each { csiConfiguration ->
            csiConfiguration.browserConnectivityWeights.add(new BrowserConnectivityWeight(
                    browser: browser,
                    connectivity: connectivityProfile,
                    weight: 1
            ))

            pagesToTest.each { page ->
                csiConfiguration.pageWeights.add(new PageWeight(
                        page: page,
                        weight: 1
                ))
            }
        }
    }
}

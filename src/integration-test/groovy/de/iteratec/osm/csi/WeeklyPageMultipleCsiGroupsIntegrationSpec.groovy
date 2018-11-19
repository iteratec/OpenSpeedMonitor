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
import de.iteratec.osm.measurement.environment.wptserver.EventResultPersisterService
import de.iteratec.osm.measurement.schedule.ConnectivityProfile
import de.iteratec.osm.measurement.schedule.Job
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.report.chart.CsiAggregation
import de.iteratec.osm.report.chart.CsiAggregationInterval
import de.iteratec.osm.result.EventResult
import de.iteratec.osm.result.JobResult
import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import org.joda.time.DateTime

@Integration(applicationClass = openspeedmonitor.Application.class)
@Rollback
class WeeklyPageMultipleCsiGroupsIntegrationSpec extends NonTransactionalIntegrationSpec {

    /** injected by grails */
    PageCsiAggregationService pageCsiAggregationService
    EventResultPersisterService eventResultPersisterService

    CsiAggregationInterval hourly
    CsiAggregationInterval weekly
    List<JobGroup> csiGroups
    List<EventResult> results
    List<Page> pages

    static final List<String> allPages = [
            'HP',
            'MES',
    ]

    static final DateTime startOfWeek = new DateTime(2012, 11, 12, 0, 0, 0)
    static final String csiGroup1Name = 'csiGroup1'
    static final String csiGroup2Name = 'csiGroup2'

    /**
     * Creating testdata.
     */
    def setup() {
        System.out.println('Create some common test-data...')
        OsmConfiguration.build()

        createCsiAggregationIntervall()
        createPages()
        createCsiConfigurations()
        createJobGroups()
        createEventResults()
        System.out.println('Create some common test-data... DONE')

        hourly = CsiAggregationInterval.findByIntervalInMinutes(CsiAggregationInterval.HOURLY)
        weekly = CsiAggregationInterval.findByIntervalInMinutes(CsiAggregationInterval.WEEKLY)
    }

    void "test creation and calculation of weekly page CSI values"() {
        given: "a page belonging to a jobGroup and a date"
        Page testedPage
        Date startDate
        JobGroup csiGroup
        testedPage = Page.findByName(pageName)
        startDate = startOfWeek.toDate()
        csiGroup = JobGroup.findByName(jobGroupName)
        when: "the page CSI aggregation gets calculated"
        List<CsiAggregation> wpmvsOfOneGroupPageCombination
        results = EventResult.findAllByJobResultDateBetween(startOfWeek.toDate(), startOfWeek.plusWeeks(1).toDate())
        CsiAggregationInterval weeklyInterval = CsiAggregationInterval.findByIntervalInMinutes(CsiAggregationInterval.WEEKLY)
        wpmvsOfOneGroupPageCombination = pageCsiAggregationService.getOrCalculatePageCsiAggregations(startDate, startDate, weeklyInterval, [csiGroup], [testedPage])
        then: "check if page and jobGroup are correct, assert that expected csi and calculated csi are equal"
        wpmvsOfOneGroupPageCombination.each { CsiAggregation mvWeeklyPage ->
            mvWeeklyPage.jobGroupId == csiGroup.id
            mvWeeklyPage.pageId == testedPage.id
            expectedCsi == mvWeeklyPage.csByWptDocCompleteInPercent
        }
        where:
        pageName | jobGroupName  | expectedCsi
        "HP"     | csiGroup1Name | 0.95d
        "MES"    | csiGroup1Name | 0.35d
        "HP"     | csiGroup1Name | 0.55d
        "MES"    | csiGroup1Name | 0.75d
    }

    private void createCsiAggregationIntervall() {
        new CsiAggregationInterval(
                name: "hourly",
                intervalInMinutes: CsiAggregationInterval.HOURLY
        ).save(failOnError: true)
        new CsiAggregationInterval(
                name: "daily",
                intervalInMinutes: CsiAggregationInterval.DAILY
        ).save(failOnError: true)
        new CsiAggregationInterval(
                name: "weekly",
                intervalInMinutes: CsiAggregationInterval.WEEKLY
        ).save(failOnError: true)
    }

    private void createCsiConfigurations() {
        CsiConfiguration csiConfiguration1 = CsiConfiguration.build(label: "csiConfiguration1")
        CsiConfiguration csiConfiguration2 = CsiConfiguration.build(label: "csiConfiguration2")

        Browser browser = Browser.build(name: "testBrowser")
        ConnectivityProfile connectivityProfile = ConnectivityProfile.build(name: "testConnectivityProfile")
        configureCsiConfigurationsWithAllWeightsEqualToOne([csiConfiguration1, csiConfiguration2], browser, connectivityProfile)
    }

    private void createJobGroups() {
        JobGroup.build(
                name: csiGroup1Name,
                csiConfiguration: CsiConfiguration.findByLabel("csiConfiguration1")
        )

        JobGroup.build(
                name: csiGroup2Name,
                csiConfiguration: CsiConfiguration.findByLabel("csiConfiguration1")
        )
        csiGroups = JobGroup.findAll()
    }

    private void createPages() {
        allPages.collect { pageName ->
            Page.build(name: pageName).save(failOnError: true)
        }
        pages = Page.findAll()
    }

    private void createEventResults() {
        DateTime date = DateTime.parse("2012-11-12T15:15:59Z")

        Browser browser = Browser.findByName("testBrowser")
        ConnectivityProfile connectivityProfile = ConnectivityProfile.findByName("testConnectivityProfile")

        List customerSatisfactionList = [0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8]
        Integer csiListIndex = 0

        csiGroups.eachWithIndex { JobGroup csiGroup, int groupIndex ->
            pages.eachWithIndex { Page page, int pageIndex ->
                2.times {
                    EventResult.build(
                            docCompleteTimeInMillisecs: 3167,
                            csByWptDocCompleteInPercent: customerSatisfactionList[csiListIndex],
                            jobResultDate: date.plusHours((1 + groupIndex) * (1 + pageIndex)).toDate(),
                            jobGroup: csiGroup,
                            page: page,
                            browser: browser,
                            connectivityProfile: connectivityProfile,
                            jobResult: JobResult.build(date: date.plusHours((1 + groupIndex) * (1 + pageIndex)).toDate(), job: Job.build(jobGroup: csiGroup))
                    )
                    EventResult.build(
                            docCompleteTimeInMillisecs: 2911,
                            csByWptDocCompleteInPercent: customerSatisfactionList[csiListIndex],
                            jobResultDate: date.plusHours((1 + groupIndex) * (1 + pageIndex)).toDate(),
                            jobGroup: csiGroup,
                            page: page,
                            browser: browser,
                            connectivityProfile: connectivityProfile,
                            jobResult: JobResult.build(date: date.plusHours((1 + groupIndex) * (1 + pageIndex)).toDate(), job: Job.build(jobGroup: csiGroup))
                    )
                    csiListIndex++
                }
            }
        }

        EventResult.findAll().each {
            eventResultPersisterService.informDependentCsiAggregations(it)
        }
    }

    private void configureCsiConfigurationsWithAllWeightsEqualToOne(List<CsiConfiguration> csiConfigurations, Browser browser, ConnectivityProfile connectivityProfile) {
        csiConfigurations.each { csiConfiguration ->
            csiConfiguration.browserConnectivityWeights.add(new BrowserConnectivityWeight(
                    browser: browser,
                    connectivity: connectivityProfile,
                    weight: 1
            ))

            pages.each { page ->
                csiConfiguration.pageWeights.add(new PageWeight(
                        page: page,
                        weight: 1
                ))
            }
            csiConfiguration.save()
        }
    }
}

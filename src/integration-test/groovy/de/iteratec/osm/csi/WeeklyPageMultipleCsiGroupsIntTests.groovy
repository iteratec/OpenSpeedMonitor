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
import de.iteratec.osm.measurement.environment.wptserverproxy.ResultPersisterService
import de.iteratec.osm.measurement.schedule.ConnectivityProfile
import de.iteratec.osm.measurement.schedule.Job
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.report.chart.AggregationType
import de.iteratec.osm.report.chart.CsiAggregation
import de.iteratec.osm.report.chart.CsiAggregationInterval
import de.iteratec.osm.result.EventResult
import de.iteratec.osm.result.JobResult
import grails.test.mixin.integration.Integration
import org.joda.time.DateTime
import org.springframework.test.annotation.Rollback

import static org.junit.Assert.*
import static spock.util.matcher.HamcrestMatchers.closeTo

@Integration
@Rollback
class WeeklyPageMultipleCsiGroupsIntTests extends NonTransactionalIntegrationSpec {

    /** injected by grails */
    PageCsiAggregationService pageCsiAggregationService
    CsiAggregationUpdateService csiAggregationUpdateService
    ResultPersisterService resultPersisterService

    CsiAggregationInterval hourly
    CsiAggregationInterval weekly
    Map<String, Double> targetValues
    List<JobGroup> csiGroups
    List<EventResult> results

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
        EventResult.withNewSession { session ->
            OsmConfiguration.build()

            createCsiAggregationIntervall()
            createPages()
            createCsiConfigurations()
            createJobGroups()
            createEventResults()
            System.out.println('Create some common test-data... DONE')

            hourly = CsiAggregationInterval.findByIntervalInMinutes(CsiAggregationInterval.HOURLY)
            weekly = CsiAggregationInterval.findByIntervalInMinutes(CsiAggregationInterval.WEEKLY)
            csiGroups = [
                    JobGroup.findByName(csiGroup1Name),
                    JobGroup.findByName(csiGroup2Name)
            ]

            targetValues = [
                    'csiGroup1_HP' : 0.95d,
                    'csiGroup1_MES': 0.35d,
                    'csiGroup2_HP' : 0.55d,
                    'csiGroup2_MES': 0.75d
            ]
            session.flush()
        }
    }

    void testCreationAndCalculationOfWeeklyPageValuesFor_MES() {
        given:
        Page testedPage
        Date startDate
        EventResult.withNewSession { session ->
            testedPage = Page.findByName("MES")
            startDate = startOfWeek.toDate()
        }
        when:
        List<CsiAggregation> wpmvsOfOneGroupPageCombination
        JobGroup csiGroup
        EventResult.withNewSession { session ->
            results = EventResult.findAllByJobResultDateBetween(startOfWeek.toDate(), startOfWeek.plusWeeks(1).toDate())
            CsiAggregationInterval weeklyInterval = CsiAggregationInterval.findByIntervalInMinutes(CsiAggregationInterval.WEEKLY)
            csiGroup = JobGroup.findByName(csiGroup1Name)
//            csiGroups.each { JobGroup csiGroup ->
            wpmvsOfOneGroupPageCombination = pageCsiAggregationService.getOrCalculatePageCsiAggregations(startDate, startDate, weeklyInterval, [csiGroup], [testedPage])

//            }
            session.flush()
        }
        then:
        results.size() == 16
        wpmvsOfOneGroupPageCombination.size() == 1
        wpmvsOfOneGroupPageCombination.each { CsiAggregation mvWeeklyPage ->
            assert mvWeeklyPage.jobGroupId == csiGroup.id
            assert mvWeeklyPage.pageId == testedPage.id
            assertEquals targetValues["${csiGroup.name}_${testedPage.name}"], closeTo(mvWeeklyPage.csByWptDocCompleteInPercent, 0.01d)
        }
    }

//    void testCreationAndCalculationOfWeeklyPageValuesFor_HP() {
//        given:
//        Integer countResultsPerWeeklyPageMv = 4
//        Integer countWeeklyPageMvsToBeCreated = 2
//        when:
//        EventResult.withNewSession { session ->
//            results = EventResult.findAllByJobResultDateBetween(startOfWeek.toDate(), startOfWeek.plusWeeks(1).toDate())
//            session.flush()
//        }
//        then:
//        EventResult.withNewSession { session ->
//
//            results.size() == 16
//            creationAndCalculationOfWeeklyPageValuesTest("HP", countWeeklyPageMvsToBeCreated)
//            session.flush()
//        }
//    }

    /**
     * After pre-calculation of hourly job-{@link CsiAggregation}s the creation and calculation of weekly page-{@link CsiAggregation}s is tested.
     */
    private void creationAndCalculationOfWeeklyPageValuesTest(String pageName,
                                                              final Integer countWeeklyPageMvsToBeCreated) {

        Page testedPage = Page.findByName(pageName)
        Date startDate = startOfWeek.toDate()
        CsiAggregationInterval mvInterval = CsiAggregationInterval.findByIntervalInMinutes(CsiAggregationInterval.WEEKLY)
        List<CsiAggregation> wpmvs = pageCsiAggregationService.getOrCalculatePageCsiAggregations(startDate, startDate, mvInterval, csiGroups, [testedPage])
        assertNotNull(wpmvs)
        assertEquals(countWeeklyPageMvsToBeCreated, wpmvs.size())

        wpmvs.each { CsiAggregation mvWeeklyPage ->
            assertEquals(startDate, mvWeeklyPage.started)
            assertEquals(weekly.intervalInMinutes, mvWeeklyPage.interval.intervalInMinutes)
            assertEquals(AggregationType.PAGE, mvWeeklyPage.aggregationType)
            assertTrue(mvWeeklyPage.isCalculated())
        }

        CsiAggregationInterval weeklyInterval = CsiAggregationInterval.findByIntervalInMinutes(CsiAggregationInterval.WEEKLY)
        csiGroups.each { JobGroup csiGroup ->
            List<CsiAggregation> wpmvsOfOneGroupPageCombination = pageCsiAggregationService.getOrCalculatePageCsiAggregations(startDate, startDate, weeklyInterval, [csiGroup], [testedPage])
            assertEquals(1, wpmvsOfOneGroupPageCombination.size())

            wpmvsOfOneGroupPageCombination.each { CsiAggregation mvWeeklyPage ->
                assert mvWeeklyPage.jobGroupId == csiGroup.id
                assert mvWeeklyPage.pageId == testedPage.id
                assertNotNull(mvWeeklyPage.csByWptDocCompleteInPercent)
                assert targetValues["${csiGroup.name}_${testedPage.name}"], closeTo(mvWeeklyPage.csByWptDocCompleteInPercent, 0.01d)
            }
        }
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
    }

    private void createPages() {
        allPages.collect { pageName ->
            Page.build(name: pageName).save(failOnError: true)
        }
    }

    private void createEventResults() {
        DateTime date = DateTime.parse("2012-11-12T15:15:59Z")
        JobGroup jobGroup1 = JobGroup.findByName(csiGroup1Name)
        JobGroup jobGroup2 = JobGroup.findByName(csiGroup2Name)
        Browser browser = Browser.findByName("testBrowser")
        ConnectivityProfile connectivityProfile = ConnectivityProfile.findByName("testConnectivityProfile")

        4.times {
            EventResult.build(
                    docCompleteTimeInMillisecs: 3167,
                    csByWptDocCompleteInPercent: 0.1,
                    jobResultDate: date.plusHours(it).toDate(),
                    jobGroup: jobGroup1,
                    page: Page.findByName('HP'),
                    browser: browser,
                    connectivityProfile: connectivityProfile,
                    jobResult: JobResult.build(date: date.plusHours(it).toDate(), job: Job.build(jobGroup: jobGroup1))
            )
        }
        4.times {
            EventResult.build(
                    docCompleteTimeInMillisecs: 2911,
                    csByWptDocCompleteInPercent: 0.1,
                    jobResultDate: date.plusHours(it).toDate(),
                    jobGroup: jobGroup1,
                    browser: browser,
                    connectivityProfile: connectivityProfile,
                    page: Page.findByName('MES'),
                    jobResult: JobResult.build(date: date.plusHours(it).toDate(), job: Job.build(jobGroup: jobGroup1))
            )
        }
        4.times {
            EventResult.build(
                    docCompleteTimeInMillisecs: 3167,
                    csByWptDocCompleteInPercent: 0.1,
                    jobResultDate: date.plusHours(it).toDate(),
                    jobGroup: jobGroup2,
                    browser: browser,
                    connectivityProfile: connectivityProfile,
                    page: Page.findByName('HP'),
                    jobResult: JobResult.build(date: date.plusHours(it).toDate(), job: Job.build(jobGroup: jobGroup2))
            )
        }
        4.times {
            EventResult.build(
                    docCompleteTimeInMillisecs: 2911,
                    csByWptDocCompleteInPercent: 0.1,
                    jobResultDate: date.plusHours(it).toDate(),
                    jobGroup: jobGroup2,
                    browser: browser,
                    connectivityProfile: connectivityProfile,
                    page: Page.findByName('MES'),
                    jobResult: JobResult.build(date: date.plusHours(it).toDate(), job: Job.build(jobGroup: jobGroup2))
            )
        }

        EventResult.findAll().each {
            resultPersisterService.informDependentCsiAggregations(it)
        }
    }

    private void configureCsiConfigurationsWithAllWeightsEqualToOne(List<CsiConfiguration> csiConfigurations, Browser browser, ConnectivityProfile connectivityProfile) {
        csiConfigurations.each { csiConfiguration ->
            csiConfiguration.browserConnectivityWeights.add(new BrowserConnectivityWeight(
                    browser: browser,
                    connectivity: connectivityProfile,
                    weight: 1
            ))

            allPages.each { page ->
                csiConfiguration.pageWeights.add(new PageWeight(
                        page: Page.findByName(page),
                        weight: 1
                ))
            }
            csiConfiguration.save()
        }
    }
}

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

import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.report.chart.CsiAggregation
import de.iteratec.osm.report.chart.CsiAggregationInterval
import de.iteratec.osm.result.MeasuredEvent
import grails.test.mixin.integration.Integration
import grails.transaction.Rollback
import org.joda.time.DateTime

/**
 * Contains tests which test the creation of {@link de.iteratec.osm.report.chart.CsiAggregation}s without the existence of corresponding {@link EventResult}s.<br>
 * For all persisted {@link de.iteratec.osm.report.chart.CsiAggregation}s a {@link CsiAggregationUpdateEvent} should be created, which marks measured value as calculated.
 * @author nkuhn
 *
 */
@Integration
@Rollback
class CreatingYesNoDataMvsIntegrationSpec extends NonTransactionalIntegrationSpec {
    /** injected by grails */
    PageCsiAggregationService pageCsiAggregationService
    JobGroupCsiAggregationService jobGroupCsiAggregationService

    DateTime startOfCreatingWeeklyPageValues = new DateTime(2012, 2, 6, 0, 0, 0)
    DateTime startOfCreatingWeeklyShopValues = new DateTime(2012, 3, 12, 0, 0, 0)

    /**
     * Creating testdata.
     */

    def setup() {
        createInterval()
        createPagesAndEvents()
        createJobGroups()
    }

    /**
     * Creating weekly-page {@link CsiAggregation}s without data.
     */
    void "Creating weekly page values test"() {
        given:
        Date endDate = startOfCreatingWeeklyPageValues.plusWeeks(1).toDate()
        Date startDate = startOfCreatingWeeklyPageValues.toDate()
        CsiAggregationInterval weeklyInterval = CsiAggregationInterval.findByIntervalInMinutes(CsiAggregationInterval.WEEKLY)
        List<JobGroup> jobGroups = JobGroup.findAllByCsiConfigurationIsNotNull()
        List<Page> pages = Page.list()
        Integer countPages = pages.size()
        Integer countWeeks = 2

        when: "The weekly page CSIAggregation was calculated"
        List<CsiAggregation> wpmvs = pageCsiAggregationService.getOrCalculatePageCsiAggregations(startDate, endDate, weeklyInterval, jobGroups, pages)

        then: "We should get exactly one aggregation per week and page and every should be calculated"
        wpmvs.size() == countWeeks * countPages
        wpmvs.every {
            it.isCalculated()
        }
    }

    /**
     * Creating weekly-shop {@link de.iteratec.osm.report.chart.CsiAggregation}s without data.
     */
    void "Creating weekly shop values test"() {
        given:
        Date endDate = startOfCreatingWeeklyShopValues.plusWeeks(1).toDate()
        Date startDate = startOfCreatingWeeklyShopValues.toDate()
        List<JobGroup> jobGroups = JobGroup.list()
        List<Page> pages = Page.list()
        Integer countPages = pages.size()
        Integer countWeeks = 2
        CsiAggregationInterval weeklyInterval = CsiAggregationInterval.findByIntervalInMinutes(CsiAggregationInterval.WEEKLY)

        when: "The CsiAggregations of the given data was calculated"
        List<CsiAggregation> wsmvs = jobGroupCsiAggregationService.getOrCalculateShopCsiAggregations(startDate, endDate, weeklyInterval, jobGroups)

        then: "We should get exactly the amount of weeks as aggregations and every aggregations should be calculated"
        wsmvs.size() == countWeeks
        wsmvs.every {
            it.isCalculated()
        }
        pageCsiAggregationService.findAll(startDate, endDate, weeklyInterval, jobGroups, pages).size() == countWeeks * countPages
    }

    private static void createJobGroups() {
        CsiDay csiDay = CsiDay.build()
        CsiConfiguration csiConfiguration = CsiConfiguration.build(label: "TestLabel", csiDay: csiDay)
        JobGroup.build(name: 'CSI', csiConfiguration: csiConfiguration)
    }

    private static void createPagesAndEvents() {
        ['HP', 'MES', 'SE', 'ADS', 'WKBS', 'WK', Page.UNDEFINED].each { pageName ->
            Page page = Page.build(name: pageName)
            MeasuredEvent.build(name: 'CreatingYesNoDataMvsIntegrationSpec-' + pageName, testedPage: page)
        }
    }
    private static void createInterval(){
        CsiAggregationInterval.build(name: "weekly", intervalInMinutes: CsiAggregationInterval.WEEKLY)
    }
}

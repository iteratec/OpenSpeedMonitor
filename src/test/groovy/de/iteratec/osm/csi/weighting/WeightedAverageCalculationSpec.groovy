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

package de.iteratec.osm.csi.weighting

import de.iteratec.osm.csi.*
import de.iteratec.osm.measurement.environment.Browser
import de.iteratec.osm.measurement.schedule.ConnectivityProfile
import de.iteratec.osm.measurement.schedule.ConnectivityProfileService
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.report.chart.CsiAggregation
import de.iteratec.osm.report.chart.CsiAggregationUpdateEvent
import de.iteratec.osm.result.EventResult
import de.iteratec.osm.util.PerformanceLoggingService
import grails.buildtestdata.BuildDataTest
import grails.buildtestdata.mixin.Build
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import org.joda.time.DateTime
import spock.lang.Specification
import spock.lang.Unroll

import static de.iteratec.osm.csi.weighting.WeightFactor.*
import static spock.util.matcher.HamcrestMatchers.closeTo
import static spock.util.matcher.HamcrestSupport.that

@TestFor(WeightingService)
@Mock([EventResult, CsiAggregation, CsiAggregationUpdateEvent, BrowserConnectivityWeight, Browser, ConnectivityProfile,
        JobGroup, CsiDay, CsiConfiguration, CsiSystem])
@Build([EventResult, CsiAggregation, CsiConfiguration, Browser, Page, BrowserConnectivityWeight, PageWeight, JobGroup,
        CsiDay, ConnectivityProfile])
class WeightedAverageCalculationSpec extends Specification implements BuildDataTest {

    private static final double DELTA = 1e-15

    static final Date TWO_A_CLOCK_AM = new DateTime(2014, 1, 1, 2, 30, 12).toDate()
    static final Date FIVE_A_CLOCK_PM = new DateTime(2014, 1, 1, 17, 43, 56).toDate()

    static JobGroup JOB_GROUP

    static final String PAGE_WEIGHTED_50 = 'PAGE_WEIGHTED_50'
    static final String PAGE_WEIGHTED_70 = 'PAGE_WEIGHTED_70'
    static final String BROWSER_WEIGHTED_50 = 'BROWSER_WEIGHTED_50'
    static final String BROWSER_WEIGHTED_70 = 'BROWSER_WEIGHTED_70'
    static final String CONNECTIVITY_WEIGHTED_50 = 'CONNECTIVITY_WEIGHTED_50'
    static final String CONNECTIVITY_WEIGHTED_70 = 'CONNECTIVITY_WEIGHTED_70'

    static CsiConfiguration CSI_CONFIGURATION

    def doWithSpring = {
        performanceLoggingService(PerformanceLoggingService)
    }

    void setup() {
        createTestDataCommonToAllTests()
    }

    void "Without WeightFactors weight is always 1 when calculated for EventResults"() {

        when: "Weight gets calculated for an EventResult without WeightFactors."
        Double deliveredWeight = service.getWeight(EventResult.build(), [] as Set, CSI_CONFIGURATION)

        then: "That weiht is 1."
        that deliveredWeight , closeTo(1, DELTA)

    }

    void "Without WeightFactors weight is always 1 when calculated for CsiAggregations"() {

        when: "Weight gets calculated for a CsiAggregation without WeightFactors."
        Double deliveredWeight = service.getWeight(CsiAggregation.build(), [] as Set, CSI_CONFIGURATION)

        then: "That weight is 1."
        that deliveredWeight , closeTo(1, DELTA)

    }

    @Unroll
    void "Weight gets calculated correctly with HOUROFDAY as WeightFactor for EventResult with Date #date."() {

        given: "HOUROFDAY WeightFactor and some EventResults in place."
        Set<WeightFactor> weightFactors = [HOUROFDAY] as Set
        CsiValue eventResult = EventResult.buildWithoutSave(jobResultDate: date, jobGroup: JOB_GROUP)

        when: "Weights get calculated."
        double weight = service.getWeight(eventResult, weightFactors, CSI_CONFIGURATION)

        then: "They are calculated correctly respective hour of day."
        that weight, closeTo(expectedWeight, DELTA)

        where:
        date            || expectedWeight
        TWO_A_CLOCK_AM  || 0.2d
        FIVE_A_CLOCK_PM || 7.3d

    }

    @Unroll
    void "Weight gets calculated correctly with HOUROFDAY as WeightFactor for CsiAggregation with Date #date."() {

        given: "HOUROFDAY WeightFactor and some CsiAggregations in place."
        Set<WeightFactor> weightFactors = [HOUROFDAY] as Set
        CsiValue csiAggregation = CsiAggregation.buildWithoutSave(started: date, jobGroup: JOB_GROUP)

        when: "Weights get calculated."
        double weight = service.getWeight(csiAggregation, weightFactors, CSI_CONFIGURATION)

        then: "They are calculated correctly respective hour of day."
        that weight, closeTo(expectedWeight, DELTA)

        where:
        date            || expectedWeight
        TWO_A_CLOCK_AM  || 0.2d
        FIVE_A_CLOCK_PM || 7.3d

    }

    void "Weight gets calculated correctly with BROWSER_CONNECTIVITY_COMBINATION as WeightFactor for EventResults."() {

        given: "BROWSER_CONNECTIVITY_COMBINATION WeightFactor and some EventResults in place."
        Set<WeightFactor> weightFactors = [BROWSER_CONNECTIVITY_COMBINATION] as Set
        EventResult eventResult = EventResult.build(
            browser: Browser.findByName(browserName),
            connectivityProfile: ConnectivityProfile.findByName(connName)
        )

        when: "Weights get calculated."
        double weight = service.getWeight(eventResult, weightFactors, CSI_CONFIGURATION)

        then: "They are calculated correctly if respective BrowserConnectivityWeight exists. Otherwise weight is zero."
        that weight, closeTo(expectedWeight, DELTA)

        where:
        browserName         | connName                 || expectedWeight
        BROWSER_WEIGHTED_50 | CONNECTIVITY_WEIGHTED_50 || 0.5d
        BROWSER_WEIGHTED_70 | CONNECTIVITY_WEIGHTED_70 || 0.7d
        BROWSER_WEIGHTED_70 | CONNECTIVITY_WEIGHTED_50 || 0d

    }

    void "Weight gets calculated correctly with BROWSER_CONNECTIVITY_COMBINATION as WeightFactor for CsiAggregations."() {

        given: "BROWSER_CONNECTIVITY_COMBINATION WeightFactor and some CsiAggregations in place."
        Set<WeightFactor> weightFactors = [BROWSER_CONNECTIVITY_COMBINATION] as Set
        CsiAggregation csiAggregation = CsiAggregation.build(
                browser: Browser.findByName(browserName),
                connectivityProfile: ConnectivityProfile.findByName(connName)
        )

        when: "Weights get calculated."
        double weight = service.getWeight(csiAggregation, weightFactors, CSI_CONFIGURATION)

        then: "They are calculated correctly if respective BrowserConnectivityWeight exists. Otherwise weight is zero."
        that weight, closeTo(expectedWeight, DELTA)

        where:
        browserName         | connName                 || expectedWeight
        BROWSER_WEIGHTED_50 | CONNECTIVITY_WEIGHTED_50 || 0.5d
        BROWSER_WEIGHTED_70 | CONNECTIVITY_WEIGHTED_70 || 0.7d
        BROWSER_WEIGHTED_70 | CONNECTIVITY_WEIGHTED_50 || 0d

    }

    @Unroll
    void "Weight gets calculated correctly with PAGE as WeightFactor for EventResult with Page #pageName."() {

        given: "PAGE WeightFactor and EventResult in place."
        Set<WeightFactor> weightFactors = [PAGE] as Set
        CsiValue eventResult = EventResult.build(page: Page.findByName(pageName)?:Page.build())

        when: "Weight get calculated."
        double weight = service.getWeight(eventResult, weightFactors, CSI_CONFIGURATION)

        then: "They are calculated correctly if PageWeight for respective PAGE exists. Otherwise weight is zero."
        that weight, closeTo(expectedWeight, DELTA)

        where:
        pageName         || expectedWeight
        PAGE_WEIGHTED_50 || 0.5d
        PAGE_WEIGHTED_70 || 0.7d
        'WITHOUT_WEIGHT' || 0d

    }

    @Unroll
    void "Weight gets calculated correctly with PAGE as WeightFactor for CsiAggregation of Page #pageName."() {

        given: "PAGE WeightFactor and some CsiAggregations in place."
        Set<WeightFactor> weightFactors = [PAGE] as Set
        CsiAggregation csiAggregation = CsiAggregation.build(page: Page.findByName(pageName)?:Page.build())

        when: "Weights get calculated."
        double weight = service.getWeight(csiAggregation, weightFactors, CSI_CONFIGURATION)

        then: "They are calculated correctly if PageWeight for respective PAGE exists. Otherwise weight is zero."
        that weight, closeTo(expectedWeight, DELTA)

        where:
        pageName         || expectedWeight
        PAGE_WEIGHTED_50 || 0.5d
        PAGE_WEIGHTED_70 || 0.7d
        'WITHOUT_WEIGHT' || 0d

    }

    void "Weight gets calculated correctly with multiple WeightFactors for EventResults."() {

        given:
        CsiValue eventResult = EventResult.build(
                browser: Browser.findByName(browserName),
                page: Page.findByName(pageName),
                jobResultDate: date,
                connectivityProfile: ConnectivityProfile.findByName(connName),
                jobGroup:  JOB_GROUP
        )

        when:
        Double weight = service.getWeight(eventResult, weightFactors, CSI_CONFIGURATION)
        double expectedWeight = hourofdayWeight * browserConnectivityWeight * pageWeight

        then:
        that weight, closeTo(expectedWeight, DELTA)

        where:
        browserName         | pageName         | date            | connName                 || hourofdayWeight |browserConnectivityWeight | pageWeight
        BROWSER_WEIGHTED_50 | PAGE_WEIGHTED_50 | TWO_A_CLOCK_AM  | CONNECTIVITY_WEIGHTED_50 || 0.2d            | 0.5d                     | 0.5d
        BROWSER_WEIGHTED_70 | PAGE_WEIGHTED_70 | FIVE_A_CLOCK_PM | CONNECTIVITY_WEIGHTED_70 || 7.3d            | 0.7d                     | 0.7d
        BROWSER_WEIGHTED_70 | PAGE_WEIGHTED_70 | FIVE_A_CLOCK_PM | CONNECTIVITY_WEIGHTED_70 || 1d              | 0.7d                     | 0.7d
        BROWSER_WEIGHTED_70 | PAGE_WEIGHTED_70 | FIVE_A_CLOCK_PM | CONNECTIVITY_WEIGHTED_70 || 7.3d            | 0.7d                     | 1d

        weightFactors << [
            [HOUROFDAY, PAGE, BROWSER_CONNECTIVITY_COMBINATION] as Set,
            [HOUROFDAY, PAGE, BROWSER_CONNECTIVITY_COMBINATION] as Set,
            [PAGE, BROWSER_CONNECTIVITY_COMBINATION] as Set,
            [HOUROFDAY, BROWSER_CONNECTIVITY_COMBINATION] as Set
        ]

    }

    void "Weight gets calculated correctly with multiple WeightFactors for CsiAggregations."() {

        given:
        CsiValue csiAggregation = CsiAggregation.build(
                browser: Browser.findByName(browserName),
                page: Page.findByName(pageName),
                started: date,
                connectivityProfile: ConnectivityProfile.findByName(connName),
                jobGroup:  JOB_GROUP
        )

        when:
        Double weight = service.getWeight(csiAggregation, weightFactors, CSI_CONFIGURATION)
        double expectedWeight = hourofdayWeight * browserConnectivityWeight * pageWeight

        then:
        that weight, closeTo(expectedWeight, DELTA)

        where:
        browserName         | pageName         | date            | connName                 || hourofdayWeight |browserConnectivityWeight | pageWeight
        BROWSER_WEIGHTED_50 | PAGE_WEIGHTED_50 | TWO_A_CLOCK_AM  | CONNECTIVITY_WEIGHTED_50 || 0.2d            | 0.5d                     | 0.5d
        BROWSER_WEIGHTED_70 | PAGE_WEIGHTED_70 | FIVE_A_CLOCK_PM | CONNECTIVITY_WEIGHTED_70 || 7.3d            | 0.7d                     | 0.7d
        BROWSER_WEIGHTED_70 | PAGE_WEIGHTED_70 | FIVE_A_CLOCK_PM | CONNECTIVITY_WEIGHTED_70 || 1d              | 0.7d                     | 0.7d
        BROWSER_WEIGHTED_70 | PAGE_WEIGHTED_70 | FIVE_A_CLOCK_PM | CONNECTIVITY_WEIGHTED_70 || 7.3d            | 0.7d                     | 1d

        weightFactors << [
            [HOUROFDAY, PAGE, BROWSER_CONNECTIVITY_COMBINATION] as Set,
            [HOUROFDAY, PAGE, BROWSER_CONNECTIVITY_COMBINATION] as Set,
            [PAGE, BROWSER_CONNECTIVITY_COMBINATION] as Set,
            [HOUROFDAY, BROWSER_CONNECTIVITY_COMBINATION] as Set
        ]

    }

    private createTestDataCommonToAllTests() {

        ConnectivityProfile conn50 = ConnectivityProfile.build(name: CONNECTIVITY_WEIGHTED_50)
        conn50.connectivityProfileService = Mock(ConnectivityProfileService)
        ConnectivityProfile conn70 = ConnectivityProfile.build(name: CONNECTIVITY_WEIGHTED_70)
        conn70.connectivityProfileService = Mock(ConnectivityProfileService)

        CSI_CONFIGURATION = CsiConfiguration.build(
            csiDay: CsiDay.build(
                    hour0Weight: 2.9d,
                    hour1Weight: 0.4d,
                    hour2Weight: 0.2d,
                    hour3Weight: 0.1d,
                    hour4Weight: 0.1d,
                    hour5Weight: 0.2d,
                    hour6Weight: 0.7d,
                    hour7Weight: 1.7d,
                    hour8Weight: 3.2d,
                    hour9Weight: 4.8d,
                    hour10Weight: 5.6d,
                    hour11Weight: 5.7d,
                    hour12Weight: 5.5d,
                    hour13Weight: 5.8d,
                    hour14Weight: 5.9d,
                    hour15Weight: 6.0d,
                    hour16Weight: 6.7d,
                    hour17Weight: 7.3d,
                    hour18Weight: 7.6d,
                    hour19Weight: 8.8d,
                    hour20Weight: 9.3d,
                    hour21Weight: 7.0d,
                    hour22Weight: 3.6d,
                    hour23Weight: 0.9d
            ),
            browserConnectivityWeights: [
                BrowserConnectivityWeight.build(
                    connectivity: conn50,
                    browser: Browser.build(name: BROWSER_WEIGHTED_50),
                    weight: 0.5d
                ),
                BrowserConnectivityWeight.build(
                    connectivity: conn70,
                    browser: Browser.build(name: BROWSER_WEIGHTED_70),
                    weight: 0.7d
                )
            ],
            pageWeights: [
                PageWeight.build(
                    page: Page.build(name: PAGE_WEIGHTED_50),
                    weight: 0.5d
                ),
                PageWeight.build(
                    page: Page.build(name: PAGE_WEIGHTED_70),
                    weight: 0.7d
                )
            ]
        )

        JOB_GROUP = JobGroup.build(csiConfiguration: CSI_CONFIGURATION)
    }

}

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

package de.iteratec.osm.result

import grails.buildtestdata.BuildDataTest
import grails.test.mixin.TestFor
import grails.test.mixin.Mock
import grails.buildtestdata.mixin.Build
import grails.web.mapping.LinkGenerator
import spock.lang.Specification

import de.iteratec.osm.csi.Page
import de.iteratec.osm.measurement.environment.Browser
import de.iteratec.osm.measurement.environment.Location
import de.iteratec.osm.measurement.schedule.ConnectivityProfile
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.report.chart.*
import de.iteratec.osm.result.dao.EventResultDaoService
import de.iteratec.osm.util.I18nService
import de.iteratec.osm.util.PerformanceLoggingService


@TestFor(EventResultDashboardService)
@Mock([EventResult, MeasuredEvent, JobGroup, Location, ConnectivityProfile, Browser, Page])
@Build([EventResult, MeasuredEvent, JobGroup, Location, ConnectivityProfile, Browser, Page])
class SummarizedChartLegendEntriesSpec extends Specification implements BuildDataTest {

    EventResultDashboardService serviceUnderTest

    def doWithSpring = {
        eventResultDaoService(EventResultDaoService)
        csiAggregationUtilService(CsiAggregationUtilService)
        osmChartProcessingService(OsmChartProcessingService)
        i18nService(I18nService)
        performanceLoggingService(PerformanceLoggingService)
    }

    def setup() {
        serviceUnderTest = service
        mockGrailsLinkGenerator()
        mockI18NServices()
    }

    def "no summarization possible because every legend part in every event result is different"(int csiAggregationInterval) {
        setup: "build two event results with different label attributes"
        serviceUnderTest.eventResultDaoService = Stub(EventResultDaoService) {
            getLimitedMedianEventResultsBy(_, _, _, _, _, _) >> [
                    EventResult.build(
                            jobGroup: JobGroup.build(name: "Job Group 1"),
                            measuredEvent: MeasuredEvent.build(name: "Measured Event 1"),
                            location: Location.build(location: "Location 1"),
                            connectivityProfile: ConnectivityProfile.build(name: "Connectivity Profile 1"),
                            cachedView: CachedView.UNCACHED,
                            docCompleteTimeInMillisecs: 1,
                            docCompleteRequests: 1
                    ),
                    EventResult.build(
                            jobGroup: JobGroup.build(name: "Job Group 2"),
                            measuredEvent: MeasuredEvent.build(name: "Measured Event 2"),
                            location: Location.build(location: "Location 2"),
                            connectivityProfile: ConnectivityProfile.build(name: "Connectivity Profile 2"),
                            cachedView: CachedView.UNCACHED,
                            docCompleteTimeInMillisecs: 1,
                            docCompleteRequests: 1
                    )
            ]
        }

        when: "the labels get analysed for common parts"
        OsmRickshawChart chart = serviceUnderTest.getEventResultDashboardHighchartGraphs(
                null,
                null,
                csiAggregationInterval,
                [new SelectedMeasurand(name: Measurand.DOC_COMPLETE_TIME.toString(), cachedView: CachedView.UNCACHED, selectedType: SelectedMeasurandType.MEASURAND),
                 new SelectedMeasurand(name: Measurand.DOC_COMPLETE_REQUESTS.toString(), cachedView: CachedView.UNCACHED, selectedType: SelectedMeasurandType.MEASURAND)],
                new ErQueryParams())
        List<OsmChartGraph> resultGraphs = chart.osmChartGraphs

        then: "they are build correctly and have no common parts"
        resultGraphs.size() == 4
        List<String> graphLabels = resultGraphs*.label
        graphLabels.containsAll([
                "Uncached " + Measurand.DOC_COMPLETE_TIME.toString() + " | Job Group 1 | Measured Event 1 | Location 1 | Connectivity Profile 1",
                "Uncached " + Measurand.DOC_COMPLETE_TIME.toString() + " | Job Group 2 | Measured Event 2 | Location 2 | Connectivity Profile 2",
                "Uncached " + Measurand.DOC_COMPLETE_REQUESTS.toString() + " | Job Group 1 | Measured Event 1 | Location 1 | Connectivity Profile 1",
                "Uncached " + Measurand.DOC_COMPLETE_REQUESTS.toString() + " | Job Group 2 | Measured Event 2 | Location 2 | Connectivity Profile 2"
        ])
        chart.osmChartGraphsCommonLabel == ""

        where: "all CSI aggregation intervals are tested"
        csiAggregationInterval        | _
        CsiAggregationInterval.RAW    | _
        CsiAggregationInterval.HOURLY | _
        CsiAggregationInterval.DAILY  | _
        CsiAggregationInterval.WEEKLY | _
    }

    def "no summarization necessary because all event results belong to same graph"(int csiAggregationInterval) {
        setup: "build two event results which belong to the same graph and therefore have the same label attributes"
        JobGroup jobGroup = JobGroup.build(name: "Job Group")
        MeasuredEvent measuredEvent = MeasuredEvent.build(name: "Measured Event")
        Page page = Page.build(name: "Page")
        Location location = Location.build(location: "Location")
        ConnectivityProfile connectivityProfile = ConnectivityProfile.build(name: "Connectivity Profile")
        Browser browser = Browser.build()

        serviceUnderTest.eventResultDaoService = Stub(EventResultDaoService) {
            getLimitedMedianEventResultsBy(_, _, _, _, _, _) >> {
                def eventResults = []
                2.times {
                    eventResults.push(
                            EventResult.build(
                                    jobGroup: jobGroup,
                                    measuredEvent: measuredEvent,
                                    page: page,
                                    location: location,
                                    connectivityProfile: connectivityProfile,
                                    browser: browser,
                                    cachedView: CachedView.UNCACHED,
                                    docCompleteTimeInMillisecs: 0
                            )
                    )
                }

                return eventResults
            }
        }

        when: "the labels get analysed for common parts"
        OsmRickshawChart chart = serviceUnderTest.getEventResultDashboardHighchartGraphs(
                null,
                null,
                CsiAggregationInterval.RAW,
                [ new SelectedMeasurand(name: Measurand.DOC_COMPLETE_TIME.toString(), cachedView: CachedView.UNCACHED, selectedType: SelectedMeasurandType.MEASURAND)],
                new ErQueryParams())
        List<OsmChartGraph> resultGraphs = chart.osmChartGraphs

        then: "all label parts are the same and we get one label"
        resultGraphs.size() == 1
        resultGraphs[0].label == "Uncached " + Measurand.DOC_COMPLETE_TIME.toString() + " | Job Group | Measured Event | Location | Connectivity Profile"
        chart.osmChartGraphsCommonLabel == ""

        where: "all CSI aggregation intervals are tested"
        csiAggregationInterval        | _
        CsiAggregationInterval.RAW    | _
        CsiAggregationInterval.HOURLY | _
        CsiAggregationInterval.DAILY  | _
        CsiAggregationInterval.WEEKLY | _

    }

    void "some legend parts in every event result the same, some different"(int csiAggregationInterval) {
        setup: "build two event results where same label attributes are the same and some differ"
        serviceUnderTest.eventResultDaoService = Stub(EventResultDaoService) {
            getLimitedMedianEventResultsBy(_, _, _, _, _, _) >> [
                    EventResult.build(
                            jobGroup: JobGroup.build(name: "Job Group 1"),
                            measuredEvent: MeasuredEvent.build(name: "Measured Event 1"),
                            location: Location.build(location: "Location 1"),
                            connectivityProfile: ConnectivityProfile.build(name: "Connectivity Profile 1"),
                            cachedView: CachedView.UNCACHED,
                            docCompleteTimeInMillisecs: 0
                    ),
                    EventResult.build(
                            jobGroup: JobGroup.build(name: "Job Group 1"),
                            measuredEvent: MeasuredEvent.build(name: "Measured Event 1"),
                            location: Location.build(location: "Location 2"),
                            connectivityProfile: ConnectivityProfile.build(name: "Connectivity Profile 2"),
                            cachedView: CachedView.UNCACHED,
                            docCompleteTimeInMillisecs: 0
                    )
            ]
        }

        when: "the labels get analysed for common parts"
        OsmRickshawChart chart = serviceUnderTest.getEventResultDashboardHighchartGraphs(
                null,
                null,
                CsiAggregationInterval.RAW,
                [ new SelectedMeasurand(name:  Measurand.DOC_COMPLETE_TIME.toString(), cachedView:  CachedView.UNCACHED, selectedType: SelectedMeasurandType.MEASURAND)],
                new ErQueryParams())
        List<OsmChartGraph> resultGraphs = chart.osmChartGraphs

        then: "we get two graph labels and a common part label"
        resultGraphs.size() == 2
        List<String> graphLables = resultGraphs*.label
        graphLables.containsAll([
                "Location 1 | Connectivity Profile 1",
                "Location 2 | Connectivity Profile 2",
        ])
        chart.osmChartGraphsCommonLabel == "<b></b>: Uncached " + Measurand.DOC_COMPLETE_TIME.toString() + " | <b></b>: Job Group 1 | <b></b>: Measured Event 1"

        where: "all CSI aggregation intervals are tested"
        csiAggregationInterval        | _
        CsiAggregationInterval.RAW    | _
        CsiAggregationInterval.HOURLY | _
        CsiAggregationInterval.DAILY  | _
        CsiAggregationInterval.WEEKLY | _
    }

    void "single legend parts in some but not all event results the same"(int csiAggregationInterval) {
        setup: "build four event results with the described configuration"
        serviceUnderTest.eventResultDaoService = Stub(EventResultDaoService) {
            getLimitedMedianEventResultsBy(_, _, _, _, _, _) >> [
                    EventResult.build(
                            jobGroup: JobGroup.build(name: "Job Group 1"),
                            measuredEvent: MeasuredEvent.build(name: "Measured Event 1"),
                            location: Location.build(location: "Location 1"),
                            connectivityProfile: ConnectivityProfile.build(name: "Connectivity Profile 1"),
                            cachedView: CachedView.UNCACHED,
                            docCompleteTimeInMillisecs: 0,
                            docCompleteRequests: 0
                    ),
                    EventResult.build(
                            jobGroup: JobGroup.build(name: "Job Group 2"),
                            measuredEvent: MeasuredEvent.build(name: "Measured Event 1"),
                            location: Location.build(location: "Location 1"),
                            connectivityProfile: ConnectivityProfile.build(name: "Connectivity Profile 1"),
                            cachedView: CachedView.UNCACHED,
                            docCompleteTimeInMillisecs: 0,
                            docCompleteRequests: 0
                    ),
                    EventResult.build(
                            jobGroup: JobGroup.build(name: "Job Group 3"),
                            measuredEvent: MeasuredEvent.build(name: "Measured Event 2"),
                            location: Location.build(location: "Location 2"),
                            connectivityProfile: ConnectivityProfile.build(name: "Connectivity Profile 2"),
                            cachedView: CachedView.UNCACHED,
                            docCompleteTimeInMillisecs: 0,
                            docCompleteRequests: 0
                    ),
                    EventResult.build(
                            jobGroup: JobGroup.build(name: "Job Group 4"),
                            measuredEvent: MeasuredEvent.build(name: "Measured Event 3"),
                            location: Location.build(location: "Location 3"),
                            connectivityProfile: ConnectivityProfile.build(name: "Connectivity Profile 3"),
                            cachedView: CachedView.UNCACHED,
                            docCompleteTimeInMillisecs: 0,
                            docCompleteRequests: 0
                    )
            ]
        }

        when: "the labels get analysed for common parts"
        OsmRickshawChart chart = serviceUnderTest.getEventResultDashboardHighchartGraphs(
                null,
                null,
                CsiAggregationInterval.RAW,
                [new SelectedMeasurand(name:  Measurand.DOC_COMPLETE_TIME.toString(), cachedView:  CachedView.UNCACHED, selectedType: SelectedMeasurandType.MEASURAND),
                 new SelectedMeasurand(name:  Measurand.DOC_COMPLETE_REQUESTS.toString(), cachedView:  CachedView.UNCACHED, selectedType: SelectedMeasurandType.MEASURAND)],
                new ErQueryParams())
        List<OsmChartGraph> resultGraphs = chart.osmChartGraphs

        then: "all label are build correctly and the common label is empty"
        resultGraphs.size() == 8
        List<String> graphLabels = resultGraphs*.label
        graphLabels.containsAll([
                "Uncached " + Measurand.DOC_COMPLETE_TIME.toString() + " | Job Group 1 | Measured Event 1 | Location 1 | Connectivity Profile 1",
                "Uncached " + Measurand.DOC_COMPLETE_TIME.toString() + " | Job Group 2 | Measured Event 1 | Location 1 | Connectivity Profile 1",
                "Uncached " + Measurand.DOC_COMPLETE_TIME.toString() + " | Job Group 3 | Measured Event 2 | Location 2 | Connectivity Profile 2",
                "Uncached " + Measurand.DOC_COMPLETE_TIME.toString() + " | Job Group 4 | Measured Event 3 | Location 3 | Connectivity Profile 3",
                "Uncached " + Measurand.DOC_COMPLETE_REQUESTS.toString() + " | Job Group 1 | Measured Event 1 | Location 1 | Connectivity Profile 1",
                "Uncached " + Measurand.DOC_COMPLETE_REQUESTS.toString() + " | Job Group 2 | Measured Event 1 | Location 1 | Connectivity Profile 1",
                "Uncached " + Measurand.DOC_COMPLETE_REQUESTS.toString() + " | Job Group 3 | Measured Event 2 | Location 2 | Connectivity Profile 2",
                "Uncached " + Measurand.DOC_COMPLETE_REQUESTS.toString() + " | Job Group 4 | Measured Event 3 | Location 3 | Connectivity Profile 3"
        ])
        chart.osmChartGraphsCommonLabel == ''

        where: "all CSI aggregation intervals are tested"
        csiAggregationInterval        | _
        CsiAggregationInterval.RAW    | _
        CsiAggregationInterval.HOURLY | _
        CsiAggregationInterval.DAILY  | _
        CsiAggregationInterval.WEEKLY | _
    }

    def mockGrailsLinkGenerator() {
        serviceUnderTest.grailsLinkGenerator = Mock(LinkGenerator)
    }

    def mockI18NServices() {
        serviceUnderTest.i18nService = Stub(I18nService) {
            msg(_, _, _) >> { args ->
                return args[1]
            }
        }
        serviceUnderTest.osmChartProcessingService.i18nService = serviceUnderTest.i18nService
    }
}

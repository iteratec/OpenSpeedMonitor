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

import grails.test.mixin.TestFor
import grails.test.mixin.Mock
import grails.buildtestdata.mixin.Build
import grails.web.mapping.LinkGenerator
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import spock.lang.Specification
import de.iteratec.osm.csi.Page
import de.iteratec.osm.measurement.environment.*
import de.iteratec.osm.measurement.schedule.*
import de.iteratec.osm.report.chart.*
import de.iteratec.osm.result.dao.EventResultDaoService
import de.iteratec.osm.util.I18nService
import de.iteratec.osm.util.PerformanceLoggingService

@TestFor(EventResultDashboardService)
@Mock([EventResult, AggregatorType, Browser, JobGroup, Location, MeasuredEvent, Page, ConnectivityProfile, CsiAggregation])
@Build([EventResult, AggregatorType, Browser, JobGroup, Location, MeasuredEvent, Page, ConnectivityProfile, CsiAggregation])
class EventResultDashboardServiceTests extends Specification {

    Browser browser
    Location location
    JobGroup jobGroup
    MeasuredEvent measuredEvent
    Page page
    ConnectivityProfile connectivityProfile

    DateTime runDate

    def doWithSpring = {
        resultCsiAggregationService(ResultCsiAggregationService)
        osmChartProcessingService(OsmChartProcessingService)
        performanceLoggingService(PerformanceLoggingService)
        csiAggregationUtilService(CsiAggregationUtilService)
        i18nService(I18nService)
        browserSerivce(BrowserService)
    }

    void setup() {
        runDate = new DateTime(DateTimeZone.UTC)

        mockI18NServices()
        mockGrailsLinkGenerator()
    }

    void "get event result dashboard chart map"(List aggregatorTypeNames, int csiAggregationInterval, int expectedNumberOfGraphs, List expectedValues) {
        given: "two event results with attribute uncached and cached respectively"
        List<EventResult> eventResults = createEventResults(2)
        eventResults[0].domTimeInMillisecs = 2000
        eventResults[0].cachedView = CachedView.CACHED
        eventResults[1].domTimeInMillisecs = 3000
        eventResults[1].cachedView = CachedView.UNCACHED

        service.eventResultDaoService = Stub(EventResultDaoService) {
            getLimitedMedianEventResultsBy(_, _, _, _, _, _) >> eventResults
        }

        when: "the data for the time series chart gets created"
        Date startTime = runDate.minusHours(1).toDate()
        Date endTime = runDate.plusHours(1).toDate()
        List<AggregatorType> aggregatorTypes = []
        aggregatorTypeNames.each { name ->
            aggregatorTypes.push(AggregatorType.build(name: name, measurandGroup: MeasurandGroup.LOAD_TIMES))
        }
        ErQueryParams queryParams = createEventResultQueryParams()

        OsmRickshawChart chart = service.getEventResultDashboardHighchartGraphs(startTime, endTime, csiAggregationInterval, aggregatorTypes, queryParams)

        then: "we get the correct number of graphs, points per graph and point values"
        chart.osmChartGraphs.size() == expectedNumberOfGraphs
        for (int i=0; i < expectedNumberOfGraphs; i++) {
            chart.osmChartGraphs[i].points.size() == 1
            chart.osmChartGraphs[i].points[0].csiAggregation == expectedValues[i]
        }

        where:
        aggregatorTypeNames                                                              | csiAggregationInterval        | expectedNumberOfGraphs | expectedValues
        [AggregatorType.RESULT_CACHED_DOM_TIME]                                          | CsiAggregationInterval.RAW    | 1                      | [2000]
        [AggregatorType.RESULT_CACHED_DOM_TIME]                                          | CsiAggregationInterval.HOURLY | 1                      | [2000]
        [AggregatorType.RESULT_UNCACHED_DOM_TIME]                                        | CsiAggregationInterval.RAW    | 1                      | [3000]
        [AggregatorType.RESULT_UNCACHED_DOM_TIME]                                        | CsiAggregationInterval.HOURLY | 1                      | [3000]
        [AggregatorType.RESULT_CACHED_DOM_TIME, AggregatorType.RESULT_UNCACHED_DOM_TIME] | CsiAggregationInterval.RAW    | 2                      | [2000, 3000]
        [AggregatorType.RESULT_CACHED_DOM_TIME, AggregatorType.RESULT_UNCACHED_DOM_TIME] | CsiAggregationInterval.HOURLY | 2                      | [2000, 3000]
    }

    void "get event result dashboard chart map with trimmed values"(int csiAggregationInterval, int expectedNumberOfPoints, int expectedValue) {
        given: "four event results with different domTimeInMillisecs"
        List<EventResult> eventResults = createEventResults(4)
        eventResults[0].domTimeInMillisecs = 2000
        eventResults[1].domTimeInMillisecs = 4000
        eventResults[2].domTimeInMillisecs = 6000
        eventResults[3].domTimeInMillisecs = 8000

        service.eventResultDaoService = Stub(EventResultDaoService) {
            getLimitedMedianEventResultsBy(_, _, _, _, _, _) >> eventResults
        }

        when: "the data for the time series chart gets created and we trim the data below and above some given values"
        Date startTime = runDate.minusHours(1).toDate()
        Date endTime = runDate.plusHours(1).toDate()
        List<AggregatorType> aggregatorTypes = [AggregatorType.build(name: AggregatorType.RESULT_CACHED_DOM_TIME, measurandGroup: MeasurandGroup.LOAD_TIMES)]
        ErQueryParams queryParams = createEventResultQueryParams()
        queryParams.minLoadTimeInMillisecs = 3000
        queryParams.maxLoadTimeInMillisecs = 7000

        OsmRickshawChart chart = service.getEventResultDashboardHighchartGraphs(startTime, endTime, csiAggregationInterval, aggregatorTypes, queryParams)

        then: "we only get the event results within the given range"
        chart.osmChartGraphs.size() == 1
        chart.osmChartGraphs[0].points.size() == expectedNumberOfPoints
        for (int i=0; i<expectedNumberOfPoints; i++) {
            chart.osmChartGraphs[0].points[i].csiAggregation == expectedValue
        }

        where:
        csiAggregationInterval          | expectedNumberOfPoints | expectedValue
        CsiAggregationInterval.RAW      | 2                      | 4000
        CsiAggregationInterval.HOURLY   | 1                      | 5000
    }


    void "build url list underlaying event results of a data point"() {
        given: "a CSI aggregation"
        CsiAggregation csiAggregation = CsiAggregation.build(underlyingEventResultsByWptDocComplete: resultIDs)
        long csiAggregationId = csiAggregation.getId()

        Map linkArgument = null
        service.grailsLinkGenerator = Stub(LinkGenerator) {
            link(_) >> { argument ->
                linkArgument = argument.first()

                return "https://www.this-url-is-not-relevant-for-the-test.com"
            }
        }

        when: "the url gets build"
        service.tryToBuildTestsDetailsURL(csiAggregation)

        then: "the passed arguments are correct"
        linkArgument['controller'] == 'highchartPointDetails'
        linkArgument['action'] == 'listAggregatedResults'
        linkArgument['absolute'] == true
        linkArgument['params']['csiAggregationId'] == csiAggregationId.toString()
        linkArgument['params']['lastKnownCountOfAggregatedResultsOrNull'] == resultIDsCount.toString()

        where:
        resultIDs | resultIDsCount
        '1,2'     | 2
        '1'       | 1
    }

    def createEventResults(amount) {
        page = Page.build()
        jobGroup = JobGroup.build()
        browser = Browser.build()
        location = Location.build()
        measuredEvent = MeasuredEvent.build()
        connectivityProfile = ConnectivityProfile.build()

        List<EventResult> eventResults = []

        amount.times {
            eventResults.push(
                    EventResult.build(
                            jobGroup: jobGroup,
                            page: page,
                            measuredEvent: measuredEvent,
                            browser: browser,
                            location: location,
                            connectivityProfile: connectivityProfile,
                            jobResultDate: runDate.toDate()
                    )
            )
        }

        return eventResults
    }

    def createEventResultQueryParams() {
        ErQueryParams queryParams = new ErQueryParams()
        queryParams.browserIds.add(browser.id)
        queryParams.jobGroupIds.add(jobGroup.id)
        queryParams.locationIds.add(location.id)
        queryParams.measuredEventIds.add(measuredEvent.id)
        queryParams.pageIds.add(page.id)

        return queryParams
    }

    def mockGrailsLinkGenerator() {
        service.grailsLinkGenerator = Mock(LinkGenerator)
    }

    def mockI18NServices() {
        service.i18nService = Stub(I18nService) {
            msg(_, _, _) >> { args ->
                return args[1]
            }
        }
        service.osmChartProcessingService.i18nService = service.i18nService
    }
}

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
@Mock([EventResult, UserTiming, Browser, JobGroup, Location, MeasuredEvent, Page, ConnectivityProfile, CsiAggregation])
@Build([EventResult, UserTiming, Browser, JobGroup, Location, MeasuredEvent, Page, ConnectivityProfile, CsiAggregation])
class EventResultDashboardServiceSpec extends Specification {

    Browser browser
    Location location
    JobGroup jobGroup
    MeasuredEvent measuredEvent
    Page page
    ConnectivityProfile connectivityProfile

    DateTime runDate

    def doWithSpring = {
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

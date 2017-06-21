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

import grails.test.mixin.TestFor
import grails.test.mixin.Mock
import grails.buildtestdata.mixin.Build
import spock.lang.Specification
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import de.iteratec.osm.OsmConfigCacheService
import de.iteratec.osm.measurement.environment.Browser
import de.iteratec.osm.measurement.environment.Location
import de.iteratec.osm.measurement.schedule.ConnectivityProfile
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.report.chart.*
import de.iteratec.osm.result.*
import de.iteratec.osm.util.PerformanceLoggingService


@TestFor(EventCsiAggregationService)
@Mock([EventResult, AggregatorType, CsiAggregationInterval, JobGroup, MeasuredEvent, Page, Browser, Location, ConnectivityProfile, JobResult, CsiAggregation])
@Build([EventResult, AggregatorType, CsiAggregationInterval, JobGroup, MeasuredEvent, Page, Browser, Location, ConnectivityProfile, JobResult])
class UpdateEventResultDependentCsiAggregationsSpec extends Specification {

    static final DateTime eventResultDateTime = new DateTime(DateTimeZone.UTC)

    CsiAggregationInterval hourlyCsiAggregationInterval

    def doWithSpring = {
        performanceLoggingService(PerformanceLoggingService)
        csiValueService(CsiValueService)
    }

    def setup() {
        AggregatorType.build(name: AggregatorType.MEASURED_EVENT, measurandGroup: MeasurandGroup.NO_MEASURAND)
        hourlyCsiAggregationInterval = CsiAggregationInterval.build(name: "hourly", intervalInMinutes: CsiAggregationInterval.HOURLY)

        mockNonRelevantServices()
    }

    def "calculate CSI aggregations of the first event result"() {
        given: "an event result with given customer satisfactions (based on doc and visually complete)"
        EventResult eventResult = createEventResults(1).first()
        eventResult.csByWptDocCompleteInPercent = 50
        eventResult.csByWptVisuallyCompleteInPercent = 40

        when: "the hourly csi aggregation gets calculated"
        service.createOrUpdateHourlyValue(eventResultDateTime, eventResult)

        then: "it is equal to the given customer satisfactions of the event result"
        List<CsiAggregation> listOfCsiAggregations = service.findAll(eventResultDateTime.toDate(), eventResultDateTime.toDate(), hourlyCsiAggregationInterval)
        listOfCsiAggregations.size() == 1
        listOfCsiAggregations.first().csByWptDocCompleteInPercent == 50
        listOfCsiAggregations.first().csByWptVisuallyCompleteInPercent == 40
    }

    def "calculate new CSI aggregation with new incoming event results"() {
        given: "two event results where the first is the base for the given csi"
        List<EventResult> eventResults = createEventResults(2)

        EventResult eventResult = eventResults[0]
        eventResult.csByWptDocCompleteInPercent = firstDocCompleteInPercent
        eventResult.csByWptVisuallyCompleteInPercent = firstVisuallyCompleteInPercent

        service.createOrUpdateHourlyValue(eventResultDateTime, eventResult)


        when: "the second event result goes into the calculation of the csi aggregation"
        EventResult newEventResult = eventResults[1]
        newEventResult.csByWptDocCompleteInPercent = secondDocCompleteInPercent
        newEventResult.csByWptVisuallyCompleteInPercent = secondVisuallyCompleteInPercent

        service.createOrUpdateHourlyValue(eventResultDateTime, newEventResult)

        then: "the mean of both customer satisfactions is the new csi aggregation"
        List<CsiAggregation> listOfCsiAggregations = service.findAll(eventResultDateTime.toDate(), eventResultDateTime.toDate(), hourlyCsiAggregationInterval)
        listOfCsiAggregations.size() == 1
        listOfCsiAggregations[0].csByWptDocCompleteInPercent == expectedDocCompleteInPercent
        listOfCsiAggregations[0].csByWptVisuallyCompleteInPercent == expectedVisuallyCompleteInPercent

        where: "some customer satisfactions are given"
        firstDocCompleteInPercent   | firstVisuallyCompleteInPercent    | secondDocCompleteInPercent | secondVisuallyCompleteInPercent  | expectedDocCompleteInPercent  | expectedVisuallyCompleteInPercent
        50                          | 40                                | 20                         | 10                               | (50 + 20) / 2                 | (40 + 10) / 2
        50                          | 40                                | null                       | null                             | 50                            | 40
        null                        | null                              | 20                         | 10                               | 20                            | 10

    }

    def "calculate csi aggregations for event results from different job groups"() {
        given: "five event results from different job groups with customer satisfactions"
        List<EventResult> eventResults = createEventResults(5)

        JobResult jobResult_1 = JobResult.build()
        JobResult jobResult_2 = JobResult.build()
        JobGroup jobGroup_1 = JobGroup.build()
        JobGroup jobGroup_2 = JobGroup.build()

        List relevantAttributesForTest = [
                [jobResult: jobResult_1, csByWptDocCompleteInPercent: 50, csByWptVisuallyCompleteInPercent: 40, jobGroup: jobGroup_1],
                [jobResult: jobResult_1, csByWptDocCompleteInPercent: 60, csByWptVisuallyCompleteInPercent: 50, jobGroup: jobGroup_1],
                [jobResult: jobResult_2, csByWptDocCompleteInPercent: 20, csByWptVisuallyCompleteInPercent: 10, jobGroup: jobGroup_2],
                [jobResult: jobResult_2, csByWptDocCompleteInPercent: 30, csByWptVisuallyCompleteInPercent: 20, jobGroup: jobGroup_2],
                [jobResult: jobResult_2, csByWptDocCompleteInPercent: 40, csByWptVisuallyCompleteInPercent: 30, jobGroup: jobGroup_2]
        ]

        relevantAttributesForTest.eachWithIndex { attributes, index ->
            attributes.each { key, value ->
                eventResults[index][key] = value
            }
        }

        when: "the csi aggregation gets updated with each event result"
        eventResults.each { eventResult ->
            service.createOrUpdateHourlyValue(eventResultDateTime, eventResult)
        }

        then: "a csi aggregation gets calculated for each job group"
        List<CsiAggregation> listOfCsiAggregations = service.findAll(eventResultDateTime.toDate(), eventResultDateTime.toDate(), hourlyCsiAggregationInterval)
        listOfCsiAggregations.size() == 2

        List<CsiAggregation> listOfCsiAggregationsForJobGroup_1 = listOfCsiAggregations.findAll {
            it.jobGroupId == jobGroup_1.id
        }
        listOfCsiAggregationsForJobGroup_1.size() == 1
        listOfCsiAggregationsForJobGroup_1[0].csByWptDocCompleteInPercent == (50 + 60) / 2
        listOfCsiAggregationsForJobGroup_1[0].csByWptVisuallyCompleteInPercent == (40 + 50) / 2

        List<CsiAggregation> listOfCsiAggregationsForJobGroup_2 = listOfCsiAggregations.findAll {
            it.jobGroupId == jobGroup_2.id
        }
        listOfCsiAggregationsForJobGroup_2.size() == 1
        listOfCsiAggregationsForJobGroup_2[0].csByWptDocCompleteInPercent == (20 + 30 + 40) / 3
        listOfCsiAggregationsForJobGroup_2[0].csByWptVisuallyCompleteInPercent == (10 + 20 + 30) / 3
    }

    def mockNonRelevantServices() {
        service.csiAggregationUpdateEventDaoService = Mock(CsiAggregationUpdateEventDaoService)
        service.csiValueService.osmConfigCacheService = Stub(OsmConfigCacheService) {
            getMinDocCompleteTimeInMillisecs(_) >> 200
            getCachedMaxDocCompleteTimeInMillisecs(_) >> 20000
        }
    }

    def createEventResults(amount) {
        JobGroup jobGroup = JobGroup.build()
        JobResult jobResult = JobResult.build()
        Page page = Page.build()
        MeasuredEvent measuredEvent = MeasuredEvent.build(testedPage: page)
        Browser browser = Browser.build()
        Location location = Location.build()
        ConnectivityProfile connectivityProfile = ConnectivityProfile.build()

        List<EventResult> eventResults = []

        amount.times {
            eventResults.push(
                    EventResult.build(
                            docCompleteTimeInMillisecs: 2000,
                            jobGroup: jobGroup,
                            jobResult: jobResult,
                            page: page,
                            measuredEvent: measuredEvent,
                            browser: browser,
                            location: location,
                            connectivityProfile: connectivityProfile
                    )
            )
        }

        return eventResults
    }
}
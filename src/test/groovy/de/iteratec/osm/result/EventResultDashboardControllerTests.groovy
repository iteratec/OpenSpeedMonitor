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

import de.iteratec.osm.csi.Page
import de.iteratec.osm.measurement.environment.Browser
import de.iteratec.osm.measurement.environment.Location
import de.iteratec.osm.measurement.schedule.ConnectivityProfile
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.measurement.schedule.dao.JobGroupDaoService
import de.iteratec.osm.report.chart.AggregatorType
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import spock.lang.Specification
/**
 * <p>
 * Test-suite of {@link EventResultDashboardController} and 
 * {@link EventResultDashboardShowAllCommand}.
 * </p> 
 *
 * @author rhe, sburnicki
 * @since IT-98
 */
@TestFor(EventResultDashboardController)
@Mock([ConnectivityProfile])
class EventResultDashboardControllerTests extends Specification {

    public static final String CUSTOM_CONNECTIVITY_NAME = 'Custom (6.000/512 Kbps, 50ms)'
    EventResultDashboardController controllerUnderTest
    EventResultDashboardShowAllCommand command

    void setup() {
        controllerUnderTest = controller

        // Mock relevant services:
        command = new EventResultDashboardShowAllCommand()
        controllerUnderTest.jobGroupDaoService = Stub(JobGroupDaoService)
        controllerUnderTest.eventResultDashboardService = Stub(EventResultDashboardService)
    }

    void "command without bound parameters is invalid"() {
        expect:
        !command.validate()
    }

    void "command bound with default parameters is valid"() {
        given:
        setDefaultParams()

        when:
        controllerUnderTest.bindData(command, params)

        then:
        command.validate()
        command.selectedAggrGroupValuesUnCached == [AggregatorType.RESULT_UNCACHED_DOC_COMPLETE_INCOMING_BYTES]
        command.selectedAggrGroupValuesCached == [AggregatorType.RESULT_CACHED_DOC_COMPLETE_TIME]
        command.trimBelowLoadTimes == 100
        command.trimAboveLoadTimes == 10000
        command.trimBelowRequestCounts == 2
        command.trimAboveRequestCounts == 200
        command.trimBelowRequestSizes == 30
        command.trimAboveRequestSizes == 3000
    }

    void "command is invalid if no aggrgroupvalue is set"() {
        given:
        setDefaultParams()
        params.remove("selectedAggrGroupValuesUnCached")
        params.remove("selectedAggrGroupValuesCached")

        when:
        controllerUnderTest.bindData(command, params)

        then:
        !command.validate()
    }

    void "static model data is correctly generated"() {
        given:
        Page page1 = new Page(name: 'Page1', weight: 0) {
            Long getId() { return 1L }
        }
        Page page2 = new Page(name: 'Page2', weight: 0.25d) {
            Long getId() { return 2L }
        }
        Page page3 = new Page(name: 'Page3', weight: 0.5d) {
            Long getId() { return 3L }
        }

        MeasuredEvent measuredEvent1 = new MeasuredEvent(name: 'MeasuredEvent1', testedPage: page3) {
            Long getId() { return 1001L }
        }
        MeasuredEvent measuredEvent2 = new MeasuredEvent(name: 'MeasuredEvent2', testedPage: page2) {
            Long getId() { return 1002L }
        }
        MeasuredEvent measuredEvent3 = new MeasuredEvent(name: 'MeasuredEvent3', testedPage: page1) {
            Long getId() { return 1003L }
        }
        MeasuredEvent measuredEvent4 = new MeasuredEvent(name: 'MeasuredEvent4', testedPage: page2) {
            Long getId() { return 1004L }
        }

        Browser browser1 = new Browser(name: 'Browser1') {
            Long getId() { return 11L }
        }

        Location location1 = new Location(label: 'Location1', browser: browser1) {
            Long getId() { return 101L }
        }
        Location location2 = new Location(label: 'Location2', browser: browser1) {
            Long getId() { return 102L }
        }
        Location location3 = new Location(label: 'Location3', browser: browser1) {
            Long getId() { return 103L }
        }

        controllerUnderTest.eventResultDashboardService.getAllJobGroups() >> {
            return [new JobGroup(name: 'Group2'),
                    new JobGroup(name: 'Group1')]
        }
        controllerUnderTest.eventResultDashboardService.getAllPages() >> {
            return [page1, page2, page3]
        }
        controllerUnderTest.eventResultDashboardService.getAllMeasuredEvents() >> {
            return [measuredEvent3, measuredEvent1, measuredEvent2, measuredEvent4]
        }
        controllerUnderTest.eventResultDashboardService.getAllBrowser() >> {
            return [browser1]
        }
        controllerUnderTest.eventResultDashboardService.getAllLocations() >> {
            return [location1, location2, location3]
        }

        when:
        Map<String, Object> result = controllerUnderTest.constructStaticViewDataOfShowAll();

        then:
        result != null
        result.size() == 13

        result["aggrGroupValuesCached"] == EventResultDashboardController.AGGREGATOR_GROUP_VALUES.get(CachedView.CACHED)
        result["aggrGroupValuesUnCached"] == EventResultDashboardController.AGGREGATOR_GROUP_VALUES.get(CachedView.UNCACHED)

        result["folders"]*.getName() == ["Group2", "Group1"]
        result["pages"]*.getName() == ["Page1", "Page2", "Page3"]
        result["measuredEvents"]*.getName() == ["MeasuredEvent3", "MeasuredEvent1", "MeasuredEvent2", "MeasuredEvent4"]
        result["browsers"]*.getName() == ["Browser1"]
        result["locations"]*.getLabel() == ["Location1", "Location2", "Location3"]

        result["dateFormat"] == EventResultDashboardController.DATE_FORMAT_STRING_FOR_HIGH_CHART
        result["aggregationIntervals"] == EventResultDashboardController.AGGREGATION_INTERVALS

        result["eventsOfPages"] == [
                1L: [1003L] as Set,
                2L: [1002L, 1004L] as Set,
                3L: [1001L] as Set
        ]
        result["locationsOfBrowsers"] == [
                11L: [101L, 102L, 103L] as Set
        ]
    }

    void "command properties are correctly copied to map"() {
        given:
        setDefaultCommandProperties()

        when:
        Map<String, Object> result = [:]
        command.copyRequestDataToViewModelMap(result)

        then:
        command.validate()
        result.size() == 29
        result["selectedInterval"] == 60
        result["selectedAggrGroupValuesCached"] == [AggregatorType.RESULT_CACHED_DOC_COMPLETE_TIME]
        result["selectedAggrGroupValuesUnCached"] == [AggregatorType.RESULT_UNCACHED_DOC_COMPLETE_INCOMING_BYTES]
    }

    void "command creates correct ErQueryParameters"() {
        given:
        setDefaultCommandProperties()

        when:
        ErQueryParams erQueryParams = command.createErQueryParams()

        then:
        command.validate()
        erQueryParams != null
        erQueryParams.minLoadTimeInMillisecs == 100
        erQueryParams.maxLoadTimeInMillisecs == 10000
        erQueryParams.minRequestCount == 2
        erQueryParams.maxRequestCount == 200
        erQueryParams.minRequestSizeInBytes == 30000
        erQueryParams.maxRequestSizeInBytes == 3000000
    }

    void "createMvQueryParams throws with invalid command"() {
        given: "an invalid command"
        !command.validate()

        when:
        command.createErQueryParams()

        then: "an exception is thrown"
        thrown IllegalStateException
    }

    private setDefaultParams() {
        TimeSeriesShowCommandBaseSpec.getDefaultParams().each { key, value ->
            params.setProperty(key, value)
        }
        params.selectedAggrGroupValuesUnCached = AggregatorType.RESULT_UNCACHED_DOC_COMPLETE_INCOMING_BYTES.toString()
        params.selectedAggrGroupValuesCached = AggregatorType.RESULT_CACHED_DOC_COMPLETE_TIME.toString()
        params.selectedConnectivities = ['1', CUSTOM_CONNECTIVITY_NAME, 'native']
        params.selectedInterval = 60
        params.trimBelowLoadTimes = 100
        params.trimAboveLoadTimes = 10000
        params.trimBelowRequestCounts = 2
        params.trimAboveRequestCounts = 200
        params.trimBelowRequestSizes = 30
        params.trimAboveRequestSizes = 3000
    }

    private setDefaultCommandProperties() {
        TimeSeriesShowCommandBaseSpec.setDefaultCommandProperties(command)
        command.selectedAggrGroupValuesUnCached = [AggregatorType.RESULT_UNCACHED_DOC_COMPLETE_INCOMING_BYTES]
        command.selectedAggrGroupValuesCached = [AggregatorType.RESULT_CACHED_DOC_COMPLETE_TIME]
        command.selectedInterval = 60

        command.trimBelowLoadTimes = 100
        command.trimAboveLoadTimes = 10000
        command.trimBelowRequestCounts = 2
        command.trimAboveRequestCounts = 200
        command.trimBelowRequestSizes = 30
        command.trimAboveRequestSizes = 3000
    }
}

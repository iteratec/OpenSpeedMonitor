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
import grails.buildtestdata.mixin.Build
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
@Build([JobGroup, Page, MeasuredEvent, Browser, Location])
@Mock([ConnectivityProfile, JobGroup, Page, MeasuredEvent, Browser, Location])
class EventResultDashboardControllerSpec extends Specification {

    public static final String CUSTOM_CONNECTIVITY_NAME = 'Custom (6.000/512 Kbps, 50ms)'
    EventResultDashboardShowAllCommand command

    void setup() {
        command = new EventResultDashboardShowAllCommand()
    }

    void "command without bound parameters is invalid"() {
        expect: "the default command is not valid"
        !command.validate()
    }

    void "command bound with default parameters is valid"() {
        given:
        setDefaultParams()

        when: "default paras are bound to the command"
        controller.bindData(command, params)

        then: "th command is valid and all fields are correctly set"
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

        when: "no aggrgroupvalue is set in the parameters"
        controller.bindData(command, params)

        then: "the command is not valid"
        !command.validate()
    }

    void "static model data is correctly generated"() {
        given:
        2.times { JobGroup.build(name: "Group${it+1}") }
        List<Page> pages = (1..3).collect { Page.build(name: "Page${it}") }
        MeasuredEvent measuredEvent1 = MeasuredEvent.build(name: "MeasuredEvent1", testedPage: pages[2])
        MeasuredEvent measuredEvent2 = MeasuredEvent.build(name: "MeasuredEvent2", testedPage: pages[1])
        MeasuredEvent measuredEvent3 = MeasuredEvent.build(name: "MeasuredEvent3", testedPage: pages[0])
        MeasuredEvent measuredEvent4 = MeasuredEvent.build(name: "MeasuredEvent4", testedPage: pages[1])
        Browser browser = Browser.build(name: "Browser1")
        List<Location> locations = (1..3).collect { Location.build(label: "Location${it}", browser: browser)}
        controller.jobGroupDaoService = Mock(JobGroupDaoService)
        controller.eventResultDashboardService = Stub(EventResultDashboardService) {
            getAllPages() >> { return Page.list() }
            getAllMeasuredEvents() >> { return MeasuredEvent.list() }
            getAllBrowser() >> { return Browser.list() }
            getAllLocations() >> { return Location.list() }
            getAllJobGroups() >> { return JobGroup.list() }
        }

        when: "the static view data is constructed"
        Map<String, Object> result = controller.constructStaticViewDataOfShowAll()

        then: "the result contains all domains and links between browser & location and page & measured event"
        result != null
        result.size() == 13

        result["aggrGroupValuesCached"] == EventResultDashboardController.AGGREGATOR_GROUP_VALUES
        result["aggrGroupValuesUnCached"] == EventResultDashboardController.AGGREGATOR_GROUP_VALUES

        result["folders"]*.getName() == ["Group1", "Group2"]
        result["pages"]*.getName() == ["Page1", "Page2", "Page3"]
        result["measuredEvents"]*.getName() == ["MeasuredEvent1", "MeasuredEvent2", "MeasuredEvent3", "MeasuredEvent4"]
        result["browsers"]*.getName() == ["Browser1"]
        result["locations"]*.getLabel() == ["Location1", "Location2", "Location3"]

        result["dateFormat"] == EventResultDashboardController.DATE_FORMAT_STRING_FOR_HIGH_CHART
        result["aggregationIntervals"] == EventResultDashboardController.AGGREGATION_INTERVALS

        result["eventsOfPages"] == [
                (pages[0].id): [measuredEvent3.id] as Set,
                (pages[1].id): [measuredEvent2.id, measuredEvent4.id] as Set,
                (pages[2].id): [measuredEvent1.id] as Set
        ]
        result["locationsOfBrowsers"] == [
                (browser.id): locations*.id as Set
        ]
    }

    void "command properties are correctly copied to map"() {
        given:
        setDefaultCommandProperties()

        when: "the request data should be copied to the view model map"
        Map<String, Object> result = [:]
        command.copyRequestDataToViewModelMap(result)

        then: "the map contains the data and the command is valid"
        command.validate()
        result.size() == 29
        result["selectedInterval"] == 60
        result["selectedAggrGroupValuesCached"] == [AggregatorType.RESULT_CACHED_DOC_COMPLETE_TIME]
        result["selectedAggrGroupValuesUnCached"] == [AggregatorType.RESULT_UNCACHED_DOC_COMPLETE_INCOMING_BYTES]
    }

    void "command creates correct ErQueryParameters"() {
        given:
        setDefaultCommandProperties()

        when: "event result query parameters are created"
        ErQueryParams erQueryParams = command.createErQueryParams()

        then: "also min/max values are set"
        command.validate()
        erQueryParams != null
        erQueryParams.minLoadTimeInMillisecs == 100
        erQueryParams.maxLoadTimeInMillisecs == 10000
        erQueryParams.minRequestCount == 2
        erQueryParams.maxRequestCount == 200
        erQueryParams.minRequestSizeInBytes == 30000
        erQueryParams.maxRequestSizeInBytes == 3000000
        erQueryParams.includeNativeConnectivity
        erQueryParams.customConnectivityNames == [CUSTOM_CONNECTIVITY_NAME] as SortedSet
    }

    void "command creates correct ErQueryParameters with empty connectivities"() {
        given:
        setDefaultCommandProperties()
        command.selectedConnectivities = []

        when: "event result query parameters are created with empty connectivities"
        ErQueryParams erQueryParams = command.createErQueryParams()

        then: "all related connectivity sets are empty, and the 'includeAll' fields are true"
        erQueryParams.connectivityProfileIds == [] as Set
        erQueryParams.customConnectivityNames == [] as Set
        erQueryParams.includeNativeConnectivity
        erQueryParams.includeAllConnectivities
    }

    void "command creates correct ErQueryParameters with only custom connectivites"() {
        given:
        setDefaultCommandProperties()
        command.selectedConnectivities = [CUSTOM_CONNECTIVITY_NAME]

        when: "event result query parameters are created with a custom connectivity name"
        ErQueryParams erQueryParams = command.createErQueryParams()

        then: "the query parameters don't include any ids and should not include all or native connectivities"
        erQueryParams.connectivityProfileIds == [] as Set
        erQueryParams.customConnectivityNames == [CUSTOM_CONNECTIVITY_NAME] as Set
        !erQueryParams.includeAllConnectivities
        !erQueryParams.includeNativeConnectivity
    }

    void "command creates correct ErQueryParameters with only native connectivites"() {
        given:
        setDefaultCommandProperties()
        command.selectedConnectivities = ["native"]

        when: "event result query parameters are created with only the native connectivity"
        ErQueryParams erQueryParams = command.createErQueryParams()

        then: "all connectivity sets are empty and only native includeNativeConnectivity is true"
        erQueryParams.connectivityProfileIds == [] as Set
        erQueryParams.customConnectivityNames == [] as Set
        !erQueryParams.includeAllConnectivities
        erQueryParams.includeNativeConnectivity
    }

    void "createMvQueryParams throws with invalid command"() {
        given: "an invalid command"
        !command.validate()

        when: "event result query parameters should be created from an invalid command"
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

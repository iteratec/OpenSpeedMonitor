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
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.Interval
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
    static EventResultDashboardShowAllCommand command

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
        command.selectedFolder == []
        command.selectedPages == []
        command.selectedMeasuredEventIds == []
        command.selectedBrowsers == []
        command.selectedLocations == []
    }

    void "command with empty bound parameters is invalid"() {
        when:
        controllerUnderTest.bindData(command, params)

        then:
        !command.validate()
        command.selectedFolder == []
        command.selectedPages == []
        command.selectedMeasuredEventIds == []
        command.selectedBrowsers == []
        command.selectedLocations == []
    }

    void "command bound with default parameters is valid"() {
        given:
        setDefaultParams()

        when:
        controllerUnderTest.bindData(command, params)

        then:
        command.validate()
        command.from == new DateTime(2013, 8, 18, 16, 0, 0, DateTimeZone.UTC)
        command.to == new DateTime(2013, 8, 18, 18, 0, 0, DateTimeZone.UTC)
        command.createTimeFrameInterval().start == command.from
        command.createTimeFrameInterval().end == command.to
        command.selectedAggrGroupValuesUnCached == [AggregatorType.RESULT_UNCACHED_DOC_COMPLETE_INCOMING_BYTES]
        command.selectedFolder == [1L]
        command.selectedPages == [1L, 5L]
        command.selectedMeasuredEventIds == [7L, 8L, 9L]
        command.selectedBrowsers == [2L]
        command.selectedLocations == [17L]
        command.includeNativeConnectivity
        command.getSelectedCustomConnectivityNames() == [CUSTOM_CONNECTIVITY_NAME]
        command.selectedConnectivityProfiles == [1L]
    }

    void "command without browsers, locations, connectivities and measuredEvents is valid"() {
        given:
        setDefaultParams()
        params.remove("selectedMeasuredEventIds")
        params.remove("selectedConnectivities")
        params.remove("selectedBrowsers")
        params.remove("selectedLocations")

        when:
        controllerUnderTest.bindData(command, params)

        then:
        command.validate()
        command.selectedMeasuredEventIds == []
        command.selectedBrowsers == []
        command.selectedLocations == []
        command.includeNativeConnectivity
        command.getSelectedCustomConnectivityNames() == []
        command.selectedConnectivityProfiles == []
    }

    void "command is invalid if 'to' is before 'from'"() {
        given:
        setDefaultParams()
        params.from = "2013-08-18T12:00:00.000Z"
        params.to = "2013-08-18T11:00:00.000Z"

        when:
        controllerUnderTest.bindData(command, params)

        then:
        !command.validate()
    }

    void "command is invalid if 'to' is equal to 'from'"() {
        given:
        setDefaultParams()
        params.from = "2013-08-18T11:00:00.000Z"
        params.to = "2013-08-18T11:00:00.000Z"

        when:
        controllerUnderTest.bindData(command, params)

        then:
        !command.validate()
    }

    void "command supports legacy date format in 'from' and 'to'"() {
        given:
        setDefaultParams()
        params.from = "18.08.2013"
        params.to = "18.08.2013"

        when:
        controllerUnderTest.bindData(command, params)

        then:
        command.validate()
        command.from == new DateTime(2013, 8, 18, 0, 0, 0, 0)
        command.to == new DateTime(2013, 8, 18, 23, 59, 59, 999)
        command.createTimeFrameInterval().start == command.from
        command.createTimeFrameInterval().end == command.to
    }

    void "command supports automatic time frame"() {
        given:
        setDefaultParams()
        params.from = null
        params.to = null
        params.selectedTimeFrameInterval = 3000
        long nowInMillis = DateTime.now().getMillis()
        long allowedDelta = 1000

        when:
        controllerUnderTest.bindData(command, params)

        then:
        command.validate()
        command.from == null
        command.to == null
        Interval timeFrame = command.createTimeFrameInterval()
        Math.abs(timeFrame.endMillis - nowInMillis) < allowedDelta
        Math.abs(timeFrame.startMillis - (nowInMillis - 3000 * 1000)) < allowedDelta
    }

    void "command is invalid when binding parameters of wrong type"() {
        given:
        setDefaultParams()
        params.selectedPages = ['NOT-A-NUMBER']
        params.selectedLocations = 'UGLY'


        when:
        controllerUnderTest.bindData(command, params)

        then:
        !command.validate()
        command.selectedPages == []
        command.selectedLocations == []
    }

    void "command is invalid without pages"() {
        given:
        setDefaultParams()
        params.selectedPages = []

        when:
        controllerUnderTest.bindData(command, params)

        then:
        !command.validate()
    }

    void "command does not include native or custom if only numbers are set"() {
        given:
        setDefaultParams()
        params.selectedConnectivities = ['1', '2']

        when:
        controllerUnderTest.bindData(command, params)

        then:
        command.validate()
        command.getSelectedCustomConnectivityNames() == []
        !command.includeNativeConnectivity
        command.getSelectedConnectivityProfiles() == [1L, 2L]
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
        result.size() == 12

        result["aggrGroupValuesCached"] == EventResultDashboardController.AGGREGATOR_GROUP_VALUES.get(CachedView.CACHED)
        result["aggrGroupValuesUnCached"] == EventResultDashboardController.AGGREGATOR_GROUP_VALUES.get(CachedView.UNCACHED)

        result["folders"]*.getName() == ["Group2", "Group1"]
        result["pages"]*.getName() == ["Page1", "Page2", "Page3"]
        result["measuredEvents"]*.getName() == ["MeasuredEvent3", "MeasuredEvent1", "MeasuredEvent2", "MeasuredEvent4"]
        result["browsers"]*.getName() == ["Browser1"]
        result["locations"]*.getLabel() == ["Location1", "Location2", "Location3"]

        result["dateFormat"] == EventResultDashboardController.DATE_FORMAT_STRING_FOR_HIGH_CHART

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
        result.size() == 29
        result["selectedFolder"] == [1L]
        result["selectedPages"] == [1L, 5L]
        result['selectedFolder'] == [1L]
        result['selectedPages'] == [1L, 5L]
        result['selectedMeasuredEventIds'] == [7L, 8L, 9L]
        result['selectedBrowsers'] == [2L]
        result['selectedLocations'] == [17L]

        result['from'] == '2013-08-18T12:00:00.000Z'
        result['to'] == '2013-08-19T13:00:00.000Z'
        result['selectedConnectivities'] == [CUSTOM_CONNECTIVITY_NAME, "1", "native"]
    }

    void "command creates correct ErQueryParameters"() {
        given:
        setDefaultCommandProperties()

        when:
        ErQueryParams erQueryParams = command.createErQueryParams()

        then:
        erQueryParams != null
        erQueryParams.jobGroupIds == [1L] as SortedSet
        erQueryParams.pageIds == [1L, 5L] as SortedSet
        erQueryParams.measuredEventIds == [7L, 8L, 9L] as SortedSet
        erQueryParams.browserIds == [2L] as SortedSet
        erQueryParams.locationIds == [17L] as SortedSet
        erQueryParams.connectivityProfileIds == [1L] as SortedSet
        !erQueryParams.includeAllConnectivities
        erQueryParams.includeNativeConnectivity
        erQueryParams.customConnectivityNames == [CUSTOM_CONNECTIVITY_NAME] as SortedSet
    }

    void "command creates correct ErQueryParameters with empty filters"() {
        given:
        setDefaultCommandProperties()
        command.selectedConnectivities = []
        command.selectedMeasuredEventIds = []
        command.selectedBrowsers = []
        command.selectedLocations = []

        when:
        ErQueryParams erQueryParams = command.createErQueryParams()

        then:
        erQueryParams.measuredEventIds == [] as Set
        erQueryParams.browserIds == [] as Set
        erQueryParams.locationIds == [] as Set
        erQueryParams.connectivityProfileIds == [] as Set
        erQueryParams.customConnectivityNames == [] as Set
        erQueryParams.includeNativeConnectivity
        erQueryParams.includeAllConnectivities
    }

    void "command creates correct ErQueryParameters with only native connectivites"() {
        given:
        setDefaultCommandProperties()
        command.selectedConnectivities = ["native"]

        when:
        ErQueryParams erQueryParams = command.createErQueryParams()

        then:
        erQueryParams.connectivityProfileIds == [] as Set
        erQueryParams.customConnectivityNames == [] as Set
        !erQueryParams.includeAllConnectivities
        erQueryParams.includeNativeConnectivity
    }

    void "command creates correct ErQueryParameters with only custom connectivites"() {
        given:
        setDefaultCommandProperties()
        command.selectedConnectivities = [CUSTOM_CONNECTIVITY_NAME]

        when:
        ErQueryParams erQueryParams = command.createErQueryParams()

        then:
        erQueryParams.connectivityProfileIds == [] as Set
        erQueryParams.customConnectivityNames == [CUSTOM_CONNECTIVITY_NAME] as Set
        !erQueryParams.includeAllConnectivities
        !erQueryParams.includeNativeConnectivity
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
        params.from = '2013-08-18T16:00:00.000Z'
        params.to = '2013-08-18T18:00:00.000Z'
        params.selectedAggrGroupValuesUnCached = AggregatorType.RESULT_UNCACHED_DOC_COMPLETE_INCOMING_BYTES.toString()
        params.selectedFolder = '1'
        params.selectedPages = ['1', '5']
        params.selectedMeasuredEventIds = ['7', '8', '9']
        params.selectedConnectivities = ['1', CUSTOM_CONNECTIVITY_NAME, 'native']
        params.selectedBrowsers = '2'
        params.selectedLocations = '17'
        params.showDataMarkers = false
        params.showDataLabels = false
        params.selectedInterval = 0
        params.selectedTimeFrameInterval = 0
        params.selectChartType = 0
        params.trimBelowLoadTimes = 0
        params.trimAboveLoadTimes = 0
        params.trimBelowRequestCounts = 0
        params.trimAboveRequestCounts = 0
        params.trimBelowRequestSizes = 0
        params.trimAboveRequestSizes = 0
        params.chartWidth = 0
        params.chartHeight = 0
        params.loadTimeMinimum = 0
    }

    private setDefaultCommandProperties() {
        command.from = new DateTime(2013, 8, 18, 12, 0, 0, DateTimeZone.UTC)
        command.to = new DateTime(2013, 8, 19, 13, 0, 0, DateTimeZone.UTC)
        command.selectedFolder = [1L]
        command.selectedPages = [1L, 5L]
        command.selectedMeasuredEventIds = [7L, 8L, 9L]
        command.selectedBrowsers = [2L]
        command.selectedLocations = [17L]
        command.selectedAggrGroupValuesCached = [AggregatorType.RESULT_CACHED_DOC_COMPLETE_INCOMING_BYTES]
        command.selectedConnectivities = [CUSTOM_CONNECTIVITY_NAME, '1', 'native']
        command.selectedInterval = 0

        command.trimBelowLoadTimes = 0
        command.trimAboveLoadTimes = 0
        command.trimBelowRequestCounts = 0
        command.trimAboveRequestCounts = 0
        command.trimBelowRequestSizes = 0
        command.trimAboveRequestSizes = 0

        command.chartWidth = 0
        command.chartHeight = 0
        command.loadTimeMinimum = 0
        command.showDataMarkers = false
        command.showDataLabels = false
        command.validate()
    }
}

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

import de.iteratec.osm.ConfigService
import de.iteratec.osm.measurement.environment.Browser
import de.iteratec.osm.measurement.environment.BrowserService
import de.iteratec.osm.measurement.environment.Location
import de.iteratec.osm.measurement.environment.WebPageTestServer
import de.iteratec.osm.measurement.schedule.ConnectivityProfile
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.measurement.schedule.dao.JobGroupDaoService
import de.iteratec.osm.measurement.schedule.dao.PageDaoService
import de.iteratec.osm.report.UserspecificDashboardService
import de.iteratec.osm.report.chart.CsiAggregationInterval
import de.iteratec.osm.report.chart.CsiAggregationUtilService
import de.iteratec.osm.result.EventResultDashboardService
import de.iteratec.osm.result.MeasuredEvent
import de.iteratec.osm.result.TimeSeriesShowCommandBaseSpec
import de.iteratec.osm.result.dao.MeasuredEventDaoService
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.Interval
import org.joda.time.format.DateTimeFormatter
import org.joda.time.format.ISODateTimeFormat
import spock.lang.Specification
/**
 * <p>
 * Test-suite of {@link CsiDashboardController} and 
 * {@link CsiDashboardShowAllCommand}.
 * </p> 
 *
 * @author mze
 * @since IT-74
 */
@TestFor(CsiDashboardController)
@Mock([ConnectivityProfile, CsiSystem, CsiConfiguration])
class CsiDashboardControllerTests extends Specification {

    DateTimeFormatter ISO_FORMAT = ISODateTimeFormat.dateTime()
    CsiDashboardController controllerUnderTest
    CsiDashboardShowAllCommand command

    void setup() {

        // The controller under test:
        controllerUnderTest = controller

        // Mock relevant services:
        command = new CsiDashboardShowAllCommand()
        command.csiAggregationUtilService = Spy(CsiAggregationUtilService)

        controllerUnderTest.jobGroupDaoService = Stub(JobGroupDaoService)
        controllerUnderTest.pageDaoService = Stub(PageDaoService)
        controllerUnderTest.measuredEventDaoService = Stub(MeasuredEventDaoService)
        controllerUnderTest.browserService = Stub(BrowserService)
        controllerUnderTest.eventResultDashboardService = Stub(EventResultDashboardService)
        controllerUnderTest.userspecificDashboardService = Stub(UserspecificDashboardService) {
            getListOfAvailableCsiDashboards() >> []
        }
        controllerUnderTest.configService = Stub(ConfigService) {
            getInitialChartHeightInPixels() >> 400
        }
        controllerUnderTest.csiHelperService = Stub(CsiHelperService) {
            getCsiChartDefaultTitle() >> 'not relevant for these tests'
        }
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
        command.aggrGroupAndInterval == CsiDashboardController.HOURLY_MEASURED_EVENT
        command.selectedCsiSystems == [] as Set
        command.csiTypeVisuallyComplete
        !command.csiTypeDocComplete
        !command.includeInterval
        !command.overwriteWarningAboutLongProcessingTime
    }

    void "command with different aggregation group and interval values is valid"(String value) {
        given:
        setDefaultParams()
        params.aggrGroupAndInterval = value
        when:
        controllerUnderTest.bindData(command, params)

        then:
        command.validate()
        command.aggrGroupAndInterval == value

        where:
        value                                         | _
        CsiDashboardController.DAILY_AGGR_GROUP_PAGE  | _
        CsiDashboardController.WEEKLY_AGGR_GROUP_PAGE | _
        CsiDashboardController.DAILY_AGGR_GROUP_SHOP  | _
        CsiDashboardController.WEEKLY_AGGR_GROUP_SHOP | _
    }

    void "command aggregation by system is invalid without selected CSI system"(String aggrGroupAndInterval) {
        given:
        setDefaultParams()
        params.aggrGroupAndInterval = aggrGroupAndInterval

        when:
        controllerUnderTest.bindData(command, params)

        then:
        !command.validate()

        where:
        aggrGroupAndInterval                            | _
        CsiDashboardController.DAILY_AGGR_GROUP_SYSTEM  | _
        CsiDashboardController.WEEKLY_AGGR_GROUP_SYSTEM | _
    }

    void "command aggregation by system is valid with selected CSI system"(String aggrGroupAndInterval) {
        given:
        setDefaultParams()
        params.aggrGroupAndInterval = aggrGroupAndInterval
        params.selectedCsiSystems = ['1', '2']

        when:
        controllerUnderTest.bindData(command, params)

        then:
        command.validate()
        command.selectedCsiSystems == [1L, 2L] as Set

        where:
        aggrGroupAndInterval                            | _
        CsiDashboardController.DAILY_AGGR_GROUP_SYSTEM  | _
        CsiDashboardController.WEEKLY_AGGR_GROUP_SYSTEM | _
    }

    void "selected folder must be set for aggregation by measured event, page or shop"(String aggrGroupAndInterval) {
        given:
        setDefaultParams()
        params.selectedFolder = []
        params.aggrGroupAndInterval = aggrGroupAndInterval

        when:
        controllerUnderTest.bindData(command, params)

        then:
        !command.validate()

        where:
        aggrGroupAndInterval                          | _
        CsiDashboardController.HOURLY_MEASURED_EVENT  | _
        CsiDashboardController.DAILY_AGGR_GROUP_PAGE  | _
        CsiDashboardController.WEEKLY_AGGR_GROUP_PAGE | _
        CsiDashboardController.DAILY_AGGR_GROUP_SHOP  | _
        CsiDashboardController.WEEKLY_AGGR_GROUP_SHOP | _
    }

    void "selected folder may be empty for aggregation by system"(String aggrGroupAndInterval) {
        given:
        setDefaultParams()
        params.selectedFolder = []
        params.selectedCsiSystems = "1"
        params.aggrGroupAndInterval = aggrGroupAndInterval

        when:
        controllerUnderTest.bindData(command, params)

        then:
        command.validate()

        where:
        aggrGroupAndInterval                            | _
        CsiDashboardController.DAILY_AGGR_GROUP_SYSTEM  | _
        CsiDashboardController.WEEKLY_AGGR_GROUP_SYSTEM | _
    }

    void "selected pages must be set for aggregation by measured event or page"(String aggrGroupAndInterval) {
        given:
        setDefaultParams()
        params.selectedPages = []
        params.aggrGroupAndInterval = aggrGroupAndInterval

        when:
        controllerUnderTest.bindData(command, params)

        then:
        !command.validate()

        where:
        aggrGroupAndInterval                          | _
        CsiDashboardController.HOURLY_MEASURED_EVENT  | _
        CsiDashboardController.DAILY_AGGR_GROUP_PAGE  | _
        CsiDashboardController.WEEKLY_AGGR_GROUP_PAGE | _
    }

    void "selected pages may be empty for aggregation by shop or system"(String aggrGroupAndInterval) {
        given:
        setDefaultParams()
        params.selectedPages = []
        params.selectedCsiSystems = "1"
        params.aggrGroupAndInterval = aggrGroupAndInterval

        when:
        controllerUnderTest.bindData(command, params)

        then:
        command.validate()

        where:
        aggrGroupAndInterval                            | _
        CsiDashboardController.DAILY_AGGR_GROUP_SHOP    | _
        CsiDashboardController.DAILY_AGGR_GROUP_SYSTEM  | _
        CsiDashboardController.WEEKLY_AGGR_GROUP_SHOP   | _
        CsiDashboardController.WEEKLY_AGGR_GROUP_SYSTEM | _
    }

    void "command is invalid if neither csiTypeDocComplete nor csiTypeVisuallyComplete is set"() {
        given:
        setDefaultParams()
        params.csiTypeVisuallyComplete = false
        params.csiTypeDocComplete = false

        when:
        controllerUnderTest.bindData(command, params)

        then:
        !command.validate()
    }

    void "command is valid if either csiTypeDocComplete or csiTypeVisuallyComplete is set"(visually, docComplete) {
        given:
        setDefaultParams()
        params.csiTypeVisuallyComplete = visually
        params.csiTypeDocComplete = docComplete

        when:
        controllerUnderTest.bindData(command, params)

        then:
        command.validate()

        where:
        visually | docComplete
        true     | false
        false    | true
        true     | true
    }

    def "Should not warn about long processing time - weekly csi aggs interval two weeks"() {
        given: "Two weeks for 2 JobGroups, 5 Pages and 2 browsers of selected data and weekly csi aggs"
        int countOfSelectedBrowser = 2
        setDefaultParams()
        DateTime start = new DateTime(2013, 9, 30, 0, 0)
        DateTime end = new DateTime(2013, 10, 13, 23, 59)
        Interval timeFrameTwoWeeks = new Interval(start, end)
        int intervalOneWeek = 7 * 24 * 60
        params.selectedFolder = ['1', '2']
        params.selectedPages = ['1', '2', '3', '4', '5']

        when: "are given by the user in dashboard"
        controllerUnderTest.bindData(command, params)

        then: "No warning is provided about longer processing time"
        !command.shouldWarnAboutLongProcessingTime(timeFrameTwoWeeks, intervalOneWeek, countOfSelectedBrowser)
    }

    def "Should not warn about long processing time - hourly csi aggs interval one year"() {
        given: "One year for 2 JobGroups, 5 Pages and 2 browsers of selected data and hourly csi aggs"
        int countOfSelectedBrowser = 2
        DateTime start = new DateTime(2013, 9, 30, 0, 0)
        DateTime end = new DateTime(2014, 9, 30, 0, 0)
        Interval timeFrameOneYear = new Interval(start, end)
        int intervalOneHour = 60 // one hour
        params.selectedFolder = ['1', '2']
        params.selectedPages = ['1', '2', '3', '4', '5']

        when: "are given by the user in dashboard"
        controllerUnderTest.bindData(command, params)

        then: "A warning is provided about longer processing time"
        command.shouldWarnAboutLongProcessingTime(timeFrameOneYear, intervalOneHour, countOfSelectedBrowser)
    }

    void "create time frame including actual interval for daily aggregation"(String aggregationGroup) {
        given:
        DateTime toDateExpected = new DateTime().withTime(12, 0, 0, 0)
        DateTime fromDateExpected = toDateExpected.minusDays(14)
        setDefaultParams()
        params.from = ISO_FORMAT.print(fromDateExpected)
        params.to = ISO_FORMAT.print(toDateExpected)
        params.aggrGroupAndInterval = aggregationGroup
        params.includeInterval = true


        when:
        controllerUnderTest.bindData(command, params)
        Interval interval = command.createTimeFrameInterval()

        then:
        command.validate()
        interval.start == fromDateExpected
        interval.end == toDateExpected

        where:
        aggregationGroup                             | _
        CsiDashboardController.DAILY_AGGR_GROUP_PAGE | _
        CsiDashboardController.DAILY_AGGR_GROUP_SHOP | _
    }

    void "create time frame excluding actual interval for daily aggregation"(String aggregationGroup) {
        given:
        DateTime toDate = new DateTime().withTime(12, 0, 0, 0)
        DateTime toDateExpected = toDate.minusMinutes(CsiAggregationInterval.DAILY)
        DateTime fromDateExpected = toDate.minusDays(14)
        setDefaultParams()
        params.from = ISO_FORMAT.print(fromDateExpected)
        params.to = ISO_FORMAT.print(toDate)
        params.aggrGroupAndInterval = aggregationGroup
        params.includeInterval = false

        when:
        controllerUnderTest.bindData(command, params)
        Interval interval = command.createTimeFrameInterval()

        then:
        command.validate()
        interval.start == fromDateExpected
        interval.end == toDateExpected

        where:
        aggregationGroup                             | _
        CsiDashboardController.DAILY_AGGR_GROUP_PAGE | _
        CsiDashboardController.DAILY_AGGR_GROUP_SHOP | _
    }

    void "create time frame excluding actual interval for small chosen interval with daily aggregation"(String aggregationGroup) {
        given:
        DateTime toDate = new DateTime(2015, 4, 20, 12, 0, 0, DateTimeZone.UTC)
        DateTime toDateExpected = toDate.minusMinutes(CsiAggregationInterval.DAILY)
        DateTime fromDate = new DateTime(2015, 4, 19, 12, 0, 0, DateTimeZone.UTC)
        DateTime fromDateExpected = fromDate.minusMinutes(CsiAggregationInterval.DAILY)
        setDefaultParams()
        params.from = ISO_FORMAT.print(fromDate)
        params.to = ISO_FORMAT.print(toDate)
        params.aggrGroupAndInterval = aggregationGroup
        params.includeInterval = false
        command.csiAggregationUtilService.isInActualInterval(_, _) >> true


        when:
        controllerUnderTest.bindData(command, params)
        Interval interval = command.createTimeFrameInterval()

        then:
        command.validate()
        interval.start == fromDateExpected
        interval.end == toDateExpected

        where:
        aggregationGroup                             | _
        CsiDashboardController.DAILY_AGGR_GROUP_PAGE | _
        CsiDashboardController.DAILY_AGGR_GROUP_SHOP | _
    }

    void "create time frame including last interval if actual interval not in chosen for daily aggregation"(String aggregationGroup) {
        given:
        DateTime toDateExpected = new DateTime().withTime(12, 0, 0, 0).minusDays(2)
        DateTime fromDateExpected = toDateExpected.minusDays(14)
        setDefaultParams()
        params.from = ISO_FORMAT.print(fromDateExpected)
        params.to = ISO_FORMAT.print(toDateExpected)
        params.aggrGroupAndInterval = aggregationGroup
        params.includeInterval = true


        when:
        controllerUnderTest.bindData(command, params)
        Interval interval = command.createTimeFrameInterval()

        then:
        command.validate()
        interval.start == fromDateExpected
        interval.end == toDateExpected

        where:
        aggregationGroup                             | _
        CsiDashboardController.DAILY_AGGR_GROUP_PAGE | _
        CsiDashboardController.DAILY_AGGR_GROUP_SHOP | _
    }

    void "create time frame including actual interval for weekly aggregation"(String aggregationGroup) {
        given:
        DateTime toDateExpected = new DateTime().withTime(12, 0, 0, 0)
        DateTime fromDateExpected = toDateExpected.minusWeeks(12)
        setDefaultParams()
        params.from = ISO_FORMAT.print(fromDateExpected)
        params.to = ISO_FORMAT.print(toDateExpected)
        params.aggrGroupAndInterval = aggregationGroup
        params.includeInterval = true


        when:
        controllerUnderTest.bindData(command, params)
        Interval interval = command.createTimeFrameInterval()

        then:
        command.validate()
        interval.start == fromDateExpected
        interval.end == toDateExpected

        where:
        aggregationGroup                              | _
        CsiDashboardController.WEEKLY_AGGR_GROUP_PAGE | _
        CsiDashboardController.WEEKLY_AGGR_GROUP_SHOP | _
    }

    void "create time frame excluding actual interval for weekly aggregation"(String aggregationGroup) {
        given:
        DateTime toDate = new DateTime().withTime(12, 0, 0, 0)
        DateTime toDateExpected = toDate.minusMinutes(CsiAggregationInterval.WEEKLY)
        DateTime fromDateExpected = toDate.minusWeeks(12)
        setDefaultParams()
        params.from = ISO_FORMAT.print(fromDateExpected)
        params.to = ISO_FORMAT.print(toDate)
        params.aggrGroupAndInterval = aggregationGroup
        params.includeInterval = false

        when:
        controllerUnderTest.bindData(command, params)
        Interval interval = command.createTimeFrameInterval()

        then:
        command.validate()
        interval.start == fromDateExpected
        interval.end == toDateExpected

        where:
        aggregationGroup                              | _
        CsiDashboardController.WEEKLY_AGGR_GROUP_PAGE | _
        CsiDashboardController.WEEKLY_AGGR_GROUP_SHOP | _
    }

    void "create time frame excluding actual interval for small chosen interval with weekly aggregation"(String aggregationGroup) {
        given:
        DateTime toDate = new DateTime(2015, 4, 20, 12, 0, 0, DateTimeZone.UTC)
        DateTime toDateExpected = toDate.minusMinutes(CsiAggregationInterval.WEEKLY)
        DateTime fromDate = new DateTime(2015, 4, 16, 12, 0, 0, DateTimeZone.UTC)
        DateTime fromDateExpected = fromDate.minusMinutes(CsiAggregationInterval.WEEKLY)
        setDefaultParams()
        params.from = ISO_FORMAT.print(fromDate)
        params.to = ISO_FORMAT.print(toDate)
        params.aggrGroupAndInterval = aggregationGroup
        params.includeInterval = false
        command.csiAggregationUtilService.isInActualInterval(_, _) >> true


        when:
        controllerUnderTest.bindData(command, params)
        Interval interval = command.createTimeFrameInterval()

        then:
        command.validate()
        interval.start == fromDateExpected
        interval.end == toDateExpected

        where:
        aggregationGroup                              | _
        CsiDashboardController.WEEKLY_AGGR_GROUP_PAGE | _
        CsiDashboardController.WEEKLY_AGGR_GROUP_SHOP | _
    }

    void "create time frame including last interval if actual interval not in chosen for weekly aggregation"(String aggregationGroup) {
        given:
        DateTime toDateExpected = new DateTime().withTime(12, 0, 0, 0).minusWeeks(2)
        DateTime fromDateExpected = toDateExpected.minusWeeks(12)
        setDefaultParams()
        params.from = ISO_FORMAT.print(fromDateExpected)
        params.to = ISO_FORMAT.print(toDateExpected)
        params.aggrGroupAndInterval = aggregationGroup
        params.includeInterval = true


        when:
        controllerUnderTest.bindData(command, params)
        Interval interval = command.createTimeFrameInterval()

        then:
        command.validate()
        interval.start == fromDateExpected
        interval.end == toDateExpected

        where:
        aggregationGroup                              | _
        CsiDashboardController.WEEKLY_AGGR_GROUP_PAGE | _
        CsiDashboardController.WEEKLY_AGGR_GROUP_SHOP | _
    }

    void "construct view data of show all"() {
        given:
        createDefaultDaoMockData()

        Browser browser1 = new Browser(name: 'Browser1') {
            Long getId() { return 11L }
        }
        controllerUnderTest.browserService.findAll() >> {
            [browser1] as Set
        }

        WebPageTestServer server1 = new WebPageTestServer(label: 'server1')

        Location location1 = new Location(label: 'Location1', location: 'locationA', browser: browser1, wptServer: server1) {
            Long getId() { return 101L }
        }
        Location location2 = new Location(label: 'Location2', location: 'locationB', browser: browser1, wptServer: server1) {
            Long getId() { return 102L }
        }
        Location location3 = new Location(label: 'Location3', location: 'locationC', browser: browser1, wptServer: server1) {
            Long getId() { return 103L }
        }

        when:
        Map<String, Object> result = controllerUnderTest.constructStaticViewDataOfShowAll()

        then:
        result != null
        result.size() == 14

        result["aggrGroupLabels"] == CsiDashboardController.AGGREGATOR_GROUP_LABELS
        result["aggrGroupValues"] == CsiDashboardController.AGGREGATOR_GROUP_VALUES
        result["folders"]*.getName() == ["Group1", "Group2"]
        result["pages"]*.getName() == ["Page1", "Page2", "Page3"]
        result["measuredEvents"]*.getName() == ["MeasuredEvent1", "MeasuredEvent2", "MeasuredEvent3", "MeasuredEvent4"]
        result["browsers"]*.getName() == ["Browser1"]
        result["locations"]*.getLabel() == ["Location1", "Location2", "Location3"]
        result["dateFormat"] == CsiDashboardController.DATE_FORMAT_STRING_FOR_HIGH_CHART
        result["eventsOfPages"] == [
                (1L): [1003L] as Set,
                (2L): [1002L, 1004L] as Set,
                (3L): [1001L] as Set
        ]
        result["locationsOfBrowsers"] == [
                (11L): [101L, 102L, 103L] as Set
        ]
    }


    void "construct view data of show all with duplicated location strings"() {
        given:
        createDefaultDaoMockData()

        Browser browser1 = new Browser(name: 'Browser1') {
            Long getId() { return 11L }
        }
        Browser browser2 = new Browser(name: 'Browser2') {
            Long getId() { return 12L }
        }
        controllerUnderTest.browserService.findAll() >> {
            [browser1, browser2] as Set
        }

        WebPageTestServer server1 = new WebPageTestServer(label: 'server1')
        WebPageTestServer server2 = new WebPageTestServer(label: 'server2')

        Location location1 = new Location(label: 'Location1', location: 'duplicatedLocation', browser: browser1, wptServer: server1) {
            Long getId() { return 101L }
        }
        Location location2 = new Location(label: 'Location2', location: 'duplicatedLocation', browser: browser2, wptServer: server2) {
            Long getId() { return 102L }
        }
        Location location3 = new Location(label: 'Location3', location: 'duplicatedLocation', browser: browser2, wptServer: server1) {
            Long getId() { return 103L }
        }

        when:
        // Run the test:
        Map<String, Object> result = controllerUnderTest.constructStaticViewDataOfShowAll()
        then:
        result != null
        result.size() == 14

        result["aggrGroupLabels"] == CsiDashboardController.AGGREGATOR_GROUP_LABELS
        result["aggrGroupValues"] == CsiDashboardController.AGGREGATOR_GROUP_VALUES
        result["folders"]*.getName() == ["Group1", "Group2"]
        result["pages"]*.getName() == ["Page1", "Page2", "Page3"]
        result["measuredEvents"]*.getName() == ["MeasuredEvent1", "MeasuredEvent2", "MeasuredEvent3", "MeasuredEvent4"]
        result["browsers"]*.getName() == ["Browser1", "Browser2"]
        result["locations"]*.getLabel() == ["Location1", "Location3", "Location2"]
        result["dateFormat"] == CsiDashboardController.DATE_FORMAT_STRING_FOR_HIGH_CHART
        result["eventsOfPages"] == [
                (1L): [1003L] as Set,
                (2L): [1002L, 1004L] as Set,
                (3L): [1001L] as Set
        ]
        result["locationsOfBrowsers"] == [
                (11L): [101L] as Set,
                (12L): [102L, 103L] as Set
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
        result != null
        result.size() == 25
        result["aggrGroupAndInterval"] == CsiDashboardController.HOURLY_MEASURED_EVENT
        result["selectedCsiSystems"] == [] as Set
        !result["overwriteWarningAboutLongProcessingTime"]
        result["csiTypeVisuallyComplete"]
        !result["csiTypeDocComplete"]
        !result["includeInterval"]
    }

    void "invalid request causes error message by passing command back to page"() {
        given:
        setDefaultParams()
        params.from = null

        when:
        controllerUnderTest.bindData(command, params)
        Map<String, Object> result = controllerUnderTest.showAll(command)

        then:
        !command.validate()
        result["command"] == command
    }


    void "empty request does not cause error message and passes command back to page"() {
        given:
        // We leave 'params' 'empty' here (only grails additions are present! => first and empty request!
        params.action = 'showAll'
        params.controller = 'csiDashboard'

        when:
        controllerUnderTest.bindData(command, params)
        Map<String, Object> model = controllerUnderTest.showAll(command)

        then:
        !command.validate()
        !model['command']
    }

    void "empty request does not cause error message with language and passes command back to page"() {
        given:
        // We leave 'params' 'empty' here (only grails additions are present! => first and empty request!
        params.lang = 'de'
        params.action = 'showAll'
        params.controller = 'csiDashboard'

        when:
        controllerUnderTest.bindData(command, params)
        Map<String, Object> model = controllerUnderTest.showAll(command)

        then:
        !command.validate()
        !model['command']
    }

    void "get control name for CSI type"(CsiType type, String controlName) {
        expect:
        CsiDashboardShowAllCommand.receiveControlnameFor(type) == controlName

        where:
        type                      | controlName
        CsiType.VISUALLY_COMPLETE | "csiTypeVisuallyComplete"
        CsiType.DOC_COMPLETE      | "csiTypeDocComplete"
    }

    void "get control name throws for non existent CSI type"() {
        when:
        CsiDashboardShowAllCommand.receiveControlnameFor(CsiType.valueOf('NOT_EXISTANT'))

        then:
        IllegalArgumentException e = thrown()
    }

    private setDefaultParams() {
        TimeSeriesShowCommandBaseSpec.getDefaultParams().each { key, value ->
            params.setProperty(key, value)
        }
        params.aggrGroupAndInterval = CsiDashboardController.HOURLY_MEASURED_EVENT
        params.selectedCsiSystems = []
        params.overwriteWarningAboutLongProcessingTime = false
        params.includeInterval = false
        params.csiTypeVisuallyComplete = true
        params.csiTypeDocComplete = false
    }

    private setDefaultCommandProperties() {
        TimeSeriesShowCommandBaseSpec.setDefaultCommandProperties(command)
        command.aggrGroupAndInterval = CsiDashboardController.HOURLY_MEASURED_EVENT
        command.selectedCsiSystems = []
        command.overwriteWarningAboutLongProcessingTime = false

        command.includeInterval = false
        command.csiTypeVisuallyComplete = true
        command.csiTypeDocComplete = false
    }

    private createDefaultDaoMockData() {
        controllerUnderTest.jobGroupDaoService.findCSIGroups() >> {
            [new JobGroup(name: 'Group2'), new JobGroup(name: 'Group1')] as Set
        }

        Page page1 = new Page(name: 'Page1', weight: 0) {
            Long getId() { return 1L }
        }
        Page page2 = new Page(name: 'Page3', weight: 0.25d) {
            Long getId() { return 2L }
        }
        Page page3 = new Page(name: 'Page2', weight: 0.5d) {
            Long getId() { return 3L }
        }
        controllerUnderTest.pageDaoService.findAll() >> {
            [page1, page2, page3] as Set
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
        controllerUnderTest.measuredEventDaoService.findAll() >> {
            [measuredEvent3, measuredEvent1, measuredEvent2, measuredEvent4] as Set
        }
    }

}

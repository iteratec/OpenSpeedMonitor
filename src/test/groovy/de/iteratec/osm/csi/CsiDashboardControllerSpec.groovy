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
import de.iteratec.osm.measurement.schedule.ConnectivityProfile
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.measurement.schedule.dao.JobGroupDaoService
import de.iteratec.osm.report.UserspecificDashboardService
import de.iteratec.osm.report.chart.CsiAggregationInterval
import de.iteratec.osm.report.chart.CsiAggregationUtilService
import de.iteratec.osm.result.EventResultDashboardService
import de.iteratec.osm.result.MeasuredEvent
import de.iteratec.osm.result.TimeSeriesShowCommandBaseSpec
import grails.buildtestdata.mixin.Build
import grails.plugins.taggable.TagLink
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import org.joda.time.DateTime
import org.joda.time.Interval
import org.joda.time.format.DateTimeFormatter
import org.joda.time.format.ISODateTimeFormat
import spock.lang.Specification

@TestFor(CsiDashboardController)
@Build([Location, JobGroup, Page, Browser, CsiConfiguration, MeasuredEvent])
@Mock([Location, JobGroup, Page, Browser, ConnectivityProfile, CsiSystem, TagLink, CsiConfiguration])
class CsiDashboardControllerSpec extends Specification {

    DateTimeFormatter ISO_FORMAT = ISODateTimeFormat.dateTime()
    CsiDashboardController controllerUnderTest
    CsiDashboardShowAllCommand command

    def doWithSpring = {
        eventResultDashboardService(EventResultDashboardService)
        browserService(BrowserService)
    }

    void setup() {
        controllerUnderTest = controller
        command = new CsiDashboardShowAllCommand()

        controller.csiHelperService = Mock(CsiHelperService)
        controller.userspecificDashboardService = Mock(UserspecificDashboardService)
        controller.configService = Stub(ConfigService) {
            getInitialChartHeightInPixels() >> 400
        }
        controller.jobGroupDaoService = Stub(JobGroupDaoService) {
            findAll() >> { JobGroup.findAll() }
            findCSIGroups() >> { JobGroup.findAllByCsiConfigurationIsNotNull() }
        }
        command.csiAggregationUtilService = Spy(CsiAggregationUtilService)
    }

    void "command without bound parameters is invalid"() {
        expect: "an empty command doesn't validate"
        !command.validate()
    }

    void "command bound with default parameters is valid"() {
        given: "a default parameter set"
        setDefaultParams()

        when: "the controller binds the data to"
        controllerUnderTest.bindData(command, params)

        then: "the correct data is bound"
        command.validate()
        command.aggrGroupAndInterval == CsiDashboardController.HOURLY_MEASURED_EVENT
        command.selectedCsiSystems == [] as Set
        command.csiTypeVisuallyComplete
        !command.csiTypeDocComplete
        !command.includeInterval
        !command.overwriteWarningAboutLongProcessingTime
    }

    void "command with different aggregation group and interval values is valid"(String value) {
        given: "the default parameters and the aggregation group and interval"
        setDefaultParams()
        params.aggrGroupAndInterval = value

        when: "the controller binds the parameters"
        controllerUnderTest.bindData(command, params)

        then: "the command is valid, with the correct aggregation group und interval set"
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
        given: "default parameters and aggregation by system, but no selected CSI system"
        setDefaultParams()
        params.aggrGroupAndInterval = aggrGroupAndInterval

        when: "the controller binds the parameters"
        controllerUnderTest.bindData(command, params)

        then: "the command is not valid"
        !command.validate()

        where:
        aggrGroupAndInterval                            | _
        CsiDashboardController.DAILY_AGGR_GROUP_SYSTEM  | _
        CsiDashboardController.WEEKLY_AGGR_GROUP_SYSTEM | _
    }

    void "command aggregation by system is valid with selected CSI system"(String aggrGroupAndInterval) {
        given: "default parameters and aggregation by system and CSI systems"
        setDefaultParams()
        params.aggrGroupAndInterval = aggrGroupAndInterval
        params.selectedCsiSystems = ['1', '2']

        when: "the controller binds the parameters"
        controllerUnderTest.bindData(command, params)

        then: "the command is valid and has the right selected CSI systems"
        command.validate()
        command.selectedCsiSystems == [1L, 2L] as Set

        where:
        aggrGroupAndInterval                            | _
        CsiDashboardController.DAILY_AGGR_GROUP_SYSTEM  | _
        CsiDashboardController.WEEKLY_AGGR_GROUP_SYSTEM | _
    }

    void "selected folder must be set for aggregation by measured event, page or shop"(String aggrGroupAndInterval) {
        given: "aggregatiuon by measured event, page, or shop, but no selected job groups/folders"
        setDefaultParams()
        params.selectedFolder = []
        params.aggrGroupAndInterval = aggrGroupAndInterval

        when: "the controller binds the parameters"
        controllerUnderTest.bindData(command, params)

        then: "the command is invalid"
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
        given: "aggregation by system, no folders, but a selected CSI system"
        setDefaultParams()
        params.selectedFolder = []
        params.selectedCsiSystems = "1"
        params.aggrGroupAndInterval = aggrGroupAndInterval

        when: "the controller binds the parameters"
        controllerUnderTest.bindData(command, params)

        then: "the command is valid"
        command.validate()

        where:
        aggrGroupAndInterval                            | _
        CsiDashboardController.DAILY_AGGR_GROUP_SYSTEM  | _
        CsiDashboardController.WEEKLY_AGGR_GROUP_SYSTEM | _
    }

    void "selected pages must be set for aggregation by measured event or page"(String aggrGroupAndInterval) {
        given: "aggregation by measured event or page, but no selected pages"
        setDefaultParams()
        params.selectedPages = []
        params.aggrGroupAndInterval = aggrGroupAndInterval

        when: "the controller binds the parameters"
        controllerUnderTest.bindData(command, params)

        then: "the command is invalid"
        !command.validate()

        where:
        aggrGroupAndInterval                          | _
        CsiDashboardController.HOURLY_MEASURED_EVENT  | _
        CsiDashboardController.DAILY_AGGR_GROUP_PAGE  | _
        CsiDashboardController.WEEKLY_AGGR_GROUP_PAGE | _
    }

    void "selected pages may be empty for aggregation by shop or system"(String aggrGroupAndInterval) {
        given: "aggregation by shop or system, no selected pages, but a selected system"
        setDefaultParams()
        params.selectedPages = []
        params.selectedCsiSystems = "1"
        params.aggrGroupAndInterval = aggrGroupAndInterval

        when:  "the controller binds the parameters"
        controllerUnderTest.bindData(command, params)

        then:  "the command is valid"
        command.validate()

        where:
        aggrGroupAndInterval                            | _
        CsiDashboardController.DAILY_AGGR_GROUP_SHOP    | _
        CsiDashboardController.DAILY_AGGR_GROUP_SYSTEM  | _
        CsiDashboardController.WEEKLY_AGGR_GROUP_SHOP   | _
        CsiDashboardController.WEEKLY_AGGR_GROUP_SYSTEM | _
    }

    void "command is invalid if neither csiTypeDocComplete nor csiTypeVisuallyComplete is set"() {
        given: "parameters where neither csiTypeDocComplete nor csiTypeVisuallyComplete is set"
        setDefaultParams()
        params.csiTypeVisuallyComplete = false
        params.csiTypeDocComplete = false

        when: "the controller binds the data"
        controllerUnderTest.bindData(command, params)

        then: "the command is not valid"
        !command.validate()
    }

    void "command is valid if either csiTypeDocComplete or csiTypeVisuallyComplete is set"(visually, docComplete) {
        given: "parameter with either csiTypeDocComplete or csiTypeVisuallyComplete set"
        setDefaultParams()
        params.csiTypeVisuallyComplete = visually
        params.csiTypeDocComplete = docComplete

        when: "the controller binds the data"
        controllerUnderTest.bindData(command, params)

        then: "the command is valid"
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


        when: "the command creates the time frame interval"
        controllerUnderTest.bindData(command, params)
        Interval interval = command.createTimeFrameInterval()

        then: "the interval is the same as bound"
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

        when: "the command creates the time frame interval"
        controllerUnderTest.bindData(command, params)
        Interval interval = command.createTimeFrameInterval()

        then: "the interval is adjusted by the aggregation interval"
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
        DateTime toDate = new DateTime(2015, 4, 20, 12, 0, 0)
        DateTime toDateExpected = toDate.minusMinutes(CsiAggregationInterval.DAILY)
        DateTime fromDate = new DateTime(2015, 4, 19, 12, 0, 0)
        DateTime fromDateExpected = fromDate.minusMinutes(CsiAggregationInterval.DAILY)
        setDefaultParams()
        params.from = ISO_FORMAT.print(fromDate)
        params.to = ISO_FORMAT.print(toDate)
        params.aggrGroupAndInterval = aggregationGroup
        params.includeInterval = false
        command.csiAggregationUtilService.isInActualInterval(_, _) >> true

        when: "the command creates the time frame interval"
        controllerUnderTest.bindData(command, params)
        Interval interval = command.createTimeFrameInterval()

        then: "the interval is adjusted for daily aggregation"
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


        when: "the command creates the time frame interval"
        controllerUnderTest.bindData(command, params)
        Interval interval = command.createTimeFrameInterval()

        then: "the interval is the same as set in the parameters"
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


        when: "the command creates the time frame interval"
        controllerUnderTest.bindData(command, params)
        Interval interval = command.createTimeFrameInterval()

        then: "the time frame is the same as in the parameters"
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

        when: "the command creates the time frame interval"
        controllerUnderTest.bindData(command, params)
        Interval interval = command.createTimeFrameInterval()

        then: "the time frame is adjusted for weekly aggregation intervals"
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
        DateTime toDate = new DateTime(2015, 4, 20, 12, 0, 0)
        DateTime toDateExpected = toDate.minusMinutes(CsiAggregationInterval.WEEKLY)
        DateTime fromDate = new DateTime(2015, 4, 16, 12, 0, 0)
        DateTime fromDateExpected = fromDate.minusMinutes(CsiAggregationInterval.WEEKLY)
        setDefaultParams()
        params.from = ISO_FORMAT.print(fromDate)
        params.to = ISO_FORMAT.print(toDate)
        params.aggrGroupAndInterval = aggregationGroup
        params.includeInterval = false
        command.csiAggregationUtilService.isInActualInterval(_, _) >> true


        when: "the command creates the time frame interval"
        controllerUnderTest.bindData(command, params)
        Interval interval = command.createTimeFrameInterval()

        then: "the time frame is adjusted for weekly intervals"
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


        when: "the command creates the time frame interval"
        controllerUnderTest.bindData(command, params)
        Interval interval = command.createTimeFrameInterval()

        then: "the time frame is the same as in the parameters"
        command.validate()
        interval.start == fromDateExpected
        interval.end == toDateExpected

        where:
        aggregationGroup                              | _
        CsiDashboardController.WEEKLY_AGGR_GROUP_PAGE | _
        CsiDashboardController.WEEKLY_AGGR_GROUP_SHOP | _
    }

    void "construct view data of show all"() {
        setup:
        2.times { JobGroup.build(name: "Group${it + 1}", csiConfiguration: CsiConfiguration.build()) }
        Browser.build(id: 11L, name: "Browser1")
        List<Page> pages = (1..3).collect { Page.build(name: "Page${it}") }
        MeasuredEvent measuredEvent1 = MeasuredEvent.build(name: "MeasuredEvent1", testedPage: pages[2])
        MeasuredEvent measuredEvent2 = MeasuredEvent.build(name: "MeasuredEvent2", testedPage: pages[1])
        MeasuredEvent measuredEvent3 = MeasuredEvent.build(name: "MeasuredEvent3", testedPage: pages[0])
        MeasuredEvent measuredEvent4 = MeasuredEvent.build(name: "MeasuredEvent4", testedPage: pages[1])
        Browser browser = Browser.build(name: "Browser1")
        List<Location> locations = (1..3).collect { Location.build(label: "Location${it}", browser: browser) }

        when: "the controller creates static view data"
        Map<String, Object> result = controllerUnderTest.constructStaticViewDataOfShowAll()

        then: "all relevant domain data is included in the result"
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
                (pages[0].id): [measuredEvent3.id] as Set,
                (pages[1].id): [measuredEvent2.id, measuredEvent4.id] as Set,
                (pages[2].id): [measuredEvent1.id] as Set
        ]
        result["locationsOfBrowsers"] == [
                (browser.id): locations*.id as Set
        ]
    }


    void "construct view data of show all with duplicated location strings"() {
        given: "multiple locations with the same 'location' property"
        Browser browser1 = Browser.build(name: "Browser1")
        Browser browser2 = Browser.build(name: "Browser2")
        Location location1 =Location.build(label: "Location1", location: 'duplicatedLocation', browser: browser1)
        Location location2 =Location.build(label: "Location2", location: 'duplicatedLocation', browser: browser2)
        Location location3 =Location.build(label: "Location3", location: 'duplicatedLocation', browser: browser2)

        when: "the controller constructs stativ view data"
        Map<String, Object> result = controllerUnderTest.constructStaticViewDataOfShowAll()

        then: "all locations and browsers are included in the result"
        result != null
        result["browsers"]*.getName() as Set == ["Browser1", "Browser2"] as Set
        result["locations"]*.getLabel() as Set == ["Location1", "Location3", "Location2"] as Set
        result["locationsOfBrowsers"] == [
                (browser1.id): [location1.id] as Set,
                (browser2.id): [location2.id, location3.id] as Set
        ]
    }

    void "command properties are correctly copied to map"() {
        given: "a valid command"
        setDefaultCommandProperties()

        when: "the data should be copied to a model map"
        Map<String, Object> result = [:]
        command.copyRequestDataToViewModelMap(result)

        then: "the command is valid and the result contains the bound information"
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
        given: "parameters without a 'from' value"
        setDefaultParams()
        params.from = null

        when: "the parameters are bound to the command"
        controllerUnderTest.bindData(command, params)
        Map<String, Object> result = controllerUnderTest.showAll(command)

        then: "the command is invalid and part of the result"
        !command.validate()
        result["command"] == command
    }


    void "empty request does not cause error message with language and passes command back to page"() {
        given: "parameters with no form data specified"
        params.lang = 'de'
        params.action = 'showAll'
        params.controller = 'csiDashboard'

        when: "the parameters are bound to the command"
        controllerUnderTest.bindData(command, params)
        Map<String, Object> model = controllerUnderTest.showAll(command)

        then: "the command is not valid"
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
        when: "the control name for a not existant CSI type is requested"
        CsiDashboardShowAllCommand.receiveControlnameFor(CsiType.valueOf('NOT_EXISTANT'))

        then: "an exception is thrown"
        thrown(IllegalArgumentException)
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
}

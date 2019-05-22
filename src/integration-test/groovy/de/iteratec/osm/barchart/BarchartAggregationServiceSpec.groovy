package de.iteratec.osm.barchart

import com.google.gson.JsonObject
import de.iteratec.osm.OsmConfiguration
import de.iteratec.osm.csi.NonTransactionalIntegrationSpec
import de.iteratec.osm.csi.Page
import de.iteratec.osm.d3Data.GetPageComparisonDataCommand
import de.iteratec.osm.measurement.environment.Browser
import de.iteratec.osm.measurement.environment.Location
import de.iteratec.osm.measurement.schedule.ConnectivityProfile
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.result.DeviceType
import de.iteratec.osm.result.EventResult
import de.iteratec.osm.result.JobResult
import de.iteratec.osm.result.MeasuredEvent
import de.iteratec.osm.result.OperatingSystem
import grails.testing.mixin.integration.Integration
import grails.gorm.transactions.Rollback
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter


@Integration(applicationClass = openspeedmonitor.Application.class)
@Rollback
class BarchartAggregationServiceSpec extends NonTransactionalIntegrationSpec {

    BarchartAggregationService barchartAggregationService
    GetBarchartCommand cmd1, cmd2, cmd3
    GetPageComparisonDataCommand comCmd
    Browser browser
    Location location
    JobGroup jobGroup
    JobResult jobResult
    MeasuredEvent measuredEvent
    Page page, page2
    ConnectivityProfile connectivityProfile

    DateTimeFormatter formatter = DateTimeFormat.forPattern("dd/MM/yyyy")
    DateTime yesterday = formatter.parseDateTime("20/05/2019")
    DateTime today = formatter.parseDateTime("21/05/2019")


    def setup() {
        setupData()
        setupBarchartCommands()
    }

    void "evaluate barChartCommand for barchartAggregations"() {
        when: "the data for the time series chart gets created and we trim the data below and above some given values"
        List<BarchartAggregation> barchartAggregations1 = barchartAggregationService.getBarchartAggregationsFor(cmd1)
        List<BarchartAggregation> barchartAggregations2 = barchartAggregationService.getBarchartAggregationsFor(cmd2)
        List<BarchartAggregation> barchartAggregations3 = barchartAggregationService.getBarchartAggregationsFor(cmd3)

        then: "we only get the event results within the given range"
        barchartAggregations1.get(0).value == 750
        barchartAggregations2.get(0).value == 1500
        barchartAggregations3.get(0).value == 1000
        barchartAggregations3.get(0).valueComparative == 750
    }

    void "evaluate pageComparisonDataC for barchartAggregations"() {
        when: "the data for the time series chart gets created and we trim the data below and above some given values"
        List<PageComparisonAggregation> barchartAggregations = barchartAggregationService.getBarChartAggregationsFor(comCmd)

        then: "we only get the event results within the given range"
        barchartAggregations.get(0).baseAggregation.value == 1000
        barchartAggregations.get(0).baseAggregation.page.name == "HP"
        barchartAggregations.get(0).comperativeAggregation.value == 2000
        barchartAggregations.get(0).comperativeAggregation.page.name == "HP_entry"
    }


    private setupData() {
        OsmConfiguration.build()
        page = Page.build(name: "HP")
        page2 = Page.build(name: "HP_entry")
        jobGroup = JobGroup.build(name: "Test")
        browser = Browser.build(id: 1.toLong())
        location = Location.build()
        measuredEvent = MeasuredEvent.build()
        connectivityProfile = ConnectivityProfile.build()

        EventResult.build(
                medianValue: true,
                page: page,
                jobGroup: jobGroup,
                browser: browser,
                location: location,
                connectivityProfile: connectivityProfile,
                measuredEvent: measuredEvent,
                docCompleteTimeInMillisecs: 500,
                operatingSystem: OperatingSystem.WINDOWS,
                deviceType: DeviceType.DESKTOP,
                jobResultDate: today.toDate()
        )

        EventResult.build(
                medianValue: true,
                page: page,
                jobGroup: jobGroup,
                browser: browser,
                location: location,
                connectivityProfile: connectivityProfile,
                measuredEvent: measuredEvent,
                docCompleteTimeInMillisecs: 1000,
                operatingSystem: OperatingSystem.WINDOWS,
                deviceType: DeviceType.DESKTOP,
                jobResultDate: today.toDate(),
        )

        EventResult.build(
                medianValue: true,
                page: page,
                jobGroup: jobGroup,
                browser: browser,
                location: location,
                connectivityProfile: connectivityProfile,
                measuredEvent: measuredEvent,
                docCompleteTimeInMillisecs: 1500,
                operatingSystem: OperatingSystem.ANDROID,
                deviceType: DeviceType.SMARTPHONE,
                jobResultDate: today.toDate(),
        )

        EventResult.build(
                medianValue: true,
                page: page,
                jobGroup: jobGroup,
                browser: browser,
                location: location,
                connectivityProfile: connectivityProfile,
                measuredEvent: measuredEvent,
                docCompleteTimeInMillisecs: 1000,
                operatingSystem: OperatingSystem.ANDROID,
                deviceType: DeviceType.DESKTOP,
                jobResultDate: today.toDate(),
        )

        EventResult.build(
                medianValue: true,
                page: page,
                jobGroup: jobGroup,
                browser: browser,
                location: location,
                connectivityProfile: connectivityProfile,
                measuredEvent: measuredEvent,
                docCompleteTimeInMillisecs: 500,
                operatingSystem: OperatingSystem.IOS,
                deviceType: DeviceType.TABLET,
                jobResultDate: yesterday.toDate(),
        )

        EventResult.build(
                medianValue: true,
                page: page,
                jobGroup: jobGroup,
                browser: browser,
                location: location,
                connectivityProfile: connectivityProfile,
                measuredEvent: measuredEvent,
                docCompleteTimeInMillisecs: 1000,
                operatingSystem: OperatingSystem.IOS,
                deviceType: DeviceType.TABLET,
                jobResultDate: yesterday.toDate(),
        )

        EventResult.build(
                medianValue: true,
                page: page2,
                jobGroup: jobGroup,
                browser: browser,
                location: location,
                connectivityProfile: connectivityProfile,
                measuredEvent: measuredEvent,
                docCompleteTimeInMillisecs: 2000,
                operatingSystem: OperatingSystem.IOS,
                deviceType: DeviceType.TABLET,
                jobResultDate: today.toDate(),
        )

    }

    private setupBarchartCommands() {
        cmd1 = new GetBarchartCommand()
        cmd2 = new GetBarchartCommand()
        cmd3 = new GetBarchartCommand()

        def selectedSeries = ["measurands": ["DOC_COMPLETE_TIME"]]

        cmd1.selectedOperatingSystems = ["Windows"]
        cmd1.selectedPages = ["HP"]
        cmd1.selectedJobGroups = ["Test"]
        cmd1.selectedAggregationValue = 'avg'
        cmd1.selectedSeries = [selectedSeries]
        cmd1.from = today.minusHours(12)
        cmd1.to = today.plusHours(12)

        cmd2.selectedOperatingSystems = ["Android"]
        cmd2.selectedDeviceTypes = ["Smartphone"]
        cmd2.selectedPages = ["HP"]
        cmd2.selectedJobGroups = ["Test"]
        cmd2.selectedAggregationValue = 'avg'
        cmd2.selectedSeries = [selectedSeries]
        cmd2.from = today.minusHours(12)
        cmd2.to = today.plusHours(12)

        cmd3.selectedPages = ["HP"]
        cmd3.selectedJobGroups = ["Test"]
        cmd3.selectedBrowsers = [1.toLong()]
        cmd3.selectedAggregationValue = 'avg'
        cmd3.selectedSeries = [selectedSeries]
        cmd3.from = today.minusHours(12)
        cmd3.to = today.plusHours(12)
        cmd3.fromComparative = yesterday.minusHours(12)
        cmd3.toComparative = yesterday.plusHours(12)

        comCmd = new GetPageComparisonDataCommand()
        comCmd.from = today.minusHours(12)
        comCmd.to = today.plusHours(12)
        comCmd.measurand = "DOC_COMPLETE_TIME"
        comCmd.selectedAggregationValue = 'avg'

        def pageComparison = new JsonSlurper().parseText('{"firstJobGroupId":"1","firstPageId":"1","secondPageId":"2","secondJobGroupId":"1"}')

        comCmd.selectedPageComparisons = [pageComparison]
    }
}

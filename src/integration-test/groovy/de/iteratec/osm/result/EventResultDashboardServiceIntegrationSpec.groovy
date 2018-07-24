package de.iteratec.osm.result

import de.iteratec.osm.OsmConfiguration
import de.iteratec.osm.csi.NonTransactionalIntegrationSpec
import de.iteratec.osm.csi.Page
import de.iteratec.osm.measurement.environment.Browser
import de.iteratec.osm.measurement.environment.Location
import de.iteratec.osm.measurement.schedule.ConnectivityProfile
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.report.chart.CsiAggregationInterval
import de.iteratec.osm.report.chart.OsmRickshawChart
import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import grails.web.mapping.LinkGenerator
import org.joda.time.DateTime
import org.joda.time.DateTimeZone

@Integration(applicationClass = openspeedmonitor.Application.class)
@Rollback
class EventResultDashboardServiceIntegrationSpec extends NonTransactionalIntegrationSpec {

    EventResultDashboardService eventResultDashboardService

    Browser browser
    Location location
    JobGroup jobGroup
    JobResult jobResult
    MeasuredEvent measuredEvent
    Page page
    ConnectivityProfile connectivityProfile
    DateTime runDate = new DateTime(DateTimeZone.UTC)


    def setup() {
        eventResultDashboardService.grailsLinkGenerator = Mock(LinkGenerator)
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


    void "get event result dashboard chart map with trimmed values"(int csiAggregationInterval, int expectedNumberOfPoints, int expectedValue) {
        given: "four event results with different domTimeInMillisecs"

        EventResult.withNewSession { session ->
            OsmConfiguration.build()
            page = Page.build()
            jobGroup = JobGroup.build()
            browser = Browser.build()
            location = Location.build()
            measuredEvent = MeasuredEvent.build()
            connectivityProfile = ConnectivityProfile.build()

            EventResult.build(
                    fullyLoadedTimeInMillisecs: 500,
                    medianValue: true,
                    connectivityProfile: connectivityProfile,

                    cachedView: CachedView.CACHED,
                    jobGroup: jobGroup,
                    page: page,
                    measuredEvent: measuredEvent,
                    browser: browser,
                    location: location,
                    jobResultDate: runDate.toDate(),
                    domTimeInMillisecs: 2000
            )
            EventResult.build(
                    fullyLoadedTimeInMillisecs: 500,
                    medianValue: true,
                    connectivityProfile: connectivityProfile,

                    cachedView: CachedView.CACHED,
                    jobGroup: jobGroup,
                    page: page,
                    measuredEvent: measuredEvent,
                    browser: browser,
                    location: location,
                    jobResultDate: runDate.toDate(),
                    domTimeInMillisecs: 4000
            )
            EventResult.build(
                    fullyLoadedTimeInMillisecs: 500,
                    medianValue: true,
                    connectivityProfile: connectivityProfile,

                    cachedView: CachedView.CACHED,
                    jobGroup: jobGroup,
                    page: page,
                    measuredEvent: measuredEvent,
                    browser: browser,
                    location: location,
                    jobResultDate: runDate.toDate(),
                    domTimeInMillisecs: 6000
            )
            EventResult.build(
                    fullyLoadedTimeInMillisecs: 500,
                    medianValue: true,
                    connectivityProfile: connectivityProfile,

                    cachedView: CachedView.CACHED,
                    jobGroup: jobGroup,
                    page: page,
                    measuredEvent: measuredEvent,
                    browser: browser,
                    location: location,
                    jobResultDate: runDate.toDate(),
                    domTimeInMillisecs: 8000
            )

            session.flush()
        }

        when: "the data for the time series chart gets created and we trim the data below and above some given values"
        Date startTime = runDate.minusHours(1).toDate()
        Date endTime = runDate.plusHours(1).toDate()
        List<SelectedMeasurand> measurands = [new SelectedMeasurand(name: Measurand.DOM_TIME.toString(), cachedView: CachedView.CACHED, selectedType: SelectedMeasurandType.MEASURAND)]
        ErQueryParams queryParams = createEventResultQueryParams()
        queryParams.minLoadTimeInMillisecs = 3000
        queryParams.maxLoadTimeInMillisecs = 7000

        OsmRickshawChart chart = eventResultDashboardService.getEventResultDashboardHighchartGraphs(startTime, endTime, csiAggregationInterval, measurands, queryParams)

        then: "we only get the event results within the given range"
        chart.osmChartGraphs.size() == 1
        chart.osmChartGraphs[0].points.size() == expectedNumberOfPoints
        for (int i = 0; i < expectedNumberOfPoints; i++) {
            chart.osmChartGraphs[0].points[i].csiAggregation == expectedValue
        }

        where:
        csiAggregationInterval        | expectedNumberOfPoints | expectedValue
        CsiAggregationInterval.RAW    | 2                      | 4000
        CsiAggregationInterval.HOURLY | 1                      | 5000
    }

    void "get event result dashboard chart map"(List<CachedView> cached, int csiAggregationInterval, int expectedNumberOfGraphs, List expectedValues, SelectedMeasurandType selectedType) {
        given: "two event results with attribute uncached and cached respectively"
        EventResult.withNewSession { session ->
            OsmConfiguration.build()
            page = Page.build()
            jobGroup = JobGroup.build()
            browser = Browser.build()
            location = Location.build()
            measuredEvent = MeasuredEvent.build()
            connectivityProfile = ConnectivityProfile.build()

            EventResult.build(
                    fullyLoadedTimeInMillisecs: 500,
                    medianValue: true,
                    connectivityProfile: connectivityProfile,

                    cachedView: CachedView.CACHED,
                    jobGroup: jobGroup,
                    page: page,
                    measuredEvent: measuredEvent,
                    browser: browser,
                    location: location,
                    jobResultDate: runDate.toDate(),
                    domTimeInMillisecs: 2000
            )
            EventResult.build(
                    fullyLoadedTimeInMillisecs: 500,
                    medianValue: true,
                    connectivityProfile: connectivityProfile,

                    cachedView: CachedView.UNCACHED,
                    jobGroup: jobGroup,
                    page: page,
                    measuredEvent: measuredEvent,
                    browser: browser,
                    location: location,
                    jobResultDate: runDate.toDate(),
                    domTimeInMillisecs: 3000
            )

            session.flush()
        }


        when: "the data for the time series chart gets created"
        Date startTime = runDate.minusHours(1).toDate()
        Date endTime = runDate.plusHours(1).toDate()
        List<SelectedMeasurand> aggregatorTypes = []
        cached.each {
            aggregatorTypes.push(new SelectedMeasurand(name: Measurand.DOM_TIME.toString(), cachedView: it, selectedType: selectedType))
        }
        ErQueryParams queryParams = createEventResultQueryParams()

        OsmRickshawChart chart = eventResultDashboardService.getEventResultDashboardHighchartGraphs(startTime, endTime, csiAggregationInterval, aggregatorTypes, queryParams)

        then: "we get the correct number of graphs, points per graph and point values"
        chart.osmChartGraphs.size() == expectedNumberOfGraphs
        for (int i = 0; i < expectedNumberOfGraphs; i++) {
            chart.osmChartGraphs[i].points.size() == 1
            chart.osmChartGraphs[i].points[0].csiAggregation == expectedValues[i]
        }

        where:
        cached                                   | csiAggregationInterval        | expectedNumberOfGraphs | expectedValues | selectedType
        [CachedView.CACHED]                      | CsiAggregationInterval.RAW    | 1                      | [2000]         | SelectedMeasurandType.MEASURAND
        [CachedView.CACHED]                      | CsiAggregationInterval.HOURLY | 1                      | [2000]         | SelectedMeasurandType.MEASURAND
        [CachedView.UNCACHED]                    | CsiAggregationInterval.RAW    | 1                      | [3000]         | SelectedMeasurandType.MEASURAND
        [CachedView.UNCACHED]                    | CsiAggregationInterval.HOURLY | 1                      | [3000]         | SelectedMeasurandType.MEASURAND
        [CachedView.CACHED, CachedView.UNCACHED] | CsiAggregationInterval.RAW    | 2                      | [2000, 3000]   | SelectedMeasurandType.MEASURAND
        [CachedView.CACHED, CachedView.UNCACHED] | CsiAggregationInterval.HOURLY | 2                      | [2000, 3000]   | SelectedMeasurandType.MEASURAND
    }

    void "get event result dashboard chart map for usertimings"(List<CachedView> cached, int csiAggregationInterval, int expectedNumberOfGraphs, List expectedValues, UserTimingType userTimingType) {
        given: "two event results with attribute uncached and cached respectively"
        EventResult.withNewSession { session ->
            OsmConfiguration.build()
            page = Page.build()
            jobGroup = JobGroup.build()
            browser = Browser.build()
            location = Location.build()
            measuredEvent = MeasuredEvent.build()
            connectivityProfile = ConnectivityProfile.build()

            UserTiming userTiming1 = UserTiming.build(
                    type: userTimingType,
                    startTime: userTimingType == UserTimingType.MARK ? 2000 : 1,
                    duration: userTimingType == UserTimingType.MEASURE ? 2000 : null,
                    name: 'name'
            )

            EventResult.build(
                    fullyLoadedTimeInMillisecs: 500,
                    medianValue: true,
                    connectivityProfile: connectivityProfile,

                    cachedView: CachedView.CACHED,
                    jobGroup: jobGroup,
                    page: page,
                    measuredEvent: measuredEvent,
                    browser: browser,
                    location: location,
                    jobResultDate: runDate.toDate(),
                    domTimeInMillisecs: 1,
                    userTimings: [userTiming1]
            )

            UserTiming userTiming2 = UserTiming.build(
                    type: userTimingType,
                    startTime: userTimingType == UserTimingType.MARK ? 3000 : 1,
                    duration: userTimingType == UserTimingType.MEASURE ? 3000 : null,
                    name: 'name'
            )

            EventResult.build(
                    fullyLoadedTimeInMillisecs: 500,
                    medianValue: true,
                    connectivityProfile: connectivityProfile,

                    cachedView: CachedView.UNCACHED,
                    jobGroup: jobGroup,
                    page: page,
                    measuredEvent: measuredEvent,
                    browser: browser,
                    location: location,
                    jobResultDate: runDate.toDate(),
                    domTimeInMillisecs: 1,
                    userTimings: [userTiming2]
            )

            session.flush()
        }


        when: "the data for the time series chart gets created"
        Date startTime = runDate.minusHours(1).toDate()
        Date endTime = runDate.plusHours(1).toDate()
        List<SelectedMeasurand> aggregatorTypes = []
        cached.each {
            aggregatorTypes.push(new SelectedMeasurand(userTimingType.selectedMeasurandType.getOptionPrefix() + "name", it))
        }
        ErQueryParams queryParams = createEventResultQueryParams()

        OsmRickshawChart chart = eventResultDashboardService.getEventResultDashboardHighchartGraphs(startTime, endTime, csiAggregationInterval, aggregatorTypes, queryParams)

        then: "we get the correct number of graphs, points per graph and point values"
        chart.osmChartGraphs.size() == expectedNumberOfGraphs
        for (int i = 0; i < expectedNumberOfGraphs; i++) {
            chart.osmChartGraphs[i].points.size() == 1
            chart.osmChartGraphs[i].points[0].csiAggregation == expectedValues[i]
        }

        where:
        cached                                   | csiAggregationInterval        | expectedNumberOfGraphs | expectedValues | userTimingType
        [CachedView.CACHED]                      | CsiAggregationInterval.RAW    | 1                      | [2000]         | UserTimingType.MARK
        [CachedView.CACHED]                      | CsiAggregationInterval.HOURLY | 1                      | [2000]         | UserTimingType.MARK
        [CachedView.UNCACHED]                    | CsiAggregationInterval.RAW    | 1                      | [3000]         | UserTimingType.MARK
        [CachedView.UNCACHED]                    | CsiAggregationInterval.HOURLY | 1                      | [3000]         | UserTimingType.MARK
        [CachedView.CACHED, CachedView.UNCACHED] | CsiAggregationInterval.RAW    | 2                      | [2000, 3000]   | UserTimingType.MARK
        [CachedView.CACHED, CachedView.UNCACHED] | CsiAggregationInterval.HOURLY | 2                      | [2000, 3000]   | UserTimingType.MARK
        [CachedView.CACHED]                      | CsiAggregationInterval.RAW    | 1                      | [2000]         | UserTimingType.MEASURE
        [CachedView.CACHED]                      | CsiAggregationInterval.HOURLY | 1                      | [2000]         | UserTimingType.MEASURE
        [CachedView.UNCACHED]                    | CsiAggregationInterval.RAW    | 1                      | [3000]         | UserTimingType.MEASURE
        [CachedView.UNCACHED]                    | CsiAggregationInterval.HOURLY | 1                      | [3000]         | UserTimingType.MEASURE
        [CachedView.CACHED, CachedView.UNCACHED] | CsiAggregationInterval.RAW    | 2                      | [2000, 3000]   | UserTimingType.MEASURE
        [CachedView.CACHED, CachedView.UNCACHED] | CsiAggregationInterval.HOURLY | 2                      | [2000, 3000]   | UserTimingType.MEASURE
    }
}

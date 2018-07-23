package de.iteratec.osm.result

import de.iteratec.osm.OsmConfiguration
import de.iteratec.osm.csi.NonTransactionalIntegrationSpec
import de.iteratec.osm.csi.Page
import de.iteratec.osm.measurement.environment.Browser
import de.iteratec.osm.measurement.environment.Location
import de.iteratec.osm.measurement.schedule.ConnectivityProfile
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.report.chart.CsiAggregationInterval
import de.iteratec.osm.report.chart.OsmChartGraph
import de.iteratec.osm.report.chart.OsmRickshawChart
import de.iteratec.osm.util.I18nService
import grails.testing.mixin.integration.Integration
import grails.transaction.Rollback
import grails.web.mapping.LinkGenerator

@Integration(applicationClass = openspeedmonitor.Application.class)
@Rollback
class EventResultDashboardServiceLabelsIntegrationSpec extends NonTransactionalIntegrationSpec {

    EventResultDashboardService eventResultDashboardService

    JobGroup jobGroup1, jobGroup2, jobGroup3, jobGroup4
    Location location1, location2, location3
    MeasuredEvent measuredEvent1, measuredEvent2, measuredEvent3
    ConnectivityProfile connectivityProfile1, connectivityProfile2, connectivityProfile3
    Page page
    Browser browser

    def setup() {
        eventResultDashboardService.grailsLinkGenerator = Mock(LinkGenerator)
        eventResultDashboardService.i18nService = Stub(I18nService) {
            msg(_, _, _) >> { args ->
                return args[1]
            }
        }

        OsmConfiguration.build()

        jobGroup1 = JobGroup.build(name: "Job Group 1")
        jobGroup2 = JobGroup.build(name: "Job Group 2")
        jobGroup3 = JobGroup.build(name: "Job Group 3")
        jobGroup4 = JobGroup.build(name: "Job Group 4")

        location1 = Location.build(location: "Location 1")
        location2 = Location.build(location: "Location 2")
        location3 = Location.build(location: "Location 3")

        measuredEvent1 = MeasuredEvent.build(name: "Measured Event 1")
        measuredEvent2 = MeasuredEvent.build(name: "Measured Event 2")
        measuredEvent3 = MeasuredEvent.build(name: "Measured Event 3")

        connectivityProfile1 = ConnectivityProfile.build(name: "Connectivity Profile 1")
        connectivityProfile2 = ConnectivityProfile.build(name: "Connectivity Profile 2")
        connectivityProfile3 = ConnectivityProfile.build(name: "Connectivity Profile 3")

        page = Page.build()
        browser = Browser.build()

    }

    def cleanup() {
        eventResultDashboardService.i18nService = grailsApplication.mainContext.getBean('i18nService')
    }

    def "no summarization possible because every legend part in every event result is different"(int csiAggregationInterval) {
        given: "build two event results with different label attributes"
        EventResult.build(
                fullyLoadedTimeInMillisecs: 500,
                medianValue: true,

                jobGroup: jobGroup1,
                measuredEvent: measuredEvent1,
                location: location1,
                connectivityProfile: connectivityProfile1,
                cachedView: CachedView.UNCACHED,
                docCompleteTimeInMillisecs: 1,
                docCompleteRequests: 1
        )
        EventResult.build(
                fullyLoadedTimeInMillisecs: 500,
                medianValue: true,

                jobGroup: jobGroup2,
                measuredEvent: measuredEvent2,
                location: location2,
                connectivityProfile: connectivityProfile2,
                cachedView: CachedView.UNCACHED,
                docCompleteTimeInMillisecs: 1,
                docCompleteRequests: 1
        )

        ErQueryParams erQueryParams = new ErQueryParams()
        erQueryParams.jobGroupIds.addAll([jobGroup1.id, jobGroup2.id])
        erQueryParams.measuredEventIds.addAll([measuredEvent1.id, measuredEvent2.id])
        erQueryParams.locationIds.addAll([location1.id, location2.id])
        erQueryParams.connectivityProfileIds.addAll([connectivityProfile1.id, connectivityProfile2.id])
        SelectedMeasurand selectedMeasurand1 = new SelectedMeasurand(name: Measurand.DOC_COMPLETE_TIME.toString(), cachedView: CachedView.UNCACHED, selectedType: SelectedMeasurandType.MEASURAND)
        SelectedMeasurand selectedMeasurand2 = new SelectedMeasurand(name: Measurand.DOC_COMPLETE_REQUESTS.toString(), cachedView: CachedView.UNCACHED, selectedType: SelectedMeasurandType.MEASURAND)

        when: "the labels get analysed for common parts"
        OsmRickshawChart chart = eventResultDashboardService.getEventResultDashboardHighchartGraphs(
                null,
                null,
                csiAggregationInterval,
                [selectedMeasurand1, selectedMeasurand2],
                erQueryParams)
        List<OsmChartGraph> resultGraphs = chart.osmChartGraphs

        then: "they are build correctly and have no common parts"
        resultGraphs.size() == 4
        List<String> graphLabels = resultGraphs*.label
        graphLabels.containsAll([
                "Uncached " + selectedMeasurand1.name + " | Job Group 1 | Measured Event 1 | Location 1 | Connectivity Profile 1",
                "Uncached " + selectedMeasurand1.name + " | Job Group 2 | Measured Event 2 | Location 2 | Connectivity Profile 2",
                "Uncached " + selectedMeasurand2.name + " | Job Group 1 | Measured Event 1 | Location 1 | Connectivity Profile 1",
                "Uncached " + selectedMeasurand2.name + " | Job Group 2 | Measured Event 2 | Location 2 | Connectivity Profile 2"
        ])
        chart.osmChartGraphsCommonLabel == ""

        where: "all CSI aggregation intervals are tested"
        csiAggregationInterval        | _
        CsiAggregationInterval.RAW    | _
        CsiAggregationInterval.HOURLY | _
        CsiAggregationInterval.DAILY  | _
        CsiAggregationInterval.WEEKLY | _
    }

    def "no summarization necessary because all event results belong to same graph"(int csiAggregationInterval) {
        setup: "build ten event results which belong to the same graph and therefore have the same label attributes"

        10.times {
            EventResult.build(
                    fullyLoadedTimeInMillisecs: 500,
                    medianValue: true,

                    jobGroup: jobGroup1,
                    measuredEvent: measuredEvent1,
                    location: location1,
                    connectivityProfile: connectivityProfile1,
                    cachedView: CachedView.UNCACHED,
                    docCompleteTimeInMillisecs: 0,

                    page: page,
                    browser: browser
            )
        }

        ErQueryParams erQueryParams = new ErQueryParams()
        erQueryParams.jobGroupIds.add(jobGroup1.id)
        erQueryParams.measuredEventIds.add(measuredEvent1.id)
        erQueryParams.locationIds.add(location1.id)
        erQueryParams.connectivityProfileIds.add(connectivityProfile1.id)
        SelectedMeasurand selectedMeasurand1 = new SelectedMeasurand(name: Measurand.DOC_COMPLETE_TIME.toString(), cachedView: CachedView.UNCACHED, selectedType: SelectedMeasurandType.MEASURAND)


        when: "the labels get analysed for common parts"
        OsmRickshawChart chart = eventResultDashboardService.getEventResultDashboardHighchartGraphs(
                null,
                null,
                csiAggregationInterval,
                [selectedMeasurand1],
                erQueryParams)
        List<OsmChartGraph> resultGraphs = chart.osmChartGraphs

        then: "all label parts are the same and we get one label"
        resultGraphs.size() == 1
        resultGraphs[0].label == "Uncached " + Measurand.DOC_COMPLETE_TIME.toString() + " | Job Group 1 | Measured Event 1 | Location 1 | Connectivity Profile 1"
        chart.osmChartGraphsCommonLabel == ""

        where: "all CSI aggregation intervals are tested"
        csiAggregationInterval        | _
        CsiAggregationInterval.RAW    | _
        CsiAggregationInterval.HOURLY | _
        CsiAggregationInterval.DAILY  | _
        CsiAggregationInterval.WEEKLY | _
    }

    void "some legend parts in every event result the same, some different"(int csiAggregationInterval) {
        setup: "build two event results where same label attributes are the same and some differ"
        EventResult.build(
                fullyLoadedTimeInMillisecs: 500,
                medianValue: true,

                jobGroup: jobGroup1,
                measuredEvent: measuredEvent1,
                location: location1,
                connectivityProfile: connectivityProfile1,
                cachedView: CachedView.UNCACHED,
                docCompleteTimeInMillisecs: 0
        )
        EventResult.build(
                fullyLoadedTimeInMillisecs: 500,
                medianValue: true,

                jobGroup: jobGroup1,
                measuredEvent: measuredEvent1,
                location: location2,
                connectivityProfile: connectivityProfile2,
                cachedView: CachedView.UNCACHED,
                docCompleteTimeInMillisecs: 0
        )

        ErQueryParams erQueryParams = new ErQueryParams()
        erQueryParams.jobGroupIds.add(jobGroup1.id)
        erQueryParams.measuredEventIds.add(measuredEvent1.id)
        erQueryParams.locationIds.addAll([location1.id, location2.id])
        erQueryParams.connectivityProfileIds.addAll([connectivityProfile1.id, connectivityProfile2.id])
        SelectedMeasurand selectedMeasurand1 = new SelectedMeasurand(name: Measurand.DOC_COMPLETE_TIME.toString(), cachedView: CachedView.UNCACHED, selectedType: SelectedMeasurandType.MEASURAND)



        when: "the labels get analysed for common parts"
        OsmRickshawChart chart = eventResultDashboardService.getEventResultDashboardHighchartGraphs(
                null,
                null,
                csiAggregationInterval,
                [selectedMeasurand1],
                erQueryParams)
        List<OsmChartGraph> resultGraphs = chart.osmChartGraphs

        then: "we get two graph labels and a common part label"
        resultGraphs.size() == 2
        List<String> graphLables = resultGraphs*.label
        graphLables.containsAll([
                "Location 1 | Connectivity Profile 1",
                "Location 2 | Connectivity Profile 2",
        ])
        chart.osmChartGraphsCommonLabel == "<b>Measurand</b>: Uncached " + Measurand.DOC_COMPLETE_TIME.toString() + " | <b>Job Group</b>: Job Group 1 | <b>Measured step</b>: Measured Event 1"

        where: "all CSI aggregation intervals are tested"
        csiAggregationInterval        | _
        CsiAggregationInterval.RAW    | _
        CsiAggregationInterval.HOURLY | _
        CsiAggregationInterval.DAILY  | _
        CsiAggregationInterval.WEEKLY | _
    }

    void "single legend parts in some but not all event results the same"(int csiAggregationInterval) {
        setup: "build four event results with the described configuration"
        EventResult.build(
                fullyLoadedTimeInMillisecs: 500,
                medianValue: true,

                jobGroup: jobGroup1,
                measuredEvent: measuredEvent1,
                location: location1,
                connectivityProfile: connectivityProfile1,
                cachedView: CachedView.UNCACHED,
                docCompleteTimeInMillisecs: 0,
                docCompleteRequests: 0
        )
        EventResult.build(
                fullyLoadedTimeInMillisecs: 500,
                medianValue: true,

                jobGroup: jobGroup2,
                measuredEvent: measuredEvent1,
                location: location1,
                connectivityProfile: connectivityProfile1,
                cachedView: CachedView.UNCACHED,
                docCompleteTimeInMillisecs: 0,
                docCompleteRequests: 0
        )
        EventResult.build(
                fullyLoadedTimeInMillisecs: 500,
                medianValue: true,

                jobGroup: jobGroup3,
                measuredEvent: measuredEvent2,
                location: location2,
                connectivityProfile: connectivityProfile2,
                cachedView: CachedView.UNCACHED,
                docCompleteTimeInMillisecs: 0,
                docCompleteRequests: 0
        )
        EventResult.build(
                fullyLoadedTimeInMillisecs: 500,
                medianValue: true,

                jobGroup: jobGroup4,
                measuredEvent: measuredEvent3,
                location: location3,
                connectivityProfile: connectivityProfile3,
                cachedView: CachedView.UNCACHED,
                docCompleteTimeInMillisecs: 0,
                docCompleteRequests: 0
        )

        ErQueryParams erQueryParams = new ErQueryParams()
        erQueryParams.jobGroupIds.addAll([jobGroup1.id, jobGroup2.id, jobGroup3.id, jobGroup4.id])
        erQueryParams.measuredEventIds.addAll([measuredEvent1.id, measuredEvent2.id, measuredEvent3.id])
        erQueryParams.locationIds.addAll([location1.id, location2.id, location3.id])
        erQueryParams.connectivityProfileIds.addAll([connectivityProfile1.id, connectivityProfile2.id, connectivityProfile3.id])
        SelectedMeasurand selectedMeasurand1 = new SelectedMeasurand(name: Measurand.DOC_COMPLETE_TIME.toString(), cachedView: CachedView.UNCACHED, selectedType: SelectedMeasurandType.MEASURAND)
        SelectedMeasurand selectedMeasurand2 = new SelectedMeasurand(name: Measurand.DOC_COMPLETE_REQUESTS.toString(), cachedView: CachedView.UNCACHED, selectedType: SelectedMeasurandType.MEASURAND)

        when: "the labels get analysed for common parts"
        OsmRickshawChart chart = eventResultDashboardService.getEventResultDashboardHighchartGraphs(
                null,
                null,
                csiAggregationInterval,
                [selectedMeasurand1, selectedMeasurand2],
                erQueryParams)
        List<OsmChartGraph> resultGraphs = chart.osmChartGraphs

        then: "all label are build correctly and the common label is empty"
        resultGraphs.size() == 8
        List<String> graphLabels = resultGraphs*.label
        graphLabels.containsAll([
                "Uncached " + Measurand.DOC_COMPLETE_TIME.toString() + " | Job Group 1 | Measured Event 1 | Location 1 | Connectivity Profile 1",
                "Uncached " + Measurand.DOC_COMPLETE_TIME.toString() + " | Job Group 2 | Measured Event 1 | Location 1 | Connectivity Profile 1",
                "Uncached " + Measurand.DOC_COMPLETE_TIME.toString() + " | Job Group 3 | Measured Event 2 | Location 2 | Connectivity Profile 2",
                "Uncached " + Measurand.DOC_COMPLETE_TIME.toString() + " | Job Group 4 | Measured Event 3 | Location 3 | Connectivity Profile 3",
                "Uncached " + Measurand.DOC_COMPLETE_REQUESTS.toString() + " | Job Group 1 | Measured Event 1 | Location 1 | Connectivity Profile 1",
                "Uncached " + Measurand.DOC_COMPLETE_REQUESTS.toString() + " | Job Group 2 | Measured Event 1 | Location 1 | Connectivity Profile 1",
                "Uncached " + Measurand.DOC_COMPLETE_REQUESTS.toString() + " | Job Group 3 | Measured Event 2 | Location 2 | Connectivity Profile 2",
                "Uncached " + Measurand.DOC_COMPLETE_REQUESTS.toString() + " | Job Group 4 | Measured Event 3 | Location 3 | Connectivity Profile 3"
        ])
        chart.osmChartGraphsCommonLabel == ''

        where: "all CSI aggregation intervals are tested"
        csiAggregationInterval        | _
        CsiAggregationInterval.RAW    | _
        CsiAggregationInterval.HOURLY | _
        CsiAggregationInterval.DAILY  | _
        CsiAggregationInterval.WEEKLY | _
    }
}

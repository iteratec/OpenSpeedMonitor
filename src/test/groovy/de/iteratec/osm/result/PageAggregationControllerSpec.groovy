package de.iteratec.osm.result

import de.iteratec.osm.barchart.BarchartAggregation
import de.iteratec.osm.csi.CsiConfiguration
import de.iteratec.osm.csi.Page
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.result.dto.PageAggregationChartSeriesDTO
import de.iteratec.osm.util.I18nService
import grails.buildtestdata.BuildDomainTest
import grails.buildtestdata.mixin.Build
import grails.testing.web.controllers.ControllerUnitTest
import spock.lang.Specification

@Build([Page, JobGroup, CsiConfiguration])
class PageAggregationControllerSpec extends Specification implements BuildDomainTest<Page>,
        ControllerUnitTest<PageAggregationController> {

    private PageAggregationController controllerUnderTest

    private Page page1
    private Page page2
    private Page page3

    def setup() {
        controllerUnderTest = controller

        page1 = Page.build(name: "page one")
        page2 = Page.build(name: "page two")
        page3 = Page.build(name: "page three")
    }

    def "test merge two lists that are equal"() {
        given: "two lists that are equal"
        List<Page> list1 = [page1, page2]
        List<Page> list2 = [page1, page2]

        when: "merging lists"
        List<Page> result = controllerUnderTest.getOrderedPagesOfAllScripts([list1, list2])

        then: "result should be equal too"
        result == list1
    }

    def "test merge two different lists with same length"() {
        given: "two lists that are not equal"
        List<Page> list1 = [page1, page2]
        List<Page> list2 = [page1, page3]
        List<Page> expectedResult = [page1, page2, page3]

        when: "merging lists"
        List<Page> result = controllerUnderTest.getOrderedPagesOfAllScripts([list1, list2])

        then: "result should be expectedResult"
        result == expectedResult
    }

    def "test merge two lists with different lengths"() {
        given: "two lists that are not equal"
        List<Page> list1 = [page1, page2]
        List<Page> list2 = [page1, page2, page3]
        List<Page> expectedResult = [page1, page2, page3]

        when: "merging lists"
        List<Page> result = controllerUnderTest.getOrderedPagesOfAllScripts([list1, list2])

        then: "result should be expectedResult"
        result == expectedResult
    }

    def "test merge three different lists"() {
        given: "three lists that are not equal and have different lengths"
        List<Page> list1 = [page1, page2, page3]
        List<Page> list2 = [page1, page3, page3]
        List<Page> list3 = [page1, page2, page3, page2]
        List<Page> expectedResult = [page1, page2, page3, page3, page2]

        when: "merging lists"
        List<Page> result = controllerUnderTest.getOrderedPagesOfAllScripts([list1, list2, list3])

        then: "result should be expectedResult"
        result == expectedResult
    }

    def "convert multiple barchart aggregations to DTO"() {
        given: "two barchart aggregations"
        JobGroup jobGroup1 = JobGroup.build(name: "job group 1", csiConfiguration: CsiConfiguration.build())
        JobGroup jobGroup2 = JobGroup.build(name: "job group 2", id: 2)
        List<BarchartAggregation> barchartAggregations = [
                new BarchartAggregation(
                        value: 1.2,
                        valueComparative: 2.4,
                        selectedMeasurand: new SelectedMeasurand("DOC_COMPLETE_TIME", CachedView.CACHED),
                        page: page1,
                        jobGroup: jobGroup1,
                        aggregationValue: "foo"
                ),
                new BarchartAggregation(
                        value: 3.2,
                        valueComparative: null,
                        selectedMeasurand: new SelectedMeasurand("SPEED_INDEX", CachedView.UNCACHED),
                        page: page2,
                        jobGroup: jobGroup2,
                        aggregationValue: "bar"
                )
        ]
        controllerUnderTest.i18nService = Stub(I18nService){
            msg(_ as String, _ as String) >> {String key, String defaultValue -> key+";"+defaultValue}
        }

        when: "being converted to DTO"
        def seriesDTOs = controllerUnderTest.convertToPageAggregationChartSeriesDTOs(barchartAggregations)

        then: "the resulting list contains DTOs for both aggregations"
        seriesDTOs == [
                new PageAggregationChartSeriesDTO(
                        measurand: "DOC_COMPLETE_TIME",
                        measurandLabel: "de.iteratec.isr.measurand.DOC_COMPLETE_TIME;DOC_COMPLETE_TIME",
                        measurandGroup: "LOAD_TIMES",
                        value: 1.2,
                        valueComparative: 2.4,
                        page: "page one",
                        jobGroup: "job group 1",
                        unit: "ms",
                        aggregationValue: "foo"),
                new PageAggregationChartSeriesDTO(
                        measurand: "SPEED_INDEX",
                        measurandLabel: "de.iteratec.isr.measurand.SPEED_INDEX;SPEED_INDEX",
                        measurandGroup: "LOAD_TIMES",
                        value: 3.2,
                        valueComparative: null,
                        page: "page two",
                        jobGroup: "job group 2",
                        unit: "ms",
                        aggregationValue: "bar")
        ]
    }
}

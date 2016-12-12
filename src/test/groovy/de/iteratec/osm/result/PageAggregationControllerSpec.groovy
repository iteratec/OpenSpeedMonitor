package de.iteratec.osm.result

import de.iteratec.osm.csi.Page
import de.iteratec.osm.csi.TestDataUtil
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import spock.lang.Specification

@TestFor(PageAggregationController)
@Mock([Page])
class PageAggregationControllerSpec extends Specification {

    private PageAggregationController controllerUnderTest

    private Page page1
    private Page page2
    private Page page3

    def setup() {
        controllerUnderTest = controller

        page1 = TestDataUtil.createPage("page one")
        page2 = TestDataUtil.createPage("page two")
        page3 = TestDataUtil.createPage("page three")
    }

    def "test merge two lists that are equal"() {
        given: "two lists that are equal"
        List<Page> list1 = [page1, page2]
        List<Page> list2 = [page1, page2]
        assert list1.equals(list2)

        when: "merging lists"
        List<Page> result = controllerUnderTest.mergeLists([list1, list2])

        then: "result should be equal too"
        result == list1
    }

    def "test merge two different lists"() {
        given: "two lists that are not equal"
        List<Page> list1 = [page1, page2]
        List<Page> list2 = [page1, page3]
        List<Page> expectedResult = [page1, page2, page3]

        when: "merging lists"
        List<Page> result = controllerUnderTest.mergeLists([list1, list2])

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
        List<Page> result = controllerUnderTest.mergeLists([list1, list2, list3])

        then: "result should be expectedResult"
        result == expectedResult
    }
}

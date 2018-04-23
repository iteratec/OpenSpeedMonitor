package de.iteratec.osm.measurement.script

import de.iteratec.osm.InMemoryConfigService
import de.iteratec.osm.csi.Page
import de.iteratec.osm.result.MeasuredEvent
import de.iteratec.osm.result.PageService
import grails.buildtestdata.BuildDataTest
import grails.buildtestdata.mixin.Build
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import spock.lang.*

/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
@TestFor(ScriptService)
@TestMixin(GrailsUnitTestMixin)
@Mock([Page, MeasuredEvent])
@Build([Page])
class ScriptServiceSpec extends Specification implements BuildDataTest {

    def doWithSpring = {
        pageService(PageService)
    }

    void "don't create new pages if they already exist"() {

        setup: "there are two pages and script parser declares these two existing pages"
        Page.build(name : "page1");
        Page.build(name : "page2");

        Set<String> newPages = ["page1", "page2"] as Set
        Map<String,String> newMeasuredEvents = [:]
        ScriptParser parser = Stub(ScriptParser){
            getNewPages() >> newPages
            getNewMeasuredEvents() >> newMeasuredEvents
        }

        when: "createNewPagesAndMeasuredEvents is called"
        service.createNewPagesAndMeasuredEvents(parser)

        then: "no new pages should have been created"
        Page.list().size() == 2
        Page.findAllByName("page1").size() == 1
        Page.findAllByName("page2").size() == 1

    }

    void "create new page if script contains non existent page"() {

        setup: "script parser declares two new pages"
        Set<String> newPages = ["page1", "page2"] as Set
        Map<String,String> newMeasuredEvents = [:]
        ScriptParser parser = Stub(ScriptParser){
            getNewPages() >> newPages
            getNewMeasuredEvents() >> newMeasuredEvents
        }

        when: "createNewPagesAndMeasuredEvents is called"
        service.createNewPagesAndMeasuredEvents(parser)

        then: "two new pages should have been created"
        Page.list().size() == 2
        Page.findAllByName("page1").size() == 1
        Page.findAllByName("page2").size() == 1

    }

    void "create new measured event if script contains non existent measured event"() {

        setup: "there are two existing pages and script parser declares two new measured events"
        Page.build(name : "page1");
        Page.build(name : "page2");

        Map<String,String> newMeasuredEvents = ["measuredEvent1": "page1", "measuredEvent2": "page2"] as Map
        ScriptParser parser = Stub(ScriptParser){
            getNewMeasuredEvents() >> newMeasuredEvents
        }

        when: "createNewPagesAndMeasuredEvents is called"
        service.createNewPagesAndMeasuredEvents(parser)

        then: "two new pages and measured events should have been created"
        MeasuredEvent.list().size() == 2
        MeasuredEvent.findAllByName("measuredEvent1").size() == 1
        MeasuredEvent.findAllByName("measuredEvent2").size() == 1

    }

    void "create new page and linked measured event if script contains non existent combination of both"() {

        setup: "script parser declares two new measured events"
        Set<String> newPages = ["page1", "page2"] as Set
        Map<String,String> newMeasuredEvents = ["measuredEvent1": "page1", "measuredEvent2": "page2"] as Map
        ScriptParser parser = Stub(ScriptParser){
            getNewPages() >> newPages
            getNewMeasuredEvents() >> newMeasuredEvents
        }

        when: "createNewPagesAndMeasuredEvents is called"
        service.createNewPagesAndMeasuredEvents(parser)

        then: "two new measured events should have been created"
        Page.list().size() == 2
        Page.findAllByName("page1").size() == 1
        Page.findAllByName("page2").size() == 1
        MeasuredEvent.list().size() == 2
        MeasuredEvent.findAllByName("measuredEvent1").size() == 1
        MeasuredEvent.findAllByName("measuredEvent2").size() == 1

    }
}
package de.iteratec.osm.measurement.script

import de.iteratec.osm.csi.Page
import de.iteratec.osm.result.MeasuredEvent
import de.iteratec.osm.result.PageService
import grails.transaction.Transactional

@Transactional
class ScriptService {
    PageService pageService

    def createNewPagesAndMeasuredEvents(Script s) {
        ScriptParser parser = new ScriptParser(pageService, s.navigationScript)
        parser.newPages.each { String name ->
            Page.findOrSaveByName(name)
        }
        parser.newMeasuredEvents.each { String measuredEventName, String pageName ->
            def page = Page.findByName(pageName)
            if (page) {
                MeasuredEvent.findOrSaveByNameAndTestedPage(measuredEventName, page)
            }
        }
    }
}

package de.iteratec.osm.measurement.script

import de.iteratec.osm.csi.Page
import de.iteratec.osm.result.MeasuredEvent
import de.iteratec.osm.result.PageService
import grails.transaction.Transactional

@Transactional
class ScriptService {
    PageService pageService

    def createNewPagesAndMeasuredEvents(ScriptParser parser) {
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

    def getMeasuredEventsForScript(Long scriptId){
        Script script = Script.get(scriptId)
        ScriptParser parser = new ScriptParser(pageService, script.navigationScript)

        return parser.getAllMeasuredEvents(script.navigationScript).collect()
    }
}

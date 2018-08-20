package de.iteratec.osm.measurement.script

import de.iteratec.osm.csi.Page
import de.iteratec.osm.result.MeasuredEvent
import de.iteratec.osm.result.PageService
import grails.gorm.transactions.Transactional

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

    def getMeasuredEventsForScript(Long scriptId) {
        Script script = Script.get(scriptId)
        return getMeasuredEventsForScript(script.navigationScript, script.label)
    }

    List<MeasuredEvent> getMeasuredEventsForScript(String navigationScript, String navigationScriptName) {
        ScriptParser parser = new ScriptParser(pageService, navigationScript, navigationScriptName)

        return parser.getAllMeasuredEvents(navigationScript).collect()
    }
}

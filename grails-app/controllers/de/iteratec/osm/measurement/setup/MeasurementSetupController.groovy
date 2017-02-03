package de.iteratec.osm.measurement.setup

import de.iteratec.osm.csi.Page
import de.iteratec.osm.measurement.script.Script
import de.iteratec.osm.result.MeasuredEvent
import grails.converters.JSON

class MeasurementSetupController {

    def index() {
        redirect(action: 'create')
    }

    def create() {
        [script: new Script(params), pages: Page.list(), measuredEvents: MeasuredEvent.list() as JSON, archivedScripts:""]
    }

    def save() {
        Script s = new Script(params)
        if (!s.save(flush: true)) {
            render(view: 'create', model: [script: s,pages: Page.list(), measuredEvents: MeasuredEvent.list() as JSON, archivedScripts: ""])
            return
        }
        createNewPagesAndMeasuredEvents(s)
        def flashMessageArgs = [getScriptI18n(), s.label]
        flash.message = message(code: 'default.created.message', args: flashMessageArgs)
        redirect(action: "list", id: s.id)
    }
}

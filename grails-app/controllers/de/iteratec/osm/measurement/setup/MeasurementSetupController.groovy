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
        redirect(action: "/")
    }
}

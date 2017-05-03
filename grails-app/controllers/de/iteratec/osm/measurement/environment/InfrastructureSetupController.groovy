package de.iteratec.osm.measurement.environment

import de.iteratec.osm.ConfigService
import de.iteratec.osm.OsmConfiguration

class InfrastructureSetupController {

    ConfigService configService
    WptServerService wptServerService

    def index() {
        forward(action: 'create')
    }

    def create() {
        return params
    }

    def cancel() {
        List<OsmConfiguration> osmConfigs = OsmConfiguration.list()
        osmConfigs[0].infrastructureSetupRan = OsmConfiguration.InfrastructureSetupRan.ABORTED
        redirect(controller: 'Landing', action: 'index')
    }

    def save() {
        List<Location> addedLocations = wptServerService.tryMakeServerAndGetLocations(params.serverSelect, params.inputWPTKey, params.inputServerName, params.inputServerAddress)
        if (addedLocations.size() > 0) {
            OsmConfiguration config = configService.getConfig()
            config.infrastructureSetupRan = OsmConfiguration.InfrastructureSetupRan.TRUE
            config.save(failOnError: true)
            redirect(controller: 'Landing', action: 'index')
            flash.success = addedLocations.size()
        }
        else {
            flash.error = "An error occured"
            forward(actionName: 'create')
        }
    }
}

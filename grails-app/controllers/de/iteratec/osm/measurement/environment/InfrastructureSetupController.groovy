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
        return [params:params,disableNavbar:true,disableBackToTop:true]
    }

    def cancel() {
        configService.setInfrastructureSetupRan(OsmConfiguration.InfrastructureSetupStatus.ABORTED)
        redirect(controller: 'Landing', action: 'index')
    }

    def save() {
        if (configService.getInfrastructureSetupRan() != OsmConfiguration.InfrastructureSetupStatus.FINISHED) {
            List<Location> addedLocations = wptServerService.tryMakeServerAndGetLocations(params.serverSelect, params.inputWptKey, params.inputServerName, params.inputServerAddress, params.inputServerKey)
            if (addedLocations.size() > 0) {
                configService.setInfrastructureSetupRan(OsmConfiguration.InfrastructureSetupStatus.FINISHED)
                flash.success = addedLocations.size()
                redirect(controller: 'Landing', action: 'index')
            } else {
                flash.error = "An error occured"
                forward(actionName: 'create')
            }
        }
    }
}

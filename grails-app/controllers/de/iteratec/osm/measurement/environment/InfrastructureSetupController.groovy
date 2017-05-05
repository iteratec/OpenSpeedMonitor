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
        return [params:params,disableNavbar:true]
    }

    def cancel() {
        configService.setInfrastructureSetupRan(OsmConfiguration.InfrastructureSetupStatus.Aborted)
        redirect(controller: 'Landing', action: 'index')
    }

    def save() {
        //if (configService.getInfrastructureSetupRan() != OsmConfiguration.InfrastructureSetupStatus.Finished) {
            List<Location> addedLocations = wptServerService.tryMakeServerAndGetLocations(params.serverSelect, params.inputWptKey, params.inputServerName, params.inputServerAddress, params.inputServerKey)
            if (addedLocations.size() > 0) {
                configService.setInfrastructureSetupRan(OsmConfiguration.InfrastructureSetupStatus.Finished)
                flash.success = addedLocations.size()
                redirect(controller: 'Landing', action: 'index')
            } else {
                flash.error = "An error occured"
                forward(actionName: 'create')
            }
        //}
    }
}

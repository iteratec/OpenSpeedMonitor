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
        params['serverCreationOptions'] = [wptServerService.OFFICIAL_WPT_URL, "custom"]
        params['disableNavbar'] = true
        params['disableBackToTop'] = true
        return params
    }

    def cancel() {
        configService.setInfrastructureSetupRan(OsmConfiguration.InfrastructureSetupStatus.ABORTED)
        redirect(controller: 'Landing', action: 'index')
    }

    def save(String serverSelect, String serverName, String serverUrl, String serverApiKey) {
        if (configService.getInfrastructureSetupRan() != OsmConfiguration.InfrastructureSetupStatus.FINISHED) {
            List<Location> addedLocations = wptServerService.tryMakeServerAndGetLocations(
                serverSelect,
                serverName,
                serverUrl,
                serverApiKey
            )
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

package de.iteratec.osm

class LandingController {

    ConfigService configService
    OsmStateService osmStateService

    def index() {
        if (configService.infrastructureSetupRan != OsmConfiguration.InfrastructureSetupRan.TRUE) {
            if (osmStateService.untouched()) {
                if (configService.infrastructureSetupRan == OsmConfiguration.InfrastructureSetupRan.FALSE) {
                    forward(controller: 'InfrastructureSetup', action: 'index')
                }
                if (configService.infrastructureSetupRan == OsmConfiguration.InfrastructureSetupRan.ABORTED) {
                    if (!flash.continue) {
                        flash.continue = "Continue Setup"
                        forward(action: 'index')
                    }
                }
            }
            else {
                OsmConfiguration config = configService.getConfig()
                config.infrastructureSetupRan = OsmConfiguration.InfrastructureSetupRan.TRUE
                config.save(failOnError: true)
                forward(action: 'index')
            }
        }
    }
}

package de.iteratec.osm

class LandingController {

    ConfigService configService
    OsmStateService osmStateService

    def index() {
        def enableButton = true;
        if (configService.infrastructureSetupRan != OsmConfiguration.InfrastructureSetupRan.TRUE) {
            if (osmStateService.untouched()) {
                if (configService.infrastructureSetupRan == OsmConfiguration.InfrastructureSetupRan.FALSE) {
                    forward(controller: 'InfrastructureSetup', action: 'index')
                }
                if (configService.infrastructureSetupRan == OsmConfiguration.InfrastructureSetupRan.ABORTED) {
                    if (!flash.continue) {
                        enableButton = false;
                        flash.continue = "Continue Setup"
                        render(view: 'index')
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
        if (enableButton) {
            flash.button = "enabled"
        }
        render(view: 'index')
    }
}

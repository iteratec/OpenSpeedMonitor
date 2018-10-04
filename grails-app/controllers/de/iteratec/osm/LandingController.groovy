package de.iteratec.osm

class LandingController {

    ConfigService configService
    OsmStateService osmStateService

    def index() {
        if (shouldRedirectToSetup()) {
            forward(controller: 'InfrastructureSetup', action: 'index')
        } else {
            render(view: "/angularFrontend")
        }
    }

    private boolean shouldRedirectToSetup() {
        if (configService.infrastructureSetupRan == OsmConfiguration.InfrastructureSetupStatus.FINISHED) {
            return false
        }
        if (!osmStateService.untouched()) {
            configService.setInfrastructureSetupRan(OsmConfiguration.InfrastructureSetupStatus.FINISHED)
        }
        return configService.infrastructureSetupRan == OsmConfiguration.InfrastructureSetupStatus.NOT_STARTED
    }
}

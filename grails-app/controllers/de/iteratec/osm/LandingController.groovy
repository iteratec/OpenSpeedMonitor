package de.iteratec.osm

class LandingController {

    ConfigService configService
    OsmStateService osmStateService

    def index() {
        if (isSetupFinished()) {
            render(view: "/angularFrontend")
        } else if (configService.infrastructureSetupRan == OsmConfiguration.InfrastructureSetupStatus.ABORTED) {
            redirect(action: "continueSetup")
        } else {
            redirect(controller: 'InfrastructureSetup', action: 'index')
        }
    }

    def continueSetup() {
        // used for angular routing
        render(view: "/angularFrontend")
    }

    private boolean isSetupFinished() {
        if (configService.infrastructureSetupRan == OsmConfiguration.InfrastructureSetupStatus.FINISHED) {
            return true
        }
        if (!osmStateService.untouched()) {
            configService.setInfrastructureSetupRan(OsmConfiguration.InfrastructureSetupStatus.FINISHED)
            return true
        }
        return false
    }
}

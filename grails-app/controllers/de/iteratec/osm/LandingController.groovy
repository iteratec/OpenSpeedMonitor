package de.iteratec.osm

class LandingController {

    ConfigService configService
    OsmStateService osmStateService

    def index() {
        if (configService.infrastructureSetupRan != OsmConfiguration.InfrastructureSetupStatus.FINISHED) {
            if (osmStateService.untouched()) {
                if (configService.infrastructureSetupRan == OsmConfiguration.InfrastructureSetupStatus.NOT_STARTED) {
                    forward(controller: 'InfrastructureSetup', action: 'index')
                }
                if (configService.infrastructureSetupRan == OsmConfiguration.InfrastructureSetupStatus.ABORTED) {
                    return [isSetupFinished:false]
                }
            }
            else {
                OsmConfiguration config = configService.getConfig()
                config.infrastructureSetupRan = OsmConfiguration.InfrastructureSetupStatus.FINISHED
                config.save(failOnError: true)
                forward(action: 'index')
            }
        }
        return [isSetupFinished:true]
    }
}

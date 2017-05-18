package de.iteratec.osm

class LandingController {

    ConfigService configService
    OsmStateService osmStateService

    def index() {
        if (configService.infrastructureSetupRan != OsmConfiguration.InfrastructureSetupStatus.Finished) {
            if (osmStateService.untouched()) {
                if (configService.infrastructureSetupRan == OsmConfiguration.InfrastructureSetupStatus.NotStarted) {
                    forward(controller: 'InfrastructureSetup', action: 'index')
                }
                if (configService.infrastructureSetupRan == OsmConfiguration.InfrastructureSetupStatus.Aborted) {
                    return [isSetupFinished:false]
                }
            }
            else {
                OsmConfiguration config = configService.getConfig()
                config.infrastructureSetupRan = OsmConfiguration.InfrastructureSetupStatus.Finished
                config.save(failOnError: true)
                forward(action: 'index')
            }
        }
        return [isSetupFinished:true]
    }
}

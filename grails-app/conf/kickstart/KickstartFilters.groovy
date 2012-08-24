package kickstart

//import groovy.time.TimeCategory

class KickstartFilters {
	Date start
	Date stop

    def filters = {
        all() {
            before = {
				// Small "logging" filter for controller & actions
                println !params.controller ? '/: ' + params : params.controller +"."+(params.action ?: "index")+": "+params
				// Better logging: needs to be switched on in Config.groovy and is a little bit verbose
				//	log.info(!params.controller ? '/: ' + params : params.controller +"."+(params.action ?: "index")+": "+params)
				// Small "logging" filter for duration of actions: take start time
				// start = new Date()
			}
			after = {
				// Small "logging" filter for duration of actions: calculate duration
				//	stop = new Date()
				//	println "... Total elapsed time: " + TimeCategory.minus( stop, start )
            }
            afterView = {
                
            }
        }
    }
}

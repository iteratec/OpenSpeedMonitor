package de.iteratec.osm.de.iteratec.osm.api

import de.iteratec.osm.api.ApiKey
import grails.validation.Validateable

import static de.iteratec.osm.util.Constants.DEFAULT_ACCESS_DENIED_MESSAGE

/**
 * Parameters of rest api functions /rest/config/activateNightlyDatabaseCleanup and
 * /rest/config/deactivateNightlyCleanup.
 */
class NightlyDatabaseCleanupActivationCommand implements Validateable{
    String apiKey

    static constraints = {
        apiKey(validator: { String currentKey, NightlyDatabaseCleanupActivationCommand cmd ->
            ApiKey validApiKey = ApiKey.findBySecretKey(currentKey)
            if (!validApiKey.allowedForNightlyDatabaseCleanupActivation) return [DEFAULT_ACCESS_DENIED_MESSAGE]
            else return true
        })
    }
}

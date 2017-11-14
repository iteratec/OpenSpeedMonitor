package de.iteratec.osm.de.iteratec.osm.api

import de.iteratec.osm.api.ApiKey
import grails.validation.Validateable

import static de.iteratec.osm.util.Constants.DEFAULT_ACCESS_DENIED_MESSAGE

/**
 * Parameters of rest api functions /rest/config/activateMeasurementsGenerally and
 * /rest/config/deactivateMeasurementsGenerally.
 * Created by nkuhn on 08.05.15.
 */
class MeasurementActivationCommand implements Validateable{
    String apiKey

    static constraints = {
        apiKey(validator: { String currentKey, MeasurementActivationCommand cmd ->
            ApiKey validApiKey = ApiKey.findBySecretKey(currentKey)
            if (!validApiKey.allowedForMeasurementActivation) return [DEFAULT_ACCESS_DENIED_MESSAGE]
            else return true
        })
    }
}

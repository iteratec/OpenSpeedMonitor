package de.iteratec.osm.api.dto

import de.iteratec.osm.csi.CsiConfiguration

/**
 * Created by birger on 04/03/16.
 */
class JsonCsiConfiguration {

    long id

    String name

    public static JsonCsiConfiguration create(CsiConfiguration csiConfiguration) {
        JsonCsiConfiguration result = new JsonCsiConfiguration()

        result.id = csiConfiguration.id
        result.name = csiConfiguration.label

        return result
    }
}

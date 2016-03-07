package de.iteratec.osm.api.dto

import de.iteratec.osm.csi.CsiConfiguration

class JsonCsiConfiguration{

    long id

    String label

    public static JsonCsiConfiguration create(CsiConfiguration csiConfiguration) {
        JsonCsiConfiguration result = new JsonCsiConfiguration()

        result.id = csiConfiguration.id
        result.label = csiConfiguration.label

        return result
    }
}

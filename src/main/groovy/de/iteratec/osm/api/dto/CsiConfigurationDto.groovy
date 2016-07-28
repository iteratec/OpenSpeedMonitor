package de.iteratec.osm.api.dto

import de.iteratec.osm.csi.CsiConfiguration

class CsiConfigurationDto {

    long id

    String label

    public static CsiConfigurationDto create(CsiConfiguration csiConfiguration) {
        CsiConfigurationDto result = new CsiConfigurationDto()

        result.id = csiConfiguration.id
        result.label = csiConfiguration.label

        return result
    }
}

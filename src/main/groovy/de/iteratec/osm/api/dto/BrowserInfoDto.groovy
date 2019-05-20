package de.iteratec.osm.api.dto

import de.iteratec.osm.result.DeviceType
import de.iteratec.osm.result.OperatingSystem

/**
 * @author nkuhn
 */
class BrowserInfoDto {
    Long browserId
    OperatingSystem operatingSystem
    DeviceType deviceType
}

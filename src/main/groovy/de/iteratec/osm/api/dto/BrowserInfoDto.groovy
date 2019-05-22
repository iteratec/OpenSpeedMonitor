package de.iteratec.osm.api.dto


import de.iteratec.osm.result.OperatingSystem
/**
 * Contains information of to the browser associated {@link de.iteratec.osm.measurement.environment.Location}s.
 * But only if the information is the same for all associated {@link de.iteratec.osm.measurement.environment.Location}s.
 * That should be the case for most browsers with WebPagetest but does not have to be forced.
 * If a browser would be associated to multiple {@link de.iteratec.osm.measurement.environment.Location}s with different
 * informations (e.g. a Chrome {@link de.iteratec.osm.measurement.environment.Browser} domain object to a WINDOWS/DESKTOP
 * location AND an ANDROID/TABLET location no BrowserInfoDto should be provided.
 * @author nkuhn* @see {@link de.iteratec.osm.measurement.environment.BrowserService}
 */
class BrowserInfoDto {
    Long browserId
    OperatingSystem operatingSystem
    DeviceTypeDto deviceType
}

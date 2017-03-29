package de.iteratec.osm.measurement.environment

import groovy.util.slurpersupport.GPathResult

/**
 * Small wrapper for {@link Location}s with further attributes in health check functionality
 * @author nkuhn
 */
class LocationWithXmlNode {
    Location location
    GPathResult locationXmlNode
}

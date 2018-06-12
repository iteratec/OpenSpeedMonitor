package de.iteratec.osm.result.dao

import groovy.transform.EqualsAndHashCode

/**
 * @author nkuhn
 */
@EqualsAndHashCode(excludes = ['alias'])
class ProjectionProperty {
    String dbName
    String alias
}

package de.iteratec.osm.result.dao.query

import groovy.transform.EqualsAndHashCode

/**
 * @author nkuhn
 */
@EqualsAndHashCode(excludes = ['alias'])
class ProjectionProperty {
    String dbName
    String alias
}

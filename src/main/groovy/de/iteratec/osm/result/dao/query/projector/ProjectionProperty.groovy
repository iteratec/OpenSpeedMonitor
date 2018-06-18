package de.iteratec.osm.result.dao.query.projector

import groovy.transform.EqualsAndHashCode

/**
 * @author nkuhn
 */
@EqualsAndHashCode(excludes = ['alias'])
class ProjectionProperty {
    String dbName
    String alias
}

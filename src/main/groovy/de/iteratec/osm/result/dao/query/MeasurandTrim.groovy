package de.iteratec.osm.result.dao.query

import de.iteratec.osm.result.Measurand
import de.iteratec.osm.result.MeasurandGroup

/**
 * @author nkuhn
 */
class MeasurandTrim {
    MeasurandGroup measurandGroup
    Measurand onlyForSpecific
    def value
    TrimQualifier qualifier
}

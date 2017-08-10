package de.iteratec.osm.barchart

import groovy.transform.EqualsAndHashCode

@EqualsAndHashCode(excludes = ["value", "valueComparative"])
class BarchartSeries {
    String measurand = ""
    String measurandLabel = ""
    String measurandGroup = ""
    Double value = null
    Double valueComparative = null
    String page = ""
    String jobGroup = ""
    String unit = ""
}

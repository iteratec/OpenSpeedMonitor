package de.iteratec.osm.barchart

import groovy.transform.EqualsAndHashCode

@EqualsAndHashCode(excludes = ["value", "valueComparative"])
class BarchartSeries {
    List<BarchartDatum> data = []
    String dimensionalUnit = ""
    String yAxisLabel = ""
    Boolean stacked = true
}

package de.iteratec.osm.barchart

import groovy.transform.EqualsAndHashCode

class BarchartSeries {
    List<BarchartDatum> data = []
    String dimensionalUnit = ""
    String yAxisLabel = ""
    Boolean stacked = true
}

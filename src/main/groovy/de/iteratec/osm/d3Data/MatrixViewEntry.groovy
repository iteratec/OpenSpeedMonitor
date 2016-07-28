package de.iteratec.osm.d3Data

/**
 * This class represents a model a single entry in a matrix
 */
class MatrixViewEntry {
    String columnName
    String rowName
    Double weight

    MatrixViewEntry() {
        columnName = "horizontal"
        rowName = "vertical"
        weight = 1.0
    }
}

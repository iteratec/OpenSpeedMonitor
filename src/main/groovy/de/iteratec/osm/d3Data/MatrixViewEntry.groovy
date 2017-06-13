package de.iteratec.osm.d3Data

/**
 * This class represents a model a single entry in a matrix
 */
class MatrixViewEntry {

    public static final String DEFAULT_COLUMN_NAME = "horizontal"
    public static final String DEFAULT_ROW_NAME = "vertical"
    public static final BigDecimal DEFAULT_CELL_WEIGHT = 1.0

    String columnName
    String rowName
    Double weight

    MatrixViewEntry() {
        columnName = DEFAULT_COLUMN_NAME
        rowName = DEFAULT_ROW_NAME
        weight = DEFAULT_CELL_WEIGHT
    }
}

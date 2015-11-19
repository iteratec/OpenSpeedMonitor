package de.iteratec.osm.d3Data

/**
 * This class represents a model for the creation of a d3 matrix
 */
class MatrixViewData {
    // Sets of all columns and rows
    Set<String> columnNames
    Set<String> rowNames

    List<MatrixViewEntry> entries

    Double weightMin
    Double weightMax

    // Labels
    String columnLabel
    String rowLabel
    String weightLabel
    String colorDarkLabel
    String colorBrightLabel
    String zeroWeightLabel

    MatrixViewData() {
        entries = new ArrayList<>()

        // Treesets elements are ordered by their natural order
        columnNames = new TreeSet<>()
        rowNames = new TreeSet<>()

        weightMin = Integer.MAX_VALUE
        weightMax = Integer.MIN_VALUE

        columnLabel = "horizontal Label"
        rowLabel = "vertical Label"
        weightLabel = "weight Label"
        colorBrightLabel = "less"
        colorDarkLabel = "more"
        zeroWeightLabel = "0"
    }

    /**
     * Adds an entry to the list of entries and updates affected fields
     * @param entry the entry to add
     */
    void addEntry(MatrixViewEntry entry) {
        columnNames.add(entry.columnName)
        rowNames.add(entry.rowName)


        double entryWeight = entry.weight
        if(entryWeight < weightMin) {
            weightMin = entryWeight
        }
        if(entryWeight > weightMax ) {
            weightMax = entryWeight
        }

        entries.add(entry)
    }

    /**
     * Adds rows to the set of rows
     * @param rows rows to add
     */
    void addRows(Set<String> rows) {
        rowNames.addAll(rows)
    }

    /**
     * Adds columns to the set of columns
     * @param columns columns to add
     */
    void addColumns(Set<String> columns) {
        columnNames.addAll(columns)
    }
}

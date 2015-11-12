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
}

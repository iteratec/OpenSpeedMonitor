package de.iteratec.osm.d3Data

/**
 * This class represents a model for the creation of a d3 treemap
 * A treemap consists of a specified amount of ChartEntries containing a name and a weight
 */
class TreemapData {
    public static final String DEFAULT_ZERO_WEIGHT_LABEL = "Daten mit Gewichtung 0.0"
    public static final String DEFAULT_DATA_NAME = "Name"
    public static final String DEFAULT_WEIGHT_NAME = "Gewichtung"
    final String name = "tree"
    List<ChartEntry> children;

    String zeroWeightLabel
    String dataName
    String weightName

    def TreemapData() {
        children = new ArrayList<>()
        zeroWeightLabel = DEFAULT_ZERO_WEIGHT_LABEL
        dataName = DEFAULT_DATA_NAME
        weightName = DEFAULT_WEIGHT_NAME
    }

    def addNode(ChartEntry node) {
        children.add(node)
    }
}

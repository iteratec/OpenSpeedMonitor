package de.iteratec.osm.d3Data

/**
 * This class represents a model for the creation of a d3 treemap
 * A treemap consists of a specified amount of ChartEntries containing a name and a weight
 */
class TreemapData {
    final String name = "tree"
    List<ChartEntry> children;

    String zeroWeightLabel
    String dataName
    String weightName

    def TreemapData(){
        children = new ArrayList<>()
        zeroWeightLabel = "Daten mit Gewichtung 0.0"
        dataName = "Name"
        weightName = "Gewichtung"
    }

    def addNode(ChartEntry node){
     children.add(node)
    }
}

package de.iteratec.osm.d3Data

/**
 * Created by mmi on 09.10.2015.
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

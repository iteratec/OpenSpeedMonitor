package de.iteratec.osm.d3Data

/**
 * This class represents a model for the creation of a d3 bar chart
 * A bar chart consists of a specified amount of ChartEntries containing a name and a weight
 */
class BarChartData {
    List<ChartEntry> bars;
    String xLabel
    String yLabel

    def BarChartData(){
        bars = new ArrayList<>()
        xLabel = "x-Axis"
        yLabel = "y-Axis"
    }

    def addDatum(ChartEntry c){
        bars.add(c)
    }
}
